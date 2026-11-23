"""Generates v3 256x256 double-layer alpha-cutout models, textures, and glowmasks
for Starlight Crystal, Prismatic Brimstone, Dragonscale, and Twin-Star armors.

Usage:
    python tools/art/make_v3_armors_art.py
"""
from __future__ import annotations

from pathlib import Path
from v3_armor_lib import ArmorGeoSpec, PlaneSpec, build_armor_textures, write_armor_geo

ASSETS = Path("src/main/resources/assets/artilian")
GEO_DIR = ASSETS / "geo/item/armor"
TEX_DIR = ASSETS / "textures/item/armor"


# =====================================================================
# 1.2 STARLIGHT CRYSTAL ARMOR
# Deep midnight-indigo under-plate + cyan/ice-crystal filigree + amethyst shards
# =====================================================================

STARLIGHT_PAL = {
    ".": (0, 0, 0, 0),
    "0": (10, 14, 34, 255),     # midnight indigo shadow
    "1": (18, 25, 56, 255),     # dark indigo plate
    "2": (30, 44, 88, 255),     # mid sapphire highlight
    "3": (52, 74, 132, 255),    # steel-indigo glint
    "r": (74, 38, 138, 255),    # deep amethyst accent
    "R": (132, 78, 224, 255),   # bright starlight amethyst
    "k": (14, 52, 82, 255),     # deep sea-teal outline
    "d": (24, 108, 152, 255),   # dark crystal cyan
    "g": (48, 178, 214, 255),   # rich starlight cyan
    "G": (104, 228, 248, 255),  # bright ice-crystal
    "h": (214, 252, 255, 255),  # diamond-ice glint
    "e": (56, 217, 192, 255),   # starlight teal glow
    "E": (168, 255, 255, 255),  # intense starlight core glow
}
STARLIGHT_GLOW = {"e", "E"}

STARLIGHT_SPEC = ArmorGeoSpec(
    set_name="starlight",
    crown_front=PlaneSpec(12, 10, 0, [-6.0, 29.8, -5.2], [0, 31, -5.2], [12, 0, 0]),
    crown_back=PlaneSpec(10, 8, 0, [-5.0, 30.2, 5.0], [0, 31.2, 5.0], [-14, 0, 0]),
    crown_side_r=PlaneSpec(0, 10, 10, [-5.2, 28.8, -5.0], [-5.2, 30, 0], [0, 0, -16]),
    crown_side_l=PlaneSpec(0, 10, 10, [5.2, 28.8, -5.0], [5.2, 30, 0], [0, 0, 16]),
    pauldron_crest_r=PlaneSpec(7, 7, 0, [-11.8, 22.8, 0.0], [-7.5, 23.5, 0], [0, 0, -26]),
    pauldron_crest_l=PlaneSpec(7, 7, 0, [4.8, 22.8, 0.0], [7.5, 23.5, 0], [0, 0, 26]),
    back_crest_r=PlaneSpec(7, 12, 0, [-7.2, 14.0, 3.5], [-0.5, 19, 3.5], [-14, 28, -14]),
    back_crest_l=PlaneSpec(7, 12, 0, [0.2, 14.0, 3.5], [0.5, 19, 3.5], [-14, -28, 14]),
    boot_wing_r=PlaneSpec(0, 6, 6, [-5.1, 0.6, -2.5], [-5.1, 3, 0], [0, 12, -14]),
    boot_wing_l=PlaneSpec(0, 6, 6, [5.1, 0.6, -2.5], [5.1, 3, 0], [0, -12, 14]),
)

