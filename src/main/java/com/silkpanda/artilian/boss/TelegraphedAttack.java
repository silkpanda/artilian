package com.silkpanda.artilian.boss;

import com.silkpanda.artilian.pet.PetElement;
import org.joml.Vector3f;

/**
 * Immutable description of one boss attack. The constructor enforces the kid-safe telegraph floor:
 * no attack can be built with less than 30 ticks (1.5 s) of warning.
 */
public record TelegraphedAttack(
        String id, int windupTicks, double radius, float damage, PetElement element) {

    /** 30 ticks at 20 ticks per second: the shortest allowed warning before impact. */
    public static final int MIN_WINDUP_TICKS = 30;

    /** DESIGN.md boss / danger telegraph colour, #FF4D5A. */
    public static final Vector3f DANGER_RED = new Vector3f(1.0F, 0.302F, 0.353F);

    public TelegraphedAttack {
        if (windupTicks < MIN_WINDUP_TICKS) {
            throw new IllegalArgumentException(
                    "Boss attacks need at least " + MIN_WINDUP_TICKS + " ticks of warning, got " + windupTicks);
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("Boss attack radius must be positive, got " + radius);
        }
        if (damage < 0) {
            throw new IllegalArgumentException("Boss attack damage must not be negative, got " + damage);
        }
    }
}
