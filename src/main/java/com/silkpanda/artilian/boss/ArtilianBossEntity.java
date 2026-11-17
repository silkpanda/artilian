package com.silkpanda.artilian.boss;

import com.silkpanda.artilian.pet.ElementalMob;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
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

import java.util.ArrayList;

/**
 * Shared base for Artilian bosses. Adds a range-managed boss bar, persistence, and arena safety
 * to the structure-enemy pattern: elemental typing, kid-readable GeckoLib animations, and no
 * block-breaking behaviour of any kind.
 */
public abstract class ArtilianBossEntity extends Monster implements GeoEntity, ElementalMob {
    /** Players inside this horizontal-and-vertical radius see the boss bar. */
    public static final double BOSS_BAR_RANGE = 48.0;

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private final RawAnimation idleAnim;
    private final RawAnimation walkAnim;
    private final RawAnimation attackAnim;
    /** Reserved for Stage 2 telegraphs; registered as a triggerable animation now. */
    private final RawAnimation telegraphAnim;
    private final ServerBossEvent bossEvent;

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

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % 10 == 0) {
            this.updateBossBarPlayers();
        }
        this.updateBossBarProgress();
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
