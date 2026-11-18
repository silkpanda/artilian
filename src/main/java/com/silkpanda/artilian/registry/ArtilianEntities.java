package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.enemy.AstralStalkerEntity;
import com.silkpanda.artilian.enemy.CinderRaiderEntity;
import com.silkpanda.artilian.enemy.PrismSentinelEntity;
import com.silkpanda.artilian.pet.CinderCragEntity;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import com.silkpanda.artilian.pet.EmberDrakeEntity;
import com.silkpanda.artilian.pet.FlurrywingEntity;
import com.silkpanda.artilian.pet.FrogduckEntity;
import com.silkpanda.artilian.pet.GalecornEntity;
import com.silkpanda.artilian.pet.PeepkinEntity;
import com.silkpanda.artilian.pet.PrismlingEntity;
import com.silkpanda.artilian.pet.ShadowWispEntity;
import com.silkpanda.artilian.pet.SunbeamEntity;
import com.silkpanda.artilian.pet.ZapkinEntity;
import com.silkpanda.artilian.pet.ZaphoxEntity;
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

    /** Shadow Wisp (working name for Steve's Shadow Creature): large Shadow pet native to Starlight Highlands. */
    public static final DeferredHolder<EntityType<?>, EntityType<ShadowWispEntity>> SHADOW_WISP =
            ENTITY_TYPES.register("shadow_wisp", () -> EntityType.Builder.of(ShadowWispEntity::new, MobCategory.CREATURE)
                    .sized(1.45F, 2.68F)
                    .eyeHeight(2.27F)
                    .clientTrackingRange(10)
                    .build("shadow_wisp"));

    /** Cinder Crag (working name for Steve's Fire/Rock Creature): Fire/Earth pet spawning near lava. */
    public static final DeferredHolder<EntityType<?>, EntityType<CinderCragEntity>> CINDER_CRAG =
            ENTITY_TYPES.register("cinder_crag", () -> EntityType.Builder.of(CinderCragEntity::new, MobCategory.CREATURE)
                    .fireImmune()
                    .sized(0.85F, 1.05F)
                    .eyeHeight(0.55F)
                    .clientTrackingRange(10)
                    .build("cinder_crag"));

    /** Zaphox: Alex's first creature, a large elephant-sized flying cheetah/fox/elephant Electric/Air pet native to Starlight Highlands. */
    public static final DeferredHolder<EntityType<?>, EntityType<ZaphoxEntity>> ZAPHOX =
            ENTITY_TYPES.register("zaphox", () -> EntityType.Builder.of(ZaphoxEntity::new, MobCategory.CREATURE)
                    .sized(1.85F, 2.15F)
                    .eyeHeight(1.65F)
                    .clientTrackingRange(10)
                    .build("zaphox"));

    /** Galecorn: Alex's second creature, an Air/Nature fox-unicorn that whips up whirlwind knockbacks. */
    public static final DeferredHolder<EntityType<?>, EntityType<GalecornEntity>> GALECORN =
            ENTITY_TYPES.register("galecorn", () -> EntityType.Builder.of(GalecornEntity::new, MobCategory.CREATURE)
                    .sized(0.80F, 0.80F)
                    .eyeHeight(0.60F)
                    .clientTrackingRange(10)
                    .build("galecorn"));

    /** Ember Drake: a small Dragon/Fire hatchling native to Emberbloom Meadows. */
    public static final DeferredHolder<EntityType<?>, EntityType<EmberDrakeEntity>> EMBER_DRAKE =
            ENTITY_TYPES.register("ember_drake", () -> EntityType.Builder.of(EmberDrakeEntity::new, MobCategory.CREATURE)
                    .fireImmune()
                    .sized(0.75F, 0.72F)
                    .eyeHeight(0.54F)
                    .clientTrackingRange(10)
                    .build("ember_drake"));

    /** Peepkin: a curious Shadow/Starlight eye-themed pet native to Starlight Highlands & Crystal Canopy Woods. */
    public static final DeferredHolder<EntityType<?>, EntityType<PeepkinEntity>> PEEPKIN =
            ENTITY_TYPES.register("peepkin", () -> EntityType.Builder.of(PeepkinEntity::new, MobCategory.CREATURE)
                    .sized(0.72F, 0.85F)
                    .eyeHeight(0.55F)
                    .clientTrackingRange(10)
                    .build("peepkin"));

    /** Prismling: the legendary rainbow all-types pet for everyone, rare in all three Overworld biomes. */
    public static final DeferredHolder<EntityType<?>, EntityType<PrismlingEntity>> PRISMLING =
            ENTITY_TYPES.register("prismling", () -> EntityType.Builder.of(PrismlingEntity::new, MobCategory.CREATURE)
                    .sized(2.8F, 4.1F)
                    .eyeHeight(3.3F)
                    .clientTrackingRange(10)
                    .build("prismling"));

    /** Flurrywing: the kids' four-winged snow-fairy snowman, Ice / Air, about 2.75 blocks tall, Starlight Highlands only. */
    public static final DeferredHolder<EntityType<?>, EntityType<FlurrywingEntity>> FLURRYWING =
            ENTITY_TYPES.register("flurrywing", () -> EntityType.Builder.of(FlurrywingEntity::new, MobCategory.CREATURE)
                    .sized(1.3F, 2.75F)
                    .eyeHeight(2.3F)
                    .clientTrackingRange(10)
                    .build("flurrywing"));

    /** Sunbeam: the legendary Sunsteel sun pet, ghast-tall, rare in Emberbloom Meadows. */
    public static final DeferredHolder<EntityType<?>, EntityType<SunbeamEntity>> SUNBEAM =
            ENTITY_TYPES.register("sunbeam", () -> EntityType.Builder.of(SunbeamEntity::new, MobCategory.CREATURE)
                    .sized(3.0F, 4.0F)
                    .eyeHeight(3.3F)
                    .clientTrackingRange(10)
                    .build("sunbeam"));

    /** Frogduck: the kids' amphibious teal frog with an orange duck on its head, Water / Nature, Crystal Canopy Woods. */
    public static final DeferredHolder<EntityType<?>, EntityType<FrogduckEntity>> FROGDUCK =
            ENTITY_TYPES.register("frogduck", () -> EntityType.Builder.of(FrogduckEntity::new, MobCategory.CREATURE)
                    .sized(1.25F, 1.35F)
                    .eyeHeight(0.75F)
                    .clientTrackingRange(10)
                    .build("frogduck"));
    /** Zapkin: kid-drawn Electric buddy native to Starlight Highlands. */
    public static final DeferredHolder<EntityType<?>, EntityType<ZapkinEntity>> ZAPKIN =
            ENTITY_TYPES.register("zapkin", () -> EntityType.Builder.of(ZapkinEntity::new, MobCategory.CREATURE)
                    .sized(0.95F, 1.55F)
                    .eyeHeight(1.10F)
                    .clientTrackingRange(10)
                    .build("zapkin"));

    /** Prism Sentinel: Crystal/Earth guardian of the Crystal Grove Shrine. */
    public static final DeferredHolder<EntityType<?>, EntityType<PrismSentinelEntity>> PRISM_SENTINEL =
            ENTITY_TYPES.register("prism_sentinel", () -> EntityType.Builder.of(PrismSentinelEntity::new, MobCategory.MONSTER)
                    .sized(0.95F, 2.05F)
                    .eyeHeight(1.75F)
                    .clientTrackingRange(10)
                    .build("prism_sentinel"));

    /** Cinder Raider: Fire/Earth guardian of the Emberbloom Outpost. */
    public static final DeferredHolder<EntityType<?>, EntityType<CinderRaiderEntity>> CINDER_RAIDER =
            ENTITY_TYPES.register("cinder_raider", () -> EntityType.Builder.of(CinderRaiderEntity::new, MobCategory.MONSTER)
                    .fireImmune()
                    .sized(0.80F, 1.75F)
                    .eyeHeight(1.45F)
                    .clientTrackingRange(10)
                    .build("cinder_raider"));

    /** Astral Stalker: Starlight/Shadow guardian of the Starlight Watchtower. */
    public static final DeferredHolder<EntityType<?>, EntityType<AstralStalkerEntity>> ASTRAL_STALKER =
            ENTITY_TYPES.register("astral_stalker", () -> EntityType.Builder.of(AstralStalkerEntity::new, MobCategory.MONSTER)
                    .sized(0.85F, 1.85F)
                    .eyeHeight(1.55F)
                    .clientTrackingRange(10)
                    .build("astral_stalker"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
