package com.silkpanda.artilian.chester;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * What a chest screen looks at when a player opens a Chester. It is always the storage on the
 * player who opened it: there is no way to build one that points at somebody else's items.
 */
public class ChesterContainer implements Container {
    private static final double MAX_REACH_SQR = 8.0 * 8.0;

    private final ChesterEntity chester;
    private final ItemStackHandler items;

    public ChesterContainer(ChesterEntity chester, Player opener) {
        this.chester = chester;
        this.items = ChesterData.get(opener).getInventory();
    }

    /**
     * Checked by the game every tick while the screen is open. The screen closes as soon as this
     * Chester is gone, the player walks off, or the player is not his owner.
     */
    @Override
    public boolean stillValid(Player player) {
        return this.chester.isAlive()
                && this.chester.isOwner(player)
                && player.distanceToSqr(this.chester) <= MAX_REACH_SQR;
    }

    @Override
    public int getContainerSize() {
        return this.items.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < this.items.getSlots(); slot++) {
            if (!this.items.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return this.items.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.items.getStackInSlot(slot);
        this.items.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.setStackInSlot(slot, stack);
    }

    @Override
    public void setChanged() {
        // The storage is saved with the player, so there is nothing to mark.
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < this.items.getSlots(); slot++) {
            this.items.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }
}
