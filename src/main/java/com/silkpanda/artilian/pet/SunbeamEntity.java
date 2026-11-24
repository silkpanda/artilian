package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Optional;

/**
 * Sunbeam: the kids' legendary Sunsteel pet, a ghast-tall sun with wings and a flame streamer.
 * It hovers like Flurrywing and carries the sun aura: a movable light block at its chest plus
 * healing for its owner and the owner's other pets. Element: {@link PetElement#SUNSTEEL}.
 */
public class SunbeamEntity extends ArtilianPetEntity {
    /** Full-bright light level carried in the single block the sun aura places. */
    public static final int LIGHT_LEVEL = 15;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sunbeam.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sunbeam.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.sunbeam.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sunbeam.attack");

    private static final String KEY_PLACED_LIGHT = "SunbeamPlacedLight";

    /**
     * The one light block this Sunbeam placed itself. Saved with the entity so a Sunbeam unloaded or
     * shut down mid-glow still knows its old light after reload and clears it on its next move.
     */
    private @Nullable BlockPos placedLight;

    public SunbeamEntity(EntityType<? extends SunbeamEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    /** Spawns in Emberbloom Meadows on grass-like ground or Ember Turf. */
    public static boolean checkSunbeamSpawnRules(
            EntityType<? extends SunbeamEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(ArtilianBlocks.EMBER_TURF.get());
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Legs dangle: it glides down on its wings and never takes fall damage.
        return false;
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.SUNSTEEL;
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.onGround() && !this.isSitting() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.65, 1.0));
            this.hasImpulse = true;
        }
        if (this.level().isClientSide) {
            // Sunsteel sparks around the body, about once every 5 ticks.
            if (this.random.nextInt(5) == 0) {
                this.level().addParticle(
                        ParticleTypes.WAX_ON,
                        this.getRandomX(1.0),
                        this.getY(0.3 + this.random.nextDouble() * 0.6),
                        this.getRandomZ(1.0),
                        (this.random.nextDouble() - 0.5) * 0.02,
                        0.01,
                        (this.random.nextDouble() - 0.5) * 0.02);
            }
            // A flame drifting down off the streamer at the front, about once every 20 ticks.
            if (this.random.nextInt(20) == 0) {
                double radians = this.getYRot() * (Math.PI / 180.0F);
                this.level().addParticle(
                        ParticleTypes.FLAME,
                        this.getX() - Math.sin(radians) * 0.6,
                        this.getY(0.3),
                        this.getZ() + Math.cos(radians) * 0.6,
                        (this.random.nextDouble() - 0.5) * 0.01,
                        -0.06,
                        (this.random.nextDouble() - 0.5) * 0.01);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.updatePlacedLight();
            if (this.tickCount % 40 == 0) {
                this.applySunAura();
            }
        }
    }

    /** Moves the one light block to the chest position, placing only into air. */
    private void updatePlacedLight() {
        BlockPos target = BlockPos.containing(this.getX(), this.getY(0.6), this.getZ());
        if (target.equals(this.placedLight)) {
            return;
        }
        this.clearPlacedLight();
        if (this.level().getBlockState(target).isAir()) {
            this.level().setBlock(target, Blocks.LIGHT.defaultBlockState()
                    .setValue(LightBlock.LEVEL, LIGHT_LEVEL), 3);
            this.placedLight = target;
        }
    }

    /** Clears only a light block this Sunbeam placed, and forgets the position either way. */
    private void clearPlacedLight() {
        if (this.placedLight != null && this.level().getBlockState(this.placedLight).is(Blocks.LIGHT)) {
            this.level().setBlock(this.placedLight, Blocks.AIR.defaultBlockState(), 3);
        }
        this.placedLight = null;
    }

    /**
     * The sun aura's healing half. Tamed and standing Sunbeams give their owner Regeneration I and
     * heal their owner's other nearby Artilian pets. Never touches other players, their pets,
     * wild pets, or hostile mobs. Public so GameTests can call the rule directly.
     */
    public void applySunAura() {
        if (this.level().isClientSide || !this.isTamed() || this.isSitting()) {
            return;
        }
        Player owner = this.getOwnerInLevel();
        if (owner != null && this.distanceToSqr(owner) <= 64.0) {
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));
        }
        for (ArtilianPetEntity pet : this.level().getEntitiesOfClass(
                ArtilianPetEntity.class, this.getBoundingBox().inflate(8.0))) {
            if (pet != this && pet.isAlive() && this.getOwnerId().equals(pet.getOwnerId())) {
                pet.heal(1.0F);
            }
        }
    }

    public @Nullable BlockPos getPlacedLight() {
        return this.placedLight;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.placedLight != null) {
            tag.putLong(KEY_PLACED_LIGHT, this.placedLight.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.placedLight = tag.contains(KEY_PLACED_LIGHT) ? BlockPos.of(tag.getLong(KEY_PLACED_LIGHT)) : null;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide) {
            this.clearPlacedLight();
        }
        super.remove(reason);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (hit && this.level() instanceof ServerLevel serverLevel) {
            this.emitChestSunbeam(serverLevel, entity);
        }
        return hit;
    }

    /** Fires a focused beam of solar particles from Sunbeam's chest core to the attacked target. */
    private void emitChestSunbeam(ServerLevel serverLevel, Entity target) {
        Vec3 chest = new Vec3(this.getX(), this.getY(0.55), this.getZ());
        Vec3 dest = new Vec3(target.getX(), target.getY(0.5), target.getZ());
        Vec3 delta = dest.subtract(chest);
        double dist = delta.length();
        int steps = Math.max(4, (int) Math.ceil(dist * 4.0));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3 p = chest.add(delta.scale(t));
            serverLevel.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
            if (i % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
        serverLevel.sendParticles(ParticleTypes.WAX_ON, dest.x, dest.y, dest.z, 8, 0.25, 0.25, 0.25, 0.03);
        this.playSound(SoundEvents.BLAZE_SHOOT, 0.55F, 1.3F);
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
