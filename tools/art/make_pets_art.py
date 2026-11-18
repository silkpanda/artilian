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

    # Dark charcoal-violet blocky head + vivid purple tapered torso & tendrils (Steve's drawing)
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

    # Boxy charcoal-violet head with shadowy speckles matching Steve's shaded head block
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

    # Crimson-basalt stone & molten orange-gold magma core (Steve's fire/rock creature)
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

    # Sunny pastel-yellow fox-unicorn coat (Alex's drawing yellow body) + aqua-mint mane/accents + billowing white cloud fan
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


def make_ember_drake():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    # DESIGN.md Dragon Jungle Accent: Canopy Emerald (#22C55E) + Ember Amber (#F97316)
    ED_HI = (110, 238, 152, 255)
    ED_BASE = (34, 197, 94, 255)
    ED_SHADE = (22, 148, 70, 255)
    ED_DEEP = (14, 98, 48, 255)

    AMB_HI = (255, 222, 120, 255)
    AMB_BASE = (249, 115, 22, 255)
    AMB_SHADE = (194, 72, 14, 255)
    AMB_DEEP = (138, 44, 10, 255)

    HORN_HI = (255, 246, 214, 255)
    HORN_BASE = (224, 204, 164, 255)
    HORN_SHADE = (176, 152, 118, 255)

    # 1. Body [6, 5, 9] at [0, 0] + Belly [5, 1, 8] at [32, 0] + Dorsal Spikes [1, 2, 2] at [60, 0] & [68, 0]
    smooth_coat(img, 0, 0, 6, 5, 9, ED_HI, ED_BASE, ED_SHADE, ED_DEEP)
    bf = faces(0, 0, 6, 5, 9)
    # Subtle dragon scale checker along top/sides
    for side in ("top", "east", "west"):
        sx, sy, sw, sh = bf[side]
        for py in range(sy, sy + sh):
            for px in range(sx, sx + sw):
                if (px + py) % 3 == 0:
                    put(img, px, py, ED_HI if side == "top" else ED_SHADE)
    smooth_coat(img, 32, 0, 5, 1, 8, AMB_HI, (255, 190, 96, 255), AMB_BASE, AMB_SHADE)
    smooth_coat(img, 60, 0, 1, 2, 2, AMB_HI, AMB_BASE, AMB_SHADE, AMB_DEEP)
    smooth_coat(img, 68, 0, 1, 2, 2, AMB_HI, AMB_BASE, AMB_SHADE, AMB_DEEP)

    # 2. Neck [4, 4, 3] at [76, 0]
    smooth_coat(img, 76, 0, 4, 4, 3, ED_HI, ED_BASE, ED_SHADE, ED_DEEP)
    nf = faces(76, 0, 4, 4, 3)
    fill(img, (nf["front"][0] + 1, nf["front"][1] + 1, 2, 3), AMB_HI)

    # 3. Head [6, 5, 5] at [0, 16], Snout [4, 3, 3] at [24, 16], Jaw [3, 1, 3] at [40, 16], Crest [1, 2, 2] at [54, 16]
    smooth_coat(img, 0, 16, 6, 5, 5, ED_HI, ED_BASE, ED_SHADE, ED_DEEP)
    smooth_coat(img, 24, 16, 4, 3, 3, ED_HI, ED_BASE, ED_SHADE, ED_DEEP)
    snf = faces(24, 16, 4, 3, 3)
    put(img, snf["front"][0], snf["front"][1] + 1, ED_DEEP)
    put(img, snf["front"][0] + 3, snf["front"][1] + 1, ED_DEEP)
    smooth_coat(img, 40, 16, 3, 1, 3, AMB_HI, (255, 190, 96, 255), AMB_BASE, AMB_SHADE)
    smooth_coat(img, 54, 16, 1, 2, 2, AMB_HI, AMB_BASE, AMB_SHADE, AMB_DEEP)

    # 4. Horns [1, 3, 1] at [62, 16] & [68, 16]
    for hu in (62, 68):
        smooth_coat(img, hu, 16, 1, 3, 1, HORN_HI, HORN_BASE, HORN_SHADE, HORN_SHADE)

    # 5. Golden-Amber Eyes [2, 2, 1] at [74, 16] & [82, 16]
    for idx, eu in enumerate((74, 82)):
        ef = faces(eu, 16, 2, 2, 1)
        for rect in ef.values():
            fill(img, rect, AMB_DEEP)
        ex, ey, _, _ = ef["front"]
        fill(img, (ex, ey, 2, 2), AMB_HI)
        put(img, ex + (1 if idx == 0 else 0), ey + 1, (38, 18, 10, 255))
        put(img, ex + (0 if idx == 0 else 1), ey, (255, 255, 255, 255))

    # 6. Fluttering Baby Wings: Arm [6, 1, 2] at [0, 28] & [40, 28], Membrane [6, 1, 4] at [18, 28] & [58, 28]
    for wu in (0, 40):
        smooth_coat(img, wu, 28, 6, 1, 2, ED_HI, ED_BASE, ED_SHADE, ED_DEEP)
    for mu in (18, 58):
        smooth_coat(img, mu, 28, 6, 1, 4, AMB_HI, AMB_BASE, AMB_SHADE, AMB_DEEP)

    # 7. Tail Base [4, 4, 4] at [0, 36], Spike [1, 2, 2] at [18, 36], Tail Tip [3, 3, 4] at [26, 36], Spade [4, 2, 3] at [42, 36]
    smooth_coat(img, 0, 36, 4, 4, 4, ED_HI, ED_BASE, ED_SHADE, ED_DEEP)
    smooth_coat(img, 18, 36, 1, 2, 2, AMB_HI, AMB_BASE, AMB_SHADE, AMB_DEEP)
    smooth_coat(img, 26, 36, 3, 3, 4, ED_HI, ED_BASE, ED_SHADE, ED_DEEP)
    smooth_coat(img, 42, 36, 4, 2, 3, AMB_HI, AMB_BASE, AMB_SHADE, AMB_DEEP)

    # 8. Stubby Clawed Legs [2, 4, 2] at [0, 48], [10, 48], [20, 48], [30, 48]
    for lu in (0, 10, 20, 30):
        smooth_coat(img, lu, 48, 2, 4, 2, ED_HI, ED_BASE, ED_SHADE, ED_DEEP)
        lf = faces(lu, 48, 2, 4, 2)
        fill(img, lf["bottom"], HORN_BASE)
        put(img, lf["front"][0], lf["front"][1] + 3, HORN_HI)
        put(img, lf["front"][0] + 1, lf["front"][1] + 3, HORN_HI)

    dest = OUT / "entity/ember_drake.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)


