package com.silkpanda.artilian.dimension;

import com.mojang.datafixers.util.Pair;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.worldgen.ArtilianWorldGen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

import java.util.List;
import java.util.OptionalLong;

/**
 * Resource keys and datagen bootstraps for Steve's Giant Dragon Jungle dimension.
 *
 * <p>Phase 5 Stage 2 replaces Stage 1's flat placeholder with prehistoric mega-jungle
 * noise terrain, giant 2x2 Dragonwood trees, glowing Amber Moss, and two distinct biomes.
 */
public final class DragonJungleDimension {
    private DragonJungleDimension() {}

    public static final ResourceLocation DRAGON_JUNGLE_EFFECTS = ArtilianMod.id("dragon_jungle");
    public static final ResourceKey<Level> DRAGON_JUNGLE_LEVEL =
            ResourceKey.create(Registries.DIMENSION, ArtilianMod.id("dragon_jungle"));
    public static final ResourceKey<DimensionType> DRAGON_JUNGLE_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ArtilianMod.id("dragon_jungle"));
    public static final ResourceKey<LevelStem> DRAGON_JUNGLE_STEM =
            ResourceKey.create(Registries.LEVEL_STEM, ArtilianMod.id("dragon_jungle"));
    public static final ResourceKey<Biome> DRAGON_JUNGLE_BIOME =
            ResourceKey.create(Registries.BIOME, ArtilianMod.id("dragon_jungle"));
    public static final ResourceKey<Biome> AMBER_GROVE_BIOME =
            ResourceKey.create(Registries.BIOME, ArtilianMod.id("amber_volcanic_grove"));
    public static final ResourceKey<NoiseGeneratorSettings> DRAGON_JUNGLE_NOISE =
            ResourceKey.create(Registries.NOISE_SETTINGS, ArtilianMod.id("dragon_jungle"));

    public static void bootstrapDimensionType(BootstrapContext<DimensionType> context) {
        context.register(
                DRAGON_JUNGLE_TYPE,
                new DimensionType(
                        OptionalLong.empty(), // Normal day/night cycle for lush jungle sunlight
                        true,  // hasSkyLight
                        false, // hasCeiling
                        false, // ultrawarm
                        true,  // natural
                        1.0,   // coordinateScale (1:1 with Overworld for kid-friendly travel)
                        false, // bedWorks
                        false, // respawnAnchorWorks
                        -64,   // minY
                        384,   // height
                        384,   // logicalHeight
                        BlockTags.INFINIBURN_OVERWORLD,
                        DRAGON_JUNGLE_EFFECTS,
                        0.25F, // ambientLight so caves and nights stay readable for kids
                        new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));
    }

    public static void bootstrapBiome(BootstrapContext<Biome> context) {
        // 1. Primary Mega-Jungle Canopy biome
        BiomeGenerationSettings.Builder jungleGen = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER));
        jungleGen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.MEGA_DRAGONWOOD_TREE_PLACED);
        jungleGen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.DRAGONWOOD_TREE_PLACED);
        jungleGen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.AMBER_MOSS_PATCH_PLACED);
        BiomeDefaultFeatures.addJungleGrass(jungleGen);
        BiomeDefaultFeatures.addDefaultExtraVegetation(jungleGen);

        BiomeSpecialEffects jungleEffects = new BiomeSpecialEffects.Builder()
                .skyColor(0x5AC8FA)
                .fogColor(0x9BE7C4)
                .waterColor(0x2DD4BF)
                .waterFogColor(0x134E4A)
                .grassColorOverride(0x22C55E)
                .foliageColorOverride(0x16A34A)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .ambientParticle(new AmbientParticleSettings(ParticleTypes.SPORE_BLOSSOM_AIR, 0.005F))
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JUNGLE))
                .build();

        MobSpawnSettings.Builder jungleSpawns = new MobSpawnSettings.Builder();
        jungleSpawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ArtilianEntities.CANOPY_WYVERN.get(), 18, 1, 2));

        context.register(
                DRAGON_JUNGLE_BIOME,
                new Biome.BiomeBuilder()
                        .hasPrecipitation(true)
                        .temperature(0.95F)
                        .downfall(0.9F)
                        .specialEffects(jungleEffects)
                        .mobSpawnSettings(jungleSpawns.build())
                        .generationSettings(jungleGen.build())
                        .build());

        // 2. Amber Volcanic Grove biome
        BiomeGenerationSettings.Builder amberGen = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER));
        amberGen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.DRAGONWOOD_TREE_PLACED);
        amberGen.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.AMBER_MOSS_PATCH_PLACED);
        BiomeDefaultFeatures.addFerns(amberGen);
        BiomeDefaultFeatures.addForestGrass(amberGen);

        BiomeSpecialEffects amberEffects = new BiomeSpecialEffects.Builder()
                .skyColor(0xFDBA74)
                .fogColor(0xFED7AA)
                .waterColor(0xF59E0B)
                .waterFogColor(0x78350F)
                .grassColorOverride(0x84CC16)
                .foliageColorOverride(0xF97316)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .ambientParticle(new AmbientParticleSettings(ParticleTypes.FLAME, 0.002F))
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SPARSE_JUNGLE))
                .build();

        context.register(
                AMBER_GROVE_BIOME,
                new Biome.BiomeBuilder()
                        .hasPrecipitation(false)
                        .temperature(1.2F)
                        .downfall(0.3F)
                        .specialEffects(amberEffects)
                        .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                        .generationSettings(amberGen.build())
                        .build());
    }

    public static void bootstrapNoiseSettings(BootstrapContext<NoiseGeneratorSettings> context) {
        SurfaceRules.RuleSource bedrockFloor = SurfaceRules.ifTrue(
                SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
                SurfaceRules.state(Blocks.BEDROCK.defaultBlockState()));

        SurfaceRules.RuleSource jungleSurface = SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                                SurfaceRules.steep(),
                                SurfaceRules.state(ArtilianBlocks.PRIMAL_BASALT.get().defaultBlockState())),
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.ifTrue(
                                        SurfaceRules.waterBlockCheck(-1, 0),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.isBiome(AMBER_GROVE_BIOME),
                                                        SurfaceRules.state(Blocks.MOSS_BLOCK.defaultBlockState())),
                                                SurfaceRules.state(ArtilianBlocks.JUNGLE_TURF.get().defaultBlockState())))),
                        SurfaceRules.ifTrue(
                                SurfaceRules.UNDER_FLOOR,
                                SurfaceRules.state(Blocks.DIRT.defaultBlockState()))));

        SurfaceRules.RuleSource rules = SurfaceRules.sequence(
                bedrockFloor,
                jungleSurface,
                SurfaceRuleData.overworld());

        NoiseGeneratorSettings overworldBase = NoiseGeneratorSettings.overworld(context, false, false);

        context.register(
                DRAGON_JUNGLE_NOISE,
                new NoiseGeneratorSettings(
                        NoiseSettings.create(-64, 384, 1, 2),
                        ArtilianBlocks.PRIMAL_BASALT.get().defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),
                        overworldBase.noiseRouter(),
                        rules,
                        List.of(),
                        63,
                        false,
                        true,
                        true,
                        false));
    }

    public static void bootstrapLevelStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);

        Holder<DimensionType> typeHolder = dimensionTypes.getOrThrow(DRAGON_JUNGLE_TYPE);
        Holder<Biome> jungleHolder = biomes.getOrThrow(DRAGON_JUNGLE_BIOME);
        Holder<Biome> amberGroveHolder = biomes.getOrThrow(AMBER_GROVE_BIOME);
        Holder<NoiseGeneratorSettings> noiseHolder = noiseSettings.getOrThrow(DRAGON_JUNGLE_NOISE);

        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.createFromList(
                new Climate.ParameterList<>(List.of(
                        Pair.of(Climate.parameters(
                                Climate.Parameter.span(-1.0F, 0.35F),
                                Climate.Parameter.span(-1.0F, 1.0F),
                                Climate.Parameter.span(-1.0F, 1.0F),
                                Climate.Parameter.span(-1.0F, 1.0F),
                                Climate.Parameter.point(0.0F),
                                Climate.Parameter.span(-1.0F, 1.0F),
                                0.0F), jungleHolder),
                        Pair.of(Climate.parameters(
                                Climate.Parameter.span(0.35F, 1.0F),
                                Climate.Parameter.span(-1.0F, 1.0F),
                                Climate.Parameter.span(-1.0F, 1.0F),
                                Climate.Parameter.span(-1.0F, 1.0F),
                                Climate.Parameter.point(0.0F),
                                Climate.Parameter.span(-1.0F, 1.0F),
                                0.0F), amberGroveHolder)
                )));

        context.register(
                DRAGON_JUNGLE_STEM,
                new LevelStem(typeHolder, new NoiseBasedChunkGenerator(biomeSource, noiseHolder)));
    }
}
