# Adventures in Artilian: Chester Plan (2026-11-15)

<!-- PLAN.md Phase 1 Stage 2. One PLAN stage, one PR, one jar. The numbered stages below are
commits inside that PR. -->

> **Status: All 6 stages built and verified locally, in PR (waiting on LAN co-op playtest).** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Storage | `8cadb48`. Local: `runData` (no change to `src/generated`), `build`, `runGameTestServer` green (4/4 required tests). Red-proof 1: `copyOnDeath()` removed, `survivesDeath` failed ("Lost the 5 diamonds in slot 0 after the owner died"), exit 1. Red-proof 2: `serializeNBT` skipping `Inventory`, `survivesSaveAndLoad` failed, and `survivesDeath` with it, because the death copy goes through the same serializer. Both restored, 4/4 green again. Jar holds `META-INF/jarjar/geckolib-neoforge-1.21.1-4.9.3.jar`. `survivesDeath` drives `ServerPlayer#restoreFrom(old, false)` on NeoForge `FakePlayer`s, the call the game makes on respawn. |
> | 2 Entity & first login | `2706ee4`. `ChesterEntity` (GeckoLib `GeoEntity`, owner UUID, `FollowOwnerGoal`, `isInvulnerableTo` everything except void/`/kill`), placeholder geo/animation/texture, `ChesterService.handleLogin` first-login spawn. GameTests: `firstLoginSpawnsOneChester`, `secondLoginSpawnsNothing`, `eachPlayerGetsTheirOwn`, `nothingHurtsChester`, `killingChesterLosesNothing`. Red-proof: removing `firstSpawnDone` check fails `secondLoginSpawnsNothing`; removing invulnerability override fails `nothingHurtsChester`. |
> | 3 Whistle | `82cdb5c`. `ChesterWhistleItem` (given on first login, kept out of death drops and returned on respawn/login, stick + gold nugget recipe/model/lang via datagen, 40-tick cooldown, flute sound). `ChesterService.summon` removes prior loaded Chester and updates `chesterEntityId`; `discardIfOrphan` removes stale loaded Chesters. GameTests: `whistleWithNoChesterSpawnsOne`, `whistleLeavesExactlyOne`, `orphanedChesterRemovesItself`, `whistleDoesNotTouchTheItems`, `firstLoginGivesOneWhistle`, `whistleComesBackAfterDeath`. Red-proof: removing orphan self-removal fails `orphanedChesterRemovesItself`; removing old-Chester discard in `summon` fails `whistleLeavesExactlyOne`. |
> | 4 Menu & ownership gate | `65354e8`. `ChesterContainer` (27-slot `ChestMenu`, `MAX_REACH_SQR` = 64, alive & owner checks in `stillValid`), server-side owner check in `mobInteract` & `createMenu`, non-owner locked message. Security probes `P1-01`, `P1-02`, `P1-04`, `P1-06` in `ChesterSecurityProbes` each seen EXPLOITED with control off and HELD with control on. |
> | 5 Sleeping form | `2d8cade`. Synced `ASLEEP` data, `ToldToSleep` saved in NBT, `Shift + Right-Click` Sit & Sleep toggle, owner bed sleep/wake (`applyOwnerBedState`), immobile & unpushable while asleep (`holdStill`, `isImmobile`, `isPushable`), snaps to 90° yaw with chest close/open sounds and particles, `ChesterModel` hides `eyes` & `feet` bones while asleep. GameTests: `ownerShiftClickTogglesSleep`, `bedSleepAndWake`, `sitAndSleepSurvivesMorning`, `sitAndSleepSurvivesSaving`, `sleepingChesterStaysPut`, and security probe `P1-05`. Red-proof: removing owner check on toggle, `holdStill`, `toldToSleep` precedence, and NBT save each fail their test. |
> | 6 Placeholder art, LAN playtest, jar | `e1248d9`. Matched Chester's wood panels in `tools/art/make_chester_art.py` / `chester.png` to the vanilla chest and centered pupils; captured side-by-side in-game comparison in `qa/evidence/chester-forms-2026-11-15.png`. Updated `deploy/README.md` (GeckoLib bundled via `jarJar`, no sharing toggle) and `README.md`. Verified `runData` (clean `src/generated`), `build` (`adventures-in-artilian-0.1.0.jar` holds `META-INF/jarjar/geckolib-neoforge-1.21.1-4.9.3.jar`), and `runGameTestServer` (`25/25` required tests passed in 526.2 ms). LAN co-op playtest by the owner, Alex, and Steve pending merge/release. |

