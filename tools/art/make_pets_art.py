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


def main():
    make_crystal_fox()
    make_shadow_wisp()
    make_cinder_crag()

    item_dir = OUT / "item"
    item_dir.mkdir(parents=True, exist_ok=True)
    draw_crystal((32, 26, 48, 255), (50, 42, 72, 255), (84, 72, 114, 255)).save(item_dir / "capture_crystal.png")
    draw_crystal(STAR_DARK, STAR_MAIN, STAR_HI).save(item_dir / "capture_crystal_filled.png")
    draw_crystal(STAR_DARK, STAR_BRIGHT, (255, 255, 255, 255), ring_color=STAR_BRIGHT).save(
        item_dir / "capture_crystal_summoned.png"
    )
    draw_crystal(GOLD_DARK, GOLD, GOLD_HI).save(item_dir / "capture_crystal_fainted.png")
    make_biome_treat().save(item_dir / "biome_treat.png")
    print("Generated pet textures (Crystal Fox, Shadow Wisp, Cinder Crag).")


if __name__ == "__main__":
    main()
