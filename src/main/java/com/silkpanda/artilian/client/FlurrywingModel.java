package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.FlurrywingEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FlurrywingModel extends GeoModel<FlurrywingEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/flurrywing.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/flurrywing.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/flurrywing.animation.json");

    @Override
    public ResourceLocation getModelResource(FlurrywingEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FlurrywingEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FlurrywingEntity entity) {
        return ANIMATIONS;
    }
}
