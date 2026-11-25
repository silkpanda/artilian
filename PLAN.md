# Plan: Adventures in Artilian

Written 2026-11-15; consolidated 2026-11-18 into a single phased and staged plan. **This is the single authoritative plan for the repository**: every phase, stage, sub-stage status table, design decision, GameTest, red-proof, security gate, and verification evidence path lives here.

---

## Now

*Updated 2026-11-25 by Antigravity. A session handoff, not history: every session rewrites this section before it ends (see AGENTS.md). Read it first.*

| | |
|---|---|
| **In progress** | **Full Art Pass (Sections 1 & 2.1–2.4) COMPLETE — PR open (`art/pass-3-pets` → `main`).** Includes 5 v3 `256×256` 3D armor sets + 20 armor icons (`art/sunsteel-armor-redesign` / `art/pass-1-armor`), 11 hero weapons & staves + 39 tool/summon/trophy/material/utility icons (`art/pass-2-weapons-items`), 17 upgraded Section 2.4 entities (Chester, 12 Phase 2 pets, Prism Sentinel, Cinder Raider, Astral Stalker, Crystal Colossus), and the consolidated 89-preview gallery (`qa/art-pass-gallery.md`). Full `python tools/art/lint_art.py --strict` exits `0` across all 32 entities + 5 armors, and `./gradlew build runGameTestServer` is `229/229` green. |
| **Next** | Review and merge the Art Pass PR (`art/pass-3-pets` → `main`) once CI is green, and run `./gradlew runClient` visual check when home. |
| **Waiting on the owner** | 1. In-game look & screenshots when home for the 5 v3 armor sets, Section 2.2/2.3 icons, Section 2.4 upgraded pets (`chester`, `crystal_fox`, `shadow_wisp`, `cinder_crag`, `zaphox`, `galecorn`, `ember_drake`, `peepkin`, `prismling`, `flurrywing`, `sunbeam`, `frogduck`, `zapkin`), **Boss Framework**, **Tier 3 Arsenal**, **The Great All-Seer**, **Primal Portal / Dragon Jungle**, **Embercrown Sovereign**, **Twin-Star Endgame**, and family LAN co-op playtest across Phases 1–5 (`qa/evidence/`). 2. The kids' official names for **Prismling**, **Flurrywing**, **Frogduck**, and **Zapkin** (placeholders; **Sunbeam** is final; only `en_us` display names change). 3. Steve's official names for **Shadow Wisp** and **Cinder Crag**. 4. Confirm the `PERSONAL_INFO_BLOCKLIST` GitHub Actions secret is set. |
| **Other work in flight** | `art/sunsteel-armor-redesign` → `art/pass-1-armor` → `art/pass-2-weapons-items` → `art/pass-3-pets` (all rolled up into the `art/pass-3-pets` → `main` PR). |
| **Loose ends** | Stage 5 kid drawings (`steve-pets-drawing-2026-11-15.jpg`, `alex-pets-drawing-2026-11-15.jpg`) are filed in `qa/reference/pets/` alongside Stage 6 drawings — always inspect the kid's drawing before upgrading a pet; `tools/art/preview_geo.py` now applies Bedrock/GeckoLib `-rx/-ry/-rz` Euler rotations accurately. `runData` rewrites `src/generated/**` with LF endings and no content change; commit only files with a real diff (`git diff --ignore-cr-at-eol`). Pass `--crystal-fox` / `--shadow-wisp` / `--cinder-crag` / `--zaphox` / `--galecorn` / `--ember-drake` / `--peepkin` / `--prismling` / `--flurrywing` / `--sunbeam` / `--frogduck` / `--zapkin` to `tools/art/make_pets_art.py` to regenerate a single pet (`make_crystal_fox()`..`make_flurrywing()` and `make_frogduck()` target `128×64` at `256×128`; `make_sunbeam()` and `make_zapkin()` target `128×128` at `256×256`). |

---

## Where It Stands

Sizes are ballparks in evenings of work, corrected as stages finish.

| Phase | Description | Stages | Rough size | Status |
|---|---|---|---|---|
| 0 | Project prep: folder, Git repo, 5-doc stack, registers, GitHub repo & CI | n/a | n/a | ✅ Done |
| 1 | NeoForge `1.21.1` baseline, Chester, starter gear, custom TNTs, Wands & Staves | 6 | 4–6 weeks | ⏳ Waiting on LAN co-op sign-off |
| 2 | Overworld biomes (`TerraBlender`), pet capture & battle, kids' roster, structures & enemies | 9 | 2–3 months | ⏳ Art signed off; LAN co-op & official names pending |
| 3 | Custom bosses, rematch altars, Tier 3 gear, advanced Staves/TNTs | 5 | 6–8 weeks | ⏳ Stages 1–5 complete; awaiting PR merges & LAN co-op sign-off |
| 4 | Alex's Dimension: The Eye Realm | 7 | 2–3 months | ⏹ Pending |
| 5 | Steve's Dimension: Giant Dragon Jungle & Grand Finale | 10 | 3–4 months | ⏳ Stages 1–10 complete & locked in PR #17 (`0.2.0`); awaiting LAN co-op sign-off |

---

## How Work Flows

1. **One stage, one PR, one jar.** Every stage (or logical sub-stage) reaches `main` via its own pull request with a playable `.jar` the kids can use (see `deploy/README.md`). Nothing waits on the rest of its phase.
2. **Test-gated stages:** every PR passes `./gradlew build`, `./gradlew runData`, and `./gradlew runGameTestServer` locally and in CI (`.github/workflows/tests.yml`) before merge.
3. **Branch and pull request:** nothing is committed directly on `main`. Every change — code, assets, and `PLAN.md` updates — goes on a branch and reaches `main` by PR.
4. **Plan in-place inside `PLAN.md`:** each phase is broken into numbered stages here. When planning a new stage, expand its section directly in `PLAN.md` with:
   - **Status & Sub-stages table** (commits, test counts, and red-proofs)
   - **Why & Decisions**
   - **Tests & Red-proofs** (control switched off → test goes red → restored green)
   - **Security Gate** (`P*-*` probes logged in `security/FINDINGS.md`, or `n/a` with reason)
   - **Evidence & Done When**
5. **Audit boundaries & kid-safe defaults:** non-lethal pet fainting, player-bound Chester storage, `allowDestructiveCustomTnt` (default `false`), and boss telegraph minimums (`>= 30` ticks) apply across every later phase and stage.

---

## How Art Gets Made

Nobody on this project is an artist and there is no budget for commissions. The approach:

1. **The owner and the kids describe; the agent builds.** Each creature starts as a description from the owner or a drawing from Alex or Steve (`qa/reference/`). If no kid drawing exists (e.g., bosses), collect or write a short silhouette/reference spec in `qa/reference/` before modeling.
2. **Write an Art Brief before modeling.** Before writing `.geo.json` or texture code for a new creature, boss, or structure, record a 5-line brief in that stage's section: target height/scale, silhouette inspiration, dark structural anchor + accent palette, key 3D recessed/angled features, and `_glowmask.png` zones.
3. **Pass-1 quality, not throwaway placeholders.** Do not cram flagship entity/boss art into a giant Stage 1 gameplay + GameTest commit where it degrades into flat boxes (`paint_box` / single-color faces). Give art its own dedicated sub-stage (or build and inspect the model first) and meet the **3D Entity, Boss & Structure Art Standard** in `DESIGN.md` on pass 1.
4. **Original models and textures.** Agents author GeckoLib `.geo.json`, `.animation.json`, and deterministic Python Pillow generators (`tools/art/make_*.py`) or Blockbench sources (`tools/art/models/*.bbmodel`). Vanilla shapes/textures may be referenced (e.g., sleeping Chester matches the vanilla oak chest). Other mods' models/textures are never copied.
5. **Open-licensed assets are fine** when the license allows reuse; record source and license under the stage.
6. **Art pass per phase.** The final stage of each phase still polishes consistency and animation feel across the phase, but should no longer need full silhouette/texture redos.

---

## Phase 0: Project Prep

- **Status:** ✅ Complete
- **Deliverables:** Repository structure, Git initialization, core docs (`README.md`, `PRODUCT.md`, `PLAN.md`, `DESIGN.md`, `AGENTS.md`, `CLAUDE.md`), registers (`qa/FINDINGS.md`, `security/FINDINGS.md`, `TECH_DEBT.md`), family distribution guide (`deploy/README.md`), and GitHub repository + CI (`.github/workflows/tests.yml` with `Personal-info guard`).

---

## Phase 1: NeoForge Baseline, Chester & Starter Adventures

*Review model notes from initial roadmap: Stage 1/3/4/5 Opus, Stage 2/6 Fable; OpenCode/Claude/Antigravity implement against stage specs.*

| Stage | What ships | Rough size | Status |
|---|---|---|---|
| 1 | Scaffold & CI baseline | 1–2 evenings | ✅ Complete (`7d4fa9c`) |
| 2 | Chester companion & `P1-01`–`P1-06` | ~2 weeks | ✅ Code & client verified; LAN co-op sign-off pending |
| 3 | Sunsteel & Starlight ores, worldgen & starter gear | ~1 week | ✅ Complete |
| 4 | Custom TNTs & `P1-07`–`P1-10` | ~1 week | ✅ Complete |
| 5 | Auto-regenerating Mana, HUD bar & 3 Wands/Staves | ~1 week | ✅ Complete |
| 6 | Phase 1 Art Pass & family co-op sign-off | 3–5 evenings | ⏳ Art built & verified (`48/48`); awaiting owner/kids sign-off |

### Phase 1 — Stage 1: Scaffold
- [x] **Complete** — Commit `7d4fa9c`.
- **Why & Decisions:** Establish a buildable, datagen-wired, test-gated NeoForge `1.21.1` project (`artilian`, `com.silkpanda.artilian`) with zero extra library dependencies yet.
  - Gradle wrapper `9.2.1`, ModDevGradle `2.0.148`, NeoForge `21.1.266`, Parchment `2024.11.17`, Foojay JDK `21` toolchain.
  - `ArtilianMod`, deferred register wiring, datagen provider writing `data/artilian/structure/empty.nbt`, `.cache` excluded from jar in `build.gradle`.
  - `.github/workflows/tests.yml` runs `runData` (fails if `src/generated/` is dirty), `build`, `runGameTestServer`, and uploads `adventures-in-artilian-<version>.jar`.
- **Tests & Red-proofs:** `ScaffoldGameTests.modIsLoaded` (`1/1` green). Red-proof: switched assert to `helper.fail(...)` → exit 1 (`1 required tests failed`) → restored green.
- **Security Gate:** `n/a` (no ownership, permissions, commands, or world-protection logic).
- **Done When:** `./gradlew build`, `runData`, `runGameTestServer` green locally & in CI; `./gradlew runClient` reaches title screen with *Adventures in Artilian 0.1.0 (artilian)*.

### Phase 1 — Stage 2: Chester Companion
- [x] **Code & Client Complete (LAN co-op playtest pending)**
- **Why & Decisions:** Alex asked for a personal cartoony living chest friend named **Chester** who sleeps looking like a normal chest, never loses items, and is private per player.
  1. **Items live on the player (`artilian:chester` attachment, `copyOnDeath()`):** 27-slot `ItemStackHandler`, `firstSpawnDone` flag, and current Chester entity UUID live on the player; `ChesterEntity` is only a door to it (immune to item loss or entity-duplication exploits).
  2. **Sleeping form is an entity:** Same `ChesterEntity` with synced `ASLEEP` / `ToldToSleep` state; immobile, unpushable, snaps to `90°` yaw, hides awake-only bones (`eyes`, `feet`, `awake_extras`) to resemble a vanilla oak chest.
  3. **One Chester per player & invulnerability:** Whistle discards any loaded prior Chester and records new UUID; orphans self-discard on tick; invulnerable to everything except void and `/kill`.
  4. **GeckoLib `4.9.3` bundled via `jarJar`:** Kids still drop one `.jar` into `mods/`.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Storage | `8cadb48` | GeckoLib `4.9.3` `jarJar`, `ArtilianAttachments`, `ChesterData` (27 slots, NBT, `copyOnDeath()`), `ChesterStorageGameTests` (`4/4`). Red-proofs: removed `copyOnDeath()` → `survivesDeath` failed; skipped `Inventory` in `serializeNBT` → `survivesSaveAndLoad` failed. |
  | 2 Entity & first login | `2706ee4` | `ChesterEntity` (`FollowOwnerGoal`, invulnerability), `ChesterService.handleLogin`. Red-proofs: removed `firstSpawnDone` check → `secondLoginSpawnsNothing` failed; removed invulnerability → `nothingHurtsChester` failed. |
  | 3 Whistle | `82cdb5c` | `ChesterWhistleItem` (first login, kept out of death drops, returned on respawn, stick + gold nugget recipe, 40t cooldown). Red-proofs: removed orphan discard → `orphanedChesterRemovesItself` failed; skipped prior-Chester discard → `whistleLeavesExactlyOne` failed. |
  | 4 Menu & ownership gate | `65354e8` | `ChesterContainer` (`27`-slot `ChestMenu`, `MAX_REACH_SQR = 64`), server owner checks in `mobInteract`, `createMenu`, and `stillValid`; `ChesterSecurityProbes` (`P1-01`, `P1-02`, `P1-04`, `P1-06`). |
  | 5 Sleeping form | `2d8cade` | `Shift + Right-Click` Sit & Sleep toggle, owner bed sleep/wake (`applyOwnerBedState`), `holdStill` / `isImmobile` / `isPushable`, chest sounds & particles, `P1-05`. Red-proofs: removed owner check on toggle, `holdStill`, `toldToSleep` precedence, and NBT save. |
  | 6 Art, docs & jar | `e1248d9` | Matched wood panels in `tools/art/make_chester_art.py`, updated `deploy/README.md` & `README.md`, `25/25` green. |

- **Security Gate (`ChesterSecurityProbes`, `security/chester/README.md`, `security/FINDINGS.md`):**
  - `P1-01`: Non-owner interact refused (`owner check on interact`).
  - `P1-02`: Non-owner forged/stale container closed (`owner check in stillValid`).
  - `P1-03`: Owner death keeps items (`copyOnDeath`).
  - `P1-04`: Whistle/two-Chester anti-duplication (`single backing inventory on player`).
  - `P1-05`: Non-owner sleep toggle / sleeping interact refused (`owner check`).
  - `P1-06`: Hopper / minecart / item capability cannot pull items (`no IItemHandler capability exposed`).
- **Evidence & Done When:** `qa/evidence/chester-forms-2026-11-15.png`. Done when LAN co-op playtest confirms Alex & Steve each get private Chesters and lose nothing on death.

### Phase 1 — Stage 3: Ores & Starter Gear
- [x] **Complete** — Locked in PR #5 (`Phase 1 Stage 3`, archived from `qa/archive/`).
- **Why & Decisions:** Two-step survival progression in `DESIGN.md` colors before custom biomes/bosses:
  - **Sunsteel (`#FF9F38`):** Stone-pickaxe tier (`between Iron & Diamond`), faster/tougher than Iron; armor defense `17`, toughness `1.0`.
  - **Starlight Crystal (`#38D9C0`):** Iron-pickaxe tier (`Diamond`-grade, mines Obsidian); glowing ores (`lightLevel 10`) and storage block (`lightLevel 14`) for kid-friendly caving visibility; armor defense `20`, toughness `2.0`, knockback resistance `0.05`.
  - Creative tab `artilian:main` (*Adventures in Artilian*) groups Chester Whistle, ores, storage blocks, materials, tools, and GeckoLib 3D armor (`SunsteelArmorRenderer` / `StarlightArmorRenderer`).
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Ores, materials, loot & tiers | `9936aae` | 6 blocks (`sunsteel_*`, `starlight_*`), 3 materials, `artilian:main`, datagen (`28/28`). Red-proof: removed `STARLIGHT_CRYSTAL_ORE` from `NEEDS_IRON_TOOL` and checked light levels `10`/`14`. |
  | 2 Overworld worldgen | `51b5cd6` | `ArtilianWorldGen` (`ore_sunsteel` size 9 Y `-24..72` count 12; `ore_starlight_crystal` size 6 Y `-64..24` count 7), `29/29`. Red-proof: omitted `ADD_STARLIGHT_CRYSTAL_ORE` → `worldgenfeaturesplacestoneanddeepslateores` failed. |
  | 3 Weapons, tools & 3D armor | `2629550` | `ArtilianTiers`, `ArtilianArmorMaterials`, `ArtilianSwordItem`, `ArtilianArmorItem` (`GeoItem`), 18 gear items, `32/32`. Red-proof: switched `SUNSTEEL` to `INCORRECT_FOR_STONE_TOOL` → pickaxe test failed. |
  | 4 Art, client check & PR | `f1fe32f`..`915032d` | `tools/art/make_ores_and_gear_art.py`, 3D GeckoLib `sunsteel_armor.geo.json` / `starlight_armor.geo.json`, sun-cluster Sunsteel ores, 4-pointed constellation Starlight ores, `32/32` green. |

- **Security Gate:** `n/a` (standard vanilla block breaking & crafting rules only).
- **Evidence:** `qa/evidence/ores-and-gear-showcase-2026-11-15.png`, `qa/evidence/starlight-glow-night-2026-11-15.png`, `qa/evidence/player-armor-inventory-2026-11-15.png`, `qa/evidence/creative-tab-artilian-2026-11-15.png`.

