package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.ZapkinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ZapkinRenderer extends GeoEntityRenderer<ZapkinEntity> {
    public static final float SCALE = 1.25F;

    public ZapkinRenderer(EntityRendererProvider.Context context) {
        super(context, new ZapkinModel());
        this.withScale(SCALE);
        this.shadowRadius = 0.55F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
