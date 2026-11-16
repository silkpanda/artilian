"""Generates clean placeholder textures for Phase 2 biomes.

Run from the project root:
    python tools/art/make_biomes_art.py

Covers:
- Phase 2 Stage 1: Crystal Canopy Woods (crystal_turf, crystal_canopy_*, crystal_bloom)
- Phase 2 Stage 2: Emberbloom Meadows (ember_turf, emberwood_*, ember_bloom) &
  Starlight Highlands (starlight_turf, starlight_slate, starlight_blossom)

Final hand-polished biome art happens in Phase 2 Stage 7.
"""
from pathlib import Path
from PIL import Image

BLOCK_TEX = Path("src/main/resources/assets/artilian/textures/block")

# Shared dark & highlight tones
SLATE_DEEP = (32, 24, 44, 255)
SLATE_DARK = (43, 35, 56, 255)      # #2B2338
SLATE_MID = (58, 47, 77, 255)       # #3A2F4D
ARCANE_TRIM = (106, 90, 140, 255)   # #6A5A8C
ARCANE_LIGHT = (142, 124, 178, 255)
ARCANE_HI = (180, 169, 200, 255)    # #B4A9C8

TEAL_HI = (210, 255, 250, 255)
TEAL_GLOW = (125, 249, 255, 255)    # #7DF9FF
TEAL_BRIGHT = (91, 231, 208, 255)
TEAL_MAIN = (56, 217, 192, 255)     # #38D9C0
TEAL_SHADE = (32, 158, 154, 255)
TEAL_DARK = (24, 98, 108, 255)
TEAL_OUTLINE = (16, 56, 66, 255)

DIRT_LIGHT = (134, 96, 67, 255)
DIRT_MAIN = (112, 78, 53, 255)
DIRT_DARK = (89, 61, 41, 255)
DIRT_DEEP = (68, 45, 30, 255)

MAGENTA_HI = (245, 165, 255, 255)
MAGENTA_MAIN = (217, 70, 239, 255)  # #D946EF
GOLD_CENTER = (255, 246, 214, 255)  # #FFF6D6

# Emberbloom Meadows warm palette (#FF9F38 Sunsteel Gold + sunset ember)
EMBER_OUTLINE = (72, 34, 20, 255)
EMBER_BARK_DEEP = (58, 26, 20, 255)
EMBER_BARK_DARK = (82, 38, 28, 255)
EMBER_BARK_MID = (108, 52, 36, 255)
EMBER_BARK_LIGHT = (140, 72, 46, 255)
EMBER_PLANK_SHADE = (146, 78, 36, 255)
EMBER_PLANK_MAIN = (196, 114, 52, 255)
EMBER_PLANK_HI = (228, 148, 72, 255)
EMBER_GOLD_HI = (255, 232, 152, 255)
EMBER_GOLD_GLOW = (255, 209, 102, 255)
EMBER_GOLD_BRIGHT = (255, 180, 68, 255)
EMBER_MAIN = (255, 159, 56, 255)    # #FF9F38
EMBER_SHADE = (218, 108, 42, 255)
EMBER_DARK = (166, 70, 32, 255)
EMBER_CORAL = (255, 94, 58, 255)
EMBER_STEM = (116, 134, 48, 255)
EMBER_STEM_DARK = (82, 98, 34, 255)


