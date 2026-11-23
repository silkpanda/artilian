package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.DragonJungleDimension;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.worldgen.ArtilianWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.Set;

/** Phase 5 Stage 2: Dragon Jungle noise terrain, multi-biome source, mega Dragonwood trees, and Amber Moss. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class DragonJungleTerrainGameTests {

    @GameTest(template = "empty")
    public static void dragonJungleUsesNoiseSettingsAndMultiBiomeSource(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();

        NoiseGeneratorSettings noise = registries.registryOrThrow(Registries.NOISE_SETTINGS)
                .get(DragonJungleDimension.DRAGON_JUNGLE_NOISE);
        helper.assertTrue(noise != null, "DRAGON_JUNGLE_NOISE should be registered");
        helper.assertTrue(noise.defaultBlock().is(ArtilianBlocks.PRIMAL_BASALT.get()),
                "Dragon Jungle noise defaultBlock should be Primal Basalt");

        helper.assertTrue(
                DragonJungleTerrainGameTests.class.getClassLoader()
                        .getResource("data/artilian/worldgen/noise_settings/dragon_jungle.json") != null,
                "data/artilian/worldgen/noise_settings/dragon_jungle.json should be generated");

        ServerLevel dragonJungle = helper.getLevel().getServer().getLevel(DragonJungleDimension.DRAGON_JUNGLE_LEVEL);
        if (dragonJungle != null) {
            ChunkGenerator generator = dragonJungle.getChunkSource().getGenerator();
            helper.assertTrue(generator instanceof NoiseBasedChunkGenerator,
                    "Dragon Jungle should use NoiseBasedChunkGenerator instead of FlatLevelSource");

            Set<ResourceKey<Biome>> possible = new HashSet<>();
            for (Holder<Biome> holder : generator.getBiomeSource().possibleBiomes()) {
                holder.unwrapKey().ifPresent(possible::add);
            }
            helper.assertTrue(possible.contains(DragonJungleDimension.DRAGON_JUNGLE_BIOME),
                    "Dragon Jungle biome source must include artilian:dragon_jungle");
            helper.assertTrue(possible.contains(DragonJungleDimension.AMBER_GROVE_BIOME),
                    "Dragon Jungle biome source must include artilian:amber_volcanic_grove");
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dragonJungleBiomesContainMegaTreesAndAmberMoss(GameTestHelper helper) {
        var biomeRegistry = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);

        Biome jungle = biomeRegistry.get(DragonJungleDimension.DRAGON_JUNGLE_BIOME);
        helper.assertTrue(jungle != null, "DRAGON_JUNGLE_BIOME should be registered");
        Set<String> jungleFeatures = placedFeatureIds(jungle);
        helper.assertTrue(jungleFeatures.contains(ArtilianWorldGen.MEGA_DRAGONWOOD_TREE_PLACED.location().toString()),
                "Dragon Jungle biome should include artilian:mega_dragonwood_tree_placed");
        helper.assertTrue(jungleFeatures.contains(ArtilianWorldGen.DRAGONWOOD_TREE_PLACED.location().toString()),
                "Dragon Jungle biome should include artilian:dragonwood_tree_placed");
        helper.assertTrue(jungleFeatures.contains(ArtilianWorldGen.AMBER_MOSS_PATCH_PLACED.location().toString()),
                "Dragon Jungle biome should include artilian:amber_moss_patch_placed");

        Biome amberGrove = biomeRegistry.get(DragonJungleDimension.AMBER_GROVE_BIOME);
        helper.assertTrue(amberGrove != null, "AMBER_GROVE_BIOME should be registered");
        Set<String> groveFeatures = placedFeatureIds(amberGrove);
        helper.assertTrue(groveFeatures.contains(ArtilianWorldGen.DRAGONWOOD_TREE_PLACED.location().toString()),
                "Amber Volcanic Grove biome should include artilian:dragonwood_tree_placed");
        helper.assertTrue(groveFeatures.contains(ArtilianWorldGen.AMBER_MOSS_PATCH_PLACED.location().toString()),
                "Amber Volcanic Grove biome should include artilian:amber_moss_patch_placed");

        helper.succeed();
    }

    @GameTest(template = "worldgen")
    public static void dragonwoodSaplingGrowsSingleAndMegaTreesOnJungleTurf(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SaplingBlock sapling = (SaplingBlock) ArtilianBlocks.DRAGONWOOD_SAPLING.get();

        // 1. Single 1x1 Dragonwood tree
        BlockPos singleGround = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlockAndUpdate(singleGround, ArtilianBlocks.JUNGLE_TURF.get().defaultBlockState());
        BlockPos singleSapling = singleGround.above();
        for (BlockPos clear : BlockPos.betweenClosed(singleSapling.offset(-2, 0, -2), singleSapling.offset(2, 12, 2))) {
            level.setBlockAndUpdate(clear, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(singleSapling, sapling.defaultBlockState());

        for (int i = 0; i < 6 && level.getBlockState(singleSapling).is(sapling); i++) {
            sapling.advanceTree(level, singleSapling, level.getBlockState(singleSapling), level.random);
        }
        helper.assertTrue(level.getBlockState(singleSapling).is(ArtilianBlocks.DRAGONWOOD_LOG.get()),
                "1x1 Dragonwood Sapling should grow into Dragonwood Log on Jungle Turf");

        boolean foundLeaves = false;
        for (BlockPos pos : BlockPos.betweenClosed(singleSapling.offset(-3, 3, -3), singleSapling.offset(3, 12, 3))) {
            if (level.getBlockState(pos).is(ArtilianBlocks.DRAGONWOOD_LEAVES.get())) {
                foundLeaves = true;
                break;
            }
        }
        helper.assertTrue(foundLeaves, "1x1 Dragonwood tree should generate Dragonwood Leaves");

        // 2. Mega 2x2 Dragonwood tree
        BlockPos megaBase = helper.absolutePos(new BlockPos(10, 1, 10));
        for (BlockPos clear : BlockPos.betweenClosed(megaBase.offset(-4, 1, -4), megaBase.offset(5, 32, 5))) {
            level.setBlockAndUpdate(clear, Blocks.AIR.defaultBlockState());
        }
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                level.setBlockAndUpdate(megaBase.offset(dx, 0, dz), ArtilianBlocks.JUNGLE_TURF.get().defaultBlockState());
                level.setBlockAndUpdate(megaBase.offset(dx, 1, dz), sapling.defaultBlockState());
            }
        }

        BlockPos megaSapling = megaBase.above();
        for (int i = 0; i < 6 && level.getBlockState(megaSapling).is(sapling); i++) {
            sapling.advanceTree(level, megaSapling, level.getBlockState(megaSapling), level.random);
        }
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                helper.assertTrue(
                        level.getBlockState(megaSapling.offset(dx, 0, dz)).is(ArtilianBlocks.DRAGONWOOD_LOG.get()),
                        "2x2 Dragonwood Saplings should grow a 2x2 Dragonwood Log trunk");
            }
        }
        helper.assertTrue(level.getBlockState(megaSapling.above(10)).is(ArtilianBlocks.DRAGONWOOD_LOG.get()),
                "Mega Dragonwood trunk should reach at least 11 blocks tall");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dragonJungleBlocksHaveExpectedTagsAndLightLevels(GameTestHelper helper) {
        BlockState turf = ArtilianBlocks.JUNGLE_TURF.get().defaultBlockState();
        BlockState basalt = ArtilianBlocks.PRIMAL_BASALT.get().defaultBlockState();
        BlockState amberMoss = ArtilianBlocks.AMBER_MOSS.get().defaultBlockState();
        BlockState dragonwoodLog = ArtilianBlocks.DRAGONWOOD_LOG.get().defaultBlockState();
        BlockState dragonwoodPlanks = ArtilianBlocks.DRAGONWOOD_PLANKS.get().defaultBlockState();
        BlockState dragonwoodLeaves = ArtilianBlocks.DRAGONWOOD_LEAVES.get().defaultBlockState();
        BlockState dragonwoodSapling = ArtilianBlocks.DRAGONWOOD_SAPLING.get().defaultBlockState();

        helper.assertTrue(turf.is(BlockTags.DIRT), "JUNGLE_TURF must be in BlockTags.DIRT so saplings can grow on it");
        helper.assertTrue(turf.is(BlockTags.ANIMALS_SPAWNABLE_ON), "JUNGLE_TURF must be in ANIMALS_SPAWNABLE_ON");
        helper.assertTrue(basalt.is(BlockTags.MINEABLE_WITH_PICKAXE) && basalt.is(BlockTags.NEEDS_STONE_TOOL),
                "PRIMAL_BASALT must be mineable with stone+ pickaxes");
        helper.assertTrue(dragonwoodLog.is(BlockTags.LOGS_THAT_BURN), "DRAGONWOOD_LOG must be in LOGS_THAT_BURN");
        helper.assertTrue(dragonwoodPlanks.is(BlockTags.PLANKS), "DRAGONWOOD_PLANKS must be in PLANKS");
        helper.assertTrue(amberMoss.getLightEmission() == 8, "AMBER_MOSS should emit light level 8");
        helper.assertTrue(dragonwoodLeaves.getLightEmission() == 3, "DRAGONWOOD_LEAVES should emit light level 3");
        helper.assertTrue(dragonwoodSapling.getLightEmission() == 5, "DRAGONWOOD_SAPLING should emit light level 5");

        helper.succeed();
    }

    private static Set<String> placedFeatureIds(Biome biome) {
        Set<String> placedIds = new HashSet<>();
        for (var step : biome.getGenerationSettings().features()) {
            for (Holder<PlacedFeature> holder : step) {
                holder.unwrapKey().ifPresent(key -> placedIds.add(key.location().toString()));
            }
        }
        return placedIds;
    }
}
