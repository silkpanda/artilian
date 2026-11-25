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
PHASE5_ENTITIES = (
    "amber_wyvern",
    "storm_glidewing",
    "canopy_dragonling",
    "amber_dragonling",
    "storm_dragonling",
    "embercrown_sovereign",
)
PHASE5_ARMORS = ("dragonscale_armor", "twin_star_armor")
BOSS_ENTITIES = ("training_golem", "crystal_colossus", "captain_brimstone", "great_all_seer", "embercrown_sovereign")
SHARED_RIGS = {
    "dragon_rig": ("canopy_wyvern",),
}


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
    desc = geo.get("minecraft:geometry", [{}])[0].get("description", {})
    uv_w = desc.get("texture_width", width)
    uv_h = desc.get("texture_height", height)
    sx, sy = width / uv_w, height / uv_h

    if width < min_width or height < min_height:
        problems.append(f"Sheet size {width}x{height} is below minimum {min_width}x{min_height}")

    taken: list[tuple[str, tuple[int, int, int, int], bool]] = []
    flat_faces = 0
    total_faces = 0

    for bone_name, cube in _iter_cubes(geo):
        origin = cube.get("origin", [0, 0, 0])
        size = cube["size"]
        uv = cube.get("uv")
        w, h, d = int(round(size[0])), int(round(size[1])), int(round(size[2]))
        if (w <= 0 and h <= 0) or (w <= 0 and d <= 0) or (h <= 0 and d <= 0):
            continue

        label = f"{bone_name}/{origin}"
        is_cutout = (w == 0 or h == 0 or d == 0) or (
            name.startswith("armor/") and float(cube.get("inflate", 0.0)) >= 0.46
        )

        if isinstance(uv, dict):
            for face in ("north", "south", "east", "west", "up", "down"):
                entry = uv.get(face)
                if not isinstance(entry, dict):
                    continue
                fuv = entry.get("uv")
                fsize = entry.get("uv_size")
                if not isinstance(fuv, (list, tuple)) or not isinstance(fsize, (list, tuple)):
                    continue
                fu, fv = float(fuv[0]), float(fuv[1])
                su, sv = float(fsize[0]), float(fsize[1])
                rx = int(round(fu + su if su < 0 else fu))
                ry = int(round(fv + sv if sv < 0 else fv))
                rw = int(round(abs(su)))
                rh = int(round(abs(sv)))
                if rw <= 0 or rh <= 0:
                    continue
                if rx < 0 or ry < 0 or rx + rw > uv_w or ry + rh > uv_h:
                    problems.append(f"{label}: {face} UV rect {(rx, ry, rw, rh)} leaves {uv_w}x{uv_h} UV grid")
                    continue
                total_faces += 1
                scaled_rect = (
                    int(round(rx * sx)),
                    int(round(ry * sy)),
                    max(1, int(round(rw * sx))),
                    max(1, int(round(rh * sy))),
                )
                alpha, colors = _describe_face(sheet, scaled_rect)
                if not is_cutout and alpha < 0.99:
                    problems.append(f"{label}: {face} face {(rx, ry, rw, rh)} is {alpha:.0%} opaque")
                elif not is_cutout and colors <= 1 and rw >= 3 and rh >= 3:
                    flat_faces += 1
                    notes.append(f"{label}: {face} face {(rx, ry, rw, rh)} is one flat colour")
            continue

        if not isinstance(uv, (list, tuple)) or len(uv) < 2:
            notes.append(f"{bone_name}/{origin}: invalid UV entry")
            continue

        u, v = int(uv[0]), int(uv[1])
        rect = box_rect(u, v, w, h, d)

        if rect[0] < 0 or rect[1] < 0 or rect[0] + rect[2] > uv_w or rect[1] + rect[3] > uv_h:
            problems.append(f"{label}: UV rect {rect} leaves {uv_w}x{uv_h} UV grid")

        is_mirror = bool(cube.get("mirror", False))
        for other_label, other, other_mirror in taken:
            if rects_overlap(rect, other) and not (is_mirror or other_mirror):
                if rect == other:
                    continue  # Intentional symmetric UV tile reuse
                problems.append(f"{label}: UV rect {rect} partially overlaps {other_label} {other}")
        taken.append((label, rect, is_mirror))

        for face, face_rect in box_faces(u, v, w, h, d).items():
            if face_rect[2] <= 0 or face_rect[3] <= 0:
                continue
            total_faces += 1
            scaled_rect = (
                int(round(face_rect[0] * sx)),
                int(round(face_rect[1] * sy)),
                max(1, int(round(face_rect[2] * sx))),
                max(1, int(round(face_rect[3] * sy))),
            )
            alpha, colors = _describe_face(sheet, scaled_rect)
            if not is_cutout and alpha < 0.99:
                problems.append(f"{label}: {face} face {face_rect} is {alpha:.0%} opaque")
            elif not is_cutout and colors <= 1 and face_rect[2] >= 3 and face_rect[3] >= 3:
                flat_faces += 1
                notes.append(f"{label}: {face} face {face_rect} is one flat colour")

    if total_faces > 0 and flat_faces > 0:
        notes.insert(0, f"{flat_faces}/{total_faces} >=3x3 faces are flat single-colour")

    return problems, notes


