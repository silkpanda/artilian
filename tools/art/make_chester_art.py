"""Draws Chester's 128x128 entity textures (awake + sleep reference) and optional 16x16 Chester Whistle icon.
Run from the project root:

    python tools/art/make_chester_art.py            # generates chester.png and chester_sleep.png (128x128)
    python tools/art/make_chester_art.py --whistle  # also regenerates chester_whistle.png

Layouts match src/main/resources/assets/artilian/geo/entity/chester.geo.json (box UV, 64x64 grid at 2x)
and src/main/resources/assets/artilian/geo/entity/chester_sleep.geo.json (per-face vanilla chest UV at 2x).
"""
import sys
from pathlib import Path

from PIL import Image

OUT = Path("src/main/resources/assets/artilian/textures")
SCALE = 2

# Rich oak-chest wood palette (6-8 stop shaded oak + interior tones)
EDGE = (42, 27, 12, 255)
WOOD_DEEP = (86, 54, 20, 255)
WOOD_DARK = (114, 74, 28, 255)
WOOD_MID = (142, 95, 36, 255)
WOOD = (166, 114, 44, 255)
WOOD_LIGHT = (188, 135, 58, 255)
WOOD_HI = (204, 152, 72, 255)
WOOD_PEAK = (218, 168, 86, 255)
INSIDE_DEEP = (34, 20, 8, 255)
INSIDE_DARK = (44, 26, 10, 255)
INSIDE = (64, 38, 16, 255)
INSIDE_LIGHT = (82, 50, 22, 255)

# Hammered iron latch
IRON_OUTLINE = (46, 46, 54, 255)
IRON_DARK = (114, 114, 124, 255)
IRON = (182, 182, 190, 255)
IRON_LIGHT = (210, 212, 220, 255)
IRON_HI = (232, 232, 238, 255)

# Sunsteel Gold feet & whistle
GOLD_OUTLINE = (72, 34, 10, 255)
GOLD_DARK = (178, 88, 18, 255)
GOLD = (255, 159, 56, 255)       # #FF9F38 Sunsteel Gold
GOLD_LIGHT = (255, 206, 98, 255)
GOLD_HI = (255, 246, 214, 255)   # #FFF6D6 highlight

# Expressive eyes & mouth
WHITE = (255, 255, 255, 255)
EYE_SHADE = (214, 226, 238, 255)
PUPIL = (30, 22, 42, 255)
TEAL_DARK = (32, 158, 154, 255)
TEAL = (56, 217, 192, 255)       # #38D9C0 Starlight Teal iris/sparkle
CYAN_HI = (125, 249, 255, 255)   # #7DF9FF bright catchlight

TONGUE_DEEP = (154, 42, 74, 255)
TONGUE_DARK = (196, 56, 92, 255)
TONGUE = (255, 108, 144, 255)
TONGUE_HI = (255, 168, 192, 255)
TOOTH = (255, 246, 214, 255)
TOOTH_SHADE = (224, 208, 172, 255)
TOOTH_DEEP = (188, 172, 138, 255)


def faces(u, v, w, h, d):
    """Bedrock box-UV face rectangles scaled by SCALE as (x, y, width, height)."""
    return {
        "top": ((u + d) * SCALE, v * SCALE, w * SCALE, d * SCALE),
        "bottom": ((u + d + w) * SCALE, v * SCALE, w * SCALE, d * SCALE),
        "east": (u * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE),
        "front": ((u + d) * SCALE, (v + d) * SCALE, w * SCALE, h * SCALE),
        "west": ((u + d + w) * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE),
        "back": ((u + d + w + d) * SCALE, (v + d) * SCALE, w * SCALE, h * SCALE),
    }


def scale_rect(rect):
    x, y, w, h = rect
    return (x * SCALE, y * SCALE, w * SCALE, h * SCALE)