STARLIGHT_SPRITES = {
    "head_inner_front": [
        "1112222222222111",
        "1122211111122211",
        "1110000000000111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000eEEeeeeEEe01",
        "10000eEEEEEEe001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000100000010001",
        "1100110000110011",
        "1110111111110111",
        "1111111111111111",
    ],
    "head_filigree_front": [
        "gGhGGg....gGGhGg",
        "dGg.gGhhhhGg.gGd",
        "kG...gGGGGg...Gk",
        "gG..k.gGGg.k..Gg",
        "GG.kGg.GG.gGk.GG",
        "gG.dG..dd..Gd.Gg",
        "kG............Gk",
        "kG............Gk",
        "gG............Gg",
        "GGk..k....k..kGG",
        "gGGk.gG..Gg.kGGg",
        "dGGgk.gGGg.kgGGd",
        "kdGGgk.GG.kgGGdk",
        ".kdGGggGGggGGdk.",
        "..kddGGGGGGddk..",
        "....kkkddkkk....",
    ],
    "head_filigree_side": [
        "gGhGGGGGGGGGGhGg",
        "dGggddGGGGddggGd",
        "kG............Gk",
        "gG..kggggggk..Gg",
        "GG.kGhGGGGhGk.GG",
        "gG.kGg....gGk.Gg",
        "dG.dG......Gd.Gd",
        "kG..k......k..Gk",
        "gG............Gg",
        "GGk..........kGG",
        "gGGk........kGGg",
        "dGGggkkkkkkggGGd",
        "kddGGGGGGGGGGddk",
        "..kkkkddddkkkk..",
        "................",
        "................",
    ],
    "head_filigree_back": [
        "gGhGGGGGGGGGGhGg",
        "dGggddGGGGddggGd",
        "kG.....GG.....Gk",
        "gG....gGGg....Gg",
        "GG...gGhhGg...GG",
        "gG..gGg..gGg..Gg",
        "dG..Gg....gG..Gd",
        "kG..gG....Gg..Gk",
        "gG...gG..Gg...Gg",
        "GG....gGGg....GG",
        "gG.....GG.....Gg",
        "dGgg........ggGd",
        "kddGGGggggGGGddk",
        "..kkkddGGddkkk..",
        "......kkkk......",
        "................",
    ],
    "crown_front": [
        "...........h............",
        "..........gGg...........",
        ".h........GGG........h..",
        ".G.......gGhgG.......G..",
        ".Gg.....kGGhGGk.....gG..",
        "kGGg....gGGEGGg....gGGk.",
        "gGGg...kGGEEEGGk...gGGg.",
        "dGGGg..gGGEhEGGg..gGGGd.",
        "kGGGGk.kGGGEEGGk.kGGGGk.",
        ".kGGGGg.gGGhhGG.gGGGGk..",
        ".dGGhGGgkGGGGGGkgGGhGGd.",
        ".kGGGGGGgGGRRGgGGGGGGk..",
        "..kGGhGGg.gGGg.gGGhGGk..",
        "..dGGg.dGg.GG.gGd.gGGd..",
        "..kGd...kGGhhGGk...dGk..",
        "..kk.....kGGGGk.....kk..",
        "..........kddk..........",
        "...........kk...........",
        "........................",
        "........................",
    ],
    "crown_back": [
        ".........h..........",
        ".h......gGg......h..",
        ".G......GGG......G..",
        ".Gg....gGhgG....gG..",
        "kGGg..kGGGGkk..gGGk.",
        "gGGg..gGGhGGg..gGGg.",
        "dGGGggGGGGGGGggGGGd.",
        "kGGGGGGgGGGgGGGGGGk.",
        ".kGGhGG.gGg.GGhGGk..",
        ".dGGGGg.kGk.gGGGGd..",
        "..kGGGGggGggGGGGk...",
        "..dGGhGGGGGGGhGGd...",
        "..kddGGGGGGGGGddk...",
        "...kkkkkkkkkkkkk....",
        "....................",
        "....................",
    ],
    "crown_side": [
        "................h...",
        "...............gG...",
        "..............gGG...",
        "........h....gGhg...",
        ".......gG...gGGGk...",
        "......gGG..gGGhGk...",
        "..h..gGhg.gGGGGk....",
        ".gG..GGGGgGGGGd.....",
        "gGG.gGGhGGGGGk..h...",
        "GGGkGGGGGGGdk..gG...",
        "dGGGGhGGGGk...gGG...",
        "kGGGGGGGdk...gGhg...",
        ".kGGGGGGgggggGGGk...",
        ".dGGhGGGGGGGGGGd....",
        "..kGGGGGGGGGGdk.....",
        "..dGGddGGddGGk......",
        "..kkk..kk..kk.......",
        "....................",
        "....................",
        "....................",
    ],
    "body_filigree_front": [
        "gGhGgk....kgGhGg",
        "dGGgGk....kGgGGd",
        "kG..gGk..kGg..Gk",
        "gG...gGkkGg...Gg",
        "GG....gGGg....GG",
        "gG...gGhhGg...Gg",
        "dG..gGgEEgGg..Gd",
        "kG.gGgEEEEgGg.Gk",
        "gG.GgEEEEEEgG.Gg",
        "GG.gG.EEEE.Gg.GG",
        "gG..gG.EE.Gg..Gg",
        "dG...gG..Gg...Gd",
        "kG....gGGg....Gk",
        "gG...kGGGGk...Gg",
        "GG..kGg..gGk..GG",
        "gG.kGg....gGk.Gg",
        "dG.dG..GG..Gd.Gd",
        "kG....gGGg....Gk",
        "gGhGGGgGGgGGGhGg",
        "dGGGGGhEEhGGGGGd",
        "kddGGGgGGgGGGddk",
        "...kkkgGGgkkk...",
        "......gGGg......",
        ".......kk.......",
    ],
    "body_filigree_back": [
        "gGhGGGGGGGGGGhGg",
        "dGGgddGGGGddgGGd",
        "kG.....GG.....Gk",
        "gG....gGGg....Gg",
        "GG...gGhhGg...GG",
        "gG..gGgEEgGg..Gg",
        "dG.gGg.EE.gGg.Gd",
        "kG.dG..EE..Gd.Gk",
        "gG....gGGg....Gg",
        "GG...gGhhGg...GG",
        "gG..gGgGGgGg..Gg",
        "dG.gGg.GG.gGg.Gd",
        "kG.dG..GG..Gd.Gk",
        "gG.....GG.....Gg",
        "GG....gGGg....GG",
        "gG...gGhhGg...Gg",
        "dG..gGg..gGg..Gd",
        "kG..dG....Gd..Gk",
        "gGhGGGGGGGGGGhGg",
        "dGGGGGGhhGGGGGGd",
        "kddGGGGGGGGGGddk",
        "....kkkddkkk....",
        "................",
        "................",
    ],
    "chest_overlay_front": [
        "gGhGgk....kgGhGg",
        ".dGGgGk..kGgGGd.",
        "..kGGgGkkGgGGk..",
        "...kGGgEEgGGk...",
        "....kGEEEEGk....",
        "...gGGEEEEGGg...",
        "..gGhEEEEEEhGg..",
        "...gGGEEEEGGg...",
        "....kGEEEEGk....",
        ".....kGGGGk.....",
        "......kGGk......",
        ".......kk.......",
    ],
    "waist_skirt_front": [
        "gGhGGGg..gGGGhGg",
        "kdd..gk..kg..ddk",
        "gG...gGhhGg...Gg",
        "dG..gGgRRgGg..Gd",
        "kG..dG.RR.Gd..Gk",
        ".kG..gG..Gg..Gk.",
        "..k...gGGg...k..",
        "......gGhg......",
        ".......GG.......",
        ".......dd.......",
        ".......kk.......",
        "................",
    ],
    "waist_skirt_side": [
        "gGhGGhGg",
        "dGGGGGGd",
        "gGgRRgGg",
        "GGgRRgGG",
        "gGgGGgGg",
        "dG.GG.Gd",
        "kG.hh.Gk",
        ".kGGGGk.",
        "..gGGg..",
        "..dGGd..",
        "...kk...",
        "........",
    ],
    "pauldron_filigree": [
        "gGhGGGGGGhGg",
        "dGggdGGdgGGd",
        "GG..gGGg..GG",
        "gG.gGhhGg.Gg",
        "dG.GgEEgG.Gd",
        "kG.gGEEGg.Gk",
        "gG..gGGg..Gg",
        "dGgg.GG.ggGd",
        "kddGGhhGGddk",
        "..kkkddkkk..",
    ],
    "pauldron_crest": [
        "............h.",
        "...........gG.",
        "..........gGG.",
        ".........gGhg.",
        ".....h..gGGGk.",
        "....gG.gGGhGk.",
        "...gGGgGGGGk..",
        "..gGhgGGhGd...",
        ".gGGGGGGGk....",
        "gGGhGGGGd.....",
        "dGGGGGGk......",
        "kddGGGk.......",
        ".kkkkk........",
        "..............",
    ],
    "back_crest": [
        "............h.",
        "...........gG.",
        "..........gGG.",
        ".........gGhg.",
        "........gGGGk.",
        ".......gGGhGk.",
        "......gGGGGk..",
        ".....gGGEGk...",
        "....gGGEEGk...",
        "...gGGhEGd....",
        "..gGGGGGGk....",
        ".gGGhGGGd.....",
        "gGGGGGGk......",
        "dGGhGGk.......",
        "kGGGGdk.......",
        ".kGGdk........",
        "..kdk.........",
        "...k..........",
        "..............",
        "..............",
        "..............",
        "..............",
        "..............",
        "..............",
    ],
    "arm_filigree": [
        "........",
        "........",
        "........",
        "........",
        "........",
        "........",
        "gGhGGhGg",
        "kdd..ddk",
        "........",
        "..gGGg..",
        ".dG..Gd.",
        "..kGGk..",
        "........",
        "..gGGg..",
        ".gG..Gg.",
        ".G.EE.G.",
        ".dG..Gd.",
        "..kGGk..",
        "........",
        "gGhGGhGg",
        "dG.hh.Gd",
        "kG....Gk",
        "kddGGddk",
        "..kkkk..",
    ],
    "leg_filigree": [
        "gGhGGhGg",
        "kdd..ddk",
        "...GG...",
        "..g..g..",
        ".gG..Gg.",
        ".dG..Gd.",
        "..g..g..",
        "...GG...",
        "..gGhg..",
        ".gGEEGg.",
        ".GGEEGG.",
        ".dGGGGd.",
        "..kGGk..",
        "........",
        "..gGGg..",
        ".gG..Gg.",
        ".dG..Gd.",
        "..k..k..",
        "........",
        "...GG...",
        "..gGGg..",
        ".dG..Gd.",
        "kddGGddk",
        "..kkkk..",
    ],
    "boot_filigree": [
        "........",
        "........",
        "........",
        "........",
        "........",
        "........",
        "........",
        "........",
        "........",
        "........",
        "gGhGGhGg",
        "kdd..ddk",
        "...GG...",
        "..g..g..",
        ".gG..Gg.",
        ".dG..Gd.",
        "..g..g..",
        "...GG...",
        "gGh..hGg",
        "dG.hh.Gd",
        "kG....Gk",
        "gG.GG.Gg",
        "dGGhhGGd",
        "kddGGddk",
    ],
    "boot_wing": [
        "..........h.",
        ".........gG.",
        "........gGG.",
        "......hgGhg.",
        ".....gGGGGk.",
        "....gGGhGk..",
        "..hgGGGGGk..",
        ".gGGGGhGd...",
        "gGGhGGGGk...",
        "dGGGGGGd....",
        "kddGGGdk....",
        "..kkkkk.....",
    ],
}


