"""Generate Phase 4 Stage 1 Eye Realm portal & Gazing Eye textures."""

import json
import math
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
BLOCK_TEX = ROOT / "src/main/resources/assets/artilian/textures/block"
ITEM_TEX = ROOT / "src/main/resources/assets/artilian/textures/item"
ENV_TEX = ROOT / "src/main/resources/assets/artilian/textures/environment"


def build_gazing_portal() -> None:
    """Create a 4-frame 16x64 animated translucent portal texture + .mcmeta."""
    frames = 4
    w, h = 16, 16 * frames
    img = Image.new("RGBA", (w, h))
    px = img.load()

    for f in range(frames):
        phase = (f / frames) * math.tau
        for y in range(16):
            for x in range(16):
                dx = (x - 7.5) / 7.5
                dy = (y - 7.5) / 7.5
                dist = math.hypot(dx, dy)
                angle = math.atan2(dy, dx)
                swirl = math.sin(dist * 7.0 - phase + angle * 2.0)
                ripple = math.cos((x + y) * 0.6 + phase)

                if dist < 0.24 + 0.04 * math.sin(phase):
                    # Bright cyan pupil glow center
                    r, g, b, a = 125, 249, 255, 230
                elif swirl > 0.35:
                    # Twilight magenta swirl band
                    r, g, b, a = 217, 70, 239, 205
                elif ripple > 0.25:
                    # Arcane violet midtone
                    r, g, b, a = 148, 52, 196, 185
                else:
                    # Deep Artilian slate backdrop
                    r, g, b, a = 43, 35, 56, 165

                px[x, f * 16 + y] = (r, g, b, a)

    BLOCK_TEX.mkdir(parents=True, exist_ok=True)
    img.save(BLOCK_TEX / "gazing_portal.png")
    mcmeta = {"animation": {"frametime": 3, "interpolate": True}}
    (BLOCK_TEX / "gazing_portal.png.mcmeta").write_text(
        json.dumps(mcmeta, indent=2) + "\n", encoding="utf-8"
    )


