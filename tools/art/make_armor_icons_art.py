"""Generates bespoke 16x16 item icons for all 5 v3 Armor Sets (20 icons total).

Each icon uses the exact hue-shifted palette and signature v3 silhouette of its
corresponding 256x256 3D GeckoLib armor set:
  1. Sunsteel       — obsidian-crimson plate, hammered gold filigree, solar crown, molten glow
  2. Starlight      — midnight-indigo cowl, ice-cyan crystal shards, amethyst gem, teal visor
  3. Prismatic      — basalt juggernaut, swept bull horns, brimstone bronze, magma & amethyst
  4. Dragonscale    — emerald-jade scales, slate-steel trim, swept dragon horns, amber core
  5. Twin-Star      — platinum-silver seraph plate, void-indigo under-suit, gold & cyan twin stars

Usage:
    python tools/art/make_armor_icons_art.py
"""
from __future__ import annotations

from pathlib import Path
from PIL import Image

OUT_DIR = Path("src/main/resources/assets/artilian/textures/item")

# Shared legend:
# '.' = transparent
# '0','1','2','3' = dark under-plate / shadow -> highlight
# 'r','R' = secondary accent (crimson / amethyst / slate-steel / cyan)
# 'k','d','g','G','h' = primary trim / filigree / scale / crystal (outline -> dark -> mid -> bright -> glint)
# 'e','E' = emissive core / eyes

PALETTES: dict[str, dict[str, tuple[int, int, int, int]]] = {
    "sunsteel": {
        ".": (0, 0, 0, 0),
        "0": (14, 7, 22, 255),
        "1": (26, 13, 39, 255),
        "2": (44, 24, 64, 255),
        "3": (68, 42, 94, 255),
        "r": (74, 11, 22, 255),
        "R": (148, 24, 38, 255),
        "k": (62, 18, 2, 255),
        "d": (132, 50, 4, 255),
        "g": (208, 114, 8, 255),
        "G": (248, 178, 22, 255),
        "h": (255, 234, 118, 255),
        "e": (255, 108, 0, 255),
        "E": (255, 224, 72, 255),
    },
    "starlight": {
        ".": (0, 0, 0, 0),
        "0": (8, 12, 30, 255),
        "1": (16, 24, 54, 255),
        "2": (28, 44, 92, 255),
        "3": (54, 78, 140, 255),
        "r": (88, 38, 168, 255),
        "R": (168, 92, 248, 255),
        "k": (12, 46, 76, 255),
        "d": (24, 108, 152, 255),
        "g": (56, 188, 224, 255),
        "G": (132, 238, 255, 255),
        "h": (232, 254, 255, 255),
        "e": (56, 217, 192, 255),
        "E": (182, 255, 255, 255),
    },
    "prismatic": {
        ".": (0, 0, 0, 0),
        "0": (12, 9, 14, 255),
        "1": (22, 17, 26, 255),
        "2": (38, 30, 44, 255),
        "3": (62, 50, 72, 255),
        "r": (110, 28, 152, 255),
        "R": (208, 72, 255, 255),
        "k": (36, 12, 6, 255),
        "d": (92, 32, 12, 255),
        "g": (168, 62, 16, 255),
        "G": (235, 118, 28, 255),
        "h": (255, 206, 110, 255),
        "e": (255, 58, 18, 255),
        "E": (255, 192, 56, 255),
    },
    "dragonscale": {
        ".": (0, 0, 0, 0),
        "0": (8, 28, 18, 255),
        "1": (15, 52, 32, 255),
        "2": (24, 86, 52, 255),
        "3": (42, 132, 78, 255),
        "r": (24, 32, 40, 255),
        "R": (64, 80, 98, 255),
        "k": (6, 36, 22, 255),
        "d": (18, 96, 56, 255),
        "g": (34, 172, 94, 255),
        "G": (92, 224, 142, 255),
        "h": (196, 255, 214, 255),
        "e": (255, 128, 12, 255),
        "E": (255, 228, 88, 255),
    },
    "twin_star": {
        ".": (0, 0, 0, 0),
        "0": (16, 12, 36, 255),
        "1": (28, 22, 60, 255),
        "2": (48, 38, 92, 255),
        "3": (76, 64, 134, 255),
        "r": (24, 118, 152, 255),
        "R": (56, 217, 192, 255),
        "k": (58, 66, 86, 255),
        "d": (128, 140, 168, 255),
        "g": (204, 214, 232, 255),
        "G": (240, 246, 255, 255),
        "h": (255, 232, 132, 255),
        "e": (104, 238, 255, 255),
        "E": (255, 216, 78, 255),
    },
}

