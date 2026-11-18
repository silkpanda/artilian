package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.PrismlingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class PrismlingRenderer extends GeoEntityRenderer<PrismlingEntity> {
    /** Massive legendary guardian scale: ~4.1 blocks tall (~5.1 including the floating elemental symbol arc). */
    public static final float SCALE = 2.0F;

    public PrismlingRenderer(EntityRendererProvider.Context context) {
        super(context, new PrismlingModel());
        this.withScale(SCALE);
        this.shadowRadius = 1.75F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
