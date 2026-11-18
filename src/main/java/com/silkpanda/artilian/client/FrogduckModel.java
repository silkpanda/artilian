package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.FrogduckEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FrogduckModel extends GeoModel<FrogduckEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/frogduck.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/frogduck.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/frogduck.animation.json");

    @Override
    public ResourceLocation getModelResource(FrogduckEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FrogduckEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FrogduckEntity entity) {
        return ANIMATIONS;
    }
}
