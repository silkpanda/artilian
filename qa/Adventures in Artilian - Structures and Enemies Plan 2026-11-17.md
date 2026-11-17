# Adventures in Artilian — Structures and Enemies Plan (2026-11-17)

Branch: `phase-2-stage-6-structures-and-enemies` (from `main` `d91a82d`).
Relates to: `PLAN.md` Phase 2 Stage 6, `DESIGN.md`, `PRODUCT.md`.

> **Status: Stage 1 complete (Stage 2 next).** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Custom structure enemies, GeckoLib art & pet battle integration | Pending commit — `StructureEnemyEntity`, `PrismSentinelEntity` (`CRYSTAL`/`EARTH`), `CinderRaiderEntity` (`FIRE`/`EARTH`), `AstralStalkerEntity` (`STARLIGHT`/`SHADOW`), `EnemyEvents`, parameterized `StructureEnemyModel`/`StructureEnemyRenderer`, `ArtilianEntityLootSubProvider`, spawn eggs, `artilian:structure_enemies` tag, `tools/art/make_structures_enemies_art.py`, `StructureEnemiesGameTests` (`93/93` green). Red-proof: omitted `PRISM_SENTINEL` from `ArtilianEntityTypeTagsProvider` -> `structureenemiesregisteredtaggedhaveeggsandelementaltypes` failed; restored green (`93/93`). |
> | 2 Biome-specific structures, loot tables & worldgen | |
> | 3 Client verification, evidence, docs & PR | |

## Why

Alex wants custom biome-specific structures and custom structure enemies (`PRODUCT.md`). Phase 2 Stages 1–5 shipped all three Overworld biomes, pet capture, battle AI (including `artilian:structure_enemies` targeting in `PetService`), and 7 Overworld pets—but `artilian:structure_enemies` currently only tags vanilla mobs (`ArtilianEntityTypeTagsProvider`), and the custom biomes have no Artilian structures to discover yet. Building one landmark structure and one signature custom enemy per Artilian biome completes the Overworld exploration and pet-battling loop before Phase 2 Stage 7's art pass and Phase 3's bosses.

## Decisions

1. **Three Overworld biome structures (one per Artilian biome):**
   - **Crystal Grove Shrine** (`artilian:crystal_grove_shrine`) in `artilian:crystal_canopy_woods` — built with Crystal Canopy wood/blocks, glowing accents, and a treasure chest.
   - **Emberbloom Outpost** (`artilian:emberbloom_outpost`) in `artilian:emberbloom_meadows` — built with Emberwood/warm stone accents and a treasure chest.
   - **Starlight Watchtower** (`artilian:starlight_watchtower`) in `artilian:starlight_highlands` — built with `starlight_slate`, glowing accents, and a treasure chest.
   - Each structure uses a custom reward `LootTable` stocked with Biome Treats, Capture Crystals, Sunsteel/Starlight materials, and occasional starter supplies.
2. **Three custom `artilian:structure_enemies` (one per structure):**
   - **Prism Sentinel** (`artilian:prism_sentinel`) — guards Crystal Grove Shrine.
   - **Cinder Raider** (`artilian:cinder_raider`) — guards Emberbloom Outpost.
   - **Astral Stalker** (`artilian:astral_stalker`) — guards Starlight Watchtower.
   - All three extend/share a common helper where appropriate, use GeckoLib models/textures/animations (`tools/art/make_structures_enemies_art.py`), ship with Creative spawn eggs, and are added to `ArtilianTags.EntityTypes.STRUCTURE_ENEMIES` so summoned pets automatically target and battle them.
3. **Kid-accessible combat & safety (`DESIGN.md`, `PRODUCT.md`):**
   - High-contrast silhouettes and readable attack cues so ages 5 and 7 can distinguish structure enemies from friendly pets at a glance.
   - Fair HP/damage balanced for Sunsteel/Starlight gear, starter wands, and summoned battle pets.
   - **No block-griefing attacks:** structure enemies never break or explode blocks.

