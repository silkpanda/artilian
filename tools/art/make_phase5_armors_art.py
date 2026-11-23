"""Generates bespoke 3D GeckoLib models, 128x128 shaded textures, and glowmasks
for Phase 5's Dragonscale Armor and Twin-Star Endgame Armor.

Usage:
    python tools/art/make_phase5_armors_art.py
"""
from __future__ import annotations

import json
import math
from pathlib import Path
from PIL import Image

ASSETS = Path("src/main/resources/assets/artilian")
GEO_DIR = ASSETS / "geo" / "item" / "armor"
TEX_DIR = ASSETS / "textures" / "item" / "armor"


def _clamp(v: float) -> int:
    return max(0, min(255, int(round(v))))


def _lerp_rgb(c0: tuple[int, int, int], c1: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    t = max(0.0, min(1.0, t))
    return (
        _clamp(c0[0] + (c1[0] - c0[0]) * t),
        _clamp(c0[1] + (c1[1] - c0[1]) * t),
        _clamp(c0[2] + (c1[2] - c0[2]) * t),
    )


def _shade_palette(stops: list[tuple[int, int, int]], t: float) -> tuple[int, int, int]:
    t = max(0.0, min(1.0, t))
    n = len(stops) - 1
    scaled = t * n
    idx = min(n - 1, int(scaled))
    frac = scaled - idx
    return _lerp_rgb(stops[idx], stops[idx + 1], frac)


def _box_faces(u: int, v: int, w: int, h: int, d: int) -> dict[str, tuple[int, int, int, int]]:
    return {
        "up": (u + d, v, w, d),
        "down": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "south": (u + d + w + d, v + d, w, h),
    }


def _paint_cube(
    img: Image.Image,
    glow: Image.Image,
    u: int,
    v: int,
    w: int,
    h: int,
    d: int,
    stops: list[tuple[int, int, int]],
    *,
    trim: tuple[int, int, int] | None = None,
    glow_color: tuple[int, int, int] | None = None,
    pattern: str = "plate",
) -> None:
    """Paint all 6 faces of a Bedrock box-UV cube with multi-stop directional shading."""
    px = img.load()
    gpx = glow.load()
    face_bias = {
        "up": 0.85,
        "north": 0.68,
        "east": 0.56,
        "west": 0.52,
        "south": 0.44,
        "down": 0.25,
    }

    for face_name, (fx, fy, fw, fh) in _box_faces(u, v, w, h, d).items():
        bias = face_bias[face_name]
        for ly in range(fh):
            for lx in range(fw):
                nx = (lx + 0.5) / max(1, fw)
                ny = (ly + 0.5) / max(1, fh)
                edge = (lx == 0 or ly == 0 or lx == fw - 1 or ly == fh - 1) and (fw >= 4 and fh >= 4)

                # Directional gradient + subtle bevel
                t = bias * 0.65 + (1.0 - ny) * 0.25 + (1.0 - abs(nx - 0.5) * 1.4) * 0.15

                if pattern == "scales":
                    # Overlapping diamond dragon-scale shading
                    cell = ((lx + (ly // 2)) % 2) * 0.14 - ((ly % 2) * 0.08)
                    t += cell
                elif pattern == "star_plate":
                    # Subtle cosmic facet / metallic sheen
                    diag = math.sin((lx * 0.9 + ly * 0.7)) * 0.10
                    t += diag
                elif pattern == "horn":
                    # Ridged horn rings
                    ridge = 0.16 if (ly % 2 == 0) else -0.10
                    t += ridge + (1.0 - ny) * 0.18
                elif pattern == "glow_gem":
                    dist = math.hypot(nx - 0.5, ny - 0.5)
                    t = 0.95 - dist * 0.85

                if edge and trim is not None:
                    base = _lerp_rgb(trim, _shade_palette(stops, t), 0.30)
                else:
                    base = _shade_palette(stops, t)

                px[fx + lx, fy + ly] = (*base, 255)

                # Optional emissive glow accents
                if glow_color is not None:
                    is_glow_px = False
                    if pattern == "glow_gem" and math.hypot(nx - 0.5, ny - 0.5) < 0.42:
                        is_glow_px = True
                    elif pattern == "scales" and fw >= 6 and fh >= 6 and lx in (fw // 2,) and ly in (fh // 2, fh // 2 - 1):
                        is_glow_px = True
                    elif pattern == "star_plate" and fw >= 5 and fh >= 5 and (lx == fw // 2 and ly == fh // 2):
                        is_glow_px = True
                    if is_glow_px:
                        gpx[fx + lx, fy + ly] = (*glow_color, 255)


class AtlasPacker:
    """Packs Bedrock box-UV islands onto a 128x128 sheet without overlaps."""

    def __init__(self, width: int = 128, height: int = 128) -> None:
        self.width = width
        self.height = height
        self.x = 0
        self.y = 0
        self.row_h = 0

    def alloc(self, w: int, h: int, d: int) -> tuple[int, int]:
        rw = 2 * (w + d)
        rh = h + d
        if self.x + rw > self.width:
            self.x = 0
            self.y += self.row_h + 1
            self.row_h = 0
        if self.y + rh > self.height:
            raise RuntimeError(f"Atlas overflow ({self.width}x{self.height}) at cube {(w, h, d)}")
        u, v = self.x, self.y
        self.x += rw + 1
        self.row_h = max(self.row_h, rh)
        return u, v


def _make_cube(
    packer: AtlasPacker,
    img: Image.Image,
    glow: Image.Image,
    origin: list[float],
    size: list[int],
    stops: list[tuple[int, int, int]],
    *,
    inflate: float | None = None,
    pivot: list[float] | None = None,
    rotation: list[float] | None = None,
    mirror: bool = False,
    uv_override: tuple[int, int] | None = None,
    trim: tuple[int, int, int] | None = None,
    glow_color: tuple[int, int, int] | None = None,
    pattern: str = "plate",
) -> tuple[dict, tuple[int, int]]:
    w, h, d = size
    if uv_override is None:
        u, v = packer.alloc(w, h, d)
        _paint_cube(img, glow, u, v, w, h, d, stops, trim=trim, glow_color=glow_color, pattern=pattern)
    else:
        u, v = uv_override

    cube: dict = {
        "origin": origin,
        "size": size,
        "uv": [u, v],
    }
    if inflate is not None:
        cube["inflate"] = inflate
    if pivot is not None:
        cube["pivot"] = pivot
    if rotation is not None:
        cube["rotation"] = rotation
    if mirror:
        cube["mirror"] = True
    return cube, (u, v)


def build_dragonscale_armor() -> None:
    """Dragon-Knight / Wyvern-Hunter armor with swept horns, dorsal spines, wing pauldrons & talons."""
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    packer = AtlasPacker(128, 128)

    # Rich Dragon Jungle palette: deep emerald-obsidian scales + volcanic amber-gold trim
    scales = [(18, 42, 30), (32, 76, 50), (54, 128, 76), (92, 178, 102), (144, 224, 138)]
    dark_plate = [(24, 28, 30), (42, 48, 52), (66, 76, 80), (96, 108, 112)]
    amber_gold = [(128, 52, 12), (196, 98, 22), (240, 154, 42), (255, 214, 96)]
    horn_bone = [(48, 34, 24), (92, 68, 46), (148, 114, 78), (208, 174, 126)]
    ember_glow = (255, 168, 44)
    trim_gold = (224, 146, 38)

    # Head: sculpted dragon helm + brow visor + snout crest + swept-back 2-segment horns + dorsal fin
    head_cubes = []
    c, _ = _make_cube(packer, img, glow, [-4, 24, -4], [8, 8, 8], scales, inflate=0.55, trim=trim_gold, glow_color=ember_glow, pattern="scales")
    head_cubes.append(c)
    # Brow ridge / dragon snout crest + cheek-plates
    c, _ = _make_cube(packer, img, glow, [-3, 29, -5.4], [6, 3, 2], amber_gold, pivot=[0, 30, -4.5], rotation=[10, 0, 0], pattern="plate")
    head_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-1, 31, -5.6], [2, 4, 3], amber_gold, pivot=[0, 31, -4.5], rotation=[22, 0, 0], glow_color=ember_glow, pattern="glow_gem")
    head_cubes.append(c)
    # Glowing dragon visor slit across the front brow
    c, _ = _make_cube(packer, img, glow, [-3, 27.5, -4.9], [6, 1, 1], amber_gold, glow_color=ember_glow, pattern="glow_gem")
    head_cubes.append(c)
    # Angled Dragon Cheek-Plates (Left + Mirrored Right)
    c, uv_cheek = _make_cube(packer, img, glow, [3.4, 24.2, -4.6], [2, 4, 4], dark_plate, pivot=[4.4, 26, -2.6], rotation=[0, 16, -8], trim=trim_gold, pattern="scales")
    head_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-5.4, 24.2, -4.6], [2, 4, 4], dark_plate, pivot=[-4.4, 26, -2.6], rotation=[0, -16, 8], mirror=True, uv_override=uv_cheek)
    head_cubes.append(c)
    # Dorsal head fin
    c, _ = _make_cube(packer, img, glow, [-1, 32, -2], [2, 3, 6], amber_gold, pivot=[0, 32, 1], rotation=[-12, 0, 0], pattern="horn")
    head_cubes.append(c)
    # Swept-back Dragon Horns (Left + Mirrored Right)
    c, uv_horn1 = _make_cube(packer, img, glow, [3.2, 29, -1.5], [2, 6, 2], horn_bone, pivot=[4.2, 30, -0.5], rotation=[-34, 16, 18], pattern="horn")
    head_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-5.2, 29, -1.5], [2, 6, 2], horn_bone, pivot=[-4.2, 30, -0.5], rotation=[-34, -16, -18], mirror=True, uv_override=uv_horn1)
    head_cubes.append(c)
    c, uv_horn2 = _make_cube(packer, img, glow, [4.6, 33.5, 1.2], [2, 5, 2], amber_gold, pivot=[5.6, 34, 2.2], rotation=[-54, 20, 22], pattern="horn")
    head_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-6.6, 33.5, 1.2], [2, 5, 2], amber_gold, pivot=[-5.6, 34, 2.2], rotation=[-54, -20, -22], mirror=True, uv_override=uv_horn2)
    head_cubes.append(c)

    # Body: dragonscale cuirass + layered chestplate + glowing ember core + 3 dorsal back spines + waist belt
    body_cubes = []
    c, _ = _make_cube(packer, img, glow, [-4, 12, -2], [8, 12, 4], scales, inflate=0.6, trim=trim_gold, glow_color=ember_glow, pattern="scales")
    body_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-3, 16, -3.2], [6, 6, 1], dark_plate, trim=trim_gold, pattern="plate")
    body_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-1, 18, -3.8], [2, 2, 1], amber_gold, glow_color=ember_glow, pattern="glow_gem")
    body_cubes.append(c)
    # Waist belt & buckle
    c, _ = _make_cube(packer, img, glow, [-4.5, 12, -2.8], [9, 2, 5], dark_plate, trim=trim_gold, pattern="plate")
    body_cubes.append(c)
    # 3 Dorsal back spines
    for idx, (sy, rot) in enumerate([(20, -28), (16.5, -34), (13, -40)]):
        c, _ = _make_cube(packer, img, glow, [-1, sy, 2.4], [2, 3, 4], amber_gold, pivot=[0, sy + 1.5, 2.4], rotation=[rot, 0, 0], pattern="horn")
        body_cubes.append(c)

    # Arms: Winged/spiked dragon pauldrons + forearm bracers + wrist fins
    c_arm_r, uv_arm = _make_cube(packer, img, glow, [-8, 12, -2], [4, 12, 4], dark_plate, inflate=0.5, trim=trim_gold, pattern="scales")
    c_arm_l, _ = _make_cube(packer, img, glow, [4, 12, -2], [4, 12, 4], dark_plate, inflate=0.5, mirror=True, uv_override=uv_arm)

    c_pauldron_r, uv_p1 = _make_cube(packer, img, glow, [-9.5, 19.5, -2.5], [5, 5, 5], scales, pivot=[-6, 22, 0], rotation=[0, 0, -16], trim=trim_gold, glow_color=ember_glow, pattern="scales")
    c_pauldron_l, _ = _make_cube(packer, img, glow, [4.5, 19.5, -2.5], [5, 5, 5], scales, pivot=[6, 22, 0], rotation=[0, 0, 16], mirror=True, uv_override=uv_p1)

    c_spike_r, uv_sp = _make_cube(packer, img, glow, [-10.2, 23.0, -1.0], [2, 4, 2], amber_gold, pivot=[-8.5, 23, 0], rotation=[0, 0, -34], pattern="horn")
    c_spike_l, _ = _make_cube(packer, img, glow, [8.2, 23.0, -1.0], [2, 4, 2], amber_gold, pivot=[8.5, 23, 0], rotation=[0, 0, 34], mirror=True, uv_override=uv_sp)

    c_bracer_r, uv_br = _make_cube(packer, img, glow, [-8.6, 13.0, -2.5], [5, 5, 5], scales, trim=trim_gold, pattern="scales")
    c_bracer_l, _ = _make_cube(packer, img, glow, [3.6, 13.0, -2.5], [5, 5, 5], scales, mirror=True, uv_override=uv_br)

    # Legs: Dragonscale cuisses + angled knee guards + hip tassets
    c_leg_l, uv_leg = _make_cube(packer, img, glow, [0, 0, -2], [4, 12, 4], dark_plate, inflate=0.35, trim=trim_gold, pattern="scales")
    c_leg_r, _ = _make_cube(packer, img, glow, [-4, 0, -2], [4, 12, 4], dark_plate, inflate=0.35, mirror=True, uv_override=uv_leg)

    c_tasset_l, uv_tas = _make_cube(packer, img, glow, [3.6, 7.0, -2.5], [1, 5, 5], scales, pivot=[4, 11, 0], rotation=[0, 0, 10], trim=trim_gold, pattern="scales")
    c_tasset_r, _ = _make_cube(packer, img, glow, [-4.6, 7.0, -2.5], [1, 5, 5], scales, pivot=[-4, 11, 0], rotation=[0, 0, -10], mirror=True, uv_override=uv_tas)

    c_knee_l, uv_kn = _make_cube(packer, img, glow, [0.5, 4.5, -3.2], [3, 3, 1], amber_gold, glow_color=ember_glow, pattern="plate")
    c_knee_r, _ = _make_cube(packer, img, glow, [-3.5, 4.5, -3.2], [3, 3, 1], amber_gold, mirror=True, uv_override=uv_kn)

    # Boots: Armored greaves + 2-toed dragon talons + heel wing-spurs
    c_boot_l, uv_boot = _make_cube(packer, img, glow, [0, 0, -2], [4, 12, 4], scales, inflate=0.68, trim=trim_gold, pattern="scales")
    c_boot_r, _ = _make_cube(packer, img, glow, [-4, 0, -2], [4, 12, 4], scales, inflate=0.68, mirror=True, uv_override=uv_boot)

    c_talon_l, uv_tal = _make_cube(packer, img, glow, [0.5, 0.0, -4.0], [3, 2, 2], horn_bone, pattern="horn")
    c_talon_r, _ = _make_cube(packer, img, glow, [-3.5, 0.0, -4.0], [3, 2, 2], horn_bone, mirror=True, uv_override=uv_tal)

    c_spur_l, uv_spur = _make_cube(packer, img, glow, [3.8, 1.5, -1.5], [1, 5, 4], amber_gold, pivot=[4.3, 3, 0.5], rotation=[-18, 0, 12], pattern="horn")
    c_spur_r, _ = _make_cube(packer, img, glow, [-4.8, 1.5, -1.5], [1, 5, 4], amber_gold, pivot=[-4.3, 3, 0.5], rotation=[-18, 0, -12], mirror=True, uv_override=uv_spur)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": "geometry.dragonscale_armor",
                "texture_width": 128,
                "texture_height": 128,
                "visible_bounds_width": 3,
                "visible_bounds_height": 3.5,
                "visible_bounds_offset": [0, 1.5, 0],
            },
            "bones": [
                {"name": "bipedHead", "pivot": [0, 24, 0]},
                {"name": "armorHead", "parent": "bipedHead", "pivot": [0, 24, 0], "cubes": head_cubes},
                {"name": "bipedBody", "pivot": [0, 24, 0]},
                {"name": "armorBody", "parent": "bipedBody", "pivot": [0, 24, 0], "cubes": body_cubes},
                {"name": "bipedRightArm", "pivot": [-5, 22, 0]},
                {"name": "armorRightArm", "parent": "bipedRightArm", "pivot": [-5, 22, 0], "cubes": [c_arm_r, c_pauldron_r, c_spike_r, c_bracer_r]},
                {"name": "bipedLeftArm", "pivot": [5, 22, 0]},
                {"name": "armorLeftArm", "parent": "bipedLeftArm", "pivot": [5, 22, 0], "cubes": [c_arm_l, c_pauldron_l, c_spike_l, c_bracer_l]},
                {"name": "bipedLeftLeg", "pivot": [2, 12, 0]},
                {"name": "armorLeftLeg", "parent": "bipedLeftLeg", "pivot": [2, 12, 0], "cubes": [c_leg_l, c_tasset_l, c_knee_l]},
                {"name": "armorLeftBoot", "parent": "bipedLeftLeg", "pivot": [2, 12, 0], "cubes": [c_boot_l, c_talon_l, c_spur_l]},
                {"name": "bipedRightLeg", "pivot": [-2, 12, 0]},
                {"name": "armorRightLeg", "parent": "bipedRightLeg", "pivot": [-2, 12, 0], "cubes": [c_leg_r, c_tasset_r, c_knee_r]},
                {"name": "armorRightBoot", "parent": "bipedRightLeg", "pivot": [-2, 12, 0], "cubes": [c_boot_r, c_talon_r, c_spur_r]},
            ],
        }],
    }

    GEO_DIR.mkdir(parents=True, exist_ok=True)
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    (GEO_DIR / "dragonscale_armor.geo.json").write_text(json.dumps(geo, indent=2) + "\n", encoding="utf-8")
    img.save(TEX_DIR / "dragonscale_armor.png")
    glow.save(TEX_DIR / "dragonscale_armor_glowmask.png")


