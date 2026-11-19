package com.silkpanda.artilian.client;

import com.silkpanda.artilian.boss.CrystalColossusEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib renderer for The Crystal Colossus with emissive crystal core/eyes/veins/shards. */
public class CrystalColossusRenderer extends GeoEntityRenderer<CrystalColossusEntity> {
    public CrystalColossusRenderer(EntityRendererProvider.Context context) {
        super(context, new TrainingGolemModel<>("crystal_colossus"));
        this.shadowRadius = 1.35F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