def make_peepkin():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    # DESIGN.md Eye Realm Accent: Twilight Magenta (#D946EF) & Pupil Glow (#7DF9FF)
    PK_HI = (128, 96, 192, 255)
    PK_BASE = (86, 56, 146, 255)
    PK_SHADE = (58, 36, 106, 255)
    PK_DEEP = (36, 22, 72, 255)

    MAG_HI = (244, 142, 255, 255)
    MAG_BASE = (217, 70, 239, 255)   # #D946EF
    MAG_SHADE = (158, 42, 184, 255)
    PUPIL_GLOW = (125, 249, 255, 255)  # #7DF9FF
    SCLERA = (250, 250, 255, 255)
    SCLERA_SHADE = (214, 212, 236, 255)

    # 1. Plush Orb Core [8, 8, 8] at [0, 0] + Belly Rim [7, 2, 7] at [34, 0] + Crown Tuft [4, 2, 4] at [64, 0]
    smooth_coat(img, 0, 0, 8, 8, 8, PK_HI, PK_BASE, PK_SHADE, PK_DEEP)
    smooth_coat(img, 34, 0, 7, 2, 7, MAG_HI, MAG_BASE, MAG_SHADE, PK_DEEP)
    smooth_coat(img, 64, 0, 4, 2, 4, MAG_HI, MAG_BASE, MAG_SHADE, PK_SHADE)

    # 2. Giant Main Eye: Sclera [6, 5, 1] at [0, 18] & Iris/Pupil [3, 3, 1] at [16, 18]
    smooth_coat(img, 0, 18, 6, 5, 1, SCLERA, SCLERA, SCLERA, SCLERA_SHADE)
    ir = faces(16, 18, 3, 3, 1)
    for rect in ir.values():
        fill(img, rect, MAG_SHADE)
    ix, iy, _, _ = ir["front"]
    fill(img, (ix, iy, 3, 3), MAG_BASE)
    put(img, ix + 1, iy + 1, (24, 18, 52, 255))
    put(img, ix + 1, iy, PUPIL_GLOW)
    put(img, ix + 1, iy + 2, PUPIL_GLOW)
    put(img, ix + 2, iy, (255, 255, 255, 255))

    # 3. Blinking Eyelids [6, 1, 1] at [28, 18] & [44, 18]
    for lu in (28, 44):
        smooth_coat(img, lu, 18, 6, 1, 1, MAG_HI, PK_BASE, PK_SHADE, PK_DEEP)
        lf = faces(lu, 18, 6, 1, 1)
        fill(img, lf["front"], MAG_BASE)

    # 4. Two Crown Eye-Buds: Stalks [1, 3, 1] at [60, 18] & [80, 18], Mini Orbs [3, 3, 3] at [66, 18] & [86, 18]
    for su in (60, 80):
        smooth_coat(img, su, 18, 1, 3, 1, PK_HI, PK_BASE, PK_SHADE, PK_DEEP)
    for ou in (66, 86):
        smooth_coat(img, ou, 18, 3, 3, 3, PK_HI, PK_BASE, PK_SHADE, PK_DEEP)
        mof = faces(ou, 18, 3, 3, 3)
        fx, fy, _, _ = mof["front"]
        fill(img, (fx, fy, 3, 3), SCLERA)
        put(img, fx + 1, fy + 1, PUPIL_GLOW)
        put(img, fx + 1, fy, MAG_BASE)

    # 5. Ear-Winglets [4, 3, 2] at [0, 28] & [14, 28]
    for eu in (0, 14):
        smooth_coat(img, eu, 28, 4, 3, 2, MAG_HI, MAG_BASE, MAG_SHADE, PK_SHADE)
        ef = faces(eu, 28, 4, 3, 2)
        fill(img, (ef["front"][0] + (3 if eu == 0 else 0), ef["front"][1], 1, 3), PUPIL_GLOW)

    # 6. Tail Stem [3, 2, 4] at [28, 28] & Peeking Tail Eye [4, 3, 3] at [44, 28]
    smooth_coat(img, 28, 28, 3, 2, 4, PK_HI, PK_BASE, PK_SHADE, PK_DEEP)
    smooth_coat(img, 44, 28, 4, 3, 3, PK_HI, PK_BASE, PK_SHADE, PK_DEEP)
    tef = faces(44, 28, 4, 3, 3)
    bx, by, _, _ = tef["back"]
    fill(img, (bx + 1, by, 2, 3), SCLERA)
    put(img, bx + 1, by + 1, PUPIL_GLOW)
    put(img, bx + 2, by + 1, MAG_BASE)

    # 7. Four Floating Tassel Feet [2, 3, 2] at [0, 38], [10, 38], [20, 38], [30, 38]
    for fu in (0, 10, 20, 30):
        smooth_coat(img, fu, 38, 2, 3, 2, PK_HI, PK_BASE, PK_SHADE, PK_DEEP)
        ff = faces(fu, 38, 2, 3, 2)
        fill(img, (ff["front"][0], ff["front"][1] + 2, 2, 1), MAG_BASE)
        fill(img, ff["bottom"], PUPIL_GLOW)

    dest = OUT / "entity/peepkin.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)


