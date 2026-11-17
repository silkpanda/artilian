package com.silkpanda.artilian.enemy;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/** Wires entity attributes and spawn placements for Artilian structure enemies. */
public final class EnemyEvents {
    private EnemyEvents() {}

    @EventBusSubscriber(modid = ArtilianMod.MODID)
    public static final class Setup {
        private Setup() {}

        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(ArtilianEntities.PRISM_SENTINEL.get(), PrismSentinelEntity.createAttributes().build());
            event.put(ArtilianEntities.CINDER_RAIDER.get(), CinderRaiderEntity.createAttributes().build());
            event.put(ArtilianEntities.ASTRAL_STALKER.get(), AstralStalkerEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
            for (var type : new net.minecraft.world.entity.EntityType[]{
                    ArtilianEntities.PRISM_SENTINEL.get(),
                    ArtilianEntities.CINDER_RAIDER.get(),
                    ArtilianEntities.ASTRAL_STALKER.get()
            }) {
                @SuppressWarnings("unchecked")
                var monsterType = (net.minecraft.world.entity.EntityType<Monster>) type;
                event.register(
                        monsterType,
                        SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        Monster::checkMonsterSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE
                );
            }
        }
    }
}
