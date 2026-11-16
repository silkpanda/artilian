# Adventures in Artilian — Crystal Canopy Woods Plan (2026-11-15)

<!-- PLAN.md Phase 2 Stage 1. One PLAN stage, one PR, one jar. The numbered stages below are
commits inside that PR. -->

> **Status: Complete (all 4 stages done; stacked PR open against `phase-1-stage-6-art-pass`).** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 TerraBlender setup & dependency wiring | `add7760` — `gradle.properties` (`terrablender_version=4.1.0.8`), `build.gradle` (Glitchfiend Maven + `jarJar`), `neoforge.mods.toml`, verified `48/48` `runGameTestServer` & `META-INF/jarjar/TerraBlender-neoforge-1.21.1-4.1.0.8.jar`. |
> | 2 Crystal Canopy Woods blocks, datagen & art | `58dd073` — `CRYSTAL_TURF`, `CRYSTAL_CANOPY_LOG`, `CRYSTAL_CANOPY_PLANKS`, `CRYSTAL_CANOPY_LEAVES`, `CRYSTAL_CANOPY_SAPLING`, `CRYSTAL_BLOOM`, `ArtilianTreeGrowers`, `ArtilianWorldGen` tree/bloom features, `tools/art/make_biomes_art.py`, datagen, `CrystalCanopyGameTests#crystalCanopyBlocksLootRecipesAndTreeGrowerWork` (`49/49`). Red-proof: emptied `ArtilianTreeGrowers.CRYSTAL_CANOPY` -> `crystalcanopyblockslootrecipesandtreegrowerwork` failed (`log (false), leaves (false)`); restored green (`49/49`). |
> | 3 Biome, worldgen features, region & surface rules | `68c526a` — `ArtilianBiomes#CRYSTAL_CANOPY_WOODS` (`#38D9C0` / `#68D8FF` palette, `ParticleTypes.GLOW`, foxes/rabbits, crystal trees & bloom patches), `ArtilianWorldGenProvider`, `ArtilianBiomeTagsProvider` (`IS_OVERWORLD`, `IS_FOREST`, `c:is_magical`), `ArtilianOverworldRegion` (TerraBlender `OVERWORLD` weight `8`), `ArtilianSurfaceRules` (`CRYSTAL_TURF` `ON_FLOOR` + `DIRT` `UNDER_FLOOR`), `ArtilianMod` registration, `51/51` `runGameTestServer` green. Red-proof: skipped `Regions.register(...)` in `ArtilianMod#onCommonSetup` -> `crystalcanopywoodsregisteredinoverworldwithterrablenderregion` failed; restored green (`51/51`). |
> | 4 Client verification, docs & PR | Fixed `ArtilianBlockStateProvider` `crystal_canopy_log` self-parenting model loop (`getExistingFile` + `runData`), verified `artilian:crystal_canopy_woods` worldgen (`crystal_turf`, `crystal_canopy_log`, `crystal_canopy_leaves`, `crystal_bloom`) & Creative Tab (`41` items) in `./gradlew runClient` (`qa/evidence/crystal-canopy-woods-2026-11-15.png`, clean `run/logs/latest.log`), updated `deploy/README.md` & `PLAN.md`, and verified `runData`/`build`/`51/51` `runGameTestServer`. |

## Why

Phase 2 begins expanding the Overworld with custom biomes so Alice and Bob have magical places to explore and find biome-specific pets and structures later in Phase 2 (`PLAN.md` Phase 2 Stage 1). Today there are no custom biomes and TerraBlender is not wired into the build. This stage adds **TerraBlender `4.1.0.8`** (bundled via `jarJar` so family co-op still installs one `.jar`) and builds the first Overworld biome, **Crystal Canopy Woods** (`artilian:crystal_canopy_woods`), end to end to prove the worldgen pipeline.

## Decisions

1. **TerraBlender `4.1.0.8` via Glitchfiend's official Maven + `jarJar`.** Fetched from `https://maven.minecraftforge.net/` (`com.github.glitchfiend:TerraBlender-neoforge:1.21.1-4.1.0.8`) and bundled inside our mod `.jar` via `jarJar` alongside GeckoLib so `deploy/README.md` stays a single-file install for family PCs.
2. **Biome identity (`Crystal Canopy Woods`):** A glowing, storybook enchanted woodland in `#38D9C0` Starlight Teal and `#6EE7FF` sky tones (`DESIGN.md`):
   - `crystal_turf`: vibrant teal-moss turf surface block (`BlockTags.DIRT` so vanilla plants, flowers, and trees grow on it; drops Dirt without Silk Touch).
   - `crystal_canopy_log` & `crystal_canopy_planks`: shimmering indigo-teal timber (`BlockTags.LOGS_THAT_BURN` / `BlockTags.PLANKS`, 1 log → 4 planks).
   - `crystal_canopy_leaves`: softly glowing teal-lavender canopy leaves (`lightLevel = 6`, `BlockTags.LEAVES`, drops saplings/sticks).
   - `crystal_canopy_sapling`: `SaplingBlock` wired to a `TreeGrower` for `crystal_canopy_tree`.
   - `crystal_bloom`: glowing (`lightLevel = 9`) starlight blossom placed across the forest floor.
