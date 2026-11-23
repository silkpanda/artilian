"""Generate Phase 5 Dragon Jungle textures (Primal Portal + Stage 2 Mega-Jungle blocks)."""

import json
import math
import random
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
BLOCK_TEX = ROOT / "src/main/resources/assets/artilian/textures/block"


def _clamp(v: int) -> int:
    return max(0, min(255, v))


def _shade(rgb: tuple[int, int, int], delta: int) -> tuple[int, int, int, 255]:
    return (_clamp(rgb[0] + delta), _clamp(rgb[1] + delta), _clamp(rgb[2] + delta), 255)


def build_primal_portal() -> None:
    """Create a 4-frame 16x64 animated translucent emerald-amber portal texture + .mcmeta."""
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
                ripple = math.cos((x - y) * 0.65 + phase)

                if dist < 0.25 + 0.04 * math.sin(phase):
                    r, g, b, a = 253, 186, 116, 232
                elif swirl > 0.38:
                    r, g, b, a = 249, 115, 22, 210
                elif ripple > 0.20:
                    r, g, b, a = 34, 197, 94, 195
                else:
                    r, g, b, a = 20, 83, 45, 175

                px[x, f * 16 + y] = (r, g, b, a)

    BLOCK_TEX.mkdir(parents=True, exist_ok=True)
    img.save(BLOCK_TEX / "primal_portal.png")
    mcmeta = {"animation": {"frametime": 3, "interpolate": True}}
    (BLOCK_TEX / "primal_portal.png.mcmeta").write_text(
        json.dumps(mcmeta, indent=2) + "\n", encoding="utf-8"
    )


def build_primal_basalt() -> None:
    """Volcanic slate-basalt with subtle warm ember mineral flecks."""
    rng = random.Random(5021)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    base = (41, 47, 56)
    for y in range(16):
        for x in range(16):
            grain = rng.randint(-10, 10)
            streak = -6 if ((x + y * 2) % 7 == 0) else 0
            if rng.random() < 0.05:
                # Subtle ember-amber mineral fleck
                px[x, y] = (140, 78, 36, 255)
            else:
                px[x, y] = _shade(base, grain + streak)
    img.save(BLOCK_TEX / "primal_basalt.png")


def build_jungle_turf() -> None:
    """Lush emerald moss-turf top and earthy loam + overhanging moss side."""
    rng = random.Random(5022)
    top = Image.new("RGBA", (16, 16))
    tpx = top.load()
    emerald = (34, 185, 86)
    for y in range(16):
        for x in range(16):
            delta = rng.randint(-18, 18)
            if rng.random() < 0.10:
                tpx[x, y] = (74, 222, 128, 255)
            else:
                tpx[x, y] = _shade(emerald, delta)
    top.save(BLOCK_TEX / "jungle_turf_top.png")

    side = Image.new("RGBA", (16, 16))
    spx = side.load()
    loam = (96, 66, 44)
    for y in range(16):
        for x in range(16):
            drip = 3 + ((x * 5 + 3) % 4)
            if y < drip:
                spx[x, y] = _shade(emerald, rng.randint(-14, 14))
            elif y == drip:
                spx[x, y] = _shade((22, 128, 61), rng.randint(-10, 10))
            else:
                if rng.random() < 0.08:
                    spx[x, y] = _shade((54, 60, 70), rng.randint(-6, 6))
                else:
                    spx[x, y] = _shade(loam, rng.randint(-12, 12))
    side.save(BLOCK_TEX / "jungle_turf_side.png")


def build_amber_moss() -> None:
    """Deep jungle moss veined with glowing prehistoric amber (#F97316 -> #FDBA74)."""
    rng = random.Random(5023)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    moss = (24, 102, 54)
    for y in range(16):
        for x in range(16):
            vein = math.sin(x * 0.7 + y * 0.5) + math.cos(x * 0.4 - y * 0.8)
            if vein > 1.25:
                px[x, y] = (253, 186, 116, 255)
            elif vein > 0.75:
                px[x, y] = (249, 115, 22, 255)
            else:
                px[x, y] = _shade(moss, rng.randint(-14, 16))
    img.save(BLOCK_TEX / "amber_moss.png")


