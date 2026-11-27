package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.safety.KidSafeTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side Mythic Dual-Realm (Eye Realm + Dragon Jungle) ability service for all Phase 6 Stage 1E
 * Twin-Star items. Every offensive skill filters strictly through {@link KidSafeTargets}, Mana costs go
 * through {@link SunsteelAbilities#consumeAbilityMana}, and block-breaking skills never touch
 * BlockEntities, fluids, unbreakables/obsidian, or place terrain fire.
 */
public final class TwinStarAbilities {
    public static final int ECLIPSE_TWIN_SLASH_MANA = 35;
    public static final int ECLIPSE_TWIN_SLASH_COOLDOWN = 90;
    public static final double ECLIPSE_TWIN_SLASH_RANGE = 9.0;
    public static final float ECLIPSE_TWIN_SLASH_DAMAGE = 15.0F;
    public static final double ECLIPSE_TWIN_SLASH_ALLY_HEAL_RADIUS = 6.0;
    public static final float ECLIPSE_TWIN_SLASH_ALLY_HEAL = 2.0F;

    public static final int TWIN_STAR_SANCTUARY_MANA = 40;
    public static final int TWIN_STAR_SANCTUARY_COOLDOWN = 120;
    public static final double TWIN_STAR_SANCTUARY_RADIUS = 8.0;

    public static final int ASTRAL_SIGHT_HASTE_TICKS = 100;
    public static final double TWIN_STAR_PULL_RADIUS = 10.0;

    public static final int TWIN_STAR_QUARRY_MANA = 30;
    public static final int TWIN_STAR_QUARRY_COOLDOWN = 100;
    public static final int TWIN_STAR_QUARRY_ORE_SCAN_RADIUS = 16;

    public static final int SOVEREIGN_GAZE_MANA = 35;
    public static final int SOVEREIGN_GAZE_COOLDOWN = 100;
    public static final double SOVEREIGN_GAZE_RADIUS = 7.5;
    public static final float SOVEREIGN_GAZE_DAMAGE = 14.0F;

    public static final int WORLDTREE_HARVEST_MANA = 25;
    public static final int WORLDTREE_HARVEST_COOLDOWN = 80;
    public static final int WORLDTREE_HARVEST_MAX_LOGS = 36;

    public static final int STARLIGHT_GLIDE_SPEED_TICKS = 100;
    public static final int STARLIGHT_GLIDE_HASTE_TICKS = 80;
    public static final int ECLIPSE_EXCAVATE_MANA = 25;
    public static final int ECLIPSE_EXCAVATE_COOLDOWN = 90;

    public static final int TWIN_REALM_GENESIS_MANA = 30;
    public static final int TWIN_REALM_GENESIS_COOLDOWN = 90;

    private record PendingDropAction(ResourceKey<Level> dimension, UUID minerId, BlockPos pos, boolean smelt) {}

    private static final List<PendingDropAction> PENDING_DROP_ACTIONS = new ArrayList<>();

    private TwinStarAbilities() {}

    /**
     * Twin-Star Blade active: converging Eye-Realm & Dragonfire arcs 9 blocks forward (stopping at solid
     * walls, zero fire blocks), dealing 15.0 dmg + 4s fire + Weakness II + Glowing to KidSafeTargets hostiles,
     * healing caster & nearby allies/Chester/pets within 6 blocks for 2.0 HP, and granting Speed II + Strength I.
     */
    public static boolean castEclipseTwinSlash(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, ECLIPSE_TWIN_SLASH_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, ECLIPSE_TWIN_SLASH_COOLDOWN);

        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Set<LivingEntity> hitTargets = new LinkedHashSet<>();

        for (double d = 0.5; d <= ECLIPSE_TWIN_SLASH_RANGE; d += 0.5) {
            Vec3 point = origin.add(look.scale(d));
            BlockPos bp = BlockPos.containing(point);
            if (!level.isInWorldBounds(bp) || !level.getBlockState(bp).getCollisionShape(level, bp).isEmpty()) {
                break;
            }
            AABB stepBox = AABB.ofSize(point, 2.2, 1.8, 2.2);
            List<LivingEntity> found = level.getEntitiesOfClass(
                    LivingEntity.class,
                    stepBox,
                    entity -> KidSafeTargets.isValidOffensiveTarget(player, entity));
            hitTargets.addAll(found);

            level.sendParticles(ParticleTypes.PORTAL, point.x, point.y, point.z, 2, 0.25, 0.2, 0.25, 0.04);
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.15, 0.15, 0.15, 0.02);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z, 2, 0.25, 0.2, 0.25, 0.02);
            level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 1, 0.15, 0.15, 0.15, 0.02);
        }

        for (LivingEntity target : hitTargets) {
            target.hurt(level.damageSources().playerAttack(player), ECLIPSE_TWIN_SLASH_DAMAGE);
            target.igniteForSeconds(4);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
        }

        double healDistSqr = ECLIPSE_TWIN_SLASH_ALLY_HEAL_RADIUS * ECLIPSE_TWIN_SLASH_ALLY_HEAL_RADIUS;
        AABB allyBox = player.getBoundingBox().inflate(ECLIPSE_TWIN_SLASH_ALLY_HEAL_RADIUS);
        List<LivingEntity> allies = level.getEntitiesOfClass(
                LivingEntity.class,
                allyBox,
                entity -> entity.isAlive()
                        && entity.distanceToSqr(player) <= healDistSqr
                        && isFriendlyAlly(entity));
        for (LivingEntity ally : allies) {
            ally.heal(ECLIPSE_TWIN_SLASH_ALLY_HEAL);
            level.sendParticles(ParticleTypes.HEART,
                    ally.getX(), ally.getY(0.85), ally.getZ(), 3, 0.2, 0.2, 0.2, 0.02);
        }

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.25F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.75F, 1.25F);
        return true;
    }

    /**
     * Twin-Star Harmony Staff secondary (Shift + Right-Click): 8-block co-op sanctuary ring that cleanses
     * harmful effects & fire on allies/Chester/pets, grants Absorption II + Resistance I + Regeneration II,
     * and pushes back + slows nearby KidSafeTargets hostiles.
     */
    public static boolean castTwinStarSanctuary(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, TWIN_STAR_SANCTUARY_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, TWIN_STAR_SANCTUARY_COOLDOWN);

        double maxDistSqr = TWIN_STAR_SANCTUARY_RADIUS * TWIN_STAR_SANCTUARY_RADIUS;
        AABB area = player.getBoundingBox().inflate(TWIN_STAR_SANCTUARY_RADIUS);
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive() && entity.distanceToSqr(player) <= maxDistSqr);

        for (LivingEntity entity : nearby) {
            if (isFriendlyAlly(entity)) {
                cleanseHarmfulEffects(entity);
                entity.clearFire();
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                level.sendParticles(ParticleTypes.HEART,
                        entity.getX(), entity.getY(0.85), entity.getZ(), 4, 0.25, 0.25, 0.25, 0.02);
            } else if (KidSafeTargets.isValidOffensiveTarget(player, entity)) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                double dx = entity.getX() - player.getX();
                double dz = entity.getZ() - player.getZ();
                double dist = Math.hypot(dx, dz);
                if (dist > 1.0E-4) {
                    entity.setDeltaMovement((dx / dist) * 0.8, Math.max(entity.getDeltaMovement().y, 0.32), (dz / dist) * 0.8);
                    entity.hurtMarked = true;
                    entity.hasImpulse = true;
                }
            }
        }

        for (int i = 0; i < 24; i++) {
            double angle = (Math.PI * 2.0 * i) / 24.0;
            double px = player.getX() + Math.cos(angle) * 3.5;
            double pz = player.getZ() + Math.sin(angle) * 3.5;
            level.sendParticles(ParticleTypes.END_ROD, px, player.getY() + 0.8, pz, 2, 0.15, 0.2, 0.15, 0.04);
            level.sendParticles(ParticleTypes.PORTAL, px, player.getY() + 0.6, pz, 2, 0.15, 0.2, 0.15, 0.05);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.25F);
        return true;
    }

    private static void cleanseHarmfulEffects(LivingEntity entity) {
        List<Holder<MobEffect>> toRemove = new ArrayList<>();
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (!instance.getEffect().value().isBeneficial()) {
                toRemove.add(instance.getEffect());
            }
        }
        for (Holder<MobEffect> effect : toRemove) {
            entity.removeEffect(effect);
        }
    }

    /** Twin-Star Pickaxe passive: mining ores grants Haste III (100t), pulls drops within 10 blocks, and auto-smelts unless Silk Touch. */
    public static void onAstralSight(ServerLevel level, LivingEntity miner, ItemStack toolStack, BlockState state, BlockPos pos) {
        if (!state.is(Tags.Blocks.ORES)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, ASTRAL_SIGHT_HASTE_TICKS, 2));
        boolean smelt = !PrismaticAbilities.hasSilkTouch(level, toolStack);
        processNearbyDrops(level, miner, pos, smelt);
        synchronized (PENDING_DROP_ACTIONS) {
            PENDING_DROP_ACTIONS.add(new PendingDropAction(level.dimension(), miner.getUUID(), pos.immutable(), smelt));
        }
        level.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.25, 0.25, 0.25, 0.02);
        level.sendParticles(ParticleTypes.PORTAL,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
    }

    /** True when Twin-Star Quarry may safely break {@code state} at {@code pos} (pickaxe-mineable, non-fluid, non-BE, non-obsidian/unbreakable). */
    public static boolean isTwinStarQuarryBreakable(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || state.isAir()
                || !state.getFluidState().isEmpty()
                || level.getBlockEntity(pos) != null) {
            return false;
        }
        float speed = state.getDestroySpeed(level, pos);
        return speed >= 0.0F && speed < 50.0F;
    }

    /**
     * Twin-Star Pickaxe active: carves a 3x3x2 tunnel of MINEABLE_WITH_PICKAXE blocks safely,
     * auto-smelts ore drops unless Silk Touch, pulls drops, and emits a 16-block read-only ore-beacon pulse.
     */
    public static boolean castTwinStarQuarry(
            ServerLevel level, Player player, ItemStack stack, Item item, BlockPos originPos, Direction face) {
        BlockState originState = level.getBlockState(originPos);
        if (!isTwinStarQuarryBreakable(level, originPos, originState)) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, TWIN_STAR_QUARRY_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, TWIN_STAR_QUARRY_COOLDOWN);

        boolean smelt = !PrismaticAbilities.hasSilkTouch(level, stack);
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
                    if (!isTwinStarQuarryBreakable(level, targetPos, targetState)) {
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
                    level.sendParticles(ParticleTypes.END_ROD,
                            targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.02);
                    level.sendParticles(ParticleTypes.DRAGON_BREATH,
                            targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.02);
                }
            }
        }

        if (broken > 0 && !player.isCreative()) {
            stack.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, ASTRAL_SIGHT_HASTE_TICKS, 2));
        scanNearbyOreBeacons(level, player.blockPosition());

        level.playSound(null, originPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.3F);
        level.playSound(null, originPos, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0F, 0.9F);
        return broken > 0;
    }

    private static void scanNearbyOreBeacons(ServerLevel level, BlockPos origin) {
        int r = TWIN_STAR_QUARRY_ORE_SCAN_RADIUS;
        int rSqr = r * r;
        int beacons = 0;
        for (int dx = -r; dx <= r && beacons < 48; dx++) {
            for (int dy = -r; dy <= r && beacons < 48; dy++) {
                for (int dz = -r; dz <= r && beacons < 48; dz++) {
                    if (dx * dx + dy * dy + dz * dz > rSqr) {
                        continue;
                    }
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (level.isInWorldBounds(pos) && level.getBlockState(pos).is(Tags.Blocks.ORES)) {
                        beacons++;
                        level.sendParticles(ParticleTypes.END_ROD,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.15, 0.2, 0.15, 0.01);
                        level.sendParticles(ParticleTypes.PORTAL,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.15, 0.2, 0.15, 0.01);
                    }
                }
            }
        }
    }

    /**
     * Twin-Star Axe combat active: 7.5-block twin-realm shockwave dealing 14.0 dmg + Glowing + Slowness II +
     * knockback to KidSafeTargets hostiles, granting caster & allies Absorption I + Speed I.
     */
    public static boolean castSovereignGaze(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, SOVEREIGN_GAZE_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, SOVEREIGN_GAZE_COOLDOWN);

        double maxDistSqr = SOVEREIGN_GAZE_RADIUS * SOVEREIGN_GAZE_RADIUS;
        AABB searchBox = player.getBoundingBox().inflate(SOVEREIGN_GAZE_RADIUS);
        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity.isAlive() && entity.distanceToSqr(player) <= maxDistSqr);

        for (LivingEntity entity : nearby) {
            if (entity == player) {
                continue;
            }
            if (KidSafeTargets.isValidOffensiveTarget(player, entity)) {
                entity.hurt(level.damageSources().playerAttack(player), SOVEREIGN_GAZE_DAMAGE);
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));

                double rx = entity.getX() - player.getX();
                double rz = entity.getZ() - player.getZ();
                double horiz = Math.hypot(rx, rz);
                double dirX = horiz > 1.0E-4 ? rx / horiz : 1.0;
                double dirZ = horiz > 1.0E-4 ? rz / horiz : 0.0;
                entity.setDeltaMovement(dirX * 1.0, Math.max(entity.getDeltaMovement().y, 0.4), dirZ * 1.0);
                entity.hurtMarked = true;
                entity.hasImpulse = true;
            } else if (isFriendlyAlly(entity)) {
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0));
            }
        }

        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0));

        for (int i = 0; i < 20; i++) {
            double angle = (Math.PI * 2.0 * i) / 20.0;
            double px = player.getX() + Math.cos(angle) * 4.0;
            double py = player.getY(0.4);
            double pz = player.getZ() + Math.sin(angle) * 4.0;
            level.sendParticles(ParticleTypes.PORTAL, px, py, pz, 2, 0.15, 0.12, 0.15, 0.04);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, px, py, pz, 2, 0.15, 0.12, 0.15, 0.03);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0F, 1.1F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }

    /** Twin-Star Axe utility active: fells up to 36 connected matching BlockTags.LOGS blocks safely. */
    public static boolean castWorldtreeHarvest(
            ServerLevel level, Player player, ItemStack stack, Item item, BlockPos originPos) {
        BlockState originState = level.getBlockState(originPos);
        if (!originState.is(BlockTags.LOGS) || level.getBlockEntity(originPos) != null) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, WORLDTREE_HARVEST_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, WORLDTREE_HARVEST_COOLDOWN);

        List<BlockPos> logs = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        BlockPos start = originPos.immutable();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty() && logs.size() < WORLDTREE_HARVEST_MAX_LOGS) {
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
                        if (Math.abs(next.getX() - start.getX()) <= 5
                                && Math.abs(next.getZ() - start.getZ()) <= 5
                                && relY >= -2 && relY <= 32
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
            level.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.02);
        }

        if (broken > 0 && !player.isCreative()) {
            stack.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
        level.playSound(null, originPos, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.0F, 0.9F);
        level.playSound(null, originPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.35F);
        return broken > 0;
    }

    /** Twin-Star Shovel passive: breaking shovel-mineable blocks grants Speed II + Haste II and pulls drops within 10 blocks. */
    public static void onStarlightGlide(ServerLevel level, LivingEntity miner, BlockState state, BlockPos pos) {
        if (!state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, STARLIGHT_GLIDE_SPEED_TICKS, 1));
        miner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, STARLIGHT_GLIDE_HASTE_TICKS, 1));
        processNearbyDrops(level, miner, pos, false);
        synchronized (PENDING_DROP_ACTIONS) {
            PENDING_DROP_ACTIONS.add(new PendingDropAction(level.dimension(), miner.getUUID(), pos.immutable(), false));
        }
        level.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.25, 0.2, 0.25, 0.02);
    }

    /** Twin-Star Shovel active: clears a 5x5 face of matching MINEABLE_WITH_SHOVEL blocks safely. */
    public static boolean castEclipseExcavate(
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
        if (!SunsteelAbilities.consumeAbilityMana(player, ECLIPSE_EXCAVATE_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, ECLIPSE_EXCAVATE_COOLDOWN);

        int broken = 0;
        Direction.Axis axis = face.getAxis();
        for (int u = -2; u <= 2; u++) {
            for (int v = -2; v <= 2; v++) {
                BlockPos targetPos = switch (axis) {
                    case Y -> originPos.offset(u, 0, v);
                    case X -> originPos.offset(0, v, u);
                    case Z -> originPos.offset(u, v, 0);
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
                level.sendParticles(ParticleTypes.PORTAL,
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        2, 0.2, 0.2, 0.2, 0.02);
            }
        }

        if (broken > 0 && !player.isCreative()) {
            stack.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, STARLIGHT_GLIDE_SPEED_TICKS, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, STARLIGHT_GLIDE_HASTE_TICKS, 1));
        level.playSound(null, originPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.PLAYERS, 1.0F, 1.15F);
        level.playSound(null, originPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.35F);
        return broken > 0;
    }

    /** Twin-Star Hoe active: 11x11 triple bonemeal + farmland hydration, 9x9 auto-harvest + replant, and ally Regeneration II. */
    public static boolean castTwinRealmGenesis(
            ServerLevel level, Player player, ItemStack stack, Item item, BlockPos clickedPos) {
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!StarlightAbilities.isAstralBloomTarget(clickedState)) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, TWIN_REALM_GENESIS_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, TWIN_REALM_GENESIS_COOLDOWN);

        BlockPos baseCropPos = clickedState.is(Blocks.FARMLAND) ? clickedPos.above() : clickedPos;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
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
                if (Math.abs(dx) <= 4 && Math.abs(dz) <= 4
                        && cropState.getBlock() instanceof CropBlock crop
                        && crop.isMaxAge(cropState)
                        && level.getBlockEntity(cropPos) == null) {
                    List<ItemStack> drops = Block.getDrops(cropState, level, cropPos, null, player, stack);
                    level.setBlock(cropPos, crop.getStateForAge(0), 3);
                    for (ItemStack drop : drops) {
                        spawnAndPullDrop(level, player, cropPos, drop);
                    }
                    level.sendParticles(ParticleTypes.END_ROD,
                            cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.02);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.02);
                } else {
                    for (int pulse = 0; pulse < 3; pulse++) {
                        BlockState currentState = level.getBlockState(cropPos);
                        if (SunsteelAbilities.isSunburstTarget(currentState)
                                && currentState.getBlock() instanceof BonemealableBlock bonemealable
                                && bonemealable.isValidBonemealTarget(level, cropPos, currentState)) {
                            bonemealable.performBonemeal(level, level.random, cropPos, currentState);
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                                    3, 0.2, 0.2, 0.2, 0.02);
                            level.sendParticles(ParticleTypes.END_ROD,
                                    cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                                    2, 0.2, 0.2, 0.2, 0.02);
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        double allyDistSqr = TWIN_STAR_SANCTUARY_RADIUS * TWIN_STAR_SANCTUARY_RADIUS;
        AABB allyBox = player.getBoundingBox().inflate(TWIN_STAR_SANCTUARY_RADIUS);
        List<LivingEntity> allies = level.getEntitiesOfClass(
                LivingEntity.class,
                allyBox,
                entity -> entity.isAlive()
                        && entity.distanceToSqr(player) <= allyDistSqr
                        && isFriendlyAlly(entity));
        for (LivingEntity ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1));
        }

        level.playSound(null, baseCropPos, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.15F);
        level.playSound(null, baseCropPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.3F);
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

    /** Processes queued Astral Sight / Starlight Glide drop pulls and auto-smelting for {@code level}. */
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
        AABB box = new AABB(pos).inflate(TWIN_STAR_PULL_RADIUS);
        double maxDistSqr = TWIN_STAR_PULL_RADIUS * TWIN_STAR_PULL_RADIUS;
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
            drop.setDeltaMovement(dir.scale(Math.min(0.65, Math.max(0.3, dist * 0.35))));
            drop.setPos(drop.position().add(dir.scale(Math.min(0.5, dist * 0.28))));
            drop.hasImpulse = true;
        }
    }
}
