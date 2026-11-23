package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dragon.WildDragonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for Phase 5 Stage 4–5 wild dragons.
 * Each species loads its own {@code geo/entity/<rigId>.geo.json},
 * {@code animations/entity/<rigId>.animation.json}, and {@code textures/entity/<speciesId>.png}.
 */
public class WildDragonModel<T extends WildDragonEntity> extends GeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation animations;
    private final ResourceLocation texture;

    public WildDragonModel(String speciesId, String rigId) {
        this.model = ArtilianMod.id("geo/entity/" + rigId + ".geo.json");
        this.animations = ArtilianMod.id("animations/entity/" + rigId + ".animation.json");
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
