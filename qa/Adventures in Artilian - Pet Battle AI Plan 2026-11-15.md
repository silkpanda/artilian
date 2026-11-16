# Adventures in Artilian — Pet Battle AI Plan (2026-11-15)

Branch: `phase-2-stage-4-pet-battle-ai` (stacked on `phase-2-stage-3-pet-capture`, PR #12).
Relates to: `PLAN.md` Phase 2 Stage 4, `PRODUCT.md`, `DESIGN.md`, `security/FINDINGS.md`.

> **Status: All stages complete.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Battle AI goals, elemental combat & datagen | `4adc633` — `ArtilianPetEntity` battle goals (`PetMeleeAttackGoal`, owner-hurt/assist defense, autonomous scan skipping Creepers), `PetService#isValidBattleTarget` & `performElementalAttack`, `PetElement#elementsOf`/`multiplierAgainst`/`applySignatureEffect`, `ElementalMob`, `artilian:structure_enemies` tag (`runData` + `runGameTestServer` 67/67 green; red-proof `elementalMatchupsScaleBattleDamageUpForSuperEffectiveAndDownForResisted` confirmed) |
> | 2 Kid-safety battle probe (`P2-04`) & red-proofs | `b515073` — `PetSecurityProbes#p2_04_tamedPetNeverAttacksPlayersChesterVillagersOrAlliedPets` (`EXPLOITED P2-04: pet target filter allowed entity.artilian.chester` then `HELD`, 68/68 GameTests green; logged in `security/FINDINGS.md` & `security/pets/README.md`) |
> | 3 Client verification, evidence, docs & PR | Live `runClient` verified Crystal Fox engaging & hitting `Pillager` with Glowing + Crystal particles (`qa/evidence/pet-battle-crystal-fox-2026-11-15.png`), zero mod errors in `run/logs/latest.log`, `PLAN.md` updated, final `runData` / `build` / `runGameTestServer` (`68/68`) green |

## Why

Phase 2 Stage 3 shipped Capture Crystals, Biome Treats, the expanded 11-element `PetElement` system, non-lethal fainting, and `Crystal Fox`, but summoned pets currently only follow and sit — they do not fight alongside the player or use their element matchups in battle (`PLAN.md` Phase 2 Stage 4). Alex and Steve want their summoned pets to defend them, assist their attacks, target hostile & structure enemies, and unleash readable elemental hits while keeping every kid-safety net intact.

## Decisions

1. **Owner-synchronized combat AI (`ArtilianPetEntity`):**
   - **Goals (active only when `isTamed() && !isSitting()`):**
     - `MeleeAttackGoal` (`1.35` speed) with an attack-lunge animation (`action` controller `"attack"` on `CrystalFoxEntity`).
     - `PetOwnerHurtByTargetGoal`: defends the owner immediately when a valid hostile hurts them.
     - `PetOwnerHurtTargetGoal`: assists when the owner strikes a valid hostile.
     - `PetNearestEnemyGoal`: scans within `12` blocks for `Enemy` or `artilian:structure_enemies`-tagged targets (`ArtilianTags.EntityTypes.STRUCTURE_ENEMIES`) so Stage 6 structure enemies are targeted automatically.
   - **Follow & Sit overrides:**
     - Sitting (`isSitting()`) clears any combat target immediately (`setTarget(null)`) so kids can park pets safely anytime.
     - If the owner moves further than `16` blocks (`MAX_LEASH_DISTANCE_SQR`), the pet disengages combat and follows/hops back to its owner so pets never get lost behind.
2. **Elemental battle damage & signature perks (`PetElement` + `PetBattleService`):**
   - `PetElement.elementsOf(LivingEntity)` resolves elemental typing for Artilian pets and intuitive vanilla mobs (e.g., `Blaze`/`MagmaCube` -> `FIRE`, `Drowned`/`Guardian` -> `WATER`, `Breeze`/`Vex` -> `AIR`, `Enderman`/`Shulker` -> `SHADOW`, `Stray` -> `ICE`, `Creeper`/`Slime` -> `NATURE`, `Husk`/`Silverfish` -> `EARTH`, `EnderDragon` -> `DRAGON`), plus an `ElementalMob` interface for future Stage 5/6 custom mobs.
   - `doHurtTarget` multiplies base `ATTACK_DAMAGE` by `primary.multiplierAgainst(defPrimary, defSecondary)` (`1.5x` Super Effective / `0.75x` Resisted / `1.0x` Neutral), spawns element particles + high chime on super-effective hits, and applies the pet's element signature perk on a `60`-tick (`3`s) cooldown (`CRYSTAL` -> `Slowness I` `40`t + `Glowing` `60`t).
   - Updates `PetRosterData` snapshot on hit/damage so health remains accurate if recalled.
3. **Strict Kid-Safe Target Filtering (`PetService#isValidBattleTarget` / `P2-04`):**
   - Pets **never** target or harm any `Player` (even in PvP accidents between Alex and Steve), `ChesterEntity`, villagers, iron/snow golems, armor stands, baby animals, or any tamed/befriended pet (`ArtilianPetEntity`, `TamableAnimal`, `AbstractHorse`).

## Stages

### Stage 1: Battle AI goals, elemental combat & datagen
- **Changes:** `ElementalMob`, `PetElement#elementsOf` & `PetElement#applySignatureEffect`, `ArtilianTags.EntityTypes#STRUCTURE_ENEMIES`, `ArtilianEntityTypeTagsProvider` + `ArtilianDataGen` wiring, `PetService#isValidBattleTarget` + battle target goals (`PetOwnerHurtByTargetGoal`, `PetOwnerHurtTargetGoal`, `PetNearestEnemyGoal`), `ArtilianPetEntity#doHurtTarget` & disengage-to-follow logic, `CrystalFoxEntity` `"attack"` pounce controller + `crystal_fox.animation.json`.
- **Tests, written first and seen failing:** `PetBattleGameTests`:
  - `tamedCrystalFoxAttacksHostileAndStructureEnemiesWhileSittingClearsTarget`
  - `tamedCrystalFoxDefendsOwnerAndAppliesCrystalElementPerk`
  - `elementalMatchupsScaleBattleDamageUpForSuperEffectiveAndDownForResisted`
  - `petDisengagesToFollowFarOwnerAndNeverTargetsCreepersPassively`
- **Red-proof:** temporarily return `1.0F` instead of elemental multiplier or disable `PetNearestEnemyGoal` -> confirm `PetBattleGameTests` fails -> restore -> green.
- **Done When:** `runData`, `build`, and `runGameTestServer` pass with all Stage 1 tests green.

### Stage 2: Kid-safety battle probe (`P2-04`) & red-proofs
- **Changes:** `PetSecurityProbes#p2_04_tamedPetNeverAttacksPlayersChesterVillagersOrAlliedPets`, `security/pets/README.md`, `security/FINDINGS.md`.
- **Red-proof:** temporarily allow `Player` or `ChesterEntity` in `PetService#isValidBattleTarget` -> confirm `p2_04` prints `EXPLOITED P2-04` -> restore -> `HELD`.
- **Done When:** All GameTests and security probes (`P2-01`–`P2-04`) pass cleanly.

### Stage 3: Client verification, evidence, docs & PR
- **Changes:** Verify Crystal Fox battle pounce & Crystal particle hit in `./gradlew runClient`, capture `qa/evidence/pet-battle-crystal-fox-2026-11-15.png`, inspect `run/logs/latest.log`, update `PLAN.md` (`Now` + Stage 4 checkbox) and this plan's status table, verify `runData`/`build`/`runGameTestServer`, push `phase-2-stage-4-pet-battle-ai`, and open stacked PR on `phase-2-stage-3-pet-capture`.
- **Done When:** All CI checks green, battle screenshot verified, and stacked PR open.

## Security gate

Applies — battle AI introduces autonomous entity targeting (`P2-04`) that must strictly spare all `Player`s, `ChesterEntity`, villagers, golems, and tamed/befriended pets even when provoked by owner-hurt or assist triggers. Probe lives in `PetSecurityProbes.java`, indexed in `security/pets/README.md` and logged in `security/FINDINGS.md`.

## Out of scope

- Additional biome pets, dragon hatchling, and eye-themed pet (Phase 2 Stage 5).
- Custom biome structures and custom structure enemy mobs (Phase 2 Stage 6; Stage 4 provides `artilian:structure_enemies` tag and `ElementalMob` interface for Stage 6 to plug into).
- Final Phase 2 art pass against Alex & Steve's drawings (Phase 2 St).
