"""Builds 4 genuinely distinct, Sunsteel-v3-grade bespoke armor sets (256x256, 2x UV density):
- Starlight Crystal: Ornate Cyan-Hooded Arcane Ranger (alpha-cutout hood cowl, sash, pouches, crystal fins)
- Prismatic Brimstone: Ornate Cursed Pirate-Admiral (0-thickness tricorne & plume, eyepatch+magma eye, gold-braided longcoat, prism core)
- Dragonscale: Ornate Emerald Dragon-Hunter (carved drake-skull visor, 0-thickness curved horns, dorsal spine fins, forearm claws)
- Twin-Star: Ornate Winged Halo Seraph (platinum+gold filigree cage, 20x20 radiant halo, 20x24 spread celestial wings)

Zero 1px box borders, zero boxy add-ons, zero overlapping UVs.

Usage:
    python tools/art/make_bespoke_armors.py
"""
from __future__ import annotations

import json
from pathlib import Path
from PIL import Image

ASSETS = Path("src/main/resources/assets/artilian")
GEO_DIR = ASSETS / "geo/item/armor"
TEX_DIR = ASSETS / "textures/item/armor"

SCALE = 2
SIZE = 128 * SCALE


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


class ArmorBuilder:
    """Packs non-overlapping UVs on a 128x128 grid (256x256 PNG) and paints sprites/shells."""

    def __init__(self, set_name: str):
        self.set_name = set_name
        self.img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
        self.glow = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
        self.u = 0
        self.v = 0
        self.row_h = 0
        self.bones: dict[str, list[dict]] = {
            "armorHead": [],
            "armorBody": [],
            "armorRightArm": [],
            "armorLeftArm": [],
            "armorRightLeg": [],
            "armorRightBoot": [],
            "armorLeftLeg": [],
            "armorLeftBoot": [],
        }

    def alloc_uv(self, w: int, h: int, d: int) -> tuple[int, int]:
        if w == 0:
            rw, rh = 2 * d, h + d
        elif d == 0:
            rw, rh = 2 * w, h
        else:
            rw, rh = 2 * (w + d), h + d
        if self.u + rw > 128:
            self.u = 0
            self.v += self.row_h + 1
            self.row_h = 0
        if self.v + rh > 128:
            raise ValueError(f"{self.set_name}: UV sheet overflow at ({self.u}, {self.v}) for {rw}x{rh}")
        uv = (self.u, self.v)
        self.u += rw + 1
        self.row_h = max(self.row_h, rh)
        return uv

    def add_cube(
        self,
        bone: str,
        origin: list[float],
        size: list[int],
        *,
        pivot: list[float] | None = None,
        rotation: list[float] | None = None,
        inflate: float = 0.0,
        mirror: bool = False,
        uv: tuple[int, int] | None = None,
    ) -> tuple[int, int]:
        w, h, d = size
        if uv is None:
            uv = self.alloc_uv(w, h, d)
        cube: dict = {"origin": origin, "size": size, "uv": [uv[0], uv[1]]}
        if pivot is not None:
            cube["pivot"] = pivot
        if rotation is not None:
            cube["rotation"] = rotation
        if inflate:
            cube["inflate"] = inflate
        if mirror:
            cube["mirror"] = True
        self.bones[bone].append(cube)
        return uv

    def paint_inner_shell(
        self,
        uv: tuple[int, int],
        w: int,
        h: int,
        d: int,
        tones: list[tuple[int, int, int, int]],
        *,
        pattern: str = "forged",
        accent: tuple[tuple[int, int, int, int], tuple[int, int, int, int]] | None = None,
    ):
        """Paints a dark, richly shaded inner armor shell (zero box borders)."""
        px = self.img.load()
        biases = {"up": 1, "north": 0, "east": 0, "west": 0, "south": 0, "down": -1}
        for fname, rect in box_faces(uv[0], uv[1], w, h, d).items():
            rx, ry, rw, rh = rect
            if rw <= 0 or rh <= 0:
                continue
            b = biases[fname]
            for dy in range(rh):
                for dx in range(rw):
                    hv = _hash(rx + dx, ry + dy)
                    vf = dy / max(1, rh - 1)
                    idx = 1 if vf < 0.65 else 0
                    if vf < 0.25 and hv > 140:
                        idx = 2
                    elif hv > 220:
                        idx = min(3, idx + 1)
                    elif hv < 55:
                        idx = max(0, idx - 1)
                    if pattern == "scales":
                        rs = dy // 4
                        lx = (dx + (2 if rs % 2 else 0)) % 4
                        ly = dy % 4
                        if ly == 3 or (ly == 2 and (lx == 0 or lx == 3)):
                            idx = 0
                        elif ly == 0 and (lx == 1 or lx == 2):
                            idx = min(3, idx + 1)
                    elif pattern == "ribbed":
                        if dy % 4 == 0:
                            idx = 0
                        elif dy % 4 == 1:
                            idx = min(3, idx + 1)
                    rgb = tones[max(0, min(3, idx + b))]
                    if accent and fname in ("north", "south") and abs(dx - (rw - 1) / 2.0) < rw * 0.26 and 2 <= dy <= rh - 3:
                        rgb = accent[1] if (dy % 4 == 1 or hv > 180) else accent[0]
                    px[rx + dx, ry + dy] = rgb

    def blit_face(
        self,
        rect: tuple[int, int, int, int],
        rows: list[str],
        pal: dict[str, tuple[int, int, int, int]],
        glow_keys: set[str],
        flip_x: bool = False,
    ):
        rx, ry, rw, rh = rect
        if rw <= 0 or rh <= 0:
            return
        px = self.img.load()
        gpx = self.glow.load()
        sh, sw = len(rows), len(rows[0])
        for dy in range(rh):
            sy = min(sh - 1, int(dy * sh / rh))
            row = rows[sy]
            for dx in range(rw):
                sx = min(sw - 1, int(dx * sw / rw))
                if flip_x:
                    sx = sw - 1 - sx
                ch = row[sx]
                rgba = pal[ch]
                px[rx + dx, ry + dy] = rgba
                if ch in glow_keys:
                    gpx[rx + dx, ry + dy] = rgba

    def paint_plane_z(self, uv: tuple[int, int], w: int, h: int, rows: list[str], pal, glow_keys):
        u, v = uv
        self.blit_face((u * SCALE, v * SCALE, w * SCALE, h * SCALE), rows, pal, glow_keys, flip_x=False)
        self.blit_face(((u + w) * SCALE, v * SCALE, w * SCALE, h * SCALE), rows, pal, glow_keys, flip_x=True)

    def paint_plane_x(self, uv: tuple[int, int], h: int, d: int, rows: list[str], pal, glow_keys):
        u, v = uv
        self.blit_face((u * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE), rows, pal, glow_keys, flip_x=False)
        self.blit_face(((u + d) * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE), rows, pal, glow_keys, flip_x=True)

    def save(self):
        geo = {
            "format_version": "1.12.0",
            "minecraft:geometry": [
                {
                    "description": {
                        "identifier": f"geometry.{self.set_name}_armor",
                        "texture_width": 128,
                        "texture_height": 128,
                        "visible_bounds_width": 4,
                        "visible_bounds_height": 4,
                        "visible_bounds_offset": [0, 1.5, 0],
                    },
                    "bones": [
                        {"name": "bipedHead", "pivot": [0, 24, 0]},
                        {"name": "armorHead", "parent": "bipedHead", "pivot": [0, 24, 0], "cubes": self.bones["armorHead"]},
                        {"name": "bipedBody", "pivot": [0, 24, 0]},
                        {"name": "armorBody", "parent": "bipedBody", "pivot": [0, 24, 0], "cubes": self.bones["armorBody"]},
                        {"name": "bipedRightArm", "pivot": [-5, 22, 0]},
                        {"name": "armorRightArm", "parent": "bipedRightArm", "pivot": [-5, 22, 0], "cubes": self.bones["armorRightArm"]},
                        {"name": "bipedLeftArm", "pivot": [5, 22, 0]},
                        {"name": "armorLeftArm", "parent": "bipedLeftArm", "pivot": [5, 22, 0], "cubes": self.bones["armorLeftArm"]},
                        {"name": "bipedRightLeg", "pivot": [-2, 12, 0]},
                        {"name": "armorRightLeg", "parent": "bipedRightLeg", "pivot": [-2, 12, 0], "cubes": self.bones["armorRightLeg"]},
                        {"name": "armorRightBoot", "parent": "bipedRightLeg", "pivot": [-2, 12, 0], "cubes": self.bones["armorRightBoot"]},
                        {"name": "bipedLeftLeg", "pivot": [2, 12, 0]},
                        {"name": "armorLeftLeg", "parent": "bipedLeftLeg", "pivot": [2, 12, 0], "cubes": self.bones["armorLeftLeg"]},
                        {"name": "armorLeftBoot", "parent": "bipedLeftLeg", "pivot": [2, 12, 0], "cubes": self.bones["armorLeftBoot"]},
                    ],
                }
            ],
        }
        geo_path = GEO_DIR / f"{self.set_name}_armor.geo.json"
        tex_path = TEX_DIR / f"{self.set_name}_armor.png"
        glow_path = TEX_DIR / f"{self.set_name}_armor_glowmask.png"
        geo_path.parent.mkdir(parents=True, exist_ok=True)
        TEX_DIR.mkdir(parents=True, exist_ok=True)
        geo_path.write_text(json.dumps(geo, indent=2) + "\n", encoding="utf-8")
        self.img.save(tex_path)
        self.glow.save(glow_path)
        print(f"Wrote {geo_path}, {tex_path} ({self.img.size}), {glow_path} ({self.glow.size})")


