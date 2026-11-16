package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.GalecornEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GalecornModel extends GeoModel<GalecornEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/galecorn.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/galecorn.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/galecorn.animation.json");

    @Override
    public ResourceLocation getModelResource(GalecornEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GalecornEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GalecornEntity entity) {
        return ANIMATIONS;
    }
}
