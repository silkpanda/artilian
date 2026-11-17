package com.silkpanda.artilian.pet;

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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Base class for Artilian biome pets. Implements {@link OwnableEntity} so all kid-safe spell and
 * TNT rules spare tamed pets automatically, and enforces non-lethal fainting back into the
 * owner's {@link CaptureCrystalItem}.
 */
public abstract class ArtilianPetEntity extends PathfinderMob implements ElementalMob, OwnableEntity, GeoEntity {
    public static final int MAX_AFFINITY = 100;
    public static final int SIGNATURE_COOLDOWN_TICKS = 60;

    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID =
            SynchedEntityData.defineId(ArtilianPetEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> PET_ID =
            SynchedEntityData.defineId(ArtilianPetEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> AFFINITY =
            SynchedEntityData.defineId(ArtilianPetEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BEFRIENDED =
            SynchedEntityData.defineId(ArtilianPetEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING =
            SynchedEntityData.defineId(ArtilianPetEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String KEY_OWNER = "Owner";
    private static final String KEY_OWNER_NAME = "OwnerName";
    private static final String KEY_PET_ID = "PetId";
    private static final String KEY_AFFINITY = "Affinity";
    private static final String KEY_BEFRIENDED = "Befriended";
    private static final String KEY_SITTING = "Sitting";

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private String ownerName = "";
    private WeakReference<Player> cachedOwner = new WeakReference<>(null);
    private int signatureCooldownTicks = 0;

    protected ArtilianPetEntity(EntityType<? extends ArtilianPetEntity> type, Level level) {
        super(type, level);
    }

    public abstract PetElement getPrimaryElement();

    public Optional<PetElement> getSecondaryElement() {
        return Optional.empty();
    }

    /** Element this pet attacks {@code target} with. Defaults to the primary element. */
    public PetElement attackElementAgainst(LivingEntity target) {
        return this.getPrimaryElement();
    }

    public SoundEvent getHappySound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    public boolean tryConsumeSignatureCooldown() {
        if (this.signatureCooldownTicks > 0) {
            return false;
        }
        this.signatureCooldownTicks = SIGNATURE_COOLDOWN_TICKS;
        return true;
    }

    public void triggerAttackAnim() {}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_ID, Optional.empty());
        builder.define(PET_ID, Optional.empty());
        builder.define(AFFINITY, 0);
        builder.define(BEFRIENDED, false);
        builder.define(SITTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitStillGoal(this));
        this.goalSelector.addGoal(2, new PetMeleeAttackGoal(this, 1.35, true));
        this.goalSelector.addGoal(3, new PetFollowOwnerGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new PetOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new PetOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false,
                candidate -> this.isTamed() && !this.isSitting()
                        && PetService.isValidAutonomousBattleTarget(this.getOwnerInLevel(), candidate)));
    }

    public static boolean checkPetSpawnRules(
            EntityType<? extends ArtilianPetEntity> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
                && level.getRawBrightness(pos, 0) > 8;
    }

    // ---- Ownership & Identity ----

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_ID).orElse(null);
    }

    public Optional<UUID> getOwnerId() {
        return this.entityData.get(OWNER_ID);
    }

    public void setOwner(Player owner) {
        this.entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
        this.ownerName = owner.getGameProfile().getName();
        this.cachedOwner = new WeakReference<>(owner);
        this.setPersistenceRequired();
    }

    @Nullable
    public Player getCachedOwner() {
        return this.cachedOwner.get();
    }