def put(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def draw_pixel_grid(rows, palette):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in palette:
                put(img, x, y, palette[ch])
    return img


def fill_dirt(img):
    for y in range(16):
        for x in range(16):
            n = (x * 11 + y * 17 + (x ^ y) * 7) % 23
            if n in (0, 1, 2):
                put(img, x, y, DIRT_LIGHT)
            elif n in (3, 4, 5, 6):
                put(img, x, y, DIRT_DARK)
            elif n == 7:
                put(img, x, y, DIRT_DEEP)
            else:
                put(img, x, y, DIRT_MAIN)


def fill_starlight_slate(img):
    for y in range(16):
        band = y // 4
        for x in range(16):
            n = (x * 13 + y * 9 + (x ^ y) * 5 + band * 3) % 29
            if y % 4 == 0 and (x + band * 5) % 9 == 0:
                put(img, x, y, SLATE_DEEP)
            elif n in (0, 1, 2):
                put(img, x, y, SLATE_DEEP)
            elif n in (3, 4, 5, 6, 7):
                put(img, x, y, SLATE_MID)
            elif n in (8, 9):
                put(img, x, y, ARCANE_TRIM)
            elif n == 10:
                put(img, x, y, TEAL_GLOW if (x + y) % 2 == 0 else ARCANE_LIGHT)
            else:
                put(img, x, y, SLATE_DARK)


def make_turf_top(pal):
    img = Image.new("RGBA", (16, 16), pal["main"])
    for y in range(16):
        for x in range(16):
            n = (x * 7 + y * 13 + (x ^ y) * 5) % 29
            if n in (0, 1, 2, 3):
                put(img, x, y, pal["bright"])
            elif n in (4, 5, 6, 7, 8):
                put(img, x, y, pal["shade"])
            elif n in (9, 10):
                put(img, x, y, pal["dark"])
            elif n == 11:
                put(img, x, y, pal["glow"])
    for sx, sy in [(2, 3), (11, 2), (6, 10), (13, 13)]:
        put(img, sx, sy, pal["hi"])
    return img


def make_turf_side(pal, under_fill_fn):
    img = Image.new("RGBA", (16, 16), SLATE_DARK)
    under_fill_fn(img)
    fringe = [4, 3, 5, 4, 3, 4, 5, 4, 3, 4, 5, 4, 3, 4, 5, 3]
    for x in range(16):
        depth = fringe[x]
        for y in range(depth):
            if y == 0:
                put(img, x, y, pal["bright"] if x % 3 != 0 else pal["glow"])
            elif y == depth - 1:
                put(img, x, y, pal["dark"])
            elif (x + y) % 4 == 0:
                put(img, x, y, pal["shade"])
            else:
                put(img, x, y, pal["main"])
    return img


def make_log(bark_pal):
    img = Image.new("RGBA", (16, 16), bark_pal["dark"])
    for y in range(16):
        for x in range(16):
            col = x % 4
            if col == 0:
                c = bark_pal["deep"]
            elif col == 1:
                c = bark_pal["mid"] if (x + y * 3) % 5 != 0 else bark_pal["accent"]
            elif col == 2:
                c = bark_pal["dark"]
            else:
                c = bark_pal["mid"]
            put(img, x, y, c)
    for x, ys in [(2, range(2, 7)), (6, range(9, 15)), (10, range(1, 6)), (14, range(7, 13))]:
        for y in ys:
            put(img, x, y, bark_pal["vein"] if y % 2 == 0 else bark_pal["vein_glow"])
            put(img, (x + 1) % 16, y, bark_pal["vein_shade"])
    return img


def make_log_top(ring_pal):
    img = Image.new("RGBA", (16, 16), ring_pal[0])
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d > 6.5:
                put(img, x, y, ring_pal[0])
            elif d > 5.2:
                put(img, x, y, ring_pal[1])
            elif d > 4.0:
                put(img, x, y, ring_pal[2])
            elif d > 2.8:
                put(img, x, y, ring_pal[3])
            elif d > 1.6:
                put(img, x, y, ring_pal[4])
            else:
                put(img, x, y, ring_pal[5])
    put(img, 7, 7, ring_pal[6])
    put(img, 8, 8, ring_pal[6])
    return img


def make_planks(plank_pal):
    img = Image.new("RGBA", (16, 16), plank_pal["main"])
    plank_seams = {0, 4, 8, 12}
    joint_x = {0: 7, 1: 2, 2: 11, 3: 5}
    for y in range(16):
        row_idx = y // 4
        for x in range(16):
            if y in plank_seams or x == joint_x[row_idx]:
                put(img, x, y, plank_pal["seam"])
            elif y % 4 == 1:
                put(img, x, y, plank_pal["light"] if (x + row_idx) % 4 != 0 else plank_pal["hi"])
            elif y % 4 == 3:
                put(img, x, y, plank_pal["seam"] if (x * 3 + y) % 7 == 0 else plank_pal["main"])
            else:
                put(img, x, y, plank_pal["fleck"] if (x + y * 5) % 11 == 0 else plank_pal["main"])
    return img


def make_leaves(leaf_pal):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            n = (x * 13 + y * 7 + (x ^ y) * 3) % 17
            if n in (1, 5, 11):
                continue
            elif n in (0, 6):
                put(img, x, y, leaf_pal["glow"])
            elif n in (2, 7, 12):
                put(img, x, y, leaf_pal["bright"])
            elif n in (3, 8, 13, 15):
                put(img, x, y, leaf_pal["main"])
            elif n in (4, 9, 14):
                put(img, x, y, leaf_pal["shade"])
            else:
                put(img, x, y, leaf_pal["dark"])
    return img


def make_starlight_slate():
    img = Image.new("RGBA", (16, 16), SLATE_DARK)
    fill_starlight_slate(img)
    return img


CRYSTAL_TURF_PAL = {
    "hi": TEAL_HI, "glow": TEAL_GLOW, "bright": TEAL_BRIGHT,
    "main": TEAL_MAIN, "shade": TEAL_SHADE, "dark": TEAL_DARK,
}
EMBER_TURF_PAL = {
    "hi": EMBER_GOLD_HI, "glow": EMBER_CORAL, "bright": EMBER_GOLD_GLOW,
    "main": EMBER_GOLD_BRIGHT, "shade": EMBER_MAIN, "dark": EMBER_SHADE,
}
STARLIGHT_TURF_PAL = {
    "hi": TEAL_GLOW, "glow": MAGENTA_MAIN, "bright": ARCANE_HI,
    "main": ARCANE_LIGHT, "shade": ARCANE_TRIM, "dark": SLATE_MID,
}


def main():
    BLOCK_TEX.mkdir(parents=True, exist_ok=True)
    outputs = {
        # Crystal Canopy Woods (Stage 1)
        "crystal_turf_top.png": make_turf_top(CRYSTAL_TURF_PAL),
        "crystal_turf_side.png": make_turf_side(CRYSTAL_TURF_PAL, fill_dirt),
        "crystal_canopy_log.png": make_log({
            "deep": SLATE_DEEP, "dark": SLATE_DARK, "mid": SLATE_MID, "accent": ARCANE_TRIM,
            "vein": TEAL_MAIN, "vein_glow": TEAL_GLOW, "vein_shade": TEAL_DARK,
        }),
        "crystal_canopy_log_top.png": make_log_top(
            [SLATE_DEEP, SLATE_MID, ARCANE_TRIM, ARCANE_LIGHT, TEAL_SHADE, TEAL_MAIN, TEAL_GLOW]
        ),
        "crystal_canopy_planks.png": make_planks({
            "main": ARCANE_TRIM, "seam": SLATE_MID, "light": ARCANE_LIGHT, "hi": ARCANE_HI, "fleck": TEAL_SHADE,
        }),
        "crystal_canopy_leaves.png": make_leaves({
            "glow": TEAL_GLOW, "bright": TEAL_BRIGHT, "main": TEAL_MAIN, "shade": TEAL_SHADE, "dark": TEAL_DARK,
        }),
        "crystal_canopy_sapling.png": draw_pixel_grid([
            "................",
            "......oooo......",
            "....ooghggoo....",
            "...ogbbmbbmgo...",
            "..ogbmhbbmbmgo..",
            "..ogbmmbmbmsmo..",
            "...omsmsmsmso...",
            "....oooddooo....",
            "......oSSo......",
            ".....ogSSgo.....",
            "......oSSo......",
            "......oSSo......",
            "......oTTo......",
            ".....ooTToo.....",
            "....oTTTTTTo....",
            "................",
        ], {
            "o": TEAL_OUTLINE, "g": TEAL_GLOW, "h": TEAL_HI, "b": TEAL_BRIGHT,
            "m": TEAL_MAIN, "s": TEAL_SHADE, "d": TEAL_DARK, "S": ARCANE_TRIM, "T": SLATE_MID,
        }),
        "crystal_bloom.png": draw_pixel_grid([
            "................",
            ".......oo.......",
            "......oMHo......",
            "....ooMGGMoo....",
            "...oMHGGGGHMo...",
            "...oMGGCCGGMo...",
            "....ooMGGMoo....",
            ".....oMHHMo.....",
            "......oYYo......",
            "....ooogg.......",
            "...ogmmo.ogg....",
            "....oosoogmo....",
            "......ooso......",
            "......oYYo......",
            ".....ooYYoo.....",
            "................",
        ], {
            "o": SLATE_DEEP, "M": MAGENTA_MAIN, "H": MAGENTA_HI, "G": TEAL_GLOW,
            "C": GOLD_CENTER, "Y": TEAL_SHADE, "g": TEAL_BRIGHT, "m": TEAL_MAIN, "s": TEAL_DARK,
        }),
        # Emberbloom Meadows (Stage 2)
        "ember_turf_top.png": make_turf_top(EMBER_TURF_PAL),
        "ember_turf_side.png": make_turf_side(EMBER_TURF_PAL, fill_dirt),
        "emberwood_log.png": make_log({
            "deep": EMBER_BARK_DEEP, "dark": EMBER_BARK_DARK, "mid": EMBER_BARK_MID, "accent": EMBER_BARK_LIGHT,
            "vein": EMBER_MAIN, "vein_glow": EMBER_GOLD_GLOW, "vein_shade": EMBER_DARK,
        }),
        "emberwood_log_top.png": make_log_top(
            [EMBER_BARK_DEEP, EMBER_BARK_MID, EMBER_BARK_LIGHT, EMBER_DARK, EMBER_SHADE, EMBER_MAIN, EMBER_GOLD_GLOW]
        ),
        "emberwood_planks.png": make_planks({
            "main": EMBER_PLANK_MAIN, "seam": EMBER_PLANK_SHADE, "light": EMBER_PLANK_HI,
            "hi": EMBER_GOLD_BRIGHT, "fleck": EMBER_DARK,
        }),
        "emberwood_leaves.png": make_leaves({
            "glow": EMBER_GOLD_GLOW, "bright": EMBER_GOLD_BRIGHT, "main": EMBER_MAIN,
            "shade": EMBER_CORAL, "dark": EMBER_SHADE,
        }),
        "emberwood_sapling.png": draw_pixel_grid([
            "................",
            "................",
            "...oooooooooo...",
            "..ogbbbbbbbmgo..",
            ".ogbmhhbbbmbmgo.",
            ".osmmbbbbmmccso.",
            "..ooossddsssoo..",
            "......oTTo......",
            ".....ooTTo......",
            "......oTTTo.....",
            ".......oTTo.....",
            "......oTTo......",
            "......oTTo......",
            ".....ooTToo.....",
            "....oTTTTTTo....",
            "................",
        ], {
            "o": EMBER_OUTLINE, "g": EMBER_GOLD_GLOW, "h": EMBER_GOLD_HI, "b": EMBER_GOLD_BRIGHT,
            "m": EMBER_MAIN, "c": EMBER_CORAL, "s": EMBER_SHADE, "d": EMBER_DARK, "T": EMBER_BARK_MID,
        }),
        "ember_bloom.png": draw_pixel_grid([
            "................",
            "......oooo......",
            "....ooCGGoo.....",
            "...oCGGGGGBo....",
            "..oCGHCCCCGBo...",
            "...oCGBCCGBo....",
            "....ooCGGoo.....",
            "......oooo......",
            "......oLLo......",
            "....ooogg.......",
            "...ogLLo.ogg....",
            "....ooDDoogLo...",
            "......oLLo......",
            "......oDDo......",
            ".....ooDDoo.....",
            "................",
        ], {
            "o": EMBER_OUTLINE, "C": EMBER_CORAL, "G": EMBER_GOLD_GLOW, "B": EMBER_MAIN,
            "H": GOLD_CENTER, "L": EMBER_STEM, "D": EMBER_STEM_DARK, "g": EMBER_GOLD_BRIGHT,
        }),
        # Starlight Highlands (Stage 2)
        "starlight_turf_top.png": make_turf_top(STARLIGHT_TURF_PAL),
        "starlight_turf_side.png": make_turf_side(STARLIGHT_TURF_PAL, fill_starlight_slate),
        "starlight_slate.png": make_starlight_slate(),
        "starlight_blossom.png": draw_pixel_grid([
            "................",
            ".......oo.......",
            "......oHGo......",
            "....ooGGGGoo....",
            "..ooMGGGGGGMoo..",
            ".oMHGGCCCCGGHMo.",
            "..ooMGGGGGGMoo..",
            "....ooMGGMoo....",
            ".....oMHHMo.....",
            "......oYYo......",
            "...oooAGG.ooo...",
            "..oAAYYo..oAYo..",
            "....ooYYoYYo....",
            "......oYYo......",
            ".....ooYYoo.....",
            "................",
        ], {
            "o": SLATE_DEEP, "M": MAGENTA_MAIN, "H": TEAL_HI, "G": TEAL_GLOW,
            "C": GOLD_CENTER, "Y": ARCANE_TRIM, "A": TEAL_MAIN,
        }),
    }
    for name, img in outputs.items():
        path = BLOCK_TEX / name
        img.save(path)
        print(f"Wrote {path}")


if __name__ == "__main__":
    main()
