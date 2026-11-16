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
 * Peepkin: a curious, floating eye-themed companion covered in blinking twilight-magenta and
 * glowing-cyan eyes, giving Alex an early taste of her Eye Realm theme in Phase 2.
 * Elements: {@link PetElement#SHADOW} / {@link PetElement#STARLIGHT}.
 */
public class PeepkinEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.peepkin.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.peepkin.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.peepkin.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.peepkin.attack");

    public PeepkinEntity(EntityType<? extends PeepkinEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5);
    }

    /** Spawns in Starlight Highlands and Crystal Canopy Woods on turf or Starlight Slate in any light. */
    public static boolean checkEyePetSpawnRules(
            EntityType<? extends PeepkinEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(ArtilianBlocks.STARLIGHT_SLATE.get())
                || below.is(ArtilianBlocks.STARLIGHT_TURF.get())
                || below.is(ArtilianBlocks.CRYSTAL_TURF.get());
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Peepkin hovers lightly with its winglet-ears and never takes fall damage
        return false;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.SHADOW;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.STARLIGHT);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.ENDER_EYE_LAUNCH;
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
        if (!this.onGround() && !this.isSitting() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.65, 1.0));
            this.hasImpulse = true;
        }
        if (this.level().isClientSide && this.random.nextInt(7) == 0) {
            this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getRandomX(0.4),
                    this.getY(0.5 + this.random.nextDouble() * 0.4),
                    this.getRandomZ(0.4),
                    (this.random.nextDouble() - 0.5) * 0.03,
                    0.02,
                    (this.random.nextDouble() - 0.5) * 0.03);
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
