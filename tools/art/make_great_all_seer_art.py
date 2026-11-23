"""Generates GeckoLib model, animations, 256x128 texture, glowmask, and item sprites
for Phase 4 Stage 6: The Great All-Seer (Eye Realm Boss).

Run from the project root:
    python tools/art/make_great_all_seer_art.py
"""
from __future__ import annotations

import json
from pathlib import Path
from PIL import Image

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

# 8-stop ramps (dark -> bright)
SLATE_RAMP = (
    (20, 15, 29, 255),
    (30, 23, 44, 255),
    (43, 35, 56, 255),
    (56, 44, 74, 255),
    (70, 56, 92, 255),
    (88, 70, 114, 255),
    (112, 92, 140, 255),
    (142, 120, 172, 255),
)

GOLD_RAMP = (
    (96, 50, 14, 255),
    (132, 72, 20, 255),
    (170, 96, 26, 255),
    (208, 124, 34, 255),
    (238, 150, 46, 255),
    (255, 178, 70, 255),
    (255, 210, 118, 255),
    (255, 238, 180, 255),
)

MAGENTA_WING_RAMP = (
    (64, 16, 86, 255),
    (92, 24, 124, 255),
    (124, 34, 162, 255),
    (158, 46, 198, 255),
    (194, 58, 224, 255),
    (217, 70, 239, 255),
    (235, 118, 248, 255),
    (248, 175, 255, 255),
)

STARLIGHT_RAMP = (
    (18, 66, 78, 255),
    (26, 98, 112, 255),
    (36, 134, 144, 255),
    (46, 176, 172, 255),
    (56, 217, 192, 255),
    (96, 236, 218, 255),
    (150, 250, 238, 255),
    (212, 255, 250, 255),
)

SCLERA_RAMP = (
    (130, 126, 158, 255),
    (158, 154, 184, 255),
    (184, 180, 208, 255),
    (208, 206, 228, 255),
    (226, 224, 242, 255),
    (238, 238, 250, 255),
    (248, 248, 255, 255),
    (255, 255, 255, 255),
)

IRIS_CYAN = (125, 249, 255, 255)
IRIS_MID = (56, 217, 232, 255)
IRIS_RING = (24, 148, 178, 255)
PUPIL_DARK = (16, 10, 28, 255)
PUPIL_GLINT = (238, 255, 255, 255)
GLOW_CYAN = (125, 249, 255, 255)
GLOW_MAGENTA = (217, 70, 239, 255)
GLOW_TEAL = (56, 217, 192, 255)
CLEAR = (0, 0, 0, 0)


def _paint_eye_face(img: Image.Image, glow: Image.Image, rect: tuple[int, int, int, int], *, slit: bool = True) -> None:
    """Paint a rich layered iris + pupil + specular glint inside rect, and mirror iris to glowmask."""
    x, y, w, h = rect
    cx = (w - 1) / 2.0
    cy = (h - 1) / 2.0
    max_r = max(1.0, min(w, h) / 2.0)
    for dy in range(h):
        for dx in range(w):
            nx = (dx - cx) / max_r
            ny = (dy - cy) / max_r
            dist = (nx * nx + ny * ny) ** 0.5
            if slit and abs(nx) < 0.24 and abs(ny) < 0.65:
                put_px(img, x + dx, y + dy, PUPIL_DARK)
            elif not slit and dist < 0.34:
                put_px(img, x + dx, y + dy, PUPIL_DARK)
            elif dist < 0.68:
                put_px(img, x + dx, y + dy, IRIS_CYAN)
                put_px(glow, x + dx, y + dy, GLOW_CYAN)
            elif dist < 0.92:
                put_px(img, x + dx, y + dy, IRIS_MID)
                put_px(glow, x + dx, y + dy, GLOW_TEAL)
            else:
                put_px(img, x + dx, y + dy, IRIS_RING)
    if w >= 4 and h >= 4:
        put_px(img, x + max(0, int(w * 0.28)), y + max(0, int(h * 0.25)), PUPIL_GLINT)


