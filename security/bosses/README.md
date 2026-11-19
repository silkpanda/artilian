# Boss World-Protection Probes (`P3-01`–`P3-03`)

Anti-spam and non-griefing probes for the Boss Framework (`PLAN.md` Phase 3 Stage 1, Sub-stage 3), Crystal Colossus (`PLAN.md` Phase 3 Stage 2, Sub-stage 2), and Captain Brimstone (`PLAN.md` Phase 3 Stage 3, Sub-stage 3).
Each probe is a NeoForge GameTest and was red-proven (`EXPLOITED` with its control switched off, then `HELD` with the control on). See [`security/FINDINGS.md`](../FINDINGS.md) for full details.

## Running the probes

```powershell
.\gradlew.bat runGameTestServer --no-daemon
```

The probes run automatically as part of the `artilian` GameTest suite (`153/153` required tests).

## Probe index

| ID | Probe | Control under test | GameTest location | Status |
|---|---|---|---|---|
| `P3-01` | One player right-clicks a Boss Altar 20 times in 20 ticks holding 20 Golem Cores: exactly one boss appears and 19 cores are left | `BossAltarBlockEntity#isBusy` (countdown running, or this altar's boss alive and loaded), checked in `#trySummon` before the item is consumed | `BossSecurityProbes.p3_01_altarSpamSummonsOneBoss` (`src/main/java/com/silkpanda/artilian/gametest/security/BossSecurityProbes.java`) | HELD (Stage 1) |
| `P3-02` | Under `mobGriefing=true` and `allowDestructiveCustomTnt=true`, Crystal Quake, targeted Starlight Spire, and Overcharged Prism Nova break zero blocks/altars/chests and deal `0` damage to Chester | `ArtilianBossEntity#impact` & `CrystalColossusEntity#onTelegraphImpact` (direct elemental damage without block explosions; `ChesterEntity` exclusion) | `BossSecurityProbes.p3_02_crystalColossusNeverBreaksBlocksOrAltarOrHarmsChester` (`src/main/java/com/silkpanda/artilian/gametest/security/BossSecurityProbes.java`) | HELD (Stage 2) |
| `P3-03` | Under `mobGriefing=true` and `allowDestructiveCustomTnt=true`, Powder Keg Toss, Cutlass Cleave, and Broadside Barrage break zero blocks/altars/chests, place zero fire blocks, and deal `0` damage to Chester | `ArtilianBossEntity#impact` & `CaptainBrimstoneEntity#onTelegraphImpact` (direct elemental damage without block explosions or terrain fire; `ChesterEntity` exclusion) | `BossSecurityProbes.p3_03_captainBrimstoneNeverBreaksBlocksOrAltarOrIgnitesTerrainOrHarmsChester` (`src/main/java/com/silkpanda/artilian/gametest/security/BossSecurityProbes.java`) | HELD (Stage 3) |
