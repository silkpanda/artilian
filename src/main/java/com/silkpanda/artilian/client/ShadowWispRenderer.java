package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.ShadowWispEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ShadowWispRenderer extends GeoEntityRenderer<ShadowWispEntity> {
    public ShadowWispRenderer(EntityRendererProvider.Context context) {
        super(context, new ShadowWispModel());
        this.shadowRadius = 0.38F;
    }
}
