"""Generates bespoke 16x16 item icons for Section 2.3: Tool Sets, Boss Summons, Trophies & Materials (39 items).

Covers all remaining 39 item icons in the Artilian item atlas:
  Tool Sets (16):
    - sunsteel_{pickaxe,axe,shovel,hoe}
    - starlight_{pickaxe,axe,shovel,hoe}
    - prismatic_{pickaxe,axe,shovel,hoe}
    - dragonscale_{pickaxe,axe,shovel,hoe}
  Boss Summons & Keys (4):
    - golem_core, brimstone_compass, ocular_key, primal_dragon_crest
  Boss Trophies (5):
    - colossus_heart, prism_core, brimstone_doubloon, all_seers_lens, sovereigns_ember_heart
  Materials & Drops (6):
    - raw_sunsteel, sunsteel_ingot, starlight_crystal, gazing_eye, dragon_scale, twin_star_core
  Companion, Pet, Utility & Doors (8):
    - chester_whistle, biome_treat, capture_crystal, capture_crystal_fainted,
      capture_crystal_filled, capture_crystal_summoned, crystal_canopy_door, emberwood_door

Usage:
    python tools/art/make_section_2_3_art.py
"""
from __future__ import annotations

from pathlib import Path
import sys
from PIL import Image, ImageDraw

OUT_DIR = Path("src/main/resources/assets/artilian/textures/item")
PREVIEW_DIR = Path("qa/evidence/previews")

