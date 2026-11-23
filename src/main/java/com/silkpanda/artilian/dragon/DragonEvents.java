package com.silkpanda.artilian.dragon;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/** Wires entity attributes and spawn placements for Phase 5 Dragon Jungle wild dragons. */
public final class DragonEvents {
    private DragonEvents() {}

    @EventBusSubscriber(modid = ArtilianMod.MODID)
    public static final class Setup {
        private Setup() {}

        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(ArtilianEntities.CANOPY_WYVERN.get(), CanopyWyvernEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
            @SuppressWarnings("unchecked")
            var wyvernType = (net.minecraft.world.entity.EntityType<Monster>) (net.minecraft.world.entity.EntityType<?>) ArtilianEntities.CANOPY_WYVERN.get();
            event.register(
                    wyvernType,
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    WildDragonEntity::checkWildDragonSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        }
    }
}
