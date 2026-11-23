"""Generates v2 GeckoLib models, animations, 256x128 textures, and glowmasks for Phase 4 Stage 3 Eye Realm enemies.

Entities:
- peeper: Detailed winged beholder-scout with articulated bat-wing fingers, horns, slit-pupil eye, and 3-segment tail.
- oculus_slime: Stepped eldritch gel mass with front cyclops eye, twin eyestalk mini-eyes, pseudopods, and crystal spikes.
- lens_guardian: Towering armored Gaze Stone & Starlight Slate sentinel with hollowed ribcage Focus Lens, tassets, knee guards, claws, and orbiting shards.
"""
from __future__ import annotations

import json
from pathlib import Path

from PIL import Image

from art_lib import (
    assert_uv_layout_clean,
    box_faces,
    box_rect,
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

SHEET_W = 256
SHEET_H = 128

SLATE_RAMP = [
    (20, 15, 30, 255),
    (32, 25, 46, 255),
    (46, 36, 66, 255),
    (64, 51, 90, 255),
    (86, 70, 116, 255),
    (112, 94, 146, 255),
]

GAZE_STONE_RAMP = [
    (30, 22, 44, 255),
    (44, 33, 64, 255),
    (60, 46, 86, 255),
    (80, 62, 112, 255),
    (104, 82, 140, 255),
    (132, 108, 170, 255),
]

MAGENTA_RAMP = [
    (68, 18, 86, 255),
    (102, 26, 128, 255),
    (144, 40, 174, 255),
    (188, 54, 216, 255),
    (217, 70, 239, 255),
    (244, 142, 254, 255),
]

SCLERA_RAMP = [
    (144, 140, 168, 255),
    (174, 170, 196, 255),
    (202, 198, 222, 255),
    (226, 224, 242, 255),
    (242, 242, 252, 255),
    (255, 255, 255, 255),
]

CYAN_LENS_RAMP = [
    (22, 100, 124, 255),
    (38, 146, 174, 255),
    (66, 194, 222, 255),
    (104, 232, 248, 255),
    (125, 249, 255, 255),
    (218, 254, 255, 255),
]

PUPIL_RAMP = [
    (10, 8, 18, 255),
    (18, 14, 30, 255),
    (28, 22, 46, 255),
    (42, 32, 66, 255),
    (58, 46, 88, 255),
    (78, 64, 112, 255),
]

GOLD_TRIM_RAMP = [
    (124, 72, 22, 255),
    (166, 102, 30, 255),
    (208, 136, 44, 255),
    (242, 168, 60, 255),
    (255, 204, 104, 255),
    (255, 238, 172, 255),
]


def _save_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def _cube(
    origin: list[float],
    size: list[float],
    uv: list[int],
    rotation: list[float] | None = None,
    pivot: list[float] | None = None,
) -> dict:
    c: dict = {"origin": origin, "size": size, "uv": uv}
    if rotation is not None:
        c["rotation"] = rotation
        c["pivot"] = pivot if pivot is not None else [
            round(origin[0] + size[0] / 2.0, 2),
            round(origin[1] + size[1] / 2.0, 2),
            round(origin[2] + size[2] / 2.0, 2),
        ]
    return c


def _pack_uvs(items: list[tuple[str, list[float], str]]) -> dict[str, tuple[int, int]]:
    """Deterministic shelf-packer for non-overlapping box UVs on a 256x128 sheet."""
    x, y, row_h = 0, 0, 0
    placed: dict[str, tuple[int, int]] = {}
    for name, size, _ in items:
        w, h, d = int(round(size[0])), int(round(size[1])), int(round(size[2]))
        _, _, rw, rh = box_rect(0, 0, w, h, d)
        if x + rw > SHEET_W:
            x = 0
            y += row_h + 1
            row_h = 0
        if y + rh > SHEET_H:
            raise ValueError(f"UV overflow packing {name} ({rw}x{rh}) at y={y}")
        placed[name] = (x, y)
        x += rw + 1
        row_h = max(row_h, rh)
    return placed


def _paint_cube(
    tex: Image.Image,
    glow: Image.Image,
    u: int,
    v: int,
    size: list[float],
    kind: str,
    seed: int,
) -> None:
    w, h, d = int(round(size[0])), int(round(size[1])), int(round(size[2]))
    if kind == "slate":
        paint_weathered_stone(tex, u, v, w, h, d, SLATE_RAMP, crack_color=(217, 70, 239, 90), seed=seed)
    elif kind == "gaze_stone":
        paint_weathered_stone(tex, u, v, w, h, d, GAZE_STONE_RAMP, crack_color=(125, 249, 255, 85), seed=seed)
    elif kind == "metal_trim":
        paint_forged_metal(tex, u, v, w, h, d, GOLD_TRIM_RAMP, seed=seed)
    elif kind == "magenta_organic":
        paint_organic_shaded(tex, u, v, w, h, d, MAGENTA_RAMP, spot_color=(125, 249, 255, 160), seed=seed)
    elif kind == "magenta_crystal":
        paint_faceted_crystal(tex, u, v, w, h, d, MAGENTA_RAMP, vein_color=(255, 210, 255, 220), seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, MAGENTA_RAMP, seed=seed)
    elif kind == "sclera":
        paint_organic_shaded(tex, u, v, w, h, d, SCLERA_RAMP, spot_color=(217, 70, 239, 150), seed=seed)
    elif kind == "cyan_glow":
        paint_faceted_crystal(tex, u, v, w, h, d, CYAN_LENS_RAMP, vein_color=(230, 255, 255, 240), seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, CYAN_LENS_RAMP, seed=seed)
    elif kind == "pupil":
        paint_organic_shaded(tex, u, v, w, h, d, PUPIL_RAMP, seed=seed)
    elif kind == "wing_membrane":
        paint_organic_shaded(tex, u, v, w, h, d, MAGENTA_RAMP, spot_color=(125, 249, 255, 220), seed=seed)
        faces = box_faces(u, v, w, h, d)
        fx, fy, fw, fh = faces["front"]
        if fw >= 5 and fh >= 5:
            cx, cy = fx + fw // 2, fy + fh // 2
            for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
                put_px(tex, cx + dx, cy + dy, (125, 249, 255, 255))
                put_px(glow, cx + dx, cy + dy, (125, 249, 255, 255))
    else:
        raise ValueError(f"Unknown material kind: {kind}")


def build_peeper() -> None:
    parts = [
        ("sclera_core", [8, 8, 8], "sclera"),
        ("eyelid_hood", [10, 3, 10], "slate"),
        ("lower_lid", [10, 2, 10], "slate"),
        ("cheek_l", [2, 5, 9], "gaze_stone"),
        ("cheek_r", [2, 5, 9], "gaze_stone"),
        ("brow_ridge_l", [5, 2, 4], "metal_trim"),
        ("brow_ridge_r", [5, 2, 4], "metal_trim"),
        ("horn_l", [2, 5, 2], "magenta_crystal"),
        ("horn_r", [2, 5, 2], "magenta_crystal"),
        ("iris_outer", [6, 6, 1], "cyan_glow"),
        ("pupil_slit", [2, 4, 1], "pupil"),
        ("wing_arm_l", [7, 2, 2], "slate"),
        ("wing_finger_l1", [2, 7, 1], "gaze_stone"),
        ("wing_finger_l2", [2, 6, 1], "gaze_stone"),
        ("wing_membrane_l", [8, 7, 1], "wing_membrane"),
        ("wing_arm_r", [7, 2, 2], "slate"),
        ("wing_finger_r1", [2, 7, 1], "gaze_stone"),
        ("wing_finger_r2", [2, 6, 1], "gaze_stone"),
        ("wing_membrane_r", [8, 7, 1], "wing_membrane"),
        ("tail_1", [3, 3, 4], "slate"),
        ("tail_2", [2, 2, 4], "gaze_stone"),
        ("tail_stinger", [2, 2, 4], "cyan_glow"),
        ("mote_l", [2, 2, 2], "cyan_glow"),
        ("mote_r", [2, 2, 2], "magenta_crystal"),
    ]
    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=100 + i)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.peeper",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 2.4,
                    "visible_bounds_height": 2.0,
                    "visible_bounds_offset": [0, 0.85, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "body",
                        "parent": "root",
                        "pivot": [0, 10, 0],
                        "cubes": [
                            _cube([-4, 6, -4], [8, 8, 8], list(uvs["sclera_core"])),
                            _cube([-5, 12, -5], [10, 3, 10], list(uvs["eyelid_hood"])),
                            _cube([-5, 5, -5], [10, 2, 10], list(uvs["lower_lid"])),
                            _cube([3.5, 7, -4.5], [2, 5, 9], list(uvs["cheek_l"])),
                            _cube([-5.5, 7, -4.5], [2, 5, 9], list(uvs["cheek_r"])),
                            _cube([0.5, 13.5, -5.5], [5, 2, 4], list(uvs["brow_ridge_l"]), rotation=[-12, 0, -14]),
                            _cube([-5.5, 13.5, -5.5], [5, 2, 4], list(uvs["brow_ridge_r"]), rotation=[-12, 0, 14]),
                            _cube([4, 14, -2], [2, 5, 2], list(uvs["horn_l"]), rotation=[-10, 0, -24]),
                            _cube([-6, 14, -2], [2, 5, 2], list(uvs["horn_r"]), rotation=[-10, 0, 24]),
                            _cube([-3, 7, -4.6], [6, 6, 1], list(uvs["iris_outer"])),
                            _cube([-1, 8, -5.2], [2, 4, 1], list(uvs["pupil_slit"])),
                        ],
                    },
                    {
                        "name": "wing_left",
                        "parent": "body",
                        "pivot": [5, 11, 0],
                        "cubes": [
                            _cube([5, 10, -1], [7, 2, 2], list(uvs["wing_arm_l"]), rotation=[0, 0, -16]),
                            _cube([11, 4, -0.5], [2, 7, 1], list(uvs["wing_finger_l1"]), rotation=[0, 0, -22]),
                            _cube([7, 4, -0.5], [2, 6, 1], list(uvs["wing_finger_l2"]), rotation=[0, 0, -8]),
                            _cube([5, 4, 0], [8, 7, 1], list(uvs["wing_membrane_l"]), rotation=[0, 0, -12]),
                        ],
                    },
                    {
                        "name": "wing_right",
                        "parent": "body",
                        "pivot": [-5, 11, 0],
                        "cubes": [
                            _cube([-12, 10, -1], [7, 2, 2], list(uvs["wing_arm_r"]), rotation=[0, 0, 16]),
                            _cube([-13, 4, -0.5], [2, 7, 1], list(uvs["wing_finger_r1"]), rotation=[0, 0, 22]),
                            _cube([-9, 4, -0.5], [2, 6, 1], list(uvs["wing_finger_r2"]), rotation=[0, 0, 8]),
                            _cube([-13, 4, 0], [8, 7, 1], list(uvs["wing_membrane_r"]), rotation=[0, 0, 12]),
                        ],
                    },
                    {
                        "name": "tail_stalk",
                        "parent": "body",
                        "pivot": [0, 8, 5],
                        "cubes": [
                            _cube([-1.5, 7, 4.5], [3, 3, 4], list(uvs["tail_1"]), rotation=[14, 0, 0]),
                            _cube([-1, 6, 8], [2, 2, 4], list(uvs["tail_2"]), rotation=[26, 0, 0]),
                            _cube([-1, 4.5, 11.5], [2, 2, 4], list(uvs["tail_stinger"]), rotation=[36, 0, 0]),
                        ],
                    },
                    {
                        "name": "orbit_motes",
                        "parent": "body",
                        "pivot": [0, 12, 0],
                        "cubes": [
                            _cube([8, 15, -3], [2, 2, 2], list(uvs["mote_l"]), rotation=[20, 45, 0]),
                            _cube([-10, 15, -3], [2, 2, 2], list(uvs["mote_r"]), rotation=[-20, 45, 0]),
                        ],
                    },
                ],
            }
        ],
    }

    anim = {
        "format_version": "1.8.0",
        "animations": {
            "animation.peeper.idle": {
                "loop": True,
                "animation_length": 1.6,
                "bones": {
                    "body": {"position": [0, "math.sin(query.anim_time * 225) * 1.4", 0]},
                    "wing_left": {"rotation": [0, 0, "math.sin(query.anim_time * 450) * 20"]},
                    "wing_right": {"rotation": [0, 0, "-math.sin(query.anim_time * 450) * 20"]},
                    "tail_stalk": {"rotation": ["math.cos(query.anim_time * 225) * 12", 0, 0]},
                    "orbit_motes": {"rotation": [0, "query.anim_time * 180", 0]},
                },
            },
            "animation.peeper.walk": {
                "loop": True,
                "animation_length": 0.8,
                "bones": {
                    "body": {"position": [0, "math.sin(query.anim_time * 450) * 1.8", 0], "rotation": [14, 0, 0]},
                    "wing_left": {"rotation": [0, 0, "math.sin(query.anim_time * 900) * 32"]},
                    "wing_right": {"rotation": [0, 0, "-math.sin(query.anim_time * 900) * 32"]},
                    "orbit_motes": {"rotation": [0, "query.anim_time * 360", 0]},
                },
            },
            "animation.peeper.attack": {
                "animation_length": 0.45,
                "bones": {
                    "body": {"rotation": {"0.0": [0, 0, 0], "0.18": [28, 0, 0], "0.45": [0, 0, 0]}},
                    "wing_left": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 0, -42], "0.45": [0, 0, 0]}},
                    "wing_right": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 0, 42], "0.45": [0, 0, 0]}},
                },
            },
        },
    }

    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))
    _save_json(GEO_DIR / "peeper.geo.json", geo)
    _save_json(ANIM_DIR / "peeper.animation.json", anim)
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    tex.save(TEX_DIR / "peeper.png")
    glow.save(TEX_DIR / "peeper_glowmask.png")


