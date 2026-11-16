# Chester Security Probes (`P1-01` – `P1-06`)

Ownership, persistence, and anti-duplication probes for Chester (`PLAN.md` Phase 1 Stage 2).
Every probe is implemented as a NeoForge GameTest and was red-proven (`EXPLOITED` with its
control switched off, then `HELD` with the control on) before merging. See
[`security/FINDINGS.md`](../FINDINGS.md) for full details.

## Running the probes

```powershell
.\gradlew.bat runGameTestServer --no-daemon
```

All 6 probes run automatically as part of the `artilian` GameTest suite (`25/25` required tests).

## Probe index

| ID | Probe | Control under test | GameTest location | Status |
|---|---|---|---|---|
| `P1-01` | Non-owner right-clicks another player's Chester and gets a menu | Owner check in `ChesterEntity#mobInteract` and `ChesterEntity#createMenu` | `ChesterSecurityProbes.p1_01_nonOwnerGetsNoMenu` (`src/main/java/com/silkpanda/artilian/gametest/security/ChesterSecurityProbes.java`) | HELD (`65354e8`) |
| `P1-02` | Forged, distant, or stale container stays valid after Chester is gone or player moves away | Owner, alive, and reach checks in `ChesterContainer#stillValid` | `ChesterSecurityProbes.p1_02_staleMenuStopsWorking` (`src/main/java/com/silkpanda/artilian/gametest/security/ChesterSecurityProbes.java`) | HELD (`65354e8`) |
| `P1-03` | Owner or Chester dies; stored items are lost or dropped on the ground | `copyOnDeath()` on `ArtilianAttachments.CHESTER`; items stored on player rather than entity | `ChesterStorageGameTests.survivesDeath` (`ChesterStorageGameTests.java`) & `ChesterEntityGameTests.killingChesterLosesNothing` (`ChesterEntityGameTests.java`) | HELD (`8cadb48`, `2706ee4`) |
| `P1-04` | Whistle mid-screen or two open screens turn one stack into two | Single backing `ItemStackHandler` in `ChesterData` on the player; old Chester discarded in `ChesterService#summon` | `ChesterSecurityProbes.p1_04_twoScreensCannotDuplicate` (`src/main/java/com/silkpanda/artilian/gametest/security/ChesterSecurityProbes.java`) | HELD (`65354e8`) |
| `P1-05` | Non-owner toggles Sit & Sleep, wakes Chester, or opens a sleeping Chester | Owner checks in `ChesterEntity#toggleSitAndSleep`, `mobInteract`, and `createMenu` | `ChesterSecurityProbes.p1_05_nonOwnerCannotUseSleep` (`src/main/java/com/silkpanda/artilian/gametest/security/ChesterSecurityProbes.java`) | HELD (`2d8cade`) |
| `P1-06` | Hopper or item capability extracts stored items from the Chester entity | `ChesterEntity` does not implement `Container` and exposes no `ENTITY_AUTOMATION` or storage `ItemHandler` capability | `ChesterSecurityProbes.p1_06_hoppersGetNothing` (`src/main/java/com/silkpanda/artilian/gametest/security/ChesterSecurityProbes.java`) | HELD (`65354e8`) |