### Phase 1 — Stage 4: Custom TNTs
- [x] **Complete** — Archived from `qa/archive/`.
- **Why & Decisions:** Four playful, expressive TNTs that never wreck kids' builds by default:
  1. `ArtilianConfig` (`allowDestructiveCustomTnt = false` default in `config/artilian-common.toml`).
  2. Every ignition path (`onCaughtFire`, `wasExploded`, empty-hand `Shift + Right-Click`, dispensers, flaming arrows) spawns `ArtilianPrimedTnt` (never vanilla `PrimedTnt`).
  3. 30-tick (`1.5`s) telegraph ground ring + rising `NOTE_BLOCK_PLING` chime (`0.8F` → `2.0F`).
  4. **Party**, **Bounce**, and **Frost Nova** never break solid terrain even with config `true`; **Cluster Quarry** only breaks non-`BlockEntity` blocks when `allowDestructiveCustomTnt = true` and spares Players & Chester.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Config, blocks, entity & telegraph | `9f2083e` | `ArtilianConfig`, `TntVariant`, `ArtilianTntBlock`, `ArtilianPrimedTnt`, `make_tnt_art.py`, `tnt_arena.nbt` (`34/34`). Red-proof: skipped `emitTelegraphRing` → telegraph test failed. |
  | 2 Party, Bounce & Frost Nova | `b436ac1` | `explodeParty` (Speed II, Regen I, hop), `explodeBounce` (launch + Slow Falling + Jump Boost, spares sleeping Chester), `explodeFrostNova` (water→Frosted Ice, hostiles frost damage, spares Players/Chester/pets), `37/37`. Red-proof: omitted `SLOW_FALLING` in Bounce. |
  | 3 Cluster Quarry & Security Gate | `1dab695` | `explodeClusterQuarry` (`SAFE_QUARRY_DAMAGE_CALCULATOR`, 4 gen-1 bomblets), `TntSecurityProbes` (`P1-07`–`P1-10`), `41/41`. |
  | 4 Client verification & PR | Stage 4 | `runClient` check, clean logs, `41/41` green. |

- **Security Gate (`TntSecurityProbes`, `security/tnt/README.md`, `security/FINDINGS.md`):**
  - `P1-07`: Default config breaks zero blocks across all 4 TNTs.
  - `P1-08`: Cluster Quarry bomblets check live `isDestructiveTntAllowed()` on detonation.
  - `P1-09`: Indirect ignition (redstone, chain, fire arrow, dispenser) spawns `ArtilianPrimedTnt`.
  - `P1-10`: Safe variants (`Party`, `Bounce`, `Frost Nova`) never break solid blocks even when config enabled.
- **Evidence:** `qa/evidence/custom-tnts-showcase-2026-11-15.png`.

### Phase 1 — Stage 5: Mana & Wands
- [x] **Complete**
- **Why & Decisions:** Accessible magic without consumable scroll ammo (`PRODUCT.md`):
  - `ManaData` (`artilian:mana`, `copyOnDeath()`): `100` max, `20`-tick post-cast pause, regenerates `5` Mana every `10` ticks (empty to full in `11`s), synced via `ManaSyncPayload` (`artilian:mana_sync`).
  - **Spark Wand** (`20` Mana, `10`t cooldown): `16`-block starlight beam (`6.0F` + `Glowing` + `Slowness I`) chaining to up to 2 hostiles (`4.0F`); spares Players, Chester, owned pets, villagers, babies.
  - **Heart-Bloom Staff** (`30` Mana, `20`t cooldown): `8.0`-block burst healing `6.0F`, `Regeneration II`, `Absorption I`, clearing negative effects & extinguishing fire on allies.
  - **Zephyr Dash Wand** (`25` Mana, `15`t cooldown): forward/upward dash (`Slow Falling` + `Speed II`) repelling hostiles within `4.0` blocks while leaving Players, Chester, and pets untouched.
  - `ManaHudOverlay`: `81x11` `#2B2338`/`#3A2F4D` panel with `#47B8FF` → `#9C5CFF` gradient fill above right hotbar.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Mana attachment, sync & items | `28f8426` | `ManaData`, `ManaSyncPayload`, `ManaService`, `ArtilianWandItem`, `make_wand_art.py`, `43/43`. Red-proof: skipped `REGEN_AMOUNT` → regen test failed (`got 60`). |
  | 2 Three wand spells & target safety | `261d7c8` | `castSparkWand`, `castHeartBloomStaff`, `castZephyrDashWand`, `isValidOffensiveTarget`/`isValidHealingTarget`, `46/46`. Red-proofs: bypassed Player/Chester exclusion; omitted `ally.heal(6.0F)`; omitted `SLOW_FALLING`. |
  | 3 HUD Mana Bar | `58d3b70` | `ManaHudOverlay` + `RegisterGuiLayersEvent`, `46/46`. |
  | 4 Client verification & PR | Stage 4 | `runClient` showcase (`80 / 100` HUD bar, particles, hotbar & glow frames), `46/46` green. |

- **Security Gate:** `n/a` (server-authoritative Mana & ally-safe target filters verified in `ManaAndWandsGameTests`).
- **Evidence:** `qa/evidence/mana-and-wands-showcase-2026-11-15.png`.

### Phase 1 — Stage 6: Phase 1 Art Pass
- [ ] **Built & Verified Locally; Waiting on Family In-Game Sign-Off**
- **Why & Decisions:** Upgrade Phase 1 placeholder art against `DESIGN.md` with deterministic Python generators (`tools/art/make_*.py`) and GeckoLib updates while keeping sleeping Chester visually indistinguishable from a vanilla oak chest.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Chester & Whistle art overhaul | `4118d2a` | `make_chester_art.py`, `awake_extras` & `teeth_upper` bones, idle double-blink/bouncy walk/double-chomp animations, `Phase1ArtGameTests` (`48/48`). Red-proof: renamed `awake_extras` → asset test failed. |
  | 2 TNTs, Wands & Mana HUD polish | `3f753e3` | Slime-spring `bounce_tnt`, twilight-indigo snowflake `frost_nova_tnt`, dark-outlined wands, `ManaHudOverlay` Sunsteel corner rivets, `48/48`. |
  | 3 Sunsteel & Starlight polish | `73d38d4` | Forged 8-rayed Sunsteel Paladin crest on `sunsteel_block`, horned `starlight_helmet` icon, `48/48`. |
  | 4 Client showcase & evidence | Stage 4 | `runClient` verified awake/sleeping Chester beside oak chest + chomp + gear + TNTs + wands + Creative Tab (`48/48`). |

- **Security Gate:** `n/a` (assets & asset-integrity checks only; `P1-01`–`P1-10` untouched & green).
- **Evidence & Done When:** `qa/evidence/chester-forms-artpass-2026-11-15.png`, `chester-chomp-artpass-2026-11-15.png`, `custom-tnts-artpass-2026-11-15.png`, `mana-and-wands-artpass-2026-11-15.png`, `ores-and-gear-artpass-2026-11-15.png`, `creative-tab-artpass-2026-11-15.png`. Done when the owner, Alex, and Steve sign off on Phase 1 in LAN co-op.

---

## Phase 2: Wild Biomes, Dungeons & Pet Battles

