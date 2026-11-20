package com.silkpanda.artilian.client;

import com.silkpanda.artilian.enemy.EyeRealmEnemyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Parameterized GeckoLib renderer for Phase 4 Stage 3 Eye Realm enemies with emissive glowmasks. */
public class EyeRealmEnemyRenderer<T extends EyeRealmEnemyEntity> extends GeoEntityRenderer<T> {
    public EyeRealmEnemyRenderer(EntityRendererProvider.Context context, String entityId, float shadowRadius) {
        super(context, new EyeRealmEnemyModel<>(entityId));
        this.shadowRadius = shadowRadius;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
