package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.dimension.DragonJungleDimension;
import com.silkpanda.artilian.dragon.AmberWyvernEntity;
import com.silkpanda.artilian.dragon.CanopyWyvernEntity;
import com.silkpanda.artilian.dragon.StormGlidewingEntity;
import com.silkpanda.artilian.dragon.WildDragonEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Phase 5 Stage 4: First Dragon Species (Canopy Wyvern) & shared GeckoLib dragon rig. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class DragonSpeciesGameTests {

    @GameTest(template = "tnt_arena")
    public static void canopyWyvernRegisteredTaggedHasEggLootAndElements(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        EntityType<CanopyWyvernEntity> type = ArtilianEntities.CANOPY_WYVERN.get();

        helper.assertTrue(type.is(ArtilianTags.EntityTypes.WILD_DRAGONS),
                "Canopy Wyvern must be tagged artilian:wild_dragons");
        helper.assertTrue(SpawnEggItem.byId(type) == ArtilianItems.CANOPY_WYVERN_SPAWN_EGG.get(),
                "Canopy Wyvern must have its registered spawn egg");

        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(type.getDefaultLootTable());
        helper.assertTrue(lootTable != LootTable.EMPTY,
                "Canopy Wyvern must have a non-empty entity loot table");

        CanopyWyvernEntity wyvern = type.create(level);
        helper.assertTrue(wyvern != null, "Canopy Wyvern must instantiate cleanly");
        helper.assertTrue(wyvern.getMaxHealth() == 44.0F,
                "Canopy Wyvern max health must be 44.0, got " + wyvern.getMaxHealth());
        helper.assertTrue(wyvern.getPrimaryElement() == PetElement.DRAGON
                        && wyvern.getSecondaryElement().equals(Optional.of(PetElement.NATURE)),
                "Canopy Wyvern must be DRAGON / NATURE");
        helper.assertFalse(wyvern.causeFallDamage(12.0F, 1.0F, level.damageSources().fall()),
                "Winged Canopy Wyvern must not take fall damage");

        wyvern.setPos(helper.absoluteVec(new Vec3(3.5, 2.0, 3.5)));
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, wyvern)
                .withParameter(LootContextParams.ORIGIN, wyvern.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        boolean droppedDragonScale = lootTable.getRandomItems(params).stream()
                .anyMatch(stack -> stack.is(ArtilianItems.DRAGON_SCALE.get()));
        helper.assertTrue(droppedDragonScale, "Canopy Wyvern loot table must drop Dragon Scale");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void dragonJungleBiomeRegistersCanopyWyvernSpawn(GameTestHelper helper) {
        Biome dragonJungle = helper.getLevel().registryAccess()
                .registryOrThrow(Registries.BIOME)
                .get(DragonJungleDimension.DRAGON_JUNGLE_BIOME);
        helper.assertTrue(dragonJungle != null, "DRAGON_JUNGLE_BIOME must be registered");

        Set<EntityType<?>> spawnedMonsters = dragonJungle.getMobSettings()
                .getMobs(MobCategory.MONSTER)
                .unwrap()
                .stream()
                .map(data -> data.type)
                .collect(Collectors.toSet());

        helper.assertTrue(spawnedMonsters.contains(ArtilianEntities.CANOPY_WYVERN.get()),
                "DRAGON_JUNGLE_BIOME must include Canopy Wyvern natural spawn");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void canopyWyvernAppliesPoisonHitAndNeverHurtsChester(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        CanopyWyvernEntity wyvern = ArtilianEntities.CANOPY_WYVERN.get().create(level);
        ChesterEntity chester = ArtilianEntities.CHESTER.get().create(level);
        Creeper dummy = EntityType.CREEPER.create(level);
        helper.assertTrue(wyvern != null && chester != null && dummy != null,
                "Entities must instantiate");

        for (var e : List.of(wyvern, chester, dummy)) {
            e.setPos(helper.absoluteVec(new Vec3(4.5, 2.0, 4.5)));
            level.addFreshEntity(e);
        }

        helper.assertTrue(wyvern.doHurtTarget(dummy) && dummy.hasEffect(MobEffects.POISON),
                "Canopy Wyvern hit must apply Poison");
        helper.assertFalse(wyvern.doHurtTarget(chester),
                "Canopy Wyvern must never hurt Chester");

        wyvern.discard();
        chester.discard();
        dummy.discard();
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void sharedDragonRigAndCanopyWyvernAssetsExist(GameTestHelper helper) {
        var cl = DragonSpeciesGameTests.class.getClassLoader();
        for (String path : List.of(
                "assets/artilian/geo/entity/dragon_rig.geo.json",
                "assets/artilian/animations/entity/dragon_rig.animation.json",
                "assets/artilian/geo/entity/amber_wyvern.geo.json",
                "assets/artilian/animations/entity/amber_wyvern.animation.json",
                "assets/artilian/geo/entity/storm_glidewing.geo.json",
                "assets/artilian/animations/entity/storm_glidewing.animation.json",
                "assets/artilian/textures/entity/canopy_wyvern.png",
                "assets/artilian/textures/entity/canopy_wyvern_glowmask.png",
                "assets/artilian/textures/entity/amber_wyvern.png",
                "assets/artilian/textures/entity/amber_wyvern_glowmask.png",
                "assets/artilian/textures/entity/storm_glidewing.png",
                "assets/artilian/textures/entity/storm_glidewing_glowmask.png",
                "assets/artilian/textures/item/dragon_scale.png")) {
            helper.assertTrue(cl.getResource(path) != null, "Missing dragon asset: " + path);
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void amberWyvernAndStormGlidewingRegisteredTaggedEggsLootAndElements(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertDragonSpecies(
                helper, level,
                ArtilianEntities.AMBER_WYVERN.get(),
                ArtilianItems.AMBER_WYVERN_SPAWN_EGG.get(),
                48.0F, PetElement.DRAGON, PetElement.FIRE);
        assertDragonSpecies(
                helper, level,
                ArtilianEntities.STORM_GLIDEWING.get(),
                ArtilianItems.STORM_GLIDEWING_SPAWN_EGG.get(),
                40.0F, PetElement.DRAGON, PetElement.ELECTRIC);

        AmberWyvernEntity amber = ArtilianEntities.AMBER_WYVERN.get().create(level);
        helper.assertTrue(amber != null && amber.fireImmune(), "Amber Wyvern must be fire-immune");
        helper.succeed();
    }

    private static <T extends WildDragonEntity> void assertDragonSpecies(
            GameTestHelper helper,
            ServerLevel level,
            EntityType<T> type,
            SpawnEggItem expectedEgg,
            float expectedHp,
            PetElement primary,
            PetElement secondary) {
        helper.assertTrue(type.is(ArtilianTags.EntityTypes.WILD_DRAGONS),
                type + " must be tagged artilian:wild_dragons");
        helper.assertTrue(SpawnEggItem.byId(type) == expectedEgg,
                type + " must have its registered spawn egg");

        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(type.getDefaultLootTable());
        helper.assertTrue(lootTable != LootTable.EMPTY, type + " must have a non-empty loot table");

        T dragon = type.create(level);
        helper.assertTrue(dragon != null, type + " must instantiate cleanly");
        helper.assertTrue(dragon.getMaxHealth() == expectedHp,
                type + " max health must be " + expectedHp + ", got " + dragon.getMaxHealth());
        helper.assertTrue(dragon.getPrimaryElement() == primary
                        && dragon.getSecondaryElement().equals(Optional.of(secondary)),
                type + " must have elements " + primary + " / " + secondary);
        helper.assertFalse(dragon.causeFallDamage(12.0F, 1.0F, level.damageSources().fall()),
                type + " must not take fall damage");

        dragon.setPos(helper.absoluteVec(new Vec3(3.5, 2.0, 3.5)));
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, dragon)
                .withParameter(LootContextParams.ORIGIN, dragon.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        boolean droppedDragonScale = lootTable.getRandomItems(params).stream()
                .anyMatch(stack -> stack.is(ArtilianItems.DRAGON_SCALE.get()));
        helper.assertTrue(droppedDragonScale, type + " loot table must drop Dragon Scale");
    }

    @GameTest(template = "tnt_arena")
    public static void dragonJungleBiomesRegisterStage5DragonSpawns(GameTestHelper helper) {
        var biomeRegistry = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        Biome dragonJungle = biomeRegistry.get(DragonJungleDimension.DRAGON_JUNGLE_BIOME);
        Biome amberGrove = biomeRegistry.get(DragonJungleDimension.AMBER_GROVE_BIOME);
        helper.assertTrue(dragonJungle != null && amberGrove != null, "Both Dragon Jungle biomes must be registered");

        Set<EntityType<?>> jungleMobs = dragonJungle.getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                .map(data -> data.type).collect(Collectors.toSet());
        Set<EntityType<?>> amberMobs = amberGrove.getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                .map(data -> data.type).collect(Collectors.toSet());

        helper.assertTrue(jungleMobs.contains(ArtilianEntities.CANOPY_WYVERN.get())
                        && jungleMobs.contains(ArtilianEntities.STORM_GLIDEWING.get()),
                "DRAGON_JUNGLE_BIOME must spawn Canopy Wyvern and Storm Glidewing");
        helper.assertTrue(amberMobs.contains(ArtilianEntities.AMBER_WYVERN.get())
                        && amberMobs.contains(ArtilianEntities.STORM_GLIDEWING.get()),
                "AMBER_GROVE_BIOME must spawn Amber Wyvern and Storm Glidewing");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void amberWyvernIgnitesAndStormGlidewingSlowsWithoutHurtingChester(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AmberWyvernEntity amber = ArtilianEntities.AMBER_WYVERN.get().create(level);
        StormGlidewingEntity storm = ArtilianEntities.STORM_GLIDEWING.get().create(level);
        ChesterEntity chester = ArtilianEntities.CHESTER.get().create(level);
        Creeper dummy1 = EntityType.CREEPER.create(level);
        Creeper dummy2 = EntityType.CREEPER.create(level);
        helper.assertTrue(amber != null && storm != null && chester != null && dummy1 != null && dummy2 != null,
                "Entities must instantiate");

        for (var e : List.of(amber, storm, chester, dummy1, dummy2)) {
            e.setPos(helper.absoluteVec(new Vec3(4.5, 2.0, 4.5)));
            level.addFreshEntity(e);
        }

        helper.assertTrue(amber.doHurtTarget(dummy1) && dummy1.isOnFire(),
                "Amber Wyvern hit must ignite target");
        helper.assertTrue(storm.doHurtTarget(dummy2) && dummy2.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "Storm Glidewing hit must apply Slowness");
        helper.assertFalse(amber.doHurtTarget(chester) || storm.doHurtTarget(chester),
                "Stage 5 dragons must never hurt Chester");

        amber.discard();
        storm.discard();
        chester.discard();
        dummy1.discard();
        dummy2.discard();
        helper.succeed();
    }
}
