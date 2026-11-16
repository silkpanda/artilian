# Pet Capture & Battle Security & Kid-Safety Probes (`P2-01` – `P2-04`)

Ownership, non-lethal fainting, anti-duplication, and battle target safety probes for Artilian pets (`PLAN.md` Phase 2 Stages 3–4).
Every probe is implemented as a NeoForge GameTest and was red-proven (`EXPLOITED` with its
control switched off, then `HELD` with the control on) before merging. See
[`security/FINDINGS.md`](../FINDINGS.md) for full details.

## Running the probes

```powershell
.\gradlew.bat runGameTestServer --no-daemon
```

All 4 probes run automatically as part of the `artilian` GameTest suite (`68/68` required tests).

## Probe index

| ID | Probe | Control under test | GameTest location | Status |
|---|---|---|---|---|
| `P2-01` | Non-owner attempts to capture, command to sit, feed, or recall another player's pet or Capture Crystal | Owner checks in `PetService#tryCapturePet`, `PetService#useCaptureCrystal`, `PetService#feedBiomeTreat`, and `ArtilianPetEntity#toggleSitting` | `PetSecurityProbes.p2_01_nonOwnerCannotCaptureStealOrRecallOthersPet` (`src/main/java/com/silkpanda/artilian/gametest/security/PetSecurityProbes.java`) | HELD (Stage 2) |
| `P2-02` | Summoned tamed pet takes fatal damage (`500F`) or owner dies | `ArtilianPetEntity#shouldFaintInsteadOfDie` (`PetService#isNonLethalFaintingEnabled()`), `PetService#faintPet`, and `copyOnDeath()` on `ArtilianAttachments.PETS` | `PetSecurityProbes.p2_02_nonLethalFaintingSavesPetIntoCrystalInsteadOfDying` (`src/main/java/com/silkpanda/artilian/gametest/security/PetSecurityProbes.java`) | HELD (Stage 2) |
| `P2-03` | Copied/cloned Capture Crystal or out-of-inventory crystal attempts to duplicate an active or fainted pet | Single `petId` tracking in `PetRosterData`, `PetService#reconcileWithRoster`, and `ArtilianPetEntity#discardIfOrphan` | `PetSecurityProbes.p2_03_clonedCrystalCannotDuplicateActivePet` (`src/main/java/com/silkpanda/artilian/gametest/security/PetSecurityProbes.java`) | HELD (Stage 2) |
| `P2-04` | Provoked or autonomously scanning tamed pet encounters another player (`Bob`), `ChesterEntity`, villager, Iron Golem, tamed wolf, befriended pet, or armor stand | Friendly entity exclusion in `PetService#isValidBattleTarget`, `ArtilianPetEntity#canAttack`, and `PetService#performElementalAttack` | `PetSecurityProbes.p2_04_tamedPetNeverAttacksPlayersChesterVillagersOrAlliedPets` (`src/main/java/com/silkpanda/artilian/gametest/security/PetSecurityProbes.java`) | HELD (Stage 2) |