def build_twin_star_armor() -> None:
    """Celestial Twin-Star Sovereign Armor uniting Eye-Realm Starlight & Dragon-Jungle Solar Gold."""
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    packer = AtlasPacker(128, 128)

    # Cosmic indigo-obsidian + Solar Gold + Starlight Teal accents
    cosmic_plate = [(20, 16, 44), (38, 32, 82), (66, 56, 134), (104, 88, 188), (152, 136, 232)]
    solar_gold = [(138, 82, 14), (202, 134, 28), (246, 192, 62), (255, 236, 138)]
    starlight_teal = [(18, 108, 108), (34, 172, 164), (72, 232, 218), (168, 255, 246)]
    royal_crimson = [(92, 18, 48), (154, 34, 78), (214, 68, 118)]
    trim_gold = (242, 186, 56)
    glow_cyan = (110, 255, 242)
    glow_gold = (255, 214, 96)

    # Head: Royal Starlight Helm + Third-Eye Diadem + Celestial Halo Crown + Swept Ear-Wings
    head_cubes = []
    c, _ = _make_cube(packer, img, glow, [-4, 24, -4], [8, 8, 8], cosmic_plate, inflate=0.58, trim=trim_gold, glow_color=glow_cyan, pattern="star_plate")
    head_cubes.append(c)
    # Brow diadem + All-Seer Third-Eye gem
    c, _ = _make_cube(packer, img, glow, [-4.5, 29.5, -5.0], [9, 2, 2], solar_gold, pattern="plate")
    head_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-1, 29.8, -5.6], [2, 2, 1], starlight_teal, glow_color=glow_cyan, pattern="glow_gem")
    head_cubes.append(c)
    # Celestial Halo Crown behind head
    c, _ = _make_cube(packer, img, glow, [-5, 32.5, 1.5], [10, 2, 2], solar_gold, pivot=[0, 32, 2.5], rotation=[-22, 0, 0], glow_color=glow_gold, pattern="glow_gem")
    head_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-1, 34.0, 2.0], [2, 4, 2], starlight_teal, pivot=[0, 34, 3.0], rotation=[-22, 0, 0], glow_color=glow_cyan, pattern="glow_gem")
    head_cubes.append(c)
    # Swept-back Celestial Ear-Wings (Left + Mirrored Right)
    c, uv_ew = _make_cube(packer, img, glow, [4.2, 27.5, -1.5], [1, 6, 5], starlight_teal, pivot=[4.7, 28, 0], rotation=[-18, 12, 24], trim=trim_gold, glow_color=glow_cyan, pattern="star_plate")
    head_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-5.2, 27.5, -1.5], [1, 6, 5], starlight_teal, pivot=[-4.7, 28, 0], rotation=[-18, -12, -24], mirror=True, uv_override=uv_ew)
    head_cubes.append(c)

    # Body: Starlight Cuirass + Twin-Star Reactor Core + Broad Mantle + Twin Celestial Wing-Blades
    body_cubes = []
    c, _ = _make_cube(packer, img, glow, [-4, 12, -2], [8, 12, 4], cosmic_plate, inflate=0.62, trim=trim_gold, glow_color=glow_cyan, pattern="star_plate")
    body_cubes.append(c)
    # Twin-Star Core (split gold + starlight gem)
    c, _ = _make_cube(packer, img, glow, [-2.5, 17, -3.3], [5, 5, 1], solar_gold, pattern="plate")
    body_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-1.5, 18, -3.9], [3, 3, 1], starlight_teal, glow_color=glow_cyan, pattern="glow_gem")
    body_cubes.append(c)
    # Royal crimson sash & belt
    c, _ = _make_cube(packer, img, glow, [-4.5, 12, -2.7], [9, 2, 5], royal_crimson, trim=trim_gold, pattern="plate")
    body_cubes.append(c)
    # Twin Celestial Wing-Blades on upper back (flared outward for unmistakable silhouette!)
    c, uv_wb1 = _make_cube(packer, img, glow, [2.5, 18.0, 2.8], [2, 12, 1], solar_gold, pivot=[3.0, 19, 3.3], rotation=[-20, -16, 42], trim=trim_gold, glow_color=glow_gold, pattern="star_plate")
    body_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-4.5, 18.0, 2.8], [2, 12, 1], solar_gold, pivot=[-3.0, 19, 3.3], rotation=[-20, 16, -42], mirror=True, uv_override=uv_wb1)
    body_cubes.append(c)
    c, uv_wb2 = _make_cube(packer, img, glow, [5.0, 16.0, 3.4], [2, 10, 1], starlight_teal, pivot=[5.5, 17, 3.9], rotation=[-24, -20, 62], glow_color=glow_cyan, pattern="glow_gem")
    body_cubes.append(c)
    c, _ = _make_cube(packer, img, glow, [-7.0, 16.0, 3.4], [2, 10, 1], starlight_teal, pivot=[-5.5, 17, 3.9], rotation=[-24, 20, -62], mirror=True, uv_override=uv_wb2)
    body_cubes.append(c)

    # Arms: Flared Star-Blade Pauldrons + Glowing Rune Gauntlets
    c_arm_r, uv_arm = _make_cube(packer, img, glow, [-8, 12, -2], [4, 12, 4], cosmic_plate, inflate=0.52, trim=trim_gold, pattern="star_plate")
    c_arm_l, _ = _make_cube(packer, img, glow, [4, 12, -2], [4, 12, 4], cosmic_plate, inflate=0.52, mirror=True, uv_override=uv_arm)

    c_pauldron_r, uv_p1 = _make_cube(packer, img, glow, [-9.6, 20.0, -2.5], [5, 4, 5], solar_gold, pivot=[-6, 22, 0], rotation=[0, 0, -18], trim=trim_gold, pattern="star_plate")
    c_pauldron_l, _ = _make_cube(packer, img, glow, [4.6, 20.0, -2.5], [5, 4, 5], solar_gold, pivot=[6, 22, 0], rotation=[0, 0, 18], mirror=True, uv_override=uv_p1)

    c_crest_r, uv_cr = _make_cube(packer, img, glow, [-10.0, 23.0, -1.5], [2, 4, 3], starlight_teal, pivot=[-8.5, 23, 0], rotation=[0, 0, -28], glow_color=glow_cyan, pattern="glow_gem")
    c_crest_l, _ = _make_cube(packer, img, glow, [8.0, 23.0, -1.5], [2, 4, 3], starlight_teal, pivot=[8.5, 23, 0], rotation=[0, 0, 28], mirror=True, uv_override=uv_cr)

    c_cuff_r, uv_cf = _make_cube(packer, img, glow, [-8.5, 13.0, -2.5], [5, 4, 5], royal_crimson, trim=trim_gold, glow_color=glow_gold, pattern="star_plate")
    c_cuff_l, _ = _make_cube(packer, img, glow, [3.5, 13.0, -2.5], [5, 4, 5], royal_crimson, mirror=True, uv_override=uv_cf)

    # Legs: Royal Cuisses + Star-Crest Knee Guards + Side Coat-Tails
    c_leg_l, uv_leg = _make_cube(packer, img, glow, [0, 0, -2], [4, 12, 4], cosmic_plate, inflate=0.36, trim=trim_gold, pattern="star_plate")
    c_leg_r, _ = _make_cube(packer, img, glow, [-4, 0, -2], [4, 12, 4], cosmic_plate, inflate=0.36, mirror=True, uv_override=uv_leg)

    c_coat_l, uv_ct = _make_cube(packer, img, glow, [3.6, 5.5, -2.2], [1, 7, 5], royal_crimson, pivot=[4, 11, 0], rotation=[0, 0, 8], trim=trim_gold, pattern="star_plate")
    c_coat_r, _ = _make_cube(packer, img, glow, [-4.6, 5.5, -2.2], [1, 7, 5], royal_crimson, pivot=[-4, 11, 0], rotation=[0, 0, -8], mirror=True, uv_override=uv_ct)

    c_knee_l, uv_kn = _make_cube(packer, img, glow, [0.5, 4.5, -3.2], [3, 3, 1], starlight_teal, glow_color=glow_cyan, pattern="glow_gem")
    c_knee_r, _ = _make_cube(packer, img, glow, [-3.5, 4.5, -3.2], [3, 3, 1], starlight_teal, mirror=True, uv_override=uv_kn)

    # Boots: Cosmic Greaves + Solar Toe-Caps + Swept Star-Wings
    c_boot_l, uv_boot = _make_cube(packer, img, glow, [0, 0, -2], [4, 12, 4], cosmic_plate, inflate=0.68, trim=trim_gold, pattern="star_plate")
    c_boot_r, _ = _make_cube(packer, img, glow, [-4, 0, -2], [4, 12, 4], cosmic_plate, inflate=0.68, mirror=True, uv_override=uv_boot)

    c_toe_l, uv_toe = _make_cube(packer, img, glow, [0.5, 0.0, -3.6], [3, 2, 2], solar_gold, pattern="plate")
    c_toe_r, _ = _make_cube(packer, img, glow, [-3.5, 0.0, -3.6], [3, 2, 2], solar_gold, mirror=True, uv_override=uv_toe)

    c_wing_l, uv_wg = _make_cube(packer, img, glow, [3.9, 1.5, -1.0], [1, 5, 4], starlight_teal, pivot=[4.4, 3, 1.0], rotation=[-20, 0, 14], glow_color=glow_cyan, pattern="glow_gem")
    c_wing_r, _ = _make_cube(packer, img, glow, [-4.9, 1.5, -1.0], [1, 5, 4], starlight_teal, pivot=[-4.4, 3, 1.0], rotation=[-20, 0, -14], mirror=True, uv_override=uv_wg)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": "geometry.twin_star_armor",
                "texture_width": 128,
                "texture_height": 128,
                "visible_bounds_width": 3,
                "visible_bounds_height": 3.5,
                "visible_bounds_offset": [0, 1.5, 0],
            },
            "bones": [
                {"name": "bipedHead", "pivot": [0, 24, 0]},
                {"name": "armorHead", "parent": "bipedHead", "pivot": [0, 24, 0], "cubes": head_cubes},
                {"name": "bipedBody", "pivot": [0, 24, 0]},
                {"name": "armorBody", "parent": "bipedBody", "pivot": [0, 24, 0], "cubes": body_cubes},
                {"name": "bipedRightArm", "pivot": [-5, 22, 0]},
                {"name": "armorRightArm", "parent": "bipedRightArm", "pivot": [-5, 22, 0], "cubes": [c_arm_r, c_pauldron_r, c_crest_r, c_cuff_r]},
                {"name": "bipedLeftArm", "pivot": [5, 22, 0]},
                {"name": "armorLeftArm", "parent": "bipedLeftArm", "pivot": [5, 22, 0], "cubes": [c_arm_l, c_pauldron_l, c_crest_l, c_cuff_l]},
                {"name": "bipedLeftLeg", "pivot": [2, 12, 0]},
                {"name": "armorLeftLeg", "parent": "bipedLeftLeg", "pivot": [2, 12, 0], "cubes": [c_leg_l, c_coat_l, c_knee_l]},
                {"name": "armorLeftBoot", "parent": "bipedLeftLeg", "pivot": [2, 12, 0], "cubes": [c_boot_l, c_toe_l, c_wing_l]},
                {"name": "bipedRightLeg", "pivot": [-2, 12, 0]},
                {"name": "armorRightLeg", "parent": "bipedRightLeg", "pivot": [-2, 12, 0], "cubes": [c_leg_r, c_coat_r, c_knee_r]},
                {"name": "armorRightBoot", "parent": "bipedRightLeg", "pivot": [-2, 12, 0], "cubes": [c_boot_r, c_toe_r, c_wing_r]},
            ],
        }],
    }

    GEO_DIR.mkdir(parents=True, exist_ok=True)
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    (GEO_DIR / "twin_star_armor.geo.json").write_text(json.dumps(geo, indent=2) + "\n", encoding="utf-8")
    img.save(TEX_DIR / "twin_star_armor.png")
    glow.save(TEX_DIR / "twin_star_armor_glowmask.png")


if __name__ == "__main__":
    build_dragonscale_armor()
    build_twin_star_armor()
    print("Generated 128x128 3D models, textures, and glowmasks for dragonscale_armor and twin_star_armor.")
