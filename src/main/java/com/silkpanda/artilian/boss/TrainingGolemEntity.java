package com.silkpanda.artilian.boss;

import com.silkpanda.artilian.pet.PetElement;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * Test-only Earth boss used to prove the boss framework before the story bosses exist.
 * No spawn egg, creative tab entry, natural spawn, or loot.
 */
public class TrainingGolemEntity extends ArtilianBossEntity {
    /** Ground Slam: 40-tick warning, 4-block radius, 8 Earth damage. */
    public static final TelegraphedAttack GROUND_SLAM =
            new TelegraphedAttack("ground_slam", 40, 4.0, 8.0F, PetElement.EARTH);

    public TrainingGolemEntity(EntityType<? extends TrainingGolemEntity> type, Level level) {
        super(type, level, "training_golem");
    }

    @Override
    protected List<TelegraphedAttack> telegraphedAttacks() {
        return List.of(GROUND_SLAM);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return baseAttributes(120.0, 0.22, 6.0, 6.0);
    }

    @Override
    public PetElement getPrimaryElement() {
        return PetElement.EARTH;
    }

    @Override
    public Optional<PetElement> getSecondaryElement() {
        return Optional.empty();
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.GREEN;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
}
