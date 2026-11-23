package com.silkpanda.artilian.dragon;

import com.silkpanda.artilian.dimension.DragonJungleDimension;
import com.silkpanda.artilian.pet.ElementalMob;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.safety.KidSafeTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Shared base for Phase 5 Stage 4–5 wild dragons ({@code artilian:wild_dragons}).
 *
 * <p>Uses the shared GeckoLib {@code dragon_rig} animation clips ({@code idle}, {@code walk},
 * {@code fly}, {@code attack}), enforces {@link KidSafeTargets} so Chester and player pets are
 * never targeted or harmed, and grants winged slow-fall safety.
 */
public abstract class WildDragonEntity extends Monster implements GeoEntity, ElementalMob {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.dragon_rig.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.dragon_rig.walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("animation.dragon_rig.fly");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.dragon_rig.attack");

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    protected WildDragonEntity(EntityType<? extends WildDragonEntity> type, Level level) {
        super(type, level);
        this.xpReward = 18;
    }

    protected static AttributeSupplier.Builder baseDragonAttributes(
            double maxHealth, double movementSpeed, double attackDamage, double armor, double knockbackResistance) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.MOVEMENT_SPEED, movementSpeed)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance);
    }

    /**
     * Allows wild dragons to spawn naturally across the Dragon Jungle at any time of day
     * on Jungle Turf, Moss Block, Primal Basalt, or Dragonwood Leaves (except in Peaceful).
     */
    public static boolean checkWildDragonSpawnRules(
            EntityType<? extends Monster> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(ArtilianBlocks.JUNGLE_TURF.get())
                || below.is(Blocks.MOSS_BLOCK)
                || below.is(ArtilianBlocks.PRIMAL_BASALT.get())
                || below.is(ArtilianBlocks.DRAGONWOOD_LEAVES.get())
                || below.isValidSpawn(level, pos.below(), type);
        boolean inDragonJungle = level.getLevel().dimension().equals(DragonJungleDimension.DRAGON_JUNGLE_LEVEL);
        return validGround && (inDragonJungle || checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this, Player.class, true, target -> KidSafeTargets.isValidOffensiveTarget(this, target)));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Winged dragons glide safely instead of taking fall damage
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.onGround() && this.getDeltaMovement().y < -0.1) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.72, 1.0));
            this.hasImpulse = true;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity living && !KidSafeTargets.isValidOffensiveTarget(this, living)) {
            return false;
        }
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            this.triggerAnim("action", "attack");
            if (this.level() instanceof ServerLevel serverLevel && target instanceof LivingEntity livingTarget) {
                serverLevel.sendParticles(
                        this.getPrimaryElement().particle(),
                        livingTarget.getX(),
                        livingTarget.getY(0.6),
                        livingTarget.getZ(),
                        10,
                        0.3,
                        0.3,
                        0.3,
                        0.05);
                onDragonHit(livingTarget, serverLevel);
            }
        }
        return hit;
    }

    /** Subclass hook for species-specific elemental hit effects. */
    protected void onDragonHit(LivingEntity target, ServerLevel level) {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (!this.onGround()) {
                return state.setAndContinue(FLY_ANIM);
            }
            return state.setAndContinue(state.isMoving() ? WALK_ANIM : IDLE_ANIM);
        }));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }
}
