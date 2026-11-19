"""Shared v2 pixel-art texture painters and Bedrock box-UV validators.

Implements TD-01 from TECH_DEBT.md so every entity, boss, and armor generator
can use 6-8 stop shaded ramps, material textures (stone strata/cracks, forged
metal seams/rivets, faceted crystal highlights, organic shading), and UV
overlap assertions without duplicating helpers.
"""
from __future__ import annotations

import hashlib
from typing import Iterable, Sequence
from PIL import Image

Color = tuple[int, int, int, int]
Rect = tuple[int, int, int, int]


def box_faces(u: int, v: int, w: int, h: int, d: int) -> dict[str, Rect]:
    """Return Bedrock box-UV face rectangles for a w x h x d cube at (u, v)."""
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }


def box_rect(u: int, v: int, w: int, h: int, d: int) -> Rect:
    """Return the bounding UV rectangle (x, y, width, height) of a box-UV cube."""
    return (u, v, 2 * (w + d), h + d)


def rects_overlap(a: Rect, b: Rect) -> bool:
    """Return True if two (x, y, w, h) rectangles overlap with positive area."""
    ax, ay, aw, ah = a
    bx, by, bw, bh = b
    return ax < bx + bw and bx < ax + aw and ay < by + bh and by < ay + ah


def assert_uv_layout_clean(
    geo: dict,
    sheet_size: tuple[int, int],
    *,
    allow_mirror_overlap: bool = True,
) -> None:
    """Assert every cube's box-UV rect fits inside sheet_size with no overlaps."""
    width, height = sheet_size
    taken: list[tuple[str, Rect, bool]] = []
    for geom in geo.get("minecraft:geometry", []):
        for bone in geom.get("bones", []):
            bname = bone.get("name", "bone")
            for idx, cube in enumerate(bone.get("cubes", [])):
                size = cube["size"]
                uv = cube.get("uv")
                if not isinstance(uv, (list, tuple)) or len(uv) < 2:
                    continue
                u, v = int(uv[0]), int(uv[1])
                w, h, d = int(round(size[0])), int(round(size[1])), int(round(size[2]))
                rect = box_rect(u, v, w, h, d)
                label = f"{bname}[{idx}]"
                if rect[0] < 0 or rect[1] < 0 or rect[0] + rect[2] > width or rect[1] + rect[3] > height:
                    raise ValueError(f"{label}: UV rect {rect} exceeds {width}x{height} sheet")
                is_mirror = bool(cube.get("mirror", False))
                for other_label, other_rect, other_mirror in taken:
                    if rects_overlap(rect, other_rect):
                        if allow_mirror_overlap and (is_mirror or other_mirror):
                            continue
                        raise ValueError(f"{label}: UV rect {rect} overlaps {other_label} {other_rect}")
                taken.append((label, rect, is_mirror))


def _hash01(x: int, y: int, seed: int) -> float:
    h = hashlib.blake2b(f"{seed}:{x}:{y}".encode("ascii"), digest_size=4).digest()
    return int.from_bytes(h, "big") / 0xFFFFFFFF


def _lerp_color(c1: Color, c2: Color, t: float) -> Color:
    t = max(0.0, min(1.0, t))
    return (
        int(round(c1[0] + (c2[0] - c1[0]) * t)),
        int(round(c1[1] + (c2[1] - c1[1]) * t)),
        int(round(c1[2] + (c2[2] - c1[2]) * t)),
        int(round(c1[3] + (c2[3] - c1[3]) * t)),
    )


def _sample_ramp(ramp: Sequence[Color], t: float) -> Color:
    if len(ramp) == 1:
        return ramp[0]
    t = max(0.0, min(1.0, t))
    pos = t * (len(ramp) - 1)
    idx = min(int(pos), len(ramp) - 2)
    return _lerp_color(ramp[idx], ramp[idx + 1], pos - idx)


def put_px(img: Image.Image, x: int, y: int, color: Color) -> None:
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def fill_rect(img: Image.Image, rect: Rect, color: Color) -> None:
    rx, ry, rw, rh = rect
    for py in range(ry, ry + rh):
        for px in range(rx, rx + rw):
            put_px(img, px, py, color)


