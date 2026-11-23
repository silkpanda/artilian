package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.PetDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Parameterized GeckoLib renderer for Phase 5 hatchable pet dragons with emissive glowmasks. */
public class PetDragonRenderer<T extends PetDragonEntity> extends GeoEntityRenderer<T> {
    public PetDragonRenderer(EntityRendererProvider.Context context, String speciesId) {
        super(context, new PetDragonModel<>(speciesId));
        this.shadowRadius = 0.45F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
