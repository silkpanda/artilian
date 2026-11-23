package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.PetDragonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Parameterized GeckoLib model for Phase 5 Stage 6 hatchable pet dragons.
 */
public class PetDragonModel<T extends PetDragonEntity> extends GeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation animations;
    private final ResourceLocation texture;

    public PetDragonModel(String speciesId) {
        this.model = ArtilianMod.id("geo/entity/" + speciesId + ".geo.json");
        this.animations = ArtilianMod.id("animations/entity/" + speciesId + ".animation.json");
        this.texture = ArtilianMod.id("textures/entity/" + speciesId + ".png");
    }

    @Override
    public ResourceLocation getModelResource(T entity) {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureResource(T entity) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T entity) {
        return this.animations;
    }
}
