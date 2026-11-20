package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.enemy.EyeRealmEnemyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Parameterized GeckoLib model for Phase 4 Stage 3 Eye Realm enemies. */
public class EyeRealmEnemyModel<T extends EyeRealmEnemyEntity> extends GeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final ResourceLocation animations;

    public EyeRealmEnemyModel(String entityId) {
        this.model = ArtilianMod.id("geo/entity/" + entityId + ".geo.json");
        this.texture = ArtilianMod.id("textures/entity/" + entityId + ".png");
        this.animations = ArtilianMod.id("animations/entity/" + entityId + ".animation.json");
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
