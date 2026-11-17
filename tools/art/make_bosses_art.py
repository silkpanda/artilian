"""Generates the placeholder GeckoLib model, animations, and texture for Phase 3 Stage 1 boss:

  - training_golem (test-only boss; stone gray with moss and glowing yellow eyes)

Run from the project root:
    python tools/art/make_bosses_art.py

The helpers below are copied from make_structures_enemies_art.py so the art style matches.
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
                    "body": {
                        "position": {"0.0": [0, 0, 0], "1.2": [0, 0.4, 0], "2.4": [0, 0, 0]}
                    },
                    "head": {
                        "rotation": {"0.0": [0, -3, 0], "1.2": [2, 3, 0], "2.4": [0, -3, 0]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 3], "1.2": [-5, 0, 6], "2.4": [0, 0, 3]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, -3], "1.2": [5, 0, -6], "2.4": [0, 0, -3]}
                    },
                },
            },
            f"{p}.walk": {
                "loop": True,
                "animation_length": 0.8,
                "bones": {
                    "body": {
                        "position": {"0.0": [0, 0, 0], "0.4": [0, 0.6, 0], "0.8": [0, 0, 0]},
                        "rotation": {"0.0": [3, 0, -2], "0.4": [3, 0, 2], "0.8": [3, 0, -2]},
                    },
                    "arm_left": {
                        "rotation": {"0.0": [24, 0, 3], "0.4": [-24, 0, 3], "0.8": [24, 0, 3]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [-24, 0, -3], "0.4": [24, 0, -3], "0.8": [-24, 0, -3]}
                    },
                    "leg_left": {
                        "rotation": {"0.0": [-26, 0, 0], "0.4": [26, 0, 0], "0.8": [-26, 0, 0]}
                    },
                    "leg_right": {
                        "rotation": {"0.0": [26, 0, 0], "0.4": [-26, 0, 0], "0.8": [26, 0, 0]}
                    },
                },
            },
            f"{p}.attack": {
                "animation_length": 0.5,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "0.18": [-12, 0, 0], "0.34": [16, 0, 0], "0.5": [0, 0, 0]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 0], "0.18": [-110, 0, 10], "0.34": [26, 0, 0], "0.5": [0, 0, 0]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, 0], "0.18": [-110, 0, -10], "0.34": [26, 0, 0], "0.5": [0, 0, 0]}
                    },
                },
            },
            # Stage 2 will trigger this: both arms rise over 1.5 s, no loop.
            f"{p}.telegraph": {
                "loop": False,
                "animation_length": 1.5,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "1.5": [-8, 0, 0]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 6], "1.5": [-120, 0, 14]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, -6], "1.5": [-120, 0, -14]}
                    },
                },
            },
        },
    }


def build_training_golem():
    eid = "training_golem"
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))

    # Blocky humanoid: 10 px legs + 20 px torso + 12 px head = 42 px, about 2.6 blocks.
    tw, th, td = 16, 20, 10
    hw, hh, hd = 10, 12, 10
    aw, ah, ad = 5, 18, 5
    lw, lh, ld = 6, 10, 6
    leg_top = lh
    torso_top = leg_top + th

    palette = {
        "stone": ((158, 158, 150, 255), (124, 124, 116, 255), (96, 96, 90, 255), (66, 66, 62, 255)),
        "moss": ((126, 176, 92, 255), (82, 132, 62, 255), (54, 96, 44, 255), (36, 66, 32, 255)),
        "eye": ((255, 246, 138, 255), (255, 220, 56, 255), (226, 174, 28, 255), (120, 88, 16, 255)),
        "pupil": (38, 32, 12, 255),
    }

    # Non-overlapping UV rectangles in 128x64.
    uv_torso = (0, 0)
    uv_head = (52, 0)
    uv_arm_l = (0, 30)
    uv_arm_r = (20, 30)
    uv_leg_l = (40, 30)
    uv_leg_r = (60, 30)

    paint_box(img, *uv_torso, tw, th, td, *palette["stone"])
    paint_box(img, *uv_head, hw, hh, hd, *palette["stone"])
    paint_box(img, *uv_arm_l, aw, ah, ad, *palette["stone"])
    paint_box(img, *uv_arm_r, aw, ah, ad, *palette["stone"])
    paint_box(img, *uv_leg_l, lw, lh, ld, *palette["stone"])
    paint_box(img, *uv_leg_r, lw, lh, ld, *palette["stone"])

    # Moss-green accents: shoulders, torso patch, and the top of the head.
    torso_front = faces(*uv_torso, tw, th, td)["front"]
    fill(img, (torso_front[0] + 1, torso_front[1] + 2, 6, 3), palette["moss"][1])
    fill(img, (torso_front[0] + 10, torso_front[1] + 5, 4, 4), palette["moss"][2])
    head_top = faces(*uv_head, hw, hh, hd)["top"]
    fill(img, (head_top[0], head_top[1], head_top[2], 3), palette["moss"][1])
    for arm_uv in (uv_arm_l, uv_arm_r):
        af = faces(*arm_uv, aw, ah, ad)
        fill(img, (af["front"][0], af["front"][1] + 2, aw, 2), palette["moss"][1])

    # Glowing yellow eyes on the head front, with dark pupils.
    head_front = faces(*uv_head, hw, hh, hd)["front"]
    ex, ey = head_front[0], head_front[1] + 4
    fill(img, (ex + 1, ey, 3, 3), palette["eye"][0])
    fill(img, (ex + 6, ey, 3, 3), palette["eye"][0])
    put(img, ex + 2, ey + 1, palette["pupil"])
    put(img, ex + 7, ey + 1, palette["pupil"])

    arm_x = tw / 2.0 + aw / 2.0 + 0.5
    leg_x = 4.0
    bones = [
        {"name": "root", "pivot": [0, 0, 0]},
        {
            "name": "body",
            "parent": "root",
            "pivot": [0, leg_top + th / 2.0, 0],
            "cubes": [
                {"origin": [-tw / 2.0, leg_top, -td / 2.0], "size": [tw, th, td], "uv": list(uv_torso)},
            ],
        },
        {
            "name": "head",
            "parent": "body",
            "pivot": [0, torso_top, 0],
            "cubes": [
                {"origin": [-hw / 2.0, torso_top, -hd / 2.0], "size": [hw, hh, hd], "uv": list(uv_head)},
            ],
        },
        {
            "name": "arm_left",
            "parent": "body",
            "pivot": [arm_x, torso_top - 2.0, 0],
            "cubes": [
                {"origin": [arm_x - aw / 2.0, torso_top - ah, -ad / 2.0], "size": [aw, ah, ad], "uv": list(uv_arm_l)},
            ],
        },
        {
            "name": "arm_right",
            "parent": "body",
            "pivot": [-arm_x, torso_top - 2.0, 0],
            "cubes": [
                {"origin": [-arm_x - aw / 2.0, torso_top - ah, -ad / 2.0], "size": [aw, ah, ad], "uv": list(uv_arm_r)},
            ],
        },
        {
            "name": "leg_left",
            "parent": "root",
            "pivot": [leg_x, leg_top, 0],
            "cubes": [
                {"origin": [leg_x - lw / 2.0, 0, -ld / 2.0], "size": [lw, lh, ld], "uv": list(uv_leg_l)},
            ],
        },
        {
            "name": "leg_right",
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
                    "visible_bounds_width": 3.0,
                    "visible_bounds_height": 2.8,
                    "visible_bounds_offset": [0, 1.3, 0],
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


def main():
    build_training_golem()
    print("Generated boss art: training_golem.")


if __name__ == "__main__":
    main()
