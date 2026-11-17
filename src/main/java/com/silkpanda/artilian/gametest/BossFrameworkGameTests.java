package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.TrainingGolemEntity;
import com.silkpanda.artilian.pet.PetService;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 3 Stage 1: reusable boss base, range-managed boss bar, persistence, and pet targeting. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class BossFrameworkGameTests {

    @GameTest(template = "structure_arena")
    public static void bossBarAddsNearbyPlayersAndDropsFarOnes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TrainingGolemEntity golem = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 9, 1, 9);
        golem.setNoAi(true);

        ServerPlayer near = CustomTntGameTests.spawnSurvivalPlayer(helper, "near", new Vec3(19.5, 1.0, 9.5)); // 10 blocks from the golem
        ServerPlayer far = CustomTntGameTests.spawnSurvivalPlayer(helper, "far", new Vec3(69.5, 1.0, 9.5)); // 60 blocks from the golem

        golem.updateBossBarPlayers();
        helper.assertTrue(golem.getBossEvent().getPlayers().contains(near),
                "Player 10 blocks away must see the boss bar");
        helper.assertFalse(golem.getBossEvent().getPlayers().contains(far),
                "Player 60 blocks away must not see the boss bar");

        near.moveTo(helper.absoluteVec(new Vec3(69.5, 1.0, 9.5)));
        golem.updateBossBarPlayers();
        helper.assertFalse(golem.getBossEvent().getPlayers().contains(near),
                "Player who walked out of range must be removed from the boss bar");

        golem.discard();
        CustomTntGameTests.removeTestPlayer(level, near);
        CustomTntGameTests.removeTestPlayer(level, far);
        helper.succeed();
    }

    @GameTest(template = "structure_arena")
    public static void bossBarTracksHealthAndClearsOnDeath(GameTestHelper helper) {
        TrainingGolemEntity golem = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 9, 1, 9);
        golem.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "player", new Vec3(14.5, 1.0, 9.5));

        golem.updateBossBarPlayers();
        helper.assertTrue(golem.getBossEvent().getPlayers().contains(player),
                "Nearby player must be on the bar before the health check");

        golem.setHealth(golem.getMaxHealth() / 2.0F);
        golem.updateBossBarProgress();
        helper.assertTrue(Math.abs(golem.getBossEvent().getProgress() - 0.5F) < 0.01F,
                "Boss bar progress must track health: expected 0.5, got " + golem.getBossEvent().getProgress());

        golem.kill();
        helper.assertTrue(golem.getBossEvent().getPlayers().isEmpty(),
                "Boss death must clear every player from the bar");
        CustomTntGameTests.removeTestPlayer(helper.getLevel(), player);
        helper.succeed();
    }

    @GameTest(template = "structure_arena")
    public static void bossNeverDespawns(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TrainingGolemEntity golem = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 9, 1, 9);
        golem.setNoAi(true);

        helper.assertFalse(golem.removeWhenFarAway(10000),
                "Bosses must never despawn when the player is far away");

        SpawnGroupData data = golem.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(golem.blockPosition()),
                MobSpawnType.MOB_SUMMONED,
                null);
        helper.assertTrue(golem.isPersistenceRequired(),
                "Boss must be persistent after finalizeSpawn (spawn data: " + data + ")");

        golem.discard();
        helper.succeed();
    }

    @GameTest(template = "structure_arena")
    public static void bossIsInBossTagAndPetsTargetIt(GameTestHelper helper) {
        TrainingGolemEntity golem = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 9, 1, 9);
        golem.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "player", new Vec3(14.5, 1.0, 9.5));

        helper.assertTrue(ArtilianEntities.TRAINING_GOLEM.get().is(ArtilianTags.EntityTypes.BOSSES),
                "TRAINING_GOLEM must be in the artilian:bosses entity tag");
        helper.assertTrue(PetService.isValidBattleTarget(player, golem),
                "Pets must be allowed to target a tagged boss");

        golem.discard();
        CustomTntGameTests.removeTestPlayer(helper.getLevel(), player);
        helper.succeed();
    }
}
