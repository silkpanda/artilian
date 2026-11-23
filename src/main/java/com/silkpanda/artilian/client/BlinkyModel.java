package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.BlinkyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlinkyModel extends GeoModel<BlinkyEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/blinky.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/blinky.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/blinky.animation.json");

    @Override
    public ResourceLocation getModelResource(BlinkyEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BlinkyEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BlinkyEntity entity) {
        return ANIMATIONS;
    }
}
