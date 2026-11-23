"""Draws placeholder block, item, and armor textures for Sunsteel and Starlight Crystal.
Run from the project root:

    python tools/art/make_ores_and_gear_art.py

Uses the DESIGN.md palette (#FF9F38 Sunsteel Gold, #38D9C0 Starlight Teal).
Hand-drawn art replaces this output in the Phase 1 Stage 6 art pass.
"""
import json
from pathlib import Path

from PIL import Image

TEX_ROOT = Path("src/main/resources/assets/artilian/textures")
GEO_ROOT = Path("src/main/resources/assets/artilian/geo/item/armor")

# Stone & deepslate bases (DESIGN.md + vanilla-compatible grayscale contrast)
STONE_LIGHT = (142, 142, 142, 255)
STONE = (127, 127, 127, 255)
STONE_DARK = (104, 104, 104, 255)
STONE_DEEP = (84, 84, 84, 255)

DEEPSLATE_LIGHT = (92, 92, 99, 255)
DEEPSLATE = (73, 73, 79, 255)
DEEPSLATE_DARK = (55, 55, 61, 255)
DEEPSLATE_DEEP = (40, 40, 45, 255)

# Sunsteel palette (#FF9F38 primary)
SUN_HI = (255, 246, 214, 255)      # #FFF6D6 warm highlight
SUN_BRIGHT = (255, 200, 96, 255)
SUN_MAIN = (255, 159, 56, 255)     # #FF9F38 Sunsteel Gold
SUN_SHADE = (212, 114, 28, 255)
SUN_DARK = (140, 66, 16, 255)
SUN_OUTLINE = (72, 34, 10, 255)

# Starlight Crystal palette (#38D9C0 primary)
STAR_HI = (224, 255, 252, 255)
STAR_BRIGHT = (125, 249, 255, 255) # #7DF9FF bright glow
STAR_MAIN = (56, 217, 192, 255)    # #38D9C0 Starlight Teal
STAR_SHADE = (32, 158, 154, 255)
STAR_DARK = (24, 98, 108, 255)
STAR_OUTLINE = (18, 48, 62, 255)

# Sunsteel accents (Ember Crimson solar core + burnished bronze trim)
SUN_GEM_HI = (255, 160, 122, 255)
SUN_GEM = (232, 72, 56, 255)        # Ember Crimson
SUN_GEM_DARK = (150, 32, 34, 255)

# Starlight Crystal accents (Ice-Steel trim for celestial armor)
STAR_TRIM_HI = (196, 236, 246, 255)  # #C4ECF6 pale ice glint
STAR_TRIM = (52, 140, 166, 255)      # #348CA6 ice-teal
STAR_TRIM_DARK = (26, 44, 78, 255)   # #1A2C4E deep navy outline

# Prismatic Brimstone palette (Tier 3 Boss-Forged: Obsidian-Plum + Prism Magenta/Cyan + Brimstone Gold)
PRISM_HI = (240, 250, 255, 255)      # Radiant prism edge
PRISM_BRIGHT = (217, 70, 239, 255)   # #D946EF Twilight Magenta
PRISM_MAIN = (126, 58, 178, 255)     # Royal prism violet
PRISM_SHADE = (66, 36, 98, 255)      # Deep arcane plum
PRISM_DARK = (38, 24, 62, 255)       # Obsidian-plum plate
PRISM_OUTLINE = (20, 14, 34, 255)    # Deepest void-slate outline
PRISM_TRIM_HI = (255, 224, 138, 255) # Brimstone gold glint
PRISM_TRIM = (232, 93, 42, 255)      # Brimstone ember trim
PRISM_TRIM_DARK = (118, 30, 24, 255) # Deep crimson trim

# Dragonscale palette (Phase 5 Dragon Jungle: Obsidian-Jungle + Canopy Emerald + Ember Amber)
DRAGON_HI = (176, 255, 198, 255)      # Bright emerald scale glint
DRAGON_BRIGHT = (74, 222, 128, 255)   # #4ADE80 luminous canopy emerald
DRAGON_MAIN = (34, 197, 94, 255)      # #22C55E Canopy Emerald
DRAGON_SHADE = (20, 128, 68, 255)     # Deep jungle scale
DRAGON_DARK = (18, 66, 44, 255)       # Dark forest plate
DRAGON_OUTLINE = (12, 34, 26, 255)    # Obsidian-jungle outline
DRAGON_TRIM_HI = (253, 224, 71, 255)  # #FDE047 solar-amber glint
DRAGON_TRIM = (249, 115, 22, 255)     # #F97316 Ember Amber trim
DRAGON_TRIM_DARK = (146, 52, 14, 255) # Deep burnt-ember trim

# Twin-Star palette (Phase 5 Stage 9 Endgame: Deep Artilian Slate + Starlight Teal + Eye Twilight Magenta & Ember Gold)
TWIN_HI = (232, 252, 255, 255)        # Radiant starlight glint
TWIN_BRIGHT = (125, 249, 255, 255)    # #7DF9FF Pupil/Starlight glow
TWIN_MAIN = (56, 217, 192, 255)       # #38D9C0 Starlight Teal
TWIN_SHADE = (126, 58, 178, 255)      # Twilight violet transition
TWIN_DARK = (43, 35, 56, 255)         # #2B2338 Deep Artilian Slate anchor
TWIN_OUTLINE = (22, 16, 32, 255)      # Deepest cosmic-slate outline
TWIN_TRIM_HI = (255, 246, 214, 255)   # #FFF6D6 Twin-Star highlight
TWIN_TRIM = (255, 159, 56, 255)       # #FF9F38 Sunsteel/Ember Gold trim
TWIN_TRIM_DARK = (217, 70, 239, 255)  # #D946EF Twilight Magenta inner trim

# Handle wood / dark slate accents for tools
HANDLE_LIGHT = (148, 102, 46, 255)
HANDLE_MAIN = (112, 74, 28, 255)
HANDLE_DARK = (68, 42, 16, 255)
SLATE_TRIM = (43, 35, 56, 255)     # #2B2338 Deep Artilian Slate
SLATE_MID = (74, 64, 92, 255)


