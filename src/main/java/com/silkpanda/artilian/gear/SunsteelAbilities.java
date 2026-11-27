package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.mana.ManaService;
import com.silkpanda.artilian.safety.KidSafeTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side Solar / Paladin ability service for all five Phase 6 Stage 1A Sunsteel items.
 * Every offensive skill filters targets through {@link KidSafeTargets}, Mana costs go through
 * {@link ManaService}, and temporary Solar Flare motes only ever occupy air blocks.
 */
public final class SunsteelAbilities {
    public static final int SOLAR_SLASH_MANA = 15;
    public static final int SOLAR_SLASH_COOLDOWN = 60;
    public static final double SOLAR_SLASH_RANGE = 5.0;
    public static final float SOLAR_SLASH_DAMAGE = 6.0F;

    public static final int SOLAR_FLARE_MANA = 10;
    public static final int SOLAR_FLARE_COOLDOWN = 80;
    public static final int SOLAR_FLARE_DURATION_TICKS = 300;
    public static final int SOLAR_FLARE_LIGHT_LEVEL = 15;

    public static final int SUNLIT_MINING_HASTE_TICKS = 60;

    public static final int SUN_CLEAVE_MANA = 15;
    public static final int SUN_CLEAVE_COOLDOWN = 80;
    public static final double SUN_CLEAVE_RANGE = 3.5;
    public static final float SUN_CLEAVE_DAMAGE = 6.0F;
    public static final int SUN_CLEAVE_SLOW_TICKS = 40;

    public static final int WARM_SCOOP_SPEED_TICKS = 50;
    public static final double WARM_SCOOP_PULL_RADIUS = 3.0;

    public static final int SUNBURST_GROWTH_MANA = 15;
    public static final int SUNBURST_GROWTH_COOLDOWN = 80;

    private record LightKey(ResourceKey<Level> dimension, BlockPos pos) {}

    private record PendingDropPull(ResourceKey<Level> dimension, UUID minerId, BlockPos pos) {}

    private static final Map<LightKey, Long> TEMPORARY_LIGHTS = new ConcurrentHashMap<>();
    private static final List<PendingDropPull> PENDING_DROP_PULLS = new ArrayList<>();

    private SunsteelAbilities() {}

    /** Consumes Mana for an ability in Survival, or succeeds freely in Creative. */
    public static boolean consumeAbilityMana(Player player, int cost) {
        if (player.isCreative()) {
            return true;
        }
        return ManaService.tryConsumeMana(player, cost);
    }

