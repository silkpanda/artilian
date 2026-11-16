package com.silkpanda.artilian.gametest;

import com.mojang.authlib.GameProfile;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.registry.ArtilianEntities;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.List;
import java.util.UUID;

/** Shared helpers for the Chester GameTests. */
public final class ChesterTestSupport {
    private ChesterTestSupport() {}

    /** A server player standing in the middle of the test area. Not in the server's player list. */
    public static ServerPlayer playerInTestArea(GameTestHelper helper, String name) {
        ServerPlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        Vec3 middle = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.moveTo(middle.x, middle.y, middle.z, 0.0F, 0.0F);
        return player;
    }

    /** Every living Chester in the level that belongs to this player. */
    public static List<? extends ChesterEntity> chestersOf(GameTestHelper helper, ServerPlayer owner) {
        return helper.getLevel().getEntities(ArtilianEntities.CHESTER.get(),
                chester -> chester.isAlive() && chester.isOwner(owner));
    }

    public static void removeChestersOf(GameTestHelper helper, ServerPlayer... owners) {
        for (ServerPlayer owner : owners) {
            chestersOf(helper, owner).forEach(ChesterEntity::discard);
        }
    }
}
