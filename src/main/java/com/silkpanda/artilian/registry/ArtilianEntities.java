package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.CaptainBrimstoneEntity;
import com.silkpanda.artilian.boss.CrystalColossusEntity;
import com.silkpanda.artilian.boss.GreatAllSeerEntity;
import com.silkpanda.artilian.boss.TrainingGolemEntity;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.dragon.AmberWyvernEntity;
import com.silkpanda.artilian.dragon.CanopyWyvernEntity;
import com.silkpanda.artilian.dragon.StormGlidewingEntity;
import com.silkpanda.artilian.enemy.AstralStalkerEntity;
import com.silkpanda.artilian.enemy.CinderRaiderEntity;
import com.silkpanda.artilian.enemy.LensGuardianEntity;
import com.silkpanda.artilian.enemy.OculusSlimeEntity;
import com.silkpanda.artilian.enemy.PeeperEntity;
import com.silkpanda.artilian.enemy.PrismSentinelEntity;
import com.silkpanda.artilian.pet.AmberDragonlingEntity;
import com.silkpanda.artilian.pet.BlinkyEntity;
import com.silkpanda.artilian.pet.CanopyDragonlingEntity;
import com.silkpanda.artilian.pet.CinderCragEntity;
import com.silkpanda.artilian.pet.CrystalFoxEntity;
import com.silkpanda.artilian.pet.EmberDrakeEntity;
import com.silkpanda.artilian.pet.FlurrywingEntity;
import com.silkpanda.artilian.pet.FrogduckEntity;
import com.silkpanda.artilian.pet.GalecornEntity;
import com.silkpanda.artilian.pet.PeepkinEntity;
import com.silkpanda.artilian.pet.PrismlingEntity;
import com.silkpanda.artilian.pet.ShadowWispEntity;
import com.silkpanda.artilian.pet.StormDragonlingEntity;
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

    /** Peeper: agile Shadow/Air eyeball scout of the Eye Realm. */
    public static final DeferredHolder<EntityType<?>, EntityType<PeeperEntity>> PEEPER =
            ENTITY_TYPES.register("peeper", () -> EntityType.Builder.of(PeeperEntity::new, MobCategory.MONSTER)
                    .sized(0.75F, 0.90F)
                    .eyeHeight(0.55F)
                    .clientTrackingRange(10)
                    .build("peeper"));

    /** Oculus Slime: bouncy Shadow/Crystal gel cube of the Eye Realm. */
    public static final DeferredHolder<EntityType<?>, EntityType<OculusSlimeEntity>> OCULUS_SLIME =
            ENTITY_TYPES.register("oculus_slime", () -> EntityType.Builder.of(OculusSlimeEntity::new, MobCategory.MONSTER)
                    .sized(1.15F, 1.15F)
                    .eyeHeight(0.65F)
                    .clientTrackingRange(10)
                    .build("oculus_slime"));

    /** Lens Guardian: towering Starlight/Shadow armored sentinel of the Eye Realm. */
    public static final DeferredHolder<EntityType<?>, EntityType<LensGuardianEntity>> LENS_GUARDIAN =
            ENTITY_TYPES.register("lens_guardian", () -> EntityType.Builder.of(LensGuardianEntity::new, MobCategory.MONSTER)
                    .sized(1.10F, 2.35F)
                    .eyeHeight(2.00F)
                    .clientTrackingRange(10)
                    .build("lens_guardian"));

    /** Blinky: Alex's friendly Starlight/Shadow ocular familiar native to the Eye Realm. */
    public static final DeferredHolder<EntityType<?>, EntityType<BlinkyEntity>> BLINKY =
            ENTITY_TYPES.register("blinky", () -> EntityType.Builder.of(BlinkyEntity::new, MobCategory.CREATURE)
                    .sized(0.85F, 0.95F)
                    .eyeHeight(0.60F)
                    .clientTrackingRange(10)
                    .build("blinky"));

    /** Test-only boss for the boss framework. Not in the creative tab and has no spawn egg. */
    public static final DeferredHolder<EntityType<?>, EntityType<TrainingGolemEntity>> TRAINING_GOLEM =
            ENTITY_TYPES.register("training_golem", () -> EntityType.Builder.of(TrainingGolemEntity::new, MobCategory.MONSTER)
                    .sized(1.6F, 2.6F)
                    .eyeHeight(2.2F)
                    .clientTrackingRange(10)
                    .build("training_golem"));

    /** Crystal Colossus: armored Crystal/Starlight titan boss of the Crystal Canopy Woods. */
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalColossusEntity>> CRYSTAL_COLOSSUS =
            ENTITY_TYPES.register("crystal_colossus", () -> EntityType.Builder.of(CrystalColossusEntity::new, MobCategory.MONSTER)
                    .sized(2.2F, 3.8F)
                    .eyeHeight(3.2F)
                    .clientTrackingRange(12)
                    .build("crystal_colossus"));

    /** Captain Brimstone: magma-forged Fire/Shadow pirate galleon-walker boss of Emberbloom Meadows. */
    public static final DeferredHolder<EntityType<?>, EntityType<CaptainBrimstoneEntity>> CAPTAIN_BRIMSTONE =
            ENTITY_TYPES.register("captain_brimstone", () -> EntityType.Builder.of(CaptainBrimstoneEntity::new, MobCategory.MONSTER)
                    .sized(2.8F, 4.2F)
                    .eyeHeight(3.2F)
                    .clientTrackingRange(12)
                    .build("captain_brimstone"));

    /** The Great All-Seer: hovering Shadow/Starlight ocular sovereign boss of the Eye Realm. */
    public static final DeferredHolder<EntityType<?>, EntityType<GreatAllSeerEntity>> GREAT_ALL_SEER =
            ENTITY_TYPES.register("great_all_seer", () -> EntityType.Builder.of(GreatAllSeerEntity::new, MobCategory.MONSTER)
                    .sized(1.8F, 2.8F)
                    .eyeHeight(2.0F)
                    .clientTrackingRange(12)
                    .build("great_all_seer"));

    /** Canopy Wyvern: first wild Dragon Jungle dragon species, establishing the shared dragon_rig. */
    public static final DeferredHolder<EntityType<?>, EntityType<CanopyWyvernEntity>> CANOPY_WYVERN =
            ENTITY_TYPES.register("canopy_wyvern", () -> EntityType.Builder.of(CanopyWyvernEntity::new, MobCategory.MONSTER)
                    .sized(1.55F, 1.95F)
                    .eyeHeight(1.55F)
                    .clientTrackingRange(12)
                    .build("canopy_wyvern"));

    /** Amber Wyvern: volcanic dragon species inhabiting the Amber Volcanic Grove. */
    public static final DeferredHolder<EntityType<?>, EntityType<AmberWyvernEntity>> AMBER_WYVERN =
            ENTITY_TYPES.register("amber_wyvern", () -> EntityType.Builder.of(AmberWyvernEntity::new, MobCategory.MONSTER)
                    .sized(1.6F, 2.0F)
                    .eyeHeight(1.6F)
                    .fireImmune()
                    .clientTrackingRange(12)
                    .build("amber_wyvern"));

    /** Storm Glidewing: swift electric sky-strider across both Dragon Jungle biomes. */
    public static final DeferredHolder<EntityType<?>, EntityType<StormGlidewingEntity>> STORM_GLIDEWING =
            ENTITY_TYPES.register("storm_glidewing", () -> EntityType.Builder.of(StormGlidewingEntity::new, MobCategory.MONSTER)
                    .sized(1.45F, 1.85F)
                    .eyeHeight(1.5F)
                    .clientTrackingRange(12)
                    .build("storm_glidewing"));

    // Phase 5 Stage 6: Hatchable Pet Dragons
    public static final DeferredHolder<EntityType<?>, EntityType<CanopyDragonlingEntity>> CANOPY_DRAGONLING =
            ENTITY_TYPES.register("canopy_dragonling", () -> EntityType.Builder.of(CanopyDragonlingEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 0.85F)
                    .eyeHeight(0.60F)
                    .clientTrackingRange(10)
                    .build("canopy_dragonling"));

    public static final DeferredHolder<EntityType<?>, EntityType<AmberDragonlingEntity>> AMBER_DRAGONLING =
            ENTITY_TYPES.register("amber_dragonling", () -> EntityType.Builder.of(AmberDragonlingEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 0.90F)
                    .eyeHeight(0.65F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("amber_dragonling"));

    public static final DeferredHolder<EntityType<?>, EntityType<StormDragonlingEntity>> STORM_DRAGONLING =
            ENTITY_TYPES.register("storm_dragonling", () -> EntityType.Builder.of(StormDragonlingEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 0.85F)
                    .eyeHeight(0.60F)
                    .clientTrackingRange(10)
                    .build("storm_dragonling"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