    /** Sunsteel Sword active: 5-block golden arc dealing 6.0 dmg, Glowing, knockback, and Speed I. */
    public static boolean castSolarSlash(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!consumeAbilityMana(player, SOLAR_SLASH_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, SOLAR_SLASH_COOLDOWN);

        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition();
        double maxDistSqr = SOLAR_SLASH_RANGE * SOLAR_SLASH_RANGE;
        AABB searchBox = player.getBoundingBox().inflate(SOLAR_SLASH_RANGE);

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity.distanceToSqr(player) <= maxDistSqr
                        && KidSafeTargets.isValidOffensiveTarget(player, entity)
                        && isInFrontCone(origin, look, entity, 0.2));

        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().playerAttack(player), SOLAR_SLASH_DAMAGE);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));

            double rx = target.getX() - player.getX();
            double rz = target.getZ() - player.getZ();
            double horiz = Math.hypot(rx, rz);
            double dirX = horiz > 1.0E-4 ? rx / horiz : look.x;
            double dirZ = horiz > 1.0E-4 ? rz / horiz : look.z;
            target.setDeltaMovement(dirX * 0.55, Math.max(target.getDeltaMovement().y, 0.25), dirZ * 0.55);
            target.hurtMarked = true;
            target.hasImpulse = true;
        }

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));

        for (int i = -3; i <= 3; i++) {
            double yawOffset = Math.toRadians(i * 12.0);
            double cos = Math.cos(yawOffset);
            double sin = Math.sin(yawOffset);
            double ax = look.x * cos - look.z * sin;
            double az = look.x * sin + look.z * cos;
            double px = player.getX() + ax * 2.4;
            double py = player.getY(0.55);
            double pz = player.getZ() + az * 2.4;
            level.sendParticles(ParticleTypes.FLAME, px, py, pz, 2, 0.1, 0.1, 0.1, 0.02);
            level.sendParticles(ParticleTypes.WAX_ON, px, py, pz, 2, 0.12, 0.12, 0.12, 0.02);
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                player.getX() + look.x * 1.8, player.getY(0.6), player.getZ() + look.z * 1.8,
                2, 0.2, 0.1, 0.2, 0.0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.1F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.55F, 1.35F);
        return true;
    }

    /** Sunsteel Pickaxe passive: mining any ore block grants Haste I (60t). */
    public static void onSunlitMining(ServerLevel level, LivingEntity miner, BlockState state, BlockPos pos) {
        if (!state.is(Tags.Blocks.ORES)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, SUNLIT_MINING_HASTE_TICKS, 0));
        level.sendParticles(ParticleTypes.WAX_ON,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                6, 0.25, 0.25, 0.25, 0.02);
    }

    /**
     * Sunsteel Pickaxe active: places a temporary level-15 {@code minecraft:light} mote in air only
     * (never overwriting non-air or BlockEntity blocks) and schedules removal after 300 ticks.
     */
    public static boolean castSolarFlare(ServerLevel level, Player player, Item item, BlockPos targetPos) {
        if (!level.isInWorldBounds(targetPos)
                || !level.getBlockState(targetPos).isAir()
                || level.getBlockEntity(targetPos) != null) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!consumeAbilityMana(player, SOLAR_FLARE_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, SOLAR_FLARE_COOLDOWN);

        BlockPos immutablePos = targetPos.immutable();
        level.setBlock(immutablePos,
                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, SOLAR_FLARE_LIGHT_LEVEL), 3);
        TEMPORARY_LIGHTS.put(
                new LightKey(level.dimension(), immutablePos),
                level.getGameTime() + SOLAR_FLARE_DURATION_TICKS);

        level.sendParticles(ParticleTypes.WAX_ON,
                immutablePos.getX() + 0.5, immutablePos.getY() + 0.5, immutablePos.getZ() + 0.5,
                10, 0.25, 0.25, 0.25, 0.03);
        level.sendParticles(ParticleTypes.FLAME,
                immutablePos.getX() + 0.5, immutablePos.getY() + 0.5, immutablePos.getZ() + 0.5,
                5, 0.15, 0.15, 0.15, 0.02);
        level.playSound(null, immutablePos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.4F);
        return true;
    }

    /** Sunsteel Axe active: 3.5-block front sweep dealing 6.0 dmg + Slowness I (40t) to hostiles. */
    public static boolean castSunCleave(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!consumeAbilityMana(player, SUN_CLEAVE_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, SUN_CLEAVE_COOLDOWN);

        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition();
        double maxDistSqr = SUN_CLEAVE_RANGE * SUN_CLEAVE_RANGE;
        AABB searchBox = player.getBoundingBox().inflate(SUN_CLEAVE_RANGE);

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity.distanceToSqr(player) <= maxDistSqr
                        && KidSafeTargets.isValidOffensiveTarget(player, entity)
                        && isInFrontCone(origin, look, entity, 0.15));

        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().playerAttack(player), SUN_CLEAVE_DAMAGE);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SUN_CLEAVE_SLOW_TICKS, 0));
        }

        level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                player.getX() + look.x * 1.6, player.getY(0.6), player.getZ() + look.z * 1.6,
                2, 0.2, 0.1, 0.2, 0.0);
        level.sendParticles(ParticleTypes.FLAME,
                player.getX() + look.x * 1.6, player.getY(0.6), player.getZ() + look.z * 1.6,
                8, 0.4, 0.2, 0.4, 0.03);
        level.sendParticles(ParticleTypes.WAX_ON,
                player.getX() + look.x * 1.6, player.getY(0.6), player.getZ() + look.z * 1.6,
                8, 0.4, 0.2, 0.4, 0.03);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 0.95F);
        return true;
    }

    /** Sunsteel Shovel passive: breaking shovel-mineable blocks grants Speed I and pulls drops. */
    public static void onWarmScoop(ServerLevel level, LivingEntity miner, BlockState state, BlockPos pos) {
        if (!state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, WARM_SCOOP_SPEED_TICKS, 0));
        pullNearbyDrops(level, miner, pos);
        synchronized (PENDING_DROP_PULLS) {
            PENDING_DROP_PULLS.add(new PendingDropPull(level.dimension(), miner.getUUID(), pos.immutable()));
        }
        level.sendParticles(ParticleTypes.WAX_ON,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                5, 0.25, 0.2, 0.25, 0.02);
    }

    /** Sunsteel Hoe active: applies a 3x3 bone-meal growth pulse around the targeted crop or sapling. */
    public static boolean castSunburstGrowth(ServerLevel level, Player player, Item item, BlockPos centerPos) {
        if (!isSunburstTarget(level.getBlockState(centerPos))) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!consumeAbilityMana(player, SUNBURST_GROWTH_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, SUNBURST_GROWTH_COOLDOWN);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos target = centerPos.offset(dx, 0, dz);
                BlockState state = level.getBlockState(target);
                if (isSunburstTarget(state) && state.getBlock() instanceof BonemealableBlock bonemealable
                        && bonemealable.isValidBonemealTarget(level, target, state)) {
                    bonemealable.performBonemeal(level, level.random, target, state);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            target.getX() + 0.5, target.getY() + 0.4, target.getZ() + 0.5,
                            4, 0.25, 0.2, 0.25, 0.02);
                    level.sendParticles(ParticleTypes.WAX_ON,
                            target.getX() + 0.5, target.getY() + 0.4, target.getZ() + 0.5,
                            2, 0.2, 0.2, 0.2, 0.02);
                }
            }
        }
        level.playSound(null, centerPos, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.1F);
        return true;
    }

    /** True when the block is a crop, sapling, stem, or berry bush eligible for Sunburst Growth. */
    public static boolean isSunburstTarget(BlockState state) {
        if (!(state.getBlock() instanceof BonemealableBlock)) {
            return false;
        }
        return state.is(BlockTags.CROPS)
                || state.is(BlockTags.SAPLINGS)
                || state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof SaplingBlock
                || state.getBlock() instanceof StemBlock
                || state.getBlock() instanceof SweetBerryBushBlock;
    }

    /** Expires temporary Solar Flare lights and processes queued Warm Scoop drop pulls for {@code level}. */
    public static void tickServerLevel(ServerLevel level) {
        long gameTime = level.getGameTime();
        ResourceKey<Level> dimension = level.dimension();

        Iterator<Map.Entry<LightKey, Long>> lightIt = TEMPORARY_LIGHTS.entrySet().iterator();
        while (lightIt.hasNext()) {
            Map.Entry<LightKey, Long> entry = lightIt.next();
            LightKey key = entry.getKey();
            if (!key.dimension().equals(dimension)) {
                continue;
            }
            if (gameTime >= entry.getValue()) {
                lightIt.remove();
                if (level.isLoaded(key.pos()) && level.getBlockState(key.pos()).is(Blocks.LIGHT)) {
                    level.setBlock(key.pos(), Blocks.AIR.defaultBlockState(), 3);
                }
            } else if (gameTime % 15 == 0 && level.isLoaded(key.pos())) {
                level.sendParticles(ParticleTypes.WAX_ON,
                        key.pos().getX() + 0.5, key.pos().getY() + 0.5, key.pos().getZ() + 0.5,
                        1, 0.1, 0.1, 0.1, 0.01);
            }
        }

        List<PendingDropPull> pulls = List.of();
        synchronized (PENDING_DROP_PULLS) {
            if (!PENDING_DROP_PULLS.isEmpty()) {
                List<PendingDropPull> remaining = new ArrayList<>();
                List<PendingDropPull> due = new ArrayList<>();
                for (PendingDropPull pull : PENDING_DROP_PULLS) {
                    if (pull.dimension().equals(dimension)) {
                        due.add(pull);
                    } else {
                        remaining.add(pull);
                    }
                }
                PENDING_DROP_PULLS.clear();
                PENDING_DROP_PULLS.addAll(remaining);
                pulls = due;
            }
        }
        for (PendingDropPull pull : pulls) {
            Entity entity = level.getEntity(pull.minerId());
            if (entity instanceof LivingEntity miner && miner.isAlive()) {
                pullNearbyDrops(level, miner, pull.pos());
            }
        }
    }

    private static void pullNearbyDrops(ServerLevel level, LivingEntity miner, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(pos).inflate(WARM_SCOOP_PULL_RADIUS);
        double maxDistSqr = WARM_SCOOP_PULL_RADIUS * WARM_SCOOP_PULL_RADIUS;
        List<ItemEntity> drops = level.getEntitiesOfClass(
                ItemEntity.class,
                box,
                item -> item.isAlive() && item.position().distanceToSqr(center) <= maxDistSqr);

        Vec3 target = miner.position().add(0.0, 0.5, 0.0);
        for (ItemEntity drop : drops) {
            Vec3 toMiner = target.subtract(drop.position());
            double dist = toMiner.length();
            if (dist > 0.05) {
                Vec3 dir = toMiner.scale(1.0 / dist);
                drop.setDeltaMovement(dir.scale(Math.min(0.45, Math.max(0.2, dist * 0.25))));
                drop.setPos(drop.position().add(dir.scale(Math.min(0.35, dist * 0.25))));
                drop.hasImpulse = true;
            }
        }
    }

    private static boolean isInFrontCone(Vec3 origin, Vec3 look, LivingEntity entity, double minDot) {
        Vec3 toEntity = entity.getBoundingBox().getCenter().subtract(origin);
        double distSqr = toEntity.lengthSqr();
        if (distSqr <= 2.25) {
            return true;
        }
        return look.dot(toEntity.normalize()) >= minDot;
    }
}
