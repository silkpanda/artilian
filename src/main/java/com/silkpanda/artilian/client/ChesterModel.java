package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class ChesterModel extends GeoModel<ChesterEntity> {
    private static final ResourceLocation MODEL = ArtilianMod.id("geo/entity/chester.geo.json");
    private static final ResourceLocation TEXTURE = ArtilianMod.id("textures/entity/chester.png");
    private static final ResourceLocation ANIMATIONS = ArtilianMod.id("animations/entity/chester.animation.json");

    /**
     * Asleep, Chester is drawn with the game's own chest texture, so he matches a placed chest
     * exactly and follows whatever resource pack the family has on. The sleeping model is the
     * same three boxes as the awake one, mapped onto the vanilla chest sheet.
     */
    private static final ResourceLocation SLEEP_MODEL = ArtilianMod.id("geo/entity/chester_sleep.geo.json");
    private static final ResourceLocation VANILLA_CHEST_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/chest/normal.png");

    @Override
    public ResourceLocation getModelResource(ChesterEntity chester) {
        return chester.isAsleep() ? SLEEP_MODEL : MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ChesterEntity chester) {
        return chester.isAsleep() ? VANILLA_CHEST_TEXTURE : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ChesterEntity chester) {
        return ANIMATIONS;
    }

    /** Asleep, Chester has no eyes, teeth, tongue, or feet: what is left is a plain chest. */
    @Override
    public void setCustomAnimations(ChesterEntity chester, long instanceId, AnimationState<ChesterEntity> state) {
        super.setCustomAnimations(chester, instanceId, state);
        boolean asleep = chester.isAsleep();
        this.getBone("eyes").ifPresent(bone -> hide(bone, asleep));
        this.getBone("feet").ifPresent(bone -> hide(bone, asleep));
        this.getBone("awake_extras").ifPresent(bone -> hide(bone, asleep));
        this.getBone("teeth_upper").ifPresent(bone -> hide(bone, asleep));
    }

    private static void hide(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        bone.setChildrenHidden(hidden);
    }
}
