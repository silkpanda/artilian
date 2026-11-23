"""Shared library for generating v3 256x256 double-layer alpha-cutout armor sets.

Builds:
- GeckoLib .geo.json (128x128 UV grid, 2x texture density = 256x256 PNG)
- Inner solid dark forged-plate shells (Row 0)
- Outer floating 1-bit alpha-cutout filigree cages (Row 1 & Row 2)
- Zero-thickness alpha-cutout silhouette planes (Row 3)
- Matching _glowmask.png for AutoGlowingGeoLayer
"""
from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path
from PIL import Image

SCALE = 2
SIZE = 128 * SCALE


@dataclass(frozen=True)
class PlaneSpec:
    """Geometry spec for a 0-thickness cutout silhouette plane."""
    w: int
    h: int
    d: int
    origin: list[float]
    pivot: list[float]
    rotation: list[float]


@dataclass(frozen=True)
class ArmorGeoSpec:
    """Per-set 3D silhouette geometry overrides for v3 armor."""
    set_name: str
    crown_front: PlaneSpec
    crown_back: PlaneSpec
    crown_side_r: PlaneSpec
    crown_side_l: PlaneSpec
    pauldron_crest_r: PlaneSpec
    pauldron_crest_l: PlaneSpec
    back_crest_r: PlaneSpec
    back_crest_l: PlaneSpec
    boot_wing_r: PlaneSpec
    boot_wing_l: PlaneSpec


def _hash(x: int, y: int) -> int:
    n = (x * 374761393 + y * 668265263) ^ 0x5BF03635
    n = (n ^ (n >> 13)) * 1274126177
    return (n ^ (n >> 16)) & 0xFF


def box_faces(u: int, v: int, w: int, h: int, d: int) -> dict[str, tuple[int, int, int, int]]:
    return {
        "up": ((u + d) * SCALE, v * SCALE, w * SCALE, d * SCALE),
        "down": ((u + d + w) * SCALE, v * SCALE, w * SCALE, d * SCALE),
        "west": (u * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE),
        "north": ((u + d) * SCALE, (v + d) * SCALE, w * SCALE, h * SCALE),
        "east": ((u + d + w) * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE),
        "south": ((u + d + w + d) * SCALE, (v + d) * SCALE, w * SCALE, h * SCALE),
    }


def blit_sprite(
    img: Image.Image,
    glow: Image.Image,
    rect: tuple[int, int, int, int],
    rows: list[str],
    pal: dict[str, tuple[int, int, int, int]],
    glow_keys: set[str],
    flip_x: bool = False,
):
    rx, ry, rw, rh = rect
    if rw <= 0 or rh <= 0:
        return
    px = img.load()
    gpx = glow.load()
    sh = len(rows)
    sw = len(rows[0])
    for dy in range(rh):
        sy = min(sh - 1, int(dy * sh / rh))
        row = rows[sy]
        for dx in range(rw):
            sx = min(sw - 1, int(dx * sw / rw))
            if flip_x:
                sx = sw - 1 - sx
            ch = row[sx]
            rgba = pal[ch]
            px[rx + dx, ry + dy] = rgba
            if ch in glow_keys:
                gpx[rx + dx, ry + dy] = rgba


def fill_forged_plate(
    img: Image.Image,
    rect: tuple[int, int, int, int],
    tones: list[tuple[int, int, int, int]],
    accent_dark: tuple[int, int, int, int],
    accent_bright: tuple[int, int, int, int],
    *,
    accent_center: bool = False,
    ribbed: bool = False,
    bias: int = 0,
):
    rx, ry, rw, rh = rect
    if rw <= 0 or rh <= 0:
        return
    px = img.load()
    for dy in range(rh):
        for dx in range(rw):
            h = _hash(rx + dx, ry + dy)
            v_frac = dy / max(1, rh - 1)
            idx = 1 if v_frac < 0.65 else 0
            if v_frac < 0.25 and h > 140:
                idx = 2
            elif h > 220:
                idx = min(3, idx + 1)
            elif h < 55:
                idx = max(0, idx - 1)
            if ribbed and dy % 4 == 0:
                idx = 0
            elif ribbed and dy % 4 == 1:
                idx = min(2, idx + 1)
            idx = max(0, min(3, idx + bias))
            rgb = tones[idx]
            if accent_center and abs(dx - (rw - 1) / 2.0) < rw * 0.26 and 2 <= dy <= rh - 3:
                rgb = accent_bright if (dy % 4 == 1 or h > 180) else accent_dark
            px[rx + dx, ry + dy] = rgb


