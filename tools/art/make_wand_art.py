"""Draws 16x16 item textures for Spark Wand, Heart-Bloom Staff, and Zephyr Dash Wand.
Run from the project root:

    python tools/art/make_wand_art.py

Uses the DESIGN.md palette (#38D9C0 Starlight Teal, #47B8FF Mana Cyan, #9C5CFF Mana Violet,
#4CD964 Healing Emerald, #FF9F38 Sunsteel Gold, #2B2338 Deep Artilian Slate).
"""
from pathlib import Path
from PIL import Image

TEX_DIR = Path("src/main/resources/assets/artilian/textures/item")

PALETTE = {
    ".": None,
    # Deep Slate & warm wood shafts
    "O": (32, 24, 46, 255),      # Deep Artilian Slate outline
    "S": (74, 64, 96, 255),      # Mid slate grip
    "s": (52, 42, 72, 255),      # Shaded slate grip
    "W": (146, 98, 48, 255),     # Warm staff wood
    "w": (98, 62, 28, 255),      # Shaded wood
    # Sunsteel Gold trim
    "G": (255, 159, 56, 255),    # #FF9F38 Sunsteel Gold
    "g": (196, 98, 22, 255),     # Shaded gold
    "Y": (255, 246, 214, 255),   # #FFF6D6 Gold highlight
    # Starlight Teal & Sky Cyan (Spark Wand)
    "D": (22, 108, 128, 255),    # Deep teal crystal outline
    "T": (56, 217, 192, 255),    # #38D9C0 Starlight Teal
    "C": (71, 184, 255, 255),    # #47B8FF Mana Sky Cyan
    "H": (125, 249, 255, 255),   # #7DF9FF Bright crystal highlight
    "B": (240, 255, 255, 255),   # Pure crystal sparkle core
    # Healing Emerald & Rose Heart Blossom (Heart-Bloom Staff)
    "b": (74, 24, 46, 255),      # Deep botanical plum outline
    "E": (76, 217, 100, 255),    # #4CD964 Healing Emerald
    "e": (28, 136, 60, 255),     # Shaded emerald leaf
    "P": (255, 116, 156, 255),   # Warm blossom pink
    "p": (212, 62, 108, 255),    # Deeper rose heart
    "L": (255, 222, 236, 255),   # Blossom highlight
    # Arcane Violet & Wind Wing (Zephyr Dash Wand)
    "K": (54, 34, 96, 255),      # Deep violet wing/gem outline
    "V": (156, 92, 255, 255),    # #9C5CFF Mana Violet
    "v": (106, 56, 198, 255),    # Deeper violet
    "U": (224, 198, 255, 255),   # Violet crystal core
    "F": (244, 248, 255, 255),   # Crisp feather white
    "f": (180, 169, 200, 255),   # #B4A9C8 Soft Lavender Grey feather shade
    # Brimstone Meteor Staff (Ember / Crimson / Gold)
    "R": (255, 77, 90, 255),     # #FF4D5A Brimstone coral-red
    "r": (174, 34, 42, 255),     # Shaded crimson
    "M": (249, 115, 22, 255),    # #F97316 Ember orange
}


def paint(rows, filename):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        assert len(row) == 16, f"{filename} row {y} has length {len(row)}"
        for x, ch in enumerate(row):
            color = PALETTE[ch]
            if color is not None:
                img.putpixel((x, y), color)
    out = TEX_DIR / filename
    out.parent.mkdir(parents=True, exist_ok=True)
    img.save(out)
    print(f"Wrote {out}")


# Each head fills about half the icon, so the three read as a crystal, a heart, and a wing at
# hotbar size. The earlier heads were six pixels wide and only the tip colour told them apart.
SPARK_WAND = [
    "..........DD....",
    ".........DHHD...",
    "........DHBBHD..",
    ".......DHBBCCHD.",
    "......DTCBCCCCTD",
    "......DTCCCCCCTD",
    ".......DTCCCCTD.",
    "........DTCCTD..",
    ".......OGDTTD...",
    "......OSGgDD....",
    ".....OSsO.......",
    "....OSsO........",
    "...OSsO.........",
    "..OSsO..........",
    ".OGgO...........",
    "..OO............",
]

HEART_BLOOM_STAFF = [
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
    ".OGgO...........",
    "..OO............",
]

ZEPHYR_DASH_WAND = [
    ".........KKKKKK.",
    ".......KKFFFFFFK",
    "......KFFFFFFfK.",
    ".....KFFFFFFfK..",
    ".....KFFUVFfKK..",
    "......KFUVVfK...",
    "......KfVVvKK...",
    ".......KKvKO....",
    ".......OGGYO....",
    "......OSGgO.....",
    ".....OSsO.......",
    "....OSsO........",
    "...OSsO.........",
    "..OSsO..........",
    ".OGgO...........",
    "..OO............",
]

PRISM_NOVA_STAFF = [
    ".........KKDD...",
    "........KVBBHD..",
    ".......KVVBBCHD.",
    "......KVVUUCCHD.",
    ".....KVVUVUCCCTD",
    "......KVVvCCCTD.",
    "......OGGYCTTD..",
    ".....OSGgOGDD...",
    "....OSsO..OO....",
    "...OSsO.........",
    "..OSsO..........",
    ".OGgO...........",
    ".OSSO...........",
    "OGgO............",
    ".OO.............",
    "................",
]

BRIMSTONE_METEOR_STAFF = [
    "..........rrOO..",
    "........rrMRRO..",
    ".......rMMYYRRO.",
    "......rMGYGYMRr.",
    ".....rMMGGGGMRr.",
    ".....rRMGYYMRr..",
    "......OOGGYOr...",
    ".....OSGgOrr....",
    "....OSsO........",
    "...OSsO.........",
    "..OSsO..........",
    ".OWwO...........",
    ".OWwO...........",
    "OGgO............",
    ".OO.............",
    "................",
]

TWIN_STAR_STAFF = [
    ".........KKGG...",
    "........KVVYYG..",
    ".......KVUHBBHG.",
    "......KVUHBTTBG.",
    ".....KVUHMTYYTG.",
    "......KVUMMEETG.",
    "......OGGYGEEG..",
    ".....OSGgOOGG...",
    "....OSsO..OO....",
    "...OSsO.........",
    "..OSsO..........",
    ".OGgO...........",
    ".OSSO...........",
    "OGgO............",
    ".OO.............",
    "................",
]


def main():
    paint(SPARK_WAND, "spark_wand.png")
    paint(HEART_BLOOM_STAFF, "heart_bloom_staff.png")
    paint(ZEPHYR_DASH_WAND, "zephyr_dash_wand.png")
    paint(PRISM_NOVA_STAFF, "prism_nova_staff.png")
    paint(BRIMSTONE_METEOR_STAFF, "brimstone_meteor_staff.png")
    paint(TWIN_STAR_STAFF, "twin_star_staff.png")


if __name__ == "__main__":
    main()
