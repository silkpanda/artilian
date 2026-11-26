package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Ticks queued Starlight Starlit Magnet and Meteor Scoop drop pulls. */
@EventBusSubscriber(modid = ArtilianMod.MODID)
public final class StarlightEvents {
    private StarlightEvents() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            StarlightAbilities.tickServerLevel(serverLevel);
        }
    }
}