def put(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def fill(img, rect, color):
    x, y, w, h = rect
    for px in range(x, x + w):
        for py in range(y, y + h):
            put(img, px, py, color)


def outline(img, rect, color, thickness=1):
    x, y, w, h = rect
    for t in range(thickness):
        for px in range(x + t, x + w - t):
            put(img, px, y + t, color)
            put(img, px, y + h - 1 - t, color)
        for py in range(y + t, y + h - t):
            put(img, x + t, py, color)
            put(img, x + w - 1 - t, py, color)


def oak_panel(img, rect, horizontal_seams=True):
    """2x high-detail oak chest face: dark frame, recessed beveled panel, plank grain, rivets."""
    x, y, w, h = rect
    fill(img, rect, WOOD)

    # Interior plank grain & subtle deterministic variation
    for py in range(y + 2, y + h - 2):
        rel_y = py - (y + 2)
        is_seam = horizontal_seams and (rel_y % 5 == 4) and (py < y + h - 3)
        for px in range(x + 2, x + w - 2):
            rel_x = px - (x + 2)
            if is_seam:
                put(img, px, py, WOOD_DARK if (rel_x + rel_y) % 4 != 0 else WOOD_MID)
            elif horizontal_seams and (rel_y % 5 == 0) and (rel_x % 6 != 0):
                put(img, px, py, WOOD_LIGHT)
            else:
                n = (rel_x * 7 + rel_y * 13 + (rel_x ^ rel_y) * 5) % 17
                if n in (0, 1):
                    put(img, px, py, WOOD_LIGHT)
                elif n == 2:
                    put(img, px, py, WOOD_HI)
                elif n in (3, 4, 5):
                    put(img, px, py, WOOD_MID)
                elif n == 6 and rel_y < 4:
                    put(img, px, py, WOOD_PEAK)
                else:
                    put(img, px, py, WOOD)

    # Recessed oak frame bezel inside outer rim
    if w >= 8 and h >= 8:
        outline(img, (x + 2, y + 2, w - 4, h - 4), WOOD_DARK)
        for px in range(x + 3, x + w - 3):
            put(img, px, y + 3, WOOD_LIGHT)
            put(img, px, y + h - 4, WOOD_MID)
        for py in range(y + 3, y + h - 3):
            put(img, x + 3, py, WOOD_LIGHT)
            put(img, x + w - 4, py, WOOD_MID)

    # Rich dark timber border (1px outer outline + 1px deep bevel) + subtle corner rivets
    outline(img, rect, EDGE)
    if w >= 6 and h >= 6:
        outline(img, (x + 1, y + 1, w - 2, h - 2), WOOD_DEEP)
        for cx, cy in ((x + 2, y + 2), (x + w - 3, y + 2), (x + 2, y + h - 3), (x + w - 3, y + h - 3)):
            put(img, cx, cy, EDGE)


def oak_interior(img, rect):
    """Shaded hollow interior of the chest lid/base (plank grain + deep rim shadow)."""
    x, y, w, h = rect
    for py in range(y, y + h):
        for px in range(x, x + w):
            rx, ry = px - x, py - y
            if rx == 0 or ry == 0 or rx == w - 1 or ry == h - 1:
                put(img, px, py, EDGE)
            elif rx == 1 or ry == 1 or rx == w - 2 or ry == h - 2:
                put(img, px, py, INSIDE_DEEP)
            elif ry % 4 == 0:
                put(img, px, py, INSIDE_DARK)
            elif (rx * 5 + ry * 9) % 7 == 0:
                put(img, px, py, INSIDE_LIGHT)
            else:
                put(img, px, py, INSIDE)


def paint_latch_rect(img, rect, face_name, invert_y=False):
    """Paint one face of the 2x4x1 iron latch (works for box-UV and per-face UV)."""
    x, y, w, h = rect
    if face_name in ("top", "up"):
        fill(img, rect, IRON_HI)
        outline(img, rect, IRON_LIGHT)
        return
    if face_name in ("bottom", "down"):
        fill(img, rect, IRON_DARK)
        if w >= 2:
            put(img, x, y, IRON_OUTLINE)
            put(img, x + w - 1, y, IRON_OUTLINE)
        return
    if face_name in ("back", "south"):
        for py in range(y, y + h):
            for px in range(x, x + w):
                put(img, px, py, IRON_OUTLINE if py in (y, y + h - 1) else IRON_DARK)
        return
    if face_name in ("east", "west"):
        for dy in range(h):
            py = (y + h - 1 - dy) if invert_y else (y + dy)
            color = IRON_HI if dy < 2 else (IRON if dy < h - 2 else IRON_DARK)
            for px in range(x, x + w):
                put(img, px, py, IRON_OUTLINE if (dy == h - 1 or px == x + w - 1) else color)
        return

    # Front / north face (4x8 pixels at 2x)
    for dy in range(h):
        py = (y + h - 1 - dy) if invert_y else (y + dy)
        for dx in range(w):
            px = x + dx
            if dy == h - 1:
                put(img, px, py, IRON_OUTLINE)
            elif dy == 0:
                put(img, px, py, IRON_HI)
            elif dy in (1, 2):
                put(img, px, py, IRON_HI if dx in (1, 2) else IRON_LIGHT)
            elif dy in (3, 4) and dx in (1, 2):
                # Keyhole recess
                put(img, px, py, IRON_OUTLINE)
            elif dy >= h - 3:
                put(img, px, py, IRON_DARK)
            else:
                put(img, px, py, IRON if dx in (0, 3) else IRON_LIGHT)


def chester():
    img = Image.new("RGBA", (64 * SCALE, 64 * SCALE), (0, 0, 0, 0))

    # Lid (14x5x14 at uv [0, 0])
    lid = faces(0, 0, 14, 5, 14)
    for name in ("top", "east", "front", "west", "back"):
        oak_panel(img, lid[name], horizontal_seams=(name == "top"))
    oak_interior(img, lid["bottom"])

    # Base (14x10x14 at uv [0, 19])
    base = faces(0, 19, 14, 10, 14)
    for name in ("bottom", "east", "front", "west", "back"):
        oak_panel(img, base[name], horizontal_seams=True)
    oak_interior(img, base["top"])

    # Iron latch (2x4x1 at uv [40, 44], non-overlapping in chester.geo.json)
    latch = faces(40, 44, 2, 4, 1)
    for name, rect in latch.items():
        paint_latch_rect(img, rect, name, invert_y=False)

    # Sunsteel-gold feet (4x2x6 at uv [0, 44] and [20, 44])
    for u in (0, 20):
        foot = faces(u, 44, 4, 2, 6)
        for name, rect in foot.items():
            x, y, w, h = rect
            if name == "top":
                fill(img, rect, GOLD_LIGHT)
                outline(img, rect, GOLD_DARK)
                for py in range(y + 2, y + h - 2, 3):
                    fill(img, (x + 2, py, w - 4, 1), GOLD)
            elif name == "bottom":
                # Shaded sole treads (eliminates flat bottom face)
                fill(img, rect, GOLD_OUTLINE)
                for py in range(y + 1, y + h - 1):
                    for px in range(x + 1, x + w - 1):
                        if py % 3 == 0:
                            put(img, px, py, GOLD_DARK)
                        elif py in (y + 2, y + 5, y + 8) and 1 <= (px - x) <= w - 2:
                            put(img, px, py, GOLD)
            else:
                # 4px tall sides/front/back: crisp golden toe arch + shaded lower sole
                fill(img, (x, y, w, 1), GOLD_LIGHT)
                fill(img, (x, y + 1, w, 1), GOLD)
                fill(img, (x, y + 2, w, 1), GOLD_DARK)
                fill(img, (x, y + 3, w, 1), GOLD_OUTLINE)
                put(img, x, y, GOLD_DARK)
                put(img, x + w - 1, y, GOLD_DARK)
        fx, fy, fw, fh = foot["front"]
        # Three sculpted golden toe glints
        for tx in (fx + 1, fx + 3, fx + 4, fx + 6):
            put(img, tx, fy, GOLD_HI)
            put(img, tx, fy + 1, GOLD_LIGHT)
        put(img, fx + 2, fy + 2, GOLD_OUTLINE)
        put(img, fx + 5, fy + 2, GOLD_OUTLINE)

    # Expressive cartoony eyes (4x4x1 at uv [0, 54] and [12, 54])
    for u, pupil_dx in ((0, 2), (12, 2)):
        eye = faces(u, 54, 4, 4, 1)
        for name, rect in eye.items():
            x, y, w, h = rect
            if name == "front":
                fill(img, rect, WHITE)
            elif name == "back":
                # Multi-tone oak-inset backing (eliminates flat back faces)
                for py in range(y, y + h):
                    for px in range(x, x + w):
                        if px in (x, x + w - 1) or py in (y, y + h - 1):
                            put(img, px, py, EDGE)
                        elif (px + py) % 3 == 0:
                            put(img, px, py, WOOD_DARK)
                        else:
                            put(img, px, py, WOOD_DEEP)
            else:
                for py in range(y, y + h):
                    for px in range(x, x + w):
                        put(img, px, py, WOOD_DARK if (px + py) % 2 == 0 else WOOD_DEEP)

        ex, ey, ew, eh = eye["front"]
        # Soft corner eyelid pixels so the 8x8 eye reads rounded & cartoony
        for cx, cy in ((ex, ey), (ex + 1, ey), (ex, ey + 1), (ex + ew - 1, ey), (ex + ew - 2, ey), (ex + ew - 1, ey + 1)):
            put(img, cx, cy, WOOD_DEEP)
        for cx, cy in ((ex, ey + eh - 1), (ex + ew - 1, ey + eh - 1)):
            put(img, cx, cy, EDGE)
        # Soft sclera shadow along bottom rim
        fill(img, (ex + 1, ey + eh - 1, ew - 2, 1), EYE_SHADE)
        # Large expressive pupil + Starlight-teal iris gradient + crisp catchlights
        fill(img, (ex + pupil_dx, ey + 2, 4, 5), PUPIL)
        fill(img, (ex + pupil_dx + 1, ey + 5, 3, 2), TEAL_DARK)
        fill(img, (ex + pupil_dx + 1, ey + 6, 2, 1), TEAL)
        put(img, ex + pupil_dx + 2, ey + 6, CYAN_HI)
        fill(img, (ex + pupil_dx, ey + 2, 2, 2), WHITE)
        put(img, ex + pupil_dx + 2, ey + 2, CYAN_HI)
        put(img, ex + pupil_dx + 3, ey + 4, TEAL)

    # Cute pink tongue (5x1x5 at uv [24, 53])
    tongue = faces(24, 53, 5, 1, 5)
    for name, rect in tongue.items():
        x, y, w, h = rect
        if name == "top":
            fill(img, rect, TONGUE)
            outline(img, rect, TONGUE_DARK)
            fill(img, (x + 4, y + 2, 2, 6), TONGUE_HI)
            fill(img, (x + 4, y + 1, 2, 1), TONGUE_DARK)
            put(img, x + 2, y + 3, TONGUE_HI)
            put(img, x + 7, y + 3, TONGUE_HI)
        elif name == "bottom":
            # Shaded tongue underside (eliminates flat bottom face on awake_extras)
            fill(img, rect, TONGUE_DARK)
            outline(img, rect, TONGUE_DEEP)
            fill(img, (x + 3, y + 3, w - 6, h - 6), TONGUE)
            fill(img, (x + 2, y + h - 2, w - 4, 1), INSIDE_DARK)
        else:
            fill(img, (x, y, w, 1), TONGUE)
            if h > 1:
                fill(img, (x, y + 1, w, h - 1), TONGUE_DARK)

    # Cute ivory chest teeth (2x2x1 at uv [46, 54])
    tooth = faces(46, 54, 2, 2, 1)
    for name, rect in tooth.items():
        x, y, w, h = rect
        if name in ("front", "top"):
            fill(img, rect, TOOTH)
            if name == "front":
                fill(img, (x, y, w, 1), WHITE)
                fill(img, (x, y + h - 1, w, 1), TOOTH_SHADE)
        else:
            fill(img, rect, TOOTH_SHADE)
            if h > 1:
                fill(img, (x, y + h - 1, w, 1), TOOTH_DEEP)

    (OUT / "entity").mkdir(parents=True, exist_ok=True)
    img.save(OUT / "entity" / "chester.png")


def chester_sleep():
    """Generate 128x128 sleeping Chester texture matching chester_sleep.geo.json per-face UVs."""
    img = Image.new("RGBA", (64 * SCALE, 64 * SCALE), (0, 0, 0, 0))

    # Lid per-face rects from chester_sleep.geo.json
    lid_outer = [
        ((28, 0, 14, 14), True),   # up
        ((0, 14, 14, 5), False),   # east
        ((14, 14, 14, 5), False),  # south
        ((28, 14, 14, 5), False),  # west
        ((42, 14, 14, 5), False),  # north
    ]
    for rect, seams in lid_outer:
        oak_panel(img, scale_rect(rect), horizontal_seams=seams)
    oak_interior(img, scale_rect((14, 0, 14, 14)))  # lid down

    # Body per-face rects from chester_sleep.geo.json
    body_outer = [
        ((14, 19, 14, 14), True),  # down
        ((0, 33, 14, 10), True),   # east
        ((14, 33, 14, 10), True),  # south
        ((28, 33, 14, 10), True),  # west
        ((42, 33, 14, 10), True),  # north
    ]
    for rect, seams in body_outer:
        oak_panel(img, scale_rect(rect), horizontal_seams=seams)
    oak_interior(img, scale_rect((28, 19, 14, 14)))  # body up

    # Latch per-face rects from chester_sleep.geo.json (negative uv_size height -> invert_y=True)
    latch_faces = [
        ("up", (3, 0, 2, 1), False),
        ("down", (1, 0, 2, 1), False),
        ("east", (0, 1, 1, 4), True),
        ("south", (1, 1, 2, 4), True),
        ("west", (3, 1, 1, 4), True),
        ("north", (4, 1, 2, 4), True),
    ]
    for face_name, rect, inv in latch_faces:
        paint_latch_rect(img, scale_rect(rect), face_name, invert_y=inv)

    (OUT / "entity").mkdir(parents=True, exist_ok=True)
    img.save(OUT / "entity" / "chester_sleep.png")


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
    chester_sleep()
    if "--whistle" in sys.argv:
        whistle()
        print("wrote chester.png, chester_sleep.png (128x128), and chester_whistle.png")
    else:
        print("wrote chester.png and chester_sleep.png (128x128)")
