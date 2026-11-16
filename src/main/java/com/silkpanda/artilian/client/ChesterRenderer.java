package com.silkpanda.artilian.client;

import com.silkpanda.artilian.chester.ChesterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ChesterRenderer extends GeoEntityRenderer<ChesterEntity> {
    public ChesterRenderer(EntityRendererProvider.Context context) {
        super(context, new ChesterModel());
        this.shadowRadius = 0.45F;
    }
}