def build_great_all_seer() -> tuple[dict, Image.Image, Image.Image]:
    # Each entry: (bone, parent, pivot, rot, cubes[(origin, size, uv, material, args)])
    # Careful non-overlapping UV allocation on 256x128 sheet:
    # box_rect(w, h, d) has width = 2*w + 2*d, height = d + h
    cubes_spec = [
        # Core armored hood (w=18, h=18, d=14 -> rect 64x32 at (0, 0))
        ("core_hood", "body", [0, 28, 0], None, [-9, 19, -7], [18, 18, 14], [0, 0], "stone", {"seed": 11}),
        # Brow ridge (w=20, h=4, d=15 -> rect 70x19 at (64, 0))
        ("brow_ridge", "core_hood", [0, 37, 0], None, [-10, 35, -8], [20, 4, 15], [64, 0], "metal", {"seed": 12}),
        # Chin plate (w=16, h=4, d=13 -> rect 58x17 at (134, 0))
        ("chin_plate", "core_hood", [0, 19, 0], None, [-8, 17, -7], [16, 4, 13], [134, 0], "metal", {"seed": 13}),
        # Temple fins (w=3, h=12, d=8 -> rect 22x20 at (192, 0), (214, 0))
        ("temple_left", "core_hood", [9, 28, 0], [0, 0, -16], [8.5, 22, -4], [3, 12, 8], [192, 0], "metal", {"seed": 14}),
        ("temple_right", "core_hood", [-9, 28, 0], [0, 0, 16], [-11.5, 22, -4], [3, 12, 8], [214, 0], "metal", {"seed": 15}),

        # Recessed eye sclera (w=12, h=12, d=3 -> rect 30x15 at (0, 32))
        ("eye_sclera", "core_hood", [0, 28, -7], None, [-6, 22, -8.5], [12, 12, 3], [0, 32], "sclera", {"seed": 21}),
        # Central glowing iris (w=8, h=8, d=2 -> rect 20x10 at (30, 32))
        ("eye_iris", "eye_sclera", [0, 28, -8], None, [-4, 24, -9.2], [8, 8, 2], [30, 32], "iris_main", {"seed": 22}),
        # Gold ocular bezel (top/bottom w=14, h=2, d=3 -> 34x5; sides w=2, h=12, d=3 -> 10x15)
        ("bezel_top", "core_hood", [0, 34, -8], None, [-7, 33, -9.0], [14, 2, 3], [64, 19], "gold", {"seed": 23}),
        ("bezel_bottom", "core_hood", [0, 22, -8], None, [-7, 21, -9.0], [14, 2, 3], [98, 19], "gold", {"seed": 24}),
        ("bezel_left", "core_hood", [7, 28, -8], None, [6, 22, -9.0], [2, 12, 3], [134, 17], "gold", {"seed": 25}),
        ("bezel_right", "core_hood", [-7, 28, -8], None, [-8, 22, -9.0], [2, 12, 3], [144, 17], "gold", {"seed": 26}),

        # Articulated upper & lower eyelids (w=13, h=4, d=4 -> rect 34x8 at (64, 24), (98, 24))
        ("lid_top", "core_hood", [0, 34, -7], None, [-6.5, 31, -9.6], [13, 4, 4], [64, 24], "stone", {"seed": 31}),
        ("lid_bottom", "core_hood", [0, 22, -7], None, [-6.5, 21, -9.6], [13, 4, 4], [98, 24], "stone", {"seed": 32}),

        # Crown halo (base w=10, h=3, d=6 -> 32x9; horns w=3, h=8, d=3 -> 12x11; crystal w=4, h=6, d=4 -> 16x10)
        ("crown_base", "core_hood", [0, 41, 0], None, [-5, 40, -3], [10, 3, 6], [154, 17], "gold", {"seed": 41}),
        ("crown_horn_left", "crown_base", [5, 42, 0], [0, 0, -22], [4, 41, -1.5], [3, 8, 3], [186, 20], "gold", {"seed": 42}),
        ("crown_horn_right", "crown_base", [-5, 42, 0], [0, 0, 22], [-7, 41, -1.5], [3, 8, 3], [198, 20], "gold", {"seed": 43}),
        ("crown_crystal", "crown_base", [0, 45, 0], [0, 45, 0], [-2, 43, -2], [4, 6, 4], [210, 20], "crystal", {"seed": 44}),

        # Upper eye-wings (inner w=14, h=7, d=2 -> rect 32x9; outer w=10, h=5, d=2 -> rect 24x7)
        ("wing_upper_left", "body", [9, 34, 2], [0, -10, -20], [9, 31, 1], [14, 7, 2], [0, 47], "wing", {"seed": 51}),
        ("wing_upper_left_tip", "wing_upper_left", [23, 35, 2], [0, 0, -14], [23, 32.5, 1], [10, 5, 2], [32, 47], "wing_tip", {"seed": 52}),
        ("wing_upper_right", "body", [-9, 34, 2], [0, 10, 20], [-23, 31, 1], [14, 7, 2], [56, 47], "wing", {"seed": 53}),
        ("wing_upper_right_tip", "wing_upper_right", [-23, 35, 2], [0, 0, 14], [-33, 32.5, 1], [10, 5, 2], [88, 47], "wing_tip", {"seed": 54}),

        # Lower eye-wings (inner w=12, h=6, d=2 -> rect 28x8; outer w=8, h=4, d=2 -> rect 20x6)
        ("wing_lower_left", "body", [9, 24, 3], [0, -14, 18], [9, 21, 2], [12, 6, 2], [0, 56], "wing", {"seed": 55}),
        ("wing_lower_left_tip", "wing_lower_left", [21, 24, 3], [0, 0, 14], [21, 22, 2], [8, 4, 2], [28, 56], "wing_tip", {"seed": 56}),
        ("wing_lower_right", "body", [-9, 24, 3], [0, 14, -18], [-21, 21, 2], [12, 6, 2], [48, 56], "wing", {"seed": 57}),
        ("wing_lower_right_tip", "wing_lower_right", [-21, 24, 3], [0, 0, -14], [-29, 22, 2], [8, 4, 2], [76, 56], "wing_tip", {"seed": 58}),

        # Orbiting Sentinel Mini-Eyes (each w=5, h=5, d=5 -> rect 20x10)
        ("orbiter_1", "orbiters", [18, 30, 0], None, [15.5, 27.5, -2.5], [5, 5, 5], [0, 64], "mini_eye", {"seed": 61}),
        ("orbiter_2", "orbiters", [-18, 30, 0], None, [-20.5, 27.5, -2.5], [5, 5, 5], [20, 64], "mini_eye", {"seed": 62}),
        ("orbiter_3", "orbiters", [0, 30, 18], None, [-2.5, 27.5, 15.5], [5, 5, 5], [40, 64], "mini_eye", {"seed": 63}),
        ("orbiter_4", "orbiters", [0, 30, -18], None, [-2.5, 27.5, -20.5], [5, 5, 5], [60, 64], "mini_eye", {"seed": 64}),

        # Segmented cosmic robe / tentacle tail
        # tail_1: w=10, h=7, d=8 -> rect 36x15
        # tail_2: w=7, h=6, d=7 -> rect 28x13
        # tail_3: w=4, h=5, d=4 -> rect 16x9
        # tail_gem: w=4, h=4, d=4 -> rect 16x8
        ("tail_1", "body", [0, 17, 1], [6, 0, 0], [-5, 10, -3], [10, 7, 8], [0, 74], "stone", {"seed": 71}),
        ("tail_2", "tail_1", [0, 10, 1], [10, 0, 0], [-3.5, 4, -2.5], [7, 6, 7], [36, 74], "metal", {"seed": 72}),
        ("tail_3", "tail_2", [0, 4, 2], [12, 0, 0], [-2, -1, 0], [4, 5, 4], [64, 74], "gold", {"seed": 73}),
        ("tail_gem", "tail_3", [0, -1, 2], [0, 45, 0], [-2, -5, 0], [4, 4, 4], [80, 74], "crystal", {"seed": 74}),
    ]

    tex = Image.new("RGBA", (256, 128), CLEAR)
    glow = Image.new("RGBA", (256, 128), CLEAR)

    uv_cubes = []
    bones = [
        {"name": "body", "pivot": [0, 28, 0]},
        {"name": "orbiters", "parent": "body", "pivot": [0, 30, 0], "rotation": [0, 45, 0]},
    ]

    for bname, parent, pivot, rot, origin, size, uv, mat, opts in cubes_spec:
        bone: dict = {"name": bname, "parent": parent, "pivot": pivot}
        if rot is not None:
            bone["rotation"] = rot
        cube = {"origin": origin, "size": size, "uv": uv}
        bone["cubes"] = [cube]
        bones.append(bone)
        uv_cubes.append({"name": bname, "origin": origin, "size": size, "uv": uv})

        u, v = uv
        w, h, d = size
        seed = opts["seed"]
        if mat == "stone":
            paint_weathered_stone(tex, u, v, w, h, d, SLATE_RAMP, seed=seed)
        elif mat == "metal":
            paint_forged_metal(tex, u, v, w, h, d, SLATE_RAMP, seed=seed)
        elif mat == "gold":
            paint_forged_metal(tex, u, v, w, h, d, GOLD_RAMP, seed=seed)
        elif mat == "crystal":
            paint_faceted_crystal(tex, u, v, w, h, d, STARLIGHT_RAMP, seed=seed)
            for face_rect in box_faces(u, v, w, h, d).values():
                fill_rect(glow, face_rect, GLOW_TEAL)
        elif mat == "sclera":
            paint_organic_shaded(tex, u, v, w, h, d, SCLERA_RAMP, seed=seed)
        elif mat == "iris_main":
            paint_organic_shaded(tex, u, v, w, h, d, STARLIGHT_RAMP, seed=seed)
            f = box_faces(u, v, w, h, d)
            _paint_eye_face(tex, glow, f["front"], slit=True)
        elif mat in ("wing", "wing_tip"):
            paint_organic_shaded(tex, u, v, w, h, d, MAGENTA_WING_RAMP, seed=seed)
            f = box_faces(u, v, w, h, d)
            fx, fy, fw, fh = f["front"]
            if mat == "wing" and fw >= 8 and fh >= 5:
                # Wing eye-sigil on front & back
                sigil = (fx + fw // 2 - 2, fy + fh // 2 - 2, 4, 4)
                _paint_eye_face(tex, glow, sigil, slit=False)
                bx, by, bw, bh = f["back"]
                _paint_eye_face(tex, glow, (bx + bw // 2 - 2, by + bh // 2 - 2, 4, 4), slit=False)
            if mat == "wing_tip":
                fill_rect(glow, (fx + fw - 3, fy, 3, fh), GLOW_MAGENTA)
        elif mat == "mini_eye":
            paint_forged_metal(tex, u, v, w, h, d, SLATE_RAMP, seed=seed)
            f = box_faces(u, v, w, h, d)
            for face_name in ("front", "back", "east", "west"):
                _paint_eye_face(tex, glow, f[face_name], slit=False)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.great_all_seer",
                    "texture_width": 256,
                    "texture_height": 128,
                    "visible_bounds_width": 4.5,
                    "visible_bounds_height": 4.0,
                    "visible_bounds_offset": [0.0, 1.8, 0.0],
                },
                "bones": bones,
            }
        ],
    }
    assert_uv_layout_clean(geo, (256, 128))
    return geo, tex, glow


