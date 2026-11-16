# Custom TNT World-Protection Probes (`P1-07` – `P1-10`, `REVIEW-01`, `REVIEW-02`)

Anti-griefing and `allowDestructiveCustomTnt` probes for Custom TNTs (`PLAN.md` Phase 1 Stage 4).
Every probe is implemented as a NeoForge GameTest and was red-proven (`EXPLOITED` with its
control switched off, then `HELD` with the control on). See [`security/FINDINGS.md`](../FINDINGS.md)
for full details.

## Running the probes

```powershell
.\gradlew.bat runGameTestServer --no-daemon
```

All probes run automatically as part of the `artilian` GameTest suite (`81/81` required tests as of PR #10 rebased onto `main`).

## Probe index

| ID | Probe | Control under test | GameTest location | Status |
|---|---|---|---|---|
| `P1-07` | Default config (`allowDestructiveCustomTnt = false`) breaks zero blocks across all 4 Custom TNTs | `ArtilianConfig.isDestructiveTntAllowed()` & `Level.ExplosionInteraction.NONE` in `ArtilianPrimedTnt#explodeClusterQuarry` | `TntSecurityProbes.p1_07_defaultConfigBreaksZeroBlocksForAllFourTnts` (`src/main/java/com/silkpanda/artilian/gametest/security/TntSecurityProbes.java`) | HELD (Stage 3) |
| `P1-08` | Cluster Quarry generation-1 bomblets respect `allowDestructiveCustomTnt = false` at detonation time even if primary spawned while enabled | Live `ArtilianConfig.isDestructiveTntAllowed()` check inside `SAFE_QUARRY_DAMAGE_CALCULATOR#shouldBlockExplode` & `explodeClusterQuarry` | `TntSecurityProbes.p1_08_clusterBombletsRespectSafeConfigOnDetonation` (`src/main/java/com/silkpanda/artilian/gametest/security/TntSecurityProbes.java`) | HELD (Stage 3) |
| `P1-09` | Redstone, chain blast (`wasExploded`), flaming arrow, and dispenser ignition prime `ArtilianPrimedTnt` (never vanilla `PrimedTnt`) and break zero blocks | `ArtilianTntBlock#onCaughtFire`, `#wasExploded`, and `registerDispenserBehaviors` | `TntSecurityProbes.p1_09_indirectIgnitionNeverSpawnsVanillaTntOrBreaksBlocks` (`src/main/java/com/silkpanda/artilian/gametest/security/TntSecurityProbes.java`) | HELD (Stage 3) |
| `P1-10` | Even when `allowDestructiveCustomTnt = true`, `Party`, `Bounce`, and `Frost Nova` never break solid blocks or damage Players/Chester, while `Cluster Quarry` spares chests/Players/Chester | Non-destructive handlers in `explodeParty`/`explodeBounce`/`explodeFrostNova` and `state.hasBlockEntity()` + `shouldDamageEntity` in `SAFE_QUARRY_DAMAGE_CALCULATOR` | `TntSecurityProbes.p1_10_safeBlastTntsNeverBreakBlocksEvenWhenConfigEnabled` (`src/main/java/com/silkpanda/artilian/gametest/security/TntSecurityProbes.java`) | HELD (Stage 3) |
| `REVIEW-01` | Cluster Quarry spares tamed pets, villagers, iron golems, and dropped items in both config modes, and still hurts monsters | `SAFE_QUARRY_DAMAGE_CALCULATOR#shouldDamageEntity` asking `KidSafeTargets#isValidOffensiveTarget` | `ReviewFixProbes.review_01_quarrySafeModeSparesPetsVillagersAndItems`, `review_01b_quarryDestructiveModeStillSparesThem` (`src/main/java/com/silkpanda/artilian/gametest/security/ReviewFixProbes.java`) | HELD (review fixes) |
| `REVIEW-02` | Frost Nova spares villagers, baby animals, and iron golems, and still freezes monsters | `explodeFrostNova` asking `KidSafeTargets#isValidOffensiveTarget` | `ReviewFixProbes.review_02_frostNovaSparesVillagersBabiesAndGolems` (`src/main/java/com/silkpanda/artilian/gametest/security/ReviewFixProbes.java`) | HELD (review fixes) |
