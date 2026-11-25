"""Generates a 256x256 (2x UV scale) Cataclysm/Immersive-Armors-style double-layer
alpha-cutout filigree texture and emissive glowmask for Sunsteel Armor.

Architecture:
- Inner shells (Row 0): solid deep obsidian-plum & dark crimson forged plate.
- Outer filigree cages (Row 1 & Row 2): 1-bit alpha-cutout gold filigree lattices floating
  over the inner shells for real 3D parallax depth and zero boxy borders.
- Zero-thickness cutout planes (Row 3): hand-crafted pixel-art silhouettes for the swept
  solar crown, side crown horns, jagged pauldron crests, back wing-fins, and boot wings.

Usage:
    python tools/art/make_sunsteel_armor_art.py
"""
from __future__ import annotations

from pathlib import Path
from PIL import Image

ASSETS = Path("src/main/resources/assets/artilian")
TEX_OUT = ASSETS / "textures/item/armor/sunsteel_armor.png"
GLOW_OUT = ASSETS / "textures/item/armor/sunsteel_armor_glowmask.png"
MODEL_TEX_OUT = Path("tools/art/models/sunsteel_armor.png")

SCALE = 2  # 128 * 2 = 256x256 (2x2 pixels per model unit -> 16x16 head, 16x24 chest)
SIZE = 128 * SCALE

# Hue-shifted forged-metal pixel-art palette (legend for sprite maps)
# '.' = transparent (Alpha = 0)
# '0' = deep obsidian shadow     (#120A1A)
# '1' = dark obsidian plate      (#1F122C)
# '2' = mid obsidian highlight   (#321E45)
# '3' = steel edge glint         (#4C3366)
# 'r' = dark ruby-crimson        (#4A0D18)
# 'R' = bright ruby-crimson      (#8C1D2B)
# 'k' = fiery umber outline/shad (#4E1A04)
# 'd' = dark antique bronze-gold (#8F3E08)
# 'g' = rich hammered gold       (#D9820B)
# 'G' = bright sunlit gold       (#F7BD24)
# 'h' = warm lemon-gold glint    (#FFF199)
# 'e' = molten orange glow       (#FF7A0A)  [also painted to glowmask]
# 'E' = bright solar core glow   (#FFE666)  [also painted to glowmask]

PAL = {
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
}

GLOW_KEYS = {"e", "E"}


def _hash(x: int, y: int) -> int:
    n = (x * 374761393 + y * 668265263) ^ 0x5BF03635
    n = (n ^ (n >> 13)) * 1274126177
    return (n ^ (n >> 16)) & 0xFF


def box_faces(u: int, v: int, w: int, h: int, d: int) -> dict[str, tuple[int, int, int, int]]:
    return {
        "up": ((u + d) * SCALE, v * SCALE, w * SCALE, d * SCALE),
        "down": ((u + d + w) * SCALE, v * SCALE, w * SCALE, d * SCALE),
        "west": (u * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE),
        "north": ((u + d) * SCALE, (v + d) * SCALE, w * SCALE, h * SCALE),
        "east": ((u + d + w) * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE),
        "south": ((u + d + w + d) * SCALE, (v + d) * SCALE, w * SCALE, h * SCALE),
    }


def blit_sprite(
    img: Image.Image,
    glow: Image.Image,
    rect: tuple[int, int, int, int],
    rows: list[str],
    flip_x: bool = False,
):
    rx, ry, rw, rh = rect
    if rw <= 0 or rh <= 0:
        return
    px = img.load()
    gpx = glow.load()
    sh = len(rows)
    sw = len(rows[0])
    for dy in range(rh):
        sy = min(sh - 1, int(dy * sh / rh))
        row = rows[sy]
        for dx in range(rw):
            sx = min(sw - 1, int(dx * sw / rw))
            if flip_x:
                sx = sw - 1 - sx
            ch = row[sx]
            rgba = PAL[ch]
            px[rx + dx, ry + dy] = rgba
            if ch in GLOW_KEYS:
                gpx[rx + dx, ry + dy] = rgba


