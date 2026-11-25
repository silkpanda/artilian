package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.PeepkinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class PeepkinRenderer extends GeoEntityRenderer<PeepkinEntity> {
    public PeepkinRenderer(EntityRendererProvider.Context context) {
        super(context, new PeepkinModel());
        this.shadowRadius = 0.36F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
