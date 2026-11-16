package com.silkpanda.artilian.worldgen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianEntities;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Custom Artilian Overworld biomes. */
public final class ArtilianBiomes {
    private ArtilianBiomes() {}

    public static final ResourceKey<Biome> CRYSTAL_CANOPY_WOODS =
            ResourceKey.create(Registries.BIOME, ArtilianMod.id("crystal_canopy_woods"));
    public static final ResourceKey<Biome> EMBERBLOOM_MEADOWS =
            ResourceKey.create(Registries.BIOME, ArtilianMod.id("emberbloom_meadows"));
    public static final ResourceKey<Biome> STARLIGHT_HIGHLANDS =
            ResourceKey.create(Registries.BIOME, ArtilianMod.id("starlight_highlands"));

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> worldCarvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(CRYSTAL_CANOPY_WOODS, buildCrystalCanopyWoods(placedFeatures, worldCarvers));
        context.register(EMBERBLOOM_MEADOWS, buildEmberbloomMeadows(placedFeatures, worldCarvers));
        context.register(STARLIGHT_HIGHLANDS, buildStarlightHighlands(placedFeatures, worldCarvers));
    }

    private static Biome buildCrystalCanopyWoods(
            HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder generation = baseGeneration(placedFeatures, worldCarvers);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.CRYSTAL_CANOPY_TREE_PLACED);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.CRYSTAL_BLOOM_PATCH_PLACED);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);

        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(spawns);
        BiomeDefaultFeatures.farmAnimals(spawns);
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ArtilianEntities.CRYSTAL_FOX.get(), 6, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ArtilianEntities.GALECORN.get(), 4, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ArtilianEntities.PEEPKIN.get(), 3, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 4, 2, 4));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));

        BiomeSpecialEffects effects = specialEffects(
                0x68D8FF, 0xA6EEFF, 0x38D9C0, 0x1C7C7D, 0x38D9C0, 0x5BE7D0,
                ParticleTypes.GLOW, 0.004F,
                Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST));

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.65F)
                .downfall(0.75F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome buildEmberbloomMeadows(
            HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder generation = baseGeneration(placedFeatures, worldCarvers);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.EMBERWOOD_TREE_PLACED);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.EMBER_BLOOM_PATCH_PLACED);
        BiomeDefaultFeatures.addMeadowVegetation(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);

        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(spawns);
        BiomeDefaultFeatures.farmAnimals(spawns);
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ArtilianEntities.GALECORN.get(), 6, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ArtilianEntities.EMBER_DRAKE.get(), 5, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 3, 2, 4));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 2, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));

        BiomeSpecialEffects effects = specialEffects(
                0xFFC07A, 0xFFE0B8, 0x4FD1C5, 0x226872, 0xF2B84B, 0xFF8C42,
                ParticleTypes.SMALL_FLAME, 0.0015F,
                Musics.createGameMusic(SoundEvents.MUSIC_BIOME_MEADOW));

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.85F)
                .downfall(0.45F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome buildStarlightHighlands(
            HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder generation = baseGeneration(placedFeatures, worldCarvers);
        generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, ArtilianWorldGen.STARLIGHT_SLATE_BOULDER_PLACED);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ArtilianWorldGen.STARLIGHT_BLOSSOM_PATCH_PLACED);
        BiomeDefaultFeatures.addDefaultGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addExtraEmeralds(generation);

        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(spawns);
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ArtilianEntities.SHADOW_WISP.get(), 6, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ArtilianEntities.ZAPHOX.get(), 5, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ArtilianEntities.PEEPKIN.get(), 4, 1, 2));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 2, 4));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 4, 2, 4));
        spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 3, 1, 3));

        BiomeSpecialEffects effects = specialEffects(
                0x8B7CFF, 0xC3B8FF, 0x526DFF, 0x222959, 0x6A5A8C, 0x8E7CB2,
                ParticleTypes.END_ROD, 0.0018F,
                Musics.createGameMusic(SoundEvents.MUSIC_BIOME_STONY_PEAKS));

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.30F)
                .downfall(0.55F)
                .specialEffects(effects)
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    private static BiomeGenerationSettings.Builder baseGeneration(
            HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        return generation;
    }

    private static BiomeSpecialEffects specialEffects(
            int skyColor,
            int fogColor,
            int waterColor,
            int waterFogColor,
            int grassColor,
            int foliageColor,
            ParticleOptions particle,
            float particleProbability,
            Music music) {
        return new BiomeSpecialEffects.Builder()
                .skyColor(skyColor)
                .fogColor(fogColor)
                .waterColor(waterColor)
                .waterFogColor(waterFogColor)
                .grassColorOverride(grassColor)
                .foliageColorOverride(foliageColor)
                .ambientParticle(new AmbientParticleSettings(particle, particleProbability))
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .backgroundMusic(music)
                .build();
    }
}