# =====================================================================
# 1.2 STARLIGHT CRYSTAL — Ornate Cyan-Hooded Arcane Ranger (Ref: Cyan Hood)
# =====================================================================

def build_starlight():
    b = ArmorBuilder("starlight")
    navy = [(10, 14, 34, 255), (18, 26, 56, 255), (30, 44, 88, 255), (50, 72, 130, 255)]
    pal = {
        ".": (0, 0, 0, 0),
        "0": navy[0], "1": navy[1], "2": navy[2], "3": navy[3],
        "k": (10, 42, 68, 255),     # deep teal-navy outline
        "d": (20, 98, 138, 255),    # dark cyan cloth
        "c": (46, 174, 210, 255),   # starlight cyan
        "C": (112, 234, 252, 255),  # bright ice-cyan
        "h": (222, 254, 255, 255),  # frost glint
        "l": (68, 38, 22, 255),     # dark leather strap
        "L": (122, 72, 42, 255),    # warm leather highlight
        "e": (56, 217, 192, 255),   # cyan glow
        "E": (190, 255, 255, 255),  # starlight core glow
    }
    glow_k = {"e", "E"}

    # Inner dark shells
    uv_hi = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.45)
    b.paint_inner_shell(uv_hi, 8, 8, 8, navy)
    b.blit_face(box_faces(*uv_hi, 8, 8, 8)["north"], [
        "1112222222222111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000eEE00EEe0001",
        "10000eE00Ee00001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000111111110001",
        "1001122222211001",
        "1111111111111111",
        "1111111111111111",
        "1111111111111111",
    ], pal, glow_k)

    uv_bi = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.45)
    b.paint_inner_shell(uv_bi, 8, 12, 4, navy, pattern="ribbed")
    uv_ai = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.38)
    b.paint_inner_shell(uv_ai, 4, 12, 4, navy, pattern="ribbed")
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.38, mirror=True, uv=uv_ai)
    uv_li = b.add_cube("armorRightLeg", [-4, 0, -2], [4, 12, 4], inflate=0.30)
    b.paint_inner_shell(uv_li, 4, 12, 4, navy)
    b.add_cube("armorLeftLeg", [0, 0, -2], [4, 12, 4], inflate=0.30, mirror=True, uv=uv_li)
    uv_bti = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.55)
    b.paint_inner_shell(uv_bti, 4, 12, 4, navy)
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.55, mirror=True, uv=uv_bti)

    # Outer 1-bit Alpha-Cutout Hood Cowl (NO crown, NO filigree cage!)
    uv_ho = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.92)
    hf = box_faces(*uv_ho, 8, 8, 8)
    b.blit_face(hf["north"], [
        "..kcCChhhhcCck..",
        ".kcCCccccccCCck.",
        "kcCccddddddccCck",
        "kCc..........cCk",
        "cC............Cc",
        "cC............Cc",
        "dc............cd",
        "kc............ck",
        "dc............cd",
        "cC............Cc",
        "cC............Cc",
        "kcC..........Cck",
        ".kcC........Cck.",
        "..kdcCccccCcdk..",
        "...kkddddddkk...",
        ".....kkkkkk.....",
    ], pal, glow_k)
    hood_side = [
        "..kcCCChhCCCck..",
        ".kcCCCccccCCCck.",
        "kcCcddccccddccCk",
        "kCc..........cCk",
        "cC............Cc",
        "dc............cd",
        "kc............ck",
        "cC............Cc",
        "cC............Cc",
        "dc............cd",
        "kcC..........Cck",
        ".kcCcc....ccCck.",
        "..kddCCCCCCddk..",
        "....kkkkkkkk....",
        "................",
        "................",
    ]
    b.blit_face(hf["east"], hood_side, pal, glow_k)
    b.blit_face(hf["west"], hood_side, pal, glow_k, flip_x=True)
    b.blit_face(hf["south"], [
        "..kcCCChhCCCck..",
        ".kcCCccccccCCck.",
        "kcCcddddddddcCck",
        "kCc..........cCk",
        "cC....cCCc....Cc",
        "dc...cChhCc...cd",
        "kc...dC..Cd...ck",
        "cC....dCCd....Cc",
        "cC............Cc",
        "dc............cd",
        "kcC..........Cck",
        ".kcCcc....ccCck.",
        "..kddCCCCCCddk..",
        "....kkkkkkkk....",
        "................",
        "................",
    ], pal, glow_k)
    b.blit_face(hf["up"], [
        "..kcCCChhCCCck..",
        ".kcCCccccccCCck.",
        "kcCcddddddddcCck",
        "kCc..........cCk",
        "cC............Cc",
        "cC............Cc",
        "cC............Cc",
        "cC............Cc",
        "cC............Cc",
        "cC............Cc",
        "cC............Cc",
        "kcC..........Cck",
        ".kcCCccccccCCck.",
        "..kddCCCCCCddk..",
        "....kkkkkkkk....",
        "................",
    ], pal, glow_k)

    # 0-thickness Swept Hood Tail at back of head
    uv_htail = b.add_cube("armorHead", [-4, 26.5, 4.8], [8, 7, 0], pivot=[0, 31, 4.8], rotation=[-26, 0, 0])
    b.paint_plane_z(uv_htail, 8, 7, [
        "kcCChhCCck......",
        "dcCCCCCCck......",
        ".kcCCCCck.......",
        "..kcCCck........",
        "...kcCk.........",
        "....kCk.........",
        ".....k..........",
    ], pal, glow_k)

    # Outer 1-bit Alpha-Cutout Diagonal Sash + Crystal Clasp + Belt + Pouch
    uv_bo = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.90)
    bf = box_faces(*uv_bo, 8, 12, 4)
    b.blit_face(bf["north"], [
        "kcCChh....hhCCck",
        "dcCCcL....LcCCcd",
        "kCc.lL....Ll.cCk",
        ".k..lLl..lLl..k.",
        "....lLkcCkLl....",
        "....lCcEEcCl....",
        ".....cCEECc.....",
        ".....dCEECd.....",
        "......kLLl......",
        "......lLLl......",
        ".......lLl......",
        ".......lLl......",
        "........lLl.....",
        "........lLl.....",
        ".........lLl....",
        ".........lLl....",
        "..........lLl...",
        "..........lLl...",
        "kLLLLlChhClLLLLk",
        "lLLLLlcEEcLLLLLl",
        "kllllllCCllllllk",
        "...ll......ll...",
        "...LL......LL...",
        "...ll......ll...",
    ], pal, glow_k)
    b.blit_face(bf["south"], [
        "kcCChhCCCCChhCCk",
        "dcCCcdddddddcCCd",
        "kCc..........cCk",
        "........lLl.....",
        ".......lLl......",
        "......lLl.......",
        ".....lLl........",
        "....lLl.........",
        "...lLl..........",
        "..lLl...........",
        ".lLl............",
        "lLl.............",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "kLLLLLLLLLLLLLLk",
        "lLLLLlChhClLLLLl",
        "klllllCCCClllllk",
        "................",
        "................",
        "................",
    ], pal, glow_k)

    # 0-thickness Sleek Crystal Shoulder Mantle Planes (NO bulky knight pauldrons!)
    uv_sm = b.add_cube("armorRightArm", [-10.5, 20.5, 0], [6, 5, 0], pivot=[-6.5, 22, 0], rotation=[0, 0, -24])
    b.paint_plane_z(uv_sm, 6, 5, [
        "....hcCck...",
        "..hcCCCck...",
        "hcCCCCCd....",
        "cCCCCCk.....",
        "kddddk......",
    ], pal, glow_k)
    b.add_cube("armorLeftArm", [4.5, 20.5, 0], [6, 5, 0], pivot=[6.5, 22, 0], rotation=[0, 0, 24], mirror=True, uv=uv_sm)

    # Outer Arm Bracers & Leg/Boot Wraps
    uv_ao = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.78)
    for s in ("north", "south", "east", "west"):
        b.blit_face(box_faces(*uv_ao, 4, 12, 4)[s], [
            "........", "........", "........", "........",
            "........", "........", "........", "........",
            "kcCChhCk", "dcCCCCcd", "........", "........",
            "........", "........", "..cEEc..", "..dCCd..",
            "........", "........", "kLLLLLLk", "lLLCCLLl",
            "lLLCCLLl", "kllllllk", "........", "........",
        ], pal, glow_k)
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.78, mirror=True, uv=uv_ao)

    uv_lo = b.add_cube("armorRightLeg", [-4, 0, -2], [4, 12, 4], inflate=0.68)
    for s in ("north", "south", "east", "west"):
        b.blit_face(box_faces(*uv_lo, 4, 12, 4)[s], [
            "kLLLLLLk", "lLLCCLLl", "kllllllk", "........",
            "........", "..cEEc..", "..dCCd..", "........",
            "........", "kLLLLLLk", "lLLLLLLl", "kllllllk",
            "........", "........", "........", "........",
            "........", "........", "........", "kcCCCCck",
            "dcCCCCcd", "kddddddk", "........", "........",
        ], pal, glow_k)
    b.add_cube("armorLeftLeg", [0, 0, -2], [4, 12, 4], inflate=0.68, mirror=True, uv=uv_lo)

    uv_bto = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.94)
    for s in ("north", "south", "east", "west"):
        b.blit_face(box_faces(*uv_bto, 4, 12, 4)[s], [
            "........", "........", "........", "........",
            "........", "........", "........", "........",
            "........", "........", "kcCChhCk", "dcCCCCcd",
            "kllllllk", "lLLLLLLl", "lLLLLLLl", "kllllllk",
            "........", "........", "kLLLLLLk", "lLLCCLLl",
            "lLLLLLLl", "lLLCCLLl", "kllllllk", "........",
        ], pal, glow_k)
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.94, mirror=True, uv=uv_bto)

    b.save()


