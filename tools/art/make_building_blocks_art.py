"""Generates door and trapdoor art for the Stage 1 building-block variants.

Run from the project root:
    python tools/art/make_building_blocks_art.py

Reads the existing crystal_canopy_planks.png and emberwood_planks.png and derives:
- block/<wood>_door_top.png     (cutout windows)
- block/<wood>_door_bottom.png
- block/<wood>_trapdoor.png     (cutout windows)
- item/<wood>_door.png          (flat icon, windows filled in)

Stairs, slabs, fences, gates and walls reuse the plank texture.
"""
from pathlib import Path
from PIL import Image

BLOCK_TEX = Path("src/main/resources/assets/artilian/textures/block")
ITEM_TEX = Path("src/main/resources/assets/artilian/textures/item")

WOODS = ("crystal_canopy", "emberwood")

# Window cutouts, kept inside the 1px frame.
TOP_WINDOWS = {(4, 3), (5, 3), (4, 4), (5, 4), (10, 8), (11, 8), (10, 9), (11, 9)}
TRAPDOOR_WINDOWS = {(5, 5), (6, 5), (5, 6), (6, 6), (10, 9), (11, 9), (10, 10), (11, 10)}


def put(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def shade(color, factor):
    r, g, b, a = color
    return (max(0, int(r * factor)), max(0, int(g * factor)), max(0, int(b * factor)), a)


def plank_grain(planks, x, y):
    """Transpose the planks so their horizontal seams read as vertical grain."""
    return planks.getpixel((y, x))


def door_panel(planks):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            color = plank_grain(planks, x, y)
            if x in (0, 15) or y in (0, 15):
                color = shade(color, 0.45)  # darker 1px frame
            elif x in (4, 8, 12) or (x in (2, 6, 10, 14) and y % 5 in (1, 2)):
                color = shade(color, 0.7)  # vertical plank seam / grain
            elif y in (1, 14):
                color = shade(color, 0.85)
            put(img, x, y, color)
    return img


def make_door_top(planks):
    img = door_panel(planks)
    for x, y in TOP_WINDOWS:
        put(img, x, y, (0, 0, 0, 0))
    return img


def make_door_bottom(planks):
    img = door_panel(planks)
    for y in (13, 14):
        for x in range(1, 15):
            put(img, x, y, shade(img.getpixel((x, y)), 0.6))
    return img


def make_trapdoor(planks):
    img = door_panel(planks)
    for y in range(16):
        for x in range(16):
            if y not in (0, 15) and y % 4 == 0:
                put(img, x, y, shade(img.getpixel((x, y)), 0.75))
    for x, y in TRAPDOOR_WINDOWS:
        put(img, x, y, (0, 0, 0, 0))
    return img


def make_item_door(planks):
    """Flat icon: same panel, with the cutout windows filled as dark glass."""
    img = make_door_top(planks)
    for x, y in TOP_WINDOWS:
        put(img, x, y, shade(plank_grain(planks, x, y), 0.25))
    return img


def main():
    BLOCK_TEX.mkdir(parents=True, exist_ok=True)
    ITEM_TEX.mkdir(parents=True, exist_ok=True)
    for wood in WOODS:
        planks = Image.open(BLOCK_TEX / f"{wood}_planks.png").convert("RGBA")
        outputs = {
            BLOCK_TEX / f"{wood}_door_top.png": make_door_top(planks),
            BLOCK_TEX / f"{wood}_door_bottom.png": make_door_bottom(planks),
            BLOCK_TEX / f"{wood}_trapdoor.png": make_trapdoor(planks),
            ITEM_TEX / f"{wood}_door.png": make_item_door(planks),
        }
        for path, img in outputs.items():
            img.save(path)
            print(f"Wrote {path}")


if __name__ == "__main__":
    main()
