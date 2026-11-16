package com.silkpanda.artilian.mana;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Syncs authoritative server Mana values to the owning client for HUD rendering and client-side checks.
 * Must stay free of {@code net.minecraft.client.*} imports so dedicated servers load safely.
 */
public record ManaSyncPayload(int mana, int maxMana) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ManaSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ArtilianMod.id("mana_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ManaSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ManaSyncPayload::mana,
            ByteBufCodecs.VAR_INT, ManaSyncPayload::maxMana,
            ManaSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(ManaSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                ManaData data = ManaData.get(player);
                data.setMaxMana(payload.maxMana());
                data.setMana(payload.mana());
            }
        });
    }
}