# =====================================================================
# 1.3 PRISMATIC BRIMSTONE ARMOR
# Volcanic obsidian-charcoal plate + molten magma-orange & amethyst-prism
# =====================================================================

PRISMATIC_PAL = {
    ".": (0, 0, 0, 0),
    "0": (14, 10, 16, 255),     # volcanic obsidian shadow
    "1": (28, 20, 32, 255),     # charcoal plate
    "2": (46, 34, 52, 255),     # mid volcanic basalt
    "3": (70, 54, 78, 255),     # basalt edge glint
    "r": (92, 26, 128, 255),    # deep amethyst prism
    "R": (184, 61, 245, 255),   # bright prism magenta
    "k": (58, 16, 4, 255),      # charred bronze outline
    "d": (148, 48, 8, 255),     # dark forged magma-bronze
    "g": (224, 96, 16, 255),    # rich brimstone orange
    "G": (255, 168, 42, 255),   # bright molten gold
    "h": (255, 232, 138, 255),  # white-hot glint
    "e": (255, 88, 12, 255),    # magma ember glow
    "E": (255, 212, 76, 255),   # intense furnace core glow
}
PRISMATIC_GLOW = {"e", "E"}

PRISMATIC_SPEC = ArmorGeoSpec(
    set_name="prismatic",
    crown_front=PlaneSpec(12, 9, 0, [-6.0, 29.8, -5.2], [0, 31, -5.2], [10, 0, 0]),
    crown_back=PlaneSpec(10, 7, 0, [-5.0, 30.4, 5.0], [0, 31.4, 5.0], [-12, 0, 0]),
    crown_side_r=PlaneSpec(0, 10, 12, [-5.2, 28.5, -6.5], [-5.2, 30, 0], [0, 0, -18]),
    crown_side_l=PlaneSpec(0, 10, 12, [5.2, 28.5, -6.5], [5.2, 30, 0], [0, 0, 18]),
    pauldron_crest_r=PlaneSpec(7, 7, 0, [-11.8, 23.0, 0.0], [-7.5, 23.5, 0], [0, 0, -20]),
    pauldron_crest_l=PlaneSpec(7, 7, 0, [4.8, 23.0, 0.0], [7.5, 23.5, 0], [0, 0, 20]),
    back_crest_r=PlaneSpec(7, 12, 0, [-7.0, 14.2, 3.4], [-0.5, 19, 3.4], [-10, 22, -12]),
    back_crest_l=PlaneSpec(7, 12, 0, [0.0, 14.2, 3.4], [0.5, 19, 3.4], [-10, -22, 12]),
    boot_wing_r=PlaneSpec(0, 6, 7, [-5.1, 0.5, -3.0], [-5.1, 3, 0], [0, 10, -12]),
    boot_wing_l=PlaneSpec(0, 6, 7, [5.1, 0.5, -3.0], [5.1, 3, 0], [0, -10, 12]),
)

