"""Draws Crystal Fox's 128x64 monster-tamer inspired crystal-fox texture and 16x16 Capture Crystal & Biome Treat icons.
Run from the project root:

    python tools/art/make_pets_art.py

Layout matches src/main/resources/assets/artilian/geo/entity/crystal_fox.geo.json (box UV, 128x64).
"""
from pathlib import Path

from PIL import Image

OUT = Path("src/main/resources/assets/artilian/textures")

# Cohesive monster-tamer coat palette:
# Pearl-frost mist fur with soft ice-teal shading & deep Starlight/Sapphire crystal points
FUR_HI = (242, 253, 253, 255)
FUR_BASE = (212, 244, 243, 255)
FUR_SHADE = (158, 218, 220, 255)
FUR_DEEP = (104, 174, 184, 255)
FUR_CHEST = (250, 254, 254, 255)

# Starlight Crystal palette (from make_ores_and_gear_art.py)
STAR_HI = (232, 255, 254, 255)
STAR_BRIGHT = (125, 249, 255, 255)  # #7DF9FF
STAR_MAIN = (56, 217, 192, 255)     # #38D9C0
STAR_SHADE = (32, 158, 154, 255)
STAR_DARK = (24, 98, 108, 255)
STAR_TRIM_HI = (162, 120, 244, 255)
STAR_TRIM = (104, 71, 158, 255)
STAR_TRIM_DARK = (61, 44, 94, 255)

NOSE = (36, 26, 58, 255)
EAR_INNER = (125, 249, 255, 255)

EYE_WHITE = (255, 255, 255, 255)
EYE_IRIS = (76, 46, 138, 255)
EYE_PUPIL = (32, 20, 58, 255)
EYE_SPARK = (168, 252, 255, 255)

# UI/Item palette (DESIGN.md)
SLATE_OUTLINE = (24, 18, 36, 255)
SLATE = (74, 58, 104, 255)
SLATE_LIGHT = (106, 90, 140, 255)
GOLD_DARK = (178, 88, 18, 255)
GOLD = (255, 159, 56, 255)
GOLD_HI = (255, 246, 214, 255)

CRYSTAL_PALETTE = {
    "g": STAR_HI,
    "h": STAR_BRIGHT,
    "b": STAR_MAIN,
    "m": STAR_SHADE,
    "s": STAR_DARK,
    "v": STAR_TRIM_HI,
    "p": STAR_TRIM,
    "P": STAR_TRIM_DARK,
}


def faces(u, v, w, h, d):
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


def paint_grid(img, x0, y0, rows, palette):
    for ry, row in enumerate(rows):
        for rx, ch in enumerate(row):
            if ch != "." and ch in palette:
                put(img, x0 + rx, y0 + ry, palette[ch])


def paint_geo_box(img, u, v, w, h, d, face_grids, palette):
    rects = faces(u, v, w, h, d)
    for name, grid in face_grids.items():
        rx, ry, rw, rh = rects[name]
        paint_grid(img, rx, ry, grid, palette)


def smooth_coat(img, u, v, w, h, d, hi, base, shade, deep):
    f = faces(u, v, w, h, d)
    for name, rect in f.items():
        x, y, rw, rh = rect
        for py in range(y, y + rh):
            t = (py - y) / max(1, rh - 1)
            for px in range(x, x + rw):
                if name == "top":
                    color = hi if (px == x or px == x + rw - 1) else base
                elif name == "bottom":
                    color = shade
                elif t < 0.25:
                    color = hi if (px + py) % 3 != 0 else base
                elif t > 0.78:
                    color = deep if py == y + rh - 1 else shade
                else:
                    color = base if (px + py) % 5 != 0 else shade
                put(img, px, py, color)


