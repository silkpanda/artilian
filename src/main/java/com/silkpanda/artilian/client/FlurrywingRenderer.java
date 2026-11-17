package com.silkpanda.artilian.client;

import com.silkpanda.artilian.pet.FlurrywingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FlurrywingRenderer extends GeoEntityRenderer<FlurrywingEntity> {
    /** The 22 px model at 2x puts the hat top at 2.75 blocks. Matches the 1.3 x 2.75 hitbox in ArtilianEntities. */
    public static final float SCALE = 2.0F;

    public FlurrywingRenderer(EntityRendererProvider.Context context) {
        super(context, new FlurrywingModel());
        this.withScale(SCALE);
        this.shadowRadius = 0.9F;
    }
}
