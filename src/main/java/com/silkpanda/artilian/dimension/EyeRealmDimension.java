package com.silkpanda.artilian.dimension;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Resource keys and datagen bootstraps for Alex's Eye Realm dimension.
 *
 * <p>Phase 4 Stage 1 establishes the dimension, biome palette, and flat placeholder
 * terrain so the Gazing Portal works end-to-end before Stage 2 adds eye terrain.
 */
public final class EyeRealmDimension {
    private EyeRealmDimension() {}

    public static final ResourceKey<Level> EYE_REALM_LEVEL =
            ResourceKey.create(Registries.DIMENSION, ArtilianMod.id("eye_realm"));
    public static final ResourceKey<DimensionType> EYE_REALM_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ArtilianMod.id("eye_realm"));
    public static final ResourceKey<LevelStem> EYE_REALM_STEM =
            ResourceKey.create(Registries.LEVEL_STEM, ArtilianMod.id("eye_realm"));
    public static final ResourceKey<Biome> EYE_REALM_BIOME =
            ResourceKey.create(Registries.BIOME, ArtilianMod.id("eye_realm"));

    public static void bootstrapDimensionType(BootstrapContext<DimensionType> context) {
        context.register(
                EYE_REALM_TYPE,
                new DimensionType(
                        OptionalLong.of(18000L), // Eternal twilight
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
                        BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                        0.25F, // ambientLight so kids can always see clearly
                        new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));
    }

    public static void bootstrapBiome(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER));

        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();

        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .skyColor(0x3D1E5A)
                .fogColor(0x5B2178)
                .waterColor(0x7DF9FF)
                .waterFogColor(0x2B2338)
                .grassColorOverride(0x9C4DCC)
                .foliageColorOverride(0xD946EF)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .ambientParticle(new AmbientParticleSettings(ParticleTypes.PORTAL, 0.004F))
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_END))
                .build();

        context.register(
                EYE_REALM_BIOME,
                new Biome.BiomeBuilder()
                        .hasPrecipitation(false)
                        .temperature(0.7F)
                        .downfall(0.2F)
                        .specialEffects(effects)
                        .mobSpawnSettings(spawns.build())
                        .generationSettings(generation.build())
                        .build());
    }

    public static void bootstrapLevelStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        Holder<DimensionType> typeHolder = dimensionTypes.getOrThrow(EYE_REALM_TYPE);
        Holder<Biome> biomeHolder = biomes.getOrThrow(EYE_REALM_BIOME);

        // Flat placeholder terrain: bedrock (-64) + deepslate (-63..59) + starlight_slate (60..63) => surface at Y=64
        FlatLevelGeneratorSettings flatSettings = new FlatLevelGeneratorSettings(
                Optional.empty(),
                biomeHolder,
                List.of());
        flatSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        flatSettings.getLayersInfo().add(new FlatLayerInfo(123, Blocks.DEEPSLATE));
        flatSettings.getLayersInfo().add(new FlatLayerInfo(4, ArtilianBlocks.STARLIGHT_SLATE.get()));
        flatSettings.updateLayers();

        context.register(
                EYE_REALM_STEM,
                new LevelStem(typeHolder, new FlatLevelSource(flatSettings)));
    }
}
