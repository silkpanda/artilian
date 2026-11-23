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

/** Peeper: agile Shadow/Air eyeball scout of the Eye Realm that marks targets with brief Glowing. */
public class PeeperEntity extends EyeRealmEnemyEntity {
    public PeeperEntity(EntityType<? extends PeeperEntity> type, Level level) {
        super(type, level, "peeper");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(22.0, 0.26, 4.0, 2.0, 0.0);
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.SHADOW;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.AIR);
    }

    @Override
    protected void onElementalHit(LivingEntity target, ServerLevel level) {
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0));
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