PRISMATIC_SPRITES = {
    **STARLIGHT_SPRITES,
    "head_inner_front": [
        "1112222222222111",
        "1122211111122211",
        "1110000000000111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000eEE00EEe0001",
        "10000eE00Ee00001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000100000010001",
        "1100110000110011",
        "1110111111110111",
        "1111111111111111",
    ],
    "crown_front": [
        ".h....................h.",
        ".G.........h..........G.",
        ".Gg.......gGg........gG.",
        "kGGg.....gGRGg......gGGk",
        "gGGg....gGRRRGg.....gGGg",
        "dGGGg..kGGEhEGGk...gGGGd",
        "kGGGGk.gGGEeEGGg..kGGGGk",
        ".kGGGGgGGGGGGGGg.gGGGGk.",
        ".dGGhGGGGgGRRgGGGGGhGGd.",
        ".kGGGGGGg.gGGg.GGGGGGk..",
        "..kGGhGGk..GG..kGGhGGk..",
        "..dGGg.dG..dd..Gd.gGGd..",
        "..kGd...kGGhhGGk...dGk..",
        "..kk.....kGGGGk.....kk..",
        "..........kddk..........",
        "...........kk...........",
        "........................",
        "........................",
    ],
    "crown_side": [
        "..h.....................",
        ".gG.....................",
        "gGG.....................",
        "GGGk.............h......",
        "dGGgk...........gG......",
        ".kGGgk.........gGG......",
        "..kGGGk.......gGhg......",
        "...dGGGg.....gGGGk......",
        "....kGGGg...gGGhGk......",
        "....dGGGGg.gGGGGk.......",
        "....kGGhGGgGGGGd........",
        ".....kGGGGGGGGk.........",
        ".....dGGhGGGGd..........",
        "......kGGGGdk...........",
        "......dGGddk............",
        "......kkkk..............",
        "........................",
        "........................",
        "........................",
        "........................",
    ],
    "pauldron_crest": [
        "............h.",
        "...........gG.",
        "..........gGG.",
        "...h.....gGhg.",
        "..gG....gGGGk.",
        ".gGG...gGGhGk.",
        ".GGGk.gGGGGk..",
        "dGGhGgGGRGd...",
        "kGGGGGRRRk....",
        ".kGGhGGGGd....",
        ".dGGGGGGk.....",
        ".kddGGGk......",
        "..kkkkk.......",
        "..............",
    ],
    "boot_wing": [
        ".............h",
        "............gG",
        "...........gGG",
        ".......h..gGhg",
        "......gG.gGGGk",
        ".....gGGgGGhGk",
        "..h.gGGGGGGGk.",
        ".gGgGGhGRGGd..",
        "gGGGGGRRRGk...",
        "dGGhGGGGGd....",
        "kddGGGGdk.....",
        "..kkkkkk......",
    ],
}


