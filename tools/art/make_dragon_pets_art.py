"""Generates 128x128 GeckoLib models, animations, textures, glowmasks, and egg block textures
for Phase 5 Stage 6 (Hatchable Pet Dragons):
- canopy_dragonling (4-legged jungle dragonling: Canopy Emerald & Ember Amber)
- amber_dragonling (bipedal wyvern hatchling: volcanic obsidian & molten Ember Amber)
- storm_dragonling (hovering sky-serpent hatchling: storm-indigo & electric cyan/gold)
- 3 Dragon Egg block textures (stages 0, 1, 2 with progressive glowing elemental cracks)
"""
from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw

from art_lib import (
    assert_uv_layout_clean,
    box_faces,
    paint_faceted_crystal,
    paint_organic_shaded,
    put_px,
)
from make_dragons_art import (
    AMBER_HORN_RAMP,
    DARK_CLAW_RAMP,
    EMERALD_BELLY_RAMP,
    JUNGLE_SCALE_RAMP,
    WING_MEMBRANE_RAMP,
)

ASSETS = Path("src/main/resources/assets/artilian")
GEO_DIR = ASSETS / "geo" / "entity"
ANIM_DIR = ASSETS / "animations" / "entity"
TEX_DIR = ASSETS / "textures" / "entity"
BLOCK_TEX_DIR = ASSETS / "textures" / "block"

SHEET_W = 128
SHEET_H = 128

# Amber Dragonling palette
OBSIDIAN_RAMP = [
    (20, 14, 18, 255),
    (32, 22, 26, 255),
    (46, 30, 34, 255),
    (64, 40, 44, 255),
    (84, 52, 54, 255),
    (108, 68, 66, 255),
]
MAGMA_RAMP = [
    (124, 34, 12, 255),
    (176, 56, 16, 255),
    (224, 88, 20, 255),
    (249, 115, 22, 255),
    (253, 186, 52, 255),
    (254, 240, 138, 255),
]

# Storm Dragonling palette
STORM_NAVY_RAMP = [
    (18, 24, 44, 255),
    (28, 36, 64, 255),
    (38, 52, 88, 255),
    (52, 70, 114, 255),
    (70, 92, 142, 255),
    (96, 120, 174, 255),
]
SKY_CYAN_RAMP = [
    (14, 86, 118, 255),
    (24, 126, 164, 255),
    (38, 172, 192, 255),
    (56, 217, 192, 255),
    (125, 249, 255, 255),
    (196, 252, 255, 255),
]
ELECTRIC_GOLD_RAMP = [
    (146, 102, 12, 255),
    (196, 142, 16, 255),
    (234, 179, 20, 255),
    (250, 204, 21, 255),
    (253, 224, 71, 255),
    (254, 249, 195, 255),
]


def _save_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def _cube(origin: list[float], size: list[float], uv: list[int], rotation: list[float] | None = None) -> dict:
    c: dict = {"origin": origin, "size": size, "uv": uv}
    if rotation is not None:
        c["rotation"] = rotation
    return c


def _paint_cube(
    img: Image.Image,
    glow: Image.Image,
    uv: list[int],
    size: list[float],
    ramp: list[tuple[int, int, int, int]],
    *,
    crystal: bool = False,
    emissive: bool = False,
    seed: int = 1,
) -> None:
    u, v = int(uv[0]), int(uv[1])
    w, h, d = max(1, int(round(size[0]))), max(1, int(round(size[1]))), max(1, int(round(size[2])))
    if crystal:
        paint_faceted_crystal(img, u, v, w, h, d, ramp, seed=seed)
    else:
        paint_organic_shaded(img, u, v, w, h, d, ramp, seed=seed)
    if emissive:
        for rect in box_faces(u, v, w, h, d).values():
            x, y, fw, fh = rect
            for py in range(y, y + fh):
                for px in range(x, x + fw):
                    put_px(glow, px, py, img.getpixel((px, py)))


