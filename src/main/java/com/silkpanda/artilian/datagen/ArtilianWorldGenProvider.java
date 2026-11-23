package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.DragonJungleDimension;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
import com.silkpanda.artilian.worldgen.ArtilianBiomes;
import com.silkpanda.artilian.worldgen.ArtilianStructures;
import com.silkpanda.artilian.worldgen.ArtilianWorldGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ArtilianWorldGenProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ArtilianWorldGen::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, ArtilianWorldGen::bootstrapPlacedFeatures)
            .add(Registries.BIOME, ArtilianBiomes::bootstrap)
            .add(Registries.TEMPLATE_POOL, ArtilianStructures::bootstrapTemplatePools)
            .add(Registries.STRUCTURE, ArtilianStructures::bootstrapStructures)
            .add(Registries.STRUCTURE_SET, ArtilianStructures::bootstrapStructureSets)
            .add(Registries.NOISE_SETTINGS, EyeRealmDimension::bootstrapNoiseSettings)
            .add(Registries.DIMENSION_TYPE, context -> {
                EyeRealmDimension.bootstrapDimensionType(context);
                DragonJungleDimension.bootstrapDimensionType(context);
            })
            .add(Registries.LEVEL_STEM, context -> {
                EyeRealmDimension.bootstrapLevelStem(context);
                DragonJungleDimension.bootstrapLevelStem(context);
            })
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ArtilianWorldGen::bootstrapBiomeModifiers);

    public ArtilianWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(ArtilianMod.MODID));
    }
}