def _paint_face_ramp(
    img: Image.Image,
    rect: Rect,
    ramp: Sequence[Color],
    *,
    bias: float = 0.5,
    seed: int = 0,
    noise_amp: float = 0.08,
) -> None:
    """Paint a directional top-to-bottom shaded ramp with subtle deterministic grain."""
    rx, ry, rw, rh = rect
    if rw <= 0 or rh <= 0:
        return
    for ly in range(rh):
        ty = 0.0 if rh == 1 else ly / (rh - 1)
        for lx in range(rw):
            tx = 0.0 if rw == 1 else lx / (rw - 1)
            # Top-left lit, bottom-right shaded + subtle micro-variation
            n = (_hash01(rx + lx, ry + ly, seed) - 0.5) * 2.0 * noise_amp
            # Invert so 0.0 is darkest, 1.0 is brightest in ramp, or vice versa:
            # Convention: ramp[0] = darkest, ramp[-1] = brightest.
            light = max(0.0, min(1.0, bias + (0.5 - ty) * 0.28 + (0.5 - tx) * 0.08 + n))
            put_px(img, rx + lx, ry + ly, _sample_ramp(ramp, light))


def _bevel_face(img: Image.Image, rect: Rect, hi: Color, lo: Color) -> None:
    rx, ry, rw, rh = rect
    if rw < 3 or rh < 3:
        return
    for lx in range(rw):
        put_px(img, rx + lx, ry, hi)
        put_px(img, rx + lx, ry + rh - 1, lo)
    for ly in range(1, rh - 1):
        put_px(img, rx, ry + ly, hi)
        put_px(img, rx + rw - 1, ry + ly, lo)


