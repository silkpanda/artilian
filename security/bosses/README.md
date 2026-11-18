# Boss World-Protection Probes (`P3-01`)

Anti-spam probe for the Boss Altar (`PLAN.md` Phase 3 Stage 1, `qa/Adventures in Artilian - Boss Framework Plan 2026-11-17.md` Stage 3).
The probe is a NeoForge GameTest and was red-proven (`EXPLOITED` with its control switched off, then `HELD` with the control on). See [`security/FINDINGS.md`](../FINDINGS.md) for full details.

## Running the probes

```powershell
.\gradlew.bat runGameTestServer --no-daemon
```

The probe runs automatically as part of the `artilian` GameTest suite (`116/116` required tests).

## Probe index

| ID | Probe | Control under test | GameTest location | Status |
|---|---|---|---|---|
| `P3-01` | One player right-clicks a Boss Altar 20 times in 20 ticks holding 20 Golem Cores: exactly one boss appears and 19 cores are left | `BossAltarBlockEntity#isBusy` (countdown running, or this altar's boss alive and loaded), checked in `#trySummon` before the item is consumed | `BossSecurityProbes.p3_01_altarSpamSummonsOneBoss` (`src/main/java/com/silkpanda/artilian/gametest/security/BossSecurityProbes.java`) | HELD (Stage 3) |