def put(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def fill(img, x, y, w, h, color):
    for px in range(x, x + w):
        for py in range(y, y + h):
            put(img, px, py, color)


def paint_pixels(img, rows, palette):
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != "." and ch in palette:
                put(img, x, y, palette[ch])


def stone_base(deepslate=False):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    c_light, c_mid, c_dark, c_deep = (
        (DEEPSLATE_LIGHT, DEEPSLATE, DEEPSLATE_DARK, DEEPSLATE_DEEP)
        if deepslate
        else (STONE_LIGHT, STONE, STONE_DARK, STONE_DEEP)
    )
    fill(img, 0, 0, 16, 16, c_mid)
    for y in range(16):
        for x in range(16):
            n = (x * 7 + y * 13 + (x ^ y) * 5) % 19
            if n in (0, 1):
                put(img, x, y, c_light)
            elif n in (2, 3, 4):
                put(img, x, y, c_dark)
            elif n == 5:
                put(img, x, y, c_deep)
    if deepslate:
        for y in (3, 7, 11, 15):
            for x in range(16):
                if (x + y) % 3 != 0:
                    put(img, x, y, c_dark)
    return img


def make_sunsteel_ore(filename, deepslate, p):
    img = stone_base(deepslate=deepslate)
    op = {
        **p,
        "r": SUN_GEM,       # #E84838 Ember Crimson solar corona accent
        "R": SUN_GEM_DARK,  # #962022 deep crimson-bronze socket shade
        "g": SUN_GEM_HI,    # #FFA07A warm solar flare glow
    }
    sunsteel_ore_rows = [
        "................",
        "..d..d..d.......",
        "...b.b.m....d...",
        "....rmr....dbd..",
        "...bhhhm....o...",
        ".dbmhhhmsd......",
        "...mhhhs........",
        "....rsr..d..d..o",
        "...m.s.s..b.b.m.",
        "..o..o..o.bhhms.",
        "........dbmhhhso",
        "..d.......mhhhs.",
        ".dbd......m.s.s.",
        "..o......o..o..o",
        "................",
        "................",
    ]
    paint_checked(img, sunsteel_ore_rows, op, filename)
    img.save(TEX_ROOT / "block" / filename)


def make_starlight_ore(filename, deepslate, p):
    img = stone_base(deepslate=deepslate)
    op = {
        **p,
        "g": SUN_HI,          # #FFF6D6 warm white-gold star core
        "v": STAR_TRIM_HI,    # #C4ECF6 bright Ice-Steel glint
        "p": STAR_TRIM,       # #348CA6 Ice-Teal socket
        "P": STAR_TRIM_DARK,  # #1A2C4E deep Astral socket rim
    }
    starlight_ore_rows = [
        "................",
        ".....P......p...",
        "....Php....vbp..",
        "....pbp...pbgmp.",
        "..Ppvbhvp..pmv..",
        ".PhbbgbbmP..P...",
        "..Ppvmbsp.......",
        "....pmp....P....",
        "....PsP...Php...",
        ".....P...pvbmP..",
        "...p....PhbgbmP.",
        "..vbp....pvmsp..",
        ".pbgmp....psp...",
        "..psv......P....",
        "...P............",
        "................",
    ]
    paint_pixels(img, starlight_ore_rows, op)
    img.save(TEX_ROOT / "block" / filename)


def make_sunsteel_storage_block(filename):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    sp = {
        "h": SUN_HI,          # #FFF6D6
        "b": SUN_BRIGHT,      # #FFC860
        "m": SUN_MAIN,        # #FF9F38
        "s": SUN_SHADE,       # #D4721C
        "d": SUN_DARK,        # #8C4210
        "o": SUN_OUTLINE,     # #48220A
        "g": SUN_GEM_HI,      # #FFA07A
        "r": SUN_GEM,         # #E84838 Ember Crimson
        "R": SUN_GEM_DARK,    # #962022 deep crimson-bronze
    }
    rows = [
        "hbbbbbbbbbbbbbbd",
        "hbmmmmmmmmmmmmmd",
        "bmRbbbbhhbbbbRms",
        "bmrRmmshhsmmRrms",
        "bmmRhbbhhbbhRmms",
        "bmssbRrmmrRbsmms",
        "bmshbrmhhmrbhmms",
        "bmhhhmhhhhmhhhms",
        "bmhhhmhhhhmhhhms",
        "bmssbrmhhmrbsmms",
        "bmssbRrmmrRbssms",
        "bmmRhbbhhbbhRmms",
        "bmrRsmdhhdmsRrms",
        "bmRssssddssssRms",
        "dssssssssssssssd",
        "dddddddddddddddd",
    ]
    paint_checked(img, rows, sp, "sunsteel_block")
    img.save(TEX_ROOT / "block" / filename)


def make_starlight_storage_block(filename):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    bp = {
        "h": STAR_HI,          # #E0FFFC
        "b": STAR_BRIGHT,      # #7DF9FF
        "m": STAR_MAIN,        # #38D9C0
        "s": STAR_SHADE,       # #209E9A
        "d": STAR_DARK,        # #18626C
        "g": SUN_HI,           # #FFF6D6 warm white-gold star core
        "v": STAR_TRIM_HI,     # #C4ECF6 bright Ice-Steel
        "p": STAR_TRIM,        # #348CA6 Ice-Teal
        "P": STAR_TRIM_DARK,   # #1A2C4E deep Ice-Teal
    }
    rows = [
        "hbbbbbbbbbbbbbbp",
        "hbmmmmmmmmmmmmsp",
        "bmvbbbbbbbbbbvmP",
        "bmpmmmmhhmmmmpdP",
        "bmmPPpphhpPPpsdP",
        "bmmPpvbhhbvpPsdP",
        "bmmpvbhggbhvpsdP",
        "bmhhbggggggbbmdP",
        "bmhhbggggggbbmdP",
        "bmppvmhggmhvpsdP",
        "bmsPpvmbbsvpPsdP",
        "bmsPPppsspPPpsdP",
        "bmpssssdssssspdp",
        "bsvssssssssssvdp",
        "psdddddddddddddp",
        "PPPPPPPPPPPPPPPP",
    ]
    paint_checked(img, rows, bp, "starlight_crystal_block")
    img.save(TEX_ROOT / "block" / filename)


def make_materials():
    # Refined 14x14 8-rayed Sun piece of the Sunsteel ore cluster
    raw_palette = {
        "0": SUN_OUTLINE,               # #48220A deep bronze lower-right outline
        "1": SUN_DARK,                  # #8C4210 dark bronze upper-left outline
        "2": (180, 84, 20, 255),        # #B45414 rich copper-gold shadow
        "3": SUN_SHADE,                 # #D4721C warm amber-gold shade
        "4": (238, 138, 42, 255),       # #EE8A2A mid-amber gold
        "5": SUN_MAIN,                  # #FF9F38 Sunsteel Gold primary
        "6": SUN_BRIGHT,                # #FFC860 bright solar gold
        "7": (255, 224, 150, 255),      # #FFE096 pale sunbeam gold
        "8": SUN_HI,                    # #FFF6D6 warm white-gold solar core
        "r": SUN_GEM,                   # #E84838 Ember Crimson solar corona ring
        "R": SUN_GEM_DARK,              # #962022 deep crimson-bronze corona shade
        "g": SUN_GEM_HI,                # #FFA07A warm solar flare glow
    }
    raw_rows = [
        "................",
        ".......11.......",
        "..11..1761..11..",
        "..171.1750.150..",
        "...1656765540...",
        "....16rggr50....",
        "..116r8888r530..",
        ".177g888888g540.",
        ".165g888888r430.",
        "..035r8888R420..",
        "....15rRRr30....",
        "...1544543320...",
        "..150.0530.030..",
        "..00..0420..00..",
        ".......00.......",
        "................",
    ]
    raw = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_checked(raw, raw_rows, raw_palette, "raw_sunsteel")
    raw.save(TEX_ROOT / "item" / "raw_sunsteel.png")

    # Exact vanilla gold_ingot / iron_ingot 16x16 silhouette mapped to the 8-step Sunsteel ramp
    ingot_palette = {
        "0": SUN_OUTLINE,               # #48220A
        "1": SUN_DARK,                  # #8C4210
        "2": (180, 84, 20, 255),        # #B45414
        "3": SUN_SHADE,                 # #D4721C
        "4": (238, 138, 42, 255),       # #EE8A2A
        "5": SUN_MAIN,                  # #FF9F38
        "6": SUN_BRIGHT,                # #FFC860
        "7": SUN_HI,                    # #FFF6D6
    }
    ingot_rows = [
        "................",
        "................",
        "..........11....",
        ".......111441...",
        "....1115555551..",
        ".11145555555540.",
        "1655555555556650",
        "1465555556665240",
        "1456556665322240",
        "1454765322223440",
        "135453222234400.",
        ".134532233000...",
        "..13433000......",
        "...1100.........",
        "................",
        "................",
    ]
    ingot = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_checked(ingot, ingot_rows, ingot_palette, "sunsteel_ingot")
    ingot.save(TEX_ROOT / "item" / "sunsteel_ingot.png")

    # Exact vanilla diamond 16x16 faceted gem silhouette mapped to Starlight Crystal Ore colors
    gem_palette = {
        "0": STAR_TRIM_DARK,            # #1A2C4E deep Astral outline
        "1": STAR_TRIM,                 # #348CA6 Ice-Teal upper-left rim
        "2": (30, 90, 116, 255),        # #1E5A74 deep ice-teal rim shade
        "3": STAR_SHADE,                # #209E9A Starlight Teal shade
        "4": (43, 199, 172, 255),       # #2BC7AC deep teal-cyan
        "5": STAR_MAIN,                 # #38D9C0 Starlight Teal main
        "6": (94, 234, 212, 255),       # #5EEAD4 bright teal-cyan
        "7": STAR_BRIGHT,               # #7DF9FF Starlight Cyan bright
        "8": (165, 248, 255, 255),      # #A5F8FF soft starlight cyan
        "9": STAR_HI,                   # #E0FFFC pale starlight glint
        "v": STAR_TRIM_HI,              # #C4ECF6 bright Ice-Steel inner-rim accent
        "g": SUN_HI,                    # #FFF6D6 warm white-gold star core highlight
    }
    gem_rows = [
        "................",
        ".......11.......",
        "......1971......",
        "......1960......",
        ".....198650.....",
        "....1v8765v0....",
        "..11987gg65400..",
        ".19987gggg65430.",
        ".16556gggg54320.",
        "..05567gg54320..",
        "....0v6543v0....",
        ".....065430.....",
        "......0530......",
        "......0430......",
        ".......00.......",
        "................",
    ]
    gem = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    paint_checked(gem, gem_rows, gem_palette, "starlight_crystal")
    gem.save(TEX_ROOT / "item" / "starlight_crystal.png")


def make_tool_set(prefix, mat_palette):
    # Exact vanilla oak stick handle ramp paired with material-specific ore/gem blade ramp
    tp = {
        **mat_palette,
        "u": (40, 30, 11, 255),   # #281E0B
        "v": (73, 54, 21, 255),   # #493615
        "w": (104, 78, 30, 255),  # #684E1E
        "x": (137, 103, 39, 255), # #896727
    }
    sword = [
        ".............ooo",
        "............ohhO",
        "...........ohmhO",
        "..........ohmhO.",
        ".........ohmbO..",
        "........ohmbO...",
        "..oo...obmbO....",
        "..odo.obgbO.....",
        "...osObmbO......",
        "...ossdbO.......",
        "....odoO........",
        "...vwOooO.......",
        "..vxu.OOoO......",
        "oowu....OO......",
        "odO.............",
        "OOO.............",
    ]
    pickaxe = [
        "................",
        "................",
        "......ooooo.....",
        ".....ohbmmbovw..",
        "......oOOOmgxu..",
        "..........vgmO..",
        ".........vwumbO.",
        "........vxu.OmO.",
        ".......vwu..OmO.",
        "......vxu...ObO.",
        ".....vwu....OhO.",
        "....vxu......O..",
        "...vwu..........",
        "..vxu...........",
        "..uu............",
        "................",
    ]
    axe = [
        "................",
        ".........oo.....",
        "........ohho....",
        ".......ohmbo....",
        "......ohmgmvw...",
        "......Ohbgsmu...",
        ".......OOvmsmO..",
        "........vwummO..",
        ".......vxu.OO...",
        "......vwu.......",
        ".....vwu........",
        "....vxu.........",
        "...vwu..........",
        "..vxu...........",
        "..uu............",
        "................",
    ]
    shovel = [
        "................",
        "................",
        "...........ooO..",
        "..........ohhmO.",
        ".........ohbghO.",
        "........ohbgbhO.",
        ".........vmbhO..",
        "........vxuhO...",
        ".......vxu.O....",
        "......vwu.......",
        ".....vxu........",
        "....vwu.........",
        "..vvxu..........",
        "..vxu...........",
        "...uu...........",
        "................",
    ]
    hoe = [
        "................",
        ".......ooo......",
        "......ohgbo.....",
        ".......OOmbovw..",
        ".........Ommxu..",
        "..........vbmO..",
        ".........vwuO...",
        "........vxu.....",
        ".......vwu......",
        "......vxu.......",
        ".....vwu........",
        "....vxu.........",
        "...vwu..........",
        "..vxu...........",
        "..uu............",
        "................",
    ]
    for kind, rows in (
        ("sword", sword),
        ("pickaxe", pickaxe),
        ("axe", axe),
        ("shovel", shovel),
        ("hoe", hoe),
    ):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        paint_checked(img, rows, tp, f"{prefix}_{kind}")
        img.save(TEX_ROOT / "item" / f"{prefix}_{kind}.png")


def paint_checked(img, rows, palette, name="pattern"):
    assert len(rows) == img.height, f"{name}: expected {img.height} rows, got {len(rows)}"
    for y, row in enumerate(rows):
        assert len(row) == img.width, f"{name} row {y}: expected width {img.width}, got {len(row)}"
        for x, ch in enumerate(row):
            if ch != ".":
                assert ch in palette, f"{name}: unknown palette key {ch!r}"
                put(img, x, y, palette[ch])


def paint_patch(img, ox, oy, w, h, rows, palette, name="patch"):
    assert len(rows) == h, f"{name}: expected {h} rows, got {len(rows)}"
    for dy, row in enumerate(rows):
        assert len(row) == w, f"{name} row {dy}: expected width {w}, got {len(row)}"
        for dx, ch in enumerate(row):
            if ch != ".":
                assert ch in palette, f"{name}: unknown palette key {ch!r}"
                put(img, ox + dx, oy + dy, palette[ch])


def paint_box_uv(img, u, v, w, h, d, box_faces, palette, name="box"):
    """Paints a Bedrock/GeckoLib Box UV cuboid (w x h x d) rooted at (u, v)."""
    layout = {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "outer": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "inner": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }
    for face_key, (fx, fy, fw, fh) in layout.items():
        if face_key in box_faces:
            paint_patch(img, fx, fy, fw, fh, box_faces[face_key], palette, f"{name}_{face_key}")


def make_sunsteel_armor_icons(p):
    ap = {
        **p,
        "T": SLATE_TRIM,
        "M": SLATE_MID,
        "r": SUN_GEM,
        "R": SUN_GEM_DARK,
        "g": SUN_GEM_HI,
    }
    helmet = [
        "......oooo......",
        ".....ohrrbo.....",
        ".oo..ohbbbo..oo.",
        ".ohboobbbmoobdo.",
        ".obmohmmmmmsbdo.",
        ".osTmmmmmmmmsTo.",
        "..ooTTTTTTTTsoo.",
        "..obmTTmmTTmsdo.",
        "..obmmoTTommsdo.",
        "..osddo..oddddo.",
        "...ooo....ooo...",
        "................",
        "................",
        "................",
        "................",
        "................",
    ]
    chestplate = [
        "................",
        ".ooooo....ooooo.",
        "ohbbmmo..ommbdmo",
        "obmmmoo..oommdso",
        "oosmmbhhhhbmmsdo",
        "..oobhgrrgbmsdo.",
        "..oobmRRRRbmsdo.",
        "..obmmbhhbmmsdo.",
        "..oTTTgrrgTTTdo.",
        "..oTMmddddmMTdo.",
        "..osddo..oddddo.",
        "...ooo....ooo...",
        "................",
        "................",
        "................",
        "................",
    ]
    leggings = [
        "................",
        "..oooooooooooo..",
        ".oTTTTgrrgTTTTo.",
        ".ohbbmddddmbmso.",
        ".obmmmmddmmmmdo.",
        ".ommmsdTTdsmmdo.",
        ".omsdo....osmmo.",
        ".ohbbo....ohbbo.",
        ".ogrmo....ogrmo.",
        ".osddo....osddo.",
        ".omsdo....omsdo.",
        ".osddo....osddo.",
        "..oTo......oTo..",
        "..ooo......ooo..",
        "................",
        "................",
    ]
    boots = [
        "................",
        "................",
        "................",
        "..oo........oo..",
        ".ohbo......obho.",
        ".obmoooooooobmo.",
        ".osTbmmoobmmsTo.",
        ".obmmddoobmmddo.",
        ".omsdo....omsdo.",
        "oomsdo....omsdoo",
        "obmmddo..obmmddo",
        "ogrrddo..ogrrddo",
        "oTTTTTo..oTTTTTo",
        ".ooooo....ooooo.",
        "................",
        "................",
    ]
    for piece, rows in (
        ("helmet", helmet),
        ("chestplate", chestplate),
        ("leggings", leggings),
        ("boots", boots),
    ):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        paint_checked(img, rows, ap, f"sunsteel_{piece}")
        img.save(TEX_ROOT / "item" / f"sunsteel_{piece}.png")


def make_starlight_armor_icons(p, prefix="starlight", trim=STAR_TRIM, trim_dark=STAR_TRIM_DARK, trim_hi=STAR_TRIM_HI):
    ap = {
        **p,
        "p": trim,
        "P": trim_dark,
        "v": trim_hi,
    }
    helmet = [
        ".......oo.......",
        ".o....ohbo....o.",
        ".oh..oobboo..mo.",
        ".obmoohbbhoomdo.",
        ".opbmbbhhbbmdpo.",
        ".oPPmmmmmmmmdPo.",
        "..oPbhmmmmhbPo..",
        "..opbhvbbvhbpo..",
        "..oPboooooobPo..",
        "..obpo....opbo..",
        "..osdo....odso..",
        "...oo......oo...",
        "................",
        "................",
        "................",
        "................",
    ]
    chestplate = [
        ".oo..........oo.",
        "ohbo........obmo",
        "obmpo..oo..opmdo",
        "oPpmmooooooppPdo",
        ".oPbmvhhhhvmbPo.",
        "..oombhhhhbmdo..",
        "..opmvhbbhvmdo..",
        "...oPmbhhbmPo...",
        "...oPPmmmmPPo...",
        "....oPbbbbPo....",
        "....oPsmmsPo....",
        ".....oPddPo.....",
        "......oooo......",
        "................",
        "................",
        "................",
    ]
    leggings = [
        "................",
        "..oooooooooooo..",
        ".oPPPpvhhvpPPPo.",
        ".ohbbmpvvpmbbmo.",
        ".obmmmpPPpmmsdo.",
        ".ommspooooosmmo.",
        ".omsPo....oPsmo.",
        ".opbho....ohbpo.",
        ".opmmo....ommpo.",
        ".oPsdo....odsPo.",
        ".omsPo....oPsmo.",
        ".oPddo....oddPo.",
        "..ooo......ooo..",
        "................",
        "................",
        "................",
    ]
    boots = [
        "................",
        "................",
        "..oo........oo..",
        ".ohbo......obho.",
        ".obmpo....opmbo.",
        ".osPPo....oPPso.",
        ".opbmo....ombpo.",
        ".opmdo....omdpo.",
        ".oPsdo....odsPo.",
        "oohbmdo..ombmhoo",
        "obhhhdo..ohhhbdo",
        "oPPPPPo..oPPPPPo",
        ".ooooo....ooooo.",
        "................",
        "................",
        "................",
    ]
    for piece, rows in (
        ("helmet", helmet),
        ("chestplate", chestplate),
        ("leggings", leggings),
        ("boots", boots),
    ):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        paint_checked(img, rows, ap, f"{prefix}_{piece}")
        img.save(TEX_ROOT / "item" / f"{prefix}_{piece}.png")


def faces(u, v, w, h, d):
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }


