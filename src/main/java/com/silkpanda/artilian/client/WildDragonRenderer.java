package com.silkpanda.artilian.client;

import com.silkpanda.artilian.dragon.WildDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Parameterized GeckoLib renderer for Phase 5 wild dragons with emissive glowmasks. */
public class WildDragonRenderer<T extends WildDragonEntity> extends GeoEntityRenderer<T> {
    public WildDragonRenderer(EntityRendererProvider.Context context, String speciesId, float shadowRadius) {
        this(context, speciesId, speciesId, shadowRadius);
    }

    public WildDragonRenderer(EntityRendererProvider.Context context, String speciesId, String rigId, float shadowRadius) {
        super(context, new WildDragonModel<>(speciesId, rigId));
        this.shadowRadius = shadowRadius;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
