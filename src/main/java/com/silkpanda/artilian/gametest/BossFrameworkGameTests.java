package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.TelegraphedAttack;
import com.silkpanda.artilian.boss.TrainingGolemEntity;
import com.silkpanda.artilian.pet.PetElement;
import com.silkpanda.artilian.pet.PetService;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
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

    @GameTest(template = "structure_arena")
    public static void telegraphRefusesWindupUnderOneAndAHalfSeconds(GameTestHelper helper) {
        boolean threw = false;
        try {
            new TelegraphedAttack("x", 29, 3.0, 1.0F, PetElement.EARTH);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        helper.assertTrue(threw, "A 29-tick (under 1.5 s) windup must be rejected");

        TelegraphedAttack allowed = new TelegraphedAttack("x", 30, 3.0, 1.0F, PetElement.EARTH);
        helper.assertTrue(allowed.windupTicks() == 30, "A 30-tick windup must be accepted");
        helper.succeed();
    }

    @GameTest(template = "structure_arena", timeoutTicks = 120)
    public static void slamDealsNoDamageBeforeWindupEnds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TrainingGolemEntity golem = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 9, 1, 9);
        golem.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(
                helper, "slam-timing", new Vec3(11.5, 1.0, 9.5)); // 2 blocks from the golem
        float maxHealth = player.getMaxHealth();

        // Fresh ServerPlayers have 60 ticks of spawn protection; let it lapse before damage is checked.
        helper.runAfterDelay(61, () -> {
            helper.assertTrue(golem.startTelegraph(TrainingGolemEntity.GROUND_SLAM),
                    "Ground Slam must start when nothing else is telegraphing");
            for (int tick = 0; tick < 39; tick++) {
                golem.tickTelegraph();
            }
            helper.assertTrue(player.getHealth() == maxHealth,
                    "No damage before the 40-tick windup ends: expected " + maxHealth
                            + ", got " + player.getHealth());
            helper.assertTrue(golem.isTelegraphing(), "Telegraph must still be active at tick 39");

            golem.tickTelegraph();
            golem.tickTelegraph();
            helper.assertTrue(player.getHealth() < maxHealth,
                    "Impact at tick 40 must damage a player inside the ring");
            helper.assertFalse(golem.isTelegraphing(), "Telegraph must be over after impact");

            golem.discard();
            CustomTntGameTests.removeTestPlayer(level, player);
            helper.succeed();
        });
    }

    @GameTest(template = "structure_arena", timeoutTicks = 120)
    public static void slamMissesPlayerWhoStepsOut(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TrainingGolemEntity golem = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 9, 1, 9);
        golem.setNoAi(true);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(
                helper, "slam-dodge", new Vec3(11.5, 1.0, 9.5)); // 2 blocks from the golem
        float maxHealth = player.getMaxHealth();

        // Spawn protection must be over so a red-proof that damages early would go red here.
        helper.runAfterDelay(61, () -> {
            golem.startTelegraph(TrainingGolemEntity.GROUND_SLAM);
            for (int tick = 0; tick < 30; tick++) {
                golem.tickTelegraph();
            }
            player.moveTo(helper.absoluteVec(new Vec3(17.5, 1.0, 9.5))); // 8 blocks from the golem
            for (int tick = 0; tick < 15; tick++) {
                golem.tickTelegraph();
            }
            helper.assertTrue(player.getHealth() == maxHealth,
                    "A player outside the ring at impact must take no damage: got " + player.getHealth());

            golem.discard();
            CustomTntGameTests.removeTestPlayer(level, player);
            helper.succeed();
        });
    }

    @GameTest(template = "structure_arena")
    public static void slamNeverBreaksBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(true, level.getServer());

        BlockPos stonePos = new BlockPos(11, 1, 9);
        BlockPos glassPos = new BlockPos(9, 1, 11);
        BlockPos chestPos = new BlockPos(11, 1, 11);
        helper.setBlock(stonePos, Blocks.STONE);
        helper.setBlock(glassPos, Blocks.GLASS);
        helper.setBlock(chestPos, Blocks.CHEST);

        TrainingGolemEntity golem = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 9, 1, 9);
        golem.setNoAi(true);
        golem.startTelegraph(TrainingGolemEntity.GROUND_SLAM);
        for (int tick = 0; tick < 45; tick++) {
            golem.tickTelegraph();
        }

        helper.assertBlockPresent(Blocks.STONE, stonePos);
        helper.assertBlockPresent(Blocks.GLASS, glassPos);
        helper.assertBlockPresent(Blocks.CHEST, chestPos);

        golem.discard();
        helper.succeed();
    }

    @GameTest(template = "structure_arena", timeoutTicks = 100)
    public static void bossHoldsStillDuringWindup(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TrainingGolemEntity golem = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 9, 1, 9);
        // AI on: the hold-still goal must beat melee and strolling during the windup.
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(
                helper, "windup-watcher", new Vec3(15.5, 1.0, 9.5)); // 6 blocks: outside slam, inside follow range

        helper.assertTrue(golem.startTelegraph(TrainingGolemEntity.GROUND_SLAM),
                "Ground Slam must start with AI enabled");
        Vec3 start = golem.position();
        helper.runAfterDelay(20, () -> {
            double moved = Math.hypot(golem.getX() - start.x, golem.getZ() - start.z);
            helper.assertTrue(moved < 0.05, "Boss drifted " + moved + " blocks during the windup");
            helper.assertTrue(golem.isTelegraphing(), "Boss must still be telegraphing after 20 ticks");
            golem.discard();
            CustomTntGameTests.removeTestPlayer(level, player);
            helper.succeed();
        });
    }

    @GameTest(template = "structure_arena")
    public static void telegraphDoesNotHurtOtherBosses(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TrainingGolemEntity striker = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 9, 1, 9);
        TrainingGolemEntity other = helper.spawn(ArtilianEntities.TRAINING_GOLEM.get(), 11, 1, 9);
        striker.setNoAi(true);
        other.setNoAi(true);
        float otherMaxHealth = other.getMaxHealth();

        striker.startTelegraph(TrainingGolemEntity.GROUND_SLAM);
        for (int tick = 0; tick < 41; tick++) {
            striker.tickTelegraph();
        }
        helper.assertTrue(other.getHealth() == otherMaxHealth,
                "A boss must never damage another boss: got " + other.getHealth() + " health");

        striker.discard();
        other.discard();
        helper.succeed();
    }
}
