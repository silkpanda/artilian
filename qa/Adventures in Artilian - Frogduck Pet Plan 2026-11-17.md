# Adventures in Artilian — Frogduck Pet Plan (2026-11-17)

Branch: `phase-2-frog-duck-pet`, stacked on `phase-2-sunbeam-pet` (`0cc374e`) because the new kid-drawn pets add to the same registry, lang, biome, art-script, and GameTest files. Rebase onto `main` as earlier PRs merge.
Relates to: `PLAN.md` Phase 2, `PRODUCT.md`, `DESIGN.md`, `qa/reference/pets/NOTES.md`, `qa/Adventures in Artilian - Flurrywing Snow Fairy Pet Plan 2026-11-17.md`, and `qa/Adventures in Artilian - Sunbeam Legendary Pet Plan 2026-11-17.md` (the patterns this follows).

> **Status: Stages 1–2 done (2026-11-17, Antigravity); Stage 3 waiting on the owner's in-game look.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Frogduck entity, amphibious traits, spawns, GeckoLib model/texture/animations & datagen | `FrogduckEntity` (`WATER` / `NATURE`, 24 HP, 4.5 ATK, 0.30 speed), amphibious (`canDrownInFluidType` false, short-hop `< 5.0F` fall-damage immunity), Crystal Canopy Woods spawn (weight 4, group 1–2). `FrogduckModel`/`FrogduckRenderer` (1.15x scale, shadow 0.75). `frogduck.geo.json` + `frogduck.animation.json` (`idle`, `walk`, `sit`, `attack`): teal frog body with front face plate (brown eyes, two teal dots, brown cloud), four legs with 3-clawed feet, and orange duck with outstretched wings perched on top of its head. `make_frogduck()` in `tools/art/make_pets_art.py` (`--frogduck`) paints `frogduck.png`. `runData` real diffs: `en_us.json`, `crystal_canopy_woods.json`, `frogduck_spawn_egg.json`. |
> | 2 GameTests, red-proofs, capture/faint verification | Four `PetRosterGameTests` added (`119/119` green). Red-proofs: removing short-hop `< 5.0F` guard turned `frogduckBreathesUnderwaterAndResistsShortHops` red; shrinking hitbox to `0.8 x 0.9` turned `frogduckIsWaterNatureAndSpawnsInCrystalCanopyWoods` red. Both restored and re-run green (`119/119`). |
> | 3 Client verification, live evidence, PR | Pending owner in-game spawn check & screenshot (`qa/evidence/frogduck-2026-11-17.png`) and PR. |

## Why

The kids drew a frog-duck creature (`qa/reference/pets/frog-duck-drawing-2026-11-17.jpg`, notes in `qa/reference/pets/NOTES.md`). It is the second of the three drawings filed on 2026-11-17 (after Sunbeam; the lightning guy comes next in its own plan).

**Name:** `Frogduck` (`artilian:frogduck`) is a placeholder display name until the kids pick their official name; only the `en_us` display name changes when they do. The registry id stays `frogduck` so captured crystals keep working.

### Art spec (from the drawing)

Source: `qa/reference/pets/frog-duck-drawing-2026-11-17.jpg` and `qa/reference/pets/NOTES.md`:
- **One single character:** a teal frog with an orange duck perched on/fused to the top of its head.
- **Teal frog body:** wide, stout teal body (`#14B8A6` / `#2DD4BF` / `#0D9488`). Its face is in the **middle of the body** (an outlined front face plate):
  - Two round **brown eyes** (`#78350F` / `#92400E`)
  - Two small **teal dots** (nostrils) below the eyes
  - A **brown cloud shape** below the dots (mouth/belly patch)
- **Four legs with clawed feet:** two front/side legs and two rear/bottom legs, each ending in a wide foot with 3 clawed toes.
- **Orange duck on top of the head:** part of the same creature, sitting on the crown:
  - Small rounded/bilobed orange head at the top with two dark slit eyes and a small orange-gold beak/crest overlapping the top of the frog's head
  - Two wide outstretched orange wings (left and right) with scalloped/feathered undersides.

### Size & Elements

