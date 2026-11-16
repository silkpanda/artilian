package com.silkpanda.artilian.chester;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Wires game events to {@link ChesterService}. No rules live here. */
public final class ChesterEvents {
    private ChesterEvents() {}

    @EventBusSubscriber(modid = ArtilianMod.MODID)
    public static final class Game {
        private Game() {}

        @SubscribeEvent
        public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                ChesterService.handleLogin(player);
            }
        }

        @SubscribeEvent
        public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                ChesterService.giveWhistleIfMissing(player);
            }
        }

        @SubscribeEvent
        public static void onDrops(LivingDropsEvent event) {
            if (event.getEntity() instanceof Player) {
                ChesterService.keepWhistleOutOfDrops(event.getDrops());
            }
        }
    }

    @EventBusSubscriber(modid = ArtilianMod.MODID)
    public static final class Setup {
        private Setup() {}

        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(ArtilianEntities.CHESTER.get(), ChesterEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void onCreativeTabs(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(ArtilianItems.CHESTER_WHISTLE.get());
            }
        }
    }
}