## Why

Alex asked for "a personal cartoony living chest friend named Chester" who sleeps looking like a normal chest. Each kid gets their own, nobody else can open it, and nothing young kids does (dying, losing Chester, falling in lava) can lose what is inside.

## Decisions

1. **The items live on the player.** A 27-slot inventory in a per-player data attachment (`artilian:chester`), copied on death, saved with the player. The Chester entity is only a door to it. Killing, unloading, or losing the entity cannot lose items because the entity never held any. Duplicating the entity cannot duplicate items for the same reason.
2. **Sleeping form is an entity.** (Closes the PLAN.md open question.) Sleeping Chester is the same entity with a synced `sleeping` flag: AI off, no movement, rendered with a vanilla-chest-shaped model. A real chest block could be broken, moved by pistons, or drained by hoppers, and would need a second copy of the ownership rules. One entity, one set of rules.
3. **One Chester per player.** The attachment records the current Chester's entity UUID. The whistle removes the old one before spawning a new one. A Chester whose UUID does not match its owner's record removes itself on its next tick.
4. **Chester cannot be hurt by players or mobs.** He is invulnerable to everything except the void and `/kill`. Those still happen, so the "Chester dies" case is real and tested.
5. **GeckoLib `4.9.3` is bundled with jarJar.** `deploy/` hands the kids one jar; a second jar to forget is a second way for the game not to start. GeckoLib is MIT licensed.
6. **Art spec: Alex's drawing of Chester's face.** Not in hand yet. Stages 2 to 5 ship a placeholder (a chest with eyes). Stage 6 applies the drawing if it has arrived; otherwise it goes to the Phase 1 art pass.

## Stages

### Stage 1: Storage
- **Changes:** GeckoLib `4.9.3` dependency (cloudsmith maven, jarJar) and its `neoforge.mods.toml` dependency block. `ArtilianAttachments` deferred register. `ChesterData`: 27-slot `ItemStackHandler`, `firstSpawnDone` flag, current Chester entity UUID; NBT serialization; `copyOnDeath`.
- **Tests, written first and seen failing:** `ChesterStorageGameTests`: (a) `survivesDeath`: items in the attachment are present on the respawned player; (b) `survivesSaveAndLoad`: serialize to NBT and back, all three fields intact; (c) `has27Slots`.
- **Red-proof:** remove `copyOnDeath()`: `survivesDeath` must go red. Make `serializeNBT` skip the inventory: `survivesSaveAndLoad` must go red.
- **Done When:** `runData`, `build`, `runGameTestServer` green, the red-proofs recorded here, and the built jar contains GeckoLib under `META-INF/jarjar/`.

### Stage 2: Entity & first login
- **Changes:** `ChesterEntity` (GeckoLib `GeoEntity`, owner UUID, follows owner, invulnerable per decision 4), placeholder geo/animation/texture, renderer, entity attributes, lang entry. On `PlayerLoggedInEvent`, if `firstSpawnDone` is false, spawn a Chester bound to the player and set the flag.
- **Tests, written first and seen failing:** first login spawns exactly one Chester owned by that player; second login spawns none; mob and player damage does nothing; `/kill` removes Chester and the owner's inventory is untouched.
- **Red-proof:** remove the `firstSpawnDone` check: "second login spawns none" goes red. Remove the invulnerability override: the damage test goes red.
- **Done When:** checks green, and `runClient` shows Chester spawning beside a new player and following them.

