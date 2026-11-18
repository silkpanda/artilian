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
import net.neoforged.neoforge.fluids.FluidType;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Optional;

/**
 * Frogduck: the kids' amphibious teal frog with an orange duck perched on its head (placeholder name).
 * One single character with four clawed feet, a face on the front of its body, and outstretched duck wings on top.
 * Elements: {@link PetElement#WATER} / {@link PetElement#NATURE}.
 */
public class FrogduckEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.frogduck.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.frogduck.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.frogduck.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.frogduck.attack");

    public FrogduckEntity(EntityType<? extends FrogduckEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5);
    }

    /** Spawns in Crystal Canopy Woods on grass-like ground or Crystal Turf. */
    public static boolean checkFrogduckSpawnRules(
            EntityType<? extends FrogduckEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.ANIMALS_SPAWNABLE_ON) || below.is(ArtilianBlocks.CRYSTAL_TURF.get());
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Frog-like hop resilience for short drops.
        if (fallDistance < 5.0F) {
            return false;
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.WATER;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.NATURE);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FROG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FROG_HURT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide && this.isInWater() && this.random.nextInt(5) == 0) {
            this.level().addParticle(
                    ParticleTypes.SPLASH,
                    this.getRandomX(0.8),
                    this.getY(0.5),
                    this.getRandomZ(0.8),
                    (this.random.nextDouble() - 0.5) * 0.05,
                    0.06,
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
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK));
    }
}
