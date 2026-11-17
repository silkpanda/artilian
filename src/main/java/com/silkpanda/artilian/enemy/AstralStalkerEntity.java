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

/**
 * Guardian of the Starlight Watchtower in Starlight Highlands.
 * Elements: {@link PetElement#STARLIGHT} / {@link PetElement#SHADOW}.
 */
public class AstralStalkerEntity extends StructureEnemyEntity {
    public AstralStalkerEntity(EntityType<? extends AstralStalkerEntity> type, Level level) {
        super(type, level, "astral_stalker");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(28.0, 0.32, 5.0, 2.0);
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.STARLIGHT;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.SHADOW);
    }

    @Override
    protected void onElementalHit(LivingEntity target, ServerLevel level) {
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }
}