def make_crystal_fox():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    # 1. Body [6, 4, 9] at [0, 0] — sleek pearl-frost coat with a soft crystal-teal spine stripe
    smooth_coat(img, 0, 0, 6, 4, 9, FUR_HI, FUR_BASE, FUR_SHADE, FUR_DEEP)
    bf = faces(0, 0, 6, 4, 9)
    tx, ty, tw, th = bf["top"]
    for dy in range(1, th - 1):
        put(img, tx + 2, ty + dy, STAR_MAIN if dy % 2 == 0 else STAR_SHADE)
        put(img, tx + 3, ty + dy, STAR_MAIN if dy % 2 == 1 else STAR_BRIGHT)
    # Soft teal hip gradient
    for side in ("east", "west"):
        sx, sy, sw, sh = bf[side]
        put(img, sx + (sw - 2 if side == "east" else 1), sy + 1, STAR_MAIN)
        put(img, sx + (sw - 3 if side == "east" else 2), sy + 1, FUR_SHADE)
    fill(img, bf["bottom"], FUR_CHEST)

    # 2. Fluffy Frost Chest Ruff [7, 3, 2] at [32, 0] with jagged bottom edge
    smooth_coat(img, 32, 0, 7, 3, 2, FUR_CHEST, FUR_HI, FUR_BASE, FUR_SHADE)
    rf = faces(32, 0, 7, 3, 2)
    rx, ry, rw, rh = rf["front"]
    for rx_i in (0, 2, 4, 6):
        put(img, rx + rx_i, ry + rh - 1, (0, 0, 0, 0))

    # 3. Single Diamond Chest Gem [2, 2, 1] at [52, 0]
    paint_geo_box(img, 52, 0, 2, 2, 1, {
        "top": ["vh"],
        "bottom": ["ms"],
        "east": ["h", "m"],
        "west": ["b", "s"],
        "front": ["gh", "bm"],
        "back": ["pp", "PP"],
    }, CRYSTAL_PALETTE)

    # 4. Head [7, 5, 6] at [0, 15] — sleek pearl-frost face with teal-crystal cheek/crown markings
    smooth_coat(img, 0, 15, 7, 5, 6, FUR_HI, FUR_BASE, FUR_SHADE, FUR_DEEP)
    hf = faces(0, 15, 7, 5, 6)
    hx, hy, hw, hh = hf["front"]
    # Cream cheeks & lower face
    fill(img, (hx + 1, hy + hh - 2, hw - 2, 2), FUR_CHEST)
    # Subtle crystal crown stripe at top of head
    tx, ty, tw, th = hf["top"]
    for dy in range(1, th):
        put(img, tx + 3, ty + dy, STAR_MAIN if dy > 2 else STAR_BRIGHT)

    # 5. Delicate Tapered Fox Snout [3, 2, 3] at [28, 15]
    smooth_coat(img, 28, 15, 3, 2, 3, FUR_CHEST, FUR_HI, FUR_BASE, FUR_SHADE)
    sf = faces(28, 15, 3, 2, 3)
    sx, sy, sw, sh = sf["front"]
    put(img, sx + 1, sy, NOSE)

    # 6. Single Diamond Forehead Gem [2, 2, 1] at [42, 15]
    paint_geo_box(img, 42, 15, 2, 2, 1, {
        "top": ["gh"],
        "bottom": ["ms"],
        "east": ["h", "m"],
        "west": ["b", "s"],
        "front": ["gh", "bm"],
        "back": ["pp", "PP"],
    }, CRYSTAL_PALETTE)

    # 7. Sleek Crystal-Tipped Fox Ears [3, 5, 2] at [50, 15] & [62, 15]
    # Base fur fades smoothly into a sharp tapered Starlight Crystal ear tip!
    for eu in (50, 62):
        paint_geo_box(img, eu, 15, 3, 5, 2, {
            "top": [".g.", ".h."],
            "bottom": ["FFF", "FFF"],
            "east": [".h", "gb", "bm", "FD", "FF"],
            "front": [".g.", "ghb", "bCb", "FCF", "FFF"],
            "west": ["h.", "bg", "mb", "DF", "FF"],
            "back": [".h.", "bmb", "sms", "DDD", "DDD"],
        }, {
            **CRYSTAL_PALETTE,
            "F": FUR_BASE,
            "D": FUR_SHADE,
            "C": STAR_BRIGHT,
        })

    # 8. Soulful Monster-Tamer Eyes [2, 2, 1] at [74, 15] & [82, 15]
    for idx, eye_u in enumerate((74, 82)):
        ef = faces(eye_u, 15, 2, 2, 1)
        for rect in ef.values():
            fill(img, rect, EYE_WHITE)
        fx, fy, fw, fh = ef["front"]
        inner_x = fx if idx == 0 else fx + 1
        outer_x = fx + 1 if idx == 0 else fx
        put(img, inner_x, fy, EYE_IRIS)
        put(img, inner_x, fy + 1, EYE_PUPIL)
        put(img, outer_x, fy, EYE_WHITE)
        put(img, outer_x, fy + 1, EYE_SPARK)

    # 9. Tapering 3-Segment Tail ending in a Faceted Crystal Spearhead!
    # Tail Base [3, 3, 4] at [0, 30] — soft fur transitioning to mist-teal
    smooth_coat(img, 0, 30, 3, 3, 4, FUR_HI, FUR_BASE, FUR_SHADE, FUR_DEEP)
    tbf = faces(0, 30, 3, 3, 4)
    fill(img, tbf["back"], STAR_MAIN)

    # Tail Mid [2, 2, 4] at [16, 30] — blends smoothly from fur into translucent-cyan crystal!
    paint_geo_box(img, 16, 30, 2, 2, 4, {
        "top": ["FF", "Db", "bm", "gh"],
        "bottom": ["DD", "Dm", "ms", "bh"],
        "east": ["FDmg", "DDsb"],
        "front": ["FF", "DD"],
        "west": ["gmDF", "bsDD"],
        "back": ["gh", "bm"],
    }, {
        **CRYSTAL_PALETTE,
        "F": FUR_BASE,
        "D": FUR_SHADE,
    })

    # Tail Crystal Cluster: central prism + 3 angled faceted crystal shards fanning from the tapered tip!
    paint_geo_box(img, 30, 30, 2, 2, 4, {
        "top": ["bm", "hb", "gh", ".g"],
        "bottom": ["ms", "bm", "bh", ".h"],
        "east": ["mbh.", "smg."],
        "front": ["hb", "ms"],
        "west": [".hbm", ".gms"],
        "back": [".g", ".h"],
    }, CRYSTAL_PALETTE)

    for shard_u in (44, 54):
        paint_geo_box(img, shard_u, 30, 1, 2, 3, {
            "top": ["m", "b", "g"],
            "bottom": ["s", "m", "h"],
            "east": ["bh.", "mg."],
            "front": ["b", "s"],
            "west": [".hb", ".gm"],
            "back": ["g", "h"],
        }, CRYSTAL_PALETTE)

    paint_geo_box(img, 64, 30, 1, 1, 3, {
        "top": ["b", "h", "g"],
        "bottom": ["m", "b", "h"],
        "east": ["bh."],
        "front": ["h"],
        "west": [".hb"],
        "back": ["g"],
    }, CRYSTAL_PALETTE)

    # 10. Agile Fox Legs [2, 4, 2] at [0, 42], [10, 42], [20, 42], [30, 42]
    # Pearl-frost leg fading organically into glowing crystal-dipped paws!
    for lu in (0, 10, 20, 30):
        smooth_coat(img, lu, 42, 2, 4, 2, FUR_HI, FUR_BASE, FUR_SHADE, FUR_DEEP)
        lf = faces(lu, 42, 2, 4, 2)
        for face_name in ("front", "back", "east", "west"):
            fx, fy, fw, fh = lf[face_name]
            fill(img, (fx, fy + 2, fw, 1), STAR_MAIN)
            fill(img, (fx, fy + 3, fw, 1), STAR_BRIGHT if face_name == "front" else STAR_SHADE)
        fill(img, lf["bottom"], STAR_DARK)

    dest = OUT / "entity/crystal_fox.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)


