"""Placeholder art and a labeled preview for marsh sprout wheat."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
BLOCK = ROOT / "src" / "main" / "resources" / "assets" / "marshlamp" / "textures" / "block"
ITEM = ROOT / "src" / "main" / "resources" / "assets" / "marshlamp" / "textures" / "item"
OUT = ROOT / "asset"

DARK = (18, 32, 28, 255)
MID = (56, 86, 78, 255)
LEAF = (78, 112, 102, 255)
AMBER = (232, 148, 26, 255)
PALE = (239, 190, 152, 255)
STEM = (36, 58, 52, 255)


def blank():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def put(image, pixels):
    pix = image.load()
    for x, y, color in pixels:
        if 0 <= x < 16 and 0 <= y < 16:
            pix[x, y] = color
    return image


def sprout(age: int) -> Image.Image:
    image = blank()
    pix = image.load()
    height = 4 + age * 4
    for y in range(16 - height, 16):
        pix[7, y] = STEM
        pix[8, y] = MID
    for y in range(16 - height, 16 - height + 3):
        pix[6, y] = LEAF
        pix[9, y] = LEAF
    if age == 2:
        for x in range(5, 11):
            pix[x, 16 - height] = AMBER
            pix[x, 16 - height + 1] = PALE if x in (6, 8) else AMBER
    return image


def seeds() -> Image.Image:
    image = blank()
    return put(image, [
        (6, 8, MID), (7, 7, LEAF), (8, 8, AMBER), (9, 9, MID),
        (5, 10, STEM), (10, 7, LEAF), (7, 11, PALE), (9, 6, AMBER),
    ])


def wheat() -> Image.Image:
    image = blank()
    pix = image.load()
    for y in range(6, 15):
        pix[7, y] = STEM
        pix[8, y] = MID
    for x in range(4, 12):
        pix[x, 4] = AMBER
        pix[x, 5] = PALE if x % 2 == 0 else AMBER
    pix[5, 7] = LEAF
    pix[10, 8] = LEAF
    return image


def bread() -> Image.Image:
    image = blank()
    pix = image.load()
    body = [
        "....########....",
        "...##########...",
        "..############..",
        "..############..",
        "...##########...",
        "....########....",
    ]
    for row, line in enumerate(body):
        for col, cell in enumerate(line):
            if cell == "#":
                color = PALE if row == 0 else (AMBER if row < 2 else MID)
                pix[col, row + 5] = color
    return image


def font(size: int):
    return ImageFont.truetype(r"C:\Windows\Fonts\msyh.ttc", size)


def card(texture: Image.Image, label: str) -> Image.Image:
    scale = 8
    icon = texture.resize((texture.width * scale, texture.height * scale), Image.Resampling.NEAREST)
    image = Image.new("RGBA", (180, 210), (18, 32, 28, 255))
    image.alpha_composite(icon, ((180 - icon.width) // 2, 16))
    draw = ImageDraw.Draw(image)
    face = font(18)
    bbox = draw.textbbox((0, 0), label, font=face)
    draw.text(((180 - (bbox[2] - bbox[0])) / 2, 160), label, fill=(246, 231, 193, 255), font=face)
    return image


def main():
    BLOCK.mkdir(parents=True, exist_ok=True)
    ITEM.mkdir(parents=True, exist_ok=True)
    stages = [sprout(0), sprout(1), sprout(2)]
    for age, image in enumerate(stages):
        image.save(BLOCK / f"marsh_sprout_{age}.png")
    seed = seeds()
    grain = wheat()
    loaf = bread()
    seed.save(ITEM / "marsh_sprout_seeds.png")
    grain.save(ITEM / "marsh_wheat.png")
    loaf.save(ITEM / "marsh_bread.png")

    labels = ["幼苗", "生长", "成熟", "种子", "沼芽麦", "软面包"]
    images = stages + [seed, grain, loaf]
    sheet = Image.new("RGBA", (180 * 6, 250), (18, 32, 28, 255))
    title = font(22)
    draw = ImageDraw.Draw(sheet)
    draw.text((16, 8), "沼芽麦占位", fill=(239, 190, 152, 255), font=title)
    for index, (label, texture) in enumerate(zip(labels, images)):
        sheet.alpha_composite(card(texture, label), (index * 180, 40))
    sheet.save(OUT / "render_sprout.png")


if __name__ == "__main__":
    main()
