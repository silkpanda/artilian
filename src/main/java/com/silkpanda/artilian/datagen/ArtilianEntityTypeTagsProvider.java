package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ArtilianEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ArtilianEntityTypeTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ArtilianMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ArtilianTags.EntityTypes.STRUCTURE_ENEMIES).add(
                EntityType.PILLAGER,
                EntityType.VINDICATOR,
                EntityType.EVOKER,
                EntityType.WITHER_SKELETON,
                EntityType.STRAY,
                EntityType.BOGGED,
                EntityType.HUSK,
                EntityType.BREEZE
        );
    }
}