# =====================================================================
# 1.3 PRISMATIC BRIMSTONE — Ornate Cursed Pirate-Admiral (Captain Brimstone + Prism Core)
# =====================================================================

def build_prismatic():
    b = ArmorBuilder("prismatic")
    charcoal = [(16, 13, 18, 255), (28, 22, 32, 255), (46, 36, 52, 255), (68, 56, 76, 255)]
    crimson = [(68, 14, 20, 255), (108, 22, 32, 255), (156, 38, 48, 255), (204, 62, 68, 255)]
    pal = {
        ".": (0, 0, 0, 0),
        "0": charcoal[0], "1": charcoal[1], "2": charcoal[2], "3": charcoal[3],
        "r": crimson[1], "R": crimson[2], "x": crimson[3],
        "k": (54, 22, 4, 255),      # dark bronze outline
        "d": (128, 62, 10, 255),    # aged brass
        "g": (208, 132, 24, 255),   # pirate gold braid
        "G": (248, 194, 58, 255),   # bright gold
        "h": (255, 240, 142, 255),  # gold glint
        "e": (255, 96, 12, 255),    # magma ember glow
        "E": (255, 220, 76, 255),   # bright magma core
        "p": (168, 48, 224, 255),   # amethyst prism
        "P": (232, 132, 255, 255),  # bright prism core
    }
    glow_k = {"e", "E", "p", "P"}

    # Inner dark shells
    uv_hi = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.45)
    b.paint_inner_shell(uv_hi, 8, 8, 8, charcoal)
    b.blit_face(box_faces(*uv_hi, 8, 8, 8)["north"], [
        "1112222222222111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1001100000eEE001",
        "10010100000eE001",
        "1000110000000001",
        "1000001000000001",
        "1000000100000001",
        "1000000010000001",
        "1000111111110001",
        "1001122222211001",
        "1111111111111111",
        "1111111111111111",
        "1111111111111111",
    ], pal, glow_k)

    uv_bi = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.45)
    b.paint_inner_shell(uv_bi, 8, 12, 4, charcoal, accent=(crimson[1], crimson[2]))
    uv_ai = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.38)
    b.paint_inner_shell(uv_ai, 4, 12, 4, charcoal)
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.38, mirror=True, uv=uv_ai)
    uv_li = b.add_cube("armorRightLeg", [-4, 0, -2], [4, 12, 4], inflate=0.30)
    b.paint_inner_shell(uv_li, 4, 12, 4, charcoal)
    b.add_cube("armorLeftLeg", [0, 0, -2], [4, 12, 4], inflate=0.30, mirror=True, uv=uv_li)
    uv_bti = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.55)
    b.paint_inner_shell(uv_bti, 4, 12, 4, charcoal)
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.55, mirror=True, uv=uv_bti)

    # Outer 1-bit Alpha-Cutout Pirate Collar & Hat Band (NO crown!)
    uv_ho = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.88)
    hf = box_faces(*uv_ho, 8, 8, 8)
    b.blit_face(hf["north"], [
        "kddGGGGGGGGGgddk",
        "k00000000000000k",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "k0............0k",
        "r0............0r",
        "R0k..........k0R",
        ".R0gGGGGGGGGg0R.",
        "..kddGGGGGGddk..",
        "....kkkkkkkk....",
    ], pal, glow_k)
    for s in ("east", "west", "south"):
        b.blit_face(hf[s], [
            "kddGGGGGGGGGGddk",
            "k00000000000000k",
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "kR............Rk",
            ".kR0000000000Rk.",
            "..kddGGGGGGddk..",
            "....kkkkkkkk....",
            "................",
        ], pal, glow_k)

    # 0-thickness Curved Pirate Tricorne Hat + Crimson Plume (ZERO boxy cubes!)
    uv_tri_f = b.add_cube("armorHead", [-7.0, 30.0, -5.4], [14, 8, 0], pivot=[0, 31, -5.4], rotation=[14, 0, 0])
    b.paint_plane_z(uv_tri_f, 14, 8, [
        "..............",
        ".........xR...",
        "........xRRr..",
        "..kGGGGGGGGGGk",
        ".kG000000000Gk",
        "kG0000gG0000Gk",
        "dG0000GG0000Gd",
        ".kddGGGGGGddk.",
    ], pal, glow_k)
    uv_tri_s = b.add_cube("armorHead", [-5.4, 30.0, -6.0], [0, 8, 12], pivot=[-5.4, 31, 0], rotation=[0, 0, -18])
    b.paint_plane_x(uv_tri_s, 8, 12, [
        "......xRx...",
        ".....xRRRr..",
        "....xRRr....",
        "kGGGGGGGGGGk",
        "G0000000000G",
        "G0000000000G",
        "dG00000000Gd",
        ".kddGGGGddk.",
    ], pal, glow_k)
    b.add_cube("armorHead", [5.4, 30.0, -6.0], [0, 8, 12], pivot=[5.4, 31, 0], rotation=[0, 0, 18], mirror=True, uv=uv_tri_s)

    # Outer 1-bit Alpha-Cutout Crimson Pirate-Admiral Longcoat + Gold Braid + Prism Core
    uv_bo = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.90)
    bf = box_faces(*uv_bo, 8, 12, 4)
    b.blit_face(bf["north"], [
        "kGGRR......RRGGk",
        "dGGRRr....rRRGGd",
        "kG.rRRk..kRRr.Gk",
        "rR..rRkpPkRr..Rr",
        "RR...pPPPPp...RR",
        "rR...pPPPPp...Rr",
        "kR....kppk....Rk",
        "rR...gG..Gg...Rr",
        "RR..gG....Gg..RR",
        "rR..dG....Gd..Rr",
        "kR............Rk",
        "rR.gG......Gg.Rr",
        "RR.dG......Gd.RR",
        "rR............Rr",
        "kR.gG......Gg.Rk",
        "rR.dG......Gd.Rr",
        "RR............RR",
        "kR............Rk",
        "kGGGGGGGGGGGGGGk",
        "dGGGGGhhhhGGGGGd",
        "kddGGGgGGgGGGddk",
        "rRRr..gGGg..rRRr",
        "rRRr...kk...rRRr",
        ".rr..........rr.",
    ], pal, glow_k)
    b.blit_face(bf["south"], [
        "kGGGGGGGGGGGGGGk",
        "dGGRRRRRRRRRRGGd",
        "kR............Rk",
        "rR............Rr",
        "RR....kGGk....RR",
        "rR...gGhhGg...Rr",
        "kR...dG..Gd...Rk",
        "rR....kGGk....Rr",
        "RR............RR",
        "rR............Rr",
        "kR............Rk",
        "rR............Rr",
        "RR............RR",
        "rR............Rr",
        "kR............Rk",
        "rR............Rr",
        "RR............RR",
        "kR............Rk",
        "kGGGGGGGGGGGGGGk",
        "dGGGGGhhhhGGGGGd",
        "kddGGGgGGgGGGddk",
        "rRRr........rRRr",
        "rRRr........rRRr",
        ".rr..........rr.",
    ], pal, glow_k)

    # Pirate Coat Skirt (0.98 inflate cutout)
    uv_sk = b.add_cube("armorBody", [-4, 6, -2], [8, 6, 4], inflate=1.06)
    wf = box_faces(*uv_sk, 8, 6, 4)
    b.blit_face(wf["north"], [
        "kGGGGGg..gGGGGGk",
        "rRR..Gk..kG..RRr",
        "rRR..G....G..RRr",
        "rRR..........RRr",
        "kRR..........RRk",
        ".kk..........kk.",
    ], pal, glow_k)
    b.blit_face(wf["south"], [
        "kGGGGGGGGGGGGGGk",
        "rRRRRRRRRRRRRRRr",
        "rRRRRRRRRRRRRRRr",
        "rRRRr..RR..rRRRr",
        "kRRr........rRRk",
        ".kk..........kk.",
    ], pal, glow_k)

    # 0-thickness Broadside Shoulder Cannon & Epaulette Planes (NO boxy cannon bricks!)
    uv_ep = b.add_cube("armorRightArm", [-11.5, 21.0, 0], [7, 6, 0], pivot=[-7.0, 22.0, 0], rotation=[0, 0, -18])
    b.paint_plane_z(uv_ep, 7, 6, [
        "....kGGk....",
        "..kGG00GGk..",
        ".kG00eE00Gk.",
        "kGG0eEEe0GGk",
        "dGG00eE00GGd",
        ".kddGGGGddk.",
    ], pal, glow_k)
    b.add_cube("armorLeftArm", [4.5, 21.0, 0], [7, 6, 0], pivot=[7.0, 22.0, 0], rotation=[0, 0, 18], mirror=True, uv=uv_ep)

    # Outer Crimson Pirate Cuffs & Gold-Buckled Sea-Boots
    uv_ao = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.78)
    for s in ("north", "south", "east", "west"):
        b.blit_face(box_faces(*uv_ao, 4, 12, 4)[s], [
            "........", "........", "........", "........",
            "........", "........", "kGGGGGGk", "rRRRRRRr",
            "rRRRRRRr", "rRRRRRRr", "krrrrrrk", "........",
            "........", "........", "........", "........",
            "........", "........", "kGGGGGGk", "dGGhhGGd",
            "rRRRRRRr", "kddddddk", "........", "........",
        ], pal, glow_k)
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.78, mirror=True, uv=uv_ao)

    uv_bto = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.94)
    for s in ("north", "south", "east", "west"):
        b.blit_face(box_faces(*uv_bto, 4, 12, 4)[s], [
            "........", "........", "........", "........",
            "........", "........", "........", "........",
            "........", "........", "kGGGGGGk", "dGGhhGGd",
            "k000000k", "k000000k", "........", "........",
            "........", "........", "kGGGGGGk", "dG0000Gd",
            "k000000k", "kddGGddk", "........", "........",
        ], pal, glow_k)
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.94, mirror=True, uv=uv_bto)

    b.save()


