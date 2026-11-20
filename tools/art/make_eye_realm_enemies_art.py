"""Generates GeckoLib models, animations, 128x128 textures, and glowmasks for Phase 4 Stage 3 Eye Realm enemies.

Entities:
- peeper: Hovering eyeball scout with slate eyelid hood, glowing cyan iris, and magenta eye-wings.
- oculus_slime: Bouncy twilight-magenta gel cube with recessed inner eye and angled crystal-gel spikes.
- lens_guardian: Towering Gaze Stone & Starlight Slate sentinel with hollowed chest Focus Lens and cyclopean helm.
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
)

ASSETS = Path("src/main/resources/assets/artilian")
GEO_DIR = ASSETS / "geo" / "entity"
ANIM_DIR = ASSETS / "animations" / "entity"
TEX_DIR = ASSETS / "textures" / "entity"

SLATE_RAMP = [
    (22, 17, 32, 255),
    (34, 27, 48, 255),
    (48, 38, 68, 255),
    (66, 53, 92, 255),
    (88, 72, 118, 255),
    (112, 94, 146, 255),
]

GAZE_STONE_RAMP = [
    (32, 24, 46, 255),
    (46, 35, 66, 255),
    (62, 48, 88, 255),
    (82, 64, 114, 255),
    (106, 84, 142, 255),
    (132, 108, 170, 255),
]

MAGENTA_RAMP = [
    (74, 20, 92, 255),
    (108, 28, 134, 255),
    (148, 42, 178, 255),
    (190, 56, 218, 255),
    (217, 70, 239, 255),
    (242, 136, 252, 255),
]

SCLERA_RAMP = [
    (148, 144, 172, 255),
    (176, 172, 198, 255),
    (204, 200, 224, 255),
    (226, 224, 242, 255),
    (242, 242, 252, 255),
    (255, 255, 255, 255),
]

CYAN_LENS_RAMP = [
    (24, 104, 128, 255),
    (40, 148, 176, 255),
    (68, 196, 224, 255),
    (104, 232, 248, 255),
    (125, 249, 255, 255),
    (214, 254, 255, 255),
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
    (128, 76, 24, 255),
    (168, 104, 32, 255),
    (208, 136, 44, 255),
    (242, 168, 60, 255),
    (255, 204, 104, 255),
    (255, 236, 168, 255),
]


def _save_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def _cube(origin: list[float], size: list[float], uv: list[int], rotation: list[float] | None = None) -> dict:
    c: dict = {"origin": origin, "size": size, "uv": uv}
    if rotation is not None:
        c["rotation"] = rotation
        c["pivot"] = [
            round(origin[0] + size[0] / 2.0, 2),
            round(origin[1] + size[1] / 2.0, 2),
            round(origin[2] + size[2] / 2.0, 2),
        ]
    return c


def _pack_uvs(items: list[tuple[str, list[float], str]], sheet_w: int = 128, sheet_h: int = 128) -> dict[str, tuple[int, int]]:
    """Deterministic shelf-packer for non-overlapping box UVs."""
    x, y, row_h = 0, 0, 0
    placed: dict[str, tuple[int, int]] = {}
    for name, size, _ in items:
        w, h, d = int(round(size[0])), int(round(size[1])), int(round(size[2]))
        _, _, rw, rh = box_rect(0, 0, w, h, d)
        if x + rw > sheet_w:
            x = 0
            y += row_h + 1
            row_h = 0
        if y + rh > sheet_h:
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
        paint_weathered_stone(tex, u, v, w, h, d, SLATE_RAMP, seed=seed)
    elif kind == "gaze_stone":
        paint_weathered_stone(tex, u, v, w, h, d, GAZE_STONE_RAMP, seed=seed)
    elif kind == "metal_trim":
        paint_forged_metal(tex, u, v, w, h, d, GOLD_TRIM_RAMP, seed=seed)
    elif kind == "magenta_organic":
        paint_organic_shaded(tex, u, v, w, h, d, MAGENTA_RAMP, seed=seed)
    elif kind == "magenta_crystal":
        paint_faceted_crystal(tex, u, v, w, h, d, MAGENTA_RAMP, seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, MAGENTA_RAMP, seed=seed)
    elif kind == "sclera":
        paint_organic_shaded(tex, u, v, w, h, d, SCLERA_RAMP, seed=seed)
    elif kind == "cyan_glow":
        paint_faceted_crystal(tex, u, v, w, h, d, CYAN_LENS_RAMP, seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, CYAN_LENS_RAMP, seed=seed)
    elif kind == "pupil":
        paint_organic_shaded(tex, u, v, w, h, d, PUPIL_RAMP, seed=seed)
    else:
        raise ValueError(f"Unknown material kind: {kind}")


def build_peeper() -> None:
    parts = [
        ("sclera_core", [8, 8, 8], "sclera"),
        ("eyelid_hood", [10, 4, 10], "slate"),
        ("lower_lid", [10, 2, 10], "slate"),
        ("brow_crest", [6, 3, 4], "magenta_organic"),
        ("iris_ring", [6, 6, 1], "cyan_glow"),
        ("pupil_core", [2, 2, 1], "pupil"),
        ("wing_arm_l", [6, 2, 2], "slate"),
        ("wing_membrane_l", [7, 6, 1], "magenta_crystal"),
        ("wing_arm_r", [6, 2, 2], "slate"),
        ("wing_membrane_r", [7, 6, 1], "magenta_crystal"),
        ("tail_base", [2, 2, 5], "slate"),
        ("tail_tip", [3, 3, 3], "cyan_glow"),
    ]
    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=100 + i)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.peeper",
                    "texture_width": 128,
                    "texture_height": 128,
                    "visible_bounds_width": 2.0,
                    "visible_bounds_height": 2.0,
                    "visible_bounds_offset": [0, 0.75, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "body",
                        "parent": "root",
                        "pivot": [0, 9, 0],
                        "cubes": [
                            _cube([-4, 5, -4], [8, 8, 8], list(uvs["sclera_core"])),
                            _cube([-5, 10, -5], [10, 4, 10], list(uvs["eyelid_hood"])),
                            _cube([-5, 4, -5], [10, 2, 10], list(uvs["lower_lid"])),
                            _cube([-3, 13, -5], [6, 3, 4], list(uvs["brow_crest"]), rotation=[-15, 0, 0]),
                            _cube([-3, 6, -4.6], [6, 6, 1], list(uvs["iris_ring"])),
                            _cube([-1, 8, -5.2], [2, 2, 1], list(uvs["pupil_core"])),
                        ],
                    },
                    {
                        "name": "wing_left",
                        "parent": "body",
                        "pivot": [5, 10, 0],
                        "cubes": [
                            _cube([5, 9, -1], [6, 2, 2], list(uvs["wing_arm_l"]), rotation=[0, 0, -18]),
                            _cube([6, 4, 0], [7, 6, 1], list(uvs["wing_membrane_l"]), rotation=[0, 0, -12]),
                        ],
                    },
                    {
                        "name": "wing_right",
                        "parent": "body",
                        "pivot": [-5, 10, 0],
                        "cubes": [
                            _cube([-11, 9, -1], [6, 2, 2], list(uvs["wing_arm_r"]), rotation=[0, 0, 18]),
                            _cube([-13, 4, 0], [7, 6, 1], list(uvs["wing_membrane_r"]), rotation=[0, 0, 12]),
                        ],
                    },
                    {
                        "name": "tail_stalk",
                        "parent": "body",
                        "pivot": [0, 7, 5],
                        "cubes": [
                            _cube([-1, 6, 5], [2, 2, 5], list(uvs["tail_base"]), rotation=[15, 0, 0]),
                            _cube([-1.5, 4.5, 9], [3, 3, 3], list(uvs["tail_tip"]), rotation=[25, 0, 0]),
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
                    "body": {"position": [0, "math.sin(query.anim_time * 225) * 1.2", 0]},
                    "wing_left": {"rotation": [0, 0, "math.sin(query.anim_time * 450) * 18"]},
                    "wing_right": {"rotation": [0, 0, "-math.sin(query.anim_time * 450) * 18"]},
                    "tail_stalk": {"rotation": ["math.cos(query.anim_time * 225) * 10", 0, 0]},
                },
            },
            "animation.peeper.walk": {
                "loop": True,
                "animation_length": 0.8,
                "bones": {
                    "body": {"position": [0, "math.sin(query.anim_time * 450) * 1.6", 0], "rotation": [12, 0, 0]},
                    "wing_left": {"rotation": [0, 0, "math.sin(query.anim_time * 900) * 28"]},
                    "wing_right": {"rotation": [0, 0, "-math.sin(query.anim_time * 900) * 28"]},
                },
            },
            "animation.peeper.attack": {
                "animation_length": 0.45,
                "bones": {
                    "body": {"rotation": {"0.0": [0, 0, 0], "0.18": [28, 0, 0], "0.45": [0, 0, 0]}},
                    "wing_left": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 0, -40], "0.45": [0, 0, 0]}},
                    "wing_right": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 0, 40], "0.45": [0, 0, 0]}},
                },
            },
        },
    }

    assert_uv_layout_clean(geo, (128, 128))
    _save_json(GEO_DIR / "peeper.geo.json", geo)
    _save_json(ANIM_DIR / "peeper.animation.json", anim)
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    tex.save(TEX_DIR / "peeper.png")
    glow.save(TEX_DIR / "peeper_glowmask.png")


def build_oculus_slime() -> None:
    parts = [
        ("outer_gel", [14, 12, 14], "magenta_organic"),
        ("base_skirt", [16, 2, 16], "slate"),
        ("inner_eye_core", [8, 8, 8], "sclera"),
        ("iris_front", [6, 6, 1], "cyan_glow"),
        ("pupil_front", [2, 2, 1], "pupil"),
        ("crest_spike_l", [3, 5, 3], "magenta_crystal"),
        ("crest_spike_r", [3, 5, 3], "magenta_crystal"),
        ("back_spike", [4, 5, 4], "cyan_glow"),
    ]
    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=200 + i)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.oculus_slime",
                    "texture_width": 128,
                    "texture_height": 128,
                    "visible_bounds_width": 2.0,
                    "visible_bounds_height": 2.0,
                    "visible_bounds_offset": [0, 0.75, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "body",
                        "parent": "root",
                        "pivot": [0, 0, 0],
                        "cubes": [
                            _cube([-7, 1, -7], [14, 12, 14], list(uvs["outer_gel"])),
                            _cube([-8, 0, -8], [16, 2, 16], list(uvs["base_skirt"])),
                            _cube([-4, 3, -4], [8, 8, 8], list(uvs["inner_eye_core"])),
                            _cube([-3, 4, -7.5], [6, 6, 1], list(uvs["iris_front"])),
                            _cube([-1, 6, -8.1], [2, 2, 1], list(uvs["pupil_front"])),
                            _cube([5, 11, -2], [3, 5, 3], list(uvs["crest_spike_l"]), rotation=[0, 0, -25]),
                            _cube([-8, 11, -2], [3, 5, 3], list(uvs["crest_spike_r"]), rotation=[0, 0, 25]),
                            _cube([-2, 11, 4], [4, 5, 4], list(uvs["back_spike"]), rotation=[22, 0, 0]),
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
                    }
                },
            },
            "animation.oculus_slime.walk": {
                "loop": True,
                "animation_length": 0.8,
                "bones": {
                    "body": {
                        "position": [0, "math.abs(math.sin(query.anim_time * 450)) * 3.5", 0],
                        "scale": [
                            "1 - math.abs(math.sin(query.anim_time * 450)) * 0.08",
                            "1 + math.abs(math.sin(query.anim_time * 450)) * 0.14",
                            "1 - math.abs(math.sin(query.anim_time * 450)) * 0.08",
                        ],
                    }
                },
            },
            "animation.oculus_slime.attack": {
                "animation_length": 0.45,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "0.18": [22, 0, 0], "0.45": [0, 0, 0]},
                        "scale": {"0.0": [1, 1, 1], "0.18": [1.15, 0.82, 1.15], "0.45": [1, 1, 1]},
                    }
                },
            },
        },
    }

    assert_uv_layout_clean(geo, (128, 128))
    _save_json(GEO_DIR / "oculus_slime.geo.json", geo)
    _save_json(ANIM_DIR / "oculus_slime.animation.json", anim)
    tex.save(TEX_DIR / "oculus_slime.png")
    glow.save(TEX_DIR / "oculus_slime_glowmask.png")


def build_lens_guardian() -> None:
    parts = [
        ("waist", [8, 5, 6], "slate"),
        ("chest_frame_l", [4, 10, 8], "gaze_stone"),
        ("chest_frame_r", [4, 10, 8], "gaze_stone"),
        ("chest_back", [8, 10, 3], "slate"),
        ("focus_lens", [4, 6, 4], "cyan_glow"),
        ("pauldron_l", [6, 5, 8], "gaze_stone"),
        ("pauldron_r", [6, 5, 8], "gaze_stone"),
        ("horn_l", [2, 6, 2], "magenta_crystal"),
        ("horn_r", [2, 6, 2], "magenta_crystal"),
        ("helm", [8, 8, 8], "gaze_stone"),
        ("visor_brow", [10, 3, 9], "metal_trim"),
        ("cyclops_iris", [4, 4, 1], "cyan_glow"),
        ("cyclops_pupil", [2, 2, 1], "pupil"),
        ("upper_arm_l", [4, 7, 4], "slate"),
        ("gauntlet_l", [5, 8, 5], "gaze_stone"),
        ("upper_arm_r", [4, 7, 4], "slate"),
        ("gauntlet_r", [5, 8, 5], "gaze_stone"),
        ("greave_l", [4, 13, 5], "gaze_stone"),
        ("greave_r", [4, 13, 5], "gaze_stone"),
    ]
    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=300 + i)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.lens_guardian",
                    "texture_width": 128,
                    "texture_height": 128,
                    "visible_bounds_width": 2.5,
                    "visible_bounds_height": 3.0,
                    "visible_bounds_offset": [0, 1.35, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "torso",
                        "parent": "root",
                        "pivot": [0, 14, 0],
                        "cubes": [
                            _cube([-4, 13, -3], [8, 5, 6], list(uvs["waist"])),
                            _cube([2, 18, -4], [4, 10, 8], list(uvs["chest_frame_l"])),
                            _cube([-6, 18, -4], [4, 10, 8], list(uvs["chest_frame_r"])),
                            _cube([-4, 18, 1], [8, 10, 3], list(uvs["chest_back"])),
                            _cube([6, 24, -4], [6, 5, 8], list(uvs["pauldron_l"]), rotation=[0, 0, -12]),
                            _cube([-12, 24, -4], [6, 5, 8], list(uvs["pauldron_r"]), rotation=[0, 0, 12]),
                            _cube([9, 28, -1], [2, 6, 2], list(uvs["horn_l"]), rotation=[0, 0, -20]),
                            _cube([-11, 28, -1], [2, 6, 2], list(uvs["horn_r"]), rotation=[0, 0, 20]),
                        ],
                    },
                    {
                        "name": "focus_lens",
                        "parent": "torso",
                        "pivot": [0, 23, -1],
                        "cubes": [
                            _cube([-2, 20, -3], [4, 6, 4], list(uvs["focus_lens"]), rotation=[0, 45, 0]),
                        ],
                    },
                    {
                        "name": "head",
                        "parent": "torso",
                        "pivot": [0, 28, 0],
                        "cubes": [
                            _cube([-4, 28, -4], [8, 8, 8], list(uvs["helm"])),
                            _cube([-5, 34, -4.5], [10, 3, 9], list(uvs["visor_brow"]), rotation=[8, 0, 0]),
                            _cube([-2, 30, -4.6], [4, 4, 1], list(uvs["cyclops_iris"])),
                            _cube([-1, 31, -5.2], [2, 2, 1], list(uvs["cyclops_pupil"])),
                        ],
                    },
                    {
                        "name": "arm_left",
                        "parent": "torso",
                        "pivot": [8, 25, 0],
                        "cubes": [
                            _cube([6, 18, -2], [4, 7, 4], list(uvs["upper_arm_l"])),
                            _cube([5.5, 10, -2.5], [5, 8, 5], list(uvs["gauntlet_l"])),
                        ],
                    },
                    {
                        "name": "arm_right",
                        "parent": "torso",
                        "pivot": [-8, 25, 0],
                        "cubes": [
                            _cube([-10, 18, -2], [4, 7, 4], list(uvs["upper_arm_r"])),
                            _cube([-10.5, 10, -2.5], [5, 8, 5], list(uvs["gauntlet_r"])),
                        ],
                    },
                    {
                        "name": "leg_left",
                        "parent": "root",
                        "pivot": [3, 13, 0],
                        "cubes": [_cube([1, 0, -2.5], [4, 13, 5], list(uvs["greave_l"]))],
                    },
                    {
                        "name": "leg_right",
                        "parent": "root",
                        "pivot": [-3, 13, 0],
                        "cubes": [_cube([-5, 0, -2.5], [4, 13, 5], list(uvs["greave_r"]))],
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
                    "torso": {"position": [0, "math.sin(query.anim_time * 180) * 0.4", 0]},
                    "head": {"rotation": ["math.sin(query.anim_time * 180) * 3", 0, 0]},
                },
            },
            "animation.lens_guardian.walk": {
                "loop": True,
                "animation_length": 1.0,
                "bones": {
                    "focus_lens": {"rotation": [0, "query.anim_time * 360", 0]},
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

    assert_uv_layout_clean(geo, (128, 128))
    _save_json(GEO_DIR / "lens_guardian.geo.json", geo)
    _save_json(ANIM_DIR / "lens_guardian.animation.json", anim)
    tex.save(TEX_DIR / "lens_guardian.png")
    glow.save(TEX_DIR / "lens_guardian_glowmask.png")


if __name__ == "__main__":
    build_peeper()
    build_oculus_slime()
    build_lens_guardian()
    print("Generated Peeper, Oculus Slime, and Lens Guardian GeckoLib assets.")
