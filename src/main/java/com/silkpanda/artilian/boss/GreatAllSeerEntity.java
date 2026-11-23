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
 * Phase 4 Eye Realm boss: The Great All-Seer. A hovering Shadow/Starlight ocular sovereign with
 * three telegraphed attacks and a half-health All-Seeing Overdrive phase. Never breaks blocks.
 */
public class GreatAllSeerEntity extends ArtilianBossEntity {
    public static final TelegraphedAttack GAZING_BEAM =
            new TelegraphedAttack("gazing_beam", 45, 3.2, 7.5F, PetElement.STARLIGHT);
    public static final TelegraphedAttack TWILIGHT_PULSE =
            new TelegraphedAttack("twilight_pulse", 40, 5.0, 7.0F, PetElement.SHADOW);
    public static final TelegraphedAttack ALL_SEEING_NOVA =
            new TelegraphedAttack("all_seeing_nova", 50, 7.5, 9.5F, PetElement.SHADOW);

    private static final EntityDataAccessor<Boolean> ALL_SEEING =
            SynchedEntityData.defineId(GreatAllSeerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int OVERDRIVE_COOLDOWN = 42;

    private int attackCycle = 0;

    public GreatAllSeerEntity(EntityType<? extends GreatAllSeerEntity> type, Level level) {
        super(type, level, "great_all_seer");
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(240.0, 0.23, 7.5, 9.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ALL_SEEING, false);
    }

    public boolean isAllSeeing() {
        return this.entityData.get(ALL_SEEING);
    }

    /** Enters Phase 2 All-Seeing Overdrive once health drops to or below half. */
    public void checkAllSeeing() {
        if (this.isAllSeeing() || !this.isAlive() || this.getHealth() > this.getMaxHealth() * 0.5F) {
            return;
        }
        this.entityData.set(ALL_SEEING, true);
        this.getBossEvent().setColor(BossEvent.BossBarColor.PURPLE);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_EYE_DEATH, SoundSource.HOSTILE, 2.0F, 0.75F);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5F, 1.15F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.55), this.getZ(),
                    36, 1.0, 1.2, 1.0, 0.08);
            serverLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY(0.55), this.getZ(),
                    40, 1.0, 1.2, 1.0, 0.2);
        }
        Component alert = Component.translatable("message.artilian.great_all_seer.all_seeing");
        for (ServerPlayer player : this.getBossEvent().getPlayers()) {
            player.displayClientMessage(alert, true);
        }
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
        this.checkAllSeeing();
        super.customServerAiStep();
    }

    @Override
    protected List<TelegraphedAttack> telegraphedAttacks() {
        return this.isAllSeeing()
                ? List.of(GAZING_BEAM, TWILIGHT_PULSE, ALL_SEEING_NOVA)
                : List.of(GAZING_BEAM, TWILIGHT_PULSE);
    }

    @Override
    public int attackCooldownTicks() {
        return this.isAllSeeing() ? OVERDRIVE_COOLDOWN : DEFAULT_ATTACK_COOLDOWN;
    }

    @Override
    @Nullable
    public TelegraphedAttack chooseTelegraphedAttack(LivingEntity target) {
        double distSqr = this.distanceToSqr(target);
        if (this.isAllSeeing() && distSqr <= 8.5 * 8.5 && (this.attackCycle % 3 == 2)) {
            this.attackCycle++;
            return ALL_SEEING_NOVA;
        }
        if (distSqr > 5.5 * 5.5 && distSqr <= 16.0 * 16.0) {
            this.attackCycle++;
            return GAZING_BEAM;
        }
        if (distSqr <= 6.0 * 6.0) {
            TelegraphedAttack pick = (this.attackCycle % 2 == 0) ? TWILIGHT_PULSE : GAZING_BEAM;
            this.attackCycle++;
            return pick;
        }
        return null;
    }

    @Override
    public Vec3 telegraphCenterFor(TelegraphedAttack attack, LivingEntity target) {
        return attack == GAZING_BEAM ? target.position() : this.position();
    }

    @Override
    protected void onTelegraphImpact(TelegraphedAttack attack, Vec3 center, List<LivingEntity> hitTargets) {
        if (attack == GAZING_BEAM) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.8F, 1.35F);
            if (this.level() instanceof ServerLevel serverLevel) {
                for (double dy = 0.2; dy <= 5.5 + 1.0E-4; dy += 0.5) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, center.x, center.y + dy, center.z,
                            4, 0.25, 0.1, 0.25, 0.02);
                    serverLevel.sendParticles(ParticleTypes.GLOW, center.x, center.y + dy, center.z,
                            3, 0.25, 0.1, 0.25, 0.02);
                }
            }
            for (LivingEntity hit : hitTargets) {
                hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            }
        } else if (attack == TWILIGHT_PULSE) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.ENDER_EYE_LAUNCH, SoundSource.HOSTILE, 1.6F, 0.75F);
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 1.2F, 0.8F);
            for (LivingEntity hit : hitTargets) {
                pushOutward(hit, center, 0.5, 0.25);
                hit.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
                hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
            }
        } else if (attack == ALL_SEEING_NOVA) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.ENDER_EYE_DEATH, SoundSource.HOSTILE, 2.0F, 0.85F);
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 1.5F, 0.7F);
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 36; i++) {
                    double angle = i * (Math.PI * 2.0 / 36);
                    double px = center.x + Math.cos(angle) * 6.5;
                    double pz = center.z + Math.sin(angle) * 6.5;
                    serverLevel.sendParticles(ParticleTypes.END_ROD, px, center.y + 0.35, pz,
                            2, 0.15, 0.15, 0.15, 0.02);
                    serverLevel.sendParticles(ParticleTypes.PORTAL, px, center.y + 0.35, pz,
                            3, 0.2, 0.2, 0.2, 0.05);
                }
            }
            for (LivingEntity hit : hitTargets) {
                pushOutward(hit, center, 0.65, 0.3);
                hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
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
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.SHADOW;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.STARLIGHT);
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.PINK;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_EYE_LAUNCH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SHULKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_EYE_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("AllSeeing", this.isAllSeeing());
        tag.putInt("AttackCycle", this.attackCycle);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        boolean allSeeing = tag.getBoolean("AllSeeing");
        this.entityData.set(ALL_SEEING, allSeeing);
        this.attackCycle = tag.getInt("AttackCycle");
        this.getBossEvent().setColor(allSeeing ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.PINK);
    }
}
