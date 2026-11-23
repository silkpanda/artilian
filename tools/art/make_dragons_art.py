"""Generates the shared 256x128 GeckoLib dragon rig and Phase 5 Stage 4/5 dragon species art.

Shared rig:
- geo/entity/dragon_rig.geo.json
- animations/entity/dragon_rig.animation.json (idle, walk, fly, attack)

Stage 4 species:
- canopy_wyvern: Canopy Emerald & Ember Amber jungle wyvern (textures/entity/canopy_wyvern.png + _glowmask.png)
- item/dragon_scale.png: 16x16 faceted emerald-amber dragon scale drop sprite
"""
from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw

from art_lib import (
    assert_uv_layout_clean,
    box_faces,
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

# Canopy Wyvern palette (dark jungle slate-emerald anchor + Canopy Emerald + Ember Amber accents)
JUNGLE_SCALE_RAMP = [
    (16, 36, 28, 255),
    (24, 52, 40, 255),
    (34, 72, 54, 255),
    (46, 94, 70, 255),
    (60, 118, 88, 255),
    (82, 146, 110, 255),
]

EMERALD_BELLY_RAMP = [
    (22, 92, 52, 255),
    (30, 128, 70, 255),
    (34, 166, 86, 255),
    (34, 197, 94, 255),
    (74, 222, 128, 255),
    (134, 239, 172, 255),
]

WING_MEMBRANE_RAMP = [
    (18, 68, 44, 255),
    (26, 98, 62, 255),
    (34, 134, 80, 255),
    (46, 172, 98, 255),
    (74, 212, 124, 255),
    (118, 238, 158, 255),
]

AMBER_HORN_RAMP = [
    (104, 42, 10, 255),
    (148, 62, 14, 255),
    (196, 86, 18, 255),
    (249, 115, 22, 255),
    (251, 164, 56, 255),
    (253, 224, 71, 255),
]

DARK_CLAW_RAMP = [
    (18, 16, 22, 255),
    (30, 26, 36, 255),
    (44, 38, 52, 255),
    (62, 54, 72, 255),
    (84, 74, 96, 255),
    (112, 100, 126, 255),
]


def _save_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def _cube(origin: list[float], size: list[float], uv: list[int], rotation: list[float] | None = None) -> dict:
    c: dict = {"origin": origin, "size": size, "uv": uv}
    if rotation is not None:
        c["rotation"] = rotation
        c["pivot"] = [
            origin[0] + size[0] * 0.5,
            origin[1] + size[1] * 0.5,
            origin[2] + size[2] * 0.5,
        ]
    return c


def build_shared_dragon_rig() -> dict:
    """Build the shared 256x128 GeckoLib dragon rig used by Stage 4 & Stage 5 wild dragons."""
    bones = [
        {
            "name": "root",
            "pivot": [0, 0, 0],
        },
        {
            "name": "body",
            "parent": "root",
            "pivot": [0, 14, 0],
            "cubes": [
                _cube([-6, 11, -10], [12, 10, 12], [0, 0]),
                _cube([-3, 12, -10.8], [6, 6, 2], [48, 0]),
                _cube([-4.5, 11.5, 2], [9, 8, 9], [64, 0]),
                _cube([-1, 21, -7], [2, 4, 5], [100, 0], [-14, 0, 0]),
                _cube([-1, 19.5, 2], [2, 4, 5], [114, 0], [-18, 0, 0]),
            ],
        },
        {
            "name": "neck_base",
            "parent": "body",
            "pivot": [0, 18, -9],
            "cubes": [
                _cube([-3, 16, -15], [6, 7, 7], [128, 0], [-22, 0, 0]),
            ],
        },
        {
            "name": "neck_upper",
            "parent": "neck_base",
            "pivot": [0, 22, -14],
            "cubes": [
                _cube([-2.5, 21, -19], [5, 7, 6], [154, 0], [12, 0, 0]),
                _cube([-1, 27, -18], [2, 3, 4], [176, 0], [-15, 0, 0]),
            ],
        },
        {
            "name": "head",
            "parent": "neck_upper",
            "pivot": [0, 27, -18],
            "cubes": [
                _cube([-4, 25, -26], [8, 6, 8], [0, 24]),
                _cube([-3, 26.2, -32], [6, 3.8, 6], [32, 24]),
                _cube([1.5, 29.5, -27.5], [3, 1.5, 4], [56, 24], [0, 0, 10]),
                _cube([-4.5, 29.5, -27.5], [3, 1.5, 4], [70, 24], [0, 0, -10]),
                _cube([2.8, 27.2, -26.5], [1.6, 2.2, 2.4], [84, 24]),
                _cube([-4.4, 27.2, -26.5], [1.6, 2.2, 2.4], [92, 24]),
                _cube([2.2, 30.2, -23], [2, 6, 2], [100, 24], [-28, 12, 12]),
                _cube([-4.2, 30.2, -23], [2, 6, 2], [108, 24], [-28, -12, -12]),
                _cube([-1, 31, -24], [2, 3, 5], [116, 24], [-20, 0, 0]),
            ],
        },
        {
            "name": "jaw",
            "parent": "head",
            "pivot": [0, 25.8, -25],
            "cubes": [
                _cube([-2.5, 24.3, -31.5], [5, 2, 6.5], [130, 24]),
            ],
        },
        {
            "name": "left_wing_inner",
            "parent": "body",
            "pivot": [5.5, 20, -6],
            "rotation": [0, 0, -8],
            "cubes": [
                _cube([5.5, 19, -7], [12, 2.5, 2.5], [0, 40]),
                _cube([5.5, 19.8, -4.5], [12, 1, 10], [30, 40]),
            ],
        },
        {
            "name": "left_wing_outer",
            "parent": "left_wing_inner",
            "pivot": [17.5, 20.2, -6],
            "rotation": [0, 0, 16],
            "cubes": [
                _cube([17.5, 19.2, -6.8], [14, 2, 2], [74, 40], [0, -10, 0]),
                _cube([16.5, 20.2, -9], [1.5, 1.5, 3], [106, 40], [15, 0, 0]),
                _cube([17.5, 19.8, -4.8], [14, 1, 9], [118, 40], [0, -10, 0]),
            ],
        },
        {
            "name": "right_wing_inner",
            "parent": "body",
            "pivot": [-5.5, 20, -6],
            "rotation": [0, 0, 8],
            "cubes": [
                _cube([-17.5, 19, -7], [12, 2.5, 2.5], [0, 54]),
                _cube([-17.5, 19.8, -4.5], [12, 1, 10], [30, 54]),
            ],
        },
        {
            "name": "right_wing_outer",
            "parent": "right_wing_inner",
            "pivot": [-17.5, 20.2, -6],
            "rotation": [0, 0, -16],
            "cubes": [
                _cube([-31.5, 19.2, -6.8], [14, 2, 2], [74, 54], [0, 10, 0]),
                _cube([-18, 20.2, -9], [1.5, 1.5, 3], [106, 54], [15, 0, 0]),
                _cube([-31.5, 19.8, -4.8], [14, 1, 9], [118, 54], [0, 10, 0]),
            ],
        },
        {
            "name": "front_left_leg",
            "parent": "body",
            "pivot": [5.5, 12, -7],
            "cubes": [
                _cube([4, 2, -9], [3.5, 10, 4], [0, 68]),
                _cube([3.5, 0, -11], [4.5, 2, 5], [16, 68]),
            ],
        },
        {
            "name": "front_right_leg",
            "parent": "body",
            "pivot": [-5.5, 12, -7],
            "cubes": [
                _cube([-7.5, 2, -9], [3.5, 10, 4], [36, 68]),
                _cube([-8, 0, -11], [4.5, 2, 5], [52, 68]),
            ],
        },
        {
            "name": "back_left_leg",
            "parent": "body",
            "pivot": [4.5, 12, 8],
            "cubes": [
                _cube([3, 2, 6], [4, 10, 5], [72, 68]),
                _cube([2.5, 0, 4], [5, 2, 6], [90, 68]),
            ],
        },
        {
            "name": "back_right_leg",
            "parent": "body",
            "pivot": [-4.5, 12, 8],
            "cubes": [
                _cube([-7, 2, 6], [4, 10, 5], [112, 68]),
                _cube([-7.5, 0, 4], [5, 2, 6], [130, 68]),
            ],
        },
        {
            "name": "tail_1",
            "parent": "body",
            "pivot": [0, 14.5, 11],
            "cubes": [
                _cube([-3, 12, 11], [6, 5, 9], [0, 86]),
                _cube([-0.8, 17, 13], [1.6, 3, 4], [30, 86], [-16, 0, 0]),
            ],
        },
        {
            "name": "tail_2",
            "parent": "tail_1",
            "pivot": [0, 14.5, 20],
            "cubes": [
                _cube([-2.2, 12.6, 20], [4.4, 4, 9], [42, 86]),
                _cube([-0.8, 16.6, 22], [1.6, 2.5, 4], [68, 86], [-16, 0, 0]),
            ],
        },
        {
            "name": "tail_3",
            "parent": "tail_2",
            "pivot": [0, 14.5, 29],
            "cubes": [
                _cube([-1.5, 13.2, 29], [3, 2.8, 8], [80, 86]),
                _cube([-3, 14, 34], [6, 1.2, 5], [102, 86]),
            ],
        },
    ]

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.dragon_rig",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 6,
                    "visible_bounds_height": 4,
                    "visible_bounds_offset": [0, 1.5, 0],
                },
                "bones": bones,
            }
        ],
    }
    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))
    return geo


