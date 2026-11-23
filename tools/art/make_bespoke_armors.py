"""Builds 4 genuinely distinct, reference-driven v3 armor sets (256x256, 2x UV density):
- Starlight Crystal: Sleek hooded arcane mage-ranger (hood cowl, diagonal sash, pouches, no crown)
- Prismatic Brimstone: Massive top-heavy dark horned juggernaut (3D bull horns, jaw guard, huge tiered pauldrons)
- Dragonscale: Reptilian dragon-skull hunter (3D snout, swept horns, dorsal spine ridge, forearm claws, drake-claw boots)
- Twin-Star: Grand winged halo seraph (huge spread celestial wings, massive vertical halo, seraph surcoat)

Usage:
    python tools/art/make_bespoke_armors.py
"""
from __future__ import annotations

import json
from dataclasses import dataclass, field
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
    """Packs non-overlapping UVs on a 128x128 grid (256x256 PNG) and paints cubes/planes."""

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

    def paint_shaded_box(
        self,
        uv: tuple[int, int],
        w: int,
        h: int,
        d: int,
        tones: list[tuple[int, int, int, int]],
        *,
        pattern: str = "forged",
        trim_color: tuple[int, int, int, int] | None = None,
        glow_color: tuple[int, int, int, int] | None = None,
        stripe_y: int | None = None,
    ):
        px = self.img.load()
        gpx = self.glow.load()
        biases = {"up": 1, "north": 0, "east": 0, "west": 0, "south": 0, "down": -1}
        for fname, rect in box_faces(uv[0], uv[1], w, h, d).items():
            rx, ry, rw, rh = rect
            if rw <= 0 or rh <= 0:
                continue
            b = biases[fname]
            for dy in range(rh):
                for dx in range(rw):
                    hash_v = _hash(rx + dx, ry + dy)
                    v_frac = dy / max(1, rh - 1)
                    idx = 1 if v_frac < 0.65 else 0
                    if v_frac < 0.25 and hash_v > 140:
                        idx = 2
                    elif hash_v > 220:
                        idx = min(3, idx + 1)
                    elif hash_v < 55:
                        idx = max(0, idx - 1)
                    rgb = tones[max(0, min(3, idx + b))]

                    if pattern == "scales":
                        row_s = dy // 4
                        lx = (dx + (2 if row_s % 2 else 0)) % 4
                        ly = dy % 4
                        if ly == 3 or (ly == 2 and (lx == 0 or lx == 3)):
                            rgb = tones[0]
                        elif ly == 0 and (lx == 1 or lx == 2):
                            rgb = tones[min(3, 2 + max(0, b))]
                    elif pattern == "volcanic":
                        if ((dx * 3 + dy * 5) % 17 == 0 and hash_v > 115) or ((dx * 5 - dy * 2) % 21 == 0 and hash_v > 135):
                            if glow_color is not None:
                                rgb = glow_color
                                gpx[rx + dx, ry + dy] = glow_color
                    elif pattern == "crystal":
                        if (dx + dy) % 6 == 0 or (dx - dy) % 7 == 0:
                            rgb = tones[min(3, idx + 1 + max(0, b))]
                        elif (dx + dy) % 6 == 3:
                            rgb = tones[0]
                    elif pattern == "cloth":
                        if (dx + dy) % 4 == 0 and hash_v > 160:
                            rgb = tones[min(3, idx + 1)]
                    elif pattern == "plate":
                        if (dx == 0 or dy == 0) and trim_color is not None:
                            rgb = trim_color
                        elif dx == rw - 1 or dy == rh - 1:
                            rgb = tones[0]

                    if stripe_y is not None and dy in (stripe_y, stripe_y + 1) and trim_color is not None:
                        rgb = trim_color

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
# 1.2 STARLIGHT CRYSTAL — Sleek Hooded Arcane Mage-Ranger (Ref: Cyan Hood)
# =====================================================================