def _paint_eye_socket(img: Image.Image, glow: Image.Image, uv: list[int], size: list[float],
                      socket_ramp, iris_color, pupil_color) -> None:
    _paint_cube(img, glow, uv, size, socket_ramp, seed=77)
    u, v = uv
    w, h, d = int(round(size[0])), int(round(size[1])), int(round(size[2]))
    faces = box_faces(u, v, w, h, d)
    for fname in ("front", "left", "right"):
        if fname not in faces:
            continue
        fx, fy, fw, fh = faces[fname]
        for py in range(fy, fy + fh):
            for px in range(fx, fx + fw):
                is_pupil = (px == fx + fw // 2 and py == fy + fh // 2)
                col = pupil_color if is_pupil else iris_color
                put_px(img, px, py, col)
                put_px(glow, px, py, col)


def _dragonling_animations(prefix: str, *, hovering: bool = False) -> dict:
    body_idle_y = "1.2 + Math.sin(query.anim_time * 160) * 0.6" if hovering else "Math.sin(query.anim_time * 160) * 0.25"
    wing_flap = "Math.sin(query.anim_time * 280) * 18" if hovering else "Math.sin(query.anim_time * 140) * 8"
    leg_walk = "Math.sin(query.anim_time * 360) * 28"
    return {
        "format_version": "1.8.0",
        "animations": {
            f"animation.{prefix}.idle": {
                "loop": True,
                "bones": {
                    "body": {"position": [0, body_idle_y, 0]},
                    "head": {"rotation": ["Math.sin(query.anim_time * 130) * 4", 0, 0]},
                    "jaw": {"rotation": ["4 + Math.sin(query.anim_time * 130) * 3", 0, 0]},
                    "left_wing": {"rotation": [0, 0, wing_flap]},
                    "right_wing": {"rotation": [0, 0, f"-({wing_flap})"]},
                    "tail_1": {"rotation": ["Math.sin(query.anim_time * 140) * 4", "Math.cos(query.anim_time * 120) * 8", 0]},
                    "tail_2": {"rotation": [0, "Math.cos(query.anim_time * 120 - 40) * 12", 0]},
                },
            },
            f"animation.{prefix}.walk": {
                "loop": True,
                "bones": {
                    "body": {"position": [0, "1.0 + Math.abs(Math.sin(query.anim_time * 360)) * 0.8", 0]},
                    "left_wing": {"rotation": [0, 0, "Math.sin(query.anim_time * 360) * 24"]},
                    "right_wing": {"rotation": [0, 0, "-Math.sin(query.anim_time * 360) * 24"]},
                    "front_left_leg": {"rotation": [leg_walk, 0, 0]},
                    "front_right_leg": {"rotation": [f"-({leg_walk})", 0, 0]},
                    "back_left_leg": {"rotation": [f"-({leg_walk})", 0, 0]},
                    "back_right_leg": {"rotation": [leg_walk, 0, 0]},
                    "tail_1": {"rotation": [4, "Math.sin(query.anim_time * 360) * 14", 0]},
                    "tail_2": {"rotation": [4, "Math.sin(query.anim_time * 360 - 45) * 18", 0]},
                },
            },
            f"animation.{prefix}.sit": {
                "loop": True,
                "bones": {
                    "body": {"position": [0, -1.8, 0], "rotation": [-12, 0, 0]},
                    "head": {"rotation": [12, 0, 0]},
                    "left_wing": {"rotation": [10, 0, -28]},
                    "right_wing": {"rotation": [10, 0, 28]},
                    "front_left_leg": {"rotation": [14, 0, 0]},
                    "front_right_leg": {"rotation": [14, 0, 0]},
                    "back_left_leg": {"rotation": [-55, 0, 0]},
                    "back_right_leg": {"rotation": [-55, 0, 0]},
                    "tail_1": {"rotation": [14, "Math.sin(query.anim_time * 120) * 10", 0]},
                },
            },
            f"animation.{prefix}.attack": {
                "loop": False,
                "animation_length": 0.45,
                "bones": {
                    "body": {"rotation": {"0.0": [0, 0, 0], "0.15": [-14, 0, 0], "0.3": [16, 0, 0], "0.45": [0, 0, 0]}},
                    "head": {"rotation": {"0.0": [0, 0, 0], "0.15": [-18, 0, 0], "0.3": [22, 0, 0], "0.45": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "0.18": [34, 0, 0], "0.45": [0, 0, 0]}},
                    "left_wing": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 0, 32], "0.45": [0, 0, 0]}},
                    "right_wing": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 0, -32], "0.45": [0, 0, 0]}},
                },
            },
        },
    }


