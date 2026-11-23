package com.silkpanda.artilian.pet;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Amber Dragonling: hatchable volcanic wyvern companion ({@link PetElement#DRAGON} / {@link PetElement#FIRE}).
 */
public class AmberDragonlingEntity extends PetDragonEntity {
    public AmberDragonlingEntity(EntityType<? extends AmberDragonlingEntity> type, Level level) {
        super(type, level, "amber_dragonling");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseDragonlingAttributes(30.0, 0.32, 6.5);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.DRAGON;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.FIRE);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.igniteForSeconds(2.5F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        living.getX(),
                        living.getY(0.6),
                        living.getZ(),
                        10,
                        0.3,
                        0.3,
                        0.3,
                        0.04);
            }
        }
        return hurt;
    }
}