| Stage | What ships | Status |
|---|---|---|
| 1 | First Biome (*Crystal Canopy Woods*) + TerraBlender `4.1.0.8` | ✅ Complete |
| 2 | Remaining Biomes (*Emberbloom Meadows* & *Starlight Highlands*) | ✅ Complete |
| 3 | Pet Capture, `PetElement`, *Crystal Fox* & `P2-01`–`P2-03` | ✅ Complete |
| 4 | Pet Battle AI, `ElementalMob` & `P2-04` | ✅ Complete |
| 5 | Core Pet Roster (*Shadow Wisp*, *Cinder Crag*, *Zaphox*, *Galecorn*, *Ember Drake*, *Peepkin*) | ✅ Complete (official names & in-game review pending) |
| 6 | Kids' Drawn Expansion (*Prismling*, *Flurrywing*, *Sunbeam* + `SUNSTEEL` + `P2-05`, *Frogduck*, *Zapkin*) | ✅ Complete (PRs #5–#7; Phase 2 art signed off 2026-11-19) |
| 7 | Biome Structures & Custom `artilian:structure_enemies` | ✅ Complete (PR #3) |
| 8 | 15 Building-Block Variants & Hand-Built Structure Rebuilds | ✅ Complete (PR #4) |
| 9 | Phase 2 Art Pass & Family Co-op Playtest Sign-Off | ⏳ Art signed off (2026-11-19); LAN co-op playtest & official kid names pending |

### Phase 2 — Stage 1: First Biome (Crystal Canopy Woods)
- [x] **Complete**
- **Why & Decisions:** Bundle TerraBlender `4.1.0.8` via `jarJar` and ship `artilian:crystal_canopy_woods` (`#38D9C0` / `#68D8FF` palette, `ArtilianOverworldRegion` weight `8` modifying `BIRCH_FOREST` / `OLD_GROWTH_BIRCH_FOREST`, `CRYSTAL_TURF`, glowing `crystal_canopy_leaves` `lightLevel 6`, `crystal_bloom` `lightLevel 9`, Overworld ore tags).
- **Sub-stages:** `add7760` (TerraBlender setup, `48/48`), `58dd073` (blocks/datagen/art, `49/49`, red-proof emptied `CRYSTAL_CANOPY` tree grower), `68c526a` (biome/region/surface rules, `51/51`, red-proof skipped `Regions.register`), Stage 4 (`runClient` verification + `crystal_canopy_log` model fix).
- **Security Gate:** `n/a` (worldgen & blocks only).
- **Evidence:** `qa/evidence/crystal-canopy-woods-2026-11-15.png`.

### Phase 2 — Stage 2: Remaining Biomes (Emberbloom Meadows & Starlight Highlands)
- [x] **Complete**
- **Why & Decisions:** Complete the three-biome Overworld trio:
  - **Emberbloom Meadows (`artilian:emberbloom_meadows`):** Apricot sky `#FFC07A`, `ParticleTypes.SMALL_FLAME`, `ember_turf`, `ember_bloom` (light `9`), `Emberwood` trees (`emberwood_leaves` light `4`), mapping from `MEADOW` / `SUNFLOWER_PLAINS`.
  - **Starlight Highlands (`artilian:starlight_highlands`):** Violet-blue sky `#8B7CFF`, `ParticleTypes.END_ROD`, `starlight_turf` over `starlight_slate` steep cliff rule + boulders, `starlight_blossom` (light `10`), mapping from `WINDSWEPT_HILLS` / `WINDSWEPT_GRAVELLY_HILLS` / `STONY_PEAKS`.
- **Sub-stages:** `bff6297` (blocks/grower/art/datagen, `52/52`, red-proof emptied `EMBERWOOD` tree grower), `f8de276` (biomes/region/surface rules, `54/54`, red-proof omitted biomes from `ArtilianOverworldRegion`), Stage 3 (removed `EntityType.BEE` spawn entry error, `runClient` verified, `54/54`).
- **Security Gate:** `n/a` (biomes & blocks only).
- **Evidence:** `qa/evidence/emberbloom-meadows-2026-11-15.png`, `qa/evidence/starlight-highlands-2026-11-15.png`.

### Phase 2 — Stage 3: Pet Capture & Elemental Foundation
- [x] **Complete**
- **Why & Decisions:** Core pet system (`ArtilianPetEntity`, `PetElement` dual-element `1.5x`/`0.75x` system — initially 11 elements, expanded to 12 in Stage 6C, `CapturedPetData` component, `PetRosterData` attachment `copyOnDeath()`), `CaptureCrystalItem` (4 states, never breaks on miss), `BiomeTreatItem` (`+50` affinity, `100` befriended, wakes fainted pets), **Crystal Fox** (`artilian:crystal_fox`, `CRYSTAL / NATURE`), and non-lethal fainting (`100`-tick rest timer back into crystal/roster on fatal damage).
- **Sub-stages:** `61c7d66` (pet core, Crystal Fox, Capture Crystal, Treat, `60/60`, red-proof `TREAT_AFFINITY_GAIN = 0`), `610ddd7` (`PetSecurityProbes` `P2-01`–`P2-03`, `63/63`), Stage 3 (`discardIfOrphan` null-`petId` fix, `runClient` verified, `63/63`).
- **Security Gate (`PetSecurityProbes`, `security/pets/README.md`, `security/FINDINGS.md`):**
  - `P2-01`: Non-owner cannot capture, steal, or recall another player's pet.
  - `P2-02`: Non-lethal fainting returns pet safely to crystal instead of dying (`AGENTS.md` kid-safe invariant).
  - `P2-03`: Cloned/copied crystal cannot duplicate an active pet (`reconcileWithRoster`).
- **Evidence:** `qa/evidence/pet-capture-crystal-fox-2026-11-15.png`, `qa/evidence/pet-capture-hud-2026-11-15.png`.

### Phase 2 — Stage 4: Pet Battle AI
- [x] **Complete**
- **Why & Decisions:** Summoned pets fight alongside owners (`PetMeleeAttackGoal`, `PetOwnerHurtByTargetGoal`, `PetOwnerHurtTargetGoal`, `PetNearestEnemyGoal` targeting `Enemy`, `artilian:structure_enemies`, and `artilian:bosses` while skipping Creepers passively), disengage to follow beyond `16` blocks, clear targets when sitting, apply `PetElement#multiplierAgainst` + signature combat perks (`60`t cooldown), and enforce unified kid-safe exclusions via `PetService#isValidBattleTarget` (never harms `Player`, `ChesterEntity`, villagers, golems, armor stands, babies, or tamed/befriended pets).
- **Sub-stages:** `4adc633` (battle goals, elemental combat, `ElementalMob`, `67/67`, red-proof returned `1.0F` multiplier), `b515073` (`P2-04` probe `68/68`), Stage 3 (`runClient` Crystal Fox vs Pillager, `68/68`).
- **Security Gate (`PetSecurityProbes`):** `P2-04` (`p2_04_tamedPetNeverAttacksPlayersChesterVillagersOrAlliedPets` — EXPLOITED then HELD).
- **Evidence:** `qa/evidence/pet-battle-crystal-fox-2026-11-15.png`.

### Phase 2 — Stage 5: Core Overworld Pet Roster
- [x] **Complete** — Merged in PR #1 (`bfa4edb`)
- **Sub-stage 5A — Steve's Drawn Pets (`Shadow Wisp` & `Cinder Crag`, `qa/evidence/steve-pets-drawing-2026-11-15.jpg`):**
  - **Shadow Wisp (`artilian:shadow_wisp`):** `SHADOW`, scaled `2.2x` (`65006ad`), Starlight Highlands at any light level.
  - **Cinder Crag (`artilian:cinder_crag`):** `FIRE / EARTH`, `fireImmune()`, Overworld & Nether near `FluidTags.LAVA` (`6x3` blocks).
  - Commits `a3a38e7`, `8704c27` (`72/72`, red-proofs: `hasNearbyLava` always true; `STARLIGHT_SLATE` omitted from shadow spawn check), `65006ad`. Evidence: `qa/evidence/pet-roster-steve-pets-2026-11-15.png`.
- **Sub-stage 5B — Alex's Drawn Pets (`Zaphox` & `Galecorn`, `qa/evidence/alex-pets-drawing-2026-11-15.jpg`):**
  - **Zaphox (`artilian:zaphox`):** `ELECTRIC / AIR`, cheetah-fox-elephant fusion with mini-trunk & sawtooth wings, glide descent + zero fall damage, Starlight Highlands.
  - **Galecorn (`artilian:galecorn`):** `AIR / NATURE`, fox-unicorn fusion with 360° Whirlwind Tail-Spin knockback (4–6 blocks), Emberbloom Meadows & Crystal Canopy Woods.
  - `76/76` green; red-proofs: `ZaphoxEntity#causeFallDamage` → true; disabled Galecorn whirlwind knockback. Evidence: `qa/evidence/pet-roster-alex-pets-2026-11-15.png`.
- **Sub-stage 5C — Dimension-Preview Pets (`Ember Drake` & `Peepkin`):**
  - **Ember Drake (`artilian:ember_drake`):** `DRAGON / FIRE`, `fireImmune()`, zero fall damage, Emberbloom Meadows (introduces Steve's Dragon theme early).
  - **Peepkin (`artilian:peepkin`):** `SHADOW / STARLIGHT` floating eye creature, zero fall damage, Starlight Highlands & Crystal Canopy Woods (introduces Alex's Eye theme early, distinct from Phase 4's Blinky).
  - Commits `51778bb`, `07bd334`, `4fa5323`, `bfa4edb` (`80/80` green, red-proofs: `fireImmune` → false; `causeFallDamage` → `super`). Evidence: `qa/evidence/pet-roster-dragon-eye-2026-11-16.png`.
- **Security Gate:** Inherited `P2-01`–`P2-04` (`HELD`).

### Phase 2 — Stage 6: Kids' Drawn Expansion & Legendary Pets
- [x] **Complete** — PRs #5, #6, #7 merged; owner signed off on Phase 2 art 2026-11-19
- Reference drawings and notes live in `qa/reference/pets/` (`NOTES.md`, `prismling-drawing-*.jpg`, `flurrywing-drawing-2026-11-17.jpg`, `sunsteel-drawing-2026-11-17.jpg`, `frog-duck-drawing-2026-11-17.jpg`, `lightning-guy-drawing-2026-11-17.jpg`).
- **6A — Prismling (`artilian:prismling`, `[x] Complete`, PR #5 `85b09b3`, `ab165d0`):**
  - First legendary pet (for everyone). `ElementalMob#isAllTypes() = true`: picks the highest-multiplier element among all `PetElement.values()` on attack, takes `1.0x` neutral damage from every element, "All Types" crystal badge.
  - 30 HP, 5.0 ATK, 0.30 speed; rare weight `1` group `1` across all three Artilian biomes.
  - Stage 3 art rework: 4.1-block towering glowing guardian (hitbox `2.8 x 4.1`, eye height `3.3`, `256x128` sheet + `prismling_glowmask.png` with visor eyes, crest, cuffs, veins, floating elemental symbols). Owner approved 4.1-block size.
  - `105/105` green; red-proofs: `attackElementAgainst` returning primary; `isAllTypes` returning `false`. Evidence: `qa/evidence/prismling-2026-11-17.png`.
- **6B — Flurrywing (`artilian:flurrywing`, `[x] Complete`, PR #6 `7702d48`, `1f60810`, `89e44cd`):**
  - `ICE / AIR`, regular roster stats (22 HP, 4.5 ATK, 0.28 speed), hitbox `1.3 x 2.75` (`2.75` blocks tall), Starlight Highlands weight `4`.
  - Three stacked snowballs with coal buttons, turquoise beanie, symmetric middle-snowball stick & mitten arms, 4 symmetric butterfly frost wings (`tools/art/models/flurrywing.bbmodel`), hover damping + zero fall damage, `SNOWFLAKE` particles.
  - `109/109` green; red-proofs: `causeFallDamage` → `super`; hitbox shrunk to `0.9 x 1.8`. Evidence: `qa/evidence/flurrywing-2026-11-17.png`.
- **6C — Sunbeam (`artilian:sunbeam`, `[x] Complete` — Stages 1–2 + v2 Art in PR #7; art signed off 2026-11-19):**
  - Second legendary pet (**Sunbeam** is the owner's final name). Adds 12th `PetElement.SUNSTEEL` (`0xFBBF24`, strong vs `SHADOW`/`ICE`/`DRAGON`, resisted by `FIRE`/`EARTH`, weak to `WATER`/`ELECTRIC`, applies `GLOWING` 60t + `WEAKNESS` 60t).
  - Ghast-tall (`3.0 x 4.0`, eye height `3.3`, 30 HP, 5.0 ATK, hover, rare weight `1` in Emberbloom Meadows).
  - **Sun Aura:** places one level-15 `minecraft:light` block at chest only in **air** (NBT-persisted `SunbeamPlacedLight`, cleared on move/unload/recall/faint/remove) + heals owner and owner's pets within 8 blocks (`REGENERATION I` + `1` HP every 40t; never others).
  - **Security Gate `P2-05` (`PetRosterGameTests.sunbeamLightOnlyReplacesAirAndIsRemovedWithIt`):** EXPLOITED when air check / NBT light save / `remove()` cleanup disabled; HELD (`security/FINDINGS.md`). Also red-proofed owner-only aura heal & `WATER` vs `SUNSTEEL` matchup.
  - **v2/v3 Art Pass (`4db7114` / `art/pass-3-pets`):** `make_pets_art.py --sunbeam` generates `sunbeam.geo.json`, `sunbeam.png`, `sunbeam_glowmask.png` (armored guardian visor, angled solar eyes, forehead sun-diamond, 5-ray solar crown, 4 spiky wing-arms, flush chest solar-emitter emblem; the sunbeam attack fires as live chest particles in `SunbeamEntity#doHurtTarget` instead of a model bone). Commits `32d67fd`, `4db7114`.
- **6D — Frogduck (`artilian:frogduck`, `[x] Complete` — Stages 1–2 in PR #7; art signed off 2026-11-19):**
  - `WATER / NATURE` (roster's first `WATER` primary), hitbox `1.25 x 1.35`, 24 HP, 4.5 ATK, Crystal Canopy Woods weight `4`.
  - Stout teal frog with middle front face plate (brown eyes, teal dots, brown cloud), four 3-clawed feet, and orange duck with outstretched wings perched on its head (`make_pets_art.py --frogduck`).
  - Amphibious: `canDrownInFluidType` false + short-hop (`< 5.0F`) fall-damage immunity.
  - Red-proofs: removed `< 5.0F` hop guard; shrank hitbox to `0.8 x 0.9`. Commits `7c3f655`, `d2ba533`.
- **6E — Zapkin (`artilian:zapkin`, `[x] Complete` in PR #7 `75e29e6`, `e88a953`, `3b6d13d`):**
  - Pure `ELECTRIC`, hitbox `0.95 x 1.55`, 22 HP, 4.5 ATK, 0.32 speed, Starlight Highlands weight `4`, `ELECTRIC_SPARK` particles.
  - v2 storm-navy armor, glowing cyan/white-hot visor, Z-kink lightning head bolts, back Tesla coils, spinning crimson/cyan/yellow plasma orb hands, `zapkin_glowmask.png` (`make_pets_art.py --zapkin`).
  - Red-proof: `getPrimaryElement` → `NATURE`. Evidence: `qa/evidence/zapkin-2026-11-17.png`.

### Phase 2 — Stage 7: Biome Structures & Structure Enemies
- [x] **Complete** — Merged in PR #3 (`f969c60`, `8284ff4`, `d549792`).
- **Why & Decisions:** Add one landmark structure and one non-griefing `artilian:structure_enemies` guardian per Artilian biome:
  - **Crystal Grove Shrine** (`artilian:crystal_grove_shrine`) in Crystal Canopy Woods, guarded by **Prism Sentinel** (`CRYSTAL / EARTH`).
  - **Emberbloom Outpost** (`artilian:emberbloom_outpost`) in Emberbloom Meadows, guarded by **Cinder Raider** (`FIRE / EARTH`).
  - **Starlight Watchtower** (`artilian:starlight_watchtower`) in Starlight Highlands, guarded by **Astral Stalker** (`STARLIGHT / SHADOW`).
- **Tests & Red-proofs:** `StructureEnemiesGameTests` (`93/93`, red-proof omitted `PRISM_SENTINEL` from `STRUCTURE_ENEMIES` tag) and `BiomeStructuresGameTests` (`97/97`, red-proof omitted `HAS_CRYSTAL_GROVE_SHRINE` biome tag).
- **Security Gate:** `n/a` (non-griefing enemies only; no block-breaking or ownership changes).
- **Evidence:** `qa/evidence/crystal-grove-shrine-2026-11-17.png`, `emberbloom-outpost-2026-11-17.png`, `starlight-watchtower-2026-11-17.png`.

### Phase 2 — Stage 8: Building-Block Variants & Structure Rebuilds
- [x] **Complete** — Merged in PR #4 (`0aa9a8e`, `ad061c3`, `a49c2f7`), archived from `qa/archive/`.
- **Why & Decisions:** Replace plain 9×9 boxy structure templates with proper depth/architecture against `qa/reference/structures/`:
  1. Added **15 building-block variants** (`crystal_canopy_*` and `emberwood_*` stairs, slab, fence, fence gate, door, trapdoor; `starlight_slate_*` stairs, slab, wall) + `make_building_blocks_art.py` + stonecutter recipes.
  2. Rebuilt `crystal_grove_shrine` (`15×13×15`), `emberbloom_outpost` (`15×13×15`), and `starlight_watchtower` (`13×20×13`) with `<= 16×20×16` footprint cap, single reward chest + loot table, `>= 2` persistent guardians, and kid-safe banned-block enforcement (no lava/fire/TNT/command blocks/spawners).
- **Tests & Red-proofs:** `BuildingBlockVariantsGameTests` + `BiomeStructuresGameTests` (`101/101` green; red-proof emptied `minecraft:walls` → wall connection test failed).
- **Security Gate:** `n/a` (template safety rules tested in `BiomeStructuresGameTests`).
- **Evidence:** `qa/evidence/crystal-grove-shrine-rebuild-*-2026-11-17.png`, `emberbloom-outpost-rebuild-*-2026-11-17.png`, `starlight-watchtower-rebuild-*-2026-11-17.png`.

### Phase 2 — Stage 9: Phase 2 Art Pass & Co-op Sign-Off
- [ ] **Art Signed Off; LAN Co-op Playtest & Official Kid Names Remaining**
- **Completed & signed off (2026-11-19):** Phase 2 art pass signed off by owner (Crystal Fox, Prismling, Flurrywing, Sunbeam v2, Frogduck, Zapkin, Stage 5 roster, Stage 8 structure rebuilds, and structure enemies).
- **Remaining:**
  - Apply official kid-chosen display names (`en_us.json` only; keep entity IDs) for placeholder pets (*Shadow Wisp*, *Cinder Crag*, *Prismling*, *Flurrywing*, *Frogduck*, *Zapkin*).
  - **Done When:** All CI checks green and a family LAN co-op playtest verifies biomes, pet capture, battling, fainting, and structure exploration.

---

## Phase 3: Custom Bosses & High-Tier Arsenal

| Stage | What ships | Status |
|---|---|---|
| 1 | Boss Framework (`ArtilianBossEntity`, `TelegraphedAttack`, `BossAltarBlock`, `P3-01`) | ⏳ Sub-stages 1–3 merged in PR #7 (`137/137`); Sub-stage 4 client check pending |
| 2 | The Crystal Colossus (`CrystalColossusEntity`, `Crystal Colossus Arena`, `P3-02`) | ✅ Complete (`145/145`, merged in PR #10) |
| 3 | Captain Brimstone (`CaptainBrimstoneEntity`, `Brimstone Bastion`, `P3-03`) | ✅ Complete (`153/153`, client verified on `phase-3-stage-3-captain-brimstone`) |
| 4 | Tier 3 Arsenal (weapons, armor, tools, advanced Staves & Boss TNTs) | ⏳ Sub-stages 1–2 complete (`162/162`); Sub-stage 3 client check pending |
| 5 | Phase 3 Art Pass & Family Co-op Sign-Off | ⏹ Pending |

### Phase 3 — Stage 1: Boss Framework & Rematch Altars
- [ ] **Sub-stages 1–3 Complete (PR #7); Sub-stage 4 (Client Verification & Screenshot) Pending**
- **Why & Decisions:** Provide shared boss infrastructure before building *The Crystal Colossus* and *Captain Brimstone*:
  1. **`boss/ArtilianBossEntity`:** Extends `Monster`, implements `GeoEntity` & `ElementalMob`; 48-block range-managed `ServerBossEvent` (`NOTCHED_10`, color from primary element, LOS not required); `setPersistenceRequired()`, `removeWhenFarAway` false, knockback resistance `0.8`, ignores `mobGriefing`.
  2. **Test boss (`artilian:training_golem`):** `TrainingGolemEntity` (`EARTH`, 120 HP, 6 ATK, *Ground Slam* 40t windup / radius 4 / 8 Earth damage); exempted from spawn-egg/tab/natural-spawn tests; placeholder art via `tools/art/make_bosses_art.py`.
  3. **`boss/TelegraphedAttack`:** Record enforcing `windupTicks >= 30` (`1.5`s floor per `PRODUCT.md`/`DESIGN.md`) at construction; `HoldStillWhileTelegraphingGoal`; `BossTelegraphs` renders `#FF4D5A` `DustParticleOptions` ground ring every 2 ticks and rising `NOTE_BLOCK_BELL` pitches (`0.8 / 1.2 / 1.6`); direct elemental damage only (never `Level.explode` with block interaction; skips bosses, spectators, creative players).
  4. **`artilian:bosses` tag:** Pet battle AI targets bosses like `artilian:structure_enemies`; non-lethal pet fainting preserved.
  5. **Rematch Altars (`BossAltarBlock`, `BossAltarBlockEntity`, `BossSummonItem`):** Tracks summoned boss UUID + 60-tick countdown; `isBusy` blocks re-summoning while countdown runs or boss is alive & loaded (before consuming item, sends `message.artilian.boss_altar.busy`); spawns boss 2 blocks above altar; frees automatically when boss dies or is removed; `Golem Core` summon item for Training Golem.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Boss base, bar & Training Golem | `d9fbb3a` | `ArtilianBossEntity`, `TrainingGolemEntity`, `artilian:bosses`, `make_bosses_art.py`, `105/105`. Red-proofs: range check true → `bossBarAddsNearbyPlayersAndDropsFarOnes` red; `removeWhenFarAway` true → `bossNeverDespawns` red; omitted from tag → `bossIsInBossTagAndPetsTargetIt` red. |
  | 2 Telegraphed attacks | `ecf9117` | `TelegraphedAttack`, `HoldStillWhileTelegraphingGoal`, *Ground Slam*, 6 GameTests (`111/111`). Red-proofs: windup floor off; damage at windup start; `ExplosionInteraction.MOB` on impact → `slamNeverBreaksBlocks` red. |
  | 3 Boss Altar, Golem Core & `P3-01` | `b7c69e4` | `BossAltarBlock`/`BlockEntity`, `BossSummonItem`, `BossTelegraphs`, `ArtilianBlockEntities`, 4 altar tests + `BossSecurityProbes` (`116/116` on branch; `137/137` in PR #7). Red-proofs: alive-boss check off; consume before busy check; `isBusy` forced false. |
  | 4 Client verification & evidence | Pending | Run `./gradlew runClient`, place Boss Altar, summon Training Golem via `/give` of `artilian:golem_core`, verify boss bar, danger-red `#FF4D5A` ground ring, climbing bell pitch, clean `run/logs/latest.log`, and save `qa/evidence/boss-framework-2026-11-17.png`. |

- **Security Gate (`BossSecurityProbes.p3_01_altarSpamSummonsOneBoss`, `security/bosses/README.md`, `security/FINDINGS.md`):**
  - `P3-01`: 20 uses in 20 ticks holding 20 Golem Cores summons exactly 1 boss and leaves 19 cores (`EXPLOITED` with `isBusy` off or stack consumed early; `HELD` in `137/137` suite).

### Phase 3 — Stage 2: The Crystal Colossus
- [x] **Complete (`145/145` GameTests & Live Client Verified)** — First full custom boss built on `ArtilianBossEntity` with three telegraphed attacks, Phase 2 *Crystal Overcharge*, an Overworld boss arena, rematch summoning, and boss drops for Phase 3 Stage 4 Tier 3 gear.
- **Why & Decisions:**
  1. **`CrystalColossusEntity` (`artilian:crystal_colossus`, `CRYSTAL / STARLIGHT`):** `200` HP (`100` hearts), `8.0` armor, `0.21` speed, `2.2 × 3.8` hitbox (3.8-block towering armored crystal titan with orbiting `floating_shards` and `AutoGlowingGeoLayer` glowmask), `BLUE` boss bar shifting to `PURPLE` on Phase 2 Overcharge. Weak to `EARTH` and `FIRE` so kids' *Training Golem*-tier pets (*Cinder Crag*, *Ember Drake*, *Crystal Fox*) feel great against it.
  2. **Three telegraphed attacks (all `>= 40` ticks, zero block breaking):**
     - **Crystal Quake (`40`t, radius `4.5`, `7.0` Crystal):** boss-centered shockwave pushing hit targets gently outward + `Slowness I` (`40`t).
     - **Starlight Spire (`45`t, radius `3.0`, `6.5` Starlight):** locks onto the target's starting position (never chases mid-windup so kids can step out of the red ring) and drops a glowing Starlight pillar + `Glowing` (`60`t).
     - **Prism Nova (`50`t, radius `7.0`, `8.5` Crystal, Phase 2 Overcharge only):** unlocks at `<= 50%` HP (`100` HP), shortens attack cooldown from `60`t to `45`t, turns the boss bar `PURPLE`, plays a resonant chime + action-bar alert, and pulses a 7-block shockwave.
  3. **Summoning & Trophy Progression:**
     - **Colossus Heart (`artilian:colossus_heart`):** `BossSummonItem` crafted from `Starlight Crystal` + `Amethyst Shard` + `Crystal Bloom` (and 1 guaranteed in the arena prep chest) for summoning/rematching at any **Boss Altar** (also added a survival recipe for `Boss Altar` from `Starlight Slate` + `Starlight Crystal` + `Sunsteel Ingot`).
     - **Prism Core (`artilian:prism_core`):** foil `BossTrophyItem` dropped (`2–3` guaranteed + `Starlight Crystal`, `Amethyst Shard`, `Biome Treat`) for forging Phase 3 Stage 4 Tier 3 Crystal gear.
  4. **Crystal Colossus Arena (`artilian:crystal_colossus_arena`):** Open-air stepped amphitheater in `CRYSTAL_CANOPY_WOODS` (`15×12×15`, wide 5-block cardinal stairs, 4 corner crystal obelisks, central `Boss Altar` under open sky, prep chest with `Colossus Heart` + `Biome Treats`, zero extra monster spawns, zero pre-spawned bosses, zero banned blocks).
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Entity, 3 attacks, Overcharge, items & art | `8ebd512` | `ArtilianBossEntity` multi-attack/target-ring hooks, `BossTrophyItem`, `CrystalColossusEntity`, `CrystalColossusRenderer`, `make_bosses_art.py`, datagen, and 5 `CrystalColossusGameTests` (`142/142`). Red-proofs: `telegraphCenterFor` forced to boss pos → `starlightSpireLocksTargetPositionAndCanBeDodged` red; `checkOvercharge` bypassed → `crystalColossusEntersOverchargeAtHalfHealthAndUnlocksPrismNova` red; `PRISM_CORE` pool omitted → `crystalColossusAttributesElementsTagEggAndLootTable` red. |
  | 2 Crystal Colossus Arena & `P3-02` | `fcf3cd1` | `artilian:crystal_colossus_arena` worldgen + chest loot + 2 arena GameTests + `BossSecurityProbes.p3_02_crystalColossusNeverBreaksBlocksOrAltarOrHarmsChester` (`145/145`). Red-proofs: `ExplosionInteraction.MOB` on impact → `P3-02` `EXPLOITED P3-02: Crystal Colossus destroyed or altered Chest`; `CRYSTAL_CANOPY_WOODS` omitted from `HAS_CRYSTAL_COLOSSUS_ARENA` → `crystalColossusArenaRegisteredInCrystalCanopyWoodsOnly` red. |
  | 3 Client verification & v2 model/art overhaul | This commit | Overhauled `build_crystal_colossus` in `tools/art/make_bosses_art.py` (256×128 UV non-overlapping sheet, Warden-style hollowed ribcage chest cavity with recessed rotated diamond Heart Core, outward-flaring horns & shoulder crystal clusters, low-hanging smasher arms with forearm crystal blades & articulated claws, dorsal spine crystals, vertical crystal-bone growth ridges, and weathered dark-slate strata/cracks/bioluminescent veins). Verified live in `./gradlew runClient`, capturing daylight arena (`qa/evidence/crystal-colossus-2026-11-19.png`) and midnight Overcharge (`qa/evidence/crystal-colossus-overcharge-2026-11-19.png`). |

- **Tests & Red-proofs (`CrystalColossusGameTests`):**
  1. `crystalColossusAttributesElementsTagEggAndLootTable`: verifies `CRYSTAL / STARLIGHT`, `200` HP, `artilian:bosses` tag, spawn egg, battle target validity, capture immunity, and `PRISM_CORE` loot drops.
  2. `starlightSpireLocksTargetPositionAndCanBeDodged`: verifies targeted ground ring stays at target's starting position and can be dodged while stationary targets inside take damage only at tick `45`.
  3. `crystalColossusEntersOverchargeAtHalfHealthAndUnlocksPrismNova`: verifies `BLUE` → `PURPLE` bar, `60`t → `45`t cooldown, NBT persistence, and `PRISM_NOVA` unlock & 6-block reach at `<= 50%` HP.
  4. `bossAltarSummonsCrystalColossusWithColossusHeartAndAllowsRematch`: verifies `Colossus Heart` summon, `P3-01` cross-summon lock against `Colossus Heart` & `Golem Core`, and rematch after death.
  5. `crystalColossusGeckoLibAssetsAndGlowmaskExist`: verifies required `.geo.json` bones, 4 animations, texture, glowmask, `colossus_heart.png`, and `prism_core.png`.
  6. `crystalColossusArenaRegisteredInCrystalCanopyWoodsOnly`: verifies biome tag exclusivity and empty monster spawn overrides.
  7. `crystalColossusArenaTemplateHasAltarPrepChestOpenSkyAndNoBannedBlocks`: verifies template bounds, 1 `Boss Altar` with clear sky, 1 prep chest with `Colossus Heart` + `Biome Treat`, zero banned blocks, zero pre-spawned bosses.
- **Security Gate (`BossSecurityProbes.p3_02_crystalColossusNeverBreaksBlocksOrAltarOrHarmsChester`, `security/bosses/README.md`, `security/FINDINGS.md`):**
  - `P3-02`: Under `mobGriefing = true` and `ArtilianConfig` destructive mode `true`, all 3 Colossus attacks (`CRYSTAL_QUAKE`, `STARLIGHT_SPIRE` aimed directly over `BOSS_ALTAR`/`CHEST`/`Chester`, and Overcharged `PRISM_NOVA`) break zero blocks/altars/chests and deal `0` damage to `ChesterEntity` (`EXPLOITED` with `ExplosionInteraction.MOB` on impact; `HELD` in `145/145` suite).
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and owner verifies the Crystal Colossus fight and arena in `./gradlew runClient`.

### Phase 3 — Stage 3: Captain Brimstone
- [x] **Complete (`153/153` GameTests & Live Client Verified)** — Second full custom boss built on `ArtilianBossEntity` with three telegraphed attacks, Phase 2 *Brimstone Broadside*, an Emberbloom Meadows arena, rematch summoning, and boss drops for Phase 3 Stage 4 Tier 3 gear.
- **Art Brief (`qa/reference/bosses/captain-brimstone.md`):**
  1. **Target height/scale:** `2.4 × 3.4` hitbox (`~4.0` blocks tall to top of mast/Jolly-Roger flag).
  2. **Silhouette inspiration (non-humanoid):** Living **Cursed Pirate Galleon-Walker** — curved dark-timber ship hull on 4 articulated iron crab/anchor legs, pointed bowsprit with skull figurehead, port & starboard broadside cannons, spoked ship's wheel, tall mast with tattered Crimson Jolly-Roger sail (skull & crossbones), and a Tricorne-hatted Pirate Skull-Captain with an eyepatch fused into the helm.
  3. **Anchor & accent palette:** Charred Emberwood & Iron Hull (`#221518`–`#5A3228`), Crimson-Oxide Jolly-Roger sailcloth (`#5E1A22`–`#9A2C34`) with bone-white (`#E8DFD0`) skull emblem, anchored with Sunsteel Gold (`#B87424`–`#FFE08A`) trim.
  4. **Key 3D recessed/angled features:** 4 angled crab-anchor legs, `25°` upward bowsprit + figurehead, flared gunwales with swiveling broadside cannons, spoked ship's wheel, and angled 3-flap Tricorne hat over a pirate skull with diagonal eyepatch.
  5. **`_glowmask.png` zones:** Hull portholes, cannon bores, uncovered magma eye socket, bowsprit figurehead eyes, and crow's-nest mast lantern (`#FF6B1A` / `#FFB84D` / `#FFF2B2`).
- **Why & Decisions:**
  1. **`CaptainBrimstoneEntity` (`artilian:captain_brimstone`, `FIRE / SHADOW`):** `220` HP (`110` hearts), `6.0` armor, `0.23` speed, `2.0 × 3.4` hitbox, `RED` boss bar shifting to `YELLOW` on Phase 2 *Brimstone Broadside*. Weak to `WATER` and `STARLIGHT` so kids' *Splashy*, *Frogduck*, and *Crystal Fox* pets shine against it.
  2. **Three telegraphed attacks (all `>= 35` ticks, zero block breaking):**
     - **Powder Keg Toss (`40`t, radius `3.5`, `7.0` Fire):** locks onto the target's starting position (never chases mid-windup so kids can step out of the red ring) and detonates a non-destructive brimstone blast + brief `30`t burn.
     - **Cutlass Cleave (`35`t, radius `4.0`, `7.5` Shadow):** boss-centered sweeping shockwave pushing targets outward + `Weakness I` (`40`t).
     - **Broadside Barrage (`50`t, radius `6.5`, `8.5` Fire, Phase 2 Broadside only):** unlocks at `<= 50%` HP (`110` HP), shortens attack cooldown from `60`t to `45`t, turns the boss bar `YELLOW`, plays cannon/blaze roar + action-bar alert, and pulses a 6.5-block ember shockwave.
  3. **Summoning & Trophy Progression:**
     - **Brimstone Compass (`artilian:brimstone_compass`):** `BossSummonItem` crafted from `Sunsteel Ingot` + `Blaze Powder` + `Charcoal` (and 1 guaranteed in the arena prep chest) for summoning/rematching at any **Boss Altar**.
     - **Brimstone Doubloon (`artilian:brimstone_doubloon`):** foil `BossTrophyItem` dropped (`2–3` guaranteed + `Sunsteel Ingot`, `Blaze Powder`, `Biome Treat`) for forging Phase 3 Stage 4 Tier 3 Fire/Brimstone gear.
  4. **Brimstone Bastion (`artilian:brimstone_bastion`):** Open-air stepped charred-slate & emberwood pirate bastion arena in `EMBERBLOOM_MEADOWS` (`15×12×15`, wide cardinal stairs, 4 corner brazier/cannon pillars, central `Boss Altar` under open sky, prep chest with `Brimstone Compass` + `Biome Treats`, zero extra monster spawns, zero pre-spawned bosses, zero banned blocks).
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Reference spec, Art Brief & Boss Art | `09633cd` | `qa/reference/bosses/captain-brimstone.md`, `build_captain_brimstone` / `build_brimstone_compass` / `build_brimstone_doubloon` in `tools/art/make_bosses_art.py` (`256×128` UV-checked sheet, `.geo.json`, `.animation.json`, `_glowmask.png`, item icons). |
  | 2 Entity, 3 attacks, Broadside, Items & Tests | `a3dab7e` | `CaptainBrimstoneEntity`, `CaptainBrimstoneRenderer`, `ArtilianEntities`, `ArtilianItems`, datagen (loot, recipes, models, lang, tags), and 5 `CaptainBrimstoneGameTests` (`150/150`). Red-proof: `telegraphCenterFor` forced to `this.position()` → `powderKegTossLocksTargetPositionAndCanBeDodged` red. |
  | 3 Brimstone Bastion Arena, `P3-03` & Client Check | `70f7183` + evidence | `artilian:brimstone_bastion` worldgen + chest loot + 2 arena GameTests + `BossSecurityProbes.p3_03_captainBrimstoneNeverBreaksBlocksOrAltarOrIgnitesTerrainOrHarmsChester` (`153/153`). Red-proof: `ExplosionInteraction.MOB` (`fire=true`) on impact → `P3-03` failed. Live `./gradlew runClient` verified (`qa/evidence/captain-brimstone-2026-11-19.png`, `qa/evidence/captain-brimstone-broadside-2026-11-19.png`). |

- **Tests & Red-proofs (`CaptainBrimstoneGameTests`):** `153/153` green (7 `CaptainBrimstoneGameTests` + `P3-03`). Red-proofs recorded above.
- **Security Gate (`BossSecurityProbes.p3_03_captainBrimstoneNeverBreaksBlocksOrAltarOrIgnitesTerrainOrHarmsChester`, `security/bosses/README.md`, `security/FINDINGS.md`):**
  - `P3-03`: Under `mobGriefing = true` and `ArtilianConfig` destructive mode `true`, all 3 Captain Brimstone attacks (`POWDER_KEG_TOSS` aimed over `BOSS_ALTAR`/`CHEST`/`EMBERWOOD_PLANKS`/`Chester`, `CUTLASS_CLEAVE`, and Broadside `BROADSIDE_BARRAGE`) break zero blocks/altars/chests, set zero fire blocks, and deal `0` damage to `ChesterEntity` (`HELD` in `153/153` suite).
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies the Captain Brimstone fight and Brimstone Bastion arena.

### Phase 3 — Stage 4: Tier 3 Arsenal
- [ ] **In Progress** — Boss-forged Tier 3 weapons, armor, tools, advanced Staves, and advanced TNTs (destructive variants strictly honor `allowDestructiveCustomTnt`).
- **Why & Decisions:** Reward defeating *The Crystal Colossus* (`Prism Core`) and *Captain Brimstone* (`Brimstone Doubloon`) with a cohesive Netherite-grade Tier 3 progression set that stays kid-friendly:
  1. **Tier 3 Prismatic Gear (`ArtilianTiers.PRISMATIC`, `ArtilianArmorMaterials.PRISMATIC`):**
     - **Tools & Weapons (`prismatic_sword`, `prismatic_pickaxe`, `prismatic_axe`, `prismatic_shovel`, `prismatic_hoe`):** `2050` durability, `9.5F` mining speed, `4.5F` attack damage bonus, `24` enchantability, `BlockTags.INCORRECT_FOR_NETHERITE_TOOL`, `fireResistant()`, repaired with `Prism Core`. `PrismaticSwordItem` applies `Glowing` (`60`t) and deals `+2.0F` bonus starlight damage on hit so kids see targets light up.
     - **Armor (`prismatic_helmet`, `prismatic_chestplate`, `prismatic_leggings`, `prismatic_boots`):** Defense `[4, 7, 9, 4]` (total `24`), `3.0F` toughness, `0.15F` knockback resistance, `24` enchantability, `fireResistant()`, GeckoLib 3D `PrismaticArmorRenderer` (`prismatic_armor.geo.json`, validated by `check_armor_sheets.py`). Wearing all 4 pieces grants `Fire Resistance` (`60`t refreshed on player tick) for Captain Brimstone & Nether safety.
     - **Recipes:** Direct crafting table recipes combining `Prism Core`, `Brimstone Doubloon`, and `Starlight Crystal` / `Stick` (no Smithing Table template friction for ages 5 & 7).
  2. **Two Advanced Boss-Forged Staves (`WandSpell` + `ManaService`):**
     - **Prism Nova Staff (`prism_nova_staff`, `40` Mana, `25`t CD):** Centered `6.5`-block starlight shockwave dealing `10.0F` magic damage + `Glowing` (`80`t) + gentle knockback to `KidSafeTargets.isValidOffensiveTarget` entities only.
     - **Brimstone Meteor Staff (`brimstone_meteor_staff`, `45` Mana, `30`t CD):** Raycasts up to `20` blocks to call down a `4.0`-block brimstone meteor dealing `12.0F` fire damage + `60`t fire to `KidSafeTargets.isValidOffensiveTarget` entities only, with **zero** block breaking and **zero** fire blocks placed.
  3. **Two Advanced Boss-Forged TNTs (`TntVariant` + `ArtilianPrimedTnt`):**
     - **Starlight Gravity TNT (`starlight_gravity_tnt`, `destructiveCapable = false`):** Always safe; pulls nearby `KidSafeTargets.isValidOffensiveTarget` entities inward, applies `Levitation` (`30`t) + `Glowing` (`100`t), and deals `6.0F` damage with zero block breaking.
     - **Brimstone Meteor TNT (`brimstone_meteor_tnt`, `destructiveCapable = true`):** Strictly honors `ArtilianConfig.allowDestructiveCustomTnt` (default `false`: zero block damage, zero fire blocks, `12.0F` blast damage to `KidSafeTargets.isValidOffensiveTarget` entities only; when `true`: `5.5F` explosion using `SAFE_QUARRY_DAMAGE_CALCULATOR` so block entities & kid-safe targets remain protected).
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Tier 3 Prismatic gear, 3D armor & tests | `954414e` | `ArtilianTiers.PRISMATIC`, `ArtilianArmorMaterials.PRISMATIC`, `PrismaticSwordItem`, `PrismaticArmorRenderer`, 9 fire-resistant items, `make_ores_and_gear_art.py`, `check_armor_sheets.py` clean, datagen, 4 `Tier3ArsenalGameTests` (`157/157`). |
  | 2 Advanced Staves, Boss TNTs & `P3-04` | This commit | `PRISM_NOVA` & `BRIMSTONE_METEOR` staves, `STARLIGHT_GRAVITY` & `BRIMSTONE_METEOR` TNTs, `make_wand_art.py`, `make_tnt_art.py`, datagen, 4 `Tier3ArsenalGameTests` + `TntSecurityProbes` `P3-04` (`162/162`, `build` green). Red-proof: `SAFE_QUARRY_DAMAGE_CALCULATOR` replaced with `null` & `ExplosionInteraction.TNT` forced → `P3-04` & `brimstoneMeteorTntHonorsDestructiveConfig` red. |
  | 3 Live client check & evidence | Pending | `./gradlew runClient` screenshot of Prismatic gear, Staves, and Boss TNTs (`qa/evidence/tier3-arsenal-2026-11-19.png`). |

- **Tests & Red-proofs (`Tier3ArsenalGameTests`, `TntSecurityProbes`):** `162/162` green (8 `Tier3ArsenalGameTests` + `P3-04`). Red-proof recorded above.
- **Security Gate (`TntSecurityProbes.p3_04_tier3StavesAndBrimstoneTntRespectKidSafeAndDestructiveConfig`, `security/FINDINGS.md`):** `P3-04` `HELD` in `162/162` suite.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, `check_armor_sheets.py` passes, and live `./gradlew runClient` verifies Tier 3 gear, Staves, and Boss TNTs.

### Phase 3 — Stage 5: Phase 3 Art Pass & Sign-Off
- [x] **Complete (`164/164` GameTests & Live Client Verified)** — Art tooling (`TD-01`), Phase 3 art & balance audit, end-to-end progression GameTests, and live client sign-off across Bosses, Altars, and Tier 3 Arsenal.
- **Why & Decisions:** Ensure every Phase 3 boss, altar, and Tier 3 item meets `DESIGN.md`'s 3D Art Standard and plays smoothly for ages 5 & 7 before moving to Phase 4 (*The Eye Realm*):
  1. **`TD-01` Art Tooling (`tools/art/art_lib.py`, `lint_art.py`, `preview_geo.py`):** Extract reusable v2 texture painters (`paint_weathered_stone`, `paint_forged_metal`, `paint_faceted_crystal`, `paint_organic_shaded`, `assert_uv_layout_clean`), generalize `check_armor_sheets.py` into `lint_art.py` across all `.geo.json` + `.png` pairs, and add headless 4-angle 3D contact-sheet rendering (`preview_geo.py`).
  2. **Phase 3 Art Polish:** Audit `training_golem`, `crystal_colossus`, `captain_brimstone`, `prismatic_armor`, `boss_altar`, and Tier 3 items with `lint_art.py` + `preview_geo.py`; upgrade `training_golem` from its Stage 1 `64×64` placeholder to a `128×128` v2 weathered-stone & arcane-core model.
  3. **Progression & Balance Sign-Off:** Verify end-to-end Phase 3 loop via `Phase3SignOffGameTests` (Boss Altar summons both bosses, drops `Prism Core` / `Brimstone Doubloon`, Tier 3 gear/staves/TNTs work with kid-safe defaults, all boss telegraphs `>= 30` ticks).
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 `TD-01` art tooling & Phase 3 audit | `11b2fac` | `art_lib.py`, `lint_art.py`, `preview_geo.py`; audited `training_golem`, `crystal_colossus`, `captain_brimstone`, `prismatic_armor` (caught `training_golem` `128×64` flat placeholder and `crystal_colossus` spire/claw UV issues). |
  | 2 Phase 3 art polish & sign-off tests | This commit | Upgraded `training_golem` to v2 `128×128` weathered stone & amber core (`art_lib.py`); fixed `crystal_colossus` `SPIRE_TALL_VIOLET` (`5×12×5`) & `CLAW_THUMB` UVs; `lint_art.py --phase3` clean; added `Phase3SignOffGameTests` (`164/164`, `runData`, `build`, `check_armor_sheets.py` green); `TD-01` marked `FIXED`. |
  | 3 Live client sign-off | This commit | `./gradlew runClient` visual verification & screenshots (`qa/evidence/tier3-arsenal-2026-11-20.png`, `qa/evidence/phase3-signoff-2026-11-20.png`); `build` & `164/164` GameTests green. |

- **Tests & Red-proofs:** `Phase3SignOffGameTests` (`164/164` green) + `python tools/art/lint_art.py --phase3` (exit `0`).
- **Security Gate:** `n/a` (art pass, tooling, and progression sign-off; `P3-01`–`P3-04` already `HELD`).
- **Done When:** `lint_art.py`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green; live `./gradlew runClient` verifies Tier 3 Arsenal and Phase 3 bosses/altar; `TD-01` is marked `FIXED`.

---

## Phase 4: The Eye Realm (Alex's Dimension)

- **Status:** 🔄 In Progress (Stage 4)
- **Done When:** All checks green, dimension and portal GameTests pass, and the Eye Realm loop is playable end to end.

| Stage | Name | Scope | Status |
|---|---|---|---|
| 1 | Dimension & Portal | `artilian:eye_realm` dimension and Gazing Portal with flat placeholder terrain | ✅ Complete (`169/169` + client verified) |
| 2 | Terrain & Sky | Eye-covered blocks, trees, terrain generation, blinking skybox | ✅ Complete (`173/173` + client verified) |
| 3 | Eye Realm Enemies | **Peepers**, **Oculus Slimes**, **Lens Guardians** | ✅ Complete (`177/177` + client verified) |
| 4 | Blinky Pet | **Blinky** capturable pet (`ArtilianPetEntity`) | ✅ Complete (`180/180` + client verified) |
| 5 | Eye Realm Structures | Dimension structures & chests | ✅ Merged into `main` (`c346c90`, `183/183`; client screenshot pending) |
| 6 | The Great All-Seer | Phase 4 boss (`ArtilianBossEntity`) with telegraphed attacks & rematch altar | 🔄 Code & GameTests complete (`187/187`); live client check pending |
| 7 | Phase 4 Art Pass | Final art polish against Alex's specs + co-op verification | 🔄 Art audit & GameTests complete (`189/189`); live client & co-op check pending |

### Phase 4 — Stage 1: Dimension & Gazing Portal
- [ ] **In Progress** — `artilian:eye_realm` dimension, `GazingPortalBlock`, `GazingEyeItem`, flat placeholder terrain, safe return portal generation, and GameTests.
- **Why & Decisions:** Give Alex a dedicated, safe, immediately visitable **Eye Realm** (`artilian:eye_realm`) before Stage 2 builds out its eye-covered terrain, trees, and blinking skybox:
  1. **Dimension & Flat Placeholder Terrain (`EyeRealmDimension` + `ArtilianWorldGenProvider`):**
     - `artilian:eye_realm` `DimensionType` (`minY = -64`, `height = 384`, `ambientLight = 0.25F` for kid-friendly visibility, `hasSkyLight = true`, `natural = true`, `coordinateScale = 1.0`).
     - `artilian:eye_realm` `Biome` with `DESIGN.md` Twilight Magenta (`#D946EF`) / Pupil Glow (`#7DF9FF`) / Deep Artilian Slate (`#2B2338`) fog, sky, water, and foliage colors.
     - `FlatLevelSource` placeholder terrain (`bedrock` at `-64`, `deepslate` up to `Y = 59`, `artilian:starlight_slate` surface `Y = 60..63`) so players arrive on solid ground at `Y = 64`.
  2. **Gazing Portal (`GazingPortalBlock`, `GazingPortalShape`, `ArtilianTags.Blocks.GAZING_PORTAL_FRAME`):**
     - Rectangular frame (`2..21` wide × `3..21` tall interior, minimum `2×3` interior) built from `artilian:gazing_portal_frame` (`artilian:starlight_slate`), supporting `X` and `Z` axes.
     - Implements NeoForge/Minecraft `Portal` (`getPortalTransitionTime`, `getPortalDestination`, `getLocalTransition`) to warp bidirectionally between `Level.OVERWORLD` and `artilian:eye_realm`.
     - **Safe Arrival (`P4-01`):** Searches within `64` blocks for an existing `GazingPortalBlock`; if none exists, builds a complete return `GazingPortal` frame + `5×5` `starlight_slate` landing pad at a safe open surface Y (never inside solid rock or below `minY + 8`).
  3. **Gazing Eye (`artilian:gazing_eye`, `GazingEyeItem`):**
     - Reusable igniter (`durability = 64`, crafted from `Ender Eye` + `Starlight Crystal` + `Amethyst Shard`) that lights a valid `GazingPortal` frame on right-click with twilight particles and sound.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Dimension, Biome, Portal Block, Gazing Eye & Art | Pending commit | `EyeRealmDimension`, `GazingPortalBlock`, `GazingPortalShape`, `GazingEyeItem`, `ArtilianTags.Blocks.GAZING_PORTAL_FRAME`, datagen (`eye_realm.json` dimension & dimension_type, biome, recipe, blockstate, models, lang), and `tools/art/make_eye_realm_art.py` (`gazing_portal.png` 4-frame animated + `.mcmeta`, `gazing_eye.png`). |
  | 2 `EyeRealmPortalGameTests` & `P4-01` | Pending commit | 5 `EyeRealmPortalGameTests` (`169/169` green, `runData`, `build`). Red-proof: `createReturnPortalWithPlatform` placing interior with flag `3` before top frame collapsed portal → `P4-01` failed (`Arrival block must be GAZING_PORTAL`); fixed by building frame edges first and filling interior with flag `18`. |
  | 3 Live client verification & evidence | Verified | `./gradlew runClient` screenshot of Gazing Portal & Eye Realm (`qa/evidence/eye-realm-portal-2026-11-20.png`). |

- **Tests & Red-proofs (`EyeRealmPortalGameTests`):** `169/169` green (5 new tests including `P4-01`). Red-proof recorded above.
- **Security / Safety Gate (`P4-01`):** `HELD` in `169/169` suite (`security/FINDINGS.md`).
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies lighting the Gazing Portal and stepping into `artilian:eye_realm`.

### Phase 4 — Stage 2: Terrain & Sky
- [x] **Complete (`173/173` GameTests & Live Client Verified)** — Eye-covered terrain blocks, Ocular Trees, `NoiseBasedChunkGenerator` worldgen, and blinking Great Sky Eye skybox.
- **Why & Decisions:** Replace Stage 1's flat placeholder with Alex's signature eye-covered twilight landscape and blinking skybox:
  1. **Eye-Covered Blocks & Ocular Wood Set (`ArtilianBlocks`, `ArtilianItems`, `ArtilianTreeGrowers.OCULAR`):**
     - `eye_turf` (`Eye Turf`), `gaze_stone` (`Gaze Stone` + stairs/slab/wall), and `blinking_eye_block` (`Blinking Eye Block`, light level `7`, 4-frame animated blinking texture).
     - `ocular_log`, `ocular_planks`, `ocular_stairs`, `ocular_slab`, `ocular_leaves` (light level `4`), `ocular_sapling` (light level `5`).
  2. **Noise Terrain & Features (`EyeRealmDimension`, `ArtilianWorldGen`):**
     - `EYE_REALM_NOISE` (`NoiseGeneratorSettings`) with Overworld-scale rolling hills (`seaLevel = 63`), `Gaze Stone` base, `Eye Turf` floor, `Starlight Slate` under-floor, `OCULAR_TREE_PLACED`, and `BLINKING_EYE_PATCH_PLACED`.
  3. **Blinking Skybox (`EyeRealmSkyEffects`):**
     - Registered on `EyeRealmDimension.EYE_REALM_EFFECTS` via `RegisterDimensionSpecialEffectsEvent`; renders twinkling cyan/magenta star-eyes and a giant Great Sky Eye overhead that smoothly blinks on a 160-tick cycle.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Eye Realm blocks, Ocular tree set & art | Pending commit | `eye_turf`, `gaze_stone` (+ stairs/slab/wall), `blinking_eye_block` (animated `.mcmeta`), `ocular_*` wood set, `ArtilianTreeGrowers.OCULAR`, datagen, and `tools/art/make_eye_realm_art.py`. |
  | 2 Noise terrain, blinking skybox & tests | Pending commit | `EYE_REALM_NOISE`, `NoiseBasedChunkGenerator` in `EYE_REALM_STEM`, `OCULAR_TREE_PLACED` + `BLINKING_EYE_PATCH_PLACED`, `EyeRealmSkyEffects`, and 4 `EyeRealmTerrainGameTests` (`173/173` green). Red-proof: removed `EYE_TURF` from `BlockTags.DIRT` → `eyeRealmBlocksHaveExpectedTagsAndLightLevels` failed → restored green. |
  | 3 Live client verification & evidence | This commit | `./gradlew runClient` screenshot of Eye Realm terrain, Ocular Trees, and blinking skybox (`qa/evidence/eye-realm-terrain-sky-2026-11-20.png`). |

- **Tests & Red-proofs (`EyeRealmTerrainGameTests`):** `173/173` green (4 new tests). Red-proof recorded above.
- **Security Gate:** `n/a` (worldgen, blocks, and client skybox only).
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies Eye Realm terrain, Ocular Trees, and the blinking skybox.

### Phase 4 — Stage 3: Eye Realm Enemies
- [x] **Complete (`177/177` GameTests & Live Client Verified)** — Three Eye Realm hostile mobs (**Peeper**, **Oculus Slime**, **Lens Guardian**), v2 `256×128` GeckoLib models & `_glowmask.png` sheets, Eye Realm biome spawn table, spawn eggs, loot tables, and GameTests.
- **Art Briefs (`qa/reference/`):**
  1. **Peeper (`artilian:peeper`):** `0.75 × 0.90` hovering eyeball scout (`~1.0` block tall). Deep Artilian-slate armored eyelid-hood (`#2B2338`–`#46385C`), 3D recessed eye socket with Pupil Glow (`#7DF9FF`) iris & dark pupil, 4 angled Twilight Magenta (`#D946EF`) bat-like eye-wings, and an articulated tail-stalk. Glowmask: iris, wing veins, tail tip.
  2. **Oculus Slime (`artilian:oculus_slime`):** `1.15 × 1.15` bouncy gelatinous slime cube with layered 3D outer membrane (`#5A2875`–`#9C4DCC`), 3D recessed inner eyeball core (`#7DF9FF` iris), 4 angled corner crystal-gel spikes (`#D946EF`), and dripping pseudopod nubs. Glowmask: inner eye iris, gel spike tips.
  3. **Lens Guardian (`artilian:lens_guardian`):** `1.10 × 2.35` towering armored sentinel (`~2.5` blocks tall) in Gaze Stone & Starlight Slate plates (`#241C30`–`#4A3C61`), broad pauldrons with angled prism-spikes, hollowed chest cavity housing a floating diamond Focus Lens (`#7DF9FF`), 3D recessed cyclopean helm (`#D946EF` / `#7DF9FF`), and articulated gauntleted arms & greaves. Glowmask: cyclopean eye, chest Focus Lens, gauntlet runes.
- **Why & Decisions:**
  1. **Shared `EyeRealmEnemyEntity` + `StructureEnemyEntity`-style GeckoLib loop:** Extends `Monster`, implements `GeoEntity` & `ElementalMob`, uses `AutoGlowingGeoLayer` in `EyeRealmEnemyRenderer`, never breaks blocks, and respects `KidSafeTargets` (never targets `ChesterEntity` or friendly pets unless provoked).
  2. **Distinct Elemental Typings & Combat Roles:**
     - **Peeper (`SHADOW / AIR`):** `22` HP, `0.26` speed, `4.0` damage, `2.0` armor; applies `Glowing` (`40`t) on hit; drops `Amethyst Shard` + rare `Biome Treat`.
     - **Oculus Slime (`SHADOW / CRYSTAL`):** `34` HP, `0.22` speed, `5.5` damage, `3.0` armor, `0.2` knockback resistance; applies `Slowness I` (`40`t) on hit; drops `Slimeball` + `Amethyst Shard`.
     - **Lens Guardian (`STARLIGHT / SHADOW`):** `54` HP, `0.21` speed, `7.5` damage, `6.0` armor, `0.45` knockback resistance; applies `Weakness I` (`40`t) on hit; drops `Starlight Crystal` + rare `Ender Pearl`.
  3. **Natural Spawns in `EYE_REALM_BIOME`:** Registered in `EyeRealmDimension.bootstrapBiome` (`PEEPER` weight `45`, `OCULUS_SLIME` weight `35`, `LENS_GUARDIAN` weight `15`) and `EnemyEvents` (`ON_GROUND`, `Monster::checkMonsterSpawnRules`), so Eternal Twilight (`18000L`) spawns them naturally while torches/light keep kid bases safe.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Art Brief, `128×128` GeckoLib models, textures & glowmasks | This commit | `tools/art/make_eye_realm_enemies_art.py` using `art_lib.py`; `lint_art.py` clean (`peeper`, `oculus_slime`, `lens_guardian`) + 4-angle `preview_geo.py` contact sheets (`qa/evidence/previews/*-preview.png`). |
  | 2 Entities, renderer, spawns, eggs, loot & GameTests | This commit | `EyeRealmEnemyEntity`, `PeeperEntity`, `OculusSlimeEntity`, `LensGuardianEntity`, `EyeRealmEnemyModel`/`Renderer` (`AutoGlowingGeoLayer`), `ArtilianEntities`, `ArtilianItems`, `EyeRealmDimension` monster spawns, datagen (`runData`), `build`, and 4 `EyeRealmEnemiesGameTests` (`177/177` green). Red-proof: removed `MobEffects.GLOWING` from `PeeperEntity.onElementalHit` → `eyeRealmEnemiesApplyElementalEffectsAndRespectKidSafeTargets` failed → restored green. |
  | 3 Live client verification & evidence | This commit | `./gradlew runClient` screenshot of Peepers, Oculus Slimes, and Lens Guardians in the Eye Realm (`qa/evidence/eye-realm-enemies-2026-11-20.png`). |

- **Tests & Red-proofs (`EyeRealmEnemiesGameTests`):** `177/177` green (4 new tests). Red-proof recorded above.
- **Security Gate:** `n/a` (standard hostile mobs with zero block-breaking and `KidSafeTargets` protection; spawn rules respect light level).
- **Done When:** `lint_art.py --strict`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green, and live `./gradlew runClient` verifies all three Eye Realm enemies in-game.

### Phase 4 — Stage 4: Blinky Pet
- [x] **Complete (`180/180` GameTests & Live Client Verified)** — **Blinky** (`artilian:blinky`), Alex's signature Eye Realm capturable pet (`ArtilianPetEntity`), `256×128` GeckoLib model & `_glowmask.png`, natural `EYE_REALM_BIOME` creature spawn, spawn egg, and GameTests.
- **Art Brief (`qa/reference/`):**
  - **Target Scale:** `0.85 × 0.95` hovering ocular familiar (`~1.0` block tall, friendly rounded proportions distinct from hostile Peepers).
  - **Silhouette Inspiration:** Friendly cyclopean starlight-eye sprite with a domed eyelid-hood, articulated upper & lower eyelids that periodically blink shut, twin angled Twilight Magenta (`#D946EF`) / Starlight Teal (`#38D9C0`) wing-fins, floating brow-crown halo, and a 3-segment comet tail.
  - **Palette & Structural Anchor:** Deep Artilian Slate & Starlight Indigo armored cowl (`#2B2338`–`#5A467A`) with forged Sunsteel Gold (`#FF9F38`) ocular bezel, creamy sclera (`#E2E0F2`), and Pupil Glow (`#7DF9FF`) iris.
  - **Key 3D Recessed/Angled Features:** Deeply recessed 3D eye socket framed by stepped bezel plates, articulated upper/lower eyelids (`lid_top`, `lid_bottom`), 15°/22° swept wing-fins, and an orbiting Starlight crystal crown.
  - **Glowmask Zones (`blinky_glowmask.png`):** Cyan iris (`#7DF9FF`), wing-fin tips, floating crown crystal, and comet tail tip.
- **Why & Decisions:**
  1. **`BlinkyEntity` (`ArtilianPetEntity`):** `STARLIGHT` primary / `SHADOW` secondary (`26` HP, `0.34` speed, `5.0` attack damage), zero fall damage with gentle hover fall-damping (`deltaMovement.y * 0.62`), super-effective (`1.5x`) against Eye Realm `SHADOW` enemies (`Peeper`, `Oculus Slime`).
  2. **Natural Spawns in `EYE_REALM_BIOME`:** Registered as `MobCategory.CREATURE` (`weight = 12`, `minCount = 1`, `maxCount = 2`) in `EyeRealmDimension.bootstrapBiome`, spawning on `EYE_TURF`, `GAZE_STONE`, `STARLIGHT_SLATE`, or `ANIMALS_SPAWNABLE_ON` (`BlinkyEntity::checkBlinkySpawnRules`).
  3. **Kid-Safe & Capture Crystal Compatible:** Inherits full `ArtilianPetEntity` taming (`Biome Treat` affinity, `Capture Crystal` capture/release, non-lethal fainting, `OwnableEntity` spell/TNT immunity).
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Art Brief, `256×128` GeckoLib model, texture & glowmask | `1b1985b` | `tools/art/make_blinky_art.py`, `lint_art.py --strict` clean (`entity/blinky`), `preview_geo.py` (`qa/evidence/previews/blinky-preview.png`). |
  | 2 `BlinkyEntity`, renderer, spawns, egg & GameTests | `1b1985b` | `BlinkyEntity`, `BlinkyModel`, `BlinkyRenderer` (`AutoGlowingGeoLayer`), `ArtilianEntities.BLINKY`, `ArtilianItems.BLINKY_SPAWN_EGG`, `PetEvents`, `EyeRealmDimension`, datagen (`runData`), `build`, and 3 `BlinkyPetGameTests` (`180/180` green). Red-proof: `BlinkyEntity.causeFallDamage` delegating to `super` → `blinkyRegisteredWithEggElementalTypesAndHoverImmunity` failed → restored green. |
  | 3 Live client verification & evidence | `1b1985b` | `./gradlew runClient` screenshot of Blinky in the Eye Realm (`qa/evidence/blinky-pet-2026-11-20.png`). |

- **Tests & Red-proofs (`BlinkyPetGameTests`):** `180/180` green (3 new tests). Red-proof recorded above.
- **Security Gate:** `n/a` (inherits `P2-01`–`P2-04` `ArtilianPetEntity` ownership, non-lethal fainting, and `CaptureCrystalItem` anti-duplication).
- **Done When:** `lint_art.py --strict`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green, and live `./gradlew runClient` verifies Blinky in the Eye Realm.

### Phase 4 — Stage 5: Eye Realm Structures
- [ ] **In Progress** — Two Eye Realm surface structures (**Ocular Observatory** and **Gazing Sanctum**) with recessed 3D depth, overhanging roofs, Blinking Eye focal accents, chest loot tables, and persistent Eye Realm enemy guardians.
- **Why & Decisions:** Give Alex's Eye Realm rewarding, atmospheric landmarks to explore and battle through:
  1. **Ocular Observatory (`artilian:ocular_observatory`, `15×15×15`):** Stepped `Gaze Stone` & `Starlight Slate` plinth, `Ocular Log` corner pillars, recessed `Gaze Stone` walls with purple lancet windows, overhanging stepped `Ocular Stairs` / `Gaze Stone Stairs` dome roof crowned by a 3D `Blinking Eye Block` Great Oculus, reward chest (`artilian:chests/ocular_observatory` with guaranteed `Biome Treat` + `Capture Crystal`, plus `Amethyst Shard`, `Starlight Crystal`, `Ender Pearl`, `Ocular Sapling`, and rare `Gazing Eye`), and persistent `Peeper` (×2) + `Oculus Slime` (×1) guardians (`spacing = 16, separation = 7, salt = 41526374`).
  2. **Gazing Sanctum (`artilian:gazing_sanctum`, `15×16×15`):** Monumental stepped `Starlight Slate` temple plinth, double-height `Ocular Log` colonnade with recessed `Magenta Stained Glass Pane` altar walls and embedded `Blinking Eye Block`s, double-tiered overhanging sanctum roof & `Starlight Crystal Block` spire, reward chest (`artilian:chests/gazing_sanctum` with guaranteed `Biome Treat`, `Capture Crystal`, and `Starlight Crystal`, plus `Ender Pearl`, `Blinking Eye Block`, `Golden Apple`, `Starlight Gravity TNT`, and rare `Gazing Eye`), and persistent `Lens Guardian` (×2) + `Peeper` (×1) guardians (`spacing = 20, separation = 9, salt = 52637485`).
  3. **Eye Realm Biome Binding (`ArtilianTags.Biomes`):** `HAS_OCULAR_OBSERVATORY` and `HAS_GAZING_SANCTUM` bound exclusively to `EyeRealmDimension.EYE_REALM_BIOME`.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Templates, loot, worldgen & GameTests | `5c5be67` | `ArtilianTags.Biomes`, `ArtilianBiomeTagsProvider`, `ArtilianStructures`, `ArtilianChestLootSubProvider`, `ArtilianStructureTemplateProvider`, `EyeRealmStructuresGameTests` (`180/180` green, `runData`, `build`). Red-proof: removed `STARLIGHT_CRYSTAL` pool from `buildGazingSanctumChest` → `gazingSanctumPlacesChestLootAndGuardians` failed → restored green. |
  | 2 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of Ocular Observatory & Gazing Sanctum in the Eye Realm (`qa/evidence/eye-realm-structures-2026-11-22.png`). |

- **Tests & Red-proofs (`EyeRealmStructuresGameTests`):** `183/183` green (3 new tests). Red-proof recorded above.
- **Security Gate:** `n/a` (worldgen structures & chest loot only; zero banned blocks, `Starlight Gravity TNT` is non-destructive, guardians are `EyeRealmEnemyEntity` with `KidSafeTargets`).
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies both Eye Realm structures in-game.

### Phase 4 — Stage 6: The Great All-Seer
- [ ] **In Progress** — Phase 4 Eye Realm boss **The Great All-Seer** (`artilian:great_all_seer`, `GreatAllSeerEntity extends ArtilianBossEntity`), `256×128` GeckoLib model & `_glowmask.png`, `Ocular Key` summon item + `BossAltarBlock` rematch support, `All-Seer's Lens` trophy, datagen, and `GreatAllSeerGameTests`.
- **Art Brief (`qa/reference/`):**
  - **Target Scale:** `1.8 × 2.8` hovering eldritch-cartoony ocular sovereign (`~3.0` blocks tall).
  - **Silhouette Inspiration:** Towering floating cyclopean sovereign with a domed armored eyelid-crown, central recessed Great Eye, 4 angled Twilight Magenta eye-wings (`wing_upper_*`, `wing_lower_*`), 4 orbiting Sentinel Mini-Eyes (`orbiter_1..4`), and a segmented cosmic robe/tail (`tail_1..3` + `tail_gem`).
  - **Palette & Structural Anchor:** Deep Artilian Slate & Void Indigo plates (`#140F1D`–`#584672`), forged Sunsteel Gold (`#EE962E`–`#FFEEB4`) ocular bezel & crown horns, creamy sclera (`#E2E0F2`), Pupil Glow (`#7DF9FF`) iris, and Twilight Magenta (`#D946EF`) eye-wings.
  - **Key 3D Recessed/Angled Features:** Deeply recessed 3D central eye socket with stepped bezel, articulated upper/lower eyelids (`lid_top`, `lid_bottom`), swept 4-wing geometry, and rotating 4-orbiter ring.
  - **Glowmask Zones (`great_all_seer_glowmask.png`):** Central cyan iris, 4 mini-eye irises, wing eye-sigils & tips, crown Starlight crystal, and tail rune gem.
- **Why & Decisions:**
  1. **`GreatAllSeerEntity` (`SHADOW / STARLIGHT`):** `240` HP, `0.23` speed, `7.5` attack damage, `9.0` armor, `150` XP; immune to fall damage (`causeFallDamage = false`) with gentle hover fall-damping; never breaks blocks and spares `ChesterEntity`.
  2. **Three Telegraphed Attacks (`>= 30` ticks windup) + Phase 2 (`<= 50%` HP *All-Seeing Overdrive*):**
     - `GAZING_BEAM` (`45`t, `3.2`r, `7.5F`, `STARLIGHT`) — locks onto target position, vertical End Rod/Glow beam, applies `Glowing` (`80`t).
     - `TWILIGHT_PULSE` (`40`t, `5.0`r, `7.0F`, `SHADOW`) — boss-centered AoE with outward knockback + `Slowness` & `Weakness` (`40`t).
     - `ALL_SEEING_NOVA` (`50`t, `7.5`r, `9.5F`, `SHADOW`) — unlocked at `<= 50%` HP (`isAllSeeing()`, `PURPLE` boss bar, `42`t cooldown), wide twilight-cyan ring burst + `Glowing` (`100`t).
  3. **Summon & Trophy (`Ocular Key` & `All-Seer's Lens`):** `OCULAR_KEY` (`BossSummonItem`) crafted from `Gazing Eye` + `Starlight Crystal` + `Amethyst Shard` + `Ender Pearl` summons The Great All-Seer on `BossAltarBlock`; drops `ALL_SEERS_LENS` (`BossTrophyItem`), `Starlight Crystal`, `Amethyst Shard`, `Ender Pearl`, and `Biome Treat`.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Art Brief, `256×128` GeckoLib model, texture, glowmask & sprites | Pending commit | `tools/art/make_great_all_seer_art.py`, `lint_art.py --strict` clean (`entity/great_all_seer`), `preview_geo.py` (`qa/evidence/previews/great_all_seer-preview.png`). |
  | 2 `GreatAllSeerEntity`, renderer, items, datagen & GameTests | Pending commit | `GreatAllSeerEntity`, `GreatAllSeerRenderer` (`AutoGlowingGeoLayer`), `OCULAR_KEY`, `ALL_SEERS_LENS`, `GREAT_ALL_SEER_SPAWN_EGG`, datagen (`runData`), `build`, and 4 `GreatAllSeerGameTests` (`187/187` green). Red-proof: `GreatAllSeerEntity.causeFallDamage` delegating to `super` → `greatAllSeerAttributesElementsTagEggHoverAndLootTable` failed → restored green. |
  | 3 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of The Great All-Seer & Boss Altar in the Eye Realm (`qa/evidence/great-all-seer-2026-11-22.png`). |

- **Tests & Red-proofs (`GreatAllSeerGameTests`):** `187/187` green (4 new tests). Red-proof recorded above.
- **Security Gate:** `n/a` (inherits `P3-01`–`P3-03` `BossAltarBlockEntity` single-boss lock, zero block-breaking, and `ChesterEntity` immunity).
- **Done When:** `lint_art.py --strict`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green, and live `./gradlew runClient` verifies The Great All-Seer in-game.

### Phase 4 — Stage 7: Phase 4 Art Pass
- [ ] **In Progress (`189/189` GameTests & Strict Art Lint Clean; Live Client Check Pending)** — Phase 4 art & preview audit (`--phase4` in `lint_art.py`), end-to-end Eye Realm sign-off tests (`Phase4SignOffGameTests`), and progression readiness verification.
- **Why & Decisions:** Ensure every Phase 4 entity, block, item, structure, and boss meets `DESIGN.md`'s 3D Art Standard and plays smoothly end-to-end before moving to Phase 5 (*Giant Dragon Jungle*):
  1. **Phase 4 Art Linter (`tools/art/lint_art.py --phase4 --strict`):** Added `PHASE4_ENTITIES` (`peeper`, `oculus_slime`, `lens_guardian`, `blinky`, `great_all_seer`), included `great_all_seer` in `BOSS_ENTITIES` (`256×128` minimum sheet), and verified all 5 models + `_glowmask.png` sheets + 4-angle `qa/evidence/previews/*-preview.png` contact sheets pass with zero UV overlaps, zero transparent holes, and zero flat single-color faces.
  2. **Phase 4 Sign-Off GameTests (`Phase4SignOffGameTests`):** Verifies all 5 Phase 4 GeckoLib models, required bones, animation clips, sheet dimensions, glowmasks, animated block `.mcmeta` files (`gazing_portal`, `blinking_eye_block`), block/item textures, `GreatAllSeerEntity` telegraph windup floor (`>= 30` ticks), Phase 4 crafting recipes, `EyeRealmDimension` registration, and structure/boss loot tables.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Phase 4 art audit, `lint_art.py --phase4` & `Phase4SignOffGameTests` | Pending commit | Updated `tools/art/lint_art.py` (`--phase4`, `great_all_seer` `256×128` boss budget), visually audited all 5 `qa/evidence/previews/*-preview.png` contact sheets, added `Phase4SignOffGameTests` (`189/189` green, `runData`, `build`, `lint_art.py --phase4 --strict` exit `0`). |
  | 2 Live client & co-op sign-off | Pending | `./gradlew runClient` walkthrough of Gazing Portal → Eye Realm terrain/sky → enemies & Blinky → Observatory/Sanctum → Great All-Seer (`qa/evidence/phase4-signoff-2026-11-22.png`). |

- **Tests & Red-proofs (`Phase4SignOffGameTests`):** `189/189` green (2 new tests) + `python tools/art/lint_art.py --phase4 --strict` (exit `0`).
- **Security Gate:** `n/a` (art pass, linter, and progression sign-off; `P4-01` already `HELD`).
- **Done When:** `lint_art.py --phase4 --strict`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies the full Eye Realm loop.

---

## Phase 5: Giant Dragon Jungle (Steve's Dimension) & Grand Finale

- **Status:** ✅ Code & GameTests Complete (Locked in PR #17, `v0.2.0`; awaiting live client & LAN co-op sign-off)
- **Done When:** All checks green, dimension and dragon GameTests pass, and both kids can clear the full Eye Realm and Dragon Jungle finale.

| Stage | Name | Scope | Status |
|---|---|---|---|
| 1 | Dimension & Portal | `artilian:dragon_jungle` dimension and Primal Portal (Steve's note: throw a boss drop into a Nether Portal to open the Dragon Jungle dimension) | 🔄 Code & GameTests complete (`194/194`); live client check pending |
| 2 | Mega-Jungle Worldgen | Prehistoric mega-jungle biomes, trees, and terrain | 🔄 Code & GameTests complete (`198/198`); live client check pending |
| 3 | Dragon Jungle Structures | **Dragon Roost** and **Amber Temple** | 🔄 Code & GameTests complete (`201/201`); live client check pending |
| 4 | First Dragon Species | One wild dragon end to end (establishes shared GeckoLib dragon rig) | 🔄 Code & GameTests complete (`205/205`); live client check pending |
| 5 | More Dragon Species | Additional wild dragon species reusing the shared dragon rig | 🔄 Code & GameTests complete (`208/208`); live client check pending |
| 6 | Hatchable Pet Dragons | Hatchable dragon companions (`ArtilianPetEntity`) | 🔄 Code & GameTests complete (`212/212`); live client check pending |
| 7 | Dragonscale Gear & Mega TNT | Dragonscale armor/weapons/tools & **Dragonfire Mega TNT** (honors `allowDestructiveCustomTnt`) | 🔄 Code & GameTests complete (`218/218`); live client check pending |
| 8 | Apex Dragon Boss | Phase 5 apex dragon boss (`ArtilianBossEntity`) with telegraphs & altar | 🔄 Code & GameTests complete (`222/222`); live client check pending |
| 9 | Twin-Star Endgame Rewards | Combined endgame rewards uniting Alex's and Steve's realms | 🔄 Code & GameTests complete (`226/226`); live client check pending |
| 10 | Phase 5 Art Pass & Finale | Final art polish & full family co-op finale verification | ✅ Complete & locked in PR #17 (`229/229`, `v0.2.0`) |

### Phase 5 — Stage 1: Dimension & Primal Portal
- [ ] **In Progress (`194/194` GameTests Green; Live Client Check Pending)** — `artilian:dragon_jungle` dimension, `PrimalPortalBlock` / `PrimalPortalShape`, `PrimalPortalEvents` (throw any `artilian:boss_drops` item into a lit Nether Portal to transform it), flat mossy placeholder terrain, safe return portal generation (`P5-01`), and `DragonJunglePortalGameTests`.
- **Why & Decisions:** Give Steve a dedicated, immediately visitable **Giant Dragon Jungle** (`artilian:dragon_jungle`) triggered by his signature boss-trophy Nether Portal ritual before Stage 2 replaces the flat floor with prehistoric mega-jungle terrain and giant trees:
  1. **Dimension & Flat Placeholder Terrain (`DragonJungleDimension` + `ArtilianWorldGenProvider`):**
     - `artilian:dragon_jungle` `DimensionType` (`OptionalLong.empty()` normal day/night cycle, `minY = -64`, `height = 384`, `ambientLight = 0.25F` for kid-friendly cave/night visibility, `hasSkyLight = true`, `natural = true`, `coordinateScale = 1.0`).
     - `artilian:dragon_jungle` `Biome` with `DESIGN.md` Canopy Emerald (`#22C55E`) & Ember Amber (`#F97316`) foliage/sky/water/fog accents and `SPORE_BLOSSOM_AIR` ambient particles.
     - `FlatLevelSource` placeholder (`bedrock` at `-64`, `stone` `-63..59`, `dirt` `60..62`, `moss_block` `63`) so players arrive on solid mossy ground at `Y = 64`.
  2. **Boss-Drop Nether Portal Ritual (`PrimalPortalEvents`, `ArtilianTags.Items.BOSS_DROPS`):**
     - `artilian:boss_drops` includes `PRISM_CORE` (Crystal Colossus), `BRIMSTONE_DOUBLOON` (Captain Brimstone), and `ALL_SEERS_LENS` (The Great All-Seer).
     - Throwing any `artilian:boss_drops` item into a lit `Blocks.NETHER_PORTAL` (intercepted on both `EntityTickEvent.Post` and `EntityTravelToDimensionEvent`) flood-fills all connected Nether Portal blocks into `PrimalPortalBlock` (preserving `AXIS`), consumes 1 item from the stack, and plays `ENDER_DRAGON_GROWL` + `PORTAL_TRIGGER` with emerald/flame particles.
  3. **Primal Portal & Safe Arrival (`PrimalPortalBlock`, `PrimalPortalShape`, `P5-01`):**
     - `ArtilianTags.Blocks.PRIMAL_PORTAL_FRAME` (`obsidian`, `crying_obsidian`), rectangular `2..21 × 3..21` frames on `X`/`Z` axes, bidirectional warp between `Level.OVERWORLD` and `artilian:dragon_jungle`.
     - **Safe Arrival (`P5-01`):** Reuses `GazingPortalBlock.findSafeSurfaceY`; builds a `5×5` `Moss Block` landing pad, clears `5×5×5` air headroom, places `4×5` `Obsidian` frame edges first, and fills `2×3` `PRIMAL_PORTAL` interior with flag `18`.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Dimension, Biome, Primal Portal, Boss-Drop Ritual & Art | Pending commit | `DragonJungleDimension`, `PrimalPortalBlock`, `PrimalPortalShape`, `PrimalPortalEvents`, `ArtilianTags.Blocks.PRIMAL_PORTAL_FRAME`, `ArtilianTags.Items.BOSS_DROPS`, `tools/art/make_dragon_jungle_art.py` (`primal_portal.png` 4-frame animated + `.mcmeta`), datagen (`runData`), `build`, and 5 `DragonJunglePortalGameTests` (`194/194` green). Red-proof: removed `BOSS_DROPS` tag check in `PrimalPortalEvents` → `nonBossDropItemInNetherPortalDoesNotTransformPortal` failed → restored green. |
  | 2 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of throwing a boss trophy into a Nether Portal and arriving in `artilian:dragon_jungle` (`qa/evidence/dragon-jungle-portal-2026-11-22.png`). |

- **Tests & Red-proofs (`DragonJunglePortalGameTests`):** `194/194` green (5 new tests including `P5-01`). Red-proof recorded above.
- **Security / Safety Gate (`P5-01`):** `HELD` in `194/194` suite (`security/FINDINGS.md`).
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies throwing a boss drop into a Nether Portal and stepping into `artilian:dragon_jungle`.

### Phase 5 — Stage 2: Mega-Jungle Worldgen
- [ ] **In Progress (`198/198` GameTests Green; Live Client Check Pending)** — Prehistoric mega-jungle noise terrain (`DRAGON_JUNGLE_NOISE`), `MultiNoiseBiomeSource` (`artilian:dragon_jungle` + `artilian:amber_volcanic_grove`), 12 new blocks (`jungle_turf`, `primal_basalt` + variants, `amber_moss`, `dragonwood_*` wood set), 1×1 & 2×2 `MEGA_DRAGONWOOD_TREE`, and `DragonJungleTerrainGameTests`.
- **Why & Decisions:** Replace Stage 1's flat placeholder with Steve's towering prehistoric Dragon Jungle:
  1. **Mega-Jungle Blocks & Dragonwood Set (`ArtilianBlocks`, `ArtilianItems`, `ArtilianTreeGrowers.DRAGONWOOD`):**
     - `jungle_turf` (`Jungle Turf`), `primal_basalt` (`Primal Basalt` + stairs/slab/wall), and glowing `amber_moss` (`Amber Moss`, light level `8`) for kid-friendly under-canopy visibility.
     - `dragonwood_log`, `dragonwood_planks`, `dragonwood_stairs`, `dragonwood_slab`, `dragonwood_leaves` (light level `3`), `dragonwood_sapling` (light level `5`).
     - `ArtilianTreeGrowers.DRAGONWOOD` supports both 1×1 `DRAGONWOOD_TREE` and 2×2 `MEGA_DRAGONWOOD_TREE` (`MegaJungleTrunkPlacer(14, 4, 14)` + `MegaJungleFoliagePlacer`).
  2. **Multi-Biome Noise Terrain (`DragonJungleDimension`, `ArtilianWorldGen`):**
     - `DRAGON_JUNGLE_NOISE` (`NoiseGeneratorSettings`, `Primal Basalt` base, `Jungle Turf` / `Moss Block` floor, `Dirt` under-floor, `seaLevel = 63`).
     - `MultiNoiseBiomeSource` splitting `artilian:dragon_jungle` (Canopy Emerald `#22C55E`, mega Dragonwood trees, Amber Moss) and `artilian:amber_volcanic_grove` (Ember Amber `#F97316`, volcanic fern grove, Amber Moss).
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Mega-Jungle blocks, Dragonwood set, multi-biome noise terrain & GameTests | Pending commit | 12 blocks/items, `tools/art/make_dragon_jungle_art.py` textures, `DRAGON_JUNGLE_NOISE`, `AMBER_GROVE_BIOME`, `MEGA_DRAGONWOOD_TREE`, datagen (`runData`), `build`, and 4 `DragonJungleTerrainGameTests` (`198/198` green). Red-proof: removed `JUNGLE_TURF` from `BlockTags.DIRT` → `dragonJungleBlocksHaveExpectedTagsAndLightLevels` and `dragonwoodSaplingGrowsSingleAndMegaTreesOnJungleTurf` failed → restored green. |
  | 2 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of Dragon Jungle terrain, giant 2×2 Dragonwood trees, and Amber Volcanic Grove (`qa/evidence/dragon-jungle-worldgen-2026-11-22.png`). |

- **Tests & Red-proofs (`DragonJungleTerrainGameTests`):** `198/198` green (4 new tests). Red-proof recorded above.
- **Security Gate:** `n/a` (worldgen, blocks, and biomes only).
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies mega-jungle terrain, giant Dragonwood trees, and both biomes.

### Phase 5 — Stage 3: Dragon Jungle Structures
- [ ] **In Progress (`201/201` GameTests Green; Live Client Check Pending)** — Two Dragon Jungle surface structures (**Dragon Roost** and **Amber Temple**) with recessed 3D depth, overhanging roofs, glowing `Amber Moss` focal accents, chest loot tables, and `DragonJungleStructuresGameTests`.
- **Why & Decisions:** Give Steve's Dragon Jungle rewarding landmarks across both biomes before Stage 4 introduces the first wild dragon species:
  1. **Dragon Roost (`artilian:dragon_roost`, `15×15×15`):** Bound to `DragonJungleDimension.DRAGON_JUNGLE_BIOME` (`ArtilianTags.Biomes.HAS_DRAGON_ROOST`). `Primal Basalt` & `Jungle Turf` foundation with `Amber Moss` accents, `Dragonwood Log` corner pillars, recessed `Dragonwood Planks` walls & `Orange Stained Glass Pane` windows, overhanging `Dragonwood Stairs` / `Primal Basalt Stairs` eaves, and an open-air sky-roost nest crown (`Hay Block` + `Gold Block` + `Amber Moss` perch). Reward chest (`artilian:chests/dragon_roost`) guarantees `Biome Treat` + `Capture Crystal` plus `Dragonwood Sapling`, `Amber Moss`, `Gold Ingot`, `Sunsteel Ingot`, `Blaze Powder`, `Golden Apple`, and rare `Starlight Crystal` (`spacing = 16, separation = 7, salt = 63748596`).
  2. **Amber Temple (`artilian:amber_temple`, `15×16×15`):** Bound to `DragonJungleDimension.AMBER_GROVE_BIOME` (`ArtilianTags.Biomes.HAS_AMBER_TEMPLE`). Stepped `Primal Basalt` & `Gilded Blackstone` pyramid-plinth, double-height `Dragonwood Log` colonnade, recessed `Yellow Stained Glass Pane` windows & glowing `Amber Moss` altar, double-tiered overhanging volcanic roof & spire. Reward chest (`artilian:chests/amber_temple`) guarantees `Biome Treat`, `Capture Crystal`, and `Amber Moss` plus `Gold Ingot`, `Sunsteel Ingot`, `Dragonwood Log`, `Diamond`, `Golden Apple`, and `Starlight Crystal` (`spacing = 18, separation = 8, salt = 74859607`).
  3. **Zero Guardians Until Stage 4:** `spawnOverrides(Map.of())` via shared `surfaceStructure(..., Map.of())` helper so no out-of-dimension mobs spawn prior to Dragon Jungle dragon species in Stage 4.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Templates, loot, worldgen & GameTests | Pending commit | `ArtilianTags.Biomes` (`HAS_DRAGON_ROOST`, `HAS_AMBER_TEMPLE`), `ArtilianBiomeTagsProvider`, `ArtilianStructures`, `ArtilianChestLootSubProvider`, `ArtilianStructureTemplateProvider`, `DragonJungleStructuresGameTests` (`201/201` green, `runData`, `build`). Red-proof: removed `AMBER_MOSS` pool from `buildAmberTempleChest` → `amberTemplePlacesTemplateWithoutBannedBlocksAndUnpacksChestLoot` failed → restored green. |
  | 2 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of Dragon Roost & Amber Temple in `artilian:dragon_jungle` (`qa/evidence/dragon-jungle-structures-2026-11-22.png`). |

- **Tests & Red-proofs (`DragonJungleStructuresGameTests`):** `201/201` green (3 new tests). Red-proof recorded above.
- **Security Gate:** `n/a` (worldgen structures & chest loot only; zero banned blocks).
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies both Dragon Jungle structures in-game.

### Phase 5 — Stage 4: First Dragon Species
- [ ] **In Progress (`205/205` GameTests Green & Strict Art Lint Clean; Live Client Check Pending)** — Establish the shared GeckoLib dragon rig (`dragon_rig.geo.json`, `dragon_rig.animation.json`, `WildDragonModel`, `WildDragonRenderer`, `WildDragonEntity`) and ship the first wild dragon species end-to-end: **Canopy Wyvern** (`artilian:canopy_wyvern`, `Dragon / Nature`) spawning in `artilian:dragon_jungle` and dropping `artilian:dragon_scale`.
- **Art Brief (`Canopy Wyvern` / `dragon_rig`):**
  - **Target height/scale:** ~`2.4` blocks tall at horn crest, ~`3.2` blocks nose-to-tail, ~`4.2` blocks wingspan (`EntityType` `1.55F × 1.95F`, `eyeHeight = 1.55F`, `shadowRadius = 1.05F`).
  - **Silhouette inspiration:** Classic storybook jungle wyvern/dragon with broad muscular chest & shoulders, tapered waist & hip, arched S-curved neck, horned dragon head with separate articulated lower jaw (`jaw`) and swept-back horns, 4 clawed legs, two-segment articulated dihedral wings (`left_wing_inner`/`outer`, `right_wing_inner`/`outer`) with leading wing-claws, and a 3-segment whip tail (`tail_1`, `tail_2`, `tail_3`) with dorsal ridge spines.
  - **Dark structural anchor + accent palette:** Deep jungle obsidian-slate (`#132A20` → `#1E4634`) armored scales anchored against `DESIGN.md` Canopy Emerald (`#22C55E` → `#4ADE80`) wing membranes/belly/crests and Ember Amber (`#F97316` → `#FDE047`) eyes, horn tips, and wing-vein runes.
  - **Key 3D recessed/angled features:** Inset eye sockets with glowing amber pupils, articulated lower `jaw`, angled brow horns (`[-25°, ±15°, 0°]`), angled dorsal spine fins along torso & tail, dihedral wing segments, and 3D taloned feet.
  - **`_glowmask.png` zones:** Emissive amber eyes, chest scale core, dorsal spine tips, and wing-vein runes (`AutoGlowingGeoLayer`).
- **Why & Decisions:**
  1. **Shared Dragon Rig (`dragon_rig.geo.json` + `dragon_rig.animation.json`):** Both Stage 4 (`canopy_wyvern`) and Stage 5's additional dragon species reuse one `256×128` UV-mapped GeckoLib skeleton (`idle`, `walk`, `fly`, `attack`), parameterized in `WildDragonModel` / `WildDragonRenderer` by species texture ID.
  2. **Wild Dragon Base (`WildDragonEntity extends Monster implements GeoEntity, ElementalMob`):** Enforces `KidSafeTargets.isValidOffensiveTarget` (Chester & player pets are never harmed), airborne wing-flutter fall immunity (`causeFallDamage` returns `false`), and `checkWildDragonSpawnRules` allowing natural daytime/nighttime spawns in `DragonJungleDimension.DRAGON_JUNGLE_BIOME` on `JUNGLE_TURF`, `MOSS_BLOCK`, `PRIMAL_BASALT`, or `DRAGONWOOD_LEAVES`.
  3. **Canopy Wyvern (`CanopyWyvernEntity`):** `PetElement.DRAGON` / `PetElement.NATURE`, `44.0` HP, `6.0` ATK, `4.0` Armor, applies brief `MobEffects.POISON` (`40` ticks) + `DRAGON_BREATH` / `SPORE_BLOSSOM_AIR` particles on hit, and drops `1–3` `Dragon Scale` (`artilian:dragon_scale`) for future Stage 7 Dragonscale gear.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Art Brief, `256×128` shared `dragon_rig`, `canopy_wyvern` textures, glowmask & `dragon_scale` sprite | Pending commit | `tools/art/make_dragons_art.py`, `lint_art.py --phase5 --strict` clean (`entity/canopy_wyvern`), `preview_geo.py` (`qa/evidence/previews/canopy_wyvern-preview.png`). |
  | 2 `WildDragonEntity`, `CanopyWyvernEntity`, renderer, `dragon_scale`, biome spawn, datagen & GameTests | Pending commit | `WildDragonEntity`, `CanopyWyvernEntity`, `WildDragonModel`, `WildDragonRenderer`, `DragonEvents`, `ArtilianEntities.CANOPY_WYVERN`, `ArtilianItems.DRAGON_SCALE` & `CANOPY_WYVERN_SPAWN_EGG`, `ArtilianTags.EntityTypes.WILD_DRAGONS`, `DragonSpeciesGameTests` (`205/205` green, `runData`, `build`). Red-proof: `WildDragonEntity.causeFallDamage` delegating to `super` → `canopywyvernregisteredtaggedhasegglootandelements` failed → restored green. |
  | 3 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of Canopy Wyvern in `artilian:dragon_jungle` (`qa/evidence/canopy-wyvern-2026-11-22.png`). |

- **Tests & Red-proofs (`DragonSpeciesGameTests`):** `205/205` green (4 new tests) + `python tools/art/lint_art.py --phase5 --strict` (exit `0`). Red-proof recorded above.
- **Security Gate:** `n/a` (wild dragon mob, loot, and biome spawns; enforces `KidSafeTargets` protection for Chester and pets).
- **Done When:** `lint_art.py --strict`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies Canopy Wyvern in `artilian:dragon_jungle`.

### Phase 5 — Stage 5: More Dragon Species
- [ ] **In Progress (`208/208` GameTests Green & Strict Art Lint Clean; Live Client Check Pending)** — Add **Amber Wyvern** (`artilian:amber_wyvern`, `Dragon / Fire`) and **Storm Glidewing** (`artilian:storm_glidewing`, `Dragon / Electric`) with **dedicated 3D GeckoLib silhouettes & animations** (no palette swaps!), grounded in the 12-type reference catalog (`qa/reference/dragon-species-references.md`).
- **Art Briefs (`Amber Wyvern` & `Storm Glidewing`):**
  - **12-Type Reference Catalog (`qa/reference/dragon-species-references.md`):** Classic Western Dragon, True Wyvern, Drake, Amphithere, Lindworm, Wyrm/Guivre, Eastern Dragon (Lung), Hydra, Feathered Serpent (Quetzalcoatl), Sea Serpent/Leviathan, Kirin/Qilin, and Knucker.
  - **Amber Wyvern (`amber_wyvern.geo.json` — Archetype #2 True Wyvern):** Bipedal 2-legged digitigrade stance (`0` separate forelegs), forward-pitched raptor torso, `2` massive clawed bat wing-arms, swept ram horns, and a barbed magma-stinger tail (`1.6F × 2.0F`, `shadowRadius = 1.1F`); obsidian-charcoal scales (`#161014` → `#4A2E30`) + molten Ember Amber (`#7C220C` → `#F97316` → `#FDE047`).
  - **Storm Glidewing (`storm_glidewing.geo.json` — Archetype #4 Amphithere):** Legless (`0` legs) hovering `4`-segment undulating sky-serpent body, `2` swept `3`-segment albatross glider wings, lightning-antler horns, cheek frills, and a wide fanned tail rudder (`1.45F × 1.85F`, `shadowRadius = 1.0F`); storm-indigo scales (`#12182C` → `#384C7C`) + sky-cyan/Starlight-teal (`#0E5676` → `#38D9C0` → `#BAF6FC`) + electric-gold (`#FACC15`).
  - **`_glowmask.png` zones:** Emissive eyes, chest elemental core, dorsal/stinger tips, and wing-tips/tail rudder (`AutoGlowingGeoLayer`).
- **Why & Decisions:**
  1. **Dedicated Rigs (`WildDragonModel(speciesId, rigId)`):** Replaced shared-rig reuse for Stage 5 so all three Phase 5 dragons have unmistakable silhouettes (`dragon_rig` Classic Western Dragon vs. `amber_wyvern` Bipedal Wyvern vs. `storm_glidewing` Legless Amphithere).
  2. **Amber Wyvern (`AmberWyvernEntity`):** `PetElement.DRAGON` / `PetElement.FIRE`, `48.0` HP, `7.0` ATK, `5.0` Armor, `fireImmune() = true`, ignites targets (`2.5s`) + `FLAME` particles, spawns in `AMBER_GROVE_BIOME` (weight `16`).
  3. **Storm Glidewing (`StormGlidewingEntity`):** `PetElement.DRAGON` / `PetElement.ELECTRIC`, `40.0` HP, `0.32` speed, `6.5` ATK, `3.0` Armor, applies `MobEffects.MOVEMENT_SLOWDOWN` (`35` ticks) + `ELECTRIC_SPARK` particles, spawns in both `DRAGON_JUNGLE_BIOME` (weight `10`) and `AMBER_GROVE_BIOME` (weight `6`).
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Art Briefs, `amber_wyvern` & `storm_glidewing` textures/glowmasks & previews | Pending commit | `tools/art/make_dragons_art.py`, `lint_art.py --phase5 --strict` clean (`canopy_wyvern`, `amber_wyvern`, `storm_glidewing`), `preview_geo.py` (`qa/evidence/previews/amber_wyvern-preview.png`, `qa/evidence/previews/storm_glidewing-preview.png`). |
  | 2 `AmberWyvernEntity`, `StormGlidewingEntity`, biome spawns, datagen & GameTests | Pending commit | `AmberWyvernEntity`, `StormGlidewingEntity`, `ArtilianEntities`, `ArtilianItems` spawn eggs, `ArtilianCreativeTabs`, `ArtilianClient`, `DragonEvents`, `DragonJungleDimension`, `ArtilianEntityTags`, `ArtilianEntityLoot`, `DragonSpeciesGameTests` (`208/208` green, `runData`, `build`). Red-proof: `AmberWyvernEntity.fireImmune()` returning `false` → `amberwyvernandstormglidewingregisteredtaggedeggslootandelements` failed → restored green. |
  | 3 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of Amber Wyvern & Storm Glidewing in `artilian:dragon_jungle` (`qa/evidence/more-dragon-species-2026-11-22.png`). |

- **Tests & Red-proofs (`DragonSpeciesGameTests`):** `208/208` green (3 new tests) + `python tools/art/lint_art.py --phase5 --strict` (exit `0`). Red-proof recorded above.
- **Security Gate:** `n/a` (wild dragon mobs, loot, and biome spawns; inherits `KidSafeTargets` protection for Chester and pets).
- **Done When:** `lint_art.py --phase5 --strict`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies Amber Wyvern & Storm Glidewing in `artilian:dragon_jungle`.

### Phase 5 — Stage 6: Hatchable Pet Dragons
- [ ] **In Progress (`212/212` GameTests Green & Strict Art Lint Clean; Live Client Check Pending)** — Add **Canopy Dragonling** (`Dragon / Nature`), **Amber Dragonling** (`Dragon / Fire`), and **Storm Dragonling** (`Dragon / Electric`) extending `PetDragonEntity` → `ArtilianPetEntity`, plus 3 hatchable `DragonEggBlock`s (`canopy_wyvern_egg`, `amber_wyvern_egg`, `storm_glidewing_egg`) dropped by wild dragons (`50%` chance).
- **Art Briefs (`Canopy Dragonling`, `Amber Dragonling`, `Storm Dragonling` & `Dragon Eggs`):**
  - **Target height/scale:** ~`0.85–0.90` blocks tall (`0.75F × 0.85–0.90F`, `shadowRadius = 0.45F`) baby counterparts of the 3 wild dragon species.
  - **Silhouettes:** `canopy_dragonling` (4-legged jungle hatchling with leafy-emerald wings & spiked tail), `amber_dragonling` (bipedal wyvern hatchling with swept ram-horn buds & glowing magma-stinger tail), `storm_dragonling` (legless hovering sky-serpent hatchling with wide cyan-gold glider wings & fanned tail rudder).
  - **Palettes & `_glowmask.png`:** `128×128` shaded sheets matching their adult species; emissive eyes, chest elemental cores, horn/wing tips, and progressive glowing egg cracks (`_0`, `_1`, `_2` on tapered 3D egg blocks).
- **Why & Decisions:**
  1. **Hatchable Egg Incubation (`DragonEggBlock`):** Scheduled ticks advance `HATCH` `0 -> 1 -> 2 -> hatch` (`240` ticks/stage on warm blocks like `Amber Moss`, `Jungle Turf`, `Moss Block`, `Hay Block`, `Magma Block`; `400` ticks/stage otherwise). Right-clicking with `Biome Treat` or `Blaze Powder` advances `HATCH` immediately so kids can speed up hatching.
  2. **Kid-Safe Pet Integration (`PetDragonEntity extends ArtilianPetEntity`):** Hatched dragonlings spawn befriended at `MAX_AFFINITY` with `persistenceRequired`, guaranteeing 100% capture into any empty `CaptureCrystalItem` (inheriting non-lethal fainting, `PetRosterData`, and `KidSafeTargets`).
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Art Briefs, `128×128` dragonling models/textures/glowmasks & 3-stage egg textures | Pending commit | `tools/art/make_dragon_pets_art.py`, `lint_art.py --phase5 --strict` clean (all 6 Phase 5 entities), `preview_geo.py` (`qa/evidence/previews/*_dragonling-preview.png`). |
  | 2 `PetDragonEntity`, 3 dragonlings, `DragonEggBlock`, loot, renderer, datagen & GameTests | Pending commit | `PetDragonEntity`, `CanopyDragonlingEntity`, `AmberDragonlingEntity`, `StormDragonlingEntity`, `DragonEggBlock`, `PetDragonModel`, `PetDragonRenderer`, `ArtilianEntities`, `ArtilianBlocks`, `ArtilianItems`, `ArtilianCreativeTabs`, `PetEvents`, `ArtilianClient`, datagen (`runData`), `build`, and 4 `DragonPetsGameTests` (`212/212` green). Red-proof: commented out `dragonling.addAffinity(MAX_AFFINITY)` in `DragonEggBlock.hatchEgg` → `dragoneggincubateshatchesbefriendeddragonlingandcapturesincrystal` failed → restored green. |
  | 3 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of Dragon Eggs hatching & dragonlings in-game (`qa/evidence/hatchable-pet-dragons-2026-11-22.png`). |

- **Tests & Red-proofs (`DragonPetsGameTests`):** `212/212` green (4 new tests) + `python tools/art/lint_art.py --phase5 --strict` (exit `0`). Red-proof recorded above.
- **Security Gate:** `n/a` (inherits `P2-01`–`P2-04` `CaptureCrystalItem` ownership, non-lethal fainting, and `KidSafeTargets`).
- **Done When:** `lint_art.py --phase5 --strict`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies Dragon Egg hatching and dragonling companions.

### Phase 5 — Stage 7: Dragonscale Gear & Mega TNT
- [ ] **In Progress (`218/218` GameTests Green; Live Client Check Pending)** — Full 9-piece **Dragonscale** gear set (`ArtilianTiers.DRAGONSCALE`, `ArtilianArmorMaterials.DRAGONSCALE`, `DragonscaleSwordItem`, `DragonscaleArmorRenderer`) crafted from `Dragon Scale`, plus **Dragonfire Mega TNT** (`TntVariant.DRAGONFIRE_MEGA`) honoring `allowDestructiveCustomTnt` (`P5-02`).
- **Why & Decisions:**
  1. **Dragonscale Gear (`ArtilianTiers.DRAGONSCALE`, `ArtilianArmorMaterials.DRAGONSCALE`):** Fire-resistant Netherite-plus tier (`2350` durability, `10.0F` mining speed, `5.0F` attack bonus, `26` enchantability) and armor (`26` total defense, `3.5F` toughness, `0.20F` knockback resistance). `DragonscaleSwordItem` ignites targets (`3s`) and applies `MobEffects.WEAKNESS` (`60` ticks) with `DRAGON_BREATH` + `FLAME` particles. Full 4-piece Dragonscale armor grants passive `Fire Resistance` + `Strength`.
  2. **Dragonfire Mega TNT (`TntVariant.DRAGONFIRE_MEGA`):** Crafted from `Dragon Scale` + `Blaze Powder` + `TNT`. Detonates with a `7.5F` blast radius, `DRAGON_BREATH` + `FLAME` + `LAVA` particles, `ENDER_DRAGON_GROWL` sound, and `16.0F` hostile-only blast damage (`KidSafeTargets.isValidOffensiveTarget`), while strictly enforcing `ArtilianConfig.isDestructiveTntAllowed()` and `SAFE_QUARRY_DAMAGE_CALCULATOR`.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Gear, Mega TNT, art (`make_ores_and_gear_art.py`, `make_tnt_art.py`), datagen & GameTests | Pending commit | `DragonscaleSwordItem`, `DragonscaleArmorRenderer`, `dragonscale_armor.geo.json`, `ArtilianTiers.DRAGONSCALE`, `ArtilianArmorMaterials.DRAGONSCALE`, `TntVariant.DRAGONFIRE_MEGA`, `ArtilianPrimedTnt#explodeDragonfireMega`, `check_armor_sheets.py` clean, datagen (`runData`), `build`, `DragonscaleGearAndMegaTntGameTests` + `TntSecurityProbes.p5_02` (`218/218` green). Red-proof: replaced `SAFE_QUARRY_DAMAGE_CALCULATOR` with `null` and forced `Level.ExplosionInteraction.TNT` in `explodeDragonfireMega` → `p5_02_dragonfiremegatnthonorsdestructiveconfigandkidsafetargets` & `p1_07` failed → restored green. |
  | 2 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of Dragonscale armor/weapons & Dragonfire Mega TNT (`qa/evidence/dragonscale-gear-mega-tnt-2026-11-22.png`). |

- **Tests & Red-proofs (`DragonscaleGearAndMegaTntGameTests` + `TntSecurityProbes`):** `218/218` green (6 new tests including `P5-02`). Red-proof recorded above.
- **Security Gate (`P5-02`):** `HELD` in `218/218` suite (`security/FINDINGS.md`).
- **Done When:** `check_armor_sheets.py`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies Dragonscale gear & Dragonfire Mega TNT in-game.

### Phase 5 — Stage 8: Apex Dragon Boss
- [ ] **In Progress (`222/222` GameTests Green & Strict Art Lint Clean; Live Client Check Pending)** — Phase 5 apex dragon boss **Embercrown Sovereign** (`artilian:embercrown_sovereign`, `Dragon / Fire`), **Primal Dragon Crest** (`BossSummonItem`), **Sovereign's Ember Heart** (`BossTrophyItem`), dedicated `256×128` GeckoLib boss model/glowmask, and `EmbercrownSovereignGameTests`.
- **Art Brief (`Embercrown Sovereign`):**
  - **Target height/scale:** ~`4.2` blocks tall at crown tips, ~`5.0` blocks nose-to-tail, ~`7.2` blocks wingspan (`EntityType` `2.6F × 3.4F`, `eyeHeight = 2.6F`, `shadowRadius = 1.8F`).
  - **Silhouette inspiration:** Towering armored 4-legged apex dragon sovereign with broad shoulder pauldrons, an inset **Primal Heart** chest core, arched S-curved neck, articulated lower `jaw`, a **5-horn Embercrown** crest, 2 colossal 3-segment dihedral wings, and a 3-segment war-tail ending in a glowing mace-blade.
  - **Dark structural anchor + accent palette:** Deep volcanic obsidian-charcoal scales (`#120C14` → `#5A3E4E`) anchored against forged Ember-Gold armor (`#601E0A` → `#F97316` → `#FDE047`) and Canopy Emerald Primal Heart core (`#166034` → `#22C55E` → `#4ADE80`).
  - **Key 3D recessed/angled features:** Inset eye sockets, articulated `jaw`, 5-horn angled Embercrown, broad shoulder pauldrons, inset Primal Heart chest core, 3-segment dihedral wings, and angled war-tail mace.
  - **`_glowmask.png` zones:** Emissive amber eyes, 5 Embercrown horn tips, Primal Heart chest core, wing-vein runes, and tail mace-blade (`AutoGlowingGeoLayer`).
- **Why & Decisions:**
  1. **Embercrown Sovereign (`EmbercrownSovereignEntity extends ArtilianBossEntity`):** `300.0` HP, `0.26` speed, `9.0` ATK, `11.0` Armor, `fireImmune() = true`, fall-immune with airborne glide damping, `PetElement.DRAGON` / `PetElement.FIRE`.
  2. **Three Kid-Readable Telegraphed Attacks & Primal Fury:** `EMBER_BREATH` (`40` ticks windup, `4.2` radius, `8.0F` dmg, ignites `3s`), `SOVEREIGN_ROAR` (`35` ticks windup, `5.5` radius, `7.5F` dmg, knockback + `Weakness`), and `METEOR_SLAM` (`50` ticks windup, `7.5` radius, `10.5F` dmg, ignites `4s`, zero block breaking). At `<= 50%` HP, enters **Primal Fury** (`PRIMAL_FURY` synched + NBT, boss bar turns `YELLOW` → `RED`, cooldown `60` → `40` ticks).
  3. **Altar Summon & Trophy (`Primal Dragon Crest` & `Sovereign's Ember Heart`):** `PRIMAL_DRAGON_CREST` (`BossSummonItem`, crafted from `Dragon Scale` + `Amber Moss` + `Blaze Powder` + `Gold Ingot`) summons `Embercrown Sovereign` on any `Boss Altar`. Defeating him drops `1` `Sovereign's Ember Heart` (in `artilian:boss_drops`), `3–6` `Dragon Scale`, `2–4` `Amber Moss`, and `1–2` `Starlight Crystal`.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Art Brief, `256×128` GeckoLib model, texture, glowmask, sprites & preview | Pending commit | `tools/art/make_embercrown_sovereign_art.py`, `lint_art.py --phase5 --strict` clean (`entity/embercrown_sovereign`), `preview_geo.py` (`qa/evidence/previews/embercrown_sovereign-preview.png`). |
  | 2 `EmbercrownSovereignEntity`, renderer, items, datagen & GameTests | Pending commit | `EmbercrownSovereignEntity`, `EmbercrownSovereignRenderer` (`AutoGlowingGeoLayer`), `PRIMAL_DRAGON_CREST`, `SOVEREIGNS_EMBER_HEART`, `EMBERCROWN_SOVEREIGN_SPAWN_EGG`, datagen (`runData`), `build`, and 4 `EmbercrownSovereignGameTests` (`222/222` green). Red-proof: flipped `EmbercrownSovereignEntity.fireImmune()` to `false` → `embercrownsovereignattributeselementstageggfireimmunityandloottable` failed → restored green. |
  | 3 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of Embercrown Sovereign & Boss Altar in `artilian:dragon_jungle` (`qa/evidence/embercrown-sovereign-2026-11-22.png`). |

- **Tests & Red-proofs (`EmbercrownSovereignGameTests`):** `222/222` green (4 new tests) + `python tools/art/lint_art.py --phase5 --strict` (exit `0`). Red-proof recorded above.
- **Security Gate:** `n/a` (inherits `P3-01`–`P3-03` `BossAltarBlockEntity` single-boss lock, zero block-breaking, and `ChesterEntity` immunity).
- **Done When:** `lint_art.py --phase5 --strict`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies Embercrown Sovereign in-game.

### Phase 5 — Stage 9: Twin-Star Endgame Rewards
- [ ] **In Progress (`226/226` GameTests Green; Live Client Check Pending)** — Combined endgame rewards uniting Alex's Eye Realm (`All-Seer's Lens`) and Steve's Dragon Jungle (`Sovereign's Ember Heart`): **Twin-Star Core**, **Twin-Star Blade**, **Twin-Star Harmony Staff**, and 4-piece **Twin-Star Armor**.
- **Why & Decisions:**
  1. **Twin-Star Core (`artilian:twin_star_core`):** Crafted from `All-Seer's Lens` + `Sovereign's Ember Heart` + `Starlight Crystal` + `Sunsteel Ingot` + `Dragon Scale`, yielding `2` cores per boss pair so Alex and Steve can both craft endgame rewards from one co-op victory.
  2. **Twin-Star Blade (`TwinStarBladeItem`, `ArtilianTiers.TWIN_STAR`):** Fire-resistant `Rarity.EPIC` sword (`2800` durability, `11.0F` speed, `6.0F` bonus damage, `30` enchantability). Ignites foes (`3s`), applies `Weakness` (`60` ticks) + `Glowing` (`80` ticks), and bursts `END_ROD`, `PORTAL`, and `DRAGON_BREATH` particles.
  3. **Twin-Star Harmony Staff (`WandSpell.TWIN_STAR_HARMONY`):** `50` Mana, `35` ticks cooldown. Heals & buffs nearby allies/Chester/pets (`4.0F` HP + `Regeneration II` + `Speed I`) while damaging (`10.0F` magic), illuminating (`Glowing`), and knocking back nearby hostiles (`KidSafeTargets`).
  4. **Twin-Star Armor (`ArtilianArmorMaterials.TWIN_STAR`, `TwinStarArmorRenderer`):** `30` total defense (`5/9/11/5`), `4.0F` toughness, `0.25F` knockback resistance, `30` enchantability, `twin_star_armor.geo.json` + `128×64` shaded sheet. Full 4-piece set grants passive `Fire Resistance`, `Strength`, `Night Vision`, and `Regeneration`.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Core, Blade, Harmony Staff, 4-piece Armor, art (`make_ores_and_gear_art.py`, `make_wand_art.py`), datagen & GameTests | Pending commit | `TwinStarBladeItem`, `TwinStarArmorRenderer`, `twin_star_armor.geo.json`, `ArtilianTiers.TWIN_STAR`, `ArtilianArmorMaterials.TWIN_STAR`, `WandSpell.TWIN_STAR_HARMONY`, `ManaService#castTwinStarHarmonyStaff`, `check_armor_sheets.py` clean, datagen (`runData`), `build`, and 4 `TwinStarEndgameGameTests` (`226/226` green). Red-proof: commented out `MobEffects.NIGHT_VISION` in `ArtilianArmorItem.applySetBonus` → `twinstarfullarmorsetgrantsallfourendgamebuffs` failed → restored green. |
  | 2 Live client verification & evidence | Pending | `./gradlew runClient` screenshot of Twin-Star Blade, Harmony Staff, and Armor (`qa/evidence/twin-star-endgame-2026-11-22.png`). |

- **Tests & Red-proofs (`TwinStarEndgameGameTests`):** `226/226` green (4 new tests). Red-proof recorded above.
- **Security Gate:** `n/a` (endgame crafting, armor buffs, and `KidSafeTargets`-filtered staff).
- **Done When:** `check_armor_sheets.py`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` verifies Twin-Star endgame rewards in-game.

### Phase 5 — Stage 10: Phase 5 Art Pass & Finale
- [x] **Complete — Locked in PR #17 (`v0.2.0`, `229/229` GameTests & Strict Art Lint Clean; Live Client Check Pending)** — Phase 5 art & preview audit (`--phase5` in `lint_art.py` including `PHASE5_ARMORS`), bespoke `128×128` 3D `dragonscale_armor` & `twin_star_armor` + `_glowmask.png` layers (`make_phase5_armors_art.py`), end-to-end Dragon Jungle & 5-phase finale sign-off tests (`Phase5SignOffGameTests`), and `mod_version=0.2.0` release JAR (`build/libs/adventures-in-artilian-0.2.0.jar`).
- **Why & Decisions:** Ensure every Phase 5 dragon, pet dragonling, boss, armor set, block, item, and dimension meets `DESIGN.md`'s 3D Art Standard and plays cleanly end-to-end with kid-safe defaults intact:
  1. **Phase 5 Art Linter (`tools/art/lint_art.py --phase5 --strict`):** Added `PHASE5_ARMORS` (`dragonscale_armor`, `twin_star_armor`) alongside all 7 Phase 5 entities (`canopy_wyvern`, `amber_wyvern`, `storm_glidewing`, `canopy_dragonling`, `amber_dragonling`, `storm_dragonling`, `embercrown_sovereign`) and verified zero UV overlaps, zero transparent holes, and zero flat single-color faces, plus 4-angle `qa/evidence/previews/*-preview.png` contact sheets for both Phase 5 armors.
  2. **Phase 5 Sign-Off & Finale GameTests (`Phase5SignOffGameTests`):** Verifies all 7 Phase 5 GeckoLib entity models, required bones, animation clips, minimum sheet sizes (`256×128` wild dragons/boss, `128×128` dragonlings), `_glowmask.png` sheets, both Phase 5 armor models/sheets, animated `primal_portal.png` + `.mcmeta`, all 9 dragon egg stage textures, Phase 5 block/item textures, `EmbercrownSovereignEntity` telegraph windup floor (`>= 30` ticks), Phase 5 crafting recipes, `DragonJungleDimension` registration, Phase 5 loot tables, and cross-phase finale integration (`allowDestructiveCustomTnt = false`, `Chester` attachment, both `EyeRealmDimension` and `DragonJungleDimension` registered, `twin_star_core` recipe).
  3. **Release Version Bump (`0.2.0`):** Updated `mod_version=0.2.0` in `gradle.properties` and built `build/libs/adventures-in-artilian-0.2.0.jar`.
- **Sub-stages:**

  | Sub-stage | Commit | Summary & Verification |
  |---|---|---|
  | 1 Phase 5 art audit, armor previews, `Phase5SignOffGameTests` & `0.2.0` JAR | Pending commit | Updated `tools/art/lint_art.py` (`PHASE5_ARMORS`), generated `dragonscale_armor-preview.png` & `twin_star_armor-preview.png`, added `Phase5SignOffGameTests` (`229/229` green), bumped `mod_version=0.2.0`, `lint_art.py --phase5 --strict` (exit `0`), `check_armor_sheets.py` clean, `runData`, and `build` (`adventures-in-artilian-0.2.0.jar`). |
  | 2 Live client & family co-op finale sign-off | Pending | `./gradlew runClient` & LAN co-op walkthrough of Eye Realm + Dragon Jungle + Twin-Star Endgame (`qa/evidence/phase5-finale-2026-11-22.png`). |

- **Tests & Red-proofs (`Phase5SignOffGameTests`):** `229/229` green (3 new tests) + `python tools/art/lint_art.py --phase5 --strict` (exit `0`).
- **Security Gate:** `n/a` (art pass, linter, version bump, and finale sign-off; `P1-01`–`P5-01` already `HELD`).
- **Done When:** `lint_art.py --phase5 --strict`, `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, and live `./gradlew runClient` / LAN co-op verifies the full 5-phase finale.

---

## Security Gates Summary (`P1-01`–`P3-04`)

All probes are red-proven (`EXPLOITED` with control off, `HELD` with control on) and logged in `security/FINDINGS.md`. Current suite: **`162/162`** GameTests passing.

| IDs | Phase / Stage | Area | GameTest Class | Index |
|---|---|---|---|---|
| `P1-01`–`P1-06` | Phase 1 Stage 2 | Chester ownership, death safety, anti-duplication, hopper immunity | `ChesterSecurityProbes` | `security/chester/README.md` |
| `P1-07`–`P1-10` | Phase 1 Stage 4 | `allowDestructiveCustomTnt` default off, Cluster Quarry & safe TNTs | `TntSecurityProbes` | `security/tnt/README.md` |
| `P2-01`–`P2-04` | Phase 2 Stages 3–4 | Pet crystal ownership, non-lethal fainting, anti-duplication, battle safety | `PetSecurityProbes` | `security/pets/README.md` |
| `P2-05` | Phase 2 Stage 6C | Sunbeam chest `minecraft:light` air-only placement & removal cleanup | `PetRosterGameTests` | `security/FINDINGS.md` |
| `P3-01`–`P3-03` | Phase 3 Stages 1–3 | Boss Altar anti-spam & Crystal Colossus / Captain Brimstone non-griefing / Chester safety | `BossSecurityProbes` | `security/bosses/README.md` |

---

## Open & Parked

- **Exact pet & dragon count per biome:** expand after core rosters are tested and fun for Alex & Steve.
- **Open-licensed creature assets:** not yet searched; worth a look in Phase 3 Stages 2–3 if agent-built bosses look too rough.
- **Restoring Phase 1 review notes (optional):** PR #2 (`d9ee782`) dropped early Phase 1 review notes from `qa/FINDINGS.md` on `main`; restorable from git history if ever needed.