# =====================================================================
# 1.4 DRAGONSCALE ARMOR
# Emerald-scale under-plate + bronze-gold carved scales + swept drake horns
# =====================================================================

DRAGONSCALE_PAL = {
    ".": (0, 0, 0, 0),
    "0": (8, 26, 16, 255),      # deep emerald shadow
    "1": (14, 48, 30, 255),     # dark dragonscale emerald
    "2": (24, 78, 48, 255),     # mid emerald scale
    "3": (42, 118, 72, 255),    # emerald highlight
    "r": (118, 42, 8, 255),     # deep ember-amber scale
    "R": (204, 88, 16, 255),    # bright drake-amber
    "k": (52, 28, 6, 255),      # dark bronze outline
    "d": (122, 68, 12, 255),    # aged drake bronze
    "g": (196, 122, 26, 255),   # forged dragon-gold
    "G": (242, 182, 58, 255),   # sunlit gold scale
    "h": (255, 234, 146, 255),  # ivory-gold fang glint
    "e": (255, 138, 0, 255),    # molten amber glow
    "E": (255, 224, 85, 255),   # dragon-heart core glow
}
DRAGONSCALE_GLOW = {"e", "E"}

DRAGONSCALE_SPEC = ArmorGeoSpec(
    set_name="dragonscale",
    crown_front=PlaneSpec(12, 9, 0, [-6.0, 29.5, -5.2], [0, 31, -5.2], [10, 0, 0]),
    crown_back=PlaneSpec(10, 7, 0, [-5.0, 30.2, 5.0], [0, 31.2, 5.0], [-14, 0, 0]),
    crown_side_r=PlaneSpec(0, 10, 12, [-5.2, 29.0, -4.5], [-5.2, 30, 0], [8, 0, -15]),
    crown_side_l=PlaneSpec(0, 10, 12, [5.2, 29.0, -4.5], [5.2, 30, 0], [8, 0, 15]),
    pauldron_crest_r=PlaneSpec(7, 7, 0, [-11.8, 22.8, 0.0], [-7.5, 23.5, 0], [0, 0, -22]),
    pauldron_crest_l=PlaneSpec(7, 7, 0, [4.8, 22.8, 0.0], [7.5, 23.5, 0], [0, 0, 22]),
    back_crest_r=PlaneSpec(7, 12, 0, [-7.0, 14.0, 3.5], [-0.5, 19, 3.5], [-16, 18, -8]),
    back_crest_l=PlaneSpec(7, 12, 0, [0.0, 14.0, 3.5], [0.5, 19, 3.5], [-16, -18, 8]),
    boot_wing_r=PlaneSpec(0, 6, 7, [-5.1, 0.5, -2.5], [-5.1, 3, 0], [0, 12, -14]),
    boot_wing_l=PlaneSpec(0, 6, 7, [5.1, 0.5, -2.5], [5.1, 3, 0], [0, -12, 14]),
)

