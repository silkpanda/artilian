"""Generates GeckoLib models, animations, and textures for Phase 2 Stage 6 structure enemies:
  - prism_sentinel  (Crystal Canopy Woods / Crystal Grove Shrine)
  - cinder_raider   (Emberbloom Meadows / Emberbloom Outpost)
  - astral_stalker  (Starlight Highlands / Starlight Watchtower)

Run from the project root:
    python tools/art/make_structures_enemies_art.py
"""
import json
from pathlib import Path
from PIL import Image

ASSETS = Path("src/main/resources/assets/artilian")


def faces(u, v, w, h, d):
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }


def fill(img, rect, color):
    x, y, w, h = rect
    for px in range(x, x + w):
        for py in range(y, y + h):
            if 0 <= px < img.width and 0 <= py < img.height:
                img.putpixel((px, py), color)


def put(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def paint_box(img, u, v, w, h, d, hi, base, shade, deep):
    f = faces(u, v, w, h, d)
    fill(img, f["top"], hi)
    fill(img, f["front"], base)
    fill(img, f["east"], shade)
    fill(img, f["west"], shade)
    fill(img, f["back"], shade)
    fill(img, f["bottom"], deep)
    # Subtle top rim highlight on front & sides
    fx, fy, fw, _ = f["front"]
    fill(img, (fx, fy, fw, 1), hi)


def make_animations(entity_id):
    p = f"animation.{entity_id}"
    return {
        "format_version": "1.8.0",
        "animations": {
            f"{p}.idle": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "torso": {
                        "position": {"0.0": [0, 0.0, 0], "1.2": [0, 0.6, 0], "2.4": [0, 0.0, 0]}
                    },
                    "head": {
                        "rotation": {"0.0": [0, -4, 0], "1.2": [3, 4, 0], "2.4": [0, -4, 0]}
                    },
                    "arm_l": {
                        "rotation": {"0.0": [0, 0, 4], "1.2": [-6, 0, 8], "2.4": [0, 0, 4]}
                    },
                    "arm_r": {
                        "rotation": {"0.0": [0, 0, -4], "1.2": [6, 0, -8], "2.4": [0, 0, -4]}
                    },
                    "crest": {
                        "position": {"0.0": [0, 0, 0], "1.2": [0, 0.9, 0], "2.4": [0, 0, 0]}
                    },
                },
            },
            f"{p}.walk": {
                "loop": True,
                "animation_length": 0.7,
                "bones": {
                    "torso": {
                        "position": {"0.0": [0, 0.0, 0], "0.35": [0, 0.8, 0], "0.7": [0, 0.0, 0]},
                        "rotation": {"0.0": [4, 0, -2], "0.35": [4, 0, 2], "0.7": [4, 0, -2]},
                    },
                    "arm_l": {
                        "rotation": {"0.0": [28, 0, 4], "0.35": [-28, 0, 4], "0.7": [28, 0, 4]}
                    },
                    "arm_r": {
                        "rotation": {"0.0": [-28, 0, -4], "0.35": [28, 0, -4], "0.7": [-28, 0, -4]}
                    },
                    "leg_l": {
                        "rotation": {"0.0": [-30, 0, 0], "0.35": [30, 0, 0], "0.7": [-30, 0, 0]}
                    },
                    "leg_r": {
                        "rotation": {"0.0": [30, 0, 0], "0.35": [-30, 0, 0], "0.7": [30, 0, 0]}
                    },
                },
            },
            f"{p}.attack": {
                "animation_length": 0.45,
                "bones": {
                    "torso": {
                        "rotation": {"0.0": [0, 0, 0], "0.15": [-14, 0, 0], "0.3": [18, 0, 0], "0.45": [0, 0, 0]}
                    },
                    "arm_l": {
                        "rotation": {"0.0": [0, 0, 0], "0.15": [-115, 0, 10], "0.3": [24, 0, 0], "0.45": [0, 0, 0]}
                    },
                    "arm_r": {
                        "rotation": {"0.0": [0, 0, 0], "0.15": [-115, 0, -10], "0.3": [24, 0, 0], "0.45": [0, 0, 0]}
                    },
                },
            },
        },
    }


