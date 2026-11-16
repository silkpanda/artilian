package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
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
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * Bob's Shadow Creature (working name: Shadow Wisp): a blocky-headed, purple-tendriled
 * Shadow pet native to Starlight Highlands. Primary element: {@link PetElement#SHADOW}.
 */
public class ShadowWispEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.shadow_wisp.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.shadow_wisp.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.shadow_wisp.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.shadow_wisp.attack");

    public ShadowWispEntity(EntityType<? extends ShadowWispEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5);
    }

    /** Spawns on Starlight Highlands turf or slate in any light level (including twilight/night). */
    public static boolean checkShadowSpawnRules(
            EntityType<? extends ShadowWispEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.ANIMALS_SPAWNABLE_ON) || below.is(ArtilianBlocks.STARLIGHT_SLATE.get());
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.SHADOW;
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.ALLAY_AMBIENT_WITH_ITEM;
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
        if (this.level().isClientSide && this.random.nextInt(7) == 0) {
            this.level().addParticle(
                    ParticleTypes.PORTAL,
                    this.getRandomX(0.45),
                    this.getY(0.4 + this.random.nextDouble() * 0.5),
                    this.getRandomZ(0.45),
                    (this.random.nextDouble() - 0.5) * 0.04,
                    0.02,
                    (this.random.nextDouble() - 0.5) * 0.04);
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
