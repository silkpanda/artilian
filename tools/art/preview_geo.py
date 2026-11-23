"""Headless Pillow z-buffered per-texel 3D renderer for GeckoLib .geo.json + .png models.

Implements TD-01 from TECH_DEBT.md so agents can inspect silhouettes, proportions,
pixel-art textures, alpha cutouts, and glowmasks from Front, 3/4, Side, and Back angles
before booting runClient.

Usage:
    python tools/art/preview_geo.py entity/crystal_colossus
    python tools/art/preview_geo.py entity/training_golem --out qa/evidence/previews/training_golem.png
    python tools/art/preview_geo.py --phase3
"""
from __future__ import annotations

import argparse
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

from art_lib import box_faces

ASSETS = Path("src/main/resources/assets/artilian")


def _rot_x(p, deg):
    r = math.radians(deg)
    c, s = math.cos(r), math.sin(r)
    return (p[0], p[1] * c - p[2] * s, p[1] * s + p[2] * c)


def _rot_y(p, deg):
    r = math.radians(deg)
    c, s = math.cos(r), math.sin(r)
    return (p[0] * c + p[2] * s, p[1], -p[0] * s + p[2] * c)


def _rot_z(p, deg):
    r = math.radians(deg)
    c, s = math.cos(r), math.sin(r)
    return (p[0] * c - p[1] * s, p[0] * s + p[1] * c, p[2])


def _apply_bone_transform(pt, pivot, rot):
    x, y, z = pt[0] - pivot[0], pt[1] - pivot[1], pt[2] - pivot[2]
    # Bedrock/GeckoLib rotations negate X, Y, Z relative to right-handed +X/+Y/+Z axes.
    x, y, z = _rot_x((x, y, z), -rot[0])
    x, y, z = _rot_y((x, y, z), -rot[1])
    x, y, z = _rot_z((x, y, z), -rot[2])
    return (x + pivot[0], y + pivot[1], z + pivot[2])


def _build_bone_map(geo: dict):
    bones = {}
    for geom in geo.get("minecraft:geometry", []):
        for b in geom.get("bones", []):
            bones[b["name"]] = b
    return bones


def _transform_point_chain(pt, bone_name: str, bones: dict):
    chain = []
    cur = bone_name
    while cur and cur in bones:
        chain.append(bones[cur])
        cur = bones[cur].get("parent")
    for b in chain:
        rot = b.get("rotation", [0, 0, 0])
        if any(rot):
            pt = _apply_bone_transform(pt, b.get("pivot", [0, 0, 0]), rot)
    return pt


def _normalize_per_face_entry(entry: dict) -> tuple[tuple[int, int, int, int], bool, bool] | None:
    uv = entry.get("uv")
    uv_size = entry.get("uv_size")
    if not isinstance(uv, (list, tuple)) or not isinstance(uv_size, (list, tuple)):
        return None
    u, v = float(uv[0]), float(uv[1])
    su, sv = float(uv_size[0]), float(uv_size[1])
    x = u + su if su < 0 else u
    y = v + sv if sv < 0 else v
    rect = (int(round(x)), int(round(y)), int(round(abs(su))), int(round(abs(sv))))
    return rect, (su < 0), (sv < 0)