### Stage 3: Whistle
- **Changes:** `chester_whistle` item (given on first login, craftable, kept on death), recipe and model via datagen. Use: remove the recorded Chester if loaded, spawn a new one at the player, record its UUID. Orphan rule from decision 3. Short cooldown and a sound.
- **Tests, written first and seen failing:** whistle with no Chester spawns one; whistle with a Chester elsewhere leaves exactly one; an orphaned Chester removes itself; inventory identical before and after.
- **Red-proof:** remove the orphan self-removal: the single-Chester test goes red.
- **Done When:** checks green, and in `runClient` the whistle brings Chester back from anywhere, including another dimension.

### Stage 4: Menu & ownership gate
- **Changes:** right-click opens a 27-slot chest screen (vanilla `ChestMenu` layout, `#C6C6C6` frame per `DESIGN.md`) backed by the owner's attachment. Server-side owner check on interact and again in `stillValid`. Non-owner gets a friendly action-bar message.
- **Tests, written first and seen failing:** owner opens; non-owner is refused; menu closes when Chester is removed or the owner moves away.
- **Red-proof:** see the security gate.
- **Done When:** checks green and the security probes are HELD.

### Stage 5: Sleeping form
- **Changes:** synced `sleeping` flag. `Shift + Right-Click` by the owner toggles Sit & Sleep. Owner sleeping in a bed puts Chester to sleep; owner waking wakes him unless he was told to Sit & Sleep. Sleeping Chester does not move, does not follow, and renders as a normal chest. Still opens for the owner, still refuses everyone else. State-change sound and particles per `PRODUCT.md`.
- **Tests, written first and seen failing:** shift-use by owner toggles; shift-use by non-owner does nothing; bed sleep sets it and waking clears it; Sit & Sleep survives a bed wake-up; sleeping Chester does not move over 100 ticks.
- **Red-proof:** remove the owner check on the toggle: the non-owner test goes red.
- **Done When:** checks green, and in `runClient` sleeping Chester is hard to tell from a chest at a glance.

### Stage 6: Placeholder art, LAN playtest, jar
- **Changes:** tidy the placeholder model and animations (idle blink, chomp on open, bouncy walk, snap-flat on sleep). Apply Alex's drawing if it is in hand. Update `deploy/README.md`, `README.md`, and PLAN.md.
- **Done When:** on a LAN world Alex and Steve each get their own Chester, cannot open the other's, and lose nothing when Chester or the player dies. The PLAN.md Stage 2 Done When, checked by the owner.

## Security gate

Applies: Chester is player inventory ownership. Probes are GameTests in `src/main/java/com/silkpanda/artilian/gametest/security/`, indexed in `security/chester/README.md`, logged in `security/FINDINGS.md`. Each is shown EXPLOITED with the control off, then HELD with it on.

| ID | Probe | Control switched off for red-proof |
|---|---|---|
| P1-01 | Non-owner right-clicks another player's Chester and gets a menu | owner check on interact |
| P1-02 | Non-owner keeps a menu open after ownership is checked (forged or stale container) | owner check in `stillValid` |
| P1-03 | Owner dies; items are gone or dropped where others can take them | `copyOnDeath` (lands in Stage 1) |
| P1-04 | Whistle while the menu is open, or two Chesters at once, yields two copies of an item | single backing inventory on the player |
| P1-05 | Non-owner toggles sleep, or opens a sleeping Chester | owner check on the sleep toggle and sleeping interact |
| P1-06 | Hopper, hopper minecart, or item capability pulls items out of Chester | no item-handler capability exposed on the entity |

## Out of scope

- Chester fighting, carrying the player, or auto-collecting drops.
- More than 27 slots, upgrades, or skins.
- Giving another player access to your Chester. Sharing is a later request if the kids ask.
- Final art. Placeholder ships; the drawing lands in Stage 6 or the Phase 1 art pass.