def draw_crystal(core_dark, core_mid, core_hi, ring_color=None):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rows = [
        (7, 8),
        (6, 9),
        (5, 10),
        (4, 11),
        (3, 12),
        (2, 13),
        (2, 13),
        (2, 13),
        (2, 13),
        (3, 12),
        (4, 11),
        (5, 10),
        (6, 9),
        (7, 8),
    ]
    for idx, (x0, x1) in enumerate(rows):
        y = idx + 1
        for x in range(x0, x1 + 1):
            is_border = x == x0 or x == x1 or idx == 0 or idx == len(rows) - 1
            if is_border:
                put(img, x, y, SLATE_OUTLINE)
            elif idx in (1, 12) or x in (x0 + 1, x1 - 1):
                put(img, x, y, GOLD if (y == 7 or y == 8) else SLATE_LIGHT if y < 7 else SLATE)
            else:
                if x < 7 and y < 7:
                    put(img, x, y, core_hi)
                elif x > 9 or y > 10:
                    put(img, x, y, core_dark)
                else:
                    put(img, x, y, core_mid)

    for x in (2, 3, 12, 13):
        put(img, x, 7, GOLD)
        put(img, x, 8, GOLD_DARK)
    put(img, 6, 4, core_hi)
    put(img, 5, 5, core_hi)

    if ring_color is not None:
        for px, py in [(2, 3), (13, 3), (2, 12), (13, 12), (7, 0), (8, 0), (7, 15), (8, 15)]:
            put(img, px, py, ring_color)
    return img


