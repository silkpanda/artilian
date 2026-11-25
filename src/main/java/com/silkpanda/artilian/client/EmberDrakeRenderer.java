package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.EmberDrakeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class EmberDrakeRenderer extends GeoEntityRenderer<EmberDrakeEntity> {
    public EmberDrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new EmberDrakeModel());
        this.shadowRadius = 0.40F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
