package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.CrystalFoxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CrystalFoxRenderer extends GeoEntityRenderer<CrystalFoxEntity> {
    public CrystalFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new CrystalFoxModel());
        this.shadowRadius = 0.38F;
    }
}