def build_shared_dragon_animations() -> dict:
    """Build shared idle, walk, fly, and attack animations for dragon_rig."""
    return {
        "format_version": "1.8.0",
        "animations": {
            "animation.dragon_rig.idle": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "body": {"position": {"0.0": [0, 0, 0], "1.2": [0, 0.4, 0], "2.4": [0, 0, 0]}},
                    "neck_base": {"rotation": {"0.0": [0, 0, 0], "1.2": [4, 0, 0], "2.4": [0, 0, 0]}},
                    "head": {"rotation": {"0.0": [0, 0, 0], "1.2": [-5, 0, 0], "2.4": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "1.2": [6, 0, 0], "2.4": [0, 0, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, -6], "1.2": [0, 0, -14], "2.4": [0, 0, -6]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 6], "1.2": [0, 0, 14], "2.4": [0, 0, 6]}},
                    "tail_1": {"rotation": {"0.0": [0, -6, 0], "1.2": [0, 6, 0], "2.4": [0, -6, 0]}},
                    "tail_2": {"rotation": {"0.0": [0, -10, 0], "1.2": [0, 10, 0], "2.4": [0, -10, 0]}},
                    "tail_3": {"rotation": {"0.0": [0, -12, 0], "1.2": [0, 12, 0], "2.4": [0, -12, 0]}},
                },
            },
            "animation.dragon_rig.walk": {
                "loop": True,
                "animation_length": 0.9,
                "bones": {
                    "body": {"position": {"0.0": [0, 0, 0], "0.22": [0, 0.7, 0], "0.45": [0, 0, 0], "0.68": [0, 0.7, 0], "0.9": [0, 0, 0]}},
                    "front_left_leg": {"rotation": {"0.0": [-24, 0, 0], "0.45": [24, 0, 0], "0.9": [-24, 0, 0]}},
                    "front_right_leg": {"rotation": {"0.0": [24, 0, 0], "0.45": [-24, 0, 0], "0.9": [24, 0, 0]}},
                    "back_left_leg": {"rotation": {"0.0": [22, 0, 0], "0.45": [-22, 0, 0], "0.9": [22, 0, 0]}},
                    "back_right_leg": {"rotation": {"0.0": [-22, 0, 0], "0.45": [22, 0, 0], "0.9": [-22, 0, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, -12], "0.45": [0, 0, -20], "0.9": [0, 0, -12]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 12], "0.45": [0, 0, 20], "0.9": [0, 0, 12]}},
                    "tail_1": {"rotation": {"0.0": [0, 10, 0], "0.45": [0, -10, 0], "0.9": [0, 10, 0]}},
                    "tail_2": {"rotation": {"0.0": [0, 14, 0], "0.45": [0, -14, 0], "0.9": [0, 14, 0]}},
                },
            },
            "animation.dragon_rig.fly": {
                "loop": True,
                "animation_length": 0.8,
                "bones": {
                    "body": {"position": {"0.0": [0, 1.5, 0], "0.4": [0, -1.0, 0], "0.8": [0, 1.5, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, -28], "0.4": [0, 0, 28], "0.8": [0, 0, -28]}},
                    "left_wing_outer": {"rotation": {"0.0": [0, 0, -18], "0.4": [0, 0, 22], "0.8": [0, 0, -18]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 28], "0.4": [0, 0, -28], "0.8": [0, 0, 28]}},
                    "right_wing_outer": {"rotation": {"0.0": [0, 0, 18], "0.4": [0, 0, -22], "0.8": [0, 0, 18]}},
                    "front_left_leg": {"rotation": [32, 0, 0]},
                    "front_right_leg": {"rotation": [32, 0, 0]},
                    "back_left_leg": {"rotation": [38, 0, 0]},
                    "back_right_leg": {"rotation": [38, 0, 0]},
                    "tail_1": {"rotation": {"0.0": [-8, 0, 0], "0.4": [8, 0, 0], "0.8": [-8, 0, 0]}},
                },
            },
            "animation.dragon_rig.attack": {
                "animation_length": 0.55,
                "bones": {
                    "neck_base": {"rotation": {"0.0": [0, 0, 0], "0.18": [-20, 0, 0], "0.35": [24, 0, 0], "0.55": [0, 0, 0]}},
                    "head": {"rotation": {"0.0": [0, 0, 0], "0.18": [-18, 0, 0], "0.35": [16, 0, 0], "0.55": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "0.18": [38, 0, 0], "0.35": [6, 0, 0], "0.55": [0, 0, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, -8], "0.2": [0, 0, -34], "0.55": [0, 0, -8]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 8], "0.2": [0, 0, 34], "0.55": [0, 0, 8]}},
                },
            },
        },
    }


