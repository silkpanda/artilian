package com.silkpanda.artilian.client;

import com.silkpanda.artilian.boss.ArtilianBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Parameterized GeckoLib renderer for Artilian bosses, starting with the Training Golem. */
public class TrainingGolemRenderer<T extends ArtilianBossEntity> extends GeoEntityRenderer<T> {
    public TrainingGolemRenderer(EntityRendererProvider.Context context, String entityId, float shadowRadius) {
        super(context, new TrainingGolemModel<>(entityId));
        this.shadowRadius = shadowRadius;
    }
}
