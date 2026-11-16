package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.ZaphoxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ZaphoxRenderer extends GeoEntityRenderer<ZaphoxEntity> {
    public ZaphoxRenderer(EntityRendererProvider.Context context) {
        super(context, new ZaphoxModel());
        this.withScale(2.4F);
        this.shadowRadius = 1.08F;
    }
}
