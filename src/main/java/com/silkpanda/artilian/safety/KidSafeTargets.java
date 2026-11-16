package com.silkpanda.artilian.safety;

import com.silkpanda.artilian.chester.ChesterEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * The one rule for what Artilian's wands and TNTs may hurt or shove. Every spell and blast asks
 * here, so a new weapon cannot forget to spare pets or villagers. Kid-safe control: do not loosen
 * it without the owner's explicit instruction (see AGENTS.md).
 */
public final class KidSafeTargets {
    private KidSafeTargets() {}

    /**
     * False for players, Chester, villagers, armor stands, tamed or owned pets, baby animals, and
     * friendly golems (iron and snow). {@code caster} is the player casting, or null for a TNT.
     */
    public static boolean isValidOffensiveTarget(@Nullable Entity caster, LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator() || entity == caster) {
            return false;
        }
        if (entity instanceof Player
                || entity instanceof ChesterEntity
                || entity instanceof ArmorStand
                || entity instanceof AbstractVillager) {
            return false;
        }
        if (entity instanceof AbstractGolem && !(entity instanceof Enemy)) {
            return false;
        }
        if (entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return false;
        }
        if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
            return false;
        }
        if (entity instanceof AbstractHorse horse && horse.isTamed()) {
            return false;
        }
        return !(entity instanceof Animal animal && animal.isBaby());
    }

    /** True for monsters. Wands aim at these first, and Party TNT leaves them out of the party. */
    public static boolean isHostile(LivingEntity entity) {
        return entity instanceof Enemy;
    }
}
