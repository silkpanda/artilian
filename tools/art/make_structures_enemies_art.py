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


def make_prism_sentinel():
    """256x256 (2x scale over 128x128 UV grid) Prism Sentinel: chiseled temple-stone & prism-crystal monolith golem + glowmask."""
    S = 2
    img = Image.new("RGBA", (128 * S, 128 * S), (0, 0, 0, 0))
    glow = Image.new("RGBA", (128 * S, 128 * S), (0, 0, 0, 0))

    # 8-stop Arcane Temple Stone ramp (#161222 -> #C4B8E0)
    STONE_RAMP = [
        (22, 18, 34, 255),     # 0: Deep crevice / masonry seam
        (36, 29, 54, 255),     # 1: Dark temple stone shadow
        (52, 43, 76, 255),     # 2: Shaded monolith slate
        (72, 60, 102, 255),    # 3: Mid arcane stone
        (94, 80, 130, 255),    # 4: Chiseled stone base
        (122, 107, 158, 255),  # 5: Sunlit stone bevel
        (156, 142, 192, 255),  # 6: Pale carved rim
        (196, 184, 224, 255),  # 7: Specular quartz glint
    ]
    # Canopy Moss & Grove Lichen accents (#0D3B32 -> #3BC29E)
    MOSS_RAMP = [
        (13, 59, 50, 255),
        (20, 89, 74, 255),
        (29, 122, 100, 255),
        (41, 158, 128, 255),
        (59, 194, 158, 255),
    ]
    # 8-stop Starlight-Teal Prism Crystal ramp (#093844 -> #38D9C0 -> #FFFFFF)
    TEAL_CRYSTAL = [
        (9, 56, 68, 255),
        (16, 94, 110, 255),
        (28, 142, 152, 255),
        (56, 217, 192, 255),
        (102, 236, 224, 255),
        (125, 249, 255, 255),
        (196, 252, 255, 255),
        (255, 255, 255, 255),
    ]
    # 8-stop Amethyst-Orchid Secondary Prism ramp (#34104C -> #A855F7 -> #FFFFFF)
    AMETHYST_RAMP = [
        (52, 16, 76, 255),
        (88, 28, 126, 255),
        (126, 34, 206, 255),
        (168, 85, 247, 255),
        (192, 132, 252, 255),
        (232, 121, 249, 255),
        (245, 208, 254, 255),
        (255, 255, 255, 255),
    ]

    PAL = {
        "0": STONE_RAMP[0],
        "1": STONE_RAMP[1],
        "2": STONE_RAMP[2],
        "3": STONE_RAMP[3],
        "4": STONE_RAMP[4],
        "5": STONE_RAMP[5],
        "6": STONE_RAMP[6],
        "7": STONE_RAMP[7],
        "m": MOSS_RAMP[1],
        "M": MOSS_RAMP[3],
        "g": MOSS_RAMP[4],
        "t": TEAL_CRYSTAL[1],
        "T": TEAL_CRYSTAL[3],
        "c": TEAL_CRYSTAL[4],
        "C": TEAL_CRYSTAL[5],
        "i": TEAL_CRYSTAL[6],
        "W": TEAL_CRYSTAL[7],
        "p": AMETHYST_RAMP[1],
        "P": AMETHYST_RAMP[3],
        "a": AMETHYST_RAMP[4],
        "A": AMETHYST_RAMP[5],
        "l": AMETHYST_RAMP[6],
    }

    def f2x(u, v, w, h, d):
        return {k: (rx * S, ry * S, rw * S, rh * S) for k, (rx, ry, rw, rh) in faces(u, v, w, h, d).items()}

    def paint_grid(target, rx, ry, grid, palette):
        for ly, row in enumerate(grid):
            for lx, ch in enumerate(row):
                if ch != ".":
                    put(target, rx + lx, ry + ly, palette[ch])

    def paint_box_2x(target, u, v, w, h, d, face_grids, palette):
        rects = f2x(u, v, w, h, d)
        for name, grid in face_grids.items():
            rx, ry, rw, rh = rects[name]
            assert len(grid) == rh and all(len(r) == rw for r in grid), (
                f"Grid size mismatch on {name} at ({u},{v}): expected {rw}x{rh}, got {len(grid[0])}x{len(grid)}"
            )
            paint_grid(target, rx, ry, grid, palette)

    def paint_cutout_z(u, v, w, h, front_grid, *, glow_keys=()):
        back_grid = [row[::-1] for row in front_grid]
        paint_box_2x(img, u, v, w, h, 0, {"front": front_grid, "back": back_grid}, PAL)
        if glow_keys:
            gf = ["".join(ch if ch in glow_keys else "." for ch in row) for row in front_grid]
            gb = ["".join(ch if ch in glow_keys else "." for ch in row) for row in back_grid]
            paint_box_2x(glow, u, v, w, h, 0, {"front": gf, "back": gb}, PAL)

    def paint_cutout_x(u, v, h, d, west_grid, *, glow_keys=()):
        east_grid = [row[::-1] for row in west_grid]
        paint_box_2x(img, u, v, 0, h, d, {"west": west_grid, "east": east_grid}, PAL)
        if glow_keys:
            gw = ["".join(ch if ch in glow_keys else "." for ch in row) for row in west_grid]
            ge = [row[::-1] for row in gw]
            paint_box_2x(glow, u, v, 0, h, d, {"west": gw, "east": ge}, PAL)

    def shade_stone_box(u, v, w, h, d, *, mossy=False, crystal=False, seed=0):
        ramp = TEAL_CRYSTAL if crystal else STONE_RAMP
        f = f2x(u, v, w, h, d)
        face_bias = {
            "top": 5.3,
            "front": 4.4,
            "east": 3.9,
            "west": 3.5,
            "back": 3.0,
            "bottom": 1.9,
        }
        for name, (rx, ry, rw, rh) in f.items():
            if rw <= 0 or rh <= 0:
                continue
            bias = face_bias[name]
            for ly in range(rh):
                ty = ly / max(1, rh - 1)
                for lx in range(rw):
                    tx = lx / max(1, rw - 1)
                    if name == "bottom":
                        edge_dist = max(abs(tx - 0.5) * 2.0, abs(ty - 0.5) * 2.0)
                        idx = bias + (1.0 - edge_dist) * 1.4 - edge_dist * 1.1
                    elif name == "top":
                        edge = min(lx, rw - 1 - lx, ly, rh - 1 - ly)
                        idx = bias - (0.55 if edge == 0 else 0.0) + (0.45 if ty < 0.45 else -0.25)
                    else:
                        idx = bias + (0.45 - ty) * 1.8 + (0.2 - tx) * 0.35
                        if ly == 0:
                            idx += 0.55
                        elif ly == rh - 1:
                            idx -= 0.7
                    if crystal:
                        facet = ((lx + ly * 2 + seed) % 5)
                        if facet == 0:
                            idx += 1.2
                        elif facet == 3:
                            idx -= 0.9
                    else:
                        # Masonry strata & chiseled stone grain
                        if name not in ("top", "bottom") and rh >= 8 and ly == rh // 2:
                            idx -= 1.3
                        if (lx * 7 + ly * 13 + seed * 5) % 15 == 0:
                            idx -= 0.85
                        elif (lx * 3 + ly * 11 + seed * 7) % 13 == 0:
                            idx += 0.6
                    c_idx = max(0, min(len(ramp) - 1, int(round(idx))))
                    color = ramp[c_idx]
                    if mossy and name != "top" and ty > 0.58 and ((lx * 5 + ly * 7 + seed) % 4 == 0):
                        color = MOSS_RAMP[3 if ty < 0.8 else 1]
                    put(img, rx + lx, ry + ly, color)

    # 1. Band 1 (y=0..14): Torso
    #    Chest [11, 8, 6] @ [0, 0] -> 22x16x12 px
    shade_stone_box(0, 0, 11, 8, 6, seed=1)
    cf = f2x(0, 0, 11, 8, 6)
    # Chiseled temple trim & glowing prism veins on chest front & back
    fx, fy, fw, fh = cf["front"]
    for px in range(2, fw - 2):
        put(img, fx + px, fy + 2, PAL["6"] if px % 2 == 0 else PAL["5"])
        put(img, fx + px, fy + fh - 3, PAL["1"])
    for py in range(4, fh - 4):
        put(img, fx + 3, fy + py, PAL["C"])
        put(img, fx + fw - 4, fy + py, PAL["C"])
        put(glow, fx + 3, fy + py, PAL["C"])
        put(glow, fx + fw - 4, fy + py, PAL["C"])
    bx, by, bw, bh = cf["back"]
    for py in range(3, bh - 3):
        put(img, bx + bw // 2 - 1, by + py, PAL["P"])
        put(img, bx + bw // 2, by + py, PAL["A"])
        put(glow, bx + bw // 2 - 1, by + py, PAL["A"])
        put(glow, bx + bw // 2, by + py, PAL["A"])

    #    Waist [7, 4, 4] @ [34, 0] -> 14x8x8 px
    shade_stone_box(34, 0, 7, 4, 4, mossy=True, seed=2)
    wf = f2x(34, 0, 7, 4, 4)["front"]
    for px in range(4, 10):
        put(img, wf[0] + px, wf[1] + 3, PAL["T" if px in (4, 9) else "C"])
        put(glow, wf[0] + px, wf[1] + 3, PAL["C"])

    #    Prism Heart Core [5, 5, 1] @ [56, 0] -> 10x10 px front
    shade_stone_box(56, 0, 5, 5, 1, seed=3)
    paint_box_2x(img, 56, 0, 5, 5, 1, {
        "front": [
            "0124554210",
            "10tTCCTt01",
            "2tTCWWCTp2",
            "3TCWWWWCP3",
            "3TCWWWWAP3",
            "3TCWWWWAP3",
            "2tTCWWAPp2",
            "10pPAAAP01",
            "0123333210",
            "0011221100",
        ],
    }, PAL)
    paint_box_2x(glow, 56, 0, 5, 5, 1, {
        "front": [
            "..........",
            "..tTCCTt..",
            ".tTCWWCTp.",
            ".TCWWWWCP.",
            ".TCWWWWAP.",
            ".TCWWWWAP.",
            ".tTCWWAPp.",
            "..pPAAAP..",
            "..........",
            "..........",
        ],
    }, PAL)

    #    Dorsal crystal spine [2, 5, 2] @ [68, 0] & cutout fin [0, 8, 4] @ [76, 0] -> 8x16 px
    shade_stone_box(68, 0, 2, 5, 2, crystal=True, seed=4)
    df = f2x(68, 0, 2, 5, 2)
    for fname in ("front", "back", "east", "west", "top"):
        rx, ry, rw, rh = df[fname]
        for py in range(rh):
            for px in range(rw):
                put(glow, rx + px, ry + py, PAL["C" if py < rh // 2 else "T"])
    paint_cutout_x(76, 0, 8, 4, [
        "..W.....",
        ".iCi....",
        ".TCCT...",
        "tTCWWp..",
        "tTCCAPp.",
        ".tTCAAp.",
        "..pPAAp.",
        "..tTCCT.",
        ".tTCWWCp",
        "tTCWWAP.",
        ".tTCAP..",
        "..pPAp..",
        "..tTC...",
        ".12tT...",
        "01221...",
        "0011....",
    ], glow_keys={"W", "i", "C", "T", "t", "A", "P", "p"})

    # 2. Band 2 (y=16..30): Head & Crest
    #    Helm [8, 7, 7] @ [0, 16] -> 16x14x14 px
    shade_stone_box(0, 16, 8, 7, 7, seed=5)
    hf = f2x(0, 16, 8, 7, 7)
    # Carved back-of-head Shrine Rune
    hbx, hby, hbw, hbh = hf["back"]
    for py in range(3, hbh - 3):
        put(img, hbx + 7, hby + py, PAL["C" if py in (5, 6, 7) else "T"])
        put(img, hbx + 8, hby + py, PAL["W" if py in (6, 7) else "C"])
        put(glow, hbx + 7, hby + py, PAL["C"])
        put(glow, hbx + 8, hby + py, PAL["W" if py in (6, 7) else "C"])

    #    Brow overhang [9, 2, 2] @ [30, 16] -> 18x4 px
    shade_stone_box(30, 16, 9, 2, 2, seed=6)

    #    Visor [6, 3, 1] @ [52, 16] -> 12x6 px front
    shade_stone_box(52, 16, 6, 3, 1, seed=7)
    paint_box_2x(img, 52, 16, 6, 3, 1, {
        "front": [
            "012333333210",
            "0tCWt00tWCp0",
            "0TCCT00TCAP0",
            "0tCC0WW0AAp0",
            "01100CC00110",
            "001122221100",
        ],
    }, PAL)
    paint_box_2x(glow, 52, 16, 6, 3, 1, {
        "front": [
            "............",
            ".tCWt..tWCp.",
            ".TCCT..TCAP.",
            ".tCC.WW.AAp.",
            ".....CC.....",
            "............",
        ],
    }, PAL)

    #    Cheek shards [2, 4, 2] @ [66, 16] & [74, 16]
    for cu in (66, 74):
        shade_stone_box(cu, 16, 2, 4, 2, crystal=True, seed=8)
        cf2 = f2x(cu, 16, 2, 4, 2)
        for fname in ("front", "back", "east", "west", "top"):
            rx, ry, rw, rh = cf2[fname]
            for py in range(rh):
                for px in range(rw):
                    put(glow, rx + px, ry + py, PAL["i" if py < 3 else "C"])

    #    Crest spire [3, 5, 3] @ [84, 16]
    shade_stone_box(84, 16, 3, 5, 3, crystal=True, seed=9)
    crf = f2x(84, 16, 3, 5, 3)
    for fname in ("front", "back", "east", "west", "top"):
        rx, ry, rw, rh = crf[fname]
        for py in range(rh):
            for px in range(rw):
                if fname == "top":
                    if 1 <= px <= rw - 2 and 1 <= py <= rh - 2:
                        put(glow, rx + px, ry + py, PAL["W"])
                elif py < 2:
                    put(glow, rx + px, ry + py, PAL["i"])
                elif 1 <= px <= rw - 2 and py < rh - 2:
                    put(glow, rx + px, ry + py, PAL["C" if py < 6 else "A"])

    #    Crest Z-cutout orbiting shards [11, 6, 0] @ [30, 22] -> 22x12 px
    paint_cutout_z(30, 22, 11, 6, [
        "..W...............l...",
        ".iCi.............lAl..",
        ".TCCT...........pAAp..",
        "tTCWWt...iWi....pAAAp.",
        ".tTCCt..iCCWi....pAAp.",
        "..tTt...tTCCT.....pp..",
        "........tTCCt.........",
        ".........tTt..........",
        "......................",
        "..l.............W.....",
        ".pAl...........iCi....",
        "..pp...........tTt....",
    ], glow_keys={"W", "i", "C", "T", "t", "l", "A", "p"})

    #    Crest X-cutout orbiting shards [0, 6, 7] @ [96, 16] -> 14x12 px
    paint_cutout_x(96, 16, 6, 7, [
        "..W.........l.",
        ".iCi.......lAl",
        ".TCCT.....pAAp",
        "tTCWWt....pAAp",
        ".tTCCt.....pp.",
        "..tTt.........",
        "..............",
        ".....iWi......",
        "....iCCWi.....",
        "....tTCCT.....",
        ".....tTt......",
        "..............",
    ], glow_keys={"W", "i", "C", "T", "t", "l", "A", "p"})

    # 3. Band 3 (y=32..50): Arms (arm_l @ base_u=0, arm_r @ base_u=56)
    shard_fan = [
        "....W...",
        "...iCi..",
        "..iCCi..",
        "..TCCT..",
        ".tTCWWp.",
        ".tTCCAP.",
        "..tTCAp.",
        "..pPAAp.",
        ".12tTCp.",
        ".012tT..",
        "..012...",
        "........",
    ]
    for idx, bu in enumerate((0, 56)):
        shade_stone_box(bu, 32, 4, 4, 5, seed=10 + idx)        # Pauldron
        shade_stone_box(bu + 18, 32, 3, 4, 3, seed=12 + idx)   # Upper arm
        shade_stone_box(bu + 30, 32, 4, 6, 4, mossy=True, seed=14 + idx)  # Gauntlet
        # Glowing prism knuckle inlays on gauntlet front
        gf = f2x(bu + 30, 32, 4, 6, 4)["front"]
        for px in (2, 3, 4, 5):
            put(img, gf[0] + px, gf[1] + gf[3] - 3, PAL["C" if px in (3, 4) else "T"])
            put(glow, gf[0] + px, gf[1] + gf[3] - 3, PAL["C" if px in (3, 4) else "T"])

        # Shoulder crystal spire [3, 5, 3] @ (bu, 42)
        shade_stone_box(bu, 42, 3, 5, 3, crystal=True, seed=16 + idx)
        spf = f2x(bu, 42, 3, 5, 3)
        for fname in ("front", "back", "east", "west", "top"):
            rx, ry, rw, rh = spf[fname]
            for py in range(rh):
                for px in range(rw):
                    put(glow, rx + px, ry + py, PAL["i" if py < 3 else "C"])

        # Shoulder shard cutout [4, 6, 0] @ (bu + 14, 42) -> 8x12 px
        paint_cutout_z(bu + 14, 42, 4, 6, shard_fan if idx == 0 else [r[::-1] for r in shard_fan],
                       glow_keys={"W", "i", "C", "T", "t", "A", "P", "p"})

    # 4. Band 4 (y=54..64): Legs (leg_l @ base_u=0, leg_r @ base_u=36)
    for idx, bu in enumerate((0, 36)):
        shade_stone_box(bu, 54, 3, 5, 3, mossy=True, seed=20 + idx)
        shade_stone_box(bu + 12, 54, 4, 5, 5, mossy=True, seed=22 + idx)
        grf = f2x(bu + 12, 54, 4, 5, 5)["front"]
        for px in (3, 4):
            put(img, grf[0] + px, grf[1] + 2, PAL["C"])
            put(img, grf[0] + px, grf[1] + 3, PAL["T"])
            put(glow, grf[0] + px, grf[1] + 2, PAL["C"])
            put(glow, grf[0] + px, grf[1] + 3, PAL["T"])

    # 5. Save textures, geo.json, and animation.json
    (ASSETS / "textures/entity").mkdir(parents=True, exist_ok=True)
    (ASSETS / "geo/entity").mkdir(parents=True, exist_ok=True)
    (ASSETS / "animations/entity").mkdir(parents=True, exist_ok=True)

    img.save(ASSETS / "textures/entity/prism_sentinel.png")
    glow.save(ASSETS / "textures/entity/prism_sentinel_glowmask.png")

    def cube(origin, size, uv, *, pivot=None, rotation=None):
        c = {"origin": origin, "size": size, "uv": uv}
        if pivot is not None:
            c["pivot"] = pivot
        if rotation is not None:
            c["rotation"] = rotation
        return c

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.prism_sentinel",
                    "texture_width": 128,
                    "texture_height": 128,
                    "visible_bounds_width": 2.8,
                    "visible_bounds_height": 3.0,
                    "visible_bounds_offset": [0, 1.2, 0],
                },
                "bones": [
                    {"name": "root", "pivot": [0, 0, 0]},
                    {
                        "name": "torso",
                        "parent": "root",
                        "pivot": [0, 15.5, 0],
                        "cubes": [
                            cube([-5.5, 13.5, -3.2], [11, 8, 6], [0, 0]),
                            cube([-3.5, 10.0, -2.2], [7, 4, 4], [34, 0]),
                            cube([-2.5, 15.0, -3.8], [5, 5, 1], [56, 0]),
                            cube([-1.0, 15.0, 2.5], [2, 5, 2], [68, 0], pivot=[0, 15.0, 3.0], rotation=[-18, 0, 0]),
                            cube([0.0, 13.0, 2.8], [0, 8, 4], [76, 0]),
                        ],
                    },
                    {
                        "name": "head",
                        "parent": "torso",
                        "pivot": [0, 21.5, 0],
                        "cubes": [
                            cube([-4.0, 21.5, -3.5], [8, 7, 7], [0, 16]),
                            cube([-4.5, 26.5, -4.2], [9, 2, 2], [30, 16]),
                            cube([-3.0, 23.2, -4.1], [6, 3, 1], [52, 16]),
                            cube([3.8, 24.0, -1.5], [2, 4, 2], [66, 16], pivot=[4.0, 24.5, -0.5], rotation=[-10, 0, 22]),
                            cube([-5.8, 24.0, -1.5], [2, 4, 2], [74, 16], pivot=[-4.0, 24.5, -0.5], rotation=[-10, 0, -22]),
                        ],
                    },
                    {
                        "name": "crest",
                        "parent": "head",
                        "pivot": [0, 29.0, 0],
                        "cubes": [
                            cube([-1.5, 29.2, -1.5], [3, 5, 3], [84, 16], pivot=[0, 31.7, 0], rotation=[0, 45, 0]),
                            cube([-5.5, 28.8, 0.0], [11, 6, 0], [30, 22]),
                            cube([0.0, 28.8, -3.5], [0, 6, 7], [96, 16]),
                        ],
                    },
                    {
                        "name": "arm_l",
                        "parent": "torso",
                        "pivot": [7.5, 19.5, 0],
                        "cubes": [
                            cube([5.5, 18.0, -2.5], [4, 4, 5], [0, 32]),
                            cube([6.0, 14.2, -1.5], [3, 4, 3], [18, 32]),
                            cube([5.5, 8.5, -2.0], [4, 6, 4], [30, 32]),
                            cube([6.2, 21.5, -1.5], [3, 5, 3], [0, 42], pivot=[7.5, 22.0, 0], rotation=[-8, 0, 24]),
                            cube([6.5, 22.0, 0.0], [4, 6, 0], [14, 42], pivot=[7.5, 22.0, 0], rotation=[0, -18, 16]),
                        ],
                    },
                    {
                        "name": "arm_r",
                        "parent": "torso",
                        "pivot": [-7.5, 19.5, 0],
                        "cubes": [
                            cube([-9.5, 18.0, -2.5], [4, 4, 5], [56, 32]),
                            cube([-9.0, 14.2, -1.5], [3, 4, 3], [74, 32]),
                            cube([-9.5, 8.5, -2.0], [4, 6, 4], [86, 32]),
                            cube([-9.2, 21.5, -1.5], [3, 5, 3], [56, 42], pivot=[-7.5, 22.0, 0], rotation=[-8, 0, -24]),
                            cube([-10.5, 22.0, 0.0], [4, 6, 0], [70, 42], pivot=[-7.5, 22.0, 0], rotation=[0, 18, -16]),
                        ],
                    },
                    {
                        "name": "leg_l",
                        "parent": "root",
                        "pivot": [3.0, 10.0, 0],
                        "cubes": [
                            cube([1.5, 5.0, -1.5], [3, 5, 3], [0, 54]),
                            cube([1.0, 0.0, -2.5], [4, 5, 5], [12, 54]),
                        ],
                    },
                    {
                        "name": "leg_r",
                        "parent": "root",
                        "pivot": [-3.0, 10.0, 0],
                        "cubes": [
                            cube([-4.5, 5.0, -1.5], [3, 5, 3], [36, 54]),
                            cube([-5.0, 0.0, -2.5], [4, 5, 5], [48, 54]),
                        ],
                    },
                ],
            }
        ],
    }

    (ASSETS / "geo/entity/prism_sentinel.geo.json").write_text(json.dumps(geo, indent=2) + "\n", encoding="utf-8")
    anim = make_animations("prism_sentinel")
    anim["animations"]["animation.prism_sentinel.idle"]["bones"]["crest"] = {
        "position": {"0.0": [0, 0.0, 0], "1.2": [0, 0.9, 0], "2.4": [0, 0.0, 0]},
        "rotation": {"0.0": [0, 0, 0], "1.2": [0, 180, 0], "2.4": [0, 360, 0]},
    }
    (ASSETS / "animations/entity/prism_sentinel.animation.json").write_text(
        json.dumps(anim, indent=2) + "\n", encoding="utf-8"
    )


def main():
    import sys

    if "--prism-sentinel" in sys.argv:
        make_prism_sentinel()
        print("Generated Prism Sentinel 256x256 texture, glowmask, geo, and animations.")
        return

    make_prism_sentinel()
    for spec in SPECS:
        build_enemy(spec)
    print("Generated structure enemy assets: prism_sentinel, cinder_raider, astral_stalker.")


if __name__ == "__main__":
    main()
