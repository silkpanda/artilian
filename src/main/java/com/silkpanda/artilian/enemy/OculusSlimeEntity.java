package com.silkpanda.artilian.enemy;

import com.silkpanda.artilian.pet.PetElement;
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

/** Oculus Slime: bouncy Shadow/Crystal gel cube of the Eye Realm that applies brief Slowness I on hit. */
public class OculusSlimeEntity extends EyeRealmEnemyEntity {
    public OculusSlimeEntity(EntityType<? extends OculusSlimeEntity> type, Level level) {
        super(type, level, "oculus_slime");
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(34.0, 0.22, 5.5, 3.0, 0.2);
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.SHADOW;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.CRYSTAL);
    }

    @Override
    protected void onElementalHit(LivingEntity target, ServerLevel level) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SLIME_SQUISH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }
}