3. **TerraBlender Region & Surface Rules:**
   - `ArtilianOverworldRegion` extends TerraBlender `Region` (`RegionType.OVERWORLD`, weight `8`) and uses `addModifiedVanillaOverworldBiomes` to replace `Biomes.BIRCH_FOREST` and `Biomes.OLD_GROWTH_BIRCH_FOREST` in its region slice with `ArtilianBiomes.CRYSTAL_CANOPY_WOODS`. This preserves valid Overworld continentalness/erosion/ocean/cave transitions while giving the kids an easy-to-find custom biome.
   - `ArtilianSurfaceRules` registers an Overworld `SurfaceRuleManager` rule placing `CRYSTAL_TURF` on `ON_FLOOR` and `Blocks.DIRT` `UNDER_FLOOR` inside `CRYSTAL_CANOPY_WOODS`.
   - `ArtilianBiomeTagsProvider` tags `CRYSTAL_CANOPY_WOODS` into `BiomeTags.IS_OVERWORLD` and `BiomeTags.IS_FOREST` so Phase 1's Sunsteel and Starlight Crystal ores also generate underneath the biome.
4. **Placeholder art until Phase 2 Stage 7:** Clean, readable textures generated deterministically by `tools/art/make_biomes_art.py`; Phase 2 Stage 7 handles the final art pass against the kids' drawings.

## Stages

### Stage 1: TerraBlender setup & dependency wiring
- **Changes:** `gradle.properties` (`terrablender_version=4.1.0.8`), `build.gradle` (Glitchfiend Maven + `jarJar` dependency), `src/main/templates/META-INF/neoforge.mods.toml` (`terrablender` dependency), `ArtilianWorldGenInit` / `ArtilianMod` `FMLCommonSetupEvent` hook stub.
- **Tests / checks:** `./gradlew build` and `./gradlew runGameTestServer` compile and load TerraBlender cleanly.
- **Done When:** TerraBlender `4.1.0.8` resolves, bundles in `build/libs/adventures-in-artilian-*.jar`, and loads in `runGameTestServer`.

### Stage 2: Crystal Canopy Woods blocks, datagen & art
- **Changes:** `CrystalTurfBlock`, `CrystalBloomBlock`, `ArtilianTreeGrowers`, `ArtilianBlocks` / `ArtilianItems` / `ArtilianCreativeTabs`, `tools/art/make_biomes_art.py`, datagen (`ArtilianBlockStateProvider`, `ArtilianItemModelProvider`, `ArtilianBlockLootSubProvider`, `ArtilianBlockTagsProvider`, `ArtilianItemTagsProvider`, `ArtilianRecipeProvider`, `ArtilianLanguageProvider`).
- **Tests, written first and seen failing:** `CrystalCanopyGameTests#crystalCanopyBlocksLootRecipesAndTreeGrowerWork` (verifies turf Silk Touch/dirt drops, log-to-planks recipe, sapling tree growth, and bloom light level).
- **Red-proof:** temporarily return empty drops or omit `CRYSTAL_CANOPY_LOG -> PLANKS` recipe -> test fails -> restore -> green.
- **Done When:** `runData`, `build`, and `runGameTestServer` pass with all blocks/items/recipes working.

### Stage 3: Biome, worldgen features, region & surface rules
- **Changes:** `ArtilianBiomes`, `ArtilianWorldGen` (adds `CRYSTAL_CANOPY_TREE`, `CRYSTAL_BLOOM_PATCH`, placed features, biome feature wiring), `ArtilianWorldGenProvider` (registers `Registries.BIOME`), `ArtilianBiomeTagsProvider`, `ArtilianOverworldRegion`, `ArtilianSurfaceRules`, `ArtilianMod` `FMLCommonSetupEvent` registration.
- **Tests, written first and seen failing:** `CrystalCanopyGameTests`:
  - `crystalCanopyWoodsRegisteredInOverworldWithTerraBlenderRegion` (verifies `artilian:crystal_canopy_woods` biome registry entry, `BiomeTags.IS_OVERWORLD`, `artilian:overworld_region` registration, and Overworld `BiomeSource` inclusion).
  - `crystalCanopyWoodsWorldgenFeaturesAndSurfaceRulesValid` (verifies placed features `crystal_canopy_tree_placed` & `crystal_bloom_patch_placed` exist on the biome alongside `ore_sunsteel_placed` & `ore_starlight_crystal_placed`, and surface rule builds without null states).
- **Red-proof:** temporarily skip `Regions.register(...)` in `ArtilianMod#onCommonSetup` -> confirm `crystalCanopyWoodsRegisteredInOverworldWithTerraBlenderRegion` fails -> restore -> green.
- **Done When:** `runData`, `build`, and `runGameTestServer` pass with all new tests green.

### Stage 4: Client verification, docs & PR
- **Changes:** Verify `Crystal Canopy Woods` blocks in Creative Tab and locate/visit an actual generated `artilian:crystal_canopy_woods` biome in `./gradlew runClient` (zero missing-texture/model errors, clean `run/logs/latest.log`), capture `qa/evidence/crystal-canopy-woods-2026-11-15.png`, update `deploy/README.md`, `PLAN.md` (mark Phase 2 Stage 1 complete & update **Now**), and this plan's status table, verify `runData`/`build`/`runGameTestServer`, push `phase-2-stage-1-first-biome`, and open stacked PR on `phase-1-stage-6-art-pass`.
- **Done When:** `runData` leaves `src/generated` clean, `build` and `runGameTestServer` pass, client biome screenshot & evidence are saved, and the PR is open.

## Security gate

n/a: Phase 2 Stage 1 adds Overworld biome generation, wood/turf/plant blocks, and TerraBlender integration only. No ownership, permissions, server commands, or `allowDestructiveCustomTnt` logic changes; all Phase 1 `P1-01`–`P1-10` probes remain green.

## Out of scope

- *Emberbloom Meadows* and *Starlight Highlands* (Phase 2 Stage 2).
- Biome-specific pets, Capture Crystals, and Biome Treats (Phase 2 Stages 3–5).
- Custom biome structures and enemies (Phase 2 Stage 6).
- Final Phase 2 art pass against kids' drawings (Phase 2 Stage 7).
