"""Placeholder marsh ingot, shard copy, and a labeled preview of the new pieces."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src" / "main" / "resources" / "assets" / "marshlamp" / "textures" / "item"
OUT = ROOT / "asset"

DARK = (18, 32, 28, 255)
MID = (56, 86, 78, 255)
LIGHT = (78, 112, 102, 255)
AMBER = (232, 148, 26, 255)
PALE = (239, 190, 152, 255)
EDGE = (36, 58, 52, 255)


def font(size: int):
    for path in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def make_ingot() -> Image.Image:
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    pixels = image.load()
    body = [
        "......######....",
        "....##########..",
        "...############.",
        "..##############",
        "..##############",
        "...############.",
        "....##########..",
        "......######....",
    ]
    for y, row in enumerate(body):
        for x, cell in enumerate(row):
            if cell == "#":
                shade = LIGHT if y < 3 else MID
                if x < 4 or x > 12:
                    shade = EDGE
                pixels[x, y + 4] = shade
    pixels[6, 6] = AMBER
    pixels[7, 6] = PALE
    pixels[8, 6] = AMBER
    pixels[9, 6] = AMBER
    return image


def scale(image: Image.Image, factor: int) -> Image.Image:
    return image.resize((image.width * factor, image.height * factor), Image.Resampling.NEAREST)


def shrine_map() -> Image.Image:
    cell = 28
    size = 11
    image = Image.new("RGBA", (size * cell, size * cell), (24, 42, 36, 255))
    draw = ImageDraw.Draw(image)
    mud = (92, 74, 48, 255)
    moss = (70, 92, 62, 255)
    water = (38, 78, 82, 255)
    flower = (232, 196, 72, 255)
    lamp = (232, 148, 26, 255)
    chest = (148, 98, 48, 255)
    center = size // 2
    for y in range(size):
        for x in range(size):
            dx, dz = x - center, y - center
            dist = max(abs(dx), abs(dz))
            color = water if dist >= 5 else mud
            if dist in (0, 2) and dist <= 2:
                color = moss
            draw.rectangle((x * cell, y * cell, x * cell + cell - 2, y * cell + cell - 2), fill=color)
    marks = {
        (0, 0): lamp,
        (1, 0): flower,
        (-1, 0): flower,
        (0, 1): flower,
        (0, -1): flower,
        (1, 1): flower,
        (-1, -1): flower,
        (3, 1): flower,
        (-3, -1): flower,
        (2, 0): chest,
    }
    for (dx, dz), color in marks.items():
        x = (center + dx) * cell + 6
        y = (center + dz) * cell + 6
        draw.rectangle((x, y, x + cell - 14, y + cell - 14), fill=color)
    return image


def main():
    TEX.mkdir(parents=True, exist_ok=True)
    glass = Image.open(TEX / "marsh_glass.png").convert("RGBA")
    glass.save(TEX / "marsh_shard.png")
    ingot = make_ingot()
    ingot.save(TEX / "marsh_ingot.png")

    sheet = Image.new("RGBA", (980, 420), (18, 32, 28, 255))
    draw = ImageDraw.Draw(sheet)
    title = font(28)
    label = font(22)
    draw.text((24, 16), "新东西预览", fill=(239, 190, 152, 255), font=title)
    sheet.paste(scale(glass, 10), (40, 80), scale(glass, 10))
    draw.text((40, 260), "沼金属碎片", fill=(248, 230, 218, 255), font=label)
    draw.text((40, 292), "沿用沼玻璃的图", fill=(160, 176, 168, 255), font=font(16))
    sheet.paste(scale(ingot, 10), (280, 80), scale(ingot, 10))
    draw.text((280, 260), "沼锭（占位）", fill=(248, 230, 218, 255), font=label)
    draw.text((280, 292), "四个碎片合成一个", fill=(160, 176, 168, 255), font=font(16))
    diagram = shrine_map()
    sheet.paste(diagram, (560, 70))
    draw.text((560, 16), "灯祠俯视", fill=(239, 190, 152, 255), font=title)
    sheet.save(OUT / "render_detail.png")


if __name__ == "__main__":
    main()
