package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.PrismlingEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PrismlingModel extends GeoModel<PrismlingEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/prismling.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/prismling.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/prismling.animation.json");

    @Override
    public ResourceLocation getModelResource(PrismlingEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PrismlingEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PrismlingEntity entity) {
        return ANIMATIONS;
    }
}
