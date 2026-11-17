package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianTags;
import com.silkpanda.artilian.worldgen.ArtilianBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ArtilianBiomeTagsProvider extends BiomeTagsProvider {
    public ArtilianBiomeTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ArtilianMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BiomeTags.IS_OVERWORLD).add(
                ArtilianBiomes.CRYSTAL_CANOPY_WOODS,
                ArtilianBiomes.EMBERBLOOM_MEADOWS,
                ArtilianBiomes.STARLIGHT_HIGHLANDS
        );
        tag(BiomeTags.IS_FOREST).add(ArtilianBiomes.CRYSTAL_CANOPY_WOODS);
        tag(Tags.Biomes.IS_PLAINS).add(ArtilianBiomes.EMBERBLOOM_MEADOWS);
        tag(Tags.Biomes.IS_FLORAL).add(ArtilianBiomes.EMBERBLOOM_MEADOWS);
        tag(BiomeTags.IS_MOUNTAIN).add(ArtilianBiomes.STARLIGHT_HIGHLANDS);
        tag(Tags.Biomes.IS_MOUNTAIN).add(ArtilianBiomes.STARLIGHT_HIGHLANDS);
        tag(Tags.Biomes.IS_MAGICAL).add(
                ArtilianBiomes.CRYSTAL_CANOPY_WOODS,
                ArtilianBiomes.EMBERBLOOM_MEADOWS,
                ArtilianBiomes.STARLIGHT_HIGHLANDS
        );

        tag(ArtilianTags.Biomes.HAS_CRYSTAL_GROVE_SHRINE).add(ArtilianBiomes.CRYSTAL_CANOPY_WOODS);
        tag(ArtilianTags.Biomes.HAS_EMBERBLOOM_OUTPOST).add(ArtilianBiomes.EMBERBLOOM_MEADOWS);
        tag(ArtilianTags.Biomes.HAS_STARLIGHT_WATCHTOWER).add(ArtilianBiomes.STARLIGHT_HIGHLANDS);
    }
}
