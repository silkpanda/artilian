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
 * First full custom boss of Adventures in Artilian: an armored Crystal/Starlight titan with three
 * telegraphed attacks and a half-health Crystal Overcharge phase. Never breaks blocks.
 */
public class CrystalColossusEntity extends ArtilianBossEntity {
    public static final TelegraphedAttack CRYSTAL_QUAKE =
            new TelegraphedAttack("crystal_quake", 40, 4.5, 7.0F, PetElement.CRYSTAL);
    public static final TelegraphedAttack STARLIGHT_SPIRE =
            new TelegraphedAttack("starlight_spire", 45, 3.0, 6.5F, PetElement.STARLIGHT);
    public static final TelegraphedAttack PRISM_NOVA =
            new TelegraphedAttack("prism_nova", 50, 7.0, 8.5F, PetElement.CRYSTAL);

    private static final EntityDataAccessor<Boolean> OVERCHARGED =
            SynchedEntityData.defineId(CrystalColossusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int OVERCHARGED_COOLDOWN = 45;

    private int attackCycle = 0;

    public CrystalColossusEntity(EntityType<? extends CrystalColossusEntity> type, Level level) {
        super(type, level, "crystal_colossus");
        this.xpReward = 120;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(200.0, 0.21, 6.0, 8.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OVERCHARGED, false);
    }

    public boolean isOvercharged() {
        return this.entityData.get(OVERCHARGED);
    }

    /** Enters Phase 2 Crystal Overcharge once health drops to or below half. */
    public void checkOvercharge() {
        if (this.isOvercharged() || !this.isAlive() || this.getHealth() > this.getMaxHealth() * 0.5F) {
            return;
        }
        this.entityData.set(OVERCHARGED, true);
        this.getBossEvent().setColor(BossEvent.BossBarColor.PURPLE);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.0F, 0.85F);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.4F, 1.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.55), this.getZ(),
                    32, 0.9, 1.1, 0.9, 0.08);
            serverLevel.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY(0.55), this.getZ(),
                    24, 0.9, 1.1, 0.9, 0.05);
        }
        Component alert = Component.translatable("message.artilian.crystal_colossus.overcharged");
        for (ServerPlayer player : this.getBossEvent().getPlayers()) {
            player.displayClientMessage(alert, true);
        }
    }

    @Override
    protected void customServerAiStep() {
        this.checkOvercharge();
        super.customServerAiStep();
    }

    @Override
    protected List<TelegraphedAttack> telegraphedAttacks() {
        return this.isOvercharged()
                ? List.of(CRYSTAL_QUAKE, STARLIGHT_SPIRE, PRISM_NOVA)
                : List.of(CRYSTAL_QUAKE, STARLIGHT_SPIRE);
    }

    @Override
    public int attackCooldownTicks() {
        return this.isOvercharged() ? OVERCHARGED_COOLDOWN : DEFAULT_ATTACK_COOLDOWN;
    }

    @Override
    @Nullable
    public TelegraphedAttack chooseTelegraphedAttack(LivingEntity target) {
        double distSqr = this.distanceToSqr(target);
        if (this.isOvercharged() && distSqr <= 8.0 * 8.0 && (this.attackCycle % 3 == 2)) {
            this.attackCycle++;
            return PRISM_NOVA;
        }
        if (distSqr > 5.0 * 5.0 && distSqr <= 14.0 * 14.0) {
            this.attackCycle++;
            return STARLIGHT_SPIRE;
        }
        if (distSqr <= 5.5 * 5.5) {
            TelegraphedAttack pick = (this.attackCycle % 2 == 0) ? CRYSTAL_QUAKE : STARLIGHT_SPIRE;
            this.attackCycle++;
            return pick;
        }
        return null;
    }

    @Override
    public Vec3 telegraphCenterFor(TelegraphedAttack attack, LivingEntity target) {
        return attack == STARLIGHT_SPIRE ? target.position() : this.position();
    }

    @Override
    protected void onTelegraphImpact(TelegraphedAttack attack, Vec3 center, List<LivingEntity> hitTargets) {
        if (attack == CRYSTAL_QUAKE) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 1.6F, 0.7F);
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 1.2F, 0.8F);
            for (LivingEntity hit : hitTargets) {
                pushOutward(hit, center, 0.45, 0.25);
                hit.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
            }
        } else if (attack == STARLIGHT_SPIRE) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.8F, 1.25F);
            if (this.level() instanceof ServerLevel serverLevel) {
                for (double dy = 0.2; dy <= 4.2 + 1.0E-4; dy += 0.5) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, center.x, center.y + dy, center.z,
                            3, 0.25, 0.1, 0.25, 0.02);
                    serverLevel.sendParticles(ParticleTypes.GLOW, center.x, center.y + dy, center.z,
                            3, 0.25, 0.1, 0.25, 0.02);
                }
            }
            for (LivingEntity hit : hitTargets) {
                hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            }
        } else if (attack == PRISM_NOVA) {
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.0F, 0.95F);
            this.level().playSound(null, center.x, center.y, center.z,
                    SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 1.5F, 0.7F);
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 32; i++) {
                    double angle = i * (Math.PI * 2.0 / 32);
                    double px = center.x + Math.cos(angle) * 6.0;
                    double pz = center.z + Math.sin(angle) * 6.0;
                    serverLevel.sendParticles(ParticleTypes.END_ROD, px, center.y + 0.3, pz,
                            2, 0.15, 0.15, 0.15, 0.02);
                    serverLevel.sendParticles(ParticleTypes.GLOW, px, center.y + 0.3, pz,
                            2, 0.15, 0.15, 0.15, 0.02);
                }
            }
            for (LivingEntity hit : hitTargets) {
                pushOutward(hit, center, 0.55, 0.28);
                hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
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
        return PetElement.CRYSTAL;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.of(PetElement.STARLIGHT);
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.BLUE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_CLUSTER_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Overcharged", this.isOvercharged());
        tag.putInt("AttackCycle", this.attackCycle);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        boolean overcharged = tag.getBoolean("Overcharged");
        this.entityData.set(OVERCHARGED, overcharged);
        this.attackCycle = tag.getInt("AttackCycle");
        this.getBossEvent().setColor(overcharged ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.BLUE);
    }
}
