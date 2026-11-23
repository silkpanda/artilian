package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.BlinkyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class BlinkyRenderer extends GeoEntityRenderer<BlinkyEntity> {
    public BlinkyRenderer(EntityRendererProvider.Context context) {
        super(context, new BlinkyModel());
        this.shadowRadius = 0.42F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
