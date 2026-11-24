package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.ZapkinEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZapkinModel extends GeoModel<ZapkinEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/zapkin.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/zapkin.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/zapkin.animation.json");

    @Override
    public ResourceLocation getModelResource(ZapkinEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ZapkinEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ZapkinEntity entity) {
        return ANIMATIONS;
    }

    @Override
    public RenderType getRenderType(ZapkinEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
