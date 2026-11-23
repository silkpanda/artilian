"""Generates GeckoLib model, animations, 256x128 texture, glowmask, and item sprites
for Phase 5 Stage 8: Embercrown Sovereign (Dragon Jungle Apex Boss).

Run from the project root:
    python tools/art/make_embercrown_sovereign_art.py
"""
from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw

from art_lib import (
    assert_uv_layout_clean,
    box_faces,
    fill_rect,
    paint_faceted_crystal,
    paint_forged_metal,
    paint_organic_shaded,
    paint_weathered_stone,
    put_px,
)

ASSETS = Path("src/main/resources/assets/artilian")
GEO_DIR = ASSETS / "geo" / "entity"
ANIM_DIR = ASSETS / "animations" / "entity"
TEX_DIR = ASSETS / "textures" / "entity"
ITEM_TEX_DIR = ASSETS / "textures" / "item"

SHEET_W = 256
SHEET_H = 128

# 6-8 stop ramps (dark -> bright)
OBSIDIAN_SCALE_RAMP = [
    (18, 12, 20, 255),
    (28, 19, 30, 255),
    (40, 27, 40, 255),
    (54, 36, 50, 255),
    (70, 48, 62, 255),
    (90, 62, 78, 255),
]

EMBER_ARMOR_RAMP = [
    (96, 30, 10, 255),
    (138, 46, 14, 255),
    (184, 68, 18, 255),
    (226, 96, 22, 255),
    (249, 115, 22, 255),
    (252, 168, 56, 255),
    (253, 224, 71, 255),
]

BELLY_MAGMA_RAMP = [
    (78, 26, 14, 255),
    (118, 42, 18, 255),
    (164, 64, 22, 255),
    (212, 92, 24, 255),
    (249, 128, 36, 255),
    (254, 192, 82, 255),
]

WING_MEMBRANE_RAMP = [
    (46, 14, 18, 255),
    (74, 22, 22, 255),
    (112, 34, 24, 255),
    (156, 54, 26, 255),
    (208, 84, 28, 255),
    (245, 134, 48, 255),
]

CROWN_GOLD_RAMP = [
    (110, 56, 12, 255),
    (152, 82, 16, 255),
    (198, 114, 24, 255),
    (238, 152, 36, 255),
    (253, 200, 68, 255),
    (255, 238, 146, 255),
]

PRIMAL_CORE_RAMP = [
    (22, 96, 52, 255),
    (34, 142, 74, 255),
    (34, 197, 94, 255),
    (74, 222, 128, 255),
    (148, 246, 180, 255),
    (220, 255, 232, 255),
]

CLAW_RAMP = [
    (24, 20, 26, 255),
    (38, 32, 42, 255),
    (56, 48, 60, 255),
    (82, 70, 86, 255),
    (114, 98, 116, 255),
    (152, 134, 154, 255),
]

GLOW_AMBER = (253, 214, 68, 255)
GLOW_ORANGE = (249, 115, 22, 255)
GLOW_EMERALD = (74, 222, 128, 255)
CLEAR = (0, 0, 0, 0)


def _cube(
    origin: list[float],
    size: list[float],
    uv: list[int],
    *,
    inflate: float = 0.0,
    mirror: bool = False,
) -> dict:
    c: dict = {"origin": origin, "size": size, "uv": uv}
    if inflate != 0.0:
        c["inflate"] = inflate
    if mirror:
        c["mirror"] = True
    return c


def _bone(
    name: str,
    pivot: list[float],
    cubes: list[dict],
    *,
    parent: str | None = None,
    rotation: list[float] | None = None,
) -> dict:
    b: dict = {"name": name, "pivot": pivot, "cubes": cubes}
    if parent is not None:
        b["parent"] = parent
    if rotation is not None:
        b["rotation"] = rotation
    return b


