package com.silkpanda.artilian.dimension;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Resource keys and datagen bootstraps for Steve's Giant Dragon Jungle dimension.
 *
 * <p>Phase 5 Stage 1 establishes the dimension, biome, and flat mossy arrival terrain
 * (replaced in Stage 2 with prehistoric mega-jungle biomes, giant trees, and noise terrain).
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
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER));

        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();

        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
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

        context.register(
                DRAGON_JUNGLE_BIOME,
                new Biome.BiomeBuilder()
                        .hasPrecipitation(true)
                        .temperature(0.95F)
                        .downfall(0.9F)
                        .specialEffects(effects)
                        .mobSpawnSettings(spawns.build())
                        .generationSettings(generation.build())
                        .build());
    }

    public static void bootstrapLevelStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        Holder<DimensionType> typeHolder = dimensionTypes.getOrThrow(DRAGON_JUNGLE_TYPE);
        Holder<Biome> biomeHolder = biomes.getOrThrow(DRAGON_JUNGLE_BIOME);

        FlatLevelGeneratorSettings flat = new FlatLevelGeneratorSettings(
                Optional.empty(), biomeHolder, List.of());
        flat.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        flat.getLayersInfo().add(new FlatLayerInfo(123, Blocks.STONE));
        flat.getLayersInfo().add(new FlatLayerInfo(3, Blocks.DIRT));
        flat.getLayersInfo().add(new FlatLayerInfo(1, Blocks.MOSS_BLOCK));
        flat.updateLayers();

        context.register(
                DRAGON_JUNGLE_STEM,
                new LevelStem(typeHolder, new FlatLevelSource(flat)));
    }
}
