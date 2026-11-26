"""Generates GeckoLib model, animations, 256x128 texture, and glowmask for Viner.

Viner is a kid-drawn Plant / Water creature (NATURE / WATER):
- 3 splayed angled abyssal-teal root feet anchoring a tapered 2-segment stem
- 4 alternating 3D angled oval seafoam-emerald leaves along the stem and a layered calyx collar
- Two long segmented sea-teal vine arms extending horizontally with hooked vine-whip tips & glowing aqua dewdrops
- Sculpted deep-ocean-navy flower housing with a recessed 3D cobalt/azure water-core face, glowing sky-aqua eyes,
  and a cheerful sprout smile, ringed by 12 angled 3D golden-yellow petals with deep-teal calyx bases
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
SHEET_H = 128

ABYSS_TEAL_RAMP = [
    (14, 48, 56, 255),
    (20, 64, 74, 255),
    (26, 80, 92, 255),
    (32, 96, 108, 255),
    (38, 110, 120, 255),
    (46, 124, 132, 255),
]

SEAFOAM_LEAF_RAMP = [
    (22, 92, 82, 255),
    (34, 116, 102, 255),
    (48, 142, 124, 255),
    (64, 168, 146, 255),
    (80, 192, 168, 255),
    (98, 212, 186, 255),
]

OCEAN_NAVY_RAMP = [
    (12, 34, 72, 255),
    (18, 48, 96, 255),
    (24, 64, 120, 255),
    (32, 82, 146, 255),
    (42, 102, 172, 255),
    (56, 124, 196, 255),
]

WATER_CORE_RAMP = [
    (16, 54, 118, 255),
    (24, 78, 152, 255),
    (36, 104, 184, 255),
    (48, 132, 214, 255),
    (64, 158, 238, 255),
    (84, 182, 255, 255),
]

GOLD_PETAL_RAMP = [
    (168, 128, 24, 255),
    (194, 152, 36, 255),
    (218, 178, 52, 255),
    (238, 204, 74, 255),
    (250, 226, 106, 255),
    (255, 244, 146, 255),
]

AQUA_GLOW_RAMP = [
    (44, 168, 198, 255),
    (68, 196, 224, 255),
    (96, 220, 242, 255),
    (132, 238, 252, 255),
    (176, 248, 255, 255),
    (224, 254, 255, 255),
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
    if kind == "abyss_teal":
        paint_weathered_stone(tex, u, v, w, h, d, ABYSS_TEAL_RAMP, crack_color=(64, 168, 146, 85), seed=seed)
    elif kind == "seafoam_leaf":
        paint_organic_shaded(tex, u, v, w, h, d, SEAFOAM_LEAF_RAMP, spot_color=(120, 232, 204, 110), seed=seed)
        faces = box_faces(u, v, w, h, d)
        fx, fy, fw, fh = faces["front"]
        if fw >= 3 and fh >= 2:
            for i in range(min(fw, fh)):
                put_px(tex, fx + i, fy + fh // 2, (124, 236, 206, 255))
    elif kind == "ocean_navy":
        paint_weathered_stone(tex, u, v, w, h, d, OCEAN_NAVY_RAMP, crack_color=(48, 132, 214, 90), seed=seed)
    elif kind == "water_core":
        paint_faceted_crystal(tex, u, v, w, h, d, WATER_CORE_RAMP, vein_color=(132, 238, 252, 200), seed=seed)
        faces = box_faces(u, v, w, h, d)
        fx, fy, fw, fh = faces["front"]
        # Subtle wave-ripple veins + cheerful sprout smile on the front face
        for ry in (1, 3, 5):
            if ry < fh:
                for rx in range(fw):
                    if (rx + ry) % 3 == 0:
                        put_px(tex, fx + rx, fy + ry, (24, 78, 152, 255))
                        put_px(glow, fx + rx, fy + ry, (72, 186, 236, 150))
        if fw >= 6 and fh >= 6:
            smile_y = fh - 2
            for sx in range(2, fw - 2):
                sy = smile_y if (sx == 2 or sx == fw - 3) else smile_y + 1
                if 0 <= sy < fh:
                    put_px(tex, fx + sx, fy + sy, (16, 54, 118, 255))
                    put_px(glow, fx + sx, fy + sy, (132, 238, 252, 220))
    elif kind == "gold_petal":
        paint_organic_shaded(tex, u, v, w, h, d, GOLD_PETAL_RAMP, spot_color=(34, 116, 102, 140), seed=seed)
        faces = box_faces(u, v, w, h, d)
        # Deep-teal calyx edge at the base of the petal
        for face_name in ("front", "back", "top", "bottom"):
            fx, fy, fw, fh = faces[face_name]
            for rx in range(fw):
                put_px(tex, fx + rx, fy + fh - 1, (26, 92, 86, 255))
    elif kind == "aqua_glow":
        paint_faceted_crystal(tex, u, v, w, h, d, AQUA_GLOW_RAMP, vein_color=(224, 254, 255, 240), seed=seed)
        paint_faceted_crystal(glow, u, v, w, h, d, AQUA_GLOW_RAMP, seed=seed)
    else:
        raise ValueError(f"Unknown material kind: {kind}")


def build_viner() -> None:
    parts: list[tuple[str, list[float], str]] = [
        ("root_base", [4, 2, 4], "abyss_teal"),
        ("root_l", [2, 2, 4], "abyss_teal"),
        ("root_r", [2, 2, 4], "abyss_teal"),
        ("root_back", [2, 2, 4], "abyss_teal"),
        ("stem_lower", [3, 5, 3], "abyss_teal"),
        ("stem_upper", [3, 5, 3], "seafoam_leaf"),
        ("calyx_core", [5, 2, 5], "abyss_teal"),
        ("leaf_l1", [4, 2, 2], "seafoam_leaf"),
        ("leaf_r1", [4, 2, 2], "seafoam_leaf"),
        ("leaf_l2", [4, 2, 2], "seafoam_leaf"),
        ("leaf_r2", [4, 2, 2], "seafoam_leaf"),
        ("vine_upper_l", [6, 2, 2], "seafoam_leaf"),
        ("vine_fore_l", [6, 2, 2], "abyss_teal"),
        ("vine_hook_l", [3, 3, 2], "seafoam_leaf"),
        ("vine_dew_l", [2, 2, 2], "aqua_glow"),
        ("vine_upper_r", [6, 2, 2], "seafoam_leaf"),
        ("vine_fore_r", [6, 2, 2], "abyss_teal"),
        ("vine_hook_r", [3, 3, 2], "seafoam_leaf"),
        ("vine_dew_r", [2, 2, 2], "aqua_glow"),
        ("head_housing", [9, 8, 5], "ocean_navy"),
        ("head_back_taper", [7, 6, 3], "abyss_teal"),
        ("water_core_face", [7, 6, 2], "water_core"),
        ("eye_bezel_l", [2, 2, 1], "ocean_navy"),
        ("eye_glow_l", [2, 2, 1], "aqua_glow"),
        ("eye_bezel_r", [2, 2, 1], "ocean_navy"),
        ("eye_glow_r", [2, 2, 1], "aqua_glow"),
    ]
    for i in range(12):
        parts.append((f"petal_{i}", [3, 4, 2], "gold_petal"))

    uvs = _pack_uvs(parts)
    tex = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    for i, (name, size, kind) in enumerate(parts):
        u, v = uvs[name]
        _paint_cube(tex, glow, u, v, size, kind, seed=1200 + i)

    # 12 angled 3D golden-yellow petals ringing the flower head perimeter
    petal_cubes: list[dict] = []
    cx, cy, cz = 0.0, 16.5, 0.2
    rx, ry = 5.0, 4.6
    for i in range(12):
        angle_deg = i * 30.0
        rad = math.radians(angle_deg)
        px = cx + math.sin(rad) * rx
        py = cy + math.cos(rad) * ry
        origin = [round(px - 1.5, 2), round(py - 2.0, 2), round(cz - 1.0, 2)]
        pivot = [round(px, 2), round(py, 2), round(cz, 2)]
        petal_cubes.append(
            _cube(
                origin,
                [3, 4, 2],
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
                    "visible_bounds_width": 2.2,
                    "visible_bounds_height": 2.0,
                    "visible_bounds_offset": [0, 0.9, 0],
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
                        "name": "root_l",
                        "parent": "root_base",
                        "pivot": [1.5, 1.2, -1.0],
                        "cubes": [
                            _cube([1.2, 0.0, -4.5], [2, 2, 4], list(uvs["root_l"]), rotation=[0, -28, 0], pivot=[2.2, 1.0, -1.5]),
                        ],
                    },
                    {
                        "name": "root_r",
                        "parent": "root_base",
                        "pivot": [-1.5, 1.2, -1.0],
                        "cubes": [
                            _cube([-3.2, 0.0, -4.5], [2, 2, 4], list(uvs["root_r"]), rotation=[0, 28, 0], pivot=[-2.2, 1.0, -1.5]),
                        ],
                    },
                    {
                        "name": "root_back",
                        "parent": "root_base",
                        "pivot": [0, 1.2, 1.5],
                        "cubes": [
                            _cube([-1.0, 0.0, 1.5], [2, 2, 4], list(uvs["root_back"]), rotation=[-8, 0, 0], pivot=[0, 1.0, 2.0]),
                        ],
                    },
                    {
                        "name": "stem_lower",
                        "parent": "root_base",
                        "pivot": [0, 2.0, 0],
                        "cubes": [
                            _cube([-1.5, 2.0, -1.5], [3, 5, 3], list(uvs["stem_lower"])),
                        ],
                    },
                    {
                        "name": "leaf_l1",
                        "parent": "stem_lower",
                        "pivot": [1.5, 4.2, 0],
                        "cubes": [
                            _cube([1.5, 3.4, -1.0], [4, 2, 2], list(uvs["leaf_l1"]), rotation=[0, 12, -18], pivot=[1.5, 4.2, 0]),
                        ],
                    },
                    {
                        "name": "leaf_r1",
                        "parent": "stem_lower",
                        "pivot": [-1.5, 5.4, 0],
                        "cubes": [
                            _cube([-5.5, 4.6, -1.0], [4, 2, 2], list(uvs["leaf_r1"]), rotation=[0, -12, 18], pivot=[-1.5, 5.4, 0]),
                        ],
                    },
                    {
                        "name": "stem_upper",
                        "parent": "stem_lower",
                        "pivot": [0, 7.0, 0],
                        "cubes": [
                            _cube([-1.5, 6.8, -1.5], [3, 5, 3], list(uvs["stem_upper"])),
                        ],
                    },
                    {
                        "name": "leaf_l2",
                        "parent": "stem_upper",
                        "pivot": [1.5, 8.6, 0],
                        "cubes": [
                            _cube([1.5, 7.8, -1.0], [4, 2, 2], list(uvs["leaf_l2"]), rotation=[0, -10, -24], pivot=[1.5, 8.6, 0]),
                        ],
                    },
                    {
                        "name": "leaf_r2",
                        "parent": "stem_upper",
                        "pivot": [-1.5, 9.6, 0],
                        "cubes": [
                            _cube([-5.5, 8.8, -1.0], [4, 2, 2], list(uvs["leaf_r2"]), rotation=[0, 10, 24], pivot=[-1.5, 9.6, 0]),
                        ],
                    },
                    {
                        "name": "calyx",
                        "parent": "stem_upper",
                        "pivot": [0, 11.8, 0],
                        "cubes": [
                            _cube([-2.5, 11.2, -2.5], [5, 2, 5], list(uvs["calyx_core"])),
                        ],
                    },
                    {
                        "name": "vine_arm_l",
                        "parent": "stem_upper",
                        "pivot": [1.5, 10.2, 0],
                        "cubes": [
                            _cube([1.5, 9.2, -1.0], [6, 2, 2], list(uvs["vine_upper_l"]), rotation=[0, 0, -10], pivot=[1.5, 10.2, 0]),
                        ],
                    },
                    {
                        "name": "vine_forearm_l",
                        "parent": "vine_arm_l",
                        "pivot": [7.2, 10.8, 0],
                        "cubes": [
                            _cube([7.2, 9.8, -1.0], [6, 2, 2], list(uvs["vine_fore_l"]), rotation=[0, -14, 14], pivot=[7.2, 10.8, 0]),
                            _cube([12.2, 8.6, -1.0], [3, 3, 2], list(uvs["vine_hook_l"]), rotation=[0, -14, -26], pivot=[12.8, 10.0, 0]),
                            _cube([14.0, 7.6, -1.0], [2, 2, 2], list(uvs["vine_dew_l"])),
                        ],
                    },
                    {
                        "name": "vine_arm_r",
                        "parent": "stem_upper",
                        "pivot": [-1.5, 10.2, 0],
                        "cubes": [
                            _cube([-7.5, 9.2, -1.0], [6, 2, 2], list(uvs["vine_upper_r"]), rotation=[0, 0, 10], pivot=[-1.5, 10.2, 0]),
                        ],
                    },
                    {
                        "name": "vine_forearm_r",
                        "parent": "vine_arm_r",
                        "pivot": [-7.2, 10.8, 0],
                        "cubes": [
                            _cube([-13.2, 9.8, -1.0], [6, 2, 2], list(uvs["vine_fore_r"]), rotation=[0, 14, -14], pivot=[-7.2, 10.8, 0]),
                            _cube([-15.2, 8.6, -1.0], [3, 3, 2], list(uvs["vine_hook_r"]), rotation=[0, 14, 26], pivot=[-12.8, 10.0, 0]),
                            _cube([-16.0, 7.6, -1.0], [2, 2, 2], list(uvs["vine_dew_r"])),
                        ],
                    },
                    {
                        "name": "head",
                        "parent": "calyx",
                        "pivot": [0, 12.6, 0],
                        "cubes": [
                            _cube([-4.5, 12.5, -2.5], [9, 8, 5], list(uvs["head_housing"])),
                            _cube([-3.5, 13.5, 2.2], [7, 6, 3], list(uvs["head_back_taper"])),
                            _cube([-3.5, 13.5, -3.2], [7, 6, 2], list(uvs["water_core_face"])),
                            _cube([1.1, 16.4, -3.6], [2, 2, 1], list(uvs["eye_bezel_l"])),
                            _cube([1.1, 16.4, -4.0], [2, 2, 1], list(uvs["eye_glow_l"])),
                            _cube([-3.1, 16.4, -3.6], [2, 2, 1], list(uvs["eye_bezel_r"])),
                            _cube([-3.1, 16.4, -4.0], [2, 2, 1], list(uvs["eye_glow_r"])),
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
                    "vine_arm_l": {"rotation": {"0.0": [0, 0, 8], "1.2": [6, -6, -8], "2.4": [0, 0, 8]}},
                    "vine_forearm_l": {"rotation": {"0.0": [0, -8, 10], "1.2": [0, 8, -10], "2.4": [0, -8, 10]}},
                    "vine_arm_r": {"rotation": {"0.0": [0, 0, -8], "1.2": [6, 6, 8], "2.4": [0, 0, -8]}},
                    "vine_forearm_r": {"rotation": {"0.0": [0, 8, -10], "1.2": [0, -8, 10], "2.4": [0, 8, -10]}},
                },
            },
            "animation.viner.walk": {
                "loop": True,
                "animation_length": 0.9,
                "bones": {
                    "root_l": {"rotation": {"0.0": [18, 0, 0], "0.45": [-16, 0, 0], "0.9": [18, 0, 0]}},
                    "root_r": {"rotation": {"0.0": [-16, 0, 0], "0.45": [18, 0, 0], "0.9": [-16, 0, 0]}},
                    "root_back": {"rotation": {"0.0": [-12, 0, 0], "0.45": [14, 0, 0], "0.9": [-12, 0, 0]}},
                    "stem_lower": {"rotation": {"0.0": [6, 0, -4], "0.45": [6, 0, 4], "0.9": [6, 0, -4]}},
                    "head": {"rotation": {"0.0": [-6, 0, 4], "0.45": [-2, 0, -4], "0.9": [-6, 0, 4]}},
                    "vine_arm_l": {"rotation": {"0.0": [14, -12, 10], "0.45": [-14, 12, -6], "0.9": [14, -12, 10]}},
                    "vine_arm_r": {"rotation": {"0.0": [-14, 12, -10], "0.45": [14, -12, 6], "0.9": [-14, 12, -10]}},
                },
            },
            "animation.viner.sit": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "root_base": {"position": {"0.0": [0, -1.5, 0], "1.2": [0, -1.2, 0], "2.4": [0, -1.5, 0]}},
                    "stem_lower": {"rotation": {"0.0": [14, 0, 0], "1.2": [18, 0, 0], "2.4": [14, 0, 0]}},
                    "head": {"rotation": {"0.0": [-10, 0, 0], "1.2": [-6, 0, 0], "2.4": [-10, 0, 0]}},
                    "vine_arm_l": {"rotation": {"0.0": [10, 24, 26], "1.2": [10, 28, 30], "2.4": [10, 24, 26]}},
                    "vine_arm_r": {"rotation": {"0.0": [10, -24, -26], "1.2": [10, -28, -30], "2.4": [10, -24, -26]}},
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
    print("Generated Viner 256x128 art, glowmask, geo, and animations.")


if __name__ == "__main__":
    build_viner()
