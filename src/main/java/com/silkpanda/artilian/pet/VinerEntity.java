package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidType;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Optional;

/**
 * Viner: a kid-drawn Plant / Water flower-vine companion with a blue water-core blossom head,
 * golden petals, leafy stem, and long hooked vine arms.
 * Amphibious (cannot drown) and unleashes Tidal Vine Lash on melee hits (Slowness + self-heal
 * on a 60-tick cooldown).
 * Elements: {@link PetElement#NATURE} / {@link PetElement#WATER}.
 */
public class VinerEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.viner.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.viner.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.viner.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.viner.attack");

    private static final int TIDAL_VINE_COOLDOWN_TICKS = 60;
    private int tidalVineCooldown;

    public VinerEntity(EntityType<? extends VinerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.29)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5);
    }

    /**
     * Spawns in Crystal Canopy Woods on Crystal Turf, or in Overworld biomes near water
     * (within 4 blocks horizontally, -2..1 vertically) on animal-spawnable ground, mud, moss, or clay.
     */
    public static boolean checkVinerSpawnRules(
            EntityType<? extends VinerEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(ArtilianBlocks.CRYSTAL_TURF.get())) {
            return true;
        }
        boolean validGround = below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(Blocks.MUD)
                || below.is(Blocks.MOSS_BLOCK)
                || below.is(Blocks.CLAY);
        if (!validGround) {
            return false;
        }
        BlockPos.MutableBlockPos check = new BlockPos.MutableBlockPos();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy = -2; dy <= 1; dy++) {
                    check.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (level.getFluidState(check).is(FluidTags.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.NATURE;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.WATER);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AXOLOTL_IDLE_WATER;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AXOLOTL_HURT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.tidalVineCooldown > 0) {
            this.tidalVineCooldown--;
        }
        if (this.level().isClientSide) {
            if (this.isInWaterOrRain() && this.random.nextInt(5) == 0) {
                this.level().addParticle(
                        ParticleTypes.SPLASH,
                        this.getRandomX(0.7),
                        this.getY(0.6),
                        this.getRandomZ(0.7),
                        (this.random.nextDouble() - 0.5) * 0.05,
                        0.06,
                        (this.random.nextDouble() - 0.5) * 0.05);
            } else if (this.random.nextInt(14) == 0) {
                this.level().addParticle(
                        ParticleTypes.HAPPY_VILLAGER,
                        this.getRandomX(0.6),
                        this.getY(0.5 + this.random.nextDouble() * 0.5),
                        this.getRandomZ(0.6),
                        0.0,
                        0.03,
                        0.0);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living && this.tidalVineCooldown <= 0) {
            applyTidalVineLash(living);
            this.tidalVineCooldown = TIDAL_VINE_COOLDOWN_TICKS;
        }
        return hurt;
    }

    /** Signature perk: Tidal Vine Lash slows the struck enemy and heals Viner for 2.0 HP. */
    public void applyTidalVineLash(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        this.heal(2.0F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.PLAYER_SPLASH,
                    SoundSource.NEUTRAL,
                    0.85F,
                    1.1F);
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.VINE_PLACE,
                    SoundSource.NEUTRAL,
                    0.9F,
                    1.0F);
            serverLevel.sendParticles(
                    ParticleTypes.SPLASH,
                    target.getX(),
                    target.getY(0.6),
                    target.getZ(),
                    12,
                    0.35,
                    0.3,
                    0.35,
                    0.08);
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    target.getX(),
                    target.getY(0.7),
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