PALETTES: dict[str, dict[str, tuple[int, int, int, int]]] = {
    "sunsteel": {
        ".": (0, 0, 0, 0),
        "0": (14, 7, 22, 255),      # Deep obsidian outline
        "1": (28, 14, 42, 255),     # Dark obsidian steel
        "2": (48, 26, 68, 255),     # Mid obsidian-purple steel
        "3": (76, 46, 102, 255),    # Bright blade bevel
        "r": (96, 16, 28, 255),     # Crimson grip shadow
        "R": (162, 30, 44, 255),    # Crimson grip wrap
        "k": (62, 18, 2, 255),      # Dark gold outline
        "d": (138, 54, 4, 255),     # Deep hammered gold
        "g": (212, 118, 10, 255),   # Hammered gold
        "G": (248, 182, 26, 255),   # Bright gold
        "h": (255, 236, 128, 255),  # Gold specular
        "e": (255, 108, 0, 255),    # Molten ember
        "E": (255, 224, 72, 255),   # Solar core
    },
    "starlight": {
        ".": (0, 0, 0, 0),
        "0": (10, 14, 34, 255),     # Midnight outline
        "1": (20, 28, 62, 255),     # Deep indigo steel
        "2": (34, 48, 96, 255),     # Mid indigo steel
        "3": (56, 78, 138, 255),    # Steel highlight
        "k": (10, 44, 72, 255),     # Deep teal-slate
        "d": (20, 102, 142, 255),   # Deep starlight cyan
        "c": (48, 180, 214, 255),   # Starlight cyan
        "C": (118, 236, 252, 255),  # Bright crystal edge
        "h": (226, 254, 255, 255),  # Pure starlight gleam
        "l": (72, 40, 22, 255),     # Leather shadow
        "L": (128, 76, 44, 255),    # Warm leather grip
        "e": (56, 217, 192, 255),   # Teal rune glow
        "E": (190, 255, 255, 255),  # Gem core
    },
    "prismatic": {
        ".": (0, 0, 0, 0),
        "0": (16, 12, 20, 255),     # Obsidian outline
        "1": (30, 24, 36, 255),     # Dark iron spine
        "2": (52, 42, 62, 255),     # Cutlass steel
        "3": (84, 70, 96, 255),     # Steel edge highlight
        "k": (54, 22, 4, 255),      # Dark gold outline
        "d": (132, 66, 10, 255),    # Deep brass-gold
        "g": (212, 136, 24, 255),   # Admiral gold
        "G": (248, 196, 62, 255),   # Bright gold
        "h": (255, 242, 148, 255),  # Gold highlight
        "r": (114, 22, 34, 255),    # Crimson grip
        "R": (168, 40, 52, 255),    # Bright crimson
        "p": (162, 46, 220, 255),   # Prism violet
        "P": (232, 136, 255, 255),  # Prism highlight
        "e": (255, 98, 14, 255),    # Magma ember
        "E": (255, 222, 82, 255),   # Magma core
    },
    "dragonscale": {
        ".": (0, 0, 0, 0),
        "0": (8, 26, 18, 255),      # Deep jungle outline
        "1": (16, 52, 34, 255),     # Dark scale shadow
        "2": (28, 92, 54, 255),     # Emerald scale mid
        "3": (48, 140, 82, 255),    # Bright scale ridge
        "g": (34, 160, 86, 255),    # Vivid emerald edge
        "G": (84, 218, 134, 255),   # Razor emerald highlight
        "h": (200, 255, 218, 255),  # Edge glint
        "k": (18, 24, 28, 255),     # Slate-iron dark
        "d": (52, 62, 72, 255),     # Slate-iron mid
        "b": (140, 152, 156, 255),  # Drake bone
        "B": (222, 224, 212, 255),  # Bone horn tip
        "e": (255, 126, 12, 255),   # Amber ember
        "E": (255, 228, 88, 255),   # Dragon-eye core
    },
    "relic": {
        ".": (0, 0, 0, 0),
        "0": (14, 10, 24, 255),     # Deep void-slate outline
        "1": (28, 22, 44, 255),     # Dark arcane slate
        "2": (48, 38, 72, 255),     # Mid arcane slate
        "3": (76, 62, 110, 255),    # Light arcane slate
        "k": (58, 24, 4, 255),      # Dark gold outline
        "d": (136, 66, 10, 255),    # Deep brass-gold
        "g": (214, 134, 22, 255),   # Gold mid
        "G": (250, 198, 64, 255),   # Bright gold
        "h": (255, 244, 160, 255),  # Gold specular
        "c": (44, 178, 212, 255),   # Starlight cyan
        "C": (124, 242, 255, 255),  # Bright cyan
        "w": (232, 254, 255, 255),  # White-hot gleam
        "W": (255, 255, 255, 255),  # Pure white specular
        "p": (142, 46, 206, 255),   # Amethyst / twilight violet
        "P": (218, 108, 255, 255),  # Bright magenta-violet
        "m": (166, 28, 42, 255),    # Crimson
        "M": (244, 78, 52, 255),    # Bright flame red
        "e": (255, 118, 12, 255),   # Ember orange
        "E": (255, 226, 78, 255),   # Solar yellow
        "v": (24, 140, 74, 255),    # Emerald shadow
        "V": (68, 214, 124, 255),   # Bright emerald
        "s": (132, 146, 164, 255),  # Silver shadow
        "S": (208, 220, 236, 255),  # Platinum silver
        "n": (92, 54, 28, 255),     # Warm wood shadow
        "N": (152, 96, 52, 255),    # Warm oak wood
    },
}

