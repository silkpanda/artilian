"""Draws Chester's 64x64 entity texture and 16x16 Chester Whistle icon.
Run from the project root:

    python tools/art/make_chester_art.py

Layout matches src/main/resources/assets/artilian/geo/entity/chester.geo.json (box UV, 64x64).
"""
from pathlib import Path

from PIL import Image

OUT = Path("src/main/resources/assets/artilian/textures")

# Rich oak-chest wood palette (original shading, vanilla chest silhouette harmony)
EDGE = (42, 27, 12, 255)
WOOD_DEEP = (86, 54, 20, 255)
WOOD_DARK = (114, 74, 28, 255)
WOOD_MID = (142, 95, 36, 255)
WOOD = (166, 114, 44, 255)
WOOD_LIGHT = (188, 135, 58, 255)
WOOD_HI = (204, 152, 72, 255)
INSIDE = (64, 38, 16, 255)
INSIDE_DARK = (44, 26, 10, 255)

# Iron latch
IRON_OUTLINE = (46, 46, 54, 255)
IRON_HI = (232, 232, 238, 255)
IRON = (182, 182, 190, 255)
IRON_DARK = (114, 114, 124, 255)

# Sunsteel Gold feet & whistle
GOLD_OUTLINE = (72, 34, 10, 255)
GOLD_DARK = (178, 88, 18, 255)
GOLD = (255, 159, 56, 255)       # #FF9F38 Sunsteel Gold
GOLD_LIGHT = (255, 206, 98, 255)
GOLD_HI = (255, 246, 214, 255)   # #FFF6D6 highlight

# Expressive eyes & mouth
WHITE = (255, 255, 255, 255)
EYE_SHADE = (224, 232, 242, 255)
PUPIL = (30, 22, 42, 255)
TEAL = (56, 217, 192, 255)       # #38D9C0 Starlight Teal iris/sparkle
CYAN_HI = (125, 249, 255, 255)   # #7DF9FF bright catchlight

TONGUE_DARK = (196, 56, 92, 255)
TONGUE = (255, 108, 144, 255)
TONGUE_HI = (255, 168, 192, 255)
TOOTH = (255, 246, 214, 255)
TOOTH_SHADE = (224, 208, 172, 255)


def faces(u, v, w, h, d):
    """Bedrock box-UV face rectangles as (x, y, width, height)."""
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }


