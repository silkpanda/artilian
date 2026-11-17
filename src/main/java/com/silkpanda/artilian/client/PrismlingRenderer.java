package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.PrismlingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PrismlingRenderer extends GeoEntityRenderer<PrismlingEntity> {
    /** Legendary-sized: twice the drawn model, about 2 blocks tall (Peepkin is 0.85). Matches the hitbox in ArtilianEntities. */
    public static final float SCALE = 2.0F;

    public PrismlingRenderer(EntityRendererProvider.Context context) {
        super(context, new PrismlingModel());
        this.withScale(SCALE);
        this.shadowRadius = 1.0F;
    }
}