- **Elements:** `WATER` primary, `NATURE` secondary (the roster's first `WATER` primary pet; no changes to `PetElement` matchup tables).
- **Regular pet:** normal spawn weight and roster stats.
- **Hitbox:** `1.25 x 1.35` (stout wide frog body + duck on top), eye height `0.75`. Model scaled `1.25F` so the 17.5-px tall model renders ~1.37 blocks tall.

### Rules

- **Stats:** 24 health, 4.5 attack, speed 0.30, follow range 28.0.
- **Amphibious:** `canDrownInFluidType` returns `false` (or `drown` immunity via `canBreatheUnderwater` / `isPushedByFluid` friendly) and `causeFallDamage` ignores short hops (`fallDistance < 5.0F`), taking normal reduced fall damage otherwise. Ambient `ParticleTypes.SPLASH` when in water or `ParticleTypes.HAPPY_VILLAGER` / `SPLASH` occasionally.
- **Spawning:** `Crystal Canopy Woods` only, on `BlockTags.ANIMALS_SPAWNABLE_ON` or `ArtilianBlocks.CRYSTAL_TURF.get()`, spawn weight **4**, group size **1–2**.
- **Sounds:** happy `SoundEvents.AMETHYST_BLOCK_CHIME`, ambient `SoundEvents.FROG_AMBIENT`, hurt `SoundEvents.FROG_HURT`.
- **Spawn egg:** `0x14B8A6` (teal) / `0xF97316` (orange).

## Stages

### Stage 1: Frogduck entity, amphibious traits, spawns, GeckoLib model/texture/animations & datagen
- **Changes:**
  - `FrogduckEntity` extends `ArtilianPetEntity`: `WATER` / `NATURE`, attributes above, `checkFrogduckSpawnRules`, amphibious water breathing, hop fall-damage tolerance (`fallDistance < 5.0F`), sounds, GeckoLib controllers (`idle`, `walk`, `sit`, `attack`).
  - Registered in `ArtilianEntities` (`FROGDUCK`, sized `1.25 x 1.35`), attributes & spawn placement in `PetEvents`, spawn egg in `ArtilianItems` + `ArtilianCreativeTabs`, biome spawn in `ArtilianBiomes` (`Crystal Canopy Woods`, weight 4, group 1–2).
  - `FrogduckModel` / `FrogduckRenderer` wired in `ArtilianClient`; `frogduck.geo.json`, `frogduck.animation.json` (`idle`, `walk`, `sit`, `attack`).
  - `make_frogduck()` in `tools/art/make_pets_art.py` generating `frogduck.png` (run with `--frogduck` so `crystal_fox.png` isn't overwritten).
  - `ArtilianLanguageProvider` (`Frogduck`, `Frogduck Spawn Egg`), `ArtilianItemModelProvider` (`frogduck_spawn_egg`), `./gradlew runData`.
- **Done When:** `./gradlew runData` and `./gradlew build` are clean.

### Stage 2: GameTests, red-proofs, capture/faint verification
- **Changes:** `PetRosterGameTests` gains:
  - `frogduckIsWaterNatureAndSpawnsInCrystalCanopyWoods`: elements `WATER`/`NATURE`, hitbox `1.25 x 1.35`, spawn rule accepts `CRYSTAL_TURF`, Crystal Canopy Woods spawn list includes it and other biomes do not.
  - `frogduckCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal`: full capture/summon/faint-into-crystal cycle.
  - `frogduckBattlesWithWaterNatureMatchups`: 1.5x vs `FIRE`/`EARTH` and `ELECTRIC` 1.5x vs it.
  - `frogduckBreathesUnderwaterAndResistsShortHops`: `canDrownInFluidType` / air supply stays full underwater and short 4-block hop deals 0 fall damage.
- **Red-proof:**
  - Change secondary element or size → `frogduckIsWaterNatureAndSpawnsInCrystalCanopyWoods` turns red → restore.
  - Remove short-hop fall-damage guard → `frogduckBreathesUnderwaterAndResistsShortHops` turns red → restore.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` pass 100%.

### Stage 3: Client verification, live evidence, PR
- **Changes:** `./gradlew runClient` starts cleanly with no Frogduck model/texture/animation warnings in `run/logs/latest.log`. Update `PLAN.md` `Now` section; if interactive client screenshot is with the owner, document that in `PLAN.md` and `qa/`. Commit on `phase-2-frog-duck-pet`.
- **Done When:** CI/local checks green, `PLAN.md` updated, branch committed.

## Security gate

n/a for new controls. `FrogduckEntity` extends `ArtilianPetEntity` and inherits `P2-01`–`P2-04` (crystal UUID ownership, non-lethal fainting, anti-duplication roster, friendly-target filtering). `PetSecurityProbes` must stay green.

## Out of scope

- The kids' official name for Frogduck (placeholder until they pick).
- The lightning guy drawing (its own plan).
- Any change to `PetElement` matchups.
