package com.silkpanda.artilian.client;

import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib armor renderer for the Phase 5 Stage 9 Twin-Star endgame armor set. */
public final class TwinStarArmorRenderer extends ArtilianArmorRenderer {
    public TwinStarArmorRenderer() {
        super("twin_star");
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
