package com.silkpanda.artilian.client;

import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib armor renderer for the Sunsteel armor set (with emissive solar glowmask). */
public final class SunsteelArmorRenderer extends ArtilianArmorRenderer {
    public SunsteelArmorRenderer() {
        super("sunsteel");
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
