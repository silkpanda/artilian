"""Generates GeckoLib models, animations, textures, and items for Phase 3 bosses:

  - training_golem (test-only boss; stone gray with moss and glowing yellow eyes)
  - boss_altar block (dark slate with a #FF4D5A rune) and golem_core item (moss-flecked stone orb)
  - crystal_colossus (Crystal/Starlight titan with glowmask & orbiting shards)
  - colossus_heart and prism_core items

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
            # Triggered during the Ground Slam windup: both arms rise over 2.0 s (40 ticks), no loop.
            f"{p}.telegraph": {
                "loop": False,
                "animation_length": 2.0,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [-8, 0, 0]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 6], "2.0": [-120, 0, 14]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, -6], "2.0": [-120, 0, -14]}
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


DANGER_RED = (255, 77, 90, 255)


def build_boss_altar():
    img = Image.new("RGBA", (16, 16), (40, 38, 52, 255))
    # Mottled slate: a fixed pattern so reruns produce the same file.
    for y in range(16):
        for x in range(16):
            if (x * 7 + y * 13) % 11 == 0:
                put(img, x, y, (54, 51, 70, 255))
            elif (x * 5 + y * 3) % 17 == 0:
                put(img, x, y, (30, 28, 40, 255))
    # Frame
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
            put(img, x, y, (24, 22, 32, 255))
    # Rune: a red diamond with a dot, the same red as the boss telegraph ring.
    for x, y in ((7, 3), (8, 3), (5, 5), (10, 5), (4, 7), (11, 7), (4, 8), (11, 8),
                 (5, 10), (10, 10), (7, 12), (8, 12), (6, 4), (9, 4), (6, 11), (9, 11)):
        put(img, x, y, DANGER_RED)
    fill(img, (7, 7, 2, 2), DANGER_RED)
    (ASSETS / "textures/block").mkdir(parents=True, exist_ok=True)
    img.save(ASSETS / "textures/block/boss_altar.png")


def build_golem_core():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    center = 7.5
    for y in range(16):
        for x in range(16):
            d = ((x - center) ** 2 + (y - center) ** 2) ** 0.5
            if d <= 6.0:
                shade = (128, 124, 118, 255) if (x + y) < 15 else (98, 94, 90, 255)
                if d > 5.0:
                    shade = (70, 68, 66, 255)
                put(img, x, y, shade)
    fill(img, (4, 5, 3, 2), (92, 128, 70, 255))
    fill(img, (9, 9, 2, 2), (92, 128, 70, 255))
    fill(img, (7, 7, 2, 2), (255, 214, 92, 255))
    put(img, 5, 4, (176, 172, 166, 255))
    (ASSETS / "textures/item").mkdir(parents=True, exist_ok=True)
    img.save(ASSETS / "textures/item/golem_core.png")


def paint_box_both(img, glow, u, v, w, h, d, pal, glow_pal=None):
    paint_box(img, u, v, w, h, d, *pal)
    if glow_pal is not None:
        paint_box(glow, u, v, w, h, d, *glow_pal)


def make_colossus_animations():
    p = "animation.crystal_colossus"
    return {
        "format_version": "1.8.0",
        "animations": {
            f"{p}.idle": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "body": {
                        "position": {"0.0": [0, 0, 0], "1.2": [0, 0.5, 0], "2.4": [0, 0, 0]}
                    },
                    "head": {
                        "rotation": {"0.0": [0, -3, 0], "1.2": [2, 3, 0], "2.4": [0, -3, 0]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 4], "1.2": [-5, 0, 7], "2.4": [0, 0, 4]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, -4], "1.2": [5, 0, -7], "2.4": [0, 0, -4]}
                    },
                    "floating_shards": {
                        "position": {"0.0": [0, 0, 0], "1.2": [0, 1.5, 0], "2.4": [0, 0, 0]},
                        "rotation": {"0.0": [0, 0, 0], "2.4": [0, 360, 0]},
                    },
                },
            },
            f"{p}.walk": {
                "loop": True,
                "animation_length": 0.9,
                "bones": {
                    "body": {
                        "position": {"0.0": [0, 0, 0], "0.45": [0, 0.8, 0], "0.9": [0, 0, 0]},
                        "rotation": {"0.0": [4, 0, -2], "0.45": [4, 0, 2], "0.9": [4, 0, -2]},
                    },
                    "arm_left": {
                        "rotation": {"0.0": [22, 0, 4], "0.45": [-22, 0, 4], "0.9": [22, 0, 4]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [-22, 0, -4], "0.45": [22, 0, -4], "0.9": [-22, 0, -4]}
                    },
                    "leg_left": {
                        "rotation": {"0.0": [-22, 0, 0], "0.45": [22, 0, 0], "0.9": [-22, 0, 0]}
                    },
                    "leg_right": {
                        "rotation": {"0.0": [22, 0, 0], "0.45": [-22, 0, 0], "0.9": [22, 0, 0]}
                    },
                    "floating_shards": {
                        "rotation": {"0.0": [0, 0, 0], "0.9": [0, 180, 0]},
                    },
                },
            },
            f"{p}.attack": {
                "animation_length": 0.55,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "0.2": [-14, 0, 0], "0.38": [18, 0, 0], "0.55": [0, 0, 0]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 0], "0.2": [-115, 0, 10], "0.38": [28, 0, 0], "0.55": [0, 0, 0]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, 0], "0.2": [-115, 0, -10], "0.38": [28, 0, 0], "0.55": [0, 0, 0]}
                    },
                    "floating_shards": {
                        "rotation": {"0.0": [0, 0, 0], "0.55": [0, 180, 0]}
                    },
                },
            },
            f"{p}.telegraph": {
                "loop": False,
                "animation_length": 2.0,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "1.5": [-10, 0, 0], "2.0": [-14, 0, 0]}
                    },
                    "shoulder_left": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, 0, -10]}
                    },
                    "shoulder_right": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, 0, 10]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 6], "2.0": [-130, 0, 16]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, -6], "2.0": [-130, 0, -16]}
                    },
                    "floating_shards": {
                        "position": {"0.0": [0, 0, 0], "2.0": [0, 4.0, 0]},
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, 720, 0]},
                    },
                },
            },
        },
    }


def build_crystal_colossus():
    eid = "crystal_colossus"
    img = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (256, 128), (0, 0, 0, 0))

    # DESIGN.md arcane slate + Starlight teal + Starlight violet palette
    slate = ((90, 76, 114, 255), (58, 47, 77, 255), (43, 35, 56, 255), (27, 21, 36, 255))
    dark_trim = ((72, 60, 96, 255), (46, 37, 64, 255), (34, 27, 48, 255), (21, 16, 30, 255))
    teal = ((168, 255, 250, 255), (56, 217, 192, 255), (34, 164, 152, 255), (20, 110, 108, 255))
    bright_teal = ((238, 255, 255, 255), (125, 249, 255, 255), (56, 217, 192, 255), (38, 178, 166, 255))
    violet = ((198, 172, 255, 255), (139, 124, 255, 255), (102, 82, 212, 255), (68, 52, 160, 255))
    gold_trim = ((255, 224, 130, 255), (255, 159, 56, 255), (196, 112, 32, 255), (132, 70, 18, 255))

    # Non-overlapping UV layout in 256x128
    uv_torso = (0, 0)              # 24x28x14 -> 76x42
    uv_head = (78, 0)              # 14x13x14 -> 56x27
    uv_pauldron_l = (136, 0)       # 10x8x14  -> 48x22
    uv_pauldron_r = (186, 0)       # 10x8x14  -> 48x22
    uv_leg_l = (0, 44)             # 9x16x10  -> 38x26
    uv_leg_r = (40, 44)            # 9x16x10  -> 38x26
    uv_upper_arm_l = (80, 30)      # 6x12x6   -> 24x18
    uv_upper_arm_r = (106, 30)     # 6x12x6   -> 24x18
    uv_fist_l = (80, 50)           # 8x14x8   -> 32x22
    uv_fist_r = (114, 50)          # 8x14x8   -> 32x22
    uv_chest_outer = (136, 24)     # 8x10x3   -> 22x13
    uv_chest_inner = (160, 24)     # 4x6x2    -> 12x8
    uv_spire_tall_l = (174, 24)    # 5x9x6    -> 22x15
    uv_spire_tall_r = (198, 24)    # 5x9x6    -> 22x15
    uv_spire_side_l = (150, 42)    # 4x7x5    -> 18x12
    uv_spire_side_r = (170, 42)    # 4x7x5    -> 18x12
    uv_crown_center = (190, 42)    # 4x7x4    -> 16x11
    uv_crown_horn_l = (208, 42)    # 3x5x3    -> 12x8
    uv_crown_horn_r = (222, 42)    # 3x5x3    -> 12x8
    uv_back_spine_big = (150, 56)  # 4x8x5    -> 18x13
    uv_back_spine_sm = (170, 56)   # 4x6x4    -> 16x10
    uv_kneecap = (188, 56)         # 7x5x2    -> 18x7
    uv_knuckle = (208, 56)         # 6x4x2    -> 16x6
    uv_shards = [(0, 74), (14, 74), (28, 74), (42, 74)]  # each 3x7x3 -> 12x10

    # Paint structural armor parts
    paint_box(img, *uv_torso, 24, 28, 14, *slate)
    paint_box(img, *uv_head, 14, 13, 14, *slate)
    paint_box(img, *uv_pauldron_l, 10, 8, 14, *dark_trim)
    paint_box(img, *uv_pauldron_r, 10, 8, 14, *dark_trim)
    paint_box(img, *uv_leg_l, 9, 16, 10, *dark_trim)
    paint_box(img, *uv_leg_r, 9, 16, 10, *dark_trim)
    paint_box(img, *uv_upper_arm_l, 6, 12, 6, *slate)
    paint_box(img, *uv_upper_arm_r, 6, 12, 6, *slate)
    paint_box(img, *uv_fist_l, 8, 14, 8, *dark_trim)
    paint_box(img, *uv_fist_r, 8, 14, 8, *dark_trim)
    paint_box(img, *uv_kneecap, 7, 5, 2, *slate)

    # Gold arcane trim along belt & pauldron rims
    tf = faces(*uv_torso, 24, 28, 14)
    fill(img, (tf["front"][0], tf["front"][1] + 24, 24, 2), gold_trim[1])
    fill(img, (tf["front"][0] + 10, tf["front"][1] + 23, 4, 4), gold_trim[0])
    for p_uv in (uv_pauldron_l, uv_pauldron_r):
        pf = faces(*p_uv, 10, 8, 14)
        fill(img, (pf["front"][0], pf["front"][1] + 6, 10, 1), gold_trim[1])
        fill(img, (pf["east"][0], pf["east"][1] + 6, 14, 1), gold_trim[1])
        fill(img, (pf["west"][0], pf["west"][1] + 6, 14, 1), gold_trim[1])

    # Glowing crystal veins across torso & fists
    for vein_rect in (
        (tf["front"][0] + 3, tf["front"][1] + 4, 2, 10),
        (tf["front"][0] + 19, tf["front"][1] + 4, 2, 10),
        (tf["front"][0] + 5, tf["front"][1] + 16, 4, 2),
        (tf["front"][0] + 15, tf["front"][1] + 16, 4, 2),
    ):
        fill(img, vein_rect, teal[1])
        fill(glow, vein_rect, bright_teal[1])
    for fist_uv in (uv_fist_l, uv_fist_r):
        ff = faces(*fist_uv, 8, 14, 8)
        for band in ((ff["front"][0] + 1, ff["front"][1] + 3, 6, 2), (ff["front"][0] + 3, ff["front"][1] + 5, 2, 5)):
            fill(img, band, teal[1])
            fill(glow, band, bright_teal[1])
    kf = faces(*uv_kneecap, 7, 5, 2)
    fill(img, (kf["front"][0] + 2, kf["front"][1] + 1, 3, 3), teal[1])
    fill(glow, (kf["front"][0] + 2, kf["front"][1] + 1, 3, 3), bright_teal[1])

    # Glowing visor eyes on head front
    hf = faces(*uv_head, 14, 13, 14)["front"]
    fill(img, (hf[0] + 1, hf[1] + 4, 12, 4), (16, 12, 24, 255))
    for eye_rect in ((hf[0] + 2, hf[1] + 5, 4, 2), (hf[0] + 8, hf[1] + 5, 4, 2)):
        fill(img, eye_rect, bright_teal[1])
        fill(glow, eye_rect, bright_teal[0])
    put(img, hf[0] + 4, hf[1] + 5, (255, 255, 255, 255))
    put(img, hf[0] + 9, hf[1] + 5, (255, 255, 255, 255))
    put(glow, hf[0] + 4, hf[1] + 5, (255, 255, 255, 255))
    put(glow, hf[0] + 9, hf[1] + 5, (255, 255, 255, 255))

    # Paint all glowing crystal clusters (base + glowmask)
    paint_box_both(img, glow, *uv_chest_outer, 8, 10, 3, violet, violet)
    paint_box_both(img, glow, *uv_chest_inner, 4, 6, 2, bright_teal, bright_teal)
    paint_box_both(img, glow, *uv_spire_tall_l, 5, 9, 6, teal, teal)
    paint_box_both(img, glow, *uv_spire_tall_r, 5, 9, 6, teal, teal)
    paint_box_both(img, glow, *uv_spire_side_l, 4, 7, 5, violet, violet)
    paint_box_both(img, glow, *uv_spire_side_r, 4, 7, 5, violet, violet)
    paint_box_both(img, glow, *uv_crown_center, 4, 7, 4, bright_teal, bright_teal)
    paint_box_both(img, glow, *uv_crown_horn_l, 3, 5, 3, violet, violet)
    paint_box_both(img, glow, *uv_crown_horn_r, 3, 5, 3, violet, violet)
    paint_box_both(img, glow, *uv_back_spine_big, 4, 8, 5, teal, teal)
    paint_box_both(img, glow, *uv_back_spine_sm, 4, 6, 4, violet, violet)
    paint_box_both(img, glow, *uv_knuckle, 6, 4, 2, bright_teal, bright_teal)
    for idx, s_uv in enumerate(uv_shards):
        pal = bright_teal if idx % 2 == 0 else violet
        paint_box_both(img, glow, *s_uv, 3, 7, 3, pal, pal)

    # Dimensions & pivots (16 px = 1 block; total height ~64 px with crown)
    leg_top = 16.0
    torso_top = 44.0
    arm_x = 17.0
    bones = [
        {"name": "root", "pivot": [0, 0, 0]},
        {
            "name": "body",
            "parent": "root",
            "pivot": [0, 30, 0],
            "cubes": [
                {"origin": [-12, leg_top, -7], "size": [24, 28, 14], "uv": list(uv_torso)},
                {"origin": [-4, 26, -9.5], "size": [8, 10, 3], "uv": list(uv_chest_outer)},
                {"origin": [-2, 28, -10.5], "size": [4, 6, 2], "uv": list(uv_chest_inner)},
                {"origin": [-2, 36, 6.5], "size": [4, 8, 5], "uv": list(uv_back_spine_big)},
                {"origin": [-2, 27, 6.5], "size": [4, 6, 4], "uv": list(uv_back_spine_sm)},
            ],
        },
        {
            "name": "head",
            "parent": "body",
            "pivot": [0, torso_top, 0],
            "cubes": [
                {"origin": [-7, torso_top, -7], "size": [14, 13, 14], "uv": list(uv_head)},
                {"origin": [-2, torso_top + 13, -2], "size": [4, 7, 4], "uv": list(uv_crown_center)},
                {"origin": [4, torso_top + 12, -1.5], "size": [3, 5, 3], "uv": list(uv_crown_horn_l)},
                {"origin": [-7, torso_top + 12, -1.5], "size": [3, 5, 3], "uv": list(uv_crown_horn_r)},
            ],
        },
        {
            "name": "shoulder_left",
            "parent": "body",
            "pivot": [16, torso_top - 1, 0],
            "cubes": [
                {"origin": [11, torso_top - 6, -7], "size": [10, 8, 14], "uv": list(uv_pauldron_l)},
                {"origin": [13.5, torso_top + 2, -3], "size": [5, 9, 6], "uv": list(uv_spire_tall_l)},
                {"origin": [19, torso_top - 2, -2.5], "size": [4, 7, 5], "uv": list(uv_spire_side_l)},
            ],
        },
        {
            "name": "shoulder_right",
            "parent": "body",
            "pivot": [-16, torso_top - 1, 0],
            "cubes": [
                {"origin": [-21, torso_top - 6, -7], "size": [10, 8, 14], "uv": list(uv_pauldron_r)},
                {"origin": [-18.5, torso_top + 2, -3], "size": [5, 9, 6], "uv": list(uv_spire_tall_r)},
                {"origin": [-23, torso_top - 2, -2.5], "size": [4, 7, 5], "uv": list(uv_spire_side_r)},
            ],
        },
        {
            "name": "arm_left",
            "parent": "body",
            "pivot": [arm_x, torso_top - 4, 0],
            "cubes": [
                {"origin": [arm_x - 3, torso_top - 16, -3], "size": [6, 12, 6], "uv": list(uv_upper_arm_l)},
                {"origin": [arm_x - 4, torso_top - 30, -4], "size": [8, 14, 8], "uv": list(uv_fist_l)},
                {"origin": [arm_x - 3, torso_top - 29, -5.5], "size": [6, 4, 2], "uv": list(uv_knuckle)},
            ],
        },
        {
            "name": "arm_right",
            "parent": "body",
            "pivot": [-arm_x, torso_top - 4, 0],
            "cubes": [
                {"origin": [-arm_x - 3, torso_top - 16, -3], "size": [6, 12, 6], "uv": list(uv_upper_arm_r)},
                {"origin": [-arm_x - 4, torso_top - 30, -4], "size": [8, 14, 8], "uv": list(uv_fist_r)},
                {"origin": [-arm_x - 3, torso_top - 29, -5.5], "size": [6, 4, 2], "uv": list(uv_knuckle)},
            ],
        },
        {
            "name": "leg_left",
            "parent": "root",
            "pivot": [6.5, leg_top, 0],
            "cubes": [
                {"origin": [2, 0, -5], "size": [9, 16, 10], "uv": list(uv_leg_l)},
                {"origin": [3, 7, -6], "size": [7, 5, 2], "uv": list(uv_kneecap)},
            ],
        },
        {
            "name": "leg_right",
            "parent": "root",
            "pivot": [-6.5, leg_top, 0],
            "cubes": [
                {"origin": [-11, 0, -5], "size": [9, 16, 10], "uv": list(uv_leg_r)},
                {"origin": [-10, 7, -6], "size": [7, 5, 2], "uv": list(uv_kneecap)},
            ],
        },
        {
            "name": "floating_shards",
            "parent": "body",
            "pivot": [0, 42, 0],
            "cubes": [
                {"origin": [15, 48, 10], "size": [3, 7, 3], "uv": list(uv_shards[0])},
                {"origin": [-18, 48, 10], "size": [3, 7, 3], "uv": list(uv_shards[1])},
                {"origin": [12, 34, -12], "size": [3, 7, 3], "uv": list(uv_shards[2])},
                {"origin": [-15, 34, -12], "size": [3, 7, 3], "uv": list(uv_shards[3])},
            ],
        },
    ]

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": f"geometry.{eid}",
                    "texture_width": 256,
                    "texture_height": 128,
                    "visible_bounds_width": 4.2,
                    "visible_bounds_height": 4.4,
                    "visible_bounds_offset": [0, 2.0, 0],
                },
                "bones": bones,
            }
        ],
    }

    (ASSETS / "textures/entity").mkdir(parents=True, exist_ok=True)
    (ASSETS / "geo/entity").mkdir(parents=True, exist_ok=True)
    (ASSETS / "animations/entity").mkdir(parents=True, exist_ok=True)

    img.save(ASSETS / f"textures/entity/{eid}.png")
    glow.save(ASSETS / f"textures/entity/{eid}_glowmask.png")
    (ASSETS / f"geo/entity/{eid}.geo.json").write_text(json.dumps(geo, indent=2) + "\n", encoding="utf-8")
    (ASSETS / f"animations/entity/{eid}.animation.json").write_text(
        json.dumps(make_colossus_animations(), indent=2) + "\n", encoding="utf-8"
    )


def build_colossus_heart():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    # Faceted heart silhouette in arcane slate + Sunsteel gold frame around glowing teal/violet crystal
    frame = (43, 35, 56, 255)
    gold = (255, 180, 72, 255)
    teal = (56, 217, 192, 255)
    bright_teal = (156, 255, 248, 255)
    violet = (139, 124, 255, 255)

    rows = [
        (3, range(3, 7)), (3, range(9, 13)),
        (4, range(2, 8)), (4, range(8, 14)),
        (5, range(2, 14)),
        (6, range(2, 14)),
        (7, range(3, 13)),
        (8, range(3, 13)),
        (9, range(4, 12)),
        (10, range(5, 11)),
        (11, range(6, 10)),
        (12, range(7, 9)),
    ]
    inside = {(x, y) for y, xs in rows for x in xs}
    for x, y in inside:
        is_border = any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if is_border:
            put(img, x, y, gold if (x + y) % 3 == 0 else frame)
        elif x < 8 and y < 8:
            put(img, x, y, bright_teal)
        elif x >= 8 and y >= 7:
            put(img, x, y, violet)
        else:
            put(img, x, y, teal)
    fill(img, (6, 6, 3, 3), (220, 255, 255, 255))
    put(img, 5, 5, (255, 255, 255, 255))
    (ASSETS / "textures/item").mkdir(parents=True, exist_ok=True)
    img.save(ASSETS / "textures/item/colossus_heart.png")


def build_prism_core():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    frame = (58, 47, 77, 255)
    gold = (255, 196, 88, 255)
    cyan = (96, 242, 232, 255)
    violet = (162, 136, 255, 255)
    magenta = (224, 118, 246, 255)

    # Diamond star-crystal silhouette
    for y in range(2, 14):
        half = 6 - abs(y - 7) if y <= 7 else 6 - (y - 7)
        if half < 1:
            half = 1
        for x in range(8 - half, 8 + half):
            if x == 8 - half or x == 8 + half - 1 or y in (2, 13):
                put(img, x, y, gold if (x == 7 or x == 8 or y == 7) else frame)
            elif x < 8 and y < 8:
                put(img, x, y, cyan)
            elif x >= 8 and y < 8:
                put(img, x, y, violet)
            elif x < 8 and y >= 8:
                put(img, x, y, violet)
            else:
                put(img, x, y, magenta)
    # Cardinal star rays
    for rx, ry in ((7, 1), (8, 1), (7, 14), (8, 14), (1, 7), (1, 8), (14, 7), (14, 8)):
        put(img, rx, ry, gold)
    fill(img, (6, 6, 4, 4), (235, 255, 255, 255))
    fill(img, (7, 7, 2, 2), (255, 255, 255, 255))
    (ASSETS / "textures/item").mkdir(parents=True, exist_ok=True)
    img.save(ASSETS / "textures/item/prism_core.png")


def main():
    build_training_golem()
    build_boss_altar()
    build_golem_core()
    build_crystal_colossus()
    build_colossus_heart()
    build_prism_core()
    print("Generated boss art: training_golem, boss_altar, golem_core, crystal_colossus, colossus_heart, prism_core.")


if __name__ == "__main__":
    main()