def make_animations() -> dict:
    p = "animation.great_all_seer"
    return {
        "format_version": "1.8.0",
        "animations": {
            f"{p}.idle": {
                "loop": True,
                "animation_length": 4.0,
                "bones": {
                    "body": {
                        "position": {"0.0": [0, 0, 0], "2.0": [0, 1.8, 0], "4.0": [0, 0, 0]}
                    },
                    "orbiters": {
                        "rotation": {"0.0": [0, 0, 0], "4.0": [0, 360, 0]}
                    },
                    "wing_upper_left": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, -8, -8], "4.0": [0, 0, 0]}
                    },
                    "wing_upper_right": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, 8, 8], "4.0": [0, 0, 0]}
                    },
                    "wing_lower_left": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, -6, 8], "4.0": [0, 0, 0]}
                    },
                    "wing_lower_right": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, 6, -8], "4.0": [0, 0, 0]}
                    },
                    "lid_top": {
                        "position": {
                            "0.0": [0, 0, 0],
                            "3.4": [0, 0, 0],
                            "3.55": [0, -3.5, 0],
                            "3.75": [0, 0, 0],
                            "4.0": [0, 0, 0],
                        }
                    },
                    "lid_bottom": {
                        "position": {
                            "0.0": [0, 0, 0],
                            "3.4": [0, 0, 0],
                            "3.55": [0, 2.5, 0],
                            "3.75": [0, 0, 0],
                            "4.0": [0, 0, 0],
                        }
                    },
                    "tail_1": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [6, 0, 0], "4.0": [0, 0, 0]}
                    },
                    "tail_2": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [10, 0, 0], "4.0": [0, 0, 0]}
                    },
                },
            },
            f"{p}.walk": {
                "loop": True,
                "animation_length": 1.6,
                "bones": {
                    "body": {
                        "position": {"0.0": [0, 0.5, 0], "0.8": [0, 2.2, 0], "1.6": [0, 0.5, 0]},
                        "rotation": {"0.0": [6, 0, 0], "0.8": [10, 0, 0], "1.6": [6, 0, 0]},
                    },
                    "orbiters": {
                        "rotation": {"0.0": [0, 0, 0], "1.6": [0, 360, 0]}
                    },
                    "wing_upper_left": {
                        "rotation": {"0.0": [0, 8, -10], "0.8": [0, -14, -18], "1.6": [0, 8, -10]}
                    },
                    "wing_upper_right": {
                        "rotation": {"0.0": [0, -8, 10], "0.8": [0, 14, 18], "1.6": [0, -8, 10]}
                    },
                },
            },
            f"{p}.attack": {
                "animation_length": 0.65,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "0.2": [-10, 0, 0], "0.45": [12, 0, 0], "0.65": [0, 0, 0]}
                    },
                    "wing_upper_left": {
                        "rotation": {"0.0": [0, 0, 0], "0.25": [0, 22, -16], "0.65": [0, 0, 0]}
                    },
                    "wing_upper_right": {
                        "rotation": {"0.0": [0, 0, 0], "0.25": [0, -22, 16], "0.65": [0, 0, 0]}
                    },
                },
            },
            f"{p}.telegraph": {
                "animation_length": 2.0,
                "bones": {
                    "body": {
                        "position": {"0.0": [0, 0, 0], "0.5": [0, 3.0, 0], "1.5": [0, 3.5, 0], "2.0": [0, 0, 0]}
                    },
                    "orbiters": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, 720, 0]}
                    },
                    "lid_top": {
                        "position": {"0.0": [0, 0, 0], "0.3": [0, 2.5, 0], "1.7": [0, 2.5, 0], "2.0": [0, 0, 0]}
                    },
                    "lid_bottom": {
                        "position": {"0.0": [0, 0, 0], "0.3": [0, -2.0, 0], "1.7": [0, -2.0, 0], "2.0": [0, 0, 0]}
                    },
                    "wing_upper_left": {
                        "rotation": {"0.0": [0, 0, 0], "0.4": [0, -20, -24], "1.6": [0, -24, -28], "2.0": [0, 0, 0]}
                    },
                    "wing_upper_right": {
                        "rotation": {"0.0": [0, 0, 0], "0.4": [0, 20, 24], "1.6": [0, 24, 28], "2.0": [0, 0, 0]}
                    },
                },
            },
        },
    }


