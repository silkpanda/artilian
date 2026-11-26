"""Generates GeckoLib model, animations, 256x256 texture, and glowmask for Viner (Art Pass 2).

Viner is a kid-drawn Plant / Water creature (NATURE / WATER):
- 4 splayed deep forest-green root tendrils gripping the earth
- Tapered 2-segment botanical-green stem with 4 alternating 3D angled leaves + 2 plump collar leaves
- Sculpted forest-green calyx cup with 4 outward-angled sepal plates cradling the blossom
- Arching M-shaped lush-green vine arms with leafy wrist fronds and luminous aqua water-droplet buds
- Tapered deep-ocean-navy flower housing with a forest-green sunflower back, recessed cobalt/azure water-core
  front face, glowing sky-aqua eyes with dark 3D pupils, sprout smile, and 12 unclipped golden-yellow petals
"""
from __future__ import annotations

import json
import math
from pathlib import Path

from PIL import Image

from art_lib import (
    assert_uv_layout_clean,
    box_faces,
    box_rect,
    paint_faceted_crystal,
    paint_organic_shaded,
    paint_weathered_stone,
    put_px,
)

ASSETS = Path("src/main/resources/assets/artilian")
GEO_DIR = ASSETS / "geo" / "entity"
ANIM_DIR = ASSETS / "animations" / "entity"
TEX_DIR = ASSETS / "textures" / "entity"

SHEET_W = 256
SHEET_H = 256

FOREST_BARK_RAMP = [
    (14, 42, 18, 255),
    (22, 60, 24, 255),
    (30, 80, 32, 255),
    (40, 102, 40, 255),
    (52, 126, 48, 255),
    (68, 152, 60, 255),
]

LUSH_LEAF_RAMP = [
    (24, 86, 26, 255),
    (38, 118, 36, 255),
    (54, 150, 48, 255),
    (74, 182, 62, 255),
    (98, 210, 78, 255),
    (136, 236, 102, 255),
]

OCEAN_NAVY_RAMP = [
    (10, 30, 68, 255),
    (16, 44, 92, 255),
    (22, 60, 116, 255),
    (30, 76, 140, 255),
    (38, 94, 164, 255),
    (48, 112, 186, 255),
]

WATER_CORE_RAMP = [
    (18, 62, 136, 255),
    (28, 88, 168, 255),
    (40, 116, 198, 255),
    (54, 146, 224, 255),
    (72, 174, 244, 255),
    (92, 202, 255, 255),
]

GOLD_PETAL_RAMP = [
    (172, 130, 20, 255),
    (196, 154, 34, 255),
    (220, 180, 50, 255),
    (238, 206, 72, 255),
    (250, 228, 104, 255),
    (255, 246, 148, 255),
]

AQUA_GLOW_RAMP = [
    (48, 176, 212, 255),
    (74, 202, 232, 255),
    (104, 224, 246, 255),
    (140, 240, 252, 255),
    (182, 250, 255, 255),
    (228, 254, 255, 255),
]

