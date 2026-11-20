"""Generate Phase 4 Stage 1 Eye Realm portal & Gazing Eye textures."""

import json
import math
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
BLOCK_TEX = ROOT / "src/main/resources/assets/artilian/textures/block"
ITEM_TEX = ROOT / "src/main/resources/assets/artilian/textures/item"


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


if __name__ == "__main__":
    build_gazing_portal()
    build_gazing_eye()
    print("Generated gazing_portal.png, gazing_portal.png.mcmeta, and gazing_eye.png")
