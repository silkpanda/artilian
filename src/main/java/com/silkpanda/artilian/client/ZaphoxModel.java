package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.ZaphoxEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZaphoxModel extends GeoModel<ZaphoxEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/zaphox.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/zaphox.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/zaphox.animation.json");

    @Override
    public ResourceLocation getModelResource(ZaphoxEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ZaphoxEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ZaphoxEntity entity) {
        return ANIMATIONS;
    }

    @Override
    public RenderType getRenderType(ZaphoxEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