def build_geo_and_uvs() -> tuple[dict, list[tuple[list[int], list[int], str, bool]]]:
    """Builds the 256x128 GeckoLib geometry for Embercrown Sovereign."""
    bones: list[dict] = []
    uv_specs: list[tuple[list[int], list[int], str, bool]] = []

    def add_uv(size: list[float], uv: list[int], kind: str, glow: bool = False) -> None:
        uv_specs.append(([int(size[0]), int(size[1]), int(size[2])], uv, kind, glow))

    bones.append(_bone("root", [0, 0, 0], []))

    # Torso & Chest (broad-shouldered armored apex dragon)
    torso_size = [16, 14, 22]
    torso_uv = [0, 0]
    add_uv(torso_size, torso_uv, "scale")
    chest_plate_size = [14, 10, 6]
    chest_plate_uv = [76, 0]
    add_uv(chest_plate_size, chest_plate_uv, "armor")
    heart_core_size = [6, 6, 2]
    heart_core_uv = [116, 0]
    add_uv(heart_core_size, heart_core_uv, "core", True)

    bones.append(
        _bone(
            "body",
            [0, 18, 0],
            [
                _cube([-8, 12, -11], torso_size, torso_uv),
                _cube([-7, 13, -12.5], chest_plate_size, chest_plate_uv),
                _cube([-3, 15, -13.5], heart_core_size, heart_core_uv),
            ],
            parent="root",
            rotation=[-8, 0, 0],
        )
    )

    # Dorsal armor ridge & shoulder pauldrons
    ridge_size = [4, 5, 16]
    ridge_uv = [0, 36]
    add_uv(ridge_size, ridge_uv, "armor", True)
    pauldron_size = [7, 6, 9]
    pauldron_l_uv = [40, 36]
    pauldron_r_uv = [72, 36]
    add_uv(pauldron_size, pauldron_l_uv, "armor")
    add_uv(pauldron_size, pauldron_r_uv, "armor")

    bones.append(
        _bone(
            "dorsal_armor",
            [0, 26, 0],
            [
                _cube([-2, 25, -7], ridge_size, ridge_uv),
                _cube([-13.5, 22, -9], pauldron_size, pauldron_l_uv),
                _cube([6.5, 22, -9], pauldron_size, pauldron_r_uv),
            ],
            parent="body",
        )
    )

    # Arched Neck
    neck1_size = [9, 9, 10]
    neck1_uv = [132, 0]
    add_uv(neck1_size, neck1_uv, "scale")
    bones.append(
        _bone(
            "neck_lower",
            [0, 23, -10],
            [_cube([-4.5, 20, -19], neck1_size, neck1_uv)],
            parent="body",
            rotation=[-22, 0, 0],
        )
    )

    neck2_size = [8, 8, 10]
    neck2_uv = [170, 0]
    add_uv(neck2_size, neck2_uv, "scale")
    bones.append(
        _bone(
            "neck_upper",
            [0, 28, -18],
            [_cube([-4, 25, -27], neck2_size, neck2_uv)],
            parent="neck_lower",
            rotation=[14, 0, 0],
        )
    )

    # Sovereign Head, Snout, Inset Eyes, Articulated Jaw, 5-Horn Embercrown
    head_size = [11, 9, 12]
    head_uv = [104, 36]
    add_uv(head_size, head_uv, "head")

    snout_size = [7, 5, 8]
    snout_uv = [150, 36]
    add_uv(snout_size, snout_uv, "scale")

    brow_size = [12, 3, 5]
    brow_uv = [180, 36]
    add_uv(brow_size, brow_uv, "armor")

    eye_size = [2, 3, 3]
    eye_l_uv = [214, 36]
    eye_r_uv = [224, 36]
    add_uv(eye_size, eye_l_uv, "eye", True)
    add_uv(eye_size, eye_r_uv, "eye", True)

    bones.append(
        _bone(
            "head",
            [0, 32, -26],
            [
                _cube([-5.5, 28, -36], head_size, head_uv),
                _cube([-3.5, 29, -43], snout_size, snout_uv),
                _cube([-6, 35, -37], brow_size, brow_uv),
                _cube([-6.1, 32, -37], eye_size, eye_l_uv),
                _cube([4.1, 32, -37], eye_size, eye_r_uv),
            ],
            parent="neck_upper",
            rotation=[8, 0, 0],
        )
    )

    # Articulated Lower Jaw
    jaw_size = [7, 3, 10]
    jaw_uv = [206, 0]
    add_uv(jaw_size, jaw_uv, "belly")
    bones.append(
        _bone(
            "jaw",
            [0, 29, -32],
            [_cube([-3.5, 26.5, -42], jaw_size, jaw_uv)],
            parent="head",
        )
    )

    # 5-Horn Embercrown
    crown_center_size = [3, 10, 4]
    crown_center_uv = [0, 58]
    add_uv(crown_center_size, crown_center_uv, "crown", True)
    bones.append(
        _bone(
            "crown_center",
            [0, 37, -32],
            [_cube([-1.5, 37, -33], crown_center_size, crown_center_uv)],
            parent="head",
            rotation=[-30, 0, 0],
        )
    )

    horn_inner_size = [2, 8, 3]
    horn_inner_l_uv = [14, 58]
    horn_inner_r_uv = [24, 58]
    add_uv(horn_inner_size, horn_inner_l_uv, "crown", True)
    add_uv(horn_inner_size, horn_inner_r_uv, "crown", True)
    bones.append(
        _bone(
            "horn_inner_left",
            [-3.5, 37, -31],
            [_cube([-4.5, 37, -32], horn_inner_size, horn_inner_l_uv)],
            parent="head",
            rotation=[-32, -16, -10],
        )
    )
    bones.append(
        _bone(
            "horn_inner_right",
            [3.5, 37, -31],
            [_cube([2.5, 37, -32], horn_inner_size, horn_inner_r_uv)],
            parent="head",
            rotation=[-32, 16, 10],
        )
    )

    horn_outer_size = [3, 11, 4]
    horn_outer_l_uv = [34, 58]
    horn_outer_r_uv = [48, 58]
    add_uv(horn_outer_size, horn_outer_l_uv, "crown", True)
    add_uv(horn_outer_size, horn_outer_r_uv, "crown", True)
    bones.append(
        _bone(
            "horn_outer_left",
            [-5.5, 36, -29],
            [_cube([-7, 36, -30], horn_outer_size, horn_outer_l_uv)],
            parent="head",
            rotation=[-26, -32, -22],
        )
    )
    bones.append(
        _bone(
            "horn_outer_right",
            [5.5, 36, -29],
            [_cube([4, 36, -30], horn_outer_size, horn_outer_r_uv)],
            parent="head",
            rotation=[-26, 32, 22],
        )
    )

    # 4 Heavy Clawed Legs
    leg_upper_size = [5, 8, 6]
    leg_lower_size = [4, 7, 5]
    foot_size = [6, 3, 7]
    leg_uvs = [
        ("front_left_leg", [-8.5, 15, -7], [62, 58], [84, 58], [102, 58]),
        ("front_right_leg", [8.5, 15, -7], [128, 58], [150, 58], [168, 58]),
        ("back_left_leg", [-8.5, 15, 8], [0, 74], [22, 74], [40, 74]),
        ("back_right_leg", [8.5, 15, 8], [66, 74], [88, 74], [106, 74]),
    ]
    for name, piv, uv_u, uv_l, uv_f in leg_uvs:
        add_uv(leg_upper_size, uv_u, "scale")
        add_uv(leg_lower_size, uv_l, "armor")
        add_uv(foot_size, uv_f, "claw")
        px, py, pz = piv
        bones.append(
            _bone(
                name,
                piv,
                [
                    _cube([px - 2.5, py - 7, pz - 3], leg_upper_size, uv_u),
                    _cube([px - 2.0, py - 13, pz - 2], leg_lower_size, uv_l),
                    _cube([px - 3.0, 0, pz - 4.5], foot_size, uv_f),
                ],
                parent="root",
            )
        )

    # Colossal 3-Segment Dihedral Wings (Left & Right)
    wing_in_spar = [14, 3, 4]
    wing_in_mem = [14, 1, 14]
    wing_mid_spar = [14, 2, 3]
    wing_mid_mem = [14, 1, 13]
    wing_out_spar = [12, 2, 3]
    wing_out_mem = [12, 1, 11]

    # Left Wing
    add_uv(wing_in_spar, [132, 74], "armor")
    add_uv(wing_in_mem, [0, 92], "wing", True)
    add_uv(wing_mid_spar, [168, 74], "armor")
    add_uv(wing_mid_mem, [56, 92], "wing", True)
    add_uv(wing_out_spar, [202, 74], "claw")
    add_uv(wing_out_mem, [110, 92], "wing", True)

    bones.append(
        _bone(
            "left_wing_inner",
            [-7, 25, -4],
            [
                _cube([-21, 24, -6], wing_in_spar, [132, 74]),
                _cube([-21, 24.5, -2], wing_in_mem, [0, 92]),
            ],
            parent="body",
            rotation=[0, 8, 18],
        )
    )
    bones.append(
        _bone(
            "left_wing_mid",
            [-21, 25, -4],
            [
                _cube([-35, 24.5, -5.5], wing_mid_spar, [168, 74]),
                _cube([-35, 24.8, -2.5], wing_mid_mem, [56, 92]),
            ],
            parent="left_wing_inner",
            rotation=[0, -6, -12],
        )
    )
    bones.append(
        _bone(
            "left_wing_outer",
            [-35, 25, -4],
            [
                _cube([-47, 24.5, -5], wing_out_spar, [202, 74]),
                _cube([-47, 24.8, -2], wing_out_mem, [110, 92]),
            ],
            parent="left_wing_mid",
            rotation=[0, -10, -16],
        )
    )

    # Right Wing
    add_uv(wing_in_spar, [132, 82], "armor")
    add_uv(wing_in_mem, [0, 108], "wing", True)
    add_uv(wing_mid_spar, [168, 82], "armor")
    add_uv(wing_mid_mem, [56, 108], "wing", True)
    add_uv(wing_out_spar, [202, 82], "claw")
    add_uv(wing_out_mem, [110, 108], "wing", True)

    bones.append(
        _bone(
            "right_wing_inner",
            [7, 25, -4],
            [
                _cube([7, 24, -6], wing_in_spar, [132, 82]),
                _cube([7, 24.5, -2], wing_in_mem, [0, 108]),
            ],
            parent="body",
            rotation=[0, -8, -18],
        )
    )
    bones.append(
        _bone(
            "right_wing_mid",
            [21, 25, -4],
            [
                _cube([21, 24.5, -5.5], wing_mid_spar, [168, 82]),
                _cube([21, 24.8, -2.5], wing_mid_mem, [56, 108]),
            ],
            parent="right_wing_inner",
            rotation=[0, 6, 12],
        )
    )
    bones.append(
        _bone(
            "right_wing_outer",
            [35, 25, -4],
            [
                _cube([35, 24.5, -5], wing_out_spar, [202, 82]),
                _cube([35, 24.8, -2], wing_out_mem, [110, 108]),
            ],
            parent="right_wing_mid",
            rotation=[0, 10, 16],
        )
    )

    # 3-Segment Spiked War-Tail
    t1_size = [8, 8, 12]
    t1_uv = [156, 92]
    t2_size = [6, 6, 12]
    t2_uv = [196, 92]
    t3_size = [4, 4, 11]
    t3_uv = [156, 112]
    mace_size = [7, 5, 6]
    mace_uv = [196, 112]
    add_uv(t1_size, t1_uv, "scale")
    add_uv(t2_size, t2_uv, "scale")
    add_uv(t3_size, t3_uv, "armor")
    add_uv(mace_size, mace_uv, "crown", True)

    bones.append(
        _bone(
            "tail_1",
            [0, 18, 11],
            [_cube([-4, 14, 11], t1_size, t1_uv)],
            parent="body",
            rotation=[-8, 0, 0],
        )
    )
    bones.append(
        _bone(
            "tail_2",
            [0, 17, 23],
            [_cube([-3, 14, 23], t2_size, t2_uv)],
            parent="tail_1",
            rotation=[6, 0, 0],
        )
    )
    bones.append(
        _bone(
            "tail_3",
            [0, 17, 35],
            [
                _cube([-2, 15, 35], t3_size, t3_uv),
                _cube([-3.5, 14.5, 45], mace_size, mace_uv),
            ],
            parent="tail_2",
            rotation=[10, 0, 0],
        )
    )

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.embercrown_sovereign",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 8.0,
                    "visible_bounds_height": 5.0,
                    "visible_bounds_offset": [0.0, 2.0, 0.0],
                },
                "bones": bones,
            }
        ],
    }
    return geo, uv_specs


