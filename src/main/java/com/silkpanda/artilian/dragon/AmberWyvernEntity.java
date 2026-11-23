package com.silkpanda.artilian.dragon;

import com.silkpanda.artilian.pet.PetElement;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Amber Wyvern: volcanic dragon species inhabiting the Amber Volcanic Grove.
 * Elements: {@link PetElement#DRAGON} / {@link PetElement#FIRE}.
 */
public class AmberWyvernEntity extends WildDragonEntity {
    public AmberWyvernEntity(EntityType<? extends AmberWyvernEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseDragonAttributes(48.0, 0.29, 7.0, 5.0, 0.5);
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
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void onDragonHit(LivingEntity target, ServerLevel level) {
        target.igniteForSeconds(2.5F);
        level.sendParticles(
                ParticleTypes.FLAME,
                target.getX(),
                target.getY(0.6),
                target.getZ(),
                10,
                0.35,
                0.25,
                0.35,
                0.04);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_FLAP;
    }
}