def paint_weathered_stone(
    img: Image.Image,
    u: int,
    v: int,
    w: int,
    h: int,
    d: int,
    ramp: Sequence[Color],
    *,
    crack_color: Color | None = None,
    moss_color: Color | None = None,
    seed: int = 0,
) -> None:
    """Paint a cube with 6-8 stop weathered stone strata, cracks, and optional moss."""
    f = box_faces(u, v, w, h, d)
    biases = {"top": 0.82, "front": 0.62, "east": 0.48, "west": 0.44, "back": 0.36, "bottom": 0.18}
    crack = crack_color or ramp[0]
    hi = ramp[-1]
    lo = ramp[0]

    for name, rect in f.items():
        _paint_face_ramp(img, rect, ramp, bias=biases[name], seed=seed + hash(name) % 97, noise_amp=0.11)
        rx, ry, rw, rh = rect
        # Horizontal strata bands every ~4 px
        for ly in range(2, rh, 4):
            for lx in range(rw):
                if _hash01(rx + lx, ry + ly, seed + 13) > 0.35:
                    t = max(0.0, biases[name] - 0.18)
                    put_px(img, rx + lx, ry + ly, _sample_ramp(ramp, t))
        # Subtle diagonal/stepped cracks on larger faces
        if rw >= 5 and rh >= 5:
            cx = rx + 1 + int(_hash01(rx, ry, seed + 31) * max(1, rw - 3))
            cy = ry + 1 + int(_hash01(rx, ry, seed + 37) * max(1, rh - 3))
            for step in range(min(4, rw - 2, rh - 2)):
                px = cx + (step % 2)
                py = cy + step
                if rx < px < rx + rw - 1 and ry < py < ry + rh - 1:
                    put_px(img, px, py, crack)
        # Moss patches along top & upper edges
        if moss_color is not None and name in ("top", "front", "east", "west"):
            for ly in range(min(rh, 3 if name != "top" else rh)):
                for lx in range(rw):
                    thresh = 0.72 if name == "top" else 0.78
                    if _hash01(rx + lx, ry + ly, seed + 71) > thresh:
                        put_px(img, rx + lx, ry + ly, moss_color)
        _bevel_face(img, rect, _lerp_color(hi, ramp[len(ramp) // 2], 0.45), lo)


def paint_forged_metal(
    img: Image.Image,
    u: int,
    v: int,
    w: int,
    h: int,
    d: int,
    ramp: Sequence[Color],
    *,
    rivet_color: Color | None = None,
    trim_color: Color | None = None,
    seed: int = 0,
) -> None:
    """Paint a cube with forged metal directional sheen, plate seams, and rivets."""
    f = box_faces(u, v, w, h, d)
    biases = {"top": 0.85, "front": 0.65, "east": 0.50, "west": 0.45, "back": 0.38, "bottom": 0.16}
    rivet = rivet_color or ramp[-1]
    seam = ramp[0]
    for name, rect in f.items():
        _paint_face_ramp(img, rect, ramp, bias=biases[name], seed=seed + 19, noise_amp=0.05)
        rx, ry, rw, rh = rect
        # Specular diagonal glint
        if rw >= 4 and rh >= 4:
            for i in range(min(rw - 2, rh - 2)):
                put_px(img, rx + 1 + i, ry + 1 + i, _sample_ramp(ramp, min(1.0, biases[name] + 0.25)))
        # Plate seam across middle on tall/wide faces
        if rh >= 6:
            mid_y = ry + rh // 2
            for lx in range(1, rw - 1):
                put_px(img, rx + lx, mid_y, seam)
        # Corner rivets
        if rw >= 5 and rh >= 5:
            for px, py in ((rx + 1, ry + 1), (rx + rw - 2, ry + 1), (rx + 1, ry + rh - 2), (rx + rw - 2, ry + rh - 2)):
                put_px(img, px, py, rivet)
        if trim_color is not None and name in ("front", "top") and rw >= 4:
            for lx in range(rw):
                put_px(img, rx + lx, ry, trim_color)


def paint_faceted_crystal(
    img: Image.Image,
    u: int,
    v: int,
    w: int,
    h: int,
    d: int,
    ramp: Sequence[Color],
    *,
    vein_color: Color | None = None,
    seed: int = 0,
) -> None:
    """Paint a cube with sharp faceted crystal highlights and inner veins."""
    f = box_faces(u, v, w, h, d)
    biases = {"top": 0.90, "front": 0.68, "east": 0.52, "west": 0.46, "back": 0.36, "bottom": 0.20}
    for name, rect in f.items():
        _paint_face_ramp(img, rect, ramp, bias=biases[name], seed=seed + 41, noise_amp=0.04)
        rx, ry, rw, rh = rect
        # Crisp bright facet ridge
        for ly in range(rh):
            for lx in range(rw):
                if (lx + ly) % 4 == 0 and lx < rw - 1:
                    put_px(img, rx + lx, ry + ly, _sample_ramp(ramp, min(1.0, biases[name] + 0.28)))
        if vein_color is not None and rw >= 4 and rh >= 4:
            for i in range(min(rw, rh) - 1):
                put_px(img, rx + i, ry + rh - 1 - i, vein_color)


def paint_organic_shaded(
    img: Image.Image,
    u: int,
    v: int,
    w: int,
    h: int,
    d: int,
    ramp: Sequence[Color],
    *,
    belly_color: Color | None = None,
    spot_color: Color | None = None,
    seed: int = 0,
) -> None:
    """Paint a cube with soft organic shading, optional belly/spots."""
    f = box_faces(u, v, w, h, d)
    biases = {"top": 0.80, "front": 0.64, "east": 0.50, "west": 0.48, "back": 0.40, "bottom": 0.22}
    for name, rect in f.items():
        _paint_face_ramp(img, rect, ramp, bias=biases[name], seed=seed + 53, noise_amp=0.06)
        rx, ry, rw, rh = rect
        if belly_color is not None and name == "front" and rw >= 4 and rh >= 4:
            for ly in range(rh // 2, rh - 1):
                for lx in range(1, rw - 1):
                    put_px(img, rx + lx, ry + ly, belly_color)
        if spot_color is not None and rw >= 4 and rh >= 4:
            for ly in range(1, rh - 1):
                for lx in range(1, rw - 1):
                    if _hash01(rx + lx, ry + ly, seed + 89) > 0.86:
                        put_px(img, rx + lx, ry + ly, spot_color)
