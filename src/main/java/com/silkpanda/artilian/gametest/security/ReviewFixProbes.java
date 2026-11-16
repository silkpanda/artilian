package com.silkpanda.artilian.gametest.security;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.config.ArtilianConfig;
import com.silkpanda.artilian.gametest.CustomTntGameTests;
import com.silkpanda.artilian.tnt.ArtilianPrimedTnt;
import com.silkpanda.artilian.tnt.TntVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Security probes from the Phase 1 code review ({@code REVIEW-01}, {@code REVIEW-02}), logged in
 * {@code security/FINDINGS.md}. Both guard {@code KidSafeTargets}: what a custom TNT may hurt.
 */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class ReviewFixProbes {

    private static void buildStoneFloor(GameTestHelper helper) {
        for (int x = 2; x <= 12; x++) {
            for (int z = 2; z <= 12; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
    }

    private static AABB arenaBox(GameTestHelper helper) {
        return new AABB(helper.absolutePos(new BlockPos(0, 0, 0))).minmax(
                new AABB(helper.absolutePos(new BlockPos(14, 14, 14))));
    }

    private static void detonate(GameTestHelper helper, TntVariant variant) {
        ServerLevel level = helper.getLevel();
        Vec3 center = helper.absoluteVec(new Vec3(7.5, 2.0, 7.5));
        ArtilianPrimedTnt tnt = new ArtilianPrimedTnt(level, center.x, center.y, center.z, null, variant);
        tnt.setFuse(1);
        level.addFreshEntity(tnt);
        tnt.tick();
    }

    /**
     * Lights one Cluster Quarry TNT two blocks from a tamed wolf, a villager, an iron golem, and a
     * pile of dropped diamonds. None of them may be touched. The zombie beside them must be hurt,
     * which proves the blast really went off.
     */
    private static void quarrySparesFriendsAndItems(GameTestHelper helper, String mode) {
        ServerLevel level = helper.getLevel();
        buildStoneFloor(helper);

        ServerPlayer alex = CustomTntGameTests.spawnSurvivalPlayer(helper, "AlexQuarry" + mode, new Vec3(4.5, 2.0, 4.5));
        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, new Vec3(9.5, 2.0, 7.5));
        wolf.tame(alex);
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new Vec3(5.5, 2.0, 7.5));
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new Vec3(7.5, 2.0, 10.0));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(7.5, 2.0, 5.0));
        Vec3 itemAbs = helper.absoluteVec(new Vec3(9.0, 2.0, 9.0));
        ItemEntity diamonds = new ItemEntity(level, itemAbs.x, itemAbs.y, itemAbs.z, new ItemStack(Items.DIAMOND, 3));
        level.addFreshEntity(diamonds);

        float wolfHp = wolf.getHealth();
        float villagerHp = villager.getHealth();
        float golemHp = golem.getHealth();
        float zombieHp = zombie.getHealth();
        List<Entity> spawned = List.of(wolf, villager, golem, zombie, diamonds);

        try {
            detonate(helper, TntVariant.CLUSTER_QUARRY);

            helper.assertTrue(wolf.isAlive() && wolf.getHealth() == wolfHp,
                    "EXPLOITED REVIEW-01 (" + mode + "): Cluster Quarry hurt a tamed wolf, hp " + wolfHp + " -> " + wolf.getHealth());
            helper.assertTrue(villager.isAlive() && villager.getHealth() == villagerHp,
                    "EXPLOITED REVIEW-01 (" + mode + "): Cluster Quarry hurt a villager, hp " + villagerHp + " -> " + villager.getHealth());
            helper.assertTrue(golem.isAlive() && golem.getHealth() == golemHp,
                    "EXPLOITED REVIEW-01 (" + mode + "): Cluster Quarry hurt an iron golem, hp " + golemHp + " -> " + golem.getHealth());
            helper.assertTrue(diamonds.isAlive(),
                    "EXPLOITED REVIEW-01 (" + mode + "): Cluster Quarry destroyed dropped items");
            helper.assertTrue(!zombie.isAlive() || zombie.getHealth() < zombieHp,
                    "Cluster Quarry should still hurt monsters (" + mode + "), zombie hp " + zombie.getHealth());
        } finally {
            level.getEntitiesOfClass(ArtilianPrimedTnt.class, arenaBox(helper)).forEach(ArtilianPrimedTnt::discard);
            spawned.forEach(Entity::discard);
            CustomTntGameTests.removeTestPlayer(level, alex);
        }
        helper.succeed();
    }

    /** REVIEW-01: with the default kid-safe config, Cluster Quarry spares pets, villagers, golems, and dropped items. */
    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void review_01_quarrySafeModeSparesPetsVillagersAndItems(GameTestHelper helper) {
        helper.assertFalse(ArtilianConfig.isDestructiveTntAllowed(),
                "Default config must disable destructive custom TNTs");
        quarrySparesFriendsAndItems(helper, "safe");
    }

    /** REVIEW-01: the same holds when an adult has switched block-breaking on. */
    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void review_01b_quarryDestructiveModeStillSparesThem(GameTestHelper helper) {
        try {
            ArtilianConfig.setTestOverride(true);
            quarrySparesFriendsAndItems(helper, "destructive");
        } finally {
            ArtilianConfig.setTestOverride(null);
        }
    }

    /** REVIEW-02: Frost Nova freezes monsters only. Villagers, baby animals, and golems are left alone. */
    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void review_02_frostNovaSparesVillagersBabiesAndGolems(GameTestHelper helper) {
        buildStoneFloor(helper);

        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new Vec3(5.5, 2.0, 7.5));
        Cow calf = helper.spawnWithNoFreeWill(EntityType.COW, new Vec3(9.5, 2.0, 7.5));
        calf.setBaby(true);
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new Vec3(7.5, 2.0, 10.0));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(7.5, 2.0, 5.0));

        float villagerHp = villager.getHealth();
        float calfHp = calf.getHealth();
        float golemHp = golem.getHealth();
        float zombieHp = zombie.getHealth();
        List<Entity> spawned = List.of(villager, calf, golem, zombie);

        try {
            detonate(helper, TntVariant.FROST_NOVA);

            helper.assertTrue(villager.getHealth() == villagerHp && !villager.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                    "EXPLOITED REVIEW-02: Frost Nova froze a villager, hp " + villagerHp + " -> " + villager.getHealth());
            helper.assertTrue(calf.getHealth() == calfHp && !calf.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                    "EXPLOITED REVIEW-02: Frost Nova froze a baby cow, hp " + calfHp + " -> " + calf.getHealth());
            helper.assertTrue(golem.getHealth() == golemHp && !golem.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                    "EXPLOITED REVIEW-02: Frost Nova froze an iron golem, hp " + golemHp + " -> " + golem.getHealth());
            helper.assertTrue(zombie.getHealth() < zombieHp && zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                    "Frost Nova should still freeze monsters, zombie hp " + zombie.getHealth());
        } finally {
            spawned.forEach(Entity::discard);
        }
        helper.succeed();
    }
}
