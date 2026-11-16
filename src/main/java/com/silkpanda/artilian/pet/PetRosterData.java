package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.registry.ArtilianAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Authoritative per-player pet state keyed by {@code petId}. Stored on the player with
 * {@code copyOnDeath()} so a cloned crystal cannot duplicate an active pet and a fainted pet
 * is never lost even if its Capture Crystal was moved into storage.
 */
public class PetRosterData implements INBTSerializable<CompoundTag> {
    private static final String KEY_ENTRIES = "Entries";
    private static final String KEY_PET_ID = "PetId";
    private static final String KEY_ACTIVE_ENTITY = "ActiveEntity";
    private static final String KEY_FAINTED = "Fainted";
    private static final String KEY_READY_AT_TICK = "ReadyAtTick";
    private static final String KEY_HEALTH = "Health";
    private static final String KEY_MAX_HEALTH = "MaxHealth";
    private static final String KEY_CUSTOM_NAME = "CustomName";

    public record PetRecord(
            @Nullable UUID activeEntityUuid,
            boolean fainted,
            long readyAtTick,
            float health,
            float maxHealth,
            @Nullable String customName
    ) {
        public boolean isSummoned() {
            return this.activeEntityUuid != null;
        }
    }

    private final Map<UUID, PetRecord> records = new HashMap<>();

    public static PetRosterData get(Player player) {
        return player.getData(ArtilianAttachments.PETS);
    }

    public Optional<PetRecord> getRecord(UUID petId) {
        return Optional.ofNullable(this.records.get(petId));
    }

    public void putRecord(UUID petId, PetRecord record) {
        this.records.put(petId, record);
    }

    public boolean isActiveEntity(UUID petId, UUID entityUuid) {
        PetRecord record = this.records.get(petId);
        return record != null && entityUuid.equals(record.activeEntityUuid());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PetRecord> entry : this.records.entrySet()) {
            PetRecord record = entry.getValue();
            CompoundTag tag = new CompoundTag();
            tag.putUUID(KEY_PET_ID, entry.getKey());
            if (record.activeEntityUuid() != null) {
                tag.putUUID(KEY_ACTIVE_ENTITY, record.activeEntityUuid());
            }
            tag.putBoolean(KEY_FAINTED, record.fainted());
            tag.putLong(KEY_READY_AT_TICK, record.readyAtTick());
            tag.putFloat(KEY_HEALTH, record.health());
            tag.putFloat(KEY_MAX_HEALTH, record.maxHealth());
            if (record.customName() != null) {
                tag.putString(KEY_CUSTOM_NAME, record.customName());
            }
            list.add(tag);
        }
        root.put(KEY_ENTRIES, list);
        return root;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag root) {
        this.records.clear();
        ListTag list = root.getList(KEY_ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            if (!tag.hasUUID(KEY_PET_ID)) {
                continue;
            }
            UUID petId = tag.getUUID(KEY_PET_ID);
            UUID activeEntity = tag.hasUUID(KEY_ACTIVE_ENTITY) ? tag.getUUID(KEY_ACTIVE_ENTITY) : null;
            boolean fainted = tag.getBoolean(KEY_FAINTED);
            long readyAtTick = tag.getLong(KEY_READY_AT_TICK);
            float health = tag.getFloat(KEY_HEALTH);
            float maxHealth = Math.max(1.0F, tag.getFloat(KEY_MAX_HEALTH));
            String customName = tag.contains(KEY_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(KEY_CUSTOM_NAME) : null;
            this.records.put(petId, new PetRecord(activeEntity, fainted, readyAtTick, health, maxHealth, customName));
        }
    }
}
