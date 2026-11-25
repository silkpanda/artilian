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

from art_lib import assert_uv_layout_clean, paint_faceted_crystal, paint_weathered_stone

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
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))

    # 7-stop weathered mossy-slate ramp (darkest -> brightest)
    stone_ramp = [
        (54, 56, 54, 255),
        (74, 76, 72, 255),
        (94, 96, 90, 255),
        (114, 116, 108, 255),
        (134, 136, 126, 255),
        (154, 156, 144, 255),
        (174, 176, 162, 255),
    ]
    dark_stone_ramp = [
        (38, 40, 42, 255),
        (52, 54, 56, 255),
        (68, 70, 72, 255),
        (84, 86, 86, 255),
        (100, 102, 100, 255),
        (116, 118, 114, 255),
    ]
    amber_ramp = [
        (132, 76, 16, 255),
        (184, 114, 24, 255),
        (228, 164, 40, 255),
        (255, 208, 78, 255),
        (255, 242, 142, 255),
        (255, 252, 210, 255),
    ]
    moss_color = (82, 132, 62, 255)
    crack_color = (42, 44, 42, 255)

    # Non-overlapping UV rectangles on 128x128:
    # Row 0 (y=0..25):
    uv_chest = (0, 0)       # 16x14x10 -> 52x24
    uv_waist = (54, 0)      # 12x6x8   -> 40x14
    uv_core = (96, 0)       # 6x6x2    -> 16x8
    uv_visor = (96, 10)     # 8x3x2    -> 20x5
    # Row 1 (y=26..48):
    uv_head = (0, 26)       # 10x10x10 -> 40x20
    uv_brow = (42, 26)      # 12x3x11  -> 46x14
    uv_pauldron_l = (90, 26)  # 7x5x7 -> 28x12 (x=90..118)
    # Row 2 (y=48..70):
    uv_pauldron_r = (0, 48)   # 7x5x7 -> 28x12
    uv_arm_l = (30, 48)       # 5x8x5 -> 20x13
    uv_arm_r = (52, 48)       # 5x8x5 -> 20x13
    uv_gaunt_l = (74, 48)     # 6x9x6 -> 24x15
    uv_gaunt_r = (100, 48)    # 6x9x6 -> 24x15
    # Row 3 (y=66..84):
    uv_leg_l = (0, 66)        # 6x6x6 -> 24x12
    uv_leg_r = (26, 66)       # 6x6x6 -> 24x12
    uv_boot_l = (52, 66)      # 7x4x7 -> 28x11
    uv_boot_r = (82, 66)      # 7x4x7 -> 28x11

    paint_weathered_stone(img, *uv_chest, 16, 14, 10, stone_ramp, crack_color=crack_color, moss_color=moss_color, seed=1)
    paint_weathered_stone(img, *uv_waist, 12, 6, 8, dark_stone_ramp, crack_color=crack_color, seed=2)
    paint_faceted_crystal(img, *uv_core, 6, 6, 2, amber_ramp, vein_color=(255, 252, 224, 255), seed=3)
    paint_faceted_crystal(img, *uv_visor, 8, 3, 2, amber_ramp, vein_color=(255, 252, 224, 255), seed=4)
    paint_weathered_stone(img, *uv_head, 10, 10, 10, stone_ramp, crack_color=crack_color, moss_color=moss_color, seed=5)
    paint_weathered_stone(img, *uv_brow, 12, 3, 11, dark_stone_ramp, crack_color=crack_color, moss_color=moss_color, seed=6)
    paint_weathered_stone(img, *uv_pauldron_l, 7, 5, 7, dark_stone_ramp, crack_color=crack_color, moss_color=moss_color, seed=7)
    paint_weathered_stone(img, *uv_pauldron_r, 7, 5, 7, dark_stone_ramp, crack_color=crack_color, moss_color=moss_color, seed=8)
    paint_weathered_stone(img, *uv_arm_l, 5, 8, 5, stone_ramp, crack_color=crack_color, seed=9)
    paint_weathered_stone(img, *uv_arm_r, 5, 8, 5, stone_ramp, crack_color=crack_color, seed=10)
    paint_weathered_stone(img, *uv_gaunt_l, 6, 9, 6, dark_stone_ramp, crack_color=crack_color, moss_color=moss_color, seed=11)
    paint_weathered_stone(img, *uv_gaunt_r, 6, 9, 6, dark_stone_ramp, crack_color=crack_color, moss_color=moss_color, seed=12)
    paint_weathered_stone(img, *uv_leg_l, 6, 6, 6, stone_ramp, crack_color=crack_color, seed=13)
    paint_weathered_stone(img, *uv_leg_r, 6, 6, 6, stone_ramp, crack_color=crack_color, seed=14)
    paint_weathered_stone(img, *uv_boot_l, 7, 4, 7, dark_stone_ramp, crack_color=crack_color, moss_color=moss_color, seed=15)
    paint_weathered_stone(img, *uv_boot_r, 7, 4, 7, dark_stone_ramp, crack_color=crack_color, moss_color=moss_color, seed=16)

    # Dark pupils inside the recessed glowing eye visor front
    vf = faces(*uv_visor, 8, 3, 2)["front"]
    put(img, vf[0] + 1, vf[1] + 1, (38, 28, 12, 255))
    put(img, vf[0] + 6, vf[1] + 1, (38, 28, 12, 255))

    bones = [
        {"name": "root", "pivot": [0, 0, 0]},
        {
            "name": "body",
            "parent": "root",
            "pivot": [0, 20, 0],
            "cubes": [
                {"origin": [-6, 10, -4], "size": [12, 6, 8], "uv": list(uv_waist)},
                {"origin": [-8, 16, -5], "size": [16, 14, 10], "uv": list(uv_chest)},
                {"origin": [-3, 20, -6.2], "size": [6, 6, 2], "pivot": [0, 23, -5.2], "rotation": [0, 0, 45], "uv": list(uv_core)},
            ],
        },
        {
            "name": "head",
            "parent": "body",
            "pivot": [0, 30, 0],
            "cubes": [
                {"origin": [-5, 30, -5], "size": [10, 10, 10], "uv": list(uv_head)},
                {"origin": [-6, 37, -5.8], "size": [12, 3, 11], "uv": list(uv_brow)},
                {"origin": [-4, 33, -5.6], "size": [8, 3, 2], "uv": list(uv_visor)},
            ],
        },
        {
            "name": "arm_left",
            "parent": "body",
            "pivot": [11, 28, 0],
            "cubes": [
                {"origin": [8, 25.5, -3.5], "size": [7, 5, 7], "pivot": [11, 28, 0], "rotation": [0, 0, 8], "uv": list(uv_pauldron_l)},
                {"origin": [8.5, 18, -2.5], "size": [5, 8, 5], "uv": list(uv_arm_l)},
                {"origin": [8, 10, -3], "size": [6, 9, 6], "uv": list(uv_gaunt_l)},
            ],
        },
        {
            "name": "arm_right",
            "parent": "body",
            "pivot": [-11, 28, 0],
            "cubes": [
                {"origin": [-15, 25.5, -3.5], "size": [7, 5, 7], "pivot": [-11, 28, 0], "rotation": [0, 0, -8], "uv": list(uv_pauldron_r)},
                {"origin": [-13.5, 18, -2.5], "size": [5, 8, 5], "uv": list(uv_arm_r)},
                {"origin": [-14, 10, -3], "size": [6, 9, 6], "uv": list(uv_gaunt_r)},
            ],
        },
        {
            "name": "leg_left",
            "parent": "root",
            "pivot": [4.5, 10, 0],
            "cubes": [
                {"origin": [1.5, 4, -3], "size": [6, 6, 6], "uv": list(uv_leg_l)},
                {"origin": [1, 0, -3.8], "size": [7, 4, 7], "uv": list(uv_boot_l)},
            ],
        },
        {
            "name": "leg_right",
            "parent": "root",
            "pivot": [-4.5, 10, 0],
            "cubes": [
                {"origin": [-7.5, 4, -3], "size": [6, 6, 6], "uv": list(uv_leg_r)},
                {"origin": [-8, 0, -3.8], "size": [7, 4, 7], "uv": list(uv_boot_r)},
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
                    "texture_height": 128,
                    "visible_bounds_width": 3.0,
                    "visible_bounds_height": 2.8,
                    "visible_bounds_offset": [0, 1.3, 0],
                },
                "bones": bones,
            }
        ],
    }
    assert_uv_layout_clean(geo, (128, 128))

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
    SPIRE_TALL_VIOLET = (22, 84, 5, 12, 5) # 20x17
    SPIRE_MED_TEAL = (44, 84, 4, 9, 4)     # 16x13
    SPIRE_MED_VIOLET = (62, 84, 4, 9, 4)   # 16x13
    SPIRE_SM_TEAL = (80, 84, 3, 7, 3)      # 12x10
    SPIRE_SM_VIOLET = (94, 84, 3, 7, 3)    # 12x10
    HORN_BASE = (108, 84, 4, 6, 4)         # 16x10
    HORN_TIP = (126, 84, 3, 9, 3)          # 12x12
    CROWN_CREST = (140, 84, 4, 8, 5)       # 18x13
    FOREARM_BLADE = (160, 84, 3, 9, 4)     # 14x13
    CLAW_FINGER = (176, 84, 3, 6, 3)       # 12x9
    CLAW_THUMB = (232, 84, 3, 5, 3)        # 12x8

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
        FOREARM_BLADE, CLAW_FINGER, CLAW_THUMB, SHARD_TEAL, SHARD_VIOLET, SHARD_CAP_TEAL, SHARD_CAP_VIOLET,
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
                        if dx + dy <= 1:
                            col = c_spec
                        elif dx >= fw - 1 and dy >= fh - 1:
                            col = c_mid
                        elif (dx + dy) % 2 == 0 or hval > 150:
                            col = c_hi
                        else:
                            col = _lerp_color(c_hi, c_mid, 0.5)
                    elif face == "bottom":
                        edge = (dx == 0 or dx == fw - 1 or dy == 0 or dy == fh - 1)
                        col = VOID_DEEP if edge else (c_root if (dx + dy) % 2 == 0 else c_deep)
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
    paint_crystal_spire_box(CLAW_THUMB, palette="teal", seed=317)
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
                {"origin": [12.5, 7, -4.2], "size": [3, 5, 3], "pivot": [14.0, 11, -2.7], "rotation": [0, 0, 12], "uv": [232, 84]},
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
                {"origin": [-15.5, 7, -4.2], "size": [3, 5, 3], "pivot": [-14.0, 11, -2.7], "rotation": [0, 0, -12], "uv": [232, 84]},
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


