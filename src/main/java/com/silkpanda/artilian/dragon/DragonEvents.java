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
            event.put(ArtilianEntities.AMBER_WYVERN.get(), AmberWyvernEntity.createAttributes().build());
            event.put(ArtilianEntities.STORM_GLIDEWING.get(), StormGlidewingEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
            registerDragonSpawn(event, ArtilianEntities.CANOPY_WYVERN.get());
            registerDragonSpawn(event, ArtilianEntities.AMBER_WYVERN.get());
            registerDragonSpawn(event, ArtilianEntities.STORM_GLIDEWING.get());
        }

        @SuppressWarnings("unchecked")
        private static void registerDragonSpawn(
                RegisterSpawnPlacementsEvent event,
                net.minecraft.world.entity.EntityType<? extends WildDragonEntity> type) {
            event.register(
                    (net.minecraft.world.entity.EntityType<Monster>) (net.minecraft.world.entity.EntityType<?>) type,
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    WildDragonEntity::checkWildDragonSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        }
    }
}
