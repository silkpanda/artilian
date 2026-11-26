package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.VinerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class VinerRenderer extends GeoEntityRenderer<VinerEntity> {
    public static final float SCALE = 1.05F;

    public VinerRenderer(EntityRendererProvider.Context context) {
        super(context, new VinerModel());
        this.withScale(SCALE);
        this.shadowRadius = 0.50F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