def build_starlight():
    b = ArmorBuilder("starlight")
    navy = [(12, 16, 36, 255), (20, 28, 58, 255), (32, 46, 88, 255), (52, 74, 128, 255)]
    cyan = [(18, 92, 118, 255), (38, 162, 194, 255), (72, 224, 240, 255), (176, 252, 255, 255)]
    leather = [(42, 24, 16, 255), (72, 42, 26, 255), (108, 66, 40, 255), (144, 92, 58, 255)]
    pal = {
        ".": (0, 0, 0, 0),
        "0": navy[0], "1": navy[1], "2": navy[2], "3": navy[3],
        "d": cyan[0], "c": cyan[1], "C": cyan[2], "h": cyan[3],
        "l": leather[1], "L": leather[2],
        "e": (56, 217, 192, 255),
        "E": (190, 255, 255, 255),
    }
    glow_k = {"e", "E"}

    # --- HEAD: Deep Hooded Cowl (NO crown, NO filigree cage!) ---
    # Inner shadowed cowl + glowing cyan eyes
    uv_hi = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.45)
    b.paint_shaded_box(uv_hi, 8, 8, 8, navy, pattern="cloth")
    b.blit_face(box_faces(*uv_hi, 8, 8, 8)["north"], [
        "1111111111111111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000eEE00EEe0001",
        "10000eE00Ee00001",
        "1000000000000001",
        "1000000000000001",
        "1001111111111001",
        "1011222222221101",
        "1011111111111101",
        "1111111111111111",
        "1111111111111111",
        "1111111111111111",
    ], pal, glow_k)

    # Thick 3D hood brim framing the face (top, left, right, back taper)
    uv_ht = b.add_cube("armorHead", [-5, 31.2, -5.2], [10, 2, 10], pivot=[0, 32, 0], rotation=[-6, 0, 0])
    b.paint_shaded_box(uv_ht, 10, 2, 10, cyan, pattern="cloth")
    uv_hl = b.add_cube("armorHead", [-5.2, 24.2, -5.0], [2, 8, 9], pivot=[-4.2, 28, 0], rotation=[0, 0, 4])
    b.paint_shaded_box(uv_hl, 2, 8, 9, cyan, pattern="cloth")
    b.add_cube("armorHead", [3.2, 24.2, -5.0], [2, 8, 9], pivot=[4.2, 28, 0], rotation=[0, 0, -4], mirror=True, uv=uv_hl)
    # Swept-back pointed hood tail
    uv_hb = b.add_cube("armorHead", [-3, 27.5, 4.0], [6, 5, 4], pivot=[0, 30, 4.0], rotation=[-22, 0, 0])
    b.paint_shaded_box(uv_hb, 6, 5, 4, navy, pattern="cloth", trim_color=cyan[2])

    # --- BODY: Sleek Arcane Tunic + Diagonal Leather Sash + Crystal Clasp + Split Coat ---
    uv_bi = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.45)
    b.paint_shaded_box(uv_bi, 8, 12, 4, navy, pattern="cloth", trim_color=cyan[1], stripe_y=20)
    # Diagonal bandolier sash across chest
    uv_sash = b.add_cube("armorBody", [-1.2, 12.0, -2.7], [3, 12, 1], pivot=[0, 18, -2.2], rotation=[0, 0, 34])
    b.paint_shaded_box(uv_sash, 3, 12, 1, leather, pattern="cloth", trim_color=cyan[2])
    # Glowing crystal brooch on sash
    uv_gem = b.add_cube("armorBody", [-1, 18.0, -3.3], [2, 3, 1])
    b.paint_shaded_box(uv_gem, 2, 3, 1, [pal["e"], pal["E"], pal["E"], pal["h"]], pattern="crystal", glow_color=pal["E"])
    # Neck scarf / hood cowl collar
    uv_col = b.add_cube("armorBody", [-5, 21.5, -3.2], [10, 3, 6], pivot=[0, 23, 0], rotation=[8, 0, 0])
    b.paint_shaded_box(uv_col, 10, 3, 6, cyan, pattern="cloth")
    # Leather belt + asymmetric hip pouch
    uv_belt = b.add_cube("armorBody", [-4.5, 11.2, -2.6], [9, 2, 5])
    b.paint_shaded_box(uv_belt, 9, 2, 5, leather, pattern="cloth", trim_color=cyan[2])
    uv_pouch = b.add_cube("armorBody", [3.2, 8.5, -2.2], [2, 3, 4], pivot=[4, 10, 0], rotation=[0, 0, -8])
    b.paint_shaded_box(uv_pouch, 2, 3, 4, leather, pattern="cloth")
    # Long flowing split mage-cloak tails at back
    uv_tail = b.add_cube("armorBody", [-4, 3.0, 2.2], [3, 9, 1], pivot=[-2.5, 12, 2.2], rotation=[14, 0, -6])
    b.paint_shaded_box(uv_tail, 3, 9, 1, navy, pattern="cloth", trim_color=cyan[2])
    b.add_cube("armorBody", [1, 3.0, 2.2], [3, 9, 1], pivot=[2.5, 12, 2.2], rotation=[14, 0, 6], mirror=True, uv=uv_tail)

    # --- ARMS: Slim Arcane Sleeves + Slanted Shoulder Mantle (NO bulky pauldrons) ---
    uv_arm = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.38)
    b.paint_shaded_box(uv_arm, 4, 12, 4, navy, pattern="cloth", trim_color=cyan[2], stripe_y=18)
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.38, mirror=True, uv=uv_arm)

    uv_sh = b.add_cube("armorRightArm", [-9.2, 20.5, -2.5], [5, 3, 5], pivot=[-6.5, 22, 0], rotation=[0, 0, -20])
    b.paint_shaded_box(uv_sh, 5, 3, 5, cyan, pattern="cloth")
    b.add_cube("armorLeftArm", [4.2, 20.5, -2.5], [5, 3, 5], pivot=[6.5, 22, 0], rotation=[0, 0, 20], mirror=True, uv=uv_sh)

    # --- LEGS & BOOTS: Slim Wrapped Greaves + Cyan Trim Boots ---
    uv_leg = b.add_cube("armorRightLeg", [-4, 0, -2], [4, 12, 4], inflate=0.30)
    b.paint_shaded_box(uv_leg, 4, 12, 4, navy, pattern="cloth", trim_color=leather[2], stripe_y=8)
    b.add_cube("armorLeftLeg", [0, 0, -2], [4, 12, 4], inflate=0.30, mirror=True, uv=uv_leg)

    uv_boot = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.55)
    b.paint_shaded_box(uv_boot, 4, 12, 4, leather, pattern="cloth", trim_color=cyan[2], stripe_y=14)
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.55, mirror=True, uv=uv_boot)

    b.save()


