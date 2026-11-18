package com.silkpanda.artilian.gametest.security;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.BossAltarBlockEntity;
import com.silkpanda.artilian.boss.TrainingGolemEntity;
import com.silkpanda.artilian.gametest.BossFrameworkGameTests;
import com.silkpanda.artilian.gametest.CustomTntGameTests;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Security probes for the Boss Framework ({@code P3-01}), indexed in {@code security/bosses/README.md}
 * and logged in {@code security/FINDINGS.md}.
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
}