def paint_textures(uv_specs: list[tuple[list[int], list[int], str, bool]]) -> tuple[Image.Image, Image.Image]:
    tex = Image.new("RGBA", (SHEET_W, SHEET_H), CLEAR)
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), CLEAR)

    for idx, (size, uv, kind, is_glow) in enumerate(uv_specs):
        u, v = uv
        w, h, d = size
        faces = box_faces(u, v, w, h, d)
        if kind == "scale":
            paint_organic_shaded(tex, u, v, w, h, d, OBSIDIAN_SCALE_RAMP, belly_color=BELLY_MAGMA_RAMP[2], seed=101 + idx)
        elif kind == "belly":
            paint_organic_shaded(tex, u, v, w, h, d, BELLY_MAGMA_RAMP, seed=303 + idx)
        elif kind == "armor":
            paint_forged_metal(tex, u, v, w, h, d, EMBER_ARMOR_RAMP, rivet_color=CROWN_GOLD_RAMP[-1])
            if is_glow:
                for face_name in ("up", "north", "south"):
                    if face_name in faces:
                        fx, fy, fw, fh = faces[face_name]
                        put_px(glow, fx + fw // 2, fy + fh // 2, GLOW_ORANGE)
        elif kind == "head":
            paint_organic_shaded(tex, u, v, w, h, d, OBSIDIAN_SCALE_RAMP, seed=505 + idx)
            if "north" in faces:
                fx, fy, fw, fh = faces["north"]
                if fw >= 8 and fh >= 6:
                    for x in range(fx + 2, fx + fw - 2, 2):
                        put_px(tex, x, fy + fh - 1, CROWN_GOLD_RAMP[-1])
        elif kind == "eye":
            paint_faceted_crystal(tex, u, v, w, h, d, CROWN_GOLD_RAMP, seed=606 + idx)
            paint_faceted_crystal(glow, u, v, w, h, d, [GLOW_ORANGE, GLOW_AMBER, (255, 255, 210, 255)], seed=606 + idx)
        elif kind == "crown":
            paint_faceted_crystal(tex, u, v, w, h, d, CROWN_GOLD_RAMP, seed=707 + idx)
            if is_glow:
                for face_name, (fx, fy, fw, fh) in faces.items():
                    for dy in range(max(1, fh // 2)):
                        for dx in range(fw):
                            put_px(glow, fx + dx, fy + dy, GLOW_AMBER)
        elif kind == "core":
            paint_faceted_crystal(tex, u, v, w, h, d, PRIMAL_CORE_RAMP, seed=808 + idx)
            paint_faceted_crystal(glow, u, v, w, h, d, [PRIMAL_CORE_RAMP[2], GLOW_EMERALD, (220, 255, 232, 255)], seed=808 + idx)
        elif kind == "wing":
            paint_organic_shaded(tex, u, v, w, h, d, WING_MEMBRANE_RAMP, seed=909 + idx)
            if is_glow:
                for face_name, (fx, fy, fw, fh) in faces.items():
                    if fw >= 6 and fh >= 6:
                        for vx in range(fx + 2, fx + fw - 1, 4):
                            for vy in range(fy + 1, fy + fh - 1):
                                put_px(tex, vx, vy, EMBER_ARMOR_RAMP[4])
                                if vy % 2 == 0:
                                    put_px(glow, vx, vy, GLOW_ORANGE)
        elif kind == "claw":
            paint_weathered_stone(tex, u, v, w, h, d, CLAW_RAMP, seed=1010 + idx)

    return tex, glow


def build_animations() -> dict:
    return {
        "format_version": "1.8.0",
        "animations": {
            "animation.embercrown_sovereign.idle": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "body": {"position": {"0.0": [0, 0, 0], "1.2": [0, 0.8, 0], "2.4": [0, 0, 0]}},
                    "neck_lower": {"rotation": {"0.0": [0, 0, 0], "1.2": [4, 0, 0], "2.4": [0, 0, 0]}},
                    "head": {"rotation": {"0.0": [0, 0, 0], "1.2": [-4, 0, 0], "2.4": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [4, 0, 0], "1.2": [14, 0, 0], "2.4": [4, 0, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, 0], "1.2": [0, 0, 10], "2.4": [0, 0, 0]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 0], "1.2": [0, 0, -10], "2.4": [0, 0, 0]}},
                    "tail_1": {"rotation": {"0.0": [0, -6, 0], "1.2": [0, 6, 0], "2.4": [0, -6, 0]}},
                    "tail_2": {"rotation": {"0.0": [0, -8, 0], "1.2": [0, 8, 0], "2.4": [0, -8, 0]}},
                },
            },
            "animation.embercrown_sovereign.walk": {
                "loop": True,
                "animation_length": 1.2,
                "bones": {
                    "body": {"position": {"0.0": [0, 0, 0], "0.3": [0, 0.9, 0], "0.6": [0, 0, 0], "0.9": [0, 0.9, 0], "1.2": [0, 0, 0]}},
                    "front_left_leg": {"rotation": {"0.0": [22, 0, 0], "0.6": [-22, 0, 0], "1.2": [22, 0, 0]}},
                    "front_right_leg": {"rotation": {"0.0": [-22, 0, 0], "0.6": [22, 0, 0], "1.2": [-22, 0, 0]}},
                    "back_left_leg": {"rotation": {"0.0": [-22, 0, 0], "0.6": [22, 0, 0], "1.2": [-22, 0, 0]}},
                    "back_right_leg": {"rotation": {"0.0": [22, 0, 0], "0.6": [-22, 0, 0], "1.2": [22, 0, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, 4], "0.6": [0, 0, 18], "1.2": [0, 0, 4]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, -4], "0.6": [0, 0, -18], "1.2": [0, 0, -4]}},
                    "tail_1": {"rotation": {"0.0": [0, -10, 0], "0.6": [0, 10, 0], "1.2": [0, -10, 0]}},
                },
            },
            "animation.embercrown_sovereign.attack": {
                "animation_length": 0.8,
                "bones": {
                    "neck_lower": {"rotation": {"0.0": [0, 0, 0], "0.25": [-14, 0, 0], "0.5": [18, 0, 0], "0.8": [0, 0, 0]}},
                    "head": {"rotation": {"0.0": [0, 0, 0], "0.25": [-18, 0, 0], "0.5": [14, 0, 0], "0.8": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "0.25": [34, 0, 0], "0.55": [8, 0, 0], "0.8": [0, 0, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, 0], "0.25": [0, 0, 28], "0.8": [0, 0, 0]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 0], "0.25": [0, 0, -28], "0.8": [0, 0, 0]}},
                },
            },
            "animation.embercrown_sovereign.telegraph": {
                "animation_length": 1.6,
                "bones": {
                    "body": {"rotation": {"0.0": [0, 0, 0], "0.5": [-16, 0, 0], "1.2": [-18, 0, 0], "1.6": [0, 0, 0]}},
                    "neck_lower": {"rotation": {"0.0": [0, 0, 0], "0.5": [-22, 0, 0], "1.2": [-24, 0, 0], "1.6": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "0.5": [42, 0, 0], "1.2": [42, 0, 0], "1.6": [0, 0, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, 0], "0.5": [0, 0, 42], "1.2": [0, 0, 46], "1.6": [0, 0, 0]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 0], "0.5": [0, 0, -42], "1.2": [0, 0, -46], "1.6": [0, 0, 0]}},
                },
            },
        },
    }


