package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.dimension.DragonJungleDimension;
import com.silkpanda.artilian.dragon.CanopyWyvernEntity;
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
                "assets/artilian/textures/entity/canopy_wyvern.png",
                "assets/artilian/textures/entity/canopy_wyvern_glowmask.png",
                "assets/artilian/textures/item/dragon_scale.png")) {
            helper.assertTrue(cl.getResource(path) != null, "Missing dragon asset: " + path);
        }
        helper.succeed();
    }
}
