package com.silkpanda.artilian.client;

import com.silkpanda.artilian.boss.EmbercrownSovereignEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib renderer for Embercrown Sovereign with emissive eyes, crown tips, chest core, and wing runes. */
public class EmbercrownSovereignRenderer extends GeoEntityRenderer<EmbercrownSovereignEntity> {
    public EmbercrownSovereignRenderer(EntityRendererProvider.Context context) {
        super(context, new TrainingGolemModel<>("embercrown_sovereign"));
        this.shadowRadius = 1.8F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
