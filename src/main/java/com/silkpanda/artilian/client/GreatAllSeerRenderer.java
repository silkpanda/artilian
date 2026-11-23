package com.silkpanda.artilian.client;

import com.silkpanda.artilian.boss.GreatAllSeerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib renderer for The Great All-Seer with emissive iris, mini-eyes, crown, and wing sigils. */
public class GreatAllSeerRenderer extends GeoEntityRenderer<GreatAllSeerEntity> {
    public GreatAllSeerRenderer(EntityRendererProvider.Context context) {
        super(context, new TrainingGolemModel<>("great_all_seer"));
        this.shadowRadius = 1.4F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
