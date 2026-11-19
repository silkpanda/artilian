# Plan: Adventures in Artilian

Written 2026-11-15; consolidated 2026-11-18 into a single phased and staged plan. **This is the single authoritative plan for the repository**: every phase, stage, sub-stage status table, design decision, GameTest, red-proof, security gate, and verification evidence path lives here.

---

## Now

*Updated 2026-11-19 by Antigravity. A session handoff, not history: every session rewrites this section before it ends (see AGENTS.md). Read it first.*

| | |
|---|---|
| **In progress** | **Phase 3 — Stage 3 (*Captain Brimstone*)** complete (`153/153` GameTests, `runData`, `build`, and live `./gradlew runClient` screenshots verified) on branch `phase-3-stage-3-captain-brimstone`, ready for PR into `main`. |
| **Next** | 1. Push `phase-3-stage-3-captain-brimstone` and open/merge PR into `main` once CI is green. 2. Implement `TD-01` (`tools/art/art_lib.py`, `lint_art.py`, `preview_geo.py`) or begin **Phase 3 — Stage 4 (*Tier 3 Arsenal*)**. 3. When home, owner checks **Boss Framework** (*Boss Altar* + *Training Golem*) in `./gradlew runClient` and runs family LAN co-op playtest. |
| **Waiting on the owner** | 1. In-game look & screenshot when home for **Boss Framework** (*Training Golem* + *Boss Altar*, `qa/evidence/`). 2. The kids' official names for **Prismling**, **Flurrywing**, **Frogduck**, and **Zapkin** (placeholders; **Sunbeam** is final; only `en_us` display names change). 3. Steve's official names for **Shadow Wisp** and **Cinder Crag**. 4. Family LAN co-op playtest to sign off Phase 1 Stage 6 and Phase 2. 5. Confirm the `PERSONAL_INFO_BLOCKLIST` GitHub Actions secret is set. |
| **Other work in flight** | None (`main` and `phase-3-stage-3-captain-brimstone` only). |
| **Loose ends** | `runData` rewrites `src/generated/**` with LF endings and no content change; commit only files with a real diff (`git diff --ignore-cr-at-eol`). `python tools/art/make_pets_art.py` regenerates `crystal_fox.png` over its hand-touched art-pass version; restore it with `git checkout` after running the script, or pass `--prismling` / `--sunbeam` / `--frogduck` / `--zapkin` to paint a single pet. |

---

## Where It Stands

Sizes are ballparks in evenings of work, corrected as stages finish.

| Phase | Description | Stages | Rough size | Status |
|---|---|---|---|---|
| 0 | Project prep: folder, Git repo, 5-doc stack, registers, GitHub repo & CI | n/a | n/a | ✅ Done |
| 1 | NeoForge `1.21.1` baseline, Chester, starter gear, custom TNTs, Wands & Staves | 6 | 4–6 weeks | ⏳ Waiting on LAN co-op sign-off |
| 2 | Overworld biomes (`TerraBlender`), pet capture & battle, kids' roster, structures & enemies | 9 | 2–3 months | ⏳ Art signed off; LAN co-op & official names pending |
| 3 | Custom bosses, rematch altars, Tier 3 gear, advanced Staves/TNTs | 5 | 6–8 weeks | ⏳ In progress (Stages 1–2 code complete) |
| 4 | Alex's Dimension: The Eye Realm | 7 | 2–3 months | ⏹ Pending |
| 5 | Steve's Dimension: Giant Dragon Jungle & Grand Finale | 10 | 3–4 months | ⏹ Pending |

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
  - Three stacked snowballs with coal buttons, turquoise beanie, asymmetric stick & mitten arms, 4 butterfly frost wings (`tools/art/models/flurrywing.bbmodel`), hover damping + zero fall damage, `SNOWFLAKE` particles.
  - `109/109` green; red-proofs: `causeFallDamage` → `super`; hitbox shrunk to `0.9 x 1.8`. Evidence: `qa/evidence/flurrywing-2026-11-17.png`.
