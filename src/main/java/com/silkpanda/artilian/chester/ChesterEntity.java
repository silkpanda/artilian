package com.silkpanda.artilian.chester;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * Chester: a living chest that belongs to exactly one player. He holds no items himself. He is
 * the door to the storage on his owner (see {@link ChesterData}).
 */
public class ChesterEntity extends PathfinderMob implements GeoEntity, MenuProvider {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID =
            SynchedEntityData.defineId(ChesterEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final String KEY_OWNER = "Owner";
    private static final EntityDataAccessor<Boolean> ASLEEP =
            SynchedEntityData.defineId(ChesterEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String KEY_OWNER_NAME = "OwnerName";
    private static final String KEY_TOLD_TO_SLEEP = "ToldToSleep";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.chester.idle");
    private static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("animation.chester.sleep");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.chester.walk");
    private static final RawAnimation CHOMP = RawAnimation.begin().thenPlay("animation.chester.chomp");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private String ownerName = "";
    private boolean toldToSleep;
    private boolean ownerInBed;

    public ChesterEntity(EntityType<? extends ChesterEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_ID, Optional.empty());
        builder.define(ASLEEP, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    // ---- Ownership ----

    public void setOwner(Player owner) {
        this.entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
        this.ownerName = owner.getGameProfile().getName();
        this.setCustomName(Component.translatable("entity.artilian.chester.owned", this.ownerName));
    }

    public Optional<UUID> getOwnerId() {
        return this.entityData.get(OWNER_ID);
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public boolean isOwner(Player player) {
        return this.getOwnerId().filter(player.getUUID()::equals).isPresent();
    }

    /** The owner, if they are in this dimension right now. Used for following only. */
    @Nullable
    public Player getOwnerInLevel() {
        return this.getOwnerId().map(this.level()::getPlayerByUUID).orElse(null);
    }

    // ---- Opening Chester ----

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!this.isOwner(player)) {
            // Friendly, and only a message. The lock itself is in createMenu and ChesterContainer.
            player.displayClientMessage(this.ownerName.isEmpty()
                    ? Component.translatable("message.artilian.chester.no_owner")
                    : Component.translatable("message.artilian.chester.not_yours", this.ownerName), true);
            this.playSound(SoundEvents.CHEST_LOCKED, 0.6F, 1.0F);
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown()) {
            this.toggleSitAndSleep(player);
            return InteractionResult.CONSUME;
        }
        player.openMenu(this);
        this.triggerAnim("action", "chomp");
        this.playSound(SoundEvents.CHEST_OPEN, 0.5F, 1.2F);
        return InteractionResult.CONSUME;
    }

    /**
     * The only place a Chester screen is made. A player who is not the owner gets nothing, and
     * the owner gets a screen onto the storage they carry themselves (see ChesterContainer).
     */
    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (!this.isOwner(player)) {
            return null;
        }
        return ChestMenu.threeRows(containerId, playerInventory, new ChesterContainer(this, player));
    }

    // ---- One Chester per player ----

    @Override
    public void tick() {
        if (this.isAsleep()) {
            this.holdStill();
        }
        super.tick();
        if (!this.level().isClientSide && this.tickCount % 5 == 0 && this.getServer() != null) {
            ServerPlayer owner = this.getOwnerId().map(this.getServer().getPlayerList()::getPlayer).orElse(null);
            if (owner != null && !this.discardIfOrphan(ChesterData.get(owner))) {
                this.applyOwnerBedState(owner.isSleeping());
            }
        }
    }

    /**
     * The owner's record names the one Chester that is theirs. If it does not name this one
     * (they whistled for a new Chester while this one was out of reach), this one leaves.
     * Returns true if it removed itself.
     */
    public boolean discardIfOrphan(ChesterData ownerData) {
        if (ownerData.getChesterEntityId().filter(this.getUUID()::equals).isPresent()) {
            return false;
        }
        this.discard();
        return true;
    }

    /** False while something should keep Chester where he is. */
    public boolean canFollow() {
        return !this.isAsleep();
    }

    // ---- Sleeping: Chester looks like a normal chest ----

    /** True while Chester is asleep. Sent to clients, which draw him as a plain chest. */
    public boolean isAsleep() {
        return this.entityData.get(ASLEEP);
    }

    /** True if the owner told Chester to Sit & Sleep. He stays asleep until they wake him. */
    public boolean isToldToSleep() {
        return this.toldToSleep;
    }

    /**
     * Shift + right-click. Only the owner can put Chester to sleep or wake him up.
     * Returns true if it did anything.
     */
    public boolean toggleSitAndSleep(Player player) {
        if (!this.isOwner(player)) {
            return false;
        }
        this.toldToSleep = !this.toldToSleep;
        this.refreshAsleep();
        player.displayClientMessage(Component.translatable(this.toldToSleep
                ? "message.artilian.chester.sleeping"
                : "message.artilian.chester.awake"), true);
        return true;
    }

    /** Chester sleeps while his owner is in a bed, and wakes with them unless told to Sit & Sleep. */
    public void applyOwnerBedState(boolean ownerIsInBed) {
        this.ownerInBed = ownerIsInBed;
        this.refreshAsleep();
    }

    private void refreshAsleep() {
        boolean shouldSleep = this.toldToSleep || this.ownerInBed;
        if (shouldSleep == this.isAsleep()) {
            return;
        }
        this.entityData.set(ASLEEP, shouldSleep);
        this.getNavigation().stop();
        if (shouldSleep) {
            // Sit square to the world, the way a placed chest does.
            float square = Math.round(this.getYRot() / 90.0F) * 90.0F;
            this.setYRot(square);
            this.setYBodyRot(square);
            this.setYHeadRot(square);
        }
        this.playSound(shouldSleep ? SoundEvents.CHEST_CLOSE : SoundEvents.CHEST_OPEN, 0.6F, 1.0F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(shouldSleep ? ParticleTypes.POOF : ParticleTypes.HAPPY_VILLAGER,
                    this.getX(), this.getY() + 0.6, this.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
        }
    }

    /** A sleeping Chester stays put: pushes and currents are thrown away. He still falls. */
    private void holdStill() {
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0.0, Math.min(motion.y, 0.0), 0.0);
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isAsleep();
    }

    @Override
    public boolean isPushable() {
        return !this.isAsleep() && super.isPushable();
    }

    // ---- Kid-safe: nothing in the world can hurt Chester ----

    /**
     * Players, mobs, lava, and explosions do nothing. Only damage that the game itself says
     * ignores invulnerability gets through: the void and /kill.
     */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && this.getServer() != null) {
            this.getOwnerId()
                    .map(this.getServer().getPlayerList()::getPlayer)
                    .ifPresent(ChesterEntity::tellOwnerChesterIsLost);
        }
    }

    private static void tellOwnerChesterIsLost(ServerPlayer owner) {
        owner.displayClientMessage(Component.translatable("message.artilian.chester.lost"), false);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    // ---- Saving ----

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.getOwnerId().ifPresent(id -> tag.putUUID(KEY_OWNER, id));
        tag.putString(KEY_OWNER_NAME, this.ownerName);
        tag.putBoolean(KEY_TOLD_TO_SLEEP, this.toldToSleep);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(OWNER_ID, tag.hasUUID(KEY_OWNER) ? Optional.of(tag.getUUID(KEY_OWNER)) : Optional.empty());
        this.ownerName = tag.getString(KEY_OWNER_NAME);
        this.toldToSleep = tag.getBoolean(KEY_TOLD_TO_SLEEP);
        this.entityData.set(ASLEEP, this.toldToSleep);
    }

    // ---- Animation ----

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state ->
                state.setAndContinue(this.isAsleep() ? SLEEP : state.isMoving() ? WALK : IDLE)));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("chomp", CHOMP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
