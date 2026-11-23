"""Generates v2 GeckoLib model, animations, 256x128 texture, and glowmask for Phase 4 Stage 4 Blinky Pet.

Blinky is Alex's friendly Eye Realm ocular familiar:
- Rounded Deep Artilian Slate & Starlight Indigo cowl with forged Sunsteel Gold ocular bezel
- Recessed 3D eye socket with glowing Pupil Glow (#7DF9FF) iris, round pupil, and catchlight glint
- Articulated upper and lower eyelids (lid_top / lid_bottom) that periodically blink shut
- Swept Twilight Magenta & Starlight Teal eye-wings, orbiting halo crystal crown, and 3-segment comet tail
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

INDIGO_COWL_RAMP = [
    (28, 22, 44, 255),
    (42, 34, 68, 255),
    (60, 48, 94, 255),
    (80, 66, 124, 255),
    (104, 88, 156, 255),
    (132, 114, 186, 255),
]

SLATE_TRIM_RAMP = [
    (20, 16, 30, 255),
    (32, 26, 48, 255),
    (46, 38, 68, 255),
    (64, 54, 92, 255),
    (86, 74, 118, 255),
    (112, 98, 148, 255),
]

GOLD_BEZEL_RAMP = [
    (132, 76, 22, 255),
    (176, 108, 32, 255),
    (218, 142, 46, 255),
    (250, 176, 62, 255),
    (255, 210, 108, 255),
    (255, 240, 176, 255),
]

MAGENTA_WING_RAMP = [
    (74, 22, 92, 255),
    (110, 32, 136, 255),
    (152, 44, 184, 255),
    (194, 58, 224, 255),
    (217, 70, 239, 255),
    (246, 150, 255, 255),
]

TEAL_FIN_RAMP = [
    (18, 92, 86, 255),
    (28, 136, 126, 255),
    (40, 180, 164, 255),
    (56, 217, 192, 255),
    (116, 238, 218, 255),
    (196, 252, 244, 255),
]

SCLERA_RAMP = [
    (158, 156, 184, 255),
    (186, 184, 210, 255),
    (212, 210, 232, 255),
    (234, 234, 248, 255),
    (246, 246, 255, 255),
    (255, 255, 255, 255),
]

CYAN_IRIS_RAMP = [
    (24, 108, 132, 255),
    (42, 154, 184, 255),
    (70, 202, 230, 255),
    (108, 236, 250, 255),
    (125, 249, 255, 255),
    (224, 255, 255, 255),
]

PUPIL_RAMP = [
    (10, 8, 18, 255),
    (18, 14, 30, 255),
    (28, 22, 46, 255),
    (42, 32, 66, 255),
    (58, 46, 88, 255),
    (78, 64, 112, 255),
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
    if kind == "indigo_cowl":
        paint_organic_shaded(tex, u, v, w, h, d, INDIGO_COWL_RAMP, spot_color=(56, 217, 192, 120), seed=seed)
    elif kind == "slate_trim":
        paint_weathered_stone(tex, u, v, w, h, d, SLATE_TRIM_RAMP, crack_color=(217, 70, 239, 85), seed=seed)
    elif kind == "gold_bezel":
        paint_forged_metal(tex, u, v, w, h, d, GOLD_BEZEL_RAMP, seed=seed)
    elif kind == "sclera":
        paint_organic_shaded(tex, u, v, w, h, d, SCLERA_RAMP, spot_color=(125, 249, 255, 110), seed=seed)
    elif kind == "cyan_glow":
        paint_faceted_crystal(tex, u, v, w, h, d, CYAN_IRIS_RAMP, vein_color=(235, 255, 255, 240), seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, CYAN_IRIS_RAMP, seed=seed)
    elif kind == "pupil":
        paint_organic_shaded(tex, u, v, w, h, d, PUPIL_RAMP, seed=seed)
    elif kind == "glint":
        paint_faceted_crystal(tex, u, v, w, h, d, SCLERA_RAMP, seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, SCLERA_RAMP, seed=seed)
    elif kind == "magenta_wing":
        paint_organic_shaded(tex, u, v, w, h, d, MAGENTA_WING_RAMP, spot_color=(125, 249, 255, 190), seed=seed)
        faces = box_faces(u, v, w, h, d)
        fx, fy, fw, fh = faces["front"]
        if fw >= 3 and fh >= 3:
            put_px(tex, fx + fw // 2, fy + fh // 2, (125, 249, 255, 255))
            put_px(glow, fx + fw // 2, fy + fh // 2, (125, 249, 255, 255))
    elif kind == "teal_glow":
        paint_faceted_crystal(tex, u, v, w, h, d, TEAL_FIN_RAMP, vein_color=(220, 255, 250, 220), seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, TEAL_FIN_RAMP, seed=seed)
    else:
        raise ValueError(f"Unknown material kind: {kind}")


def build_blinky() -> None:
    parts = [
        ("sclera_core", [8, 8, 8], "sclera"),
        ("cowl_dome", [10, 3, 10], "indigo_cowl"),
        ("cowl_crest", [6, 2, 7], "slate_trim"),
        ("cowl_base", [10, 2, 10], "indigo_cowl"),
        ("cheek_l", [2, 6, 9], "indigo_cowl"),
        ("cheek_r", [2, 6, 9], "indigo_cowl"),
        ("bezel_top", [8, 1, 2], "gold_bezel"),
        ("bezel_bottom", [8, 1, 2], "gold_bezel"),
        ("bezel_l", [1, 6, 2], "gold_bezel"),
        ("bezel_r", [1, 6, 2], "gold_bezel"),
        ("iris_ring", [6, 6, 1], "cyan_glow"),
        ("pupil_core", [3, 3, 1], "pupil"),
        ("catchlight", [1, 1, 1], "glint"),
        ("lid_top_plate", [8, 4, 2], "indigo_cowl"),
        ("lid_top_lash", [8, 1, 2], "gold_bezel"),
        ("lid_bottom_plate", [8, 3, 2], "indigo_cowl"),
        ("wing_root_l", [4, 2, 2], "slate_trim"),
        ("wing_feather_l1", [6, 3, 1], "magenta_wing"),
        ("wing_feather_l2", [5, 2, 1], "teal_glow"),
        ("wing_tip_l", [3, 2, 1], "cyan_glow"),
        ("wing_root_r", [4, 2, 2], "slate_trim"),
        ("wing_feather_r1", [6, 3, 1], "magenta_wing"),
        ("wing_feather_r2", [5, 2, 1], "teal_glow"),
        ("wing_tip_r", [3, 2, 1], "cyan_glow"),
        ("crown_gem", [3, 4, 3], "cyan_glow"),
        ("crown_mote_l", [2, 2, 2], "teal_glow"),
        ("crown_mote_r", [2, 2, 2], "magenta_wing"),
        ("tail_1", [3, 3, 4], "indigo_cowl"),
        ("tail_2", [2, 2, 4], "magenta_wing"),
        ("tail_tip", [2, 2, 3], "cyan_glow"),
    ]
    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=700 + i)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.blinky",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 2.4,
                    "visible_bounds_height": 2.2,
                    "visible_bounds_offset": [0, 0.9, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "body",
                        "parent": "root",
                        "pivot": [0, 10, 0],
                        "cubes": [
                            _cube([-4, 6, -4], [8, 8, 8], list(uvs["sclera_core"])),
                            _cube([-5, 12.5, -5], [10, 3, 10], list(uvs["cowl_dome"])),
                            _cube([-3, 15, -3.5], [6, 2, 7], list(uvs["cowl_crest"])),
                            _cube([-5, 4.5, -5], [10, 2, 10], list(uvs["cowl_base"])),
                            _cube([3.8, 6.5, -4.5], [2, 6, 9], list(uvs["cheek_l"])),
                            _cube([-5.8, 6.5, -4.5], [2, 6, 9], list(uvs["cheek_r"])),
                            _cube([-4, 13, -5.2], [8, 1, 2], list(uvs["bezel_top"])),
                            _cube([-4, 6, -5.2], [8, 1, 2], list(uvs["bezel_bottom"])),
                            _cube([3.5, 7, -5.2], [1, 6, 2], list(uvs["bezel_l"])),
                            _cube([-4.5, 7, -5.2], [1, 6, 2], list(uvs["bezel_r"])),
                            _cube([-3, 7, -4.5], [6, 6, 1], list(uvs["iris_ring"])),
                            _cube([-1.5, 8.5, -4.9], [3, 3, 1], list(uvs["pupil_core"])),
                            _cube([0.6, 10.6, -5.1], [1, 1, 1], list(uvs["catchlight"])),
                        ],
                    },
                    {
                        "name": "lid_top",
                        "parent": "body",
                        "pivot": [0, 13.5, -4.5],
                        "cubes": [
                            _cube([-4, 13.2, -5.6], [8, 4, 2], list(uvs["lid_top_plate"])),
                            _cube([-4, 12.6, -5.8], [8, 1, 2], list(uvs["lid_top_lash"])),
                        ],
                    },
                    {
                        "name": "lid_bottom",
                        "parent": "body",
                        "pivot": [0, 6.5, -4.5],
                        "cubes": [
                            _cube([-4, 3.8, -5.6], [8, 3, 2], list(uvs["lid_bottom_plate"])),
                        ],
                    },
                    {
                        "name": "wing_left",
                        "parent": "body",
                        "pivot": [5.2, 10.5, 0],
                        "cubes": [
                            _cube([5.0, 9.5, -1], [4, 2, 2], list(uvs["wing_root_l"]), rotation=[0, 0, -14]),
                            _cube([7.5, 10.0, -0.5], [6, 3, 1], list(uvs["wing_feather_l1"]), rotation=[0, 8, -22]),
                            _cube([7.0, 8.2, 0.2], [5, 2, 1], list(uvs["wing_feather_l2"]), rotation=[0, 10, -8]),
                            _cube([12.0, 11.4, -0.4], [3, 2, 1], list(uvs["wing_tip_l"]), rotation=[0, 12, -28]),
                        ],
                    },
                    {
                        "name": "wing_right",
                        "parent": "body",
                        "pivot": [-5.2, 10.5, 0],
                        "cubes": [
                            _cube([-9.0, 9.5, -1], [4, 2, 2], list(uvs["wing_root_r"]), rotation=[0, 0, 14]),
                            _cube([-13.5, 10.0, -0.5], [6, 3, 1], list(uvs["wing_feather_r1"]), rotation=[0, -8, 22]),
                            _cube([-12.0, 8.2, 0.2], [5, 2, 1], list(uvs["wing_feather_r2"]), rotation=[0, -10, 8]),
                            _cube([-15.0, 11.4, -0.4], [3, 2, 1], list(uvs["wing_tip_r"]), rotation=[0, -12, 28]),
                        ],
                    },
                    {
                        "name": "crown_halo",
                        "parent": "body",
                        "pivot": [0, 19, 0],
                        "cubes": [
                            _cube([-1.5, 18.0, -1.5], [3, 4, 3], list(uvs["crown_gem"]), rotation=[10, 45, 0]),
                            _cube([3.8, 17.5, -1.0], [2, 2, 2], list(uvs["crown_mote_l"]), rotation=[0, 20, -15]),
                            _cube([-5.8, 17.5, -1.0], [2, 2, 2], list(uvs["crown_mote_r"]), rotation=[0, -20, 15]),
                        ],
                    },
                    {
                        "name": "tail",
                        "parent": "body",
                        "pivot": [0, 8.5, 4.5],
                        "cubes": [
                            _cube([-1.5, 7.0, 4.2], [3, 3, 4], list(uvs["tail_1"]), rotation=[10, 0, 0]),
                            _cube([-1.0, 6.8, 7.8], [2, 2, 4], list(uvs["tail_2"]), rotation=[16, 0, 0]),
                            _cube([-1.0, 6.2, 11.2], [2, 2, 3], list(uvs["tail_tip"]), rotation=[24, 0, 0]),
                        ],
                    },
                ],
            }
        ],
    }

    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))
    _save_json(GEO_DIR / "blinky.geo.json", geo)
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    tex.save(TEX_DIR / "blinky.png")
    glow.save(TEX_DIR / "blinky_glowmask.png")

    anims = {
        "format_version": "1.8.0",
        "animations": {
            "animation.blinky.idle": {
                "loop": True,
                "animation_length": 3.0,
                "bones": {
                    "body": {"position": {"0.0": [0, 0, 0], "1.5": [0, 1.4, 0], "3.0": [0, 0, 0]}},
                    "lid_top": {
                        "scale": {
                            "0.0": [1, 0.15, 1],
                            "1.25": [1, 0.15, 1],
                            "1.38": [1, 1.05, 1],
                            "1.52": [1, 0.15, 1],
                            "3.0": [1, 0.15, 1],
                        }
                    },
                    "lid_bottom": {
                        "scale": {
                            "0.0": [1, 0.15, 1],
                            "1.25": [1, 0.15, 1],
                            "1.38": [1, 1.05, 1],
                            "1.52": [1, 0.15, 1],
                            "3.0": [1, 0.15, 1],
                        }
                    },
                    "wing_left": {"rotation": {"0.0": [0, 0, 4], "0.75": [0, 0, -14], "1.5": [0, 0, 4], "2.25": [0, 0, -14], "3.0": [0, 0, 4]}},
                    "wing_right": {"rotation": {"0.0": [0, 0, -4], "0.75": [0, 0, 14], "1.5": [0, 0, -4], "2.25": [0, 0, 14], "3.0": [0, 0, -4]}},
                    "crown_halo": {"rotation": {"0.0": [0, 0, 0], "3.0": [0, 360, 0]}, "position": {"0.0": [0, 0, 0], "1.5": [0, 0.8, 0], "3.0": [0, 0, 0]}},
                    "tail": {"rotation": {"0.0": [-6, -10, 0], "1.5": [6, 10, 0], "3.0": [-6, -10, 0]}},
                },
            },
            "animation.blinky.walk": {
                "loop": True,
                "animation_length": 1.2,
                "bones": {
                    "body": {"rotation": {"0.0": [10, 0, 0], "0.6": [14, 0, 0], "1.2": [10, 0, 0]}, "position": {"0.0": [0, 0.5, 0], "0.6": [0, 1.8, 0], "1.2": [0, 0.5, 0]}},
                    "lid_top": {"scale": {"0.0": [1, 0.12, 1], "1.2": [1, 0.12, 1]}},
                    "lid_bottom": {"scale": {"0.0": [1, 0.12, 1], "1.2": [1, 0.12, 1]}},
                    "wing_left": {"rotation": {"0.0": [0, 10, 12], "0.3": [0, -10, -24], "0.6": [0, 10, 12], "0.9": [0, -10, -24], "1.2": [0, 10, 12]}},
                    "wing_right": {"rotation": {"0.0": [0, -10, -12], "0.3": [0, 10, 24], "0.6": [0, -10, -12], "0.9": [0, 10, 24], "1.2": [0, -10, -12]}},
                    "crown_halo": {"rotation": {"0.0": [0, 0, 0], "1.2": [0, 180, 0]}},
                    "tail": {"rotation": {"0.0": [-10, -16, 0], "0.6": [-10, 16, 0], "1.2": [-10, -16, 0]}},
                },
            },
            "animation.blinky.sit": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "body": {"position": {"0.0": [0, -3.5, 0], "1.2": [0, -3.0, 0], "2.4": [0, -3.5, 0]}},
                    "lid_top": {"scale": {"0.0": [1, 0.45, 1], "1.2": [1, 0.55, 1], "2.4": [1, 0.45, 1]}},
                    "lid_bottom": {"scale": {"0.0": [1, 0.45, 1], "1.2": [1, 0.55, 1], "2.4": [1, 0.45, 1]}},
                    "wing_left": {"rotation": {"0.0": [0, 18, 16], "1.2": [0, 22, 20], "2.4": [0, 18, 16]}},
                    "wing_right": {"rotation": {"0.0": [0, -18, -16], "1.2": [0, -22, -20], "2.4": [0, -18, -16]}},
                    "crown_halo": {"rotation": {"0.0": [0, 0, 0], "2.4": [0, 180, 0]}},
                    "tail": {"rotation": {"0.0": [8, -6, 0], "1.2": [8, 6, 0], "2.4": [8, -6, 0]}},
                },
            },
            "animation.blinky.attack": {
                "animation_length": 0.5,
                "bones": {
                    "body": {"rotation": {"0.0": [0, 0, 0], "0.18": [-14, 0, 0], "0.34": [18, 0, 0], "0.5": [0, 0, 0]}},
                    "lid_top": {"scale": {"0.0": [1, 0.08, 1], "0.5": [1, 0.08, 1]}},
                    "lid_bottom": {"scale": {"0.0": [1, 0.08, 1], "0.5": [1, 0.08, 1]}},
                    "wing_left": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 20, -28], "0.34": [0, -18, 18], "0.5": [0, 0, 0]}},
                    "wing_right": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, -20, 28], "0.34": [0, 18, -18], "0.5": [0, 0, 0]}},
                },
            },
        },
    }
    _save_json(ANIM_DIR / "blinky.animation.json", anims)
    print("Generated Blinky v2 256x128 art, glowmask, geo, and animations.")


if __name__ == "__main__":
    build_blinky()