def _paint_cube(img: Image.Image, glow: Image.Image, uv: list[int], size: list[float],
                ramp: list[tuple[int, int, int, int]], style: str = "organic",
                seed: int = 1, glow_faces: set[str] | None = None,
                glow_color: tuple[int, int, int, int] = (249, 115, 22, 255)) -> None:
    u, v = int(uv[0]), int(uv[1])
    w, h, d = max(1, int(round(size[0]))), max(1, int(round(size[1]))), max(1, int(round(size[2])))
    if style == "metal":
        paint_forged_metal(img, u, v, w, h, d, ramp, seed=seed)
    elif style == "crystal":
        paint_faceted_crystal(img, u, v, w, h, d, ramp, seed=seed)
    elif style == "stone":
        paint_weathered_stone(img, u, v, w, h, d, ramp, seed=seed)
    else:
        paint_organic_shaded(img, u, v, w, h, d, ramp, seed=seed)

    if glow_faces:
        faces = box_faces(u, v, w, h, d)
        for fname, rect in faces.items():
            if fname in glow_faces:
                rx, ry, rw, rh = rect
                for dy in range(rh):
                    for dx in range(rw):
                        put_px(glow, rx + dx, ry + dy, glow_color)


def paint_canopy_wyvern(geo: dict) -> tuple[Image.Image, Image.Image]:
    """Paint 256x128 Canopy Wyvern texture + emissive glowmask."""
    img = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))

    bones = {b["name"]: b for b in geo["minecraft:geometry"][0]["bones"]}
    amber_glow = (251, 164, 56, 255)
    emerald_glow = (74, 222, 128, 255)

    # Body & dorsal spines
    body_cubes = bones["body"]["cubes"]
    _paint_cube(img, glow, body_cubes[0]["uv"], body_cubes[0]["size"], JUNGLE_SCALE_RAMP, "organic", seed=11)
    # Paint emerald belly stripe on bottom of chest
    belly_rect = box_faces(0, 0, 12, 10, 12)["bottom"]
    for dy in range(belly_rect[3]):
        for dx in range(belly_rect[2]):
            put_px(img, belly_rect[0] + dx, belly_rect[1] + dy, EMERALD_BELLY_RAMP[(dx + dy) % 3 + 1])
    _paint_cube(img, glow, body_cubes[1]["uv"], body_cubes[1]["size"], AMBER_HORN_RAMP, "crystal", seed=13,
                glow_faces={"front", "top", "bottom", "east", "west"}, glow_color=amber_glow)
    _paint_cube(img, glow, body_cubes[2]["uv"], body_cubes[2]["size"], JUNGLE_SCALE_RAMP, "organic", seed=14)
    _paint_cube(img, glow, body_cubes[3]["uv"], body_cubes[3]["size"], AMBER_HORN_RAMP, "crystal", seed=15,
                glow_faces={"top", "east", "west"}, glow_color=amber_glow)
    _paint_cube(img, glow, body_cubes[4]["uv"], body_cubes[4]["size"], AMBER_HORN_RAMP, "crystal", seed=16,
                glow_faces={"top", "east", "west"}, glow_color=amber_glow)

    # Neck
    for i, bname in enumerate(("neck_base", "neck_upper")):
        for j, c in enumerate(bones[bname]["cubes"]):
            ramp = AMBER_HORN_RAMP if (bname == "neck_upper" and j == 1) else JUNGLE_SCALE_RAMP
            gfaces = {"top", "front"} if (bname == "neck_upper" and j == 1) else None
            _paint_cube(img, glow, c["uv"], c["size"], ramp, "organic", seed=20 + i * 5 + j,
                        glow_faces=gfaces, glow_color=amber_glow)

    # Head & Jaw
    hc = bones["head"]["cubes"]
    _paint_cube(img, glow, hc[0]["uv"], hc[0]["size"], JUNGLE_SCALE_RAMP, "organic", seed=30)
    _paint_cube(img, glow, hc[1]["uv"], hc[1]["size"], JUNGLE_SCALE_RAMP, "organic", seed=31)
    _paint_cube(img, glow, hc[2]["uv"], hc[2]["size"], EMERALD_BELLY_RAMP, "organic", seed=32)
    _paint_cube(img, glow, hc[3]["uv"], hc[3]["size"], EMERALD_BELLY_RAMP, "organic", seed=33)
    # Glowing eyes
    _paint_cube(img, glow, hc[4]["uv"], hc[4]["size"], AMBER_HORN_RAMP, "crystal", seed=34,
                glow_faces={"front", "east", "west", "top"}, glow_color=amber_glow)
    _paint_cube(img, glow, hc[5]["uv"], hc[5]["size"], AMBER_HORN_RAMP, "crystal", seed=35,
                glow_faces={"front", "east", "west", "top"}, glow_color=amber_glow)
    # Horns & crest
    _paint_cube(img, glow, hc[6]["uv"], hc[6]["size"], AMBER_HORN_RAMP, "crystal", seed=36,
                glow_faces={"top"}, glow_color=amber_glow)
    _paint_cube(img, glow, hc[7]["uv"], hc[7]["size"], AMBER_HORN_RAMP, "crystal", seed=37,
                glow_faces={"top"}, glow_color=amber_glow)
    _paint_cube(img, glow, hc[8]["uv"], hc[8]["size"], EMERALD_BELLY_RAMP, "crystal", seed=38,
                glow_faces={"top"}, glow_color=emerald_glow)
    # Jaw
    jc = bones["jaw"]["cubes"][0]
    _paint_cube(img, glow, jc["uv"], jc["size"], EMERALD_BELLY_RAMP, "organic", seed=39)

    # Wings (armored leading edges + emerald membranes + glowing vein runes)
    for side_idx, (inner, outer) in enumerate((("left_wing_inner", "left_wing_outer"),
                                               ("right_wing_inner", "right_wing_outer"))):
        ic = bones[inner]["cubes"]
        _paint_cube(img, glow, ic[0]["uv"], ic[0]["size"], JUNGLE_SCALE_RAMP, "organic", seed=50 + side_idx * 10)
        _paint_cube(img, glow, ic[1]["uv"], ic[1]["size"], WING_MEMBRANE_RAMP, "organic", seed=51 + side_idx * 10)
        # Wing vein glow stripe across inner membrane top/bottom
        mem_rect = box_faces(ic[1]["uv"][0], ic[1]["uv"][1], 12, 1, 10)["top"]
        for dx in range(2, mem_rect[2] - 1, 4):
            for dy in range(mem_rect[3]):
                put_px(img, mem_rect[0] + dx, mem_rect[1] + dy, AMBER_HORN_RAMP[4])
                put_px(glow, mem_rect[0] + dx, mem_rect[1] + dy, emerald_glow)

        oc = bones[outer]["cubes"]
        _paint_cube(img, glow, oc[0]["uv"], oc[0]["size"], JUNGLE_SCALE_RAMP, "organic", seed=52 + side_idx * 10)
        _paint_cube(img, glow, oc[1]["uv"], oc[1]["size"], DARK_CLAW_RAMP, "metal", seed=53 + side_idx * 10)
        _paint_cube(img, glow, oc[2]["uv"], oc[2]["size"], WING_MEMBRANE_RAMP, "organic", seed=54 + side_idx * 10)
        mem2_rect = box_faces(oc[2]["uv"][0], oc[2]["uv"][1], 14, 1, 9)["top"]
        for dx in range(2, mem2_rect[2] - 1, 4):
            for dy in range(mem2_rect[3]):
                put_px(img, mem2_rect[0] + dx, mem2_rect[1] + dy, AMBER_HORN_RAMP[4])
                put_px(glow, mem2_rect[0] + dx, mem2_rect[1] + dy, emerald_glow)

    # Legs & clawed feet
    for leg_idx, lname in enumerate(("front_left_leg", "front_right_leg", "back_left_leg", "back_right_leg")):
        lc = bones[lname]["cubes"]
        _paint_cube(img, glow, lc[0]["uv"], lc[0]["size"], JUNGLE_SCALE_RAMP, "organic", seed=70 + leg_idx * 3)
        _paint_cube(img, glow, lc[1]["uv"], lc[1]["size"], DARK_CLAW_RAMP, "metal", seed=71 + leg_idx * 3)

    # Tail segments & spines
    for t_idx, tname in enumerate(("tail_1", "tail_2", "tail_3")):
        tc = bones[tname]["cubes"]
        _paint_cube(img, glow, tc[0]["uv"], tc[0]["size"], JUNGLE_SCALE_RAMP, "organic", seed=90 + t_idx * 4)
        ramp2 = EMERALD_BELLY_RAMP if tname == "tail_3" else AMBER_HORN_RAMP
        _paint_cube(img, glow, tc[1]["uv"], tc[1]["size"], ramp2, "crystal", seed=91 + t_idx * 4,
                    glow_faces={"top"}, glow_color=amber_glow if tname != "tail_3" else emerald_glow)

    return img, glow


