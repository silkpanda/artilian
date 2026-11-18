# Adventures in Artilian — Sunbeam Legendary Pet Plan (2026-11-17)

Branch: `phase-2-sunbeam-pet`, stacked on `phase-2-flurrywing-pet` (`89e44cd`) because all three new pets add to the same registry, lang, biome, art-script and GameTest files. Rebase onto `main` once the Prismling and Flurrywing PRs merge.
Relates to: `PLAN.md` Phase 2, `PRODUCT.md`, `DESIGN.md`, `qa/Adventures in Artilian - Prismling Legendary Pet Plan 2026-11-17.md` and `qa/Adventures in Artilian - Flurrywing Snow Fairy Pet Plan 2026-11-17.md` (the patterns this follows).

> **Status: Stages 1–2 done (2026-11-17, Claude Code + OpenCode); Stage 3 waiting on the owner's in-game look.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Sunsteel element, Sunbeam entity, sun aura, spawns, GeckoLib model/texture/animations & datagen | Commit `Sunbeam Stages 1-2`. Java by OpenCode from Claude's spec, reviewed by Claude. Model by Claude, headless with the `bbmodel` MCP: source `tools/art/models/sunbeam.bbmodel` (GeckoLib, box UV, 128x128), exported `sunbeam.geo.json` / `sunbeam.animation.json`. 32 px to the head top at 2x = 4 blocks; hitbox `3.0 x 4.0`, wingspan about 6.5 blocks. Bones `root` > `body` > `neck`, `head`, `streamer` > `streamer_mid` > `streamer_tips`, `wing_left`/`wing_right` (shoulder slab plus four slabs and four spikes each), `leg_left`/`leg_right`. The torso flap is on the viewer's right as drawn. Geometry gates (floating, interpenetration, mirror, degenerate) and animation gates (ground sink, detachment, timing) all pass. `make_sunbeam()` paints `sunbeam.png` and `sunbeam_glowmask.png`; run with `--sunbeam` so `crystal_fox.png` isn't overwritten. Review fix: the light position is now saved to NBT (`SunbeamPlacedLight`) so a Sunbeam saved while lit still clears its old light after reload; the spec had said not to save it. `runData` real diffs: `en_us.json` (3 keys), `emberbloom_meadows.json`, new `sunbeam_spawn_egg.json`. |
> | 2 GameTests, red-proofs, capture/faint verification | Same commit. Six `PetRosterGameTests` added, element count test moved 11 to 12. `115/115` green. Red-proofs, each restored and re-run green: dropping the owner check from `applySunAura` turned `sunbeamAuraHealsOwnerAndOwnersPetsOnly` red ("must never heal another player's pet, went to 12.0"); `if (true)` in place of the air check turned `sunbeamLightOnlyReplacesAirAndIsRemovedWithIt` red ("EXPLOITED P2-05: sun aura replaced a real block"); removing `SUNSTEEL` from `WATER`'s strong set turned `sunsteelElementMatchups` red; not saving the light turned the light probe red ("forgot its light ... would orphan it on reload"); removing the cleanup in `remove()` turned it red ("light block left behind"). |
> | 3 Client verification, live evidence, PR | Art pass v2 applied (`Sunbeam Stage 3` on consolidated branch): `make_sunbeam()` (`--sunbeam`) now writes `sunbeam.geo.json`, `sunbeam.png`, and `sunbeam_glowmask.png` with recessed visor, angled solar eyes, forehead diamond, 5-ray solar crown, 4 spiky wing-arms, and attack-only `chest_beam`; stale v1 `tools/art/models/sunbeam.bbmodel` removed. Waiting on the owner's in-game look and screenshot `qa/evidence/sunbeam-2026-11-17.png`. |

## Why

The kids drew "a legendary Sunsteel." It is the second legendary, after Prismling, and the first of the three drawings filed on 2026-11-17 (the frog duck and the lightning guy come later, separate plans).

**Name:** `Sunbeam` (`artilian:sunbeam`). The owner gave this name; it is not a placeholder.

### Owner decisions (2026-11-17)

