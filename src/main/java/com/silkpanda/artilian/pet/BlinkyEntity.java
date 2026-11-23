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
 * Blinky: Alex's signature Eye Realm ocular familiar that hovers gently, blinks its articulated
 * eyelids, and brings Starlight/Shadow synergy to battles in the Eye Realm.
 * Elements: {@link PetElement#STARLIGHT} / {@link PetElement#SHADOW}.
 */
public class BlinkyEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.blinky.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.blinky.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.blinky.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.blinky.attack");

    public BlinkyEntity(EntityType<? extends BlinkyEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    /** Spawns naturally in the Eye Realm on Eye Turf, Gaze Stone, Starlight Slate, or animal ground. */
    public static boolean checkBlinkySpawnRules(
            EntityType<? extends BlinkyEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(ArtilianBlocks.EYE_TURF.get())
                || below.is(ArtilianBlocks.GAZE_STONE.get())
                || below.is(ArtilianBlocks.STARLIGHT_SLATE.get())
                || below.is(BlockTags.ANIMALS_SPAWNABLE_ON);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Blinky hovers on starlight eye-wings and never takes fall damage
        return false;
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
        if (!this.onGround() && !this.isSitting() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.62, 1.0));
            this.hasImpulse = true;
        }
        if (this.level().isClientSide && this.random.nextInt(6) == 0) {
            this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getRandomX(0.45),
                    this.getY(0.55 + this.random.nextDouble() * 0.45),
                    this.getRandomZ(0.45),
                    (this.random.nextDouble() - 0.5) * 0.03,
                    0.025,
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
            if (state.isMoving()) {
                return state.setAndContinue(WALK);
            }
            return state.setAndContinue(IDLE);
        }));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK));
    }
}
