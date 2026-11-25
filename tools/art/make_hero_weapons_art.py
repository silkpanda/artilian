"""Generates bespoke 16x16 item icons for Section 2.2: Hero Weapons & Staves (11 items).

Upgrades all 5 hero swords/blades and 6 wands/staves with distinct silhouettes,
hue-shifted multi-stop shading, and exact thematic alignment with the v3 armor sets:
  Swords / Blades (5):
    1. sunsteel_sword       - Solar Broadsword (obsidian-crimson steel, gold sun-guard, molten core)
    2. starlight_sword      - Arcane Ranger Sabre (midnight steel, cyan crystal edge, leather grip)
    3. prismatic_sword      - Admiral's Cutlass (curved blade, gold knuckle-bow, prism-magma inlay)
    4. dragonscale_sword    - Dragon-Hunter Fangblade (serrated emerald blade, bone-horn guard, amber core)
    5. twin_star_blade      - Seraphic Twin-Star Greatsword (winged platinum-gold guard, void-indigo & cyan)
  Wands & Staves (6):
    6. spark_wand           - Faceted Starlight Crystal Wand
    7. heart_bloom_staff    - Rose-Heart & Emerald Leaf Healing Staff
    8. zephyr_dash_wand     - Swept Sky-Wing & Violet Wind-Gem Wand
    9. prism_nova_staff     - Dual-Faceted Violet/Cyan Prism Astrolabe Staff
   10. brimstone_meteor_staff - Obsidian-Claw & Molten Meteor Staff
   11. twin_star_staff      - Twin-Star Harmony Staff (orbiting cyan & amber-gold stars in golden halo)

Usage:
    python tools/art/make_hero_weapons_art.py
"""
from __future__ import annotations

from pathlib import Path
from PIL import Image

OUT_DIR = Path("src/main/resources/assets/artilian/textures/item")

