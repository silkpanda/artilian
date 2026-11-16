# Adventures in Artilian — Phase 1 Art Pass Plan (2026-11-15)

<!-- PLAN.md Phase 1 Stage 6. One PLAN stage, one PR, one jar. The numbered stages below are
commits inside that PR. -->

> **Status: Complete (all 4 stages done; ready for the owner, Alex, and Steve's in-game sign-off).** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Chester & Whistle art overhaul | `4118d2a` — `make_chester_art.py` (rich oak plank shading, beveled iron latch, Sunsteel-gold feet, teal-glinted dark-rimmed eyes, pink tongue, ivory teeth, and diagonal Sunsteel & Starlight Whistle), `chester.geo.json` (`awake_extras` & `teeth_upper` bones), `chester.animation.json` (lively idle double-blink/breathe, bouncy waddle walk, joyful double-chomp, flat sleep), `ChesterModel` hides awake-only bones when asleep; `Phase1ArtGameTests` red-proven (`chestergeckolibassetshaverequiredbonesandanimations` failed before adding `awake_extras`) then green (`48/48` in `runGameTestServer`). |
> | 2 Custom TNTs, Wands & Mana HUD polish | `3f753e3` — `make_tnt_art.py` (slime-spring & upward launch arrow `bounce_tnt`, high-contrast twilight-indigo & ice-white snowflake `frost_nova_tnt`, sharpened `party_tnt` & `cluster_quarry_tnt`), `make_wand_art.py` (centered dark-outlined `spark_wand`, `heart_bloom_staff`, and winged `zephyr_dash_wand`), `ManaHudOverlay` (Sunsteel gold corner rivets & bottom inner bevel), `48/48` `runGameTestServer` green. |
> | 3 Sunsteel & Starlight block/icon polish | `73d38d4` — `make_ores_and_gear_art.py` (`make_sunsteel_storage_block` forged Sunsteel Paladin 8-rayed sun crest with Ember Crimson accents, and fuller Archon dome `starlight_helmet` icon matching `sunsteel_helmet` proportions), `runData` (clean `src/generated`), `48/48` `runGameTestServer` green. |
> | 4 Client verification, evidence, docs & PR | Done — `./gradlew runClient` showcase verified sleeping & awake Chester beside a vanilla oak chest, chomping Chester with tongue & teeth, upgraded ores/gear/armor, all 4 Custom TNTs + Starlight Dynamite, Spark/Heart-Bloom/Zephyr Wands + Mana HUD bar, and Creative Tab with zero `artilian` missing-texture/model errors in `run/logs/latest.log`; 6 screenshots saved in `qa/evidence/*artpass-2026-11-15.png`; temporary `ShowcaseHarness.java` deleted; `PLAN.md` **Now** updated; `runData` clean, `build` and `48/48` `runGameTestServer` green. |

## Why

Phase 1 Stages 2–5 shipped playable mechanics with placeholder programmer art (flat oak panels on Chester, simple geometric bands on Bounce/Frost Nova TNT, corner-clipped or blocky wand sprites, and a plain Sunsteel block). Before Alex, Steve, and the owner do their Phase 1 co-op sign-off playtest (`PLAN.md` Stage 6), every Phase 1 creature, block, item, armor piece, and HUD element needs cohesive, expressive, high-contrast `DESIGN.md` art and zero missing-asset issues in-game.

## Decisions

1. **All assets remain 100% original and script-reproducible.** Textures are generated deterministically by `tools/art/make_*.py` (Python + Pillow) and GeckoLib `.geo.json` / `.animation.json` files are hand-authored in `src/main/resources/assets/artilian/`.
2. **Sleeping Chester stays indistinguishable at a glance from a placed chest.** Richer oak-plank grain, warm inner bevels, shaded recessed latch with dark outline, and hiding all awake-only bones (`eyes`, `feet`, `awake_extras`) in `ChesterModel` ensure sleeping Chester blends in beside vanilla oak chests while awake Chester bursts with cartoony personality (sparkling dark-rimmed eyes, tiny ivory chest teeth & pink tongue when chomping, Sunsteel-gold feet, lively idle blink/breathe, bouncy waddle walk).
3. **High-contrast silhouettes for ages 5 and 7 (`DESIGN.md`).**
   - `bounce_tnt`: unmistakable slime-spring coil & bright upward launch chevron instead of an ambiguous letter-like band.
   - `frost_nova_tnt`: deep twilight-indigo band (`#2B2338` / `#3D2C5E`) behind an ice-white & `#7DF9FF` 8-rayed snowflake crystal.
   - `party_tnt` & `cluster_quarry_tnt`: sharpened confetti/star & 4-bomblet hazard emblems.
   - Wands & Staves: crisp dark-outlined 16x16 silhouettes centered inside the frame (`Spark Wand` 4-pointed starlight crystal, `Heart-Bloom Staff` centered rose heart blossom & emerald leaves on twisted oak, `Zephyr Dash Wand` sculpted wind wing cradling a `#9C5CFF` Mana Violet gem).
   - `ManaHudOverlay`: subtle Sunsteel-gold corner rivets and inner bevel depth on the `81x11` `#47B8FF` → `#9C5CFF` bar without adding visual clutter.
   - `sunsteel_block` & `starlight_helmet`: forged 8-rayed Sunsteel Paladin block crest matching the visual richness of `starlight_crystal_block`, and a clearer horned/crowned `starlight_helmet` 16x16 item icon.
4. **No gameplay or security rules change.** All 46 existing GameTests and all 10 security probes (`P1-01`–`P1-10`) remain green. We also add a lightweight resource-integrity GameTest checking that every Phase 1 GeckoLib model, animation, and texture referenced by Chester and Sunsteel/Starlight armor is present and valid JSON/resources.

## Stages

### Stage 1: Chester & Whistle art overhaul
- **Changes:** `tools/art/make_chester_art.py`, `textures/entity/chester.png`, `textures/item/chester_whistle.png`, `geo/entity/chester.geo.json`, `animations/entity/chester.animation.json`, `client/ChesterModel.java`, plus a resource integrity GameTest in `ChesterEntityGameTests` (or `Phase1ArtGameTests`) verifying all Phase 1 GeckoLib JSON assets parse cleanly and expected bones exist.
- **Tests, written first and seen failing:** `Phase1ArtGameTests#allPhase1GeckoLibAssetsAndTexturesExist` checking `chester.geo.json`, `chester.animation.json`, `sunsteel_armor.geo.json`, `starlight_armor.geo.json`, and required `awake_extras`/`eyes`/`feet` bones.
- **Red-proof:** temporarily rename `awake_extras` in `chester.geo.json` -> test fails -> restore -> green.
- **Done When:** `./gradlew build` and `runGameTestServer` pass with Chester's upgraded model, animations, oak-plank texture, and whistle sprite.

### Stage 2: Custom TNTs, Wands & Mana HUD polish
- **Changes:** `tools/art/make_tnt_art.py` (all 4 TNT variants' `_side`, `_top`, `_bottom`), `tools/art/make_wand_art.py` (`spark_wand`, `heart_bloom_staff`, `zephyr_dash_wand`), and `client/ManaHudOverlay.java` (arcane frame bevel & corner rivets).
- **Tests / checks:** `Phase1ArtGameTests` verifies all TNT block textures and wand item textures exist on the mod classpath; `./gradlew build` and `runGameTestServer` pass.
- **Done When:** all 4 TNTs, 3 Wands/Staves, and the Mana HUD bar render cleanly with upgraded art and tests stay green.

### Stage 3: Sunsteel & Starlight block/icon polish
- **Changes:** `tools/art/make_ores_and_gear_art.py` (`sunsteel_block.png`, `starlight_helmet.png`, and cohesive highlights on `sunsteel_ingot.png`, `sunsteel_sword.png`, `starlight_sword.png`), re-run art script, check all 18 gear items + 6 ore/storage blocks.
- **Tests / checks:** `./gradlew runData`, `./gradlew build`, and `runGameTestServer` pass.
- **Done When:** Sunsteel Block and Starlight Helmet icon match the quality of the rest of the Stage 3 ores & 3D armor.

### Stage 4: Client verification, evidence, docs & PR
- **Changes:** Run temporary `./gradlew runClient` showcase verifying awake & sleeping Chester beside a vanilla oak chest, all 4 Custom TNTs, all 3 Wands & Staves + Mana HUD bar, Sunsteel/Starlight ores, blocks, weapons, tools, 3D GeckoLib armor, and the full `Adventures in Artilian` Creative Tab with zero missing-texture/model errors in `run/logs/latest.log`. Save updated screenshots in `qa/evidence/`, revert temporary harness, update this plan and `PLAN.md` **Now**, run `runData`/`build`/`runGameTestServer`, push `phase-1-stage-6-art-pass`, and open PR stacked on `phase-1-stage-5-mana-and-wands`.
- **Done When:** `runData` leaves `src/generated` clean, `build` and `runGameTestServer` are green, client showcase evidence is saved, and the stacked PR is open for the owner, Alex, and Steve's in-game sign-off.

## Security gate

n/a: art pass only (textures, GeckoLib models/animations, HUD polish, and asset-integrity tests). No ownership, permissions, server commands, or `allowDestructiveCustomTnt` logic is modified; all existing `P1-01`–`P1-10` probes remain green.

## Out of scope

- Any kids' drawing-specific revisions that haven't been shared yet (captured as follow-ups once Alex and Steve review the Phase 1 Stage 6 jar in-game).
- Phase 2 TerraBlender biomes, pets, structures, or enemies.