def build_dragonwood_set() -> None:
    """Dragonwood log (side + top), planks, leaves, and sapling."""
    rng = random.Random(5024)

    # Log side: dark mahogany bark with mossy-emerald vertical crevices
    log = Image.new("RGBA", (16, 16))
    lpx = log.load()
    bark = (68, 45, 34)
    for y in range(16):
        for x in range(16):
            if x % 4 == 1 and (y + x) % 5 == 0:
                lpx[x, y] = _shade((34, 139, 72), rng.randint(-10, 10))
            elif x % 4 == 0:
                lpx[x, y] = _shade(bark, -14 + rng.randint(-6, 6))
            else:
                lpx[x, y] = _shade(bark, rng.randint(-8, 12))
    log.save(BLOCK_TEX / "dragonwood_log.png")

    # Log top: concentric amber-brown heartwood rings
    log_top = Image.new("RGBA", (16, 16))
    ltpx = log_top.load()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d > 6.5:
                ltpx[x, y] = _shade(bark, rng.randint(-8, 8))
            elif int(d) % 2 == 0:
                ltpx[x, y] = _shade((168, 102, 48), rng.randint(-8, 8))
            else:
                ltpx[x, y] = _shade((128, 76, 38), rng.randint(-8, 8))
    log_top.save(BLOCK_TEX / "dragonwood_log_top.png")

    # Planks: 4 horizontal boards with warm prehistoric amber-mahogany grain
    planks = Image.new("RGBA", (16, 16))
    ppx = planks.load()
    plank_base = (132, 82, 46)
    for y in range(16):
        for x in range(16):
            seam = (y % 4 == 0) or (x == (y // 4) * 5 % 16)
            if seam:
                ppx[x, y] = _shade(plank_base, -28)
            else:
                ppx[x, y] = _shade(plank_base, rng.randint(-10, 12))
    planks.save(BLOCK_TEX / "dragonwood_planks.png")

    # Leaves: layered emerald canopy with tiny glowing amber buds
    leaves = Image.new("RGBA", (16, 16))
    lfpx = leaves.load()
    leaf_base = (28, 160, 76)
    for y in range(16):
        for x in range(16):
            r = rng.random()
            if r < 0.14:
                lfpx[x, y] = (0, 0, 0, 0)
            elif r < 0.19:
                lfpx[x, y] = (251, 146, 60, 255)
            else:
                lfpx[x, y] = _shade(leaf_base, rng.randint(-24, 24))
    leaves.save(BLOCK_TEX / "dragonwood_leaves.png")

    # Sapling: 16x16 transparent cutout sprout with fern fronds and amber core
    sapling = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    spx = sapling.load()
    # Stem
    for y in range(7, 16):
        spx[7, y] = (74, 48, 34, 255)
        spx[8, y] = (92, 60, 42, 255)
    # Canopy fronds
    for y in range(2, 9):
        for x in range(3, 13):
            dist = math.hypot((x - 7.5) / 4.5, (y - 5.0) / 3.2)
            if dist <= 1.0:
                if abs(x - 7.5) < 1.2 and 4 <= y <= 6:
                    spx[x, y] = (249, 115, 22, 255)
                else:
                    spx[x, y] = _shade((34, 197, 94), rng.randint(-20, 18))
    sapling.save(BLOCK_TEX / "dragonwood_sapling.png")


def main() -> None:
    BLOCK_TEX.mkdir(parents=True, exist_ok=True)
    build_primal_portal()
    build_primal_basalt()
    build_jungle_turf()
    build_amber_moss()
    build_dragonwood_set()
    print("Generated Dragon Jungle textures in", BLOCK_TEX)


if __name__ == "__main__":
    main()