def fill_inner_box(
    img: Image.Image,
    u: int,
    v: int,
    w: int,
    h: int,
    d: int,
    tones: list[tuple[int, int, int, int]],
    accent_dark: tuple[int, int, int, int],
    accent_bright: tuple[int, int, int, int],
    *,
    accent_center: bool = False,
    ribbed: bool = False,
):
    biases = {"up": 1, "north": 0, "east": 0, "west": 0, "south": 0, "down": -1}
    for fname, rect in box_faces(u, v, w, h, d).items():
        fill_forged_plate(
            img,
            rect,
            tones,
            accent_dark,
            accent_bright,
            accent_center=accent_center and fname in ("north", "south"),
            ribbed=ribbed and fname in ("north", "south", "east", "west"),
            bias=biases[fname],
        )


def paint_plane_z(
    img: Image.Image,
    glow: Image.Image,
    u: int,
    v: int,
    w: int,
    h: int,
    sprite: list[str],
    pal: dict[str, tuple[int, int, int, int]],
    glow_keys: set[str],
):
    north = (u * SCALE, v * SCALE, w * SCALE, h * SCALE)
    south = ((u + w) * SCALE, v * SCALE, w * SCALE, h * SCALE)
    blit_sprite(img, glow, north, sprite, pal, glow_keys, flip_x=False)
    blit_sprite(img, glow, south, sprite, pal, glow_keys, flip_x=True)


def paint_plane_x(
    img: Image.Image,
    glow: Image.Image,
    u: int,
    v: int,
    h: int,
    d: int,
    sprite: list[str],
    pal: dict[str, tuple[int, int, int, int]],
    glow_keys: set[str],
):
    west = (u * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE)
    east = ((u + d) * SCALE, (v + d) * SCALE, d * SCALE, h * SCALE)
    blit_sprite(img, glow, west, sprite, pal, glow_keys, flip_x=False)
    blit_sprite(img, glow, east, sprite, pal, glow_keys, flip_x=True)


def _plane_cube(spec: PlaneSpec, uv: list[int], mirror: bool = False) -> dict:
    cube = {
        "origin": spec.origin,
        "size": [spec.w, spec.h, spec.d],
        "pivot": spec.pivot,
        "rotation": spec.rotation,
        "uv": uv,
    }
    if mirror:
        cube["mirror"] = True
    return cube


def _layout_plane_uvs(spec: ArmorGeoSpec) -> dict[str, tuple[int, int]]:
    """Computes non-overlapping UV offsets for Row 3 silhouette planes, wrapping to Row 4 if needed."""
    items = [
        ("crown_front", 2 * spec.crown_front.w, spec.crown_front.h),
        ("crown_back", 2 * spec.crown_back.w, spec.crown_back.h),
        ("crown_side", 2 * spec.crown_side_r.d, spec.crown_side_r.h + spec.crown_side_r.d),
        ("pauldron_crest", 2 * spec.pauldron_crest_r.w, spec.pauldron_crest_r.h),
        ("back_crest", 2 * spec.back_crest_r.w, spec.back_crest_r.h),
        ("boot_wing", 2 * spec.boot_wing_r.d, spec.boot_wing_r.h + spec.boot_wing_r.d),
    ]
    uvs: dict[str, tuple[int, int]] = {}
    u, v, row_h = 0, 50, 0
    for key, w, h in items:
        if u + w > 128:
            u = 0
            v += row_h + 2
            row_h = 0
        uvs[key] = (u, v)
        u += w + 2
        row_h = max(row_h, h)
    return uvs


