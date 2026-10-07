"""Build a hanging cage lantern model and refresh the overview sheet."""

import importlib.util
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src" / "main" / "resources" / "assets" / "marshlamp" / "textures"
MODEL = ROOT / "src" / "main" / "resources" / "assets" / "marshlamp" / "models" / "item" / "marsh_lantern.json"

METAL = (56, 86, 78, 255)
METAL_DARK = (36, 58, 52, 255)
CHAIN = (18, 32, 28, 255)
GLOW = (232, 148, 26, 255)
GLOW_HOT = (248, 214, 140, 255)


def solid(path: Path, color, edge=None) -> None:
    image = Image.new("RGBA", (16, 16), color)
    if edge is not None:
        pix = image.load()
        for i in range(16):
            pix[i, 0] = edge
            pix[i, 15] = edge
            pix[0, i] = edge
            pix[15, i] = edge
    image.save(path)


def face(name: str) -> dict:
    return {"uv": [0, 0, 16, 16], "texture": name}


def element(box, texture: str, shade: bool = True) -> dict:
    x0, y0, z0, x1, y1, z1 = box
    return {
        "from": [x0, y0, z0],
        "to": [x1, y1, z1],
        "shade": shade,
        "faces": {side: face(texture) for side in ("down", "up", "north", "south", "west", "east")},
    }


def lantern_boxes():
    boxes = []
    for x in (4.0, 10.6):
        for z in (4.0, 10.6):
            boxes.append(((x, 0.0, z, x + 1.4, 0.7, z + 1.4), "#chain"))
    boxes.append(((3.5, 0.7, 3.5, 12.5, 1.9, 12.5), "#metal"))
    boxes.append(((4.3, 1.9, 4.3, 11.7, 2.5, 11.7), "#chain"))
    posts = [
        (4.2, 2.5, 4.2, 5.3, 10.8, 5.3),
        (10.7, 2.5, 4.2, 11.8, 10.8, 5.3),
        (4.2, 2.5, 10.7, 5.3, 10.8, 11.8),
        (10.7, 2.5, 10.7, 11.8, 10.8, 11.8),
    ]
    boxes.extend((post, "#chain") for post in posts)

    def ring(y0, y1, texture):
        boxes.append(((5.3, y0, 4.2, 10.7, y1, 5.3), texture))
        boxes.append(((5.3, y0, 10.7, 10.7, y1, 11.8), texture))
        boxes.append(((4.2, y0, 5.3, 5.3, y1, 10.7), texture))
        boxes.append(((10.7, y0, 5.3, 11.8, y1, 10.7), texture))

    ring(3.1, 3.8, "#metal")
    ring(6.3, 7.0, "#metal")
    ring(9.6, 10.3, "#chain")
    boxes.append(((5.8, 3.4, 5.8, 10.2, 10.2, 10.2), "#glow"))
    boxes.append(((7.0, 4.6, 7.0, 9.0, 9.0, 9.0), "#core"))
    boxes.append(((3.1, 10.8, 3.1, 12.9, 11.8, 12.9), "#metal"))
    boxes.append(((4.2, 11.8, 4.2, 11.8, 12.6, 11.8), "#chain"))
    for x in (3.2, 11.5):
        for z in (3.2, 11.5):
            boxes.append(((x, 11.6, z, x + 1.2, 12.5, z + 1.2), "#chain"))
    boxes.append(((6.9, 12.6, 6.9, 9.1, 13.5, 9.1), "#chain"))
    boxes.append(((7.25, 13.7, 7.25, 8.75, 14.3, 8.75), "#chain"))
    boxes.append(((7.4, 14.6, 7.4, 8.6, 15.1, 8.6), "#chain"))
    boxes.append(((7.15, 14.9, 4.6, 8.85, 15.9, 8.6), "#chain"))
    boxes.append(((7.15, 13.2, 4.6, 8.85, 15.1, 5.9), "#chain"))
    boxes.append(((7.0, 15.7, 7.0, 9.0, 16.0, 9.0), "#metal"))
    for x0 in (2.5, 12.3):
        boxes.append(((x0, 11.4, 7.3, x0 + 1.2, 12.5, 8.7), "#chain"))
        boxes.append(((x0 + 0.2, 10.2, 7.5, x0 + 1.0, 11.1, 8.5), "#chain"))
        boxes.append(((x0, 9.0, 7.3, x0 + 1.2, 9.9, 8.7), "#chain"))
    return boxes


def write_model() -> None:
    TEX.joinpath("item").mkdir(parents=True, exist_ok=True)
    solid(TEX / "item" / "lantern_metal.png", METAL, METAL_DARK)
    solid(TEX / "item" / "lantern_chain.png", CHAIN, (4, 9, 7, 255))
    glow = Image.new("RGBA", (16, 16), GLOW)
    pix = glow.load()
    for y in range(4, 12):
        for x in range(4, 12):
            pix[x, y] = GLOW_HOT
    glow.save(TEX / "item" / "lantern_glow.png")
    core = Image.new("RGBA", (16, 16), (255, 236, 190, 255))
    core.save(TEX / "item" / "lantern_core.png")

    model = {
        "textures": {
            "particle": "marshlamp:item/lantern_metal",
            "metal": "marshlamp:item/lantern_metal",
            "chain": "marshlamp:item/lantern_chain",
            "glow": "marshlamp:item/lantern_glow",
            "core": "marshlamp:item/lantern_core",
        },
        "elements": [element(box, texture, shade=texture not in ("#glow", "#core")) for box, texture in lantern_boxes()],
        "display": {
            "thirdperson_righthand": {"rotation": [70, 45, 0], "translation": [0, 2.5, 1], "scale": [0.45, 0.45, 0.45]},
            "thirdperson_lefthand": {"rotation": [70, 45, 0], "translation": [0, 2.5, 1], "scale": [0.45, 0.45, 0.45]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 3.5, 0], "scale": [0.5, 0.5, 0.5]},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 3.5, 0], "scale": [0.5, 0.5, 0.5]},
            "ground": {"translation": [0, 3, 0], "scale": [0.35, 0.35, 0.35]},
            "gui": {"rotation": [30, 225, 0], "scale": [0.8, 0.8, 0.8]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.6, 0.6, 0.6]},
        },
    }
    MODEL.write_text(json.dumps(model, indent=2), encoding="utf-8")


