"""Render catalog previews of the Marshlamp models and items into ./asset."""

from __future__ import annotations

import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src" / "main" / "resources" / "assets" / "marshlamp" / "textures"
OUT = ROOT / "asset"
CLIENT_JAR = Path.home() / ".gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar"

BG = (18, 32, 28, 255)
PAPER = (28, 48, 42, 255)


def load_font(size: int) -> ImageFont.ImageFont:
    for path in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\msyhbd.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def sample(image: Image.Image, u: float, v: float):
    width, height = image.size
    x = min(width - 1, max(0, int(u * width)))
    y = min(height - 1, max(0, int(v * height)))
    return image.getpixel((x, y))


def project(x: float, y: float, z: float, scale: float, origin_x: float, origin_y: float):
    sx = (x - z) * 0.8660254 * scale + origin_x
    sy = (x + z) * 0.5 * scale - y * scale + origin_y
    depth = x * 0.65 + z * 0.65 + y * 0.4
    return sx, sy, depth


def add_quad(tris, corners, u0, v0, u1, v1, tex_w, tex_h, texture):
    uv = [
        (u0 / tex_w, v0 / tex_h),
        (u1 / tex_w, v0 / tex_h),
        (u1 / tex_w, v1 / tex_h),
        (u0 / tex_w, v1 / tex_h),
    ]
    tris.append((corners[0], corners[1], corners[2], uv[0], uv[1], uv[2], texture))
    tris.append((corners[0], corners[2], corners[3], uv[0], uv[2], uv[3], texture))


def minecraft_box(tris, origin, size, tex_u, tex_v, tex_w, tex_h, texture):
    """Box UV layout used by Minecraft's Cube."""
    x, y, z = origin
    dx, dy, dz = size
    x0, y0, z0 = x, y, z
    x1, y1, z1 = x + dx, y + dy, z + dz
    u0 = tex_u
    u1 = u0 + dz
    u2 = u1 + dx
    u3 = u2 + dz
    u4 = u3 + dx
    v0 = tex_v
    v1 = v0 + dz
    v2 = v1 + dy
    # (x, y, z) corners named by min/max
    def p(px, py, pz):
        return (px, py, pz)

    # DOWN -Y
    add_quad(tris, [p(x1, y0, z1), p(x0, y0, z1), p(x0, y0, z0), p(x1, y0, z0)], u2, v0, u3, v1, tex_w, tex_h, texture)
    # UP +Y
    add_quad(tris, [p(x1, y1, z0), p(x0, y1, z0), p(x0, y1, z1), p(x1, y1, z1)], u1, v0, u2, v1, tex_w, tex_h, texture)
    # WEST -X
    add_quad(tris, [p(x0, y1, z0), p(x0, y0, z0), p(x0, y0, z1), p(x0, y1, z1)], u0, v1, u1, v2, tex_w, tex_h, texture)
    # EAST +X
    add_quad(tris, [p(x1, y1, z1), p(x1, y0, z1), p(x1, y0, z0), p(x1, y1, z0)], u2, v1, u3, v2, tex_w, tex_h, texture)
    # NORTH -Z
    add_quad(tris, [p(x0, y1, z0), p(x0, y0, z0), p(x1, y0, z0), p(x1, y1, z0)], u3, v1, u4, v2, tex_w, tex_h, texture)
    # SOUTH +Z
    add_quad(tris, [p(x1, y1, z1), p(x1, y0, z1), p(x0, y0, z1), p(x0, y1, z1)], u1, v1, u2, v2, tex_w, tex_h, texture)


def block_cube(tris, texture: Image.Image):
    tw, th = texture.size
    x0 = y0 = z0 = 0.0
    x1 = y1 = z1 = 1.0

    def p(x, y, z):
        return (x, y, z)

    # Full-cube face UVs, v grows downward on the texture.
    add_quad(tris, [p(0, 1, 1), p(1, 1, 1), p(1, 1, 0), p(0, 1, 0)], 0, 0, tw, th, tw, th, texture)  # up
    add_quad(tris, [p(0, 0, 0), p(1, 0, 0), p(1, 0, 1), p(0, 0, 1)], 0, 0, tw, th, tw, th, texture)  # down
    add_quad(tris, [p(0, 1, 1), p(0, 0, 1), p(1, 0, 1), p(1, 1, 1)], 0, 0, tw, th, tw, th, texture)  # south
    add_quad(tris, [p(1, 1, 0), p(1, 0, 0), p(0, 0, 0), p(0, 1, 0)], 0, 0, tw, th, tw, th, texture)  # north
    add_quad(tris, [p(1, 1, 1), p(1, 0, 1), p(1, 0, 0), p(1, 1, 0)], 0, 0, tw, th, tw, th, texture)  # east
    add_quad(tris, [p(0, 1, 0), p(0, 0, 0), p(0, 0, 1), p(0, 1, 1)], 0, 0, tw, th, tw, th, texture)  # west
    return x0, y0, z0, x1, y1, z1


