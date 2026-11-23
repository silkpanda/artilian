package com.silkpanda.artilian.client;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
import com.silkpanda.artilian.pet.CaptureCrystalItem;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Client-only setup. Nothing outside this package may reference it. */
@EventBusSubscriber(modid = ArtilianMod.MODID, value = Dist.CLIENT)
public final class ArtilianClient {
    private ArtilianClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ArtilianItems.CAPTURE_CRYSTAL.get(),
                ArtilianMod.id("crystal_state"),
                (stack, level, entity, seed) -> CaptureCrystalItem.getVisualState(stack)));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ArtilianEntities.CHESTER.get(), ChesterRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.PRIMED_TNT.get(), TntRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.CRYSTAL_FOX.get(), CrystalFoxRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.SHADOW_WISP.get(), ShadowWispRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.CINDER_CRAG.get(), CinderCragRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.ZAPHOX.get(), ZaphoxRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.GALECORN.get(), GalecornRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.EMBER_DRAKE.get(), EmberDrakeRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.PEEPKIN.get(), PeepkinRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.PRISMLING.get(), PrismlingRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.FLURRYWING.get(), FlurrywingRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.SUNBEAM.get(), SunbeamRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.FROGDUCK.get(), FrogduckRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.ZAPKIN.get(), ZapkinRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.PRISM_SENTINEL.get(),
                ctx -> new StructureEnemyRenderer<>(ctx, "prism_sentinel", 0.55F));
        event.registerEntityRenderer(ArtilianEntities.CINDER_RAIDER.get(),
                ctx -> new StructureEnemyRenderer<>(ctx, "cinder_raider", 0.45F));
        event.registerEntityRenderer(ArtilianEntities.ASTRAL_STALKER.get(),
                ctx -> new StructureEnemyRenderer<>(ctx, "astral_stalker", 0.48F));
        event.registerEntityRenderer(ArtilianEntities.PEEPER.get(),
                ctx -> new EyeRealmEnemyRenderer<>(ctx, "peeper", 0.45F));
        event.registerEntityRenderer(ArtilianEntities.OCULUS_SLIME.get(),
                ctx -> new EyeRealmEnemyRenderer<>(ctx, "oculus_slime", 0.65F));
        event.registerEntityRenderer(ArtilianEntities.LENS_GUARDIAN.get(),
                ctx -> new EyeRealmEnemyRenderer<>(ctx, "lens_guardian", 0.65F));
        event.registerEntityRenderer(ArtilianEntities.BLINKY.get(), BlinkyRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.TRAINING_GOLEM.get(),
                ctx -> new TrainingGolemRenderer<>(ctx, "training_golem", 1.0F));
        event.registerEntityRenderer(ArtilianEntities.CRYSTAL_COLOSSUS.get(), CrystalColossusRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.CAPTAIN_BRIMSTONE.get(), CaptainBrimstoneRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.GREAT_ALL_SEER.get(), GreatAllSeerRenderer::new);
        event.registerEntityRenderer(ArtilianEntities.CANOPY_WYVERN.get(),
                ctx -> new WildDragonRenderer<>(ctx, "canopy_wyvern", "dragon_rig", 1.05F));
        event.registerEntityRenderer(ArtilianEntities.AMBER_WYVERN.get(),
                ctx -> new WildDragonRenderer<>(ctx, "amber_wyvern", 1.1F));
        event.registerEntityRenderer(ArtilianEntities.STORM_GLIDEWING.get(),
                ctx -> new WildDragonRenderer<>(ctx, "storm_glidewing", 1.0F));
        event.registerEntityRenderer(ArtilianEntities.CANOPY_DRAGONLING.get(),
                ctx -> new PetDragonRenderer<>(ctx, "canopy_dragonling"));
        event.registerEntityRenderer(ArtilianEntities.AMBER_DRAGONLING.get(),
                ctx -> new PetDragonRenderer<>(ctx, "amber_dragonling"));
        event.registerEntityRenderer(ArtilianEntities.STORM_DRAGONLING.get(),
                ctx -> new PetDragonRenderer<>(ctx, "storm_dragonling"));
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ArtilianMod.id("mana_bar"), ManaHudOverlay::render);
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(EyeRealmDimension.EYE_REALM_EFFECTS, new EyeRealmSkyEffects());
    }
}