def _geo_shell(identifier: str, bones: list[dict]) -> dict:
    return {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": f"geometry.{identifier}",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 2.0,
                    "visible_bounds_height": 1.6,
                    "visible_bounds_offset": [0, 0.7, 0],
                },
                "bones": bones,
            }
        ],
    }


def build_canopy_dragonling() -> tuple[dict, Image.Image, Image.Image]:
    bones = [
        {
            "name": "body",
            "pivot": [0, 6, 0],
            "cubes": [
                _cube([-3, 4, -4], [6, 5, 8], [0, 0]),          # Torso (28x13)
                _cube([-2, 3.6, -3.5], [4, 1, 7], [0, 14]),     # Emerald belly
                _cube([-1.5, 5.2, -4.4], [3, 2, 1], [24, 14]),  # Chest core
                _cube([-0.5, 9, -2], [1, 2, 4], [30, 0]),       # Dorsal fin
            ],
        },
        {
            "name": "head",
            "parent": "body",
            "pivot": [0, 8, -4],
            "cubes": [
                _cube([-2.5, 7, -8.5], [5, 4, 5], [0, 24]),      # Skull
                _cube([-2, 7.6, -10.5], [4, 2, 2], [22, 24]),    # Upper snout
                _cube([-2.6, 8.2, -7.8], [1, 2, 2], [36, 24]),   # Left eye socket
                _cube([1.6, 8.2, -7.8], [1, 2, 2], [44, 24]),    # Right eye socket
                _cube([-2.2, 10.2, -5.0], [1, 3, 1], [52, 24], [24, 0, -14]),  # Left horn
                _cube([1.2, 10.2, -5.0], [1, 3, 1], [58, 24], [24, 0, 14]),    # Right horn
            ],
        },
        {
            "name": "jaw",
            "parent": "head",
            "pivot": [0, 7.2, -8],
            "cubes": [_cube([-1.5, 6.6, -10.3], [3, 1, 2], [22, 30])],
        },
        {
            "name": "left_wing",
            "parent": "body",
            "pivot": [3, 8.5, -1],
            "cubes": [
                _cube([2.8, 8, -2], [5, 1, 4], [0, 36], [0, 0, -10]),
                _cube([7.2, 8.6, -1.5], [4, 1, 3], [20, 36], [0, 0, -20]),
            ],
        },
        {
            "name": "right_wing",
            "parent": "body",
            "pivot": [-3, 8.5, -1],
            "cubes": [
                _cube([-7.8, 8, -2], [5, 1, 4], [0, 44], [0, 0, 10]),
                _cube([-11.2, 8.6, -1.5], [4, 1, 3], [20, 44], [0, 0, 20]),
            ],
        },
        {
            "name": "front_left_leg",
            "parent": "body",
            "pivot": [2.2, 4.5, -2.5],
            "cubes": [_cube([1.2, 0, -3.5], [2, 4.5, 2], [0, 52])],
        },
        {
            "name": "front_right_leg",
            "parent": "body",
            "pivot": [-2.2, 4.5, -2.5],
            "cubes": [_cube([-3.2, 0, -3.5], [2, 4.5, 2], [10, 52])],
        },
        {
            "name": "back_left_leg",
            "parent": "body",
            "pivot": [2.2, 4.5, 2.5],
            "cubes": [_cube([1.2, 0, 1.5], [2, 4.5, 2], [20, 52])],
        },
        {
            "name": "back_right_leg",
            "parent": "body",
            "pivot": [-2.2, 4.5, 2.5],
            "cubes": [_cube([-3.2, 0, 1.5], [2, 4.5, 2], [30, 52])],
        },
        {
            "name": "tail_1",
            "parent": "body",
            "pivot": [0, 6, 4],
            "cubes": [
                _cube([-1.5, 5, 4], [3, 3, 4], [0, 62]),
                _cube([-0.5, 8, 5], [1, 1.5, 2], [16, 62]),
            ],
        },
        {
            "name": "tail_2",
            "parent": "tail_1",
            "pivot": [0, 6, 8],
            "cubes": [_cube([-1, 5.5, 8], [2, 2, 4], [24, 62])],
        },
    ]
    geo = _geo_shell("canopy_dragonling", bones)
    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))

    img = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    _paint_cube(img, glow, [0, 0], [6, 5, 8], JUNGLE_SCALE_RAMP, seed=1)
    _paint_cube(img, glow, [0, 14], [4, 1, 7], EMERALD_BELLY_RAMP, seed=2)
    _paint_cube(img, glow, [24, 14], [3, 2, 1], AMBER_HORN_RAMP, crystal=True, emissive=True, seed=3)
    _paint_cube(img, glow, [30, 0], [1, 2, 4], AMBER_HORN_RAMP, emissive=True, seed=4)
    _paint_cube(img, glow, [0, 24], [5, 4, 5], JUNGLE_SCALE_RAMP, seed=5)
    _paint_cube(img, glow, [22, 24], [4, 2, 2], JUNGLE_SCALE_RAMP, seed=6)
    _paint_eye_socket(img, glow, [36, 24], [1, 2, 2], DARK_CLAW_RAMP, (249, 115, 22, 255), (253, 224, 71, 255))
    _paint_eye_socket(img, glow, [44, 24], [1, 2, 2], DARK_CLAW_RAMP, (249, 115, 22, 255), (253, 224, 71, 255))
    _paint_cube(img, glow, [52, 24], [1, 3, 1], AMBER_HORN_RAMP, emissive=True, seed=7)
    _paint_cube(img, glow, [58, 24], [1, 3, 1], AMBER_HORN_RAMP, emissive=True, seed=8)
    _paint_cube(img, glow, [22, 30], [3, 1, 2], EMERALD_BELLY_RAMP, seed=9)
    for uv in ([0, 36], [20, 36], [0, 44], [20, 44]):
        _paint_cube(img, glow, uv, [5 if uv[0] == 0 else 4, 1, 4 if uv[0] == 0 else 3], WING_MEMBRANE_RAMP, seed=10 + uv[0])
    for uv in ([0, 52], [10, 52], [20, 52], [30, 52]):
        _paint_cube(img, glow, uv, [2, 4.5, 2], DARK_CLAW_RAMP, seed=15 + uv[0])
    _paint_cube(img, glow, [0, 62], [3, 3, 4], JUNGLE_SCALE_RAMP, seed=20)
    _paint_cube(img, glow, [16, 62], [1, 1.5, 2], AMBER_HORN_RAMP, emissive=True, seed=21)
    _paint_cube(img, glow, [24, 62], [2, 2, 4], EMERALD_BELLY_RAMP, seed=22)
    return geo, img, glow


