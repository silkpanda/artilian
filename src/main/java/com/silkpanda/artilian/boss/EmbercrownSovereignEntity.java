package com.silkpanda.artilian.boss;

import com.silkpanda.artilian.pet.PetElement;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Phase 5 Dragon Jungle apex boss: Embercrown Sovereign. A fire-immune Dragon/Fire sovereign
 * with three telegraphed attacks and a half-health Primal Fury phase. Never breaks blocks.
 */
public class EmbercrownSovereignEntity extends ArtilianBossEntity {
    public static final TelegraphedAttack EMBER_BREATH =
            new TelegraphedAttack("ember_breath", 40, 4.2, 8.0F, PetElement.FIRE);
    public static final TelegraphedAttack SOVEREIGN_ROAR =
            new TelegraphedAttack("sovereign_roar", 35, 5.5, 7.5F, PetElement.DRAGON);
    public static final TelegraphedAttack METEOR_SLAM =
            new TelegraphedAttack("meteor_slam", 50, 7.5, 10.5F, PetElement.FIRE);

    private static final EntityDataAccessor<Boolean> PRIMAL_FURY =
            SynchedEntityData.defineId(EmbercrownSovereignEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int FURY_COOLDOWN = 40;

    private int attackCycle = 0;

    public EmbercrownSovereignEntity(EntityType<? extends EmbercrownSovereignEntity> type, Level level) {
        super(type, level, "embercrown_sovereign");
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(300.0, 0.26, 9.0, 11.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PRIMAL_FURY, false);
    }

    public boolean isPrimalFury() {
        return this.entityData.get(PRIMAL_FURY);
    }

    /** Enters Phase 2 Primal Fury once health drops to or below half. */
    public void checkPrimalFury() {
        if (this.isPrimalFury() || !this.isAlive() || this.getHealth() > this.getMaxHealth() * 0.5F) {
            return;
        }
        this.entityData.set(PRIMAL_FURY, true);
        this.getBossEvent().setColor(BossEvent.BossBarColor.RED);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.2F, 0.85F);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 1.8F, 0.7F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.55), this.getZ(),
                    45, 1.4, 1.4, 1.4, 0.1);
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY(0.55), this.getZ(),
                    40, 1.4, 1.4, 1.4, 0.08);
        }
        Component alert = Component.translatable("message.artilian.embercrown_sovereign.primal_fury");
        for (ServerPlayer player : this.getBossEvent().getPlayers()) {
            player.displayClientMessage(alert, true);
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 motion = this.getDeltaMovement();
        if (!this.onGround() && motion.y < 0.0) {
            this.setDeltaMovement(motion.x, motion.y * 0.65, motion.z);
        }
    }

    @Override
    protected void customServerAiStep() {
        this.checkPrimalFury();
        super.customServerAiStep();
    }

    @Override
    protected List<TelegraphedAttack> telegraphedAttacks() {
        return this.isPrimalFury()
                ? List.of(EMBER_BREATH, SOVEREIGN_ROAR, METEOR_SLAM)
                : List.of(EMBER_BREATH, SOVEREIGN_ROAR);
    }

    @Override
    public int attackCooldownTicks() {
        return this.isPrimalFury() ? FURY_COOLDOWN : DEFAULT_ATTACK_COOLDOWN;
    }

    @Override
    @Nullable
    public TelegraphedAttack chooseTelegraphedAttack(LivingEntity target) {
        double distSqr = this.distanceToSqr(target);
        if (this.isPrimalFury() && distSqr <= 8.5 * 8.5 && (this.attackCycle % 3 == 2)) {
            this.attackCycle++;
            return METEOR_SLAM;
        }
        if (distSqr > 5.5 * 5.5 && distSqr <= 16.0 * 16.0) {
            this.attackCycle++;
            return EMBER_BREATH;
        }
        if (distSqr <= 6.5 * 6.5) {
            TelegraphedAttack pick = (this.attackCycle % 2 == 0) ? SOVEREIGN_ROAR : EMBER_BREATH;
            this.attackCycle++;
            return pick;
        }
        return null;
    }

    @Override
    public Vec3 telegraphCenterFor(TelegraphedAttack attack, LivingEntity target) {
        return attack == EMBER_BREATH ? target.position() : this.position();
    }

    @Override
    protected void onTelegraphImpact(TelegraphedAttack attack, Vec3 center, List<LivingEntity> hitTargets) {
        if (attack == EMBER_BREATH) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 1.8F, 0.9F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.6, center.z,
                        32, 1.2, 0.6, 1.2, 0.06);
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 0.6, center.z,
                        24, 1.2, 0.6, 1.2, 0.04);
            }
            for (LivingEntity hit : hitTargets) {
                hit.igniteForSeconds(3.0F);
            }
        } else if (attack == SOVEREIGN_ROAR) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0F, 0.85F);
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 24; i++) {
                    double angle = i * (Math.PI * 2.0 / 24);
                    double px = center.x + Math.cos(angle) * 4.5;
                    double pz = center.z + Math.sin(angle) * 4.5;
                    serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, px, center.y + 0.4, pz,
                            2, 0.15, 0.15, 0.15, 0.03);
                }
            }
            for (LivingEntity hit : hitTargets) {
                pushOutward(hit, center, 0.55, 0.25);
                hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            }
        } else if (attack == METEOR_SLAM) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.8F, 0.8F);
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0F, 0.75F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 0.5, center.z,
                        1, 0.0, 0.0, 0.0, 0.0);
                for (int i = 0; i < 36; i++) {
                    double angle = i * (Math.PI * 2.0 / 36);
                    double px = center.x + Math.cos(angle) * 6.5;
                    double pz = center.z + Math.sin(angle) * 6.5;
                    serverLevel.sendParticles(ParticleTypes.FLAME, px, center.y + 0.35, pz,
                            3, 0.2, 0.2, 0.2, 0.05);
                    serverLevel.sendParticles(ParticleTypes.LAVA, px, center.y + 0.35, pz,
                            1, 0.15, 0.15, 0.15, 0.02);
                }
            }
            for (LivingEntity hit : hitTargets) {
                pushOutward(hit, center, 0.7, 0.32);
                hit.igniteForSeconds(4.0F);
            }
        }
    }

    private static void pushOutward(LivingEntity target, Vec3 center, double horizontalSpeed, double verticalSpeed) {
        double dx = target.getX() - center.x;
        double dz = target.getZ() - center.z;
        double dist = Math.hypot(dx, dz);
        if (dist < 1.0E-4) {
            dx = 0.0;
            dz = 1.0;
        } else {
            dx /= dist;
            dz /= dist;
        }
        target.setDeltaMovement(target.getDeltaMovement().add(dx * horizontalSpeed, verticalSpeed, dz * horizontalSpeed));
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    @Override
    protected void onElementalHit(LivingEntity target, ServerLevel level) {
        target.igniteForSeconds(2.5F);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.DRAGON;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.FIRE);
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.YELLOW;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_GROWL;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("PrimalFury", this.isPrimalFury());
        tag.putInt("AttackCycle", this.attackCycle);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        boolean primalFury = tag.getBoolean("PrimalFury");
        this.entityData.set(PRIMAL_FURY, primalFury);
        this.attackCycle = tag.getInt("AttackCycle");
        this.getBossEvent().setColor(primalFury ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.YELLOW);
    }
}
