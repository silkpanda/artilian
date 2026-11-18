package com.silkpanda.artilian.boss;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The shared danger cue for anything a boss is about to do: a {@code #FF4D5A} dust ring on the
 * ground and a bell that climbs in pitch. Boss attacks and the Boss Altar countdown both use it, so
 * kids learn one warning sign.
 */
public final class BossTelegraphs {
    /** Bell pitches at windup start, one-third, and two-thirds. */
    public static final float PITCH_START = 0.8F;
    public static final float PITCH_MIDDLE = 1.2F;
    public static final float PITCH_LAST = 1.6F;

    private static final int RING_POINTS = 24;

    private BossTelegraphs() {}

    /** Draws one frame of the red ring at {@code radius} around {@code center}. */
    public static void ring(ServerLevel level, Vec3 center, double radius) {
        DustParticleOptions particle = new DustParticleOptions(TelegraphedAttack.DANGER_RED, 1.5F);
        for (int i = 0; i < RING_POINTS; i++) {
            double angle = i * (Math.PI * 2.0 / RING_POINTS);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(particle, x, center.y + 0.1, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public static void bell(Level level, double x, double y, double z, float pitch) {
        level.playSound(null, x, y, z, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.HOSTILE, 2.0F, pitch);
    }

    /**
     * Ring every 2 ticks and the climbing bell at one-third and two-thirds of {@code totalTicks}.
     * Call once per tick with the tick count since the windup started (1-based). The start bell is
     * the caller's job, played when the windup begins.
     */
    public static void tick(ServerLevel level, Vec3 center, double radius, int elapsed, int totalTicks) {
        if (elapsed % 2 == 0) {
            ring(level, center, radius);
        }
        int third = totalTicks / 3;
        if (elapsed == third) {
            bell(level, center.x, center.y, center.z, PITCH_MIDDLE);
        } else if (elapsed == 2 * third) {
            bell(level, center.x, center.y, center.z, PITCH_LAST);
        }
    }
}