def build_armor_geo(spec: ArmorGeoSpec) -> dict:
    """Builds a clean GeckoLib 1.12.0 armor .geo.json with zero overlapping UVs."""
    uvs = _layout_plane_uvs(spec)
    uv_cf = list(uvs["crown_front"])
    uv_cb = list(uvs["crown_back"])
    uv_cs = list(uvs["crown_side"])
    uv_pc = list(uvs["pauldron_crest"])
    uv_bc = list(uvs["back_crest"])
    uv_bw = list(uvs["boot_wing"])

    bones = [
        {"name": "bipedHead", "pivot": [0, 24, 0]},
        {
            "name": "armorHead",
            "parent": "bipedHead",
            "pivot": [0, 24, 0],
            "cubes": [
                {"origin": [-4, 24, -4], "size": [8, 8, 8], "inflate": 0.45, "uv": [0, 0]},
                {"origin": [-4, 24, -4], "size": [8, 8, 8], "inflate": 0.88, "uv": [0, 18]},
                _plane_cube(spec.crown_front, uv_cf),
                _plane_cube(spec.crown_back, uv_cb),
                _plane_cube(spec.crown_side_r, uv_cs),
                _plane_cube(spec.crown_side_l, uv_cs, mirror=True),
            ],
        },
        {"name": "bipedBody", "pivot": [0, 24, 0]},
        {
            "name": "armorBody",
            "parent": "bipedBody",
            "pivot": [0, 24, 0],
            "cubes": [
                {"origin": [-4, 12, -2], "size": [8, 12, 4], "inflate": 0.45, "uv": [34, 0]},
                {"origin": [-4, 12, -2], "size": [8, 12, 4], "inflate": 0.90, "uv": [34, 18]},
                {"origin": [-4, 17, -2], "size": [8, 6, 4], "inflate": 1.22, "uv": [52, 36]},
                {"origin": [-4, 8, -2], "size": [8, 6, 4], "inflate": 1.08, "uv": [78, 36]},
                _plane_cube(spec.back_crest_r, uv_bc),
                _plane_cube(spec.back_crest_l, uv_bc, mirror=True),
            ],
        },
        {"name": "bipedRightArm", "pivot": [-5, 22, 0]},
        {
            "name": "armorRightArm",
            "parent": "bipedRightArm",
            "pivot": [-5, 22, 0],
            "cubes": [
                {"origin": [-8, 12, -2], "size": [4, 12, 4], "inflate": 0.38, "uv": [60, 0]},
                {"origin": [-8, 12, -2], "size": [4, 12, 4], "inflate": 0.78, "uv": [60, 18]},
                {
                    "origin": [-9.5, 19.4, -3],
                    "size": [6, 5, 6],
                    "inflate": 0.12,
                    "pivot": [-6.5, 22, 0],
                    "rotation": [0, 0, -14],
                    "uv": [0, 36],
                },
                {
                    "origin": [-9.5, 19.4, -3],
                    "size": [6, 5, 6],
                    "inflate": 0.46,
                    "pivot": [-6.5, 22, 0],
                    "rotation": [0, 0, -14],
                    "uv": [26, 36],
                },
                _plane_cube(spec.pauldron_crest_r, uv_pc),
            ],
        },
        {"name": "bipedLeftArm", "pivot": [5, 22, 0]},
        {
            "name": "armorLeftArm",
            "parent": "bipedLeftArm",
            "pivot": [5, 22, 0],
            "cubes": [
                {"origin": [4, 12, -2], "size": [4, 12, 4], "inflate": 0.38, "uv": [60, 0], "mirror": True},
                {"origin": [4, 12, -2], "size": [4, 12, 4], "inflate": 0.78, "uv": [60, 18], "mirror": True},
                {
                    "origin": [3.5, 19.4, -3],
                    "size": [6, 5, 6],
                    "inflate": 0.12,
                    "pivot": [6.5, 22, 0],
                    "rotation": [0, 0, 14],
                    "uv": [0, 36],
                    "mirror": True,
                },
                {
                    "origin": [3.5, 19.4, -3],
                    "size": [6, 5, 6],
                    "inflate": 0.46,
                    "pivot": [6.5, 22, 0],
                    "rotation": [0, 0, 14],
                    "uv": [26, 36],
                    "mirror": True,
                },
                _plane_cube(spec.pauldron_crest_l, uv_pc, mirror=True),
            ],
        },
        {"name": "bipedRightLeg", "pivot": [-2, 12, 0]},
        {
            "name": "armorRightLeg",
            "parent": "bipedRightLeg",
            "pivot": [-2, 12, 0],
            "cubes": [
                {"origin": [-4, 0, -2], "size": [4, 12, 4], "inflate": 0.30, "uv": [78, 0]},
                {"origin": [-4, 0, -2], "size": [4, 12, 4], "inflate": 0.68, "uv": [78, 18]},
            ],
        },
        {
            "name": "armorRightBoot",
            "parent": "bipedRightLeg",
            "pivot": [-2, 12, 0],
            "cubes": [
                {"origin": [-4, 0, -2], "size": [4, 12, 4], "inflate": 0.55, "uv": [96, 0]},
                {"origin": [-4, 0, -2], "size": [4, 12, 4], "inflate": 0.94, "uv": [96, 18]},
                _plane_cube(spec.boot_wing_r, uv_bw),
            ],
        },
        {"name": "bipedLeftLeg", "pivot": [2, 12, 0]},
        {
            "name": "armorLeftLeg",
            "parent": "bipedLeftLeg",
            "pivot": [2, 12, 0],
            "cubes": [
                {"origin": [0, 0, -2], "size": [4, 12, 4], "inflate": 0.30, "uv": [78, 0], "mirror": True},
                {"origin": [0, 0, -2], "size": [4, 12, 4], "inflate": 0.68, "uv": [78, 18], "mirror": True},
            ],
        },
        {
            "name": "armorLeftBoot",
            "parent": "bipedLeftLeg",
            "pivot": [2, 12, 0],
            "cubes": [
                {"origin": [0, 0, -2], "size": [4, 12, 4], "inflate": 0.55, "uv": [96, 0], "mirror": True},
                {"origin": [0, 0, -2], "size": [4, 12, 4], "inflate": 0.94, "uv": [96, 18], "mirror": True},
                _plane_cube(spec.boot_wing_l, uv_bw, mirror=True),
            ],
        },
    ]

    return {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": f"geometry.{spec.set_name}_armor",
                    "texture_width": 128,
                    "texture_height": 128,
                    "visible_bounds_width": 3,
                    "visible_bounds_height": 3.5,
                    "visible_bounds_offset": [0, 1.25, 0],
                },
                "bones": bones,
            }
        ],
    }