def make_prismling():
    """Prismling: the kids' rainbow legendary pet reimagined as a towering elemental guardian.

    Layout matches src/main/resources/assets/artilian/geo/entity/prismling.geo.json (256x128),
    plus an emissive _glowmask.png for the stoic glowing visor eyes, forehead crest, blue cuffs,
    rainbow crystal veins, and eight crisp 16x16 floating elemental symbols.
    """
    img = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (256, 128), (0, 0, 0, 0))

    # The drawing's seven body stripes, left to right.
    BANDS = [
        (239, 68, 68, 255),    # red
        (249, 115, 22, 255),   # orange
        (250, 204, 21, 255),   # yellow
        (20, 184, 166, 255),   # teal
        (56, 189, 248, 255),   # sky blue
        (168, 85, 247, 255),   # purple
        (196, 181, 253, 255),  # lavender
    ]
    OUTLINE_HI = (253, 196, 72, 255)
    OUTLINE = (245, 158, 11, 255)
    OUTLINE_DARK = (194, 102, 8, 255)
    OUTLINE_DEEP = (132, 60, 6, 255)

    IVORY_HI = (250, 244, 232, 255)
    IVORY = (236, 226, 204, 255)
    IVORY_SHADE = (198, 184, 158, 255)
    IVORY_DEEP = (158, 144, 122, 255)
    VISOR = (30, 24, 44, 255)
    VISOR_RIM = (54, 44, 78, 255)
    EYE_GLOW = (125, 249, 255, 255)
    EYE_CORE = (245, 255, 255, 255)

    CUFF_HI = (147, 224, 255, 255)
    CUFF = (59, 130, 246, 255)
    CUFF_SHADE = (37, 99, 235, 255)
    CUFF_DEEP = (29, 64, 175, 255)

    def tint(color, amount):
        r, g, b, a = color
        if amount >= 0:
            return (int(r + (255 - r) * amount), int(g + (255 - g) * amount), int(b + (255 - b) * amount), a)
        k = 1 + amount
        return (int(r * k), int(g * k), int(b * k), a)

    def shade_box(u, v, w, h, d, hi, base, shade, deep, target=img):
        f = faces(u, v, w, h, d)
        for name, rect in f.items():
            x, y, rw, rh = rect
            col = hi if name == "top" else deep if name == "bottom" else base if name in ("front", "back") else shade
            fill(target, rect, col)
            if name in ("front", "back", "east", "west"):
                for px in range(x, x + rw):
                    put(target, px, y, hi)
                    put(target, px, y + rh - 1, deep)
                for py in range(y + 1, y + rh - 1):
                    put(target, x, py, tint(col, 0.15))
                    put(target, x + rw - 1, py, shade)
            elif name == "top":
                for px in range(x, x + rw):
                    put(target, px, y, tint(hi, 0.25))

    def paint_rainbow_band(target, x, y, band_w, h, color, is_top=False, is_bottom=False, add_glow=False):
        for dx in range(band_w):
            for dy in range(h):
                if is_top:
                    c = tint(color, 0.35 if dy == 0 else 0.20)
                elif is_bottom:
                    c = tint(color, -0.32)
                else:
                    if dy == 0:
                        c = tint(color, 0.32)
                    elif dy == h - 1:
                        c = tint(color, -0.25)
                    elif dx == 0:
                        c = tint(color, 0.18)
                    elif dx == band_w - 1:
                        c = tint(color, -0.14)
                    elif dy in (2, 3) and dx == 1:
                        c = tint(color, 0.45)
                    else:
                        c = color
                put(target, x + dx, y + dy, c)
        if add_glow and not is_top and not is_bottom:
            for dy in (2, 3, 5):
                put(glow, x + 1, y + dy, tint(color, 0.5))

    # 1. Body rainbow core [28, 10, 10] at [0, 0] + center chest prism [12, 8, 1] at [0, 106].
    bf = faces(0, 0, 28, 10, 10)
    for face, order in (("front", BANDS), ("back", list(reversed(BANDS)))):
        fx, fy, fw, fh = bf[face]
        for i, color in enumerate(order):
            paint_rainbow_band(img, fx + i * 4, fy, 4, fh, color, add_glow=True)
    tx, ty, tw, th = bf["top"]
    for i, color in enumerate(BANDS):
        paint_rainbow_band(img, tx + i * 4, ty, 4, th, color, is_top=True)
    bx, by, bw, bh = bf["bottom"]
    for i, color in enumerate(BANDS):
        paint_rainbow_band(img, bx + i * 4, by, 4, bh, color, is_bottom=True)
    for side_name, col in (("east", BANDS[0]), ("west", BANDS[-1])):
        sx, sy, sw, sh = bf[side_name]
        fill(img, (sx, sy, sw, sh), col)
        for py in range(sy, sy + sh):
            put(img, sx, py, tint(col, 0.2))
            put(img, sx + sw - 1, py, tint(col, -0.2))

    cf = faces(0, 106, 12, 8, 1)
    for face, order in (("front", BANDS[2:5]), ("back", list(reversed(BANDS[2:5])))):
        cx, cy, cw, ch = cf[face]
        for i, color in enumerate(order):
            paint_rainbow_band(img, cx + i * 4, cy, 4, ch, tint(color, 0.12), add_glow=True)
        for px in range(cx, cx + cw):
            put(img, px, cy, OUTLINE_HI)
            put(img, px, cy + ch - 1, OUTLINE_DARK)
    fill(img, cf["top"], OUTLINE_HI)
    fill(img, cf["bottom"], OUTLINE_DARK)
    fill(img, cf["east"], OUTLINE)
    fill(img, cf["west"], OUTLINE)

    # 2. Shoulder armor [32, 4, 12] at [0, 21], waist [24, 3, 10] at [0, 70], side pillars [2, 10, 11] at [0, 84], pauldrons [6, 3, 10] at [28, 84].
    shade_box(0, 21, 32, 4, 12, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)
    sf = faces(0, 21, 32, 4, 12)
    for face, order in (("front", BANDS), ("back", list(reversed(BANDS)))):
        sx, sy, _, _ = sf[face]
        for i, color in enumerate(order):
            fill(img, (sx + 2 + i * 4, sy + 3, 4, 1), tint(color, 0.1))
    shade_box(0, 70, 24, 3, 10, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)
    shade_box(0, 84, 2, 10, 11, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)
    shade_box(28, 84, 6, 3, 10, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)

    # 3. Stoic Guardian Head [12, 10, 11] at [78, 0] + crown [14, 3, 12] at [76, 38] + cheek rails [2, 9, 12] at [84, 54] + crest & horns.
    shade_box(78, 0, 12, 10, 11, IVORY_HI, IVORY, IVORY_SHADE, IVORY_DEEP)
    hf = faces(78, 0, 12, 10, 11)
    fx, fy, fw, fh = hf["front"]
    fill(img, (fx, fy, fw, fh), IVORY)
    for px in range(fx, fx + fw):
        put(img, px, fy, IVORY_DEEP)
        put(img, px, fy + fh - 1, OUTLINE_DARK)
    # Deep visor recess across eyes
    fill(img, (fx + 1, fy + 2, 10, 3), VISOR)
    for px in range(fx + 1, fx + 11):
        put(img, px, fy + 1, VISOR_RIM)
    # Angular glowing guardian eyes
    for ex in (fx + 2, fx + 7):
        fill(img, (ex, fy + 2, 3, 2), EYE_GLOW)
        put(img, ex + 1, fy + 2, EYE_CORE)
        fill(glow, (ex, fy + 2, 3, 2), EYE_GLOW)
        put(glow, ex + 1, fy + 2, EYE_CORE)
    # Stoic sculpted faceplate & golden jaw line (no smiley face)
    fill(img, (fx + 1, fy + 5, 10, 4), IVORY)
    fill(img, (fx + 2, fy + 6, 8, 1), IVORY_SHADE)
    fill(img, (fx + 4, fy + 7, 4, 1), IVORY_DEEP)
    fill(img, (fx + 3, fy + 8, 6, 1), OUTLINE_DARK)
    put(img, fx + 1, fy + 6, IVORY_DEEP)
    put(img, fx + 10, fy + 6, IVORY_DEEP)

    shade_box(76, 38, 14, 3, 12, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)
    shade_box(84, 54, 2, 9, 12, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)
    shade_box(90, 96, 3, 4, 2, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)
    # Glowing crystal core on forehead crest
    cf_crest = faces(90, 96, 3, 4, 2)["front"]
    fill(img, (cf_crest[0] + 1, cf_crest[1] + 1, 1, 2), EYE_CORE)
    fill(glow, (cf_crest[0] + 1, cf_crest[1] + 1, 1, 2), EYE_GLOW)
    shade_box(90, 104, 2, 4, 2, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)

    # 4. Pillar Legs [9, 9, 9]: right red [0, 38], left lavender [38, 38], plus armored soles [10, 2, 10].
    for u, color in ((0, BANDS[0]), (38, BANDS[-1])):
        shade_box(u, 38, 9, 9, 9, tint(color, 0.3), color, tint(color, -0.18), tint(color, -0.35))
        lf = faces(u, 38, 9, 9, 9)
        lx, ly, lw, _ = lf["front"]
        fill(img, (lx, ly, lw, 2), OUTLINE_DARK)
        fill(img, (lx + 2, ly + 3, 5, 2), tint(color, 0.28))
    shade_box(0, 57, 10, 2, 10, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)
    shade_box(42, 57, 10, 2, 10, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)
    for u, color in ((0, BANDS[0]), (42, BANDS[-1])):
        bx, by, bw, bh = faces(u, 57, 10, 2, 10)["bottom"]
        fill(img, (bx + 1, by + 1, bw - 2, bh - 2), tint(color, -0.22))

    # 5. Arms, 3-pronged hands & glowing octagonal blue cuffs.
    for u, v, w, h, d in (
        (62, 91, 4, 4, 4),
        (62, 84, 10, 3, 3),
        (90, 82, 3, 8, 3),
        (104, 82, 5, 2, 2),
        (104, 88, 4, 2, 2),
    ):
        shade_box(u, v, w, h, d, OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DEEP)

    for u, v, w, h, d in ((28, 100, 4, 6, 6), (50, 100, 3, 7, 5), (68, 100, 3, 5, 7)):
        shade_box(u, v, w, h, d, CUFF_HI, CUFF, CUFF_SHADE, CUFF_DEEP)
        cf_cuff = faces(u, v, w, h, d)
        for face_name in ("front", "back", "east", "west", "top"):
            cx, cy, cw, ch = cf_cuff[face_name]
            mid_x = cx + cw // 2
            for py in range(cy + 1, cy + max(2, ch - 1)):
                put(img, mid_x, py, CUFF_HI)
                put(glow, mid_x, py, CUFF_HI)

    # 6. Eight crisp 16x16 pixel-art Elemental Symbols (u = 128..255, v = 0..15).
    SYM_PALETTE = {
        "Y": (253, 224, 71, 255),   # bright electric yellow
        "W": (255, 255, 255, 255),  # pure white highlight
        "A": (217, 119, 6, 255),    # amber outline
        "O": (249, 115, 22, 255),   # orange
        "R": (220, 38, 38, 255),    # crimson fire
        "D": (127, 29, 29, 255),    # dark crimson
        "P": (168, 85, 247, 255),   # shadow purple
        "V": (88, 28, 135, 255),    # deep violet
        "M": (232, 121, 249, 255),  # twilight magenta
        "G": (250, 204, 21, 255),   # sun gold
        "C": (125, 249, 255, 255),  # frost / starlight cyan
        "B": (56, 189, 248, 255),   # ice blue
        "I": (14, 116, 144, 255),   # deep ice teal
        "T": (56, 217, 192, 255),   # crystal teal
        "E": (34, 197, 94, 255),    # nature emerald
        "L": (21, 128, 61, 255),    # deep leaf green
        "S": (148, 163, 184, 255),  # silver slate
        "K": (120, 88, 64, 255),    # earth bronze
        "N": (78, 54, 38, 255),     # dark earth
    }
    GLOW_CHARS = set("YWOMGCBTES")

    SYMBOLS = {
        128: [  # Lightning bolt
            "........AAAA....",
            ".......AAWWAA...",
            "......AAWWYYA...",
            ".....AAWWYYAA...",
            "....AAWWYYAA....",
            "...AAWWYYAA.....",
            "..AAWWYYYYAAAA..",
            "..AAYYYYWWYYAA..",
            "....AAAAYYWWAA..",
            "......AAYYWWAA..",
            ".....AAYYWWAA...",
            "....AAYYWWAA....",
            "...AAYYWWAA.....",
            "..AAYYWWAA......",
            "..AAYYAAA.......",
            "...AAA..........",
        ],
        144: [  # Fire flame
            ".......DD.......",
            "......DRRD......",
            ".....DRRORD.....",
            "....DRROOORD....",
            "....DROYYORD....",
            "...DRROYYOORD...",
            "...DROYYWYYRD...",
            "..DRROYYWYYORD..",
            "..DROOYYWYYOORD.",
            "..DROYYYYYYOORD.",
            "..DRROYYYYOORRD.",
            "...DRROOOOORRD..",
            "...DDRROOORRDD..",
            "....DDRRRRRDD...",
            ".....DDDDDDD....",
            "................",
        ],
        160: [  # Shadow crescent + glowing twilight eye/orb
            ".....VVVVVV.....",
            "...VVPPPPPPVV...",
            "..VPPPMMMMPPV...",
            ".VPPPM....VPV...",
            ".VPPM..MM..VV...",
            "VPPPM.MWWM......",
            "VPPPM.MWWM......",
            "VPPPM..MM.......",
            "VPPMM...........",
            ".VPPM.....VV....",
            ".VPPPM...VPV....",
            "..VPPPMMPPPVV...",
            "...VVPPPPPPVV...",
            ".....VVVVVV.....",
            "................",
            "................",
        ],
        176: [  # Radiant Sun
            ".......AA.......",
            "..A....GG....A..",
            "...A..AGGG..A...",
            "....AAGYYGAA....",
            "....AGYYYYGA....",
            "..AAGYYWWYYGAA..",
            "..AGYYWWWWYYGA..",
            ".AGGYWWWWWWYGGA.",
            "..AGYYWWWWYYGA..",
            "..AAGYYWWYYGAA..",
            "....AGYYYYGA....",
            "....AAGYYGAA....",
            "...A..AGGG..A...",
            "..A....GG....A..",
            ".......AA.......",
            "................",
        ],
        192: [  # Frost snowflake
            ".......II.......",
            ".....IICCII.....",
            "..I..ICWWCI..I..",
            "..IC.IIBBI.CI...",
            "...ICBBBBBBCI...",
            ".IIBBBBWWBBBBII.",
            ".ICCBBWWWWBBCCI.",
            "..IIWWWWWWWWII..",
            ".ICCBBWWWWBBCCI.",
            ".IIBBBBWWBBBBII.",
            "...ICBBBBBBCI...",
            "..IC.IIBBI.CI...",
            "..I..ICWWCI..I..",
            ".....IICCII.....",
            ".......II.......",
            "................",
        ],
        208: [  # Crystal prism
            ".......II.......",
            "......ITTI......",
            ".....ITWWTI.....",
            "....ITWWCCTI....",
            "...ITWCCCTTTI...",
            "..ITWCCTTTTTTI..",
            ".ITWCCTTTTTTBTI.",
            ".ITCCTTTTTTBBTI.",
            "..ITTTTTTTBBTI..",
            "...ITTTTTBBTI...",
            "....ITTTBBTI....",
            ".....ITBBTI.....",
            "......IBBI......",
            ".......II.......",
            "................",
            "................",
        ],
        224: [  # Nature leaf
            "..........LLLL..",
            "........LLEEEEL.",
            ".......LEEEWWEL.",
            "......LEEEWEEEL.",
            ".....LEEEWEEEEL.",
            "....LEEEWEEEEEL.",
            "...LEEEWEEEEEL..",
            "..LEEEWEEEEEL...",
            "..LEEWEEEEEL....",
            "..LEEWWEEEL.....",
            "..LLEEEEEL......",
            "...LLEEEL.......",
            "..LL.LLL........",
            ".LL.............",
            "................",
            "................",
        ],
        240: [  # Earth mountain / ore peak
            ".......NN.......",
            "......NSSN......",
            ".....NSWWSSN....",
            "....NSSWWSSKN...",
            "...NSSSSSSKKKN..",
            "...NSKKKKKKKRN..",
            "..NKKKWWWWKKKRN.",
            "..NKKKWWCCKKKRN.",
            ".NKKKKCCCCKKKKRN",
            ".NKKKKKKKKKKKKRN",
            "NKKKKKKNNKKKKKKN",
            "NNNNNNNNNNNNNNNN",
            "................",
            "................",
            "................",
            "................",
        ],
    }

    for u0, rows in SYMBOLS.items():
        paint_grid(img, u0, 0, rows, SYM_PALETTE)
        for ry, row in enumerate(rows):
            for rx, ch in enumerate(row):
                if ch in GLOW_CHARS and ch in SYM_PALETTE:
                    put(glow, u0 + rx, ry, SYM_PALETTE[ch])

    dest = OUT / "entity/prismling.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)
    glow.save(OUT / "entity/prismling_glowmask.png")


