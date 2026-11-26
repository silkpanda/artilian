package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.FangwingEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FangwingModel extends GeoModel<FangwingEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/fangwing.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/fangwing.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/fangwing.animation.json");

    @Override
    public ResourceLocation getModelResource(FangwingEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FangwingEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FangwingEntity entity) {
        return ANIMATIONS;
    }
}
