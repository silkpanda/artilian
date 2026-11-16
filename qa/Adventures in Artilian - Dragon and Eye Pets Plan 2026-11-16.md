# Adventures in Artilian — Dragon & Eye Pets Plan (2026-11-16)

Branch: `phase-2-stage-5-dragon-eye-pets` (stacked on `phase-2-stage-5-alex-pets`, commit `a956de4`).
Relates to: `PLAN.md` Phase 2 Stage 5, `PRODUCT.md`, `DESIGN.md`, `qa/Adventures in Artilian - Pet Roster Plan 2026-11-15.md`.

> **Status: All stages complete.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Ember Drake & Peepkin: entities, spawns, GeckoLib models/textures/animations & datagen | `51778bb` — `EmberDrakeEntity` (`DRAGON / FIRE`, `fireImmune()`, zero fall damage, `Emberbloom Meadows` spawn), `PeepkinEntity` (`SHADOW / STARLIGHT`, zero fall damage, `Starlight Highlands` & `Crystal Canopy Woods` spawns), GeckoLib `.geo.json`/`.animation.json`/`make_pets_art.py` textures, `ArtilianLanguageProvider`, clean `runData` & `build`. |
> | 2 GameTests, red-proofs, battle/capture/faint verification & P2 safety checks | `07bd334` — `PetRosterGameTests` (`emberDrakeIsDragonFireImmuneToFireAndSpawnsInEmberbloomMeadows`, `peepkinIsShadowStarlightFloatsSafelyAndSpawnsInHighlandsAndWoods`, `dragonAndEyePetsCaptureBefriendSummonAndFaintSafelyIntoCrystal`, `dragonAndEyePetsBattleWithDragonAndShadowElementalMatchupsAndPerks`), red-proven (`EmberDrakeEntity#fireImmune` -> `false` & `PeepkinEntity#causeFallDamage` -> `super.causeFallDamage`), restored & `80/80` `runGameTestServer` + `build` green. |
> | 3 Client verification, live evidence, PLAN Stage 5 sign-off & PR | `4fa5323` + live showcase follow-up — verified `./gradlew runClient` live in-world rendering of standing & sitting `Ember Drake` and `Peepkin` (refined Peepkin's sclera/eyelid proportions from initial capture), saved `qa/evidence/pet-roster-dragon-eye-2026-11-16.png`, checked off Phase 2 Stage 5 in `PLAN.md`, verified clean `runData` / `build` / `80/80 runGameTestServer`, and opened stacked PR #17 on `phase-2-stage-5-alex-pets`. |

## Why

Phase 2 St requires biome-specific pets across all three Overworld biomes **plus** one small dragon hatchling and one eye-themed pet so Steve and Alex each encounter their signature dimension's theme early:
1. **Ember Drake** (`artilian:ember_drake`) — a small, friendly **Dragon / Fire** hatchling (`DRAGON` primary, `FIRE` secondary) native to **Emberbloom Meadows**, introducing Steve's Dragon theme early (`#22C55E` Canopy Emerald & `#F97316` Ember Amber palette from `DESIGN.md`).
2. **Peepkin** (`artilian:peepkin`) — a playful, floating **Shadow / Starlight** eye-themed creature (`SHADOW` primary, `STARLIGHT` secondary) native to **Starlight Highlands** and **Crystal Canopy Woods**, introducing Alex's Eye Realm theme early (`#D946EF` Twilight Magenta & `#7DF9FF` Pupil Glow palette from `DESIGN.md`, distinct from Phase 4's **Blinky**).

## Stages