def collect_pairs(
    phase3_only: bool,
    phase4_only: bool,
    phase5_only: bool,
    entities: list[str] | None = None,
) -> list[tuple[str, Path, Path, int, int]]:
    pairs: list[tuple[str, Path, Path, int, int]] = []
    if entities:
        for eid in entities:
            pairs.append((f"entity/{eid}", ENTITY_GEO_DIR / f"{eid}.geo.json", ENTITY_TEX_DIR / f"{eid}.png", 128, 128))
        return pairs

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

    if phase5_only:
        for rig, species_list in SHARED_RIGS.items():
            for species_id in species_list:
                pairs.append((f"entity/{species_id}", ENTITY_GEO_DIR / f"{rig}.geo.json", ENTITY_TEX_DIR / f"{species_id}.png", 256, 128))
        for eid in PHASE5_ENTITIES:
            min_w = 128 if eid.endswith("_dragonling") else 256
            pairs.append((f"entity/{eid}", ENTITY_GEO_DIR / f"{eid}.geo.json", ENTITY_TEX_DIR / f"{eid}.png", min_w, 128))
        for aid in PHASE5_ARMORS:
            pairs.append((f"armor/{aid}", ARMOR_GEO_DIR / f"{aid}.geo.json", ARMOR_TEX_DIR / f"{aid}.png", 64, 64))
        return pairs

    for geo_path in sorted(ENTITY_GEO_DIR.glob("*.geo.json")):
        eid = geo_path.name.removesuffix(".geo.json")
        if eid in SHARED_RIGS:
            for species_id in SHARED_RIGS[eid]:
                pairs.append((f"entity/{species_id}", geo_path, ENTITY_TEX_DIR / f"{species_id}.png", 256, 128))
            continue
        if eid in ("chester", "chester_sleep"):
            min_w, min_h = 128, 128
        else:
            min_w = 256 if eid in ("crystal_colossus", "captain_brimstone", "great_all_seer") else 128
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
    parser.add_argument("--phase5", action="store_true", help="Check only Phase 5 Dragon Jungle entities.")
    parser.add_argument("--entity", action="append", default=[], help="Check specific entity ID(s), e.g. --entity chester --entity chester_sleep")
    parser.add_argument("--strict", action="store_true", help="Fail if any flat single-colour faces are found.")
    args = parser.parse_args()

    failed = False
    for name, geo_path, tex_path, min_w, min_h in collect_pairs(args.phase3, args.phase4, args.phase5, args.entity):
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
