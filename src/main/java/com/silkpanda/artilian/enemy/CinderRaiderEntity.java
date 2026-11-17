package com.silkpanda.artilian.enemy;

import com.silkpanda.artilian.pet.PetElement;
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
 * Guardian of the Emberbloom Outpost in Emberbloom Meadows.
 * Elements: {@link PetElement#FIRE} / {@link PetElement#EARTH}.
 */
public class CinderRaiderEntity extends StructureEnemyEntity {
    public CinderRaiderEntity(EntityType<? extends CinderRaiderEntity> type, Level level) {
        super(type, level, "cinder_raider");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(26.0, 0.30, 5.0, 2.0);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.FIRE;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.EARTH);
    }

    @Override
    protected void onElementalHit(LivingEntity target, ServerLevel level) {
        target.igniteForSeconds(2.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }
}
