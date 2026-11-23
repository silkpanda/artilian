package com.silkpanda.artilian.pet;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * Shared base for Phase 5 Stage 6 hatchable pet dragons ({@code Canopy Dragonling},
 * {@code Amber Dragonling}, {@code Storm Dragonling}).
 *
 * <p>Extends {@link ArtilianPetEntity} so hatched dragons inherit Capture Crystal binding,
 * non-lethal fainting, {@link com.silkpanda.artilian.safety.KidSafeTargets}, and Biome Treat
 * bonding. Grants winged slow-fall safety and parameterized GeckoLib animation clips.
 */
public abstract class PetDragonEntity extends ArtilianPetEntity {
    private final RawAnimation idleAnim;
    private final RawAnimation walkAnim;
    private final RawAnimation sitAnim;
    private final RawAnimation attackAnim;

    protected PetDragonEntity(EntityType<? extends PetDragonEntity> type, Level level, String speciesId) {
        super(type, level);
        this.idleAnim = RawAnimation.begin().thenLoop("animation." + speciesId + ".idle");
        this.walkAnim = RawAnimation.begin().thenLoop("animation." + speciesId + ".walk");
        this.sitAnim = RawAnimation.begin().thenLoop("animation." + speciesId + ".sit");
        this.attackAnim = RawAnimation.begin().thenPlay("animation." + speciesId + ".attack");
    }

    protected static AttributeSupplier.Builder baseDragonlingAttributes(
            double maxHealth, double movementSpeed, double attackDamage) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.MOVEMENT_SPEED, movementSpeed)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, attackDamage);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Winged dragonlings flutter safely instead of taking fall damage
        return false;
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.onGround() && !this.isSitting() && this.getDeltaMovement().y < -0.06) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.65, 1.0));
            this.hasImpulse = true;
        }
    }

    @Override
    public void triggerAttackAnim() {
        this.triggerAnim("action", "attack");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (this.isSitting()) {
                return state.setAndContinue(this.sitAnim);
            }
            if (state.isMoving()) {
                return state.setAndContinue(this.walkAnim);
            }
            return state.setAndContinue(this.idleAnim);
        }));
        controllers.add(new AnimationController<>(this, "action", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", this.attackAnim));
    }
}
