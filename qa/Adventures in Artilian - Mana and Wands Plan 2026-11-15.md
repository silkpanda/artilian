# Adventures in Artilian — Mana and Wands Plan (2026-11-15)

<!-- PLAN.md Phase 1 Stage 5. One PLAN stage, one PR, one jar. The numbered stages below are
commits inside that PR. -->

> **Status: All 4 stages built and verified locally, in PR.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Mana attachment, regen, sync, items, datagen & art | `28f8426` — `ManaData` (`100` max, `20`t post-cast delay, `+5` every `10`t), `ArtilianAttachments#MANA` (`copyOnDeath()`), `ManaSyncPayload` (`artilian:mana_sync`), `WandSpell`, `ArtilianWandItem`, `ManaService`, `ManaEvents` (`LevelTickEvent.Post`), `SPARK_WAND`/`HEART_BLOOM_STAFF`/`ZEPHYR_DASH_WAND`, `tools/art/make_wand_art.py`, datagen, `43/43` tests. Red-proof: skipped `REGEN_AMOUNT` addition in `ManaData#tickServer` -> `manaattachmentpersistsregeneratesandsyncstoclient` failed (`expected >= 65, got 60`); restored green (`43/43`). |
> | 2 Spark, Heart-Bloom & Zephyr Dash spells | `261d7c8` — `ManaService` spells (`castSparkWand` 16-block beam + 6.0F primary + up to 2 4.5-block chains, `castHeartBloomStaff` 8.0-block 6.0F heal + Regen II + Absorption + cleanse + extinguish, `castZephyrDashWand` dash + Slow Falling + Speed II + 4.0-block hostile repel) & kid-safe `isValidOffensiveTarget`/`isValidHealingTarget`, `46/46` tests. Red-proofs: (1) omitted Player/Chester exclusion in `isValidOffensiveTarget` -> `sparkwandzapshostileswithchainsparkwhilesparingplayerschesterandpets` and `zephyrdashwandpropelscasterwithslowfallingandrepelshostileswithoutmovingsleepingchester` failed; (2) omitted `ally.heal(6.0F)` -> `heartbloomstaffhealsandshieldsplayerschesterandpetswhileignoringhostiles` failed; (3) omitted `MobEffects.SLOW_FALLING` -> `zephyrdashwandpropelscasterwithslowfallingandrepelshostileswithoutmovingsleepingchester` failed; restored green (`46/46`). |
> | 3 HUD Mana Bar (`ManaHudOverlay`) | `58d3b70` — `ManaHudOverlay` (`81x11` bar above right hotbar, `#2B2338`/`#3A2F4D` panel, `#6A5A8C` border, `#47B8FF` -> `#9C5CFF` gradient fill, `#FFF6D6` readout, shifts up when underwater/mounted) & `RegisterGuiLayersEvent` registration in `ArtilianClient`, `./gradlew build` + `46/46` `runGameTestServer`. |
> | 4 Client verification, docs & PR | Done in Stage 4 commit — temporary `./gradlew runClient` showcase captured in `qa/evidence/mana-and-wands-showcase-2026-11-15.png` (Survival HUD showing `80 / 100` gradient Mana bar above hunger, Spark Wand particles, and all 3 wands/staves in hotbar & glow item frames; clean `run/logs/latest.log`; reverted temporary `ArtilianClient` harness), updated `PLAN.md`, verified `runData`, `./gradlew build`, & `46/46` `runGameTestServer`, pushed `phase-1-stage-5-mana-and-wands` and opened stacked PR. |

## Why

Alice and Bob want accessible magic without frustrating consumable scroll ammo (`PRODUCT.md`). Today players have melee gear and custom TNTs, but no Mana resource, no HUD Mana bar, and no Wands or Staves. This stage adds an auto-regenerating per-player Mana pool synced to a compact `DESIGN.md` HUD bar and three starter spell items: **Spark Wand**, **Heart-Bloom Staff**, and **Zephyr Dash Wand**.

## Decisions

1. **Auto-regenerating Mana (`artilian:mana`) with `copyOnDeath()`.** Starts at `100 / 100` (`DEFAULT_MAX_MANA = 100`). Casting starts a `20`-tick (`1.0` second) pause before regenerating `5` Mana every `10` ticks (`0.5` seconds), reaching full from empty in `11` seconds with zero consumables needed. Respawning restores full Mana and syncs immediately.
2. **Server-authoritative Mana & client HUD sync (`ManaSyncPayload`).** All Mana checks, deductions, and cooldowns run on the server in `ManaService`; client `use()` only previews validity while `ManaSyncPayload` (`artilian:mana_sync`) updates the client player's `ManaData` on login, respawn, dimension change, cast, and regen tick.
3. **Kid-safe targeting across every spell:**
   - **Spark Wand** (`20` Mana, `10`-tick cooldown): fires a `16`-block starlight spark beam (`1.75`-block ray radius aim assist) dealing `6.0F` magic damage + `Glowing` (`60` ticks) + `Slowness I` (`40` ticks) and chaining to up to `2` nearby hostiles within `4.5` blocks for `4.0F` magic damage + `Glowing`. Strictly spares Players, `ChesterEntity`, tamed/owned pets, villagers, and baby passive animals.
   - **Heart-Bloom Staff** (`30` Mana, `20`-tick cooldown): emits an `8.0`-block emerald blossom burst healing `6.0F` HP, granting `Regeneration II` (`100` ticks) + `Absorption I` (`200` ticks), clearing `Poison`/`Wither`/`Slowness`/`Weakness`, and extinguishing fire on the caster, nearby Players, `ChesterEntity`, tamed pets, and passive animals while ignoring hostile monsters.
   - **Zephyr Dash Wand** (`25` Mana, `15`-tick cooldown): launches the caster forward/upward (`1.45` horizontal look scale, `max(0.55, look.y * 0.9 + 0.35)` lift), resets `fallDistance`, grants `Slow Falling` (`100` ticks) + `Speed II` (`100` ticks), and repels nearby hostile mobs within `4.0` blocks while leaving Players, `ChesterEntity` (sleeping or awake), and tamed pets untouched.
