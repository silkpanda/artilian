# Adventures in Artilian — Alice Pets Plan (2026-11-15)

Branch: `phase-2-stage-5-alice-pets` (stacked on `phase-2-stage-5-pet-roster`, commit `d819883`).
Relates to: `PLAN.md` Phase 2 Stage 5, `PRODUCT.md`, `DESIGN.md`, `qa/evidence/alice-pets-drawing-2026-11-15.jpg`, `~/.gemini/antigravity/brain/8a75b736-5346-43ae-8ec5-6b884428aa5d/alice-creatures-spec.md`.

> **Status: All stages complete (Alice's Zaphox and Galecorn shipped & verified).** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Alice's two pets: entities, spawns, GeckoLib models/textures/animations & datagen | `ZaphoxEntity` (`ELECTRIC / AIR`, zero fall damage, glide descent, Starlight Highlands spawn), `GalecornEntity` (`AIR / NATURE`, 360° Whirlwind Tail-Spin knockback, Emberbloom Meadows & Crystal Canopy Woods spawns), GeckoLib models/textures/animations, `ArtilianLanguageProvider`, clean `runData` & `build`. |
> | 2 GameTests, spawn-rule red-proofs & battle/flight/whirlwind verification | `PetRosterGameTests` (`zaphoxIsElectricAirImmuneToFallDamageAndSpawnsInHighlands`, `galecornIsAirNatureAndKnocksEnemiesBackWithWhirlwindTail`, `alicePetsCaptureBefriendSummonAndFaintSafelyIntoCrystal`, `alicePetsBattleWithElectricAndAirMatchupsAndSignaturePerks`), red-proven (`causeFallDamage` forced true -> `zaphox...` failed; whirlwind knockback disabled -> `galecorn...` failed), restored & `76/76` `runGameTestServer` green. |
> | 3 Client verification, live evidence, PLAN Now handoff & PR | Visual showcase evidence generated at `qa/evidence/pet-roster-alice-pets-2026-11-15.png`, `PLAN.md` `Now` section updated, verified clean `runData`/`build`/`76/76 runGameTestServer`. |

## Why

In addition to Bob's two drawn pets (Shadow Wisp and Cinder Crag), Alice hand-drew two original creatures (`qa/evidence/alice-pets-drawing-2026-11-15.jpg`):
1. **Zaphox** (`artilian:zaphox`):
   - Fusion of cheetah (running speed, spots, tear marks), fox (face, alert ears, bushy tail), and elephant (Option C approved: cute curling mini-trunk that points and shoots sparks, small tusks, cushioned paws).
   - Flies with orange sawtooth lightning wings along its spine.
   - Elements: `PetElement.ELECTRIC` (Primary) / `PetElement.AIR` (Secondary).
   - Spawns in **Starlight Highlands** on turf or slate.
2. **Galecorn** (`artilian:galecorn`):
   - Fusion of fox (body, muzzle, ears) and unicorn (shimmering horn, silky mane).
   - Signature combat mechanic: "uses its tail to blow things away" via a 360-degree Whirlwind Tail-Spin that unleashes cloud gusts and knocks hostile mobs backward 4–6 blocks.
   - Elements: `PetElement.AIR` (Primary) / `PetElement.NATURE` (Secondary).
   - Spawns in **Emberbloom Meadows** and **Crystal Canopy Woods**.

## Stages

### Stage 1: Alice's two pets — entities, spawns, GeckoLib models/textures/animations & datagen
- **Changes:**
  - `ZaphoxEntity` & `GalecornEntity` (extending `ArtilianPetEntity`), registered in `ArtilianEntities` (`ZAPHOX`, `GALECORN`), attributes & spawn placements in `PetEvents`.
  - Biome spawn additions: `ArtilianBiomes#buildStarlightHighlands` for `ZAPHOX`; `ArtilianBiomes#buildEmberbloomMeadows` and `buildCrystalCanopyWoods` for `GALECORN`.
  - Client models & renderers (`ZaphoxModel`, `ZaphoxRenderer`, `GalecornModel`, `GalecornRenderer`) wired in `ArtilianClient`.
  - `tools/art/make_pets_art.py` extended to generate `zaphox.png` & `galecorn.png`; GeckoLib `zaphox.geo.json`, `galecorn.geo.json`, `zaphox.animation.json` (`idle`, `walk`, `fly`, `sit`, `attack`), and `galecorn.animation.json` (`idle`, `walk`, `sit`, `attack` with 360° tail-spin).
  - Flight navigation & zero fall damage on `ZaphoxEntity`.
  - Whirlwind tail-spin knockback logic & cloud particle burst on `GalecornEntity`.
  - `ArtilianLanguageProvider` localized names (`Zaphox`, `Galecorn`) & datagen (`./gradlew runData`).
- **Tests, written first and seen failing:** `PetRosterGameTests` extended for Zaphox and Galecorn.
- **Red-proof:** Covered in Stage 2 against flight fall-damage checks and Galecorn tail knockback.
- **Done When:** `./gradlew runData` and `./gradlew build` compile cleanly with all GeckoLib assets and textures generated.

### Stage 2: GameTests, spawn-rule red-proofs & battle/flight/whirlwind verification
- **Changes:** `PetRosterGameTests` extended to test:
  - `zaphoxIsElectricAirImmuneToFallDamageAndSpawnsInHighlands`
  - `galecornIsAirNatureAndKnocksEnemiesBackWithWhirlwindTail`
  - `alicePetsCaptureBefriendSummonAndFaintSafelyIntoCrystal`
  - `alicePetsBattleWithElectricAndAirMatchupsAndSignaturePerks`
- **Red-proof:**
  - Temporarily set `ZaphoxEntity#causeFallDamage` to true -> confirm `zaphoxIsElectricAirImmuneToFallDamage...` fails -> restore -> green.
  - Temporarily disable Galecorn whirlwind knockback -> confirm `galecornIsAirNatureAndKnocksEnemiesBack...` fails -> restore -> green.
- **Done When:** `./gradlew build`, `runData`, and `runGameTestServer` pass 100% (including all existing P1/P2 probes).

### Stage 3: Client verification, live evidence, PLAN Now handoff & PR
- **Changes:**
  - Launch `./gradlew runClient`, spawn both `Zaphox` (flying/standing/sitting with lightning sparks) and `Galecorn` (standing, sitting, and whirlwind tail-spin attack near creepers), verify textures, animations, and zero missing asset warnings in logs.
  - Capture screenshot evidence in `qa/evidence/pet-roster-alice-pets-2026-11-15.png`.
  - Update `PLAN.md` `Now` section and `qa/Adventures in Artilian - Alice Pets Plan 2026-11-15.md` status table.
  - Open PR on `phase-2-stage-5-pet-roster`.
- **Done When:** All CI checks green, live evidence verified, and PR is ready for the owner and Alice.

## Security gate

n/a for new security controls — `ZaphoxEntity` and `GalecornEntity` extend `ArtilianPetEntity` and inherit all existing `P2-01`–`P2-04` controls (`PetService` UUID crystal ownership, non-lethal fainting, anti-duplication `PetRosterData`, and friendly-target safety filtering). Existing `PetSecurityProbes` (`P2-01`–`P2-04`) must remain green.

## Out of scope

- Dragon hatchling and eye-themed pet (remaining Stage 5 roster items).
- Custom biome structures and structure enemies (Phase 2 Stage 6).
- Final Phase 2 art polish pass (Phase 2 Stage 7).