# =====================================================================
# 1.3 PRISMATIC BRIMSTONE — Heavy Dark Horned Juggernaut (Ref: Dark Horned)
# =====================================================================

def build_prismatic():
    b = ArmorBuilder("prismatic")
    obsidian = [(14, 12, 16, 255), (26, 22, 30, 255), (42, 36, 48, 255), (64, 56, 72, 255)]
    magma = [(160, 32, 10, 255), (235, 72, 16, 255), (255, 136, 28, 255), (255, 220, 96, 255)]
    prism = [(108, 26, 152, 255), (172, 48, 224, 255), (218, 96, 255, 255), (246, 182, 255, 255)]
    pal = {
        ".": (0, 0, 0, 0),
        "0": obsidian[0], "1": obsidian[1], "2": obsidian[2], "3": obsidian[3],
        "e": magma[1], "E": magma[3], "p": prism[1], "P": prism[2],
    }
    glow_k = {"e", "E", "p", "P"}

    # --- HEAD: Sculpted Dark Beast-Knight Helm + HUGE 3D Segmented Horns ---
    uv_hi = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.45)
    b.paint_shaded_box(uv_hi, 8, 8, 8, obsidian, pattern="volcanic", glow_color=magma[1])
    b.blit_face(box_faces(*uv_hi, 8, 8, 8)["north"], [
        "1111111111111111",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "100eEE0000EEe001",
        "1000eE0000Ee0001",
        "1000000000000001",
        "1000000000000001",
        "1000000000000001",
        "1001110000111001",
        "1001210000121001",
        "1001110000111001",
        "1111111111111111",
        "1111111111111111",
        "1111111111111111",
    ], pal, glow_k)

    # Flared 3D brow ridge & heavy jaw plates
    uv_brow = b.add_cube("armorHead", [-5.5, 29.8, -5.5], [11, 3, 5], pivot=[0, 31, -3], rotation=[14, 0, 0])
    b.paint_shaded_box(uv_brow, 11, 3, 5, obsidian, pattern="plate", trim_color=obsidian[3])
    uv_jaw = b.add_cube("armorHead", [-5.0, 23.2, -5.2], [3, 4, 5])
    b.paint_shaded_box(uv_jaw, 3, 4, 5, obsidian, pattern="plate")
    b.add_cube("armorHead", [2.0, 23.2, -5.2], [3, 4, 5], mirror=True, uv=uv_jaw)

    # HUGE 3D Multi-Segmented Curved Demon/Bull Horns (rising 11 units!)
    uv_h1 = b.add_cube("armorHead", [-6.5, 31.5, -1.5], [3, 5, 3], pivot=[-5, 32, 0], rotation=[-8, 0, -20])
    b.paint_shaded_box(uv_h1, 3, 5, 3, obsidian, pattern="volcanic", glow_color=magma[1])
    b.add_cube("armorHead", [3.5, 31.5, -1.5], [3, 5, 3], pivot=[5, 32, 0], rotation=[-8, 0, 20], mirror=True, uv=uv_h1)

    uv_h2 = b.add_cube("armorHead", [-7.5, 35.8, -2.5], [2, 5, 2], pivot=[-6.5, 36, -1.5], rotation=[18, 0, -8])
    b.paint_shaded_box(uv_h2, 2, 5, 2, obsidian, pattern="volcanic", glow_color=magma[2])
    b.add_cube("armorHead", [5.5, 35.8, -2.5], [2, 5, 2], pivot=[6.5, 36, -1.5], rotation=[18, 0, 8], mirror=True, uv=uv_h2)

    uv_h3 = b.add_cube("armorHead", [-7.2, 40.0, -4.2], [1, 4, 2], pivot=[-6.7, 40, -3.2], rotation=[34, 0, 10])
    b.paint_shaded_box(uv_h3, 1, 4, 2, magma, pattern="crystal", glow_color=magma[2])
    b.add_cube("armorHead", [6.2, 40.0, -4.2], [1, 4, 2], pivot=[6.7, 40, -3.2], rotation=[34, 0, -10], mirror=True, uv=uv_h3)

    # --- BODY: Massive Sculpted Juggernaut Breastplate + Cracked Magma + Prism Core ---
    uv_bi = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.55)
    b.paint_shaded_box(uv_bi, 8, 12, 4, obsidian, pattern="volcanic", glow_color=magma[1])
    # Sculpted angular chest armor block + amethyst prism heart
    uv_chest = b.add_cube("armorBody", [-4.5, 15.5, -3.6], [9, 7, 2], pivot=[0, 19, -2.6], rotation=[8, 0, 0])
    b.paint_shaded_box(uv_chest, 9, 7, 2, obsidian, pattern="volcanic", glow_color=magma[2])
    uv_core = b.add_cube("armorBody", [-1.5, 17.5, -4.4], [3, 3, 1])
    b.paint_shaded_box(uv_core, 3, 3, 1, prism, pattern="crystal", glow_color=prism[2])
    # Wide heavy armored tasset plates over hips
    uv_tas = b.add_cube("armorBody", [-5.5, 7.5, -3.0], [4, 6, 6], pivot=[-4.5, 13, 0], rotation=[0, 0, 10])
    b.paint_shaded_box(uv_tas, 4, 6, 6, obsidian, pattern="volcanic", glow_color=magma[1])
    b.add_cube("armorBody", [1.5, 7.5, -3.0], [4, 6, 6], pivot=[4.5, 13, 0], rotation=[0, 0, -10], mirror=True, uv=uv_tas)

    # --- ARMS: Oversized 2-Tier Juggernaut Pauldrons ---
    uv_arm = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.42)
    b.paint_shaded_box(uv_arm, 4, 12, 4, obsidian, pattern="volcanic", glow_color=magma[1])
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.42, mirror=True, uv=uv_arm)

    uv_p1 = b.add_cube("armorRightArm", [-11.5, 18.5, -3.5], [7, 5, 7], pivot=[-7.5, 21, 0], rotation=[0, 0, -12])
    b.paint_shaded_box(uv_p1, 7, 5, 7, obsidian, pattern="volcanic", glow_color=magma[1])
    b.add_cube("armorLeftArm", [4.5, 18.5, -3.5], [7, 5, 7], pivot=[7.5, 21, 0], rotation=[0, 0, 12], mirror=True, uv=uv_p1)

    uv_p2 = b.add_cube("armorRightArm", [-12.2, 22.8, -3.0], [6, 3, 6], pivot=[-8.5, 23.5, 0], rotation=[0, 0, -24])
    b.paint_shaded_box(uv_p2, 6, 3, 6, obsidian, pattern="plate", trim_color=magma[2])
    b.add_cube("armorLeftArm", [6.2, 22.8, -3.0], [6, 3, 6], pivot=[8.5, 23.5, 0], rotation=[0, 0, 24], mirror=True, uv=uv_p2)

    # --- LEGS & BOOTS: Slim Dark Greaves (making upper body look huge!) ---
    uv_leg = b.add_cube("armorRightLeg", [-4, 0, -2], [4, 12, 4], inflate=0.28)
    b.paint_shaded_box(uv_leg, 4, 12, 4, obsidian, pattern="plate", trim_color=obsidian[3])
    b.add_cube("armorLeftLeg", [0, 0, -2], [4, 12, 4], inflate=0.28, mirror=True, uv=uv_leg)

    uv_boot = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.52)
    b.paint_shaded_box(uv_boot, 4, 12, 4, obsidian, pattern="volcanic", glow_color=magma[1])
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.52, mirror=True, uv=uv_boot)

    b.save()


