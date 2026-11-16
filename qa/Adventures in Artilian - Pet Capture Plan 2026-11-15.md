# Adventures in Artilian — Pet Capture Plan (2026-11-15)

Branch: `phase-2-stage-3-pet-capture` (stacked on `phase-2-stage-2-remaining-biomes`, PR #11).
Relates to: `PLAN.md` Phase 2 Stage 3, `PRODUCT.md`, `DESIGN.md`, `security/FINDINGS.md`.

> **Status: Complete (all 3 stages done; stacked PR #12 open against `phase-2-stage-2-remaining-biomes`).** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Pet core, Crystal Fox, Capture Crystal, Biome Treat, art & datagen | `61c7d66` — Expanded 11-element dual-element `PetElement` system (`Fire`, `Water`, `Nature`, `Electric`, `Ice`, `Air`, `Earth`, `Crystal`, `Starlight`, `Shadow`, `Dragon` with `1.5x`/`0.75x` multipliers), `CapturedPetData` (`ArtilianDataComponents#CAPTURED_PET`), `PetRosterData` (`ArtilianAttachments#PETS` `copyOnDeath()`), `ArtilianPetEntity`, `CrystalFoxEntity` (`Crystal / Nature`, spawns in `Crystal Canopy Woods` on `ANIMALS_SPAWNABLE_ON`), `CaptureCrystalItem` (4 state textures via `crystal_state`), `BiomeTreatItem` (`artilian:biome_blooms` tag recipe), `CrystalFoxModel`/`CrystalFoxRenderer`, `tools/art/make_pets_art.py`, datagen, `60/60` `runGameTestServer` green. Red-proof: set `PetService#TREAT_AFFINITY_GAIN = 0` -> `biometreatbefriendswildcrystalfoxandcapturecrystalcapturesit` failed (`got 0`); restored green (`60/60`). |
> | 2 Non-lethal fainting, ownership/duplication security probes & red-proofs | `610ddd7` — `PetSecurityProbes` (`P2-01`, `P2-02`, `P2-03`), `security/pets/README.md`, `security/FINDINGS.md`, `63/63` `runGameTestServer` green. Red-proofs: `P2-01` bypassed `isOwnedBy` in `PetService#useCaptureCrystal` -> `EXPLOITED P2-01`; `P2-02` returned `false` in `PetService#isNonLethalFaintingEnabled` -> `EXPLOITED P2-02`; `P2-03` skipped `PetService#reconcileWithRoster` -> `EXPLOITED P2-03`; all restored and `63/63` HELD. |
> | 3 Client verification, evidence, docs & PR | Verified live `./gradlew runClient` rendering of `Crystal Fox` (sitting + standing GeckoLib models, expressive eyes, crystal crest & tail tip, leaf collar) and all 4 `Capture Crystal` states + `Biome Treat` in hotbar (`qa/evidence/pet-capture-crystal-fox-2026-11-15.png`, clean `run/logs/latest.log`), fixed `ArtilianPetEntity#discardIfOrphan` so pets without a `petId` aren't discarded on tick, updated `PLAN.md`, verified clean `runData`, `./gradlew build`, and `63/63` `runGameTestServer`, and updated PR #12 (`phase-2-stage-3-pet-capture`). |

## Why

Alex wants biome-specific pets that can be captured, carried safely, and called out for adventures, and `PRODUCT.md` guarantees pets **faint safely back into their Capture Crystal** instead of dying permanently. Today Artilian has three Overworld biomes (Phase 2 Stages 1–2), but no Capture Crystals, no Biome Treats, and no capturable pets. Phase 2 Stage 3 builds the full pet capture foundation end to end with **Crystal Fox** (`artilian:crystal_fox`) in **Crystal Canopy Woods** so Stage 4 (Pet battle AI) and St (full Biome Pet roster + dragon hatchling + eye pet) can plug straight in.

## Decisions

1. **Reusable pet base (`ArtilianPetEntity extends PathfinderMob implements OwnableEntity, GeoEntity`):**
   - Implementing `OwnableEntity` integrates immediately with existing kid-safe spell/TNT protections (`ManaService#isValidOffensiveTarget` spares owned pets; `ManaService#isValidHealingTarget` heals them with Heart-Bloom Staff).
   - Stores `OWNER_ID`, `PET_ID`, `AFFINITY` (`0–100`), `BEFRIENDED`, and `SITTING` in synched entity data + NBT, with `PetFollowOwnerGoal`, `FloatGoal`, sitting, and `discardIfOrphan(PetRosterData)` to block duplicates.
2. **Expanded 11-element dual-element system (`PetElement`) & first pet `Crystal Fox` (`artilian:crystal_fox`):**
   - **11 Elements:** `Fire`, `Water`, `Nature`, `Electric`, `Ice`, `Air`, `Earth`, `Crystal`, `Starlight`, `Shadow`, and `Dragon`. Each pet carries a primary element and an optional secondary element, plus clean `1.5x` / `0.75x` type-effectiveness multipliers (`PetElement#multiplierAgainst`) ready for Stage 4 pet battles.
   - **Crystal Fox** (`artilian:crystal_fox`, **`Crystal / Nature`**): native to **Crystal Canopy Woods** (`MobCategory.CREATURE`, weight `6`, group `1–2`), spawning on `Crystal Turf` (`BlockTags.ANIMALS_SPAWNABLE_ON`).
   - Cartoony GeckoLib model (`crystal_fox.geo.json` + `crystal_fox.animation.json`) in the `#38D9C0` Starlight Teal / `#68D8FF` Crystal palette with oversized eyes, bouncy walk cycle, and crystal ear tufts & tail tip.
3. **Capture Crystals & Biome Treats (`DataComponents` + `ArtilianAttachments.PETS`):**
   - **Biome Treat** (`artilian:biome_treat`): crafted from any Artilian biome flower (`artilian:biome_blooms` tag) + `Sweet Berries` + `Sugar` (yields 4). Right-clicking a wild pet boosts affinity by `50` (`100` = befriended, `HEART` particles); right-clicking an owned pet heals `6.0F` HP; holding a treat in the offhand while using a fainted Capture Crystal wakes the pet early.
   - **Capture Crystal** (`artilian:capture_crystal`): crafted from `Starlight Crystal` + `Sunsteel Ingot` + `Glass` (yields 2). Right-clicking a wild pet captures it (`100%` when befriended or below `60%` HP; `60%` otherwise, and a miss never destroys the crystal!). Right-clicking in air with a filled crystal **summons** or **recalls** the pet.
   - **Single source of truth (`PetRosterData`, `copyOnDeath()`):** Every captured pet has a unique `petId` tracked in the owner's `artilian:pets` attachment alongside the `CapturedPetData` component on the crystal. This prevents duplicate pets from cloned/copied crystals and guarantees a pet is never lost even if its crystal was moved when the pet fainted.
4. **Kid-safe Non-Lethal Fainting & Ownership Lock (`P2-01`–`P2-03`):**
   - When a tamed pet takes fatal damage (combat, fall, fire, explosions, or void), `ArtilianPetEntity` intercepts death before `die()`, marks the pet `fainted = true` with a short `100`-tick (5s) rest timer, saves it back into the owner's crystal & `PetRosterData`, plays gentle recall particles/sound, notifies the owner, and discards the entity with zero loot/XP drops.
   - Only the owning player can summon, recall, command, or re-capture their pet (`P2-01`).

## Stages

### Stage 1: Pet core, Crystal Fox, Capture Crystal, Biome Treat, art & datagen
- **Changes:** `ArtilianDataComponents` (`CAPTURED_PET`), `CapturedPetData`, `PetRosterData`, `ArtilianAttachments#PETS`, `PetService`, `ArtilianPetEntity`, `CrystalFoxEntity`, `PetFollowOwnerGoal`, `PetEvents`, `CaptureCrystalItem`, `BiomeTreatItem`, `ArtilianItems` / `ArtilianEntities` / `ArtilianCreativeTabs`, `ArtilianBiomes` spawn + `BlockTags.ANIMALS_SPAWNABLE_ON`, `CrystalFoxModel` / `CrystalFoxRenderer` / `ArtilianClient`, `crystal_fox.geo.json`, `crystal_fox.animation.json`, `tools/art/make_pets_art.py`, datagen (`ArtilianLanguageProvider`, `ArtilianItemModelProvider`, `ArtilianRecipeProvider`, `ArtilianBlockTagsProvider`, `ArtilianItemTagsProvider`).
- **Tests, written first and seen failing:** `PetCaptureGameTests`:
  - `biomeTreatBefriendsWildCrystalFoxAndCaptureCrystalCapturesIt`
  - `captureCrystalSummonsAndRecallsCrystalFoxPreservingIdentityAndHealth`
  - `biomeTreatHealsOwnedPetAndWakesFaintedCrystal`
  - `biomeTreatAndCaptureCrystalRecipesWork`
- **Red-proof:** temporarily disable affinity gain in `BiomeTreatItem` or capture storage in `CaptureCrystalItem` -> confirm `PetCaptureGameTests` fails -> restore -> green.
- **Done When:** `runData`, `build`, and `runGameTestServer` pass with all Stage 1 tests green.

### Stage 2: Non-lethal fainting, ownership/duplication security probes & red-proofs
- **Changes:** `PetSecurityProbes` (`P2-01`, `P2-02`, `P2-03`), `security/pets/README.md`, `security/FINDINGS.md`, plus `ManaService` ally-safety verification for `CrystalFoxEntity`.
- **Tests, written first and seen failing:** `PetSecurityProbes`:
  - `p2_01_nonOwnerCannotCaptureStealOrRecallOthersPet`
  - `p2_02_nonLethalFaintingSavesPetIntoCrystalInsteadOfDying`
  - `p2_03_clonedCrystalCannotDuplicateActivePet`
- **Red-proof:**
  - `P2-01`: disable owner check in `PetService` -> `EXPLOITED P2-01` -> restore -> `HELD`.
  - `P2-02`: disable `shouldFaintInsteadOfDie` guard -> `EXPLOITED P2-02` -> restore -> `HELD`.
  - `P2-03`: disable active-entity duplicate check in `PetService#summon` -> `EXPLOITED P2-03` -> restore -> `HELD`.
- **Done When:** All GameTests and security probes pass (`HELD`), red-proofs recorded in `security/FINDINGS.md` and this plan.

### Stage 3: Client verification, evidence, docs & PR
- **Changes:** Verify `Crystal Fox`, `Capture Crystal` (empty/filled/summoned/fainted states), and `Biome Treat` in `./gradlew runClient` (zero missing-texture/model errors, clean `run/logs/latest.log`), capture `qa/evidence/pet-capture-crystal-fox-2026-11-15.png` & `qa/evidence/pet-capture-hud-2026-11-15.png`, update `PLAN.md` (`Now` + Stage 3 checkbox) and this plan's status table, verify `runData`/`build`/`runGameTestServer`, push `phase-2-stage-3-pet-capture`, and open stacked PR on `phase-2-stage-2-remaining-biomes`.
- **Done When:** `runData` clean, `build` and `runGameTestServer` green, client evidence verified, and stacked PR open.

## Security gate

Applies — pet capture introduces player-owned `CaptureCrystalItem` / `ArtilianPetEntity` binding (`P2-01`), the kid-safe non-lethal fainting rule (`P2-02`, `AGENTS.md`), and single-instance anti-duplication (`P2-03`). Probes live in `src/main/java/com/silkpanda/artilian/gametest/security/PetSecurityProbes.java`, indexed in `security/pets/README.md` and logged in `security/FINDINGS.md`.

## Out of scope

- Pet combat / targeting structure enemies (Phase 2 Stage 4).
- Additional biome pets (Emberbloom / Starlight Highlands pets, dragon hatchling, eye-themed pet — Phase 2 Stage 5).
- Custom biome structures and structure enemies (Phase 2 Stage 6).
- Final Phase 2 art pass against Alex & Steve's drawings (Phase 2 St).
