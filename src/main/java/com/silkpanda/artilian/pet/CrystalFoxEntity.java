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

import java.util.Optional;

/**
 * Crystal Fox: a playful, teal-and-crystal woodland fox native to Crystal Canopy Woods.
 * Primary element: {@link PetElement#CRYSTAL}, Secondary element: {@link PetElement#NATURE}.
 */
public class CrystalFoxEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.crystal_fox.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.crystal_fox.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.crystal_fox.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.crystal_fox.attack");

    public CrystalFoxEntity(EntityType<? extends CrystalFoxEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.CRYSTAL;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.NATURE);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    public void triggerAttackAnim() {
        this.triggerAnim("action", "attack");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (this.isSitting()) {
                return state.setAndContinue(SIT);
            }
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK));
    }
}
