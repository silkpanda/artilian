package com.silkpanda.artilian.pet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.pathfinder.PathType;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Optional;

/**
 * Steve's Fire/Rock Creature (working name: Cinder Crag): a squat crimson-stone pet with
 * fiery eyes and mouth in its middle body, topped by three flame spires, spawning anywhere
 * near lava. Elements: {@link PetElement#FIRE} / {@link PetElement#EARTH}.
 */
public class CinderCragEntity extends ArtilianPetEntity {
    public static final int LAVA_SEARCH_RADIUS_XZ = 6;
    public static final int LAVA_SEARCH_MIN_Y = -2;
    public static final int LAVA_SEARCH_MAX_Y = 3;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.cinder_crag.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.cinder_crag.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.cinder_crag.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.cinder_crag.attack");

    public CinderCragEntity(EntityType<? extends CinderCragEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    /** Spawns on solid non-fluid ground wherever lava is nearby (surface pools, caves, or Nether). */
    public static boolean checkLavaSpawnRules(
            EntityType<? extends CinderCragEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) {
            return false;
        }
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (below.is(BlockTags.LEAVES) || !below.isFaceSturdy(level, belowPos, Direction.UP)) {
            return false;
        }
        return hasNearbyLava(level, pos);
    }

    public static boolean hasNearbyLava(LevelAccessor level, BlockPos center) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -LAVA_SEARCH_RADIUS_XZ; dx <= LAVA_SEARCH_RADIUS_XZ; dx++) {
            for (int dz = -LAVA_SEARCH_RADIUS_XZ; dz <= LAVA_SEARCH_RADIUS_XZ; dz++) {
                for (int dy = LAVA_SEARCH_MIN_Y; dy <= LAVA_SEARCH_MAX_Y; dy++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (level.getFluidState(cursor).is(FluidTags.LAVA)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.FIRE;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.EARTH);
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.FIRECHARGE_USE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAMPFIRE_CRACKLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.STONE_HIT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide && this.random.nextInt(5) == 0) {
            this.level().addParticle(
                    ParticleTypes.SMALL_FLAME,
                    this.getRandomX(0.32),
                    this.getY() + 1.08 + this.random.nextDouble() * 0.15,
                    this.getRandomZ(0.32),
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
