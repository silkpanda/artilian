package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
import com.silkpanda.artilian.enemy.EyeRealmEnemyEntity;
import com.silkpanda.artilian.enemy.LensGuardianEntity;
import com.silkpanda.artilian.enemy.OculusSlimeEntity;
import com.silkpanda.artilian.enemy.PeeperEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Phase 4 Stage 3: Eye Realm enemies (Peeper, Oculus Slime, Lens Guardian), spawns, effects, and assets. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class EyeRealmEnemiesGameTests {

    @GameTest(template = "tnt_arena")
    public static void eyeRealmEnemiesRegisteredTaggedHaveEggsLootAndElementalTypes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        List<EntityType<? extends EyeRealmEnemyEntity>> types = List.of(
                ArtilianEntities.PEEPER.get(),
                ArtilianEntities.OCULUS_SLIME.get(),
                ArtilianEntities.LENS_GUARDIAN.get()
        );
        for (EntityType<? extends EyeRealmEnemyEntity> type : types) {
            helper.assertTrue(type.is(ArtilianTags.EntityTypes.EYE_REALM_ENEMIES),
                    type + " must be tagged artilian:eye_realm_enemies");
            helper.assertTrue(SpawnEggItem.byId(type) != null,
                    type + " must have a registered spawn egg");
            var lootTable = level.getServer().reloadableRegistries().getLootTable(type.getDefaultLootTable());
            helper.assertTrue(lootTable != LootTable.EMPTY,
                    type + " must have a non-empty entity loot table");
        }

        PeeperEntity peeper = ArtilianEntities.PEEPER.get().create(level);
        OculusSlimeEntity slime = ArtilianEntities.OCULUS_SLIME.get().create(level);
        LensGuardianEntity guardian = ArtilianEntities.LENS_GUARDIAN.get().create(level);
        helper.assertTrue(peeper != null && slime != null && guardian != null,
                "All three Eye Realm enemies must instantiate cleanly");

        helper.assertTrue(peeper.getMaxHealth() == 22.0F
                        && peeper.getPrimaryElement() == PetElement.SHADOW
                        && peeper.getSecondaryElement().equals(Optional.of(PetElement.AIR)),
                "Peeper must be 22 HP SHADOW / AIR");
        helper.assertTrue(slime.getMaxHealth() == 34.0F
                        && slime.getPrimaryElement() == PetElement.SHADOW
                        && slime.getSecondaryElement().equals(Optional.of(PetElement.CRYSTAL)),
                "Oculus Slime must be 34 HP SHADOW / CRYSTAL");
        helper.assertTrue(guardian.getMaxHealth() == 54.0F
                        && guardian.getPrimaryElement() == PetElement.STARLIGHT
                        && guardian.getSecondaryElement().equals(Optional.of(PetElement.SHADOW)),
                "Lens Guardian must be 54 HP STARLIGHT / SHADOW");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void eyeRealmBiomeRegistersMonsterSpawnsForAllThreeEnemies(GameTestHelper helper) {
        Biome eyeBiome = helper.getLevel().registryAccess()
                .registryOrThrow(Registries.BIOME)
                .get(EyeRealmDimension.EYE_REALM_BIOME);
        helper.assertTrue(eyeBiome != null, "EYE_REALM_BIOME must be registered");

        Set<EntityType<?>> spawnedMonsters = eyeBiome.getMobSettings()
                .getMobs(MobCategory.MONSTER)
                .unwrap()
                .stream()
                .map(data -> data.type)
                .collect(Collectors.toSet());

        helper.assertTrue(spawnedMonsters.contains(ArtilianEntities.PEEPER.get()),
                "EYE_REALM_BIOME must spawn Peeper");
        helper.assertTrue(spawnedMonsters.contains(ArtilianEntities.OCULUS_SLIME.get()),
                "EYE_REALM_BIOME must spawn Oculus Slime");
        helper.assertTrue(spawnedMonsters.contains(ArtilianEntities.LENS_GUARDIAN.get()),
                "EYE_REALM_BIOME must spawn Lens Guardian");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void eyeRealmEnemiesApplyElementalEffectsAndRespectKidSafeTargets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        PeeperEntity peeper = ArtilianEntities.PEEPER.get().create(level);
        OculusSlimeEntity slime = ArtilianEntities.OCULUS_SLIME.get().create(level);
        LensGuardianEntity guardian = ArtilianEntities.LENS_GUARDIAN.get().create(level);
        ChesterEntity chester = ArtilianEntities.CHESTER.get().create(level);
        Zombie dummy1 = EntityType.ZOMBIE.create(level);
        Zombie dummy2 = EntityType.ZOMBIE.create(level);
        Zombie dummy3 = EntityType.ZOMBIE.create(level);
        helper.assertTrue(peeper != null && slime != null && guardian != null && chester != null
                        && dummy1 != null && dummy2 != null && dummy3 != null,
                "Test entities must instantiate");

        for (var e : List.of(peeper, slime, guardian, chester, dummy1, dummy2, dummy3)) {
            e.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(4.5, 2.0, 4.5)));
            level.addFreshEntity(e);
        }

        helper.assertTrue(peeper.doHurtTarget(dummy1) && dummy1.hasEffect(MobEffects.GLOWING),
                "Peeper hit must apply Glowing");
        helper.assertTrue(slime.doHurtTarget(dummy2) && dummy2.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "Oculus Slime hit must apply Slowness");
        helper.assertTrue(guardian.doHurtTarget(dummy3) && dummy3.hasEffect(MobEffects.WEAKNESS),
                "Lens Guardian hit must apply Weakness");

        helper.assertFalse(peeper.doHurtTarget(chester), "Peeper must never hurt Chester");
        helper.assertFalse(slime.doHurtTarget(chester), "Oculus Slime must never hurt Chester");
        helper.assertFalse(guardian.doHurtTarget(chester), "Lens Guardian must never hurt Chester");

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void eyeRealmEnemiesGeckoLibAssetsAndGlowmasksExist(GameTestHelper helper) {
        ClassLoader cl = ArtilianMod.class.getClassLoader();
        for (String id : List.of("peeper", "oculus_slime", "lens_guardian")) {
            helper.assertTrue(cl.getResource("assets/artilian/geo/entity/" + id + ".geo.json") != null,
                    "Missing geo model for " + id);
            helper.assertTrue(cl.getResource("assets/artilian/animations/entity/" + id + ".animation.json") != null,
                    "Missing animations for " + id);
            helper.assertTrue(cl.getResource("assets/artilian/textures/entity/" + id + ".png") != null,
                    "Missing texture for " + id);
            helper.assertTrue(cl.getResource("assets/artilian/textures/entity/" + id + "_glowmask.png") != null,
                    "Missing glowmask texture for " + id);
        }
        helper.succeed();
    }
}