def build_amber_dragonling() -> tuple[dict, Image.Image, Image.Image]:
    # Bipedal wyvern hatchling (no front legs, swept ram horns, bat wing-arms, magma stinger tail)
    bones = [
        {
            "name": "body",
            "pivot": [0, 6.5, 0],
            "cubes": [
                _cube([-3, 4.5, -3.5], [6, 5.5, 7], [0, 0], [10, 0, 0]),
                _cube([-2, 4.0, -3.2], [4, 1.2, 6], [0, 14], [10, 0, 0]),
                _cube([-1.5, 5.8, -4.0], [3, 2, 1], [24, 14]),
                _cube([-0.6, 9.8, -1.5], [1.2, 2, 4], [30, 0]),
            ],
        },
        {
            "name": "head",
            "parent": "body",
            "pivot": [0, 9.5, -3.5],
            "cubes": [
                _cube([-2.5, 8.5, -7.8], [5, 4, 4.8], [0, 24]),
                _cube([-1.8, 9.0, -9.8], [3.6, 2, 2], [22, 24]),
                _cube([-2.6, 9.7, -7.2], [1, 2, 2], [36, 24]),
                _cube([1.6, 9.7, -7.2], [1, 2, 2], [44, 24]),
                _cube([-2.4, 11.4, -4.5], [1, 4, 1], [52, 24], [30, -15, -16]),
                _cube([1.4, 11.4, -4.5], [1, 4, 1], [58, 24], [30, 15, 16]),
            ],
        },
        {
            "name": "jaw",
            "parent": "head",
            "pivot": [0, 8.6, -7.5],
            "cubes": [_cube([-1.5, 8.0, -9.6], [3, 1, 2], [22, 30])],
        },
        {
            "name": "left_wing",
            "parent": "body",
            "pivot": [3, 9.0, -1],
            "cubes": [
                _cube([2.8, 8.2, -2], [5, 1, 4], [0, 36], [0, 0, -12]),
                _cube([7.2, 8.9, -1.5], [5, 1, 4], [20, 36], [0, 0, -22]),
            ],
        },
        {
            "name": "right_wing",
            "parent": "body",
            "pivot": [-3, 9.0, -1],
            "cubes": [
                _cube([-7.8, 8.2, -2], [5, 1, 4], [0, 44], [0, 0, 12]),
                _cube([-12.2, 8.9, -1.5], [5, 1, 4], [20, 44], [0, 0, 22]),
            ],
        },
        {
            "name": "back_left_leg",
            "parent": "body",
            "pivot": [2.2, 5.0, 0.5],
            "cubes": [_cube([1.2, 0, -0.5], [2, 5, 2], [0, 52])],
        },
        {
            "name": "back_right_leg",
            "parent": "body",
            "pivot": [-2.2, 5.0, 0.5],
            "cubes": [_cube([-3.2, 0, -0.5], [2, 5, 2], [12, 52])],
        },
        {
            "name": "tail_1",
            "parent": "body",
            "pivot": [0, 6.0, 3.5],
            "cubes": [_cube([-1.5, 5.0, 3.5], [3, 3, 5], [0, 62])],
        },
        {
            "name": "tail_2",
            "parent": "tail_1",
            "pivot": [0, 6.0, 8],
            "cubes": [_cube([-1.5, 5.2, 8], [3, 3, 3], [20, 62])],  # Magma stinger bulb
        },
    ]
    geo = _geo_shell("amber_dragonling", bones)
    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))

    img = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    _paint_cube(img, glow, [0, 0], [6, 6, 7], OBSIDIAN_RAMP, seed=31)
    _paint_cube(img, glow, [0, 14], [4, 1, 6], MAGMA_RAMP, emissive=True, seed=32)
    _paint_cube(img, glow, [24, 14], [3, 2, 1], MAGMA_RAMP, crystal=True, emissive=True, seed=33)
    _paint_cube(img, glow, [30, 0], [1, 2, 4], MAGMA_RAMP, emissive=True, seed=34)
    _paint_cube(img, glow, [0, 24], [5, 4, 5], OBSIDIAN_RAMP, seed=35)
    _paint_cube(img, glow, [22, 24], [4, 2, 2], OBSIDIAN_RAMP, seed=36)
    _paint_eye_socket(img, glow, [36, 24], [1, 2, 2], OBSIDIAN_RAMP, (249, 115, 22, 255), (254, 240, 138, 255))
    _paint_eye_socket(img, glow, [44, 24], [1, 2, 2], OBSIDIAN_RAMP, (249, 115, 22, 255), (254, 240, 138, 255))
    _paint_cube(img, glow, [52, 24], [1, 4, 1], MAGMA_RAMP, emissive=True, seed=37)
    _paint_cube(img, glow, [58, 24], [1, 4, 1], MAGMA_RAMP, emissive=True, seed=38)
    _paint_cube(img, glow, [22, 30], [3, 1, 2], MAGMA_RAMP, seed=39)
    for uv in ([0, 36], [20, 36], [0, 44], [20, 44]):
        _paint_cube(img, glow, uv, [5, 1, 4], MAGMA_RAMP if uv[0] > 0 else OBSIDIAN_RAMP, emissive=(uv[0] > 0), seed=40 + uv[0])
    _paint_cube(img, glow, [0, 52], [2, 5, 2], DARK_CLAW_RAMP, seed=45)
    _paint_cube(img, glow, [12, 52], [2, 5, 2], DARK_CLAW_RAMP, seed=46)
    _paint_cube(img, glow, [0, 62], [3, 3, 5], OBSIDIAN_RAMP, seed=47)
    _paint_cube(img, glow, [20, 62], [3, 3, 3], MAGMA_RAMP, crystal=True, emissive=True, seed=48)
    return geo, img, glow


