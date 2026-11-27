package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.VinerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VinerModel extends GeoModel<VinerEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/viner.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/viner.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/viner.animation.json");

    @Override
    public ResourceLocation getModelResource(VinerEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(VinerEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(VinerEntity entity) {
        return ANIMATIONS;
    }
}
