package com.silkpanda.artilian.tnt;

import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.config.ArtilianConfig;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.safety.KidSafeTargets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Custom primed TNT entity supporting Artilian's four TNT variants, 1.5s telegraph rings,
 * bounce physics, and kid-safe {@code allowDestructiveCustomTnt} detonation handling.
 */
public class ArtilianPrimedTnt extends PrimedTnt {
    public static final int DEFAULT_FUSE_TICKS = 80;
    public static final int TELEGRAPH_TICKS = 30;
    public static final int TELEGRAPH_INTERVAL_TICKS = 5;
    public static final double TELEGRAPH_RING_RADIUS = 2.5;
    public static final int TELEGRAPH_RING_POINTS = 16;

    private static final EntityDataAccessor<String> DATA_VARIANT =
            SynchedEntityData.defineId(ArtilianPrimedTnt.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_CLUSTER_GEN =
            SynchedEntityData.defineId(ArtilianPrimedTnt.class, EntityDataSerializers.INT);

    private static final String TAG_VARIANT = "variant";
    private static final String TAG_CLUSTER_GEN = "cluster_generation";
    private static final String TAG_OWNER_UUID = "owner_uuid";

    private static final ExplosionDamageCalculator SAFE_QUARRY_DAMAGE_CALCULATOR = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldBlockExplode(
                Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
            if (!ArtilianConfig.isDestructiveTntAllowed() || state.hasBlockEntity()) {
                return false;
            }
            return super.shouldBlockExplode(explosion, reader, pos, state, power);
        }

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            // Only monsters and wild grown animals. Pets, villagers, golems, players, Chester, and
            // everything that is not alive (dropped items, item frames, boats) take no blast damage.
            return entity instanceof LivingEntity living && KidSafeTargets.isValidOffensiveTarget(null, living);
        }
    };

    @Nullable
    private LivingEntity artilianOwner;
    @Nullable
    private UUID ownerUuid;
    private int telegraphPulsesEmitted;

    public ArtilianPrimedTnt(EntityType<? extends ArtilianPrimedTnt> entityType, Level level) {
        super(entityType, level);
        this.setFuse(DEFAULT_FUSE_TICKS);
        this.setBlockState(this.getVariant().defaultBlockState());
    }

    public ArtilianPrimedTnt(
            Level level,
            double x,
            double y,
            double z,
            @Nullable LivingEntity owner,
            TntVariant variant) {
        this(ArtilianEntities.PRIMED_TNT.get(), level);
        this.setPos(x, y, z);
        double angle = level.random.nextDouble() * (Math.PI * 2.0);
        this.setDeltaMovement(-Math.sin(angle) * 0.02, 0.2F, -Math.cos(angle) * 0.02);
        this.setFuse(DEFAULT_FUSE_TICKS);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setArtilianOwner(owner);
        this.setVariant(variant);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, TntVariant.PARTY.getSerializedName());
        builder.define(DATA_CLUSTER_GEN, 0);
    }

    public TntVariant getVariant() {
        return TntVariant.fromName(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(TntVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.getSerializedName());
        this.setBlockState(variant.defaultBlockState());
    }

    public int getClusterGeneration() {
        return this.entityData.get(DATA_CLUSTER_GEN);
    }

    public void setClusterGeneration(int generation) {
        this.entityData.set(DATA_CLUSTER_GEN, Math.max(0, generation));
    }

    public void setArtilianOwner(@Nullable LivingEntity owner) {
        this.artilianOwner = owner;
        this.ownerUuid = owner != null ? owner.getUUID() : null;
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        if (this.artilianOwner != null && !this.artilianOwner.isRemoved()) {
            return this.artilianOwner;
        }
        if (this.ownerUuid != null && this.level() instanceof ServerLevel serverLevel) {
            Entity resolved = serverLevel.getEntity(this.ownerUuid);
            if (resolved instanceof LivingEntity living) {
                this.artilianOwner = living;
                return living;
            }
        }
        return super.getOwner();
    }

    public boolean isTelegraphActive() {
        int fuse = this.getFuse();
        return fuse > 0 && fuse <= TELEGRAPH_TICKS;
    }

    /** Rising pitch from 0.8F (at 30 ticks) to 2.0F (right before detonation). */
    public float getTelegraphPitch() {
        float progress = 1.0F - Mth.clamp((float) this.getFuse() / (float) TELEGRAPH_TICKS, 0.0F, 1.0F);
        return 0.8F + 1.2F * progress;
    }

    public int getTelegraphPulsesEmitted() {
        return this.telegraphPulsesEmitted;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }

        if (this.getVariant() == TntVariant.BOUNCE && this.onGround() && this.getFuse() > 2) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * 1.05, 0.52, motion.z * 1.05);
            this.setOnGround(false);
            if (!this.level().isClientSide) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SLIME_JUMP, SoundSource.BLOCKS, 0.6F, 1.2F);
            }
        }

        if (!this.level().isClientSide
                && this.level() instanceof ServerLevel serverLevel
                && this.isTelegraphActive()
                && this.getFuse() % TELEGRAPH_INTERVAL_TICKS == 0) {
            emitTelegraphRing(serverLevel);
        }
    }

    private void emitTelegraphRing(ServerLevel serverLevel) {
        ParticleOptions particle = this.getVariant().getTelegraphParticle();
        double baseY = this.getY() + 0.1;
        for (int i = 0; i < TELEGRAPH_RING_POINTS; i++) {
            double angle = (Math.PI * 2.0 * i) / TELEGRAPH_RING_POINTS;
            double px = this.getX() + Math.cos(angle) * TELEGRAPH_RING_RADIUS;
            double pz = this.getZ() + Math.sin(angle) * TELEGRAPH_RING_RADIUS;
            serverLevel.sendParticles(particle, px, baseY, pz, 1, 0.0, 0.02, 0.0, 0.0);
        }
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.NOTE_BLOCK_PLING, SoundSource.BLOCKS, 1.0F, this.getTelegraphPitch());
        this.telegraphPulsesEmitted++;
    }

    @Override
    protected void explode() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        switch (this.getVariant()) {
            case PARTY -> explodeParty(serverLevel);
            case BOUNCE -> explodeBounce(serverLevel);
            case FROST_NOVA -> explodeFrostNova(serverLevel);
            case CLUSTER_QUARRY -> explodeClusterQuarry(serverLevel);
            case STARLIGHT_GRAVITY -> explodeStarlightGravity(serverLevel);
            case BRIMSTONE_METEOR -> explodeBrimstoneMeteor(serverLevel);
            case DRAGONFIRE_MEGA -> explodeDragonfireMega(serverLevel);
        }
    }

    private void explodeClusterQuarry(ServerLevel level) {
        boolean destructive = ArtilianConfig.isDestructiveTntAllowed();
        float radius = this.getClusterGeneration() == 0 ? 3.0F : 2.2F;
        level.explode(
                this,
                Explosion.getDefaultDamageSource(level, this),
                SAFE_QUARRY_DAMAGE_CALCULATOR,
                this.getX(),
                this.getY(0.0625),
                this.getZ(),
                radius,
                false,
                destructive ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);

        if (!destructive && this.getClusterGeneration() == 0) {
            notifySafeMode(level);
        }

        if (this.getClusterGeneration() == 0) {
            double[][] offsets = {
                    {0.18, 0.28, 0.18},
                    {-0.18, 0.28, 0.18},
                    {0.18, 0.28, -0.18},
                    {-0.18, 0.28, -0.18}
            };
            for (double[] vel : offsets) {
                ArtilianPrimedTnt bomblet = new ArtilianPrimedTnt(
                        level,
                        this.getX(),
                        this.getY() + 0.3,
                        this.getZ(),
                        this.getOwner(),
                        TntVariant.CLUSTER_QUARRY);
                bomblet.setClusterGeneration(1);
                bomblet.setFuse(20);
                bomblet.setDeltaMovement(vel[0], vel[1], vel[2]);
                level.addFreshEntity(bomblet);
            }
        }
    }

    private void notifySafeMode(ServerLevel level) {
        Component safeNotice = Component.translatable("message.artilian.tnt.safe_mode")
                .withStyle(ChatFormatting.YELLOW);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) <= 256.0) {
                player.displayClientMessage(safeNotice, true);
            }
        }
    }

    private void explodeStarlightGravity(ServerLevel level) {
        double radius = 6.5;
        AABB box = this.getBoundingBox().inflate(radius);
        Vec3 center = this.position();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (KidSafeTargets.isValidOffensiveTarget(null, target)
                    && target.distanceToSqr(center) <= radius * radius) {
                Vec3 pull = center.subtract(target.position());
                double len = pull.length();
                if (len > 0.2) {
                    Vec3 dir = pull.scale(1.0 / len);
                    target.setDeltaMovement(dir.x * 0.75, 0.45, dir.z * 0.75);
                    target.hasImpulse = true;
                    target.hurtMarked = true;
                }
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0));
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                target.hurt(level.damageSources().magic(), 6.0F);
            }
        }
        level.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.6, this.getZ(),
                40, 1.5, 0.8, 1.5, 0.25);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.6, this.getZ(),
                24, 1.2, 0.8, 1.2, 0.08);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.4F, 0.85F);
    }

    private void explodeBrimstoneMeteor(ServerLevel level) {
        boolean destructive = ArtilianConfig.isDestructiveTntAllowed();
        level.explode(
                this,
                Explosion.getDefaultDamageSource(level, this),
                SAFE_QUARRY_DAMAGE_CALCULATOR,
                this.getX(),
                this.getY(0.0625),
                this.getZ(),
                5.5F,
                false,
                destructive ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);

        if (!destructive) {
            notifySafeMode(level);
        }

        double radius = 6.0;
        AABB box = this.getBoundingBox().inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (KidSafeTargets.isValidOffensiveTarget(null, target)
                    && target.distanceToSqr(this) <= radius * radius) {
                target.hurt(level.damageSources().explosion(this, this.getOwner()), 12.0F);
                target.igniteForSeconds(3);
            }
        }
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.5, this.getZ(),
                45, 1.8, 0.8, 1.8, 0.1);
        level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(),
                18, 1.4, 0.6, 1.4, 0.05);
    }

    private void explodeDragonfireMega(ServerLevel level) {
        boolean destructive = ArtilianConfig.isDestructiveTntAllowed();
        level.explode(
                this,
                Explosion.getDefaultDamageSource(level, this),
                SAFE_QUARRY_DAMAGE_CALCULATOR,
                this.getX(),
                this.getY(0.0625),
                this.getZ(),
                7.5F,
                false,
                destructive ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);

        if (!destructive) {
            notifySafeMode(level);
        }

        double radius = 8.0;
        AABB box = this.getBoundingBox().inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (KidSafeTargets.isValidOffensiveTarget(null, target)
                    && target.distanceToSqr(this) <= radius * radius) {
                target.hurt(level.damageSources().explosion(this, this.getOwner()), 16.0F);
                target.igniteForSeconds(4);
            }
        }
        level.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY() + 0.7, this.getZ(),
                56, 2.2, 1.0, 2.2, 0.12);
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.6, this.getZ(),
                48, 2.0, 0.9, 2.0, 0.12);
        level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(),
                22, 1.6, 0.7, 1.6, 0.06);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 1.4F, 0.95F);
    }

    private void explodeParty(ServerLevel level) {
        level.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 0.8, this.getZ(), 32, 1.5, 1.0, 1.5, 0.18);
        level.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + 1.0, this.getZ(), 16, 2.0, 1.0, 2.0, 0.5);
        level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.8, this.getZ(), 12, 1.5, 0.8, 1.5, 0.05);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.5, this.getZ(), 20, 2.0, 0.8, 2.0, 0.1);

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 1.2F, 1.1F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.BLOCKS, 1.0F, 1.2F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);

        AABB area = this.getBoundingBox().inflate(6.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class, area, entity -> !entity.isSpectator() && entity.distanceToSqr(this) <= 36.0);
        for (LivingEntity living : targets) {
            if (KidSafeTargets.isHostile(living)) {
                continue; // Monsters are not invited: no Speed or Regeneration for a creeper.
            }
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            Vec3 vel = living.getDeltaMovement();
            living.setDeltaMovement(vel.x, Math.max(vel.y, 0.42), vel.z);
            living.hasImpulse = true;
            living.hurtMarked = true;
            living.fallDistance = 0.0F;
        }
    }

    private void explodeBounce(ServerLevel level) {
        level.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.5, this.getZ(), 36, 1.8, 0.8, 1.8, 0.15);
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.5, this.getZ(), 24, 1.6, 0.6, 1.6, 0.12);

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.2F, 0.9F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.BLOCKS, 1.0F, 1.1F);

        AABB area = this.getBoundingBox().inflate(6.0);
        List<Entity> targets = level.getEntitiesOfClass(
                Entity.class, area, entity -> entity != this && !entity.isSpectator() && entity.distanceToSqr(this) <= 36.0);
        for (Entity entity : targets) {
            if (entity instanceof ChesterEntity chester && chester.isAsleep()) {
                continue;
            }
            double dx = entity.getX() - this.getX();
            double dz = entity.getZ() - this.getZ();
            double horiz = Math.hypot(dx, dz);
            double pushX = horiz > 0.05 ? (dx / horiz) * 0.45 : 0.0;
            double pushZ = horiz > 0.05 ? (dz / horiz) * 0.45 : 0.0;
            entity.setDeltaMovement(pushX, 1.15, pushZ);
            entity.hasImpulse = true;
            entity.hurtMarked = true;
            entity.fallDistance = 0.0F;
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0));
                living.addEffect(new MobEffectInstance(MobEffects.JUMP, 160, 1));
            }
        }
    }

    private void explodeFrostNova(ServerLevel level) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.8, this.getZ(), 48, 2.2, 1.0, 2.2, 0.12);
        level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY() + 0.8, this.getZ(), 24, 2.0, 0.8, 2.0, 0.08);

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.3F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_HURT_FREEZE, SoundSource.BLOCKS, 1.0F, 0.9F);

        BlockPos center = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 1, 3))) {
            BlockState state = level.getBlockState(pos);
            // Source blocks only: frosted ice melts back into a source, so freezing flowing
            // water would leave a new spring behind.
            if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
                level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
            }
        }

        AABB area = this.getBoundingBox().inflate(6.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class, area, entity -> !entity.isSpectator() && entity.distanceToSqr(this) <= 36.0);
        for (LivingEntity living : targets) {
            if (!KidSafeTargets.isValidOffensiveTarget(null, living)) {
                continue;
            }
            living.hurt(this.damageSources().freeze(), 6.0F);
            living.setTicksFrozen(Math.max(living.getTicksFrozen(), 200));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString(TAG_VARIANT, this.getVariant().getSerializedName());
        compound.putInt(TAG_CLUSTER_GEN, this.getClusterGeneration());
        if (this.ownerUuid != null) {
            compound.putUUID(TAG_OWNER_UUID, this.ownerUuid);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_VARIANT)) {
            this.setVariant(TntVariant.fromName(compound.getString(TAG_VARIANT)));
        }
        if (compound.contains(TAG_CLUSTER_GEN)) {
            this.setClusterGeneration(compound.getInt(TAG_CLUSTER_GEN));
        }
        if (compound.hasUUID(TAG_OWNER_UUID)) {
            this.ownerUuid = compound.getUUID(TAG_OWNER_UUID);
            this.artilianOwner = null;
        }
    }
}