def make_captain_brimstone_animations():
    p = "animation.captain_brimstone"
    return {
        "format_version": "1.8.0",
        "animations": {
            f"{p}.idle": {
                "loop": True,
                "animation_length": 2.4,
                "bones": {
                    "body": {
                        "position": {"0.0": [0, 0, 0], "1.2": [0, 0.8, 0], "2.4": [0, 0, 0]},
                        "rotation": {"0.0": [0, 0, -1.5], "1.2": [1.5, 0, 1.5], "2.4": [0, 0, -1.5]},
                    },
                    "head": {
                        "rotation": {"0.0": [0, -6, 0], "1.2": [3, 6, 0], "2.4": [0, -6, 0]},
                    },
                    "mast": {
                        "rotation": {"0.0": [0, 0, -2], "1.2": [2, 0, 2], "2.4": [0, 0, -2]},
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 0], "1.2": [-4, 0, 0], "2.4": [0, 0, 0]},
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, 0], "1.2": [-4, 0, 0], "2.4": [0, 0, 0]},
                    },
                },
            },
            f"{p}.walk": {
                "loop": True,
                "animation_length": 0.8,
                "bones": {
                    "body": {
                        "position": {"0.0": [0, 0, 0], "0.4": [0, 1.0, 0], "0.8": [0, 0, 0]},
                        "rotation": {"0.0": [2, 0, -4], "0.4": [2, 0, 4], "0.8": [2, 0, -4]},
                    },
                    "leg_left": {
                        "rotation": {"0.0": [-22, 0, 0], "0.4": [22, 0, 0], "0.8": [-22, 0, 0]},
                    },
                    "leg_right": {
                        "rotation": {"0.0": [22, 0, 0], "0.4": [-22, 0, 0], "0.8": [22, 0, 0]},
                    },
                    "leg_back_left": {
                        "rotation": {"0.0": [22, 0, 0], "0.4": [-22, 0, 0], "0.8": [22, 0, 0]},
                    },
                    "leg_back_right": {
                        "rotation": {"0.0": [-22, 0, 0], "0.4": [22, 0, 0], "0.8": [-22, 0, 0]},
                    },
                },
            },
            f"{p}.attack": {
                "animation_length": 0.5,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "0.18": [-8, 0, 0], "0.34": [10, 0, 0], "0.5": [0, 0, 0]},
                    },
                    "arm_left": {
                        "position": {"0.0": [0, 0, 0], "0.18": [0, 0, 4], "0.5": [0, 0, 0]},
                        "rotation": {"0.0": [0, 0, 0], "0.18": [-18, 0, 0], "0.5": [0, 0, 0]},
                    },
                    "arm_right": {
                        "position": {"0.0": [0, 0, 0], "0.18": [0, 0, 4], "0.5": [0, 0, 0]},
                        "rotation": {"0.0": [0, 0, 0], "0.18": [-18, 0, 0], "0.5": [0, 0, 0]},
                    },
                },
            },
            f"{p}.telegraph": {
                "loop": False,
                "animation_length": 2.0,
                "bones": {
                    "body": {
                        "rotation": {"0.0": [0, 0, 0], "1.0": [-10, 0, 0], "1.8": [-14, 0, 0], "2.0": [6, 0, 0]},
                    },
                    "arm_left": {
                        "rotation": {"0.0": [0, 0, 0], "1.0": [-28, 0, 0], "1.8": [-35, 0, 0], "2.0": [8, 0, 0]},
                    },
                    "arm_right": {
                        "rotation": {"0.0": [0, 0, 0], "1.0": [-28, 0, 0], "1.8": [-35, 0, 0], "2.0": [8, 0, 0]},
                    },
                    "mast": {
                        "rotation": {"0.0": [0, 0, 0], "1.0": [8, 0, 0], "2.0": [-6, 0, 0]},
                    },
                },
            },
        },
    }


