package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.CinderCragEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CinderCragRenderer extends GeoEntityRenderer<CinderCragEntity> {
    public CinderCragRenderer(EntityRendererProvider.Context context) {
        super(context, new CinderCragModel());
        this.shadowRadius = 0.45F;
    }
}
