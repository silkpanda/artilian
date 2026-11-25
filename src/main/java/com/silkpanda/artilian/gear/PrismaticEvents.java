package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Ticks queued Prismatic drop pulls and auto-smelting. */
@EventBusSubscriber(modid = ArtilianMod.MODID)
public final class PrismaticEvents {
    private PrismaticEvents() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            PrismaticAbilities.tickServerLevel(serverLevel);
        }
    }
}
