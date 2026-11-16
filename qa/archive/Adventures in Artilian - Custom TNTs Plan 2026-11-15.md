# Adventures in Artilian — Custom TNTs Plan (2026-11-15)

<!-- PLAN.md Phase 1 Stage 4. One PLAN stage, one PR, one jar. The numbered stages below are
commits inside that PR. -->

> **Status: All 4 stages built and verified locally, in PR.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Config, blocks, entity, telegraph, datagen & art | `9f2083e` — `ArtilianConfig` (`allowDestructiveCustomTnt` default `false`), `TntVariant`, `ArtilianTntBlock`, `ArtilianPrimedTnt` (30-tick telegraph ring + rising chime), dispenser wiring, `tools/art/make_tnt_art.py`, datagen (`tnt_arena.nbt`, blockstates/models/loot/recipes/lang), `34/34` tests. Red-proof: skipped `emitTelegraphRing(serverLevel)` in `ArtilianPrimedTnt#tick` -> `primedcustomtntemitstelegraphbeforedetonation` failed; restored green (`34/34`). |
> | 2 Party, Bounce & Frost Nova detonations | `b436ac1` — `explodeParty` (Speed II, Regen I, hop, celebration particles/sounds, zero damage/block breaks), `explodeBounce` (launches entities with Slow Falling + Jump Boost while sparing sleeping Chester, zero damage/block breaks), `explodeFrostNova` (freezes nearby water into `Frosted Ice`, freeze-damages/slows hostiles while sparing Players, Chester, and tamed pets), `37/37` tests. Red-proof: omitted `MobEffects.SLOW_FALLING` in `explodeBounce` -> `bouncetntlaunchesentitieswithslowfallingandleavessleepingchesterput` failed; restored green (`37/37`). |
> | 3 Cluster Quarry TNT & security gate (`P1-07`–`P1-10`) | `1dab695` — `explodeClusterQuarry` (`SAFE_QUARRY_DAMAGE_CALCULATOR` checking `ArtilianConfig.isDestructiveTntAllowed()`, sparing `hasBlockEntity()` blocks, Players, & Chester, safe-mode action bar notice, 4 gen-1 bomblets with 20t fuse), `TntSecurityProbes` (`P1-07`–`P1-10`), `security/FINDINGS.md`, `security/tnt/README.md`, `41/41` tests. Red-proof: bypassed config check (`P1-07` + `EXPLOITED P1-08`), delegated `wasExploded` to `super` (`EXPLOITED P1-09`), and exploded `Party TNT` when config enabled (`EXPLOITED P1-10`); restored green (`41/41`). |
> | 4 Client verification, docs & PR | Done in Stage 4 commit — temporary `./gradlew runClient` showcase captured in `qa/evidence/custom-tnts-showcase-2026-11-15.png` (clean `run/logs/latest.log`, reverted temporary `ArtilianClient` harness), updated `deploy/README.md` & `PLAN.md`, verified `runData`, `./gradlew build`, & `41/41` `runGameTestServer`, pushed `phase-1-stage-4-custom-tnts` and opened stacked PR. |

## Why

Steve asked for custom TNTs, and both kids need playful, expressive explosives that won't accidentally blow up their builds by default. Right now there are no custom TNTs and no `allowDestructiveCustomTnt` safety switch. This stage adds four distinct custom TNTs (**Party TNT**, **Bounce TNT**, **Frost Nova TNT**, and **Cluster Quarry TNT**), the kid-safe common config `allowDestructiveCustomTnt = false`, and the `1.5`-second visual/audio fuse telegraph from `DESIGN.md`.

## Decisions