def build_storm_dragonling() -> tuple[dict, Image.Image, Image.Image]:
    # Legless hovering sky-serpent hatchling with wide glider wings & fanned tail rudder
    bones = [
        {
            "name": "body",
            "pivot": [0, 7, 0],
            "cubes": [
                _cube([-2.5, 5, -4], [5, 4.5, 7], [0, 0]),
                _cube([-1.8, 4.5, -3.5], [3.6, 1, 6], [0, 14]),
                _cube([-1.2, 6.2, -4.4], [2.4, 1.8, 1], [24, 14]),
            ],
        },
        {
            "name": "head",
            "parent": "body",
            "pivot": [0, 8, -4],
            "cubes": [
                _cube([-2.5, 6.8, -8.5], [5, 4, 5], [0, 24]),
                _cube([-1.5, 7.2, -10.2], [3, 2, 2], [22, 24]),
                _cube([-2.6, 8.0, -7.5], [1, 2, 2], [36, 24]),
                _cube([1.6, 8.0, -7.5], [1, 2, 2], [44, 24]),
                _cube([-2.4, 10.0, -5.0], [1, 3, 1], [52, 24], [20, 0, -24]),
                _cube([1.4, 10.0, -5.0], [1, 3, 1], [58, 24], [20, 0, 24]),
            ],
        },
        {
            "name": "jaw",
            "parent": "head",
            "pivot": [0, 7.0, -7.8],
            "cubes": [_cube([-1.5, 6.4, -9.8], [3, 1, 2], [22, 30])],
        },
        {
            "name": "left_wing",
            "parent": "body",
            "pivot": [2.5, 8.5, -1.5],
            "cubes": [
                _cube([2.3, 8.0, -2.5], [6, 1, 4], [0, 36], [0, 0, -8]),
                _cube([7.8, 8.5, -2.0], [5, 1, 3], [22, 36], [0, 0, -16]),
            ],
        },
        {
            "name": "right_wing",
            "parent": "body",
            "pivot": [-2.5, 8.5, -1.5],
            "cubes": [
                _cube([-8.3, 8.0, -2.5], [6, 1, 4], [0, 44], [0, 0, 8]),
                _cube([-12.8, 8.5, -2.0], [5, 1, 3], [22, 44], [0, 0, 16]),
            ],
        },
        {
            "name": "tail_1",
            "parent": "body",
            "pivot": [0, 6.5, 3],
            "cubes": [_cube([-1.8, 5.5, 3], [3.6, 3, 4], [0, 54])],
        },
        {
            "name": "tail_2",
            "parent": "tail_1",
            "pivot": [0, 6.5, 7],
            "cubes": [
                _cube([-1.2, 5.8, 7], [2.4, 2, 4], [18, 54]),
                _cube([-3.0, 6.2, 10], [6, 1, 3], [32, 54]),  # Fanned tail rudder
            ],
        },
    ]
    geo = _geo_shell("storm_dragonling", bones)
    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))

    img = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    _paint_cube(img, glow, [0, 0], [5, 5, 7], STORM_NAVY_RAMP, seed=51)
    _paint_cube(img, glow, [0, 14], [4, 1, 6], SKY_CYAN_RAMP, seed=52)
    _paint_cube(img, glow, [24, 14], [2, 2, 1], ELECTRIC_GOLD_RAMP, crystal=True, emissive=True, seed=53)
    _paint_cube(img, glow, [0, 24], [5, 4, 5], STORM_NAVY_RAMP, seed=54)
    _paint_cube(img, glow, [22, 24], [3, 2, 2], STORM_NAVY_RAMP, seed=55)
    _paint_eye_socket(img, glow, [36, 24], [1, 2, 2], STORM_NAVY_RAMP, (56, 217, 192, 255), (254, 249, 195, 255))
    _paint_eye_socket(img, glow, [44, 24], [1, 2, 2], STORM_NAVY_RAMP, (56, 217, 192, 255), (254, 249, 195, 255))
    _paint_cube(img, glow, [52, 24], [1, 3, 1], ELECTRIC_GOLD_RAMP, emissive=True, seed=56)
    _paint_cube(img, glow, [58, 24], [1, 3, 1], ELECTRIC_GOLD_RAMP, emissive=True, seed=57)
    _paint_cube(img, glow, [22, 30], [3, 1, 2], SKY_CYAN_RAMP, seed=58)
    for uv in ([0, 36], [22, 36], [0, 44], [22, 44]):
        _paint_cube(img, glow, uv, [6 if uv[0] == 0 else 5, 1, 4 if uv[0] == 0 else 3],
                    SKY_CYAN_RAMP if uv[0] > 0 else STORM_NAVY_RAMP, emissive=(uv[0] > 0), seed=60 + uv[0])
    _paint_cube(img, glow, [0, 54], [4, 3, 4], STORM_NAVY_RAMP, seed=65)
    _paint_cube(img, glow, [18, 54], [2, 2, 4], STORM_NAVY_RAMP, seed=66)
    _paint_cube(img, glow, [32, 54], [6, 1, 3], ELECTRIC_GOLD_RAMP, emissive=True, seed=67)
    return geo, img, glow