def write_armor_geo(spec: ArmorGeoSpec, out_path: Path):
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(json.dumps(build_armor_geo(spec), indent=2) + "\n", encoding="utf-8")


def build_armor_textures(
    spec: ArmorGeoSpec,
    pal: dict[str, tuple[int, int, int, int]],
    glow_keys: set[str],
    sprites: dict[str, list[str]],
) -> tuple[Image.Image, Image.Image]:
    """Builds 256x256 texture + glowmask using the shared v3 UV layout."""
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    tones = [pal["0"], pal["1"], pal["2"], pal["3"]]
    acc_d, acc_b = pal["r"], pal["R"]

    # Row 0: Inner solid forged plate shells
    fill_inner_box(img, 0, 0, 8, 8, 8, tones, acc_d, acc_b)
    blit_sprite(img, glow, box_faces(0, 0, 8, 8, 8)["north"], sprites["head_inner_front"], pal, glow_keys)
    fill_inner_box(img, 34, 0, 8, 12, 4, tones, acc_d, acc_b, accent_center=True, ribbed=True)
    fill_inner_box(img, 60, 0, 4, 12, 4, tones, acc_d, acc_b, ribbed=True)
    fill_inner_box(img, 78, 0, 4, 12, 4, tones, acc_d, acc_b, accent_center=True, ribbed=True)
    fill_inner_box(img, 96, 0, 4, 12, 4, tones, acc_d, acc_b, ribbed=True)

    # Row 1: Outer floating 1-bit alpha-cutout filigree cages
    hf = box_faces(0, 18, 8, 8, 8)
    blit_sprite(img, glow, hf["north"], sprites["head_filigree_front"], pal, glow_keys)
    blit_sprite(img, glow, hf["east"], sprites["head_filigree_side"], pal, glow_keys)
    blit_sprite(img, glow, hf["west"], sprites["head_filigree_side"], pal, glow_keys, flip_x=True)
    blit_sprite(img, glow, hf["south"], sprites["head_filigree_back"], pal, glow_keys)
    blit_sprite(img, glow, hf["up"], sprites["head_filigree_back"], pal, glow_keys)

    bf = box_faces(34, 18, 8, 12, 4)
    blit_sprite(img, glow, bf["north"], sprites["body_filigree_front"], pal, glow_keys)
    blit_sprite(img, glow, bf["south"], sprites["body_filigree_back"], pal, glow_keys)
    blit_sprite(img, glow, bf["east"], sprites["arm_filigree"], pal, glow_keys)
    blit_sprite(img, glow, bf["west"], sprites["arm_filigree"], pal, glow_keys, flip_x=True)

    af = box_faces(60, 18, 4, 12, 4)
    for side in ("north", "south", "east", "west"):
        blit_sprite(img, glow, af[side], sprites["arm_filigree"], pal, glow_keys)

    lf = box_faces(78, 18, 4, 12, 4)
    for side in ("north", "south", "east", "west"):
        blit_sprite(img, glow, lf[side], sprites["leg_filigree"], pal, glow_keys)

    btf = box_faces(96, 18, 4, 12, 4)
    for side in ("north", "south", "east", "west"):
        blit_sprite(img, glow, btf[side], sprites["boot_filigree"], pal, glow_keys)

    # Row 2: Layered pauldrons, raised chest crest, waist tassets
    fill_inner_box(img, 0, 36, 6, 5, 6, tones, acc_d, acc_b, accent_center=True)
    pf = box_faces(26, 36, 6, 5, 6)
    for side in ("north", "south", "east", "west", "up"):
        blit_sprite(img, glow, pf[side], sprites["pauldron_filigree"], pal, glow_keys)

    cf = box_faces(52, 36, 8, 6, 4)
    blit_sprite(img, glow, cf["north"], sprites["chest_overlay_front"], pal, glow_keys)

    wf = box_faces(78, 36, 8, 6, 4)
    blit_sprite(img, glow, wf["north"], sprites["waist_skirt_front"], pal, glow_keys)
    blit_sprite(img, glow, wf["south"], sprites["waist_skirt_front"], pal, glow_keys)
    blit_sprite(img, glow, wf["east"], sprites["waist_skirt_side"], pal, glow_keys)
    blit_sprite(img, glow, wf["west"], sprites["waist_skirt_side"], pal, glow_keys, flip_x=True)

    # Row 3+: Zero-thickness silhouette planes (UV offsets match build_armor_geo)
    uvs = _layout_plane_uvs(spec)
    paint_plane_z(img, glow, *uvs["crown_front"], spec.crown_front.w, spec.crown_front.h, sprites["crown_front"], pal, glow_keys)
    paint_plane_z(img, glow, *uvs["crown_back"], spec.crown_back.w, spec.crown_back.h, sprites["crown_back"], pal, glow_keys)
    paint_plane_x(img, glow, *uvs["crown_side"], spec.crown_side_r.h, spec.crown_side_r.d, sprites["crown_side"], pal, glow_keys)
    paint_plane_z(img, glow, *uvs["pauldron_crest"], spec.pauldron_crest_r.w, spec.pauldron_crest_r.h, sprites["pauldron_crest"], pal, glow_keys)
    paint_plane_z(img, glow, *uvs["back_crest"], spec.back_crest_r.w, spec.back_crest_r.h, sprites["back_crest"], pal, glow_keys)
    paint_plane_x(img, glow, *uvs["boot_wing"], spec.boot_wing_r.h, spec.boot_wing_r.d, sprites["boot_wing"], pal, glow_keys)

    return img, glow
