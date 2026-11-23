"""Generates the shared 256x128 GeckoLib dragon rig and Phase 5 Stage 4/5 dragon species art.

Shared rig:
- geo/entity/dragon_rig.geo.json
- animations/entity/dragon_rig.animation.json (idle, walk, fly, attack)

Stage 4 species:
- canopy_wyvern: Canopy Emerald & Ember Amber jungle wyvern (textures/entity/canopy_wyvern.png + _glowmask.png)
- item/dragon_scale.png: 16x16 faceted emerald-amber dragon scale drop sprite

Stage 5 species:
- amber_wyvern: Volcanic obsidian & molten Ember Amber wyvern (textures/entity/amber_wyvern.png + _glowmask.png)
- storm_glidewing: Storm-indigo & electric cyan/gold sky dragon (textures/entity/storm_glidewing.png + _glowmask.png)
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


def paint_dragon_species(
    geo: dict,
    *,
    scale_ramp: list[tuple[int, int, int, int]],
    belly_ramp: list[tuple[int, int, int, int]],
    wing_ramp: list[tuple[int, int, int, int]],
    accent_ramp: list[tuple[int, int, int, int]],
    claw_ramp: list[tuple[int, int, int, int]],
    primary_glow: tuple[int, int, int, int],
    secondary_glow: tuple[int, int, int, int],
    seed_offset: int = 0,
) -> tuple[Image.Image, Image.Image]:
    """Paint a 256x128 dragon species texture + emissive glowmask onto the shared dragon_rig."""
    img = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))

    bones = {b["name"]: b for b in geo["minecraft:geometry"][0]["bones"]}

    # Body & dorsal spines
    body_cubes = bones["body"]["cubes"]
    _paint_cube(img, glow, body_cubes[0]["uv"], body_cubes[0]["size"], scale_ramp, "organic", seed=11 + seed_offset)
    # Paint contrasting belly stripe on bottom of chest
    belly_rect = box_faces(0, 0, 12, 10, 12)["bottom"]
    for dy in range(belly_rect[3]):
        for dx in range(belly_rect[2]):
            put_px(img, belly_rect[0] + dx, belly_rect[1] + dy, belly_ramp[(dx + dy) % 3 + 1])
    _paint_cube(img, glow, body_cubes[1]["uv"], body_cubes[1]["size"], accent_ramp, "crystal", seed=13 + seed_offset,
                glow_faces={"front", "top", "bottom", "east", "west"}, glow_color=primary_glow)
    _paint_cube(img, glow, body_cubes[2]["uv"], body_cubes[2]["size"], scale_ramp, "organic", seed=14 + seed_offset)
    _paint_cube(img, glow, body_cubes[3]["uv"], body_cubes[3]["size"], accent_ramp, "crystal", seed=15 + seed_offset,
                glow_faces={"top", "east", "west"}, glow_color=primary_glow)
    _paint_cube(img, glow, body_cubes[4]["uv"], body_cubes[4]["size"], accent_ramp, "crystal", seed=16 + seed_offset,
                glow_faces={"top", "east", "west"}, glow_color=primary_glow)

    # Neck
    for i, bname in enumerate(("neck_base", "neck_upper")):
        for j, c in enumerate(bones[bname]["cubes"]):
            ramp = accent_ramp if (bname == "neck_upper" and j == 1) else scale_ramp
            gfaces = {"top", "front"} if (bname == "neck_upper" and j == 1) else None
            _paint_cube(img, glow, c["uv"], c["size"], ramp, "organic", seed=20 + i * 5 + j + seed_offset,
                        glow_faces=gfaces, glow_color=primary_glow)

    # Head & Jaw
    hc = bones["head"]["cubes"]
    _paint_cube(img, glow, hc[0]["uv"], hc[0]["size"], scale_ramp, "organic", seed=30 + seed_offset)
    _paint_cube(img, glow, hc[1]["uv"], hc[1]["size"], scale_ramp, "organic", seed=31 + seed_offset)
    _paint_cube(img, glow, hc[2]["uv"], hc[2]["size"], belly_ramp, "organic", seed=32 + seed_offset)
    _paint_cube(img, glow, hc[3]["uv"], hc[3]["size"], belly_ramp, "organic", seed=33 + seed_offset)
    # Glowing eyes
    _paint_cube(img, glow, hc[4]["uv"], hc[4]["size"], accent_ramp, "crystal", seed=34 + seed_offset,
                glow_faces={"front", "east", "west", "top"}, glow_color=primary_glow)
    _paint_cube(img, glow, hc[5]["uv"], hc[5]["size"], accent_ramp, "crystal", seed=35 + seed_offset,
                glow_faces={"front", "east", "west", "top"}, glow_color=primary_glow)
    # Horns & crest
    _paint_cube(img, glow, hc[6]["uv"], hc[6]["size"], accent_ramp, "crystal", seed=36 + seed_offset,
                glow_faces={"top"}, glow_color=primary_glow)
    _paint_cube(img, glow, hc[7]["uv"], hc[7]["size"], accent_ramp, "crystal", seed=37 + seed_offset,
                glow_faces={"top"}, glow_color=primary_glow)
    _paint_cube(img, glow, hc[8]["uv"], hc[8]["size"], belly_ramp, "crystal", seed=38 + seed_offset,
                glow_faces={"top"}, glow_color=secondary_glow)
    # Jaw
    jc = bones["jaw"]["cubes"][0]
    _paint_cube(img, glow, jc["uv"], jc["size"], belly_ramp, "organic", seed=39 + seed_offset)

    # Wings (armored leading edges + shaded membranes + glowing vein runes)
    for side_idx, (inner, outer) in enumerate((("left_wing_inner", "left_wing_outer"),
                                               ("right_wing_inner", "right_wing_outer"))):
        ic = bones[inner]["cubes"]
        _paint_cube(img, glow, ic[0]["uv"], ic[0]["size"], scale_ramp, "organic", seed=50 + side_idx * 10 + seed_offset)
        _paint_cube(img, glow, ic[1]["uv"], ic[1]["size"], wing_ramp, "organic", seed=51 + side_idx * 10 + seed_offset)
        mem_rect = box_faces(ic[1]["uv"][0], ic[1]["uv"][1], 12, 1, 10)["top"]
        for dx in range(2, mem_rect[2] - 1, 4):
            for dy in range(mem_rect[3]):
                put_px(img, mem_rect[0] + dx, mem_rect[1] + dy, accent_ramp[4])
                put_px(glow, mem_rect[0] + dx, mem_rect[1] + dy, secondary_glow)

        oc = bones[outer]["cubes"]
        _paint_cube(img, glow, oc[0]["uv"], oc[0]["size"], scale_ramp, "organic", seed=52 + side_idx * 10 + seed_offset)
        _paint_cube(img, glow, oc[1]["uv"], oc[1]["size"], claw_ramp, "metal", seed=53 + side_idx * 10 + seed_offset)
        _paint_cube(img, glow, oc[2]["uv"], oc[2]["size"], wing_ramp, "organic", seed=54 + side_idx * 10 + seed_offset)
        mem2_rect = box_faces(oc[2]["uv"][0], oc[2]["uv"][1], 14, 1, 9)["top"]
        for dx in range(2, mem2_rect[2] - 1, 4):
            for dy in range(mem2_rect[3]):
                put_px(img, mem2_rect[0] + dx, mem2_rect[1] + dy, accent_ramp[4])
                put_px(glow, mem2_rect[0] + dx, mem2_rect[1] + dy, secondary_glow)

    # Legs & clawed feet
    for leg_idx, lname in enumerate(("front_left_leg", "front_right_leg", "back_left_leg", "back_right_leg")):
        lc = bones[lname]["cubes"]
        _paint_cube(img, glow, lc[0]["uv"], lc[0]["size"], scale_ramp, "organic", seed=70 + leg_idx * 3 + seed_offset)
        _paint_cube(img, glow, lc[1]["uv"], lc[1]["size"], claw_ramp, "metal", seed=71 + leg_idx * 3 + seed_offset)

    # Tail segments & spines
    for t_idx, tname in enumerate(("tail_1", "tail_2", "tail_3")):
        tc = bones[tname]["cubes"]
        _paint_cube(img, glow, tc[0]["uv"], tc[0]["size"], scale_ramp, "organic", seed=90 + t_idx * 4 + seed_offset)
        ramp2 = belly_ramp if tname == "tail_3" else accent_ramp
        _paint_cube(img, glow, tc[1]["uv"], tc[1]["size"], ramp2, "crystal", seed=91 + t_idx * 4 + seed_offset,
                    glow_faces={"top"}, glow_color=primary_glow if tname != "tail_3" else secondary_glow)

    return img, glow


AMBER_OBSIDIAN_RAMP = [
    (22, 16, 20, 255),
    (36, 24, 28, 255),
    (54, 34, 38, 255),
    (74, 46, 48, 255),
    (98, 60, 58, 255),
    (126, 78, 70, 255),
]

MOLTEN_BELLY_RAMP = [
    (124, 34, 12, 255),
    (166, 52, 16, 255),
    (212, 78, 20, 255),
    (249, 115, 22, 255),
    (251, 164, 56, 255),
    (253, 224, 71, 255),
]

EMBER_WING_RAMP = [
    (88, 22, 16, 255),
    (124, 34, 20, 255),
    (168, 52, 24, 255),
    (214, 78, 26, 255),
    (245, 118, 38, 255),
    (252, 174, 78, 255),
]

STORM_INDIGO_RAMP = [
    (18, 24, 44, 255),
    (28, 38, 68, 255),
    (40, 54, 94, 255),
    (56, 76, 124, 255),
    (76, 102, 158, 255),
    (102, 134, 196, 255),
]

STORM_CYAN_BELLY_RAMP = [
    (14, 86, 118, 255),
    (20, 122, 158, 255),
    (32, 162, 196, 255),
    (56, 217, 192, 255),
    (112, 236, 224, 255),
    (186, 246, 252, 255),
]

STORM_WING_RAMP = [
    (16, 58, 92, 255),
    (24, 86, 132, 255),
    (34, 122, 174, 255),
    (48, 162, 214, 255),
    (84, 204, 238, 255),
    (142, 232, 252, 255),
]

ELECTRIC_GOLD_RAMP = [
    (122, 86, 10, 255),
    (168, 122, 14, 255),
    (214, 162, 18, 255),
    (250, 204, 21, 255),
    (253, 224, 71, 255),
    (254, 249, 195, 255),
]

SPECIES_PALETTES = {
    "canopy_wyvern": dict(
        scale_ramp=JUNGLE_SCALE_RAMP,
        belly_ramp=EMERALD_BELLY_RAMP,
        wing_ramp=WING_MEMBRANE_RAMP,
        accent_ramp=AMBER_HORN_RAMP,
        claw_ramp=DARK_CLAW_RAMP,
        primary_glow=(251, 164, 56, 255),
        secondary_glow=(74, 222, 128, 255),
        seed_offset=0,
    ),
    "amber_wyvern": dict(
        scale_ramp=AMBER_OBSIDIAN_RAMP,
        belly_ramp=MOLTEN_BELLY_RAMP,
        wing_ramp=EMBER_WING_RAMP,
        accent_ramp=MOLTEN_BELLY_RAMP,
        claw_ramp=DARK_CLAW_RAMP,
        primary_glow=(253, 224, 71, 255),
        secondary_glow=(249, 115, 22, 255),
        seed_offset=100,
    ),
    "storm_glidewing": dict(
        scale_ramp=STORM_INDIGO_RAMP,
        belly_ramp=STORM_CYAN_BELLY_RAMP,
        wing_ramp=STORM_WING_RAMP,
        accent_ramp=ELECTRIC_GOLD_RAMP,
        claw_ramp=DARK_CLAW_RAMP,
        primary_glow=(250, 204, 21, 255),
        secondary_glow=(56, 217, 192, 255),
        seed_offset=200,
    ),
}


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


def build_amber_wyvern_geo() -> dict:
    """Build a true bipedal Wyvern rig (2 hind legs, 0 front legs, 2 giant clawed wing-arms, barbed stinger tail)."""
    bones = [
        {"name": "root", "pivot": [0, 0, 0]},
        {
            "name": "body",
            "parent": "root",
            "pivot": [0, 16, 0],
            "rotation": [-16, 0, 0],
            "cubes": [
                _cube([-5, 13, -9], [10, 9, 12], [0, 0]),
                _cube([-2.5, 14.5, -9.8], [5, 5, 2], [44, 0]),
                _cube([-4, 12.5, 3], [8, 7, 8], [58, 0]),
                _cube([-1, 21.5, -6], [2, 4, 5], [90, 0], [-20, 0, 0]),
                _cube([-1, 19.5, 2], [2, 4, 5], [104, 0], [-22, 0, 0]),
            ],
        },
        {
            "name": "neck_base",
            "parent": "body",
            "pivot": [0, 20, -8],
            "cubes": [_cube([-2.5, 19, -14], [5, 6, 7], [118, 0], [-28, 0, 0])],
        },
        {
            "name": "neck_upper",
            "parent": "neck_base",
            "pivot": [0, 24, -13],
            "cubes": [
                _cube([-2, 24, -18], [4, 6, 6], [142, 0], [16, 0, 0]),
                _cube([-1, 29.5, -17], [2, 3, 4], [162, 0], [-18, 0, 0]),
            ],
        },
        {
            "name": "head",
            "parent": "neck_upper",
            "pivot": [0, 29, -17],
            "cubes": [
                _cube([-4, 27, -25], [8, 6, 8], [0, 24]),
                _cube([-2.5, 28.2, -31], [5, 3.5, 6], [32, 24]),
                _cube([2.8, 29.2, -25.5], [1.6, 2.2, 2.4], [54, 24]),
                _cube([-4.4, 29.2, -25.5], [1.6, 2.2, 2.4], [62, 24]),
                _cube([2.2, 32, -22], [2.2, 7, 2.2], [70, 24], [-36, 16, 18]),
                _cube([-4.4, 32, -22], [2.2, 7, 2.2], [80, 24], [-36, -16, -18]),
                _cube([-1, 33, -23], [2, 3, 5], [90, 24], [-22, 0, 0]),
            ],
        },
        {
            "name": "jaw",
            "parent": "head",
            "pivot": [0, 28, -24],
            "cubes": [_cube([-2.5, 26.2, -30.5], [5, 2, 6], [104, 24])],
        },
        # 2 Massive Clawed Wing-Arms (no front legs!)
        {
            "name": "left_wing_arm",
            "parent": "body",
            "pivot": [5, 20, -6],
            "rotation": [8, -12, -16],
            "cubes": [
                _cube([5, 19, -7], [13, 2.4, 2.4], [0, 44]),
                _cube([16.5, 19, -9.5], [2, 3.5, 2], [32, 44], [25, 0, -10]),
                _cube([5, 19.6, -5], [13, 1, 12], [0, 50]),
            ],
        },
        {
            "name": "left_wing_outer",
            "parent": "left_wing_arm",
            "pivot": [18, 20, -6],
            "rotation": [0, -10, 12],
            "cubes": [
                _cube([18, 19.2, -6.8], [16, 2, 2], [52, 44]),
                _cube([18, 19.6, -5], [16, 1, 11], [52, 50]),
            ],
        },
        {
            "name": "right_wing_arm",
            "parent": "body",
            "pivot": [-5, 20, -6],
            "rotation": [8, 12, 16],
            "cubes": [
                _cube([-18, 19, -7], [13, 2.4, 2.4], [108, 44]),
                _cube([-18.5, 19, -9.5], [2, 3.5, 2], [140, 44], [25, 0, 10]),
                _cube([-18, 19.6, -5], [13, 1, 12], [108, 50]),
            ],
        },
        {
            "name": "right_wing_outer",
            "parent": "right_wing_arm",
            "pivot": [-18, 20, -6],
            "rotation": [0, 10, -12],
            "cubes": [
                _cube([-34, 19.2, -6.8], [16, 2, 2], [160, 44]),
                _cube([-34, 19.6, -5], [16, 1, 11], [160, 50]),
            ],
        },
        # 2 Powerful Bipedal Digitigrade Hind Legs
        {
            "name": "left_leg",
            "parent": "root",
            "pivot": [4.5, 14, 5],
            "cubes": [
                _cube([3, 7, 2.5], [4.5, 8, 5.5], [0, 68], [-18, 0, 0]),
                _cube([3.5, 1.5, 5], [3.5, 7, 3.8], [20, 68], [20, 0, 0]),
                _cube([2.8, 0, 1.5], [5, 2.2, 6.5], [36, 68]),
            ],
        },
        {
            "name": "right_leg",
            "parent": "root",
            "pivot": [-4.5, 14, 5],
            "cubes": [
                _cube([-7.5, 7, 2.5], [4.5, 8, 5.5], [60, 68], [-18, 0, 0]),
                _cube([-7, 1.5, 5], [3.5, 7, 3.8], [80, 68], [20, 0, 0]),
                _cube([-7.8, 0, 1.5], [5, 2.2, 6.5], [96, 68]),
            ],
        },
        # Barbed Magma-Stinger Tail
        {
            "name": "tail_1",
            "parent": "body",
            "pivot": [0, 15, 11],
            "cubes": [
                _cube([-2.5, 13, 11], [5, 4.5, 10], [0, 84]),
                _cube([-0.8, 17.5, 13], [1.6, 3.5, 4], [30, 84], [-18, 0, 0]),
            ],
        },
        {
            "name": "tail_2",
            "parent": "tail_1",
            "pivot": [0, 15, 21],
            "cubes": [
                _cube([-1.75, 13.5, 21], [3.5, 3.2, 10], [42, 84]),
                _cube([-0.75, 16.7, 23], [1.5, 3, 4], [70, 84], [-18, 0, 0]),
            ],
        },
        {
            "name": "tail_stinger",
            "parent": "tail_2",
            "pivot": [0, 15, 31],
            "cubes": [
                _cube([-1.2, 13.8, 31], [2.4, 2.4, 8], [82, 84]),
                _cube([-3.5, 14.3, 37], [7, 1.4, 6], [104, 84]),
            ],
        },
    ]
    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.amber_wyvern",
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


def build_amber_wyvern_animations() -> dict:
    return {
        "format_version": "1.8.0",
        "animations": {
            "animation.dragon_rig.idle": {
                "loop": True,
                "animation_length": 2.2,
                "bones": {
                    "body": {"position": {"0.0": [0, 0, 0], "1.1": [0, 0.5, 0], "2.2": [0, 0, 0]}},
                    "head": {"rotation": {"0.0": [0, 0, 0], "1.1": [-6, 0, 0], "2.2": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "1.1": [8, 0, 0], "2.2": [0, 0, 0]}},
                    "left_wing_arm": {"rotation": {"0.0": [0, 0, 0], "1.1": [4, 0, -8], "2.2": [0, 0, 0]}},
                    "right_wing_arm": {"rotation": {"0.0": [0, 0, 0], "1.1": [4, 0, 8], "2.2": [0, 0, 0]}},
                    "tail_1": {"rotation": {"0.0": [0, -8, 0], "1.1": [0, 8, 0], "2.2": [0, -8, 0]}},
                    "tail_stinger": {"rotation": {"0.0": [8, -12, 0], "1.1": [14, 12, 0], "2.2": [8, -12, 0]}},
                },
            },
            "animation.dragon_rig.walk": {
                "loop": True,
                "animation_length": 0.8,
                "bones": {
                    "body": {"position": {"0.0": [0, 0, 0], "0.2": [0, 0.9, 0], "0.4": [0, 0, 0], "0.6": [0, 0.9, 0], "0.8": [0, 0, 0]}},
                    "left_leg": {"rotation": {"0.0": [-26, 0, 0], "0.4": [26, 0, 0], "0.8": [-26, 0, 0]}},
                    "right_leg": {"rotation": {"0.0": [26, 0, 0], "0.4": [-26, 0, 0], "0.8": [26, 0, 0]}},
                    "left_wing_arm": {"rotation": {"0.0": [10, 0, -10], "0.4": [-6, 0, -18], "0.8": [10, 0, -10]}},
                    "right_wing_arm": {"rotation": {"0.0": [-6, 0, 18], "0.4": [10, 0, 10], "0.8": [-6, 0, 18]}},
                    "tail_1": {"rotation": {"0.0": [0, 12, 0], "0.4": [0, -12, 0], "0.8": [0, 12, 0]}},
                },
            },
            "animation.dragon_rig.fly": {
                "loop": True,
                "animation_length": 0.75,
                "bones": {
                    "body": {"position": {"0.0": [0, 2.0, 0], "0.38": [0, -1.0, 0], "0.75": [0, 2.0, 0]}},
                    "left_wing_arm": {"rotation": {"0.0": [0, 0, -32], "0.38": [0, 0, 32], "0.75": [0, 0, -32]}},
                    "left_wing_outer": {"rotation": {"0.0": [0, 0, -20], "0.38": [0, 0, 24], "0.75": [0, 0, -20]}},
                    "right_wing_arm": {"rotation": {"0.0": [0, 0, 32], "0.38": [0, 0, -32], "0.75": [0, 0, 32]}},
                    "right_wing_outer": {"rotation": {"0.0": [0, 0, 20], "0.38": [0, 0, -24], "0.75": [0, 0, 20]}},
                    "left_leg": {"rotation": [40, 0, 0]},
                    "right_leg": {"rotation": [40, 0, 0]},
                },
            },
            "animation.dragon_rig.attack": {
                "animation_length": 0.5,
                "bones": {
                    "body": {"rotation": {"0.0": [0, 0, 0], "0.18": [-12, 0, 0], "0.35": [16, 0, 0], "0.5": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "0.18": [40, 0, 0], "0.35": [4, 0, 0], "0.5": [0, 0, 0]}},
                    "tail_stinger": {"rotation": {"0.0": [0, 0, 0], "0.22": [35, 0, 0], "0.5": [0, 0, 0]}},
                },
            },
        },
    }


def build_storm_glidewing_geo() -> dict:
    """Build a legless Amphithere sky-serpent rig (0 legs, 4-segment undulating body, 3-segment swept glider wings)."""
    bones = [
        {"name": "root", "pivot": [0, 0, 0]},
        {
            "name": "body",
            "parent": "root",
            "pivot": [0, 17, -4],
            "cubes": [
                _cube([-3.5, 13.5, -10], [7, 7, 11], [0, 0]),
                _cube([-2, 15, -10.8], [4, 4, 2], [36, 0]),
            ],
        },
        {
            "name": "neck",
            "parent": "body",
            "pivot": [0, 18, -10],
            "cubes": [_cube([-2.25, 16, -17], [4.5, 5, 8], [0, 24], [-14, 0, 0])],
        },
        {
            "name": "head",
            "parent": "neck",
            "pivot": [0, 19.5, -16.5],
            "cubes": [
                _cube([-3.25, 17.5, -23.5], [6.5, 5, 7], [26, 24]),
                _cube([-2.25, 18.4, -29.5], [4.5, 3, 6], [54, 24]),
                _cube([2.2, 19.2, -24], [1.4, 2, 2.2], [76, 24]),
                _cube([-3.6, 19.2, -24], [1.4, 2, 2.2], [84, 24]),
                _cube([2.0, 22, -20], [1.8, 8, 2], [92, 24], [-40, 22, 16]),
                _cube([-3.8, 22, -20], [1.8, 8, 2], [100, 24], [-40, -22, -16]),
                _cube([2.8, 18.2, -22], [1.5, 3, 5], [108, 24], [0, 22, 0]),
                _cube([-4.3, 18.2, -22], [1.5, 3, 5], [122, 24], [0, -22, 0]),
            ],
        },
        {
            "name": "jaw",
            "parent": "head",
            "pivot": [0, 18.2, -23],
            "cubes": [_cube([-2.25, 16.6, -29], [4.5, 1.8, 6], [136, 24])],
        },
        # 3-Segment Swept Albatross/Falcon Glider Wings (Zero Legs!)
        {
            "name": "left_wing_inner",
            "parent": "body",
            "pivot": [3.5, 19, -7],
            "rotation": [0, -14, -8],
            "cubes": [
                _cube([3.5, 18.2, -8], [12, 2, 2.2], [0, 44]),
                _cube([3.5, 18.6, -6], [12, 1, 9], [0, 50]),
            ],
        },
        {
            "name": "left_wing_mid",
            "parent": "left_wing_inner",
            "pivot": [15.5, 19, -7],
            "rotation": [0, -12, 6],
            "cubes": [
                _cube([15.5, 18.3, -7.8], [14, 1.8, 2], [44, 44]),
                _cube([15.5, 18.6, -5.8], [14, 1, 8], [44, 50]),
            ],
        },
        {
            "name": "left_wing_tip",
            "parent": "left_wing_mid",
            "pivot": [29.5, 19, -7],
            "rotation": [0, -18, 8],
            "cubes": [_cube([29.5, 18.5, -7], [11, 1.2, 5], [88, 44])],
        },
        {
            "name": "right_wing_inner",
            "parent": "body",
            "pivot": [-3.5, 19, -7],
            "rotation": [0, 14, 8],
            "cubes": [
                _cube([-15.5, 18.2, -8], [12, 2, 2.2], [0, 62]),
                _cube([-15.5, 18.6, -6], [12, 1, 9], [0, 68]),
            ],
        },
        {
            "name": "right_wing_mid",
            "parent": "right_wing_inner",
            "pivot": [-15.5, 19, -7],
            "rotation": [0, 12, -6],
            "cubes": [
                _cube([-29.5, 18.3, -7.8], [14, 1.8, 2], [44, 62]),
                _cube([-29.5, 18.6, -5.8], [14, 1, 8], [44, 68]),
            ],
        },
        {
            "name": "right_wing_tip",
            "parent": "right_wing_mid",
            "pivot": [-29.5, 19, -7],
            "rotation": [0, 18, -8],
            "cubes": [_cube([-40.5, 18.5, -7], [11, 1.2, 5], [88, 62])],
        },
        # 4-Segment Serpentine Coils + Wide Fanned Tail Plume
        {
            "name": "coil_1",
            "parent": "body",
            "pivot": [0, 16, 1],
            "cubes": [_cube([-3, 13, 1], [6, 6, 10], [48, 0])],
        },
        {
            "name": "coil_2",
            "parent": "coil_1",
            "pivot": [0, 15.5, 11],
            "cubes": [_cube([-2.5, 13, 11], [5, 5, 10], [80, 0])],
        },
        {
            "name": "coil_3",
            "parent": "coil_2",
            "pivot": [0, 15, 21],
            "cubes": [_cube([-2, 13, 21], [4, 4, 10], [110, 0])],
        },
        {
            "name": "tail_plume",
            "parent": "coil_3",
            "pivot": [0, 15, 31],
            "cubes": [
                _cube([-1.25, 13.7, 31], [2.5, 2.5, 8], [138, 0]),
                _cube([-5, 14.4, 37], [10, 1.2, 7], [160, 0]),
            ],
        },
    ]
    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.storm_glidewing",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 7,
                    "visible_bounds_height": 3,
                    "visible_bounds_offset": [0, 1.5, 0],
                },
                "bones": bones,
            }
        ],
    }
    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))
    return geo


def build_storm_glidewing_animations() -> dict:
    return {
        "format_version": "1.8.0",
        "animations": {
            "animation.dragon_rig.idle": {
                "loop": True,
                "animation_length": 2.0,
                "bones": {
                    "body": {"position": {"0.0": [0, 0, 0], "1.0": [0, 1.2, 0], "2.0": [0, 0, 0]}},
                    "coil_1": {"rotation": {"0.0": [0, -10, 0], "1.0": [0, 10, 0], "2.0": [0, -10, 0]}},
                    "coil_2": {"rotation": {"0.0": [0, 16, 0], "1.0": [0, -16, 0], "2.0": [0, 16, 0]}},
                    "coil_3": {"rotation": {"0.0": [0, -18, 0], "1.0": [0, 18, 0], "2.0": [0, -18, 0]}},
                    "tail_plume": {"rotation": {"0.0": [0, 14, 0], "1.0": [0, -14, 0], "2.0": [0, 14, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, -6], "1.0": [0, 0, 8], "2.0": [0, 0, -6]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 6], "1.0": [0, 0, -8], "2.0": [0, 0, 6]}},
                },
            },
            "animation.dragon_rig.walk": {
                "loop": True,
                "animation_length": 1.0,
                "bones": {
                    "body": {"position": {"0.0": [0, -2.0, 0], "0.5": [0, -0.8, 0], "1.0": [0, -2.0, 0]}},
                    "coil_1": {"rotation": {"0.0": [0, -18, 0], "0.5": [0, 18, 0], "1.0": [0, -18, 0]}},
                    "coil_2": {"rotation": {"0.0": [0, 24, 0], "0.5": [0, -24, 0], "1.0": [0, 24, 0]}},
                    "coil_3": {"rotation": {"0.0": [0, -26, 0], "0.5": [0, 26, 0], "1.0": [0, -26, 0]}},
                    "tail_plume": {"rotation": {"0.0": [0, 20, 0], "0.5": [0, -20, 0], "1.0": [0, 20, 0]}},
                },
            },
            "animation.dragon_rig.fly": {
                "loop": True,
                "animation_length": 0.7,
                "bones": {
                    "body": {"position": {"0.0": [0, 2.0, 0], "0.35": [0, -1.0, 0], "0.7": [0, 2.0, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, -26], "0.35": [0, 0, 26], "0.7": [0, 0, -26]}},
                    "left_wing_mid": {"rotation": {"0.0": [0, 0, -18], "0.35": [0, 0, 20], "0.7": [0, 0, -18]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 26], "0.35": [0, 0, -26], "0.7": [0, 0, 26]}},
                    "right_wing_mid": {"rotation": {"0.0": [0, 0, 18], "0.35": [0, 0, -20], "0.7": [0, 0, 18]}},
                    "coil_1": {"rotation": {"0.0": [8, 0, 0], "0.35": [-8, 0, 0], "0.7": [8, 0, 0]}},
                    "coil_2": {"rotation": {"0.0": [-10, 0, 0], "0.35": [10, 0, 0], "0.7": [-10, 0, 0]}},
                },
            },
            "animation.dragon_rig.attack": {
                "animation_length": 0.45,
                "bones": {
                    "neck": {"rotation": {"0.0": [0, 0, 0], "0.15": [-22, 0, 0], "0.3": [26, 0, 0], "0.45": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "0.15": [42, 0, 0], "0.3": [4, 0, 0], "0.45": [0, 0, 0]}},
                    "left_wing_inner": {"rotation": {"0.0": [0, 0, -8], "0.18": [0, 0, -36], "0.45": [0, 0, -8]}},
                    "right_wing_inner": {"rotation": {"0.0": [0, 0, 8], "0.18": [0, 0, 36], "0.45": [0, 0, 8]}},
                },
            },
        },
    }


def paint_amber_wyvern(geo: dict) -> tuple[Image.Image, Image.Image]:
    """Paint the bipedal Amber Wyvern onto its dedicated UV layout."""
    img = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    bones = {b["name"]: b for b in geo["minecraft:geometry"][0]["bones"]}
    yellow_glow = (253, 224, 71, 255)
    amber_glow = (249, 115, 22, 255)

    for bname, bone in bones.items():
        for idx, c in enumerate(bone.get("cubes", [])):
            ramp = AMBER_OBSIDIAN_RAMP
            gfaces = None
            gcol = amber_glow
            style = "organic"
            if (bname == "body" and idx in (1, 3, 4)) or (bname == "neck_upper" and idx == 1) or (bname == "head" and idx in (2, 3, 4, 5, 6)):
                ramp = MOLTEN_BELLY_RAMP
                style = "crystal"
                gcol = yellow_glow
                gfaces = {"front", "top", "east", "west"} if bname == "head" and idx in (2, 3) else {"top", "front"}
            elif bname in ("jaw", "tail_stinger"):
                ramp = MOLTEN_BELLY_RAMP
                if bname == "tail_stinger" and idx == 1:
                    gfaces = {"top", "bottom"}
            elif "wing" in bname and idx == len(bone["cubes"]) - 1:
                ramp = EMBER_WING_RAMP
            elif "wing" in bname and idx == 1 and len(bone["cubes"]) == 3:
                ramp = DARK_CLAW_RAMP
                style = "metal"
            elif "leg" in bname and idx == 2:
                ramp = DARK_CLAW_RAMP
                style = "metal"
            elif bname in ("tail_1", "tail_2") and idx == 1:
                ramp = MOLTEN_BELLY_RAMP
                style = "crystal"
                gfaces = {"top"}
            _paint_cube(img, glow, c["uv"], c["size"], ramp, style, seed=100 + idx * 7 + len(bname),
                        glow_faces=gfaces, glow_color=gcol)
    return img, glow


def paint_storm_glidewing(geo: dict) -> tuple[Image.Image, Image.Image]:
    """Paint the legless Amphithere Storm Glidewing onto its dedicated UV layout."""
    img = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    bones = {b["name"]: b for b in geo["minecraft:geometry"][0]["bones"]}
    gold_glow = (250, 204, 21, 255)
    cyan_glow = (56, 217, 192, 255)

    for bname, bone in bones.items():
        for idx, c in enumerate(bone.get("cubes", [])):
            ramp = STORM_INDIGO_RAMP
            gfaces = None
            gcol = cyan_glow
            style = "organic"
            if (bname == "body" and idx == 1) or (bname == "head" and idx in (2, 3, 4, 5)):
                ramp = ELECTRIC_GOLD_RAMP
                style = "crystal"
                gcol = gold_glow
                gfaces = {"front", "top", "east", "west"} if bname == "head" and idx in (2, 3) else {"top"}
            elif (bname == "head" and idx in (6, 7)) or bname == "jaw":
                ramp = STORM_CYAN_BELLY_RAMP
            elif "wing" in bname and (idx == 1 or bname.endswith("_tip")):
                ramp = STORM_WING_RAMP
                if bname.endswith("_tip"):
                    gfaces = {"top"}
                    gcol = cyan_glow
            elif bname == "tail_plume" and idx == 1:
                ramp = STORM_CYAN_BELLY_RAMP
                gfaces = {"top"}
                gcol = cyan_glow
            _paint_cube(img, glow, c["uv"], c["size"], ramp, style, seed=200 + idx * 7 + len(bname),
                        glow_faces=gfaces, glow_color=gcol)
    return img, glow


def main() -> None:
    geo = build_shared_dragon_rig()
    _save_json(GEO_DIR / "dragon_rig.geo.json", geo)
    _save_json(ANIM_DIR / "dragon_rig.animation.json", build_shared_dragon_animations())

    amber_geo = build_amber_wyvern_geo()
    _save_json(GEO_DIR / "amber_wyvern.geo.json", amber_geo)
    _save_json(ANIM_DIR / "amber_wyvern.animation.json", build_amber_wyvern_animations())

    storm_geo = build_storm_glidewing_geo()
    _save_json(GEO_DIR / "storm_glidewing.geo.json", storm_geo)
    _save_json(ANIM_DIR / "storm_glidewing.animation.json", build_storm_glidewing_animations())

    TEX_DIR.mkdir(parents=True, exist_ok=True)
    wyvern_img, wyvern_glow = paint_dragon_species(geo, **SPECIES_PALETTES["canopy_wyvern"])
    wyvern_img.save(TEX_DIR / "canopy_wyvern.png")
    wyvern_glow.save(TEX_DIR / "canopy_wyvern_glowmask.png")

    amber_img, amber_glow = paint_amber_wyvern(amber_geo)
    amber_img.save(TEX_DIR / "amber_wyvern.png")
    amber_glow.save(TEX_DIR / "amber_wyvern_glowmask.png")

    storm_img, storm_glow = paint_storm_glidewing(storm_geo)
    storm_img.save(TEX_DIR / "storm_glidewing.png")
    storm_glow.save(TEX_DIR / "storm_glidewing_glowmask.png")

    ITEM_TEX_DIR.mkdir(parents=True, exist_ok=True)
    make_dragon_scale_sprite().save(ITEM_TEX_DIR / "dragon_scale.png")
    print("Generated distinct 3D models, animations, textures & glowmasks for canopy_wyvern, amber_wyvern, and storm_glidewing")


if __name__ == "__main__":
    main()