def make_dragon_egg_texture(ramp: list[tuple[int, int, int, int]], crack_color: tuple[int, int, int, int],
                            stage: int, seed: int) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    for y in range(16):
        for x in range(16):
            idx = min(len(ramp) - 1, max(0, (len(ramp) - 1) * (15 - y) // 15 + ((x + y + seed) % 3 == 0) - 1))
            put_px(img, x, y, ramp[idx])
    # Add organic mottled egg spots
    draw = ImageDraw.Draw(img)
    for sx, sy in ((3, 4), (10, 3), (6, 10), (12, 11), (2, 12)):
        draw.rectangle((sx, sy, sx + 1, sy + 1), fill=ramp[min(len(ramp) - 1, 4)])
    if stage >= 1:
        # Hairline glowing elemental cracks
        for cx, cy in ((7, 3), (8, 4), (8, 5), (7, 6), (6, 7), (7, 8)):
            put_px(img, cx, cy, crack_color)
    if stage >= 2:
        # Wide glowing fissures ready to hatch
        for cx, cy in ((9, 5), (10, 6), (11, 7), (10, 8), (9, 9), (8, 10), (7, 11), (5, 7), (4, 8), (4, 9)):
            put_px(img, cx, cy, crack_color)
    return img


def main() -> None:
    specs = [
        ("canopy_dragonling", build_canopy_dragonling, False),
        ("amber_dragonling", build_amber_dragonling, False),
        ("storm_dragonling", build_storm_dragonling, True),
    ]
    for name, builder, hovering in specs:
        geo, img, glow = builder()
        _save_json(GEO_DIR / f"{name}.geo.json", geo)
        _save_json(ANIM_DIR / f"{name}.animation.json", _dragonling_animations(name, hovering=hovering))
        TEX_DIR.mkdir(parents=True, exist_ok=True)
        img.save(TEX_DIR / f"{name}.png")
        glow.save(TEX_DIR / f"{name}_glowmask.png")

    BLOCK_TEX_DIR.mkdir(parents=True, exist_ok=True)
    egg_specs = [
        ("canopy_wyvern_egg", JUNGLE_SCALE_RAMP, (74, 222, 128, 255), 101),
        ("amber_wyvern_egg", OBSIDIAN_RAMP, (251, 164, 56, 255), 202),
        ("storm_glidewing_egg", STORM_NAVY_RAMP, (125, 249, 255, 255), 303),
    ]
    for egg_name, ramp, crack, seed in egg_specs:
        for stage in (0, 1, 2):
            make_dragon_egg_texture(ramp, crack, stage, seed).save(BLOCK_TEX_DIR / f"{egg_name}_{stage}.png")


if __name__ == "__main__":
    main()