def put(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def fill(img, rect, color):
    x, y, w, h = rect
    for px in range(x, x + w):
        for py in range(y, y + h):
            put(img, px, py, color)


def outline(img, rect, color):
    x, y, w, h = rect
    for px in range(x, x + w):
        put(img, px, y, color)
        put(img, px, y + h - 1, color)
    for py in range(y, y + h):
        put(img, x, py, color)
        put(img, x + w - 1, py, color)


def oak_panel(img, rect, horizontal_seams=True):
    """Textured oak chest face: dark bevel border, warm oak frame, shaded horizontal planks."""
    x, y, w, h = rect
    fill(img, rect, WOOD)

    # Interior plank grain & subtle deterministic variation
    for py in range(y + 1, y + h - 1):
        rel_y = py - (y + 1)
        is_seam = horizontal_seams and (rel_y % 3 == 2) and (py < y + h - 2)
        for px in range(x + 1, x + w - 1):
            rel_x = px - (x + 1)
            if is_seam:
                put(img, px, py, WOOD_DARK if (rel_x + rel_y) % 5 != 0 else WOOD_MID)
            else:
                n = (rel_x * 5 + rel_y * 11 + (rel_x ^ rel_y) * 3) % 13
                if n in (0, 1):
                    put(img, px, py, WOOD_LIGHT)
                elif n == 2:
                    put(img, px, py, WOOD_HI)
                elif n in (3, 4):
                    put(img, px, py, WOOD_MID)
                else:
                    put(img, px, py, WOOD)

    # Inner oak bezel around the recessed panel
    if w > 4 and h > 4:
        outline(img, (x + 1, y + 1, w - 2, h - 2), WOOD_DARK)
        for px in range(x + 2, x + w - 2):
            put(img, px, y + 2, WOOD_LIGHT)
            put(img, px, y + h - 3, WOOD_MID)
        for py in range(y + 2, y + h - 2):
            put(img, x + 2, py, WOOD_LIGHT)
            put(img, x + w - 3, py, WOOD_MID)

    # Outer dark border with subtle corner rivets/shadow
    outline(img, rect, EDGE)
    if w >= 6 and h >= 5:
        for cx, cy in ((x + 1, y + 1), (x + w - 2, y + 1), (x + 1, y + h - 2), (x + w - 2, y + h - 2)):
            put(img, cx, cy, WOOD_DEEP)


def chester():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    # Lid (14x5x14 at uv [0, 0])
    lid = faces(0, 0, 14, 5, 14)
    for name in ("top", "east", "front", "west", "back"):
        oak_panel(img, lid[name], horizontal_seams=(name == "top"))
    fill(img, lid["bottom"], INSIDE)
    outline(img, lid["bottom"], EDGE)

    # Base (14x10x14 at uv [0, 19])
    base = faces(0, 19, 14, 10, 14)
    for name in ("bottom", "east", "front", "west", "back"):
        oak_panel(img, base[name], horizontal_seams=True)
    fill(img, base["top"], INSIDE)
    outline(img, base["top"], INSIDE_DARK)

    # Iron latch (2x4x1 at uv [0, 0], sharing unused lid top-left corner)
    latch = faces(0, 0, 2, 4, 1)
    for name, rect in latch.items():
        fill(img, rect, IRON_DARK if name in ("bottom", "back") else IRON)
    lx, ly, lw, lh = latch["front"]
    put(img, lx, ly, IRON_HI)
    put(img, lx + 1, ly, IRON_HI)
    put(img, lx, ly + 1, IRON)
    put(img, lx + 1, ly + 1, IRON_HI)
    put(img, lx, ly + 2, IRON_DARK)
    put(img, lx + 1, ly + 2, IRON)
    put(img, lx, ly + 3, IRON_OUTLINE)
    put(img, lx + 1, ly + 3, IRON_OUTLINE)
    tx, ty, tw, th = latch["top"]
    fill(img, (tx, ty, tw, th), IRON_HI)

    # Sunsteel-gold feet (4x2x6 at uv [0, 44] and [20, 44])
    for u in (0, 20):
        foot = faces(u, 44, 4, 2, 6)
        for name, rect in foot.items():
            if name == "top":
                fill(img, rect, GOLD_LIGHT)
                outline(img, rect, GOLD_DARK)
            elif name == "bottom":
                fill(img, rect, GOLD_OUTLINE)
            else:
                # 2px tall side/front/back: bright golden upper toe, shaded lower sole
                x, y, w, h = rect
                fill(img, (x, y, w, 1), GOLD)
                fill(img, (x, y + 1, w, 1), GOLD_DARK)
                put(img, x, y, GOLD_DARK)
                put(img, x + w - 1, y, GOLD_DARK)
        fx, fy, fw, fh = foot["front"]
        put(img, fx + 1, fy, GOLD_HI)
        put(img, fx + 2, fy, GOLD_HI)
        put(img, fx, fy + 1, GOLD_OUTLINE)
        put(img, fx + 3, fy + 1, GOLD_OUTLINE)

    # Expressive cartoony eyes (4x4x1 at uv [0, 54] and [12, 54])
    for u, pupil_dx in ((0, 1), (12, 1)):
        eye = faces(u, 54, 4, 4, 1)
        for name, rect in eye.items():
            if name == "front":
                fill(img, rect, WHITE)
            else:
                fill(img, rect, WOOD_DEEP)
        ex, ey, ew, eh = eye["front"]
        # Soft corner eyelid pixels so the 4x4 eye reads rounded & cartoony
        put(img, ex, ey, WOOD_DEEP)
        put(img, ex + 3, ey, WOOD_DEEP)
        put(img, ex, ey + 3, EYE_SHADE)
        put(img, ex + 3, ey + 3, EYE_SHADE)
        # Large expressive pupil + Starlight-teal iris glint + crisp catchlight
        fill(img, (ex + pupil_dx, ey + 1, 2, 3), PUPIL)
        put(img, ex + pupil_dx + 1, ey + 3, TEAL)
        put(img, ex + pupil_dx, ey + 1, WHITE)
        put(img, ex + pupil_dx + 1, ey + 1, CYAN_HI)

    # Cute pink tongue (5x1x5 at uv [24, 53])
    tongue = faces(24, 53, 5, 1, 5)
    for name, rect in tongue.items():
        fill(img, rect, TONGUE if name == "top" else TONGUE_DARK)
    tx, ty, tw, th = tongue["top"]
    fill(img, (tx + 2, ty + 1, 1, 3), TONGUE_HI)
    put(img, tx + 2, ty, TONGUE_DARK)

    # Cute ivory chest teeth (2x2x1 at uv [46, 54])
    tooth = faces(46, 54, 2, 2, 1)
    for name, rect in tooth.items():
        fill(img, rect, TOOTH if name in ("front", "top") else TOOTH_SHADE)
    fx, fy, fw, fh = tooth["front"]
    put(img, fx, fy + 1, TOOTH_SHADE)
    put(img, fx + 1, fy + 1, TOOTH_SHADE)

    (OUT / "entity").mkdir(parents=True, exist_ok=True)
    img.save(OUT / "entity" / "chester.png")


def whistle():
    """Crisp 16x16 Sunsteel Gold & Starlight Teal Adventurer's Whistle."""
    palette = {
        ".": None,
        "O": GOLD_OUTLINE,
        "D": GOLD_DARK,
        "G": GOLD,
        "L": GOLD_LIGHT,
        "H": GOLD_HI,
        "P": PUPIL,
        "T": TEAL,
        "C": CYAN_HI,
    }
    rows = [
        "................",
        "..C.............",
        ".CTC......OO....",
        "..T......OLLOO..",
        "........OLHHGDO.",
        "...C....OGPGGDO.",
        "..CTC..OLGDDDO..",
        "...T...OLGGDO...",
        "......OLGGDO....",
        "...OOOLGGDO.....",
        "..OLLOOGDO......",
        ".OL..LGDO.......",
        ".OG..GDO........",
        ".ODDDDD.........",
        "..OOOO..........",
        "................",
    ]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        assert len(row) == 16
        for x, ch in enumerate(row):
            color = palette[ch]
            if color is not None:
                put(img, x, y, color)
    (OUT / "item").mkdir(parents=True, exist_ok=True)
    img.save(OUT / "item" / "chester_whistle.png")


if __name__ == "__main__":
    chester()
    whistle()
    print("wrote chester.png and chester_whistle.png")