- **6C — Sunbeam (`artilian:sunbeam`, `[x] Complete` — Stages 1–2 + v2 Art in PR #7; art signed off 2026-11-19):**
  - Second legendary pet (**Sunbeam** is the owner's final name). Adds 12th `PetElement.SUNSTEEL` (`0xFBBF24`, strong vs `SHADOW`/`ICE`/`DRAGON`, resisted by `FIRE`/`EARTH`, weak to `WATER`/`ELECTRIC`, applies `GLOWING` 60t + `WEAKNESS` 60t).
  - Ghast-tall (`3.0 x 4.0`, eye height `3.3`, 30 HP, 5.0 ATK, hover, rare weight `1` in Emberbloom Meadows).
  - **Sun Aura:** places one level-15 `minecraft:light` block at chest only in **air** (NBT-persisted `SunbeamPlacedLight`, cleared on move/unload/recall/faint/remove) + heals owner and owner's pets within 8 blocks (`REGENERATION I` + `1` HP every 40t; never others).
  - **Security Gate `P2-05` (`PetRosterGameTests.sunbeamLightOnlyReplacesAirAndIsRemovedWithIt`):** EXPLOITED when air check / NBT light save / `remove()` cleanup disabled; HELD (`security/FINDINGS.md`). Also red-proofed owner-only aura heal & `WATER` vs `SUNSTEEL` matchup.
  - **v2 Art Pass (`4db7114`):** `make_pets_art.py --sunbeam` generates `sunbeam.geo.json`, `sunbeam.png`, `sunbeam_glowmask.png` (armored guardian visor, angled solar eyes, forehead sun-diamond, 5-ray solar crown, 4 spiky wing-arms, attack-only `chest_beam`). Commits `32d67fd`, `4db7114`.
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
| 4 | Tier 3 Arsenal (weapons, armor, tools, advanced Staves & Boss TNTs) | ⏹ Pending |
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
  | 1 Tier 3 Prismatic gear, 3D armor & tests | Pending | `ArtilianTiers.PRISMATIC`, `ArtilianArmorMaterials.PRISMATIC`, `PrismaticSwordItem`, `PrismaticArmorRenderer`, 9 items, `make_ores_and_gear_art.py`, datagen, `Tier3ArsenalGameTests`. |
  | 2 Advanced Staves, Boss TNTs & `P3-04` | Pending | `PRISM_NOVA` & `BRIMSTONE_METEOR` staves, `STARLIGHT_GRAVITY` & `BRIMSTONE_METEOR` TNTs, `make_wand_art.py`, `make_tnt_art.py`, `TntSecurityProbes` `P3-04`, `security/FINDINGS.md`. |
  | 3 Live client check & evidence | Pending | `./gradlew runClient` screenshot of Prismatic gear, Staves, and Boss TNTs (`qa/evidence/tier3-arsenal-2026-11-19.png`). |

- **Tests & Red-proofs (`Tier3ArsenalGameTests`, `TntSecurityProbes`):** Planned for Sub-stages 1–2.
- **Security Gate (`TntSecurityProbes.p3_04_tier3StavesAndBrimstoneTntRespectKidSafeAndDestructiveConfig`, `security/tnt/README.md`, `security/FINDINGS.md`):** Planned for Sub-stage 2.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` are green locally & in CI, `check_armor_sheets.py` passes, and live `./gradlew runClient` verifies Tier 3 gear, Staves, and Boss TNTs.

### Phase 3 — Stage 5: Phase 3 Art Pass & Sign-Off
- [ ] **Pending** — Art pass over Phase 3 bosses, altars, and Tier 3 gear; done when all checks are green and both bosses are summonable, beatable by a young kid with a parent, and drop working Tier 3 gear.

---

## Phase 4: The Eye Realm (Alex's Dimension)

- **Status:** ⏹ Pending
- **Done When:** All checks green, dimension and portal GameTests pass, and the Eye Realm loop is playable end to end.

| Stage | Name | Scope | Status |
|---|---|---|---|
| 1 | Dimension & Portal | `artilian:eye_realm` dimension and Gazing Portal with flat placeholder terrain | [ ] Pending |
| 2 | Terrain & Sky | Eye-covered blocks, trees, terrain generation, blinking skybox | [ ] Pending |
| 3 | Eye Realm Enemies | **Peepers**, **Oculus Slimes**, **Lens Guardians** | [ ] Pending |
| 4 | Blinky Pet | **Blinky** capturable pet (`ArtilianPetEntity`) | [ ] Pending |
| 5 | Eye Realm Structures | Dimension structures & chests | [ ] Pending |
| 6 | The Great All-Seer | Phase 4 boss (`ArtilianBossEntity`) with telegraphed attacks & rematch altar | [ ] Pending |
| 7 | Phase 4 Art Pass | Final art polish against Alex's specs + co-op verification | [ ] Pending |

---

## Phase 5: Giant Dragon Jungle (Steve's Dimension) & Grand Finale

- **Status:** ⏹ Pending
- **Done When:** All checks green, dimension and dragon GameTests pass, and both kids can clear the full Eye Realm and Dragon Jungle finale.

| Stage | Name | Scope | Status |
|---|---|---|---|
| 1 | Dimension & Portal | `artilian:dragon_jungle` dimension and Primal Portal | [ ] Pending |
| 2 | Mega-Jungle Worldgen | Prehistoric mega-jungle biomes, trees, and terrain | [ ] Pending |
| 3 | Dragon Jungle Structures | **Dragon Roost** and **Amber Temple** | [ ] Pending |
| 4 | First Dragon Species | One wild dragon end to end (establishes shared GeckoLib dragon rig) | [ ] Pending |
| 5 | More Dragon Species | Additional wild dragon species reusing the shared dragon rig | [ ] Pending |
| 6 | Hatchable Pet Dragons | Hatchable dragon companions (`ArtilianPetEntity`) | [ ] Pending |
| 7 | Dragonscale Gear & Mega TNT | Dragonscale armor/weapons/tools & **Dragonfire Mega TNT** (honors `allowDestructiveCustomTnt`) | [ ] Pending |
| 8 | Apex Dragon Boss | Phase 5 apex dragon boss (`ArtilianBossEntity`) with telegraphs & altar | [ ] Pending |
| 9 | Twin-Star Endgame Rewards | Combined endgame rewards uniting Alex's and Steve's realms | [ ] Pending |
| 10 | Phase 5 Art Pass & Finale | Final art polish & full family co-op finale verification | [ ] Pending |

---

## Security Gates Summary (`P1-01`–`P3-03`)

All probes are red-proven (`EXPLOITED` with control off, `HELD` with control on) and logged in `security/FINDINGS.md`. Current suite: **`153/153`** GameTests passing.

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
