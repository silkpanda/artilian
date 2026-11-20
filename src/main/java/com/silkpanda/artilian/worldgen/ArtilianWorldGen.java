package com.silkpanda.artilian.worldgen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

/** Overworld ore and biome vegetation worldgen definitions. */
public final class ArtilianWorldGen {
    private ArtilianWorldGen() {}

    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_SUNSTEEL =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("ore_sunsteel"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_STARLIGHT_CRYSTAL =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("ore_starlight_crystal"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTAL_CANOPY_TREE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("crystal_canopy_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTAL_BLOOM_PATCH =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("crystal_bloom_patch"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> EMBERWOOD_TREE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("emberwood_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> EMBER_BLOOM_PATCH =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("ember_bloom_patch"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> STARLIGHT_SLATE_BOULDER =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("starlight_slate_boulder"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> STARLIGHT_BLOSSOM_PATCH =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("starlight_blossom_patch"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> OCULAR_TREE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("ocular_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> BLINKING_EYE_PATCH =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ArtilianMod.id("blinking_eye_patch"));

    public static final ResourceKey<PlacedFeature> ORE_SUNSTEEL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("ore_sunsteel_placed"));
    public static final ResourceKey<PlacedFeature> ORE_STARLIGHT_CRYSTAL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("ore_starlight_crystal_placed"));
    public static final ResourceKey<PlacedFeature> CRYSTAL_CANOPY_TREE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("crystal_canopy_tree_placed"));
    public static final ResourceKey<PlacedFeature> CRYSTAL_BLOOM_PATCH_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("crystal_bloom_patch_placed"));
    public static final ResourceKey<PlacedFeature> EMBERWOOD_TREE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("emberwood_tree_placed"));
    public static final ResourceKey<PlacedFeature> EMBER_BLOOM_PATCH_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("ember_bloom_patch_placed"));
    public static final ResourceKey<PlacedFeature> STARLIGHT_SLATE_BOULDER_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("starlight_slate_boulder_placed"));
    public static final ResourceKey<PlacedFeature> STARLIGHT_BLOSSOM_PATCH_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("starlight_blossom_patch_placed"));
    public static final ResourceKey<PlacedFeature> OCULAR_TREE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("ocular_tree_placed"));
    public static final ResourceKey<PlacedFeature> BLINKING_EYE_PATCH_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ArtilianMod.id("blinking_eye_patch_placed"));

    public static final ResourceKey<BiomeModifier> ADD_SUNSTEEL_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ArtilianMod.id("add_sunsteel_ore"));
    public static final ResourceKey<BiomeModifier> ADD_STARLIGHT_CRYSTAL_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ArtilianMod.id("add_starlight_crystal_ore"));
    public static final ResourceKey<BiomeModifier> ADD_CINDER_CRAG_OVERWORLD =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ArtilianMod.id("add_cinder_crag_overworld"));
    public static final ResourceKey<BiomeModifier> ADD_CINDER_CRAG_NETHER =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ArtilianMod.id("add_cinder_crag_nether"));

    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> sunsteelTargets = List.of(
                OreConfiguration.target(stoneReplaceables, ArtilianBlocks.SUNSTEEL_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE.get().defaultBlockState())
        );
        context.register(ORE_SUNSTEEL, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(sunsteelTargets, 9)));

        List<OreConfiguration.TargetBlockState> starlightTargets = List.of(
                OreConfiguration.target(stoneReplaceables, ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE.get().defaultBlockState())
        );
        context.register(ORE_STARLIGHT_CRYSTAL, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(starlightTargets, 6)));

        context.register(CRYSTAL_CANOPY_TREE, new ConfiguredFeature<>(
                Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(ArtilianBlocks.CRYSTAL_CANOPY_LOG.get()),
                        new StraightTrunkPlacer(5, 2, 1),
                        BlockStateProvider.simple(ArtilianBlocks.CRYSTAL_CANOPY_LEAVES.get()),
                        new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(2), 3),
                        new TwoLayersFeatureSize(1, 0, 1))
                        .dirt(BlockStateProvider.simple(Blocks.DIRT))
                        .build()));

        context.register(CRYSTAL_BLOOM_PATCH, flowerPatch(ArtilianBlocks.CRYSTAL_BLOOM.get(), 28, 6, 2));

        context.register(EMBERWOOD_TREE, new ConfiguredFeature<>(
                Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(ArtilianBlocks.EMBERWOOD_LOG.get()),
                        new ForkingTrunkPlacer(5, 2, 2),
                        BlockStateProvider.simple(ArtilianBlocks.EMBERWOOD_LEAVES.get()),
                        new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)),
                        new TwoLayersFeatureSize(1, 0, 2))
                        .dirt(BlockStateProvider.simple(Blocks.DIRT))
                        .build()));

        context.register(EMBER_BLOOM_PATCH, flowerPatch(ArtilianBlocks.EMBER_BLOOM.get(), 32, 7, 2));

        context.register(STARLIGHT_SLATE_BOULDER, new ConfiguredFeature<>(
                Feature.FOREST_ROCK,
                new BlockStateConfiguration(ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState())));

        context.register(STARLIGHT_BLOSSOM_PATCH, flowerPatch(ArtilianBlocks.STARLIGHT_BLOSSOM.get(), 24, 6, 2));

        context.register(OCULAR_TREE, new ConfiguredFeature<>(
                Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(ArtilianBlocks.OCULAR_LOG.get()),
                        new StraightTrunkPlacer(4, 2, 1),
                        BlockStateProvider.simple(ArtilianBlocks.OCULAR_LEAVES.get()),
                        new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(2), 3),
                        new TwoLayersFeatureSize(1, 0, 1))
                        .dirt(BlockStateProvider.simple(ArtilianBlocks.GAZE_STONE.get()))
                        .build()));

        context.register(BLINKING_EYE_PATCH, new ConfiguredFeature<>(
                Feature.RANDOM_PATCH,
                new RandomPatchConfiguration(
                        8,
                        5,
                        2,
                        PlacementUtils.onlyWhenEmpty(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(
                                        BlockStateProvider.simple(ArtilianBlocks.BLINKING_EYE_BLOCK.get()))))));
    }

    private static ConfiguredFeature<RandomPatchConfiguration, Feature<RandomPatchConfiguration>> flowerPatch(
            Block flower, int tries, int xzSpread, int ySpread) {
        return new ConfiguredFeature<>(
                Feature.FLOWER,
                new RandomPatchConfiguration(
                        tries,
                        xzSpread,
                        ySpread,
                        PlacementUtils.onlyWhenEmpty(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(BlockStateProvider.simple(flower)))));
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(ORE_SUNSTEEL_PLACED, new PlacedFeature(
                configured.getOrThrow(ORE_SUNSTEEL),
                orePlacement(12, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(72)))));

        context.register(ORE_STARLIGHT_CRYSTAL_PLACED, new PlacedFeature(
                configured.getOrThrow(ORE_STARLIGHT_CRYSTAL),
                orePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(24)))));

        context.register(CRYSTAL_CANOPY_TREE_PLACED, new PlacedFeature(
                configured.getOrThrow(CRYSTAL_CANOPY_TREE),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(8, 0.1F, 1),
                        ArtilianBlocks.CRYSTAL_CANOPY_SAPLING.get())));

        context.register(CRYSTAL_BLOOM_PATCH_PLACED, new PlacedFeature(
                configured.getOrThrow(CRYSTAL_BLOOM_PATCH),
                surfacePatch(3)));

        context.register(EMBERWOOD_TREE_PLACED, new PlacedFeature(
                configured.getOrThrow(EMBERWOOD_TREE),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(1, 0.2F, 1),
                        ArtilianBlocks.EMBERWOOD_SAPLING.get())));

        context.register(EMBER_BLOOM_PATCH_PLACED, new PlacedFeature(
                configured.getOrThrow(EMBER_BLOOM_PATCH),
                surfacePatch(5)));

        context.register(STARLIGHT_SLATE_BOULDER_PLACED, new PlacedFeature(
                configured.getOrThrow(STARLIGHT_SLATE_BOULDER),
                surfacePatch(2)));

        context.register(STARLIGHT_BLOSSOM_PATCH_PLACED, new PlacedFeature(
                configured.getOrThrow(STARLIGHT_BLOSSOM_PATCH),
                surfacePatch(4)));

        context.register(OCULAR_TREE_PLACED, new PlacedFeature(
                configured.getOrThrow(OCULAR_TREE),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(5, 0.1F, 1),
                        ArtilianBlocks.OCULAR_SAPLING.get())));

        context.register(BLINKING_EYE_PATCH_PLACED, new PlacedFeature(
                configured.getOrThrow(BLINKING_EYE_PATCH),
                surfacePatch(2)));
    }

    private static List<PlacementModifier> surfacePatch(int countPerChunk) {
        return List.of(
                CountPlacement.of(countPerChunk),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome());
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);

        context.register(ADD_SUNSTEEL_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placed.getOrThrow(ORE_SUNSTEEL_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_STARLIGHT_CRYSTAL_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placed.getOrThrow(ORE_STARLIGHT_CRYSTAL_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        MobSpawnSettings.SpawnerData cinderCragSpawn =
                new MobSpawnSettings.SpawnerData(ArtilianEntities.CINDER_CRAG.get(), 8, 1, 2);
        context.register(ADD_CINDER_CRAG_OVERWORLD, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                cinderCragSpawn));
        context.register(ADD_CINDER_CRAG_NETHER, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                cinderCragSpawn));
    }

    private static List<PlacementModifier> orePlacement(int veinsPerChunk, PlacementModifier heightRange) {
        return List.of(
                CountPlacement.of(veinsPerChunk),
                InSquarePlacement.spread(),
                heightRange,
                BiomeFilter.biome());
    }
}
