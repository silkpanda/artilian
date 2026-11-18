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

import java.util.Optional;

/**
 * Kid-drawn Electric buddy (placeholder name: Zapkin).
 * Sculpted yellow-and-red lightning creature with jagged head bolts and glowing orb hands.
 * Element: {@link PetElement#ELECTRIC}.
 */
public class ZapkinEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.zapkin.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.zapkin.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.zapkin.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.zapkin.attack");

    public ZapkinEntity(EntityType<? extends ZapkinEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5);
    }

    /** Spawns on Starlight Highlands turf or slate ground. */
    public static boolean checkZapkinSpawnRules(
            EntityType<? extends ZapkinEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(ArtilianBlocks.STARLIGHT_SLATE.get())
                || below.is(ArtilianBlocks.STARLIGHT_TURF.get());
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.ELECTRIC;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.empty();
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
        if (this.level().isClientSide && this.random.nextInt(5) == 0) {
            this.level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getRandomX(0.6),
                    this.getY(0.65 + this.random.nextDouble() * 0.55),
                    this.getRandomZ(0.6),
                    (this.random.nextDouble() - 0.5) * 0.06,
                    0.03,
                    (this.random.nextDouble() - 0.5) * 0.06);
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
