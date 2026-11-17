# Adventures in Artilian — Flurrywing Snow Fairy Pet Plan (2026-11-17)

Branch: `phase-2-flurrywing-pet`, stacked on `phase-2-prism-pet` (`85b09b3`) because both add a pet to the same registry, lang, art-script and GameTest files. Rebase onto `main` once Prismling's PR merges.
Relates to: `PLAN.md` Phase 2, `PRODUCT.md`, `DESIGN.md`, `qa/Adventures in Artilian - Prismling Legendary Pet Plan 2026-11-17.md` and `qa/Adventures in Artilian - Dragon and Eye Pets Plan 2026-11-16.md` (the patterns this follows).

> **Status: Not started.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Flurrywing entity, hover, spawns, GeckoLib model/texture/animations & datagen | |
> | 2 GameTests, red-proofs, capture/faint verification | |
> | 3 Client verification, live evidence, PR | |

## Why

This is the second kid-drawn creature the Prismling plan left out of scope. The kids drew a snowman with wings. The owner wants it **2.5 to 3 blocks tall** and **Ice**.

**Name:** `Flurrywing` (`artilian:flurrywing`) is a placeholder display name. The kids pick the real name; only the `en_us` display name changes when they do. The registry id stays `flurrywing` so captured crystals keep working.

### Owner decisions (2026-11-17)

- The two big rounded shapes under the body are **lower wings**. It is a four-winged snow-fairy snowman with no legs. It hovers.
- **Regular pet.** Normal spawn weight and roster stats; it is just tall. Prismling stays the only legendary.
- Elements: **Ice / Air** (`ICE` primary, `AIR` secondary).

### Art spec (from the drawing)

Source: `qa/reference/pets/flurrywing-drawing-2026-11-17.jpg` (photo, already upright). Drawn in one turquoise marker, so the outline color is the drawing's; fills are ours.

Upright, top to bottom:
- **Hat:** a small rounded cap/beanie on top of the head.
- **Head:** the smallest of three stacked snowballs, with two dot eyes and a small mouth.
- **Middle ball:** medium snowball with a dashed vertical line down its center: a row of buttons (2 buttons).
- **Bottom ball:** the largest snowball, about twice the middle's width, also with a dashed button row down the center (3 buttons). It does not touch the ground; it floats.
- **Upper wings:** two large wings rising from behind the middle ball on either side, reaching above the hat. The left one is tall and pointed (a teardrop pointing up), the right one broader and rounded. The model can keep that asymmetry or mirror the rounded one; Stage 3 asks the kids.
- **Lower wings:** two large rounded-triangle wings fanning down and out from under the bottom ball, each about as wide as the bottom ball. Butterfly layout: two up, two down.
- **Arms:** on the bottom ball's sides. Left arm is a bent stick (out, then down, like an L). Right arm ends in a triangular mitten/flipper.

Model translation (blocky, matching the roster):
- Three stacked boxes for the snowballs (head < middle < bottom), snow-white with a pale ice-blue shade side, turquoise `#2DD4BF`-ish outline pixels to echo the marker. Coal-black button pixels down the front center (2 on the middle, 3 on the bottom). Coal eyes, small mouth.
- Hat: small turquoise box on the head.
- Wings: four thin flat planes, translucent-looking frost pattern (pale cyan `#7DF9FF` from `ICE` with white veins), upper pair hinged at the middle ball's back, lower pair hinged under the bottom ball.
- Arms: a brown stick (left) and an ice-blue mitten (right), each a small box chain.

### Size

The owner asked for 2.5 to 3 blocks tall. Target **about 2.75 blocks to the top of the hat**, hitbox **1.3 x 2.75** (Prismling 1.6 x 2.0, Peepkin 0.72 x 0.85). Wingspan reads about 3 blocks. Render scale is whatever gets the model there (Prismling uses 2x); the hitbox is the tested number. It is the tallest pet in the game while being a regular pet, which is fine: the owner asked for height, and its stats stay at roster level.

### Rules

