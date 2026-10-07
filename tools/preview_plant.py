"""Render the resting marsh wisp plant."""

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RESTING = ROOT / "src" / "main" / "resources" / "assets" / "marshlamp" / "textures" / "block" / "marsh_wisp.png"
OUT = ROOT / "asset"
BG = (18, 32, 28, 255)


def sample(image, u, v):
    w, h = image.size
    x = min(w - 1, max(0, int(u * w)))
    y = min(h - 1, max(0, int(v * h)))
    return image.getpixel((x, y))


def cross_tris(texture):
    # Two full-height planes. Axis-aligned so an isometric view shows both.
    planes = [
        [(0.8, 16, 8), (15.2, 16, 8), (15.2, 0, 8), (0.8, 0, 8)],
        [(8, 16, 0.8), (8, 16, 15.2), (8, 0, 15.2), (8, 0, 0.8)],
    ]
    tris = []
    uv = [(0, 0), (1, 0), (1, 1), (0, 1)]
    for corners in planes:
        centered = [((x - 8) / 16, (y - 8) / 16, (z - 8) / 16) for x, y, z in corners]
        for tri, tri_uv in (
            ((0, 1, 2), (uv[0], uv[1], uv[2])),
            ((0, 2, 3), (uv[0], uv[2], uv[3])),
        ):
            tris.append((
                centered[tri[0]], centered[tri[1]], centered[tri[2]],
                tri_uv[0], tri_uv[1], tri_uv[2], texture,
            ))
    return tris


def raster(tris, size=640, scale=280):
    image = Image.new("RGBA", (size, size), BG)
    pixels = image.load()
    ox, oy = size * 0.5, size * 0.58

    def project(x, y, z):
        sx = (x - z) * 0.8660254 * scale + ox
        sy = (x + z) * 0.5 * scale - y * scale + oy
        depth = x * 0.65 + z * 0.65 + y * 0.4
        return sx, sy, depth

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
    return image


def main():
    resting_tex = Image.open(RESTING).convert("RGBA")
    resting = raster(cross_tris(resting_tex)).resize((320, 320), Image.Resampling.BOX)
    resting.save(OUT / "preview_marsh_wisp.png")
    print("wrote", OUT / "marsh_wisp_resting.png")


if __name__ == "__main__":
    main()