def _cube_quads(geo: dict, tex_size: tuple[int, int]):
    desc = geo.get("minecraft:geometry", [{}])[0].get("description", {})
    uv_w = float(desc.get("texture_width", tex_size[0]) or tex_size[0])
    uv_h = float(desc.get("texture_height", tex_size[1]) or tex_size[1])
    scale_x = tex_size[0] / uv_w
    scale_y = tex_size[1] / uv_h

    bones = _build_bone_map(geo)
    quads = []
    for bname, bone in bones.items():
        for cube in bone.get("cubes", []):
            ox, oy, oz = cube["origin"]
            sx, sy, sz = cube["size"]
            if sum(1 for s in (sx, sy, sz) if s <= 0) >= 2:
                continue

            inf = float(cube.get("inflate", 0.0))
            x0, y0, z0 = ox - inf, oy - inf, oz - inf
            x1, y1, z1 = ox + sx + inf, oy + sy + inf, oz + sz + inf

            w, h, d = int(round(sx)), int(round(sy)), int(round(sz))
            mirror = bool(cube.get("mirror", False))
            uv = cube.get("uv", [0, 0])
            face_uvs: dict[str, tuple[tuple[int, int, int, int], bool, bool]] = {}

            if isinstance(uv, dict):
                uv_map = {
                    "front": "north",
                    "back": "south",
                    "east": "east",
                    "west": "west",
                    "top": "up",
                    "bottom": "down",
                }
                for fname, bed_name in uv_map.items():
                    if bed_name in uv and isinstance(uv[bed_name], dict):
                        parsed = _normalize_per_face_entry(uv[bed_name])
                        if parsed is not None:
                            face_uvs[fname] = parsed
            else:
                u, v = (int(uv[0]), int(uv[1])) if isinstance(uv, (list, tuple)) else (0, 0)
                frects = box_faces(u, v, w, h, d)
                for fname, rect in frects.items():
                    target_rect = rect
                    if mirror:
                        if fname == "east":
                            target_rect = frects["west"]
                        elif fname == "west":
                            target_rect = frects["east"]
                    face_uvs[fname] = (target_rect, mirror, False)

            crot = cube.get("rotation")
            cpiv = cube.get("pivot", [0, 0, 0])

            # 6 faces in Bedrock coords (north = -Z front, south = +Z back), ordered TL, TR, BR, BL in UV
            faces_3d = [
                ("front", [(x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0)], 1.00),
                ("back", [(x1, y1, z1), (x0, y1, z1), (x0, y0, z1), (x1, y0, z1)], 0.84),
                ("east", [(x0, y1, z1), (x0, y1, z0), (x0, y0, z0), (x0, y0, z1)], 0.90),
                ("west", [(x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0)], 0.90),
                ("top", [(x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)], 1.06),
                ("bottom", [(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)], 0.72),
            ]
            if w == 0:
                allowed = {"east", "west"}
            elif h == 0:
                allowed = {"top", "bottom"}
            elif d == 0:
                allowed = {"front", "back"}
            else:
                allowed = {"front", "back", "east", "west", "top", "bottom"}

            for fname, pts, shade in faces_3d:
                if fname not in allowed or fname not in face_uvs:
                    continue
                rect, flip_u, flip_v = face_uvs[fname]
                rx, ry, rw, rh = rect
                if rw <= 0 or rh <= 0:
                    continue

                tpts = list(pts)
                if crot and any(crot):
                    tpts = [_apply_bone_transform(p, cpiv, crot) for p in tpts]
                tpts = [_transform_point_chain(p, bname, bones) for p in tpts]

                quads.append((tpts, (rx, ry, rw, rh), scale_x, scale_y, flip_u, flip_v, shade))
    return quads


def render_angle(
    quads,
    tex: Image.Image,
    glow: Image.Image | None,
    yaw_deg: float,
    pitch_deg: float,
    size: int = 280,
) -> Image.Image:
    panel = Image.new("RGBA", (size, size), (32, 27, 44, 255))
    if not quads:
        return panel

    tpx = tex.load()
    gpx = glow.load() if glow is not None else None
    tw, th = tex.size

    cam_quads = []
    all_x: list[float] = []
    all_y: list[float] = []
    for pts, rect, scale_x, scale_y, flip_u, flip_v, shade in quads:
        cpts = []
        for p in pts:
            rp = _rot_y(p, yaw_deg)
            rp = _rot_x(rp, pitch_deg)
            # Camera sits at -Z looking toward +Z, so larger -rp[2] is closer to camera
            cp = (rp[0], rp[1], -rp[2])
            cpts.append(cp)
            all_x.append(cp[0])
            all_y.append(cp[1])
        cam_quads.append((cpts, rect, scale_x, scale_y, flip_u, flip_v, shade))

    min_x, max_x = min(all_x), max(all_x)
    min_y, max_y = min(all_y), max(all_y)
    span = max(max_x - min_x, max_y - min_y, 1.0)
    scale = (size * 0.78) / span
    cx = (min_x + max_x) / 2.0
    cy = (min_y + max_y) / 2.0

    zbuf = [[-1e9] * size for _ in range(size)]
    ipx = panel.load()

    for cpts, rect, scale_x, scale_y, flip_u, flip_v, shade in cam_quads:
        spts = [
            (size * 0.5 + (p[0] - cx) * scale, size * 0.5 - (p[1] - cy) * scale, p[2])
            for p in cpts
        ]
        ru, rv, rw, rh = rect
        tris = [
            ((spts[0], (0.0, 0.0)), (spts[1], (1.0, 0.0)), (spts[2], (1.0, 1.0))),
            ((spts[0], (0.0, 0.0)), (spts[2], (1.0, 1.0)), (spts[3], (0.0, 1.0))),
        ]
        for (p0, uv0), (p1, uv1), (p2, uv2) in tris:
            denom = (p1[1] - p2[1]) * (p0[0] - p2[0]) + (p2[0] - p1[0]) * (p0[1] - p2[1])
            if abs(denom) < 1e-6:
                continue
            bx0 = max(0, int(math.floor(min(p0[0], p1[0], p2[0]))))
            bx1 = min(size - 1, int(math.ceil(max(p0[0], p1[0], p2[0]))))
            by0 = max(0, int(math.floor(min(p0[1], p1[1], p2[1]))))
            by1 = min(size - 1, int(math.ceil(max(p0[1], p1[1], p2[1]))))
            for py in range(by0, by1 + 1):
                py_c = py + 0.5
                for px_i in range(bx0, bx1 + 1):
                    px_c = px_i + 0.5
                    w0 = ((p1[1] - p2[1]) * (px_c - p2[0]) + (p2[0] - p1[0]) * (py_c - p2[1])) / denom
                    w1 = ((p2[1] - p0[1]) * (px_c - p2[0]) + (p0[0] - p2[0]) * (py_c - p2[1])) / denom
                    w2 = 1.0 - w0 - w1
                    if w0 < -0.002 or w1 < -0.002 or w2 < -0.002:
                        continue
                    z = w0 * p0[2] + w1 * p1[2] + w2 * p2[2]
                    if z <= zbuf[py][px_i]:
                        continue
                    fu = w0 * uv0[0] + w1 * uv1[0] + w2 * uv2[0]
                    fv = w0 * uv0[1] + w1 * uv1[1] + w2 * uv2[1]
                    fu = max(0.0, min(0.999999, fu))
                    fv = max(0.0, min(0.999999, fv))
                    if flip_u:
                        fu = 0.999999 - fu
                    if flip_v:
                        fv = 0.999999 - fv
                    tx = min(tw - 1, max(0, int((ru + fu * rw) * scale_x)))
                    ty = min(th - 1, max(0, int((rv + fv * rh) * scale_y)))
                    rgba = tpx[tx, ty]
                    if rgba[3] < 128:
                        continue
                    zbuf[py][px_i] = z
                    if gpx is not None and gpx[tx, ty][3] > 128:
                        gr, gg, gb, _ = gpx[tx, ty]
                        ipx[px_i, py] = (gr, gg, gb, 255)
                    else:
                        ipx[px_i, py] = (
                            min(255, int(rgba[0] * shade)),
                            min(255, int(rgba[1] * shade)),
                            min(255, int(rgba[2] * shade)),
                            255,
                        )
    return panel