PALETTES: dict[str, dict[str, tuple[int, int, int, int]]] = {
    "sunsteel_sword": {
        ".": (0, 0, 0, 0),
        "0": (14, 7, 22, 255),      # Deep obsidian outline
        "1": (28, 14, 42, 255),     # Dark obsidian blade
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
    "starlight_sword": {
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
    "prismatic_sword": {
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
    "dragonscale_sword": {
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
    "twin_star_blade": {
        ".": (0, 0, 0, 0),
        "0": (14, 10, 32, 255),     # Void-indigo outline
        "1": (26, 18, 58, 255),     # Void-indigo fuller
        "2": (46, 32, 92, 255),     # Mid indigo
        "k": (58, 66, 86, 255),     # Slate-silver shadow
        "d": (132, 144, 174, 255),  # Platinum shadow
        "w": (204, 214, 236, 255),  # Platinum silver
        "W": (244, 248, 255, 255),  # Pure platinum gleam
        "g": (220, 156, 28, 255),   # Celestial gold
        "G": (252, 212, 74, 255),   # Bright gold
        "h": (255, 244, 172, 255),  # Gold specular
        "c": (56, 216, 240, 255),   # Starlight cyan
        "C": (186, 252, 255, 255),  # Twin-star core
    },
    "wand_shared": {
        ".": (0, 0, 0, 0),
        # Deep Slate & warm wood shafts
        "O": (26, 18, 40, 255),     # Deep Artilian Slate outline
        "S": (78, 66, 102, 255),    # Mid slate grip
        "s": (48, 38, 70, 255),     # Shaded slate grip
        "W": (148, 98, 48, 255),    # Warm staff wood
        "w": (96, 60, 26, 255),     # Shaded wood
        # Gold trim
        "k": (72, 28, 4, 255),      # Dark gold outline
        "g": (202, 108, 18, 255),   # Shaded gold
        "G": (250, 174, 38, 255),   # Sunsteel Gold
        "Y": (255, 242, 176, 255),  # Gold highlight
        # Starlight Teal & Sky Cyan (Spark Wand & Prism Nova)
        "D": (16, 92, 118, 255),    # Deep teal crystal outline
        "T": (48, 206, 188, 255),   # Starlight Teal
        "C": (74, 188, 255, 255),   # Mana Sky Cyan
        "H": (142, 248, 255, 255),  # Bright crystal highlight
        "B": (244, 255, 255, 255),  # Pure crystal sparkle core
        # Healing Emerald & Rose Heart Blossom (Heart-Bloom Staff)
        "b": (72, 20, 44, 255),     # Deep botanical plum outline
        "E": (76, 217, 100, 255),   # Healing Emerald
        "e": (26, 134, 58, 255),    # Shaded emerald leaf
        "P": (255, 118, 158, 255),  # Warm blossom pink
        "p": (208, 56, 104, 255),   # Deeper rose heart
        "L": (255, 224, 238, 255),  # Blossom highlight
        # Arcane Violet & Wind Wing (Zephyr Dash & Prism Nova)
        "K": (48, 28, 88, 255),     # Deep violet outline
        "V": (156, 92, 255, 255),   # Mana Violet
        "v": (102, 52, 194, 255),   # Deeper violet
        "U": (224, 198, 255, 255),  # Violet crystal core
        "F": (244, 248, 255, 255),  # Crisp feather white
        "f": (182, 172, 206, 255),  # Soft Lavender Grey feather shade
        # Brimstone Meteor Staff (Ember / Crimson / Gold)
        "R": (255, 76, 86, 255),    # Brimstone coral-red
        "r": (158, 28, 38, 255),    # Shaded crimson
        "M": (252, 122, 20, 255),   # Ember orange
        "m": (42, 18, 26, 255),     # Volcanic obsidian claw
        # Twin-Star Harmony Staff extras
        "A": (255, 196, 56, 255),   # Amber-gold star
        "a": (255, 118, 16, 255),   # Amber star rim
        "N": (204, 214, 236, 255),  # Platinum wing tip
    },
}

ICONS: dict[str, tuple[str, list[str]]] = {
    # -------------------------------------------------------------------------
    # 5 HERO SWORDS / BLADES
    # -------------------------------------------------------------------------
    "sunsteel_sword": (
        "sunsteel_sword",
        [
            "............0000",
            "...........032h0",
            "..........032gh0",
            ".........032g120",
            "........032g120.",
            ".......032g120..",
            "....kk032g120...",
            "...kGG22g120....",
            "...kGEeg120.....",
            "....kGe20k......",
            "...0rkGGk.......",
            "..0Rr0kGk.......",
            ".0Rr0..k........",
            "kGR0............",
            "kGk.............",
            "kk..............",
        ],
    ),
    "starlight_sword": (
        "starlight_sword",
        [
            "..............00",
            "............0Ch0",
            "...........0ChC0",
            "..........0Cc20.",
            ".........0Cc20..",
            "........0Ce20...",
            ".......0Cc20....",
            "......0Cc20.....",
            "...0d0Cc20......",
            "..0cCEc20d0.....",
            "...00c20c0......",
            "....0L000.......",
            "...0lL0.........",
            "..0lL0..........",
            ".0CE0...........",
            ".000............",
        ],
    ),
    "prismatic_sword": (
        "prismatic_sword",
        [
            "............0000",
            "...........03320",
            "..........032p0.",
            "..........032P0.",
            ".........032p10.",
            "........032e10..",
            ".......032E10...",
            "......032e10....",
            "...kk032p10.....",
            "..kGhG2p10......",
            "..kGkR2k........",
            "..k0rRGk........",
            "..k0RkGk........",
            "..kGGkk.........",
            ".0GGk...........",
            ".0kk............",
        ],
    ),
    "dragonscale_sword": (
        "dragonscale_sword",
        [
            ".............000",
            "...........00Gh0",
            "..........0GhG20",
            ".........0G2320.",
            "........00g3210.",
            ".......0Gh2120..",
            "......0G23210...",
            "...B00g32120....",
            "..bB0g2120......",
            "...bdEe20..B....",
            "...kdEGd00bB....",
            "..01kdkdbk0.....",
            ".0120..kk.......",
            "0bB0............",
            "0b0.............",
            "00..............",
        ],
    ),
    "twin_star_blade": (
        "twin_star_blade",
        [
            ".............000",
            "............0Wh0",
            "...........0W1W0",
            "..........0W1c0.",
            ".........0W1c0..",
            "........0W120...",
            "..0W...0W120....",
            "..0wW.0W120.....",
            "...0g0WCC0......",
            "...0gGWC0.0g0...",
            "....01GG0gG0....",
            "...0100GG0......",
            "..0d10..0.......",
            ".0gG0...........",
            "0Gh0............",
            "000.............",
        ],
    ),

    # -------------------------------------------------------------------------
    # 6 WANDS & STAVES
    # -------------------------------------------------------------------------
    "spark_wand": (
        "wand_shared",
        [
            ".............DD.",
            "............DHBD",
            "...........DHBTD",
            ".....H....DHBCD.",
            ".........DTCCTD.",
            "........DTCCCTD.",
            ".......DTCTCTD..",
            ".......kGTTTD.H.",
            "......OSGgDD....",
            ".....OSsO.......",
            "....OSsO........",
            "...OSsO.........",
            "..OSsO..........",
            ".OSsO...........",
            "kGgO............",
            ".kk.............",
        ],
    ),
    "heart_bloom_staff": (
        "wand_shared",
        [
            "................",
            "......bbb.bbb...",
            ".....bPLPbPPPb..",
            ".....bPLPPPPpb..",
            ".....bPPPPPPpb..",
            "......bPPPPpb...",
            "....ee.bPPpb.ee.",
            "...eEEe.bpb.eEEe",
            "....eEEeObeEEe..",
            ".....eeOWGOee...",
            ".....OWwO.......",
            "....OWwO........",
            "...OWwO.........",
            "..OWwO..........",
            ".kGgO...........",
            "..kk............",
        ],
    ),
    "zephyr_dash_wand": (
        "wand_shared",
        [
            ".........KKKKKK.",
            ".......KKFFFFFFK",
            "......KFFFFFFfK.",
            ".....KFFFFFFfK..",
            ".....KFFUVFfKK..",
            "......KFUVVfK...",
            "......KfVVvKK...",
            ".......KKvKO....",
            ".......kGGYk....",
            "......OSGgO.....",
            ".....OSsO.......",
            "....OSsO........",
            "...OSsO.........",
            "..OSsO..........",
            ".kGgO...........",
            "..kk............",
        ],
    ),
    "prism_nova_staff": (
        "wand_shared",
        [
            ".........KKDD...",
            "........KVBBHD..",
            ".......KUVBBCHD.",
            "......KVUUBBCHTD",
            ".....KVVUvBCCTD.",
            "......KvvkGCTD..",
            "......kGGYGGD...",
            ".....OSGgOkDD...",
            "....OSsO..OO....",
            "...OSsO.........",
            "..OSsO..........",
            ".kGgO...........",
            ".OSSO...........",
            "kGgO............",
            ".kk.............",
            "................",
        ],
    ),
    "brimstone_meteor_staff": (
        "wand_shared",
        [
            "..........mmrr..",
            "........mmMRRm..",
            ".......mMMYYRRm.",
            "......rMGYGYMRr.",
            ".....mMMGGGGMRm.",
            ".....mRMGYYMRm..",
            "......mmGGYmrm..",
            ".....OSGgOmm....",
            "....OSsO........",
            "...OSsO.........",
            "..OSsO..........",
            ".OWwO...........",
            ".OWwO...........",
            "kGgO............",
            ".kk.............",
            "................",
        ],
    ),
    "twin_star_staff": (
        "wand_shared",
        [
            "..........kDDk..",
            ".......N.kDBBHD.",
            "......kNkDTTBTD.",
            ".....kAAk.kDDGk.",
            "....kAYYak.kGGk.",
            ".....kAaakGGYk..",
            "......kkkGYGkN..",
            ".....OSGgOkGk...",
            "....OSsO........",
            "...OSsO.........",
            "..OSsO..........",
            ".kGgO...........",
            ".OSSO...........",
            "kGgO............",
            ".kk.............",
            "................",
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


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    count = 0
    for item_name, (pal_name, grid) in ICONS.items():
        pal = PALETTES[pal_name]
        out_path = OUT_DIR / f"{item_name}.png"
        render_icon(grid, pal).save(out_path)
        count += 1
    print(f"Generated {count} hero weapon & staff icons in {OUT_DIR}")


if __name__ == "__main__":
    main()
