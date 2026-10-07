"""Read a Minecraft structure .nbt and draw an isometric preview."""

from __future__ import annotations

import gzip
import struct
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "asset" / "marsh_shrine.nbt"
OUT = ROOT / "asset" / "render_shrine.png"

COLORS = {
    "minecraft:air": None,
    "minecraft:cave_air": None,
    "minecraft:void_air": None,
    "minecraft:structure_void": None,
    "minecraft:mud": (110, 84, 58),
    "minecraft:dirt": (134, 96, 67),
    "minecraft:grass_block": (92, 140, 62),
    "minecraft:moss_block": (78, 120, 70),
    "minecraft:mossy_cobblestone": (96, 120, 86),
    "minecraft:cobblestone": (120, 120, 120),
    "minecraft:stone": (125, 125, 125),
    "minecraft:water": (48, 92, 130),
    "minecraft:chest": (168, 114, 52),
    "minecraft:trapped_chest": (148, 90, 48),
    "minecraft:mangrove_log": (92, 58, 42),
    "minecraft:mangrove_wood": (86, 54, 40),
    "minecraft:stripped_mangrove_log": (122, 78, 58),
    "minecraft:mangrove_leaves": (48, 110, 58),
    "minecraft:mangrove_roots": (78, 56, 40),
    "minecraft:muddy_mangrove_roots": (72, 64, 46),
    "minecraft:vine": (42, 96, 48),
    "minecraft:oak_log": (110, 86, 52),
    "minecraft:oak_leaves": (58, 120, 48),
    "marshlamp:marsh_lamp": (214, 140, 36),
    "marshlamp:marsh_wisp": (210, 190, 70),
    "marshlamp:marsh_sprout": (70, 150, 62),
    "marshlamp:marsh_glow": (232, 180, 70),
}


def _read(data: bytes, offset: int, fmt: str):
    size = struct.calcsize(fmt)
    return struct.unpack_from(fmt, data, offset)[0], offset + size


def parse_payload(data: bytes, offset: int, tag_type: int):
    if tag_type == 1:
        return _read(data, offset, ">b")
    if tag_type == 2:
        return _read(data, offset, ">h")
    if tag_type == 3:
        return _read(data, offset, ">i")
    if tag_type == 4:
        return _read(data, offset, ">q")
    if tag_type == 5:
        return _read(data, offset, ">f")
    if tag_type == 6:
        return _read(data, offset, ">d")
    if tag_type == 7:
        length, offset = _read(data, offset, ">i")
        return data[offset:offset + length], offset + length
    if tag_type == 8:
        length, offset = _read(data, offset, ">H")
        return data[offset:offset + length].decode("utf-8"), offset + length
    if tag_type == 9:
        inner, offset = _read(data, offset, ">b")
        length, offset = _read(data, offset, ">i")
        values = []
        for _ in range(length):
            value, offset = parse_payload(data, offset, inner)
            values.append(value)
        return values, offset
    if tag_type == 10:
        compound = {}
        while True:
            child, offset = _read(data, offset, ">b")
            if child == 0:
                return compound, offset
            name, offset = parse_payload(data, offset, 8)
            value, offset = parse_payload(data, offset, child)
            compound[name] = value
    if tag_type == 11:
        length, offset = _read(data, offset, ">i")
        values = list(struct.unpack_from(f">{length}i", data, offset))
        return values, offset + 4 * length
    if tag_type == 12:
        length, offset = _read(data, offset, ">i")
        values = list(struct.unpack_from(f">{length}q", data, offset))
        return values, offset + 8 * length
    raise ValueError(f"unknown tag {tag_type}")


def parse_root(raw: bytes):
    tag_type, offset = _read(raw, 0, ">b")
    if tag_type != 10:
        raise ValueError(f"root tag is {tag_type}")
    _, offset = parse_payload(raw, offset, 8)
    compound, _ = parse_payload(raw, offset, 10)
    return compound


