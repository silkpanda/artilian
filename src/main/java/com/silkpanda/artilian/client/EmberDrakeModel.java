package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.EmberDrakeEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EmberDrakeModel extends GeoModel<EmberDrakeEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/ember_drake.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/ember_drake.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/ember_drake.animation.json");

    @Override
    public ResourceLocation getModelResource(EmberDrakeEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(EmberDrakeEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(EmberDrakeEntity entity) {
        return ANIMATIONS;
    }

    @Override
    public RenderType getRenderType(EmberDrakeEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
