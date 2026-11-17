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
 * Guardian of the Crystal Grove Shrine in Crystal Canopy Woods.
 * Elements: {@link PetElement#CRYSTAL} / {@link PetElement#EARTH}.
 */
public class PrismSentinelEntity extends StructureEnemyEntity {
    public PrismSentinelEntity(EntityType<? extends PrismSentinelEntity> type, Level level) {
        super(type, level, "prism_sentinel");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(30.0, 0.24, 4.5, 4.0);
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.CRYSTAL;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.EARTH);
    }

    @Override
    protected void onElementalHit(LivingEntity target, ServerLevel level) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_CLUSTER_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }
}
