package com.silkpanda.artilian.chester;

import com.silkpanda.artilian.registry.ArtilianAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Everything a player's Chester owns, stored on the player. The Chester entity is only a door to
 * this: it never holds an item, so losing, killing, or duplicating the entity cannot lose or
 * duplicate what is inside.
 */
public class ChesterData implements INBTSerializable<CompoundTag> {
    public static final int SLOTS = 27;

    private static final String KEY_INVENTORY = "Inventory";
    private static final String KEY_FIRST_SPAWN_DONE = "FirstSpawnDone";
    private static final String KEY_CHESTER_ENTITY = "ChesterEntity";

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS);
    private boolean firstSpawnDone;
    @Nullable
    private UUID chesterEntityId;

    public static ChesterData get(Player player) {
        return player.getData(ArtilianAttachments.CHESTER);
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    /** True once this player has been given their first Chester. */
    public boolean isFirstSpawnDone() {
        return this.firstSpawnDone;
    }

    public void setFirstSpawnDone(boolean firstSpawnDone) {
        this.firstSpawnDone = firstSpawnDone;
    }

    /** The one Chester entity that currently belongs to this player, if any. */
    public Optional<UUID> getChesterEntityId() {
        return Optional.ofNullable(this.chesterEntityId);
    }

    public void setChesterEntityId(@Nullable UUID chesterEntityId) {
        this.chesterEntityId = chesterEntityId;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put(KEY_INVENTORY, this.inventory.serializeNBT(provider));
        tag.putBoolean(KEY_FIRST_SPAWN_DONE, this.firstSpawnDone);
        if (this.chesterEntityId != null) {
            tag.putUUID(KEY_CHESTER_ENTITY, this.chesterEntityId);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains(KEY_INVENTORY)) {
            this.inventory.deserializeNBT(provider, tag.getCompound(KEY_INVENTORY));
            // A saved "Size" resizes the handler. Chester is always 27 slots.
            if (this.inventory.getSlots() != SLOTS) {
                this.inventory.setSize(SLOTS);
            }
        }
        this.firstSpawnDone = tag.getBoolean(KEY_FIRST_SPAWN_DONE);
        this.chesterEntityId = tag.hasUUID(KEY_CHESTER_ENTITY) ? tag.getUUID(KEY_CHESTER_ENTITY) : null;
    }
}