# =====================================================================
# 1.4 DRAGONSCALE — Reptilian Dragon-Skull, Snout, Dorsal Spines & Claws
# =====================================================================

def build_dragonscale():
    b = ArmorBuilder("dragonscale")
    emerald = [(8, 28, 18, 255), (16, 56, 34, 255), (28, 96, 56, 255), (52, 152, 88, 255)]
    bone_iron = [(32, 38, 44, 255), (58, 68, 78, 255), (96, 110, 124, 255), (148, 166, 182, 255)]
    amber = [(255, 132, 12, 255), (255, 224, 82, 255)]
    pal = {
        ".": (0, 0, 0, 0),
        "0": emerald[0], "1": emerald[1], "2": emerald[2], "3": emerald[3],
        "e": amber[0], "E": amber[1],
    }
    glow_k = {"e", "E"}

    # --- HEAD: Dragon Cranium Helm + Protruding 3D Snout + Swept-Back Horns ---
    uv_hi = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.45)
    b.paint_shaded_box(uv_hi, 8, 8, 8, emerald, pattern="scales")
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
        "1001111111111001",
        "1001222222221001",
        "1001111111111001",
        "1111111111111111",
        "1111111111111111",
        "1111111111111111",
    ], pal, glow_k)

    # Protruding 3D Dragon Snout & Lower Jaw
    uv_snout = b.add_cube("armorHead", [-3, 27.5, -8.5], [6, 3, 5], pivot=[0, 29, -4], rotation=[8, 0, 0])
    b.paint_shaded_box(uv_snout, 6, 3, 5, emerald, pattern="scales")
    uv_ljaw = b.add_cube("armorHead", [-2.5, 25.0, -7.2], [5, 2, 4])
    b.paint_shaded_box(uv_ljaw, 5, 2, 4, bone_iron, pattern="plate")

    # Swept-back 3D Dragon Horns
    uv_dh1 = b.add_cube("armorHead", [-5.5, 30.5, -1.0], [2, 3, 6], pivot=[-4.5, 31.5, 0], rotation=[28, -16, -10])
    b.paint_shaded_box(uv_dh1, 2, 3, 6, bone_iron, pattern="plate")
    b.add_cube("armorHead", [3.5, 30.5, -1.0], [2, 3, 6], pivot=[4.5, 31.5, 0], rotation=[28, 16, 10], mirror=True, uv=uv_dh1)

    uv_dh2 = b.add_cube("armorHead", [-6.5, 32.5, 4.0], [2, 2, 5], pivot=[-5.5, 33, 4.5], rotation=[40, -20, -10])
    b.paint_shaded_box(uv_dh2, 2, 2, 5, bone_iron, pattern="plate", glow_color=amber[0])
    b.add_cube("armorHead", [4.5, 32.5, 4.0], [2, 2, 5], pivot=[5.5, 33, 4.5], rotation=[40, 20, 10], mirror=True, uv=uv_dh2)

    # --- BODY: Scaled Cuirass + 5-Spike Central Dorsal Dragon Spine Ridge ---
    uv_bi = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.45)
    b.paint_shaded_box(uv_bi, 8, 12, 4, emerald, pattern="scales")
    uv_sc = b.add_cube("armorBody", [-3.5, 16.0, -3.2], [7, 6, 1])
    b.paint_shaded_box(uv_sc, 7, 6, 1, emerald, pattern="scales", trim_color=bone_iron[2])
    # 5 angled 3D dorsal dragon spines down the back!
    uv_sp = b.add_cube("armorBody", [-1, 21.0, 2.2], [2, 3, 4], pivot=[0, 22, 2.5], rotation=[-22, 0, 0])
    b.paint_shaded_box(uv_sp, 2, 3, 4, bone_iron, pattern="plate")
    for sy in (18.0, 15.0, 12.0, 9.0):
        b.add_cube("armorBody", [-1, sy, 2.2], [2, 3, 4], pivot=[0, sy + 1, 2.5], rotation=[-22, 0, 0], uv=uv_sp)

    # --- ARMS: Scaled Sleeves + Jagged Pauldrons + Forearm Dragon Claws ---
    uv_arm = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.38)
    b.paint_shaded_box(uv_arm, 4, 12, 4, emerald, pattern="scales")
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.38, mirror=True, uv=uv_arm)

    uv_dp = b.add_cube("armorRightArm", [-10.0, 19.5, -3.0], [6, 5, 6], pivot=[-7, 22, 0], rotation=[0, 0, -18])
    b.paint_shaded_box(uv_dp, 6, 5, 6, emerald, pattern="scales", trim_color=bone_iron[2])
    b.add_cube("armorLeftArm", [4.0, 19.5, -3.0], [6, 5, 6], pivot=[7, 22, 0], rotation=[0, 0, 18], mirror=True, uv=uv_dp)

    # 3D Forearm Dragon Claw Blades
    uv_claw = b.add_cube("armorRightArm", [-9.5, 11.5, -1.0], [1, 6, 3], pivot=[-8.5, 15, 0.5], rotation=[-12, 0, -10])
    b.paint_shaded_box(uv_claw, 1, 6, 3, bone_iron, pattern="plate")
    b.add_cube("armorLeftArm", [8.5, 11.5, -1.0], [1, 6, 3], pivot=[8.5, 15, 0.5], rotation=[-12, 0, 10], mirror=True, uv=uv_claw)

    # --- LEGS & BOOTS: Scaled Greaves + Reptilian Drake Toe-Claws ---
    uv_leg = b.add_cube("armorRightLeg", [-4, 0, -2], [4, 12, 4], inflate=0.30)
    b.paint_shaded_box(uv_leg, 4, 12, 4, emerald, pattern="scales")
    b.add_cube("armorLeftLeg", [0, 0, -2], [4, 12, 4], inflate=0.30, mirror=True, uv=uv_leg)

    uv_boot = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.55)
    b.paint_shaded_box(uv_boot, 4, 12, 4, emerald, pattern="scales", trim_color=bone_iron[2])
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.55, mirror=True, uv=uv_boot)

    uv_toe = b.add_cube("armorRightBoot", [-3.5, -0.4, -4.5], [3, 2, 3])
    b.paint_shaded_box(uv_toe, 3, 2, 3, bone_iron, pattern="plate")
    b.add_cube("armorLeftBoot", [0.5, -0.4, -4.5], [3, 2, 3], mirror=True, uv=uv_toe)

    b.save()


