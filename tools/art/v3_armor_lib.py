"""Shared library for generating bespoke v3 256x256 armor sets with distinct 3D silhouettes.

Each armor set can define:
- Its own inner plate texture generator (forged, crystalline, volcanic cracks, dragon scales, platinum)
- Its own outer filigree/armor-plate sprite maps (never copy-pasted across sets)
- Its own extra 3D cubes (segmented horns, snouts, crystal shards, claws, dorsal spines, wings, capes)
- Its own 0-thickness alpha-cutout silhouette planes
- Matching _glowmask.png for AutoGlowingGeoLayer
"""
from __future__ import annotations

import json
from dataclasses import dataclass, field
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
class ExtraCubeSpec:
    """Extra 3D cube attached to an armor bone (e.g., 3D horns, snout, crystal shards, claws)."""
    bone: str
    origin: list[float]
    size: list[int]
    pivot: list[float] = field(default_factory=lambda: [0, 0, 0])
    rotation: list[float] = field(default_factory=lambda: [0, 0, 0])
    inflate: float = 0.0
    mirror: bool = False
    style: str = "accent"  # "plate", "accent", "glow", "trim"
    uv_group: str = ""     # cubes sharing a uv_group share the same UV tile


@dataclass(frozen=True)
class ArmorGeoSpec:
    """Per-set 3D silhouette geometry for v3 armor."""
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
    pauldron_size: list[int] = field(default_factory=lambda: [6, 5, 6])
    pauldron_origin_r: list[float] = field(default_factory=lambda: [-9.5, 19.4, -3])
    pauldron_origin_l: list[float] = field(default_factory=lambda: [3.5, 19.4, -3])
    pauldron_rot: float = 14.0
    waist_height: int = 6
    waist_y: float = 8.0
    extra_cubes: list[ExtraCubeSpec] = field(default_factory=list)


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


def fill_styled_plate(
    img: Image.Image,
    glow: Image.Image,
    rect: tuple[int, int, int, int],
    pal: dict[str, tuple[int, int, int, int]],
    glow_keys: set[str],
    *,
    pattern: str = "forged",
    accent_center: bool = False,
    bias: int = 0,
):
    """Paints textured 2x-density armor surfaces with distinct per-set material patterns."""
    rx, ry, rw, rh = rect
    if rw <= 0 or rh <= 0:
        return
    px = img.load()
    gpx = glow.load()
    tones = [pal["0"], pal["1"], pal["2"], pal["3"]]

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

            rgb = tones[max(0, min(3, idx + bias))]

            if pattern == "crystal":
                # Faceted diagonal crystal planes
                if (dx + dy) % 6 == 0 or (dx - dy) % 7 == 0:
                    rgb = tones[min(3, idx + 1 + max(0, bias))]
                elif (dx + dy) % 6 == 3:
                    rgb = tones[0]
            elif pattern == "volcanic":
                # Cracked basalt with glowing magma fissures
                if ((dx * 3 + dy * 5) % 19 == 0 and h > 110) or ((dx * 5 - dy * 2) % 23 == 0 and h > 130):
                    rgb = pal["e"] if h > 180 else pal["d"]
                    if h > 180 and "e" in glow_keys:
                        gpx[rx + dx, ry + dy] = rgb
            elif pattern == "scales":
                # Overlapping staggered dragon-scale scallops
                row_s = dy // 4
                col_s = (dx + (2 if row_s % 2 else 0)) // 4
                lx = (dx + (2 if row_s % 2 else 0)) % 4
                ly = dy % 4
                if ly == 3 or (ly == 2 and (lx == 0 or lx == 3)):
                    rgb = tones[0]
                elif ly == 0 and (lx == 1 or lx == 2):
                    rgb = tones[min(3, 2 + max(0, bias))]
                elif (row_s + col_s) % 3 == 0 and ly == 1:
                    rgb = tones[min(3, 2 + max(0, bias))]
            elif pattern == "platinum":
                # Smooth high-contrast brushed platinum with crisp bevels
                if dx == 0 or dy == 0:
                    rgb = tones[min(3, 3 + bias)]
                elif dx == rw - 1 or dy == rh - 1:
                    rgb = tones[0]
                elif dy % 5 == 0:
                    rgb = tones[min(3, idx + 1)]
            else:
                if dy % 4 == 0:
                    rgb = tones[0]
                elif dy % 4 == 1:
                    rgb = tones[min(3, idx + 1)]

            if accent_center and abs(dx - (rw - 1) / 2.0) < rw * 0.26 and 2 <= dy <= rh - 3:
                rgb = pal["R"] if (dy % 4 == 1 or h > 180) else pal["r"]

            px[rx + dx, ry + dy] = rgb