def fill_dark_forged_plate(
    img: Image.Image,
    rect: tuple[int, int, int, int],
    *,
    crimson_center: bool = False,
    ribbed: bool = False,
    bias: int = 0,
):
    """Paints a dark, hammered obsidian-plum steel surface (no borders!) so floating gold filigree pops."""
    rx, ry, rw, rh = rect
    if rw <= 0 or rh <= 0:
        return
    px = img.load()
    tones = [PAL["0"], PAL["1"], PAL["2"], PAL["3"]]
    for dy in range(rh):
        for dx in range(rw):
            h = _hash(rx + dx, ry + dy)
            v_frac = dy / max(1, rh - 1)
            idx = 1 if v_frac < 0.65 else 0
            if v_frac < 0.25 and h > 140:
                idx = 2
            elif h > 220:
                idx = min(3, idx + 1)
            elif h < 55:
                idx = max(0, idx - 1)
            if ribbed and dy % 4 == 0:
                idx = 0
            elif ribbed and dy % 4 == 1:
                idx = min(2, idx + 1)
            idx = max(0, min(3, idx + bias))
            rgb = tones[idx]
            if crimson_center and abs(dx - (rw - 1) / 2.0) < rw * 0.26 and 2 <= dy <= rh - 3:
                rgb = PAL["R"] if (dy % 4 == 1 or h > 180) else PAL["r"]
            px[rx + dx, ry + dy] = rgb


def fill_inner_box(img: Image.Image, u: int, v: int, w: int, h: int, d: int, *, crimson_center: bool = False, ribbed: bool = False):
    biases = {"up": 1, "north": 0, "east": 0, "west": 0, "south": 0, "down": -1}
    for fname, rect in box_faces(u, v, w, h, d).items():
        fill_dark_forged_plate(
            img,
            rect,
            crimson_center=crimson_center and fname in ("north", "south"),
            ribbed=ribbed and fname in ("north", "south", "east", "west"),
            bias=biases[fname],
        )


# =====================================================================
# HAND-CRAFTED PIXEL-ART SPRITES (1-BIT ALPHA CUTOUTS FOR 3D FILIGREE!)
# =====================================================================

# 1. Head Inner Front (16x16): deep shadowed helmet interior + blazing solar eyes
HEAD_INNER_FRONT = [
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
]

# 2. Head Outer Filigree Front (16x16): sculpted golden V-brow, nasal guard, cheek fangs & jaw trim
HEAD_FILIGREE_FRONT = [
    "gGhGGGg..gGGGhGg",
    "dGg..gGhhGg..gGd",
    "kG....gGGg....Gk",
    "gG..k.gGGg.k..Gg",
    "GG.kGg.GG.gGk.GG",
    "gG.dG..gg..Gd.Gg",
    "dG.....dd.....Gd",
    "kG............Gk",
    "gG............Gg",
    "GGk..........kGG",
    "gGGk........kGGg",
    "dGGgk..gg..kgGGd",
    "kdGGgk.GG.kgGGdk",
    ".kdGGggGGggGGdk.",
    "..kddGGGGGGddk..",
    "....kkkddkkk....",
]

# Head Outer Filigree Side (16x16): sweeping gold crown band & cheekplate trim
HEAD_FILIGREE_SIDE = [
    "gGhGGGGGGGGGGhGg",
    "dGggddGGGGddggGd",
    "kG............Gk",
    "gG...kggggk...Gg",
    "GG..kGhhGGgk..GG",
    "gG.kGg..dGGgk.Gg",
    "dG.dG....gGGd.Gd",
    "kG..k....kGG..Gk",
    "gG........GG..Gg",
    "GGk......kGG..GG",
    "gGGk....kGGg..Gg",
    "dGGggkkggGGd..Gd",
    "kddGGGGGGddk..dk",
    "..kkkddkkk......",
    "................",
    "................",
]

# Head Outer Filigree Back (16x16): royal gold diamond & nape filigree
HEAD_FILIGREE_BACK = [
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
]

