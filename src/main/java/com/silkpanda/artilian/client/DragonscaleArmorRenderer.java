package com.silkpanda.artilian.client;

import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib armor renderer for the Phase 5 Dragonscale armor set. */
public final class DragonscaleArmorRenderer extends ArtilianArmorRenderer {
    public DragonscaleArmorRenderer() {
        super("dragonscale");
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
