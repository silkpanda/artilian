package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.FrogduckEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FrogduckRenderer extends GeoEntityRenderer<FrogduckEntity> {
    /** 19 px tall model at 1.15x = ~1.37 blocks tall. Matches the 1.25 x 1.35 hitbox in ArtilianEntities. */
    public static final float SCALE = 1.15F;

    public FrogduckRenderer(EntityRendererProvider.Context context) {
        super(context, new FrogduckModel());
        this.withScale(SCALE);
        this.shadowRadius = 0.75F;
    }
}