DRAGONSCALE_SPRITES = {
    **PRISMATIC_SPRITES,
    "head_inner_front": [
        "1112222222222111",
        "1122211111122211",
        "1110000000000111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000eEE00EEe0001",
        "10000eE00Ee00001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000100000010001",
        "1100110000110011",
        "1110111111110111",
        "1111111111111111",
    ],
    "crown_front": [
        "...........h............",
        "..h.......gGg.......h...",
        "..G.......GGG.......G...",
        ".gGg.....gGhgG.....gGg..",
        ".GGGg...kGGhGGk...gGGG..",
        "kGGhGg..gGGEGGg..gGhGGk.",
        "gGGGGg.kGGEEEGGk.gGGGGg.",
        "dGGhGGggGGEhEGGggGGhGGd.",
        "kGGGGGGGgGGEEGGgGGGGGGk.",
        ".kGGhGGGGgGGGGgGGGGhGk..",
        ".dGGGGGGg.gGGg.GGGGGGd..",
        "..kGGhGGk..GG..kGGhGGk..",
        "..dGGg.dG..dd..Gd.gGGd..",
        "..kGd...kGGhhGGk...dGk..",
        "..kk.....kGGGGk.....kk..",
        "..........kddk..........",
        "...........kk...........",
        "........................",
    ],
    "crown_side": [
        ".....................h..",
        "....................gG..",
        "...................gGG..",
        ".............h....gGhg..",
        "............gG...gGGGk..",
        "...........gGG..gGGhGk..",
        "......h...gGhg.gGGGGk...",
        ".....gG..gGGGGgGGGGd....",
        "....gGG.gGGhGGGGGGk.....",
        "...gGGGkGGGGGGGGdk......",
        "..gGGhGGGGGGGGdk........",
        ".gGGGGGGGGGGdk..........",
        "gGGhGGGGGGdk............",
        "dGGGGGGGGk..............",
        "kddGGGddk...............",
        "..kkkkkk................",
        "........................",
        "........................",
        "........................",
        "........................",
    ],
    "back_crest": [
        "............h.",
        "...........gG.",
        "..........gGG.",
        ".........gGhg.",
        "........gGGGk.",
        ".......gGGhGk.",
        ".....hgGGGGk..",
        "....gGGGEGk...",
        "...gGGGEEGk...",
        "..gGGGGEGd....",
        "h.GGGGGGk.....",
        "GgGGhGGd......",
        "GGGGGdk.......",
        "dGGhGk........",
        "kGGGGk........",
        ".kGGdk........",
        "..kdk.........",
        "...k..........",
        "..............",
        "..............",
        "..............",
        "..............",
        "..............",
        "..............",
    ],
}


# =====================================================================
# 1.5 TWIN-STAR ARMOR
# Void-violet under-suit + royal gold & starlight-cyan celestial filigree
# + floating Twin-Star Halo & grand winged back aura
# =====================================================================