def make_flurrywing():
    """Flurrywing: the kids' four-winged snow-fairy snowman (qa/reference/pets/flurrywing-drawing-2026-11-17.jpg).

    Layout matches src/main/resources/assets/artilian/geo/entity/flurrywing.geo.json (box UV, 128x64),
    exported from tools/art/models/flurrywing.bbmodel. Each entry is (u, v, w, h, d).
    """
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    SNOW = (248, 252, 255, 255)
    SNOW_HI = (255, 255, 255, 255)
    SNOW_SHADE = (214, 236, 248, 255)   # pale ice-blue shade side
    SNOW_DEEP = (184, 216, 238, 255)
    OUTLINE = (45, 212, 191, 255)       # #2DD4BF, the drawing's turquoise marker
    OUTLINE_DARK = (17, 158, 142, 255)
    OUTLINE_HI = (110, 231, 214, 255)
    COAL = (36, 36, 44, 255)
    WING = (125, 249, 255, 255)         # #7DF9FF, Ice
    WING_PALE = (178, 252, 255, 255)
    VEIN = (255, 255, 255, 255)
    STICK = (122, 80, 42, 255)
    STICK_DARK = (86, 54, 26, 255)
    MITTEN = (125, 188, 240, 255)
    MITTEN_HI = (168, 214, 250, 255)
    MITTEN_DARK = (86, 150, 214, 255)

    BALLS = {
        "bottom_core": (0, 0, 10, 6, 10),
        "bottom_cap": (40, 0, 8, 8, 8),
        "middle_core": (72, 0, 6, 4, 6),
        "middle_cap": (96, 0, 4, 6, 4),
        "head_core": (0, 16, 5, 4, 5),
        "head_cap": (20, 16, 3, 5, 3),
    }
    HAT = {"hat_brim": (32, 16, 6, 1, 6), "hat_crown": (74, 16, 4, 1, 4)}
    STICKS = {"stick_upper": (100, 29, 5, 1, 1), "stick_lower": (40, 25, 1, 3, 1), "mitten_arm": (12, 32, 4, 1, 1)}
    MITTENS = {"mitten_palm": (12, 25, 3, 2, 2), "mitten_tip": (32, 25, 2, 2, 2)}
    # Wing slabs, hinge to tip. Pointed upper-left, rounded upper-right, then the two lower fans.
    UPPER_WINGS = [
        [(22, 25, 4, 3, 1), (90, 16, 6, 4, 1), (0, 25, 5, 3, 1), (88, 29, 3, 2, 1), (96, 29, 1, 2, 1)],
        [(78, 29, 4, 2, 1), (104, 16, 7, 3, 1), (56, 16, 8, 4, 1), (36, 29, 6, 2, 1), (22, 32, 4, 1, 1)],
    ]
    LOWER_WINGS = [
        [(50, 29, 6, 2, 1), (84, 25, 8, 2, 1), (44, 25, 9, 2, 1), (102, 25, 8, 2, 1), (112, 29, 5, 1, 1)],
        [(64, 29, 6, 2, 1), (0, 29, 8, 2, 1), (64, 25, 9, 2, 1), (18, 29, 8, 2, 1), (0, 32, 5, 1, 1)],
    ]

    # 1. Snowballs: white fronts, pale ice-blue sides, turquoise marker rim down each silhouette edge.
    for name, (u, v, w, h, d) in BALLS.items():
        f = faces(u, v, w, h, d)
        for face, (x, y, fw, fh) in f.items():
            base = {"top": SNOW_HI, "bottom": SNOW_DEEP, "east": SNOW_SHADE, "west": SNOW_SHADE}.get(face, SNOW)
            fill(img, (x, y, fw, fh), base)
            if face in ("front", "back", "east", "west"):
                for py in range(y, y + fh):
                    put(img, x, py, OUTLINE)
                    put(img, x + fw - 1, py, OUTLINE)
                if name.endswith("_cap"):
                    for px in range(x, x + fw):
                        put(img, px, y, OUTLINE)
                        put(img, px, y + fh - 1, OUTLINE_DARK)
                for py in range(y + 1, y + fh - 1):  # frosty sparkle
                    for px in range(x + 1, x + fw - 1):
                        if (px * 7 + py * 3) % 11 == 0:
                            put(img, px, py, SNOW_SHADE if base == SNOW else SNOW)
            elif face == "top" and name.endswith("_core"):
                for px in range(x, x + fw):
                    put(img, px, y, OUTLINE)
                    put(img, px, y + fh - 1, OUTLINE)
                for py in range(y, y + fh):
                    put(img, x, py, OUTLINE)
                    put(img, x + fw - 1, py, OUTLINE)

    # Coal buttons: the drawing's dashed line, 3 dashes on the bottom ball and 2 on the middle.
    x, y, _, _ = faces(*BALLS["bottom_core"])["front"]
    for row in (0, 2, 4):
        fill(img, (x + 4, y + row, 2, 1), COAL)
    x, y, _, _ = faces(*BALLS["middle_core"])["front"]
    for row in (0, 2):
        fill(img, (x + 2, y + row, 2, 1), COAL)

    # Coal face: two dot eyes and a small mouth.
    x, y, _, _ = faces(*BALLS["head_core"])["front"]
    fill(img, (x + 1, y, 3, 4), SNOW)
    put(img, x + 1, y + 1, COAL)
    put(img, x + 3, y + 1, COAL)
    put(img, x + 2, y + 3, COAL)

    # 2. Turquoise beanie.
    smooth_coat(img, *HAT["hat_brim"], OUTLINE_HI, OUTLINE, OUTLINE_DARK, OUTLINE_DARK)
    smooth_coat(img, *HAT["hat_crown"], OUTLINE_HI, OUTLINE_HI, OUTLINE, OUTLINE_DARK)

    # 3. Brown stick arm and ice-blue mitten.
    for box in STICKS.values():
        smooth_coat(img, *box, STICK, STICK, STICK_DARK, STICK_DARK)
    for box in MITTENS.values():
        smooth_coat(img, *box, MITTEN_HI, MITTEN, MITTEN_DARK, MITTEN_DARK)

    # 4. Frost wings: pale cyan panes with white veins, turquoise marker on every thin edge.
    for slabs in UPPER_WINGS + LOWER_WINGS:
        upper = slabs in UPPER_WINGS
        for i, (u, v, w, h, d) in enumerate(slabs):
            f = faces(u, v, w, h, d)
            for face in ("top", "bottom", "east", "west"):
                fill(img, f[face], OUTLINE)
            for face in ("front", "back"):
                x, y, fw, fh = f[face]
                fill(img, (x, y, fw, fh), WING)
                for py in range(y, y + fh):
                    for px in range(x, x + fw):
                        if (px + py) % 4 == 0:
                            put(img, px, py, VEIN)
                        elif (px - py) % 6 == 0:
                            put(img, px, py, WING_PALE)
                    put(img, x, py, OUTLINE)
                    put(img, x + fw - 1, py, OUTLINE)
                if i == len(slabs) - 1:  # the tip slab closes the wing: top edge up high, bottom edge down low
                    for px in range(x, x + fw):
                        put(img, px, y if upper else y + fh - 1, OUTLINE)

    dest = OUT / "entity/flurrywing.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)


