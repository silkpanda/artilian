package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.safety.KidSafeTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side Tier 4 Dragonscale ability service for all five Phase 6 Stage 1D items.
 * Every offensive skill filters targets through {@link KidSafeTargets}, Mana costs go through
 * {@link SunsteelAbilities#consumeAbilityMana}, and block-breaking skills never touch BlockEntities,
 * fluids, unbreakables/obsidian, or place terrain fire.
 */
public final class DragonscaleAbilities {
    public static final int DRAGONFIRE_BREATH_MANA = 30;
    public static final int DRAGONFIRE_BREATH_COOLDOWN = 90;
    public static final double DRAGONFIRE_BREATH_RANGE = 7.0;
    public static final float DRAGONFIRE_BREATH_DAMAGE = 12.0F;

    public static final int WYVERN_FORGE_HASTE_TICKS = 100;
    public static final double DRAGONSCALE_PULL_RADIUS = 8.0;

    public static final int DRAGON_BORE_MANA = 25;
    public static final int DRAGON_BORE_COOLDOWN = 100;

    public static final int WINGBEAT_ROAR_MANA = 30;
    public static final int WINGBEAT_ROAR_COOLDOWN = 100;
    public static final double WINGBEAT_ROAR_RANGE = 6.5;
    public static final float WINGBEAT_ROAR_DAMAGE = 11.5F;
    public static final int WINGBEAT_ROAR_SLOWNESS_TICKS = 60;
    public static final int WINGBEAT_ROAR_RESISTANCE_TICKS = 80;

    public static final int CANOPY_FELL_MANA = 20;
    public static final int CANOPY_FELL_COOLDOWN = 80;
    public static final int CANOPY_FELL_MAX_LOGS = 24;

    public static final int SOVEREIGN_SCOOP_SPEED_TICKS = 80;
    public static final int SOVEREIGN_SCOOP_HASTE_TICKS = 60;
    public static final int TERRA_RAKE_MANA = 25;
    public static final int TERRA_RAKE_COOLDOWN = 90;

    public static final int PRIMAL_OVERGROWTH_MANA = 25;
    public static final int PRIMAL_OVERGROWTH_COOLDOWN = 90;

    private record PendingDropAction(ResourceKey<Level> dimension, UUID minerId, BlockPos pos, boolean smelt) {}

    private static final List<PendingDropAction> PENDING_DROP_ACTIONS = new ArrayList<>();

    private DragonscaleAbilities() {}

    /** Dragonscale Sword active: 7-block dragonfire cone dealing 12.0 dmg, 4s fire, Weakness II, and knockback. */
    public static boolean castDragonfireBreath(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, DRAGONFIRE_BREATH_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, DRAGONFIRE_BREATH_COOLDOWN);

        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double maxDistSqr = DRAGONFIRE_BREATH_RANGE * DRAGONFIRE_BREATH_RANGE;
        AABB searchBox = player.getBoundingBox().inflate(DRAGONFIRE_BREATH_RANGE);

        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> KidSafeTargets.isValidOffensiveTarget(player, entity));

        for (LivingEntity target : candidates) {
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(origin);
            double distSqr = toTarget.lengthSqr();
            if (distSqr > maxDistSqr || distSqr < 1.0E-6) {
                continue;
            }
            Vec3 dir = toTarget.normalize();
            double dot = look.dot(dir);
            if (dot >= 0.45 || (dot > 0.0 && distSqr <= 1.75 * 1.75)) {
                target.hurt(level.damageSources().playerAttack(player), DRAGONFIRE_BREATH_DAMAGE);
                target.igniteForSeconds(4);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                target.setDeltaMovement(dir.x * 0.55, Math.max(target.getDeltaMovement().y, 0.25), dir.z * 0.55);
                target.hurtMarked = true;
                target.hasImpulse = true;
            }
        }

        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0));

        for (double d = 1.0; d <= DRAGONFIRE_BREATH_RANGE; d += 0.85) {
            Vec3 point = origin.add(look.scale(d));
            double spread = 0.15 + d * 0.14;
            level.sendParticles(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z, 3, spread, spread * 0.6, spread, 0.02);
            level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 2, spread * 0.8, spread * 0.5, spread * 0.8, 0.02);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7F, 1.2F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.85F, 0.95F);
        return true;
    }

    /** Dragonscale Pickaxe passive: mining ores grants Haste II, pulls drops within 8 blocks, and auto-smelts unless Silk Touch. */
    public static void onWyvernForge(ServerLevel level, LivingEntity miner, ItemStack toolStack, BlockState state, BlockPos pos) {
        if (!state.is(Tags.Blocks.ORES)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, WYVERN_FORGE_HASTE_TICKS, 1));
        boolean smelt = !PrismaticAbilities.hasSilkTouch(level, toolStack);
        processNearbyDrops(level, miner, pos, smelt);
        synchronized (PENDING_DROP_ACTIONS) {
            PENDING_DROP_ACTIONS.add(new PendingDropAction(level.dimension(), miner.getUUID(), pos.immutable(), smelt));
        }
        level.sendParticles(ParticleTypes.DRAGON_BREATH,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.25, 0.25, 0.25, 0.02);
        level.sendParticles(ParticleTypes.FLAME,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
    }

    /** True when Dragon Bore may safely break {@code state} at {@code pos} (pickaxe-mineable, non-fluid, non-BE, non-obsidian/unbreakable). */
    public static boolean isDragonBoreBreakable(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || !state.getFluidState().isEmpty()
                || level.getBlockEntity(pos) != null) {
            return false;
        }
        float speed = state.getDestroySpeed(level, pos);
        return speed >= 0.0F && speed < 50.0F;
    }

    /** Dragonscale Pickaxe active: carves a 3x3 tunnel face of MINEABLE_WITH_PICKAXE blocks safely. */
    public static boolean castDragonBore(
            ServerLevel level, Player player, ItemStack stack, Item item, BlockPos originPos, Direction face) {
        BlockState originState = level.getBlockState(originPos);
        if (!isDragonBoreBreakable(level, originPos, originState)) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, DRAGON_BORE_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, DRAGON_BORE_COOLDOWN);

        boolean smelt = !PrismaticAbilities.hasSilkTouch(level, stack);
        int broken = 0;
        Direction.Axis axis = face.getAxis();
        for (int u = -1; u <= 1; u++) {
            for (int v = -1; v <= 1; v++) {
                BlockPos targetPos = switch (axis) {
                    case Y -> originPos.offset(u, 0, v);
                    case X -> originPos.offset(0, v, u);
                    case Z -> originPos.offset(u, v, 0);
                };
                BlockState targetState = level.getBlockState(targetPos);
                if (!isDragonBoreBreakable(level, targetPos, targetState)) {
                    continue;
                }
                List<ItemStack> drops = Block.getDrops(targetState, level, targetPos, null, player, stack);
                boolean isOre = targetState.is(Tags.Blocks.ORES);
                level.removeBlock(targetPos, false);
                broken++;
                for (ItemStack drop : drops) {
                    ItemStack out = (smelt && isOre) ? PrismaticAbilities.trySmeltItemStack(level, drop) : drop;
                    spawnAndPullDrop(level, player, targetPos, out);
                }
                level.sendParticles(ParticleTypes.DRAGON_BREATH,
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        4, 0.2, 0.2, 0.2, 0.02);
                level.sendParticles(ParticleTypes.FLAME,
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        2, 0.15, 0.15, 0.15, 0.02);
            }
        }

        if (broken > 0 && !player.isCreative()) {
            stack.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, WYVERN_FORGE_HASTE_TICKS, 1));
        level.playSound(null, originPos, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.7F, 1.3F);
        level.playSound(null, originPos, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0F, 0.9F);
        return broken > 0;
    }

    /** Dragonscale Axe combat active: 6.5-block wingbeat gust damaging/slowing hostiles and granting allies Resistance I. */
    public static boolean castWingbeatRoar(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, WINGBEAT_ROAR_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, WINGBEAT_ROAR_COOLDOWN);

        double maxDistSqr = WINGBEAT_ROAR_RANGE * WINGBEAT_ROAR_RANGE;
        AABB searchBox = player.getBoundingBox().inflate(WINGBEAT_ROAR_RANGE);
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity.isAlive() && entity.distanceToSqr(player) <= maxDistSqr);

        for (LivingEntity entity : nearby) {
            if (entity == player) {
                continue;
            }
            if (KidSafeTargets.isValidOffensiveTarget(player, entity)) {
                entity.hurt(level.damageSources().playerAttack(player), WINGBEAT_ROAR_DAMAGE);
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, WINGBEAT_ROAR_SLOWNESS_TICKS, 1));

                double rx = entity.getX() - player.getX();
                double rz = entity.getZ() - player.getZ();
                double horiz = Math.hypot(rx, rz);
                double dirX = horiz > 1.0E-4 ? rx / horiz : 1.0;
                double dirZ = horiz > 1.0E-4 ? rz / horiz : 0.0;
                entity.setDeltaMovement(dirX * 0.95, Math.max(entity.getDeltaMovement().y, 0.38), dirZ * 0.95);
                entity.hurtMarked = true;
                entity.hasImpulse = true;
            } else if (isFriendlyAlly(entity)) {
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, WINGBEAT_ROAR_RESISTANCE_TICKS, 0));
            }
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, WINGBEAT_ROAR_RESISTANCE_TICKS, 0));

        for (int i = 0; i < 18; i++) {
            double angle = (Math.PI * 2.0 * i) / 18.0;
            double px = player.getX() + Math.cos(angle) * 3.5;
            double py = player.getY(0.35);
            double pz = player.getZ() + Math.sin(angle) * 3.5;
            level.sendParticles(ParticleTypes.DRAGON_BREATH, px, py, pz, 2, 0.15, 0.12, 0.15, 0.03);
            level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 1, 0.1, 0.08, 0.1, 0.02);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    private static boolean isFriendlyAlly(LivingEntity entity) {
        if (entity instanceof Player || entity instanceof ChesterEntity) {
            return true;
        }
        if (entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return true;
        }
        return entity instanceof TamableAnimal tamable && tamable.isTame();
    }

    /** Dragonscale Axe utility active: fells up to 24 connected matching BlockTags.LOGS blocks safely. */
    public static boolean castCanopyFell(
            ServerLevel level, Player player, ItemStack stack, Item item, BlockPos originPos) {
        BlockState originState = level.getBlockState(originPos);
        if (!originState.is(BlockTags.LOGS) || level.getBlockEntity(originPos) != null) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, CANOPY_FELL_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, CANOPY_FELL_COOLDOWN);

        List<BlockPos> logs = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        BlockPos start = originPos.immutable();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty() && logs.size() < CANOPY_FELL_MAX_LOGS) {
            BlockPos current = queue.poll();
            BlockState currentState = level.getBlockState(current);
            if (!currentState.is(originState.getBlock()) || !currentState.is(BlockTags.LOGS)
                    || level.getBlockEntity(current) != null) {
                continue;
            }
            logs.add(current);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        BlockPos next = current.offset(dx, dy, dz);
                        int relY = next.getY() - start.getY();
                        if (Math.abs(next.getX() - start.getX()) <= 4
                                && Math.abs(next.getZ() - start.getZ()) <= 4
                                && relY >= -2 && relY <= 24
                                && visited.add(next)) {
                            queue.add(next);
                        }
                    }
                }
            }
        }

        int broken = 0;
        for (BlockPos pos : logs) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(originState.getBlock()) || !state.is(BlockTags.LOGS) || level.getBlockEntity(pos) != null) {
                continue;
            }
            List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, stack);
            level.removeBlock(pos, false);
            broken++;
            for (ItemStack drop : drops) {
                spawnAndPullDrop(level, player, pos, drop);
            }
            level.sendParticles(ParticleTypes.DRAGON_BREATH,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
        }

        if (broken > 0 && !player.isCreative()) {
            stack.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
        level.playSound(null, originPos, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.0F, 0.85F);
        level.playSound(null, originPos, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.65F, 1.25F);
        return broken > 0;
    }

    /** Dragonscale Shovel passive: breaking shovel-mineable blocks grants Speed II + Haste I and pulls drops within 8 blocks. */
    public static void onSovereignScoop(ServerLevel level, LivingEntity miner, BlockState state, BlockPos pos) {
        if (!state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SOVEREIGN_SCOOP_SPEED_TICKS, 1));
        miner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, SOVEREIGN_SCOOP_HASTE_TICKS, 0));
        processNearbyDrops(level, miner, pos, false);
        synchronized (PENDING_DROP_ACTIONS) {
            PENDING_DROP_ACTIONS.add(new PendingDropAction(level.dimension(), miner.getUUID(), pos.immutable(), false));
        }
        level.sendParticles(ParticleTypes.DRAGON_BREATH,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.25, 0.2, 0.25, 0.02);
    }

    /** Dragonscale Shovel active: clears a 3x3x2 pocket of matching MINEABLE_WITH_SHOVEL blocks safely. */
    public static boolean castTerraRake(
            ServerLevel level, Player player, ItemStack stack, Item item, BlockPos originPos, Direction face) {
        BlockState originState = level.getBlockState(originPos);
        if (!originState.is(BlockTags.MINEABLE_WITH_SHOVEL)
                || !originState.getFluidState().isEmpty()
                || level.getBlockEntity(originPos) != null) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, TERRA_RAKE_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, TERRA_RAKE_COOLDOWN);

        int broken = 0;
        Direction depthDir = face.getOpposite();
        Direction.Axis axis = face.getAxis();
        for (int d = 0; d <= 1; d++) {
            BlockPos layerOrigin = originPos.relative(depthDir, d);
            for (int u = -1; u <= 1; u++) {
                for (int v = -1; v <= 1; v++) {
                    BlockPos targetPos = switch (axis) {
                        case Y -> layerOrigin.offset(u, 0, v);
                        case X -> layerOrigin.offset(0, v, u);
                        case Z -> layerOrigin.offset(u, v, 0);
                    };
                    BlockState targetState = level.getBlockState(targetPos);
                    if (!targetState.is(originState.getBlock())
                            || !targetState.is(BlockTags.MINEABLE_WITH_SHOVEL)
                            || !targetState.getFluidState().isEmpty()
                            || level.getBlockEntity(targetPos) != null) {
                        continue;
                    }
                    List<ItemStack> drops = Block.getDrops(targetState, level, targetPos, null, player, stack);
                    level.removeBlock(targetPos, false);
                    broken++;
                    for (ItemStack drop : drops) {
                        spawnAndPullDrop(level, player, targetPos, drop);
                    }
                    level.sendParticles(ParticleTypes.DRAGON_BREATH,
                            targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.02);
                }
            }
        }

        if (broken > 0 && !player.isCreative()) {
            stack.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SOVEREIGN_SCOOP_SPEED_TICKS, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, SOVEREIGN_SCOOP_HASTE_TICKS, 0));
        level.playSound(null, originPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.PLAYERS, 1.0F, 1.05F);
        level.playSound(null, originPos, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.6F, 1.3F);
        return broken > 0;
    }

    /** Dragonscale Hoe active: 9x9 double bonemeal + farmland hydration, 7x7 auto-harvest + replant, and Regeneration I. */
    public static boolean castPrimalOvergrowth(
            ServerLevel level, Player player, ItemStack stack, Item item, BlockPos clickedPos) {
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!PrismaticAbilities.isPrismaticHarvestTarget(clickedState)) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, PRIMAL_OVERGROWTH_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, PRIMAL_OVERGROWTH_COOLDOWN);

        BlockPos baseCropPos = clickedState.is(Blocks.FARMLAND) ? clickedPos.above() : clickedPos;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos farmPos = baseCropPos.offset(dx, -1, dz);
                BlockState farmState = level.getBlockState(farmPos);
                if (farmState.is(Blocks.FARMLAND) && farmState.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
                    level.setBlock(farmPos, farmState.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 3);
                    level.sendParticles(ParticleTypes.SPLASH,
                            farmPos.getX() + 0.5, farmPos.getY() + 1.02, farmPos.getZ() + 0.5,
                            3, 0.25, 0.05, 0.25, 0.02);
                }

                BlockPos cropPos = baseCropPos.offset(dx, 0, dz);
                BlockState cropState = level.getBlockState(cropPos);
                if (Math.abs(dx) <= 3 && Math.abs(dz) <= 3
                        && cropState.getBlock() instanceof CropBlock crop
                        && crop.isMaxAge(cropState)
                        && level.getBlockEntity(cropPos) == null) {
                    List<ItemStack> drops = Block.getDrops(cropState, level, cropPos, null, player, stack);
                    level.setBlock(cropPos, crop.getStateForAge(0), 3);
                    for (ItemStack drop : drops) {
                        spawnAndPullDrop(level, player, cropPos, drop);
                    }
                    level.sendParticles(ParticleTypes.DRAGON_BREATH,
                            cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                            4, 0.2, 0.2, 0.2, 0.02);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.02);
                } else {
                    for (int pulse = 0; pulse < 2; pulse++) {
                        BlockState currentState = level.getBlockState(cropPos);
                        if (SunsteelAbilities.isSunburstTarget(currentState)
                                && currentState.getBlock() instanceof BonemealableBlock bonemealable
                                && bonemealable.isValidBonemealTarget(level, cropPos, currentState)) {
                            bonemealable.performBonemeal(level, level.random, cropPos, currentState);
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                                    3, 0.2, 0.2, 0.2, 0.02);
                            level.sendParticles(ParticleTypes.DRAGON_BREATH,
                                    cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                                    2, 0.2, 0.2, 0.2, 0.02);
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0));
        level.playSound(null, baseCropPos, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.1F);
        level.playSound(null, baseCropPos, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.65F, 1.35F);
        return true;
    }

    /** Processes queued Wyvern Forge / Sovereign Scoop drop pulls and auto-smelting for {@code level}. */
    public static void tickServerLevel(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        List<PendingDropAction> due = List.of();
        synchronized (PENDING_DROP_ACTIONS) {
            if (!PENDING_DROP_ACTIONS.isEmpty()) {
                List<PendingDropAction> remaining = new ArrayList<>();
                List<PendingDropAction> matched = new ArrayList<>();
                for (PendingDropAction action : PENDING_DROP_ACTIONS) {
                    if (action.dimension().equals(dimension)) {
                        matched.add(action);
                    } else {
                        remaining.add(action);
                    }
                }
                PENDING_DROP_ACTIONS.clear();
                PENDING_DROP_ACTIONS.addAll(remaining);
                due = matched;
            }
        }
        for (PendingDropAction action : due) {
            Entity entity = level.getEntity(action.minerId());
            if (entity instanceof LivingEntity miner && miner.isAlive()) {
                processNearbyDrops(level, miner, action.pos(), action.smelt());
            }
        }
    }

    private static void spawnAndPullDrop(ServerLevel level, Player player, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
        pullDropToward(entity, player);
    }

    private static void processNearbyDrops(ServerLevel level, LivingEntity miner, BlockPos pos, boolean smelt) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(pos).inflate(DRAGONSCALE_PULL_RADIUS);
        double maxDistSqr = DRAGONSCALE_PULL_RADIUS * DRAGONSCALE_PULL_RADIUS;
        List<ItemEntity> drops = level.getEntitiesOfClass(
                ItemEntity.class,
                box,
                item -> item.isAlive() && item.position().distanceToSqr(center) <= maxDistSqr);

        for (ItemEntity drop : drops) {
            if (smelt) {
                ItemStack current = drop.getItem();
                ItemStack smelted = PrismaticAbilities.trySmeltItemStack(level, current);
                if (smelted != current) {
                    drop.setItem(smelted);
                }
            }
            pullDropToward(drop, miner);
        }
    }

    private static void pullDropToward(ItemEntity drop, LivingEntity miner) {
        Vec3 target = miner.position().add(0.0, 0.5, 0.0);
        Vec3 toMiner = target.subtract(drop.position());
        double dist = toMiner.length();
        if (dist > 0.05) {
            Vec3 dir = toMiner.scale(1.0 / dist);
            drop.setDeltaMovement(dir.scale(Math.min(0.6, Math.max(0.28, dist * 0.32))));
            drop.setPos(drop.position().add(dir.scale(Math.min(0.45, dist * 0.25))));
            drop.hasImpulse = true;
        }
    }
}
