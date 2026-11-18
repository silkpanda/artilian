# Adventures in Artilian — Prismling Legendary Pet Plan (2026-11-17)

Branch: `phase-2-prism-pet` (from `main` at `9a1655b`).
Relates to: `PLAN.md` Phase 2, `PRODUCT.md`, `DESIGN.md`, `qa/archive/` pet roster plans, `qa/Adventures in Artilian - Dragon and Eye Pets Plan 2026-11-16.md` (the pattern this follows).

> **Status: Stages 1–3 complete; PR open, waiting on CI.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 All-types battle rule, Prismling entity, spawns, GeckoLib model/texture/animations & datagen | Built by Claude Code in the Stage 1–2 commit on `phase-2-prism-pet`. `ElementalMob#isAllTypes`, `ArtilianPetEntity#attackElementAgainst`, `PrismlingEntity`, 64x64 texture from `make_prismling()`, rendered at 2x scale with a 1.6 x 2.0 hitbox (owner asked for it substantially larger than Peepkin). Clean `runData` (lang + three biome spawn entries + spawn egg model) and `build`. |
> | 2 GameTests, red-proofs, capture/faint verification | Same commit. Four `PetRosterGameTests` added. Red-proven: `attackElementAgainst` returning the primary turned `prismlingAttacksWithTheBestElementAgainstEveryTarget` red (picked STARLIGHT 1.0x vs Creeper, best is 1.5x); `isAllTypes` returning `false` turned `prismlingTakesNeutralDamageFromEveryElement` (SHADOW hit for 1.5x) and `prismlingIsAllTypesLegendarySizedAndSpawnsInAllThreeBiomes` red. Restored: `105/105` `runGameTestServer` green. |
> | 3 Client verification, live evidence, PR | In-game check turned into an art rework: new 256x128 geo, texture and animations, `prismling_glowmask.png` with `AutoGlowingGeoLayer` (visor eyes, crest, cuffs, crystal veins, floating element symbols), hitbox 1.6 x 2.0 to 2.8 x 4.1, eye height 3.3, shadow 1.75. Owner chose to ship the 4.1-block size. Rainbow reads red on viewer-left. No Prismling model or texture warnings in `run/logs/latest.log`. Evidence: `qa/evidence/prismling-2026-11-17.png`. Claude Code re-ran `build` + `runGameTestServer`: `105/105` green. Commit + PR by Claude Code. |

## Why

The kids drew a new pet for everyone (not tied to Alex or Steve): "a rainbow legendary pet that is all types." It is the first legendary in the game.

**Name:** `Prismling` (`artilian:prismling`) is a placeholder display name. The kids pick the real name; only the `en_us` display name changes when they do. The registry id stays `prismling` so captured crystals keep working.

### Art spec (from the drawing)

Source: `qa/reference/pets/prismling-drawing-2026-11-17.jpg` (photo, sideways) and `qa/reference/pets/prismling-drawing-upright-2026-11-17.jpg` (rotated upright). Owner confirmed orientation: head on the right of the photo, legs on the left, arms top and bottom.

Upright, it is a blocky creature:
- **Body:** a wide, short box (about 2.3x wider than tall) outlined in orange, filled with seven vertical rainbow bands left to right: red, orange, yellow, teal, sky blue, purple, lavender.
- **Head:** a square box sitting on top of the body (the drawing has it within a pixel of center, so the model centers it), pale/cream with an orange outline and a big orange U-shaped smile.
- **Legs:** two short square block legs under the body, the left one red, the right one lavender (matching the bands at each end).
- **Arms:** thin orange stick arms sticking straight out sideways from each end of the body, ending in a three-pronged forked hand, each with a blue oval band (a cuff) partway along.
- **The type icons:** small symbols drawn around it (yellow cloud, fire, purple orb, gray block, sun, blue snowflake-star, brown rock) are the elements. In-game they become the ambient particles cycling through all 11 element particles.

### The all-types rule

