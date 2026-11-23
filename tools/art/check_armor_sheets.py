"""Checks the Sunsteel and Starlight armor geo sheets for UV problems.

Run from the project root:

    python tools/art/check_armor_sheets.py

For every cube in each `geo/item/armor/<set>_armor.geo.json` it derives the
Bedrock box-UV rectangle, then reports:

* cubes whose rectangle falls outside the sheet,
* rectangles that overlap an earlier rectangle (texture bleeding),
* faces that sample fully transparent pixels (invisible armor face),
* faces that sample a single flat colour (no shading detail).

Exit code is 1 when a hard problem (outside sheet, overlap, transparent face)
is found.
"""
import json
import sys
from pathlib import Path

from PIL import Image

ASSETS = Path("src/main/resources/assets/artilian")
GEO_DIR = ASSETS / "geo" / "item" / "armor"
TEX_DIR = ASSETS / "textures" / "item" / "armor"


def box_faces(u, v, w, h, d):
    """Bedrock box-UV rectangles for a w x h x d cube with uv offset (u, v)."""
    return {
        "up": (u + d, v, w, d),
        "down": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "south": (u + d + w + d, v + d, w, h),
    }


def rect_of(u, v, w, h, d):
    return (u, v, 2 * (w + d), h + d)


def overlaps(a, b):
    ax, ay, aw, ah = a
    bx, by, bw, bh = b
    return ax < bx + bw and bx < ax + aw and ay < by + bh and by < ay + ah


def describe_face(img, rect):
    """Alpha coverage and colour variety of one face rectangle."""
    x, y, w, h = rect
    px = img.crop((x, y, x + w, y + h)).convert("RGBA")
    data = list(px.getdata())
    if not data:
        return 0.0, 0, None
    visible = [p for p in data if p[3] > 0]
    alpha = len(visible) / len(data)
    colors = {(p[0] >> 4, p[1] >> 4, p[2] >> 4) for p in visible}
    return alpha, len(colors), (px.getpixel((0, 0)) if data else None)


def check(geo_path, tex_path):
    geo = json.loads(geo_path.read_text(encoding="utf-8"))
    sheet = Image.open(tex_path)
    width, height = sheet.size
    problems, notes = [], []

    cubes = []
    for bone in geo["minecraft:geometry"][0]["bones"]:
        for cube in bone.get("cubes", []):
            cubes.append((bone["name"], cube))

    taken = []
    for bone_name, cube in cubes:
        origin, size = cube["origin"], cube["size"]
        u, v = cube.get("uv", [0, 0])[:2]
        rect = rect_of(u, v, size[0], size[1], size[2])
        label = f"{bone_name}/{origin}"
        if rect[0] < 0 or rect[1] < 0 or rect[0] + rect[2] > width or rect[1] + rect[3] > height:
            problems.append(
                f"{label}: UV rect {rect} leaves the {width}x{height} sheet"
            )
        for other_label, other, other_mirror in taken:
            if overlaps(rect, other) and not (cube.get("mirror") or other_mirror):
                problems.append(f"{label}: UV rect {rect} overlaps {other_label} {other}")
        taken.append((label, rect, bool(cube.get("mirror"))))

        for face, face_rect in box_faces(u, v, size[0], size[1], size[2]).items():
            alpha, colors, _ = describe_face(sheet, face_rect)
            if alpha < 0.99:
                problems.append(
                    f"{label}: {face} face {face_rect} is {alpha:.0%} opaque (holes in the armour)"
                )
            elif colors <= 1:
                notes.append(f"{label}: {face} face {face_rect} is one flat colour")

    return problems, notes


def main():
    failed = False
    for set_name in ("sunsteel", "starlight", "prismatic", "dragonscale"):
        geo_path = GEO_DIR / f"{set_name}_armor.geo.json"
        tex_path = TEX_DIR / f"{set_name}_armor.png"
        problems, notes = check(geo_path, tex_path)
        print(f"== {set_name}_armor ==")
        for problem in problems:
            print(f"  PROBLEM {problem}")
        for note in notes:
            print(f"  note    {note}")
        if not problems:
            print("  UV layout clean: in bounds, no overlaps, every face textured")
        print()
        failed = failed or bool(problems)
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
