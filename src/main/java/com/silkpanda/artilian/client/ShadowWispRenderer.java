package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.ShadowWispEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ShadowWispRenderer extends GeoEntityRenderer<ShadowWispEntity> {
    public ShadowWispRenderer(EntityRendererProvider.Context context) {
        super(context, new ShadowWispModel());
        this.withScale(2.2F);
        this.shadowRadius = 0.84F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