4. **Compact `DESIGN.md` HUD Mana Bar (`ManaHudOverlay`).** Renders cleanly above the right side of the hotbar (shifting up if underwater air bubbles are visible) with `#2B2338` / `#3A2F4D` backing, `#6A5A8C` trim, `#47B8FF` → `#9C5CFF` gradient fill, and `#FFF6D6` `mana / maxMana` readout.

## Stages

### Stage 1: Mana attachment, regeneration, network sync, wand items, datagen & art
- **Changes:** `ManaData`, `ArtilianAttachments#MANA`, `ManaSyncPayload` + payload registration in `ArtilianMod`, `WandSpell`, `ArtilianWandItem`, `ManaService` (Mana consumption, regen tick, sync), `ManaEvents`, `ArtilianItems` (`SPARK_WAND`, `HEART_BLOOM_STAFF`, `ZEPHYR_DASH_WAND`), `ArtilianCreativeTabs`, `tools/art/make_wand_art.py`, datagen (`ArtilianItemModelProvider`, `ArtilianRecipeProvider`, `ArtilianLanguageProvider`).
- **Tests, written first and seen failing:** `ManaAndWandsGameTests`: `manaAttachmentPersistsRegeneratesAndSyncsToClient`, `wandRecipesExistAndInsufficientManaBlocksCast`.
- **Red-proof:** temporarily disable `REGEN_AMOUNT` addition in `ManaData#tickServer` and confirm `manaAttachmentPersistsRegeneratesAndSyncsToClient` fails; restore and confirm green.
- **Done When:** `runData`, `build`, and `runGameTestServer` pass with Stage 1 tests green.

### Stage 2: Spark Wand, Heart-Bloom Staff & Zephyr Dash Wand spell mechanics
- **Changes:** `ManaService` spell implementations (`castSparkWand`, `castHeartBloomStaff`, `castZephyrDashWand`) and shared ally/offensive target predicates (`isValidOffensiveTarget`, `isValidHealingTarget`).
- **Tests, written first and seen failing:** `ManaAndWandsGameTests`:
  - `sparkWandZapsHostilesWithChainSparkWhileSparingPlayersChesterAndPets`
  - `heartBloomStaffHealsAndShieldsPlayersChesterAndPetsWhileIgnoringHostiles`
  - `zephyrDashWandPropelsCasterWithSlowFallingAndRepelsHostilesWithoutMovingSleepingChester`
- **Red-proof:**
  1. Bypass Player/Chester exclusion in `isValidOffensiveTarget` -> confirm Spark Wand test fails -> restore.
  2. Omit `ally.heal(6.0F)` in `castHeartBloomStaff` -> confirm Heart-Bloom test fails -> restore.
  3. Omit `MobEffects.SLOW_FALLING` in `castZephyrDashWand` -> confirm Zephyr Dash test fails -> restore.
- **Done When:** `build` and `runGameTestServer` pass with all Stage 2 tests green.

### Stage 3: HUD Mana Bar (`ManaHudOverlay`)
- **Changes:** `ManaHudOverlay` and `RegisterGuiLayersEvent` wiring in `ArtilianClient`.
- **Tests / checks:** `./gradlew build` and `./gradlew runGameTestServer` remain green.
- **Done When:** HUD Mana Bar compiles cleanly against `RegisterGuiLayersEvent` and passes build + GameTest server.

### Stage 4: Client verification, docs & PR
- **Changes:** Verify all 3 Wands/Staves and the HUD Mana Bar in `./gradlew runClient` (zero missing-texture/model errors), save `qa/evidence/mana-and-wands-showcase-2026-11-15.png`, update `PLAN.md` (mark Stage 5 complete & update **Now**), update this plan status table, run `runData`/`build`/`runGameTestServer`, push branch, and open stacked PR on `phase-1-stage-4-custom-tnts`.
- **Done When:** `runData` leaves `src/generated` clean, `build` and `runGameTestServer` pass (`46/46`), client evidence is saved, and the PR is open.

## Security & Kid-Safety

No new config toggle or container ownership boundary is added here, so `security/FINDINGS.md` probes are n/a; however, `ManaService` enforces two strict safety invariants red-proven in `ManaAndWandsGameTests`:
- Server-authoritative Mana checks (`tryConsumeMana`) prevent casting without sufficient Mana and sync authoritative values via `ManaSyncPayload`.
- Ally-safe target filtering (`isValidOffensiveTarget`, `isValidHealingTarget`) guarantees offensive wands and knockback gusts never harm or displace Players, `ChesterEntity`, or tamed pets.

## Out of scope

- Tier 2/3 Staves and mana-boosting armor/trinkets (arrive in Phase 3).
- Mana potions or consumable mana scrolls (`PRODUCT.md` specifies auto-regenerating Mana without scroll ammo).
