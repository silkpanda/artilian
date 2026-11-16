# Adventures in Artilian — Ores and Starter Gear Plan (2026-11-15)

<!-- PLAN.md Phase 1 Stage 3. One PLAN stage, one PR, one jar. The numbered stages below are
commits inside that PR. -->

> **Status: All 4 stages complete and locked up (PR #5).** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Ores, materials, loot & mining tiers | `9936aae` (+ Starlight glow follow-up) — Sunsteel & Starlight ores (stone + deepslate, `lightLevel 10`), Starlight Crystal Block (`lightLevel 14`), Sunsteel Block, materials, `artilian:main` Creative Tab, datagen, `28/28` tests. Red-proof: removed `STARLIGHT_CRYSTAL_ORE` from `BlockTags.NEEDS_IRON_TOOL` -> `starlightorerequiresironpickaxeandsilktouchworks` failed; asserted `lightLevel 10`/`14` before raising from `5`/`7` -> `starlightorerequiresironpickaxeandsilktouchworks` failed; restored green. |
> | 2 Worldgen | `51b5cd6` — `ArtilianWorldGen`, `ArtilianWorldGenProvider`, Overworld `BiomeModifier`s, `worldgen.nbt`, `29/29` tests. Red-proof: omitted `ADD_STARLIGHT_CRYSTAL_ORE` -> `worldgenfeaturesplacestoneanddeepslateores` failed; restored green. |
> | 3 Weapons, tools & armor | `2629550` (+ GeckoLib armor follow-up) — `ArtilianTiers`, `ArtilianArmorMaterials`, `ArtilianSwordItem`, `ArtilianArmorItem` (`GeoItem` with `SunsteelArmorRenderer` / `StarlightArmorRenderer` extending `ArtilianArmorRenderer` via `DefaultedItemGeoModel` at `geo/item/armor/<set>_armor.geo.json` and `textures/item/armor/<set>_armor.png`, backed by 3D GeckoLib `1.12.0` armor models), 18 Sunsteel & Starlight gear items, datagen, `32/32` tests. Red-proof: switched `ArtilianTiers.SUNSTEEL` to `BlockTags.INCORRECT_FOR_STONE_TOOL` -> `sunsteelpickaxeminesstarlightandstarlightminesobsidian` failed; restored green. |
> | 4 Placeholder art, client check, docs & PR | `f1fe32f` + visual evidence + `78ca95f` + Starlight ore constellation art + `5e6e04f` + `d9922fb` + sun-cluster Sunsteel ore + `915032d` + refined-sun Raw Sunsteel + visual evidence refresh — `tools/art/make_ores_and_gear_art.py` (including 3D GeckoLib `sunsteel_armor.geo.json` / `starlight_armor.geo.json` models and `64x64` `textures/item/armor/sunsteel_armor.png` / `textures/item/armor/starlight_armor.png` sheets featuring Sunsteel Solar Plume Crest, Swept Sun-Wings, Solar Core Emblem, Layered Pauldrons, Knee Poleyns & Sabaton Wings, plus Starlight Brow Gem Spire, Crystal Spire Horns, Starlight Core Crystal, Crystal Epaulettes & Cyan Crystal Ankle-Fins; miniature 8-rayed radiant sun ore clusters embedded in stone/deepslate for `sunsteel_ore` and `deepslate_sunsteel_ore`; 4-pointed star constellation Starlight ores/crystal block with Astral Violet/Indigo sockets and warm white-gold star cores; refined 8-rayed sun sprite for `raw_sunsteel` pairing with the multifaceted 4-pointed star crystal `starlight_crystal`; and exact vanilla 1.21.1 silhouettes for `sunsteel_ingot` and all 10 `sunsteel_*` and `starlight_*` `sword`, `pickaxe`, `axe`, `shovel`, and `hoe` items in their matching ore palettes), updated plan & `PLAN.md` **Now**, PR #5; verified `runData` (clean `src/generated`), `./gradlew build`, `runGameTestServer` (`32/32`), and refreshed `./gradlew runClient` visual evidence (`qa/evidence/ores-and-gear-showcase-2026-11-15.png`, `qa/evidence/starlight-glow-night-2026-11-15.png`, `qa/evidence/player-armor-inventory-2026-11-15.png`, `qa/evidence/creative-tab-artilian-2026-11-15.png`) showing both 3D GeckoLib armor sets on armor stands and player inventory plus all updated ore/item sprites with zero missing-texture or model errors. |

## Why

Bob asked for custom weapons, armor, and tools, and both kids need an inviting, readable progression loop in survival before custom biomes and bosses arrive. Right now there are no Artilian ores in the world and no starter gear to craft. This feature introduces Artilian's two signature materials from `DESIGN.md` — **Sunsteel** (`#FF9F38` Sunsteel Gold) and **Starlight Crystal** (`#38D9C0` Starlight Teal) — with clear mining tiers, Overworld ore generation, and full starter sets of tools, weapons, and armor.

## Decisions

1. **Two-step starter progression.** **Sunsteel** sits cleanly between Iron and Diamond (stone pickaxe or better to mine, faster and tougher than Iron with better enchantability). **Starlight Crystal** sits at Diamond-grade power (iron pickaxe or better to mine, mines Obsidian, higher speed/durability/enchantability).
2. **Kid-friendly visibility.** Starlight Crystal Ore and Deepslate Starlight Crystal Ore (`lightLevel 10`) and Starlight Crystal Block (`lightLevel 14`) emit a bright torch-level teal glow underground so young kids can spot them easily while caving.
3. **One Creative Mode Tab (`artilian:main`).** Groups the Chester Whistle, ores, blocks, materials, weapons, tools, and armor in one easy-to-find tab ("Adventures in Artilian") in addition to standard crafting/smelting recipes.
4. **Placeholder art first.** Crisp programmer-art textures matching the `DESIGN.md` palette (including 4-pointed star constellation Starlight Crystal ores with Astral Violet/Indigo sockets and warm white-gold star cores so they read distinctly from vanilla Diamond Ore) are generated via `tools/art/make_ores_and_gear_art.py` and revisited in the Phase 1 Stage 6 art pass.

## Stages

### Stage 1: Ores, materials, loot & mining-tier rules
- **Changes:**
  - `ArtilianBlocks`: `sunsteel_ore`, `deepslate_sunsteel_ore`, `sunsteel_block`, `starlight_crystal_ore`, `deepslate_starlight_crystal_ore`, `starlight_crystal_block` (with `BlockItem`s in `ArtilianItems`).
  - `ArtilianItems`: `raw_sunsteel`, `sunsteel_ingot`, `starlight_crystal`.
  - `ArtilianCreativeTabs`: `artilian:main` Creative Mode Tab wired in `ArtilianMod`.
  - Datagen: `ArtilianBlockStateProvider`, `ArtilianItemModelProvider`, `ArtilianBlockTagsProvider`, `ArtilianItemTagsProvider`, `ArtilianLootTableProvider` (`ArtilianBlockLootSubProvider`), `ArtilianRecipeProvider` (smelting, blasting, 9-block storage recipes), `ArtilianLanguageProvider`.
- **Tests, written first and seen failing:** `OresAndGearGameTests`: `sunsteelOreDropsRawSunsteelWithStonePickaxe`, `starlightOreRequiresIronPickaxeAndSilkTouchWorks`, `smeltingAndStorageRecipesExist`.
- **Red-proof:** temporarily remove `STARLIGHT_CRYSTAL_ORE` from `BlockTags.NEEDS_IRON_TOOL` and confirm `starlightOreRequiresIronPickaxeAndSilkTouchWorks` goes red; restore and confirm green.
- **Done When:** `runData` produces clean loot/tags/models/recipes, `build` passes, and all Stage 1 GameTests + existing tests are green.

### Stage 2: Worldgen
- **Changes:**
  - `ArtilianWorldGen`: `ConfiguredFeature`s (`artilian:ore_sunsteel` size 9, `artilian:ore_starlight_crystal` size 6) targeting stone and deepslate replaceables, `PlacedFeature`s (`artilian:ore_sunsteel_placed` count 12 from Y `-24` to `72`, `artilian:ore_starlight_crystal_placed` count 7 from Y `-64` to `24`), and NeoForge `BiomeModifier`s adding both to `#minecraft:is_overworld` under `UNDERGROUND_ORES`.
  - `ArtilianWorldGenProvider` (`DatapackBuiltinEntriesProvider`) wired into `ArtilianDataGen`, plus a 7x7x7 `worldgen` empty template in `GameTestStructureProvider`.
- **Tests, written first and seen failing:** `OresAndGearGameTests.worldgenFeaturesPlaceStoneAndDeepslateOres` checks Overworld biome `UNDERGROUND_ORES` registration and invokes `ConfiguredFeature#place` in a stone+deepslate volume to verify both stone and deepslate ore variants generate.
- **Red-proof:** temporarily omit `ADD_STARLIGHT_CRYSTAL_ORE` from the biome modifier bootstrap, re-run `runData` + `runGameTestServer`, confirm `worldgenFeaturesPlaceStoneAndDeepslateOres` fails; restore and confirm green.
- **Done When:** `runData`, `build`, and `runGameTestServer` pass with both ores confirmed in Overworld generation.

### Stage 3: Sunsteel & Starlight weapons, tools, and armor
- **Changes:**
  - `ArtilianTiers`: `SUNSTEEL` and `STARLIGHT` `SimpleTier`s.
  - `ArtilianArmorMaterials`: `DeferredRegister<ArmorMaterial>` wired in `ArtilianMod` with `SUNSTEEL` (total 17 defense, toughness 1.0, repair with `sunsteel_ingot`) and `STARLIGHT` (total 20 defense, toughness 2.0, knockback resistance 0.05, repair with `starlight_crystal`).
  - `ArtilianItems`: 10 weapons/tools (`sunsteel_*` and `starlight_*` sword, pickaxe, axe, shovel, hoe — swords include 1-line `DESIGN.md` color tooltips) and 8 GeckoLib `GeoItem` armor items (`ArtilianArmorItem`: `sunsteel_*` and `starlight_*` helmet, chestplate, leggings, boots) rendered per set via `SunsteelArmorRenderer` / `StarlightArmorRenderer` (`DefaultedItemGeoModel` paths `geo/item/armor/<set>_armor.geo.json` and `textures/item/armor/<set>_armor.png`, backed by 3D GeckoLib `1.12.0` armor models and `64x64` texture sheets).
  - Datagen: handheld & armor item models, vanilla tool/armor item tags, crafting recipes for all 18 gear items, English lang entries.
- **Tests, written first and seen failing:** `OresAndGearGameTests`: `sunsteelPickaxeMinesStarlightAndStarlightMinesObsidian`, `everyStarterGearPieceIsCraftable`, `weaponsAndArmorApplyStatsAndRepair`.
- **Red-proof:**
  1. Switch `ArtilianTiers.SUNSTEEL` incorrect-blocks tag to `BlockTags.INCORRECT_FOR_STONE_TOOL` and confirm `sunsteelPickaxeMinesStarlightAndStarlightMinesObsidian` goes red; restore.
  2. Omit one gear recipe and confirm `everyStarterGearPieceIsCraftable` goes red; restore.
- **Done When:** `runData`, `build`, and `runGameTestServer` are green and all 18 starter items are craftable and functional.

### Stage 4: Placeholder art, client verification, docs & PR
- **Changes:**
  - `tools/art/make_ores_and_gear_art.py` generates all placeholder block, item, and armor layer textures using the `DESIGN.md` palette.
  - Verify `runClient` renders items, blocks, creative tab, and equipped armor without missing texture errors.
  - Update this plan, `PLAN.md`'s **Now** section, and open the PR stacked on `phase-1-stage-2-chester`.
- **Done When:** `runData` leaves `src/generated` clean, `build` produces a valid mod jar, `runGameTestServer` passes all tests, and visual verification succeeds.

## Security gate

n/a: no ownership, permissions, server commands, or destructive world-griefing controls are added in this stage; standard vanilla block breaking and crafting rules apply.

## Out of scope

- Custom enchantment effects, set bonuses, mana-consuming weapons, or custom TNTs (those arrive in Stages 4 and 5).
- Custom biome-exclusive ore distributions (arrive with TerraBlender biomes in Phase 2).
- Final hand-painted art (revisited in Phase 1 Stage 6 art pass).
