"""Generates GeckoLib model, animations, 256x128 texture, and glowmask for Fangwing.

Fangwing is a scary purple bat pet (SHADOW / AIR) inspired by a kid's drawing:
- Sculpted Deep Void-Plum & Dark Slate body with layered brow ridge and tapered lower torso
- Tall pointed bat ears with glowing orchid inner ridges
- Raised 3D round ocular bezels with glowing Twilight Magenta eyes and dark slit pupils
- Recessed dark abyssal mouth cavity framed by 4 sharp 3D ivory fangs (2 upper, 2 lower)
- Broad, articulated 2-segment geometric bat wings with dark bone spars, clawed thumbs, and arcane wing veins
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

VOID_PLUM_RAMP = [
    (34, 18, 54, 255),
    (52, 28, 82, 255),
    (74, 40, 114, 255),
    (98, 54, 148, 255),
    (124, 72, 182, 255),
    (152, 96, 212, 255),
]

DARK_SLATE_RAMP = [
    (20, 14, 34, 255),
    (32, 22, 52, 255),
    (46, 32, 74, 255),
    (62, 44, 98, 255),
    (82, 60, 126, 255),
    (106, 80, 156, 255),
]

VIOLET_MEMBRANE_RAMP = [
    (48, 20, 78, 255),
    (70, 30, 112, 255),
    (96, 42, 148, 255),
    (126, 56, 186, 255),
    (158, 74, 220, 255),
    (190, 102, 244, 255),
]

ORCHID_GLOW_RAMP = [
    (128, 34, 160, 255),
    (168, 46, 204, 255),
    (204, 62, 232, 255),
    (217, 70, 239, 255),
    (242, 130, 255, 255),
    (255, 212, 255, 255),
]

IVORY_FANG_RAMP = [
    (168, 158, 184, 255),
    (194, 186, 210, 255),
    (218, 212, 232, 255),
    (238, 234, 248, 255),
    (248, 246, 255, 255),
    (255, 255, 255, 255),
]

ABYSS_VOID_RAMP = [
    (8, 6, 16, 255),
    (14, 10, 26, 255),
    (22, 16, 38, 255),
    (32, 24, 54, 255),
    (44, 34, 72, 255),
    (58, 46, 92, 255),
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
    if kind == "void_plum":
        paint_organic_shaded(tex, u, v, w, h, d, VOID_PLUM_RAMP, spot_color=(168, 86, 224, 110), seed=seed)
    elif kind == "dark_slate":
        paint_weathered_stone(tex, u, v, w, h, d, DARK_SLATE_RAMP, crack_color=(158, 74, 220, 90), seed=seed)
    elif kind == "violet_membrane":
        paint_organic_shaded(tex, u, v, w, h, d, VIOLET_MEMBRANE_RAMP, spot_color=(217, 70, 239, 130), seed=seed)
        faces = box_faces(u, v, w, h, d)
        fx, fy, fw, fh = faces["front"]
        if fw >= 4 and fh >= 3:
            for i in range(min(fw, fh)):
                px, py = fx + i, fy + (fh - 1 - i)
                put_px(tex, px, py, (217, 70, 239, 255))
                put_px(glow, px, py, (190, 60, 220, 220))
    elif kind == "orchid_glow":
        paint_faceted_crystal(tex, u, v, w, h, d, ORCHID_GLOW_RAMP, vein_color=(255, 225, 255, 240), seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, ORCHID_GLOW_RAMP, seed=seed)
    elif kind == "ivory_fang":
        paint_forged_metal(tex, u, v, w, h, d, IVORY_FANG_RAMP, seed=seed)
    elif kind == "abyss_void":
        paint_organic_shaded(tex, u, v, w, h, d, ABYSS_VOID_RAMP, seed=seed)
    else:
        raise ValueError(f"Unknown material kind: {kind}")


def build_fangwing() -> None:
    parts = [
        ("torso_core", [8, 9, 6], "void_plum"),
        ("head_crest", [9, 2, 7], "dark_slate"),
        ("brow_ridge", [9, 2, 2], "dark_slate"),
        ("cheek_l", [2, 6, 6], "void_plum"),
        ("cheek_r", [2, 6, 6], "void_plum"),
        ("mouth_cavity", [7, 4, 2], "abyss_void"),
        ("lower_taper", [5, 3, 4], "void_plum"),
        ("ear_outer_l", [3, 5, 3], "dark_slate"),
        ("ear_tip_l", [2, 3, 2], "orchid_glow"),
        ("ear_inner_l", [2, 3, 1], "orchid_glow"),
        ("ear_outer_r", [3, 5, 3], "dark_slate"),
        ("ear_tip_r", [2, 3, 2], "orchid_glow"),
        ("ear_inner_r", [2, 3, 1], "orchid_glow"),
        ("eye_bezel_l", [3, 3, 2], "dark_slate"),
        ("eye_sclera_l", [3, 3, 1], "orchid_glow"),
        ("eye_pupil_l", [1, 2, 1], "abyss_void"),
        ("eye_bezel_r", [3, 3, 2], "dark_slate"),
        ("eye_sclera_r", [3, 3, 1], "orchid_glow"),
        ("eye_pupil_r", [1, 2, 1], "abyss_void"),
        ("upper_fang_l", [1, 2, 1], "ivory_fang"),
        ("upper_fang_r", [1, 2, 1], "ivory_fang"),
        ("jaw_plate", [8, 2, 4], "dark_slate"),
        ("lower_fang_l", [1, 2, 1], "ivory_fang"),
        ("lower_fang_r", [1, 2, 1], "ivory_fang"),
        ("wing_spar_l1", [8, 2, 2], "dark_slate"),
        ("wing_mem_l1", [8, 5, 1], "violet_membrane"),
        ("wing_thumb_l", [2, 2, 2], "orchid_glow"),
        ("wing_spar_l2", [8, 2, 2], "dark_slate"),
        ("wing_mem_l2", [8, 5, 1], "violet_membrane"),
        ("wing_tip_l", [4, 3, 1], "orchid_glow"),
        ("wing_spar_r1", [8, 2, 2], "dark_slate"),
        ("wing_mem_r1", [8, 5, 1], "violet_membrane"),
        ("wing_thumb_r", [2, 2, 2], "orchid_glow"),
        ("wing_spar_r2", [8, 2, 2], "dark_slate"),
        ("wing_mem_r2", [8, 5, 1], "violet_membrane"),
        ("wing_tip_r", [4, 3, 1], "orchid_glow"),
        ("talon_l", [2, 3, 2], "dark_slate"),
        ("talon_r", [2, 3, 2], "dark_slate"),
    ]
    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=900 + i)

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.fangwing",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 3.4,
                    "visible_bounds_height": 2.4,
                    "visible_bounds_offset": [0, 1.0, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "body",
                        "parent": "root",
                        "pivot": [0, 11.5, 0],
                        "cubes": [
                            _cube([-4, 8, -3], [8, 9, 6], list(uvs["torso_core"])),
                            _cube([-4.5, 16, -3.5], [9, 2, 7], list(uvs["head_crest"])),
                            _cube([-4.5, 14.5, -4.1], [9, 2, 2], list(uvs["brow_ridge"])),
                            _cube([3.4, 9.5, -3.4], [2, 6, 6], list(uvs["cheek_l"])),
                            _cube([-5.4, 9.5, -3.4], [2, 6, 6], list(uvs["cheek_r"])),
                            _cube([-3.5, 8.8, -3.4], [7, 4, 2], list(uvs["mouth_cavity"])),
                            _cube([-2.5, 6.0, -2.0], [5, 3, 4], list(uvs["lower_taper"]), rotation=[10, 0, 0]),
                            _cube([1.2, 12.6, -4.1], [3, 3, 2], list(uvs["eye_bezel_l"])),
                            _cube([1.4, 12.8, -4.4], [3, 3, 1], list(uvs["eye_sclera_l"])),
                            _cube([2.4, 13.3, -4.7], [1, 2, 1], list(uvs["eye_pupil_l"])),
                            _cube([-4.2, 12.6, -4.1], [3, 3, 2], list(uvs["eye_bezel_r"])),
                            _cube([-4.4, 12.8, -4.4], [3, 3, 1], list(uvs["eye_sclera_r"])),
                            _cube([-3.4, 13.3, -4.7], [1, 2, 1], list(uvs["eye_pupil_r"])),
                            _cube([1.8, 11.1, -4.1], [1, 2, 1], list(uvs["upper_fang_l"]), rotation=[0, 0, 8]),
                            _cube([-2.8, 11.1, -4.1], [1, 2, 1], list(uvs["upper_fang_r"]), rotation=[0, 0, -8]),
                        ],
                    },
                    {
                        "name": "ear_left",
                        "parent": "body",
                        "pivot": [3.2, 17.5, 0],
                        "cubes": [
                            _cube([2.0, 17.2, -1.5], [3, 5, 3], list(uvs["ear_outer_l"]), rotation=[0, 0, -10]),
                            _cube([2.6, 21.8, -1.0], [2, 3, 2], list(uvs["ear_tip_l"]), rotation=[0, 0, -14]),
                            _cube([2.3, 18.0, -1.8], [2, 3, 1], list(uvs["ear_inner_l"]), rotation=[0, 0, -10]),
                        ],
                    },
                    {
                        "name": "ear_right",
                        "parent": "body",
                        "pivot": [-3.2, 17.5, 0],
                        "cubes": [
                            _cube([-5.0, 17.2, -1.5], [3, 5, 3], list(uvs["ear_outer_r"]), rotation=[0, 0, 10]),
                            _cube([-4.6, 21.8, -1.0], [2, 3, 2], list(uvs["ear_tip_r"]), rotation=[0, 0, 14]),
                            _cube([-4.3, 18.0, -1.8], [2, 3, 1], list(uvs["ear_inner_r"]), rotation=[0, 0, 10]),
                        ],
                    },
                    {
                        "name": "jaw",
                        "parent": "body",
                        "pivot": [0, 9.0, -2.5],
                        "cubes": [
                            _cube([-4.0, 7.4, -4.0], [8, 2, 4], list(uvs["jaw_plate"])),
                            _cube([0.8, 9.0, -4.1], [1, 2, 1], list(uvs["lower_fang_l"]), rotation=[0, 0, -8]),
                            _cube([-1.8, 9.0, -4.1], [1, 2, 1], list(uvs["lower_fang_r"]), rotation=[0, 0, 8]),
                        ],
                    },
                    {
                        "name": "wing_left",
                        "parent": "body",
                        "pivot": [4.5, 13.5, 0],
                        "cubes": [
                            _cube([4.5, 13.2, -1.0], [8, 2, 2], list(uvs["wing_spar_l1"]), rotation=[0, 0, -8]),
                            _cube([4.5, 8.6, -0.4], [8, 5, 1], list(uvs["wing_mem_l1"]), rotation=[0, 4, -8]),
                            _cube([11.0, 14.6, -1.2], [2, 2, 2], list(uvs["wing_thumb_l"]), rotation=[0, 0, -24]),
                        ],
                    },
                    {
                        "name": "wing_left_outer",
                        "parent": "wing_left",
                        "pivot": [12.0, 13.2, 0],
                        "cubes": [
                            _cube([12.0, 12.6, -0.8], [8, 2, 2], list(uvs["wing_spar_l2"]), rotation=[0, 8, -14]),
                            _cube([11.8, 8.0, -0.3], [8, 5, 1], list(uvs["wing_mem_l2"]), rotation=[0, 8, -14]),
                            _cube([19.0, 10.6, -0.4], [4, 3, 1], list(uvs["wing_tip_l"]), rotation=[0, 12, -22]),
                        ],
                    },
                    {
                        "name": "wing_right",
                        "parent": "body",
                        "pivot": [-4.5, 13.5, 0],
                        "cubes": [
                            _cube([-12.5, 13.2, -1.0], [8, 2, 2], list(uvs["wing_spar_r1"]), rotation=[0, 0, 8]),
                            _cube([-12.5, 8.6, -0.4], [8, 5, 1], list(uvs["wing_mem_r1"]), rotation=[0, -4, 8]),
                            _cube([-13.0, 14.6, -1.2], [2, 2, 2], list(uvs["wing_thumb_r"]), rotation=[0, 0, 24]),
                        ],
                    },
                    {
                        "name": "wing_right_outer",
                        "parent": "wing_right",
                        "pivot": [-12.0, 13.2, 0],
                        "cubes": [
                            _cube([-20.0, 12.6, -0.8], [8, 2, 2], list(uvs["wing_spar_r2"]), rotation=[0, -8, 14]),
                            _cube([-19.8, 8.0, -0.3], [8, 5, 1], list(uvs["wing_mem_r2"]), rotation=[0, -8, 14]),
                            _cube([-23.0, 10.6, -0.4], [4, 3, 1], list(uvs["wing_tip_r"]), rotation=[0, -12, 22]),
                        ],
                    },
                    {
                        "name": "foot_left",
                        "parent": "body",
                        "pivot": [2.0, 6.2, 0],
                        "cubes": [
                            _cube([1.2, 3.8, -1.2], [2, 3, 2], list(uvs["talon_l"]), rotation=[14, 0, 0]),
                        ],
                    },
                    {
                        "name": "foot_right",
                        "parent": "body",
                        "pivot": [-2.0, 6.2, 0],
                        "cubes": [
                            _cube([-3.2, 3.8, -1.2], [2, 3, 2], list(uvs["talon_r"]), rotation=[14, 0, 0]),
                        ],
                    },
                ],
            }
        ],
    }

    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))
    _save_json(GEO_DIR / "fangwing.geo.json", geo)
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    tex.save(TEX_DIR / "fangwing.png")
    glow.save(TEX_DIR / "fangwing_glowmask.png")

    anims = {
        "format_version": "1.8.0",
        "animations": {
            "animation.fangwing.idle": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "body": {"position": {"0.0": [0, 0, 0], "1.2": [0, 1.8, 0], "2.4": [0, 0, 0]}},
                    "wing_left": {"rotation": {"0.0": [0, 4, 14], "0.6": [0, -6, -18], "1.2": [0, 4, 14], "1.8": [0, -6, -18], "2.4": [0, 4, 14]}},
                    "wing_left_outer": {"rotation": {"0.0": [0, 10, 12], "0.6": [0, -10, -14], "1.2": [0, 10, 12], "1.8": [0, -10, -14], "2.4": [0, 10, 12]}},
                    "wing_right": {"rotation": {"0.0": [0, -4, -14], "0.6": [0, 6, 18], "1.2": [0, -4, -14], "1.8": [0, 6, 18], "2.4": [0, -4, -14]}},
                    "wing_right_outer": {"rotation": {"0.0": [0, -10, -12], "0.6": [0, 10, 14], "1.2": [0, -10, -12], "1.8": [0, 10, 14], "2.4": [0, -10, -12]}},
                    "ear_left": {"rotation": {"0.0": [0, 0, 0], "1.0": [6, 0, -4], "2.4": [0, 0, 0]}},
                    "ear_right": {"rotation": {"0.0": [0, 0, 0], "1.0": [6, 0, 4], "2.4": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [4, 0, 0], "1.2": [10, 0, 0], "2.4": [4, 0, 0]}},
                },
            },
            "animation.fangwing.walk": {
                "loop": True,
                "animation_length": 1.0,
                "bones": {
                    "body": {"rotation": {"0.0": [14, 0, 0], "0.5": [18, 0, 0], "1.0": [14, 0, 0]}, "position": {"0.0": [0, 0.8, 0], "0.5": [0, 2.2, 0], "1.0": [0, 0.8, 0]}},
                    "wing_left": {"rotation": {"0.0": [0, 8, 20], "0.25": [0, -12, -26], "0.5": [0, 8, 20], "0.75": [0, -12, -26], "1.0": [0, 8, 20]}},
                    "wing_left_outer": {"rotation": {"0.0": [0, 14, 16], "0.25": [0, -14, -20], "0.5": [0, 14, 16], "0.75": [0, -14, -20], "1.0": [0, 14, 16]}},
                    "wing_right": {"rotation": {"0.0": [0, -8, -20], "0.25": [0, 12, 26], "0.5": [0, -8, -20], "0.75": [0, 12, 26], "1.0": [0, -8, -20]}},
                    "wing_right_outer": {"rotation": {"0.0": [0, -14, -16], "0.25": [0, 14, 20], "0.5": [0, -14, -16], "0.75": [0, 14, 20], "1.0": [0, -14, -16]}},
                    "jaw": {"rotation": {"0.0": [8, 0, 0], "0.5": [14, 0, 0], "1.0": [8, 0, 0]}},
                },
            },
            "animation.fangwing.sit": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "body": {"position": {"0.0": [0, -3.5, 0], "1.2": [0, -3.0, 0], "2.4": [0, -3.5, 0]}},
                    "wing_left": {"rotation": {"0.0": [0, 38, 24], "1.2": [0, 42, 28], "2.4": [0, 38, 24]}},
                    "wing_left_outer": {"rotation": {"0.0": [0, 34, 10], "1.2": [0, 38, 12], "2.4": [0, 34, 10]}},
                    "wing_right": {"rotation": {"0.0": [0, -38, -24], "1.2": [0, -42, -28], "2.4": [0, -38, -24]}},
                    "wing_right_outer": {"rotation": {"0.0": [0, -34, -10], "1.2": [0, -38, -12], "2.4": [0, -34, -10]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "1.2": [4, 0, 0], "2.4": [0, 0, 0]}},
                },
            },
            "animation.fangwing.attack": {
                "animation_length": 0.55,
                "bones": {
                    "body": {"rotation": {"0.0": [0, 0, 0], "0.18": [-16, 0, 0], "0.35": [20, 0, 0], "0.55": [0, 0, 0]}},
                    "jaw": {"rotation": {"0.0": [0, 0, 0], "0.18": [28, 0, 0], "0.38": [22, 0, 0], "0.55": [0, 0, 0]}},
                    "wing_left": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, -18, -32], "0.35": [0, 22, 22], "0.55": [0, 0, 0]}},
                    "wing_right": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 18, 32], "0.35": [0, -22, -22], "0.55": [0, 0, 0]}},
                },
            },
        },
    }
    _save_json(ANIM_DIR / "fangwing.animation.json", anims)
    print("Generated Fangwing 256x128 art, glowmask, geo, and animations.")


if __name__ == "__main__":
    build_fangwing()
