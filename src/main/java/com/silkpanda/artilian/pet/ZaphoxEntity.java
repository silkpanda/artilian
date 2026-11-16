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
 * Alex's First Creature: Zaphox — a fusion of cheetah (speed, spots, tear marks), fox (alert ears,
 * bushy tail), and elephant (Option C: curling mini-trunk that points and shocks, tiny tusks).
 * Flies with orange sawtooth wings along its back. Elements: {@link PetElement#ELECTRIC} / {@link PetElement#AIR}.
 */
public class ZaphoxEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.zaphox.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.zaphox.walk");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.zaphox.fly");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.zaphox.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.zaphox.attack");

    public ZaphoxEntity(EntityType<? extends ZaphoxEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    /** Spawns on Starlight Highlands turf or slate ground. */
    public static boolean checkZaphoxSpawnRules(
            EntityType<? extends ZaphoxEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(ArtilianBlocks.STARLIGHT_SLATE.get())
                || below.is(ArtilianBlocks.STARLIGHT_TURF.get());
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Zaphox spreads its wings to glide safely — immune to fall damage
        return false;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.ELECTRIC;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.AIR);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.FOX_SNIFF;
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
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && !this.isSitting() && this.onGround()
                && (this.getTarget() != null || (this.getNavigation().isInProgress() && this.random.nextInt(20) == 0))) {
            // Playful wing-assisted lightning bound so Zaphox visibly flies/glides across terrain
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.34, 0.0));
            this.hasImpulse = true;
        }
        if (!this.onGround() && !this.isSitting() && this.getDeltaMovement().y < 0.0) {
            // Gentle wing-glide descent
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.55, 1.0));
            this.hasImpulse = true;
        }

        if (this.level().isClientSide && this.random.nextInt(6) == 0) {
            this.level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getRandomX(0.4),
                    this.getY(0.6 + this.random.nextDouble() * 0.4),
                    this.getRandomZ(0.4),
                    (this.random.nextDouble() - 0.5) * 0.05,
                    0.02,
                    (this.random.nextDouble() - 0.5) * 0.05);
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
            if (!this.onGround()) {
                return state.setAndContinue(FLY);
            }
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK));
    }
}