def build_oculus_slime() -> None:
    parts = [
        ("base_puddle", [16, 2, 16], "slate"),
        ("lower_skirt", [14, 4, 14], "magenta_organic"),
        ("mid_gel", [12, 6, 12], "magenta_organic"),
        ("upper_dome", [10, 3, 10], "magenta_organic"),
        ("eye_socket_rim", [8, 8, 2], "slate"),
        ("sclera_front", [6, 6, 1], "sclera"),
        ("iris_front", [4, 4, 1], "cyan_glow"),
        ("pupil_front", [2, 3, 1], "pupil"),
        ("eyestalk_l", [2, 5, 2], "magenta_organic"),
        ("mini_eye_l", [3, 3, 3], "cyan_glow"),
        ("eyestalk_r", [2, 5, 2], "magenta_organic"),
        ("mini_eye_r", [3, 3, 3], "cyan_glow"),
        ("crown_crystal_1", [3, 5, 3], "magenta_crystal"),
        ("crown_crystal_2", [2, 4, 2], "magenta_crystal"),
        ("crown_crystal_3", [2, 4, 2], "cyan_glow"),
        ("tentacle_l", [3, 5, 3], "magenta_organic"),
        ("tentacle_r", [3, 5, 3], "magenta_organic"),
        ("drip_fl", [3, 3, 3], "cyan_glow"),
        ("drip_fr", [3, 3, 3], "cyan_glow"),
        ("drip_bl", [3, 3, 3], "magenta_crystal"),
        ("drip_br", [3, 3, 3], "magenta_crystal"),
    ]
    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=200 + i)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.oculus_slime",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 2.2,
                    "visible_bounds_height": 2.2,
                    "visible_bounds_offset": [0, 0.8, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "body",
                        "parent": "root",
                        "pivot": [0, 0, 0],
                        "cubes": [
                            _cube([-8, 0, -8], [16, 2, 16], list(uvs["base_puddle"])),
                            _cube([-7, 2, -7], [14, 4, 14], list(uvs["lower_skirt"])),
                            _cube([-6, 6, -6], [12, 6, 12], list(uvs["mid_gel"])),
                            _cube([-5, 12, -5], [10, 3, 10], list(uvs["upper_dome"])),
                            _cube([-4, 4, -7.2], [8, 8, 2], list(uvs["eye_socket_rim"])),
                            _cube([-3, 5, -7.6], [6, 6, 1], list(uvs["sclera_front"])),
                            _cube([-2, 6, -8.1], [4, 4, 1], list(uvs["iris_front"])),
                            _cube([-1, 6.5, -8.6], [2, 3, 1], list(uvs["pupil_front"])),
                            _cube([-1.5, 14, 1], [3, 5, 3], list(uvs["crown_crystal_1"]), rotation=[16, 0, 0]),
                            _cube([3, 13, 2], [2, 4, 2], list(uvs["crown_crystal_2"]), rotation=[18, 0, -20]),
                            _cube([-5, 13, 2], [2, 4, 2], list(uvs["crown_crystal_3"]), rotation=[18, 0, 20]),
                            _cube([6, 2, -6], [3, 3, 3], list(uvs["drip_fl"])),
                            _cube([-9, 2, -6], [3, 3, 3], list(uvs["drip_fr"])),
                            _cube([6, 2, 4], [3, 3, 3], list(uvs["drip_bl"])),
                            _cube([-9, 2, 4], [3, 3, 3], list(uvs["drip_br"])),
                        ],
                    },
                    {
                        "name": "eyestalks",
                        "parent": "body",
                        "pivot": [0, 12, -2],
                        "cubes": [
                            _cube([4, 12, -3], [2, 5, 2], list(uvs["eyestalk_l"]), rotation=[12, 0, -24]),
                            _cube([4.8, 16.5, -3.5], [3, 3, 3], list(uvs["mini_eye_l"]), rotation=[12, 0, -24]),
                            _cube([-6, 12, -3], [2, 5, 2], list(uvs["eyestalk_r"]), rotation=[12, 0, 24]),
                            _cube([-7.8, 16.5, -3.5], [3, 3, 3], list(uvs["mini_eye_r"]), rotation=[12, 0, 24]),
                        ],
                    },
                    {
                        "name": "tentacles",
                        "parent": "body",
                        "pivot": [0, 5, 0],
                        "cubes": [
                            _cube([6.5, 5, -1.5], [3, 5, 3], list(uvs["tentacle_l"]), rotation=[0, 0, -28]),
                            _cube([-9.5, 5, -1.5], [3, 5, 3], list(uvs["tentacle_r"]), rotation=[0, 0, 28]),
                        ],
                    },
                ],
            }
        ],
    }

    anim = {
        "format_version": "1.8.0",
        "animations": {
            "animation.oculus_slime.idle": {
                "loop": True,
                "animation_length": 1.6,
                "bones": {
                    "body": {
                        "scale": [
                            "1 + math.sin(query.anim_time * 225) * 0.06",
                            "1 - math.sin(query.anim_time * 225) * 0.08",
                            "1 + math.sin(query.anim_time * 225) * 0.06",
                        ]
                    },
                    "eyestalks": {"rotation": ["math.sin(query.anim_time * 225) * 8", 0, 0]},
                    "tentacles": {"rotation": [0, 0, "math.cos(query.anim_time * 225) * 10"]},
                },
            },
            "animation.oculus_slime.walk": {
                "loop": True,
                "animation_length": 0.8,
                "bones": {
                    "body": {
                        "position": [0, "math.abs(math.sin(query.anim_time * 450)) * 3.8", 0],
                        "scale": [
                            "1 - math.abs(math.sin(query.anim_time * 450)) * 0.08",
                            "1 + math.abs(math.sin(query.anim_time * 450)) * 0.14",
                            "1 - math.abs(math.sin(query.anim_time * 450)) * 0.08",
                        ],
                    },
                    "eyestalks": {"rotation": ["math.sin(query.anim_time * 450) * 14", 0, 0]},
                },
            },
            "animation.oculus_slime.attack": {
                "animation_length": 0.45,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "0.18": [24, 0, 0], "0.45": [0, 0, 0]},
                        "scale": {"0.0": [1, 1, 1], "0.18": [1.15, 0.82, 1.15], "0.45": [1, 1, 1]},
                    }
                },
            },
        },
    }

    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))
    _save_json(GEO_DIR / "oculus_slime.geo.json", geo)
    _save_json(ANIM_DIR / "oculus_slime.animation.json", anim)
    tex.save(TEX_DIR / "oculus_slime.png")
    glow.save(TEX_DIR / "oculus_slime_glowmask.png")


