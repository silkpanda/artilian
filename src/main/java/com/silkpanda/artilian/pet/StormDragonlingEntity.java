package com.silkpanda.artilian.pet;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Storm Dragonling: hatchable sky-serpent companion ({@link PetElement#DRAGON} / {@link PetElement#ELECTRIC}).
 */
public class StormDragonlingEntity extends PetDragonEntity {
    public StormDragonlingEntity(EntityType<? extends StormDragonlingEntity> type, Level level) {
        super(type, level, "storm_dragonling");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseDragonlingAttributes(26.0, 0.35, 6.0);
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.DRAGON;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.ELECTRIC);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 35, 0));
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        living.getX(),
                        living.getY(0.6),
                        living.getZ(),
                        12,
                        0.35,
                        0.35,
                        0.35,
                        0.05);
            }
        }
        return hurt;
    }
}