def build_enemy(spec):
    eid = spec["id"]
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    pal = spec["palette"]

    tw, td, th = spec["torso_size"]
    hw, hd, hh = spec["head_size"]
    aw, ad, ah = spec["arm_size"]
    lw, ld, lh = spec["leg_size"]

    # Y offsets in pixels (16 px = 1 block)
    leg_top = lh
    torso_top = leg_top + th

    # UV layout (non-overlapping in 128x64)
    uv_torso = (0, 0)
    uv_head = (36, 0)
    uv_core = (68, 0)
    uv_visor = (84, 0)
    uv_arm_l = (0, 24)
    uv_arm_r = (20, 24)
    uv_leg_l = (40, 24)
    uv_leg_r = (60, 24)
    uv_crest_l = (80, 24)
    uv_crest_r = (96, 24)
    uv_crest_top = (0, 44)

    # Paint armor/body boxes
    paint_box(img, *uv_torso, tw, th, td, *pal["armor"])
    paint_box(img, *uv_head, hw, hh, hd, *pal["head"])
    paint_box(img, *uv_core, 4, 4, 1, *pal["glow"])
    paint_box(img, *uv_visor, hw - 2, 2, 1, *pal["glow"])
    paint_box(img, *uv_arm_l, aw, ah, ad, *pal["limb"])
    paint_box(img, *uv_arm_r, aw, ah, ad, *pal["limb"])
    paint_box(img, *uv_leg_l, lw, lh, ld, *pal["limb"])
    paint_box(img, *uv_leg_r, lw, lh, ld, *pal["limb"])
    paint_box(img, *uv_crest_l, 3, 4, 3, *pal["accent"])
    paint_box(img, *uv_crest_r, 3, 4, 3, *pal["accent"])
    paint_box(img, *uv_crest_top, 3, 4, 3, *pal["glow"])

    # Paint bright eye pupils / visor shine on head front and visor front
    vf = faces(*uv_visor, hw - 2, 2, 1)["front"]
    put(img, vf[0] + 1, vf[1], pal["eye_pupil"])
    put(img, vf[0] + vf[2] - 2, vf[1], pal["eye_pupil"])
    put(img, vf[0], vf[1], pal["eye_spark"])

    # Paint glowing gauntlet cuffs on arm bottoms
    for arm_uv in (uv_arm_l, uv_arm_r):
        af = faces(*arm_uv, aw, ah, ad)
        fill(img, (af["front"][0], af["front"][1] + ah - 2, aw, 2), pal["glow"][1])

    # Build GeckoLib .geo.json
    arm_x = tw / 2.0 + aw / 2.0 + 0.5
    leg_x = max(2.2, tw / 4.0 + 0.5)

    bones = [
        {"name": "root", "pivot": [0, 0, 0]},
        {
            "name": "torso",
            "parent": "root",
            "pivot": [0, leg_top + th / 2.0, 0],
            "cubes": [
                {"origin": [-tw / 2.0, leg_top, -td / 2.0], "size": [tw, th, td], "uv": list(uv_torso)},
                {"origin": [-2.0, leg_top + th - 6.0, -td / 2.0 - 0.5], "size": [4, 4, 1], "uv": list(uv_core)},
            ],
        },
        {
            "name": "head",
            "parent": "torso",
            "pivot": [0, torso_top, 0],
            "cubes": [
                {"origin": [-hw / 2.0, torso_top, -hd / 2.0], "size": [hw, hh, hd], "uv": list(uv_head)},
                {"origin": [-(hw - 2) / 2.0, torso_top + 2.0, -hd / 2.0 - 0.6], "size": [hw - 2, 2, 1], "uv": list(uv_visor)},
            ],
        },
        {
            "name": "crest",
            "parent": "head",
            "pivot": [0, torso_top + hh, 0],
            "cubes": [
                {"origin": [-1.5, torso_top + hh, -1.5], "size": [3, 4, 3], "uv": list(uv_crest_top)},
            ],
        },
        {
            "name": "arm_l",
            "parent": "torso",
            "pivot": [arm_x, torso_top - 1.5, 0],
            "cubes": [
                {"origin": [arm_x - aw / 2.0, torso_top - ah, -ad / 2.0], "size": [aw, ah, ad], "uv": list(uv_arm_l)},
                {"origin": [arm_x - 1.5, torso_top - 0.5, -1.5], "size": [3, 4, 3], "uv": list(uv_crest_l)},
            ],
        },
        {
            "name": "arm_r",
            "parent": "torso",
            "pivot": [-arm_x, torso_top - 1.5, 0],
            "cubes": [
                {"origin": [-arm_x - aw / 2.0, torso_top - ah, -ad / 2.0], "size": [aw, ah, ad], "uv": list(uv_arm_r)},
                {"origin": [-arm_x - 1.5, torso_top - 0.5, -1.5], "size": [3, 4, 3], "uv": list(uv_crest_r)},
            ],
        },
        {
            "name": "leg_l",
            "parent": "root",
            "pivot": [leg_x, leg_top, 0],
            "cubes": [
                {"origin": [leg_x - lw / 2.0, 0, -ld / 2.0], "size": [lw, lh, ld], "uv": list(uv_leg_l)},
            ],
        },
        {
            "name": "leg_r",
            "parent": "root",
            "pivot": [-leg_x, leg_top, 0],
            "cubes": [
                {"origin": [-leg_x - lw / 2.0, 0, -ld / 2.0], "size": [lw, lh, ld], "uv": list(uv_leg_r)},
            ],
        },
    ]

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": f"geometry.{eid}",
                    "texture_width": 128,
                    "texture_height": 64,
                    "visible_bounds_width": 2.5,
                    "visible_bounds_height": 2.8,
                    "visible_bounds_offset": [0, 1.1, 0],
                },
                "bones": bones,
            }
        ],
    }

    (ASSETS / "textures/entity").mkdir(parents=True, exist_ok=True)
    (ASSETS / "geo/entity").mkdir(parents=True, exist_ok=True)
    (ASSETS / "animations/entity").mkdir(parents=True, exist_ok=True)

    img.save(ASSETS / f"textures/entity/{eid}.png")
    (ASSETS / f"geo/entity/{eid}.geo.json").write_text(json.dumps(geo, indent=2) + "\n", encoding="utf-8")
    (ASSETS / f"animations/entity/{eid}.animation.json").write_text(
        json.dumps(make_animations(eid), indent=2) + "\n", encoding="utf-8"
    )