def build_lens_guardian() -> None:
    parts = [
        ("waist_belt", [9, 4, 7], "metal_trim"),
        ("belt_buckle", [3, 3, 2], "cyan_glow"),
        ("tasset_l", [4, 5, 6], "slate"),
        ("tasset_r", [4, 5, 6], "slate"),
        ("chest_rib_l", [4, 10, 8], "gaze_stone"),
        ("chest_rib_r", [4, 10, 8], "gaze_stone"),
        ("chest_back", [8, 10, 3], "slate"),
        ("gorget", [10, 2, 8], "metal_trim"),
        ("focus_lens_core", [4, 6, 4], "cyan_glow"),
        ("lens_ring_top", [5, 1, 5], "metal_trim"),
        ("lens_ring_bot", [5, 1, 5], "metal_trim"),
        ("pauldron_l", [7, 5, 8], "gaze_stone"),
        ("pauldron_r", [7, 5, 8], "gaze_stone"),
        ("horn_l", [2, 6, 2], "magenta_crystal"),
        ("horn_r", [2, 6, 2], "magenta_crystal"),
        ("floating_shard_l", [2, 5, 2], "cyan_glow"),
        ("floating_shard_r", [2, 5, 2], "cyan_glow"),
        ("helm_core", [8, 8, 8], "gaze_stone"),
        ("visor_brow", [10, 3, 9], "metal_trim"),
        ("crown_crest_l", [2, 5, 3], "magenta_crystal"),
        ("crown_crest_r", [2, 5, 3], "magenta_crystal"),
        ("cyclops_sclera", [5, 5, 1], "sclera"),
        ("cyclops_iris", [3, 3, 1], "cyan_glow"),
        ("cyclops_pupil", [1, 2, 1], "pupil"),
        ("upper_arm_l", [4, 7, 4], "slate"),
        ("gauntlet_l", [5, 8, 5], "gaze_stone"),
        ("claw_l1", [1, 3, 2], "metal_trim"),
        ("claw_l2", [1, 3, 2], "metal_trim"),
        ("upper_arm_r", [4, 7, 4], "slate"),
        ("gauntlet_r", [5, 8, 5], "gaze_stone"),
        ("claw_r1", [1, 3, 2], "metal_trim"),
        ("claw_r2", [1, 3, 2], "metal_trim"),
        ("thigh_l", [4, 6, 5], "slate"),
        ("knee_guard_l", [5, 3, 6], "metal_trim"),
        ("shin_l", [4, 7, 5], "gaze_stone"),
        ("thigh_r", [4, 6, 5], "slate"),
        ("knee_guard_r", [5, 3, 6], "metal_trim"),
        ("shin_r", [4, 7, 5], "gaze_stone"),
    ]
    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=300 + i)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.lens_guardian",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 2.8,
                    "visible_bounds_height": 3.2,
                    "visible_bounds_offset": [0, 1.4, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "torso",
                        "parent": "root",
                        "pivot": [0, 14, 0],
                        "cubes": [
                            _cube([-4.5, 13, -3.5], [9, 4, 7], list(uvs["waist_belt"])),
                            _cube([-1.5, 13.5, -4.3], [3, 3, 2], list(uvs["belt_buckle"])),
                            _cube([3.5, 10, -3], [4, 5, 6], list(uvs["tasset_l"]), rotation=[0, 0, -14]),
                            _cube([-7.5, 10, -3], [4, 5, 6], list(uvs["tasset_r"]), rotation=[0, 0, 14]),
                            _cube([2, 17, -4], [4, 10, 8], list(uvs["chest_rib_l"])),
                            _cube([-6, 17, -4], [4, 10, 8], list(uvs["chest_rib_r"])),
                            _cube([-4, 17, 1], [8, 10, 3], list(uvs["chest_back"])),
                            _cube([-5, 26.5, -4], [10, 2, 8], list(uvs["gorget"])),
                            _cube([6, 24, -4], [7, 5, 8], list(uvs["pauldron_l"]), rotation=[0, 0, -14]),
                            _cube([-13, 24, -4], [7, 5, 8], list(uvs["pauldron_r"]), rotation=[0, 0, 14]),
                            _cube([10, 28, -1], [2, 6, 2], list(uvs["horn_l"]), rotation=[0, 0, -22]),
                            _cube([-12, 28, -1], [2, 6, 2], list(uvs["horn_r"]), rotation=[0, 0, 22]),
                        ],
                    },
                    {
                        "name": "focus_lens",
                        "parent": "torso",
                        "pivot": [0, 22, -1],
                        "cubes": [
                            _cube([-2, 19, -3], [4, 6, 4], list(uvs["focus_lens_core"]), rotation=[0, 45, 0]),
                            _cube([-2.5, 25, -3.5], [5, 1, 5], list(uvs["lens_ring_top"]), rotation=[0, 45, 0]),
                            _cube([-2.5, 18, -3.5], [5, 1, 5], list(uvs["lens_ring_bot"]), rotation=[0, 45, 0]),
                        ],
                    },
                    {
                        "name": "orbit_shards",
                        "parent": "torso",
                        "pivot": [0, 25, 0],
                        "cubes": [
                            _cube([14, 24, -1], [2, 5, 2], list(uvs["floating_shard_l"]), rotation=[15, 30, -10]),
                            _cube([-16, 24, -1], [2, 5, 2], list(uvs["floating_shard_r"]), rotation=[-15, 30, 10]),
                        ],
                    },
                    {
                        "name": "head",
                        "parent": "torso",
                        "pivot": [0, 28.5, 0],
                        "cubes": [
                            _cube([-4, 28.5, -4], [8, 8, 8], list(uvs["helm_core"])),
                            _cube([-5, 34.5, -4.5], [10, 3, 9], list(uvs["visor_brow"]), rotation=[8, 0, 0]),
                            _cube([3, 36, -2], [2, 5, 3], list(uvs["crown_crest_l"]), rotation=[-12, 0, -18]),
                            _cube([-5, 36, -2], [2, 5, 3], list(uvs["crown_crest_r"]), rotation=[-12, 0, 18]),
                            _cube([-2.5, 30, -4.5], [5, 5, 1], list(uvs["cyclops_sclera"])),
                            _cube([-1.5, 31, -5.0], [3, 3, 1], list(uvs["cyclops_iris"])),
                            _cube([-0.5, 31.5, -5.5], [1, 2, 1], list(uvs["cyclops_pupil"])),
                        ],
                    },
                    {
                        "name": "arm_left",
                        "parent": "torso",
                        "pivot": [8, 25, 0],
                        "cubes": [
                            _cube([6.5, 18, -2], [4, 7, 4], list(uvs["upper_arm_l"])),
                            _cube([6, 10, -2.5], [5, 8, 5], list(uvs["gauntlet_l"])),
                            _cube([6.5, 7, -2], [1, 3, 2], list(uvs["claw_l1"]), rotation=[12, 0, 0]),
                            _cube([9, 7, -2], [1, 3, 2], list(uvs["claw_l2"]), rotation=[12, 0, 0]),
                        ],
                    },
                    {
                        "name": "arm_right",
                        "parent": "torso",
                        "pivot": [-8, 25, 0],
                        "cubes": [
                            _cube([-10.5, 18, -2], [4, 7, 4], list(uvs["upper_arm_r"])),
                            _cube([-11, 10, -2.5], [5, 8, 5], list(uvs["gauntlet_r"])),
                            _cube([-7.5, 7, -2], [1, 3, 2], list(uvs["claw_r1"]), rotation=[12, 0, 0]),
                            _cube([-10, 7, -2], [1, 3, 2], list(uvs["claw_r2"]), rotation=[12, 0, 0]),
                        ],
                    },
                    {
                        "name": "leg_left",
                        "parent": "root",
                        "pivot": [3, 13, 0],
                        "cubes": [
                            _cube([1, 7, -2.5], [4, 6, 5], list(uvs["thigh_l"])),
                            _cube([0.5, 6, -3], [5, 3, 6], list(uvs["knee_guard_l"])),
                            _cube([1, 0, -2.5], [4, 7, 5], list(uvs["shin_l"])),
                        ],
                    },
                    {
                        "name": "leg_right",
                        "parent": "root",
                        "pivot": [-3, 13, 0],
                        "cubes": [
                            _cube([-5, 7, -2.5], [4, 6, 5], list(uvs["thigh_r"])),
                            _cube([-5.5, 6, -3], [5, 3, 6], list(uvs["knee_guard_r"])),
                            _cube([-5, 0, -2.5], [4, 7, 5], list(uvs["shin_r"])),
                        ],
                    },
                ],
            }
        ],
    }

    anim = {
        "format_version": "1.8.0",
        "animations": {
            "animation.lens_guardian.idle": {
                "loop": True,
                "animation_length": 2.0,
                "bones": {
                    "focus_lens": {"rotation": [0, "query.anim_time * 180", 0]},
                    "orbit_shards": {
                        "rotation": [0, "query.anim_time * -180", 0],
                        "position": [0, "math.sin(query.anim_time * 180) * 0.8", 0],
                    },
                    "torso": {"position": [0, "math.sin(query.anim_time * 180) * 0.4", 0]},
                    "head": {"rotation": ["math.sin(query.anim_time * 180) * 3", 0, 0]},
                },
            },
            "animation.lens_guardian.walk": {
                "loop": True,
                "animation_length": 1.0,
                "bones": {
                    "focus_lens": {"rotation": [0, "query.anim_time * 360", 0]},
                    "orbit_shards": {"rotation": [0, "query.anim_time * -360", 0]},
                    "leg_left": {"rotation": ["math.sin(query.anim_time * 360) * 28", 0, 0]},
                    "leg_right": {"rotation": ["-math.sin(query.anim_time * 360) * 28", 0, 0]},
                    "arm_left": {"rotation": ["-math.sin(query.anim_time * 360) * 22", 0, 0]},
                    "arm_right": {"rotation": ["math.sin(query.anim_time * 360) * 22", 0, 0]},
                },
            },
            "animation.lens_guardian.attack": {
                "animation_length": 0.55,
                "bones": {
                    "arm_left": {"rotation": {"0.0": [0, 0, 0], "0.22": [-75, 0, 0], "0.55": [0, 0, 0]}},
                    "arm_right": {"rotation": {"0.0": [0, 0, 0], "0.22": [-75, 0, 0], "0.55": [0, 0, 0]}},
                    "torso": {"rotation": {"0.0": [0, 0, 0], "0.22": [14, 0, 0], "0.55": [0, 0, 0]}},
                },
            },
        },
    }

    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))
    _save_json(GEO_DIR / "lens_guardian.geo.json", geo)
    _save_json(ANIM_DIR / "lens_guardian.animation.json", anim)
    tex.save(TEX_DIR / "lens_guardian.png")
    glow.save(TEX_DIR / "lens_guardian_glowmask.png")


if __name__ == "__main__":
    build_peeper()
    build_oculus_slime()
    build_lens_guardian()
    print("Generated v2 Peeper, Oculus Slime, and Lens Guardian GeckoLib assets.")