# =====================================================================
# 1.4 DRAGONSCALE — Ornate Emerald Dragon-Hunter (Carved Drake Skull + Dorsal Spines)
# =====================================================================

def build_dragonscale():
    b = ArmorBuilder("dragonscale")
    emerald = [(8, 28, 18, 255), (15, 52, 32, 255), (26, 88, 52, 255), (44, 134, 78, 255)]
    pal = {
        ".": (0, 0, 0, 0),
        "0": emerald[0], "1": emerald[1], "2": emerald[2], "3": emerald[3],
        "k": (16, 22, 26, 255),     # dark slate-iron outline
        "d": (46, 56, 66, 255),     # dark drake-bone
        "g": (28, 148, 78, 255),    # vivid emerald scale
        "G": (74, 212, 126, 255),   # bright jade scale
        "h": (196, 255, 214, 255),  # pale jade-bone glint
        "e": (255, 128, 12, 255),   # molten amber glow
        "E": (255, 228, 88, 255),   # dragon-heart core glow
    }
    glow_k = {"e", "E"}

    # Inner emerald-scale shells
    uv_hi = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.45)
    b.paint_inner_shell(uv_hi, 8, 8, 8, emerald, pattern="scales")
    b.blit_face(box_faces(*uv_hi, 8, 8, 8)["north"], [
        "1112222222222111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "100eEE0000EEe001",
        "1000eE0000Ee0001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000111111110001",
        "1001122222211001",
        "1111111111111111",
        "1111111111111111",
        "1111111111111111",
        "1111111111111111",
    ], pal, glow_k)

    uv_bi = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.45)
    b.paint_inner_shell(uv_bi, 8, 12, 4, emerald, pattern="scales")
    uv_ai = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.38)
    b.paint_inner_shell(uv_ai, 4, 12, 4, emerald, pattern="scales")
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.38, mirror=True, uv=uv_ai)
    uv_li = b.add_cube("armorRightLeg", [-4, 0, -2], [4, 12, 4], inflate=0.30)
    b.paint_inner_shell(uv_li, 4, 12, 4, emerald, pattern="scales")
    b.add_cube("armorLeftLeg", [0, 0, -2], [4, 12, 4], inflate=0.30, mirror=True, uv=uv_li)
    uv_bti = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.55)
    b.paint_inner_shell(uv_bti, 4, 12, 4, emerald, pattern="scales")
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.55, mirror=True, uv=uv_bti)

    # Outer 1-bit Alpha-Cutout Carved Dragon-Skull Visor
    uv_ho = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.88)
    hf = box_faces(*uv_ho, 8, 8, 8)
    b.blit_face(hf["north"], [
        "kGhGgk....kgGhGk",
        "dGGgGGk..kGGgGGd",
        "kG..gGGkkGGg..Gk",
        "gG...gGGGGg...Gg",
        "GG....kGGk....GG",
        "gG............Gg",
        "dG............Gd",
        "kG............Gk",
        "gGk..........kGg",
        "GGgk........kgGG",
        "dGGgk......kgGGd",
        "kGGgGk....kGgGGk",
        ".kGGgGk..kGgGGk.",
        "..kGGgGkkGgGGk..",
        "...kddGGGGddk...",
        ".....kkkkkk.....",
    ], pal, glow_k)
    for s in ("east", "west", "south"):
        b.blit_face(hf[s], [
            "kGhGGGGGGGGGGhGk",
            "dGGgddGGGGddgGGd",
            "kG..gGGkkGGg..Gk",
            ".kGg.GG..GG.gGk.",
            "..kGgGG..GGgGk..",
            "kG............Gk",
            "gG............Gg",
            "dG............Gd",
            "kG............Gk",
            "gGk..........kGg",
            "dGGgk......kgGGd",
            ".kGGgk....kgGGk.",
            "..kddGGggGGddk..",
            "....kkkkkkkk....",
            "................",
            "................",
        ], pal, glow_k)

    # 0-thickness Curved Swept Dragon Horns (side of head — crisp tapered horn curve!)
    uv_dhorn = b.add_cube("armorHead", [-5.2, 29.0, -3.5], [0, 10, 12], pivot=[-5.2, 30, 0], rotation=[8, 0, -16])
    b.paint_plane_x(uv_dhorn, 10, 12, [
        "..........h.",
        ".........hG.",
        "........hGG.",
        ".......dGGk.",
        "......dGGk..",
        ".....dGGk...",
        "...hdGGk....",
        "..hGGGk.....",
        ".gGGGk......",
        "kddk........",
    ], pal, glow_k)
    b.add_cube("armorHead", [5.2, 29.0, -3.5], [0, 10, 12], pivot=[5.2, 30, 0], rotation=[8, 0, 16], mirror=True, uv=uv_dhorn)

    # Outer 1-bit Alpha-Cutout Overlapping V-Chevron Dragon Scales on Body
    uv_bo = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.90)
    bf = box_faces(*uv_bo, 8, 12, 4)
    b.blit_face(bf["north"], [
        "kGhGgk....kgGhGk",
        "dGGgGGk..kGGgGGd",
        ".kGGgGGkkGGgGGk.",
        "..kGGgGGGGgGGk..",
        "...kGGgEEgGGk...",
        "....kGEEEEGk....",
        "kGhGgkEEEEkgGhGk",
        ".dGGgGGEEGGgGGd.",
        "..kGGgGGGGgGGk..",
        "...kGGgGGgGGk...",
        "....kGGGGGGk....",
        "kGhGgkGGGGkgGhGk",
        ".dGGgGGGGGGgGGd.",
        "..kGGgGGGGgGGk..",
        "...kGGgGGgGGk...",
        "....kGGGGGGk....",
        ".....kGGGGk.....",
        "......kGGk......",
        "kGhGGGGGGGGGGhGk",
        "dGGhGGGGGGGGhGGd",
        "kddGGGgGGgGGGddk",
        "...kkkGGGGkkk...",
        "......gGGg......",
        ".......kk.......",
    ], pal, glow_k)
    b.blit_face(bf["south"], [
        "kGhGGGGGGGGGGhGk",
        "dGGhGGGGGGGGhGGd",
        "kG..gGG..GGg..Gk",
        ".kGg.GG..GG.gGk.",
        "..kGgGG..GGgGk..",
        "...kGGG..GGGk...",
        "kGhGgkG..GkGhGGk",
        ".dGGgGG..GGgGGd.",
        "..kGGgG..GgGGk..",
        "...kGGG..GGGk...",
        "....kGG..GGk....",
        "kGhGgkG..GkGhGGk",
        ".dGGgGG..GGgGGd.",
        "..kGGgG..GgGGk..",
        "...kGGG..GGGk...",
        "....kGG..GGk....",
        ".....kG..Gk.....",
        "......k..k......",
        "kGhGGGGGGGGGGhGk",
        "dGGhGGGGGGGGhGGd",
        "kddGGGgGGgGGGddk",
        "....kkkkkkkk....",
        "................",
        "................",
    ], pal, glow_k)

    # 0-thickness Serrated Dorsal Dragon Spine Ridge down the back!
    uv_spine = b.add_cube("armorBody", [0, 10.0, 2.5], [0, 14, 6], pivot=[0, 17, 2.5], rotation=[0, 0, 0])
    b.paint_plane_x(uv_spine, 14, 6, [
        "....h.",
        "...hG.",
        "..dGk.",
        ".dGk..",
        "....h.",
        "...hG.",
        "..dGk.",
        ".dGk..",
        "....h.",
        "...hG.",
        "..dGk.",
        ".dGk..",
        "..k...",
        "......",
    ], pal, glow_k)

    # 0-thickness Jagged Drake-Wing Pauldron Spires & Forearm Claw Fins
    uv_dp = b.add_cube("armorRightArm", [-11.5, 20.5, 0], [7, 7, 0], pivot=[-7.0, 22.0, 0], rotation=[0, 0, -22])
    b.paint_plane_z(uv_dp, 7, 7, [
        "..........h.",
        ".........gG.",
        "......h.gGG.",
        ".....gGgGGk.",
        "..h.gGGGGk..",
        ".gGgGGhGk...",
        "kddGGGdk....",
    ], pal, glow_k)
    b.add_cube("armorLeftArm", [4.5, 20.5, 0], [7, 7, 0], pivot=[7.0, 22.0, 0], rotation=[0, 0, 22], mirror=True, uv=uv_dp)

    uv_claw = b.add_cube("armorRightArm", [-9.2, 11.5, -1.5], [0, 6, 4], pivot=[-8.5, 15, 0.5], rotation=[0, 0, -12])
    b.paint_plane_x(uv_claw, 6, 4, [
        "...h",
        "..hG",
        ".dGk",
        ".dGk",
        "..k.",
        "....",
    ], pal, glow_k)
    b.add_cube("armorLeftArm", [9.2, 11.5, -1.5], [0, 6, 4], pivot=[8.5, 15, 0.5], rotation=[0, 0, 12], mirror=True, uv=uv_claw)

    b.save()


