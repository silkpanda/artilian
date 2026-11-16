package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.worldgen.ArtilianBiomes;
import com.silkpanda.artilian.worldgen.ArtilianSurfaceRules;
import com.silkpanda.artilian.worldgen.ArtilianWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Phase 2 Stage 2: Emberbloom Meadows & Starlight Highlands blocks, tree grower, biomes & region. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class RemainingBiomesGameTests {

    @GameTest(template = "tnt_arena")
    public static void remainingBiomeBlocksLootRecipesAndTreeGrowerWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "SteveBiome");

        // 1. Ember Turf & Starlight Turf act as dirt and drop Dirt normally, self with Silk Touch.
        BlockPos emberTurfRel = new BlockPos(5, 1, 5);
        assertTurfDrops(helper, level, player, emberTurfRel, ArtilianBlocks.EMBER_TURF.get(), ArtilianItems.EMBER_TURF_ITEM.get());

        BlockPos starlightTurfRel = new BlockPos(9, 1, 9);
        assertTurfDrops(helper, level, player, starlightTurfRel, ArtilianBlocks.STARLIGHT_TURF.get(), ArtilianItems.STARLIGHT_TURF_ITEM.get());

        // 2. Ember Bloom (light 9) survives on Ember Turf; Starlight Blossom (light 10) survives on Starlight Turf.
        assertFlowerOnTurf(helper, level, emberTurfRel.above(), ArtilianBlocks.EMBER_BLOOM.get(), 9);
        assertFlowerOnTurf(helper, level, starlightTurfRel.above(), ArtilianBlocks.STARLIGHT_BLOSSOM.get(), 10);

        // 3. Starlight Slate is stone-tagged pickaxe-mineable and drops itself cleanly.
        BlockPos slateRel = new BlockPos(11, 1, 5);
        BlockPos slateAbs = helper.absolutePos(slateRel);
        helper.setBlock(slateRel, ArtilianBlocks.STARLIGHT_SLATE.get());
        BlockState slateState = level.getBlockState(slateAbs);
        helper.assertTrue(slateState.is(BlockTags.MINEABLE_WITH_PICKAXE),
                "Starlight Slate should be tagged minecraft:mineable/pickaxe");
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        List<ItemStack> slateDrops = Block.getDrops(slateState, level, slateAbs, null, player, pickaxe);
        helper.assertTrue(slateDrops.stream().anyMatch(s -> s.is(ArtilianItems.STARLIGHT_SLATE_ITEM.get())),
                "Starlight Slate should drop itself with Iron Pickaxe, got " + slateDrops);

        // 4. Emberwood Log -> 4x Emberwood Planks recipe.
        CraftingInput logInput = CraftingInput.of(1, 1, List.of(new ItemStack(ArtilianItems.EMBERWOOD_LOG_ITEM.get())));
        Optional<RecipeHolder<CraftingRecipe>> planksRecipe =
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, logInput, level);
        helper.assertTrue(planksRecipe.isPresent(), "Expected Emberwood Log -> Planks recipe");
        ItemStack planksOut = planksRecipe.get().value().assemble(logInput, level.registryAccess());
        helper.assertTrue(planksOut.is(ArtilianItems.EMBERWOOD_PLANKS_ITEM.get()) && planksOut.getCount() == 4,
                "Expected 4x Emberwood Planks from 1 Log, got " + planksOut);

        // 5. Emberwood Sapling grows into Emberwood Tree (logs + light-4 glowing leaves).
        BlockPos saplingRel = emberTurfRel.above();
        helper.setBlock(saplingRel, Blocks.AIR);
        helper.setBlock(saplingRel, ArtilianBlocks.EMBERWOOD_SAPLING.get());
        BlockPos saplingAbs = helper.absolutePos(saplingRel);
        SaplingBlock sapling = (SaplingBlock) level.getBlockState(saplingAbs).getBlock();
        for (int attempt = 0; attempt < 6 && level.getBlockState(saplingAbs).is(ArtilianBlocks.EMBERWOOD_SAPLING.get()); attempt++) {
            sapling.advanceTree(level, saplingAbs, level.getBlockState(saplingAbs), level.getRandom());
        }

        boolean foundLog = false;
        boolean foundLeaves = false;
        for (BlockPos check : BlockPos.betweenClosed(saplingAbs.offset(-5, 0, -5), saplingAbs.offset(5, 12, 5))) {
            BlockState checkState = level.getBlockState(check);
            if (checkState.is(ArtilianBlocks.EMBERWOOD_LOG.get())) {
                foundLog = true;
            } else if (checkState.is(ArtilianBlocks.EMBERWOOD_LEAVES.get())) {
                foundLeaves = true;
                int leafLight = checkState.getLightEmission(level, check);
                helper.assertTrue(leafLight == 4, "Expected Emberwood Leaves light 4, got " + leafLight);
            }
        }
        helper.assertTrue(foundLog && foundLeaves,
                "Expected grown Emberwood tree with both log (" + foundLog + ") and leaves (" + foundLeaves + ")");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void remainingBiomesRegisteredInOverworldWithTerraBlenderRegion(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);

        Holder.Reference<Biome> ember = biomes.getHolder(ArtilianBiomes.EMBERBLOOM_MEADOWS)
                .orElseThrow(() -> new AssertionError("Missing Emberbloom Meadows biome in registry"));
        helper.assertTrue(ember.is(BiomeTags.IS_OVERWORLD), "Emberbloom Meadows must be tagged minecraft:is_overworld");
        helper.assertTrue(ember.is(Tags.Biomes.IS_PLAINS), "Emberbloom Meadows must be tagged c:is_plains");
        helper.assertTrue(ember.is(Tags.Biomes.IS_FLORAL), "Emberbloom Meadows must be tagged c:is_floral");
        helper.assertTrue(ember.is(Tags.Biomes.IS_MAGICAL), "Emberbloom Meadows must be tagged c:is_magical");

        Holder.Reference<Biome> starlight = biomes.getHolder(ArtilianBiomes.STARLIGHT_HIGHLANDS)
                .orElseThrow(() -> new AssertionError("Missing Starlight Highlands biome in registry"));
        helper.assertTrue(starlight.is(BiomeTags.IS_OVERWORLD), "Starlight Highlands must be tagged minecraft:is_overworld");
        helper.assertTrue(starlight.is(BiomeTags.IS_MOUNTAIN), "Starlight Highlands must be tagged minecraft:is_mountain");
        helper.assertTrue(starlight.is(Tags.Biomes.IS_MAGICAL), "Starlight Highlands must be tagged c:is_magical");

        Optional<Region> artilianRegion = Regions.get(RegionType.OVERWORLD).stream()
                .filter(region -> ArtilianMod.id("overworld_region").equals(region.getName()))
                .findFirst();
        helper.assertTrue(artilianRegion.isPresent(),
                "TerraBlender OVERWORLD regions should contain artilian:overworld_region");

        List<ResourceKey<Biome>> mappedBiomes = new ArrayList<>();
        artilianRegion.get().addBiomes(biomes, pair -> mappedBiomes.add(pair.getSecond()));
        helper.assertTrue(mappedBiomes.contains(ArtilianBiomes.EMBERBLOOM_MEADOWS),
                "artilian:overworld_region must emit Emberbloom Meadows");
        helper.assertTrue(mappedBiomes.contains(ArtilianBiomes.STARLIGHT_HIGHLANDS),
                "artilian:overworld_region must emit Starlight Highlands");

        helper.assertTrue(ArtilianSurfaceRules.makeRules() != null,
                "Artilian surface rules must build non-null RuleSource");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void remainingBiomesWorldgenFeaturesAndPlacementsWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Registry<PlacedFeature> placedRegistry = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        Registry<ConfiguredFeature<?, ?>> configuredRegistry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);

        Holder<PlacedFeature> sunsteelPlaced = placedRegistry.getHolderOrThrow(ArtilianWorldGen.ORE_SUNSTEEL_PLACED);
        Holder<PlacedFeature> starlightPlaced = placedRegistry.getHolderOrThrow(ArtilianWorldGen.ORE_STARLIGHT_CRYSTAL_PLACED);
        int localModStep = GenerationStep.Decoration.LOCAL_MODIFICATIONS.ordinal();
        int vegStep = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
        int oreStep = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();

        Biome emberBiome = biomes.getHolderOrThrow(ArtilianBiomes.EMBERBLOOM_MEADOWS).value();
        List<HolderSet<PlacedFeature>> emberFeatures = emberBiome.getGenerationSettings().features();
        helper.assertTrue(vegStep < emberFeatures.size()
                        && emberFeatures.get(vegStep).contains(placedRegistry.getHolderOrThrow(ArtilianWorldGen.EMBERWOOD_TREE_PLACED)),
                "Emberbloom Meadows missing Emberwood Tree placed feature");
        helper.assertTrue(emberFeatures.get(vegStep).contains(placedRegistry.getHolderOrThrow(ArtilianWorldGen.EMBER_BLOOM_PATCH_PLACED)),
                "Emberbloom Meadows missing Ember Bloom patch placed feature");
        helper.assertTrue(oreStep < emberFeatures.size() && emberFeatures.get(oreStep).contains(sunsteelPlaced)
                        && emberFeatures.get(oreStep).contains(starlightPlaced),
                "Emberbloom Meadows missing Phase 1 Overworld ores");

        Biome starlightBiome = biomes.getHolderOrThrow(ArtilianBiomes.STARLIGHT_HIGHLANDS).value();
        List<HolderSet<PlacedFeature>> starlightFeatures = starlightBiome.getGenerationSettings().features();
        helper.assertTrue(localModStep < starlightFeatures.size()
                        && starlightFeatures.get(localModStep).contains(placedRegistry.getHolderOrThrow(ArtilianWorldGen.STARLIGHT_SLATE_BOULDER_PLACED)),
                "Starlight Highlands missing Starlight Slate boulder placed feature");
        helper.assertTrue(vegStep < starlightFeatures.size()
                        && starlightFeatures.get(vegStep).contains(placedRegistry.getHolderOrThrow(ArtilianWorldGen.STARLIGHT_BLOSSOM_PATCH_PLACED)),
                "Starlight Highlands missing Starlight Blossom patch placed feature");
        helper.assertTrue(oreStep < starlightFeatures.size() && starlightFeatures.get(oreStep).contains(sunsteelPlaced)
                        && starlightFeatures.get(oreStep).contains(starlightPlaced),
                "Starlight Highlands missing Phase 1 Overworld ores");

        // Verify Ember Bloom patch places onto Ember Turf in West half, and Blossom + Slate boulder place in East half.
        for (int x = 2; x <= 6; x++) {
            for (int z = 3; z <= 11; z++) {
                helper.setBlock(new BlockPos(x, 1, z), ArtilianBlocks.EMBER_TURF.get());
            }
        }
        for (int x = 8; x <= 12; x++) {
            for (int z = 3; z <= 11; z++) {
                helper.setBlock(new BlockPos(x, 1, z), ArtilianBlocks.STARLIGHT_TURF.get());
            }
        }
        boolean emberPlaced = configuredRegistry.getHolderOrThrow(ArtilianWorldGen.EMBER_BLOOM_PATCH).value()
                .place(level, level.getChunkSource().getGenerator(), RandomSource.create(123L), helper.absolutePos(new BlockPos(4, 2, 7)));
        boolean blossomPlaced = configuredRegistry.getHolderOrThrow(ArtilianWorldGen.STARLIGHT_BLOSSOM_PATCH).value()
                .place(level, level.getChunkSource().getGenerator(), RandomSource.create(456L), helper.absolutePos(new BlockPos(10, 2, 7)));
        boolean boulderPlaced = configuredRegistry.getHolderOrThrow(ArtilianWorldGen.STARLIGHT_SLATE_BOULDER).value()
                .place(level, level.getChunkSource().getGenerator(), RandomSource.create(789L), helper.absolutePos(new BlockPos(10, 2, 4)));

        int emberBlooms = countBlock(helper, ArtilianBlocks.EMBER_BLOOM.get());
        int starlightBlossoms = countBlock(helper, ArtilianBlocks.STARLIGHT_BLOSSOM.get());
        int slateBoulders = countBlock(helper, ArtilianBlocks.STARLIGHT_SLATE.get());
        helper.assertTrue(emberPlaced && emberBlooms > 0,
                "Expected Ember Bloom patch on Ember Turf, found " + emberBlooms);
        helper.assertTrue(blossomPlaced && starlightBlossoms > 0,
                "Expected Starlight Blossom patch on Starlight Turf, found " + starlightBlossoms);
        helper.assertTrue(boulderPlaced && slateBoulders > 0,
                "Expected Starlight Slate boulder on Starlight Turf, found " + slateBoulders);
        helper.succeed();
    }

    private static int countBlock(GameTestHelper helper, Block target) {
        int count = 0;
        for (int x = 1; x <= 13; x++) {
            for (int y = 2; y <= 6; y++) {
                for (int z = 1; z <= 13; z++) {
                    if (helper.getBlockState(new BlockPos(x, y, z)).is(target)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static void assertTurfDrops(
            GameTestHelper helper, ServerLevel level, ServerPlayer player, BlockPos rel, Block block, Item selfItem) {
        BlockPos abs = helper.absolutePos(rel);
        helper.setBlock(rel, block);
        BlockState state = level.getBlockState(abs);
        helper.assertTrue(state.is(BlockTags.DIRT), block + " should be tagged minecraft:dirt");

        ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
        player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        List<ItemStack> normalDrops = Block.getDrops(state, level, abs, null, player, shovel);
        helper.assertTrue(normalDrops.stream().anyMatch(s -> s.is(Items.DIRT)),
                block + " without Silk Touch should drop Dirt, got " + normalDrops);

        ItemStack silkShovel = new ItemStack(Items.IRON_SHOVEL);
        Holder<Enchantment> silkTouch = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        silkShovel.enchant(silkTouch, 1);
        List<ItemStack> silkDrops = Block.getDrops(state, level, abs, null, player, silkShovel);
        helper.assertTrue(silkDrops.stream().anyMatch(s -> s.is(selfItem)),
                block + " with Silk Touch should drop itself, got " + silkDrops);
    }

    private static void assertFlowerOnTurf(
            GameTestHelper helper, ServerLevel level, BlockPos rel, Block flower, int expectedLight) {
        helper.setBlock(rel, flower);
        BlockPos abs = helper.absolutePos(rel);
        BlockState state = helper.getBlockState(rel);
        helper.assertTrue(state.canSurvive(level, abs), flower + " should survive on turf");
        int actualLight = state.getLightEmission(level, abs);
        helper.assertTrue(actualLight == expectedLight,
                "Expected light level " + expectedLight + " on " + flower + ", got " + actualLight);
    }
}