## Stages

### Stage 1: Custom structure enemies, GeckoLib art & pet battle integration
- **Changes:** `ArtilianEntities`, `ArtilianItems` (3 spawn eggs), `ArtilianCreativeTabs`, custom enemy classes (`PrismSentinelEntity`, `CinderRaiderEntity`, `AstralStalkerEntity` + shared base/helper if needed), GeckoLib models/renderers/animations + `tools/art/make_structures_enemies_art.py`, `ArtilianEntityTypeTagsProvider` (`STRUCTURE_ENEMIES`), `ArtilianItemModelProvider`, `ArtilianLanguageProvider`, `ArtilianEntityLootSubProvider` (if needed).
- **Tests, written first and seen failing:** `StructureEnemiesGameTests`:
  - All three enemies are registered, spawn cleanly with attributes, have working spawn eggs, and are present in `ArtilianTags.EntityTypes.STRUCTURE_ENEMIES`.
  - Summoned Overworld pets (`PetService` / battle AI) automatically acquire and engage all three custom enemies without harming owners or other pets.
- **Red-proof:** Temporarily omit one custom enemy from `ArtilianTags.EntityTypes.STRUCTURE_ENEMIES` -> confirm `StructureEnemiesGameTests` fails -> restore -> green.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` pass cleanly.

### Stage 2: Biome-specific structures, loot tables & worldgen
- **Changes:** Structure NBT pieces / `Structure` + `StructureSet` + `TemplatePool` bootstrap in `ArtilianStructures` / `ArtilianWorldGenProvider`, `ArtilianBiomeTagsProvider` (`has_structure/...` tags mapped to `CRYSTAL_CANOPY_WOODS`, `EMBERBLOOM_MEADOWS`, `STARLIGHT_HIGHLANDS`), structure chest `LootTable`s via datagen, and initial/spawned structure enemy placement inside each structure.
- **Tests, written first and seen failing:** `BiomeStructuresGameTests`:
  - All three structures and structure sets exist in registries, each targets only its matching Artilian biome tag, and each structure chest loot table generates valid items.
  - Structure pieces/placement generate cleanly in a test world/server lookup and place/spawn their expected `artilian:structure_enemies` guardian.
- **Red-proof:** Temporarily remove a biome's `has_structure/...` tag or omit a structure from its `StructureSet` -> confirm `BiomeStructuresGameTests` fails -> restore -> green.
- **Done When:** `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` pass cleanly.

### Stage 3: Client verification, evidence, docs & PR
- **Changes:** Run `./gradlew runClient --no-daemon`, locate and visit all three structures (`/locate structure artilian:...`) in a fresh Normal Overworld, verify structure chest loot and pet-vs-enemy combat against all three guardians, verify `run/logs/latest.log` has zero `artilian` errors/warnings, save screenshots in `qa/evidence/`, update `PLAN.md` (Phase 2 Stage 6 checkbox + `Now` table) and this plan's status table, verify `./gradlew runData`, `./gradlew build`, and `./gradlew runGameTestServer` green, and open a PR to `main`.
- **Done When:** All CI checks green (`Tests` + `Personal-info guard`), all three structures and guardians visually verified in-game, and PR open against `main`.

## Security gate

n/a: Phase 2 Stage 6 adds Overworld structures, structure chest loot, and non-griefing structure enemies only. No ownership/UUID inventory logic (Chester), server permission commands, or `allowDestructiveCustomTnt` block-destruction controls are altered; structure enemies must not break world blocks.

## Out of scope

- Phase 3 bosses (`ArtilianBossEntity`, Boss Altars, Crystal Colossus, Captain Brimstone).
- Eye Realm and Dragon Jungle structures/enemies (Phases 4–5).
- Final Phase 2 art polish against Alex & Steve's drawings (Phase 2 Stage 7).
