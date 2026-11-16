package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CrystalFoxModel extends GeoModel<CrystalFoxEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/crystal_fox.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/crystal_fox.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/crystal_fox.animation.json");

    @Override
    public ResourceLocation getModelResource(CrystalFoxEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CrystalFoxEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CrystalFoxEntity entity) {
        return ANIMATIONS;
    }
}
