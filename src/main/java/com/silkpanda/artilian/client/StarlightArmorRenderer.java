package com.silkpanda.artilian.client;

import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib armor renderer for the Starlight Crystal armor set (with emissive glowmask). */
public final class StarlightArmorRenderer extends ArtilianArmorRenderer {
    public StarlightArmorRenderer() {
        super("starlight");
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