ICONS: dict[str, tuple[str, list[str]]] = {
    # =========================================================================
    # 1. SUNSTEEL TOOLS (4) — Classic vanilla silhouettes + Sunsteel palette
    # =========================================================================
    "sunsteel_pickaxe": (
        "sunsteel",
        [
            "................",
            ".....kGGGGk.....",
            "....kG3223Gk....",
            "...kG21001eGk...",
            "...kk0...kEG2k..",
            "........kRr32Gk.",
            ".......kRr0123k.",
            "......kRr0.012Gk",
            ".....kRr0...k1Gk",
            "....kRr0.....kGk",
            "...kRr0......kGk",
            "..kRr0........kk",
            ".kRr0...........",
            ".kG0............",
            "..kk............",
            "................",
        ],
    ),
    "sunsteel_axe": (
        "sunsteel",
        [
            "................",
            "......kGGGk.....",
            ".....kG323Gk....",
            "....kG3211eGk...",
            "....kG211eEGk...",
            "....kG21kRr0k...",
            ".....kkRr0......",
            "......kRr0......",
            ".....kRr0.......",
            "....kRr0........",
            "...kRr0.........",
            "..kRr0..........",
            ".kRr0...........",
            ".kG0............",
            "..kk............",
            "................",
        ],
    ),
    "sunsteel_shovel": (
        "sunsteel",
        [
            "................",
            "..........kGGGk.",
            ".........kG323Gk",
            ".........k3212Gk",
            ".........k21eE1k",
            ".........kG1e1k.",
            "........kRrkk...",
            ".......kRr0.....",
            "......kRr0......",
            ".....kRr0.......",
            "....kRr0........",
            "...kRr0.........",
            "..kRr0..........",
            ".kG0............",
            "..kk............",
            "................",
        ],
    ),
    "sunsteel_hoe": (
        "sunsteel",
        [
            "................",
            ".....kGGGGk.....",
            "....kG3223Gk....",
            "...kG21001eGk...",
            "...kk0...kEGk...",
            "........kRr0....",
            ".......kRr0.....",
            "......kRr0......",
            ".....kRr0.......",
            "....kRr0........",
            "...kRr0.........",
            "..kRr0..........",
            ".kRr0...........",
            ".kG0............",
            "..kk............",
            "................",
        ],
    ),

    # =========================================================================
    # 2. STARLIGHT TOOLS (4) — Classic vanilla silhouettes + Starlight palette
    # =========================================================================
    "starlight_pickaxe": (
        "starlight",
        [
            "................",
            ".....kCCCCk.....",
            "....kC3223Ck....",
            "...kC21001eCk...",
            "...kk0...kEC2k..",
            "........kLl32Ck.",
            ".......kLl0123k.",
            "......kLl0.012Ck",
            ".....kLl0...k1Ck",
            "....kLl0.....kCk",
            "...kLl0......kCk",
            "..kLl0........kk",
            ".kLl0...........",
            ".kC0............",
            "..kk............",
            "................",
        ],
    ),
    "starlight_axe": (
        "starlight",
        [
            "................",
            "......kCCCk.....",
            ".....kC323Ck....",
            "....kC3211eCk...",
            "....kC211eECk...",
            "....kC21kLl0k...",
            ".....kkLl0......",
            "......kLl0......",
            ".....kLl0.......",
            "....kLl0........",
            "...kLl0.........",
            "..kLl0..........",
            ".kLl0...........",
            ".kC0............",
            "..kk............",
            "................",
        ],
    ),
    "starlight_shovel": (
        "starlight",
        [
            "................",
            "..........kCCCk.",
            ".........kC323Ck",
            ".........k3212Ck",
            ".........k21eE1k",
            ".........kC1e1k.",
            "........kLlkk...",
            ".......kLl0.....",
            "......kLl0......",
            ".....kLl0.......",
            "....kLl0........",
            "...kLl0.........",
            "..kLl0..........",
            ".kC0............",
            "..kk............",
            "................",
        ],
    ),
    "starlight_hoe": (
        "starlight",
        [
            "................",
            ".....kCCCCk.....",
            "....kC3223Ck....",
            "...kC21001eCk...",
            "...kk0...kECk...",
            "........kLl0....",
            ".......kLl0.....",
            "......kLl0......",
            ".....kLl0.......",
            "....kLl0........",
            "...kLl0.........",
            "..kLl0..........",
            ".kLl0...........",
            ".kC0............",
            "..kk............",
            "................",
        ],
    ),

    # =========================================================================
    # 3. PRISMATIC TOOLS (4) — Classic vanilla silhouettes + Prismatic palette
    # =========================================================================
    "prismatic_pickaxe": (
        "prismatic",
        [
            "................",
            ".....kGGGGk.....",
            "....kG3pP3Gk....",
            "...kG21001eGk...",
            "...kk0...kEG2k..",
            "........kRr32Gk.",
            ".......kRr0123k.",
            "......kRr0.01pGk",
            ".....kRr0...kPGk",
            "....kRr0.....kGk",
            "...kRr0......kGk",
            "..kRr0........kk",
            ".kRr0...........",
            ".kP0............",
            "..kk............",
            "................",
        ],
    ),
    "prismatic_axe": (
        "prismatic",
        [
            "................",
            "......kGGGk.....",
            ".....kG3p3Gk....",
            "....kG32P1eGk...",
            "....kG211eEGk...",
            "....kG21kRr0k...",
            ".....kkRr0......",
            "......kRr0......",
            ".....kRr0.......",
            "....kRr0........",
            "...kRr0.........",
            "..kRr0..........",
            ".kRr0...........",
            ".kP0............",
            "..kk............",
            "................",
        ],
    ),
    "prismatic_shovel": (
        "prismatic",
        [
            "................",
            "..........kGGGk.",
            ".........kG3p3Gk",
            ".........k32P2Gk",
            ".........k21eE1k",
            ".........kG1e1k.",
            "........kRrkk...",
            ".......kRr0.....",
            "......kRr0......",
            ".....kRr0.......",
            "....kRr0........",
            "...kRr0.........",
            "..kRr0..........",
            ".kP0............",
            "..kk............",
            "................",
        ],
    ),
    "prismatic_hoe": (
        "prismatic",
        [
            "................",
            ".....kGGGGk.....",
            "....kG3pP3Gk....",
            "...kG21001eGk...",
            "...kk0...kEGk...",
            "........kRr0....",
            ".......kRr0.....",
            "......kRr0......",
            ".....kRr0.......",
            "....kRr0........",
            "...kRr0.........",
            "..kRr0..........",
            ".kRr0...........",
            ".kP0............",
            "..kk............",
            "................",
        ],
    ),

    # =========================================================================
    # 4. DRAGONSCALE TOOLS (4) — Classic vanilla silhouettes + Dragonscale palette
    # =========================================================================
    "dragonscale_pickaxe": (
        "dragonscale",
        [
            "................",
            ".....kGGGGk.....",
            "....kG3223Gk....",
            "...kG21001eGk...",
            "...kk0...kEG2k..",
            "........kdk32Gk.",
            ".......kdk0123k.",
            "......kdk0.012Gk",
            ".....kdk0...k1Gk",
            "....kdk0.....kGk",
            "...kdk0......kGk",
            "..kdk0........kk",
            ".kdk0...........",
            ".kB0............",
            "..kk............",
            "................",
        ],
    ),
    "dragonscale_axe": (
        "dragonscale",
        [
            "................",
            "......kGGGk.....",
            ".....kG323Gk....",
            "....kG3211eGk...",
            "....kG211eEGk...",
            "....kG21kdk0k...",
            ".....kkdk0......",
            "......kdk0......",
            ".....kdk0.......",
            "....kdk0........",
            "...kdk0.........",
            "..kdk0..........",
            ".kdk0...........",
            ".kB0............",
            "..kk............",
            "................",
        ],
    ),
    "dragonscale_shovel": (
        "dragonscale",
        [
            "................",
            "..........kGGGk.",
            ".........kG323Gk",
            ".........k3212Gk",
            ".........k21eE1k",
            ".........kG1e1k.",
            "........kdkkk...",
            ".......kdk0.....",
            "......kdk0......",
            ".....kdk0.......",
            "....kdk0........",
            "...kdk0.........",
            "..kdk0..........",
            ".kB0............",
            "..kk............",
            "................",
        ],
    ),
    "dragonscale_hoe": (
        "dragonscale",
        [
            "................",
            ".....kGGGGk.....",
            "....kG3223Gk....",
            "...kG21001eGk...",
            "...kk0...kEGk...",
            "........kdk0....",
            ".......kdk0.....",
            "......kdk0......",
            ".....kdk0.......",
            "....kdk0........",
            "...kdk0.........",
            "..kdk0..........",
            ".kdk0...........",
            ".kB0............",
            "..kk............",
            "................",
        ],
    ),

    # =========================================================================
    # 5. BOSS SUMMONS & KEYS (4)
    # =========================================================================
    "golem_core": (
        "relic",
        [
            ".....kkkkkk.....",
            "...kdgGGGGgdk...",
            "..kd32222223dk..",
            ".kd3211111123dk.",
            ".k321kddddk123k.",
            ".k321dCCCCd123k.",
            ".k321dCwWCd123k.",
            ".k321dCCCCd123k.",
            ".k321dEEEEc123k.",
            ".k321kddddk123k.",
            ".kd3211111123dk.",
            "..kd32211223dk..",
            "...kdgGddGGgdk..",
            ".....kkkkkk.....",
            "................",
            "................",
        ],
    ),
    "brimstone_compass": (
        "relic",
        [
            "......kGGk......",
            ".....kGhhGk.....",
            "....kdk..kdk....",
            "...kdG3223Gdk...",
            "..kdG211112Gdk..",
            "..kG211MM112Gk..",
            ".kG211MEEM112Gk.",
            ".kG21cCwWCc12Gk.",
            ".kG211cCCc112Gk.",
            ".kG2111cc1112Gk.",
            "..kG21111112Gk..",
            "..kdG211112Gdk..",
            "...kdG3223Gdk...",
            "....kdGGGGdk....",
            ".....kkkkkk.....",
            "................",
        ],
    ),
    "ocular_key": (
        "relic",
        [
            ".....kkkkkk.....",
            "...kd332233dk...",
            "..k321kPPk123k..",
            "..k31pPCwCp13k..",
            "..k31pCCwCp13k..",
            "..k321kPPk123k..",
            "...kd332233dk...",
            ".....k3223k.....",
            "......k32k......",
            "......k32kGGk...",
            "......k32kGhk...",
            "......k32k......",
            "......k32kGGk...",
            "......k32kdGk...",
            "......k21k......",
            ".......kk.......",
        ],
    ),
    "primal_dragon_crest": (
        "relic",
        [
            ".kV..........Vk.",
            ".kVv........vVk.",
            "..kVv.kEEk.vVk..",
            "..kVvkEMMEkvVk..",
            "...kVkmEEEmkVk..",
            "...kdGGEEGGdk...",
            "..kd321MM123dk..",
            "..k321mEEm123k..",
            "...k32111123k...",
            "....k322223k....",
            "....kdG33Gdk....",
            ".....kdGGdk.....",
            "......kGGk......",
            ".......kk.......",
            "................",
            "................",
        ],
    ),

    # =========================================================================
    # 6. BOSS TROPHIES (5)
    # =========================================================================
    "colossus_heart": (
        "relic",
        [
            "....kkk..kkk....",
            "..kpPPPkkpPPpk..",
            ".kpPwwPPpPCCCPk.",
            ".kpPwPPCCwCCCPk.",
            ".kpPPCCCwCCCCpk.",
            "..kpPCCCCCCCCpk.",
            "..kpPCCCCCCCpk..",
            "...kpPCCCCCpk...",
            "....kpPCCCpk....",
            ".....kpPCpk.....",
            "......kppk......",
            ".......kk.......",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "prism_core": (
        "relic",
        [
            ".......kk.......",
            "......kCwk......",
            ".....kCCwPk.....",
            "....kCCCwPPk....",
            "...kCCCCwPPPk...",
            "..kCCCCwwPPPPk..",
            ".kCCwCwEEwPPPGk.",
            ".kCCwwEEEEwPPGk.",
            "..kCCCwEEwPPGk..",
            "...kCCCwwPPGk...",
            "....kCCCPPGk....",
            ".....kCCPGk.....",
            "......kCGk......",
            ".......kk.......",
            "................",
            "................",
        ],
    ),
    "brimstone_doubloon": (
        "relic",
        [
            ".....kkkkkk.....",
            "...kdGGhhGGdk...",
            "..kdG322223Gdk..",
            ".kdG21dGGd12Gdk.",
            ".kG21dMEEMd12Gk.",
            ".kG21GMEEMG12Gk.",
            ".kG21GEEEEG12Gk.",
            ".kG21dGEEGd12Gk.",
            ".kG211dGGd112Gk.",
            ".kdG21111112Gdk.",
            "..kdG322223Gdk..",
            "...kdGGGGGGdk...",
            ".....kkkkkk.....",
            "................",
            "................",
            "................",
        ],
    ),
    "all_seers_lens": (
        "relic",
        [
            ".....kkkkkk.....",
            "...kdGGhhGGdk...",
            "..kdG211112Gdk..",
            ".kdG21pPPp12Gdk.",
            ".kG21pPCwCp12Gk.",
            ".kG21pCCwCp12Gk.",
            ".kG21pPCCPp12Gk.",
            ".kdG21pPPp12Gdk.",
            "..kdG211112Gdk..",
            "...kdGGGGGGdk...",
            ".....kkkkkk.....",
            ".......kdGk.....",
            "........kdGk....",
            ".........kdGk...",
            "..........kk....",
            "................",
        ],
    ),
    "sovereigns_ember_heart": (
        "relic",
        [
            "....kG.kG.Gk....",
            "...kGhGGhhGhk...",
            "..kmMMGGGGMMmk..",
            ".kmMMEEEEEEMMmk.",
            ".kmMEEwwwwEEMmk.",
            ".kmMEEwEEwEEMmk.",
            "..kmMEEEEEEMmk..",
            "..kmMMEEEEMMmk..",
            "...kmMMeeMMmk...",
            "....kmMeeMmk....",
            ".....kmMMmk.....",
            "......kmmk......",
            ".......kk.......",
            "................",
            "................",
            "................",
        ],
    ),

    # =========================================================================
    # 7. MATERIALS & DROPS (6)
    # =========================================================================
    "raw_sunsteel": (
        "sunsteel",
        [
            "......kkkk......",
            "....k023320k....",
            "...k023GG320k...",
            "..k021GEe1230k..",
            "..k0123G112320k.",
            ".k011220012GG20k",
            ".k01Ggh1012Ge10k",
            "..k02G10123210k.",
            "..k0110123210k..",
            "...k00121110k...",
            "....k011100k....",
            ".....kkkkkk.....",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "sunsteel_ingot": (
        "sunsteel",
        [
            "................",
            "................",
            "......kkkkk.....",
            "....kdGGGGhdk...",
            "...kdG3223GGdk..",
            "..kdG21eE123Gdk.",
            "..kG21eEE1232Gk.",
            "..k1211112321Gk.",
            "..k112223211dGk.",
            "..k0112211ddGk..",
            "...k0011ddddk...",
            "....kkkkkkkk....",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "starlight_crystal": (
        "starlight",
        [
            ".......kk.......",
            "......kChk......",
            ".....kChCCk.....",
            "....kChCCCck....",
            "....kChCCcCk....",
            "...kChCCcCCck...",
            "...kCCCcCCCck...",
            "...kCCcCCdCck...",
            "....kCcCCdCk....",
            "....kCCddCdk....",
            ".....kCdddk.....",
            "......kddk......",
            ".......kk.......",
            "................",
            "................",
            "................",
        ],
    ),
    "gazing_eye": (
        "relic",
        [
            ".....kkkkkk.....",
            "...k33222233k...",
            "..k32pPPPPp23k..",
            ".k32pPPCCPPp23k.",
            ".k3pPPCwwCPPp3k.",
            ".k3pPPC0wCPPp3k.",
            ".k3pPPC00CPPp3k.",
            ".k3pPPCCCCPPp3k.",
            ".k32pPPPPPPp23k.",
            "..k322pppp223k..",
            "...k33222233k...",
            ".....kkkkkk.....",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "dragon_scale": (
        "dragonscale",
        [
            ".....kkkkkk.....",
            "...k3GGGGGG3k...",
            "..k3GghhhhGG3k..",
            ".k3Gg332233gG3k.",
            ".k3g32211223g3k.",
            ".k3221B33B1223k.",
            "..k3211BB1123k..",
            "..k2211111122k..",
            "...k21111112k...",
            "....k111111k....",
            ".....k1111k.....",
            "......kkkk......",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "twin_star_core": (
        "relic",
        [
            ".....kkkkkk.....",
            "...kSShGGGhSSk..",
            "..kSSCCGGEEESSk.",
            ".kSSCCCwGEEEESk.",
            ".kSCCCwwEEwEESk.",
            ".kSCCCwwEEwEESk.",
            ".kSCCCcGEEEEESk.",
            ".kSSCCcGGEEESSk.",
            "..kSSccGGeeSSk..",
            "...kSSdGGGdSSk..",
            ".....kkkkkk.....",
            "................",
            "................",
            "................",
            "................",
            "................",
        ],
    ),

    # =========================================================================
    # 8. COMPANION, PET, UTILITY & DOORS (8)
    # =========================================================================
    "chester_whistle": (
        "relic",
        [
            "................",
            ".........kkkk...",
            ".......kdGGGhk..",
            "......kdGNNNNdk.",
            ".....kdGNnNNnGk.",
            "....kNNnNNNNnGk.",
            "...kNNNNk00kNGk.",
            "..kNNNNNk00kNGk.",
            "..kNnNNnGGGGnGk.",
            "...kNnnNNNNnnk..",
            "....kdddddddk...",
            ".....kkkkkkk....",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "biome_treat": (
        "relic",
        [
            ".......kk.......",
            "......kGGk......",
            ".....kGhhGk.....",
            ".kkkkGNNNNkkkk..",
            "kGhhGNNNNNNGhhGk",
            ".kGNNVVPMPVVNGk.",
            "..kNNNVVVVVNNk..",
            "...kNNNNNNNNk...",
            "..kGNNNNNNNNGk..",
            "..kGNnNkkNnNGk..",
            ".kGnnk....knnnk.",
            ".kkkk......kkkk.",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "capture_crystal": (
        "relic",
        [
            ".....kkkkkk.....",
            "...kSSwCCwSSk...",
            "..kSSCCCCCCSSk..",
            ".kSSCCCCCCCCSSk.",
            ".k1111kSSk1111k.",
            ".k111kSssSk111k.",
            ".k1111kSSk1111k.",
            ".kssSSSSSSSSssk.",
            "..kssSSSSSSssk..",
            "...kssSSSSssk...",
            ".....kkkkkk.....",
            "................",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "capture_crystal_fainted": (
        "relic",
        [
            ".....kkkkkk.....",
            "...k11s22s11k...",
            "..k1122222211k..",
            ".k112222222211k.",
            ".k0000kGGk0000k.",
            ".k000kGEeGk000k.",
            ".k0000kGGk0000k.",
            ".k112222222211k.",
            "..k1122222211k..",
            "...k11s22s11k...",
            ".....kkkkkk.....",
            "................",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "capture_crystal_filled": (
        "relic",
        [
            ".....kkkkkk.....",
            "...kVVwCCwVVk...",
            "..kVVCCCCCCVVk..",
            ".kVVCCCCCCCCVVk.",
            ".k1111kGGk1111k.",
            ".k111kGwwGk111k.",
            ".k1111kGGk1111k.",
            ".kssSVVVVVVSssk.",
            "..kssSVVVVSssk..",
            "...kssSVVSssk...",
            ".....kkkkkk.....",
            "................",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "capture_crystal_summoned": (
        "relic",
        [
            "......kCCk......",
            ".....kCwwCk.....",
            "....kPPCCPPk....",
            "...kPCCCCCCPk...",
            "..kPCCCCCCCCPk..",
            ".k1111kCCk1111k.",
            ".k111kCwwCk111k.",
            ".k1111kCCk1111k.",
            ".kssSSSSSSSSssk.",
            "..kssSSSSSSssk..",
            "...kssSSSSssk...",
            ".....kkkkkk.....",
            "................",
            "................",
            "................",
            "................",
        ],
    ),
    "crystal_canopy_door": (
        "starlight",
        [
            "....kkkkkkkk....",
            "...k22333322k...",
            "..k231CCCC132k..",
            "..k231ChhC132k..",
            "..k231CCCC132k..",
            "..k2311111132k..",
            "..k2322222232k..",
            "..k2322222232k..",
            "..k232222EL32k..",
            "..k2322222232k..",
            "..k2311111132k..",
            "..k2312222132k..",
            "..k2312222132k..",
            "..k2311111132k..",
            "..k2222222222k..",
            "..kkkkkkkkkkkk..",
        ],
    ),
    "emberwood_door": (
        "sunsteel",
        [
            "....kkkkkkkk....",
            "...krrRRRRrrk...",
            "..krR1eEEe1Rrk..",
            "..krR1EEEE1Rrk..",
            "..krR1eEEe1Rrk..",
            "..krR111111Rrk..",
            "..krR222222Rrk..",
            "..krR222222Rrk..",
            "..krR2222GhRrk..",
            "..krR222222Rrk..",
            "..krR111111Rrk..",
            "..krR122221Rrk..",
            "..krR122221Rrk..",
            "..krR111111Rrk..",
            "..krrrrrrrrrrk..",
            "..kkkkkkkkkkkk..",
        ],
    ),
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


def generate_previews() -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    items = list(ICONS.keys())
    for name in items:
        img = Image.open(OUT_DIR / f"{name}.png").convert("RGBA")
        zoomed = img.resize((64, 64), Image.Resampling.NEAREST)
        zoomed.save(PREVIEW_DIR / f"{name}-preview.png")

    cols = 8
    rows = (len(items) + cols - 1) // cols
    cell_w, cell_h = 112, 96
    pad = 16
    sheet_w = pad * 2 + cols * cell_w
    sheet_h = pad * 2 + rows * cell_h
    sheet = Image.new("RGBA", (sheet_w, sheet_h), (43, 35, 56, 255))
    draw = ImageDraw.Draw(sheet)

    for idx, name in enumerate(items):
        r, c = divmod(idx, cols)
        x0 = pad + c * cell_w
        y0 = pad + r * cell_h
        draw.rounded_rectangle(
            [x0 + 4, y0 + 4, x0 + cell_w - 4, y0 + cell_h - 4],
            radius=6,
            fill=(58, 47, 77, 255),
            outline=(106, 90, 140, 255),
        )
        icon = Image.open(PREVIEW_DIR / f"{name}-preview.png").convert("RGBA")
        sheet.paste(icon, (x0 + (cell_w - 64) // 2, y0 + 8), icon)
        label = name.replace("_", " ")
        if len(label) > 15:
            label = label[:14] + "."
        draw.text((x0 + 8, y0 + 76), label, fill=(255, 246, 214, 255))

    sheet.save(PREVIEW_DIR / "section_2_3_items-preview.png")
    print(f"Generated {len(items)} individual previews and overview sheet in {PREVIEW_DIR}")


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    count = 0
    for item_name, (pal_name, grid) in ICONS.items():
        pal = PALETTES[pal_name]
        out_path = OUT_DIR / f"{item_name}.png"
        render_icon(grid, pal).save(out_path)
        count += 1
    print(f"Generated {count} Section 2.3 item icons in {OUT_DIR}")
    if "--previews" in sys.argv:
        generate_previews()


if __name__ == "__main__":
    main()