def make_sunsteel_armor_layers(p):
    # Solar Paladin Knight armor: T-visor greathelm + 3D golden solar crest & swept sun-wings (hat u=32),
    # ember sunburst cuirass with layered pauldrons, riveted poleyn leggings, winged sabatons.
    ap = {
        **p,
        "T": SLATE_TRIM,
        "M": SLATE_MID,
        "r": SUN_GEM,
        "R": SUN_GEM_DARK,
        "g": SUN_GEM_HI,
    }
    l1 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    # Head base (u=0, v=0, 8x8x8)
    head_top = [
        "dssbbssd",
        "sbmhhmbs",
        "smbmmsms",
        "bhrmmrhh",
        "bhrmmrhh",
        "smbmmsms",
        "sbmhhmbs",
        "TbbbbbbT",
    ]
    head_front = [
        "TbbbbbbT",
        "sbmgrbsm",
        "sTTTTTTs",
        "sm.mm.ms",
        "sm.mm.ms",
        "sbmTTmbs",
        "ssd..dss",
        "TTT..TTT",
    ]
    head_side_l = [
        "TbbbbbbT",
        "bmhggmbs",
        "mmbmsmdT",
        "mmbmmsdT",
        "mbbmssdd",
        "ssbmsddd",
        "dddTTddd",
        "TTTTTT..",
    ]
    head_side_r = [row[::-1] for row in head_side_l]
    head_back = [
        "TmmmmmmT",
        "bmgrrgms",
        "smmmmmms",
        "smbmmsms",
        "sdmmmmdd",
        "sddTTdds",
        "dddddddd",
        "TTTTTTTT",
    ]
    paint_patch(l1, 8, 0, 8, 8, head_top, ap, "sun_head_top")
    paint_patch(l1, 0, 8, 8, 8, head_side_r, ap, "sun_head_east")
    paint_patch(l1, 8, 8, 8, 8, head_front, ap, "sun_head_front")
    paint_patch(l1, 16, 8, 8, 8, head_side_l, ap, "sun_head_west")
    paint_patch(l1, 24, 8, 8, 8, head_back, ap, "sun_head_back")

    # Hat outer 3D layer (u=32, v=0, 8x8x8): 3D central solar crest + swept-back sun-wings
    hat_top = [
        "...rg...",
        "...hb...",
        "...rg...",
        "...hb...",
        "...rg...",
        "...hb...",
        "..hrrh..",
        "..hbbh..",
    ]
    hat_front = [
        "...hg...",
        ".bbhgbb.",
        "h......h",
        "........",
        "........",
        "........",
        "........",
        "........",
    ]
    hat_side_l = [
        "...hbh..",
        "bbbbbhb.",
        ".sbbbms.",
        "...sbms.",
        "....sd..",
        "........",
        "........",
        "........",
    ]
    hat_side_r = [row[::-1] for row in hat_side_l]
    hat_back = [
        "...rg...",
        "...hb...",
        "...rg...",
        "...sd...",
        "........",
        "........",
        "........",
        "........",
    ]
    paint_patch(l1, 40, 0, 8, 8, hat_top, ap, "sun_hat_top")
    paint_patch(l1, 32, 8, 8, 8, hat_side_r, ap, "sun_hat_east")
    paint_patch(l1, 40, 8, 8, 8, hat_front, ap, "sun_hat_front")
    paint_patch(l1, 48, 8, 8, 8, hat_side_l, ap, "sun_hat_west")
    paint_patch(l1, 56, 8, 8, 8, hat_back, ap, "sun_hat_back")

    # Body (u=16, v=16, 8x12x4)
    body_top = [
        "bb....bb",
        "bm....mb",
        "ms....sm",
        "dd....dd",
    ]
    body_front = [
        "bm....mb",
        "bmhbbhmb",
        "bhgrrghb",
        "bmRRRRmb",
        "smbhbbms",
        "sbmmsbbs",
        "sdmmmmdd",
        "sTTmrTTs",
        "TTTgrTTT",
        "sbmmmmbs",
        "ssdd.dds",
        "TTT..TTT",
    ]
    body_back = [
        "bm....mb",
        "bmmmmmmb",
        "smbhbhms",
        "smgrrgms",
        "smmmmmms",
        "sdmmmmdd",
        "sddmmdds",
        "TTTTTTTT",
        "TTmmmmTT",
        "sbmmmmbs",
        "ssd..dds",
        "TT....TT",
    ]
    body_side = [
        "bmms",
        "bmmm",
        "smms",
        "smms",
        "sdms",
        "sdds",
        "dddd",
        "TTTT",
        "TTTT",
        "smms",
        "sdds",
        "TT..",
    ]
    paint_patch(l1, 20, 16, 8, 4, body_top, ap, "sun_body_top")
    paint_patch(l1, 16, 20, 4, 12, body_side, ap, "sun_body_east")
    paint_patch(l1, 20, 20, 8, 12, body_front, ap, "sun_body_front")
    paint_patch(l1, 28, 20, 4, 12, body_side, ap, "sun_body_west")
    paint_patch(l1, 32, 20, 8, 12, body_back, ap, "sun_body_back")

    # Arms / Rounded Paladin Pauldrons (u=40, v=16, 4x12x4)
    arm_top = [
        "shbs",
        "hbbm",
        "brms",
        "ssdd",
    ]
    arm_outer = [
        "shbs",
        "brgb",
        "bRmb",
        "smms",
        "sdds",
        "TTTT",
        "....",
        "....",
        "....",
        "....",
        "....",
        "....",
    ]
    arm_front = [
        "bmbT",
        "hhbT",
        "bbms",
        "smms",
        "sdds",
        "TTTT",
        "....",
        "....",
        "....",
        "....",
        "....",
        "....",
    ]
    arm_inner = [
        "TT..",
        "ms..",
        "ds..",
        "TT..",
        "....",
        "....",
        "....",
        "....",
        "....",
        "....",
        "....",
        "....",
    ]
    arm_back = [row[::-1] for row in arm_front]
    paint_patch(l1, 44, 16, 4, 4, arm_top, ap, "sun_arm_top")
    paint_patch(l1, 40, 20, 4, 12, arm_outer, ap, "sun_arm_outer")
    paint_patch(l1, 44, 20, 4, 12, arm_front, ap, "sun_arm_front")
    paint_patch(l1, 48, 20, 4, 12, arm_inner, ap, "sun_arm_inner")
    paint_patch(l1, 52, 20, 4, 12, arm_back, ap, "sun_arm_back")

    # Boots (u=0, v=16, 4x12x4 bottom half + ankle wings)
    boot_bottom = [
        "TTTT",
        "TMMT",
        "TMMT",
        "TTTT",
    ]
    boot_front = [
        "....",
        "....",
        "....",
        "....",
        "....",
        ".bh.",
        "bhbb",
        "smms",
        "smms",
        "bgrb",
        "dmmd",
        "TTTT",
    ]
    boot_outer = [
        "....",
        "....",
        "....",
        "....",
        "....",
        "hb..",
        "hbms",
        "mbms",
        "smms",
        "sdms",
        "dddd",
        "TTTT",
    ]
    boot_inner = [
        "....",
        "....",
        "....",
        "....",
        "....",
        "....",
        "TmmT",
        "smms",
        "smms",
        "sdds",
        "dddd",
        "TTTT",
    ]
    paint_patch(l1, 8, 16, 4, 4, boot_bottom, ap, "sun_boot_bottom")
    paint_patch(l1, 0, 20, 4, 12, boot_outer, ap, "sun_boot_outer")
    paint_patch(l1, 4, 20, 4, 12, boot_front, ap, "sun_boot_front")
    paint_patch(l1, 8, 20, 4, 12, boot_inner, ap, "sun_boot_inner")
    paint_patch(l1, 12, 20, 4, 12, boot_inner, ap, "sun_boot_back")

    l1.save(TEX_ROOT / "models" / "armor" / "sunsteel_layer_1.png")

    # Layer 2: Sunsteel Leggings (slate & ember belt + gold cuisses + knee poleyns)
    l2 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    l2_belt_front = [
        "TTgrrgTT",
        "TbmRRmbT",
        "sdmmmmdd",
        "T.dddd.T",
        "........",
    ]
    l2_belt_back = [
        "TTTTTTTT",
        "TTmmmmTT",
        "sdmmmmdd",
        "T.dddd.T",
        "........",
    ]
    l2_belt_side = [
        "TTTT",
        "TmmT",
        "smms",
        "sdds",
        "....",
    ]
    paint_patch(l2, 16, 27, 4, 5, l2_belt_side, ap, "sun_l2_side1")
    paint_patch(l2, 20, 27, 8, 5, l2_belt_front, ap, "sun_l2_front")
    paint_patch(l2, 28, 27, 4, 5, l2_belt_side, ap, "sun_l2_side2")
    paint_patch(l2, 32, 27, 8, 5, l2_belt_back, ap, "sun_l2_back")

    leg_front = [
        "hbbs",
        "bmms",
        "smms",
        "sdds",
        "bhhb",
        "mgrs",
        "sdms",
        "sdds",
        "TTdT",
        "....",
        "....",
        "....",
    ]
    leg_side = [
        "smms",
        "bmmm",
        "smms",
        "sdds",
        "sTTs",
        "smms",
        "smms",
        "sdds",
        "dddd",
        "....",
        "....",
        "....",
    ]
    leg_inner = [
        "TTms",
        "smms",
        "smms",
        "TTds",
        "TTss",
        "smms",
        "sdds",
        "dddd",
        "TT..",
        "....",
        "....",
        "....",
    ]
    paint_patch(l2, 0, 20, 4, 12, leg_side, ap, "sun_leg_outer")
    paint_patch(l2, 4, 20, 4, 12, leg_front, ap, "sun_leg_front")
    paint_patch(l2, 8, 20, 4, 12, leg_inner, ap, "sun_leg_inner")
    paint_patch(l2, 12, 20, 4, 12, leg_side, ap, "sun_leg_back")
    l2.save(TEX_ROOT / "models" / "armor" / "sunsteel_layer_2.png")
    return l1, l2, ap