# =====================================================================
# 1.5 TWIN-STAR — Grand Winged Halo Celestial Seraph (Ref: Winged Halo + Winged Purple/Teal)
# =====================================================================

def build_twin_star():
    b = ArmorBuilder("twin_star")
    white = [(148, 158, 180, 255), (202, 212, 232, 255), (234, 240, 252, 255), (255, 255, 255, 255)]
    indigo = [(18, 12, 42, 255), (34, 24, 76, 255), (56, 42, 114, 255), (84, 66, 156, 255)]
    gold = [(154, 102, 14, 255), (218, 158, 28, 255), (248, 206, 68, 255), (255, 242, 148, 255)]
    pal = {
        ".": (0, 0, 0, 0),
        "0": indigo[0], "1": indigo[1], "2": indigo[2],
        "w": white[1], "W": white[3],
        "g": gold[1], "G": gold[2], "h": gold[3],
        "e": (104, 238, 255, 255),
        "E": (212, 255, 255, 255),
    }
    glow_k = {"e", "E"}

    # --- HEAD: Sleek Platinum Seraph Knight Helm + MASSIVE Vertical Celestial Halo ---
    uv_hi = b.add_cube("armorHead", [-4, 24, -4], [8, 8, 8], inflate=0.45)
    b.paint_shaded_box(uv_hi, 8, 8, 8, white, pattern="plate", trim_color=gold[2])
    b.blit_face(box_faces(*uv_hi, 8, 8, 8)["north"], [
        "wwwwWWGGGGWWwwww",
        "wwwWWGhhhhGWWwww",
        "wwWWgGGGGGGgWWww",
        "w00000gGGg00000w",
        "w00000gGGg00000w",
        "w00eEEgGGgEEe00w",
        "w000eEgGGgEe000w",
        "w00000gGGg00000w",
        "w00000gGGg00000w",
        "w000000GG000000w",
        "ww00000GG00000ww",
        "www0000gg0000www",
        "wwwwWWwwwwWWwwww",
        "wwwwWWwwwwWWwwww",
        "wwwwwwwwwwwwwwww",
        "wwwwwwwwwwwwwwww",
    ], pal, glow_k)

    # MASSIVE 20x20 Vertical Radiant Halo Ring behind the head!
    uv_halo = b.add_cube("armorHead", [-10, 22.0, 5.2], [20, 20, 0], pivot=[0, 32, 5.2], rotation=[-10, 0, 0])
    halo_sprite = [
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
    ]
    b.paint_plane_z(uv_halo, 20, 20, halo_sprite, pal, glow_k)

    # --- BODY: Royal Platinum & Indigo Surcoat + GIANT Spread Celestial Wings! ---
    uv_bi = b.add_cube("armorBody", [-4, 12, -2], [8, 12, 4], inflate=0.45)
    b.paint_shaded_box(uv_bi, 8, 12, 4, indigo, pattern="cloth", trim_color=gold[2], stripe_y=20)
    uv_chest = b.add_cube("armorBody", [-4.2, 15.0, -2.8], [8, 7, 5])
    b.paint_shaded_box(uv_chest, 8, 7, 5, white, pattern="plate", trim_color=gold[2])
    # Dual Cyan+Gold Twin-Star Chest Core
    uv_star = b.add_cube("armorBody", [-1.5, 17.5, -3.4], [3, 3, 1])
    b.paint_shaded_box(uv_star, 3, 3, 1, [pal["e"], pal["E"], pal["E"], pal["h"]], pattern="crystal", glow_color=pal["E"])

    # GIANT 20x24 Spread Celestial Wings on each side (40+ units wide!)
    wing_sprite = [
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
    ]
    uv_wing = b.add_cube("armorBody", [-22.0, 10.0, 4.2], [20, 24, 0], pivot=[-2.0, 20.0, 4.2], rotation=[-8, 22, -6])
    b.paint_plane_z(uv_wing, 20, 24, wing_sprite, pal, glow_k)
    b.add_cube("armorBody", [2.0, 10.0, 4.2], [20, 24, 0], pivot=[2.0, 20.0, 4.2], rotation=[-8, -22, 6], mirror=True, uv=uv_wing)

    # Flowing Royal Indigo & Gold Robe Skirt
    uv_skirt = b.add_cube("armorBody", [-4.5, 4.0, -2.5], [9, 8, 5], inflate=0.25)
    b.paint_shaded_box(uv_skirt, 9, 8, 5, indigo, pattern="cloth", trim_color=gold[2], stripe_y=14)

    # --- ARMS: Platinum Sleeves + Winged Gold-Trimmed Pauldrons ---
    uv_arm = b.add_cube("armorRightArm", [-8, 12, -2], [4, 12, 4], inflate=0.38)
    b.paint_shaded_box(uv_arm, 4, 12, 4, white, pattern="plate", trim_color=gold[2])
    b.add_cube("armorLeftArm", [4, 12, -2], [4, 12, 4], inflate=0.38, mirror=True, uv=uv_arm)

    uv_tp = b.add_cube("armorRightArm", [-10.0, 19.8, -3.0], [6, 4, 6], pivot=[-7, 22, 0], rotation=[0, 0, -20])
    b.paint_shaded_box(uv_tp, 6, 4, 6, white, pattern="plate", trim_color=gold[2])
    b.add_cube("armorLeftArm", [4.0, 19.8, -3.0], [6, 4, 6], pivot=[7, 22, 0], rotation=[0, 0, 20], mirror=True, uv=uv_tp)

    # --- LEGS & BOOTS: Indigo & Platinum Greaves + Gold-Trimmed Seraph Boots ---
    uv_leg = b.add_cube("armorRightLeg", [-4, 0, -2], [4, 12, 4], inflate=0.28)
    b.paint_shaded_box(uv_leg, 4, 12, 4, indigo, pattern="cloth", trim_color=gold[2])
    b.add_cube("armorLeftLeg", [0, 0, -2], [4, 12, 4], inflate=0.28, mirror=True, uv=uv_leg)

    uv_boot = b.add_cube("armorRightBoot", [-4, 0, -2], [4, 12, 4], inflate=0.52)
    b.paint_shaded_box(uv_boot, 4, 12, 4, white, pattern="plate", trim_color=gold[2])
    b.add_cube("armorLeftBoot", [0, 0, -2], [4, 12, 4], inflate=0.52, mirror=True, uv=uv_boot)

    b.save()


def main():
    build_starlight()
    build_prismatic()
    build_dragonscale()
    build_twin_star()


if __name__ == "__main__":
    main()