def preview_target(target: str, out_path: Path | None = None) -> Path:
    if target.startswith("entity/"):
        name = target.split("/", 1)[1]
        geo_name = "dragon_rig" if name in ("canopy_wyvern",) else name
        geo_path = ASSETS / "geo" / "entity" / f"{geo_name}.geo.json"
        tex_path = ASSETS / "textures" / "entity" / f"{name}.png"
    elif target.startswith("armor/"):
        name = target.split("/", 1)[1]
        geo_path = ASSETS / "geo" / "item" / "armor" / f"{name}.geo.json"
        tex_path = ASSETS / "textures" / "item" / "armor" / f"{name}.png"
    else:
        raise ValueError(f"Target must start with entity/ or armor/: {target}")

    geo = json.loads(geo_path.read_text(encoding="utf-8"))
    tex = Image.open(tex_path).convert("RGBA")
    glow_path = tex_path.with_name(f"{tex_path.stem}_glowmask.png")
    glow = Image.open(glow_path).convert("RGBA") if glow_path.exists() else None
    if glow is not None and glow.size != tex.size:
        glow = None

    quads = _cube_quads(geo, tex.size)

    angles = [
        ("Front", 0, -10),
        ("3/4", 35, -18),
        ("Side", 90, -10),
        ("Back", 180, -10),
    ]
    cell = 280
    sheet = Image.new("RGBA", (cell * 4, cell + 30), (24, 20, 34, 255))
    draw = ImageDraw.Draw(sheet)
    for idx, (label, yaw, pitch) in enumerate(angles):
        panel = render_angle(quads, tex, glow, yaw, pitch, size=cell)
        sheet.paste(panel, (idx * cell, 30))
        draw.text((idx * cell + 10, 8), f"{name} - {label}", fill=(240, 236, 255, 255))

    if out_path is None:
        out_path = Path("qa/evidence/previews") / f"{name}-preview.png"
    out_path.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(out_path)
    return out_path


def main() -> int:
    parser = argparse.ArgumentParser(description="Render 4-angle textured 3D contact sheets for GeckoLib models.")
    parser.add_argument("target", nargs="?", help="Model target like entity/crystal_colossus or armor/prismatic_armor")
    parser.add_argument("--out", type=Path, default=None, help="Output PNG path")
    parser.add_argument("--phase3", action="store_true", help="Render previews for all Phase 3 models")
    args = parser.parse_args()

    targets = (
        ["entity/training_golem", "entity/crystal_colossus", "entity/captain_brimstone", "armor/prismatic_armor"]
        if args.phase3
        else ([args.target] if args.target else [])
    )
    if not targets:
        parser.error("Provide a target (e.g. entity/crystal_colossus) or --phase3")

    for t in targets:
        out = preview_target(t, args.out if len(targets) == 1 else None)
        print(f"Rendered {t} -> {out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