def make_starlight_armor_layers(p, prefix="starlight", trim=STAR_TRIM, trim_dark=STAR_TRIM_DARK, trim_hi=STAR_TRIM_HI):
    # Celestial / Astral Archon armor: Open-face Circlet + 3D Crystal Spire Horns (hat u=32),
    # Ice-Teal trimmed diamond-core cuirass with spiky crystal pauldrons & bracers,
    # Ice-Teal sash leggings, and crystal-finned ice boots.
    ap = {
        **p,
        "p": trim,
        "P": trim_dark,
        "v": trim_hi,
        "g": SUN_HI,
    }
    l1 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    # Head base: Celestial Circlet/Diadem (leaves top center & lower face open!)
    head_top = [
        "PppbbppP",
        "p......p",
        "P......P",
        "p......p",
        "p......p",
        "P......P",
        "pbmmmmbp",
        "PvbhhbvP",
    ]
    head_front = [
        "PpmhhmpP",
        "pbvhhbvp",
        "Ps.hh.sP",
        "p......p",
        "P......P",
        "........",
        "........",
        "........",
    ]
    head_side_l = [
        "PvbhhbvP",
        "pmmmmmbp",
        "Ppmmmbvp",
        "PPsbmms.",
        "PPssmd..",
        ".P..d...",
        "........",
        "........",
    ]
    head_side_r = [row[::-1] for row in head_side_l]
    head_back = [
        "PppbbppP",
        "pmvhhvmp",
        "PpsmmspP",
        ".PpmmpP.",
        "..PPPP..",
        "........",
        "........",
        "........",
    ]
    paint_patch(l1, 8, 0, 8, 8, head_top, ap, "star_head_top")
    paint_patch(l1, 0, 8, 8, 8, head_side_r, ap, "star_head_east")
    paint_patch(l1, 8, 8, 8, 8, head_front, ap, "star_head_front")
    paint_patch(l1, 16, 8, 8, 8, head_side_l, ap, "star_head_west")
    paint_patch(l1, 24, 8, 8, 8, head_back, ap, "star_head_back")

    # Hat outer 3D layer: 3D Twin Crystal Spire Horns & Diadem Peak
    hat_top = [
        "........",
        "h......h",
        "b......b",
        "v......v",
        "p......p",
        "hb....bh",
        "bm.hh.mb",
        "vb.hh.bv",
    ]
    hat_front = [
        "h..hh..h",
        "b.vhhv.b",
        "p..bb..p",
        "........",
        "........",
        "........",
        "........",
        "........",
    ]
    hat_side_l = [
        "h....hh.",
        "bm..bbv.",
        "pmsbmmp.",
        ".Ppmpp..",
        "........",
        "........",
        "........",
        "........",
    ]
    hat_side_r = [row[::-1] for row in hat_side_l]
    hat_back = [
        "........",
        "...hh...",
        "..vhhv..",
        "...pp...",
        "........",
        "........",
        "........",
        "........",
    ]
    paint_patch(l1, 40, 0, 8, 8, hat_top, ap, "star_hat_top")
    paint_patch(l1, 32, 8, 8, 8, hat_side_r, ap, "star_hat_east")
    paint_patch(l1, 40, 8, 8, 8, hat_front, ap, "star_hat_front")
    paint_patch(l1, 48, 8, 8, 8, hat_side_l, ap, "star_hat_west")
    paint_patch(l1, 56, 8, 8, 8, hat_back, ap, "star_hat_back")

    # Body: Sleek Ice-Teal Tunic + Faceted Diamond Core + Pointed V-tasset
    body_top = [
        "hb....bh",
        "pv....vp",
        "p......p",
        "P......P",
    ]
    body_front = [
        "vp....pv",
        "pbp..pbp",
        "PmbhhbmP",
        "PbhbbhbP",
        ".PbhhbP.",
        ".PpmmpP.",
        "..PvvP..",
        "pppmmppp",
        "PvbhhbvP",
        ".PbmmsP.",
        "..PsdP..",
        "...PP...",
    ]
    body_back = [
        "vp....pv",
        "pmp..pmp",
        "PmvhhvmP",
        "PmbhhbmP",
        ".PbmmbP.",
        ".PpsspP.",
        "..PPPP..",
        "PppbbppP",
        "PvmmmmvP",
        ".PmssmP.",
        "..PsdP..",
        "...PP...",
    ]
    body_side = [
        "pmmp",
        "pmsp",
        "PpsP",
        "PPpP",
        ".PP.",
        ".PP.",
        "PPPP",
        "pvpp",
        "PPPP",
        ".PP.",
        "....",
        "....",
    ]
    paint_patch(l1, 20, 16, 8, 4, body_top, ap, "star_body_top")
    paint_patch(l1, 16, 20, 4, 12, body_side, ap, "star_body_east")
    paint_patch(l1, 20, 20, 8, 12, body_front, ap, "star_body_front")
    paint_patch(l1, 28, 20, 4, 12, body_side, ap, "star_body_west")
    paint_patch(l1, 32, 20, 8, 12, body_back, ap, "star_body_back")

    # Arms: Spiked Crystal Pauldrons + Ice-Teal Forearm Bracers!
    arm_top = [
        "hhbb",
        "hbmp",
        "vmpp",
        "pPPP",
    ]
    arm_outer = [
        "hhvp",
        "bhmp",
        "bmsP",
        "pmp.",
        "PP..",
        "....",
        "....",
        "....",
        "PhvP",
        "PbpP",
        "....",
        "....",
    ]
    arm_front = [
        "hbmp",
        "bmsp",
        "pmp.",
        "PP..",
        "....",
        "....",
        "....",
        "....",
        "PbvP",
        "PPpP",
        "....",
        "....",
    ]
    arm_inner = [
        "pP..",
        "P...",
        "....",
        "....",
        "....",
        "....",
        "....",
        "....",
        "P...",
        "P...",
        "....",
        "....",
    ]
    arm_back = [row[::-1] for row in arm_front]
    paint_patch(l1, 44, 16, 4, 4, arm_top, ap, "star_arm_top")
    paint_patch(l1, 40, 20, 4, 12, arm_outer, ap, "star_arm_outer")
    paint_patch(l1, 44, 20, 4, 12, arm_front, ap, "star_arm_front")
    paint_patch(l1, 48, 20, 4, 12, arm_inner, ap, "star_arm_inner")
    paint_patch(l1, 52, 20, 4, 12, arm_back, ap, "star_arm_back")

    # Boots: Ice-Teal cuff + Glowing Cyan Crystal Ankle-Fins
    boot_bottom = [
        "PPPP",
        "PppP",
        "PppP",
        "PPPP",
    ]
    boot_front = [
        "....",
        "....",
        "....",
        "....",
        "....",
        "p..p",
        "PhhP",
        "Pbvp",
        "pmmp",
        "bhhb",
        "smms",
        "PPPP",
    ]
    boot_outer = [
        "....",
        "....",
        "....",
        "....",
        "hb..",
        "bmp.",
        "pmsP",
        "Pvmp",
        "pmsp",
        "smms",
        "dsmd",
        "PPPP",
    ]
    boot_inner = [
        "....",
        "....",
        "....",
        "....",
        "....",
        "....",
        "PppP",
        "PppP",
        "pssp",
        "smms",
        "dsmd",
        "PPPP",
    ]
    paint_patch(l1, 8, 16, 4, 4, boot_bottom, ap, "star_boot_bottom")
    paint_patch(l1, 0, 20, 4, 12, boot_outer, ap, "star_boot_outer")
    paint_patch(l1, 4, 20, 4, 12, boot_front, ap, "star_boot_front")
    paint_patch(l1, 8, 20, 4, 12, boot_inner, ap, "star_boot_inner")
    paint_patch(l1, 12, 20, 4, 12, boot_inner, ap, "star_boot_back")

    l1.save(TEX_ROOT / "models" / "armor" / f"{prefix}_layer_1.png")

    # Layer 2: Ice-Teal Waist Sash + Diamond Star Buckle + Faceted Crystal Greaves
    l2 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    l2_belt_front = [
        "PPvhhvPP",
        "PpbhhbpP",
        "PpmmmmpP",
        "..PppP..",
        "........",
    ]
    l2_belt_back = [
        "PPppppPP",
        "PpvhhvpP",
        "PPppppPP",
        "..PPPP..",
        "........",
    ]
    l2_belt_side = [
        "PPPP",
        "Pvvp",
        "PPPP",
        "....",
        "....",
    ]
    paint_patch(l2, 16, 27, 4, 5, l2_belt_side, ap, "star_l2_side1")
    paint_patch(l2, 20, 27, 8, 5, l2_belt_front, ap, "star_l2_front")
    paint_patch(l2, 28, 27, 4, 5, l2_belt_side, ap, "star_l2_side2")
    paint_patch(l2, 32, 27, 8, 5, l2_belt_back, ap, "star_l2_back")

    leg_front = [
        "hbmp",
        "bmsP",
        "smsp",
        "PpsP",
        "vhhv",
        "pbbp",
        "PmsP",
        "pdsP",
        "PPPP",
        "....",
        "....",
        "....",
    ]
    leg_side = [
        "hbmp",
        "bmmp",
        "pmsp",
        "PppP",
        "PvpP",
        "pmsp",
        "pmsp",
        "PdsP",
        "PPPP",
        "....",
        "....",
        "....",
    ]
    leg_inner = [
        "PppP",
        "pmsP",
        "PmsP",
        "PdsP",
        "PPPP",
        "pmsP",
        "PdsP",
        "PPPP",
        "....",
        "....",
        "....",
        "....",
    ]
    paint_patch(l2, 0, 20, 4, 12, leg_side, ap, "star_leg_outer")
    paint_patch(l2, 4, 20, 4, 12, leg_front, ap, "star_leg_front")
    paint_patch(l2, 8, 20, 4, 12, leg_inner, ap, "star_leg_inner")
    paint_patch(l2, 12, 20, 4, 12, leg_side, ap, "star_leg_back")
    l2.save(TEX_ROOT / "models" / "armor" / f"{prefix}_layer_2.png")
    return l1, l2, ap


