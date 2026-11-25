package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Ticks temporary Sunsteel Solar Flare light motes and queued Warm Scoop drop pulls. */
@EventBusSubscriber(modid = ArtilianMod.MODID)
public final class SunsteelEvents {
    private SunsteelEvents() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            SunsteelAbilities.tickServerLevel(serverLevel);
        }
    }
}