# 3. Front Ornate Solar Crown Plane (24x18): razor-sharp sweeping crown with central diamond crest & curved spikes!
CROWN_FRONT = [
    "...........h............",
    "...........G............",
    "..h.......gGg.......h...",
    "..G.......GGG.......G...",
    "..Gg.....gGhgG.....gG...",
    ".kGGg....GGhGG....gGGk..",
    "h.kGGg..gGGEGGg..gGGk.h.",
    "G..kGGg.GGEEEGG.gGGk..G.",
    "Gg..kGGgGGEhEGGgGGk..gG.",
    "GGg..dGGGGEEEGGGGd..gGG.",
    "kGGg.kGGGGhGhGGGGk.gGGk.",
    ".kGGgkGGGgGGGgGGGkgGGk..",
    "..kGGGGGG.gGg.GGGGGGk...",
    "..dGGhGGg.kGk.gGGhGGd...",
    ".kGGg.dGGg.k.gGGd.gGGk..",
    ".dGd...kGGgggGGk...dGd..",
    ".kk.....kddGddk.....kk..",
    ".........kkkkk..........",
]

# Crown Back Plane (20x14): tapered rear crown crest
CROWN_BACK = [
    ".........h..........",
    "..h.....gGg.....h...",
    "..G.....GGG.....G...",
    "..Gg...gGhgG...gG...",
    ".kGGg..GGGGG..gGGk..",
    "h.kGGggGGhGGggGGk.h.",
    "G..kGGGGGGGGGGGk..G.",
    "Gg..dGGgGGGgGGd..gG.",
    "kGg.kGG.gGg.GGk.gGk.",
    ".kGGgGG.kGk.GGgGGk..",
    "..kGGGGggGggGGGGk...",
    "..dGGhGGGGGGGhGGd...",
    "..kddGGGGGGGGGddk...",
    "...kkkkkkkkkkkkk....",
]

# Crown Side Horn/Wing Plane (20x18): swept curved golden side crown horns!
CROWN_SIDE = [
    "..............h.....",
    ".............gG.....",
    "............gGG.....",
    ".......h...gGhg.....",
    "......gG..gGGGk.....",
    ".....gGG.gGGhGk.....",
    "....gGhg.GGGGk......",
    "..h.GGGGgGGGd.......",
    ".gGgGGhGGGGk...h....",
    ".GGGGGGGGdk...gG....",
    "gGGhGGGGk....gGG....",
    "GGGGGGdk....gGhg....",
    "kGGGGGGgggggGGGk....",
    ".kGGhGGGGGGGGdk.....",
    "..kGGGGGGGGdk.......",
    "..dGGddGGddk........",
    "..kkk..kkkk.........",
    "....................",
]

# 4. Body Outer Filigree Front (16x24): ornate golden diamond harness, ribs & solar belt
BODY_FILIGREE_FRONT = [
    "gGhGgk....kgGhGg",
    "dGGgGk....kGgGGd",
    "kG..gGk..kGg..Gk",
    "gG...gGkkGg...Gg",
    "GG....gGGg....GG",
    "gG...gGhhGg...Gg",
    "dG..gGg..gGg..Gd",
    "kG.gGg.EE.gGg.Gk",
    "gG.Gg.EEEE.gG.Gg",
    "GG.gG..EE..Gg.GG",
    "gG..gG....Gg..Gg",
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
]

# Body Outer Filigree Back (16x24): royal gold spine & chevron cage
BODY_FILIGREE_BACK = [
    "gGhGGGGGGGGGGhGg",
    "dGGgddGGGGddgGGd",
    "kG.....GG.....Gk",
    "gG....gGGg....Gg",
    "GG...gGhhGg...GG",
    "gG..gGgGGgGg..Gg",
    "dG.gGg.GG.gGg.Gd",
    "kG.dG..EE..Gd.Gk",
    "gG....gEEg....Gg",
    "GG...gGEEGg...GG",
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
]

# Chest Breastplate Raised Floating Overlay (16x12): bold 3D floating diamond crest + solar core!
CHEST_OVERLAY_FRONT = [
    "gGhGgk....kgGhGg",
    ".dGGgGk..kGgGGd.",
    "..kGGgGkkGgGGk..",
    "...kGGgGGgGGk...",
    "....kGGhhGGk....",
    "...gGGgEEgGGg...",
    "..gGhgEEEEghGg..",
    "...gGGgEEgGGg...",
    "....kGGhhGGk....",
    ".....kGGGGk.....",
    "......kGGk......",
    ".......kk.......",
]