- **Elements:** `ICE` primary, `AIR` secondary. No changes to matchup tables.
- **Hover:** it has no legs. Same model as Peepkin: `causeFallDamage` returns `false`, and in `aiStep` downward motion is damped (`y * 0.65`) when not on the ground and not sitting. Idle and walk animations bob the whole body a little above the ground and flap the wings; sit animation folds the wings and settles the bottom ball onto the ground.
- **Ambient particles:** `ParticleTypes.SNOWFLAKE` drifting down from the wings, about one every 6 ticks on the client.
- **Spawning:** `Starlight Highlands` only (the high, cold-feeling biome), on turf, `Starlight Slate` or `Starlight Turf`, spawn weight **4**, group size 1–2. Regular-pet range is 3–6.
- **Stats:** regular roster: 22 health, 4.5 attack, speed 0.28 (a touch slower than Peepkin's 0.32; it is big and floaty).
- **Sounds:** happy `SoundEvents.AMETHYST_BLOCK_CHIME`; ambient `SoundEvents.SNOW_GOLEM_AMBIENT`; hurt `SoundEvents.SNOW_GOLEM_HURT`.

## Stages

### Stage 1: Flurrywing entity, hover, spawns, GeckoLib model/texture/animations & datagen
- **Changes:**
  - `FlurrywingEntity` extends `ArtilianPetEntity` (pattern: `PeepkinEntity`): `ICE` / `AIR`, attributes above, `checkFlurrywingSpawnRules`, hover in `aiStep`, `causeFallDamage` → `false`, snowflake particles, sounds, GeckoLib controllers (`idle`, `walk`, `sit`, `attack`).
  - Registered in `ArtilianEntities` (`FLURRYWING`, sized 1.3 x 2.75), attributes and spawn placement in `PetEvents`, spawn egg in `ArtilianItems` + creative tab, biome spawn in `ArtilianBiomes` (Starlight Highlands, weight 4, group 1–2).
  - `FlurrywingModel` / `FlurrywingRenderer` wired in `ArtilianClient`; `flurrywing.geo.json`, `flurrywing.animation.json`.
  - `make_flurrywing()` in `tools/art/make_pets_art.py` generating `flurrywing.png`. After running the script, `git checkout` `crystal_fox.png` (see `PLAN.md` loose ends).
  - `ArtilianLanguageProvider` (`Flurrywing`, `Flurrywing Spawn Egg`), `ArtilianItemModelProvider` spawn egg, `./gradlew runData`. Commit only generated files with a real diff (`git diff --ignore-cr-at-eol`).
- **Tests, written first and seen failing:** written in Stage 2.
- **Red-proof:** Stage 2.
- **Done When:** `./gradlew runData` and `./gradlew build` are clean.

### Stage 2: GameTests, red-proofs, capture/faint verification
- **Changes:** `PetRosterGameTests` gains:
  - `flurrywingIsIceAirTallAndSpawnsInStarlightHighlands`: elements are `ICE`/`AIR`; hitbox height ≥ 2.5 and ≤ 3.0; spawn rule accepts Starlight Slate and Starlight Turf; Highlands spawn list has it and the other two biomes don't.
  - `flurrywingHoversAndTakesNoFallDamage`: dropped from 10 blocks, it lands at full health.
  - `flurrywingCapturesBefriendsSummonsAndFaintsSafelyIntoCrystal`: same shape as the Prismling capture test.
  - `flurrywingBattlesWithIceMatchups`: its hit on a `NATURE` target uses 1.5x; a `FIRE` attacker hitting it uses 1.5x (Ice is weak to Fire).
- **Red-proof:**
  - `FlurrywingEntity#causeFallDamage` → `super.causeFallDamage(...)` → `flurrywingHoversAndTakesNoFallDamage` turns red → restore.
  - Entity size in `ArtilianEntities` → `0.9 x 1.8` → `flurrywingIsIceAirTallAndSpawnsInStarlightHighlands` turns red → restore.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` pass 100% (Prismling's branch is at `105/105`; this should land at `109/109`).

### Stage 3: Client verification, live evidence, PR
- **Changes:** `./gradlew runClient`, spawn Flurrywing hovering, moving, sitting and attacking; check no missing-model/texture warnings in `run/logs/latest.log`; confirm it stands about 2.75 blocks next to a 2-block pillar and that the four wings read as butterfly wings from the front. Save `qa/evidence/flurrywing-2026-11-17.png`. Show the kids; if they want the upper wings matched (both pointed or both round), change the geo and re-shoot. Update `PLAN.md` Now and open the PR (stacked on `phase-2-prism-pet` until that merges).
- **Done When:** CI green, evidence saved, PR open.

## Security gate

n/a for new controls. `FlurrywingEntity` extends `ArtilianPetEntity` and inherits `P2-01`–`P2-04` (crystal UUID ownership, non-lethal fainting, anti-duplication roster, friendly-target filtering). `PetSecurityProbes` must stay green.

## Out of scope

- The kids' official name for Flurrywing (placeholder until they pick).
- Real flight (pathing through the air). It hovers and floats down, like Peepkin.
- Melting in Emberbloom Meadows, snow trails, or any other snowman behavior nobody asked for.
- Legendary status or legendary capture rules.
- Any change to `PetElement` matchups.