TWIN_STAR_PAL = {
    ".": (0, 0, 0, 0),
    "0": (14, 8, 28, 255),      # void-violet shadow
    "1": (26, 15, 52, 255),     # deep royal violet plate
    "2": (44, 26, 84, 255),     # mid cosmic violet
    "3": (70, 44, 124, 255),    # violet edge glint
    "r": (20, 110, 140, 255),   # deep starlight cyan
    "R": (56, 217, 192, 255),   # bright starlight teal-cyan
    "k": (62, 34, 4, 255),      # royal bronze outline
    "d": (148, 92, 12, 255),    # dark celestial gold
    "g": (224, 156, 28, 255),   # rich royal gold
    "G": (252, 208, 68, 255),   # bright star-gold
    "h": (255, 246, 168, 255),  # radiant gold-white glint
    "e": (104, 236, 255, 255),  # celestial cyan glow
    "E": (255, 240, 138, 255),  # dual star-gold core glow
}
TWIN_STAR_GLOW = {"e", "E", "R"}

TWIN_STAR_SPEC = ArmorGeoSpec(
    set_name="twin_star",
    crown_front=PlaneSpec(14, 10, 0, [-7.0, 29.8, -5.2], [0, 31, -5.2], [12, 0, 0]),
    crown_back=PlaneSpec(12, 10, 0, [-6.0, 30.8, 5.4], [0, 32, 5.4], [-18, 0, 0]),
    crown_side_r=PlaneSpec(0, 10, 12, [-5.2, 28.8, -5.5], [-5.2, 30, 0], [0, 0, -14]),
    crown_side_l=PlaneSpec(0, 10, 12, [5.2, 28.8, -5.5], [5.2, 30, 0], [0, 0, 14]),
    pauldron_crest_r=PlaneSpec(8, 8, 0, [-12.6, 22.8, 0.0], [-7.5, 23.5, 0], [0, 0, -24]),
    pauldron_crest_l=PlaneSpec(8, 8, 0, [4.6, 22.8, 0.0], [7.5, 23.5, 0], [0, 0, 24]),
    back_crest_r=PlaneSpec(9, 14, 0, [-9.2, 13.5, 3.6], [-0.5, 19, 3.6], [-14, 26, -12]),
    back_crest_l=PlaneSpec(9, 14, 0, [0.2, 13.5, 3.6], [0.5, 19, 3.6], [-14, -26, 12]),
    boot_wing_r=PlaneSpec(0, 7, 7, [-5.1, 0.4, -3.0], [-5.1, 3, 0], [0, 12, -16]),
    boot_wing_l=PlaneSpec(0, 7, 7, [5.1, 0.4, -3.0], [5.1, 3, 0], [0, -12, 16]),
)

