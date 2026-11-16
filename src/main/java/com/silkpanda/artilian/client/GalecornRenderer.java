package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.GalecornEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GalecornRenderer extends GeoEntityRenderer<GalecornEntity> {
    public GalecornRenderer(EntityRendererProvider.Context context) {
        super(context, new GalecornModel());
        this.shadowRadius = 0.42F;
    }
}
