package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.pet.CinderCragEntity;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import com.silkpanda.artilian.pet.ShadowWispEntity;
import com.silkpanda.artilian.tnt.ArtilianPrimedTnt;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArtilianEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ArtilianMod.MODID);

    /** 14 pixels wide and 16 tall: a chest standing on two small feet. */
    public static final DeferredHolder<EntityType<?>, EntityType<ChesterEntity>> CHESTER =
            ENTITY_TYPES.register("chester", () -> EntityType.Builder.of(ChesterEntity::new, MobCategory.MISC)
                    .sized(0.875F, 1.0F)
                    .clientTrackingRange(10)
                    .build("chester"));

    public static final DeferredHolder<EntityType<?>, EntityType<ArtilianPrimedTnt>> PRIMED_TNT =
            ENTITY_TYPES.register("primed_tnt", () -> EntityType.Builder.<ArtilianPrimedTnt>of(ArtilianPrimedTnt::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.98F, 0.98F)
                    .eyeHeight(0.15F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_tnt"));

    /** Crystal Fox: playful Crystal/Nature pet native to Crystal Canopy Woods. */
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalFoxEntity>> CRYSTAL_FOX =
            ENTITY_TYPES.register("crystal_fox", () -> EntityType.Builder.of(CrystalFoxEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 0.65F)
                    .eyeHeight(0.45F)
                    .clientTrackingRange(10)
                    .build("crystal_fox"));

    /** Shadow Wisp (working name for Bob's Shadow Creature): Shadow pet native to Starlight Highlands. */
    public static final DeferredHolder<EntityType<?>, EntityType<ShadowWispEntity>> SHADOW_WISP =
            ENTITY_TYPES.register("shadow_wisp", () -> EntityType.Builder.of(ShadowWispEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 1.15F)
                    .eyeHeight(0.95F)
                    .clientTrackingRange(10)
                    .build("shadow_wisp"));

    /** Cinder Crag (working name for Bob's Fire/Rock Creature): Fire/Earth pet spawning near lava. */
    public static final DeferredHolder<EntityType<?>, EntityType<CinderCragEntity>> CINDER_CRAG =
            ENTITY_TYPES.register("cinder_crag", () -> EntityType.Builder.of(CinderCragEntity::new, MobCategory.CREATURE)
                    .fireImmune()
                    .sized(0.85F, 1.05F)
                    .eyeHeight(0.55F)
                    .clientTrackingRange(10)
                    .build("cinder_crag"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
