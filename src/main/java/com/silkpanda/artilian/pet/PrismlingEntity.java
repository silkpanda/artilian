package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Prismling: the first legendary pet, drawn by the kids as "a rainbow legendary pet that is all types."
 * A pet for everyone, found rarely in all three Overworld biomes.
 *
 * <p>All-types rule: each hit uses whichever of the 11 elements does the most damage against the target,
 * and every element hits Prismling for neutral damage (see {@link ElementalMob#isAllTypes()}).
 * {@link #getPrimaryElement()} is only a storage fallback for capture crystals.
 */
public class PrismlingEntity extends ArtilianPetEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.prismling.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.prismling.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.prismling.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.prismling.attack");

    public PrismlingEntity(EntityType<? extends PrismlingEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    /** Spawns on grass-like ground or any of the three Artilian biome turfs, plus Starlight Slate. */
    public static boolean checkPrismlingSpawnRules(
            EntityType<? extends PrismlingEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(ArtilianBlocks.EMBER_TURF.get())
                || below.is(ArtilianBlocks.STARLIGHT_TURF.get())
                || below.is(ArtilianBlocks.CRYSTAL_TURF.get())
                || below.is(ArtilianBlocks.STARLIGHT_SLATE.get());
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.STARLIGHT;
    }

    @Override
    public boolean isAllTypes() {
        return true;
    }

    /** Picks the element with the highest multiplier against {@code target}; ties are broken at random. */
    @Override
    public PetElement attackElementAgainst(LivingEntity target) {
        float best = Float.NEGATIVE_INFINITY;
        List<PetElement> tied = new ArrayList<>();
        for (PetElement element : PetElement.values()) {
            float multiplier = element.multiplierAgainst(target);
            if (multiplier > best) {
                best = multiplier;
                tied.clear();
                tied.add(element);
            } else if (multiplier == best) {
                tied.add(element);
            }
        }
        return tied.get(this.random.nextInt(tied.size()));
    }

    @Override
    public SoundEvent getHappySound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITH_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // The type symbols drawn around it: cycle through every element's particle, one type per half second.
        if (this.level().isClientSide && this.random.nextInt(6) == 0) {
            PetElement[] elements = PetElement.values();
            PetElement shown = elements[(this.tickCount / 10) % elements.length];
            this.level().addParticle(
                    shown.particle(),
                    this.getRandomX(0.6),
                    this.getY(0.3 + this.random.nextDouble() * 0.6),
                    this.getRandomZ(0.6),
                    (this.random.nextDouble() - 0.5) * 0.03,
                    0.02,
                    (this.random.nextDouble() - 0.5) * 0.03);
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
