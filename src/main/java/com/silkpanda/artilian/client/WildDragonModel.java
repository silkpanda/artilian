package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dragon.WildDragonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Shared GeckoLib model for Phase 5 Stage 4–5 wild dragons.
 * All species reuse {@code geo/entity/dragon_rig.geo.json} and {@code animations/entity/dragon_rig.animation.json}
 * while swapping {@code textures/entity/<speciesId>.png} (and {@code _glowmask.png}).
 */
public class WildDragonModel<T extends WildDragonEntity> extends GeoModel<T> {
    private static final ResourceLocation SHARED_MODEL = ArtilianMod.id("geo/entity/dragon_rig.geo.json");
    private static final ResourceLocation SHARED_ANIMATIONS = ArtilianMod.id("animations/entity/dragon_rig.animation.json");

    private final ResourceLocation texture;

    public WildDragonModel(String speciesId) {
        this.texture = ArtilianMod.id("textures/entity/" + speciesId + ".png");
    }

    @Override
    public ResourceLocation getModelResource(T entity) {
        return SHARED_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(T entity) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T entity) {
        return SHARED_ANIMATIONS;
    }
}
