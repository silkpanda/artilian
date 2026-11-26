package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
 * Fangwing (working name): a scary purple bat pet inspired by a kid's drawing.
 * Floats and swoops on broad geometric wings, takes zero fall damage, and unleashes a
 * Void Screech on melee hits (Weakness + Slowness on a 60-tick cooldown).
 * Elements: {@link PetElement#SHADOW} / {@link PetElement#AIR}.
 */
public class FangwingEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.fangwing.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.fangwing.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.fangwing.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.fangwing.attack");

    private static final int VOID_SCREECH_COOLDOWN_TICKS = 60;
    private int voidScreechCooldown;

    public FangwingEntity(EntityType<? extends FangwingEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5);
    }

    /**
     * Spawns in Starlight Highlands on Starlight Turf / Slate at any light level,
     * or in dark Overworld caves / night (rawBrightness <= 7) on animal-spawnable ground or stone.
     */
    public static boolean checkFangwingSpawnRules(
            EntityType<? extends FangwingEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(ArtilianBlocks.STARLIGHT_TURF.get()) || below.is(ArtilianBlocks.STARLIGHT_SLATE.get())) {
            return true;
        }
        boolean validGround = below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(BlockTags.BASE_STONE_OVERWORLD);
        return validGround && level.getMaxLocalRawBrightness(pos) <= 7;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Swoops on broad bat wings and never takes fall damage.
        return false;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.SHADOW;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.AIR);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.voidScreechCooldown > 0) {
            this.voidScreechCooldown--;
        }
        if (!this.onGround() && !this.isSitting() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.62, 1.0));
            this.hasImpulse = true;
        }
        if (this.level().isClientSide && this.random.nextInt(7) == 0) {
            this.level().addParticle(
                    ParticleTypes.PORTAL,
                    this.getRandomX(0.6),
                    this.getY(0.4 + this.random.nextDouble() * 0.5),
                    this.getRandomZ(0.6),
                    (this.random.nextDouble() - 0.5) * 0.04,
                    0.02,
                    (this.random.nextDouble() - 0.5) * 0.04);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living && this.voidScreechCooldown <= 0) {
            applyVoidScreech(living);
            this.voidScreechCooldown = VOID_SCREECH_COOLDOWN_TICKS;
        }
        return hurt;
    }

    /** Signature perk: Void Screech weakens and slows the struck enemy. */
    public void applyVoidScreech(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.BAT_AMBIENT,
                    SoundSource.NEUTRAL,
                    0.9F,
                    0.65F);
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.PHANTOM_AMBIENT,
                    SoundSource.NEUTRAL,
                    0.6F,
                    1.2F);
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    target.getX(),
                    target.getY(0.6),
                    target.getZ(),
                    10,
                    0.35,
                    0.3,
                    0.35,
                    0.08);
            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    target.getX(),
                    target.getY(0.6),
                    target.getZ(),
                    6,
                    0.3,
                    0.25,
                    0.3,
                    0.04);
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