- **Element:** Sunsteel only. Sunsteel is not an element yet, so it becomes the **12th** `PetElement`, `SUNSTEEL`. Sunbeam is pure Sunsteel, no secondary.
- **Size:** as tall as a ghast, so **4 blocks**.
- **Legendary power:** the **sun aura** (below).

### Art spec (from the drawing)

Source: `qa/reference/pets/sunsteel-drawing-2026-11-17.jpg` (photo, upright, one orange crayon). Notes in `qa/reference/pets/NOTES.md`.

- **Head:** big and round, on a short neck. Two round eyes with dark pupils, a wide smile (a V-shaped grin).
- **Chest:** a round scribbled disc just below the neck: read as a glowing sun core.
- **Streamer:** a long solid-orange flame streamer hangs from the sun core down the front of the body, past the bottom of the body, splitting into strands at the end.
- **Body:** a tall rectangle under the head, with a little flap on the right side.
- **Legs:** two short legs below the body with small feet.
- **Wings:** two big angular wings from the upper body, one each side, reaching outward and up past the head. Each wing has spiky "X"/"Y" claw spikes at its tips and corners.

Model translation (blocky, matching the roster):
- Sunsteel gold-orange body (`#F59E0B` / `#FB923C`) with darker burnt-orange outline pixels (`#C2410C`) to echo the crayon line.
- Rounded head from stacked boxes (wide box plus narrower top/bottom caps), cream-gold face, dark eyes, dark V grin.
- Chest sun core: a small protruding disc, bright yellow-white.
- Streamer: three chained segments (yellow at the core, orange, red-orange tips) so it can wave.
- Wings: thin flat planes, warm orange membrane with darker struts, three spike boxes at each tip.
- Glow mask (`sunbeam_glowmask.png`, `AutoGlowingGeoLayer` like Prismling): sun core, streamer, wing spikes, eyes.

### Size

Ghast-tall: **4.0 blocks** to the top of the head, hitbox **3.0 x 4.0** (Prismling 2.8 x 4.1). The model is 32 px tall rendered at 2x. Wingspan reads about 5 blocks, outside the hitbox. Like Prismling, it will not fit through doors or 2-high tunnels; the owner asked for ghast size knowing Prismling's trade-off.

### The Sunsteel element

`SUNSTEEL("sunsteel", 0xFBBF24, ParticleTypes.WAX_ON)`: sun-forged metal. Matchups (1.5x strong / 0.75x resisted):
- **Sunsteel attacking:** strong vs `SHADOW` (sun drives out shadow), `ICE` (melts it), `DRAGON` (sunsteel is what dragon-slayer gear is made of); resisted by `FIRE` and `EARTH`.
- **Attacking Sunsteel:** `WATER` and `ELECTRIC` are strong vs Sunsteel (rust, metal conducts): add `SUNSTEEL` to their strong sets. `SHADOW` and `ICE` are resisted by Sunsteel: add `SUNSTEEL` to their weak sets.
- No other existing matchup changes.
- **Signature effect:** target gets `GLOWING` 60 ticks and `WEAKNESS` 60 ticks ("dazzled by the sun").
- **Lang:** `element.artilian.sunsteel` = "Sunsteel".
- Prismling's all-types rule iterates `PetElement.values()`, so it picks up Sunsteel automatically.

### Sunbeam rules

- **Legendary stats:** 30 health, 5.0 attack, speed 0.30 (same tier as Prismling).
- **Hover:** like Flurrywing. Legs dangle; it glides. `causeFallDamage` returns `false`; downward motion damped (`y * 0.65`) in the air when not sitting.
- **Sun aura (the legendary power):**
  - **Light:** Sunbeam carries its own light. Server side, it keeps one `minecraft:light` block (level 15) at its chest position, moving it as Sunbeam moves. It only ever places into **air** and only removes a light block it placed itself. When Sunbeam is removed for any reason (recalled to crystal, fainted, unloaded, discarded) the light block is removed. Wild and tamed Sunbeams both glow.
  - **Healing:** while tamed and not sitting, every 40 ticks it gives its owner within 8 blocks `REGENERATION` I for 60 ticks, and heals each of the owner's other Artilian pets within 8 blocks by 1 health. Never heals anyone else's player or pet, never heals hostiles.
  - Ambient particles: `WAX_ON` sparks and an occasional `FLAME` drifting off the streamer.
