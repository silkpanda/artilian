package com.silkpanda.artilian.pet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

/**
 * Immutable snapshot of a captured pet stored inside a {@link CaptureCrystalItem}.
 */
public record CapturedPetData(
        UUID petId,
        ResourceLocation entityType,
        PetElement primaryElement,
        Optional<PetElement> secondaryElement,
        UUID ownerUuid,
        String ownerName,
        Optional<String> customName,
        float health,
        float maxHealth,
        boolean fainted,
        long readyAtTick,
        boolean summoned,
        Optional<UUID> activeEntityUuid
) {
    public static final Codec<CapturedPetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("pet_id").forGetter(CapturedPetData::petId),
            ResourceLocation.CODEC.fieldOf("entity_type").forGetter(CapturedPetData::entityType),
            PetElement.CODEC.fieldOf("primary_element").forGetter(CapturedPetData::primaryElement),
            PetElement.CODEC.optionalFieldOf("secondary_element").forGetter(CapturedPetData::secondaryElement),
            UUIDUtil.CODEC.fieldOf("owner_uuid").forGetter(CapturedPetData::ownerUuid),
            Codec.STRING.fieldOf("owner_name").forGetter(CapturedPetData::ownerName),
            Codec.STRING.optionalFieldOf("custom_name").forGetter(CapturedPetData::customName),
            Codec.FLOAT.fieldOf("health").forGetter(CapturedPetData::health),
            Codec.FLOAT.fieldOf("max_health").forGetter(CapturedPetData::maxHealth),
            Codec.BOOL.fieldOf("fainted").forGetter(CapturedPetData::fainted),
            Codec.LONG.fieldOf("ready_at_tick").forGetter(CapturedPetData::readyAtTick),
            Codec.BOOL.fieldOf("summoned").forGetter(CapturedPetData::summoned),
            UUIDUtil.CODEC.optionalFieldOf("active_entity_uuid").forGetter(CapturedPetData::activeEntityUuid)
    ).apply(instance, CapturedPetData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CapturedPetData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeUUID(data.petId());
                buf.writeResourceLocation(data.entityType());
                PetElement.STREAM_CODEC.encode(buf, data.primaryElement());
                buf.writeOptional(data.secondaryElement(), PetElement.STREAM_CODEC::encode);
                buf.writeUUID(data.ownerUuid());
                buf.writeUtf(data.ownerName());
                buf.writeOptional(data.customName(), (b, name) -> b.writeUtf(name));
                buf.writeFloat(data.health());
                buf.writeFloat(data.maxHealth());
                buf.writeBoolean(data.fainted());
                buf.writeVarLong(data.readyAtTick());
                buf.writeBoolean(data.summoned());
                buf.writeOptional(data.activeEntityUuid(), (b, id) -> b.writeUUID(id));
            },
            buf -> new CapturedPetData(
                    buf.readUUID(),
                    buf.readResourceLocation(),
                    PetElement.STREAM_CODEC.decode(buf),
                    buf.readOptional(PetElement.STREAM_CODEC::decode),
                    buf.readUUID(),
                    buf.readUtf(),
                    buf.readOptional(b -> b.readUtf()),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readBoolean(),
                    buf.readVarLong(),
                    buf.readBoolean(),
                    buf.readOptional(b -> b.readUUID())
            )
    );

    public boolean isOwnedBy(UUID playerId) {
        return this.ownerUuid.equals(playerId);
    }

    public CapturedPetData asSummoned(UUID entityUuid, float currentHealth, Optional<String> updatedName) {
        return new CapturedPetData(
                this.petId,
                this.entityType,
                this.primaryElement,
                this.secondaryElement,
                this.ownerUuid,
                this.ownerName,
                updatedName,
                currentHealth,
                this.maxHealth,
                false,
                0L,
                true,
                Optional.of(entityUuid)
        );
    }

    public CapturedPetData asRecalled(float currentHealth, Optional<String> updatedName) {
        return new CapturedPetData(
                this.petId,
                this.entityType,
                this.primaryElement,
                this.secondaryElement,
                this.ownerUuid,
                this.ownerName,
                updatedName,
                currentHealth,
                this.maxHealth,
                false,
                0L,
                false,
                Optional.empty()
        );
    }

    public CapturedPetData asFainted(long readyAtTick, Optional<String> updatedName) {
        return new CapturedPetData(
                this.petId,
                this.entityType,
                this.primaryElement,
                this.secondaryElement,
                this.ownerUuid,
                this.ownerName,
                updatedName,
                this.maxHealth,
                this.maxHealth,
                true,
                readyAtTick,
                false,
                Optional.empty()
        );
    }
}