SPECS = [
    {
        "id": "prism_sentinel",
        "torso_size": (10, 6, 11),
        "head_size": (8, 7, 7),
        "arm_size": (4, 4, 11),
        "leg_size": (4, 4, 10),
        "palette": {
            "armor": ((98, 84, 134, 255), (58, 47, 77, 255), (43, 35, 56, 255), (28, 22, 38, 255)),
            "head": ((118, 102, 160, 255), (72, 58, 104, 255), (48, 38, 70, 255), (28, 22, 38, 255)),
            "limb": ((90, 76, 124, 255), (54, 44, 74, 255), (38, 30, 54, 255), (24, 18, 34, 255)),
            "accent": ((156, 252, 244, 255), (56, 217, 192, 255), (32, 158, 154, 255), (24, 98, 108, 255)),
            "glow": ((228, 255, 255, 255), (125, 249, 255, 255), (56, 217, 192, 255), (28, 132, 136, 255)),
            "eye_pupil": (24, 34, 62, 255),
            "eye_spark": (255, 255, 255, 255),
        },
    },
    {
        "id": "cinder_raider",
        "torso_size": (8, 5, 10),
        "head_size": (8, 7, 7),
        "arm_size": (3, 3, 10),
        "leg_size": (3, 3, 9),
        "palette": {
            "armor": ((96, 48, 42, 255), (68, 30, 28, 255), (46, 20, 22, 255), (28, 12, 14, 255)),
            "head": ((116, 56, 46, 255), (82, 36, 32, 255), (54, 24, 24, 255), (32, 14, 16, 255)),
            "limb": ((88, 44, 38, 255), (60, 28, 26, 255), (42, 18, 20, 255), (26, 12, 14, 255)),
            "accent": ((255, 224, 142, 255), (255, 159, 56, 255), (214, 104, 24, 255), (142, 54, 14, 255)),
            "glow": ((255, 246, 214, 255), (249, 115, 22, 255), (206, 68, 18, 255), (132, 34, 12, 255)),
            "eye_pupil": (38, 14, 10, 255),
            "eye_spark": (255, 255, 255, 255),
        },
    },
    {
        "id": "astral_stalker",
        "torso_size": (8, 5, 11),
        "head_size": (7, 7, 7),
        "arm_size": (3, 3, 10),
        "leg_size": (3, 3, 10),
        "palette": {
            "armor": ((84, 66, 126, 255), (52, 40, 84, 255), (36, 28, 60, 255), (22, 16, 40, 255)),
            "head": ((96, 76, 142, 255), (62, 48, 98, 255), (42, 32, 70, 255), (24, 18, 44, 255)),
            "limb": ((78, 60, 116, 255), (48, 36, 78, 255), (34, 24, 56, 255), (20, 14, 38, 255)),
            "accent": ((244, 148, 255, 255), (217, 70, 239, 255), (154, 42, 182, 255), (96, 24, 124, 255)),
            "glow": ((255, 226, 255, 255), (139, 124, 255, 255), (217, 70, 239, 255), (110, 42, 168, 255)),
            "eye_pupil": (24, 14, 48, 255),
            "eye_spark": (125, 249, 255, 255),
        },
    },
]


def main():
    for spec in SPECS:
        build_enemy(spec)
    print("Generated structure enemy assets: prism_sentinel, cinder_raider, astral_stalker.")


if __name__ == "__main__":
    main()
