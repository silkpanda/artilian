"""Draws 16x16 block textures (_side, _top, _bottom) for Phase 1 Custom TNTs.
Run from the project root:

    python tools/art/make_tnt_art.py

Uses the DESIGN.md palette (#FF9F38 Sunsteel Gold, #38D9C0 Starlight Teal,
#D946EF Twilight Magenta, #4CD964 Slime Green, #FF4D5A Coral, #2B2338 Deep Slate).
"""
from pathlib import Path
from PIL import Image

BLOCK_TEX_DIR = Path("src/main/resources/assets/artilian/textures/block")

# Core DESIGN.md palette
SLATE_DARK = (24, 18, 34, 255)
SLATE = (43, 35, 56, 255)        # #2B2338 Deep Artilian Slate
SLATE_LIGHT = (74, 62, 94, 255)
WHITE = (255, 255, 255, 255)
CREAM = (255, 246, 214, 255)     # #FFF6D6
CREAM_SHADE = (232, 218, 178, 255)

MAGENTA_HI = (245, 162, 255, 255)
MAGENTA = (217, 70, 239, 255)    # #D946EF
MAGENTA_DARK = (144, 30, 168, 255)

CORAL_HI = (255, 146, 154, 255)
CORAL = (255, 77, 90, 255)       # #FF4D5A
CORAL_DARK = (174, 38, 52, 255)

GOLD_HI = (255, 222, 128, 255)
GOLD = (255, 159, 56, 255)       # #FF9F38
GOLD_DARK = (188, 92, 18, 255)

EMERALD_HI = (164, 252, 174, 255)
EMERALD = (76, 217, 100, 255)    # #4CD964
EMERALD_MID = (34, 197, 94, 255) # #22C55E
EMERALD_DARK = (18, 118, 52, 255)

CYAN_HI = (232, 255, 255, 255)
CYAN = (125, 249, 255, 255)      # #7DF9FF
TEAL = (56, 217, 192, 255)       # #38D9C0
TEAL_DARK = (22, 118, 120, 255)
INDIGO_DARK = (32, 24, 54, 255)
INDIGO = (58, 44, 92, 255)

EMBER_HI = (255, 176, 86, 255)
EMBER = (249, 115, 22, 255)      # #F97316
EMBER_DARK = (166, 52, 20, 255)