Pets carry a primary and an optional secondary element, so "all types" needs a rule of its own:
- **Attacking:** on each hit, Prismling picks whichever of the 11 elements does the most damage against the target (ties broken randomly) and uses that element's damage multiplier, hit particle, and signature effect.
- **Defending:** it has no weaknesses and no resistances. Every elemental attack against it uses the neutral 1.0x multiplier.
- **Crystal badge:** shows "All Types" instead of a single element.
- **Spawning:** rare, in all three Overworld biomes (Emberbloom Meadows, Starlight Highlands, Crystal Canopy Woods) at spawn weight 1, group size 1. The other pets use weights 3 to 6, so it shows up roughly a fifth as often as a typical pet.
- **Size:** the owner asked for it substantially larger than Peepkin. Stage 3's art rework made it a towering guardian: renders at 2x, about 4.1 blocks tall (about 5 with the floating element-symbol arc), hitbox 2.8 x 4.1 (Peepkin is 0.72 x 0.85). Owner chose to ship this size on 2026-11-17, knowing it won't fit through doors, 2-high tunnels or dense woods. A GameTest holds it at 2x Peepkin or bigger.
- **Stats:** 30 health (other pets 22), 5.0 attack (Peepkin 4.5), speed 0.30. A step above the roster, and still beatable by a well-matched pet.

## Stages

### Stage 1: All-types battle rule, Prismling entity, spawns, GeckoLib model/texture/animations & datagen
- **Changes:**
  - `ElementalMob#isAllTypes()` default `false`; `PetElement.elementsOf` returns empty (neutral) for all-types mobs.
  - `ArtilianPetEntity#attackElementAgainst(LivingEntity)` default returns the primary element; `PetService.performElementalAttack` uses it for multiplier, particle, and signature effect.
  - `PrismlingEntity` (primary `STARLIGHT` as storage fallback, `isAllTypes() == true`), registered, attributes, spawn placement, spawn egg, creative tab, biome spawns in all three biomes.
  - Crystal tooltip badge reads "All Types" for Prismling.
  - `PrismlingModel`/`PrismlingRenderer`, `prismling.geo.json`, `prismling.animation.json` (`idle`, `walk`, `sit`, `attack`), texture from `tools/art/make_pets_art.py`, lang entries, `./gradlew runData`.
- **Tests, written first and seen failing:** written in Stage 2.
- **Red-proof:** Stage 2.
- **Done When:** `./gradlew runData` and `./gradlew build` are clean.

### Stage 2: GameTests, red-proofs, capture/faint verification
- **Changes:** `PetRosterGameTests` gains:
  - `prismlingIsAllTypesLegendarySizedAndSpawnsInAllThreeBiomes`
  - `prismlingAttacksWithTheBestElementAgainstEveryTarget`
  - `prismlingTakesNeutralDamageFromEveryElement`
  - `prismlingCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal`
- **Red-proof:**
  - Make `PrismlingEntity#attackElementAgainst` return `getPrimaryElement()` → the best-element test turns red → restore.
  - Make `PrismlingEntity#isAllTypes` return `false` → the neutral-defense test turns red → restore.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` pass 100%.

### Stage 3: Client verification, live evidence, PR
- **Changes:** Antigravity runs `./gradlew runClient`, spawns Prismling standing, walking, sitting and attacking, checks no missing-model/texture warnings in `run/logs/latest.log`, and saves `qa/evidence/prismling-2026-11-17.png`. Check the rainbow reads red on the creature's viewer-left like the drawing; if it's mirrored, swap the band order in `make_prismling()` and the two leg colors. Then update `PLAN.md` Now and open the PR.
- **Done When:** CI green, evidence saved, PR open.

## Security gate

n/a for new controls. `PrismlingEntity` extends `ArtilianPetEntity` and inherits `P2-01`–`P2-04` (crystal UUID ownership, non-lethal fainting, anti-duplication roster, friendly-target filtering). `PetSecurityProbes` must stay green.

## Out of scope

- The kids' official name for Prismling (placeholder until they pick).
- A twelfth `PetElement` enum value or any change to existing matchup tables.
- The second new creature (separate plan once its drawing arrives).
- Legendary-only capture rules (e.g. harder capture odds). Not asked for.