# =====================================================================
# 1.5 TWIN-STAR — Ornate Winged Halo Celestial Seraph (Ref: Winged Halo)
# =====================================================================

def build_twin_star():
    b = ArmorBuilder("twin_star")
    indigo = [(14, 10, 32, 255), (24, 16, 54, 255), (42, 28, 86, 255), (68, 48, 128, 255)]
    pal = {
        ".": (0, 0, 0, 0),
        "0": indigo[0], "1": indigo[1], "2": indigo[2], "3": indigo[3],
        "k": (58, 66, 86, 255),     # slate-silver outline
        "d": (132, 144, 172, 255),  # shaded pearl-silver
        "w": (204, 214, 234, 255),  # bright platinum-silver
        "W": (244, 248, 255, 255),  # pure seraph white
        "g": (222, 158, 28, 255),   # celestial gold
        "G": (252, 212, 74, 255),   # radiant gold
        "h": (255, 244, 168, 255),  # starlight glint
        "e": (104, 238, 255, 255),  # celestial cyan glow
        "E": (218, 255, 255, 255),  # pure cyan-white core glow
    }
    glow_k = {"e", "E"}

    # Inner deep void-indigo shells
    uv_hi = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.45)
    b.paint_inner_shell(uv_hi, 8, 8, 8, indigo)
    b.blit_face(box_faces(*uv_hi, 8, 8, 8)["north"], [
        "1112222222222111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "100eEE0000EEe001",
        "1000eE0000Ee0001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000111111110001",
        "1001122222211001",
        "1111111111111111",
        "1111111111111111",
        "1111111111111111",
    ], pal, glow_k)

    uv_bi = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.45)
    b.paint_inner_shell(uv_bi, 8, 12, 4, indigo, pattern="ribbed")
    uv_ai = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.38)
    b.paint_inner_shell(uv_ai, 4, 12, 4, indigo)
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.38, mirror=True, uv=uv_ai)
    uv_li = b.add_cube("armorRightLeg", [-4, 0, -2], [4, 12, 4], inflate=0.30)
    b.paint_inner_shell(uv_li, 4, 12, 4, indigo)
    b.add_cube("armorLeftLeg", [0, 0, -2], [4, 12, 4], inflate=0.30, mirror=True, uv=uv_li)
    uv_bti = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.55)
    b.paint_inner_shell(uv_bti, 4, 12, 4, indigo)
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.55, mirror=True, uv=uv_bti)

    # Outer 1-bit Alpha-Cutout Platinum & Gold Seraph T-Visor Cage
    uv_ho = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.88)
    hf = box_faces(*uv_ho, 8, 8, 8)
    b.blit_face(hf["north"], [
        "kwWWWWGGGGWWWWwk",
        "dwWWWGhhhhGWWWwd",
        "kwWWgGGGGGGgWWwk",
        "wW....gGGg....Ww",
        "WW....gGGg....WW",
        "wW....gGGg....Ww",
        "dw....gGGg....wd",
        "kw....gGGg....wk",
        "wW....gGGg....Ww",
        "WWk...gGGg...kWW",
        "dwWk...GG...kWwd",
        ".kwWk..GG..kWwk.",
        "..kwWggggggWwk..",
        "...kddWWWWddk...",
        ".....kkkkkk.....",
        "................",
    ], pal, glow_k)
    for s in ("east", "west", "south"):
        b.blit_face(hf[s], [
            "kwWWWWGGGGWWWWwk",
            "dwWWWWGGGGWWWWwd",
            "kW.....GG.....Wk",
            "wW....gGGg....Ww",
            "WW...gGhhGg...WW",
            "wW..dw....wd..Ww",
            "dw............wd",
            "kw............wk",
            "wW............Ww",
            "WWk..........kWW",
            "dwWk........kWwd",
            ".kwWggkkkkggWwk.",
            "..kddWWWWWWddk..",
            "....kkkkkkkk....",
            "................",
            "................",
        ], pal, glow_k)

    # MASSIVE 20x20 Vertical Radiant Halo Ring behind the head!
    uv_halo = b.add_cube("armorHead", [-10, 22.0, 5.2], [20, 20, 0], pivot=[0, 32, 5.2], rotation=[-10, 0, 0])
    b.paint_plane_z(uv_halo, 20, 20, [
        ".........EE.........",
        "......eeEEEEee......",
        "....eeEEGGGGEEee....",
        "...eEEGG....GGEEe...",
        "..eEEG........GEEe..",
        ".eEEG..........GEEe.",
        ".eEG....gGGg....GEe.",
        "eEE....gGhhGg....EEe",
        "eEG....GheehG....GEe",
        "EEG....GeEEeG....GEE",
        "EEG....GheehG....GEE",
        "eEG....gGhhGg....GEe",
        "eEE.....gGGg.....EEe",
        ".eEG............GEe.",
        ".eEEG..........GEEe.",
        "..eEEG........GEEe..",
        "...eEEGG....GGEEe...",
        "....eeEEGGGGEEee....",
        "......eeEEEEee......",
        ".........EE.........",
    ], pal, glow_k)

    # Outer 1-bit Alpha-Cutout Platinum & Gold Seraph Filigree on Body
    uv_bo = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.90)
    bf = box_faces(*uv_bo, 8, 12, 4)
    b.blit_face(bf["north"], [
        "kwWWwk....kwWWwk",
        "dwWWwWk..kWwWWwd",
        "kW..wWWkkWWw..Wk",
        "wW...wGGGGw...Ww",
        "WW...gGEEGg...WW",
        "wW..gGEEEEGg..Ww",
        "dw..GGEhhEGG..wd",
        "kw..gGEEEEGg..wk",
        "wW...gGEEGg...Ww",
        "WW....gGGg....WW",
        "wW...wW..Ww...Ww",
        "dw..wW....Ww..wd",
        "kW.wW......Ww.Wk",
        "wW.dW..GG..Wd.Ww",
        "WW....gGGg....WW",
        "wW....dGGd....Ww",
        "dw............wd",
        "kW............Wk",
        "kwWWGGGGGGGGWWwk",
        "dwWWGGhhhhGGWWwd",
        "kddWWgGGGGgWWddk",
        "...kkkGGGGkkk...",
        "......gGGg......",
        ".......kk.......",
    ], pal, glow_k)
    b.blit_face(bf["south"], [
        "kwWWWWGGGGWWWWwk",
        "dwWWWWGGGGWWWWwd",
        "kW.....GG.....Wk",
        "wW....gGGg....Ww",
        "WW...gGhhGg...WW",
        "wW..wWw..wWw..Ww",
        "dw..Ww....wW..wd",
        "kW..wW....Ww..Wk",
        "wW...wW..Ww...Ww",
        "WW....wWWw....WW",
        "wW.....WW.....Ww",
        "dw............wd",
        "kW............Wk",
        "wW............Ww",
        "WW............WW",
        "wW............Ww",
        "dw............wd",
        "kW............Wk",
        "kwWWGGGGGGGGWWwk",
        "dwWWGGhhhhGGWWwd",
        "kddWWWWWWWWWWddk",
        "....kkkkkkkk....",
        "................",
        "................",
    ], pal, glow_k)

    # GIANT 20x24 Spread Celestial Wings on each side!
    uv_wing = b.add_cube("armorBody", [-22.0, 10.0, 4.2], [20, 24, 0], pivot=[-2.0, 20.0, 4.2], rotation=[-8, 22, -6])
    b.paint_plane_z(uv_wing, 20, 24, [
        "..................EE",
        "................eeEW",
        "..............eeEEWW",
        "............eeEEGGWW",
        "..........eeEEGGWWWW",
        "........eeEEGGWWWWw.",
        "......eeEEGGWWWWw...",
        "....eeEEGGWWWWw.....",
        "...eEEGGWWWWw.......",
        "..eEGGWWWWw.........",
        ".eEGWWWWw...........",
        "eEGWWWw.............",
        "EGWWw...............",
        "GWWw...eeEEGG.......",
        "WWw..eeEEGGWWw......",
        "Ww..eEEGGWWWw.......",
        "w..eEGGWWWw.........",
        "..eEGWWWw...........",
        "..EGWWw...eeEE......",
        "..GWw...eeEEWWw.....",
        "..w....eEEGGWw......",
        "......eEGGWw........",
        "......EGWw..........",
        "......w.............",
    ], pal, glow_k)
    b.add_cube("armorBody", [2.0, 10.0, 4.2], [20, 24, 0], pivot=[2.0, 20.0, 4.2], rotation=[-8, -22, 6], mirror=True, uv=uv_wing)

    # Outer Platinum & Gold Arm, Leg, Boot Filigree
    uv_ao = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.78)
    for s in ("north", "south", "east", "west"):
        b.blit_face(box_faces(*uv_ao, 4, 12, 4)[s], [
            "........", "........", "........", "........",
            "........", "........", "kwWWGGwk", "kdd..ddk",
            "........", "..wGGw..", ".dW..Wd.", "..kGGk..",
            "........", "..wWWw..", ".wW..Ww.", ".W.EE.W.",
            ".dW..Wd.", "..kWWk..", "........", "kwWWGGwk",
            "dW.hh.Wd", "kW....Wk", "kddGGddk", "..kkkk..",
        ], pal, glow_k)
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.78, mirror=True, uv=uv_ao)

    uv_bto = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.94)
    for s in ("north", "south", "east", "west"):
        b.blit_face(box_faces(*uv_bto, 4, 12, 4)[s], [
            "........", "........", "........", "........",
            "........", "........", "........", "........",
            "........", "........", "kwWWGGwk", "kdd..ddk",
            "...WW...", "..w..w..", ".wW..Ww.", ".dW..Wd.",
            "..w..w..", "...WW...", "kwWh.hWk", "dW.EE.Wd",
            "kW....Wk", "wW.GG.Ww", "dWWGGWWd", "kddGGddk",
        ], pal, glow_k)
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.94, mirror=True, uv=uv_bto)

    b.save()


def main():
    build_starlight()
    build_prismatic()
    build_dragonscale()
    build_twin_star()


if __name__ == "__main__":
    main()
