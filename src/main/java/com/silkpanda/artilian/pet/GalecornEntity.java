package com.silkpanda.artilian.pet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Optional;

/**
 * Alex's Second Creature: Galecorn — a fusion of fox and unicorn with an Air element.
 * Uses its gigantic, fluffy whirlwind tail to blow hostile mobs away with a 360-degree tail-spin.
 * Elements: {@link PetElement#AIR} / {@link PetElement#NATURE}.
 */
public class GalecornEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.galecorn.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.galecorn.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.galecorn.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.galecorn.attack");

    public GalecornEntity(EntityType<? extends GalecornEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5);
    }

    /** Spawns on peaceful animal ground in daylight/meadows. */
    public static boolean checkGalecornSpawnRules(
            EntityType<? extends GalecornEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
                && level.getRawBrightness(pos, 0) > 7;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.AIR;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.NATURE);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.HORSE_BREATHE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FOX_SNIFF;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            applyWhirlwindTailKnockback(living);
        }
        return hurt;
    }

    /** Alex's signature feature: uses its giant tail to blow enemies away! */
    public void applyWhirlwindTailKnockback(LivingEntity target) {
        Vec3 diff = target.position().subtract(this.position());
        double distSq = diff.x * diff.x + diff.z * diff.z;
        double dist = Math.max(0.1, Math.sqrt(distSq));
        double pushX = (diff.x / dist) * 0.85;
        double pushZ = (diff.z / dist) * 0.85;

        target.setDeltaMovement(pushX, 0.38, pushZ);
        target.hasImpulse = true;
        target.hurtMarked = true;

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    this.getX(),
                    this.getY(0.5),
                    this.getZ(),
                    12,
                    0.4,
                    0.2,
                    0.4,
                    0.05);
            serverLevel.sendParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    target.getX(),
                    target.getY(0.5),
                    target.getZ(),
                    2,
                    0.2,
                    0.1,
                    0.2,
                    0.0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide && this.random.nextInt(8) == 0) {
            this.level().addParticle(
                    ParticleTypes.CLOUD,
                    this.getRandomX(0.45),
                    this.getY(0.4 + this.random.nextDouble() * 0.4),
                    this.getRandomZ(0.45),
                    (this.random.nextDouble() - 0.5) * 0.02,
                    0.01,
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