def make_biome_treat():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    COOK_OUT = (86, 48, 22, 255)
    COOK_DARK = (168, 106, 48, 255)
    COOK_MID = (218, 154, 78, 255)
    COOK_LIGHT = (242, 194, 118, 255)
    GLAZE = (56, 217, 192, 255)
    GLAZE_HI = (168, 252, 240, 255)
    BERRY = (232, 68, 118, 255)
    BERRY_HI = (255, 162, 194, 255)

    spans = {
        3: (4, 6, 9, 11),
        4: (3, 7, 8, 12),
        5: (2, 13),
        6: (2, 13),
        7: (2, 13),
        8: (3, 12),
        9: (4, 11),
        10: (5, 10),
        11: (6, 9),
        12: (7, 8),
    }
    for y, coords in spans.items():
        if len(coords) == 4:
            ranges = [(coords[0], coords[1]), (coords[2], coords[3])]
        else:
            ranges = [(coords[0], coords[1])]
        for x0, x1 in ranges:
            for x in range(x0, x1 + 1):
                put(img, x, y, COOK_MID)

    pixels = img.load()
    copy = img.copy().load()
    for y in range(1, 15):
        for x in range(1, 15):
            if copy[x, y][3] == 0:
                neighbors = [copy[x + dx, y + dy][3] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
                if any(a > 0 for a in neighbors):
                    pixels[x, y] = COOK_OUT

    for y in range(4, 8):
        for x in range(4, 12):
            if copy[x, y][3] > 0:
                put(img, x, y, GLAZE_HI if y == 4 or x < 6 else GLAZE)
    for x in range(6, 10):
        put(img, x, 8, COOK_LIGHT)
    for x in range(6, 10):
        put(img, x, 10, COOK_DARK)

    fill(img, (7, 5, 2, 2), BERRY)
    put(img, 7, 5, BERRY_HI)
    return img


def make_shadow_wisp():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    # Dark charcoal-violet blocky head + vivid purple tapered torso & tendrils (Bob's drawing)
    SH_HEAD_HI = (78, 68, 98, 255)
    SH_HEAD_BASE = (58, 50, 76, 255)
    SH_HEAD_SHADE = (42, 36, 58, 255)
    SH_HEAD_DEEP = (28, 23, 40, 255)
    SH_SPOT = (22, 18, 32, 255)

    SH_BODY_HI = (178, 108, 248, 255)
    SH_BODY_BASE = (142, 68, 218, 255)
    SH_BODY_SHADE = (104, 42, 174, 255)
    SH_BODY_DEEP = (68, 26, 122, 255)

    SH_EYE_OUTER = (217, 70, 239, 255)   # #D946EF Eye/Shadow magenta
    SH_EYE_GLOW = (248, 208, 255, 255)

    # Upper & lower tapered V-torso
    smooth_coat(img, 0, 0, 5, 4, 4, SH_BODY_HI, SH_BODY_BASE, SH_BODY_SHADE, SH_BODY_DEEP)
    smooth_coat(img, 20, 0, 3, 3, 3, SH_BODY_HI, SH_BODY_BASE, SH_BODY_SHADE, SH_BODY_DEEP)
    # Glowing chest core
    cf = faces(34, 0, 2, 2, 1)
    for rect in cf.values():
        fill(img, rect, SH_EYE_OUTER)
    fill(img, cf["front"], SH_EYE_GLOW)

    # Boxy charcoal-violet head with shadowy speckles matching Bob's shaded head block
    smooth_coat(img, 0, 10, 8, 6, 7, SH_HEAD_HI, SH_HEAD_BASE, SH_HEAD_SHADE, SH_HEAD_DEEP)
    hf = faces(0, 10, 8, 6, 7)
    fx, fy, fw, fh = hf["front"]
    for sx, sy in [(1, 1), (6, 1), (3, 2), (4, 3), (2, 4), (5, 4), (1, 5), (6, 5)]:
        put(img, fx + sx, fy + sy, SH_SPOT)
    tx, ty, tw, th = hf["top"]
    for sx, sy in [(2, 2), (5, 2), (3, 4), (4, 5)]:
        put(img, tx + sx, ty + sy, SH_SPOT)

    # Glowing Shadow eyes
    for eye_u in (34, 42):
        ef = faces(eye_u, 10, 2, 2, 1)
        for rect in ef.values():
            fill(img, rect, SH_EYE_OUTER)
        ex, ey, _, _ = ef["front"]
        put(img, ex, ey, SH_EYE_GLOW)
        put(img, ex + 1, ey, SH_EYE_OUTER)
        put(img, ex, ey + 1, SH_EYE_OUTER)
        put(img, ex + 1, ey + 1, SH_EYE_GLOW)

    # Spindly purple arms + 3 splayed claw tendrils each
    for au in (0, 24):
        smooth_coat(img, au, 26, 1, 5, 1, SH_BODY_HI, SH_BODY_BASE, SH_BODY_SHADE, SH_BODY_DEEP)
    for cu in (6, 12, 18, 30, 36, 42):
        smooth_coat(img, cu, 26, 1, 3, 1, SH_BODY_HI, SH_BODY_BASE, SH_BODY_SHADE, SH_BODY_DEEP)
        cf = faces(cu, 26, 1, 3, 1)
        put(img, cf["front"][0], cf["front"][1] + 2, SH_EYE_OUTER)
        put(img, cf["bottom"][0], cf["bottom"][1], SH_EYE_GLOW)

    # Two long purple lower legs/tendrils
    for lu in (0, 20):
        smooth_coat(img, lu, 36, 2, 4, 2, SH_BODY_HI, SH_BODY_BASE, SH_BODY_SHADE, SH_BODY_DEEP)
    for tu in (10, 30):
        smooth_coat(img, tu, 36, 2, 3, 2, SH_BODY_BASE, SH_BODY_SHADE, SH_BODY_DEEP, SH_HEAD_DEEP)
        tf = faces(tu, 36, 2, 3, 2)
        fill(img, tf["bottom"], SH_EYE_OUTER)

    dest = OUT / "entity/shadow_wisp.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)


def make_cinder_crag():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    # Crimson-basalt stone & molten orange-gold magma core (Bob's fire/rock creature)
    RK_HI = (225, 56, 84, 255)
    RK_BASE = (182, 32, 62, 255)
    RK_SHADE = (132, 22, 48, 255)
    RK_DEEP = (82, 16, 36, 255)

    FL_CORE = (255, 242, 148, 255)
    FL_GOLD = (255, 184, 48, 255)
    FL_ORANGE = (249, 115, 22, 255)   # #F97316
    FL_CRIMSON = (225, 29, 72, 255)   # #E11D48

    # Main middle body [9, 7, 7] at [0, 0] with molten magma cracks & glowing inner face
    smooth_coat(img, 0, 0, 9, 7, 7, RK_HI, RK_BASE, RK_SHADE, RK_DEEP)
    bf = faces(0, 0, 9, 7, 7)
    fx, fy, fw, fh = bf["front"]
    # Molten magma arch framing the eyes & mouth on the middle body
    for dy in range(1, fh - 1):
        for dx in range(1, fw - 1):
            if dx in (1, fw - 2) or dy in (1, fh - 1):
                put(img, fx + dx, fy + dy, FL_ORANGE if (dx + dy) % 2 == 0 else FL_GOLD)
    # Molten cracks on sides & back
    for side in ("east", "west", "back"):
        sx, sy, sw, sh = bf[side]
        put(img, sx + sw // 2, sy + 2, FL_ORANGE)
        put(img, sx + sw // 2, sy + 3, FL_GOLD)
        put(img, sx + max(0, sw // 2 - 1), sy + 4, FL_ORANGE)

    # Flat rocky brow [10, 2, 8] at [34, 0]
    smooth_coat(img, 34, 0, 10, 2, 8, RK_HI, RK_BASE, RK_SHADE, RK_DEEP)
    brf = faces(34, 0, 10, 2, 8)
    for px in range(brf["front"][0] + 1, brf["front"][0] + 9, 2):
        put(img, px, brf["front"][1] + 1, FL_ORANGE)

    # Eyes [2, 2, 1] at [72, 0] & [80, 0] — glowing fiery amber eyes
    for idx, eu in enumerate((72, 80)):
        ef = faces(eu, 0, 2, 2, 1)
        for rect in ef.values():
            fill(img, rect, FL_CRIMSON)
        ex, ey, _, _ = ef["front"]
        fill(img, (ex, ey, 2, 2), FL_GOLD)
        put(img, ex + (1 if idx == 0 else 0), ey, FL_CORE)
        put(img, ex + (0 if idx == 0 else 1), ey + 1, FL_ORANGE)

    # Wide arched magma mouth [5, 2, 1] at [88, 0]
    mf = faces(88, 0, 5, 2, 1)
    for rect in mf.values():
        fill(img, rect, RK_DEEP)
    mx, my, mw, mh = mf["front"]
    fill(img, (mx, my, mw, mh), FL_ORANGE)
    fill(img, (mx + 1, my, mw - 2, 1), FL_CORE)
    put(img, mx + 2, my + 1, FL_GOLD)
    put(img, mx, my, FL_CRIMSON)
    put(img, mx + mw - 1, my, FL_CRIMSON)

    # 3 Pointy Flame Spires on top
    # Base [3, 3, 3] at [0, 16] and [30, 16]
    for fu in (0, 30):
        smooth_coat(img, fu, 16, 3, 3, 3, FL_GOLD, FL_ORANGE, FL_CRIMSON, RK_SHADE)
        ff = faces(fu, 16, 3, 3, 3)
        for face_name in ("front", "back", "east", "west"):
            x0, y0, _, _ = ff[face_name]
            put(img, x0 + 1, y0 + 1, FL_CORE)
            put(img, x0 + 1, y0 + 2, FL_GOLD)
    # Mid [2, 2, 2] at [14, 16], [44, 16], and center base [60, 16]
    for mu in (14, 44, 60):
        smooth_coat(img, mu, 16, 2, 2, 2, FL_CORE, FL_GOLD, FL_ORANGE, FL_CRIMSON)
    # Tips [1, 2, 1] at [24, 16], [54, 16], [70, 16]
    for tu in (24, 54, 70):
        smooth_coat(img, tu, 16, 1, 2, 1, FL_CORE, FL_CORE, FL_GOLD, FL_ORANGE)

    # 2 Sturdy Crimson-Stone Pillar Legs + Flared Feet
    for lu in (0, 32):
        smooth_coat(img, lu, 26, 3, 4, 3, RK_HI, RK_BASE, RK_SHADE, RK_DEEP)
        lf = faces(lu, 26, 3, 4, 3)
        put(img, lf["front"][0] + 1, lf["front"][1] + 1, FL_ORANGE)
    for fu in (14, 46):
        smooth_coat(img, fu, 26, 4, 1, 4, RK_BASE, RK_SHADE, RK_DEEP, RK_DEEP)

    dest = OUT / "entity/cinder_crag.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)


def make_zaphox():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    ZX_GOLD_HI = (254, 240, 178, 255)
    ZX_GOLD_BASE = (251, 191, 36, 255)
    ZX_GOLD_SHADE = (217, 138, 16, 255)
    ZX_GOLD_DEEP = (176, 92, 12, 255)
    ZX_SPOT = (92, 42, 14, 255)

    ZX_FOX_HI = (251, 146, 60, 255)
    ZX_FOX_RUFF = (234, 88, 12, 255)

    ZX_WING_CORE = (254, 249, 195, 255)
    ZX_WING_HI = (253, 224, 71, 255)
    ZX_WING_BASE = (249, 115, 22, 255)
    ZX_WING_SHADE = (194, 65, 12, 255)

    ZX_TEAR = (30, 27, 75, 255)
    ZX_SPARK = (125, 249, 255, 255)
    ZX_EYE = (250, 204, 21, 255)
    ZX_PUPIL = (24, 24, 27, 255)
    ZX_WHITE = (255, 255, 255, 255)
    ZX_TUSK = (254, 249, 195, 255)
    ZX_NOSE = (49, 46, 129, 255)

    # 1. Sleek Cheetah Torso [5, 4, 11] at [0, 0]
    smooth_coat(img, 0, 0, 5, 4, 11, ZX_GOLD_HI, ZX_GOLD_BASE, ZX_GOLD_SHADE, ZX_GOLD_DEEP)
    bf = faces(0, 0, 5, 4, 11)
    fill(img, bf["bottom"], ZX_GOLD_HI)
    for side in ("east", "west"):
        sx, sy, sw, sh = bf[side]
        for px, py in [(2, 1), (5, 2), (8, 1), (3, 3), (7, 3), (9, 2)]:
            put(img, sx + px, sy + py, ZX_SPOT)
        put(img, sx + 4, sy + 1, ZX_SPARK)
    tx, ty, tw, th = bf["top"]
    for px, py in [(1, 2), (3, 3), (1, 6), (3, 7), (2, 9)]:
        put(img, tx + px, ty + py, ZX_SPOT)

    # 2. Chest / Shoulder Arch [6, 5, 3] at [32, 0] — warm fox orange with cream bib
    smooth_coat(img, 32, 0, 6, 5, 3, ZX_FOX_HI, ZX_FOX_RUFF, ZX_GOLD_SHADE, ZX_GOLD_DEEP)
    rf = faces(32, 0, 6, 5, 3)
    fill(img, (rf["front"][0] + 1, rf["front"][1] + 1, 4, 3), ZX_GOLD_HI)

    # 3. Head [7, 5, 5] at [50, 0] — cheetah-fox face with tear-marks & cream muzzle frame
    smooth_coat(img, 50, 0, 7, 5, 5, ZX_GOLD_HI, ZX_GOLD_BASE, ZX_GOLD_SHADE, ZX_GOLD_DEEP)
    hf = faces(50, 0, 7, 5, 5)
    fx, fy, fw, fh = hf["front"]
    fill(img, (fx + 1, fy + 3, fw - 2, 2), ZX_GOLD_HI)
    # Cheetah tear-marks + electric cheek accent
    for dy in (2, 3, 4):
        put(img, fx + 1, fy + dy, ZX_TEAR)
        put(img, fx + fw - 2, fy + dy, ZX_TEAR)
    put(img, fx, fy + 2, ZX_SPARK)
    put(img, fx + fw - 1, fy + 2, ZX_SPARK)
    # Cheek ruffs [1, 3, 2] at [74, 0] & [80, 0]
    for cu in (74, 80):
        smooth_coat(img, cu, 0, 1, 3, 2, ZX_GOLD_HI, ZX_FOX_HI, ZX_FOX_RUFF, ZX_GOLD_DEEP)

    # 4. Snout [3, 2, 2] at [86, 0]
    smooth_coat(img, 86, 0, 3, 2, 2, ZX_GOLD_HI, ZX_GOLD_BASE, ZX_GOLD_SHADE, ZX_GOLD_DEEP)
    sf = faces(86, 0, 3, 2, 2)
    put(img, sf["front"][0] + 1, sf["front"][1], ZX_NOSE)

    # 5. Upward-Curling Elephant Mini-Trunk: Base [2,2,2]@[96,0], Mid [2,2,2]@[104,0], Tip [2,2,2]@[112,0]
    smooth_coat(img, 96, 0, 2, 2, 2, ZX_GOLD_HI, ZX_GOLD_BASE, ZX_GOLD_SHADE, ZX_GOLD_DEEP)
    smooth_coat(img, 104, 0, 2, 2, 2, ZX_GOLD_BASE, ZX_GOLD_SHADE, ZX_GOLD_DEEP, ZX_SPOT)
    smooth_coat(img, 112, 0, 2, 2, 2, ZX_WING_HI, ZX_GOLD_BASE, ZX_GOLD_SHADE, ZX_GOLD_DEEP)
    ttf = faces(112, 0, 2, 2, 2)
    fill(img, ttf["front"], ZX_SPARK)
    fill(img, ttf["top"], ZX_WING_CORE)

    # 6. Small Ivory Tusks [1, 1, 2] at [120, 0] & [120, 4]
    for tv in (0, 4):
        smooth_coat(img, 120, tv, 1, 1, 2, ZX_WHITE, ZX_TUSK, ZX_GOLD_HI, ZX_GOLD_BASE)

    # 7. Forehead Lightning Curl Crest [1, 2, 1] at [32, 10] & [36, 10]
    smooth_coat(img, 32, 10, 1, 2, 1, ZX_WING_HI, ZX_WING_BASE, ZX_WING_SHADE, ZX_SPOT)
    smooth_coat(img, 36, 10, 1, 2, 1, ZX_WING_CORE, ZX_WING_HI, ZX_WING_BASE, ZX_SPARK)

    # 8. Expressive 2x3 Eyes at [40, 10] & [46, 10]
    for idx, eu in enumerate((40, 46)):
        ef = faces(eu, 10, 2, 3, 1)
        for rect in ef.values():
            fill(img, rect, ZX_TEAR)
        ex, ey, _, _ = ef["front"]
        fill(img, (ex, ey, 2, 3), ZX_EYE)
        px_col = ex + (1 if idx == 0 else 0)
        hi_col = ex + (0 if idx == 0 else 1)
        put(img, px_col, ey, ZX_PUPIL)
        put(img, px_col, ey + 1, ZX_PUPIL)
        put(img, px_col, ey + 2, ZX_PUPIL)
        put(img, hi_col, ey, ZX_WHITE)
        put(img, hi_col, ey + 2, ZX_SPARK)

    # 9. Hybrid Elephant-Fox Ears:
    # Tall Fox Ear [2, 5, 1] at [0, 16] & [12, 16]; Flared Elephant Pinna [2, 4, 1] at [6, 16] & [18, 16]
    for eu in (0, 12):
        smooth_coat(img, eu, 16, 2, 5, 1, ZX_FOX_HI, ZX_FOX_RUFF, ZX_GOLD_DEEP, ZX_SPOT)
        ef = faces(eu, 16, 2, 5, 1)
        put(img, ef["front"][0] + (0 if eu == 0 else 1), ef["front"][1] + 2, ZX_GOLD_HI)
        put(img, ef["front"][0] + (0 if eu == 0 else 1), ef["front"][1] + 3, ZX_GOLD_HI)
        put(img, ef["front"][0] + (0 if eu == 0 else 1), ef["front"][1] + 4, ZX_GOLD_HI)
        fill(img, ef["top"], ZX_SPOT)
    for pu in (6, 18):
        smooth_coat(img, pu, 16, 2, 4, 1, ZX_GOLD_HI, ZX_GOLD_BASE, ZX_GOLD_SHADE, ZX_FOX_RUFF)
        pf = faces(pu, 16, 2, 4, 1)
        fill(img, (pf["front"][0], pf["front"][1] + 1, 2, 2), ZX_GOLD_HI)

    # 10. Dramatic Jagged Orange Lightning-Bolt Wings (3 stepped blades per wing)
    # Left wing: [24, 16] (1,5,3), [32, 16] (1,6,3), [40, 16] (1,5,3)
    # Right wing: [48, 16] (1,5,3), [56, 16] (1,6,3), [64, 16] (1,5,3)
    for wu, wh in ((24, 5), (32, 6), (40, 5), (48, 5), (56, 6), (64, 5)):
        smooth_coat(img, wu, 16, 1, wh, 3, ZX_WING_HI, ZX_WING_BASE, ZX_WING_SHADE, ZX_FOX_RUFF)
        wf = faces(wu, 16, 1, wh, 3)
        fill(img, wf["top"], ZX_WING_CORE)
        for side in ("east", "west"):
            sx, sy, sw, sh = wf[side]
            for dy in range(sh):
                put(img, sx + (dy % sw), sy + dy, ZX_WING_HI if dy < 2 else ZX_WING_CORE if dy == 0 else ZX_WING_HI)
                if dy == 0:
                    put(img, sx + sw - 1, sy, ZX_SPARK)

    # 11. Original Cheetah Whip-Tail + Zigzag Lightning-Bolt Tip
    # tail_base [2, 2, 5] at [0, 26], tail_mid [2, 2, 5] at [14, 26]
    for tu in (0, 14):
        smooth_coat(img, tu, 26, 2, 2, 5, ZX_GOLD_HI, ZX_GOLD_BASE, ZX_GOLD_SHADE, ZX_GOLD_DEEP)
        tf = faces(tu, 26, 2, 2, 5)
        for side in ("east", "west", "top"):
            sx, sy, sw, sh = tf[side]
            put(img, sx + 1, sy, ZX_SPOT)
            put(img, sx + min(sw - 1, 3), sy + min(sh - 1, 1), ZX_SPOT)
    # Zigzag Lightning Bolt Tip: [28, 26] (1,2,3), [36, 26] (1,2,3), [44, 26] (1,1,3)
    smooth_coat(img, 28, 26, 1, 2, 3, ZX_WING_HI, ZX_WING_BASE, ZX_WING_SHADE, ZX_FOX_RUFF)
    smooth_coat(img, 36, 26, 1, 2, 3, ZX_WING_CORE, ZX_WING_HI, ZX_WING_BASE, ZX_WING_SHADE)
    smooth_coat(img, 44, 26, 1, 1, 3, ZX_SPARK, ZX_WING_CORE, ZX_WING_HI, ZX_SPARK)

    # 12. Slender Cheetah Sprint Legs [2, 8, 2] at [0, 36], [10, 36], [20, 36], [30, 36]
    for lu in (0, 10, 20, 30):
        smooth_coat(img, lu, 36, 2, 8, 2, ZX_GOLD_HI, ZX_GOLD_BASE, ZX_GOLD_SHADE, ZX_GOLD_DEEP)
        lf = faces(lu, 36, 2, 8, 2)
        for face_name in ("front", "east", "west"):
            fx, fy, fw, fh = lf[face_name]
            put(img, fx, fy + 2, ZX_SPOT)
            put(img, fx + 1, fy + 4, ZX_SPOT)
            fill(img, (fx, fy + 7, fw, 1), ZX_TEAR)
        fill(img, lf["bottom"], ZX_TEAR)

    dest = OUT / "entity/zaphox.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)


def make_galecorn():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    # Sunny pastel-yellow fox-unicorn coat (Alice's drawing yellow body) + aqua-mint mane/accents + billowing white cloud fan
    GC_COAT_CORE = (255, 252, 232, 255)
    GC_COAT_HI = (254, 246, 194, 255)
    GC_COAT_BASE = (253, 224, 104, 255)
    GC_COAT_SHADE = (238, 188, 64, 255)

    GC_CYAN_HI = (178, 248, 244, 255)
    GC_CYAN_BASE = (56, 217, 192, 255)
    GC_CYAN_SHADE = (28, 168, 178, 255)

    GC_HORN_CORE = (255, 255, 255, 255)
    GC_HORN_HI = (224, 247, 255, 255)
    GC_HORN_BLUE = (125, 249, 255, 255)

    GC_CLOUD_HI = (255, 255, 255, 255)
    GC_CLOUD_BASE = (240, 247, 255, 255)
    GC_CLOUD_SHADE = (214, 232, 248, 255)
    GC_CLOUD_BREEZE = (166, 224, 248, 255)

    GC_EYE_IRIS = (14, 148, 214, 255)
    GC_EYE_PUPIL = (16, 62, 104, 255)
    GC_NOSE = (28, 152, 206, 255)

    # 1. Sunny Yellow Fox-Unicorn Body [5, 4, 9] at [0, 0]
    smooth_coat(img, 0, 0, 5, 4, 9, GC_COAT_HI, GC_COAT_BASE, GC_COAT_SHADE, GC_COAT_SHADE)
    bf = faces(0, 0, 5, 4, 9)
    fill(img, bf["bottom"], GC_COAT_CORE)

    # 2. Fluffy Cream Chest Tuft [4, 3, 2] at [28, 0]
    smooth_coat(img, 28, 0, 4, 3, 2, GC_COAT_CORE, GC_COAT_HI, GC_COAT_BASE, GC_COAT_SHADE)

    # 3. Flowing Silky Unicorn Mane: neck [3, 4, 3] at [40, 0] & back lock [2, 3, 4] at [52, 0]
    smooth_coat(img, 40, 0, 3, 4, 3, GC_CYAN_HI, GC_CYAN_BASE, GC_CLOUD_BREEZE, GC_CYAN_SHADE)
    mnf = faces(40, 0, 3, 4, 3)
    fill(img, mnf["top"], GC_CLOUD_HI)
    smooth_coat(img, 52, 0, 2, 3, 4, GC_CLOUD_HI, GC_CYAN_HI, GC_CYAN_BASE, GC_CYAN_SHADE)

    # 4. Head [6, 5, 5] at [64, 0] — Aqua-teal crown & outer head blending into clean cream face
    smooth_coat(img, 64, 0, 6, 5, 5, GC_CYAN_HI, GC_CYAN_BASE, GC_CYAN_SHADE, GC_CYAN_SHADE)
    hf = faces(64, 0, 6, 5, 5)
    fx, fy, fw, fh = hf["front"]
    fill(img, (fx + 1, fy + 2, fw - 2, fh - 2), GC_COAT_CORE)
    fill(img, (fx + 2, fy + 1, fw - 4, 1), GC_COAT_HI)
    # Cheek tufts [1, 3, 2] at [86, 0] & [92, 0]
    for cu in (86, 92):
        smooth_coat(img, cu, 0, 1, 3, 2, GC_COAT_CORE, GC_COAT_HI, GC_CYAN_HI, GC_CYAN_BASE)

    # 5. Clean Cream Snout [3, 2, 3] at [98, 0]
    smooth_coat(img, 98, 0, 3, 2, 3, GC_COAT_CORE, GC_COAT_HI, GC_COAT_BASE, GC_COAT_BASE)
    sf = faces(98, 0, 3, 2, 3)
    put(img, sf["front"][0] + 1, sf["front"][1], GC_NOSE)

    # 6. Spiraling Tapered Unicorn Horn: Base [2,2,2]@[110,0], Mid [1,3,1]@[118,0], Tip [1,2,1]@[122,0]
    smooth_coat(img, 110, 0, 2, 2, 2, GC_HORN_CORE, GC_HORN_HI, GC_HORN_BLUE, GC_CYAN_BASE)
    smooth_coat(img, 118, 0, 1, 3, 1, GC_HORN_CORE, GC_HORN_BLUE, GC_HORN_HI, GC_CYAN_BASE)
    hm = faces(118, 0, 1, 3, 1)
    for side in ("front", "east", "west", "back"):
        put(img, hm[side][0], hm[side][1] + 1, GC_HORN_CORE)
    smooth_coat(img, 122, 0, 1, 2, 1, GC_HORN_CORE, GC_HORN_CORE, GC_HORN_BLUE, GC_HORN_HI)

    # 7. Forelock Crest [2, 3, 2] at [28, 6]
    smooth_coat(img, 28, 6, 2, 3, 2, GC_CLOUD_HI, GC_CYAN_HI, GC_CYAN_BASE, GC_CYAN_SHADE)

    # 8. Alert Fox Ears [2, 4, 1] at [36, 7] & [42, 7]
    for eu in (36, 42):
        smooth_coat(img, eu, 7, 2, 4, 1, GC_CYAN_HI, GC_CYAN_BASE, GC_CYAN_SHADE, GC_CYAN_SHADE)
        ef = faces(eu, 7, 2, 4, 1)
        inner_x = ef["front"][0] + (0 if eu == 36 else 1)
        put(img, inner_x, ef["front"][1] + 1, GC_COAT_CORE)
        put(img, inner_x, ef["front"][1] + 2, GC_COAT_CORE)
        put(img, inner_x, ef["front"][1] + 3, GC_COAT_HI)

    # 9. Expressive 2x3 Sky-Blue Eyes at [48, 7] & [54, 7]
    for idx, eu in enumerate((48, 54)):
        ef = faces(eu, 7, 2, 3, 1)
        for rect in ef.values():
            fill(img, rect, GC_EYE_PUPIL)
        ex, ey, _, _ = ef["front"]
        fill(img, (ex, ey, 2, 3), GC_EYE_IRIS)
        px_col = ex + (1 if idx == 0 else 0)
        hi_col = ex + (0 if idx == 0 else 1)
        put(img, px_col, ey, GC_EYE_PUPIL)
        put(img, px_col, ey + 1, GC_EYE_PUPIL)
        put(img, px_col, ey + 2, GC_EYE_PUPIL)
        put(img, hi_col, ey, GC_CLOUD_HI)
        put(img, hi_col, ey + 2, GC_HORN_BLUE)

    # 10. Giant Horizontal Whirlwind Cloud-Fan Tail
    # tail_root [4,3,3]@[0,14], tail_cloud_center [5,4,6]@[14,14], crest [4,2,4]@[36,14]
    # tail_fan_left [4,3,6]@[52,14], tail_fan_right [4,3,6]@[72,14]
    # tail_gust_tips center [4,3,4]@[92,14], fins [2,2,4]@[108,14] & [0,24]
    smooth_coat(img, 0, 14, 4, 3, 3, GC_CLOUD_HI, GC_CLOUD_BASE, GC_CLOUD_BREEZE, GC_CYAN_HI)
    smooth_coat(img, 14, 14, 5, 4, 6, GC_CLOUD_HI, GC_CLOUD_BASE, GC_CLOUD_SHADE, GC_CLOUD_BREEZE)
    smooth_coat(img, 36, 14, 4, 2, 4, GC_CLOUD_HI, GC_CLOUD_HI, GC_CLOUD_BASE, GC_CLOUD_BREEZE)
    for fan_u in (52, 72):
        smooth_coat(img, fan_u, 14, 4, 3, 6, GC_CLOUD_HI, GC_CLOUD_BASE, GC_CLOUD_SHADE, GC_CLOUD_BREEZE)
        ff = faces(fan_u, 14, 4, 3, 6)
        tx, ty, tw, th = ff["top"]
        for dy in range(th):
            put(img, tx + (dy % tw), ty + dy, GC_HORN_BLUE if dy % 2 == 0 else GC_CLOUD_BREEZE)
    smooth_coat(img, 92, 14, 4, 3, 4, GC_CLOUD_HI, GC_CLOUD_BASE, GC_CLOUD_BREEZE, GC_CYAN_HI)
    smooth_coat(img, 108, 14, 2, 2, 4, GC_CLOUD_HI, GC_HORN_HI, GC_CLOUD_BREEZE, GC_CYAN_BASE)
    smooth_coat(img, 0, 24, 2, 2, 4, GC_CLOUD_HI, GC_HORN_HI, GC_CLOUD_BREEZE, GC_CYAN_BASE)

    # 11. Graceful Yellow Legs + Aqua Fetlocks + Pearlescent Hooves [2, 6, 2] at [0, 34]..[30, 34]
    for lu in (0, 10, 20, 30):
        smooth_coat(img, lu, 34, 2, 6, 2, GC_COAT_HI, GC_COAT_BASE, GC_COAT_SHADE, GC_COAT_SHADE)
        lf = faces(lu, 34, 2, 6, 2)
        for face_name in ("front", "back", "east", "west"):
            fx, fy, fw, fh = lf[face_name]
            fill(img, (fx, fy + 3, fw, 2), GC_CYAN_BASE)
            fill(img, (fx, fy + 5, fw, 1), GC_HORN_HI)
        fill(img, lf["bottom"], GC_CLOUD_BREEZE)

    dest = OUT / "entity/galecorn.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)


def main():
    make_crystal_fox()
    make_shadow_wisp()
    make_cinder_crag()
    make_zaphox()
    make_galecorn()

    item_dir = OUT / "item"
    item_dir.mkdir(parents=True, exist_ok=True)
    draw_crystal((32, 26, 48, 255), (50, 42, 72, 255), (84, 72, 114, 255)).save(item_dir / "capture_crystal.png")
    draw_crystal(STAR_DARK, STAR_MAIN, STAR_HI).save(item_dir / "capture_crystal_filled.png")
    draw_crystal(STAR_DARK, STAR_BRIGHT, (255, 255, 255, 255), ring_color=STAR_BRIGHT).save(
        item_dir / "capture_crystal_summoned.png"
    )
    draw_crystal(GOLD_DARK, GOLD, GOLD_HI).save(item_dir / "capture_crystal_fainted.png")
    make_biome_treat().save(item_dir / "biome_treat.png")
    print("Generated pet textures (Crystal Fox, Shadow Wisp, Cinder Crag, Zaphox, Galecorn).")


if __name__ == "__main__":
    main()