def make_ocular_key_sprite() -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    gold = (238, 150, 46, 255)
    gold_hi = (255, 210, 118, 255)
    gold_lo = (140, 78, 20, 255)
    slate = (43, 35, 56, 255)
    # Ornate eye-bow at top
    for x in range(5, 11):
        put_px(img, x, 2, gold_hi)
        put_px(img, x, 6, gold_lo)
    for y in range(3, 6):
        put_px(img, 4, y, gold)
        put_px(img, 11, y, gold)
    # Eye inside bow
    for y in range(3, 6):
        for x in range(5, 11):
            put_px(img, x, y, (226, 224, 242, 255))
    for y in range(3, 6):
        put_px(img, 7, y, IRIS_CYAN)
        put_px(img, 8, y, IRIS_CYAN)
    put_px(img, 7, 4, PUPIL_DARK)
    # Key shaft & teeth
    for y in range(7, 14):
        put_px(img, 7, y, slate)
        put_px(img, 8, y, gold)
    for x in range(9, 12):
        put_px(img, x, 11, gold_hi)
        put_px(img, x, 13, GLOW_TEAL)
    return img


def make_all_seers_lens_sprite() -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    gold = (238, 150, 46, 255)
    gold_hi = (255, 210, 118, 255)
    magenta = (217, 70, 239, 255)
    # Octagonal frame
    for x in range(5, 11):
        put_px(img, x, 2, gold_hi)
        put_px(img, x, 13, gold)
    for y in range(5, 11):
        put_px(img, 2, y, gold_hi)
        put_px(img, 13, y, gold)
    for cx, cy in ((3, 4), (4, 3), (11, 3), (12, 4), (3, 11), (4, 12), (11, 12), (12, 11)):
        put_px(img, cx, cy, magenta)
    # Glowing lens interior
    for y in range(4, 12):
        for x in range(4, 12):
            dist = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if dist < 1.6:
                put_px(img, x, y, PUPIL_DARK)
            elif dist < 3.0:
                put_px(img, x, y, IRIS_CYAN)
            else:
                put_px(img, x, y, IRIS_MID)
    put_px(img, 6, 6, PUPIL_GLINT)
    return img


def main() -> None:
    geo, tex, glow = build_great_all_seer()
    anims = make_animations()

    (ASSETS / "geo" / "entity" / "great_all_seer.geo.json").write_text(
        json.dumps(geo, indent=2), encoding="utf-8"
    )
    (ASSETS / "animations" / "entity" / "great_all_seer.animation.json").write_text(
        json.dumps(anims, indent=2), encoding="utf-8"
    )
    tex.save(ASSETS / "textures" / "entity" / "great_all_seer.png")
    glow.save(ASSETS / "textures" / "entity" / "great_all_seer_glowmask.png")

    make_ocular_key_sprite().save(ASSETS / "textures" / "item" / "ocular_key.png")
    make_all_seers_lens_sprite().save(ASSETS / "textures" / "item" / "all_seers_lens.png")
    print("Generated great_all_seer art & item sprites.")


if __name__ == "__main__":
    main()
