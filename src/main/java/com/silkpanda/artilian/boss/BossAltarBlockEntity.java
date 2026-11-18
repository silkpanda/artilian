package com.silkpanda.artilian.boss;

import com.silkpanda.artilian.registry.ArtilianBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Remembers the boss its altar summoned. The altar is busy while a countdown runs or while that
 * boss is alive and loaded; once the boss dies or is removed, the altar is free for a rematch.
 */
public class BossAltarBlockEntity extends BlockEntity {
    /** 3 seconds of red ring and climbing bell before the boss appears. */
    public static final int COUNTDOWN_TICKS = 60;
    /** Radius of the countdown ring around the altar. */
    public static final double RING_RADIUS = 2.5;

    @Nullable
    private UUID bossUuid;
    @Nullable
    private EntityType<?> pendingBoss;
    private int countdown;

    public BossAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ArtilianBlockEntities.BOSS_ALTAR.get(), pos, state);
    }

    /** True while a countdown runs or this altar's boss is still alive in a loaded chunk. */
    public boolean isBusy(ServerLevel level) {
        if (this.pendingBoss != null) {
            return true;
        }
        if (this.bossUuid == null) {
            return false;
        }
        Entity boss = level.getEntity(this.bossUuid);
        return boss != null && boss.isAlive();
    }

    /**
     * Starts a summon if the altar is free. The busy check runs before the item is touched, so a
     * refused summon never costs the player anything.
     */
    public boolean trySummon(ServerLevel level, Player player, ItemStack stack, BossSummonItem item) {
        if (this.isBusy(level)) {
            player.displayClientMessage(Component.translatable("message.artilian.boss_altar.busy"), true);
            return false;
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        this.pendingBoss = item.bossType();
        this.bossUuid = null;
        this.countdown = 0;
        Vec3 center = this.ringCenter();
        BossTelegraphs.bell(level, center.x, center.y, center.z, BossTelegraphs.PITCH_START);
        this.setChanged();
        return true;
    }

    @Nullable
    public UUID bossUuid() {
        return this.bossUuid;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BossAltarBlockEntity altar) {
        if (altar.pendingBoss == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        altar.countdown++;
        BossTelegraphs.tick(serverLevel, altar.ringCenter(), RING_RADIUS, altar.countdown, COUNTDOWN_TICKS);
        if (altar.countdown >= COUNTDOWN_TICKS) {
            altar.spawnBoss(serverLevel);
        }
        altar.setChanged();
    }

    private void spawnBoss(ServerLevel level) {
        EntityType<?> type = this.pendingBoss;
        this.pendingBoss = null;
        this.countdown = 0;
        if (type == null) {
            return;
        }
        Entity entity = type.create(level);
        if (!(entity instanceof ArtilianBossEntity boss)) {
            if (entity != null) {
                entity.discard();
            }
            return;
        }
        BlockPos pos = this.getBlockPos();
        boss.moveTo(pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null);
        level.addFreshEntity(boss);
        this.bossUuid = boss.getUUID();
    }

    private Vec3 ringCenter() {
        BlockPos pos = this.getBlockPos();
        return new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.bossUuid != null) {
            tag.putUUID("Boss", this.bossUuid);
        }
        if (this.pendingBoss != null) {
            tag.putString("PendingBoss", BuiltInRegistries.ENTITY_TYPE.getKey(this.pendingBoss).toString());
            tag.putInt("Countdown", this.countdown);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.bossUuid = tag.hasUUID("Boss") ? tag.getUUID("Boss") : null;
        this.pendingBoss = null;
        this.countdown = 0;
        if (tag.contains("PendingBoss")) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("PendingBoss"));
            if (id != null) {
                this.pendingBoss = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
                this.countdown = tag.getInt("Countdown");
            }
        }
    }
}