    public void setOwnerRaw(UUID ownerUuid, String ownerName) {
        this.entityData.set(OWNER_ID, Optional.of(ownerUuid));
        this.ownerName = ownerName;
        this.setPersistenceRequired();
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public boolean isTamed() {
        return this.getOwnerId().isPresent();
    }

    public boolean isOwner(Player player) {
        return this.getOwnerId().filter(player.getUUID()::equals).isPresent();
    }

    @Nullable
    public Player getOwnerInLevel() {
        Player found = this.getOwnerId().map(this.level()::getPlayerByUUID).orElse(null);
        if (found != null) {
            return found;
        }
        Player cached = this.getCachedOwner();
        return cached != null && cached.level() == this.level() ? cached : null;
    }

    public Optional<UUID> getPetId() {
        return this.entityData.get(PET_ID);
    }

    public void setPetId(UUID petId) {
        this.entityData.set(PET_ID, Optional.of(petId));
    }

    // ---- Affinity & Sitting ----

    public int getAffinity() {
        return this.entityData.get(AFFINITY);
    }

    public void addAffinity(int delta) {
        int updated = Math.min(MAX_AFFINITY, Math.max(0, this.getAffinity() + delta));
        this.entityData.set(AFFINITY, updated);
        if (updated >= MAX_AFFINITY) {
            this.entityData.set(BEFRIENDED, true);
            this.setPersistenceRequired();
        }
    }

    public boolean isBefriended() {
        return this.entityData.get(BEFRIENDED);
    }

    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(SITTING, sitting);
        if (sitting) {
            this.setTarget(null);
            this.getNavigation().stop();
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.isTamed()
                && !this.isSitting()
                && PetService.isValidBattleTarget(this.getOwnerInLevel(), target)
                && super.canAttack(target);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (this.level() instanceof ServerLevel serverLevel && entity instanceof LivingEntity target) {
            return PetService.performElementalAttack(this, target, serverLevel);
        }
        return false;
    }

    public boolean canFollow() {
        return this.isTamed() && !this.isSitting();
    }

    public boolean isTooFarFromOwner() {
        Player owner = this.getOwnerInLevel();
        return owner != null && this.distanceToSqr(owner) >= PetFollowOwnerGoal.COMBAT_LEASH_DISTANCE_SQR;
    }

    public boolean toggleSitting(Player player) {
        if (!this.isTamed() || !this.isOwner(player)) {
            return false;
        }
        boolean next = !this.isSitting();
        this.setSitting(next);
        player.displayClientMessage(Component.translatable(
                next ? "message.artilian.pet.sitting" : "message.artilian.pet.following",
                this.getDisplayName()), true);
        return true;
    }

    // ---- Interaction ----

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof CaptureCrystalItem || held.getItem() instanceof BiomeTreatItem) {
            // Let Item#interactLivingEntity handle capture and feeding cleanly.
            return InteractionResult.PASS;
        }
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (this.isTamed()) {
            if (!this.isOwner(player)) {
                PetService.notifyNotOwner(player, this);
                return InteractionResult.CONSUME;
            }
            if (player.isShiftKeyDown()) {
                this.toggleSitting(player);
                return InteractionResult.CONSUME;
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY(0.9), this.getZ(),
                        3, 0.2, 0.2, 0.2, 0.02);
                this.playSound(this.getHappySound(), 0.7F, 1.2F);
            }
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, hand);
    }

    // ---- Non-lethal fainting & orphan guard ----

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.signatureCooldownTicks > 0) {
                this.signatureCooldownTicks--;
            }
            if (this.getTarget() != null && (!this.isTamed() || this.isSitting() || !this.canAttack(this.getTarget()))) {
                this.setTarget(null);
            }
            if (this.tickCount % 10 == 0 && this.getServer() != null && this.isTamed()) {
                ServerPlayer owner = this.getOwnerId().map(this.getServer().getPlayerList()::getPlayer).orElse(null);
                if (owner != null) {
                    this.discardIfOrphan(PetRosterData.get(owner));
                }
            }
        }
    }

    public boolean discardIfOrphan(PetRosterData roster) {
        Optional<UUID> petId = this.getPetId();
        if (petId.isEmpty()) {
            return false;
        }
        if (!roster.isActiveEntity(petId.get(), this.getUUID())) {
            this.discard();
            return true;
        }
        return false;
    }

    /** Kid-safe control: tamed pets faint safely back into their crystal instead of dying permanently. */
    public boolean shouldFaintInsteadOfDie(DamageSource source) {
        return PetService.isNonLethalFaintingEnabled() && this.isTamed() && !source.is(DamageTypes.GENERIC_KILL);
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
        super.actuallyHurt(damageSource, damageAmount);
        if (!this.level().isClientSide && this.getHealth() <= 0.0F && this.shouldFaintInsteadOfDie(damageSource)) {
            this.setHealth(1.0F);
            PetService.faintPet(this);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && this.shouldFaintInsteadOfDie(source)) {
            this.setHealth(1.0F);
            PetService.faintPet(this);
            return;
        }
        super.die(source);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTamed() && !this.isBefriended();
    }

    @Override
    protected boolean shouldDropLoot() {
        return !this.isTamed();
    }

    @Override
    public boolean shouldDropExperience() {
        return !this.isTamed();
    }

    // ---- Saving ----

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.getOwnerId().ifPresent(id -> tag.putUUID(KEY_OWNER, id));
        tag.putString(KEY_OWNER_NAME, this.ownerName);
        this.getPetId().ifPresent(id -> tag.putUUID(KEY_PET_ID, id));
        tag.putInt(KEY_AFFINITY, this.getAffinity());
        tag.putBoolean(KEY_BEFRIENDED, this.isBefriended());
        tag.putBoolean(KEY_SITTING, this.isSitting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(OWNER_ID, tag.hasUUID(KEY_OWNER) ? Optional.of(tag.getUUID(KEY_OWNER)) : Optional.empty());
        this.ownerName = tag.getString(KEY_OWNER_NAME);
        this.entityData.set(PET_ID, tag.hasUUID(KEY_PET_ID) ? Optional.of(tag.getUUID(KEY_PET_ID)) : Optional.empty());
        this.entityData.set(AFFINITY, tag.getInt(KEY_AFFINITY));
        this.entityData.set(BEFRIENDED, tag.getBoolean(KEY_BEFRIENDED));
        this.entityData.set(SITTING, tag.getBoolean(KEY_SITTING));
        if (this.isTamed() || this.isBefriended()) {
            this.setPersistenceRequired();
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    /** Holds a sitting pet in place while keeping look goals responsive. */
    private static final class SitStillGoal extends Goal {
        private final ArtilianPetEntity pet;

        private SitStillGoal(ArtilianPetEntity pet) {
            this.pet = pet;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.pet.isTamed() && !this.pet.isInWaterOrBubble() && this.pet.isSitting();
        }

        @Override
        public void start() {
            this.pet.getNavigation().stop();
        }
    }

    /** Melee attack goal active only when tamed and not sitting. */
    private static final class PetMeleeAttackGoal extends MeleeAttackGoal {
        private final ArtilianPetEntity pet;

        private PetMeleeAttackGoal(ArtilianPetEntity pet, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(pet, speedModifier, followingTargetEvenIfNotSeen);
            this.pet = pet;
        }

        @Override
        public boolean canUse() {
            return this.pet.isTamed() && !this.pet.isSitting() && !this.pet.isTooFarFromOwner() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.pet.isTamed() && !this.pet.isSitting() && !this.pet.isTooFarFromOwner() && super.canContinueToUse();
        }
    }

    /** Defends the pet's owner immediately when a valid hostile hurts them. */
    private static final class PetOwnerHurtByTargetGoal extends TargetGoal {
        private final ArtilianPetEntity pet;
        @Nullable
        private LivingEntity attacker;
        private int timestamp = -1;

        private PetOwnerHurtByTargetGoal(ArtilianPetEntity pet) {
            super(pet, false);
            this.pet = pet;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.pet.isTamed() || this.pet.isSitting()) {
                return false;
            }
            Player owner = this.pet.getOwnerInLevel();
            if (owner == null) {
                return false;
            }
            this.attacker = owner.getLastHurtByMob();
            int currentTimestamp = owner.getLastHurtByMobTimestamp();
            return currentTimestamp != this.timestamp
                    && this.attacker != null
                    && this.pet.canAttack(this.attacker)
                    && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            Player owner = this.pet.getOwnerInLevel();
            if (owner != null) {
                this.timestamp = owner.getLastHurtByMobTimestamp();
            }
            super.start();
        }
    }

    /** Assists the pet's owner against valid hostile targets they strike. */
    private static final class PetOwnerHurtTargetGoal extends TargetGoal {
        private final ArtilianPetEntity pet;
        @Nullable
        private LivingEntity targetMob;
        private int timestamp = -1;

        private PetOwnerHurtTargetGoal(ArtilianPetEntity pet) {
            super(pet, false);
            this.pet = pet;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.pet.isTamed() || this.pet.isSitting()) {
                return false;
            }
            Player owner = this.pet.getOwnerInLevel();
            if (owner == null) {
                return false;
            }
            this.targetMob = owner.getLastHurtMob();
            int currentTimestamp = owner.getLastHurtMobTimestamp();
            return currentTimestamp != this.timestamp
                    && this.targetMob != null
                    && this.pet.canAttack(this.targetMob)
                    && this.canAttack(this.targetMob, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.targetMob);
            Player owner = this.pet.getOwnerInLevel();
            if (owner != null) {
                this.timestamp = owner.getLastHurtMobTimestamp();
            }
            super.start();
        }
    }
}
