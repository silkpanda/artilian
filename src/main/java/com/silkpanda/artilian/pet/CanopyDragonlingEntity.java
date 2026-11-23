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
 * Canopy Dragonling: hatchable jungle dragon companion ({@link PetElement#DRAGON} / {@link PetElement#NATURE}).
 */
public class CanopyDragonlingEntity extends PetDragonEntity {
    public CanopyDragonlingEntity(EntityType<? extends CanopyDragonlingEntity> type, Level level) {
        super(type, level, "canopy_dragonling");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseDragonlingAttributes(28.0, 0.33, 6.0);
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.DRAGON;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.NATURE);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0));
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SPORE_BLOSSOM_AIR,
                        living.getX(),
                        living.getY(0.6),
                        living.getZ(),
                        8,
                        0.3,
                        0.3,
                        0.3,
                        0.03);
            }
        }
        return hurt;
    }
}
