package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianEntities;
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
                EntityType.BREEZE,
                ArtilianEntities.PRISM_SENTINEL.get(),
                ArtilianEntities.CINDER_RAIDER.get(),
                ArtilianEntities.ASTRAL_STALKER.get()
        );
        tag(ArtilianTags.EntityTypes.EYE_REALM_ENEMIES).add(
                ArtilianEntities.PEEPER.get(),
                ArtilianEntities.OCULUS_SLIME.get(),
                ArtilianEntities.LENS_GUARDIAN.get()
        );
        tag(ArtilianTags.EntityTypes.WILD_DRAGONS).add(
                ArtilianEntities.CANOPY_WYVERN.get(),
                ArtilianEntities.AMBER_WYVERN.get(),
                ArtilianEntities.STORM_GLIDEWING.get()
        );
        tag(ArtilianTags.EntityTypes.BOSSES).add(
                ArtilianEntities.TRAINING_GOLEM.get(),
                ArtilianEntities.CRYSTAL_COLOSSUS.get(),
                ArtilianEntities.CAPTAIN_BRIMSTONE.get(),
                ArtilianEntities.GREAT_ALL_SEER.get(),
                ArtilianEntities.EMBERCROWN_SOVEREIGN.get()
        );
    }
}
