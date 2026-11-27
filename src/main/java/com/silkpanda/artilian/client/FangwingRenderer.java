package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.FangwingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class FangwingRenderer extends GeoEntityRenderer<FangwingEntity> {
    public static final float SCALE = 1.15F;

    public FangwingRenderer(EntityRendererProvider.Context context) {
        super(context, new FangwingModel());
        this.withScale(SCALE);
        this.shadowRadius = 0.5F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
