# Adventures in Artilian — Boss Framework Plan (2026-11-17)

Branch: `phase-3-stage-1-boss-framework` (from `main` at `9a1655b`).
Relates to: `PLAN.md` Phase 3 Stage 1, `PRODUCT.md` (hard constraint: telegraphed boss attacks), `DESIGN.md` (Clear Telegraphs, Boss / Danger Telegraph `#FF4D5A`, clean boss bars), `enemy/StructureEnemyEntity.java` (the pattern this extends), `safety/KidSafeTargets.java`.

> **Status: Not started.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Boss base, boss bar, arena safety | |
> | 2 Telegraphed attacks | |
> | 3 Boss Altar and rematches | |
> | 4 Client verification, live evidence, PR | |

## Why

Steve wants custom bosses, and Phase 3 ships two of them (*The Crystal Colossus*, *Captain Brimstone*). Both, and every boss after them in Phases 4 and 5, need the same plumbing: a boss bar, attacks young kids can see coming, an arena they can't wreck, and a way to fight the boss again. Building that once, before the first boss, means each boss stage is only that boss's attacks, model, and loot.

`PRODUCT.md` makes it a hard constraint: every phase stays kid-accessible, with telegraphed boss attacks and clear audio/visual cues. `DESIGN.md` makes it specific: every boss AoE uses ground rings, glowing particles, and rising audio pitch at least 1.5 seconds before impact.

## Decisions

1. **Base class.** `boss/ArtilianBossEntity` extends `Monster`, implements `GeoEntity` and `ElementalMob`, same shape as `StructureEnemyEntity`. Subclasses supply elements, attributes, attacks and animations.
2. **Test boss.** The framework needs a concrete boss for GameTests before Stage 2 exists. `artilian:training_golem` is registered but kept out of the creative tab, has no spawn egg, never spawns naturally, and drops nothing. It uses a placeholder cube model. If the kids want a practice boss later, adding it to the creative tab and giving it an altar item is a small change.
3. **The code enforces the telegraph floor.** `TelegraphedAttack` refuses a windup under 30 ticks (1.5 s) at construction. A boss can't ship a faster attack by accident.
4. **Bosses never break blocks.** Same rule as structure enemies and the TNT default: boss AoE uses `Level.ExplosionInteraction.NONE` or direct damage, and `ArtilianBossEntity` ignores `mobGriefing`. Boss arenas in later stages are player-built or generated; either way they survive the fight.
5. **Pets fight bosses.** Bosses go in a new `artilian:bosses` entity tag, and the pet battle AI treats it like `artilian:structure_enemies`. Non-lethal fainting is unchanged: a pet knocked out by a boss faints into its crystal.
6. **Altars hold one boss at a time.** An altar remembers the UUID of the boss it summoned and won't summon again while that boss is alive and loaded. This keeps a LAN world from filling with bosses because a kid held right-click.
7. **Altars are not craftable in this stage.** They come from creative and, in Stages 2 and 3 of Phase 3, from the bosses' arenas. A recipe is the owner's call later.

## Stages

### Stage 1: Boss base, boss bar, arena safety
- **Changes:** `boss/ArtilianBossEntity.java`: a `ServerBossEvent` (name = the boss's display name, color from the boss's primary element, `NOTCHED_10` overlay), players added when within 48 blocks and line of sight not required, removed when they leave range, die, log out, or the boss dies or is removed; progress tracks `health / maxHealth`. `setPersistenceRequired()` on spawn; `removeWhenFarAway` returns false; knockback resistance 0.8. `boss/TrainingGolemEntity.java` (Earth, 120 health, 6 attack, melee only in this stage). Registry entry, attributes, entity tag `artilian:bosses` (datagen), pet battle AI target filter updated, placeholder GeckoLib model/texture/animations (`animation.training_golem.idle|walk|attack|telegraph`), renderer. Language keys.
- **Tests, written first and seen failing:** `BossFrameworkGameTests`:
  - `bossBarAddsNearbyPlayersAndDropsFarOnes`: a mock player at 10 blocks is on the bar, one at 60 is not; moving the near one to 60 removes them.
  - `bossBarTracksHealthAndClearsOnDeath`: damage to 50% shows progress 0.5; killing the boss leaves the bar with no players.
  - `bossNeverDespawns`: `removeWhenFarAway` false and persistence set after `finalizeSpawn`.
  - `bossIsInBossTagAndPetsTargetIt`: the golem is in `artilian:bosses`; a summoned pet next to it picks it as a target.
- **Red-proof:** skip the range check (add every player) → `bossBarAddsNearbyPlayersAndDropsFarOnes` red; make `removeWhenFarAway` return true → `bossNeverDespawns` red; drop the tag from the pet target filter → `bossIsInBossTagAndPetsTargetIt` red. Restore each.
- **Done When:** `./gradlew runData`, `./gradlew build`, `./gradlew runGameTestServer` pass 100%.

