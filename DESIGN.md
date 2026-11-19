# Design Tokens & Guidelines — Adventures in Artilian

## Foundations

### Typography
- **Display / Headings:** Minecraft pixel-art display style / crisp bold bitmap headers for Boss Bars, Dimension Banners, and Title Plates
- **Body / Interface:** Default Minecraft UI font (`minecraft:default`) for high readability at 1080p & kid-friendly GUI scales
- **Code / Monospace:** N/A (in-game tooltips & item lore use clean Minecraft formatting codes)

### Color Palette
- **Canvas / Background (UI Panels):** `#2B2338` (Deep Artilian Slate) / `#C6C6C6` (Classic Chest-compatible neutral frame for Chester UI)
- **Surface / Card:** `#3A2F4D` (Elevated Arcane Panel)
- **Primary / Brand:** `#38D9C0` (Starlight Teal) & `#FF9F38` (Sunsteel Gold)
- **On Primary (Text):** `#FFFFFF` / `#FFF6D6`
- **Border / Hairline:** `#6A5A8C` (Arcane Trim)
- **Muted / Secondary:** `#B4A9C8` (Soft Lavender Grey)
- **Semantic Accents:**
  - Mana Bar: `#47B8FF` → `#9C5CFF`
  - Pet Health / Healing: `#4CD964`
  - Boss / Danger Telegraph: `#FF4D5A`
  - Eye Realm Accent: `#D946EF` (Twilight Magenta) & `#7DF9FF` (Pupil Glow)
  - Dragon Jungle Accent: `#22C55E` (Canopy Emerald) & `#F97316` (Ember Amber)

## Surface & Hierarchy
- **Readability First (Ages 5 & 7):** Keep HUD elements minimal (one compact Mana bar near the hotbar, clean boss bars, clear pet status icon).
- **No Crowded Screens:** Chester's 27-slot UI and Pet Capture tooltips should use clear icons, short names, and color-coded stats rather than walls of text.
- **Clear Telegraphs:** Every boss AoE or explosive fuse uses distinct ground rings, glowing particles, and rising audio pitch at least 1.5 seconds before impact.

## Component Conventions
- **Cartoony Entity Language (GeckoLib):** Exaggerated eyes, bouncy walk cycles, and expressive idle animations (Chester blinking & chomping; snapping flat into a vanilla-style normal chest when sleeping).
- **Color-Coded Tooltips:** Weapons, Wands/Staves, Capture Crystals, and TNTs show concise 1–2 line kid-readable effect tags (`Safe Blast`, `Heals Pets`, `Captures Biome Pets`).

## 3D Entity, Boss & Structure Art Standard

First-pass art must meet this bar on pass 1 rather than shipping flat programmer-art placeholders that require a full redo later.

- **Texture Sheet Budget:** Minimum `128×128` for articulated pets and structure enemies; `256×128` (or larger) for legendary pets and bosses. Never pack an articulated creature onto `64×64` with stretched or overlapping UVs.
- **Silhouette & 3D Depth:** Build layered body mass (broad shoulders/pauldrons, tapered waist, distinct limbs) and include angled/rotated geometry (horns, spikes, Z-kink bolts, claws, wings, floating shards). Recessed features (visors, eye sockets, chest cores, snouts) must use real 3D inset/outset cubes — never a flat box with a face painted onto one plane.
- **Value & Material Contrast:** Anchor bright accents and `_glowmask.png` zones against a darker structural base (arcane slate, storm-navy, deep bronze, or rich shadow tones) so glowing eyes, crests, and runes pop cleanly instead of blending into bright-on-bright mush.
- **Surface Detail (No Flat Faces):** Use 6–8 stop color ramps, directional top/bottom shading, inner bevels, and deterministic pixel-art texture (stone strata/cracks, forged metal seams, faceted crystal highlights, or organic shading).
  - **Banned v1 anti-patterns:** single-color flat faces, uniform 1px neon box outlines around every cube, and overlapping UV rectangles.
- **Translating Kid Drawings (`qa/reference/pets/`):** Preserve the kid's *concept, colors, and signature features* faithfully, but translate 2D crayon shapes into 3D Minecraft/Warden/Iron-Golem-grade craft (armored plates, glowing visors, articulated limbs) rather than literal stick-figure cubes or flat smiley faces.
- **Bosses & Enemies Without Drawings:** Establish a short visual reference or silhouette spec in `qa/reference/` before modeling so every agent builds toward the same target.
- **Structures:** Avoid plain boxy cubes; use depth (recessed walls, roof overhangs, stepped stairs, pillars, and the 15 building-block variants) consistent with `qa/reference/structures/`.
