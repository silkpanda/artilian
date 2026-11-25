package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.PeepkinEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PeepkinModel extends GeoModel<PeepkinEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/peepkin.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/peepkin.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/peepkin.animation.json");

    @Override
    public ResourceLocation getModelResource(PeepkinEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PeepkinEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PeepkinEntity entity) {
        return ANIMATIONS;
    }

    @Override
    public RenderType getRenderType(PeepkinEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
