package com.silkpanda.artilian.gametest.security;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterContainer;
import com.silkpanda.artilian.chester.ChesterData;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.chester.ChesterService;
import com.silkpanda.artilian.gametest.ChesterTestSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Security probes for Chester, indexed in security/chester/README.md. Each one tries the attack
 * and fails the test with "EXPLOITED" if it works. A probe only counts once it has been seen
 * failing against a build with its control switched off. P1-03 lives in ChesterStorageGameTests.
 */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class ChesterSecurityProbes {

    /** P1-01: a player who is not the owner right-clicks a Chester and gets a screen. */
    @GameTest(template = "empty")
    public static void p1_01_nonOwnerGetsNoMenu(GameTestHelper helper) {
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "Steve");
        ChesterEntity alexsChester = ChesterService.summon(alex);
        ChesterData.get(alex).getInventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 5));

        alexsChester.interact(steve, InteractionHand.MAIN_HAND);
        AbstractContainerMenu forSteve = alexsChester.createMenu(1, steve.getInventory(), steve);
        AbstractContainerMenu forAlex = alexsChester.createMenu(2, alex.getInventory(), alex);

        helper.assertTrue(forSteve == null, "EXPLOITED P1-01: a non-owner was given a Chester screen");
        helper.assertTrue(steve.containerMenu == steve.inventoryMenu, "EXPLOITED P1-01: a non-owner has a Chester screen open");
        helper.assertTrue(forAlex != null && forAlex.getSlot(0).getItem().is(Items.DIAMOND),
                "The owner could not open their own Chester");

        ChesterTestSupport.removeChestersOf(helper, alex);
        helper.succeed();
    }

    /** P1-02: a screen that should no longer be open (wrong player, Chester gone, too far) keeps working. */
    @GameTest(template = "empty")
    public static void p1_02_staleMenuStopsWorking(GameTestHelper helper) {
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "Steve");
        ChesterEntity alexsChester = ChesterService.summon(alex);

        // A container built by hand for the wrong player, skipping createMenu altogether.
        Container forged = new ChesterContainer(alexsChester, steve);
        helper.assertFalse(forged.stillValid(steve), "EXPLOITED P1-02: a forged screen stayed open for a non-owner");

        Container real = new ChesterContainer(alexsChester, alex);
        helper.assertTrue(real.stillValid(alex), "The owner's screen should be valid beside their Chester");

        alex.moveTo(alex.getX() + 20.0, alex.getY(), alex.getZ());
        helper.assertFalse(real.stillValid(alex), "EXPLOITED P1-02: the screen stayed open from 20 blocks away");
        alex.moveTo(alex.getX() - 20.0, alex.getY(), alex.getZ());

        alexsChester.discard();
        helper.assertFalse(real.stillValid(alex), "EXPLOITED P1-02: the screen stayed open after Chester was removed");

        helper.succeed();
    }

    /** P1-04: two screens, two Chesters, or a whistle mid-screen turn one item into two. */
    @GameTest(template = "empty")
    public static void p1_04_twoScreensCannotDuplicate(GameTestHelper helper) {
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ChesterEntity firstChester = ChesterService.summon(alex);
        Container firstScreen = new ChesterContainer(firstChester, alex);
        firstScreen.setItem(0, new ItemStack(Items.GOLD_INGOT, 10));

        // Whistle while the first screen is still open.
        ChesterEntity secondChester = ChesterService.summon(alex);
        Container secondScreen = new ChesterContainer(secondChester, alex);

        helper.assertFalse(firstScreen.stillValid(alex), "EXPLOITED P1-04: the old screen stayed open after the whistle");

        ItemStack taken = secondScreen.removeItem(0, 4);
        int inFirstScreen = firstScreen.getItem(0).getCount();
        int inSecondScreen = secondScreen.getItem(0).getCount();
        int onThePlayer = ChesterData.get(alex).getInventory().getStackInSlot(0).getCount();

        helper.assertTrue(taken.getCount() == 4, "Taking 4 gold gave " + taken.getCount());
        helper.assertTrue(inFirstScreen == 6 && inSecondScreen == 6 && onThePlayer == 6,
                "EXPLOITED P1-04: 10 gold, 4 taken, but the screens show " + inFirstScreen + " and " + inSecondScreen
                        + " and the player holds " + onThePlayer);

        ChesterTestSupport.removeChestersOf(helper, alex);
        helper.succeed();
    }

    /** P1-05: a player who is not the owner puts a Chester to sleep, wakes him, or opens him while he sleeps. */
    @GameTest(template = "empty")
    public static void p1_05_nonOwnerCannotUseSleep(GameTestHelper helper) {
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "Steve");
        ChesterEntity alexsChester = ChesterService.summon(alex);
        steve.setShiftKeyDown(true);

        alexsChester.interact(steve, InteractionHand.MAIN_HAND);
        boolean toggled = alexsChester.toggleSitAndSleep(steve);
        helper.assertTrue(!toggled && !alexsChester.isAsleep(), "EXPLOITED P1-05: a non-owner put Chester to sleep");

        alexsChester.toggleSitAndSleep(alex);
        helper.assertTrue(alexsChester.isAsleep(), "The owner could not put their own Chester to sleep");

        alexsChester.interact(steve, InteractionHand.MAIN_HAND);
        boolean woke = alexsChester.toggleSitAndSleep(steve);
        helper.assertTrue(!woke && alexsChester.isAsleep(), "EXPLOITED P1-05: a non-owner woke a sleeping Chester");
        helper.assertTrue(alexsChester.createMenu(1, steve.getInventory(), steve) == null,
                "EXPLOITED P1-05: a non-owner opened a sleeping Chester");
        helper.assertTrue(alexsChester.createMenu(2, alex.getInventory(), alex) != null,
                "The owner could not open their own sleeping Chester");

        ChesterTestSupport.removeChestersOf(helper, alex);
        helper.succeed();
    }

    /** P1-06: a hopper or any other machine pulls items out through the Chester entity. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void p1_06_hoppersGetNothing(GameTestHelper helper) {
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "Alex");
        ChesterEntity chester = ChesterService.summon(alex);
        ChesterData.get(alex).getInventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 5));

        helper.assertFalse(chester instanceof Container, "EXPLOITED P1-06: Chester is a container that hoppers can read");
        // NeoForge gives every living entity a handler for its own hands and armour. That one is
        // allowed to exist. It must be empty: it must never be a window onto the stored items.
        IItemHandler equipment = chester.getCapability(Capabilities.ItemHandler.ENTITY);
        if (equipment != null) {
            helper.assertTrue(equipment.getSlots() < ChesterData.SLOTS,
                    "EXPLOITED P1-06: Chester exposes a " + equipment.getSlots() + "-slot item handler");
            for (int slot = 0; slot < equipment.getSlots(); slot++) {
                helper.assertTrue(equipment.getStackInSlot(slot).isEmpty(),
                        "EXPLOITED P1-06: Chester's item handler shows " + equipment.getStackInSlot(slot));
            }
        }
        helper.assertTrue(chester.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, null) == null,
                "EXPLOITED P1-06: Chester exposes an item handler to machines");
        for (Direction side : Direction.values()) {
            helper.assertTrue(chester.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, side) == null,
                    "EXPLOITED P1-06: Chester exposes an item handler to machines on side " + side);
        }

        // And a real hopper, with Chester sitting on top of it for 3 seconds.
        BlockPos hopperPos = new BlockPos(1, 1, 1);
        helper.setBlock(hopperPos, Blocks.HOPPER);
        Vec3 onTop = helper.absoluteVec(new Vec3(1.5, 2.0, 1.5));
        chester.moveTo(onTop.x, onTop.y, onTop.z);

        helper.runAfterDelay(60, () -> {
            HopperBlockEntity hopper = helper.getBlockEntity(hopperPos);
            helper.assertTrue(hopper.isEmpty(), "EXPLOITED P1-06: a hopper pulled items out of Chester");
            ItemStack kept = ChesterData.get(alex).getInventory().getStackInSlot(0);
            helper.assertTrue(kept.is(Items.DIAMOND) && kept.getCount() == 5,
                    "EXPLOITED P1-06: the owner's items changed while Chester sat on a hopper, found " + kept);
            ChesterTestSupport.removeChestersOf(helper, alex);
            helper.succeed();
        });
    }
}