def block_name(entry) -> str:
    if isinstance(entry, str):
        return entry
    if isinstance(entry, dict):
        name = entry.get("Name") or entry.get("name")
        if isinstance(name, str):
            return name
    return str(entry)


def color_for(name: str):
    if name in COLORS:
        return COLORS[name]
    if name.endswith("air") or name.endswith("structure_void"):
        return None
    hue = sum(name.encode("utf-8")) % 200
    return (40 + hue % 80, 70 + (hue * 3) % 90, 50 + (hue * 5) % 80)


def project(x, y, z, scale, origin_x, origin_y):
    sx = (x - z) * 0.866 * scale + origin_x
    sy = (x + z) * 0.5 * scale - y * scale + origin_y
    return sx, sy


def draw_block(draw: ImageDraw.ImageDraw, x, y, z, color, scale, origin_x, origin_y):
    top = tuple(min(255, c + 36) for c in color)
    left = tuple(max(0, c - 18) for c in color)
    right = color
    corners = {
        "t0": project(x, y + 1, z, scale, origin_x, origin_y),
        "t1": project(x + 1, y + 1, z, scale, origin_x, origin_y),
        "t2": project(x + 1, y + 1, z + 1, scale, origin_x, origin_y),
        "t3": project(x, y + 1, z + 1, scale, origin_x, origin_y),
        "b1": project(x + 1, y, z, scale, origin_x, origin_y),
        "b2": project(x + 1, y, z + 1, scale, origin_x, origin_y),
        "b3": project(x, y, z + 1, scale, origin_x, origin_y),
    }
    draw.polygon([corners["t0"], corners["t1"], corners["t2"], corners["t3"]], fill=top)
    draw.polygon([corners["t1"], corners["b1"], corners["b2"], corners["t2"]], fill=right)
    draw.polygon([corners["t3"], corners["t2"], corners["b2"], corners["b3"]], fill=left)


def main():
    raw = gzip.decompress(SRC.read_bytes())
    root = parse_root(raw)
    print("keys", list(root))
    size = root.get("size")
    palette = root.get("palette") or root.get("palettes")
    blocks = root.get("blocks") or []
    print("size", size, "blocks", len(blocks), "palette", len(palette) if palette else None)
    if isinstance(palette, list) and palette and isinstance(palette[0], list):
        palette = palette[0]
    names = [block_name(entry) for entry in palette]
    for index, name in enumerate(names):
        print(f"  {index}: {name}")

    occupied = []
    for block in blocks:
        state = block["state"]
        pos = block["pos"]
        name = names[state]
        color = color_for(name)
        if color is None:
            continue
        occupied.append((pos[0], pos[1], pos[2], color, name))
    if not occupied:
        raise SystemExit("no visible blocks")

    xs = [item[0] for item in occupied]
    ys = [item[1] for item in occupied]
    zs = [item[2] for item in occupied]
    width = max(xs) - min(xs) + 1
    depth = max(zs) - min(zs) + 1
    height = max(ys) - min(ys) + 1
    print("span", width, height, depth)

    scale = max(8, min(28, int(640 / max(width + depth, height + 2))))
    image = Image.new("RGBA", (1100, 760), (18, 32, 28, 255))
    draw = ImageDraw.Draw(image)
    origin_x = 520
    origin_y = 180 + height * scale
    for x, y, z, color, _name in sorted(occupied, key=lambda item: (item[0] + item[2], item[1])):
        draw_block(draw, x, y, z, color, scale, origin_x, origin_y)

    font = ImageFont.truetype(r"C:\Windows\Fonts\msyh.ttc", 28)
    small = ImageFont.truetype(r"C:\Windows\Fonts\msyh.ttc", 18)
    draw.text((24, 18), "灯祠结构", fill=(239, 190, 152, 255), font=font)
    draw.text((24, 58), f"{width} × {height} × {depth} 格，{len(occupied)} 个可见方块", fill=(200, 210, 200, 255), font=small)
    image.save(OUT)
    print("wrote", OUT)


if __name__ == "__main__":
    main()