def make_dragon_scale_sprite() -> Image.Image:
    """Create a 16x16 faceted emerald-and-amber Dragon Scale item sprite."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Diamond/shield scale silhouette
    outline = (16, 36, 28, 255)
    poly = [(8, 1), (13, 5), (12, 11), (8, 14), (4, 11), (3, 5)]
    draw.polygon(poly, fill=EMERALD_BELLY_RAMP[2], outline=outline)
    # Inner bevel & amber highlight
    draw.polygon([(8, 3), (11, 6), (10, 10), (8, 12), (6, 10), (5, 6)], fill=EMERALD_BELLY_RAMP[4])
    draw.line([(8, 3), (8, 12)], fill=AMBER_HORN_RAMP[4])
    draw.point((7, 4), fill=AMBER_HORN_RAMP[5])
    draw.point((8, 4), fill=AMBER_HORN_RAMP[5])
    return img


def main() -> None:
    geo = build_shared_dragon_rig()
    _save_json(GEO_DIR / "dragon_rig.geo.json", geo)
    _save_json(ANIM_DIR / "dragon_rig.animation.json", build_shared_dragon_animations())

    TEX_DIR.mkdir(parents=True, exist_ok=True)
    wyvern_img, wyvern_glow = paint_canopy_wyvern(geo)
    wyvern_img.save(TEX_DIR / "canopy_wyvern.png")
    wyvern_glow.save(TEX_DIR / "canopy_wyvern_glowmask.png")

    ITEM_TEX_DIR.mkdir(parents=True, exist_ok=True)
    make_dragon_scale_sprite().save(ITEM_TEX_DIR / "dragon_scale.png")
    print("Generated shared dragon_rig.geo.json, dragon_rig.animation.json, canopy_wyvern.png (+glowmask), and dragon_scale.png")


if __name__ == "__main__":
    main()