def fill_styled_box(
    img: Image.Image,
    glow: Image.Image,
    u: int,
    v: int,
    w: int,
    h: int,
    d: int,
    pal: dict[str, tuple[int, int, int, int]],
    glow_keys: set[str],
    *,
    pattern: str = "forged",
    accent_center: bool = False,
):
    biases = {"up": 1, "north": 0, "east": 0, "west": 0, "south": 0, "down": -1}
    for fname, rect in box_faces(u, v, w, h, d).items():
        fill_styled_plate(
            img,
            glow,
            rect,
            pal,
            glow_keys,
            pattern=pattern,
            accent_center=accent_center and fname in ("north", "south"),
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


def _layout_plane_and_extra_uvs(spec: ArmorGeoSpec) -> tuple[dict[str, tuple[int, int]], dict[int, tuple[int, int]]]:
    """Computes non-overlapping UV offsets for Row 3+ silhouette planes and extra 3D cubes."""
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

    extra_uvs: dict[int, tuple[int, int]] = {}
    group_uvs: dict[str, tuple[int, int]] = {}
    for idx, ec in enumerate(spec.extra_cubes):
        if ec.uv_group and ec.uv_group in group_uvs:
            extra_uvs[idx] = group_uvs[ec.uv_group]
            continue
        ew, eh, ed = ec.size
        rw = 2 * (ew + ed)
        rh = eh + ed
        if u + rw > 128:
            u = 0
            v += row_h + 2
            row_h = 0
        extra_uvs[idx] = (u, v)
        if ec.uv_group:
            group_uvs[ec.uv_group] = (u, v)
        u += rw + 2
        row_h = max(row_h, rh)

    return uvs, extra_uvs


def build_armor_geo(spec: ArmorGeoSpec) -> dict:
    """Builds a clean GeckoLib 1.12.0 armor .geo.json with zero overlapping UVs."""
    uvs, extra_uvs = _layout_plane_and_extra_uvs(spec)
    pw, ph, pd = spec.pauldron_size
    u_p1 = 0
    u_p2 = u_p1 + 2 * (pw + pd) + 2
    u_ch = u_p2 + 2 * (pw + pd) + 2
    u_wa = u_ch + 24 + 2

    bone_cubes: dict[str, list[dict]] = {
        "armorHead": [
            {"origin": [-4, 24, -4], "size": [8, 8, 8], "inflate": 0.45, "uv": [0, 0]},
            {"origin": [-4, 24, -4], "size": [8, 8, 8], "inflate": 0.88, "uv": [0, 18]},
            _plane_cube(spec.crown_front, list(uvs["crown_front"])),
            _plane_cube(spec.crown_back, list(uvs["crown_back"])),
            _plane_cube(spec.crown_side_r, list(uvs["crown_side"])),
            _plane_cube(spec.crown_side_l, list(uvs["crown_side"]), mirror=True),
        ],
        "armorBody": [
            {"origin": [-4, 12, -2], "size": [8, 12, 4], "inflate": 0.45, "uv": [34, 0]},
            {"origin": [-4, 12, -2], "size": [8, 12, 4], "inflate": 0.90, "uv": [34, 18]},
            {"origin": [-4, 17, -2], "size": [8, 6, 4], "inflate": 1.22, "uv": [u_ch, 36]},
            {"origin": [-4, spec.waist_y, -2], "size": [8, spec.waist_height, 4], "inflate": 1.08, "uv": [u_wa, 36]},
            _plane_cube(spec.back_crest_r, list(uvs["back_crest"])),
            _plane_cube(spec.back_crest_l, list(uvs["back_crest"]), mirror=True),
        ],
        "armorRightArm": [
            {"origin": [-8, 12, -2], "size": [4, 12, 4], "inflate": 0.38, "uv": [60, 0]},
            {"origin": [-8, 12, -2], "size": [4, 12, 4], "inflate": 0.78, "uv": [60, 18]},
            {
                "origin": spec.pauldron_origin_r,
                "size": [pw, ph, pd],
                "inflate": 0.12,
                "pivot": [-6.5, 22, 0],
                "rotation": [0, 0, -spec.pauldron_rot],
                "uv": [u_p1, 36],
            },
            {
                "origin": spec.pauldron_origin_r,
                "size": [pw, ph, pd],
                "inflate": 0.46,
                "pivot": [-6.5, 22, 0],
                "rotation": [0, 0, -spec.pauldron_rot],
                "uv": [u_p2, 36],
            },
            _plane_cube(spec.pauldron_crest_r, list(uvs["pauldron_crest"])),
        ],
        "armorLeftArm": [
            {"origin": [4, 12, -2], "size": [4, 12, 4], "inflate": 0.38, "uv": [60, 0], "mirror": True},
            {"origin": [4, 12, -2], "size": [4, 12, 4], "inflate": 0.78, "uv": [60, 18], "mirror": True},
            {
                "origin": spec.pauldron_origin_l,
                "size": [pw, ph, pd],
                "inflate": 0.12,
                "pivot": [6.5, 22, 0],
                "rotation": [0, 0, spec.pauldron_rot],
                "uv": [u_p1, 36],
                "mirror": True,
            },
            {
                "origin": spec.pauldron_origin_l,
                "size": [pw, ph, pd],
                "inflate": 0.46,
                "pivot": [6.5, 22, 0],
                "rotation": [0, 0, spec.pauldron_rot],
                "uv": [u_p2, 36],
                "mirror": True,
            },
            _plane_cube(spec.pauldron_crest_l, list(uvs["pauldron_crest"]), mirror=True),
        ],
        "armorRightLeg": [
            {"origin": [-4, 0, -2], "size": [4, 12, 4], "inflate": 0.30, "uv": [78, 0]},
            {"origin": [-4, 0, -2], "size": [4, 12, 4], "inflate": 0.68, "uv": [78, 18]},
        ],
        "armorRightBoot": [
            {"origin": [-4, 0, -2], "size": [4, 12, 4], "inflate": 0.55, "uv": [96, 0]},
            {"origin": [-4, 0, -2], "size": [4, 12, 4], "inflate": 0.94, "uv": [96, 18]},
            _plane_cube(spec.boot_wing_r, list(uvs["boot_wing"])),
        ],
        "armorLeftLeg": [
            {"origin": [0, 0, -2], "size": [4, 12, 4], "inflate": 0.30, "uv": [78, 0], "mirror": True},
            {"origin": [0, 0, -2], "size": [4, 12, 4], "inflate": 0.68, "uv": [78, 18], "mirror": True},
        ],
        "armorLeftBoot": [
            {"origin": [0, 0, -2], "size": [4, 12, 4], "inflate": 0.55, "uv": [96, 0], "mirror": True},
            {"origin": [0, 0, -2], "size": [4, 12, 4], "inflate": 0.94, "uv": [96, 18], "mirror": True},
            _plane_cube(spec.boot_wing_l, list(uvs["boot_wing"]), mirror=True),
        ],
    }

    for idx, ec in enumerate(spec.extra_cubes):
        cube = {
            "origin": ec.origin,
            "size": ec.size,
            "pivot": ec.pivot,
            "rotation": ec.rotation,
            "uv": list(extra_uvs[idx]),
        }
        if ec.inflate:
            cube["inflate"] = ec.inflate
        if ec.mirror:
            cube["mirror"] = True
        bone_cubes[ec.bone].append(cube)

    bones = [
        {"name": "bipedHead", "pivot": [0, 24, 0]},
        {"name": "armorHead", "parent": "bipedHead", "pivot": [0, 24, 0], "cubes": bone_cubes["armorHead"]},
        {"name": "bipedBody", "pivot": [0, 24, 0]},
        {"name": "armorBody", "parent": "bipedBody", "pivot": [0, 24, 0], "cubes": bone_cubes["armorBody"]},
        {"name": "bipedRightArm", "pivot": [-5, 22, 0]},
        {"name": "armorRightArm", "parent": "bipedRightArm", "pivot": [-5, 22, 0], "cubes": bone_cubes["armorRightArm"]},
        {"name": "bipedLeftArm", "pivot": [5, 22, 0]},
        {"name": "armorLeftArm", "parent": "bipedLeftArm", "pivot": [5, 22, 0], "cubes": bone_cubes["armorLeftArm"]},
        {"name": "bipedRightLeg", "pivot": [-2, 12, 0]},
        {"name": "armorRightLeg", "parent": "bipedRightLeg", "pivot": [-2, 12, 0], "cubes": bone_cubes["armorRightLeg"]},
        {"name": "armorRightBoot", "parent": "bipedRightLeg", "pivot": [-2, 12, 0], "cubes": bone_cubes["armorRightBoot"]},
        {"name": "bipedLeftLeg", "pivot": [2, 12, 0]},
        {"name": "armorLeftLeg", "parent": "bipedLeftLeg", "pivot": [2, 12, 0], "cubes": bone_cubes["armorLeftLeg"]},
        {"name": "armorLeftBoot", "parent": "bipedLeftLeg", "pivot": [2, 12, 0], "cubes": bone_cubes["armorLeftBoot"]},
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
    *,
    pattern: str = "forged",
) -> tuple[Image.Image, Image.Image]:
    """Builds 256x256 texture + glowmask with bespoke per-set material patterns and 3D extras."""
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    glow = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # Row 0: Inner solid forged/styled plate shells
    fill_styled_box(img, glow, 0, 0, 8, 8, 8, pal, glow_keys, pattern=pattern)
    blit_sprite(img, glow, box_faces(0, 0, 8, 8, 8)["north"], sprites["head_inner_front"], pal, glow_keys)
    fill_styled_box(img, glow, 34, 0, 8, 12, 4, pal, glow_keys, pattern=pattern, accent_center=True)
    fill_styled_box(img, glow, 60, 0, 4, 12, 4, pal, glow_keys, pattern=pattern)
    fill_styled_box(img, glow, 78, 0, 4, 12, 4, pal, glow_keys, pattern=pattern, accent_center=True)
    fill_styled_box(img, glow, 96, 0, 4, 12, 4, pal, glow_keys, pattern=pattern)

    # Row 1: Outer floating 1-bit alpha-cutout filigree / armor-plate cages
    hf = box_faces(0, 18, 8, 8, 8)
    blit_sprite(img, glow, hf["north"], sprites["head_filigree_front"], pal, glow_keys)
    blit_sprite(img, glow, hf["east"], sprites["head_filigree_side"], pal, glow_keys)
    blit_sprite(img, glow, hf["west"], sprites["head_filigree_side"], pal, glow_keys, flip_x=True)
    blit_sprite(img, glow, hf["south"], sprites["head_filigree_back"], pal, glow_keys)
    blit_sprite(img, glow, hf["up"], sprites["head_filigree_top"], pal, glow_keys)

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
    pw, ph, pd = spec.pauldron_size
    u_p1 = 0
    u_p2 = u_p1 + 2 * (pw + pd) + 2
    u_ch = u_p2 + 2 * (pw + pd) + 2
    u_wa = u_ch + 24 + 2

    fill_styled_box(img, glow, u_p1, 36, pw, ph, pd, pal, glow_keys, pattern=pattern, accent_center=True)
    pf = box_faces(u_p2, 36, pw, ph, pd)
    for side in ("north", "south", "east", "west", "up"):
        blit_sprite(img, glow, pf[side], sprites["pauldron_filigree"], pal, glow_keys)

    cf = box_faces(u_ch, 36, 8, 6, 4)
    blit_sprite(img, glow, cf["north"], sprites["chest_overlay_front"], pal, glow_keys)

    wf = box_faces(u_wa, 36, 8, spec.waist_height, 4)
    blit_sprite(img, glow, wf["north"], sprites["waist_skirt_front"], pal, glow_keys)
    blit_sprite(img, glow, wf["south"], sprites["waist_skirt_front"], pal, glow_keys)
    blit_sprite(img, glow, wf["east"], sprites["waist_skirt_side"], pal, glow_keys)
    blit_sprite(img, glow, wf["west"], sprites["waist_skirt_side"], pal, glow_keys, flip_x=True)

    # Row 3+: Zero-thickness silhouette planes & extra 3D cubes
    uvs, extra_uvs = _layout_plane_and_extra_uvs(spec)
    paint_plane_z(img, glow, *uvs["crown_front"], spec.crown_front.w, spec.crown_front.h, sprites["crown_front"], pal, glow_keys)
    paint_plane_z(img, glow, *uvs["crown_back"], spec.crown_back.w, spec.crown_back.h, sprites["crown_back"], pal, glow_keys)
    paint_plane_x(img, glow, *uvs["crown_side"], spec.crown_side_r.h, spec.crown_side_r.d, sprites["crown_side"], pal, glow_keys)
    paint_plane_z(img, glow, *uvs["pauldron_crest"], spec.pauldron_crest_r.w, spec.pauldron_crest_r.h, sprites["pauldron_crest"], pal, glow_keys)
    paint_plane_z(img, glow, *uvs["back_crest"], spec.back_crest_r.w, spec.back_crest_r.h, sprites["back_crest"], pal, glow_keys)
    paint_plane_x(img, glow, *uvs["boot_wing"], spec.boot_wing_r.h, spec.boot_wing_r.d, sprites["boot_wing"], pal, glow_keys)

    for idx, ec in enumerate(spec.extra_cubes):
        u, v = extra_uvs[idx]
        ew, eh, ed = ec.size
        if ec.style == "glow":
            # Glowing crystal/ember cube with bright edges
            for fname, rect in box_faces(u, v, ew, eh, ed).items():
                rx, ry, rw, rh = rect
                px = img.load()
                gpx = glow.load()
                for dy in range(rh):
                    for dx in range(rw):
                        is_edge = dx == 0 or dy == 0 or dx == rw - 1 or dy == rh - 1
                        ch = "e" if is_edge else "E"
                        rgba = pal[ch]
                        px[rx + dx, ry + dy] = rgba
                        if ch in glow_keys:
                            gpx[rx + dx, ry + dy] = rgba
        elif ec.style == "trim":
            trim_pal = {
                "0": pal["k"],
                "1": pal["d"],
                "2": pal["g"],
                "3": pal["G"],
                "r": pal["r"],
                "R": pal["R"],
                "d": pal["d"],
                "e": pal["e"],
            }
            fill_styled_box(img, glow, u, v, ew, eh, ed, trim_pal, set(), pattern="forged")
        else:
            fill_styled_box(img, glow, u, v, ew, eh, ed, pal, glow_keys, pattern=pattern, accent_center=(ec.style == "accent"))

    return img, glow
