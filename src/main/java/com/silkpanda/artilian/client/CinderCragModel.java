package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.CinderCragEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CinderCragModel extends GeoModel<CinderCragEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/cinder_crag.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/cinder_crag.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/cinder_crag.animation.json");

    @Override
    public ResourceLocation getModelResource(CinderCragEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CinderCragEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CinderCragEntity entity) {
        return ANIMATIONS;
    }

    @Override
    public RenderType getRenderType(CinderCragEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
