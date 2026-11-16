package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.chester.ChesterService;
import com.silkpanda.artilian.registry.ArtilianEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Chester Plan Stage 5: Sit & Sleep, sleeping when the owner sleeps, and staying put. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class ChesterSleepGameTests {

    @GameTest(template = "empty")
    public static void ownerShiftClickTogglesSleep(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ChesterEntity chester = ChesterService.summon(owner);
        owner.setShiftKeyDown(true);

        chester.interact(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(chester.isAsleep(), "Shift + right-click by the owner should put Chester to sleep");

        chester.interact(owner, InteractionHand.MAIN_HAND);
        helper.assertFalse(chester.isAsleep(), "A second shift + right-click should wake Chester");

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bedSleepAndWake(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Steve");
        ChesterEntity chester = ChesterService.summon(owner);

        chester.applyOwnerBedState(true);
        helper.assertTrue(chester.isAsleep(), "Chester should sleep when his owner gets into bed");

        chester.applyOwnerBedState(false);
        helper.assertFalse(chester.isAsleep(), "Chester should wake when his owner gets up");

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sitAndSleepSurvivesMorning(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ChesterEntity chester = ChesterService.summon(owner);
        chester.toggleSitAndSleep(owner);

        chester.applyOwnerBedState(true);
        chester.applyOwnerBedState(false);

        helper.assertTrue(chester.isAsleep(), "A Chester told to Sit & Sleep woke up when his owner got out of bed");

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sitAndSleepSurvivesSaving(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ChesterEntity chester = ChesterService.summon(owner);
        chester.toggleSitAndSleep(owner);

        CompoundTag saved = chester.saveWithoutId(new CompoundTag());
        ChesterEntity loaded = ArtilianEntities.CHESTER.get().create(helper.getLevel());
        loaded.load(saved);

        helper.assertTrue(loaded.isAsleep() && loaded.isToldToSleep(), "Chester forgot he was told to Sit & Sleep");
        helper.assertTrue(loaded.isOwner(owner) && "Alex".equals(loaded.getOwnerName()), "Chester forgot his owner");

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void sleepingChesterStaysPut(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Steve");
        ChesterEntity chester = ChesterService.summon(owner);
        chester.toggleSitAndSleep(owner);

        helper.assertFalse(chester.isPushable(), "A sleeping Chester should not be pushable");
        helper.assertFalse(chester.canFollow(), "A sleeping Chester should not follow his owner");

        // Let him land, then shove him hard every tick for a second.
        helper.runAfterDelay(20, () -> {
            double x = chester.getX();
            double z = chester.getZ();
            for (int tick = 1; tick <= 20; tick++) {
                helper.runAfterDelay(tick, () -> chester.setDeltaMovement(0.6, 0.0, 0.6));
            }
            helper.runAfterDelay(25, () -> {
                double moved = Math.abs(chester.getX() - x) + Math.abs(chester.getZ() - z);
                helper.assertTrue(moved < 0.01, "A sleeping Chester was pushed " + moved + " blocks");
                ChesterTestSupport.removeChestersOf(helper, owner);
                helper.succeed();
            });
        });
    }
}
