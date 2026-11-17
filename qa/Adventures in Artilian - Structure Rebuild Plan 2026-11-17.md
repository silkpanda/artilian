# Adventures in Artilian — Structure Rebuild Plan (2026-11-17)

Branch: `phase-2-stage-7-structure-rebuild`, stacked on `phase-2-stage-6-structures-and-enemies` (`d549792`, PR #3). Rebase onto `main` once PR #3 merges, and don't open this branch's PR before then.
Relates to: `PLAN.md` Phase 2 Stage 7 (Art pass), `DESIGN.md`, `qa/reference/structures/`.

> **Status: Stage 1 complete; Stage 2 next.** Record each stage's commit here.
>
> | Stage | Done |
> |---|---|
> | 1 Building-block variants | This commit. 15 blocks (crystal canopy and emberwood stairs, slab, fence, fence gate, door, trapdoor; starlight slate stairs, slab, wall) with items, creative-tab entries, blockstates/models, loot (doubled slab drops 2, door drops 1), crafting recipes, slate stonecutter recipes under `artilian:` (vanilla `stonecutterResultFromBase` writes to `minecraft:`, replaced), vanilla tags, language, `tools/art/make_building_blocks_art.py` door/trapdoor textures, `BuildingBlockVariantsGameTests` (`101/101` green). Red-proof: emptied `minecraft:walls` -> `fencesandwallsconnect` and `buildingblockvariantsregisteredwithitemsandtags` failed (wall stood alone with `east=none`); restored green (`101/101`). Not yet placed in the dev client; door item icons are full squares, not door-shaped (placeholder art). |
> | 2 Hand-built template pipeline | |
> | 3 Build the three structures | |
> | 4 In-game verification, evidence & PR | |

## Why

The three Phase 2 Stage 6 structures work but look plain. The Crystal Grove Shrine and the Emberbloom Outpost are the same four-post gazebo in different wood, and the Starlight Watchtower is a striped box. All three come from the same 9×9 skeleton, with the chest at `(4,1,2)` and the guardians at `(2.5,1,5.5)` and `(6.5,1,5.5)`. See `qa/reference/structures/ours-vs-reference-2026-11-17.png`.

There are two causes:

1. **Nobody built them.** `ArtilianStructureTemplateProvider` places blocks by coordinate, mostly with `fillBox`. That approach produces boxes.
2. **The mod has no half-blocks.** Crystal canopy wood, emberwood and starlight slate exist only as full cubes. Every reference build in `qa/reference/structures/` gets its depth from stairs, slabs, fences and walls. Across all three templates, the only detail block is one vanilla `STONE_BRICK_WALL`.

## Decisions

1. **The structures are hand-built in-game and saved with structure blocks.** The owner and the kids build them in a creative dev world. The `.nbt` files go in `src/main/resources/data/artilian/structure/`, and the three code builders are deleted. Agents never hand-place structure geometry in code again.
2. **Each structure gets a reference to build from.** These are for mood and shape only. Nobody copies them block for block.

   | Structure | Biome | Build from | Must have |
   |---|---|---|---|
   | Crystal Grove Shrine | Crystal Canopy Woods | `overgrown-temple-ruin.webp` | Stepped base on three sides, columns with vines, a carved band under the roof, an open front |
   | Emberbloom Outpost | Emberbloom Meadows | `timber-workshop-by-river.png`, `ruined-gatehouse-palisade.webp` | Timber frame over a stone base, a fenced porch or palisade, a chimney with campfire smoke |
   | Starlight Watchtower | Starlight Highlands | `hillside-pagoda-small.png`, `farmhouse-with-tower-and-crops.png` | A door at ground level, stairs or a ladder inside up to a deck you can stand on, window openings, a starlight beacon on top |
3. **Size limits.** Each structure fits a footprint of at most 16×16 and is at most 20 blocks tall. That keeps it in one structure block (the cap is 48×48×48), and `TerrainAdjustment.BEARD_THIN` can still settle it onto hillsides. Nothing gets the six-tier-pagoda treatment.
4. **Gameplay contract, unchanged from Stage 6.** Each structure has one reward chest using its existing loot table (`CRYSTAL_GROVE_SHRINE_CHEST`, `EMBERBLOOM_OUTPOST_CHEST`, `STARLIGHT_WATCHTOWER_CHEST`), at least two persistent guardians of its existing type, and an entrance the youngest player can find without help.
5. **Kid-safe contents.** No lava, fire, TNT, command blocks, spawners or leftover structure blocks inside a template. The interiors must be lit enough that vanilla hostiles can't spawn inside.

## Stages

### Stage 1: Building-block variants
- **Changes:** `registry/ArtilianBlocks` and `ArtilianItems` add 15 blocks:
  - for crystal canopy planks and emberwood planks: stairs, slab, fence, fence gate, door and trapdoor (12 blocks);
  - for starlight slate: stairs, slab and wall (3 blocks).

  Wire every one of them through `ArtilianBlockStateProvider`, `ArtilianItemModelProvider`, `ArtilianBlockLootSubProvider` (slabs drop 2 when doubled, doors drop 1), `ArtilianRecipeProvider` (vanilla-shaped recipes, plus stonecutter recipes for the slate), `ArtilianBlockTagsProvider` (`minecraft:wooden_stairs`, `wooden_slabs`, `wooden_fences`, `fence_gates`, `wooden_doors`, `wooden_trapdoors`, `walls`, and `mineable/axe` or `mineable/pickaxe`), `ArtilianItemTagsProvider`, `ArtilianLanguageProvider` and `ArtilianCreativeTabs`.
- **Tests, written first and seen failing:** `BuildingBlockVariantsGameTests`. For each of the 15 blocks, the test checks four things: it's registered with an item, it drops itself when broken (and a doubled slab drops 2), a recipe produces it, and it carries the right vanilla tags. Two extra checks: a placed fence connects to the next fence, and a placed wall connects to the next wall.
- **Red-proof:** Remove `STARLIGHT_SLATE_WALL` from the `minecraft:walls` tag. The wall-connection test should go red. Put it back and the test is green again.
- **Done When:** `runData`, `build` and `runGameTestServer` are green, and all 15 blocks place correctly in the dev client.
- **Rough size:** One evening. It's repetitive datagen work, a good fit for an OpenCode dispatch.

### Stage 2: Hand-built template pipeline
- **Changes:**
  - Move the three current generated `.nbt` files from `src/generated/resources/data/artilian/structure/` to `src/main/resources/data/artilian/structure/` unchanged. This makes the switch a visual no-op, so the pipeline change ships separately from any new art.
  - Delete `buildCrystalGroveShrine`, `buildEmberbloomOutpost` and `buildStarlightWatchtower` from `ArtilianStructureTemplateProvider`.
  - Write `qa/reference/structures/HOW-TO-SAVE.md`, the build-and-save steps for the owner and the kids:
    1. In a creative superflat world in the dev client, build the structure.
    2. Place the chest and set its loot table with `/data merge block <x y z> {LootTable:"artilian:chests/<name>"}`.
    3. Run `/tick freeze`.
    4. Summon the guardians with `{PersistenceRequired:1b}`.
    5. Save with a structure block (Save mode, name `artilian:<name>`, "include entities" on).
    6. Run `/tick unfreeze`.
    7. Copy the file from `run/saves/<world>/generated/artilian/structures/` into `src/main/resources/data/artilian/structure/`.
- **Tests, written first and seen failing:** `StructureTemplateContentsGameTests`. It loads each template through `StructureTemplateManager` and checks:
  - the size is within Decision 3;
  - there's exactly one chest, and its `LootTable` matches the structure;
  - there are at least 2 guardians of the right type, each with `PersistenceRequired` set and no `NoAI` tag;
  - the template contains none of the blocks banned in Decision 5.
- **Red-proof:** Save a copy of the shrine template with the chest's `LootTable` stripped. The loot check should go red. Restore the real template and the test is green again. Do the same for the guardian check with one guardian removed.
- **Done When:** `runData` leaves no structure `.nbt` files in `src/generated`, the existing `BiomeStructuresGameTests` and the new tests are green, and `/locate` still finds all three structures in a fresh world.

### Stage 3: Build the three structures
- **Changes:** The owner and the kids build each structure against its row in Decision 2, save it with `HOW-TO-SAVE.md`, and drop it in to replace the placeholder `.nbt`. Build one at a time, in this order: shrine, then outpost, then watchtower. The shrine goes first because it looks plainest today, and the crystal canopy set from Stage 1 has every variant it needs.
- **Agent's part:** After each file lands, run `StructureTemplateContentsGameTests`, `/locate` the structure in a fresh world, and save a screenshot to `qa/evidence/`. Report any failed check in plain words: which block is banned, or which guardian is missing its tag.
- **Done When:** All three hand-built templates pass the contents tests, and the owner has looked at each one in-game and approved it.
- **Rough size:** About an hour of building per structure.

### Stage 4: In-game verification, evidence & PR
- **Changes:** In a fresh Normal world, visit all three structures. Open each chest, fight each structure's guardians with a summoned pet, and confirm that pets target the guardians and faint non-lethally. These are the combat and loot checks Phase 2 Stage 6 never recorded. Check `run/logs/latest.log` for `artilian` errors. Then:
  1. Save new screenshots and an updated `ours-vs-reference` comparison to `qa/evidence/`.
  2. Update `PLAN.md` and this plan's status table.
  3. Move this plan to `qa/archive/`.
  4. Open a PR.
- **Done When:** CI is green (`Tests` and `Personal-info guard`), the combat and loot checks are recorded with screenshots, and the PR is open against `main`.

## Security gate

n/a. This changes block content and structure templates only. No Chester ownership, server permissions or TNT controls are touched. Decision 5 (no lava, fire, TNT, command blocks or spawners in templates) is a kid-safety rule, and the Stage 2 tests enforce it.

## Out of scope

- New structures from the reference folder (the pagoda, the domed sea palace, the skull castle, the step pyramid). Each one gets its own plan once these three are done.
- Multi-piece jigsaw structures. All three stay single-piece templates.
- Re-art of the structure enemies (Prism Sentinel, Cinder Raider, Astral Stalker). That stays in the general Phase 2 Stage 7 art pass.
- Eye Realm and Dragon Jungle structures (Phases 4 and 5).