# Waist Skirt / Hanging Tassets Front (16x12): sculpted V-pointed center tabard + open-work side hip plates!
WAIST_SKIRT_FRONT = [
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
]

# Waist Skirt Side (8x12): pointed hip tasset blade
WAIST_SKIRT_SIDE = [
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
]

# 5. Pauldron Filigree (12x10): scalloped royal shoulder trim with diamond cutout
PAULDRON_FILIGREE = [
    "gGhGGGGGGhGg",
    "dGggdGGdgGGd",
    "GG..gGGg..GG",
    "gG.gGhhGg.Gg",
    "dG.Gg..gG.Gd",
    "kG.gG..Gg.Gk",
    "gG..gGGg..Gg",
    "dGgg.GG.ggGd",
    "kddGGhhGGddk",
    "..kkkddkkk..",
]

# Pauldron Crest Plane (12x12): razor-sharp swept shoulder spike!
PAULDRON_CREST = [
    "..........h.",
    ".........gG.",
    "........gGG.",
    ".......gGhg.",
    "....h.gGGGk.",
    "...gGgGGhGk.",
    "..gGGGGGGk..",
    ".gGGhGGGd...",
    "gGGGGGGk....",
    "dGGhGGd.....",
    "kddGGdk.....",
    "..kkkk......",
]

# Back Wing-Fin Plane (12x20): dramatic swept solar wing-fin on back!
BACK_CREST = [
    "..........h.",
    ".........gG.",
    "........gGG.",
    ".......gGhg.",
    "......gGGGk.",
    ".....gGGhGk.",
    "....gGGGGk..",
    "...gGGhGk...",
    "..gGGGGGk...",
    ".gGGhGGd....",
    ".GGGGGGk....",
    "gGGhGGd.....",
    "GGGGGGk.....",
    "dGGhGGk.....",
    "kGGGGdk.....",
    ".kGGdk......",
    "..kdk.......",
    "...k........",
    "............",
    "............",
]

# 6. Arm Outer Filigree (8x24): crisp open-work gold trim over dark obsidian sleeves
ARM_FILIGREE = [
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
]

# 7. Leg Outer Filigree (8x24): open-work diamond thigh cage + bold knee diamond over dark obsidian!
LEG_FILIGREE = [
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
]

# 8. Boot Outer Filigree (8x24): crisp gold shin diamond + sabaton toe trim with dark windows!
BOOT_FILIGREE = [
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
]

# Boot Ankle Wing Plane (12x12): razor-sharp swept golden ankle wing!
BOOT_WING = [
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
]


def paint_plane_z(img: Image.Image, glow: Image.Image, u: int, v: int, w: int, h: int, sprite: list[str]):
    """Paints both north and south faces of a 0-thickness Z-plane (size [w, h, 0])."""
    north = (u * SCALE, v * SCALE, w * SCALE, h * SCALE)
    south = ((u + w) * SCALE, v * SCALE, w * SCALE, h * SCALE)
    blit_sprite(img, glow, north, sprite, flip_x=False)
    blit_sprite(img, glow, south, sprite, flip_x=True)


def paint_plane_x(img: Image.Image, glow: Image.Image, u: int, v: int, h: int, d: int, sprite: list[str]):
    """Paints both west and east faces of a 0-thickness X-plane (size [0, h, d])."""
    west = (u * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE)
    east = ((u + d) * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE)
    blit_sprite(img, glow, west, sprite, flip_x=False)
    blit_sprite(img, glow, east, sprite, flip_x=True)


