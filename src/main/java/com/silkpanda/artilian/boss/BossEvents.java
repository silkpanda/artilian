package com.silkpanda.artilian.boss;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/** Wires boss attributes. Bosses never spawn naturally, so there are no spawn placements. */
public final class BossEvents {
    private BossEvents() {}

    @EventBusSubscriber(modid = ArtilianMod.MODID)
    public static final class Setup {
        private Setup() {}

        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(ArtilianEntities.TRAINING_GOLEM.get(), TrainingGolemEntity.createAttributes().build());
            event.put(ArtilianEntities.CRYSTAL_COLOSSUS.get(), CrystalColossusEntity.createAttributes().build());
            event.put(ArtilianEntities.CAPTAIN_BRIMSTONE.get(), CaptainBrimstoneEntity.createAttributes().build());
        }
    }
}
