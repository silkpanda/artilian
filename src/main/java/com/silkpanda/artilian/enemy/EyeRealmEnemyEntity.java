package com.silkpanda.artilian.enemy;

import com.silkpanda.artilian.pet.ElementalMob;
import com.silkpanda.artilian.safety.KidSafeTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Shared base for Phase 4 Stage 3 Eye Realm hostile mobs ({@code artilian:eye_realm_enemies}).
 * Carries elemental typings, GeckoLib idle/walk/attack animations, and kid-safe targeting.
 */
public abstract class EyeRealmEnemyEntity extends Monster implements GeoEntity, ElementalMob {
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private final RawAnimation idleAnim;
    private final RawAnimation walkAnim;
    private final RawAnimation attackAnim;

    protected EyeRealmEnemyEntity(EntityType<? extends EyeRealmEnemyEntity> type, Level level, String animationKey) {
        super(type, level);
        this.idleAnim = RawAnimation.begin().thenLoop("animation." + animationKey + ".idle");
        this.walkAnim = RawAnimation.begin().thenLoop("animation." + animationKey + ".walk");
        this.attackAnim = RawAnimation.begin().thenPlay("animation." + animationKey + ".attack");
        this.xpReward = 10;
    }

    protected static AttributeSupplier.Builder baseAttributes(
            double maxHealth, double movementSpeed, double attackDamage, double armor, double knockbackResistance) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.MOVEMENT_SPEED, movementSpeed)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance);
    }

    public static boolean checkEyeRealmSpawnRules(
            EntityType<? extends Monster> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this, Player.class, true, target -> KidSafeTargets.isValidOffensiveTarget(this, target)));
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
                        8,
                        0.25,
                        0.25,
                        0.25,
                        0.04);
                onElementalHit(livingTarget, serverLevel);
            }
        }
        return hit;
    }

    /** Subclass hook for mild, kid-readable elemental hit feedback. */
    protected void onElementalHit(LivingEntity target, ServerLevel level) {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4,
                state -> state.setAndContinue(state.isMoving() ? this.walkAnim : this.idleAnim)));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", this.attackAnim));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }
}
