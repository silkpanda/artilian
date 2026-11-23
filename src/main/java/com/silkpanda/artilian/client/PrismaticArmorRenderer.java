package com.silkpanda.artilian.client;

import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib armor renderer for the Tier 3 Prismatic Brimstone armor set (with emissive glowmask). */
public final class PrismaticArmorRenderer extends ArtilianArmorRenderer {
    public PrismaticArmorRenderer() {
        super("prismatic");
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