1. **Kid-safe default (`allowDestructiveCustomTnt = false`).** Registered in `ArtilianConfig` (`config/artilian-common.toml`). When `false`, **no** custom TNT breaks blocks. **Party**, **Bounce**, and **Frost Nova** are always non-destructive to solid terrain even if the config is turned on (`Frost Nova` only freezes nearby water into `Frosted Ice`). Only **Cluster Quarry TNT** is destructive-capable when `allowDestructiveCustomTnt = true` (and still protects `BlockEntity` containers like chests and protects Players and Chester from explosion damage).
2. **Every ignition path spawns `ArtilianPrimedTnt`.** `ArtilianTntBlock` overrides `onCaughtFire`, `wasExploded`, `useWithoutItem` (`Shift + Right-Click` with empty hand for young-kid convenience), `getFlammability`, `getFireSpreadSpeed`, and `DispenserBlock` behavior so redstone, Flint & Steel, fire charges, chain explosions, flaming arrows, and dispensers always spawn `ArtilianPrimedTnt` (never vanilla `PrimedTnt`).
3. **1.5-second `DESIGN.md` telegraph (`TELEGRAPH_TICKS = 30`).** During the final 30 ticks before detonation, `ArtilianPrimedTnt` emits a 16-point ground ring of variant-colored particles every 5 ticks and plays a rising `NOTE_BLOCK_PLING` chime (`0.8F` -> `2.0F`) so kids always see and hear the blast coming.
4. **Placeholder art via `tools/art/make_tnt_art.py`.** Distinct top/side/bottom textures for all four TNTs using the `DESIGN.md` palette, rendered cleanly on both blocks and primed entities (`TntRenderer`).

## Stages

### Stage 1: Config, blocks, entity, telegraph, datagen & art
- **Changes:** `ArtilianConfig` (`COMMON_SPEC`, `allowDestructiveCustomTnt` defaulting to `false`, `setTestOverride`), `TntVariant`, `ArtilianTntBlock`, `ArtilianPrimedTnt` (variant/cluster sync, NBT save/load, 30-tick telegraph ring & rising chime, bounce physics hook), `ArtilianBlocks` / `ArtilianItems` / `ArtilianCreativeTabs` / `ArtilianEntities` / `ArtilianClient` / `ArtilianMod` (config + `FMLCommonSetupEvent` dispenser registration), `tools/art/make_tnt_art.py`, datagen (`ArtilianBlockStateProvider`, `ArtilianBlockLootSubProvider`, `ArtilianRecipeProvider`, `ArtilianLanguageProvider`).
- **Tests, written first and seen failing:** `CustomTntGameTests`: `defaultConfigIsKidSafeAndRecipesExist`, `primedCustomTntEmitsTelegraphBeforeDetonation`.
- **Red-proof:** disable the `TELEGRAPH_TICKS` emission loop in `ArtilianPrimedTnt#tick` and confirm `primedCustomTntEmitsTelegraphBeforeDetonation` fails; restore and confirm green.
- **Done When:** `runData`, `build`, and `runGameTestServer` pass with all Stage 1 tests green.

### Stage 2: Party, Bounce & Frost Nova detonations
- **Changes:** `ArtilianPrimedTnt` detonation handlers:
  - `Party TNT`: firework/note/heart/happy-villager celebration particles + chimes, Speed II (`200` ticks) + Regeneration I (`100` ticks) + small cheerful hop, zero damage, zero blocks broken.
  - `Bounce TNT`: slime/cloud burst + wind/slime sound, launches nearby entities (`y = 1.15`, `Slow Falling` + `Jump Boost II`) while leaving sleeping `ChesterEntity` stationary, zero damage, zero blocks broken.
  - `Frost Nova TNT`: snowflake burst, freezes nearby `Blocks.WATER` into `Blocks.FROSTED_ICE`, applies `6.0F` freeze damage + freeze ticks + `Slowness IV` + `Weakness` to non-player, non-Chester, unowned living mobs while sparing Players, Chester, and tamed pets.
