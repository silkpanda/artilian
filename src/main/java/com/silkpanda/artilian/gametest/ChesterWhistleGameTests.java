package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterData;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.chester.ChesterService;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/** Chester Plan Stage 3: the whistle brings Chester back, and there is only ever one of him. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class ChesterWhistleGameTests {

    @GameTest(template = "empty")
    public static void whistleWithNoChesterSpawnsOne(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alice");

        blowWhistle(helper, owner);

        helper.assertTrue(ChesterTestSupport.chestersOf(helper, owner).size() == 1,
                "The whistle should bring 1 Chester, found " + ChesterTestSupport.chestersOf(helper, owner).size());
        helper.assertTrue(owner.getCooldowns().isOnCooldown(ArtilianItems.CHESTER_WHISTLE.get()),
                "The whistle should need a short rest after use");

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void whistleLeavesExactlyOne(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alice");
        ChesterEntity first = ChesterService.summon(owner);
        first.moveTo(first.getX() + 1.0, first.getY(), first.getZ());

        blowWhistle(helper, owner);

        List<? extends ChesterEntity> chesters = ChesterTestSupport.chestersOf(helper, owner);
        helper.assertTrue(chesters.size() == 1, "After the whistle there should be 1 Chester, found " + chesters.size());
        helper.assertFalse(first.isAlive(), "The old Chester is still here after the whistle");
        helper.assertTrue(ChesterData.get(owner).getChesterEntityId().filter(chesters.get(0).getUUID()::equals).isPresent(),
                "The player record does not point at the new Chester");

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void orphanedChesterRemovesItself(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Bob");
        ChesterEntity real = ChesterService.summon(owner);
        // A second Chester that claims the same owner but is not the one on record. This is
        // what an old Chester looks like when its chunk loads after the owner whistled.
        ChesterEntity leftover = ArtilianEntities.CHESTER.get().create(helper.getLevel());
        leftover.setOwner(owner);
        leftover.moveTo(real.getX(), real.getY(), real.getZ());
        helper.getLevel().addFreshEntity(leftover);

        boolean leftoverRemoved = leftover.discardIfOrphan(ChesterData.get(owner));
        boolean realRemoved = real.discardIfOrphan(ChesterData.get(owner));

        helper.assertTrue(leftoverRemoved && !leftover.isAlive(), "A leftover Chester did not remove itself");
        helper.assertTrue(!realRemoved && real.isAlive(), "The real Chester removed itself");
        helper.assertTrue(ChesterTestSupport.chestersOf(helper, owner).size() == 1,
                "The player should have exactly 1 Chester, found " + ChesterTestSupport.chestersOf(helper, owner).size());

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void whistleDoesNotTouchTheItems(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Alice");
        ChesterService.summon(owner);
        ChesterData.get(owner).getInventory().setStackInSlot(0, new ItemStack(Items.EMERALD, 12));
        ChesterData.get(owner).getInventory().setStackInSlot(26, new ItemStack(Items.CAKE));

        ChesterService.summon(owner);
        ChesterService.summon(owner);

        ItemStack emeralds = ChesterData.get(owner).getInventory().getStackInSlot(0);
        ItemStack cake = ChesterData.get(owner).getInventory().getStackInSlot(26);
        helper.assertTrue(emeralds.is(Items.EMERALD) && emeralds.getCount() == 12, "The whistle changed slot 0 to " + emeralds);
        helper.assertTrue(cake.is(Items.CAKE) && cake.getCount() == 1, "The whistle changed slot 26 to " + cake);

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void firstLoginGivesOneWhistle(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Bob");

        ChesterService.handleLogin(owner);
        ChesterService.handleLogin(owner);

        helper.assertTrue(countWhistles(owner) == 1, "Logging in twice should leave 1 whistle, found " + countWhistles(owner));

        ChesterTestSupport.removeChestersOf(helper, owner);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void whistleComesBackAfterDeath(GameTestHelper helper) {
        ServerPlayer owner = ChesterTestSupport.playerInTestArea(helper, "Bob");
        List<ItemEntity> deathDrops = new ArrayList<>();
        deathDrops.add(drop(helper, new ItemStack(ArtilianItems.CHESTER_WHISTLE.get())));
        deathDrops.add(drop(helper, new ItemStack(Items.DIRT, 3)));

        ChesterService.keepWhistleOutOfDrops(deathDrops);
        boolean given = ChesterService.giveWhistleIfMissing(owner);
        boolean givenAgain = ChesterService.giveWhistleIfMissing(owner);

        helper.assertTrue(deathDrops.size() == 1 && deathDrops.get(0).getItem().is(Items.DIRT),
                "Only the whistle should be kept out of the death drops");
        helper.assertTrue(given && !givenAgain && countWhistles(owner) == 1,
                "A respawned player should end up with exactly 1 whistle, found " + countWhistles(owner));
        helper.succeed();
    }

    private static void blowWhistle(GameTestHelper helper, ServerPlayer player) {
        ItemStack whistle = new ItemStack(ArtilianItems.CHESTER_WHISTLE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, whistle);
        whistle.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    }

    private static int countWhistles(ServerPlayer player) {
        return player.getInventory().countItem(ArtilianItems.CHESTER_WHISTLE.get());
    }

    private static ItemEntity drop(GameTestHelper helper, ItemStack stack) {
        return new ItemEntity(helper.getLevel(), 0.0, 0.0, 0.0, stack);
    }
}
