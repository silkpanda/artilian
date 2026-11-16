package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.gear.ArtilianArmorItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * Shared GeckoLib armor renderer resolving {@code geo/item/armor/<set>_armor.geo.json}
 * and {@code textures/item/armor/<set>_armor.png} via {@link DefaultedItemGeoModel}.
 */
public class ArtilianArmorRenderer extends GeoArmorRenderer<ArtilianArmorItem> {
    public ArtilianArmorRenderer(String set) {
        super(new DefaultedItemGeoModel<>(ArtilianMod.id("armor/" + set + "_armor")));
    }
}
