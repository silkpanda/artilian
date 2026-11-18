package com.silkpanda.artilian.boss;

import com.silkpanda.artilian.pet.ElementalMob;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Shared base for Artilian bosses. Adds a range-managed boss bar, persistence, and arena safety
 * to the structure-enemy pattern: elemental typing, kid-readable GeckoLib animations, and no
 * block-breaking behaviour of any kind.
 */
public abstract class ArtilianBossEntity extends Monster implements GeoEntity, ElementalMob {
    /** Players inside this horizontal-and-vertical radius see the boss bar. */
    public static final double BOSS_BAR_RANGE = 48.0;

    /** Minimum rest between telegraphed attacks, in ticks. */
    public static final int DEFAULT_ATTACK_COOLDOWN = 60;

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private final RawAnimation idleAnim;
    private final RawAnimation walkAnim;
    private final RawAnimation attackAnim;
    /** Reserved for telegraphs; registered as a triggerable animation. */
    private final RawAnimation telegraphAnim;
    private final ServerBossEvent bossEvent;

    @Nullable
    private TelegraphedAttack activeAttack;
    private int telegraphTicks;
    private Vec3 telegraphCenter = Vec3.ZERO;
    private int attackCooldown;

    protected ArtilianBossEntity(EntityType<? extends ArtilianBossEntity> type, Level level, String animationKey) {
        super(type, level);
        this.idleAnim = RawAnimation.begin().thenLoop("animation." + animationKey + ".idle");
        this.walkAnim = RawAnimation.begin().thenLoop("animation." + animationKey + ".walk");
        this.attackAnim = RawAnimation.begin().thenPlay("animation." + animationKey + ".attack");
        this.telegraphAnim = RawAnimation.begin().thenPlay("animation." + animationKey + ".telegraph");
        this.bossEvent = new ServerBossEvent(
                this.getDisplayName(), bossBarColor(), BossEvent.BossBarOverlay.NOTCHED_10);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.xpReward = 50;
    }

    /** Bar colour supplied by the boss; red unless a subclass picks something else. */
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.RED;
    }

    public ServerBossEvent getBossEvent() {
        return this.bossEvent;
    }

    /** Bosses never despawn just because the player wandered off. */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData group) {
        this.setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, spawnType, group);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        // The range check below owns the boss bar; tracking is not the same as being in range.
        super.startSeenByPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        // The range check below owns the boss bar; logging out is handled on the next update.
        super.stopSeenByPlayer(player);
    }

    /** True when the player is alive, non-spectating, in this level, and inside boss-bar range. */
    public boolean isInBossBarRange(ServerPlayer player) {
        return player.isAlive()
                && !player.isSpectator()
                && player.level() == this.level()
                && this.distanceToSqr(player) <= BOSS_BAR_RANGE * BOSS_BAR_RANGE;
    }

    /** Adds in-range players and drops everyone else, including players who left the level. */
    public void updateBossBarPlayers() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (ServerPlayer player : serverLevel.players()) {
            if (this.isInBossBarRange(player)) {
                this.bossEvent.addPlayer(player);
            } else {
                this.bossEvent.removePlayer(player);
            }
        }
        for (ServerPlayer player : new ArrayList<>(this.bossEvent.getPlayers())) {
            if (!this.isInBossBarRange(player)) {
                this.bossEvent.removePlayer(player);
            }
        }
    }

    /** Mirrors current health into the bar so kids can see the fight's progress. */
    public void updateBossBarProgress() {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    /** Attacks this boss can start. Subclasses supply their own; the base has none. */
    protected List<TelegraphedAttack> telegraphedAttacks() {
        return List.of();
    }

    public boolean isTelegraphing() {
        return this.activeAttack != null;
    }

    /** Starts one attack: freeze in place, draw the ring, and begin the climbing bell. */
    public boolean startTelegraph(TelegraphedAttack attack) {
        if (this.isTelegraphing()) {
            return false;
        }
        this.activeAttack = attack;
        this.telegraphTicks = 0;
        this.telegraphCenter = this.position();
        this.getNavigation().stop();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0.0, motion.y, 0.0);
        this.triggerAnim("action", "telegraph");
        this.playBell(BossTelegraphs.PITCH_START);
        return true;
    }

    /** Advances the current telegraph by one tick. No-op when no attack is running. */
    public void tickTelegraph() {
        TelegraphedAttack attack = this.activeAttack;
        if (attack == null) {
            return;
        }
        this.telegraphTicks++;
        if (this.level() instanceof ServerLevel serverLevel) {
            BossTelegraphs.tick(serverLevel, this.telegraphCenter, attack.radius(),
                    this.telegraphTicks, attack.windupTicks());
        }
        this.getNavigation().stop();
        if (this.telegraphTicks >= attack.windupTicks()) {
            this.impact(attack);
            this.activeAttack = null;
            this.attackCooldown = DEFAULT_ATTACK_COOLDOWN;
        }
    }

    private void playBell(float pitch) {
        BossTelegraphs.bell(this.level(), this.getX(), this.getY(), this.getZ(), pitch);
    }

    /**
     * Deals elemental damage inside the ring. Direct damage only: bosses never interact with blocks,
     * whatever the mobGriefing rule says.
     */
    protected void impact(TelegraphedAttack attack) {
        Vec3 center = this.telegraphCenter;
        AABB area = new AABB(center, center).inflate(attack.radius(), 2.0, attack.radius());
        double radiusSqr = attack.radius() * attack.radius();
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == this || target instanceof ArtilianBossEntity
                    || target.isSpectator() || (target instanceof Player player && player.isCreative())) {
                continue;
            }
            double dx = target.getX() - center.x;
            double dz = target.getZ() - center.z;
            if (dx * dx + dz * dz > radiusSqr) {
                continue;
            }
            target.hurt(this.damageSources().mobAttack(this),
                    attack.damage() * attack.element().multiplierAgainst(target));
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(attack.element().particle(), center.x, center.y + 0.1, center.z,
                    24, 0.5, 0.1, 0.5, 0.05);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % 10 == 0) {
            this.updateBossBarPlayers();
        }
        this.updateBossBarProgress();

        if (this.isTelegraphing()) {
            this.tickTelegraph();
        } else if (this.attackCooldown > 0) {
            this.attackCooldown--;
        } else {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                for (TelegraphedAttack attack : this.telegraphedAttacks()) {
                    double range = attack.radius() + 1.0;
                    if (this.distanceToSqr(target) <= range * range) {
                        this.startTelegraph(attack);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public void die(DamageSource source) {
        this.bossEvent.removeAllPlayers();
        super.die(source);
    }

    protected static AttributeSupplier.Builder baseAttributes(
            double maxHealth, double movementSpeed, double attackDamage, double armor) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.MOVEMENT_SPEED, movementSpeed)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new HoldStillWhileTelegraphingGoal());
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
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

    /** Locks movement, jumping and looking for the whole windup so the red ring stays put. */
    private class HoldStillWhileTelegraphingGoal extends Goal {
        HoldStillWhileTelegraphingGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return ArtilianBossEntity.this.isTelegraphing();
        }

        @Override
        public void tick() {
            ArtilianBossEntity.this.getNavigation().stop();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4,
                state -> state.setAndContinue(state.isMoving() ? this.walkAnim : this.idleAnim)));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", this.attackAnim)
                .triggerableAnim("telegraph", this.telegraphAnim));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }
}