- **Tests, written first and seen failing:** `CustomTntGameTests`: `partyTntBuffsPlayersWithoutDamageOrBlockBreak`, `bounceTntLaunchesEntitiesWithSlowFallingAndLeavesSleepingChesterPut`, `frostNovaFreezesHostilesAndWaterWhileSparingPlayersAndChester`.
- **Red-proof:** temporarily omit `MobEffects.SLOW_FALLING` in `explodeBounce` (and/or player immunity in `explodeFrostNova`), confirm GameTest fails, restore and confirm green.
- **Done When:** `build` and `runGameTestServer` pass with all Stage 2 tests green.

### Stage 3: Cluster Quarry TNT & security gate (`P1-07`–`P1-10`)
- **Changes:** `ArtilianPrimedTnt#explodeClusterQuarry` with `SAFE_QUARRY_DAMAGE_CALCULATOR` (honors `ArtilianConfig.isDestructiveTntAllowed()`, spares `hasBlockEntity()` blocks, spares Players & Chester, emits safe-mode action bar message when disabled, spawns 4 generation-1 bomblets with `20`-tick fuse). Security probes in `src/main/java/com/silkpanda/artilian/gametest/security/TntSecurityProbes.java`, documented in `security/FINDINGS.md` and `security/tnt/README.md`.
- **Tests, written first and seen failing:** `TntSecurityProbes`: `p1_07_defaultConfigBreaksZeroBlocksForAllFourTnts`, `p1_08_clusterBombletsRespectSafeConfigOnDetonation`, `p1_09_indirectIgnitionNeverSpawnsVanillaTntOrBreaksBlocks`, `p1_10_safeBlastTntsNeverBreakBlocksEvenWhenConfigEnabled`.
- **Red-proof:** see Security gate table below; each probe proven EXPLOITED with its control switched off, then HELD with it on.
- **Done When:** all 4 security probes are red-proven, HELD, logged in `security/FINDINGS.md`, and `runGameTestServer` passes.

### Stage 4: Client verification, docs & PR
- **Changes:** Verify all 4 Custom TNT blocks, Creative Tab entries, and primed entities in `./gradlew runClient` (zero missing-texture/model errors), capture `qa/evidence/custom-tnts-showcase-2026-11-15.png`, update `deploy/README.md`, update this plan & `PLAN.md` **Now**, run `runData`/`build`/`runGameTestServer`, push branch, and open PR stacked on `phase-1-stage-3-ores-and-gear`.
- **Done When:** `runData` leaves `src/generated` clean, `build` and `runGameTestServer` pass, client evidence is saved, and the PR is open.

## Security gate

Applies: `allowDestructiveCustomTnt` world-protection control. Probes live in `src/main/java/com/silkpanda/artilian/gametest/security/TntSecurityProbes.java`, indexed in `security/tnt/README.md`, logged in `security/FINDINGS.md`.

| ID | Probe | Control switched off for red-proof |
|---|---|---|
| `P1-07` | Default config (`allowDestructiveCustomTnt = false`) breaks zero blocks across all 4 Custom TNTs | Config check in `explodeClusterQuarry` (`ExplosionInteraction.NONE`) |
| `P1-08` | Cluster Quarry secondary bomblets respect `allowDestructiveCustomTnt = false` dynamically on detonation | Live `ArtilianConfig.isDestructiveTntAllowed()` check inside `SAFE_QUARRY_DAMAGE_CALCULATOR#shouldBlockExplode` |
| `P1-09` | Redstone, chain blast, flaming arrow, and dispenser ignition spawn `ArtilianPrimedTnt` (never vanilla `PrimedTnt`) and break zero blocks | `wasExploded` / dispenser override returning `ArtilianPrimedTnt` |
| `P1-10` | `Party`, `Bounce`, and `Frost Nova` never break solid blocks or harm Players/Chester even when `allowDestructiveCustomTnt = true` | Non-destructive behavior in `explodeParty`/`explodeBounce`/`explodeFrostNova` |

## Out of scope

- Boss or Dragonfire Mega TNTs (arrive in Phase 3 and Phase 5, and will also honor `allowDestructiveCustomTnt`).
- Automated claim/chunk protection integration beyond `allowDestructiveCustomTnt` and `BlockEntity` protection.
