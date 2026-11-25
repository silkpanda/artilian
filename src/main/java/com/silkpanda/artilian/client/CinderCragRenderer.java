package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.CinderCragEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class CinderCragRenderer extends GeoEntityRenderer<CinderCragEntity> {
    public CinderCragRenderer(EntityRendererProvider.Context context) {
        super(context, new CinderCragModel());
        this.shadowRadius = 0.45F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
