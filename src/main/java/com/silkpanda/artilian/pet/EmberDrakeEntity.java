package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Optional;

/**
 * Ember Drake: a small, friendly Canopy-Emerald & Ember-Amber dragon hatchling native to
 * Emberbloom Meadows, giving Steve an early glimpse of his Dragon Jungle dimension theme.
 * Elements: {@link PetElement#DRAGON} / {@link PetElement#FIRE}.
 */
public class EmberDrakeEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.ember_drake.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.ember_drake.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.ember_drake.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.ember_drake.attack");

    public EmberDrakeEntity(EntityType<? extends EmberDrakeEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    /** Spawns in sunlit Emberbloom Meadows on Ember Turf or standard animal ground. */
    public static boolean checkDragonHatchlingSpawnRules(
            EntityType<? extends EmberDrakeEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(ArtilianBlocks.EMBER_TURF.get());
        return validGround && level.getRawBrightness(pos, 0) > 7;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Winged hatchling flutters safely to the ground
        return false;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.DRAGON;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.FIRE);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.onGround() && !this.isSitting() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.7, 1.0));
            this.hasImpulse = true;
        }
        if (this.level().isClientSide && this.random.nextInt(8) == 0) {
            this.level().addParticle(
                    ParticleTypes.SMALL_FLAME,
                    this.getRandomX(0.35),
                    this.getY(0.45 + this.random.nextDouble() * 0.35),
                    this.getRandomZ(0.35),
                    0.0,
                    0.02,
                    0.0);
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
                return state.setAndContinue(SIT);
            }
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK));
    }
}
