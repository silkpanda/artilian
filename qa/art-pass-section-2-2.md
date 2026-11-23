# Art Pass — Section 2.2: Hero Weapons & Staves

- **Branch:** `art/pass-2-weapons-items`
- **Commit:** `0a27088`
- **Generator:** `tools/art/make_hero_weapons_art.py`

## What Changed

Repainted all 11 `16×16` hero weapon and staff icons in `src/main/resources/assets/artilian/textures/item/` with distinct silhouettes, hue-shifted shading, and v3 armor-set alignment:

### Swords & Blades (5)
- `sunsteel_sword.png` — **Solar Broadsword** (obsidian-crimson steel, hammered gold sun-guard & fuller, molten core)
- `starlight_sword.png` — **Arcane Ranger Sabre** (indigo steel, cyan crystal edge, swept guard, leather grip, teardrop pommel)
- `prismatic_sword.png` — **Admiral's Cutlass** (curved blade, gold knuckle-bow D-guard, violet & magma prism inlay)
- `dragonscale_sword.png` — **Dragon-Hunter Fangblade** (serrated emerald-scale blade, drake-bone horn guard, amber core)
- `twin_star_blade.png` — **Seraphic Twin-Star Greatsword** (platinum-silver & void-indigo blade, winged gold/platinum guard, cyan core)

### Wands & Staves (6)
- `spark_wand.png` — Faceted cyan-teal crystal spire wand with floating spark glints
- `heart_bloom_staff.png` — Rose-pink heart blossom cradled by emerald leaves on a warm wood shaft
- `zephyr_dash_wand.png` — Swept sky-wing & mana-violet wind-gem wand
- `prism_nova_staff.png` — Dual-faceted violet/cyan prism astrolabe staff
- `brimstone_meteor_staff.png` — Obsidian-clawed molten meteor staff
- `twin_star_staff.png` — Golden crescent & platinum wing cradle holding orbiting amber-gold and starlight-cyan stars

## Verification

- `python tools/art/make_hero_weapons_art.py` — generated 11 icons in `src/main/resources/assets/artilian/textures/item`, visually inspected via magnified contact sheet.
- `python tools/art/check_armor_sheets.py` — clean across all 5 armor sets.
- `python tools/art/lint_art.py --phase5 --strict` — clean across all Phase 5 entities and armors.
- `./gradlew build` — `BUILD SUCCESSFUL`.

## Next

- Start **Section 2.3 (Tool Sets, Boss Summons & Trophies — 39 items)** on `art/pass-2-weapons-items`.
