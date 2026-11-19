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
 * Second full custom boss of Adventures in Artilian: a magma-forged Fire/Shadow pirate captain
 * with three telegraphed attacks and a half-health Brimstone Broadside phase. Never breaks blocks
 * or ignites terrain.
 */
public class CaptainBrimstoneEntity extends ArtilianBossEntity {
    public static final TelegraphedAttack POWDER_KEG_TOSS =
            new TelegraphedAttack("powder_keg_toss", 40, 3.5, 7.0F, PetElement.FIRE);
    public static final TelegraphedAttack CUTLASS_CLEAVE =
            new TelegraphedAttack("cutlass_cleave", 35, 4.0, 7.5F, PetElement.SHADOW);
    public static final TelegraphedAttack BROADSIDE_BARRAGE =
            new TelegraphedAttack("broadside_barrage", 50, 6.5, 8.5F, PetElement.FIRE);

    private static final EntityDataAccessor<Boolean> BROADSIDE =
            SynchedEntityData.defineId(CaptainBrimstoneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int BROADSIDE_COOLDOWN = 45;

    private int attackCycle = 0;

    public CaptainBrimstoneEntity(EntityType<? extends CaptainBrimstoneEntity> type, Level level) {
        super(type, level, "captain_brimstone");
        this.xpReward = 140;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(220.0, 0.23, 6.5, 6.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BROADSIDE, false);
    }

    public boolean isBroadsideActive() {
        return this.entityData.get(BROADSIDE);
    }

    /** Enters Phase 2 Brimstone Broadside once health drops to or below half. */
    public void checkBroadsidePhase() {
        if (this.isBroadsideActive() || !this.isAlive() || this.getHealth() > this.getMaxHealth() * 0.5F) {
            return;
        }
        this.entityData.set(BROADSIDE, true);
        this.getBossEvent().setColor(BossEvent.BossBarColor.YELLOW);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.75F);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.3F, 0.9F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(0.55), this.getZ(),
                    34, 0.9, 1.0, 0.9, 0.08);
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(0.55), this.getZ(),
                    22, 0.9, 1.0, 0.9, 0.05);
        }
        Component alert = Component.translatable("message.artilian.captain_brimstone.broadside");
        for (ServerPlayer player : this.getBossEvent().getPlayers()) {
            player.displayClientMessage(alert, true);
        }
    }

    @Override
    protected void customServerAiStep() {
        this.checkBroadsidePhase();
        super.customServerAiStep();
    }

    @Override
    protected List<TelegraphedAttack> telegraphedAttacks() {
        return this.isBroadsideActive()
                ? List.of(POWDER_KEG_TOSS, CUTLASS_CLEAVE, BROADSIDE_BARRAGE)
                : List.of(POWDER_KEG_TOSS, CUTLASS_CLEAVE);
    }

    @Override
    public int attackCooldownTicks() {
        return this.isBroadsideActive() ? BROADSIDE_COOLDOWN : DEFAULT_ATTACK_COOLDOWN;
    }

    @Override
    @Nullable
    public TelegraphedAttack chooseTelegraphedAttack(LivingEntity target) {
        double distSqr = this.distanceToSqr(target);
        if (this.isBroadsideActive() && distSqr <= 8.0 * 8.0 && (this.attackCycle % 3 == 2)) {
            this.attackCycle++;
            return BROADSIDE_BARRAGE;
        }
        if (distSqr > 4.5 * 4.5 && distSqr <= 14.0 * 14.0) {
            this.attackCycle++;
            return POWDER_KEG_TOSS;
        }
        if (distSqr <= 5.0 * 5.0) {
            TelegraphedAttack pick = (this.attackCycle % 2 == 0) ? CUTLASS_CLEAVE : POWDER_KEG_TOSS;
            this.attackCycle++;
            return pick;
        }
        return null;
    }

    @Override
    public Vec3 telegraphCenterFor(TelegraphedAttack attack, LivingEntity target) {
        return attack == POWDER_KEG_TOSS ? target.position() : this.position();
    }

    @Override
    protected void onTelegraphImpact(TelegraphedAttack attack, Vec3 center, List<LivingEntity> hitTargets) {
        if (attack == POWDER_KEG_TOSS) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 1.05F);
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.3F, 0.9F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.5, center.z,
                        28, 1.1, 0.5, 1.1, 0.06);
                serverLevel.sendParticles(ParticleTypes.LAVA, center.x, center.y + 0.4, center.z,
                        12, 0.8, 0.3, 0.8, 0.04);
            }
            for (LivingEntity hit : hitTargets) {
                pushOutward(hit, center, 0.4, 0.22);
                hit.igniteForTicks(30);
            }
        } else if (attack == CUTLASS_CLEAVE) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.8F, 0.75F);
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.2F, 0.85F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 1.2, center.z,
                        8, 1.6, 0.3, 1.6, 0.0);
                serverLevel.sendParticles(ParticleTypes.SMOKE, center.x, center.y + 0.8, center.z,
                        14, 1.2, 0.4, 1.2, 0.03);
            }
            for (LivingEntity hit : hitTargets) {
                pushOutward(hit, center, 0.55, 0.25);
                hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
            }
        } else if (attack == BROADSIDE_BARRAGE) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.75F);
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.6F, 0.8F);
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 32; i++) {
                    double angle = i * (Math.PI * 2.0 / 32);
                    double px = center.x + Math.cos(angle) * 5.5;
                    double pz = center.z + Math.sin(angle) * 5.5;
                    serverLevel.sendParticles(ParticleTypes.FLAME, px, center.y + 0.3, pz,
                            3, 0.2, 0.2, 0.2, 0.03);
                    serverLevel.sendParticles(ParticleTypes.SMOKE, px, center.y + 0.4, pz,
                            2, 0.2, 0.2, 0.2, 0.02);
                }
            }
            for (LivingEntity hit : hitTargets) {
                pushOutward(hit, center, 0.6, 0.28);
                hit.igniteForTicks(40);
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
        target.igniteForTicks(30);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0));
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.FIRE;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.SHADOW);
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.RED;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("BroadsideActive", this.isBroadsideActive());
        tag.putInt("AttackCycle", this.attackCycle);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        boolean broadside = tag.getBoolean("BroadsideActive");
        this.entityData.set(BROADSIDE, broadside);
        this.attackCycle = tag.getInt("AttackCycle");
        this.getBossEvent().setColor(broadside ? BossEvent.BossBarColor.YELLOW : BossEvent.BossBarColor.RED);
    }
}
