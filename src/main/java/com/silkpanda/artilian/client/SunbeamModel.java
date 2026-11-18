package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.SunbeamEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SunbeamModel extends GeoModel<SunbeamEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/sunbeam.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/sunbeam.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/sunbeam.animation.json");

    @Override
    public ResourceLocation getModelResource(SunbeamEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SunbeamEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SunbeamEntity entity) {
        return ANIMATIONS;
    }
}
