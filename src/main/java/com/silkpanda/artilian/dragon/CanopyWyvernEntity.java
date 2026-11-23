package com.silkpanda.artilian.dragon;

import com.silkpanda.artilian.pet.PetElement;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Canopy Wyvern: the first wild Dragon Jungle species, establishing the shared {@code dragon_rig}.
 * Elements: {@link PetElement#DRAGON} / {@link PetElement#NATURE}.
 */
public class CanopyWyvernEntity extends WildDragonEntity {
    public CanopyWyvernEntity(EntityType<? extends CanopyWyvernEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseDragonAttributes(44.0, 0.28, 6.0, 4.0, 0.4);
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
    protected void onDragonHit(LivingEntity target, ServerLevel level) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0));
        level.sendParticles(
                ParticleTypes.SPORE_BLOSSOM_AIR,
                target.getX(),
                target.getY(0.7),
                target.getZ(),
                8,
                0.3,
                0.25,
                0.3,
                0.02);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_IMITATE_ENDER_DRAGON;
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