TWIN_STAR_SPRITES = {
    **STARLIGHT_SPRITES,
    "head_inner_front": [
        "1112222222222111",
        "1122211111122211",
        "1110000000000111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000eEE00EEe0001",
        "10000eE00Ee00001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000100000010001",
        "1100110000110011",
        "1110111111110111",
        "1111111111111111",
    ],
    "crown_front": [
        ".............h..............",
        "............gGg.............",
        "..h.........GGG.........h...",
        "..G........gGhgG........G...",
        "..Gg......kGReRGk......gG...",
        ".kGGg.....gREeERg.....gGGk..",
        "h.kGGg...kGReEeRGk...gGGk.h.",
        "G..kGGg..gRGEEEGRg..gGGk..G.",
        "Gg..kGGggGGRReRRGGggGGk..gG.",
        "GGg..dGGGGGGhGhGGGGGGd..gGG.",
        "kGGg.kGGGGg.GGG.gGGGGk.gGGk.",
        ".kGGgkGGGg..gGg..gGGGkgGGk..",
        "..kGGGGGG...kGk...GGGGGGk...",
        "..dGGhGGg....k....gGGhGGd...",
        ".kGGg.dGGg.......gGGd.gGGk..",
        ".dGd...kGGgggggggGGk...dGd..",
        ".kk.....kddGGGGGddk.....kk..",
        ".........kkkkkkkkk..........",
        "............................",
        "............................",
    ],
    "crown_back": [
        "..........gggggg........",
        "........gGhhEEhhGg......",
        "......gGGRReeeeRRGGg....",
        ".....gGGd........dGGg...",
        "....gGGk...h..h...kGGg..",
        "...gGGd...gG..Gg...dGGg.",
        "...GGG...gGR..RGg...GGG.",
        "..kGGk...GRe..eRG...kGGk",
        "..kGGk...gRG..GRg...kGGk",
        "...GGG....gG..Gg....GGG.",
        "...dGGk....k..k....kGGd.",
        "....kGGg..........gGGk..",
        ".....dGGgk......kgGGd...",
        "......kddGGggggGGddk....",
        "........kkkkkkkkkk......",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
    ],
    "crown_side": [
        "....................h...",
        "...................gG...",
        "..................gGG...",
        "...........h.....gGhg...",
        "..........gG....gGGGk...",
        ".........gGG...gGRRGk...",
        "...h....gGhg..gReeGk....",
        "..gG...gGGGG.gGReGk.....",
        ".gGG..gGGhGGgGGGdk..h...",
        "gGGG.gGGGGGGGGdk...gG...",
        "dGGGgGGhGGGGdk....gGG...",
        "kGGGGGGGGGGk.....gGhg...",
        ".kGGGGGGGGgggggggGGGk...",
        ".dGGhGGGGGGGGGGGGGGd....",
        "..kGGGGGGGGGGGGGGdk.....",
        "..dGGddGGddGGddGGk......",
        "..kkk..kk..kk..kk.......",
        "........................",
        "........................",
        "........................",
    ],
    "pauldron_crest": [
        "..............h.",
        ".............gG.",
        "............gGG.",
        "...........gGhg.",
        "......h...gGRGk.",
        ".....gG..gGReGk.",
        "....gGG.gGGRGk..",
        "...gGhggGGGGk...",
        "..gGGGGGGhGd....",
        ".gGGhGGGGGk.....",
        "gGGGGGGGGd......",
        "dGGhGGGGk.......",
        "kddGGGGk........",
        ".kkkkkk.........",
        "................",
        "................",
    ],
    "back_crest": [
        "................h.",
        "...............gG.",
        "..............gGG.",
        ".............gGhg.",
        "............gGRGk.",
        "...........gGReGk.",
        ".........hgGRReGk.",
        "........gGGGRRGk..",
        ".......gGGhGGGGk..",
        "......gGGGGGGGd...",
        "....hgGGhGGGGk....",
        "...gGGGGGGGGd.....",
        "..gGGhGGGGdk......",
        ".gGGGGGGGk........",
        "gGGhGGGGd.........",
        "GGGGGGGk..........",
        "dGGhGGk...........",
        "kGGGGdk...........",
        ".kGGdk............",
        "..kdk.............",
        "...k..............",
        "..................",
        "..................",
        "..................",
        "..................",
        "..................",
        "..................",
        "..................",
    ],
    "boot_wing": [
        "............h.",
        "...........gG.",
        "..........gGG.",
        ".........gGhg.",
        ".....h..gGRGk.",
        "....gG.gGReGk.",
        "...gGGgGGRGk..",
        "..gGhGGGGGd...",
        ".gGGGGGGdk....",
        "gGGhGGGGk.....",
        "dGGGGGGd......",
        "kddGGdk.......",
        "..kkkk........",
        "..............",
    ],
}


SETS = [
    ("starlight", STARLIGHT_SPEC, STARLIGHT_PAL, STARLIGHT_GLOW, STARLIGHT_SPRITES),
    ("prismatic", PRISMATIC_SPEC, PRISMATIC_PAL, PRISMATIC_GLOW, PRISMATIC_SPRITES),
    ("dragonscale", DRAGONSCALE_SPEC, DRAGONSCALE_PAL, DRAGONSCALE_GLOW, DRAGONSCALE_SPRITES),
    ("twin_star", TWIN_STAR_SPEC, TWIN_STAR_PAL, TWIN_STAR_GLOW, TWIN_STAR_SPRITES),
]


def main():
    for name, spec, pal, glow_keys, sprites in SETS:
        geo_path = GEO_DIR / f"{name}_armor.geo.json"
        tex_path = TEX_DIR / f"{name}_armor.png"
        glow_path = TEX_DIR / f"{name}_armor_glowmask.png"

        write_armor_geo(spec, geo_path)
        img, glow = build_armor_textures(spec, pal, glow_keys, sprites)
        TEX_DIR.mkdir(parents=True, exist_ok=True)
        img.save(tex_path)
        glow.save(glow_path)
        print(f"Wrote {geo_path}, {tex_path} ({img.size}), {glow_path} ({glow.size})")


if __name__ == "__main__":
    main()
