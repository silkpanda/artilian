# Adventures in Artilian — Phase 1 Review Fixes Plan (2026-11-15)

<!-- PLAN.md Phase 1 Stage 7. One PLAN stage, one PR, one jar. The numbered stages below are
commits inside that PR. -->

> **Status: Stages 1 and 2 complete.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 One kid-safe target rule for TNTs and wands | Branch `phase-1-stage-7-review-fixes`. New `safety/KidSafeTargets`; Cluster Quarry, Frost Nova, Party TNT and the Spark Wand all ask it. Eight new GameTests, all eight red with the old `ManaService.java` and `ArtilianPrimedTnt.java` restored (`8 required tests failed`, the 48 existing tests stayed green), then `56/56` green with the fix. `./gradlew build` green. No datagen change. |
> | 2 Art fixes from the same review | Sleeping Chester drawn with the game's own chest texture (`chester_sleep.geo.json`, `ChesterModel`); bigger wand heads; one 8x8 emblem per TNT side. `Phase1ArtGameTests.sleepingChesterModelIsAPlainChest` red with a bone renamed, then `57/57` green (`81/81` after rebasing onto `main`). `./gradlew build` green. Checked in `runClient`: screenshots saved as `qa/evidence/*artfix-2026-11-15.png`, no missing-texture or model errors in the log. Note: `QA-05` (3D armor plates) was dropped during rebase onto `main` because PR #15 (`art/2026-11-15-armor-pass`) already shipped the sealed 64x64 armor pass on `main`. |

## Why

A code review of Phase 1 (stages 1 to 6) found that each weapon decided for itself what it was
allowed to hurt, and the answers disagreed:

- Cluster Quarry TNT in its default kid-safe mode protected blocks, Players, and Chester, and
  nothing else. It still killed tamed pets and villagers and destroyed dropped items (`REVIEW-01`).
- Frost Nova TNT spared owned pets but froze villagers, baby animals, and golems (`REVIEW-02`).
- The Spark Wand hit whichever unprotected mob was closest, including a sheep in front of the
  zombie or a village iron golem, and reached through walls (`QA-01`).
- Party TNT handed Speed and Regeneration to monsters (`QA-02`).
- Frost Nova turned flowing water into permanent water sources (`QA-03`).

## Stages

### Stage 1: One kid-safe target rule for TNTs and wands
- **Changes:** new `safety/KidSafeTargets.java` (`isValidOffensiveTarget`, `isHostile`), holding
  the filter that used to live in `ManaService`, plus protection for iron and snow golems.
  `ManaService.isValidOffensiveTarget` delegates to it. `ArtilianPrimedTnt`: the quarry damage
  calculator, `explodeFrostNova`, and `explodeParty` ask it; Frost Nova freezes source water only.
  `ManaService.castSparkWand`: ray clipped at the first solid block, line of sight required for
  the primary and chain targets, monsters sorted ahead of animals.
- **Tests, written first and seen failing:** `ReviewFixProbes` (`review_01`, `review_01b`,
  `review_02`) and `ReviewFixGameTests` (`sparkWandPrefersMonsterOverCloserAnimal`,
  `sparkWandBlockedByWall`, `sparkWandSparesIronGolem`, `partyTntDoesNotBuffMonsters`,
  `frostNovaFreezesOnlySourceWater`).
- **Red-proof:** the two changed source files were put back to their `phase-1-stage-6-art-pass`
  versions with the new tests in place. All eight new tests turned red and no existing test did.
- **Done When:** `runGameTestServer` reports `56/56`, `./gradlew build` is green, and the findings
  are logged as FIXED in `security/FINDINGS.md` and `qa/FINDINGS.md`.

### Stage 2: Art fixes from the same review
- **Changes:** `geo/entity/chester_sleep.geo.json` and `client/ChesterModel.java` (asleep, Chester
  uses the vanilla chest texture through a second model with the same three boxes);
  `tools/art/make_wand_art.py` and `tools/art/make_tnt_art.py` (bigger shapes). (The original
  `QA-05` 3D armor rewrite was dropped when rebasing onto `main` in favor of PR #15's merged
  64x64 armor pass.)
- **Tests, written first and seen failing:** `Phase1ArtGameTests.sleepingChesterModelIsAPlainChest`.
- **Red-proof:** renamed the `lid` bone in the sleeping model; the test turned red; restored.
- **Done When:** in `runClient`, sleeping Chester is indistinguishable from a placed chest, and
  each wand and TNT has its own shape.

## Security gate

Applies: this changes world and pet protection for custom TNTs. Probes `REVIEW-01` and
`REVIEW-02` in `gametest/security/ReviewFixProbes.java`, indexed in `security/tnt/README.md`,
each shown EXPLOITED against the old rule and HELD with the fix. Recorded in
`security/FINDINGS.md`.

## Out of scope

- Starlight Crystal ore frequency (7 veins per chunk for diamond-grade gear). A balance call for
  the family playtest.
- Bounce TNT, Heart-Bloom Staff, Zephyr Dash Wand, Chester, and the config: no rule changed.
- The feel of the Spark Wand's new aim. Stage 1's rules are covered by GameTests and Stage 2's
  art was checked in `runClient`, but nobody has cast the wand at a mob by hand yet. That belongs
  in the LAN playtest.
- Chester's awake texture, walk, blink, and chomp, and the 2D armor item icons: unchanged.
- Kids' drawings: none have been shared yet, so nothing here was matched against one.