def put(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def fill(img, x, y, w, h, color):
    for px in range(x, x + w):
        for py in range(y, y + h):
            put(img, px, py, color)


def make_sticks_side(stick_palettes):
    """Creates a 16x16 side texture with 4 cylindrical TNT sticks and crisp end-caps."""
    img = Image.new("RGBA", (16, 16), SLATE_DARK)
    for idx in range(4):
        x0 = idx * 4
        hi, main, dark = stick_palettes[idx % len(stick_palettes)]
        fill(img, x0, 0, 4, 16, main)
        fill(img, x0, 1, 1, 14, hi)
        fill(img, x0 + 3, 1, 1, 14, dark)
        # Subtle mid-stick shading & crisp dark top/bottom cap rim
        fill(img, x0, 0, 4, 1, SLATE_DARK)
        fill(img, x0, 15, 4, 1, SLATE_DARK)
        put(img, x0 + 1, 0, hi)
        put(img, x0 + 2, 0, main)
        put(img, x0 + 1, 15, dark)
        put(img, x0 + 2, 15, dark)
    return img


def make_grid_cap(stick_palettes, wick_color, center_color, rim_color=SLATE_DARK, corner_accents=None):
    """Creates a 16x16 top or bottom cap with bundled stick ends and recessed center."""
    img = Image.new("RGBA", (16, 16), SLATE_DARK)
    for row in range(4):
        for col in range(4):
            x0, y0 = col * 4, row * 4
            hi, main, dark = stick_palettes[(row + col) % len(stick_palettes)]
            fill(img, x0, y0, 4, 4, main)
            fill(img, x0, y0, 3, 1, hi)
            fill(img, x0, y0, 1, 3, hi)
            fill(img, x0 + 3, y0, 1, 4, dark)
            fill(img, x0, y0 + 3, 4, 1, dark)
            put(img, x0 + 1, y0 + 1, SLATE)

    # Center strap & fuse socket
    fill(img, 5, 5, 6, 6, rim_color)
    fill(img, 6, 6, 4, 4, center_color)
    if wick_color is not None:
        fill(img, 7, 7, 2, 2, wick_color)
        put(img, 7, 6, WHITE)
        put(img, 8, 7, WHITE)
    if corner_accents:
        for cx, cy in ((2, 2), (12, 2), (2, 12), (12, 12)):
            fill(img, cx, cy, 2, 2, corner_accents)
    return img


def emblem_band(side, band, trim, rows, colors):
    """Wraps the sticks in a tall band (rows 3 to 12) and draws one 8x8 emblem in its middle.

    The emblem is the block's whole identity from a distance, so it is as big as the face
    allows: one shape, eight pixels across.
    """
    fill(side, 0, 3, 16, 10, band)
    fill(side, 0, 3, 16, 1, trim)
    fill(side, 0, 12, 16, 1, trim)
    assert len(rows) == 8, "emblem must be 8 rows"
    for y, row in enumerate(rows):
        assert len(row) == 8, f"emblem row {y} must be 8 wide"
        for x, ch in enumerate(row):
            if ch != ".":
                put(side, 4 + x, 4 + y, colors[ch])


def make_party_tnt():
    palettes = [
        (MAGENTA_HI, MAGENTA, MAGENTA_DARK),
        (CORAL_HI, CORAL, CORAL_DARK),
        (GOLD_HI, GOLD, GOLD_DARK),
        (CYAN, TEAL, TEAL_DARK),
    ]
    side = make_sticks_side(palettes)
    # Cream wrapper with a big firework burst.
    emblem_band(side, CREAM, SLATE, [
        "...MM...",
        "...MM...",
        "..MCCM..",
        "MMCGGCMM",
        "MMCGGCMM",
        "..MCCM..",
        "...MM...",
        "...MM...",
    ], {"M": MAGENTA, "C": CORAL, "G": GOLD_HI})
    # Confetti either side of the burst.
    for x, y, c in (
        (1, 5, TEAL), (2, 8, CORAL), (1, 10, MAGENTA),
        (14, 5, MAGENTA), (13, 8, TEAL), (14, 10, GOLD),
    ):
        put(side, x, y, c)

    top = make_grid_cap(palettes, GOLD_HI, CREAM, corner_accents=MAGENTA_HI)
    bottom = make_grid_cap(palettes, None, SLATE_LIGHT)
    return side, top, bottom


def make_bounce_tnt():
    palettes = [
        (EMERALD_HI, EMERALD, EMERALD_DARK),
        (EMERALD, EMERALD_MID, EMERALD_DARK),
    ]
    side = make_sticks_side(palettes)
    # Dark band with one big "up" arrow.
    emblem_band(side, SLATE, SLATE_DARK, [
        "...WW...",
        "..WHHW..",
        ".WHHHHW.",
        "WHHHHHHW",
        "...EE...",
        "...EE...",
        "...EE...",
        "...EE...",
    ], {"W": WHITE, "H": EMERALD_HI, "E": EMERALD})

    top = make_grid_cap(palettes, EMERALD_HI, SLATE, rim_color=EMERALD_MID, corner_accents=EMERALD_HI)
    bottom = make_grid_cap(palettes, None, EMERALD_DARK)
    return side, top, bottom


def make_frost_nova_tnt():
    palettes = [
        (CYAN_HI, CYAN, TEAL_DARK),
        (CYAN, TEAL, TEAL_DARK),
    ]
    side = make_sticks_side(palettes)
    # Twilight-indigo band with one big snowflake.
    emblem_band(side, INDIGO_DARK, TEAL_DARK, [
        "C..HH..C",
        ".C.HH.C.",
        "..CHHC..",
        "HHHWWHHH",
        "HHHWWHHH",
        "..CHHC..",
        ".C.HH.C.",
        "C..HH..C",
    ], {"W": WHITE, "H": CYAN_HI, "C": CYAN})

    top = make_grid_cap(palettes, CYAN_HI, INDIGO, rim_color=CYAN, corner_accents=CYAN_HI)
    bottom = make_grid_cap(palettes, None, INDIGO_DARK)
    return side, top, bottom


def make_cluster_quarry_tnt():
    palettes = [
        (GOLD_HI, GOLD, GOLD_DARK),
        (EMBER_HI, EMBER, EMBER_DARK),
    ]
    side = make_sticks_side(palettes)
    # Dark band with four round bomblets, two by two.
    emblem_band(side, SLATE, SLATE_DARK, [
        "EEE..EEE",
        "EYE..EYE",
        "EEE..EEE",
        "........",
        "........",
        "EEE..EEE",
        "EYE..EYE",
        "EEE..EEE",
    ], {"E": EMBER, "Y": GOLD_HI})

    top = make_grid_cap(palettes, WHITE, SLATE_DARK, rim_color=GOLD_DARK, corner_accents=EMBER_HI)
    bottom = make_grid_cap(palettes, None, SLATE)
    return side, top, bottom


def make_starlight_gravity_tnt():
    palettes = [
        (MAGENTA_HI, MAGENTA, MAGENTA_DARK),
        (CYAN_HI, TEAL, TEAL_DARK),
    ]
    side = make_sticks_side(palettes)
    emblem_band(side, INDIGO_DARK, MAGENTA_DARK, [
        "..HHHH..",
        ".H....H.",
        "H.M..M.H",
        "H..WW..H",
        "H..WW..H",
        "H.M..M.H",
        ".H....H.",
        "..HHHH..",
    ], {"W": WHITE, "H": CYAN_HI, "M": MAGENTA_HI})

    top = make_grid_cap(palettes, CYAN_HI, INDIGO, rim_color=MAGENTA, corner_accents=CYAN_HI)
    bottom = make_grid_cap(palettes, None, INDIGO_DARK)
    return side, top, bottom


def make_brimstone_meteor_tnt():
    palettes = [
        (CORAL_HI, CORAL, CORAL_DARK),
        (GOLD_HI, EMBER, EMBER_DARK),
    ]
    side = make_sticks_side(palettes)
    emblem_band(side, SLATE_DARK, CORAL_DARK, [
        "...YY...",
        "..YEEY..",
        ".YERREY.",
        "YERWWREY",
        "YERWWREY",
        ".YERREY.",
        "..YEEY..",
        "...YY...",
    ], {"W": WHITE, "Y": GOLD_HI, "E": EMBER, "R": CORAL})

    top = make_grid_cap(palettes, GOLD_HI, SLATE_DARK, rim_color=CORAL, corner_accents=GOLD_HI)
    bottom = make_grid_cap(palettes, None, SLATE)
    return side, top, bottom


def main():
    BLOCK_TEX_DIR.mkdir(parents=True, exist_ok=True)
    variants = {
        "party_tnt": make_party_tnt(),
        "bounce_tnt": make_bounce_tnt(),
        "frost_nova_tnt": make_frost_nova_tnt(),
        "cluster_quarry_tnt": make_cluster_quarry_tnt(),
        "starlight_gravity_tnt": make_starlight_gravity_tnt(),
        "brimstone_meteor_tnt": make_brimstone_meteor_tnt(),
    }
    for name, (side, top, bottom) in variants.items():
        side.save(BLOCK_TEX_DIR / f"{name}_side.png")
        top.save(BLOCK_TEX_DIR / f"{name}_top.png")
        bottom.save(BLOCK_TEX_DIR / f"{name}_bottom.png")
        print(f"Generated {name}_side/top/bottom.png")


if __name__ == "__main__":
    main()
