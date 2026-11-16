package com.silkpanda.artilian.chester;

import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/** The rules for who gets a Chester and when. Server side only. */
public final class ChesterService {
    private ChesterService() {}

    /**
     * Gives a player their Chester the first time they ever join. Later logins spawn nothing,
     * but every login makes sure the player still has a whistle.
     */
    public static void handleLogin(ServerPlayer player) {
        ChesterData data = ChesterData.get(player);
        giveWhistleIfMissing(player);
        if (data.isFirstSpawnDone()) {
            return;
        }
        summon(player);
        data.setFirstSpawnDone(true);
    }

    /**
     * Puts the player's one Chester beside them. Any Chester they already had is removed first,
     * so a player never has two. A Chester that could not be found (its chunk is not loaded)
     * removes itself when it next wakes up, see {@link ChesterEntity#discardIfOrphan}.
     */
    public static ChesterEntity summon(ServerPlayer player) {
        ChesterData data = ChesterData.get(player);
        data.getChesterEntityId()
                .flatMap(id -> findLoaded(player.getServer(), id))
                .ifPresent(Entity::discard);

        ServerLevel level = player.serverLevel();
        ChesterEntity chester = ArtilianEntities.CHESTER.get().create(level);
        if (chester == null) {
            throw new IllegalStateException("Could not create a Chester entity");
        }
        chester.setOwner(player);

        Vec3 inFront = player.position().add(Vec3.directionFromRotation(0.0F, player.getYRot()).scale(1.5));
        float facingOwner = player.getYRot() + 180.0F;
        chester.moveTo(inFront.x, inFront.y, inFront.z, facingOwner, 0.0F);
        if (!level.noCollision(chester)) {
            chester.moveTo(player.getX(), player.getY(), player.getZ(), facingOwner, 0.0F);
        }
        chester.setYBodyRot(facingOwner);
        chester.setYHeadRot(facingOwner);

        level.addFreshEntity(chester);
        data.setChesterEntityId(chester.getUUID());
        return chester;
    }

    /** Finds a Chester by entity id in any dimension, if its chunk is loaded. */
    public static Optional<ChesterEntity> findLoaded(MinecraftServer server, UUID entityId) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(entityId) instanceof ChesterEntity chester) {
                return Optional.of(chester);
            }
        }
        return Optional.empty();
    }

    // ---- The whistle is never lost for good ----

    public static boolean hasWhistle(ServerPlayer player) {
        return player.getInventory().hasAnyMatching(ChesterService::isWhistle);
    }

    /** Called on login and on respawn. Returns true if a whistle was handed over. */
    public static boolean giveWhistleIfMissing(ServerPlayer player) {
        if (hasWhistle(player)) {
            return false;
        }
        ItemStack whistle = new ItemStack(ArtilianItems.CHESTER_WHISTLE.get());
        if (!player.getInventory().add(whistle)) {
            player.drop(whistle, false);
        }
        return true;
    }

    /**
     * A dying player does not drop their whistle. They get a new one when they respawn, so
     * leaving the old one on the ground would only pile up spares.
     */
    public static void keepWhistleOutOfDrops(Collection<ItemEntity> drops) {
        drops.removeIf(drop -> isWhistle(drop.getItem()));
    }

    private static boolean isWhistle(ItemStack stack) {
        return stack.is(ArtilianItems.CHESTER_WHISTLE.get());
    }
}