def make_item_sprites() -> None:
    ITEM_TEX_DIR.mkdir(parents=True, exist_ok=True)

    # 16x16 Primal Dragon Crest (summon key)
    crest = Image.new("RGBA", (16, 16), CLEAR)
    d = ImageDraw.Draw(crest)
    # Winged obsidian-gold dragon crest
    for y in range(4, 13):
        for x in range(3, 13):
            if abs(x - 7.5) + abs(y - 8) <= 4.8:
                c = CROWN_GOLD_RAMP[min(5, max(0, (12 - y) // 2))]
                put_px(crest, x, y, c)
    # Crown horns on top
    for dx, dy in [(4, 3), (4, 2), (7, 2), (8, 2), (7, 1), (8, 1), (11, 3), (11, 2)]:
        put_px(crest, dx, dy, CROWN_GOLD_RAMP[-1])
    # Emerald Primal Heart center gem
    for y in range(6, 10):
        for x in range(6, 10):
            if (x, y) not in ((6, 6), (9, 6), (6, 9), (9, 9)):
                put_px(crest, x, y, PRIMAL_CORE_RAMP[3 if (x + y) % 2 == 0 else 4])
    crest.save(ITEM_TEX_DIR / "primal_dragon_crest.png")

    # 16x16 Sovereign's Ember Heart (boss trophy)
    heart = Image.new("RGBA", (16, 16), CLEAR)
    for y in range(2, 15):
        for x in range(2, 14):
            dist = abs(x - 7.5) + abs(y - 8) * 1.15
            if dist <= 5.8:
                idx = min(6, max(0, int(6 - dist)))
                put_px(heart, x, y, EMBER_ARMOR_RAMP[idx])
    # Inner emerald-gold core highlight
    for y in range(6, 10):
        for x in range(6, 10):
            put_px(heart, x, y, PRIMAL_CORE_RAMP[min(5, 2 + (9 - y))])
    put_px(heart, 7, 7, (255, 255, 235, 255))
    heart.save(ITEM_TEX_DIR / "sovereigns_ember_heart.png")


def main() -> None:
    GEO_DIR.mkdir(parents=True, exist_ok=True)
    ANIM_DIR.mkdir(parents=True, exist_ok=True)
    TEX_DIR.mkdir(parents=True, exist_ok=True)

    geo, uv_specs = build_geo_and_uvs()
    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H), allow_mirror_overlap=False)
    (GEO_DIR / "embercrown_sovereign.geo.json").write_text(json.dumps(geo, indent=2) + "\n", encoding="utf-8")
    (ANIM_DIR / "embercrown_sovereign.animation.json").write_text(
        json.dumps(build_animations(), indent=2) + "\n", encoding="utf-8"
    )

    tex, glow = paint_textures(uv_specs)
    tex.save(TEX_DIR / "embercrown_sovereign.png")
    glow.save(TEX_DIR / "embercrown_sovereign_glowmask.png")

    make_item_sprites()
    print("Generated Embercrown Sovereign geo, animations, 256x128 texture, glowmask, and item sprites.")


if __name__ == "__main__":
    main()
