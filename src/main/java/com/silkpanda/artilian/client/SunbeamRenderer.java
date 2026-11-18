package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.SunbeamEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SunbeamRenderer extends GeoEntityRenderer<SunbeamEntity> {
    /** Legendary scale: the 32 px model at 2x puts the head top at 4 blocks, matching the hitbox. */
    public static final float SCALE = 2.0F;

    public SunbeamRenderer(EntityRendererProvider.Context context) {
        super(context, new SunbeamModel());
        this.withScale(SCALE);
        this.shadowRadius = 1.6F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