- **Spawning:** rare, `Emberbloom Meadows` only (the sunny ember biome) on grass-like ground or Ember Turf, weight **1**, group 1.
- **Sounds:** happy `AMETHYST_BLOCK_CHIME`, ambient `ALLAY_AMBIENT_WITHOUT_ITEM`, hurt `ALLAY_HURT`. (Blaze sounds were considered and dropped as too scary for the kids.)
- **Spawn egg:** `0xFB923C` / `0xFDE68A`.
- **Crystal badge:** "Sunsteel" via the normal element badge.

## Stages

### Stage 1: Sunsteel element, Sunbeam entity, sun aura, spawns, GeckoLib model/texture/animations & datagen
- **Changes:**
  - `PetElement.SUNSTEEL` with matchups, signature effect, lang.
  - `SunbeamEntity`, registered `3.0 x 4.0`, eye height 3.3, attributes, spawn placement, spawn egg, creative tab, Emberbloom spawn.
  - `SunbeamModel`/`SunbeamRenderer` (2x scale, shadow 1.6, `AutoGlowingGeoLayer`).
  - Model built headless with the `bbmodel` MCP: `tools/art/models/sunbeam.bbmodel`, exported `sunbeam.geo.json`, `sunbeam.animation.json` (`idle`, `walk`, `sit`, `attack`), `sunbeam.png`, `sunbeam_glowmask.png`.
  - `./gradlew runData`.
- **Done When:** `./gradlew runData` and `./gradlew build` are clean.

### Stage 2: GameTests, red-proofs, capture/faint verification
- **Changes:** existing element-count test goes from 11 to 12. New `PetRosterGameTests`:
  - `sunsteelElementMatchups`: Sunsteel 1.5x vs Shadow/Ice/Dragon, 0.75x vs Fire/Earth; Water and Electric 1.5x vs Sunsteel; Shadow and Ice 0.75x vs Sunsteel.
  - `sunbeamIsSunsteelLegendaryGhastSizedAndSpawnsInEmberbloom`
  - `sunbeamHoversAndTakesNoFallDamage`
  - `sunbeamAuraHealsOwnerAndOwnersPetsOnly`
  - `sunbeamLightOnlyReplacesAirAndIsRemovedWithIt` (world-protection probe)
  - `sunbeamCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal`
- **Red-proof:**
  - Drop the owner check from the aura heal → the heals-owner-only test turns red → restore.
  - Let the light placement ignore the air check → the light test turns red → restore.
  - Remove `SUNSTEEL` from `WATER`'s strong set → the matchup test turns red → restore.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` pass 100%.

### Stage 3: Client verification, live evidence, PR
- **Changes:** `./gradlew runClient`, spawn Sunbeam at night: check it lights the ground around it, wings and streamer read as drawn, glow layer works, no model/texture warnings in `run/logs/latest.log`. Save `qa/evidence/sunbeam-2026-11-17.png`. Update `PLAN.md` Now and open the PR.
- **Done When:** CI green, evidence saved, PR open.

## Security gate

Sunbeam places and removes `minecraft:light` blocks, which touches world protection. Rule: it only places into air and only removes light blocks it placed. Probe `sunbeamLightOnlyReplacesAirAndIsRemovedWithIt` is red-proven then HELD; entry `P2-05` in `security/FINDINGS.md`. The aura heal only reaches the owner and the owner's pets (`sunbeamAuraHealsOwnerAndOwnersPetsOnly`). `SunbeamEntity` extends `ArtilianPetEntity` and inherits `P2-01`–`P2-04`; `PetSecurityProbes` must stay green.

## Out of scope

- The frog duck and the lightning guy (their own plans).
- Sunsteel gear or wands gaining the Sunsteel pet element.
- True free flight (it glides like Flurrywing).
- Legendary-only capture rules.
