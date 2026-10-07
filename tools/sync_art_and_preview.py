"""Copy the drawn textures into the mod and render previews into asset/preview_*.png."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ASSET = ROOT / "asset"
BLOCK = ROOT / "src" / "main" / "resources" / "assets" / "marshlamp" / "textures" / "block"
ITEM = ROOT / "src" / "main" / "resources" / "assets" / "marshlamp" / "textures" / "item"
BG = (18, 32, 28, 255)


def brighten(image: Image.Image) -> Image.Image:
    out = image.copy()
    pix = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = pix[x, y]
            if a < 8:
                continue
            pix[x, y] = (
                min(255, int(r * 1.4 + 28)),
                min(255, int(g * 1.28 + 16)),
                min(255, int(b * 1.05)),
                a,
            )
    return out


def copy_sources() -> None:
    for name in ("bottled_wisp", "marsh_glass", "marsh_hoe", "marsh_pickaxe", "marsh_axe"):
        image = Image.open(ASSET / f"{name}.png").convert("RGBA")
        if image.size != (16, 16):
            raise SystemExit(f"{name} is {image.size}, expected 16x16")
        image.save(ITEM / f"{name}.png")
    flower = Image.open(ASSET / "marsh_wisp.png").convert("RGBA")
    if flower.size != (16, 16):
        raise SystemExit(f"marsh_wisp is {flower.size}, expected 16x16")
    flower.save(BLOCK / "marsh_wisp.png")
    brighten(flower).save(BLOCK / "marsh_wisp_blooming.png")
    top = Image.open(ASSET / "marsh_lamp_toper.png").convert("RGBA")
    if top.size != (16, 16):
        raise SystemExit(f"lamp top is {top.size}, expected 16x16")
    top.save(BLOCK / "marsh_lamp_topper.png")


def sample(image, u, v):
    w, h = image.size
    x = min(w - 1, max(0, int(u * w)))
    y = min(h - 1, max(0, int(v * h)))
    return image.getpixel((x, y))


def raster(tris, size=640, scale=280, center=(0, 0, 0)):
    image = Image.new("RGBA", (size, size), BG)
    pixels = image.load()
    ox, oy = size * 0.5, size * 0.58
    cx, cy, cz = center

    def project(x, y, z):
        x, y, z = x - cx, y - cy, z - cz
        sx = (x - z) * 0.8660254 * scale + ox
        sy = (x + z) * 0.5 * scale - y * scale + oy
        return sx, sy, x * 0.65 + z * 0.65 + y * 0.4

    prepared = []
    for a, b, c, ua, ub, uc, texture in tris:
        pa, pb, pc = project(*a), project(*b), project(*c)
        prepared.append(((pa[2] + pb[2] + pc[2]) / 3, pa, pb, pc, ua, ub, uc, texture))
    prepared.sort(key=lambda item: item[0])
    zbuf = [[-1e9] * size for _ in range(size)]
    for _, pa, pb, pc, ua, ub, uc, texture in prepared:
        min_x = max(0, int(min(pa[0], pb[0], pc[0])))
        max_x = min(size - 1, int(max(pa[0], pb[0], pc[0])) + 1)
        min_y = max(0, int(min(pa[1], pb[1], pc[1])))
        max_y = min(size - 1, int(max(pa[1], pb[1], pc[1])) + 1)
        denom = (pb[1] - pc[1]) * (pa[0] - pc[0]) + (pc[0] - pb[0]) * (pa[1] - pc[1])
        if abs(denom) < 1e-6:
            continue
        for y in range(min_y, max_y + 1):
            for x in range(min_x, max_x + 1):
                px, py = x + 0.5, y + 0.5
                w0 = ((pb[1] - pc[1]) * (px - pc[0]) + (pc[0] - pb[0]) * (py - pc[1])) / denom
                w1 = ((pc[1] - pa[1]) * (px - pc[0]) + (pa[0] - pc[0]) * (py - pc[1])) / denom
                w2 = 1 - w0 - w1
                if min(w0, w1, w2) < -0.001:
                    continue
                frag = w0 * pa[2] + w1 * pb[2] + w2 * pc[2]
                if frag <= zbuf[y][x]:
                    continue
                color = sample(texture, w0 * ua[0] + w1 * ub[0] + w2 * uc[0], w0 * ua[1] + w1 * ub[1] + w2 * uc[1])
                if color[3] < 16:
                    continue
                zbuf[y][x] = frag
                pixels[x, y] = (*color[:3], 255)
    return image.resize((320, 320), Image.Resampling.BOX)


def cross_tris(texture):
    planes = [
        [(0.8, 16, 8), (15.2, 16, 8), (15.2, 0, 8), (0.8, 0, 8)],
        [(8, 16, 0.8), (8, 16, 15.2), (8, 0, 15.2), (8, 0, 0.8)],
    ]
    uv = [(0, 0), (1, 0), (1, 1), (0, 1)]
    tris = []
    for corners in planes:
        centered = [((x - 8) / 16, (y - 8) / 16, (z - 8) / 16) for x, y, z in corners]
        for ids in ((0, 1, 2), (0, 2, 3)):
            tris.append((centered[ids[0]], centered[ids[1]], centered[ids[2]], uv[ids[0]], uv[ids[1]], uv[ids[2]], texture))
    return tris


def lamp_tris(side, top):
    def face(corners, texture):
        uv = [(0, 0), (1, 0), (1, 1), (0, 1)]
        return [
            (corners[0], corners[1], corners[2], uv[0], uv[1], uv[2], texture),
            (corners[0], corners[2], corners[3], uv[0], uv[2], uv[3], texture),
        ]

    p = lambda x, y, z: (x, y, z)
    tris = []
    tris += face([p(0, 1, 1), p(1, 1, 1), p(1, 1, 0), p(0, 1, 0)], top)
    tris += face([p(0, 0, 0), p(1, 0, 0), p(1, 0, 1), p(0, 0, 1)], side)
    tris += face([p(0, 1, 1), p(0, 0, 1), p(1, 0, 1), p(1, 1, 1)], side)
    tris += face([p(1, 1, 0), p(1, 0, 0), p(0, 0, 0), p(0, 1, 0)], side)
    tris += face([p(1, 1, 1), p(1, 0, 1), p(1, 0, 0), p(1, 1, 0)], side)
    tris += face([p(0, 1, 0), p(0, 0, 0), p(0, 0, 1), p(0, 1, 1)], side)
    return tris


def font(size):
    for path in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def main():
    copy_sources()
    dim = raster(cross_tris(Image.open(BLOCK / "marsh_wisp.png").convert("RGBA")))
    lit = raster(cross_tris(Image.open(BLOCK / "marsh_wisp_blooming.png").convert("RGBA")))
    lamp = raster(
        lamp_tris(
            Image.open(BLOCK / "marsh_lamp.png").convert("RGBA"),
            Image.open(BLOCK / "marsh_lamp_topper.png").convert("RGBA"),
        ),
        scale=250,
        center=(0.5, 0.5, 0.5),
    )
    dim.save(ASSET / "render_wisp_off.png")
    lit.save(ASSET / "render_wisp_on.png")
    lamp.save(ASSET / "render_lamp.png")
    old = ASSET / "marsh_wisp_resting.png"
    if old.exists():
        old.unlink()

    sheet = Image.new("RGBA", (1020, 400), (28, 48, 42, 255))
    sheet.alpha_composite(dim, (20, 16))
    sheet.alpha_composite(lit, (350, 16))
    sheet.alpha_composite(lamp, (680, 16))
    draw = ImageDraw.Draw(sheet)
    label = font(26)
    for text, x in (("沼灯芯，等待恢复", 20), ("沼灯芯，可收获", 350), ("沼灯", 680)):
        box = draw.textbbox((0, 0), text, font=label)
        draw.text((x + (320 - (box[2] - box[0])) / 2, 348), text, fill=(246, 231, 193, 255), font=label)
    sheet.save(ASSET / "render_sheet.png")
    print("previews ready")


if __name__ == "__main__":
    main()