ABYSS_PUPIL_RAMP = [
    (8, 18, 38, 255),
    (12, 26, 50, 255),
    (16, 34, 62, 255),
    (20, 42, 74, 255),
    (24, 48, 86, 255),
    (28, 56, 98, 255),
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
    """Deterministic shelf-packer for non-overlapping box UVs on a 256x256 sheet."""
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
    if kind == "forest_bark":
        paint_weathered_stone(tex, u, v, w, h, d, FOREST_BARK_RAMP, crack_color=(98, 210, 78, 85), seed=seed)
    elif kind == "lush_leaf":
        paint_organic_shaded(tex, u, v, w, h, d, LUSH_LEAF_RAMP, spot_color=(136, 236, 102, 120), seed=seed)
        faces = box_faces(u, v, w, h, d)
        for fname in ("front", "top", "back"):
            fx, fy, fw, fh = faces[fname]
            if fw >= 3 and fh >= 2:
                for i in range(fw):
                    put_px(tex, fx + i, fy + fh // 2, (152, 242, 114, 255))
    elif kind == "ocean_navy":
        paint_weathered_stone(tex, u, v, w, h, d, OCEAN_NAVY_RAMP, crack_color=(54, 146, 224, 90), seed=seed)
    elif kind == "water_core":
        paint_faceted_crystal(tex, u, v, w, h, d, WATER_CORE_RAMP, vein_color=(140, 240, 252, 200), seed=seed)
        faces = box_faces(u, v, w, h, d)
        fx, fy, fw, fh = faces["front"]
        # Central dark-cobalt wave swirl + subtle hydro-vein glow + sprout smile
        for ry in range(fh):
            for rx in range(fw):
                if (rx + ry) % 3 == 0 and 1 <= ry < fh - 1:
                    put_px(tex, fx + rx, fy + ry, (22, 74, 152, 255))
                    put_px(glow, fx + rx, fy + ry, (74, 192, 242, 140))
        if fw >= 6 and fh >= 5:
            smile_y = fh - 2
            for sx in range(2, fw - 2):
                sy = smile_y if (sx == 2 or sx == fw - 3) else smile_y + 1
                if 0 <= sy < fh:
                    put_px(tex, fx + sx, fy + sy, (10, 34, 82, 255))
                    put_px(glow, fx + sx, fy + sy, (140, 240, 252, 220))
    elif kind == "gold_petal":
        paint_organic_shaded(tex, u, v, w, h, d, GOLD_PETAL_RAMP, spot_color=(54, 150, 48, 120), seed=seed)
        faces = box_faces(u, v, w, h, d)
        # Lush forest-green calyx base edge at the bottom of each petal
        for face_name in ("front", "back", "top", "bottom"):
            fx, fy, fw, fh = faces[face_name]
            for rx in range(fw):
                put_px(tex, fx + rx, fy + fh - 1, (30, 96, 36, 255))
    elif kind == "aqua_glow":
        paint_faceted_crystal(tex, u, v, w, h, d, AQUA_GLOW_RAMP, vein_color=(228, 254, 255, 240), seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, AQUA_GLOW_RAMP, seed=seed)
    elif kind == "abyss_pupil":
        paint_organic_shaded(tex, u, v, w, h, d, ABYSS_PUPIL_RAMP, seed=seed)
    else:
        raise ValueError(f"Unknown material kind: {kind}")


def build_viner() -> None:
    parts: list[tuple[str, list[float], str]] = [
        ("root_base", [4, 2, 4], "forest_bark"),
        ("root_fl", [2, 2, 4], "forest_bark"),
        ("root_fr", [2, 2, 4], "forest_bark"),
        ("root_bl", [2, 2, 4], "forest_bark"),
        ("root_br", [2, 2, 4], "forest_bark"),
        ("stem_lower", [3, 5, 3], "forest_bark"),
        ("stem_upper", [3, 5, 3], "lush_leaf"),
        ("leaf_l1", [4, 2, 2], "lush_leaf"),
        ("leaf_r1", [4, 2, 2], "lush_leaf"),
        ("leaf_l2", [4, 2, 2], "lush_leaf"),
        ("leaf_r2", [4, 2, 2], "lush_leaf"),
        ("collar_leaf_l", [4, 2, 3], "lush_leaf"),
        ("collar_leaf_r", [4, 2, 3], "lush_leaf"),
        ("calyx_core", [5, 2, 5], "forest_bark"),
        ("sepal_l", [3, 2, 3], "forest_bark"),
        ("sepal_r", [3, 2, 3], "forest_bark"),
        ("sepal_f", [4, 2, 2], "forest_bark"),
        ("sepal_b", [4, 2, 2], "forest_bark"),
        ("vine_upper_l", [6, 2, 2], "lush_leaf"),
        ("vine_frond_l", [3, 2, 2], "lush_leaf"),
        ("vine_fore_l", [6, 2, 2], "forest_bark"),
        ("vine_hook_l", [3, 3, 2], "lush_leaf"),
        ("vine_dew_l", [2, 2, 2], "aqua_glow"),
        ("vine_upper_r", [6, 2, 2], "lush_leaf"),
        ("vine_frond_r", [3, 2, 2], "lush_leaf"),
        ("vine_fore_r", [6, 2, 2], "forest_bark"),
        ("vine_hook_r", [3, 3, 2], "lush_leaf"),
        ("vine_dew_r", [2, 2, 2], "aqua_glow"),
        ("head_lower_basin", [9, 5, 5], "ocean_navy"),
        ("head_upper_crown", [7, 4, 5], "ocean_navy"),
        ("head_crest_cap", [5, 2, 4], "ocean_navy"),
        ("head_back_calyx", [7, 6, 3], "forest_bark"),
        ("water_core_face", [7, 6, 2], "water_core"),
        ("water_core_crest", [5, 2, 2], "water_core"),
        ("eye_bezel_l", [2, 2, 1], "ocean_navy"),
        ("eye_glow_l", [2, 2, 1], "aqua_glow"),
        ("eye_pupil_l", [1, 1, 1], "abyss_pupil"),
        ("eye_bezel_r", [2, 2, 1], "ocean_navy"),
        ("eye_glow_r", [2, 2, 1], "aqua_glow"),
        ("eye_pupil_r", [1, 1, 1], "abyss_pupil"),
    ]
    for i in range(12):
        parts.append((f"petal_{i}", [3, 5, 2], "gold_petal"))

    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=1400 + i)

    # 12 unclipped outward-flaring golden petals around the tapered flower head
    petal_cubes: list[dict] = []
    cx, cy, cz = 0.0, 17.5, 0.2
    rx, ry = 6.0, 5.6
    for i in range(12):
        angle_deg = i * 30.0
        rad = math.radians(angle_deg)
        px = cx + math.sin(rad) * rx
        py = cy + math.cos(rad) * ry
        origin = [round(px - 1.5, 2), round(py - 2.5, 2), round(cz - 1.0, 2)]
        pivot = [round(px, 2), round(py, 2), round(cz, 2)]
        petal_cubes.append(
            _cube(
                origin,
                [3, 5, 2],
                list(uvs[f"petal_{i}"]),
                rotation=[0, 0, round(-angle_deg, 1)],
                pivot=pivot,
            )
        )

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.viner",
                    "texture_width": SHEET_W,
                    "texture_height": SHEET_H,
                    "visible_bounds_width": 2.4,
                    "visible_bounds_height": 2.2,
                    "visible_bounds_offset": [0, 1.0, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "root_base",
                        "parent": "root",
                        "pivot": [0, 1.0, 0],
                        "cubes": [
                            _cube([-2.0, 0.0, -2.0], [4, 2, 4], list(uvs["root_base"])),
                        ],
                    },
                    {
                        "name": "root_fl",
                        "parent": "root_base",
                        "pivot": [1.5, 1.2, -1.2],
                        "cubes": [
                            _cube([1.2, 0.0, -4.6], [2, 2, 4], list(uvs["root_fl"]), rotation=[0, -32, 0], pivot=[2.2, 1.0, -1.6]),
                        ],
                    },
                    {
                        "name": "root_fr",
                        "parent": "root_base",
                        "pivot": [-1.5, 1.2, -1.2],
                        "cubes": [
                            _cube([-3.2, 0.0, -4.6], [2, 2, 4], list(uvs["root_fr"]), rotation=[0, 32, 0], pivot=[-2.2, 1.0, -1.6]),
                        ],
                    },
                    {
                        "name": "root_bl",
                        "parent": "root_base",
                        "pivot": [1.5, 1.2, 1.2],
                        "cubes": [
                            _cube([1.2, 0.0, 1.0], [2, 2, 4], list(uvs["root_bl"]), rotation=[0, 32, 0], pivot=[2.2, 1.0, 1.6]),
                        ],
                    },
                    {
                        "name": "root_br",
                        "parent": "root_base",
                        "pivot": [-1.5, 1.2, 1.2],
                        "cubes": [
                            _cube([-3.2, 0.0, 1.0], [2, 2, 4], list(uvs["root_br"]), rotation=[0, -32, 0], pivot=[-2.2, 1.0, 1.6]),
                        ],
                    },
                    {
                        "name": "stem_lower",
                        "parent": "root_base",
                        "pivot": [0, 2.0, 0],
                        "cubes": [
                            _cube([-1.5, 2.0, -1.5], [3, 5, 3], list(uvs["stem_lower"])),
                            _cube([1.5, 3.2, -1.0], [4, 2, 2], list(uvs["leaf_l1"]), rotation=[0, 12, -18], pivot=[1.5, 4.0, 0]),
                            _cube([-5.5, 4.4, -1.0], [4, 2, 2], list(uvs["leaf_r1"]), rotation=[0, -12, 18], pivot=[-1.5, 5.2, 0]),
                        ],
                    },
                    {
                        "name": "stem_upper",
                        "parent": "stem_lower",
                        "pivot": [0, 7.0, 0],
                        "cubes": [
                            _cube([-1.5, 6.8, -1.5], [3, 5, 3], list(uvs["stem_upper"])),
                            _cube([1.5, 7.6, -1.0], [4, 2, 2], list(uvs["leaf_l2"]), rotation=[0, -10, -24], pivot=[1.5, 8.4, 0]),
                            _cube([-5.5, 8.6, -1.0], [4, 2, 2], list(uvs["leaf_r2"]), rotation=[0, 10, 24], pivot=[-1.5, 9.4, 0]),
                            _cube([1.2, 10.6, -1.5], [4, 2, 3], list(uvs["collar_leaf_l"]), rotation=[0, 8, -14], pivot=[1.2, 11.4, 0]),
                            _cube([-5.2, 10.6, -1.5], [4, 2, 3], list(uvs["collar_leaf_r"]), rotation=[0, -8, 14], pivot=[-1.2, 11.4, 0]),
                        ],
                    },
                    {
                        "name": "calyx",
                        "parent": "stem_upper",
                        "pivot": [0, 11.8, 0],
                        "cubes": [
                            _cube([-2.5, 11.4, -2.5], [5, 2, 5], list(uvs["calyx_core"])),
                            _cube([2.2, 12.0, -1.5], [3, 2, 3], list(uvs["sepal_l"]), rotation=[0, 0, -22], pivot=[2.4, 12.6, 0]),
                            _cube([-5.2, 12.0, -1.5], [3, 2, 3], list(uvs["sepal_r"]), rotation=[0, 0, 22], pivot=[-2.4, 12.6, 0]),
                            _cube([-2.0, 11.8, -3.2], [4, 2, 2], list(uvs["sepal_f"]), rotation=[22, 0, 0], pivot=[0, 12.6, -2.4]),
                            _cube([-2.0, 11.8, 1.2], [4, 2, 2], list(uvs["sepal_b"]), rotation=[-22, 0, 0], pivot=[0, 12.6, 2.4]),
                        ],
                    },
                    {
                        "name": "vine_arm_l",
                        "parent": "stem_upper",
                        "pivot": [1.5, 10.6, 0],
                        "cubes": [
                            _cube([1.5, 9.6, -1.0], [6, 2, 2], list(uvs["vine_upper_l"]), rotation=[0, 0, -24], pivot=[1.5, 10.6, 0]),
                            _cube([5.8, 12.2, -1.0], [3, 2, 2], list(uvs["vine_frond_l"]), rotation=[0, -12, 22], pivot=[6.8, 13.0, 0]),
                        ],
                    },
                    {
                        "name": "vine_forearm_l",
                        "parent": "vine_arm_l",
                        "pivot": [7.0, 13.0, 0],
                        "cubes": [
                            _cube([7.0, 12.0, -1.0], [6, 2, 2], list(uvs["vine_fore_l"]), rotation=[0, -10, 48], pivot=[7.0, 13.0, 0]),
                            _cube([10.5, 7.2, -1.0], [3, 3, 2], list(uvs["vine_hook_l"]), rotation=[0, -12, 20], pivot=[11.0, 8.6, 0]),
                            _cube([11.6, 5.8, -1.0], [2, 2, 2], list(uvs["vine_dew_l"]), rotation=[0, -12, 20], pivot=[11.0, 8.6, 0]),
                        ],
                    },
                    {
                        "name": "vine_arm_r",
                        "parent": "stem_upper",
                        "pivot": [-1.5, 10.6, 0],
                        "cubes": [
                            _cube([-7.5, 9.6, -1.0], [6, 2, 2], list(uvs["vine_upper_r"]), rotation=[0, 0, 24], pivot=[-1.5, 10.6, 0]),
                            _cube([-8.8, 12.2, -1.0], [3, 2, 2], list(uvs["vine_frond_r"]), rotation=[0, 12, -22], pivot=[-6.8, 13.0, 0]),
                        ],
                    },
                    {
                        "name": "vine_forearm_r",
                        "parent": "vine_arm_r",
                        "pivot": [-7.0, 13.0, 0],
                        "cubes": [
                            _cube([-13.0, 12.0, -1.0], [6, 2, 2], list(uvs["vine_fore_r"]), rotation=[0, 10, -48], pivot=[-7.0, 13.0, 0]),
                            _cube([-13.5, 7.2, -1.0], [3, 3, 2], list(uvs["vine_hook_r"]), rotation=[0, 12, -20], pivot=[-11.0, 8.6, 0]),
                            _cube([-13.6, 5.8, -1.0], [2, 2, 2], list(uvs["vine_dew_r"]), rotation=[0, 12, -20], pivot=[-11.0, 8.6, 0]),
                        ],
                    },
                    {
                        "name": "head",
                        "parent": "calyx",
                        "pivot": [0, 13.0, 0],
                        "cubes": [
                            _cube([-4.5, 13.0, -2.5], [9, 5, 5], list(uvs["head_lower_basin"])),
                            _cube([-3.5, 18.0, -2.5], [7, 4, 5], list(uvs["head_upper_crown"])),
                            _cube([-2.5, 21.8, -2.0], [5, 2, 4], list(uvs["head_crest_cap"])),
                            _cube([-3.5, 14.2, 2.2], [7, 6, 3], list(uvs["head_back_calyx"])),
                            _cube([-3.5, 14.0, -3.2], [7, 6, 2], list(uvs["water_core_face"])),
                            _cube([-2.5, 20.0, -3.1], [5, 2, 2], list(uvs["water_core_crest"])),
                            _cube([1.1, 17.2, -3.6], [2, 2, 1], list(uvs["eye_bezel_l"])),
                            _cube([1.1, 17.2, -4.0], [2, 2, 1], list(uvs["eye_glow_l"])),
                            _cube([1.6, 17.7, -4.4], [1, 1, 1], list(uvs["eye_pupil_l"])),
                            _cube([-3.1, 17.2, -3.6], [2, 2, 1], list(uvs["eye_bezel_r"])),
                            _cube([-3.1, 17.2, -4.0], [2, 2, 1], list(uvs["eye_glow_r"])),
                            _cube([-2.6, 17.7, -4.4], [1, 1, 1], list(uvs["eye_pupil_r"])),
                            *petal_cubes,
                        ],
                    },
                ],
            }
        ],
    }

    assert_uv_layout_clean(geo, (SHEET_W, SHEET_H))
    _save_json(GEO_DIR / "viner.geo.json", geo)
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    tex.save(TEX_DIR / "viner.png")
    glow.save(TEX_DIR / "viner_glowmask.png")

    anims = {
        "format_version": "1.8.0",
        "animations": {
            "animation.viner.idle": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "stem_lower": {"rotation": {"0.0": [0, 0, -2], "1.2": [0, 0, 2], "2.4": [0, 0, -2]}},
                    "head": {"rotation": {"0.0": [0, 0, 3], "1.2": [4, 0, -3], "2.4": [0, 0, 3]}},
                    "vine_arm_l": {"rotation": {"0.0": [0, 0, 6], "1.2": [6, -6, -6], "2.4": [0, 0, 6]}},
                    "vine_forearm_l": {"rotation": {"0.0": [0, -6, 8], "1.2": [0, 6, -8], "2.4": [0, -6, 8]}},
                    "vine_arm_r": {"rotation": {"0.0": [0, 0, -6], "1.2": [6, 6, 6], "2.4": [0, 0, -6]}},
                    "vine_forearm_r": {"rotation": {"0.0": [0, 6, -8], "1.2": [0, -6, 8], "2.4": [0, 6, -8]}},
                },
            },
            "animation.viner.walk": {
                "loop": True,
                "animation_length": 0.9,
                "bones": {
                    "root_fl": {"rotation": {"0.0": [18, 0, 0], "0.45": [-16, 0, 0], "0.9": [18, 0, 0]}},
                    "root_fr": {"rotation": {"0.0": [-16, 0, 0], "0.45": [18, 0, 0], "0.9": [-16, 0, 0]}},
                    "root_bl": {"rotation": {"0.0": [-14, 0, 0], "0.45": [16, 0, 0], "0.9": [-14, 0, 0]}},
                    "root_br": {"rotation": {"0.0": [16, 0, 0], "0.45": [-14, 0, 0], "0.9": [16, 0, 0]}},
                    "stem_lower": {"rotation": {"0.0": [6, 0, -4], "0.45": [6, 0, 4], "0.9": [6, 0, -4]}},
                    "head": {"rotation": {"0.0": [-6, 0, 4], "0.45": [-2, 0, -4], "0.9": [-6, 0, 4]}},
                    "vine_arm_l": {"rotation": {"0.0": [14, -12, 8], "0.45": [-14, 12, -6], "0.9": [14, -12, 8]}},
                    "vine_arm_r": {"rotation": {"0.0": [-14, 12, -8], "0.45": [14, -12, 6], "0.9": [-14, 12, -8]}},
                },
            },
            "animation.viner.sit": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "root_base": {"position": {"0.0": [0, -1.5, 0], "1.2": [0, -1.2, 0], "2.4": [0, -1.5, 0]}},
                    "stem_lower": {"rotation": {"0.0": [14, 0, 0], "1.2": [18, 0, 0], "2.4": [14, 0, 0]}},
                    "head": {"rotation": {"0.0": [-10, 0, 0], "1.2": [-6, 0, 0], "2.4": [-10, 0, 0]}},
                    "vine_arm_l": {"rotation": {"0.0": [10, 24, 22], "1.2": [10, 28, 26], "2.4": [10, 24, 22]}},
                    "vine_arm_r": {"rotation": {"0.0": [10, -24, -22], "1.2": [10, -28, -26], "2.4": [10, -24, -22]}},
                },
            },
            "animation.viner.attack": {
                "animation_length": 0.55,
                "bones": {
                    "stem_lower": {"rotation": {"0.0": [0, 0, 0], "0.18": [-14, 0, 0], "0.34": [22, 0, 0], "0.55": [0, 0, 0]}},
                    "head": {"rotation": {"0.0": [0, 0, 0], "0.18": [-10, 0, 0], "0.34": [12, 0, 0], "0.55": [0, 0, 0]}},
                    "vine_arm_l": {"rotation": {"0.0": [0, 0, 0], "0.16": [0, 22, 18], "0.32": [0, -48, -10], "0.55": [0, 0, 0]}},
                    "vine_forearm_l": {"rotation": {"0.0": [0, 0, 0], "0.16": [0, 20, 12], "0.32": [0, -34, -14], "0.55": [0, 0, 0]}},
                    "vine_arm_r": {"rotation": {"0.0": [0, 0, 0], "0.16": [0, -22, -18], "0.32": [0, 48, 10], "0.55": [0, 0, 0]}},
                    "vine_forearm_r": {"rotation": {"0.0": [0, 0, 0], "0.16": [0, -20, -12], "0.32": [0, 34, 14], "0.55": [0, 0, 0]}},
                },
            },
        },
    }
    _save_json(ANIM_DIR / "viner.animation.json", anims)
    print("Generated Viner Pass 2 256x256 art, glowmask, geo, and animations.")


if __name__ == "__main__":
    build_viner()