### Stage 1: Ember Drake & Peepkin — entities, spawns, GeckoLib models/textures/animations & datagen
- **Changes:**
  - `EmberDrakeEntity` & `PeepkinEntity` extending `ArtilianPetEntity`, registered in `ArtilianEntities` (`EMBER_DRAKE`, `PEEPKIN`), attributes & spawn placements wired in `PetEvents`.
  - Biome spawns in `ArtilianBiomes`: `EMBER_DRAKE` in `EMBERBLOOM_MEADOWS`; `PEEPKIN` in `STARLIGHT_HIGHLANDS` and `CRYSTAL_CANOPY_WOODS`.
  - Client models & renderers (`EmberDrakeModel`, `EmberDrakeRenderer`, `PeepkinModel`, `PeepkinRenderer`) wired in `ArtilianClient`.
  - `tools/art/make_pets_art.py` extended to generate `ember_drake.png` & `peepkin.png`; original GeckoLib `ember_drake.geo.json`, `peepkin.geo.json`, `ember_drake.animation.json`, and `peepkin.animation.json` (`idle`, `walk`, `sit`, `attack`).
  - `ArtilianLanguageProvider` localized names (`Ember Drake`, `Peepkin`) & datagen (`./gradlew runData`).
- **Tests, written first and seen failing:** `PetRosterGameTests` extended in Stage 2 for elements, fire/fall rules, biome spawns, capture/treat/faint, and elemental combat.
- **Red-proof:** Covered in Stage 2 against `EmberDrakeEntity#checkDragonHatchlingSpawnRules` / `fireImmune()` and `PeepkinEntity#checkEyePetSpawnRules` / `causeFallDamage()`.
- **Done When:** `./gradlew runData` and `./gradlew build` compile cleanly with all GeckoLib assets, textures, and biome JSONs generated.

### Stage 2: GameTests, red-proofs, battle/capture/faint verification & P2 safety checks
- **Changes:** `PetRosterGameTests` extended with:
  - `emberDrakeIsDragonFireImmuneToFireAndSpawnsInEmberbloomMeadows`
  - `peepkinIsShadowStarlightFloatsSafelyAndSpawnsInHighlandsAndWoods`
  - `dragonAndEyePetsCaptureBefriendSummonAndFaintSafelyIntoCrystal`
  - `dragonAndEyePetsBattleWithDragonAndShadowElementalMatchupsAndPerks`
- **Red-proof:**
  - Temporarily return `false` in `EmberDrakeEntity#fireImmune()` (or reject `EMBER_TURF` in `checkDragonHatchlingSpawnRules`) -> confirm `emberDrake...` test turns red -> restore -> green.
  - Temporarily return `super.causeFallDamage(...)` in `PeepkinEntity#causeFallDamage` (or reject `STARLIGHT_SLATE` in `checkEyePetSpawnRules`) -> confirm `peepkin...` test turns red -> restore -> green.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` pass 100% (including all existing P1/P2 probes).

### Stage 3: Client verification, live evidence, PLAN Stage 5 sign-off & PR
- **Changes:**
  - Launch `./gradlew runClient`, spawn both `Ember Drake` and `Peepkin` (standing, sitting, blinking/flapping, ambient particles), verify zero missing-model/texture warnings in `run/logs/latest.log`.
  - Capture screenshot evidence in `qa/evidence/pet-roster-dragon-eye-2026-11-16.png`.
  - Check off Phase 2 Stage 5 in `PLAN.md`, update `PLAN.md` `Now` section and this plan's status table.
  - Push `phase-2-stage-5-dragon-eye-pets` and open stacked PR on `phase-2-stage-5-alex-pets`.
- **Done When:** All CI checks green, live client evidence verified, and stacked PR open.

## Security gate

n/a for new security controls — `EmberDrakeEntity` and `PeepkinEntity` extend `ArtilianPetEntity` and inherit all `P2-01`–`P2-04` controls (`PetService` UUID crystal ownership, non-lethal fainting, anti-duplication `PetRosterData`, and friendly-target safety filtering). Existing `PetSecurityProbes` (`P2-01`–`P2-04`) must remain green.

## Out of scope

- Official renaming of Steve's `Shadow Wisp` / `Cinder Crag` (waiting on Steve).
- Phase 2 Stage 6 custom biome structures and structure enemies.
- Phase 4 `Blinky` pet and Phase 5 adult dragon species.
