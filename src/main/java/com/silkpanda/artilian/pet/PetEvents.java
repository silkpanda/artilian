package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/** Wires entity attributes and natural spawn placements for Artilian pets. */
public final class PetEvents {
    private PetEvents() {}

    @EventBusSubscriber(modid = ArtilianMod.MODID)
    public static final class Setup {
        private Setup() {}

        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(ArtilianEntities.CRYSTAL_FOX.get(), CrystalFoxEntity.createAttributes().build());
            event.put(ArtilianEntities.SHADOW_WISP.get(), ShadowWispEntity.createAttributes().build());
            event.put(ArtilianEntities.CINDER_CRAG.get(), CinderCragEntity.createAttributes().build());
            event.put(ArtilianEntities.ZAPHOX.get(), ZaphoxEntity.createAttributes().build());
            event.put(ArtilianEntities.GALECORN.get(), GalecornEntity.createAttributes().build());
            event.put(ArtilianEntities.EMBER_DRAKE.get(), EmberDrakeEntity.createAttributes().build());
            event.put(ArtilianEntities.PEEPKIN.get(), PeepkinEntity.createAttributes().build());
            event.put(ArtilianEntities.PRISMLING.get(), PrismlingEntity.createAttributes().build());
            event.put(ArtilianEntities.FLURRYWING.get(), FlurrywingEntity.createAttributes().build());
            event.put(ArtilianEntities.SUNBEAM.get(), SunbeamEntity.createAttributes().build());
            event.put(ArtilianEntities.FROGDUCK.get(), FrogduckEntity.createAttributes().build());
            event.put(ArtilianEntities.ZAPKIN.get(), ZapkinEntity.createAttributes().build());
            event.put(ArtilianEntities.FANGWING.get(), FangwingEntity.createAttributes().build());
            event.put(ArtilianEntities.BLINKY.get(), BlinkyEntity.createAttributes().build());
            event.put(ArtilianEntities.CANOPY_DRAGONLING.get(), CanopyDragonlingEntity.createAttributes().build());
            event.put(ArtilianEntities.AMBER_DRAGONLING.get(), AmberDragonlingEntity.createAttributes().build());
            event.put(ArtilianEntities.STORM_DRAGONLING.get(), StormDragonlingEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
            event.register(
                    ArtilianEntities.CRYSTAL_FOX.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    ArtilianPetEntity::checkPetSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.SHADOW_WISP.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    ShadowWispEntity::checkShadowSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.CINDER_CRAG.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    CinderCragEntity::checkLavaSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.ZAPHOX.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    ZaphoxEntity::checkZaphoxSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.GALECORN.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    GalecornEntity::checkGalecornSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.EMBER_DRAKE.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    EmberDrakeEntity::checkDragonHatchlingSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.PEEPKIN.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    PeepkinEntity::checkEyePetSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.PRISMLING.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    PrismlingEntity::checkPrismlingSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.FLURRYWING.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    FlurrywingEntity::checkFlurrywingSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.SUNBEAM.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SunbeamEntity::checkSunbeamSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.FROGDUCK.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    FrogduckEntity::checkFrogduckSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.ZAPKIN.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    ZapkinEntity::checkZapkinSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.FANGWING.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    FangwingEntity::checkFangwingSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
            event.register(
                    ArtilianEntities.BLINKY.get(),
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BlinkyEntity::checkBlinkySpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        }
    }
}