def build_gazing_eye() -> None:
    """Create 16x16 Gazing Eye igniter item texture."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    for y in range(16):
        for x in range(16):
            dx = (x - 7.5) / 6.5
            dy = (y - 7.5) / 4.8
            ell = dx * dx + dy * dy
            circle = ((x - 7.5) / 6.8) ** 2 + ((y - 7.5) / 6.8) ** 2

            if ell <= 1.0:
                # Eye almond interior
                iris_d = math.hypot(x - 7.5, y - 7.5)
                if iris_d <= 1.6:
                    # Deep pupil with specular glint
                    if x == 6 and y == 6:
                        px[x, y] = (235, 252, 255, 255)
                    else:
                        px[x, y] = (28, 16, 42, 255)
                elif iris_d <= 3.1:
                    # Cyan & magenta iris ring
                    if (x + y) % 2 == 0:
                        px[x, y] = (125, 249, 255, 255)
                    else:
                        px[x, y] = (217, 70, 239, 255)
                else:
                    # Lavender sclera
                    shade = 232 - int(abs(x - 7.5) * 6 + abs(y - 7.5) * 8)
                    px[x, y] = (shade, max(180, shade - 18), 248, 255)
            elif ell <= 1.32:
                # Gold / Starlight Slate bezel
                if y < 7:
                    px[x, y] = (255, 196, 86, 255)
                else:
                    px[x, y] = (184, 116, 36, 255)
            elif circle <= 1.05 and (x in (7, 8) or y in (7, 8)):
                # Cardinal arcane prongs
                px[x, y] = (125, 249, 255, 220)

    ITEM_TEX.mkdir(parents=True, exist_ok=True)
    img.save(ITEM_TEX / "gazing_eye.png")


def _draw_mini_eye(px, cx: int, cy: int, w_half: int, iris_rgb: tuple[int, int, int], sclera_rgb=(230, 212, 245)) -> None:
    """Draw a small stylized watching eye centered at (cx, cy) onto a 16x16 pixel buffer."""
    for dx in range(-w_half, w_half + 1):
        px[cx + dx, cy] = (*sclera_rgb, 255)
    px[cx, cy] = (24, 14, 38, 255)
    if w_half >= 2:
        px[cx - 1, cy] = (*iris_rgb, 255)
        px[cx + 1, cy] = (*iris_rgb, 255)
        px[cx, cy - 1] = (*iris_rgb, 220)
        px[cx, cy + 1] = (*iris_rgb, 220)


def build_gaze_stone() -> None:
    """16x16 deep slate-violet stone with strata & subtle carved watching eyes."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    px = img.load()
    ramp = [
        (33, 24, 48),
        (43, 35, 56),
        (54, 42, 72),
        (66, 51, 88),
        (80, 62, 106),
        (96, 76, 124),
    ]
    for y in range(16):
        for x in range(16):
            h = (x * 13 + y * 29 + (x ^ y) * 7) % 10
            idx = min(5, max(0, (h // 2) + (1 if (y % 4 == 0) else 0)))
            if y in (4, 11) and (x + y) % 3 == 0:
                idx = max(0, idx - 2)
            px[x, y] = (*ramp[idx], 255)
    # Two subtle carved eyes in the stone
    _draw_mini_eye(px, 4, 5, 2, (148, 72, 212), sclera_rgb=(188, 172, 212))
    _draw_mini_eye(px, 11, 12, 2, (100, 210, 228), sclera_rgb=(188, 172, 212))
    img.save(BLOCK_TEX / "gaze_stone.png")


def build_eye_turf() -> None:
    """16x16 eye_turf_top and eye_turf_side textures."""
    top = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    tpx = top.load()
    turf_ramp = [
        (78, 30, 108),
        (104, 38, 140),
        (132, 48, 172),
        (164, 56, 204),
        (196, 66, 224),
        (217, 70, 239),
    ]
    for y in range(16):
        for x in range(16):
            idx = (x * 7 + y * 11 + (x * y)) % len(turf_ramp)
            tpx[x, y] = (*turf_ramp[idx], 255)
    _draw_mini_eye(tpx, 4, 4, 2, (125, 249, 255))
    _draw_mini_eye(tpx, 11, 11, 2, (217, 70, 239))
    _draw_mini_eye(tpx, 12, 3, 1, (125, 249, 255))
    top.save(BLOCK_TEX / "eye_turf_top.png")

    # Side: gaze_stone lower 11px, overhanging twilight turf top 5px + peeking eye
    stone = Image.open(BLOCK_TEX / "gaze_stone.png").convert("RGBA")
    side = stone.copy()
    spx = side.load()
    for x in range(16):
        drip = 4 + ((x * 5 + 3) % 3)
        for y in range(drip):
            idx = min(5, max(0, 4 - y + ((x + y) % 2)))
            spx[x, y] = (*turf_ramp[idx], 255)
    _draw_mini_eye(spx, 8, 2, 2, (125, 249, 255))
    side.save(BLOCK_TEX / "eye_turf_side.png")


def build_blinking_eye_block() -> None:
    """4-frame 16x64 animated blinking eye block + .mcmeta."""
    frames = 4
    img = Image.new("RGBA", (16, 16 * frames), (0, 0, 0, 255))
    px = img.load()
    openness = [1.0, 0.55, 0.12, 0.55]

    for f in range(frames):
        oy = f * 16
        open_amt = openness[f]
        for y in range(16):
            for x in range(16):
                # Deep slate-purple frame border
                border = min(x, 15 - x, y, 15 - y)
                if border == 0:
                    px[x, oy + y] = (33, 24, 48, 255)
                    continue
                if border == 1:
                    px[x, oy + y] = (58, 42, 82, 255)
                    continue

                dx = (x - 7.5) / 5.5
                dy = (y - 7.5) / (4.5 * max(0.1, open_amt))
                ell = dx * dx + dy * dy
                if ell <= 1.0:
                    iris_d = math.hypot(x - 7.5, y - 7.5)
                    if iris_d <= 1.7:
                        px[x, oy + y] = (235, 252, 255, 255) if (x == 6 and y == 6) else (22, 12, 34, 255)
                    elif iris_d <= 3.3:
                        px[x, oy + y] = (125, 249, 255, 255) if (x + y) % 2 == 0 else (217, 70, 239, 255)
                    else:
                        px[x, oy + y] = (232, 214, 248, 255)
                else:
                    # Eyelid shade
                    shade = 82 if y < 8 else 64
                    px[x, oy + y] = (shade, 44, shade + 24, 255)

    img.save(BLOCK_TEX / "blinking_eye_block.png")
    mcmeta = {"animation": {"frametime": 10, "interpolate": False}}
    (BLOCK_TEX / "blinking_eye_block.png.mcmeta").write_text(
        json.dumps(mcmeta, indent=2) + "\n", encoding="utf-8"
    )


def build_ocular_wood_set() -> None:
    """16x16 ocular_log, ocular_log_top, ocular_planks, ocular_leaves, ocular_sapling."""
    # Log side: dark indigo-violet bark with watching eyes
    log = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    lpx = log.load()
    bark = [(38, 24, 54), (50, 33, 70), (64, 42, 88), (78, 52, 106), (92, 64, 124)]
    for y in range(16):
        for x in range(16):
            idx = (x * 3 + (y // 2) + (x % 3)) % len(bark)
            if x in (3, 7, 12):
                idx = max(0, idx - 2)
            lpx[x, y] = (*bark[idx], 255)
    _draw_mini_eye(lpx, 5, 5, 2, (125, 249, 255))
    _draw_mini_eye(lpx, 11, 11, 2, (217, 70, 239))
    log.save(BLOCK_TEX / "ocular_log.png")

    # Log top: concentric purple rings with cyan core pupil
    ltop = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    ltpx = ltop.load()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d > 6.5:
                ltpx[x, y] = (46, 30, 66, 255)
            elif d > 4.5:
                ltpx[x, y] = (118, 66, 152, 255)
            elif d > 2.5:
                ltpx[x, y] = (158, 92, 196, 255)
            elif d > 1.2:
                ltpx[x, y] = (125, 249, 255, 255)
            else:
                ltpx[x, y] = (28, 16, 42, 255)
    ltop.save(BLOCK_TEX / "ocular_log_top.png")

    # Planks: 4 horizontal twilight-plum boards with seam shadows
    planks = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    ppx = planks.load()
    board_ramp = [(96, 48, 128), (114, 58, 148), (132, 68, 168), (148, 80, 184)]
    for y in range(16):
        for x in range(16):
            if y % 4 == 0 or (y // 4 % 2 == 0 and x == 8) or (y // 4 % 2 == 1 and x == 2):
                ppx[x, y] = (62, 32, 86, 255)
            else:
                idx = ((x + y * 3) % len(board_ramp))
                ppx[x, y] = (*board_ramp[idx], 255)
    planks.save(BLOCK_TEX / "ocular_planks.png")

    # Leaves: cutout alpha twilight magenta foliage with tiny watching eyes
    leaves = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    lfpx = leaves.load()
    leaf_ramp = [(108, 32, 138), (138, 42, 174), (172, 54, 208), (217, 70, 239)]
    for y in range(16):
        for x in range(16):
            if (x * 7 + y * 13) % 5 == 0:
                continue
            idx = (x + y * 2) % len(leaf_ramp)
            lfpx[x, y] = (*leaf_ramp[idx], 255)
    _draw_mini_eye(lfpx, 5, 6, 1, (125, 249, 255))
    _draw_mini_eye(lfpx, 11, 10, 1, (125, 249, 255))
    leaves.save(BLOCK_TEX / "ocular_leaves.png")

    # Sapling: 16x16 cutout mini eye-tree sprout
    sap = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    spx = sap.load()
    for y in range(8, 15):
        spx[7, y] = (78, 52, 106, 255)
        spx[8, y] = (50, 33, 70, 255)
    for y in range(3, 10):
        for x in range(4, 12):
            if math.hypot(x - 7.5, y - 6.0) <= 3.6:
                spx[x, y] = (172, 54, 208, 255) if (x + y) % 2 == 0 else (217, 70, 239, 255)
    _draw_mini_eye(spx, 7, 6, 2, (125, 249, 255))
    sap.save(BLOCK_TEX / "ocular_sapling.png")


def build_great_sky_eye() -> None:
    """256x512 (4 frames of 256x128) cosmic almond Great Sky Eye with soft aura & blinking lids."""
    w, fh = 256, 128
    frames = 4
    openness_list = [1.0, 0.55, 0.08, 0.55]
    img = Image.new("RGBA", (w, fh * frames), (0, 0, 0, 0))
    px = img.load()

    cx, cy = 127.5, 63.5
    for f in range(frames):
        oy = f * fh
        open_amt = openness_list[f]
        for y in range(fh):
            ny = (y - cy) / 42.0
            for x in range(w):
                nx = (x - cx) / 96.0
                # Almond shape: |ny| <= lid_curve(nx)
                if abs(nx) <= 1.0:
                    max_lid = (1.0 - nx * nx) ** 0.72
                else:
                    max_lid = -1.0

                # Soft cosmic halo outside the eye
                halo_dist = math.hypot(nx * 1.15, ny * 1.1)
                if halo_dist < 1.35:
                    halo_alpha = int(max(0.0, (1.35 - halo_dist) / 1.35) ** 2.2 * 130)
                else:
                    halo_alpha = 0

                in_almond = max_lid > 0.0 and abs(ny) <= max_lid
                if not in_almond:
                    if halo_alpha > 0:
                        # Twilight magenta -> cyan cosmic nebula aura
                        mix = max(0.0, min(1.0, 1.0 - halo_dist / 1.35))
                        r = int(140 + 77 * mix)
                        g = int(42 + 40 * mix)
                        b = int(196 + 43 * mix)
                        px[x, oy + y] = (r, g, b, halo_alpha)
                    continue

                # Inside almond: check if covered by blinking eyelid
                lid_open = max_lid * open_amt
                if abs(ny) > lid_open:
                    # Curved twilight-indigo eyelid with glowing lash rim
                    rim_dist = abs(ny) - lid_open
                    if rim_dist < 0.09:
                        px[x, oy + y] = (217, 70, 239, 240)
                    else:
                        shade = int(46 + 24 * (1.0 - abs(ny) / max(0.01, max_lid)))
                        px[x, oy + y] = (shade, 26, shade + 34, 235)
                    continue

                # Visible eye interior (sclera, iris, pupil, specular glint)
                dx = x - cx
                dy = y - cy
                iris_r = math.hypot(dx, dy)
                angle = math.atan2(dy, dx)

                if iris_r < 14.0:
                    # Deep cosmic void pupil + crisp starlight glint
                    glint_d = math.hypot(dx + 5.5, dy + 5.5)
                    if glint_d < 3.6:
                        px[x, oy + y] = (240, 253, 255, 255)
                    elif iris_r > 11.2:
                        px[x, oy + y] = (64, 215, 245, 255)
                    else:
                        px[x, oy + y] = (18, 10, 34, 255)
                elif iris_r < 32.0:
                    # Radial striated cyan -> magenta -> violet iris
                    t = (iris_r - 14.0) / 18.0
                    ray = 0.5 + 0.5 * math.sin(angle * 18.0 + iris_r * 0.25)
                    if t < 0.42:
                        # Bright pupil-glow cyan ring
                        r = int(105 + 45 * ray)
                        g = int(230 + 25 * ray)
                        b = 255
                    elif t < 0.82:
                        # Twilight magenta iris body
                        r = int(190 + 35 * ray)
                        g = int(52 + 38 * ray)
                        b = int(218 + 25 * ray)
                    else:
                        # Dark arcane iris limbal ring
                        r = int(86 + 30 * ray)
                        g = int(32 + 18 * ray)
                        b = int(128 + 30 * ray)
                    px[x, oy + y] = (r, g, b, 250)
                else:
                    # Shaded lavender-moon sclera with subtle edge vignette
                    edge = abs(ny) / max(0.01, max_lid)
                    lum = 1.0 - 0.28 * (edge * edge) - 0.14 * (nx * nx)
                    r = int(236 * lum)
                    g = int(216 * lum)
                    b = int(248 * lum)
                    px[x, oy + y] = (r, g, b, 242)

                # Soft lash outline along the almond edge
                if max_lid - abs(ny) < 0.065:
                    px[x, oy + y] = (43, 25, 66, 245)

    ENV_TEX.mkdir(parents=True, exist_ok=True)
    img.save(ENV_TEX / "great_sky_eye.png")


def build_sky_star_eye() -> None:
    """16x16 soft glowing cosmic star-eye sprite for the Eye Realm sky."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx = (x - 7.5) / 7.5
            dy = (y - 7.5) / 7.5
            d = math.hypot(dx, dy)
            if d < 0.28:
                px[x, y] = (240, 253, 255, 255)
            elif d < 0.55:
                px[x, y] = (125, 249, 255, 210)
            elif d < 1.0:
                a = int(((1.0 - d) / 0.45) ** 2 * 140)
                px[x, y] = (217, 70, 239, a)
    ENV_TEX.mkdir(parents=True, exist_ok=True)
    img.save(ENV_TEX / "sky_star_eye.png")


if __name__ == "__main__":
    build_gazing_portal()
    build_gazing_eye()
    build_gaze_stone()
    build_eye_turf()
    build_blinking_eye_block()
    build_ocular_wood_set()
    build_great_sky_eye()
    build_sky_star_eye()
    print("Generated Phase 4 Eye Realm textures.")
