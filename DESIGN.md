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
