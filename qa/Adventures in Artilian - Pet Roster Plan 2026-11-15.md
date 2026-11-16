# Adventures in Artilian — Pet Roster Plan (2026-11-15)

Branch: `phase-2-stage-5-pet-roster` (stacked on `phase-2-stage-4-pet-battle-ai`, PR #13).
Relates to: `PLAN.md` Phase 2 Stage 5, `PRODUCT.md`, `DESIGN.md`, `qa/evidence/bob-pets-drawing-2026-11-15.jpg`.

> **Status: Stages 1–3 complete (Bob's two pets shipped & verified); Stage 4 (remaining St roster) queued next.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Bob's two pets: entities, spawns, GeckoLib models/textures/animations & datagen | `a3a38e7` — `ShadowWispEntity` (`SHADOW`, spawns in `Starlight Highlands` on `ANIMALS_SPAWNABLE_ON` or `Starlight Slate` at any light level), `CinderCragEntity` (`FIRE / EARTH`, `fireImmune()`, spawns across `#minecraft:is_overworld` & `#minecraft:is_nether` wherever `FluidTags.LAVA` is within 6 blocks), GeckoLib `.geo.json`/`.animation.json`/`make_pets_art.py` textures, `ArtilianLanguageProvider`, clean `runData` & `build`. |
> | 2 GameTests, spawn-rule red-proofs & battle/safety verification | `8704c27` — `PetRosterGameTests` (`shadowWispIsShadowTypeAndOnlySpawnsOnStarlightGround`, `cinderCragIsFireEarthFireImmuneAndOnlySpawnsNearLava`, `bobPetsCaptureBefriendSummonAndFaintSafelyIntoCrystal`, `bobPetsBattleWithShadowAndFireElementalMatchupsAndPerks`), red-proven (`hasNearbyLava` forced `true` -> `cinderCrag...` failed; `STARLIGHT_SLATE` omitted from `checkShadowSpawnRules` -> `shadowWisp...` failed), restored & `72/72` `runGameTestServer` green. |
> | 3 Client verification, live evidence, docs & PR | Verified live `./gradlew runClient` rendering of `Shadow Wisp` and `Cinder Crag` (standing + sitting poses, portal/flame particles, zero missing-model/texture warnings in `run/logs/latest.log`), captured `qa/evidence/pet-roster-bob-pets-2026-11-15.png`, updated `PLAN.md` `Now` section, verified clean `runData`/`build`/`72/72 runGameTestServer`, and opened stacked PR on `phase-2-stage-4-pet-battle-ai`. |
> | 4 Remaining Stage 5 roster (Emberbloom pet, small dragon hatchling, eye-themed pet — follow-up) | |

## Why

Phase 2 Stages 3–4 shipped the pet capture system, 11-element dual-element combat, and `Crystal Fox` (`Crystal / Nature`) in **Crystal Canopy Woods**, while **Emberbloom Meadows**, **Starlight Highlands**, and lava caves/pools still have no native Artilian pets. Bob drew two brand-new pets (`qa/evidence/bob-pets-drawing-2026-11-15.jpg`) with the owner's placement specs:

1. **Shadow Creature** (working name: **Shadow Wisp**, `artilian:shadow_wisp` — Bob will pick its official name):
   - **Drawing spec:** Dark charcoal-violet blocky head with subtle shadowed face markings and glowing violet eyes, atop a tapered purple V-shaped torso with 4 spindly purple limbs — two side arms tipped with 3 splayed claw tendrils and two long curved trailing legs.
   - **Element:** `Shadow` (`PetElement.SHADOW`).
   - **Spawn:** **Starlight Highlands** (`ArtilianBiomes#STARLIGHT_HIGHLANDS`), including dim/night lighting so it thrives across the twilight highlands.
2. **Fire / Rock Creature** (working name: **Cinder Crag**, `artilian:cinder_crag` — Bob will pick its official name):
   - **Drawing spec:** Squat, chunky crimson-stone & molten-orange body housing its angled fiery eyes and wide arched magma mouth right in the middle of the torso, a flat stone brow across the top crowned by **3 pointy flame spires** (two larger outer flames + one smaller center flame), standing on two sturdy blocky rock pillar legs.
   - **Elements:** `Fire / Earth` (`PetElement.FIRE`, `PetElement.EARTH`), `fireImmune()`.
   - **Spawn:** **Anywhere there is lava** — Overworld and Nether biomes via `BiomeModifiers.AddSpawnsBiomeModifier`, gated by a strict nearby-lava spawn check (`FluidTags.LAVA` within 6 horizontal / 3 vertical blocks).

## Stages

### Stage 1: Bob's two pets — entities, spawns, GeckoLib models/textures/animations & datagen
- **Changes:**
  - `ShadowWispEntity` & `CinderCragEntity` (extending `ArtilianPetEntity`), registered in `ArtilianEntities` (`SHADOW_WISP`, `CINDER_CRAG`), attributes & spawn placements in `PetEvents`.
  - `ArtilianBiomes#buildStarlightHighlands` spawn entry for `SHADOW_WISP`; `ArtilianWorldGen#bootstrapBiomeModifiers` (`ADD_CINDER_CRAG_OVERWORLD`, `ADD_CINDER_CRAG_NETHER`) for `CINDER_CRAG` near lava.
  - Client models & renderers (`ShadowWispModel`, `ShadowWispRenderer`, `CinderCragModel`, `CinderCragRenderer`) wired in `ArtilianClient`.
  - `tools/art/make_pets_art.py` extended to generate `shadow_wisp.png` & `cinder_crag.png`; GeckoLib `shadow_wisp.geo.json`, `cinder_crag.geo.json`, `shadow_wisp.animation.json`, and `cinder_crag.animation.json` (`idle`, `walk`, `sit`, `attack`).
  - `ArtilianLanguageProvider` working names & datagen (`./gradlew runData`).
- **Tests, written first and seen failing:** `PetRosterGameTests` (Stage 2) verifying entity registration, elements, fire immunity, spawn rules, capture/treat/summon/faint, and elemental combat.
- **Red-proof:** Covered in Stage 2 against `CinderCragEntity#checkLavaSpawnRules` and `ShadowWispEntity#checkShadowSpawnRules`.
- **Done When:** `runData` and `build` compile cleanly with all GeckoLib assets, textures, and biome modifiers generated.

### Stage 2: GameTests, spawn-rule red-proofs & battle/safety verification
- **Changes:** `PetRosterGameTests` testing:
  - `shadowWispIsShadowTypeAndOnlySpawnsOnStarlightGround`
  - `cinderCragIsFireEarthFireImmuneAndOnlySpawnsNearLava`
  - `bobPetsCaptureBefriendSummonAndFaintSafelyIntoCrystal`
  - `bobPetsBattleWithShadowAndFireElementalMatchupsAndPerks`
- **Red-proof:**
  - Temporarily return `true` unconditionally in `CinderCragEntity#hasNearbyLava` -> confirm `cinderCragIsFireEarthFireImmuneAndOnlySpawnsNearLava` fails when tested on dry stone -> restore -> green.
  - Temporarily reject low-light or `STARLIGHT_TURF` in `ShadowWispEntity#checkShadowSpawnRules` -> confirm `shadowWispIsShadowTypeAndOnlySpawnsOnStarlightGround` fails -> restore -> green.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` pass 100% (including all existing `P1`/`P2` security probes).

### Stage 3: Client verification, live evidence, docs & PR
- **Changes:**
  - Launch `./gradlew runClient`, spawn both `Shadow Wisp` and `Cinder Crag` (standing, sitting, and near lava / in Starlight Highlands), verify textures, animations, and ambient particles in `run/logs/latest.log` (zero missing-model/texture warnings).
  - Capture `qa/evidence/pet-roster-bob-pets-2026-11-15.png`.
  - Update `PLAN.md` (`Now` section) and this plan's status table, push `phase-2-stage-5-pet-roster`, and open a stacked PR on `phase-2-stage-4-pet-battle-ai`.
- **Done When:** All CI checks (`build`, `runData`, `runGameTestServer`) pass, live client screenshot is verified, and the stacked PR is open for the owner & Bob's review/naming.

### Stage 4: Remaining Stage 5 roster (follow-up)
- Deferred cleanly to a follow-up PR on `phase-2-stage-5-pet-roster` once Bob names these two pets: Emberbloom Meadows meadow pet, a small dragon hatchling (`DRAGON`), and an eye-themed pet (`SHADOW / STARLIGHT`) so `PLAN.md` St can be checked off.

## Security gate

n/a for new security controls — `ShadowWispEntity` and `CinderCragEntity` extend `ArtilianPetEntity` and inherit all existing `P2-01`–`P2-04` controls (`PetService` UUID crystal ownership, non-lethal fainting, anti-duplication `PetRosterData`, and friendly-target safety filtering). Existing `PetSecurityProbes` (`P2-01`–`P2-04`) must remain green.

## Out of scope

- Official pet names (the owner will have Bob name both pets; working names `Shadow Wisp` and `Cinder Crag` are used until then).
- Dragon hatchling, eye-themed pet, and non-lava Emberbloom Meadows pet (Stage 4 follow-up).
- Custom biome structures and structure enemies (Phase 2 Stage 6).
- Final Phase 2 art polish pass (Phase 2 Stage 7).
