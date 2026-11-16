package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.ShadowWispEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ShadowWispModel extends GeoModel<ShadowWispEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/shadow_wisp.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/shadow_wisp.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/shadow_wisp.animation.json");

    @Override
    public ResourceLocation getModelResource(ShadowWispEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ShadowWispEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ShadowWispEntity entity) {
        return ANIMATIONS;
    }
}
