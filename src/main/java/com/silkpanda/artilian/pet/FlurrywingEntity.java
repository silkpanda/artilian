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
 * Flurrywing: the kids' four-winged snow-fairy snowman (placeholder name until they pick one).
 * Three stacked snowballs, a beanie, a stick arm and a mitten arm, no legs: it hovers like Peepkin.
 * A regular pet that happens to be the tallest one, about 2.75 blocks to the top of its hat.
 * Elements: {@link PetElement#ICE} / {@link PetElement#AIR}.
 */
public class FlurrywingEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.flurrywing.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.flurrywing.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.flurrywing.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.flurrywing.attack");

    public FlurrywingEntity(EntityType<? extends FlurrywingEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5);
    }

    /** Spawns in Starlight Highlands on grass-like ground, Starlight Slate or Starlight Turf. */
    public static boolean checkFlurrywingSpawnRules(
            EntityType<? extends FlurrywingEntity> type,
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
        // No legs: it floats down on four frost wings and never takes fall damage
        return false;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.ICE;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.AIR);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNOW_GOLEM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SNOW_GOLEM_HURT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.onGround() && !this.isSitting() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.65, 1.0));
            this.hasImpulse = true;
        }
        // Snowflakes drift down from the wings, about one every 6 ticks.
        if (this.level().isClientSide && this.random.nextInt(6) == 0) {
            this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getRandomX(1.0),
                    this.getY(0.4 + this.random.nextDouble() * 0.5),
                    this.getRandomZ(1.0),
                    (this.random.nextDouble() - 0.5) * 0.02,
                    -0.03,
                    (this.random.nextDouble() - 0.5) * 0.02);
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
