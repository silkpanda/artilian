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
                        "position": {"0.0": [0, 0, 0], "1.2": [0, 0.7, 0], "2.4": [0, 0, 0]},
                        "rotation": {"0.0": [3, 0, 0], "1.2": [5, 0, 0], "2.4": [3, 0, 0]},
                    },
                    "head": {
                        "rotation": {"0.0": [2, -4, 0], "1.2": [-2, 4, 0], "2.4": [2, -4, 0]}
                    },
                    "shoulder_left": {
                        "rotation": {"0.0": [0, 0, 0], "1.2": [0, 0, -4], "2.4": [0, 0, 0]}
                    },
                    "shoulder_right": {
                        "rotation": {"0.0": [0, 0, 0], "1.2": [0, 0, 4], "2.4": [0, 0, 0]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [4, 0, 5], "1.2": [-4, 0, 9], "2.4": [4, 0, 5]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [-4, 0, -5], "1.2": [4, 0, -9], "2.4": [-4, 0, -5]}
                    },
                    "floating_shards": {
                        "position": {"0.0": [0, 0, 0], "1.2": [0, 1.8, 0], "2.4": [0, 0, 0]},
                        "rotation": {"0.0": [0, 0, 0], "2.4": [0, 360, 0]},
                    },
                },
            },
            f"{p}.walk": {
                "loop": True,
                "animation_length": 1.0,
                "bones": {
                    "body": {
                        "position": {"0.0": [0, 0, 0], "0.25": [0, 1.0, 0], "0.5": [0, 0, 0], "0.75": [0, 1.0, 0], "1.0": [0, 0, 0]},
                        "rotation": {"0.0": [6, -3, -3], "0.5": [6, 3, 3], "1.0": [6, -3, -3]},
                    },
                    "head": {
                        "rotation": {"0.0": [-2, 3, 0], "0.5": [-2, -3, 0], "1.0": [-2, 3, 0]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [26, 0, 6], "0.5": [-26, 0, 6], "1.0": [26, 0, 6]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [-26, 0, -6], "0.5": [26, 0, -6], "1.0": [-26, 0, -6]}
                    },
                    "leg_left": {
                        "rotation": {"0.0": [-24, 0, 0], "0.5": [24, 0, 0], "1.0": [-24, 0, 0]}
                    },
                    "leg_right": {
                        "rotation": {"0.0": [24, 0, 0], "0.5": [-24, 0, 0], "1.0": [24, 0, 0]}
                    },
                    "floating_shards": {
                        "rotation": {"0.0": [0, 0, 0], "1.0": [0, 180, 0]},
                    },
                },
            },
            f"{p}.attack": {
                "animation_length": 0.6,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [4, 0, 0], "0.22": [-16, 0, 0], "0.4": [22, 0, 0], "0.6": [4, 0, 0]}
                    },
                    "head": {
                        "rotation": {"0.0": [0, 0, 0], "0.22": [-10, 0, 0], "0.4": [14, 0, 0], "0.6": [0, 0, 0]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 5], "0.22": [-125, -10, 14], "0.4": [34, 8, 4], "0.6": [0, 0, 5]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, -5], "0.22": [-125, 10, -14], "0.4": [34, -8, -4], "0.6": [0, 0, -5]}
                    },
                    "floating_shards": {
                        "rotation": {"0.0": [0, 0, 0], "0.6": [0, 180, 0]}
                    },
                },
            },
            f"{p}.telegraph": {
                "loop": False,
                "animation_length": 2.0,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [4, 0, 0], "1.4": [-12, 0, 0], "2.0": [-16, 0, 0]}
                    },
                    "head": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [-14, 0, 0]}
                    },
                    "shoulder_left": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, 0, -12]}
                    },
                    "shoulder_right": {
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, 0, 12]}
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 6], "2.0": [-135, -8, 18]}
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, -6], "2.0": [-135, 8, -18]}
                    },
                    "floating_shards": {
                        "position": {"0.0": [0, 0, 0], "2.0": [0, 4.5, 0]},
                        "rotation": {"0.0": [0, 0, 0], "2.0": [0, 720, 0]},
                    },
                },
            },
        },
    }


def _h(x, y, seed=0):
    """Deterministic pixel hash in [0, 255] for stone/crystal pixel-art dithering."""
    n = (x * 374761393 + y * 668265263 + seed * 1442695041) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFF)


