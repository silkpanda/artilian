# Plan: Adventures in Artilian

Written 2026-11-15, resized the same day. This is the one plan for the repository: where each phase stands, what is open, and what comes next. Each feature's stage-by-stage plan lives in `qa/` while it is being built and moves to `qa/archive/` when it is done.

---

## Now

*Updated 2026-11-17 by Claude Code. A session handoff, not history: every session rewrites this section before it ends (see AGENTS.md). Read it first.*

| | |
|---|---|
| **In progress** | **Prismling** (`qa/Adventures in Artilian - Prismling Legendary Pet Plan 2026-11-17.md`): Stages 1–3 done on `phase-2-prism-pet`, pushed, PR open and waiting on CI. Stage 3 reworked the art into a 4.1-block glowing guardian (hitbox 2.8 x 4.1); the owner chose that size knowing it won't fit through doors or tunnels. `105/105` GameTests green. **Flurrywing** (the second kid-drawn creature, `phase-2-flurrywing-pet` in the `artilian-flurrywing` worktree): Stages 1–2 committed on top of Prismling's Stage 1–2 commit; untracked in-game screenshot and a scratch build brief (`qa/flurrywing-build-brief.md`, delete, don't commit) sit in that worktree. Its Blockbench source sits untracked in this worktree's `tools/art/models/` (the bbmodel MCP root); belongs to Flurrywing. |
| **Next** | 1. Merge the Prismling PR once CI is green. 2. Rebase `phase-2-flurrywing-pet` onto `main`, finish its Stage 3 (commit the evidence screenshot), push and open its PR. 3. Then the earlier queue: rest of the Phase 2 Stage 7 art pass, family LAN playtest, Phase 3 Stage 1 (*Boss framework*). |
| **Waiting on the owner** | 1. The kids' real names for **Prismling** and **Flurrywing** (placeholders; only the `en_us` display names change, the ids stay). 2. Merging the Prismling PR. 3. Steve's official names for **Shadow Wisp** and **Cinder Crag**, and the in-game review of **Zaphox**, **Galecorn**, **Ember Drake**, and **Peepkin**. 4. The LAN co-op playtest to check off Phase 1 Stage 6 and Phase 2. 5. Confirm the `PERSONAL_INFO_BLOCKLIST` GitHub Actions secret is set. |
| **Other work in flight** | Unmerged/superseded local or remote branches: `phase-2-stage-5-main`, `chore/2026-11-16-strip-identifiers`, `phase-1-stage-7-review-fixes`, `phase-2-stage-5-pet-roster`, `phase-2-stage-5-alice-pets`, `phase-2-stage-5-dragon-eye-pets`, `art/2026-11-15-crystal-fox-pass`, `art/2026-11-15-armor-pass`, `showcase/2026-11-15-curseforge`. PR #2 (`d9ee782`) dropped `qa/FINDINGS.md` Phase 1 review notes and the Phase 1 review-fix plan doc from `main`; restorable in a docs PR. |
| **Loose ends** | `runData` rewrites ~150 `src/generated/**` files with LF endings and no content change; commit only files with a real diff (`git diff --ignore-cr-at-eol`). `python tools/art/make_pets_art.py` regenerates `crystal_fox.png` over its hand-touched art-pass version; restore it with `git checkout` after running the script. |

---

## Where It Stands

Sizes are ballparks in evenings of work, corrected as stages finish.

| Phase | Description | Stages | Rough size | Status |
|---|---|---|---|---|
| 0 | Project prep: folder, Git repo, 5-doc stack, registers, GitHub repo & CI | n/a | n/a | ✅ Done |
| 1 | NeoForge 1.21.1 baseline, Chester, starter gear, custom TNTs, Wands & Staves | 6 | 4–6 weeks | ⏳ Next |
| 2 | Overworld biomes (TerraBlender), pet capture & battle, structures & enemies | 7 | 2–3 months | ⏳ In progress |
| 3 | Custom bosses, rematch altars, Tier 3 gear, advanced Staves/TNTs | 5 | 6–8 weeks | ⏹ Pending |
| 4 | Alex's Dimension: The Eye Realm | 7 | 2–3 months | ⏹ Pending |
| 5 | Steve's Dimension: Giant Dragon Jungle & finale | 10 | 3–4 months | ⏹ Pending |

---

## How Work Flows

1. **One stage, one PR, one jar.** Every stage is its own pull request and ends with a playable `.jar` the kids can use (see `deploy/README.md`). No stage waits on the rest of its phase.
2. **Test-gated stages:** every stage passes `./gradlew build`, `./gradlew runData`, and `./gradlew runGameTestServer` in CI before it merges. CI runs all three from Phase 1 Stage 1 onward.
3. **Branch and pull request:** every change reaches `main` through a pull request with CI green. Nothing is committed on `main` directly.
4. **Placeholder art is allowed.** A stage may ship with rough models and textures. Each phase ends with an art pass stage that replaces them.
5. **Audit boundaries:** respect `AGENTS.md` rules and constraints. Kid-safe defaults (non-lethal pet fainting, player-bound Chester storage, `allowDestructiveCustomTnt`) apply to every later stage that touches them, including boss and dragon TNTs.

---

## How Art Gets Made

Nobody on this project is an artist and there is no budget for commissions. The approach:

1. **The owner and the kids describe; the agent builds.** Each creature starts as a description from the owner or a drawing from Alex or Steve. The drawing or description is the art spec, recorded in the stage's `qa/` plan.
2. **Structure first.** Before modeling a kind of creature (walker, flyer, boss), the agent researches how established GeckoLib mods rig that kind: bone layout, animation set, texture sheet size. The research informs structure only.
3. **Original models and textures.** The agent writes the GeckoLib `.geo.json`, `.animation.json`, and textures itself, to the spec. Vanilla shapes and textures may be referenced as a starting point (Chester's sleeping form is the vanilla chest). Other mods' models and textures are never copied or rebuilt piece by piece.
4. **Open-licensed assets are fine** when the license allows reuse. Record the source and license in the stage plan.
5. **Art pass per phase.** The last stage of each phase revisits every placeholder against the kids' drawings. Blockbench (free) is the tool for any hand touch-up.

---

## Work Queue

### Phase 1: NeoForge Baseline, Chester & Starter Adventures

"Review model" is which Claude model specs and reviews the stage; OpenCode implements either way.

| Stage | What ships | Rough size | Review model |
|---|---|---|---|
| 1 | Scaffold | 1–2 evenings | Opus |
| 2 | Chester | ~2 weeks | Fable |
| 3 | Ores & starter gear | ~1 week | Opus |
| 4 | Custom TNTs | ~1 week | Opus |
| 5 | Mana & wands | ~1 week | Opus |
| 6 | Art pass | 3–5 evenings | Fable |

- [x] **Stage 1: Scaffold.** NeoForge `1.21.1` (`ModDevGradle`) project, `ArtilianMod.java`, registries, datagen wiring, one trivial GameTest. `.github/workflows/tests.yml` extended to run `runData` and `runGameTestServer` as well as `build`. GeckoLib and TerraBlender are *not* added here.
  - **Done When:** `./gradlew build`, `runData`, and `runGameTestServer` are green locally and in CI, and `./gradlew runClient` reaches the title screen with the mod listed.
- [x] **Stage 2: Chester.** Adds GeckoLib `4.9.3`. Per-player data attachment, first-login spawn, **Chester Whistle**, 27-slot player-bound inventory that survives death and entity loss, sleeping normal-chest form on both triggers (owner bed sleep, `Shift + Right-Click` Sit & Sleep). Security gate applies: ownership and item-duplication probes, red-proven then HELD, logged in `security/FINDINGS.md`.
  - **Done When:** GameTests pass, and on a LAN world Alex and Steve each get their own Chester, cannot open the other's, and lose nothing when Chester or the player dies.
- [x] **Stage 3: Ores & starter gear.** **Sunsteel** and **Starlight Crystal** ores and materials, worldgen for both, starter weapons, armor, and tools, recipes and loot via datagen.
  - **Done When:** both ores generate, and every starter item is craftable and usable in survival.
- [x] **Stage 4: Custom TNTs.** Party, Bounce, Frost Nova, Cluster Quarry. `allowDestructiveCustomTnt` config, default off. Security gate applies: block-damage probes red-proven then HELD.
  - **Done When:** GameTests prove no TNT breaks blocks with the config off, and all four behave as described in a playtest.
- [x] **Stage 5: Mana & wands.** Auto-regenerating mana attachment synced to a HUD bar, *Spark Wand*, *Heart-Bloom Staff*, *Zephyr Dash Wand*.
  - **Done When:** Mana GameTests pass, and both kids can cast all three in LAN co-op with the bar tracking correctly.
- [ ] **Stage 6: Art pass.** Replace Phase 1 placeholders against the kids' drawings and `DESIGN.md`.
  - **Done When:** The owner, Alex, and Steve sign off on Chester and the starter gear in-game.

### Phase 2: Wild Biomes, Dungeons & Pet Battles

- [x] **Stage 1: First biome.** Adds TerraBlender `4.1.0.8`. *Crystal Canopy Woods* only, to prove the worldgen pipeline.
- [x] **Stage 2: Remaining biomes.** *Emberbloom Meadows* and *Starlight Highlands*.
- [x] **Stage 3: Pet capture.** One pet end to end (**Crystal Fox**, `Crystal / Nature`) + expanded 11-element dual-element system (`PetElement`): **Capture Crystals**, **Biome Treats**, summon and recall, non-lethal fainting back into the crystal (red-proven GameTest + `P2-01`–`P2-03` probes).
- [x] **Stage 4: Pet battle AI.** Summoned pets fight alongside the player, defend/assist owners, apply 11-element damage multipliers + signature combat perks, and target hostiles & `artilian:structure_enemies` (`P2-04` red-proven HELD).
- [x] **Stage 5: Pet roster.** Biome-specific pets for all three biomes (**Crystal Fox**, **Shadow Wisp**, **Cinder Crag**, **Zaphox**, **Galecorn**), plus small dragon hatchling (**Ember Drake**) and eye-themed pet (**Peepkin**) so each kid meets their dimension's theme early.
- [x] **Stage 6: Structures & enemies.** Biome-specific structures (*Crystal Grove Shrine*, *Emberbloom Outpost*, *Starlight Watchtower*) and custom `artilian:structure_enemies` (*Prism Sentinel*, *Cinder Raider*, *Astral Stalker*).
- [ ] **Stage 7: Art pass.**
- **Done When:** all checks green and a co-op playtest verifies pet capture, battling, fainting, and structure exploration.

### Phase 3: Custom Bosses & High-Tier Arsenal

- [ ] **Stage 1: Boss framework.** `ArtilianBossEntity` (`ServerBossEvent`, telegraphed attacks per `DESIGN.md`) and Boss Altars for rematches.
- [ ] **Stage 2: The Crystal Colossus.**
- [ ] **Stage 3: Captain Brimstone.**
- [ ] **Stage 4: Tier 3 arsenal.** Boss-forged weapons, armor, tools, advanced Staves and TNTs (destructive ones honor `allowDestructiveCustomTnt`).
- [ ] **Stage 5: Art pass.**
- **Done When:** all checks green, both bosses are summonable, beatable by a young kid with a parent, and drop working Tier 3 gear.

### Phase 4: The Eye Realm (Alex's Dimension)

- [ ] **Stage 1: Dimension & portal.** `artilian:eye_realm` and the Gazing Portal, flat placeholder terrain.
- [ ] **Stage 2: Terrain & sky.** Eye-covered blocks, trees, terrain, blinking skybox.
- [ ] **Stage 3: Enemies.** **Peepers**, **Oculus Slimes**, **Lens Guardians**.
- [ ] **Stage 4: Blinky** pet.
- [ ] **Stage 5: Structures.**
- [ ] **Stage 6: The Great All-Seer** boss.
- [ ] **Stage 7: Art pass.**
- **Done When:** all checks green, dimension and portal GameTests pass, and the Eye Realm loop is playable end to end.

### Phase 5: Giant Dragon Jungle (Steve's Dimension) & Grand Finale

- [ ] **Stage 1: Dimension & portal.** `artilian:dragon_jungle` and the Primal Portal.
- [ ] **Stage 2: Mega-jungle worldgen.**
- [ ] **Stage 3: Structures.** Dragon Roost and Amber Temple.
- [ ] **Stage 4: First dragon species.** One wild dragon end to end; this sets the rig every later species reuses.
- [ ] **Stage 5: More dragon species.**
- [ ] **Stage 6: Hatchable pet dragons.**
- [ ] **Stage 7: Dragonscale gear & Dragonfire Mega TNT** (honors `allowDestructiveCustomTnt`).
- [ ] **Stage 8: Apex Dragon Boss.**
- [ ] **Stage 9: Twin-Star endgame rewards.**
- [ ] **Stage 10: Art pass.**
- **Done When:** all checks green, dimension and dragon GameTests pass, and both kids can clear the full Eye Realm and Dragon Jungle finale.

---

## Open & Parked

- **Exact pet & dragon count per biome:** expand after core rosters are tested and fun for Alex & Steve (the owner + kids).
- **Open-licensed creature assets:** not yet searched. Worth a look before Phase 3 if agent-built bosses look too rough.
