package com.silkpanda.artilian.client;

import com.silkpanda.artilian.boss.CaptainBrimstoneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib renderer for Captain Brimstone with emissive furnace core, eyes, cannon bore, and cutlass runes. */
public class CaptainBrimstoneRenderer extends GeoEntityRenderer<CaptainBrimstoneEntity> {
    public CaptainBrimstoneRenderer(EntityRendererProvider.Context context) {
        super(context, new TrainingGolemModel<>("captain_brimstone"));
        this.shadowRadius = 1.9F;
        this.withScale(1.65F);
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
