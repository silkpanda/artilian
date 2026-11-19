package com.silkpanda.artilian.gametest.security;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.BossAltarBlockEntity;
import com.silkpanda.artilian.boss.CaptainBrimstoneEntity;
import com.silkpanda.artilian.boss.CrystalColossusEntity;
import com.silkpanda.artilian.boss.TrainingGolemEntity;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.chester.ChesterService;
import com.silkpanda.artilian.config.ArtilianConfig;
import com.silkpanda.artilian.gametest.BossFrameworkGameTests;
import com.silkpanda.artilian.gametest.CustomTntGameTests;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Security probes for the Boss Framework & Crystal Colossus ({@code P3-01}, {@code P3-02}), indexed
 * in {@code security/bosses/README.md} and logged in {@code security/FINDINGS.md}.
 */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class BossSecurityProbes {

    private static final int SPAM_CLICKS = 20;

    /** P3-01: one player hammers the altar 20 times in 20 ticks with 20 Golem Cores. One boss, 19 cores left. */
    @GameTest(template = "structure_arena", timeoutTicks = 140)
    public static void p3_01_altarSpamSummonsOneBoss(GameTestHelper helper) {
        BlockPos rel = new BlockPos(9, 1, 9);
        helper.setBlock(rel, ArtilianBlocks.BOSS_ALTAR.get());
        BlockPos altar = helper.absolutePos(rel);
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, "altar-spammer", new Vec3(17.5, 1.0, 9.5));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ArtilianItems.GOLEM_CORE.get(), SPAM_CLICKS));

        for (int tick = 0; tick < SPAM_CLICKS; tick++) {
            helper.runAfterDelay(tick, () -> BossFrameworkGameTests.useAltar(helper, player, altar));
        }
        // Long enough for every click's countdown to have finished if any of them restarted it.
        helper.runAfterDelay(SPAM_CLICKS + BossAltarBlockEntity.COUNTDOWN_TICKS + 2, () -> {
            List<TrainingGolemEntity> bosses = BossFrameworkGameTests.golemsNear(helper, altar);
            int cores = player.getMainHandItem().getCount();
            helper.getLevel().removeBlock(altar, false);
            bosses.forEach(TrainingGolemEntity::discard);
            CustomTntGameTests.removeTestPlayer(helper.getLevel(), player);
            helper.assertTrue(bosses.size() == 1,
                    "EXPLOITED P3-01: altar spam summoned " + bosses.size() + " bosses");
            helper.assertTrue(cores == SPAM_CLICKS - 1,
                    "EXPLOITED P3-01: altar spam ate " + (SPAM_CLICKS - cores) + " Golem Cores for one boss");
            helper.succeed();
        });
    }

    /**
     * P3-02: Under mobGriefing=true and destructive TNT config=true, none of the Crystal Colossus's
     * three attacks break world blocks, Boss Altars, or Chests, or deal damage to Chester.
     */
    @GameTest(template = "structure_arena")
    public static void p3_02_crystalColossusNeverBreaksBlocksOrAltarOrHarmsChester(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var mobGriefingRule = level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING);
        boolean previousMobGriefing = mobGriefingRule.get();

        BlockPos altarPos = new BlockPos(11, 1, 9);
        BlockPos chestPos = new BlockPos(9, 1, 11);
        BlockPos glassPos = new BlockPos(11, 1, 11);
        BlockPos stonePos = new BlockPos(7, 1, 9);
        helper.setBlock(altarPos, ArtilianBlocks.BOSS_ALTAR.get());
        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(glassPos, Blocks.GLASS);
        helper.setBlock(stonePos, Blocks.STONE);

        CrystalColossusEntity boss = helper.spawn(ArtilianEntities.CRYSTAL_COLOSSUS.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer owner = CustomTntGameTests.spawnSurvivalPlayer(helper, "p3-02-owner", new Vec3(2.5, 1.0, 2.5));
        ChesterEntity chester = ChesterService.summon(owner);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(10.5, 1.0, 9.5));
        chester.moveTo(chesterPos.x, chesterPos.y, chesterPos.z, 0.0F, 0.0F);
        float chesterStartHp = chester.getHealth();

        try {
            mobGriefingRule.set(true, level.getServer());
            ArtilianConfig.setTestOverride(Boolean.TRUE);

            // 1. Crystal Quake (boss-centered, radius 4.5)
            boss.startTelegraph(CrystalColossusEntity.CRYSTAL_QUAKE);
            for (int t = 0; t < CrystalColossusEntity.CRYSTAL_QUAKE.windupTicks(); t++) {
                boss.tickTelegraph();
            }

            // 2. Starlight Spire (targeted right on top of Chester / Boss Altar / Chest)
            boss.startTelegraph(CrystalColossusEntity.STARLIGHT_SPIRE, chesterPos);
            for (int t = 0; t < CrystalColossusEntity.STARLIGHT_SPIRE.windupTicks(); t++) {
                boss.tickTelegraph();
            }

            // 3. Overcharged Prism Nova (radius 7.0 covering all four blocks and Chester)
            boss.setHealth(100.0F);
            boss.checkOvercharge();
            boss.startTelegraph(CrystalColossusEntity.PRISM_NOVA);
            for (int t = 0; t < CrystalColossusEntity.PRISM_NOVA.windupTicks(); t++) {
                boss.tickTelegraph();
            }

            helper.assertTrue(helper.getBlockState(altarPos).is(ArtilianBlocks.BOSS_ALTAR.get()),
                    "EXPLOITED P3-02: Crystal Colossus destroyed or altered Boss Altar");
            helper.assertTrue(helper.getBlockState(chestPos).is(Blocks.CHEST),
                    "EXPLOITED P3-02: Crystal Colossus destroyed or altered Chest");
            helper.assertTrue(helper.getBlockState(glassPos).is(Blocks.GLASS),
                    "EXPLOITED P3-02: Crystal Colossus destroyed or altered Glass");
            helper.assertTrue(helper.getBlockState(stonePos).is(Blocks.STONE),
                    "EXPLOITED P3-02: Crystal Colossus destroyed or altered Stone");
            helper.assertTrue(chester.isAlive() && chester.getHealth() == chesterStartHp,
                    "EXPLOITED P3-02: Crystal Colossus harmed Chester (health "
                            + chester.getHealth() + " vs " + chesterStartHp + ")");
        } finally {
            mobGriefingRule.set(previousMobGriefing, level.getServer());
            ArtilianConfig.setTestOverride(null);
            boss.discard();
            chester.discard();
            CustomTntGameTests.removeTestPlayer(level, owner);
        }
        helper.succeed();
    }

    /**
     * P3-03: Even with mobGriefing=true and ArtilianConfig destructive mode enabled, Captain Brimstone's
     * Powder Keg Toss, Cutlass Cleave, and Broadside Barrage must never break blocks/altars/chests,
     * place fire blocks on terrain, or harm Chester.
     */
    @GameTest(template = "structure_arena")
    public static void p3_03_captainBrimstoneNeverBreaksBlocksOrAltarOrIgnitesTerrainOrHarmsChester(
            GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GameRules.BooleanValue mobGriefingRule = level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING);
        boolean previousMobGriefing = mobGriefingRule.get();

        BlockPos altarPos = new BlockPos(11, 1, 9);
        BlockPos chestPos = new BlockPos(9, 1, 11);
        BlockPos planksPos = new BlockPos(11, 1, 11);
        BlockPos stonePos = new BlockPos(7, 1, 9);
        helper.setBlock(altarPos, ArtilianBlocks.BOSS_ALTAR.get());
        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(planksPos, ArtilianBlocks.EMBERWOOD_PLANKS.get());
        helper.setBlock(stonePos, Blocks.STONE);

        CaptainBrimstoneEntity boss = helper.spawn(ArtilianEntities.CAPTAIN_BRIMSTONE.get(), 9, 1, 9);
        boss.setNoAi(true);
        ServerPlayer owner = CustomTntGameTests.spawnSurvivalPlayer(helper, "p3-03-owner", new Vec3(2.5, 1.0, 2.5));
        ChesterEntity chester = ChesterService.summon(owner);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(10.5, 1.0, 9.5));
        chester.moveTo(chesterPos.x, chesterPos.y, chesterPos.z, 0.0F, 0.0F);
        float chesterStartHp = chester.getHealth();

        try {
            mobGriefingRule.set(true, level.getServer());
            ArtilianConfig.setTestOverride(Boolean.TRUE);

            // 1. Powder Keg Toss (targeted right on top of Chester / Boss Altar / Chest)
            boss.startTelegraph(CaptainBrimstoneEntity.POWDER_KEG_TOSS, chesterPos);
            for (int t = 0; t < CaptainBrimstoneEntity.POWDER_KEG_TOSS.windupTicks(); t++) {
                boss.tickTelegraph();
            }

            // 2. Cutlass Cleave (boss-centered, radius 4.0)
            boss.startTelegraph(CaptainBrimstoneEntity.CUTLASS_CLEAVE);
            for (int t = 0; t < CaptainBrimstoneEntity.CUTLASS_CLEAVE.windupTicks(); t++) {
                boss.tickTelegraph();
            }

            // 3. Broadside Barrage (radius 6.5 covering all four blocks and Chester)
            boss.setHealth(110.0F);
            boss.checkBroadsidePhase();
            boss.startTelegraph(CaptainBrimstoneEntity.BROADSIDE_BARRAGE);
            for (int t = 0; t < CaptainBrimstoneEntity.BROADSIDE_BARRAGE.windupTicks(); t++) {
                boss.tickTelegraph();
            }

            helper.assertTrue(helper.getBlockState(altarPos).is(ArtilianBlocks.BOSS_ALTAR.get()),
                    "EXPLOITED P3-03: Captain Brimstone destroyed or altered Boss Altar");
            helper.assertTrue(helper.getBlockState(chestPos).is(Blocks.CHEST),
                    "EXPLOITED P3-03: Captain Brimstone destroyed or altered Chest");
            helper.assertTrue(helper.getBlockState(planksPos).is(ArtilianBlocks.EMBERWOOD_PLANKS.get()),
                    "EXPLOITED P3-03: Captain Brimstone burned or altered Emberwood Planks");
            helper.assertTrue(helper.getBlockState(stonePos).is(Blocks.STONE),
                    "EXPLOITED P3-03: Captain Brimstone destroyed or altered Stone");
            helper.assertFalse(helper.getBlockState(planksPos.above()).is(Blocks.FIRE),
                    "EXPLOITED P3-03: Captain Brimstone ignited fire blocks on arena terrain");
            helper.assertTrue(chester.isAlive() && chester.getHealth() == chesterStartHp,
                    "EXPLOITED P3-03: Captain Brimstone harmed Chester (health "
                            + chester.getHealth() + " vs " + chesterStartHp + ")");
        } finally {
            mobGriefingRule.set(previousMobGriefing, level.getServer());
            ArtilianConfig.setTestOverride(null);
            boss.discard();
            chester.discard();
            CustomTntGameTests.removeTestPlayer(level, owner);
        }
        helper.succeed();
    }
}
