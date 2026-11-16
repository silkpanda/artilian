package com.silkpanda.artilian.mana;

import com.silkpanda.artilian.registry.ArtilianAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Per-player auto-regenerating Mana pool stored via NeoForge data attachments.
 * Replicated to the owning client via {@link ManaSyncPayload}.
 */
public class ManaData implements INBTSerializable<CompoundTag> {
    public static final int DEFAULT_MAX_MANA = 100;
    public static final int REGEN_INTERVAL_TICKS = 10;
    public static final int REGEN_AMOUNT = 5;
    public static final int REGEN_DELAY_AFTER_CAST_TICKS = 20;

    private static final String KEY_MANA = "Mana";
    private static final String KEY_MAX_MANA = "MaxMana";
    private static final String KEY_REGEN_COOLDOWN = "RegenCooldownTicks";

    private int mana = DEFAULT_MAX_MANA;
    private int maxMana = DEFAULT_MAX_MANA;
    private int regenTickCounter;
    private int regenCooldownTicks;

    // Transient tracking for server-side sync verification in GameTests.
    private int syncCount;
    private int lastSyncedMana = -1;

    public static ManaData get(Player player) {
        return player.getData(ArtilianAttachments.MANA);
    }

    public int getMana() {
        return this.mana;
    }

    public int getMaxMana() {
        return this.maxMana;
    }

    public int getRegenCooldownTicks() {
        return this.regenCooldownTicks;
    }

    public int getSyncCount() {
        return this.syncCount;
    }

    public int getLastSyncedMana() {
        return this.lastSyncedMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = Math.max(1, maxMana);
        this.mana = Mth.clamp(this.mana, 0, this.maxMana);
    }

    public void setMana(int mana) {
        this.mana = Mth.clamp(mana, 0, this.maxMana);
    }

    public boolean canSpend(int cost) {
        return cost >= 0 && this.mana >= cost;
    }

    /**
     * Attempts to deduct {@code cost} Mana and start the post-cast regen delay.
     *
     * @return {@code true} if Mana was sufficient and deducted
     */
    public boolean trySpend(int cost) {
        if (!this.canSpend(cost)) {
            return false;
        }
        this.mana -= cost;
        this.regenCooldownTicks = REGEN_DELAY_AFTER_CAST_TICKS;
        this.regenTickCounter = 0;
        return true;
    }

    /** Adds up to {@code amount} Mana, clamped to {@link #maxMana}. */
    public boolean restore(int amount) {
        if (amount <= 0 || this.mana >= this.maxMana) {
            return false;
        }
        int next = Math.min(this.maxMana, this.mana + amount);
        boolean changed = next != this.mana;
        this.mana = next;
        return changed;
    }

    /**
     * Advances server-side auto-regeneration by one tick.
     *
     * @return {@code true} when Mana actually changed and clients should be synced
     */
    public boolean tickServer() {
        if (this.regenCooldownTicks > 0) {
            this.regenCooldownTicks--;
            return false;
        }
        if (this.mana >= this.maxMana) {
            this.regenTickCounter = 0;
            return false;
        }
        this.regenTickCounter++;
        if (this.regenTickCounter >= REGEN_INTERVAL_TICKS) {
            this.regenTickCounter = 0;
            this.mana = Math.min(this.maxMana, this.mana + REGEN_AMOUNT);
            return true;
        }
        return false;
    }

    void recordSync() {
        this.syncCount++;
        this.lastSyncedMana = this.mana;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_MANA, this.mana);
        tag.putInt(KEY_MAX_MANA, this.maxMana);
        tag.putInt(KEY_REGEN_COOLDOWN, this.regenCooldownTicks);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        int loadedMax = tag.contains(KEY_MAX_MANA) ? tag.getInt(KEY_MAX_MANA) : DEFAULT_MAX_MANA;
        this.maxMana = Math.max(1, loadedMax);
        int loadedMana = tag.contains(KEY_MANA) ? tag.getInt(KEY_MANA) : this.maxMana;
        this.mana = Mth.clamp(loadedMana, 0, this.maxMana);
        this.regenCooldownTicks = Math.max(0, tag.getInt(KEY_REGEN_COOLDOWN));
        this.regenTickCounter = 0;
    }
}
