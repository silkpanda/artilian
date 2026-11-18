# Security findings — Adventures in Artilian

Every ownership, permission, or world-protection finding and how it was fixed. Log it the moment it's found, even before there's a fix.

**IDs:** `P[phase]-[nn]` for a finding from a phase's security/ownership gate, `REVIEW-[nn]` for one from a code review.

**Status:** OPEN, FIXED (with the commit and the probe or GameTest that proves it), or ACCEPTED (with the reason and who accepted it).

**The gate:** each probe/test in `security/` prints EXPLOITED or HELD. A probe counts only once it has been shown EXPLOITED against a build with the control switched off, then HELD with the control back on. Only test against local dev worlds/servers.

Template for an entry:

### P1-01 — [short description] — OPEN
- **Severity:** [High / Medium / Low]
- **Where:** [file, entity interaction, or server handler]
- **Attack:** [what a non-owner player or action does]
- **Impact:** [what they get (e.g., opening another player's Chester, bypassing TNT protection)]
- **Fix:** [what changed and the commit]
- **Proof:** [probe/GameTest ID and file, red-proven then HELD]

---

### P1-01 — Non-owner opens another player's Chester — FIXED
- **Severity:** High
- **Where:** `chester/ChesterEntity.java` (`mobInteract`, `createMenu`)
- **Attack:** Another player (e.g., Steve) right-clicks Alex's Chester or attempts to open a menu on it.
- **Impact:** With the control off, a non-owner could open a container menu against Chester.
- **Fix:** Both `mobInteract` and `createMenu` check `this.isOwner(player)` server-side; non-owners get a locked chest sound and action-bar message (`message.artilian.chester.not_yours`) and `createMenu` returns `null`. Commit `65354e8`.
- **Proof:** GameTest `ChesterSecurityProbes.p1_01_nonOwnerGetsNoMenu`. EXPLOITED with the owner check switched off; HELD with it on (`25/25` tests passing in `runGameTestServer`).

---

### P1-02 — Forged, distant, or stale Chester screen stays open — FIXED
- **Severity:** High
- **Where:** `chester/ChesterContainer.java` (`stillValid`)
- **Attack:** A player holds a `ChesterContainer` built for a non-owner, moves more than 8 blocks away (`MAX_REACH_SQR`), or keeps a screen open after the Chester entity is discarded/replaced.
- **Impact:** With the control off, a forged or stale container would remain valid and usable indefinitely.
- **Fix:** `ChesterContainer#stillValid` enforces `this.chester.isAlive() && this.chester.isOwner(player) && player.distanceToSqr(this.chester) <= MAX_REACH_SQR`. Commit `65354e8`.
- **Proof:** GameTest `ChesterSecurityProbes.p1_02_staleMenuStopsWorking`. EXPLOITED with `stillValid` checks switched off; HELD with them on.

---

### P1-03 — Owner or Chester dies and Chester's items are lost or dropped — FIXED
- **Severity:** High
- **Where:** `registry/ArtilianAttachments.java` (`CHESTER` attachment), `chester/ChesterData.java`, `chester/ChesterEntity.java`
- **Attack:** No attacker needed. The owner dies (lava, a mob, a fall) and respawns, or Chester is removed via `/kill` or falls into the void.
- **Impact:** With `copyOnDeath()` off, the respawned player gets an empty Chester: all 27 slots gone.
- **Fix:** The 27-slot inventory lives in the per-player attachment `artilian:chester`, registered with `copyOnDeath()` (commit `8cadb48`), and `ChesterEntity` holds no items (`shouldDropLoot()` returns `false`, commit `2706ee4`).
- **Proof:** GameTests `ChesterStorageGameTests.survivesDeath` (EXPLOITED with `copyOnDeath()` removed: `"Lost the 5 diamonds in slot 0 after the owner died"`, `runGameTestServer` exit 1; HELD when restored) and `ChesterEntityGameTests.killingChesterLosesNothing` (verifies `/kill` leaves the owner's attachment intact and drops zero item entities).

---

### P1-04 — Whistle mid-screen or multiple Chesters duplicate items — FIXED
- **Severity:** High
- **Where:** `chester/ChesterData.java`, `chester/ChesterContainer.java`, `chester/ChesterService.java` (`summon`), `chester/ChesterEntity.java` (`discardIfOrphan`)
- **Attack:** A player opens Chester's menu, uses the Chester Whistle to summon a new Chester while the old screen is still open, and pulls items out through both screens or two Chesters.
- **Impact:** If the entity stored items or screens held separate copies, summoning or interacting across two screens could duplicate items.
- **Fix:** `ChesterContainer` reads and mutates the single `ItemStackHandler` in `ChesterData` on the player, `ChesterService#summon` discards the prior loaded Chester and updates `chesterEntityId` (invalidating the old screen via `ChesterContainer#stillValid`), and `ChesterEntity#discardIfOrphan` removes any orphaned Chester when loaded. Commits `82cdb5c` and `65354e8`.
- **Proof:** GameTests `ChesterSecurityProbes.p1_04_twoScreensCannotDuplicate` (EXPLOITED with controls off; HELD with them on) and `ChesterWhistleGameTests.orphanedChesterRemovesItself`.

---

### P1-05 — Non-owner toggles Sit & Sleep, wakes Chester, or opens a sleeping Chester — FIXED
- **Severity:** Medium
- **Where:** `chester/ChesterEntity.java` (`mobInteract`, `toggleSitAndSleep`, `createMenu`)
- **Attack:** A non-owner shift-right-clicks or calls `toggleSitAndSleep` on someone else's Chester to pin them in place, wake them, or open them while disguised as a normal chest.
- **Impact:** With the owner check off, another player could control or open a player's sleeping Chester.
- **Fix:** `toggleSitAndSleep`, `mobInteract`, and `createMenu` all enforce `this.isOwner(player)` whether awake or asleep. Commit `2d8cade`.
- **Proof:** GameTest `ChesterSecurityProbes.p1_05_nonOwnerCannotUseSleep`. EXPLOITED with the owner check removed; HELD with it on.

---

### P1-06 — Hopper or item capability extracts stored items from Chester — FIXED
- **Severity:** High
- **Where:** `chester/ChesterEntity.java`
- **Attack:** A player places a hopper underneath Chester (or queries `Capabilities.ItemHandler.ENTITY` / `ENTITY_AUTOMATION`) to drain items out of another player's Chester.
- **Impact:** With a container or storage item handler exposed on the entity, hoppers or machines could extract private items.
- **Fix:** `ChesterEntity` does not implement `Container`, exposes no `ENTITY_AUTOMATION` capability, and stores no items on the entity's equipment slots. Commit `65354e8`.
- **Proof:** GameTest `ChesterSecurityProbes.p1_06_hoppersGetNothing` (sits Chester on a live hopper for 60 ticks and checks capability queries). EXPLOITED when a storage capability/container was exposed; HELD with it off.

---

### P1-07 — Custom TNT breaks blocks under default kid-safe config (`allowDestructiveCustomTnt = false`) — FIXED
- **Severity:** High
- **Where:** `config/ArtilianConfig.java`, `tnt/ArtilianPrimedTnt.java` (`explodeClusterQuarry`, `SAFE_QUARRY_DAMAGE_CALCULATOR`)
- **Attack:** A player lights Party, Bounce, Frost Nova, or Cluster Quarry TNT beside glass, wood planks, and stone while `allowDestructiveCustomTnt` is at its default `false` value.
- **Impact:** Without the config gate, Cluster Quarry TNT and its bomblets would destroy kids' bases by default.
- **Fix:** `ArtilianConfig.ALLOW_DESTRUCTIVE_CUSTOM_TNT` defaults to `false`; `explodeClusterQuarry` passes `Level.ExplosionInteraction.NONE` and `SAFE_QUARRY_DAMAGE_CALCULATOR#shouldBlockExplode` returns `false` whenever `!ArtilianConfig.isDestructiveTntAllowed()`. Stage 3 on `phase-1-stage-4-custom-tnts`.
- **Proof:** GameTest `TntSecurityProbes.p1_07_defaultConfigBreaksZeroBlocksForAllFourTnts`. EXPLOITED when config check in `explodeClusterQuarry` and `SAFE_QUARRY_DAMAGE_CALCULATOR` was bypassed (`Expected Stone, got Air`); HELD with it on (`41/41` tests passing in `runGameTestServer`).

---

### P1-08 — Cluster Quarry sub-bomblets ignore safe config toggled off before detonation — FIXED
- **Severity:** High
- **Where:** `tnt/ArtilianPrimedTnt.java` (`SAFE_QUARRY_DAMAGE_CALCULATOR#shouldBlockExplode`, `explodeClusterQuarry`)
- **Attack:** Primary Cluster Quarry TNT detonates while `allowDestructiveCustomTnt = true` and spawns 4 generation-1 bomblets, then `allowDestructiveCustomTnt` is `false` before the bomblets' 20-tick fuse expires.
- **Impact:** If bomblets cached destructiveness at spawn time instead of checking `ArtilianConfig.isDestructiveTntAllowed()` at detonation time, they would still destroy blocks in safe mode.
- **Fix:** Both `explodeClusterQuarry` and `SAFE_QUARRY_DAMAGE_CALCULATOR#shouldBlockExplode` query `ArtilianConfig.isDestructiveTntAllowed()` live at detonation time. Stage 3 on `phase-1-stage-4-custom-tnts`.
- **Proof:** GameTest `TntSecurityProbes.p1_08_clusterBombletsRespectSafeConfigOnDetonation`. EXPLOITED (`EXPLOITED P1-08: block broken or altered at BlockPos{x=7, y=2, z=5}, found Block{minecraft:air}`) with config check bypassed; HELD with it on (`41/41` tests passing).

---

### P1-09 — Indirect ignition (redstone, chain blast, flaming arrow, dispenser) spawns vanilla TNT — FIXED
- **Severity:** High
- **Where:** `tnt/ArtilianTntBlock.java` (`onCaughtFire`, `wasExploded`, `registerDispenserBehaviors`)
- **Attack:** A player or contraption ignites a Custom TNT block via redstone, nearby explosion (`wasExploded`), flaming arrow (`onProjectileHit`), or a `DispenserBlock`.
- **Impact:** Inheriting vanilla `TntBlock#wasExploded` or missing `DispenserBlock` registration spawns vanilla `EntityType.TNT` (`PrimedTnt`), bypassing `allowDestructiveCustomTnt` completely.
- **Fix:** `ArtilianTntBlock` overrides `onCaughtFire`, `wasExploded`, and registers per-block `DispenserBlock` behaviors in `FMLCommonSetupEvent` so every path spawns `ArtilianPrimedTnt` (`artilian:primed_tnt`). Stage 3 on `phase-1-stage-4-custom-tnts`.
- **Proof:** GameTest `TntSecurityProbes.p1_09_indirectIgnitionNeverSpawnsVanillaTntOrBreaksBlocks`. EXPLOITED (`EXPLOITED P1-09: indirect ignition spawned non-Artilian TNT entity entity.minecraft.tnt`) when `wasExploded` delegated to `super.wasExploded`; HELD with override restored (`41/41` tests passing).

---

### P1-10 — Safe-blast TNTs or Quarry blasts break containers/safe terrain or harm Players/Chester when config is enabled — FIXED
- **Severity:** High
- **Where:** `tnt/ArtilianPrimedTnt.java` (`explodeParty`, `explodeBounce`, `explodeFrostNova`, `SAFE_QUARRY_DAMAGE_CALCULATOR`)
- **Attack:** With `allowDestructiveCustomTnt = true`, a player ignites Party, Bounce, Frost Nova, and Cluster Quarry TNTs near glass/planks, a `Chest` (`BlockEntity`), a survival `Player`, and `ChesterEntity`.
- **Impact:** If Party/Bounce/Frost Nova honored `allowDestructiveCustomTnt` or Quarry damaged chests/players/Chester, enabling Quarry mode would wreck chests or hurt players.
- **Fix:** Party, Bounce, and Frost Nova never call `Level#explode` with block destruction or player damage; `SAFE_QUARRY_DAMAGE_CALCULATOR` blocks `state.hasBlockEntity()` destruction and returns `false` in `shouldDamageEntity` for `Player` and `ChesterEntity`. Stage 3 on `phase-1-stage-4-custom-tnts`.
- **Proof:** GameTest `TntSecurityProbes.p1_10_safeBlastTntsNeverBreakBlocksEvenWhenConfigEnabled`. EXPLOITED (`EXPLOITED P1-10: floor block broken at BlockPos{x=7, y=1, z=7}, found Block{minecraft:air}`) when `explodeParty` called `Level#explode` under `isDestructiveTntAllowed()`; HELD with fix in place (`41/41` tests passing).

---

### P2-01 — Non-owner captures, commands, heals, or recalls another player's pet or Capture Crystal — FIXED
- **Severity:** High
- **Where:** `pet/PetService.java` (`tryCapturePet`, `useCaptureCrystal`, `feedBiomeTreat`), `pet/ArtilianPetEntity.java` (`toggleSitting`, `mobInteract`)
- **Attack:** Another player (e.g., Steve) uses an empty Capture Crystal on Alex's summoned Crystal Fox, shift-right-clicks to sit it, feeds it a Biome Treat, or uses Alex's filled Capture Crystal to recall or summon it.
- **Impact:** Without owner verification on both the pet entity and `CapturedPetData` on the crystal, a second player could steal, control, or recall someone else's companion.
- **Fix:** Both `ArtilianPetEntity` (`isOwner`) and `CapturedPetData` (`isOwnedBy`) enforce owner UUID matching server-side across capture, sit/follow, feeding, summon, and recall. Stage 2 on `phase-2-stage-3-pet-capture`.
- **Proof:** GameTest `PetSecurityProbes.p2_01_nonOwnerCannotCaptureStealOrRecallOthersPet`. EXPLOITED (`EXPLOITED P2-01: a non-owner recalled or stole from another player's Capture Crystal`) with `isOwnedBy` check bypassed in `PetService#useCaptureCrystal`; HELD with it on (`63/63` tests passing).

---

### P2-02 — Fatal damage kills a tamed pet permanently instead of fainting safely back into its Capture Crystal — FIXED
- **Severity:** High
- **Where:** `pet/ArtilianPetEntity.java` (`shouldFaintInsteadOfDie`, `actuallyHurt`, `die`), `pet/PetService.java` (`isNonLethalFaintingEnabled`, `faintPet`), `registry/ArtilianAttachments.java` (`PETS` with `copyOnDeath()`)
- **Attack:** A summoned Crystal Fox takes massive fatal combat/environmental damage (`500.0F` mobAttack) or its owner dies while the pet is resting.
- **Impact:** Without the non-lethal fainting guard and `copyOnDeath()` attachment persistence, kids permanently lose named/bonded pets in combat or on respawn.
- **Fix:** `ArtilianPetEntity#actuallyHurt` and `#die` intercept fatal damage before death when `PetService#isNonLethalFaintingEnabled()` is true, safely saving `fainted = true` + `100`-tick rest timer into both `PetRosterData` (`copyOnDeath()`) and the owner's `CaptureCrystalItem` with zero drops. Stage 2 on `phase-2-stage-3-pet-capture`.
- **Proof:** GameTest `PetSecurityProbes.p2_02_nonLethalFaintingSavesPetIntoCrystalInsteadOfDying`. EXPLOITED (`EXPLOITED P2-02: lethal damage killed the pet instead of fainting it safely into its Capture Crystal`) when `isNonLethalFaintingEnabled()` returned `false`; HELD with it on (`63/63` tests passing).

---

### P2-03 — Copied or out-of-inventory Capture Crystal duplicates an active pet or bypasses faint rest — FIXED
- **Severity:** Medium
- **Where:** `pet/PetService.java` (`useCaptureCrystal`, `reconcileWithRoster`), `pet/PetRosterData.java`, `pet/ArtilianPetEntity.java` (`discardIfOrphan`)
- **Attack:** A player clones a filled Capture Crystal (`crystalB = crystalA.copy()`) and uses `crystalB` while `crystalA`'s pet is active, or removes `crystalA` from inventory before the pet faints and re-uses the stale crystal snapshot.
- **Impact:** Without server-side `PetRosterData` reconciliation keyed by `petId`, stale or cloned crystals could spawn duplicate pet entities or bypass the faint rest window.
- **Fix:** `PetService#useCaptureCrystal` reconciles every crystal against `PetRosterData` (`petId -> activeEntityUuid, fainted, readyAtTick`) before acting, recalls active instances instead of spawning duplicates, and `ArtilianPetEntity#discardIfOrphan` removes any non-authoritative instance on tick. Stage 2 on `phase-2-stage-3-pet-capture`.
- **Proof:** GameTest `PetSecurityProbes.p2_03_clonedCrystalCannotDuplicateActivePet`. EXPLOITED (`EXPLOITED P2-03: out-of-inventory crystal bypassed faint rest and spawned a duplicate pet`) when `reconcileWithRoster` was bypassed; HELD with it on (`63/63` tests passing).

---

### P2-04 — Tamed pet battle AI targets or harms Players, Chester, villagers, friendly golems, or allied/tamed pets — FIXED
- **Severity:** High
- **Where:** `pet/PetService.java` (`isValidBattleTarget`, `isValidAutonomousBattleTarget`, `performElementalAttack`), `pet/ArtilianPetEntity.java` (`canAttack`, battle target goals)
- **Attack:** A summoned Crystal Fox stands next to another `Player` (`Steve`), `ChesterEntity`, `Villager`, `IronGolem`, tamed `Wolf`, befriended `CrystalFoxEntity`, and `ArmorStand` while provoked via owner-hurt (`setLastHurtByMob`) / owner-assist (`setLastHurtMob`), autonomous target scans, and direct `doHurtTarget`.
- **Impact:** Without unified friendly-entity exclusion across battle target acquisition and melee execution, co-op players or companions could injure siblings, Chester chests, village traders, or allied pets.
- **Fix:** `PetService#isValidBattleTarget` rejects all `Player`, `ChesterEntity`, `ArmorStand`, `AbstractVillager`, non-hostile `AbstractGolem`, tamed/befriended pets, and baby animals, enforced identically by `ArtilianPetEntity#canAttack`, target goals, and `PetService#performElementalAttack`. Stage 2 on `phase-2-stage-4-pet-battle-ai`.
- **Proof:** GameTest `PetSecurityProbes.p2_04_tamedPetNeverAttacksPlayersChesterVillagersOrAlliedPets`. EXPLOITED (`EXPLOITED P2-04: pet target filter allowed entity.artilian.chester`) when friendly entity checks were bypassed in `PetService#isValidBattleTarget`; HELD with checks restored (`68/68` tests passing).

---

### P2-05 — Sunbeam's sun-aura light replaces a real block or is left behind in the world — FIXED
- **Severity:** High
- **Where:** `pet/SunbeamEntity.java` (`updatePlacedLight`, `clearPlacedLight`, `remove`)
- **Attack:** Sunbeam moves its chest over a solid block, or is removed from the world (recalled, fainted, unloaded, discarded) while its light block is still placed.
- **Impact:** Without the air check, the aura would overwrite terrain, floors, or builds with `minecraft:light`; without the removal cleanup, it would leave stray permanent light blocks in the world.
- **Fix:** `updatePlacedLight()` places a level-15 `minecraft:light` only when the chest target `isAir()`, tracks just that one `BlockPos`, and clears it first whenever the target changes. `clearPlacedLight()` removes the block only if it is still `Blocks.LIGHT`, and `remove(RemovalReason)` clears it on every removal path. The position is saved with the entity (`SunbeamPlacedLight`), so a Sunbeam saved while lit (server stop, chunk unload) still clears its old light after reload.
- **Proof:** GameTest `PetRosterGameTests.sunbeamLightOnlyReplacesAirAndIsRemovedWithIt` (failure messages begin `EXPLOITED P2-05:`). EXPLOITED (`EXPLOITED P2-05: sun aura replaced a real block`, `forgot its light ... would orphan it on reload`, `light block left behind`); HELD with the air check, NBT persistence, and removal cleanup in place.

---

### P3-01 — Boss Altar spam summons many bosses or eats summon items — FIXED
- **Severity:** Medium
- **Where:** `boss/BossAltarBlockEntity.java` (`isBusy`, `trySummon`), `boss/BossAltarBlock.java` (`useItemOn`)
- **Attack:** A player on a LAN world holds right-click on a Boss Altar with a stack of summon items (20 Golem Cores in 20 ticks), or uses the altar again while its boss is still alive.
- **Impact:** Without a one-boss-per-altar rule, every click starts or restarts a summon: the stack is drained for nothing, and with the countdown check also gone, bosses pile up until the server stalls.
- **Fix:** `BossAltarBlockEntity#isBusy` is true while a countdown runs or while the altar's stored boss UUID resolves to a living, loaded entity. `#trySummon` checks it before touching the stack and sends `message.artilian.boss_altar.busy` instead. The altar frees itself when its boss dies or is removed, so rematches work. Commit `Boss Framework Stage 3` on `phase-3-stage-1-boss-framework`.
- **Proof:** GameTest `BossSecurityProbes.p3_01_altarSpamSummonsOneBoss`. EXPLOITED (`EXPLOITED P3-01: altar spam ate 20 Golem Cores for one boss`) with `isBusy` forced to `false`, and again with the stack consumed before the busy check; HELD with the control on (`116/116` tests passing). With `isBusy` off, each click restarts the countdown, so the spam drains the stack rather than stacking bosses; the probe asserts both the boss count and the core count.
