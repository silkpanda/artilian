"""Headless Pillow 3D contact-sheet previewer for GeckoLib .geo.json + .png models.

Implements TD-01 from TECH_DEBT.md so agents can inspect silhouettes, proportions,
and texture shading from Front, 3/4, Side, and Back angles before booting runClient.

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
    # Bedrock rotation order: Z, Y, X (negated X/Y in Bedrock space)
    x, y, z = _rot_x((x, y, z), rot[0])
    x, y, z = _rot_y((x, y, z), rot[1])
    x, y, z = _rot_z((x, y, z), rot[2])
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


def _avg_face_color(img: Image.Image, rect: tuple[int, int, int, int], shade: float):
    rx, ry, rw, rh = rect
    if rw <= 0 or rh <= 0:
        return (128, 128, 128, 255)
    crop = img.crop((rx, ry, rx + rw, ry + rh)).convert("RGBA")
    pixels = [p for p in crop.getdata() if p[3] > 20]
    if not pixels:
        return None
    r = int(sum(p[0] for p in pixels) / len(pixels) * shade)
    g = int(sum(p[1] for p in pixels) / len(pixels) * shade)
    b = int(sum(p[2] for p in pixels) / len(pixels) * shade)
    return (min(255, r), min(255, g), min(255, b), 255)


def _cube_quads(geo: dict, tex: Image.Image):
    bones = _build_bone_map(geo)
    quads = []
    for bname, bone in bones.items():
        for cube in bone.get("cubes", []):
            ox, oy, oz = cube["origin"]
            sx, sy, sz = cube["size"]
            inf = cube.get("inflate", 0.0)
            x0, y0, z0 = ox - inf, oy - inf, oz - inf
            x1, y1, z1 = ox + sx + inf, oy + sy + inf, oz + sz + inf
            uv = cube.get("uv", [0, 0])
            u, v = (int(uv[0]), int(uv[1])) if isinstance(uv, (list, tuple)) else (0, 0)
            w, h, d = max(1, int(round(sx))), max(1, int(round(sy))), max(1, int(round(sz)))
            frects = box_faces(u, v, w, h, d)

            # 6 faces in Bedrock coords (north = -Z front, south = +Z back)
            faces_3d = [
                ("front", [(x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0)], 1.00),
                ("back", [(x1, y1, z1), (x0, y1, z1), (x0, y0, z1), (x1, y0, z1)], 0.85),
                ("east", [(x0, y1, z1), (x0, y1, z0), (x0, y0, z0), (x0, y0, z1)], 0.90),
                ("west", [(x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0)], 0.90),
                ("top", [(x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)], 1.05),
                ("bottom", [(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)], 0.72),
            ]
            for fname, pts, shade in faces_3d:
                color = _avg_face_color(tex, frects[fname], shade)
                if color is None:
                    continue
                tpts = [_transform_point_chain(p, bname, bones) for p in pts]
                crot = cube.get("rotation")
                if crot:
                    cpiv = cube.get("pivot", [0, 0, 0])
                    tpts = [_apply_bone_transform(p, cpiv, crot) for p in tpts]
                quads.append((tpts, color))
    return quads


def render_angle(quads, yaw_deg: float, pitch_deg: float, size: int = 240) -> Image.Image:
    panel = Image.new("RGBA", (size, size), (36, 30, 48, 255))
    draw = ImageDraw.Draw(panel)
    if not quads:
        return panel

    projected = []
    all_x, all_y = [], []
    for pts, color in quads:
        rpts = []
        for p in pts:
            rp = _rot_y(p, yaw_deg)
            rp = _rot_x(rp, pitch_deg)
            rpts.append(rp)
            all_x.append(rp[0])
            all_y.append(rp[1])
        avg_z = sum(p[2] for p in rpts) / 4.0
        projected.append((avg_z, rpts, color))

    min_x, max_x = min(all_x), max(all_x)
    min_y, max_y = min(all_y), max(all_y)
    span = max(max_x - min_x, max_y - min_y, 1.0)
    scale = (size * 0.76) / span
    cx = (min_x + max_x) / 2.0
    cy = (min_y + max_y) / 2.0

    # Painter's algorithm: furthest (+Z in this view) drawn first
    projected.sort(key=lambda item: item[0], reverse=True)
    for _, rpts, color in projected:
        poly = [(size / 2 + (p[0] - cx) * scale, size / 2 - (p[1] - cy) * scale) for p in rpts]
        draw.polygon(poly, fill=color, outline=(22, 18, 30, 120))
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
    quads = _cube_quads(geo, tex)

    angles = [
        ("Front", 0, -10),
        ("3/4", 35, -18),
        ("Side", 90, -10),
        ("Back", 180, -10),
    ]
    cell = 240
    sheet = Image.new("RGBA", (cell * 4, cell + 28), (26, 21, 36, 255))
    draw = ImageDraw.Draw(sheet)
    for idx, (label, yaw, pitch) in enumerate(angles):
        panel = render_angle(quads, yaw, pitch, size=cell)
        sheet.paste(panel, (idx * cell, 28))
        draw.text((idx * cell + 10, 8), f"{name} - {label}", fill=(240, 236, 255, 255))

    if out_path is None:
        out_path = Path("qa/evidence/previews") / f"{name}-preview.png"
    out_path.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(out_path)
    return out_path


def main() -> int:
    parser = argparse.ArgumentParser(description="Render 4-angle 3D contact sheets for GeckoLib models.")
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