def build_captain_brimstone():
    """Non-humanoid Cursed Pirate Galleon-Walker: scuttling 4-legged ship hull, bowsprit skull
    figurehead, broadside cannons, spoked ship's wheel, Tricorne pirate skull-captain with eyepatch,
    and tall mast flying a Crimson Jolly-Roger skull & crossbones sail.
    """
    eid = "captain_brimstone"
    img = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    glow = Image.new("RGBA", (256, 128), (0, 0, 0, 0))

    # Dark weathered ship timber (6-stop ramp)
    WOOD_VOID = (24, 14, 14, 255)
    WOOD_DEEP = (40, 22, 20, 255)
    WOOD_DARK = (62, 34, 28, 255)
    WOOD_MID = (86, 48, 36, 255)
    WOOD_HI = (114, 66, 48, 255)

    # Charred iron hull bands & cannons
    IRON_VOID = (16, 13, 18, 255)
    IRON_DEEP = (28, 23, 30, 255)
    IRON_DARK = (44, 37, 46, 255)
    IRON_MID = (66, 56, 68, 255)
    IRON_HI = (94, 82, 96, 255)

    # Sunsteel Gold trim
    GOLD_DEEP = (128, 72, 20, 255)
    GOLD_DARK = (176, 108, 32, 255)
    GOLD_MID = (224, 152, 52, 255)
    GOLD_HI = (255, 208, 96, 255)

    # Crimson Jolly-Roger sailcloth & bone-white skull
    SAIL_DEEP = (68, 16, 22, 255)
    SAIL_DARK = (102, 26, 34, 255)
    SAIL_MID = (138, 38, 46, 255)
    SAIL_HI = (168, 56, 60, 255)
    BONE_DARK = (146, 136, 124, 255)
    BONE_MID = (206, 196, 180, 255)
    BONE_HI = (238, 230, 214, 255)

    # Magma glowmask
    MAGMA_ROOT = (138, 34, 12, 255)
    MAGMA_HOT = (255, 142, 38, 255)
    MAGMA_BRIGHT = (255, 198, 76, 255)
    MAGMA_CORE = (255, 242, 178, 255)

    # Non-overlapping 256x128 UV layout: (u, v, w, h, d) -> 2*(d+w) x (d+h)
    HULL_MAIN = (0, 0, 22, 7, 24)         # 92x31 -> x:[0..92), y:[0..31)
    HULL_KEEL = (94, 0, 16, 5, 26)         # 84x31 -> x:[94..178), y:[0..31)
    QUARTERDECK = (180, 0, 20, 5, 9)       # 58x14 -> x:[180..238), y:[0..14)
    BOWSPRIT = (180, 15, 3, 3, 14)         # 34x17 -> x:[180..214), y:[15..32)
    FIGUREHEAD = (216, 15, 6, 6, 6)        # 24x12 -> x:[216..240), y:[15..27)

    GUNWALE_RAIL = (0, 32, 24, 3, 2)       # 52x5 -> x:[0..52), y:[32..37)
    MAST_POLE = (0, 38, 3, 26, 3)          # 12x29 -> x:[0..12), y:[38..67)
    YARDARM = (14, 38, 20, 2, 2)           # 44x4 -> x:[14..58), y:[38..42)
    SAIL_MAIN = (14, 43, 18, 14, 1)        # 38x15 -> x:[14..52), y:[43..58)
    CROWS_NEST = (54, 43, 7, 3, 7)         # 28x10 -> x:[54..82), y:[43..53)
    MAST_LANTERN = (84, 43, 3, 4, 3)       # 12x7 -> x:[84..96), y:[43..50)

    SKULL_HEAD = (100, 34, 10, 9, 9)       # 38x18 -> x:[100..138), y:[34..52)
    HAT_CROWN = (140, 34, 11, 5, 10)       # 42x15 -> x:[140..182), y:[34..49)
    HAT_BRIM_F = (184, 34, 14, 2, 5)       # 38x7 -> x:[184..222), y:[34..41)
    HAT_BRIM_S = (184, 42, 3, 4, 12)       # 30x16 -> x:[184..214), y:[42..58)
    SHIPS_WHEEL = (216, 42, 10, 10, 2)     # 24x12 -> x:[216..240), y:[42..54)

    CANNON_BARREL_L = (0, 68, 6, 6, 12)    # 36x18 -> x:[0..36), y:[68..86)
    CANNON_BARREL_R = (38, 68, 6, 6, 12)   # 36x18 -> x:[38..74), y:[68..86)
    CANNON_RIM = (76, 68, 7, 7, 3)         # 20x10 -> x:[76..96), y:[68..78)
    CANNON_BORE = (76, 79, 4, 4, 2)        # 12x6 -> x:[76..88), y:[79..85)
    CANNON_CARRIAGE = (98, 68, 6, 4, 7)    # 26x11 -> x:[98..124), y:[68..79)

    LEG_FEMUR = (126, 68, 4, 10, 4)        # 16x14 -> x:[126..142), y:[68..82)
    LEG_CLAW = (144, 68, 4, 11, 4)         # 16x15 -> x:[144..160), y:[68..83)
    LEG_TIP = (162, 68, 5, 3, 5)           # 20x8 -> x:[162..182), y:[68..76)

    all_uv_boxes = [
        HULL_MAIN, HULL_KEEL, QUARTERDECK, BOWSPRIT, FIGUREHEAD,
        GUNWALE_RAIL, MAST_POLE, YARDARM, SAIL_MAIN, CROWS_NEST, MAST_LANTERN,
        SKULL_HEAD, HAT_CROWN, HAT_BRIM_F, HAT_BRIM_S, SHIPS_WHEEL,
        CANNON_BARREL_L, CANNON_BARREL_R, CANNON_RIM, CANNON_BORE, CANNON_CARRIAGE,
        LEG_FEMUR, LEG_CLAW, LEG_TIP,
    ]
    occupied = set()
    for u, v, w, h, d in all_uv_boxes:
        uw, uh = 2 * (d + w), d + h
        assert u + uw <= 256 and v + uh <= 128, f"UV box {(u, v, w, h, d)} out of 256x128 bounds"
        for px in range(u, u + uw):
            for py in range(v, v + uh):
                assert (px, py) not in occupied, f"UV overlap at {(px, py)} in box {(u, v, w, h, d)}"
                occupied.add((px, py))

    def paint_plank_box(box, pal_hi, pal_mid, pal_dark, pal_deep, seed=1,
                        plank_lines=True, gold_top=False, portholes=False):
        u, v, w, h, d = box
        for face, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
            for dy in range(fh):
                py = fy + dy
                ty = dy / max(1, fh - 1)
                for dx in range(fw):
                    px = fx + dx
                    hv = _h(px, py, seed)
                    if face == "top":
                        col = pal_hi if hv > 110 else pal_mid
                    elif face == "bottom":
                        col = pal_deep
                    elif face in ("east", "west"):
                        col = _lerp_color(pal_mid, pal_dark, 0.25 + 0.55 * ty)
                    else:
                        col = _lerp_color(pal_hi, pal_dark, 0.15 + 0.65 * ty)

                    if plank_lines and face != "bottom" and (dy % 3 == 2):
                        col = _lerp_color(col, pal_deep, 0.5)
                    elif hv < 45:
                        col = _lerp_color(col, pal_deep, 0.45)
                    elif hv > 205:
                        col = _lerp_color(col, pal_hi, 0.35)

                    if fw >= 4 and fh >= 4:
                        if dy == 0 and face != "bottom":
                            col = GOLD_MID if gold_top else _lerp_color(col, pal_hi, 0.4)
                        elif dy == fh - 1:
                            col = _lerp_color(col, pal_deep, 0.55)
                        elif dx == 0 or dx == fw - 1:
                            col = _lerp_color(col, pal_deep, 0.4)

                    put(img, px, py, col)

            # Glowing magma portholes along the hull sides
            if portholes and face in ("east", "west") and fw >= 18 and fh >= 6:
                for px_off in range(3, fw - 3, 5):
                    fill(img, (fx + px_off, fy + 2, 2, 2), MAGMA_BRIGHT)
                    fill(glow, (fx + px_off, fy + 2, 2, 2), MAGMA_BRIGHT)
                    put(img, fx + px_off, fy + 2, MAGMA_CORE)
                    put(glow, fx + px_off, fy + 2, MAGMA_CORE)
                    # Gold ring around porthole
                    for rx in (px_off - 1, px_off + 2):
                        put(img, fx + rx, fy + 2, GOLD_DARK)
                        put(img, fx + rx, fy + 3, GOLD_DARK)

    # Hull, keel, quarterdeck, bowsprit, gunwale
    paint_plank_box(HULL_MAIN, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=201, gold_top=True, portholes=True)
    paint_plank_box(HULL_KEEL, IRON_MID, IRON_DARK, IRON_DEEP, IRON_VOID, seed=202, plank_lines=False)
    paint_plank_box(QUARTERDECK, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=203, gold_top=True)
    paint_plank_box(GUNWALE_RAIL, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=204, gold_top=True)
    paint_plank_box(BOWSPRIT, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=205)

    # Bowsprit Skull Figurehead
    paint_plank_box(FIGUREHEAD, BONE_HI, BONE_MID, BONE_DARK, IRON_VOID, seed=206, plank_lines=False)
    fgx, fgy, fgw, fgh = faces(*FIGUREHEAD)["front"]
    for ex in (fgx + 1, fgx + fgw - 2):
        put(img, ex, fgy + 2, MAGMA_BRIGHT)
        put(glow, ex, fgy + 2, MAGMA_BRIGHT)

    # Mast, yardarm, crow's nest, glowing lantern
    paint_plank_box(MAST_POLE, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=207)
    paint_plank_box(YARDARM, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=208)
    paint_plank_box(CROWS_NEST, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=209, gold_top=True)
    paint_plank_box(MAST_LANTERN, MAGMA_CORE, MAGMA_BRIGHT, MAGMA_HOT, MAGMA_ROOT, seed=210, plank_lines=False)
    for face, rect in faces(*MAST_LANTERN).items():
        fill(glow, rect, MAGMA_BRIGHT)

    # Crimson Jolly-Roger Sail with bold Skull & Crossbones on front & back
    paint_plank_box(SAIL_MAIN, SAIL_HI, SAIL_MID, SAIL_DARK, SAIL_DEEP, seed=211, plank_lines=False)
    for face_name in ("front", "back"):
        sfx, sfy, sfw, sfh = faces(*SAIL_MAIN)[face_name]
        # Gold border hem on sail
        fill(img, (sfx, sfy, sfw, 1), GOLD_MID)
        fill(img, (sfx, sfy + sfh - 1, sfw, 1), GOLD_DARK)
        # Pixel-art Skull & Crossbones centered on 18x14 sail
        cx, cy = sfx + sfw // 2, sfy + sfh // 2 - 1
        fill(img, (cx - 3, cy - 3, 6, 4), BONE_HI)
        fill(img, (cx - 2, cy + 1, 4, 2), BONE_MID)
        # Dark skull eyes & nose
        put(img, cx - 2, cy - 1, IRON_VOID)
        put(img, cx + 1, cy - 1, IRON_VOID)
        put(img, cx - 1, cy + 1, IRON_VOID)
        put(img, cx, cy + 1, IRON_VOID)
        # Crossbones
        for d in (-4, -3, 2, 3):
            put(img, cx + d, cy + 3, BONE_HI)
            put(img, cx + d, cy + 4, BONE_MID)
            put(img, cx - d - 1, cy + 3, BONE_HI)

    # Pirate Skull-Captain Head with Black Eyepatch + 1 Blazing Magma Eye + Tricorne Hat
    paint_plank_box(SKULL_HEAD, BONE_HI, BONE_MID, BONE_DARK, IRON_VOID, seed=212, plank_lines=False)
    skx, sky, skw, skh = faces(*SKULL_HEAD)["front"]
    # Black eyepatch strap + patch over right eye (left side of front face)
    for dx in range(skw):
        put(img, skx + dx, sky + 2, IRON_VOID)
    fill(img, (skx + 1, sky + 3, 3, 3), IRON_VOID)
    # Blazing magma eye in left eye socket (right side of front face)
    fill(img, (skx + skw - 4, sky + 3, 2, 2), MAGMA_BRIGHT)
    fill(glow, (skx + skw - 4, sky + 3, 2, 2), MAGMA_BRIGHT)
    put(img, skx + skw - 3, sky + 3, MAGMA_CORE)
    put(glow, skx + skw - 3, sky + 3, MAGMA_CORE)
    # Grinning skull teeth
    for tx in range(skx + 2, skx + skw - 2, 2):
        put(img, tx, sky + skh - 2, IRON_VOID)

    paint_plank_box(HAT_CROWN, IRON_HI, IRON_MID, IRON_DARK, IRON_VOID, seed=213, plank_lines=False)
    paint_plank_box(HAT_BRIM_F, IRON_MID, IRON_DARK, IRON_DEEP, IRON_VOID, seed=214, plank_lines=False, gold_top=True)
    paint_plank_box(HAT_BRIM_S, IRON_MID, IRON_DARK, IRON_DEEP, IRON_VOID, seed=215, plank_lines=False, gold_top=True)
    hfx, hfy, hfw, hfh = faces(*HAT_CROWN)["front"]
    fill(img, (hfx + hfw // 2 - 1, hfy + 1, 2, 2), GOLD_HI)

    # Spoked Ship's Wheel
    paint_plank_box(SHIPS_WHEEL, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=216, plank_lines=False)
    for face_name in ("front", "back"):
        wfx, wfy, wfw, wfh = faces(*SHIPS_WHEEL)[face_name]
        fill(img, (wfx + 4, wfy + 4, 2, 2), GOLD_HI)
        for s in range(0, 10, 3):
            put(img, wfx + s, wfy + 4, GOLD_MID)
            put(img, wfx + 4, wfy + s, GOLD_MID)

    # Broadside Cannons + Carriages + 4 Crab-Anchor Legs
    paint_plank_box(CANNON_BARREL_L, IRON_HI, IRON_MID, IRON_DARK, IRON_VOID, seed=217, plank_lines=False)
    paint_plank_box(CANNON_BARREL_R, IRON_HI, IRON_MID, IRON_DARK, IRON_VOID, seed=218, plank_lines=False)
    paint_plank_box(CANNON_RIM, GOLD_HI, GOLD_MID, GOLD_DARK, GOLD_DEEP, seed=219, plank_lines=False)
    paint_plank_box(CANNON_BORE, MAGMA_CORE, MAGMA_BRIGHT, MAGMA_HOT, MAGMA_ROOT, seed=220, plank_lines=False)
    for face, rect in faces(*CANNON_BORE).items():
        fill(glow, rect, MAGMA_BRIGHT)
    paint_plank_box(CANNON_CARRIAGE, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=221)

    paint_plank_box(LEG_FEMUR, WOOD_HI, WOOD_MID, WOOD_DARK, WOOD_VOID, seed=222)
    paint_plank_box(LEG_CLAW, IRON_HI, IRON_MID, IRON_DARK, IRON_VOID, seed=223, plank_lines=False)
    paint_plank_box(LEG_TIP, GOLD_HI, GOLD_MID, GOLD_DARK, GOLD_DEEP, seed=224, plank_lines=False)

    bones = [
        {"name": "root", "pivot": [0, 0, 0]},
        {
            "name": "body",
            "parent": "root",
            "pivot": [0, 14, 0],
            "cubes": [
                # Ship Keel & Main Pirate Galleon Hull
                {"origin": [-8.0, 9, -13.0], "size": [16, 5, 26], "uv": [94, 0]},
                {"origin": [-11.0, 14, -12.0], "size": [22, 7, 24], "uv": [0, 0]},
                # Gunwale rails (port & starboard)
                {"origin": [-12.0, 21, -13.0], "size": [24, 3, 2], "uv": [0, 32]},
                {"origin": [-12.0, 21, 11.0], "size": [24, 3, 2], "uv": [0, 32]},
                # Raised Quarterdeck Stern
                {"origin": [-10.0, 21, 4.0], "size": [20, 5, 9], "uv": [180, 0]},
                # Sweeping 25-deg Bowsprit & Skull Figurehead jutting forward (-Z)
                {"origin": [-1.5, 19, -23.0], "size": [3, 3, 14], "pivot": [0, 20, -11], "rotation": [-25, 0, 0], "uv": [180, 15]},
                {"origin": [-3.0, 21, -24.0], "size": [6, 6, 6], "pivot": [0, 20, -11], "rotation": [-25, 0, 0], "uv": [216, 15]},
            ],
        },
        {
            "name": "head",
            "parent": "body",
            "pivot": [0, 26, 7],
            "cubes": [
                # Spoked Ship's Wheel behind the Captain Skull
                {"origin": [-5.0, 26, 9.0], "size": [10, 10, 2], "uv": [216, 42]},
                # Pirate Skull-Captain with Eyepatch & Magma Eye
                {"origin": [-5.0, 26, 1.0], "size": [10, 9, 9], "uv": [100, 34]},
                # Sweeping 3-flap Tricorne Hat
                {"origin": [-5.5, 34, 0.5], "size": [11, 5, 10], "uv": [140, 34]},
                {"origin": [-7.0, 34, -3.5], "size": [14, 2, 5], "pivot": [0, 34, 1.0], "rotation": [-18, 0, 0], "uv": [184, 34]},
                {"origin": [5.0, 34, -1.0], "size": [3, 4, 12], "pivot": [5.5, 34, 5.0], "rotation": [0, 0, 24], "uv": [184, 42]},
                {"origin": [-8.0, 34, -1.0], "size": [3, 4, 12], "pivot": [-5.5, 34, 5.0], "rotation": [0, 0, -24], "uv": [184, 42]},
            ],
        },
        {
            "name": "mast",
            "parent": "body",
            "pivot": [0, 21, -2],
            "cubes": [
                # Tall Mainmast, Yardarm, Crimson Jolly-Roger Sail, Crow's Nest & Glowing Lantern
                {"origin": [-1.5, 21, -3.5], "size": [3, 26, 3], "uv": [0, 38]},
                {"origin": [-10.0, 42, -5.0], "size": [20, 2, 2], "uv": [14, 38]},
                {"origin": [-9.0, 28, -4.8], "size": [18, 14, 1], "uv": [14, 43]},
                {"origin": [-3.5, 45, -5.5], "size": [7, 3, 7], "uv": [54, 43]},
                {"origin": [-1.5, 48, -3.5], "size": [3, 4, 3], "uv": [84, 43]},
            ],
        },
        {
            "name": "arm_left",
            "parent": "body",
            "pivot": [12.0, 22, -2],
            "cubes": [
                # Port Broadside Cannon on wooden carriage
                {"origin": [9.0, 19, -5.5], "size": [6, 4, 7], "uv": [98, 68]},
                {"origin": [9.0, 21, -8.5], "size": [6, 6, 12], "uv": [0, 68]},
                {"origin": [8.5, 20.5, -10.5], "size": [7, 7, 3], "uv": [76, 68]},
                {"origin": [10.0, 22, -11.5], "size": [4, 4, 2], "uv": [76, 79]},
            ],
        },
        {
            "name": "arm_right",
            "parent": "body",
            "pivot": [-12.0, 22, -2],
            "cubes": [
                # Starboard Broadside Cannon on wooden carriage
                {"origin": [-15.0, 19, -5.5], "size": [6, 4, 7], "uv": [98, 68]},
                {"origin": [-15.0, 21, -8.5], "size": [6, 6, 12], "uv": [38, 68]},
                {"origin": [-15.5, 20.5, -10.5], "size": [7, 7, 3], "uv": [76, 68]},
                {"origin": [-14.0, 22, -11.5], "size": [4, 4, 2], "uv": [76, 79]},
            ],
        },
        {
            "name": "leg_left",
            "parent": "root",
            "pivot": [10.0, 13, -7],
            "cubes": [
                {"origin": [9.0, 6, -9.0], "size": [4, 10, 4], "pivot": [10.0, 13, -7], "rotation": [-12, 0, -34], "uv": [126, 68]},
                {"origin": [13.0, 0, -10.0], "size": [4, 11, 4], "pivot": [14.0, 9, -8], "rotation": [8, 0, 16], "uv": [144, 68]},
                {"origin": [11.5, 0, -10.5], "size": [5, 3, 5], "uv": [162, 68]},
            ],
        },
        {
            "name": "leg_right",
            "parent": "root",
            "pivot": [-10.0, 13, -7],
            "cubes": [
                {"origin": [-13.0, 6, -9.0], "size": [4, 10, 4], "pivot": [-10.0, 13, -7], "rotation": [-12, 0, 34], "uv": [126, 68]},
                {"origin": [-17.0, 0, -10.0], "size": [4, 11, 4], "pivot": [-14.0, 9, -8], "rotation": [8, 0, -16], "uv": [144, 68]},
                {"origin": [-16.5, 0, -10.5], "size": [5, 3, 5], "uv": [162, 68]},
            ],
        },
        {
            "name": "leg_back_left",
            "parent": "root",
            "pivot": [10.0, 13, 7],
            "cubes": [
                {"origin": [9.0, 6, 5.0], "size": [4, 10, 4], "pivot": [10.0, 13, 7], "rotation": [12, 0, -34], "uv": [126, 68]},
                {"origin": [13.0, 0, 6.0], "size": [4, 11, 4], "pivot": [14.0, 9, 8], "rotation": [-8, 0, 16], "uv": [144, 68]},
                {"origin": [11.5, 0, 5.5], "size": [5, 3, 5], "uv": [162, 68]},
            ],
        },
        {
            "name": "leg_back_right",
            "parent": "root",
            "pivot": [-10.0, 13, 7],
            "cubes": [
                {"origin": [-13.0, 6, 5.0], "size": [4, 10, 4], "pivot": [-10.0, 13, 7], "rotation": [12, 0, 34], "uv": [126, 68]},
                {"origin": [-17.0, 0, 6.0], "size": [4, 11, 4], "pivot": [-14.0, 9, 8], "rotation": [-8, 0, -16], "uv": [144, 68]},
                {"origin": [-16.5, 0, 5.5], "size": [5, 3, 5], "uv": [162, 68]},
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
                    "visible_bounds_width": 4.8,
                    "visible_bounds_height": 4.6,
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
        json.dumps(make_captain_brimstone_animations(), indent=2) + "\n", encoding="utf-8"
    )


def build_brimstone_compass():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    iron = (44, 34, 42, 255)
    gold = (232, 168, 56, 255)
    face = (28, 22, 28, 255)
    ember = (255, 120, 32, 255)
    core = (255, 228, 128, 255)
    for y in range(2, 14):
        for x in range(2, 14):
            dx = x - 7.5
            dy = y - 7.5
            d2 = dx * dx + dy * dy
            if d2 <= 30:
                if d2 >= 21:
                    put(img, x, y, gold if (x + y) % 2 == 0 else iron)
                else:
                    put(img, x, y, face)
    # Top ring + glowing ember needle
    for rx, ry in ((7, 1), (8, 1)):
        put(img, rx, ry, gold)
    for nx, ny in ((7, 4), (8, 4), (7, 5), (8, 5), (7, 6), (8, 6)):
        put(img, nx, ny, core if ny == 4 else ember)
    for sx, sy in ((7, 9), (8, 9), (7, 10), (8, 10)):
        put(img, sx, sy, (124, 36, 40, 255))
    fill(img, (7, 7, 2, 2), gold)
    (ASSETS / "textures/item").mkdir(parents=True, exist_ok=True)
    img.save(ASSETS / "textures/item/brimstone_compass.png")


def build_brimstone_doubloon():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    gold_dark = (148, 84, 22, 255)
    gold = (238, 174, 58, 255)
    gold_hi = (255, 228, 120, 255)
    ember = (255, 108, 28, 255)
    core = (255, 212, 92, 255)
    for y in range(2, 14):
        for x in range(2, 14):
            d2 = (x - 7.5) ** 2 + (y - 7.5) ** 2
            if d2 <= 32:
                if d2 >= 23:
                    put(img, x, y, gold_dark)
                elif x + y < 14:
                    put(img, x, y, gold_hi)
                else:
                    put(img, x, y, gold)
    # Embossed ember flame emblem in center
    for fx, fy in ((7, 5), (8, 5), (6, 6), (7, 6), (8, 6), (9, 6), (6, 7), (9, 7), (7, 8), (8, 8), (7, 9), (8, 9)):
        put(img, fx, fy, ember)
    fill(img, (7, 6, 2, 2), core)
    (ASSETS / "textures/item").mkdir(parents=True, exist_ok=True)
    img.save(ASSETS / "textures/item/brimstone_doubloon.png")


def main():
    import sys

    if "--crystal-colossus" in sys.argv:
        build_crystal_colossus()
        print("Generated Crystal Colossus 256x128 texture, glowmask, geo, and animations.")
        return

    build_training_golem()
    build_boss_altar()
    build_golem_core()
    build_crystal_colossus()
    build_colossus_heart()
    build_prism_core()
    build_captain_brimstone()
    build_brimstone_compass()
    build_brimstone_doubloon()
    print("Generated boss art: training_golem, boss_altar, golem_core, crystal_colossus, colossus_heart, prism_core, captain_brimstone, brimstone_compass, brimstone_doubloon.")


if __name__ == "__main__":
    main()