def _lerp_color(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return (
        int(round(c1[0] + (c2[0] - c1[0]) * t)),
        int(round(c1[1] + (c2[1] - c1[1]) * t)),
        int(round(c1[2] + (c2[2] - c1[2]) * t)),
        255,
    )


def build_crystal_colossus():
    eid = "crystal_colossus"
    img = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (256, 128), (0, 0, 0, 0))

    # Rich 8-stop Warden/Iron-Golem inspired Arcane Slate + Starlight Teal + Amethyst Violet + Sunsteel Gold palette
    VOID_DEEP = (11, 8, 19, 255)
    SLATE_SEAM = (19, 14, 30, 255)
    SLATE_DARK = (30, 23, 45, 255)
    SLATE_BASE = (44, 35, 64, 255)
    SLATE_MID = (60, 48, 86, 255)
    SLATE_HI = (82, 67, 114, 255)
    SLATE_RIM = (106, 89, 142, 255)

    GOLD_DEEP = (120, 62, 16, 255)
    GOLD_MID = (198, 114, 30, 255)
    GOLD_BRIGHT = (255, 174, 56, 255)
    GOLD_SPEC = (255, 232, 148, 255)

    TEAL_ROOT = (20, 74, 82, 255)
    TEAL_DEEP = (26, 126, 128, 255)
    TEAL_MID = (48, 192, 178, 255)
    TEAL_BRIGHT = (88, 238, 224, 255)
    CYAN_HOT = (158, 252, 255, 255)
    WHITE_CYAN = (236, 255, 255, 255)

    VIOLET_ROOT = (44, 26, 88, 255)
    VIOLET_DEEP = (76, 48, 148, 255)
    VIOLET_MID = (124, 90, 224, 255)
    VIOLET_BRIGHT = (172, 140, 255, 255)
    LAVENDER_HOT = (226, 206, 255, 255)

    RIB_IVORY = (196, 234, 232, 255)
    RIB_MID = (118, 178, 184, 255)
    RIB_SHADE = (62, 108, 122, 255)

    # Non-overlapping UV box definitions: (u, v, w, h, d)
    CHEST_UPPER = (0, 0, 22, 18, 14)       # 72x32
    WAIST = (74, 0, 14, 8, 10)             # 48x18
    BELT = (124, 0, 16, 3, 11)             # 54x14
    COLLAR = (180, 0, 16, 4, 11)           # 54x15
    TASSET = (236, 0, 6, 6, 2)             # 16x8
    RIB_PILLAR_L = (74, 19, 4, 12, 2)      # 12x14
    RIB_PILLAR_R = (88, 19, 4, 12, 2)      # 12x14
    RIB_BAR = (102, 19, 6, 2, 2)           # 16x4
    HEART_CORE = (124, 16, 8, 10, 4)       # 24x14
    HEART_INNER = (150, 16, 5, 7, 3)       # 16x10
    PAULDRON_RIM_L = (190, 16, 11, 4, 14)  # 50x18

    HEAD_SKULL = (0, 34, 13, 11, 12)       # 50x23
    HEAD_BROW = (52, 34, 15, 3, 5)         # 40x8
    HEAD_JAW = (52, 43, 11, 4, 9)          # 40x13
    PAULDRON_MAIN_L = (94, 34, 10, 9, 13)  # 46x22
    PAULDRON_MAIN_R = (142, 34, 10, 9, 13) # 46x22
    PAULDRON_RIM_R = (190, 36, 11, 4, 14)  # 50x18

    ARM_UPPER_L = (0, 58, 6, 12, 7)        # 26x19
    ARM_UPPER_R = (28, 58, 6, 12, 7)       # 26x19
    FOREARM_L = (56, 58, 9, 15, 9)         # 36x24
    FOREARM_R = (94, 58, 9, 15, 9)         # 36x24
    THIGH_L = (132, 58, 7, 8, 7)           # 28x15
    THIGH_R = (162, 58, 7, 8, 7)           # 28x15
    GREAVE_L = (192, 56, 9, 10, 9)         # 36x19
    GREAVE_R = (192, 76, 9, 10, 9)         # 36x19
    TOE_CAP = (230, 56, 9, 4, 3)           # 24x7
    KNEE_SPUR = (230, 66, 4, 7, 4)         # 16x11

    SPIRE_TALL_TEAL = (0, 84, 5, 12, 5)    # 20x17
    SPIRE_TALL_VIOLET = (22, 84, 5, 11, 5) # 20x16
    SPIRE_MED_TEAL = (44, 84, 4, 9, 4)     # 16x13
    SPIRE_MED_VIOLET = (62, 84, 4, 9, 4)   # 16x13
    SPIRE_SM_TEAL = (80, 84, 3, 7, 3)      # 12x10
    SPIRE_SM_VIOLET = (94, 84, 3, 7, 3)    # 12x10
    HORN_BASE = (108, 84, 4, 6, 4)         # 16x10
    HORN_TIP = (126, 84, 3, 9, 3)          # 12x12
    CROWN_CREST = (140, 84, 4, 8, 5)       # 18x13
    FOREARM_BLADE = (160, 84, 3, 9, 4)     # 14x13
    CLAW_FINGER = (176, 84, 3, 6, 3)       # 12x9

    SHARD_TEAL = (0, 104, 4, 9, 4)         # 16x13
    SHARD_VIOLET = (18, 104, 4, 9, 4)      # 16x13
    SHARD_CAP_TEAL = (36, 104, 3, 5, 3)    # 12x8
    SHARD_CAP_VIOLET = (50, 104, 3, 5, 3)  # 12x8

    all_uv_boxes = [
        CHEST_UPPER, WAIST, BELT, COLLAR, TASSET, RIB_PILLAR_L, RIB_PILLAR_R, RIB_BAR,
        HEART_CORE, HEART_INNER, PAULDRON_RIM_L, HEAD_SKULL, HEAD_BROW, HEAD_JAW,
        PAULDRON_MAIN_L, PAULDRON_MAIN_R, PAULDRON_RIM_R, ARM_UPPER_L, ARM_UPPER_R,
        FOREARM_L, FOREARM_R, THIGH_L, THIGH_R, GREAVE_L, GREAVE_R, TOE_CAP, KNEE_SPUR,
        SPIRE_TALL_TEAL, SPIRE_TALL_VIOLET, SPIRE_MED_TEAL, SPIRE_MED_VIOLET,
        SPIRE_SM_TEAL, SPIRE_SM_VIOLET, HORN_BASE, HORN_TIP, CROWN_CREST,
        FOREARM_BLADE, CLAW_FINGER, SHARD_TEAL, SHARD_VIOLET, SHARD_CAP_TEAL, SHARD_CAP_VIOLET,
    ]
    occupied = set()
    for u, v, w, h, d in all_uv_boxes:
        uw, uh = 2 * (d + w), d + h
        assert u + uw <= 256 and v + uh <= 128, f"UV box {(u, v, w, h, d)} out of 256x128 bounds"
        for px in range(u, u + uw):
            for py in range(v, v + uh):
                assert (px, py) not in occupied, f"UV overlap at {(px, py)} in box {(u, v, w, h, d)}"
                occupied.add((px, py))

    def paint_stone_armor_box(box, seed=1, gold_border=False, crystal_ridges=False, spore_density=16):
        """Warden & Iron-Golem style textured stone/slate armor with strata, bevels, cracks, rivets, and glowing pores."""
        u, v, w, h, d = box
        for face, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
            for dy in range(fh):
                py = fy + dy
                ty = dy / max(1, fh - 1)
                for dx in range(fw):
                    px = fx + dx
                    tx = dx / max(1, fw - 1)
                    hval = _h(px, py, seed)
                    strata = ((dy * 3 + (dx // 2)) % 5 == 0)

                    # Base directional lighting + dithered stone texture
                    if face == "top":
                        base = SLATE_HI if hval > 110 else (SLATE_RIM if hval > 210 else SLATE_MID)
                    elif face == "bottom":
                        base = VOID_DEEP if hval < 120 else SLATE_SEAM
                    elif face in ("east", "west"):
                        shade_t = 0.25 + 0.55 * ty
                        base = _lerp_color(SLATE_MID, SLATE_DARK, shade_t)
                        if hval < 55 or strata:
                            base = _lerp_color(base, SLATE_SEAM, 0.45)
                        elif hval > 205:
                            base = _lerp_color(base, SLATE_HI, 0.35)
                    else:
                        shade_t = 0.15 + 0.65 * ty
                        base = _lerp_color(SLATE_HI, SLATE_DARK, shade_t)
                        if strata and hval < 160:
                            base = _lerp_color(base, SLATE_SEAM, 0.4)
                        elif hval < 48:
                            base = _lerp_color(base, SLATE_SEAM, 0.55)
                        elif hval > 195:
                            base = _lerp_color(base, SLATE_RIM, 0.35)

                    # Organic stone edge shading (no artificial bright box borders)
                    is_outer_edge = (dx == 0 or dx == fw - 1 or dy == 0 or dy == fh - 1)
                    is_inner_bevel = (dx == 1 or dx == fw - 2 or dy == 1 or dy == fh - 2)
                    if fw >= 4 and fh >= 4 and is_outer_edge:
                        if dy == 0 and face != "bottom":
                            base = _lerp_color(base, SLATE_HI, 0.45)
                        elif dy == fh - 1:
                            base = _lerp_color(base, VOID_DEEP, 0.55)
                        else:
                            base = _lerp_color(base, SLATE_SEAM, 0.5)
                    elif fw >= 6 and fh >= 6 and is_inner_bevel:
                        if dy == 1 or dx == 1:
                            base = _lerp_color(base, SLATE_HI, 0.25)
                        elif dy == fh - 2 or dx == fw - 2:
                            base = _lerp_color(base, SLATE_DARK, 0.35)

                    # Subtle weathered stone cracks & moss/crystal crevices
                    crack_pattern = ((dx * 5 + dy * 7 + seed) % 19 == 0) or ((dx * 11 - dy * 3 + seed) % 23 == 0)
                    if not is_outer_edge and crack_pattern and hval < 115:
                        base = SLATE_SEAM if hval < 65 else TEAL_ROOT

                    put(img, px, py, base)

                    # Bioluminescent sculk-like starlight pores & runic vein tendrils on non-edge pixels
                    if not is_outer_edge and face not in ("top", "bottom"):
                        pore_hash = _h(px * 3, py * 5, seed + 77)
                        if pore_hash < spore_density:
                            pore_col = TEAL_BRIGHT if (pore_hash % 3 != 0) else VIOLET_BRIGHT
                            put(img, px, py, pore_col)
                            put(glow, px, py, pore_col)

            # Warden-inspired jagged vertical crystal-bone growth ridges rising from the bottom of gauntlets/greaves
            if crystal_ridges and face in ("front", "back", "east", "west") and fw >= 5 and fh >= 8:
                for dx in range(1, fw - 1):
                    ridge_h = 3 + (_h(dx, fy, seed + 31) % (fh // 2 + 1))
                    if dx % 2 == 1:
                        for rdy in range(ridge_h):
                            py = fy + fh - 2 - rdy
                            rt = rdy / max(1, ridge_h - 1)
                            col = _lerp_color(TEAL_DEEP, WHITE_CYAN if dx % 4 == 1 else LAVENDER_HOT, rt)
                            put(img, fx + dx, py, col)
                            if rt > 0.35:
                                put(glow, fx + dx, py, col)

    def paint_crystal_spire_box(box, palette="teal", seed=11):
        """Faceted crystal prism with diagonal facet planes, specular edge highlights, and dark stone-crusted roots."""
        u, v, w, h, d = box
        if palette == "teal":
            c_root, c_deep, c_mid, c_hi, c_spec = TEAL_ROOT, TEAL_DEEP, TEAL_MID, TEAL_BRIGHT, WHITE_CYAN
        elif palette == "violet":
            c_root, c_deep, c_mid, c_hi, c_spec = VIOLET_ROOT, VIOLET_DEEP, VIOLET_MID, VIOLET_BRIGHT, LAVENDER_HOT
        else:
            c_root, c_deep, c_mid, c_hi, c_spec = RIB_SHADE, RIB_MID, RIB_IVORY, WHITE_CYAN, WHITE_CYAN

        for face, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
            for dy in range(fh):
                py = fy + dy
                # ty=0 is tip (top), ty=1 is root (bottom)
                ty = dy / max(1, fh - 1)
                for dx in range(fw):
                    px = fx + dx
                    tx = dx / max(1, fw - 1)
                    facet = ((dx + dy) % 4)
                    hval = _h(px, py, seed)

                    if face == "top":
                        col = c_spec if (dx == 0 or dy == 0 or hval > 150) else c_hi
                    elif face == "bottom":
                        col = SLATE_DARK
                    else:
                        # Vertical gradient from glowing tip (ty=0) to stone-crusted root (ty=1)
                        if ty < 0.22:
                            col = _lerp_color(c_spec, c_hi, ty / 0.22)
                        elif ty < 0.68:
                            col = _lerp_color(c_hi, c_mid, (ty - 0.22) / 0.46)
                        elif ty < 0.88:
                            col = _lerp_color(c_mid, c_deep, (ty - 0.68) / 0.20)
                        else:
                            col = _lerp_color(c_root, SLATE_DARK, (ty - 0.88) / 0.12)

                        # Diagonal crystal facet planes & specular edge ridges
                        if dx == 0 or (dx == fw // 2 and dy < fh - 2):
                            col = _lerp_color(col, c_spec, 0.65 if ty < 0.75 else 0.25)
                        elif dx == fw - 1:
                            col = _lerp_color(col, c_root, 0.55)
                        elif facet == 0 and ty < 0.8:
                            col = _lerp_color(col, c_spec, 0.45)
                        elif facet == 2:
                            col = _lerp_color(col, c_deep, 0.35)

                    put(img, px, py, col)
                    if face != "bottom" and ty < 0.85:
                        put(glow, px, py, col)

    # 1. Paint all stone armor boxes with rich weathered strata & pores
    paint_stone_armor_box(CHEST_UPPER, seed=101, gold_border=True, spore_density=14)
    paint_stone_armor_box(WAIST, seed=102, gold_border=False, spore_density=12)
    paint_stone_armor_box(BELT, seed=103, gold_border=True, spore_density=8)
    paint_stone_armor_box(COLLAR, seed=104, gold_border=True, spore_density=10)
    paint_stone_armor_box(TASSET, seed=105, gold_border=True, spore_density=10)
    paint_stone_armor_box(HEAD_SKULL, seed=106, gold_border=True, spore_density=12)
    paint_stone_armor_box(HEAD_BROW, seed=107, gold_border=True, spore_density=6)
    paint_stone_armor_box(HEAD_JAW, seed=108, gold_border=False, spore_density=10)
    paint_stone_armor_box(PAULDRON_MAIN_L, seed=109, gold_border=True, crystal_ridges=True, spore_density=16)
    paint_stone_armor_box(PAULDRON_MAIN_R, seed=110, gold_border=True, crystal_ridges=True, spore_density=16)
    paint_stone_armor_box(PAULDRON_RIM_L, seed=111, gold_border=True, spore_density=10)
    paint_stone_armor_box(PAULDRON_RIM_R, seed=112, gold_border=True, spore_density=10)
    paint_stone_armor_box(ARM_UPPER_L, seed=113, gold_border=False, spore_density=14)
    paint_stone_armor_box(ARM_UPPER_R, seed=114, gold_border=False, spore_density=14)
    paint_stone_armor_box(FOREARM_L, seed=115, gold_border=True, crystal_ridges=True, spore_density=18)
    paint_stone_armor_box(FOREARM_R, seed=116, gold_border=True, crystal_ridges=True, spore_density=18)
    paint_stone_armor_box(THIGH_L, seed=117, gold_border=False, spore_density=12)
    paint_stone_armor_box(THIGH_R, seed=118, gold_border=False, spore_density=12)
    paint_stone_armor_box(GREAVE_L, seed=119, gold_border=True, crystal_ridges=True, spore_density=16)
    paint_stone_armor_box(GREAVE_R, seed=120, gold_border=True, crystal_ridges=True, spore_density=16)
    paint_stone_armor_box(TOE_CAP, seed=121, gold_border=True, spore_density=8)

    # 2. Paint ribcage bone-crystal arches & glowing heart core boxes
    paint_crystal_spire_box(RIB_PILLAR_L, palette="rib", seed=201)
    paint_crystal_spire_box(RIB_PILLAR_R, palette="rib", seed=202)
    paint_crystal_spire_box(RIB_BAR, palette="rib", seed=203)
    paint_crystal_spire_box(HEART_CORE, palette="violet", seed=204)
    paint_crystal_spire_box(HEART_INNER, palette="teal", seed=205)

    # 3. Paint all faceted crystal spires, horns, claws, knee spurs, and floating obelisks
    paint_crystal_spire_box(SPIRE_TALL_TEAL, palette="teal", seed=301)
    paint_crystal_spire_box(SPIRE_TALL_VIOLET, palette="violet", seed=302)
    paint_crystal_spire_box(SPIRE_MED_TEAL, palette="teal", seed=303)
    paint_crystal_spire_box(SPIRE_MED_VIOLET, palette="violet", seed=304)
    paint_crystal_spire_box(SPIRE_SM_TEAL, palette="teal", seed=305)
    paint_crystal_spire_box(SPIRE_SM_VIOLET, palette="violet", seed=306)
    paint_crystal_spire_box(HORN_BASE, palette="violet", seed=307)
    paint_crystal_spire_box(HORN_TIP, palette="teal", seed=308)
    paint_crystal_spire_box(CROWN_CREST, palette="teal", seed=309)
    paint_crystal_spire_box(FOREARM_BLADE, palette="teal", seed=310)
    paint_crystal_spire_box(CLAW_FINGER, palette="teal", seed=311)
    paint_crystal_spire_box(KNEE_SPUR, palette="violet", seed=312)
    paint_crystal_spire_box(SHARD_TEAL, palette="teal", seed=313)
    paint_crystal_spire_box(SHARD_VIOLET, palette="violet", seed=314)
    paint_crystal_spire_box(SHARD_CAP_TEAL, palette="teal", seed=315)
    paint_crystal_spire_box(SHARD_CAP_VIOLET, palette="violet", seed=316)

    # 4. Hand-detailed Warden-style hollowed ribcage cavity, pulsing Soul-Crystal Heart & branching veins on CHEST_UPPER
    cf = faces(*CHEST_UPPER)
    fx, fy, fw, fh = cf["front"]  # 22x18
    # Deep recessed void cavity in center of chest (x: 4..17, y: 2..15)
    for dy in range(2, 16):
        for dx in range(3, 19):
            dist = ((dx - 10.5) ** 2 / 46.0 + (dy - 8.5) ** 2 / 44.0) ** 0.5
            if dist <= 1.0:
                col = VOID_DEEP if dist < 0.78 else SLATE_SEAM
                put(img, fx + dx, fy + dy, col)
                # Glowing starlight soul-heart & concentric energy aura deep inside the ribcage cavity
                if dist <= 0.24:
                    put(img, fx + dx, fy + dy, WHITE_CYAN)
                    put(glow, fx + dx, fy + dy, WHITE_CYAN)
                elif dist <= 0.42:
                    put(img, fx + dx, fy + dy, CYAN_HOT)
                    put(glow, fx + dx, fy + dy, CYAN_HOT)
                elif 0.54 <= dist <= 0.68:
                    rcol = VIOLET_BRIGHT if (dx + dy) % 2 == 0 else TEAL_BRIGHT
                    put(img, fx + dx, fy + dy, rcol)
                    put(glow, fx + dx, fy + dy, rcol)
    # Sculpted curved pale-teal/ivory crystal ribs arching across the chest cavity (like warden.png)
    for rib_idx, rib_y in enumerate((3, 6, 9, 12, 14)):
        rib_span = (3, 4, 5, 6, 7, 8, 13, 14, 15, 16, 17, 18) if rib_idx in (1, 2, 3) else (4, 5, 6, 7, 14, 15, 16, 17)
        for side_dx in rib_span:
            inward = min(abs(side_dx - 3), abs(18 - side_dx))
            curve_y = rib_y + (1 if inward >= 3 else 0)
            rcol = WHITE_CYAN if inward >= 4 else (RIB_IVORY if inward >= 2 else RIB_MID)
            put(img, fx + side_dx, fy + curve_y, rcol)
            put(img, fx + side_dx, fy + curve_y + 1, RIB_SHADE)
            if inward >= 3:
                put(glow, fx + side_dx, fy + curve_y, rcol)
    # Organic branching bioluminescent veins radiating from the ribcage to the shoulders & flanks
    vein_coords = [
        (1, 2), (2, 2), (3, 3), (2, 4), (1, 5), (2, 8), (1, 9), (2, 12), (3, 13), (1, 14),
        (20, 2), (19, 2), (18, 3), (19, 4), (20, 5), (19, 8), (20, 9), (19, 12), (18, 13), (20, 14),
        (10, 1), (11, 1), (10, 16), (11, 16),
    ]
    for vx, vy in vein_coords:
        vcol = TEAL_BRIGHT if (vx + vy) % 2 == 0 else VIOLET_BRIGHT
        put(img, fx + vx, fy + vy, vcol)
        put(glow, fx + vx, fy + vy, vcol)

    # 5. Warden-style glowing segmented dorsal spine on CHEST_UPPER back
    bx, by, bw, bh = cf["back"]  # 22x18
    for dy in range(1, bh - 1):
        spine_w = 5 - (dy // 5)
        for dx in range(bw // 2 - spine_w, bw // 2 + spine_w):
            d_center = abs(dx - (bw // 2 - 0.5))
            if dy % 3 in (0, 1):
                scol = WHITE_CYAN if d_center < 1.2 else (TEAL_BRIGHT if d_center < 2.5 else VIOLET_MID)
                put(img, bx + dx, by + dy, scol)
                if d_center < 2.8:
                    put(glow, bx + dx, by + dy, scol)
            else:
                put(img, bx + dx, by + dy, VOID_DEEP)

    # 6. Runic belt buckle & waist vents
    beltx, belty, beltw, belth = faces(*BELT)["front"]
    fill(img, (beltx + beltw // 2 - 3, belty, 6, belth), GOLD_BRIGHT)
    fill(img, (beltx + beltw // 2 - 2, belty + 1, 4, 1), WHITE_CYAN)
    fill(glow, (beltx + beltw // 2 - 2, belty + 1, 4, 1), WHITE_CYAN)

    # 7. Fierce sculpted helmet face, shadowed eye sockets, multi-tone glowing eyes & forehead starlight rune
    hfx, hfy, hfw, hfh = faces(*HEAD_SKULL)["front"]  # 13x11
    # Deep recessed visor cavity & nasal bridge
    fill(img, (hfx + 1, hfy + 3, 11, 5), VOID_DEEP)
    fill(img, (hfx + 5, hfy + 3, 3, 6), SLATE_MID)
    fill(img, (hfx + 6, hfy + 3, 1, 5), SLATE_RIM)
    # Left & right glowing multi-tone eyes (x: 1..4 and 8..11, y: 4..6)
    for ex in (hfx + 1, hfx + 8):
        fill(img, (ex, hfy + 4, 4, 3), TEAL_MID)
        fill(glow, (ex, hfy + 4, 4, 3), TEAL_BRIGHT)
        fill(img, (ex + 1, hfy + 4, 3, 2), CYAN_HOT)
        fill(glow, (ex + 1, hfy + 4, 3, 2), CYAN_HOT)
        put(img, ex + 2, hfy + 4, WHITE_CYAN)
        put(glow, ex + 2, hfy + 4, WHITE_CYAN)
    # Forehead diamond star-rune & cheek respirator vents
    for target in (img, glow):
        put(target, hfx + 6, hfy + 1, LAVENDER_HOT)
        fill(target, (hfx + 5, hfy + 2, 3, 1), VIOLET_BRIGHT)
        put(target, hfx + 6, hfy + 3, WHITE_CYAN)
        put(target, hfx + 2, hfy + 9, TEAL_BRIGHT)
        put(target, hfx + 10, hfy + 9, TEAL_BRIGHT)

    # Jaw tooth-grille bars
    jfx, jfy, jfw, jfh = faces(*HEAD_JAW)["front"]  # 11x4
    for dx in range(1, jfw - 1):
        col = SLATE_RIM if dx % 2 == 1 else VOID_DEEP
        fill(img, (jfx + dx, jfy + 1, 1, 2), col)
        if dx % 2 == 0:
            put(img, jfx + dx, jfy + 2, TEAL_MID)
            put(glow, jfx + dx, jfy + 2, TEAL_MID)

    # 8. Build sculpted V-tapered geometry with outward-flaring crystal horns, pauldrons, spine, and claws
    bones = [
        {"name": "root", "pivot": [0, 0, 0]},
        {
            "name": "body",
            "parent": "root",
            "pivot": [0, 18, 0],
            "cubes": [
                # Narrow armored waist & runic belt
                {"origin": [-7, 16, -5], "size": [14, 8, 10], "uv": [74, 0]},
                {"origin": [-8, 22, -5.5], "size": [16, 3, 11], "uv": [124, 0]},
                {"origin": [-3, 13, -6.2], "size": [6, 6, 2], "pivot": [0, 19, -5.2], "rotation": [-10, 0, 0], "uv": [236, 0]},
                {"origin": [-3, 13, 4.2], "size": [6, 6, 2], "pivot": [0, 19, 5.2], "rotation": [10, 0, 0], "uv": [236, 0]},
                # Massive broad upper chest (with Warden-style painted ribcage cavity) & armored collar/traps
                {"origin": [-11, 24, -7], "size": [22, 18, 14], "uv": [0, 0]},
                {"origin": [-8, 42, -5.5], "size": [16, 4, 11], "uv": [180, 0]},
                # Recessed pulsing diamond Heart Core nested flush in the center of the ribcage
                {"origin": [-2.5, 29.5, -7.8], "size": [5, 7, 3], "pivot": [0, 33.0, -7.0], "rotation": [0, 0, 45], "uv": [150, 16]},
                # Angled dorsal crystal spires erupting down the back spine
                {"origin": [-2.5, 36, 5.5], "size": [5, 12, 5], "pivot": [0, 38, 7], "rotation": [-28, 0, 0], "uv": [0, 84]},
                {"origin": [-2, 30, 5.5], "size": [4, 9, 4], "pivot": [0, 32, 7], "rotation": [-34, 0, 0], "uv": [62, 84]},
                {"origin": [-1.5, 24, 5.0], "size": [3, 7, 3], "pivot": [0, 26, 6.5], "rotation": [-40, 0, 0], "uv": [80, 84]},
            ],
        },
        {
            "name": "head",
            "parent": "body",
            "pivot": [0, 45, -1.5],
            "cubes": [
                # Angular helmet skull, angled brow crest & articulated jaw
                {"origin": [-6.5, 45, -7.5], "size": [13, 11, 12], "uv": [0, 34]},
                {"origin": [-7.5, 53, -8.8], "size": [15, 3, 5], "pivot": [0, 54.5, -7.5], "rotation": [10, 0, 0], "uv": [52, 34]},
                {"origin": [-5.5, 43, -7.0], "size": [11, 4, 9], "uv": [52, 43]},
                # Central angled crystal crown spire
                {"origin": [-2, 55, -4.5], "size": [4, 8, 5], "pivot": [0, 56, -2], "rotation": [-16, 0, 0], "uv": [140, 84]},
                # Outward-flaring Warden-style crystal antler horns (Left +X uses +Z rotation to flare outward)
                {"origin": [5.0, 52, -3.5], "size": [4, 6, 4], "pivot": [6.5, 53, -1.5], "rotation": [-10, 0, 34], "uv": [108, 84]},
                {"origin": [7.5, 56, -3.0], "size": [3, 9, 3], "pivot": [8.5, 56, -1.5], "rotation": [-8, 0, 22], "uv": [126, 84]},
                # Outward-flaring Warden-style crystal antler horns (Right -X uses -Z rotation to flare outward)
                {"origin": [-9.0, 52, -3.5], "size": [4, 6, 4], "pivot": [-6.5, 53, -1.5], "rotation": [-10, 0, -34], "uv": [108, 84]},
                {"origin": [-10.5, 56, -3.0], "size": [3, 9, 3], "pivot": [-8.5, 56, -1.5], "rotation": [-8, 0, -22], "uv": [126, 84]},
            ],
        },
        {
            "name": "shoulder_left",
            "parent": "body",
            "pivot": [13.5, 41, 0],
            "cubes": [
                {"origin": [10.5, 36, -6.5], "size": [10, 9, 13], "pivot": [14, 40, 0], "rotation": [0, 0, 10], "uv": [94, 34]},
                {"origin": [10.0, 33, -7.0], "size": [11, 4, 14], "pivot": [14, 35, 0], "rotation": [0, 0, 14], "uv": [190, 16]},
                # 3 outward-flaring crystal spires erupting from left shoulder (+Z flares outward on +X)
                {"origin": [13.0, 43, -2.5], "size": [5, 12, 5], "pivot": [15, 44, 0], "rotation": [-6, 0, 24], "uv": [0, 84]},
                {"origin": [14.5, 41, 1.5], "size": [4, 9, 4], "pivot": [16.5, 42, 3.5], "rotation": [-18, 0, 36], "uv": [62, 84]},
                {"origin": [17.0, 38, -4.0], "size": [3, 7, 3], "pivot": [18.5, 39, -2.5], "rotation": [12, 0, 46], "uv": [80, 84]},
            ],
        },
        {
            "name": "shoulder_right",
            "parent": "body",
            "pivot": [-13.5, 41, 0],
            "cubes": [
                {"origin": [-20.5, 36, -6.5], "size": [10, 9, 13], "pivot": [-14, 40, 0], "rotation": [0, 0, -10], "uv": [142, 34]},
                {"origin": [-21.0, 33, -7.0], "size": [11, 4, 14], "pivot": [-14, 35, 0], "rotation": [0, 0, -14], "uv": [190, 36]},
                # 3 outward-flaring crystal spires erupting from right shoulder (-Z flares outward on -X)
                {"origin": [-18.0, 43, -2.5], "size": [5, 12, 5], "pivot": [-15, 44, 0], "rotation": [-6, 0, -24], "uv": [22, 84]},
                {"origin": [-18.5, 41, 1.5], "size": [4, 9, 4], "pivot": [-16.5, 42, 3.5], "rotation": [-18, 0, -36], "uv": [44, 84]},
                {"origin": [-20.0, 38, -4.0], "size": [3, 7, 3], "pivot": [-18.5, 39, -2.5], "rotation": [12, 0, -46], "uv": [94, 84]},
            ],
        },
        {
            "name": "arm_left",
            "parent": "body",
            "pivot": [16.0, 38, 0],
            "cubes": [
                # Upper arm + massive low-hanging crystal-bracer forearm (reaches y=7 like Warden/Iron Golem)
                {"origin": [13.0, 26, -3.5], "size": [6, 12, 7], "uv": [0, 58]},
                {"origin": [12.0, 11, -4.5], "size": [9, 15, 9], "uv": [56, 58]},
                # Outward-flaring forearm crystal blades
                {"origin": [19.0, 17, -2.0], "size": [3, 9, 4], "pivot": [20.0, 18, 0], "rotation": [-12, 0, 26], "uv": [160, 84]},
                {"origin": [18.5, 13, 0.5], "size": [3, 7, 3], "pivot": [19.5, 14, 2], "rotation": [-18, 0, 34], "uv": [94, 84]},
                # Articulated crystal claw fingers curving slightly inward
                {"origin": [18.0, 6, -3.8], "size": [3, 6, 3], "pivot": [19.5, 11, -2.3], "rotation": [0, 0, -14], "uv": [176, 84]},
                {"origin": [18.0, 6, 0.8], "size": [3, 6, 3], "pivot": [19.5, 11, 2.3], "rotation": [0, 0, -14], "uv": [176, 84]},
                {"origin": [12.5, 7, -4.2], "size": [3, 5, 3], "pivot": [14.0, 11, -2.7], "rotation": [0, 0, 12], "uv": [176, 84]},
            ],
        },
        {
            "name": "arm_right",
            "parent": "body",
            "pivot": [-16.0, 38, 0],
            "cubes": [
                {"origin": [-19.0, 26, -3.5], "size": [6, 12, 7], "uv": [28, 58]},
                {"origin": [-21.0, 11, -4.5], "size": [9, 15, 9], "uv": [94, 58]},
                # Outward-flaring forearm crystal blades
                {"origin": [-22.0, 17, -2.0], "size": [3, 9, 4], "pivot": [-20.0, 18, 0], "rotation": [-12, 0, -26], "uv": [160, 84]},
                {"origin": [-21.5, 13, 0.5], "size": [3, 7, 3], "pivot": [-19.5, 14, 2], "rotation": [-18, 0, -34], "uv": [80, 84]},
                # Articulated crystal claw fingers curving slightly inward
                {"origin": [-21.0, 6, -3.8], "size": [3, 6, 3], "pivot": [-19.5, 11, -2.3], "rotation": [0, 0, 14], "uv": [176, 84]},
                {"origin": [-21.0, 6, 0.8], "size": [3, 6, 3], "pivot": [-19.5, 11, 2.3], "rotation": [0, 0, 14], "uv": [176, 84]},
                {"origin": [-15.5, 7, -4.2], "size": [3, 5, 3], "pivot": [-14.0, 11, -2.7], "rotation": [0, 0, -12], "uv": [176, 84]},
            ],
        },
        {
            "name": "leg_left",
            "parent": "root",
            "pivot": [6.0, 17, 0],
            "cubes": [
                {"origin": [2.5, 9, -3.5], "size": [7, 8, 7], "uv": [132, 58]},
                {"origin": [1.5, 0, -4.5], "size": [9, 10, 9], "uv": [192, 56]},
                {"origin": [1.5, 0, -6.8], "size": [9, 4, 3], "uv": [230, 56]},
                {"origin": [4.0, 8, -6.2], "size": [4, 7, 4], "pivot": [6.0, 10, -4.5], "rotation": [22, 0, 0], "uv": [230, 66]},
            ],
        },
        {
            "name": "leg_right",
            "parent": "root",
            "pivot": [-6.0, 17, 0],
            "cubes": [
                {"origin": [-9.5, 9, -3.5], "size": [7, 8, 7], "uv": [162, 58]},
                {"origin": [-10.5, 0, -4.5], "size": [9, 10, 9], "uv": [192, 76]},
                {"origin": [-10.5, 0, -6.8], "size": [9, 4, 3], "uv": [230, 56]},
                {"origin": [-8.0, 8, -6.2], "size": [4, 7, 4], "pivot": [-6.0, 10, -4.5], "rotation": [22, 0, 0], "uv": [230, 66]},
            ],
        },
        {
            "name": "floating_shards",
            "parent": "body",
            "pivot": [0, 40, 0],
            "cubes": [
                {"origin": [18, 47, 9], "size": [4, 9, 4], "pivot": [20, 51, 11], "rotation": [-16, 20, -18], "uv": [0, 104]},
                {"origin": [18.5, 54, 9.5], "size": [3, 5, 3], "pivot": [20, 51, 11], "rotation": [-16, 20, -18], "uv": [36, 104]},
                {"origin": [-22, 47, 9], "size": [4, 9, 4], "pivot": [-20, 51, 11], "rotation": [-16, -20, 18], "uv": [18, 104]},
                {"origin": [-21.5, 54, 9.5], "size": [3, 5, 3], "pivot": [-20, 51, 11], "rotation": [-16, -20, 18], "uv": [50, 104]},
                {"origin": [16, 33, -13], "size": [4, 9, 4], "pivot": [18, 37, -11], "rotation": [16, -25, -20], "uv": [18, 104]},
                {"origin": [-20, 33, -13], "size": [4, 9, 4], "pivot": [-18, 37, -11], "rotation": [16, 25, 20], "uv": [0, 104]},
                {"origin": [0, 52, 13], "size": [4, 9, 4], "pivot": [2, 56, 15], "rotation": [-22, 0, 0], "uv": [0, 104]},
                {"origin": [-4, 32, 14], "size": [4, 9, 4], "pivot": [-2, 36, 16], "rotation": [22, 0, 0], "uv": [18, 104]},
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
                    "visible_bounds_width": 4.5,
                    "visible_bounds_height": 4.6,
                    "visible_bounds_offset": [0, 2.1, 0],
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
