package com.silkpanda.artilian.gametest;

import com.mojang.authlib.GameProfile;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.UUID;

/**
 * Chester Plan Stage 1: the 27 slots live on the player and survive saving and dying.
 * survivesDeath is also security probe P1-03 (see security/FINDINGS.md).
 */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class ChesterStorageGameTests {
    private static final String STICK_NAME = "Alice's Stick";

    @GameTest(template = "empty")
    public static void has27Slots(GameTestHelper helper) {
        ChesterData data = ChesterData.get(newPlayer(helper, "chester-fresh"));

        helper.assertTrue(data.getInventory().getSlots() == 27,
                "Chester should have 27 slots but has " + data.getInventory().getSlots());
        for (int slot = 0; slot < data.getInventory().getSlots(); slot++) {
            helper.assertTrue(data.getInventory().getStackInSlot(slot).isEmpty(),
                    "A new Chester should be empty but slot " + slot + " is not");
        }
        helper.assertFalse(data.isFirstSpawnDone(), "A new player should not be marked as already given a Chester");
        helper.assertTrue(data.getChesterEntityId().isEmpty(), "A new player should have no Chester entity recorded");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void survivesSaveAndLoad(GameTestHelper helper) {
        UUID chesterId = UUID.randomUUID();
        ChesterData saved = new ChesterData();
        fill(saved, chesterId);

        CompoundTag tag = saved.serializeNBT(helper.getLevel().registryAccess());
        ChesterData loaded = new ChesterData();
        loaded.deserializeNBT(helper.getLevel().registryAccess(), tag);

        assertFilled(helper, loaded, chesterId, "after save and load");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void survivesDeath(GameTestHelper helper) {
        UUID chesterId = UUID.randomUUID();
        ServerPlayer beforeDeath = newPlayer(helper, "chester-owner");
        fill(ChesterData.get(beforeDeath), chesterId);

        // The same call the game makes on respawn. keepEverything = false means "this was a
        // death", which fires PlayerEvent.Clone and copies only copyOnDeath attachments.
        ServerPlayer respawned = new FakePlayer(helper.getLevel(), beforeDeath.getGameProfile());
        respawned.restoreFrom(beforeDeath, false);

        assertFilled(helper, ChesterData.get(respawned), chesterId, "after the owner died");
        helper.succeed();
    }

    private static ServerPlayer newPlayer(GameTestHelper helper, String name) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
    }

    private static void fill(ChesterData data, UUID chesterId) {
        ItemStack stick = new ItemStack(Items.STICK);
        stick.set(DataComponents.CUSTOM_NAME, Component.literal(STICK_NAME));
        data.getInventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 5));
        data.getInventory().setStackInSlot(ChesterData.SLOTS - 1, stick);
        data.setFirstSpawnDone(true);
        data.setChesterEntityId(chesterId);
    }

    private static void assertFilled(GameTestHelper helper, ChesterData data, UUID chesterId, String when) {
        ItemStackHandler inventory = data.getInventory();
        helper.assertTrue(inventory.getSlots() == ChesterData.SLOTS,
                "Chester has " + inventory.getSlots() + " slots " + when);

        ItemStack diamonds = inventory.getStackInSlot(0);
        helper.assertTrue(diamonds.is(Items.DIAMOND) && diamonds.getCount() == 5,
                "Lost the 5 diamonds in slot 0 " + when + ", found " + diamonds);

        ItemStack stick = inventory.getStackInSlot(ChesterData.SLOTS - 1);
        helper.assertTrue(stick.is(Items.STICK) && stick.getCount() == 1,
                "Lost the stick in the last slot " + when + ", found " + stick);
        Component name = stick.get(DataComponents.CUSTOM_NAME);
        helper.assertTrue(name != null && STICK_NAME.equals(name.getString()),
                "Lost the stick's name " + when);

        helper.assertTrue(data.isFirstSpawnDone(), "Forgot the player already has a Chester " + when);
        helper.assertTrue(data.getChesterEntityId().filter(chesterId::equals).isPresent(),
                "Lost track of which Chester belongs to the player " + when);
    }
}
