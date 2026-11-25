package com.silkpanda.artilian.client;

import com.silkpanda.artilian.enemy.StructureEnemyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Parameterized GeckoLib renderer for Phase 2 Stage 6 structure enemies. */
public class StructureEnemyRenderer<T extends StructureEnemyEntity> extends GeoEntityRenderer<T> {
    public StructureEnemyRenderer(EntityRendererProvider.Context context, String entityId, float shadowRadius) {
        this(context, entityId, shadowRadius, false);
    }

    public StructureEnemyRenderer(
            EntityRendererProvider.Context context, String entityId, float shadowRadius, boolean glowing) {
        super(context, new StructureEnemyModel<>(entityId));
        this.shadowRadius = shadowRadius;
        if (glowing) {
            this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        }
    }
}