### Stage 2: Telegraphed attacks
- **Changes:** `boss/TelegraphedAttack.java`: windup ticks (constructor throws `IllegalArgumentException` under 30), radius, damage, element, and an `onImpact` hook. While winding up, the boss shows a ground ring of `DustParticleOptions` in `#FF4D5A` at the radius every 2 ticks, plays `NOTE_BLOCK_BELL` at three pitches climbing from 0.8 to 1.6 (at windup start, one-third, two-thirds), and plays its `telegraph` animation. On impact: damage to every `LivingEntity` in the radius except the boss and other bosses, elemental multiplier applied through `PetElement`, never `Level.explode` with block interaction. `ArtilianBossEntity` gets an attack scheduler: one telegraphed attack at a time, a cooldown between attacks (default 60 ticks), and the boss stops moving during windup so the ring stays put. The Training Golem gets one attack: *Ground Slam*, 40-tick windup, radius 4, 8 damage.
- **Tests, written first and seen failing:**
  - `telegraphRefusesWindupUnderOneAndAHalfSeconds`: constructing a 29-tick attack throws; 30 does not.
  - `slamDealsNoDamageBeforeWindupEnds`: a mock player in radius has full health at tick 39 and less at tick 41.
  - `slamMissesPlayerWhoStepsOut`: a player moved outside the radius at tick 30 takes no damage at impact.
  - `slamNeverBreaksBlocks`: stone, glass, and a chest around the golem are all still there after impact, with `mobGriefing` true.
  - `bossHoldsStillDuringWindup`: the golem's position doesn't change across the windup.
- **Red-proof:** remove the windup floor check → `telegraphRefusesWindupUnderOneAndAHalfSeconds` red; apply damage at windup start → `slamDealsNoDamageBeforeWindupEnds` and `slamMissesPlayerWhoStepsOut` red; switch impact to `Level.explode` with `ExplosionInteraction.MOB` → `slamNeverBreaksBlocks` red. Restore each.
- **Done When:** all three Gradle tasks pass 100%.

### Stage 3: Boss Altar and rematches
- **Changes:** `boss/BossAltarBlock.java` + `BossAltarBlockEntity` (stores the summoned boss UUID). `boss/BossSummonItem.java`: each boss names one summon item; the Training Golem's is the *Golem Core* (not in the creative tab; tests only, like the golem). Right-click the altar with the summon item: the item is consumed, a 60-tick countdown runs with the same red ring and climbing pitch as a telegraph, then the boss appears 2 blocks above the altar. Right-click while the altar's boss is alive: nothing is consumed, the player gets an action-bar message (`message.artilian.boss_altar.busy`). When the boss dies or is removed, the altar is free again (rematch). Breaking the altar doesn't despawn its boss. Block, item, block entity registration, loot (altar drops itself), block model and placeholder texture via datagen, language keys.
- **Tests, written first and seen failing:**
  - `altarSummonsBossAfterCountdown`: no boss at tick 59, one boss at tick 61, item stack shrank by 1.
  - `altarRefusesSecondSummonWhileBossAlive`: a second use while the boss lives leaves one boss and the second item unconsumed.
  - `altarAllowsRematchAfterBossDies`: kill the boss, use again, a new boss appears.
  - `altarIgnoresWrongItem`: right-click with a stick summons nothing and consumes nothing.
- **Red-proof:** skip the alive-boss check → `altarRefusesSecondSummonWhileBossAlive` red; consume the item before the busy check → the same test red on the item count. Restore each.
- **Done When:** all three Gradle tasks pass 100%, and the security gate below is HELD.

### Stage 4: Client verification, live evidence, PR
- **Changes:** `./gradlew runClient`: place an altar, summon the Training Golem with `/give` of the Golem Core, watch the boss bar appear and drain, stand in a Ground Slam ring and step out of one, check the ring color reads as danger red and the bell climbs, check no missing-model/texture warnings in `run/logs/latest.log`. Save `qa/evidence/boss-framework-2026-11-17.png` mid-telegraph. Update `PLAN.md` Now and open the PR.
- **Done When:** CI green, evidence saved, PR open.

## Security gate

The altar is a world-protection control: without the one-boss rule, any player on a LAN world can stack bosses until the server stalls. Probe in `security/bosses/`:

- **P3-01 — Altar spam summons many bosses.** `BossSecurityProbes.p3_01_altarSpamSummonsOneBoss`: one player uses the altar 20 times in 20 ticks holding 20 Golem Cores. EXPLOITED with the alive-boss check switched off (more than one boss); HELD with it on (one boss, 19 cores left). Logged in `security/FINDINGS.md`.

## Out of scope

- The Crystal Colossus and Captain Brimstone (Phase 3 Stages 2 and 3), their arenas, attacks, models and loot.
- An altar recipe, or altars in world generation.
- Boss music.
- Difficulty scaling by player count.
- Keep-inventory or death rules during boss fights.
- Making the Training Golem a player-facing practice boss (owner's call later).
