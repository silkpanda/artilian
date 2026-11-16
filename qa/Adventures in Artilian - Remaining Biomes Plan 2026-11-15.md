# Adventures in Artilian — Remaining Biomes Plan (2026-11-15)

Branch: `phase-2-stage-2-remaining-biomes` (stacked on `phase-2-stage-1-first-biome`, PR #9).
Relates to: `PLAN.md` Phase 2 Stage 2, `DESIGN.md`, `PRODUCT.md`.

> **Status: Complete (all 3 stages done; stacked PR open against `phase-2-stage-1-first-biome`).** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Biome blocks, tree grower, art & datagen | `bff6297` — `EMBER_TURF`, `EMBERWOOD_*` (`log`, `planks`, `leaves` light 4, `sapling`), `EMBER_BLOOM` (light 9), `STARLIGHT_TURF`, `STARLIGHT_SLATE`, `STARLIGHT_BLOSSOM` (light 10), `ArtilianTreeGrowers#EMBERWOOD`, tree/boulder/flower features, `tools/art/make_biomes_art.py`, parameterized datagen, `RemainingBiomesGameTests#remainingBiomeBlocksLootRecipesAndTreeGrowerWork` (`52/52`). Red-proof: emptied `ArtilianTreeGrowers.EMBERWOOD` -> `remainingbiomeblockslootrecipesandtreegrowerwork` failed (`log (false) and leaves (false)`); restored green (`52/52`). |
> | 2 Biomes, worldgen features, region & surface rules | `f8de276` — `ArtilianBiomes#EMBERBLOOM_MEADOWS` (`#FFC07A` / `#F2B84B`, `SMALL_FLAME`, horses/rabbits/donkeys) & `STARLIGHT_HIGHLANDS` (`#8B7CFF` / `#6A5A8C`, `END_ROD`, goats/wolves/foxes), `ArtilianOverworldRegion`, `ArtilianSurfaceRules` (steep `STARLIGHT_SLATE` cliffs + `turfRule`), `ArtilianBiomeTagsProvider`, `54/54` `runGameTestServer` green. Red-proof: omitted new biomes from `ArtilianOverworldRegion` -> `remainingbiomesregisteredinoverworldwithterrablenderregion` failed; restored green (`54/54`). |
> | 3 Client verification, evidence, docs & PR | Verified live Overworld worldgen (`artilian:emberbloom_meadows` & `artilian:starlight_highlands`) & Creative Tab (`50` items) via `./gradlew runClient` (removed `EntityType.BEE` spawn entry from `EMBERBLOOM_MEADOWS` after catching `RegisterSpawnPlacementsEvent` error in log, re-ran `runData` + `runClient` cleanly), saved `qa/evidence/emberbloom-meadows-2026-11-15.png` & `qa/evidence/starlight-highlands-2026-11-15.png`, updated `PLAN.md`, and verified `runData` / `build` / `54/54` `runGameTestServer`. |

## Why

Phase 2 Stage 1 proved TerraBlender (`4.1.0.8`) and the Overworld biome pipeline with *Crystal Canopy Woods*. Stage 2 (`PLAN.md` Phase 2 Stage 2) completes Artilian's three-biome Overworld trio so Alice and Bob can explore warm golden flower meadows (**Emberbloom Meadows**) and misty twilight mountain plateaus (**Starlight Highlands**) before pet capture and biome-specific pets arrive in Stages 3–5.

## Decisions

1. **Biome identities (`DESIGN.md` palette):**
   - **Emberbloom Meadows** (`artilian:emberbloom_meadows`): Sunsteel-gold (`#FF9F38` / `#F2B84B`) rolling meadow with apricot-sunset sky (`#FFC07A`), subtle floating `ParticleTypes.SMALL_FLAME` motes, warm `ember_turf` (`BlockTags.DIRT`, drops Dirt without Silk Touch), glowing `ember_bloom` (`lightLevel = 9`, `MobEffects.FIRE_RESISTANCE`), and sparse sunset-gold `Emberwood` trees (`emberwood_log`, `emberwood_planks`, `emberwood_leaves` at `lightLevel = 4`, `emberwood_sapling` via `ArtilianTreeGrowers#EMBERWOOD` using `AcaciaFoliagePlacer`).
   - **Starlight Highlands** (`artilian:starlight_highlands`): High-altitude arcane slate plateau (`#2B2338` / `#6A5A8C`) with twilight violet-blue sky (`#8B7CFF`), `ParticleTypes.END_ROD` starlight motes, `starlight_turf` (`BlockTags.DIRT`, drops Dirt without Silk Touch) resting over `starlight_slate` (`BlockTags.MINEABLE_WITH_PICKAXE`, exposed on steep cliffs + `FOREST_ROCK` boulder outcrops), and glowing `starlight_blossom` (`lightLevel = 10`, `MobEffects.NIGHT_VISION`).
2. **TerraBlender Overworld mapping (`ArtilianOverworldRegion`):**
   - Keep `RegionType.OVERWORLD` weight `8` so vanilla biomes remain common while custom biomes reliably appear.
   - Map `Biomes.MEADOW` & `Biomes.SUNFLOWER_PLAINS` -> `ArtilianBiomes.EMBERBLOOM_MEADOWS` and `Biomes.WINDSWEPT_HILLS`, `Biomes.WINDSWEPT_GRAVELLY_HILLS`, & `Biomes.STONY_PEAKS` -> `ArtilianBiomes.STARLIGHT_HIGHLANDS`.
3. **Biome & ore compatibility:**
   - Tag `EMBERBLOOM_MEADOWS` into `BiomeTags.IS_OVERWORLD`, `Tags.Biomes.IS_PLAINS`, `Tags.Biomes.IS_FLORAL`, `Tags.Biomes.IS_MAGICAL`.
   - Tag `STARLIGHT_HIGHLANDS` into `BiomeTags.IS_OVERWORLD`, `BiomeTags.IS_MOUNTAIN`, `Tags.Biomes.IS_MOUNTAIN`, `Tags.Biomes.IS_MAGICAL`.
   - Keep Phase 1 Sunsteel and Starlight Crystal ore generation flowing automatically under both biomes via `BiomeTags.IS_OVERWORLD`, without giving away free surface `STARLIGHT_CRYSTAL_BLOCK` outcrops.
4. **Placeholder art until Phase 2 Stage 7:**
   - Extend `tools/art/make_biomes_art.py` deterministically using parameterized palette helpers rather than duplicated loops; Phase 2 St handles final polish from Alice & Bob's drawings.

## Stages

### Stage 1: Biome blocks, tree grower, art & datagen
- **Changes:** `ArtilianBlocks`, `ArtilianItems`, `ArtilianCreativeTabs`, `ArtilianTreeGrowers#EMBERWOOD`, `ArtilianWorldGen#EMBERWOOD_TREE` / `EMBER_BLOOM_PATCH` / `STARLIGHT_SLATE_BOULDER` / `STARLIGHT_BLOSSOM_PATCH` configured & placed features, `tools/art/make_biomes_art.py` (+ 11 textures), parameterized datagen (`ArtilianBlockStateProvider`, `ArtilianItemModelProvider`, `ArtilianBlockLootSubProvider`, `ArtilianBlockTagsProvider`, `ArtilianRecipeProvider`, `ArtilianLanguageProvider`).
- **Tests, written first and seen failing:** `RemainingBiomesGameTests#remainingBiomeBlocksLootRecipesAndTreeGrowerWork` (verifies `ember_turf`/`starlight_turf` Silk Touch + Dirt drops, `ember_bloom`/`starlight_blossom` survival & light levels `9`/`10`, `emberwood_log` -> 4x `emberwood_planks`, `emberwood_sapling` growth into glowing `emberwood_leaves` (`lightLevel = 4`), and `starlight_slate` stone mineability).
- **Red-proof:** temporarily remove `Emberwood` sapling tree feature or return empty drops -> confirm `remainingbiomeblockslootrecipesandtreegrowerwork` fails -> restore -> green.
- **Done When:** `runData`, `build`, and `runGameTestServer` pass cleanly.

### Stage 2: Biomes, worldgen features, region & surface rules
- **Changes:** `ArtilianBiomes#EMBERBLOOM_MEADOWS` & `STARLIGHT_HIGHLANDS` (shared helper for Overworld base generation/effects), `ArtilianOverworldRegion`, `ArtilianSurfaceRules` (parameterized turf rule + steep `starlight_slate` cliff rule), `ArtilianBiomeTagsProvider`.
- **Tests, written first and seen failing:** `RemainingBiomesGameTests`:
  - `remainingBiomesRegisteredInOverworldWithTerraBlenderRegion` (verifies both biomes exist, hold expected `BiomeTags.IS_OVERWORLD` / meadow / mountain / magical tags, and are emitted by `artilian:overworld_region`).
  - `remainingBiomesWorldgenFeaturesAndPlacementsWork` (verifies biome placed features including Sunsteel & Starlight ores, `ember_bloom_patch` placement on `ember_turf`, and `starlight_blossom_patch` + `starlight_slate_boulder` placement on `starlight_turf`/`starlight_slate`).
- **Red-proof:** temporarily omit `EMBERBLOOM_MEADOWS` or `STARLIGHT_HIGHLANDS` from `ArtilianOverworldRegion` -> confirm `remainingbiomesregisteredinoverworldwithterrablenderregion` fails -> restore -> green.
- **Done When:** `runData`, `build`, and `runGameTestServer` pass cleanly.

### Stage 3: Client verification, evidence, docs & PR
- **Changes:** Launch `./gradlew.bat runClient --no-daemon`, generate a fresh Normal Overworld, locate & verify both `artilian:emberbloom_meadows` and `artilian:starlight_highlands` in live chunk generation, check `run/logs/latest.log` for zero `artilian` errors/warnings, save `qa/evidence/emberbloom-meadows-2026-11-15.png` and `qa/evidence/starlight-highlands-2026-11-15.png`, remove the temporary client harness, update `PLAN.md` (`Now` + Stage 2 checkbox) and this plan's status table, verify `runData`/`build`/`runGameTestServer`, push `phase-2-stage-2-remaining-biomes`, and open stacked PR on `phase-2-stage-1-first-biome`.
- **Done When:** All CI checks green, both biome screenshots visually verified, and stacked PR open.

## Security gate

n/a: Phase 2 Stage 2 adds Overworld biomes, terrain/plant blocks, art, and TerraBlender region mappings only. No ownership, permissions, server commands, or `allowDestructiveCustomTnt` logic changes; all `P1-01`–`P1-10` probes remain green.

## Out of scope

- Biome-specific pets, Capture Crystals, Biome Treats, and pet battling (Phase 2 Stages 3–5).
- Custom biome structures and structure enemies (Phase 2 Stage 6).
- Final Phase 2 art pass against Alice & Bob's drawings (Phase 2 St).