def lantern_preview():
    spec = importlib.util.spec_from_file_location("sync", ROOT / "tools" / "sync_art_and_preview.py")
    sync = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(sync)
    textures = {
        "#metal": Image.open(TEX / "item" / "lantern_metal.png").convert("RGBA"),
        "#chain": Image.open(TEX / "item" / "lantern_chain.png").convert("RGBA"),
        "#glow": Image.open(TEX / "item" / "lantern_glow.png").convert("RGBA"),
        "#core": Image.open(TEX / "item" / "lantern_core.png").convert("RGBA"),
    }
    tris = []

    def add_box(box, texture):
        x0, y0, z0, x1, y1, z1 = [value / 16 for value in box]
        corners = {
            "up": [(x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)],
            "down": [(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)],
            "south": [(x0, y1, z1), (x0, y0, z1), (x1, y0, z1), (x1, y1, z1)],
            "north": [(x1, y1, z0), (x1, y0, z0), (x0, y0, z0), (x0, y1, z0)],
            "east": [(x1, y1, z1), (x1, y0, z1), (x1, y0, z0), (x1, y1, z0)],
            "west": [(x0, y1, z0), (x0, y0, z0), (x0, y0, z1), (x0, y1, z1)],
        }
        uv = [(0, 0), (1, 0), (1, 1), (0, 1)]
        for quad in corners.values():
            tris.append((quad[0], quad[1], quad[2], uv[0], uv[1], uv[2], texture))
            tris.append((quad[0], quad[2], quad[3], uv[0], uv[2], uv[3], texture))

    for box, key in lantern_boxes():
        add_box(box, textures[key])
    return sync.raster(tris, scale=250, center=(0.5, 0.55, 0.5))


def card(path: Path):
    tex = Image.open(path).convert("RGBA")
    icon = tex.resize((tex.width * 12, tex.height * 12), Image.Resampling.NEAREST)
    plate = Image.new("RGBA", (320, 320), (18, 32, 28, 255))
    x = (320 - icon.width) // 2
    y = (320 - icon.height) // 2
    shadow = Image.new("RGBA", plate.size, (0, 0, 0, 0))
    shadow.paste((0, 0, 0, 70), (x + 6, y + 8), icon.getchannel("A"))
    plate.alpha_composite(shadow)
    plate.paste(icon, (x, y), icon)
    return plate


def write_sheet(lantern: Image.Image) -> None:
    spec = importlib.util.spec_from_file_location("sync", ROOT / "tools" / "sync_art_and_preview.py")
    sync = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(sync)
    block = TEX / "block"
    item = TEX / "item"
    flower = Image.open(block / "marsh_wisp.png").convert("RGBA")
    bloom = Image.open(block / "marsh_wisp_blooming.png").convert("RGBA")
    side = Image.open(block / "marsh_lamp.png").convert("RGBA")
    top = Image.open(block / "marsh_lamp_topper.png").convert("RGBA")
    images = [
        ("沼灯芯，等待恢复", sync.raster(sync.cross_tris(flower))),
        ("沼灯芯，可收获", sync.raster(sync.cross_tris(bloom))),
        ("沼灯", sync.raster(sync.lamp_tris(side, top), scale=250, center=(0.5, 0.5, 0.5))),
        ("手提沼灯", lantern),
        ("灯芯", card(item / "bottled_wisp.png")),
        ("沼玻璃", card(item / "marsh_glass.png")),
        ("沼锄", card(item / "marsh_hoe.png")),
        ("沼镐", card(item / "marsh_pickaxe.png")),
        ("沼斧", card(item / "marsh_axe.png")),
    ]
    cols, cell, label_h = 3, 340, 44
    sheet = Image.new("RGBA", (cols * cell, 3 * (320 + label_h) + 28), (28, 48, 42, 255))
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype(r"C:\Windows\Fonts\msyh.ttc", 26)
    for index, (text, image) in enumerate(images):
        col, row = index % cols, index // cols
        x = col * cell + (cell - 320) // 2
        y = 16 + row * (320 + label_h)
        sheet.alpha_composite(image, (x, y))
        box = draw.textbbox((0, 0), text, font=font)
        draw.text((col * cell + (cell - (box[2] - box[0])) / 2, y + 324), text, fill=(246, 231, 193, 255), font=font)
    sheet.save(ROOT / "asset" / "render_all.png")
    lantern.save(ROOT / "asset" / "render_lantern.png")


if __name__ == "__main__":
    write_model()
    write_sheet(lantern_preview())
    print("lantern model ready")
