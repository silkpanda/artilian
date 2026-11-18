package com.silkpanda.artilian;

import com.mojang.logging.LogUtils;
import com.silkpanda.artilian.config.ArtilianConfig;
import com.silkpanda.artilian.mana.ManaSyncPayload;
import com.silkpanda.artilian.registry.ArtilianArmorMaterials;
import com.silkpanda.artilian.registry.ArtilianAttachments;
import com.silkpanda.artilian.registry.ArtilianBlockEntities;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianCreativeTabs;
import com.silkpanda.artilian.registry.ArtilianDataComponents;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.tnt.ArtilianTntBlock;
import com.silkpanda.artilian.worldgen.ArtilianOverworldRegion;
import com.silkpanda.artilian.worldgen.ArtilianSurfaceRules;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

@Mod(ArtilianMod.MODID)
public class ArtilianMod {
    public static final String MODID = "artilian";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ArtilianMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ArtilianConfig.COMMON_SPEC);
        ArtilianBlocks.register(modEventBus);
        ArtilianBlockEntities.register(modEventBus);
        ArtilianArmorMaterials.register(modEventBus);
        ArtilianDataComponents.register(modEventBus);
        ArtilianItems.register(modEventBus);
        ArtilianCreativeTabs.register(modEventBus);
        ArtilianAttachments.register(modEventBus);
        ArtilianEntities.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::registerPayloads);
        LOGGER.info("Adventures in Artilian is loading");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ArtilianTntBlock.registerDispenserBehaviors();
            Regions.register(new ArtilianOverworldRegion(id("overworld_region"), 8));
            SurfaceRuleManager.addSurfaceRules(
                    SurfaceRuleManager.RuleCategory.OVERWORLD,
                    MODID,
                    ArtilianSurfaceRules.makeRules());
        });
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ManaSyncPayload.TYPE, ManaSyncPayload.STREAM_CODEC, ManaSyncPayload::handleOnClient);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
