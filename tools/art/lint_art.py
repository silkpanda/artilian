"""Lints GeckoLib .geo.json + .png pairs for UV, alpha, shading, and sheet-size rules.

Implements TD-01 from TECH_DEBT.md (generalizing check_armor_sheets.py).

Usage:
    python tools/art/lint_art.py              # lint all entity + armor pairs
    python tools/art/lint_art.py --phase3     # lint Phase 3 bosses + armor strictly
    python tools/art/lint_art.py --phase4     # lint Phase 4 Eye Realm entities strictly
    python tools/art/lint_art.py --strict     # treat flat faces & small sheets as errors
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from PIL import Image

from art_lib import box_faces, box_rect, rects_overlap

ASSETS = Path("src/main/resources/assets/artilian")
ENTITY_GEO_DIR = ASSETS / "geo" / "entity"
ENTITY_TEX_DIR = ASSETS / "textures" / "entity"
ARMOR_GEO_DIR = ASSETS / "geo" / "item" / "armor"
ARMOR_TEX_DIR = ASSETS / "textures" / "item" / "armor"

PHASE3_ENTITIES = ("training_golem", "crystal_colossus", "captain_brimstone")
PHASE3_ARMORS = ("prismatic_armor",)
PHASE4_ENTITIES = ("peeper", "oculus_slime", "lens_guardian", "blinky", "great_all_seer")
BOSS_ENTITIES = ("training_golem", "crystal_colossus", "captain_brimstone", "great_all_seer")


def _describe_face(img: Image.Image, rect: tuple[int, int, int, int]) -> tuple[float, int]:
    x, y, w, h = rect
    if w <= 0 or h <= 0:
        return 1.0, 2
    px = img.crop((x, y, x + w, y + h)).convert("RGBA")
    data = list(px.getdata())
    if not data:
        return 0.0, 0
    visible = [p for p in data if p[3] > 0]
    alpha = len(visible) / len(data)
    colors = {(p[0] >> 4, p[1] >> 4, p[2] >> 4) for p in visible}
    return alpha, len(colors)


def _iter_cubes(geo: dict):
    for geom in geo.get("minecraft:geometry", []):
        for bone in geom.get("bones", []):
            bname = bone.get("name", "bone")
            for cube in bone.get("cubes", []):
                yield bname, cube


def lint_pair(
    name: str,
    geo_path: Path,
    tex_path: Path,
    *,
    min_width: int = 128,
    min_height: int = 64,
) -> tuple[list[str], list[str]]:
    """Return (problems, notes) for a single .geo.json + .png pair."""
    problems: list[str] = []
    notes: list[str] = []

    if not geo_path.exists():
        return [f"Missing geo file: {geo_path}"], []
    if not tex_path.exists():
        return [f"Missing texture file: {tex_path}"], []

    geo = json.loads(geo_path.read_text(encoding="utf-8"))
    sheet = Image.open(tex_path)
    width, height = sheet.size

    if width < min_width or height < min_height:
        problems.append(f"Sheet size {width}x{height} is below minimum {min_width}x{min_height}")

    taken: list[tuple[str, tuple[int, int, int, int], bool]] = []
    flat_faces = 0
    total_faces = 0

    for bone_name, cube in _iter_cubes(geo):
        origin = cube.get("origin", [0, 0, 0])
        size = cube["size"]
        uv = cube.get("uv")
        if not isinstance(uv, (list, tuple)) or len(uv) < 2:
            notes.append(f"{bone_name}/{origin}: per-face UV dict (skipped box-UV check)")
            continue

        u, v = int(uv[0]), int(uv[1])
        w, h, d = int(round(size[0])), int(round(size[1])), int(round(size[2]))
        if w <= 0 or h <= 0 or d <= 0:
            continue

        rect = box_rect(u, v, w, h, d)
        label = f"{bone_name}/{origin}"

        if rect[0] < 0 or rect[1] < 0 or rect[0] + rect[2] > width or rect[1] + rect[3] > height:
            problems.append(f"{label}: UV rect {rect} leaves {width}x{height} sheet")

        is_mirror = bool(cube.get("mirror", False))
        for other_label, other, other_mirror in taken:
            if rects_overlap(rect, other) and not (is_mirror or other_mirror):
                if rect == other:
                    continue  # Intentional symmetric UV tile reuse
                problems.append(f"{label}: UV rect {rect} partially overlaps {other_label} {other}")
        taken.append((label, rect, is_mirror))

        for face, face_rect in box_faces(u, v, w, h, d).items():
            total_faces += 1
            alpha, colors = _describe_face(sheet, face_rect)
            if alpha < 0.99:
                problems.append(f"{label}: {face} face {face_rect} is {alpha:.0%} opaque")
            elif colors <= 1 and face_rect[2] >= 3 and face_rect[3] >= 3:
                flat_faces += 1
                notes.append(f"{label}: {face} face {face_rect} is one flat colour")

    if total_faces > 0 and flat_faces > 0:
        notes.insert(0, f"{flat_faces}/{total_faces} >=3x3 faces are flat single-colour")

    return problems, notes


def collect_pairs(phase3_only: bool, phase4_only: bool) -> list[tuple[str, Path, Path, int, int]]:
    pairs: list[tuple[str, Path, Path, int, int]] = []
    if phase3_only:
        for eid in PHASE3_ENTITIES:
            min_w = 256 if eid in ("crystal_colossus", "captain_brimstone") else 128
            min_h = 128
            pairs.append((f"entity/{eid}", ENTITY_GEO_DIR / f"{eid}.geo.json", ENTITY_TEX_DIR / f"{eid}.png", min_w, min_h))
        for aid in PHASE3_ARMORS:
            pairs.append((f"armor/{aid}", ARMOR_GEO_DIR / f"{aid}.geo.json", ARMOR_TEX_DIR / f"{aid}.png", 64, 64))
        return pairs

    if phase4_only:
        for eid in PHASE4_ENTITIES:
            min_w = 256 if eid == "great_all_seer" else 128
            min_h = 128
            pairs.append((f"entity/{eid}", ENTITY_GEO_DIR / f"{eid}.geo.json", ENTITY_TEX_DIR / f"{eid}.png", min_w, min_h))
        return pairs

    for geo_path in sorted(ENTITY_GEO_DIR.glob("*.geo.json")):
        eid = geo_path.name.removesuffix(".geo.json")
        min_w = 256 if eid in ("crystal_colossus", "captain_brimstone", "great_all_seer") else (128 if eid != "chester" else 64)
        min_h = 128 if eid in BOSS_ENTITIES else 64
        pairs.append((f"entity/{eid}", geo_path, ENTITY_TEX_DIR / f"{eid}.png", min_w, min_h))

    for geo_path in sorted(ARMOR_GEO_DIR.glob("*.geo.json")):
        aid = geo_path.name.removesuffix(".geo.json")
        pairs.append((f"armor/{aid}", geo_path, ARMOR_TEX_DIR / f"{aid}.png", 64, 64))

    return pairs


def main() -> int:
    parser = argparse.ArgumentParser(description="Lint Artilian GeckoLib models and textures.")
    parser.add_argument("--phase3", action="store_true", help="Check only Phase 3 bosses and armor.")
    parser.add_argument("--phase4", action="store_true", help="Check only Phase 4 Eye Realm entities.")
    parser.add_argument("--strict", action="store_true", help="Fail if any flat single-colour faces are found.")
    args = parser.parse_args()

    failed = False
    for name, geo_path, tex_path, min_w, min_h in collect_pairs(args.phase3, args.phase4):
        problems, notes = lint_pair(name, geo_path, tex_path, min_width=min_w, min_height=min_h)
        print(f"== {name} ==")
        for p in problems:
            print(f"  PROBLEM {p}")
        for n in notes:
            print(f"  note    {n}")
        if not problems and not notes:
            print("  CLEAN: UV layout, sheet size, opacity, and shading all pass")
        elif not problems:
            print("  UV & sheet clean (see notes)")
        print()
        if problems or (args.strict and any("flat single-colour" in n for n in notes)):
            failed = True
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
