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
 * Storm Glidewing: swift sky-striding dragon species across both Dragon Jungle biomes.
 * Elements: {@link PetElement#DRAGON} / {@link PetElement#ELECTRIC}.
 */
public class StormGlidewingEntity extends WildDragonEntity {
    public StormGlidewingEntity(EntityType<? extends StormGlidewingEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseDragonAttributes(40.0, 0.32, 6.5, 3.0, 0.3);
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
    protected void onDragonHit(LivingEntity target, ServerLevel level) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 35, 0));
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                target.getX(),
                target.getY(0.7),
                target.getZ(),
                12,
                0.35,
                0.3,
                0.35,
                0.08);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
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
