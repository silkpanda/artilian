"""Generate Phase 5 Stage 1 Dragon Jungle Primal Portal animated texture."""

import json
import math
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
BLOCK_TEX = ROOT / "src/main/resources/assets/artilian/textures/block"


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
                    # Bright ember-amber primal core (#F97316 -> #FDBA74)
                    r, g, b, a = 253, 186, 116, 232
                elif swirl > 0.38:
                    # Ember amber swirl band (#F97316)
                    r, g, b, a = 249, 115, 22, 210
                elif ripple > 0.20:
                    # Canopy emerald midtone (#22C55E)
                    r, g, b, a = 34, 197, 94, 195
                else:
                    # Deep jungle moss-emerald backdrop (#14532D)
                    r, g, b, a = 20, 83, 45, 175

                px[x, f * 16 + y] = (r, g, b, a)

    BLOCK_TEX.mkdir(parents=True, exist_ok=True)
    img.save(BLOCK_TEX / "primal_portal.png")
    mcmeta = {"animation": {"frametime": 3, "interpolate": True}}
    (BLOCK_TEX / "primal_portal.png.mcmeta").write_text(
        json.dumps(mcmeta, indent=2) + "\n", encoding="utf-8"
    )


def main() -> None:
    build_primal_portal()
    print("Generated Primal Portal texture + mcmeta in", BLOCK_TEX)


if __name__ == "__main__":
    main()