def raster(tris, size=640, scale=220, center=None, background=BG):
    image = Image.new("RGBA", (size, size), background)
    pixels = image.load()
    if center is None:
        center = (0.5, 0.5, 0.5)
    cx, cy, cz = center
    origin_x = size * 0.5
    origin_y = size * 0.58
    prepared = []
    for a, b, c, ua, ub, uc, texture in tris:
        pa = project(a[0] - cx, a[1] - cy, a[2] - cz, scale, origin_x, origin_y)
        pb = project(b[0] - cx, b[1] - cy, b[2] - cz, scale, origin_x, origin_y)
        pc = project(c[0] - cx, c[1] - cy, c[2] - cz, scale, origin_x, origin_y)
        depth = (pa[2] + pb[2] + pc[2]) / 3
        prepared.append((depth, pa, pb, pc, ua, ub, uc, texture))
    prepared.sort(key=lambda item: item[0])
    zbuf = [[-1e9] * size for _ in range(size)]
    for depth, pa, pb, pc, ua, ub, uc, texture in prepared:
        pts = [(pa[0], pa[1], ua), (pb[0], pb[1], ub), (pc[0], pc[1], uc)]
        min_x = max(0, int(min(p[0] for p in pts)))
        max_x = min(size - 1, int(max(p[0] for p in pts)) + 1)
        min_y = max(0, int(min(p[1] for p in pts)))
        max_y = min(size - 1, int(max(p[1] for p in pts)) + 1)
        ax, ay = pa[0], pa[1]
        bx, by = pb[0], pb[1]
        cx_, cy_ = pc[0], pc[1]
        denom = (by - cy_) * (ax - cx_) + (cx_ - bx) * (ay - cy_)
        if abs(denom) < 1e-6:
            continue
        for y in range(min_y, max_y + 1):
            for x in range(min_x, max_x + 1):
                px = x + 0.5
                py = y + 0.5
                w0 = ((by - cy_) * (px - cx_) + (cx_ - bx) * (py - cy_)) / denom
                w1 = ((cy_ - ay) * (px - cx_) + (ax - cx_) * (py - cy_)) / denom
                w2 = 1 - w0 - w1
                if w0 < 0 or w1 < 0 or w2 < 0:
                    continue
                frag_depth = w0 * pa[2] + w1 * pb[2] + w2 * pc[2]
                if frag_depth <= zbuf[y][x]:
                    continue
                u = w0 * ua[0] + w1 * ub[0] + w2 * uc[0]
                v = w0 * ua[1] + w1 * ub[1] + w2 * uc[1]
                r, g, b, a = sample(texture, u, v)
                if a < 16:
                    continue
                zbuf[y][x] = frag_depth
                pixels[x, y] = (r, g, b, 255)
    return image


def item_card(texture: Image.Image, size=320) -> Image.Image:
    card = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    scale = 14
    icon = texture.resize((texture.width * scale, texture.height * scale), Image.Resampling.NEAREST)
    x = (size - icon.width) // 2
    y = (size - icon.height) // 2 + 4
    shadow = Image.new("RGBA", card.size, (0, 0, 0, 0))
    shadow.paste(icon, (x + 8, y + 10), icon)
    tint = Image.new("RGBA", card.size, (0, 0, 0, 90))
    shadow = Image.composite(tint, Image.new("RGBA", card.size, (0, 0, 0, 0)), shadow.getchannel("A"))
    card.alpha_composite(shadow)
    card.alpha_composite(icon, (x, y))
    plate = Image.new("RGBA", (size, size), BG)
    plate.alpha_composite(card)
    return plate


def tint(image: Image.Image, color: int) -> Image.Image:
    red, green, blue = (color >> 16) & 255, (color >> 8) & 255, color & 255
    out = image.copy()
    pix = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = pix[x, y]
            pix[x, y] = (r * red // 255, g * green // 255, b * blue // 255, a)
    return out


def spawn_egg() -> Image.Image:
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        base = Image.open(jar.open("assets/minecraft/textures/item/spawn_egg.png")).convert("RGBA")
        overlay = Image.open(jar.open("assets/minecraft/textures/item/spawn_egg_overlay.png")).convert("RGBA")
    egg = Image.new("RGBA", base.size, (0, 0, 0, 0))
    egg.alpha_composite(tint(base, 0xF6E7C1))
    egg.alpha_composite(tint(overlay, 0x1F6F78))
    return egg


def labeled_sheet(entries: list[tuple[str, Image.Image]]) -> Image.Image:
    font = load_font(28)
    cols = 3
    cell = 360
    label_h = 48
    rows = (len(entries) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * cell, rows * (cell + label_h) + 24), PAPER)
    draw = ImageDraw.Draw(sheet)
    for index, (label, image) in enumerate(entries):
        col = index % cols
        row = index // cols
        x = col * cell + (cell - image.width) // 2
        y = 16 + row * (cell + label_h)
        sheet.alpha_composite(image.resize((320, 320), Image.Resampling.NEAREST), (col * cell + 20, y))
        bbox = draw.textbbox((0, 0), label, font=font)
        text_w = bbox[2] - bbox[0]
        draw.text((col * cell + (cell - text_w) / 2, y + 324), label, fill=(246, 231, 193, 255), font=font)
    return sheet


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    lamp_tex = Image.open(TEX / "block" / "marsh_lamp.png").convert("RGBA")
    items = {
        "bottled_wisp": Image.open(TEX / "item" / "bottled_wisp.png").convert("RGBA"),
        "marsh_glass": Image.open(TEX / "item" / "marsh_glass.png").convert("RGBA"),
    }

    lamp_tris = []
    block_cube(lamp_tris, lamp_tex)
    lamp = raster(lamp_tris, size=640, scale=250, center=(0.5, 0.5, 0.5))
    lamp_preview = lamp.resize((320, 320), Image.Resampling.BOX)
    lamp_preview.save(OUT / "marsh_lamp.png")

    cards = {}
    for key, texture in items.items():
        card = item_card(texture)
        card.save(OUT / f"{key}.png")
        cards[key] = card

    print("wrote", OUT)


if __name__ == "__main__":
    main()