def build_textures() -> tuple[Image.Image, Image.Image]:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # --- ROW 0: Inner Solid Dark Obsidian-Plum Forged Armor Shells ---
    fill_inner_box(img, 0, 0, 8, 8, 8)                                     # head_inner
    blit_sprite(img, glow, box_faces(0, 0, 8, 8, 8)["north"], HEAD_INNER_FRONT)
    fill_inner_box(img, 34, 0, 8, 12, 4, crimson_center=True, ribbed=True) # body_inner
    fill_inner_box(img, 60, 0, 4, 12, 4, ribbed=True)                      # arm_inner
    fill_inner_box(img, 78, 0, 4, 12, 4, crimson_center=True, ribbed=True) # leg_inner
    fill_inner_box(img, 96, 0, 4, 12, 4, ribbed=True)                      # boot_inner

    # --- ROW 1: Outer Floating Alpha-Cutout Gold Filigree Cages ---
    hf = box_faces(0, 18, 8, 8, 8)
    blit_sprite(img, glow, hf["north"], HEAD_FILIGREE_FRONT)
    blit_sprite(img, glow, hf["east"], HEAD_FILIGREE_SIDE)
    blit_sprite(img, glow, hf["west"], HEAD_FILIGREE_SIDE, flip_x=True)
    blit_sprite(img, glow, hf["south"], HEAD_FILIGREE_BACK)
    # Top of head filigree: crisp gold crown rim around dark obsidian cap
    blit_sprite(img, glow, hf["up"], HEAD_FILIGREE_BACK)

    bf = box_faces(34, 18, 8, 12, 4)
    blit_sprite(img, glow, bf["north"], BODY_FILIGREE_FRONT)
    blit_sprite(img, glow, bf["south"], BODY_FILIGREE_BACK)
    blit_sprite(img, glow, bf["east"], ARM_FILIGREE)
    blit_sprite(img, glow, bf["west"], ARM_FILIGREE, flip_x=True)

    af = box_faces(60, 18, 4, 12, 4)
    for side in ("north", "south", "east", "west"):
        blit_sprite(img, glow, af[side], ARM_FILIGREE)

    lf = box_faces(78, 18, 4, 12, 4)
    for side in ("north", "south", "east", "west"):
        blit_sprite(img, glow, lf[side], LEG_FILIGREE)

    btf = box_faces(96, 18, 4, 12, 4)
    for side in ("north", "south", "east", "west"):
        blit_sprite(img, glow, btf[side], BOOT_FILIGREE)

    # --- ROW 2: Layered Pauldrons, Raised Chest Crest & Scalloped Waist Skirt ---
    fill_inner_box(img, 0, 36, 6, 5, 6, crimson_center=True)               # pauldron_shell
    pf = box_faces(26, 36, 6, 5, 6)
    for side in ("north", "south", "east", "west", "up"):
        blit_sprite(img, glow, pf[side], PAULDRON_FILIGREE)

    cf = box_faces(52, 36, 8, 6, 4)
    blit_sprite(img, glow, cf["north"], CHEST_OVERLAY_FRONT)

    wf = box_faces(78, 36, 8, 6, 4)
    blit_sprite(img, glow, wf["north"], WAIST_SKIRT_FRONT)
    blit_sprite(img, glow, wf["south"], WAIST_SKIRT_FRONT)
    blit_sprite(img, glow, wf["east"], WAIST_SKIRT_SIDE)
    blit_sprite(img, glow, wf["west"], WAIST_SKIRT_SIDE, flip_x=True)

    # --- ROW 3: Zero-Thickness Razor-Sharp Alpha-Cutout Silhouette Planes ---
    paint_plane_z(img, glow, 0, 50, 12, 9, CROWN_FRONT)                    # crown_front
    paint_plane_z(img, glow, 26, 50, 10, 7, CROWN_BACK)                    # crown_back
    paint_plane_x(img, glow, 48, 50, 9, 10, CROWN_SIDE)                    # crown_side_r/l
    paint_plane_z(img, glow, 70, 50, 6, 6, PAULDRON_CREST)                 # pauldron_crest_r/l
    paint_plane_z(img, glow, 84, 50, 6, 10, BACK_CREST)                    # back_crest_r/l
    paint_plane_x(img, glow, 98, 50, 6, 6, BOOT_WING)                      # boot_wing_r/l

    return img, glow


def main():
    img, glow = build_textures()
    TEX_OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(TEX_OUT)
    glow.save(GLOW_OUT)
    img.save(MODEL_TEX_OUT)
    print(f"Wrote {TEX_OUT} ({img.size}), {GLOW_OUT} ({glow.size}), and {MODEL_TEX_OUT}")


if __name__ == "__main__":
    main()
