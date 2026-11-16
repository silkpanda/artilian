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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
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

/** Phase 2 Stage 1: Crystal Canopy Woods blocks, loot, recipes, tree grower, and TerraBlender biome. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class CrystalCanopyGameTests {

    @GameTest(template = "tnt_arena")
    public static void crystalCanopyBlocksLootRecipesAndTreeGrowerWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "AlexBiome");

        // 1. Crystal Turf acts as dirt, drops Dirt normally and Crystal Turf with Silk Touch.
        BlockPos turfRel = new BlockPos(7, 1, 7);
        BlockPos turfAbs = helper.absolutePos(turfRel);
        helper.setBlock(turfRel, ArtilianBlocks.CRYSTAL_TURF.get());
        BlockState turfState = level.getBlockState(turfAbs);
        helper.assertTrue(turfState.is(BlockTags.DIRT), "Crystal Turf should be tagged minecraft:dirt");

        ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
        player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        List<ItemStack> normalDrops = Block.getDrops(turfState, level, turfAbs, null, player, shovel);
        helper.assertTrue(normalDrops.stream().anyMatch(s -> s.is(Items.DIRT)),
                "Crystal Turf without Silk Touch should drop Dirt, got " + normalDrops);

        ItemStack silkShovel = new ItemStack(Items.IRON_SHOVEL);
        Holder<Enchantment> silkTouch = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        silkShovel.enchant(silkTouch, 1);
        List<ItemStack> silkDrops = Block.getDrops(turfState, level, turfAbs, null, player, silkShovel);
        helper.assertTrue(silkDrops.stream().anyMatch(s -> s.is(ArtilianItems.CRYSTAL_TURF_ITEM.get())),
                "Crystal Turf with Silk Touch should drop Crystal Turf, got " + silkDrops);

        // 2. Crystal Bloom emits light 9 and survives on Crystal Turf.
        BlockPos bloomRel = turfRel.above();
        helper.setBlock(bloomRel, ArtilianBlocks.CRYSTAL_BLOOM.get());
        BlockState bloomState = helper.getBlockState(bloomRel);
        helper.assertTrue(bloomState.canSurvive(level, helper.absolutePos(bloomRel)),
                "Crystal Bloom should survive on Crystal Turf");
        int bloomLight = bloomState.getLightEmission(level, helper.absolutePos(bloomRel));
        helper.assertTrue(bloomLight == 9, "Expected Crystal Bloom light level 9, got " + bloomLight);

        // 3. Log -> 4 Planks recipe resolves cleanly.
        CraftingInput logInput = CraftingInput.of(1, 1, List.of(new ItemStack(ArtilianItems.CRYSTAL_CANOPY_LOG_ITEM.get())));
        Optional<RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>> planksRecipe =
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, logInput, level);
        helper.assertTrue(planksRecipe.isPresent(), "Expected Crystal Canopy Log -> Planks recipe");
        ItemStack planksOut = planksRecipe.get().value().assemble(logInput, level.registryAccess());
        helper.assertTrue(planksOut.is(ArtilianItems.CRYSTAL_CANOPY_PLANKS_ITEM.get()) && planksOut.getCount() == 4,
                "Expected 4x Crystal Canopy Planks from 1 Log, got " + planksOut);

        // 4. Sapling placed on Crystal Turf grows into a Crystal Canopy tree (logs + glowing leaves).
        helper.setBlock(bloomRel, Blocks.AIR);
        helper.setBlock(bloomRel, ArtilianBlocks.CRYSTAL_CANOPY_SAPLING.get());
        BlockPos saplingAbs = helper.absolutePos(bloomRel);
        BlockState saplingState = level.getBlockState(saplingAbs);
        SaplingBlock sapling = (SaplingBlock) saplingState.getBlock();
        for (int attempt = 0; attempt < 6 && level.getBlockState(saplingAbs).is(ArtilianBlocks.CRYSTAL_CANOPY_SAPLING.get()); attempt++) {
            sapling.advanceTree(level, saplingAbs, level.getBlockState(saplingAbs), level.getRandom());
        }

        boolean foundLog = false;
        boolean foundLeaves = false;
        for (BlockPos check : BlockPos.betweenClosed(saplingAbs.offset(-4, 0, -4), saplingAbs.offset(4, 12, 4))) {
            BlockState checkState = level.getBlockState(check);
            if (checkState.is(ArtilianBlocks.CRYSTAL_CANOPY_LOG.get())) {
                foundLog = true;
            } else if (checkState.is(ArtilianBlocks.CRYSTAL_CANOPY_LEAVES.get())) {
                foundLeaves = true;
                int leafLight = checkState.getLightEmission(level, check);
                helper.assertTrue(leafLight == 6, "Expected Crystal Canopy Leaves light 6, got " + leafLight);
            }
        }
        helper.assertTrue(foundLog && foundLeaves,
                "Expected grown Crystal Canopy tree with both log (" + foundLog + ") and leaves (" + foundLeaves + ")");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crystalCanopyWoodsRegisteredInOverworldWithTerraBlenderRegion(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder.Reference<Biome> biomeHolder = biomes.getHolder(ArtilianBiomes.CRYSTAL_CANOPY_WOODS)
                .orElseThrow(() -> new AssertionError("Missing Crystal Canopy Woods biome in registry"));

        helper.assertTrue(biomeHolder.is(BiomeTags.IS_OVERWORLD),
                "Crystal Canopy Woods must be tagged minecraft:is_overworld for Overworld ore injection");
        helper.assertTrue(biomeHolder.is(BiomeTags.IS_FOREST),
                "Crystal Canopy Woods must be tagged minecraft:is_forest");
        helper.assertTrue(biomeHolder.is(Tags.Biomes.IS_MAGICAL),
                "Crystal Canopy Woods must be tagged c:is_magical");

        Optional<Region> artilianRegion = Regions.get(RegionType.OVERWORLD).stream()
                .filter(region -> ArtilianMod.id("overworld_region").equals(region.getName()))
                .findFirst();
        helper.assertTrue(artilianRegion.isPresent(),
                "TerraBlender OVERWORLD regions should contain artilian:overworld_region");

        List<ResourceKey<Biome>> mappedBiomes = new ArrayList<>();
        artilianRegion.get().addBiomes(biomes, pair -> mappedBiomes.add(pair.getSecond()));
        helper.assertTrue(mappedBiomes.contains(ArtilianBiomes.CRYSTAL_CANOPY_WOODS),
                "artilian:overworld_region must emit Crystal Canopy Woods into Overworld climate points");

        helper.assertTrue(ArtilianSurfaceRules.makeRules() != null,
                "Artilian surface rules must build non-null RuleSource");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void crystalCanopyWoodsWorldgenFeaturesAndBloomPatchWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Registry<PlacedFeature> placedRegistry = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        Biome crystalCanopy = biomes.getHolderOrThrow(ArtilianBiomes.CRYSTAL_CANOPY_WOODS).value();

        Holder<PlacedFeature> treePlaced = placedRegistry.getHolderOrThrow(ArtilianWorldGen.CRYSTAL_CANOPY_TREE_PLACED);
        Holder<PlacedFeature> bloomPlaced = placedRegistry.getHolderOrThrow(ArtilianWorldGen.CRYSTAL_BLOOM_PATCH_PLACED);
        Holder<PlacedFeature> sunsteelPlaced = placedRegistry.getHolderOrThrow(ArtilianWorldGen.ORE_SUNSTEEL_PLACED);
        Holder<PlacedFeature> starlightPlaced = placedRegistry.getHolderOrThrow(ArtilianWorldGen.ORE_STARLIGHT_CRYSTAL_PLACED);

        List<HolderSet<PlacedFeature>> features = crystalCanopy.getGenerationSettings().features();
        int vegStep = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
        int oreStep = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();
        helper.assertTrue(vegStep < features.size() && features.get(vegStep).contains(treePlaced),
                "Crystal Canopy Woods missing Crystal Canopy Tree placed feature");
        helper.assertTrue(features.get(vegStep).contains(bloomPlaced),
                "Crystal Canopy Woods missing Crystal Bloom patch placed feature");
        helper.assertTrue(oreStep < features.size() && features.get(oreStep).contains(sunsteelPlaced),
                "Crystal Canopy Woods missing Sunsteel Ore injection");
        helper.assertTrue(features.get(oreStep).contains(starlightPlaced),
                "Crystal Canopy Woods missing Starlight Crystal Ore injection");

        // Verify Crystal Bloom patch places flowers naturally onto a Crystal Turf floor.
        for (int x = 3; x <= 11; x++) {
            for (int z = 3; z <= 11; z++) {
                helper.setBlock(new BlockPos(x, 1, z), ArtilianBlocks.CRYSTAL_TURF.get());
            }
        }
        ConfiguredFeature<?, ?> bloomFeature = level.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolderOrThrow(ArtilianWorldGen.CRYSTAL_BLOOM_PATCH)
                .value();
        boolean placed = bloomFeature.place(
                level,
                level.getChunkSource().getGenerator(),
                RandomSource.create(1234L),
                helper.absolutePos(new BlockPos(7, 2, 7)));
        int bloomsFound = 0;
        for (int x = 1; x <= 13; x++) {
            for (int z = 1; z <= 13; z++) {
                if (helper.getBlockState(new BlockPos(x, 2, z)).is(ArtilianBlocks.CRYSTAL_BLOOM.get())) {
                    bloomsFound++;
                }
            }
        }
        helper.assertTrue(placed && bloomsFound > 0,
                "Expected Crystal Bloom patch to place glowing blooms on Crystal Turf, found " + bloomsFound);
        helper.succeed();
    }
}