def make_sunbeam():
    """Sunbeam: the kids' legendary Sunsteel guardian (qa/reference/pets/sunsteel-drawing-2026-11-17.jpg).

    Art pass v2:
    - Creature-like Sunsteel guardian head: dark recessed visor, angled glowing solar eyes,
      forehead sun-diamond, 3D armored beak/jaw-crest (no human smiley face), and 5-ray solar crown
    - Sculpted Sunsteel breastplate & layered chest chevron (no permanent chest orb/streamer)
    - Attack-only `chest_beam` bone (hidden at scale 0 in idle/walk/sit, blasts forward in attack)
    - Four articulated spiky solar wing-arms with 3-pronged starburst claws & chunky greaves

    Writes sunbeam.geo.json, sunbeam.png, and sunbeam_glowmask.png from a single UV layout.
    """
    import json

    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (128, 128), (0, 0, 0, 0))

    # Rich Sunsteel & solar-plasma palette
    WHITE_HOT = (255, 253, 236, 255)
    SUN_HI = (254, 224, 102, 255)
    GOLD = (245, 168, 24, 255)
    GOLD_MID = (217, 119, 16, 255)
    BRONZE = (164, 76, 18, 255)
    BRONZE_DEEP = (112, 48, 12, 255)
    SEAM = (86, 36, 10, 255)
    VISOR = (42, 20, 10, 255)
    VISOR_SHADE = (28, 12, 6, 255)

    FLAME_Y = (253, 224, 71, 255)
    FLAME_O = (249, 115, 22, 255)

    # Box UV definitions: (u, v, w, h, d) — zero overlaps on 128x128
    CHEST = (0, 0, 12, 9, 8)
    CHEST_PLATE = (42, 0, 10, 6, 1)
    PELVIS = (66, 0, 10, 3, 7)
    FLAP = (102, 0, 2, 5, 3)
    NECK = (102, 9, 5, 2, 5)

    HEAD_CORE = (0, 19, 12, 9, 10)
    HEAD_BAND = (46, 19, 14, 5, 8)
    HEAD_CAP = (92, 19, 10, 2, 8)
    HEAD_CHIN = (46, 33, 10, 1, 8)
    SNOUT = (84, 33, 6, 3, 2)
    CROWN_RAY = (102, 33, 2, 5, 2)
    CROWN_RAY_SM = (112, 33, 2, 4, 2)

    BEAM_CORE = (0, 42, 4, 4, 14)
    BEAM_FLARE = (38, 42, 6, 6, 1)

    THIGH = (0, 60, 4, 3, 4)
    SHIN = (18, 60, 4, 3, 3)
    BOOT = (34, 60, 4, 2, 6)

    PAULDRON_UP = (0, 70, 5, 5, 6)
    ARM_UP = (24, 70, 8, 3, 3)
    FORE_UP = (48, 70, 8, 3, 3)
    SPIKE_LG = (72, 70, 5, 1, 1)
    SPIKE_MD = (86, 70, 4, 1, 1)

    PAULDRON_LO = (0, 84, 4, 4, 4)
    ARM_LO = (18, 84, 8, 3, 3)
    FORE_LO = (42, 84, 7, 2, 2)
    SPIKE_SM = (62, 84, 3, 1, 1)

    def plate(box, hi, base, shade, deep, target=img, rim=SEAM):
        """Beveled Sunsteel armor plate with top highlight, side shading, and dark seam trim."""
        for face, (x, y, fw, fh) in faces(*box).items():
            for py in range(y, y + fh):
                t = (py - y) / max(1, fh - 1)
                for px in range(x, x + fw):
                    if face == "top":
                        color = hi
                    elif face == "bottom":
                        color = deep
                    elif face in ("east", "west"):
                        color = shade if t < 0.75 else deep
                    else:
                        color = hi if t < 0.18 else (base if t < 0.78 else shade)
                    put(target, px, py, color)
            if face in ("front", "back", "east", "west") and fw >= 3 and fh >= 3:
                for py in range(y, y + fh):
                    put(target, x, py, rim)
                    put(target, x + fw - 1, py, rim)
                for px in range(x, x + fw):
                    put(target, px, y + fh - 1, rim)
                    if fh >= 5:
                        put(target, px, y, rim)

    def flat(box, color, target):
        for rect in faces(*box).values():
            fill(target, rect, color)

    # 1. Armor plates
    plate(CHEST, SUN_HI, GOLD, GOLD_MID, BRONZE)
    plate(CHEST_PLATE, SUN_HI, GOLD, GOLD_MID, BRONZE)
    plate(FLAP, SUN_HI, GOLD, GOLD_MID, BRONZE)
    plate(PELVIS, GOLD, BRONZE, BRONZE_DEEP, SEAM)
    plate(NECK, BRONZE, BRONZE_DEEP, BRONZE_DEEP, SEAM)

    plate(THIGH, SUN_HI, GOLD, GOLD_MID, BRONZE)
    plate(SHIN, GOLD, BRONZE, BRONZE_DEEP, SEAM)
    plate(BOOT, SUN_HI, GOLD, GOLD_MID, BRONZE_DEEP)

    plate(PAULDRON_UP, SUN_HI, GOLD, GOLD_MID, BRONZE)
    plate(ARM_UP, GOLD, BRONZE, BRONZE_DEEP, SEAM)
    plate(FORE_UP, SUN_HI, GOLD, GOLD_MID, BRONZE)

    plate(PAULDRON_LO, SUN_HI, GOLD, GOLD_MID, BRONZE)
    plate(ARM_LO, GOLD, BRONZE, BRONZE_DEEP, SEAM)
    plate(FORE_LO, SUN_HI, GOLD, GOLD_MID, BRONZE)

    # Sculpted Sunsteel breastplate chevron & recessed sternum seam (no permanent orb)
    cx, cy, cw, ch = faces(*CHEST_PLATE)["front"]
    for i in range(4):
        put(img, cx + 1 + i, cy + 1 + i, SEAM)
        put(img, cx + cw - 2 - i, cy + 1 + i, SEAM)
        put(img, cx + 1 + i, cy + i, SUN_HI)
        put(img, cx + cw - 2 - i, cy + i, SUN_HI)
    for py in range(cy + 3, cy + ch - 1):
        put(img, cx + cw // 2 - 1, py, BRONZE)
        put(img, cx + cw // 2, py, BRONZE)
    # Subtle glowing solar-forge vents along the upper chest flanks
    for target in (img, glow):
        put(target, cx + 1, cy + 2, FLAME_Y)
        put(target, cx + cw - 2, cy + 2, FLAME_Y)

    # Backplate solar spine & shoulder vents
    bx, by, bw, bh = faces(*CHEST)["back"]
    for py in range(by + 1, by + bh - 1):
        put(img, bx + bw // 2 - 1, py, SEAM)
        put(img, bx + bw // 2, py, SEAM)
        if py % 2 == 0:
            for target in (img, glow):
                put(target, bx + bw // 2 - 2, py, FLAME_Y)
                put(target, bx + bw // 2 + 1, py, FLAME_Y)

    # Pelvis belt buckle
    px0, py0, pw, ph = faces(*PELVIS)["front"]
    fill(img, (px0 + pw // 2 - 2, py0, 4, ph), GOLD)
    for target in (img, glow):
        fill(target, (px0 + pw // 2 - 1, py0 + 1, 2, 1), FLAME_Y)

    # Pauldron ridge + glowing wing conduits
    for box in (PAULDRON_UP, PAULDRON_LO):
        for face in ("front", "back", "east", "west"):
            x, y, fw, fh = faces(*box)[face]
            for px in range(x + 1, x + fw - 1):
                put(img, px, y + 2, BRONZE)
    for box in (ARM_UP, FORE_UP, ARM_LO):
        for face in ("front", "back", "top"):
            x, y, fw, fh = faces(*box)[face]
            for px in range(x + 1, x + fw - 1):
                for target in (img, glow):
                    put(target, px, y + fh // 2, FLAME_Y if box == FORE_UP else FLAME_O)
    for face in ("front", "back", "top"):
        x, y, fw, fh = faces(*FORE_LO)[face]
        for px in range(x + 1, x + fw - 1):
            for target in (img, glow):
                put(target, px, y, FLAME_Y)

    # Boot glowing ankle trim
    x, y, fw, fh = faces(*BOOT)["front"]
    for target in (img, glow):
        fill(target, (x + 1, y, fw - 2, 1), FLAME_Y)

    # 2. Creature Head: gold helm, dark recessed visor, angled glowing solar eyes, forehead sun-gem, armored snout
    plate(HEAD_CORE, SUN_HI, GOLD, GOLD_MID, BRONZE)
    plate(HEAD_BAND, SUN_HI, GOLD, GOLD_MID, BRONZE)
    plate(HEAD_CAP, WHITE_HOT, SUN_HI, GOLD, GOLD_MID)
    plate(HEAD_CHIN, GOLD, GOLD_MID, BRONZE, BRONZE_DEEP)
    plate(SNOUT, SUN_HI, GOLD, GOLD_MID, BRONZE)

    # Snout beak-ridge detail
    sx0, sy0, sw, sh = faces(*SNOUT)["front"]
    fill(img, (sx0 + 1, sy0 + 1, sw - 2, 1), BRONZE)
    put(img, sx0 + sw // 2 - 1, sy0, WHITE_HOT)
    put(img, sx0 + sw // 2, sy0, WHITE_HOT)

    # Side ear-crests glow softly on HEAD_BAND east/west
    for face in ("east", "west"):
        x, y, fw, fh = faces(*HEAD_BAND)[face]
        for target in (img, glow):
            fill(target, (x + fw // 2 - 1, y + 1, 2, fh - 2), FLAME_Y)

    # Dark Sunsteel visor recess on HEAD_CORE front
    hx, hy, hw, hh = faces(*HEAD_CORE)["front"]
    fill(img, (hx + 1, hy + 1, hw - 2, hh - 2), VISOR)
    for px in range(hx + 1, hx + hw - 1):
        put(img, px, hy + 1, VISOR_SHADE)

    # Forehead sun-diamond (honoring the 3rd circle in the kid's drawing)
    for target in (img, glow):
        put(target, hx + hw // 2 - 1, hy + 1, FLAME_Y)
        put(target, hx + hw // 2, hy + 1, FLAME_Y)
        put(target, hx + hw // 2 - 1, hy + 2, WHITE_HOT)
        put(target, hx + hw // 2, hy + 2, WHITE_HOT)

    # Angled glowing creature eyes (fierce yet noble, no human sclera/pupils)
    # Left eye slopes down-right, right eye slopes down-left
    for target in (img, glow):
        # Left eye
        put(target, hx + 2, hy + 3, FLAME_O)
        put(target, hx + 3, hy + 3, FLAME_Y)
        put(target, hx + 2, hy + 4, FLAME_Y)
        put(target, hx + 3, hy + 4, WHITE_HOT)
        put(target, hx + 4, hy + 4, WHITE_HOT)
        # Right eye
        put(target, hx + hw - 3, hy + 3, FLAME_O)
        put(target, hx + hw - 4, hy + 3, FLAME_Y)
        put(target, hx + hw - 3, hy + 4, FLAME_Y)
        put(target, hx + hw - 4, hy + 4, WHITE_HOT)
        put(target, hx + hw - 5, hy + 4, WHITE_HOT)

    # 3. Solar Crown Rays behind/above the head (glowing!)
    for box in (CROWN_RAY, CROWN_RAY_SM):
        for target in (img, glow):
            for face, (x, y, fw, fh) in faces(*box).items():
                for py in range(y, y + fh):
                    t = (py - y) / max(1, fh - 1)
                    color = WHITE_HOT if t < 0.35 else (FLAME_Y if t < 0.75 else FLAME_O)
                    for px in range(x, x + fw):
                        put(target, px, py, color)

    # 4. Spiky Solar Starburst Claws (glowing white-hot -> gold!)
    for box in (SPIKE_LG, SPIKE_MD, SPIKE_SM):
        for target in (img, glow):
            for face, (x, y, fw, fh) in faces(*box).items():
                for py in range(y, y + fh):
                    for px in range(x, x + fw):
                        put(target, px, py, WHITE_HOT if px == x else (FLAME_Y if px < x + fw - 1 else FLAME_O))

    # 5. Attack-only Chest Solar Beam (hidden at scale 0 in idle/walk/sit)
    for target in (img, glow):
        flat(BEAM_CORE, FLAME_Y, target)
        for face, (x, y, fw, fh) in faces(*BEAM_CORE).items():
            if fw >= 3 and fh >= 3:
                fill(target, (x + 1, y + 1, fw - 2, fh - 2), WHITE_HOT)
        for face, (x, y, fw, fh) in faces(*BEAM_FLARE).items():
            fill(target, (x, y, fw, fh), FLAME_O)
            if fw >= 4 and fh >= 4:
                fill(target, (x + 1, y + 1, fw - 2, fh - 2), FLAME_Y)
            if fw >= 6 and fh >= 6:
                fill(target, (x + 2, y + 2, fw - 4, fh - 4), WHITE_HOT)

    # Save textures
    dest = OUT / "entity/sunbeam.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)
    glow.save(OUT / "entity/sunbeam_glowmask.png")

    # 6. Write matching sunbeam.geo.json so UVs and geometry can never drift
    def cube(origin, box, mirror=False, pivot=None, rotation=None):
        c = {
            "origin": origin,
            "size": [box[2], box[3], box[4]],
            "uv": [box[0], box[1]],
        }
        if mirror:
            c["mirror"] = True
        if pivot is not None:
            c["pivot"] = pivot
        if rotation is not None:
            c["rotation"] = rotation
        return c

    def wing_upper_bones(side):
        s = -1 if side == "left" else 1
        mir = side == "right"
        arm_name = f"wing_{side}_upper"
        tip_name = f"wing_{side}_upper_tip"

        def ox(x, w):
            return [x if s < 0 else -x - w]

        return [
            {
                "name": arm_name,
                "parent": "body",
                "pivot": [6 * s, 18, 0],
                "rotation": [0, 0, 10 * s],
                "cubes": [
                    cube([ox(-10, 5)[0], 15, -3], PAULDRON_UP, mirror=mir),
                    cube([ox(-17, 8)[0], 16, -1], ARM_UP, mirror=mir),
                ],
            },
            {
                "name": tip_name,
                "parent": arm_name,
                "pivot": [17 * s, 18, 0],
                "rotation": [0, 0, 32 * s],
                "cubes": [
                    cube([ox(-24, 8)[0], 16, -1], FORE_UP, mirror=mir),
                    cube([ox(-29, 5)[0], 17, 0], SPIKE_LG, mirror=mir),
                    cube(
                        [ox(-27, 4)[0], 19, 0], SPIKE_MD, mirror=mir,
                        pivot=[24 * s, 19, 0.5], rotation=[0, 0, 36 * s],
                    ),
                    cube(
                        [ox(-27, 4)[0], 16, 0], SPIKE_MD, mirror=mir,
                        pivot=[24 * s, 16, 0.5], rotation=[0, 0, -36 * s],
                    ),
                ],
            },
        ]

    def wing_lower_bones(side):
        s = -1 if side == "left" else 1
        mir = side == "right"
        arm_name = f"wing_{side}_lower"
        tip_name = f"wing_{side}_lower_tip"

        def ox(x, w):
            return x if s < 0 else -x - w

        return [
            {
                "name": arm_name,
                "parent": "body",
                "pivot": [6 * s, 10.5, 1.5],
                "rotation": [0, -14 * s, -18 * s],
                "cubes": [
                    cube([ox(-9, 4), 8.5, -0.5], PAULDRON_LO, mirror=mir),
                    cube([ox(-16, 8), 9, 0.5], ARM_LO, mirror=mir),
                ],
            },
            {
                "name": tip_name,
                "parent": arm_name,
                "pivot": [16 * s, 10.5, 1.5],
                "rotation": [0, 0, 26 * s],
                "cubes": [
                    cube([ox(-22, 7), 9.5, 0.5], FORE_LO, mirror=mir),
                    cube([ox(-26, 4), 10, 0.5], SPIKE_MD, mirror=mir),
                    cube(
                        [ox(-25, 3), 11, 0.5], SPIKE_SM, mirror=mir,
                        pivot=[22 * s, 11, 1.0], rotation=[0, 0, 38 * s],
                    ),
                    cube(
                        [ox(-25, 3), 9, 0.5], SPIKE_SM, mirror=mir,
                        pivot=[22 * s, 9.5, 1.0], rotation=[0, 0, -38 * s],
                    ),
                ],
            },
        ]

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.sunbeam",
                    "texture_width": 128,
                    "texture_height": 128,
                    "visible_bounds_width": 7,
                    "visible_bounds_height": 4.5,
                    "visible_bounds_offset": [0, 2.0, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "body",
                        "parent": "root",
                        "pivot": [0, 8, 0],
                        "cubes": [
                            cube([-5, 7, -3.5], PELVIS),
                            cube([-6, 10, -4], CHEST),
                            cube([-5, 12, -4.8], CHEST_PLATE),
                            cube([6, 10, -1.5], FLAP),
                        ],
                    },
                    {
                        "name": "chest_beam",
                        "parent": "body",
                        "pivot": [0, 14.5, -5],
                        "cubes": [
                            cube([-3, 11.5, -5.5], BEAM_FLARE),
                            cube([-2, 12.5, -19.5], BEAM_CORE),
                        ],
                    },
                    {
                        "name": "neck",
                        "parent": "body",
                        "pivot": [0, 19, 0],
                        "cubes": [cube([-2.5, 19, -2.5], NECK)],
                    },
                    {
                        "name": "head",
                        "parent": "body",
                        "pivot": [0, 21, 0],
                        "cubes": [
                            cube([-6, 21, -5], HEAD_CORE),
                            cube([-7, 23, -4], HEAD_BAND),
                            cube([-5, 30, -4], HEAD_CAP),
                            cube([-5, 20, -4], HEAD_CHIN),
                            cube([-3, 21, -6.2], SNOUT),
                            # 5-ray solar crown fanning visibly above and around the head
                            cube([-1, 31, 1], CROWN_RAY, pivot=[0, 27, 2], rotation=[0, 0, 0]),
                            cube([-1, 31, 1], CROWN_RAY_SM, pivot=[0, 26, 2], rotation=[0, 0, -28]),
                            cube([-1, 31, 1], CROWN_RAY_SM, pivot=[0, 26, 2], rotation=[0, 0, 28]),
                            cube([-1, 30, 1], CROWN_RAY_SM, pivot=[0, 25, 2], rotation=[0, 0, -56]),
                            cube([-1, 30, 1], CROWN_RAY_SM, pivot=[0, 25, 2], rotation=[0, 0, 56]),
                        ],
                    },
                    *wing_upper_bones("left"),
                    *wing_upper_bones("right"),
                    *wing_lower_bones("left"),
                    *wing_lower_bones("right"),
                    {
                        "name": "leg_left",
                        "parent": "body",
                        "pivot": [-3, 7, 0],
                        "cubes": [
                            cube([-5, 5, -2], THIGH),
                            cube([-5, 2, -1], SHIN),
                            cube([-5, 0, -3.5], BOOT),
                        ],
                    },
                    {
                        "name": "leg_right",
                        "parent": "body",
                        "pivot": [3, 7, 0],
                        "cubes": [
                            cube([1, 5, -2], THIGH, mirror=True),
                            cube([1, 2, -1], SHIN, mirror=True),
                            cube([1, 0, -3.5], BOOT, mirror=True),
                        ],
                    },
                ],
            }
        ],
    }
    geo_path = Path("src/main/resources/assets/artilian/geo/entity/sunbeam.geo.json")
    geo_path.write_text(json.dumps(geo, indent="\t") + "\n", encoding="utf-8")


def make_frogduck():
    """Frogduck: the kids' amphibious teal frog with an orange duck on its head (qa/reference/pets/frog-duck-drawing-2026-11-17.jpg).

    Layout matches src/main/resources/assets/artilian/geo/entity/frogduck.geo.json (box UV, 128x64).
    """
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    TEAL_HI = (94, 234, 212, 255)
    TEAL = (20, 184, 166, 255)
    TEAL_DEEP = (13, 148, 136, 255)
    TEAL_OUTLINE = (15, 118, 110, 255)
    FACE = (186, 248, 240, 255)
    BROWN = (120, 53, 15, 255)
    BROWN_HI = (146, 64, 14, 255)
    CLAW = (224, 252, 248, 255)

    ORANGE_HI = (253, 186, 116, 255)
    ORANGE = (249, 115, 22, 255)
    ORANGE_DEEP = (234, 88, 12, 255)
    ORANGE_OUTLINE = (194, 65, 12, 255)
    GOLD_BILL = (251, 191, 36, 255)

    def paint_box(box, base, side, top_c, outline_c):
        f = faces(*box)
        for face, (x, y, fw, fh) in f.items():
            color = {"top": top_c, "bottom": outline_c, "east": side, "west": side}.get(face, base)
            fill(img, (x, y, fw, fh), color)
            if face in ("front", "back", "east", "west"):
                for py in range(y, y + fh):
                    put(img, x, py, outline_c)
                    put(img, x + fw - 1, py, outline_c)
                for px in range(x, x + fw):
                    put(img, px, y + fh - 1, outline_c)

    # 1. Teal frog body & hump
    TORSO = (0, 0, 12, 8, 10)
    HUMP = (46, 0, 10, 1, 8)
    FACE_PLATE = (46, 10, 8, 7, 1)
    paint_box(TORSO, TEAL, TEAL_DEEP, TEAL_HI, TEAL_OUTLINE)
    paint_box(HUMP, TEAL_HI, TEAL, TEAL_HI, TEAL_OUTLINE)
    paint_box(FACE_PLATE, TEAL, TEAL_DEEP, TEAL_HI, TEAL_OUTLINE)

    # Face in the middle of the body: brown eyes, two teal dots, brown cloud below them.
    fx, fy, fw, fh = faces(*FACE_PLATE)["front"]
    fill(img, (fx + 1, fy + 1, fw - 2, fh - 2), FACE)
    # Two round brown eyes
    fill(img, (fx + 1, fy + 1, 2, 2), BROWN)
    fill(img, (fx + 5, fy + 1, 2, 2), BROWN)
    put(img, fx + 1, fy + 1, BROWN_HI)
    put(img, fx + 5, fy + 1, BROWN_HI)
    # Two teal nostril dots
    put(img, fx + 2, fy + 3, TEAL_OUTLINE)
    put(img, fx + 5, fy + 3, TEAL_OUTLINE)
    # Brown cloud shape below them
    fill(img, (fx + 2, fy + 4, 4, 2), BROWN)
    put(img, fx + 1, fy + 5, BROWN_HI)
    put(img, fx + 6, fy + 5, BROWN_HI)
    put(img, fx + 3, fy + 4, BROWN_HI)
    put(img, fx + 4, fy + 4, BROWN_HI)

    # 2. Four legs with clawed feet
    for leg in ((0, 20, 3, 5, 3), (32, 20, 3, 5, 3), (0, 30, 3, 4, 3), (32, 30, 3, 4, 3)):
        paint_box(leg, TEAL, TEAL_DEEP, TEAL_HI, TEAL_OUTLINE)
    for foot in ((14, 20, 4, 1, 4), (46, 20, 4, 1, 4), (14, 30, 4, 1, 4), (46, 30, 4, 1, 4)):
        paint_box(foot, TEAL_DEEP, TEAL_OUTLINE, TEAL, TEAL_OUTLINE)
    for face, rect in faces(66, 10, 1, 1, 1).items():
        fill(img, rect, CLAW)

    # 3. Orange duck on top of its head (part of the same creature)
    DUCK_CHEST = (84, 0, 5, 3, 5)
    DUCK_CREST = (106, 0, 2, 2, 1)
    DUCK_HEAD = (84, 10, 5, 4, 4)
    DUCK_BILL = (104, 10, 3, 1, 1)
    paint_box(DUCK_CHEST, ORANGE, ORANGE_DEEP, ORANGE_HI, ORANGE_OUTLINE)
    paint_box(DUCK_CREST, ORANGE, ORANGE_DEEP, ORANGE_HI, ORANGE_OUTLINE)
    paint_box(DUCK_HEAD, ORANGE, ORANGE_DEEP, ORANGE_HI, ORANGE_OUTLINE)
    for face, rect in faces(*DUCK_BILL).items():
        fill(img, rect, GOLD_BILL)
    # Duck slit eyes
    dx, dy, _, _ = faces(*DUCK_HEAD)["front"]
    put(img, dx + 1, dy + 1, BROWN)
    put(img, dx + 1, dy + 2, BROWN)
    put(img, dx + 3, dy + 1, BROWN)
    put(img, dx + 3, dy + 2, BROWN)

    # Outstretched orange duck wings
    for wing in ((66, 20, 6, 2, 2), (66, 26, 6, 2, 2)):
        paint_box(wing, ORANGE, ORANGE_DEEP, ORANGE_HI, ORANGE_OUTLINE)
    for tip in ((84, 20, 3, 1, 1), (84, 26, 3, 1, 1)):
        paint_box(tip, ORANGE_HI, ORANGE, ORANGE_HI, ORANGE_OUTLINE)

    dest = OUT / "entity/frogduck.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest)


def make_zapkin():
    """64x64 texture + glowmask for Zapkin (kid-drawn yellow-and-red lightning buddy)."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    glow = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    pal = {
        "Y": (250, 204, 21, 255),
        "H": (254, 240, 138, 255),
        "W": (255, 251, 235, 255),
        "R": (220, 38, 38, 255),
        "D": (153, 27, 27, 255),
        "O": (249, 115, 22, 255),
    }

    def outline_box(target, u, v, w, h, d, fill_col, edge_col, hi_col=None):
        rects = faces(u, v, w, h, d)
        for name, (rx, ry, rw, rh) in rects.items():
            for px in range(rx, rx + rw):
                for py in range(ry, ry + rh):
                    is_edge = (px == rx or px == rx + rw - 1 or py == ry or py == ry + rh - 1)
                    if is_edge:
                        put(target, px, py, edge_col)
                    elif hi_col is not None and (py == ry + 1 or (name == "top" and px == rx + 1)):
                        put(target, px, py, hi_col)
                    else:
                        put(target, px, py, fill_col)

    # Head [8, 6, 7] at (0, 0)
    outline_box(img, 0, 0, 8, 6, 7, pal["Y"], pal["R"], pal["H"])
    head_front = [
        "RRRRRRRR",
        "RWHRRHWR",
        "RYHRRHYR",
        "RYYYYYYR",
        "RYRYYRYR",
        "RRDRRDRR",
    ]
    paint_geo_box(img, 0, 0, 8, 6, 7, {"front": head_front}, pal)
    head_glow = [
        "........",
        ".WH..HW.",
        ".YH..HY.",
        "........",
        "........",
        "........",
    ]
    paint_geo_box(glow, 0, 0, 8, 6, 7, {"front": head_glow}, pal)

    # Brow flare [9, 2, 2] at (30, 0)
    outline_box(img, 30, 0, 9, 2, 2, pal["R"], pal["D"], pal["O"])

    # Lightning head spikes: bright yellow fill with crisp red outer edge
    for (u, v, w, h, d) in [
        (30, 5, 2, 3, 2),
        (39, 5, 2, 2, 2),
        (48, 5, 2, 3, 2),
        (56, 5, 2, 2, 2),
        (30, 11, 2, 3, 2),
        (39, 11, 2, 3, 2),
        (48, 11, 2, 2, 2),
    ]:
        rects = faces(u, v, w, h, d)
        for name, (rx, ry, rw, rh) in rects.items():
            for px in range(rx, rx + rw):
                for py in range(ry, ry + rh):
                    col = pal["R"] if (px == rx and name in ("front", "back")) else pal["H"]
                    if py == ry and name in ("front", "back"):
                        col = pal["W"]
                    put(img, px, py, col)
                    put(glow, px, py, pal["H"])

    # Torso waist [6, 3, 4] at (0, 18) & chest [7, 4, 5] at (20, 18)
    outline_box(img, 0, 18, 6, 3, 4, pal["Y"], pal["R"], pal["H"])
    outline_box(img, 20, 18, 7, 4, 5, pal["Y"], pal["R"], pal["H"])
    chest_front = [
        "RRRRRRR",
        "RYYWYYR",
        "RYWRWYR",
        "RRRRRRR",
    ]
    paint_geo_box(img, 20, 18, 7, 4, 5, {"front": chest_front}, pal)
    chest_glow = [
        ".......",
        "...W...",
        "..W.W..",
        ".......",
    ]
    paint_geo_box(glow, 20, 18, 7, 4, 5, {"front": chest_glow}, pal)

    # Arms [4, 2, 2] at (45, 18) and (45, 23): yellow core with red top/bottom edge
    for av in (18, 23):
        for name, (rx, ry, rw, rh) in faces(45, av, 4, 2, 2).items():
            for px in range(rx, rx + rw):
                for py in range(ry, ry + rh):
                    put(img, px, py, pal["R"] if py == ry else pal["Y"])

    # Legs [2, 5, 2] at (0, 26) and (10, 26): yellow core with red left/right outer edge
    for lu in (0, 10):
        for name, (rx, ry, rw, rh) in faces(lu, 26, 2, 5, 2).items():
            for px in range(rx, rx + rw):
                for py in range(ry, ry + rh):
                    put(img, px, py, pal["R"] if (px == rx or py == ry + rh - 1) else pal["Y"])

    # Feet [3, 2, 4] at (0, 34) and (15, 34)
    outline_box(img, 0, 34, 3, 2, 4, pal["Y"], pal["R"], pal["H"])
    outline_box(img, 15, 34, 3, 2, 4, pal["Y"], pal["R"], pal["H"])

    # Round yellow-and-red orb hands: core [4, 4, 4] + ring [3, 3, 5]
    for (cu, ru) in [(0, 41), (32, 41)]:
        outline_box(img, cu, 41, 4, 4, 4, pal["H"], pal["R"], pal["W"])
        outline_box(glow, cu, 41, 4, 4, 4, pal["H"], (0, 0, 0, 0), pal["W"])
    for ru in (16, 48):
        outline_box(img, ru, 41, 3, 3, 5, pal["Y"], pal["R"], pal["H"])

    entity_dir = OUT / "entity"
    entity_dir.mkdir(parents=True, exist_ok=True)
    img.save(entity_dir / "zapkin.png")
    glow.save(entity_dir / "zapkin_glowmask.png")


def main():
    import sys

    if "--frogduck" in sys.argv:
        make_frogduck()
        print("Generated Frogduck 128x64 texture.")
        return
    if "--sunbeam" in sys.argv:
        make_sunbeam()
        print("Generated Sunbeam 128x128 texture and glowmask.")
        return
    if "--prismling" in sys.argv:
        make_prismling()
        print("Generated Prismling 128x128 texture and glowmask.")
        return
    make_crystal_fox()
    make_shadow_wisp()
    make_cinder_crag()
    make_zaphox()
    make_galecorn()
    make_ember_drake()
    make_peepkin()
    make_prismling()
    make_flurrywing()
    make_sunbeam()
    make_frogduck()
    make_zapkin()

    item_dir = OUT / "item"
    item_dir.mkdir(parents=True, exist_ok=True)
    draw_crystal((32, 26, 48, 255), (50, 42, 72, 255), (84, 72, 114, 255)).save(item_dir / "capture_crystal.png")
    draw_crystal(STAR_DARK, STAR_MAIN, STAR_HI).save(item_dir / "capture_crystal_filled.png")
    draw_crystal(STAR_DARK, STAR_BRIGHT, (255, 255, 255, 255), ring_color=STAR_BRIGHT).save(
        item_dir / "capture_crystal_summoned.png"
    )
    draw_crystal(GOLD_DARK, GOLD, GOLD_HI).save(item_dir / "capture_crystal_fainted.png")
    make_biome_treat().save(item_dir / "biome_treat.png")
    print("Generated pet textures (Crystal Fox, Shadow Wisp, Cinder Crag, Zaphox, Galecorn, Ember Drake, Peepkin, Prismling, Flurrywing).")


if __name__ == "__main__":
    main()