def box_faces(u, v, w, h, d):
    """Bedrock box-UV rectangles for a w x h x d cube with uv offset (u, v)."""
    return {
        "up": (u + d, v, w, d),
        "down": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "south": (u + d + w + d, v + d, w, h),
    }


def armor_sheet_rects(geo_name):
    """UV rectangles the GeckoLib armour geometry actually samples.

    Read straight from the geo file so the sheet and the model can never drift:
    a cube added to the model gets its texture area sealed automatically.
    """
    geo = json.loads((GEO_ROOT / geo_name).read_text(encoding="utf-8"))
    rects, faces = [], []
    for bone in geo["minecraft:geometry"][0]["bones"]:
        for cube in bone.get("cubes", []):
            u, v = cube.get("uv", [0, 0])[:2]
            w, h, d = cube["size"]
            rect = (u, v, 2 * (w + d), h + d)
            if rect not in rects:
                rects.append(rect)
            for face in box_faces(u, v, w, h, d).values():
                if face not in faces:
                    faces.append(face)
    return rects, faces


def seal_rects(img, rects, base, passes=8):
    """Fills transparent pixels inside every UV rectangle from its painted neighbours.

    Armour is worn on the player's skin: an unpainted pixel is a see-through hole.
    Growing each painted area outward keeps the hand-placed detail and closes the
    gaps, and anything still empty after `passes` falls back to the set's base colour.
    """
    for _ in range(passes):
        grown = []
        for x, y, w, h in rects:
            for px in range(x, x + w):
                for py in range(y, y + h):
                    if img.getpixel((px, py))[3] != 0:
                        continue
                    samples = [
                        img.getpixel((qx, qy))
                        for qx in (px - 1, px + 1)
                        for qy in (py,)
                        if x <= qx < x + w
                    ] + [
                        img.getpixel((qx, qy))
                        for qy in (py - 1, py + 1)
                        for qx in (px,)
                        if y <= qy < y + h
                    ]
                    samples = [s for s in samples if s[3] != 0]
                    if not samples:
                        continue
                    avg = tuple(sum(s[i] for s in samples) // len(samples) for i in range(4))
                    grown.append((px, py, avg))
        if not grown:
            break
        for px, py, color in grown:
            img.putpixel((px, py), color)
    for x, y, w, h in rects:
        for px in range(x, x + w):
            for py in range(y, y + h):
                if img.getpixel((px, py))[3] == 0:
                    img.putpixel((px, py), base)


def shade_flat_faces(img, faces, top=1.07, bottom=0.93):
    """Gives single-colour faces a top-lit ramp so armour plating reads as solid.

    Only faces at least 3x3 with two or fewer colours are touched, so painted
    detail and 1px trims keep their exact pixels.
    """
    for x, y, w, h in faces:
        if w < 3 or h < 3:
            continue
        colors = {
            img.getpixel((px, py))
            for px in range(x, x + w)
            for py in range(y, y + h)
            if img.getpixel((px, py))[3] != 0
        }
        if len(colors) > 2:
            continue
        for row in range(h):
            factor = top + (bottom - top) * (row / max(h - 1, 1))
            for px in range(x, x + w):
                r, g, b, a = img.getpixel((px, py := y + row))
                if a == 0:
                    continue
                img.putpixel(
                    (px, py),
                    (
                        min(255, round(r * factor)),
                        min(255, round(g * factor)),
                        min(255, round(b * factor)),
                        a,
                    ),
                )


def compose_geckolib_armor_sheet(
    filename, l1, l2, leg_top_color, leg_bot_color, ap, boxes, prefix, geo_name=None, base=None
):
    """Assembles a 64x64 GeckoLib GeoArmorRenderer texture sheet from layer_1, layer_2 legs, and 3D detail boxes."""
    sheet = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    sheet.paste(l1, (0, 0))
    sheet.paste(l2.crop((0, 16, 16, 32)), (0, 32))
    fill(sheet, 4, 32, 4, 4, leg_top_color)
    fill(sheet, 8, 32, 4, 4, leg_bot_color)

    for box_name, (u, v, w, h, d, box_faces) in boxes.items():
        paint_box_uv(sheet, u, v, w, h, d, box_faces, ap, f"{prefix}_{box_name}")

    if geo_name:
        rects, faces = armor_sheet_rects(geo_name)
        seal_rects(sheet, rects, base)
        shade_flat_faces(sheet, faces)

    sheet.save(TEX_ROOT / "item" / "armor" / filename)


def make_sunsteel_armor_geo_sheet(l1, l2, ap):
    boxes = {
        "tasset": (
            16, 32, 4, 5, 4,
            {
                "top": ["TTTT"] * 4,
                "bottom": ["dddd"] * 4,
                "outer": ["TTTT", "bmmT", "srms", "sdds", "TTdT"],
                "front": ["TTgr", "TbmR", "smmm", "sdds", "T.dT"],
                "inner": ["TTTT", "TmmT", "smms", "sdds", "...."],
                "back": ["TTTT", "TTmm", "sdmm", "sdds", "T..T"],
            },
        ),
        "pauldron": (
            32, 32, 5, 5, 5,
            {
                "top": ["shhhs", "hbbmh", "brmrb", "mbmmb", "TTdTT"],
                "bottom": ["ddddd", "dTTTd", "dTTTd", "dTTTd", "TTTTT"],
                "outer": ["shbhs", "brgrb", "bRrrb", "smmms", "TTTTT"],
                "front": ["bhhbT", "hbmmT", "bbmms", "sddms", "TTTTT"],
                "inner": ["TTmmT", "mmdd.", "dsdd.", "TT...", "....."],
                "back": ["Tbhhb", "Tmmbh", "smmbb", "smdds", "TTTTT"],
            },
        ),
        "chest_core": (
            52, 32, 4, 5, 1,
            {
                "top": ["hbbh"],
                "bottom": ["TTTT"],
                "outer": ["s", "b", "m", "d", "T"],
                "front": ["hbbh", "grrg", "RRRR", "bhbb", "TTTT"],
                "inner": ["s", "b", "m", "d", "T"],
                "back": ["dddd"] * 5,
            },
        ),
        "knee_poleyn": (
            52, 38, 3, 3, 1,
            {
                "top": ["hbh"],
                "bottom": ["TTT"],
                "outer": ["b", "m", "T"],
                "front": ["bhb", "mrR", "TdT"],
                "inner": ["b", "m", "T"],
                "back": ["ddd", "ddd", "TTT"],
            },
        ),
        "plume_crest": (
            0, 48, 2, 7, 9,
            {
                "top": ["gg", "rg", "hr", "rh", "rg", "rh", "rg", "rR", "dR"],
                "bottom": ["dd"] * 9,
                "outer": [
                    "ghhrrgRdd",
                    "hbbmrrmsd",
                    "dmmddTTdd",
                    "ddmmddTdd",
                    "ddmmddTdd",
                    "dddmddTdd",
                    "dddTTTddd",
                ],
                "front": ["gg", "hh", "bb", "mm", "ss", "dd", "TT"],
                "inner": [
                    "ddRggrrhg",
                    "dsmrrmbbh",
                    "ddTTddmmd",
                    "ddmmddTdd",
                    "ddmmddTdd",
                    "dddmddTdd",
                    "dddTTTddd",
                ],
                "back": ["rg", "rR", "dd", "dd", "dd", "TT", "TT"],
            },
        ),
        "sun_wing": (
            22, 48, 1, 4, 6,
            {
                "top": ["h", "h", "b", "b", "r", "m"],
                "bottom": ["d"] * 6,
                "outer": ["hhbbmm", "hbbgrs", ".bmmRd", "..sdTT"],
                "front": ["h", "b", "s", "."],
                "inner": ["mmbbhh", "smmbbb", "dsmms.", "TTdd.."],
                "back": ["m", "s", "d", "T"],
            },
        ),
        "ankle_wing": (
            36, 48, 1, 4, 4,
            {
                "top": ["h", "b", "r", "m"],
                "bottom": ["d", "d", "T", "T"],
                "outer": ["hbh.", "bbms", "mrds", ".dTT"],
                "front": ["h", "b", "m", "."],
                "inner": [".mbh", "smmb", "sdds", "TTT."],
                "back": ["m", "s", "d", "T"],
            },
        ),
    }
    compose_geckolib_armor_sheet(
        "sunsteel_armor.png",
        l1,
        l2,
        SLATE_TRIM,
        SUN_DARK,
        ap,
        boxes,
        "sun",
        geo_name="sunsteel_armor.geo.json",
        base=SUN_SHADE,
    )


def make_starlight_armor_geo_sheet(l1, l2, ap, prefix="starlight", trim_dark=STAR_TRIM_DARK, trim=STAR_TRIM, shade=STAR_SHADE):
    boxes = {
        "tasset": (
            16, 32, 4, 5, 4,
            {
                "top": ["PPPP"] * 4,
                "bottom": ["pppp"] * 4,
                "outer": ["PPPP", "Pvvp", "PPPP", "pp..", "...."],
                "front": ["PPvh", "Ppbh", "Ppmm", "..Pp", "...."],
                "inner": ["PPPP", "Pvvp", "PPPP", "....", "...."],
                "back": ["PPpp", "Ppvh", "PPpp", "..PP", "...."],
            },
        ),
        "epaulette": (
            32, 32, 5, 5, 5,
            {
                "top": ["hhbbh", "hbggb", "vhbhv", "pPPPp", "PPPPP"],
                "bottom": ["ppppp", "pPPPp", "pPPPp", "pPPPp", "PPPPP"],
                "outer": ["hhbbp", "bhggp", "bmsbP", "pvmPP", "PPPPP"],
                "front": ["hhbmp", "bhbsp", "pmvPP", "PPPPP", "....."],
                "inner": ["ppPP.", "PP...", ".....", ".....", "....."],
                "back": ["pmbhb", "psbhb", "PPvmp", "PPPPP", "....."],
            },
        ),
        "chest_core": (
            52, 32, 4, 5, 1,
            {
                "top": ["vhhv"],
                "bottom": ["PPPP"],
                "outer": ["p", "v", "h", "b", "P"],
                "front": ["PhhP", "bggb", "bhhb", "Pmmp", "PPPP"],
                "inner": ["p", "v", "h", "b", "P"],
                "back": ["PPPP"] * 5,
            },
        ),
        "knee_crystal": (
            52, 38, 3, 3, 1,
            {
                "top": ["vhv"],
                "bottom": ["PPP"],
                "outer": ["p", "b", "P"],
                "front": ["vhv", "hgh", "PbP"],
                "inner": ["p", "b", "P"],
                "back": ["ppp", "ppp", "PPP"],
            },
        ),
        "brow_spire": (
            0, 48, 2, 7, 2,
            {
                "top": ["gh", "hb"],
                "bottom": ["PP", "PP"],
                "outer": ["hh", "gg", "gb", "bb", "bv", "pP", "PP"],
                "front": ["hh", "gg", "bb", "bm", "ms", "sp", "PP"],
                "inner": ["hh", "gg", "bg", "bv", "vb", "Pp", "PP"],
                "back": ["vv", "bb", "bm", "ms", "pp", "pP", "PP"],
            },
        ),
        "spire_horn": (
            12, 48, 2, 9, 3,
            {
                "top": ["gh", "hb", "bm"],
                "bottom": ["pp", "PP", "PP"],
                "outer": [
                    "hh.",
                    "hgb",
                    "gbh",
                    "bmb",
                    "vsv",
                    "msp",
                    "psv",
                    "pPP",
                    "PPP",
                ],
                "front": ["gh", "hb", "bh", "bm", "vs", "ms", "sp", "pP", "PP"],
                "inner": [
                    ".hh",
                    "hbg",
                    "hbg",
                    "bmb",
                    "psv",
                    "msm",
                    "PPp",
                    "PPp",
                    "PPP",
                ],
                "back": ["hb", "bm", "mb", "ms", "vs", "sp", "vp", "pP", "PP"],
            },
        ),
        "ankle_fin": (
            36, 48, 1, 5, 4,
            {
                "top": ["h", "h", "b", "m"],
                "bottom": ["p", "p", "P", "P"],
                "outer": ["hbh.", "bhbs", "bmsv", "mmpP", ".pPP"],
                "front": ["h", "b", "m", "p", "."],
                "inner": [".mbh", "sbmb", "psms", "PPpp", "PPP."],
                "back": ["m", "s", "p", "P", "P"],
            },
        ),
    }
    compose_geckolib_armor_sheet(
        f"{prefix}_armor.png",
        l1,
        l2,
        trim_dark,
        trim,
        ap,
        boxes,
        "star",
        geo_name=f"{prefix}_armor.geo.json",
        base=shade,
    )


def main():
    (TEX_ROOT / "block").mkdir(parents=True, exist_ok=True)
    (TEX_ROOT / "item").mkdir(parents=True, exist_ok=True)
    (TEX_ROOT / "item" / "armor").mkdir(parents=True, exist_ok=True)
    (TEX_ROOT / "models" / "armor").mkdir(parents=True, exist_ok=True)

    sun_p = {
        "h": SUN_HI, "hi": SUN_HI,
        "b": SUN_BRIGHT, "bright": SUN_BRIGHT,
        "m": SUN_MAIN, "main": SUN_MAIN,
        "s": SUN_SHADE, "shade": SUN_SHADE,
        "d": SUN_DARK, "dark": SUN_DARK,
        "o": SUN_OUTLINE,
    }
    star_p = {
        "h": STAR_HI, "hi": STAR_HI,
        "b": STAR_BRIGHT, "bright": STAR_BRIGHT,
        "m": STAR_MAIN, "main": STAR_MAIN,
        "s": STAR_SHADE, "shade": STAR_SHADE,
        "d": STAR_DARK, "dark": STAR_DARK,
        "o": STAR_OUTLINE,
    }

    sun_tool_p = {
        "O": SUN_OUTLINE,
        "o": SUN_DARK,
        "d": (180, 72, 24, 255),
        "s": SUN_SHADE,
        "m": SUN_MAIN,
        "b": SUN_BRIGHT,
        "h": SUN_HI,
        "g": SUN_HI,
    }
    star_tool_p = {
        "O": STAR_TRIM_DARK,
        "o": STAR_TRIM,
        "d": STAR_TRIM_HI,
        "s": STAR_SHADE,
        "m": STAR_MAIN,
        "b": STAR_BRIGHT,
        "h": STAR_HI,
        "g": SUN_HI,
    }

    prism_p = {
        "h": PRISM_HI, "hi": PRISM_HI,
        "b": PRISM_BRIGHT, "bright": PRISM_BRIGHT,
        "m": PRISM_MAIN, "main": PRISM_MAIN,
        "s": PRISM_SHADE, "shade": PRISM_SHADE,
        "d": PRISM_DARK, "dark": PRISM_DARK,
        "o": PRISM_OUTLINE,
    }
    prism_tool_p = {
        "O": PRISM_OUTLINE,
        "o": PRISM_TRIM_DARK,
        "d": PRISM_TRIM,
        "s": PRISM_SHADE,
        "m": PRISM_MAIN,
        "b": PRISM_BRIGHT,
        "h": PRISM_HI,
        "g": PRISM_TRIM_HI,
    }

    dragon_p = {
        "h": DRAGON_HI, "hi": DRAGON_HI,
        "b": DRAGON_BRIGHT, "bright": DRAGON_BRIGHT,
        "m": DRAGON_MAIN, "main": DRAGON_MAIN,
        "s": DRAGON_SHADE, "shade": DRAGON_SHADE,
        "d": DRAGON_DARK, "dark": DRAGON_DARK,
        "o": DRAGON_OUTLINE,
    }
    dragon_tool_p = {
        "O": DRAGON_OUTLINE,
        "o": DRAGON_TRIM_DARK,
        "d": DRAGON_TRIM,
        "s": DRAGON_SHADE,
        "m": DRAGON_MAIN,
        "b": DRAGON_BRIGHT,
        "h": DRAGON_HI,
        "g": DRAGON_TRIM_HI,
    }

    twin_p = {
        "h": TWIN_HI, "hi": TWIN_HI,
        "b": TWIN_BRIGHT, "bright": TWIN_BRIGHT,
        "m": TWIN_MAIN, "main": TWIN_MAIN,
        "s": TWIN_SHADE, "shade": TWIN_SHADE,
        "d": TWIN_DARK, "dark": TWIN_DARK,
        "o": TWIN_OUTLINE,
    }
    twin_tool_p = {
        "O": TWIN_OUTLINE,
        "o": TWIN_TRIM_DARK,
        "d": TWIN_TRIM,
        "s": TWIN_SHADE,
        "m": TWIN_MAIN,
        "b": TWIN_BRIGHT,
        "h": TWIN_HI,
        "g": TWIN_TRIM_HI,
    }

    make_sunsteel_ore("sunsteel_ore.png", False, sun_p)
    make_sunsteel_ore("deepslate_sunsteel_ore.png", True, sun_p)
    make_sunsteel_storage_block("sunsteel_block.png")

    make_starlight_ore("starlight_crystal_ore.png", False, star_p)
    make_starlight_ore("deepslate_starlight_crystal_ore.png", True, star_p)
    make_starlight_storage_block("starlight_crystal_block.png")

    make_materials()
    make_tool_set("sunsteel", sun_tool_p)
    make_tool_set("starlight", star_tool_p)
    make_tool_set("prismatic", prism_tool_p)
    make_tool_set("dragonscale", dragon_tool_p)
    make_sunsteel_armor_icons(sun_p)
    make_starlight_armor_icons(star_p)
    make_starlight_armor_icons(prism_p, prefix="prismatic", trim=PRISM_TRIM, trim_dark=PRISM_TRIM_DARK, trim_hi=PRISM_TRIM_HI)
    make_starlight_armor_icons(dragon_p, prefix="dragonscale", trim=DRAGON_TRIM, trim_dark=DRAGON_TRIM_DARK, trim_hi=DRAGON_TRIM_HI)
    sun_l1, sun_l2, sun_ap = make_sunsteel_armor_layers(sun_p)
    star_l1, star_l2, star_ap = make_starlight_armor_layers(star_p)
    prism_l1, prism_l2, prism_ap = make_starlight_armor_layers(
        prism_p, prefix="prismatic", trim=PRISM_TRIM, trim_dark=PRISM_TRIM_DARK, trim_hi=PRISM_TRIM_HI)
    dragon_l1, dragon_l2, dragon_ap = make_starlight_armor_layers(
        dragon_p, prefix="dragonscale", trim=DRAGON_TRIM, trim_dark=DRAGON_TRIM_DARK, trim_hi=DRAGON_TRIM_HI)
    make_sunsteel_armor_geo_sheet(sun_l1, sun_l2, sun_ap)
    make_starlight_armor_geo_sheet(star_l1, star_l2, star_ap)
    make_starlight_armor_geo_sheet(
        prism_l1, prism_l2, prism_ap, prefix="prismatic", trim_dark=PRISM_TRIM_DARK, trim=PRISM_TRIM, shade=PRISM_SHADE)
    make_starlight_armor_geo_sheet(
        dragon_l1, dragon_l2, dragon_ap, prefix="dragonscale", trim_dark=DRAGON_TRIM_DARK, trim=DRAGON_TRIM, shade=DRAGON_SHADE)

    # Twin-Star Endgame Rewards (Phase 5 Stage 9)
    twin_core_grid = [
        "......OOOO......",
        "....OOggggOO....",
        "...OggbbbbggO...",
        "..OgbbhhhhbbgO..",
        ".OgbhhmmmmhhbgO.",
        ".OghmssssssmhgO.",
        "OgbssooooossbgO.",
        "OgbsoTTTTTTosbgO",
        "OgbsoTTTTTTosbgO",
        "OgbssooooossbgO.",
        ".OghmddddddmhgO.",
        ".OgbhhddddhhbgO.",
        "..OgbbhhhhbbgO..",
        "...OggbbbbggO...",
        "....OOggggOO....",
        "......OOOO......",
    ]
    twin_core_p = {
        "O": TWIN_OUTLINE,
        "g": TWIN_TRIM,
        "b": TWIN_BRIGHT,
        "h": TWIN_HI,
        "m": TWIN_MAIN,
        "s": TWIN_SHADE,
        "o": TWIN_TRIM_DARK,
        "T": TWIN_TRIM_HI,
        "d": DRAGON_MAIN,
    }
    twin_core_img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(twin_core_grid):
        for x, ch in enumerate(row):
            if ch != ".":
                put(twin_core_img, x, y, twin_core_p[ch])
    twin_core_img.save(TEX_ROOT / "item" / "twin_star_core.png")

    twin_tp = {
        **twin_tool_p,
        "u": (40, 30, 11, 255),
        "v": (73, 54, 21, 255),
        "w": (104, 78, 30, 255),
        "x": (137, 103, 39, 255),
    }
    twin_sword = [
        ".............ooo",
        "............ohhO",
        "...........ohmhO",
        "..........ohmhO.",
        ".........ohmbO..",
        "........ohmbO...",
        "..oo...obmbO....",
        "..odo.obgbO.....",
        "...osObmbO......",
        "...ossdbO.......",
        "....odoO........",
        "...vwOooO.......",
        "..vxu.OOoO......",
        "oowu....OO......",
        "odO.............",
        "OOO.............",
    ]
    twin_sword_img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(twin_sword):
        for x, ch in enumerate(row):
            if ch != ".":
                put(twin_sword_img, x, y, twin_tp[ch])
    twin_sword_img.save(TEX_ROOT / "item" / "twin_star_blade.png")

    make_starlight_armor_icons(twin_p, prefix="twin_star", trim=TWIN_TRIM, trim_dark=TWIN_TRIM_DARK, trim_hi=TWIN_TRIM_HI)
    twin_l1, twin_l2, twin_ap = make_starlight_armor_layers(
        twin_p, prefix="twin_star", trim=TWIN_TRIM, trim_dark=TWIN_TRIM_DARK, trim_hi=TWIN_TRIM_HI)
    make_starlight_armor_geo_sheet(
        twin_l1, twin_l2, twin_ap, prefix="twin_star", trim_dark=TWIN_TRIM_DARK, trim=TWIN_TRIM, shade=TWIN_SHADE)
    print("Generated all Sunsteel, Starlight Crystal, Prismatic, Dragonscale, and Twin-Star textures.")


if __name__ == "__main__":
    main()
