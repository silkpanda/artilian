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

/** Lens Guardian: towering Starlight/Shadow armored sentinel of the Eye Realm. */
public class LensGuardianEntity extends EyeRealmEnemyEntity {
    public LensGuardianEntity(EntityType<? extends LensGuardianEntity> type, Level level) {
        super(type, level, "lens_guardian");
        this.xpReward = 18;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(54.0, 0.21, 7.5, 6.0, 0.45);
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
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
}