ICONS: dict[str, dict[str, list[str]]] = {
    # -----------------------------------------------------------------
    # 1. SUNSTEEL — Swept solar crown, gold filigree, ruby & obsidian
    # -----------------------------------------------------------------
    "sunsteel": {
        "helmet": [
            ".......GhG......",
            "..k...GghgG...k.",
            ".kGk..gGRGg..kGk",
            ".dGgk.dGrGd.kgGd",
            ".dGGgkdGGGdkgGGd",
            ".kG2222GGG2222Gk",
            ".kG1000gRg0001Gk",
            ".k100eE000Ee001k",
            ".k1000000000001k",
            ".kg10001110001gk",
            ".kG10012321001Gk",
            ".dG1r1122211r1Gd",
            "..kdg1110111gdk.",
            "...kdGGg0gGGdk..",
            "....kkdg.gdkk...",
            "................",
        ],
        "chestplate": [
            ".kGGk......kGGk.",
            "kGhGGk....kGGhGk",
            "dGG22gk..kg22GGd",
            "kG112GgGGgG211Gk",
            "k0111GgrrgG1110k",
            "k1100gGEeEg0011k",
            ".k100gEeEEg001k.",
            ".k111dGggGd111k.",
            "..k121dGGd121k..",
            "..k1r11GG11r1k..",
            "..k1R1gGGg1R1k..",
            "..k01dG22Gd10k..",
            "..k00dG11Gd00k..",
            "...k0dgRRgd0k...",
            "...kkddggddkk...",
            "................",
        ],
        "leggings": [
            "................",
            "..kddddggddddk..",
            "..kGGRGGGGRGGk..",
            "..k011gRRg110k..",
            "..k121dGGd121k..",
            "..k111g00g111k..",
            "..kG10k..k01Gk..",
            "..kg10k..k01gk..",
            "..kG11k..k11Gk..",
            "..kd21k..k12dk..",
            "..kG11k..k11Gk..",
            "..k101k..k101k..",
            "..k1r0k..k0r1k..",
            "..kGd0k..k0dGk..",
            "..kkkk....kkkk..",
            "................",
        ],
        "boots": [
            "................",
            "................",
            "................",
            "................",
            ".kGk........kGk.",
            "kGgk........kgGk",
            "kdg11k....k11gdk",
            "kG121k....k121Gk",
            "k1101k....k1011k",
            "k1r01k....k10r1k",
            "kGd111k..k111dGk",
            "kG12211..11221Gk",
            "k01110k..k01110k",
            "kdddddk..kdddddk",
            ".kkkkk....kkkkk.",
            "................",
        ],
    },

    # -----------------------------------------------------------------
    # 2. STARLIGHT — Jagged crystal shards, hooded cowl, amethyst & cyan
    # -----------------------------------------------------------------
    "starlight": {
        "helmet": [
            ".......hG.......",
            ".h....gGGg....h.",
            "kGk..dGRRGd..kGk",
            "dGgk.kGggGk.kgGd",
            "kGGgdGGGGGGdgGGk",
            ".dG222gGGg222Gd.",
            ".kG100kGGk001Gk.",
            ".k10eEEEEEEe01k.",
            ".k100eEEEEe001k.",
            ".kg1000000001gk.",
            ".kG10012321001Gk",
            ".dGg111222111gGd",
            "..kdGg11011gGdk.",
            "...kdGGgRgGGdk..",
            "....kkdd.ddkk...",
            "................",
        ],
        "chestplate": [
            "h..............h",
            "kGk..........kGk",
            "dGGgk......kgGGd",
            "kG22GgdkkdG22Gk.",
            "k1112GGRRGG2111k",
            "k1100dgEEgd0011k",
            ".k100geEEeg001k.",
            ".k111dGggGd111k.",
            "..k121gGGg121k..",
            "..k131dGGd131k..",
            "..k1r1gGGg1r1k..",
            "..k01dG22Gd10k..",
            "..k00gG11Gg00k..",
            "...k0dgRRgd0k...",
            "...kkddggddkk...",
            "................",
        ],
        "leggings": [
            "................",
            "..kddddggddddk..",
            "..kGGRGGGGRGGk..",
            "..k011gRRg110k..",
            "..k121dGGd121k..",
            "..k131g00g131k..",
            "..kG10k..k01Gk..",
            "..kg10k..k01gk..",
            "..kG21k..k12Gk..",
            "..kdG1k..k1Gdk..",
            "..kG11k..k11Gk..",
            "..k101k..k101k..",
            "..k1r0k..k0r1k..",
            "..kGd0k..k0dGk..",
            "..kkkk....kkkk..",
            "................",
        ],
        "boots": [
            "................",
            "................",
            "................",
            "..h..........h..",
            ".kGk........kGk.",
            "kGgk........kgGk",
            "kdg11k....k11gdk",
            "kG121k....k121Gk",
            "k1101k....k1011k",
            "k1r01k....k10r1k",
            "kGd111k..k111dGk",
            "kG12311..11321Gk",
            "k01110k..k01110k",
            "kdddddk..kdddddk",
            ".kkkkk....kkkkk.",
            "................",
        ],
    },

    # -----------------------------------------------------------------
    # 3. PRISMATIC BRIMSTONE — Bull horns, basalt juggernaut, magma & prism
    # -----------------------------------------------------------------
    "prismatic": {
        "helmet": [
            "h..............h",
            "Gh............hG",
            "kGh...kggk...hGk",
            ".kGgkdgRRgdkgGk.",
            "..kG22gGGg22Gk..",
            "..k1100000011k..",
            "..k10eE00Ee01k..",
            "..k1000000001k..",
            "..kg10011001gk..",
            "..kG101232101Gk.",
            "..k11012221011k.",
            "..kd1e11011e1dk.",
            "...kdGGg0gGGdk..",
            "....kkdg.gdkk...",
            "................",
            "................",
        ],
        "chestplate": [
            "hk............kh",
            "kGgk........kgGk",
            "dGG22k....k22GGd",
            "kG112GgGGgG211Gk",
            "k0111dgRRgd1110k",
            "k1100eGEeEGe001k",
            ".k100gEeEEeg00k.",
            ".k111kGggGk111k.",
            "..k121dGGd121k..",
            "..k1e11GG11e1k..",
            "..k1E1gRRg1E1k..",
            "..k01dG22Gd10k..",
            "..k00dG11Gd00k..",
            "...k0dgRRgd0k...",
            "...kkddggddkk...",
            "................",
        ],
        "leggings": [
            "................",
            "..kddddggddddk..",
            "..kGGRGGGGRGGk..",
            "..k011gRRg110k..",
            "..k121dGGd121k..",
            "..k1e1g00g1e1k..",
            "..kG10k..k01Gk..",
            "..kg10k..k01gk..",
            "..kG11k..k11Gk..",
            "..kd21k..k12dk..",
            "..kG11k..k11Gk..",
            "..k101k..k101k..",
            "..k1e0k..k0e1k..",
            "..kGd0k..k0dGk..",
            "..kkkk....kkkk..",
            "................",
        ],
        "boots": [
            "................",
            "................",
            "................",
            "................",
            ".kGk........kGk.",
            "kGgk........kgGk",
            "kdg11k....k11gdk",
            "kG121k....k121Gk",
            "k1e01k....k10e1k",
            "k1101k....k1011k",
            "kGd111k..k111dGk",
            "kG12211..11221Gk",
            "k01110k..k01110k",
            "kdddddk..kdddddk",
            ".kkkkk....kkkkk.",
            "................",
        ],
    },

    # -----------------------------------------------------------------
    # 4. DRAGONSCALE — Swept dragon horns, jade scales, slate-steel, amber glow
    # -----------------------------------------------------------------
    "dragonscale": {
        "helmet": [
            "R..............R",
            "rR............Rr",
            ".rRk..kGGk..kRr.",
            ".kGgkdgGGgdkgGk.",
            "..kG22gRRg22Gk..",
            "..k1100000011k..",
            "..k10eE00Ee01k..",
            "..k1000000001k..",
            "..kg10233201gk..",
            "..kG10122101Gk..",
            "..kd1r1111r1dk..",
            "...kR112211Rk...",
            "...kdGGggGGdk...",
            "....kkdggdkk....",
            "................",
            "................",
        ],
        "chestplate": [
            "Rk............kR",
            "rRgk........kgRr",
            "kGG22k....k22GGk",
            "dG112GgRRgG211Gd",
            "k0111dgGGgd1110k",
            "k1100dGEeEGd001k",
            ".k100gEeEEeg00k.",
            ".k1211dGGd1121k.",
            "..k232gGGg232k..",
            "..k1r11GG11r1k..",
            "..k1R1dGGd1R1k..",
            "..k01gG22Gg10k..",
            "..k00dG11Gd00k..",
            "...k0dgRRgd0k...",
            "...kkddggddkk...",
            "................",
        ],
        "leggings": [
            "................",
            "..kddddRRddddk..",
            "..kGGRGGGGRGGk..",
            "..k011gRRg110k..",
            "..k121dGGd121k..",
            "..k232g00g232k..",
            "..kG10k..k01Gk..",
            "..kg10k..k01gk..",
            "..kG21k..k12Gk..",
            "..kdR1k..k1Rdk..",
            "..kG11k..k11Gk..",
            "..k101k..k101k..",
            "..k1r0k..k0r1k..",
            "..kGd0k..k0dGk..",
            "..kkkk....kkkk..",
            "................",
        ],
        "boots": [
            "................",
            "................",
            "................",
            "................",
            ".kRk........kRk.",
            "kRgk........kgRk",
            "kdg11k....k11gdk",
            "kG121k....k121Gk",
            "k2301k....k1032k",
            "k1r01k....k10r1k",
            "kGd111k..k111dGk",
            "kG12211..11221Gk",
            "k01110k..k01110k",
            "kRddddk..kddddRk",
            ".kkkkk....kkkkk.",
            "................",
        ],
    },

    # -----------------------------------------------------------------
    # 5. TWIN-STAR — Seraph winged halo, platinum-silver, void-indigo, cyan+gold
    # -----------------------------------------------------------------
    "twin_star": {
        "helmet": [
            "G.....hEEh.....G",
            "gG...GghhgG...Gg",
            "dgG..gGRRGg..Ggd",
            "kGGgk.kGGk.kgGGk",
            ".dGG22gGGg22GGd.",
            ".kG1000000001Gk.",
            ".k10eE0000Ee01k.",
            ".k100000000001k.",
            ".kg10012321001gk",
            ".kG1012222101Gk.",
            ".dG1r112211r1Gd.",
            "..kdg110011gdk..",
            "...kdGhEEhGdk...",
            "....kkdggdkk....",
            "................",
            "................",
        ],
        "chestplate": [
            "Gh............hG",
            "gGGk........kGGg",
            "dGG22k....k22GGd",
            "kG112GghhgG211Gk",
            "k0111GgRRgG1110k",
            "k1100gEeEEg0011k",
            ".k100gEEeEg001k.",
            ".k111dGhhGd111k.",
            "..k121gGGg121k..",
            "..k1r11GG11r1k..",
            "..k1R1gGGg1R1k..",
            "..k01dG22Gd10k..",
            "..k00dG11Gd00k..",
            "...k0dgRRgd0k...",
            "...kkddggddkk...",
            "................",
        ],
        "leggings": [
            "................",
            "..kddddhhddddk..",
            "..kGGRGGGGRGGk..",
            "..k011gRRg110k..",
            "..k121dGGd121k..",
            "..k131h00h131k..",
            "..kG10k..k01Gk..",
            "..kg10k..k01gk..",
            "..kG21k..k12Gk..",
            "..kdG1k..k1Gdk..",
            "..kG11k..k11Gk..",
            "..k101k..k101k..",
            "..k1r0k..k0r1k..",
            "..kGd0k..k0dGk..",
            "..kkkk....kkkk..",
            "................",
        ],
        "boots": [
            "................",
            "................",
            "................",
            "h..............h",
            "GGk..........kGG",
            "gGgk........kgGg",
            "kdg11k....k11gdk",
            "kG121k....k121Gk",
            "k1101k....k1011k",
            "k1r01k....k10r1k",
            "kGd111k..k111dGk",
            "kG12311..11321Gk",
            "k01110k..k01110k",
            "kdddddk..kdddddk",
            ".kkkkk....kkkkk.",
            "................",
        ],
    },
}


def render_icon(grid: list[str], pal: dict[str, tuple[int, int, int, int]]) -> Image.Image:
    assert len(grid) == 16, f"Expected 16 rows, got {len(grid)}"
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(grid):
        assert len(row) == 16, f"Row {y} length {len(row)} != 16: {row!r}"
        for x, ch in enumerate(row):
            px[x, y] = pal[ch]
    return img


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    count = 0
    for set_name, pieces in ICONS.items():
        pal = PALETTES[set_name]
        for piece_name, grid in pieces.items():
            out_path = OUT_DIR / f"{set_name}_{piece_name}.png"
            render_icon(grid, pal).save(out_path)
            count += 1
    print(f"Generated {count} v3 armor item icons in {OUT_DIR}")


if __name__ == "__main__":
    main()
