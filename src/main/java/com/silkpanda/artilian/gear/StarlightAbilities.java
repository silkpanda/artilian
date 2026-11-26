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
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side Cosmic / Astral ability service for all five Phase 6 Stage 1B Starlight items.
 * Every offensive and pull skill filters targets through {@link KidSafeTargets}, Mana costs go
 * through {@link ManaService}, Starstep Dash stops before solid blocks, and Constellation Pulse
 * performs a strictly read-only ore scan.
 */
public final class StarlightAbilities {
    public static final int STARSTEP_DASH_MANA = 20;
    public static final int STARSTEP_DASH_COOLDOWN = 70;
    public static final double STARSTEP_DASH_RANGE = 6.0;
    public static final float STARSTEP_DASH_DAMAGE = 7.5F;
    public static final int STARSTEP_DASH_LEVITATION_TICKS = 30;
    public static final int STARSTEP_DASH_SLOW_FALLING_TICKS = 40;

    public static final int STARLIT_MAGNET_HASTE_TICKS = 80;
    public static final double STARLIT_MAGNET_PULL_RADIUS = 6.0;

    public static final int CONSTELLATION_PULSE_MANA = 15;
    public static final int CONSTELLATION_PULSE_COOLDOWN = 100;
    public static final int CONSTELLATION_PULSE_NIGHT_VISION_TICKS = 160;
    public static final int CONSTELLATION_PULSE_SCAN_RADIUS = 12;

    public static final int GRAVITY_VORTEX_MANA = 20;
    public static final int GRAVITY_VORTEX_COOLDOWN = 90;
    public static final double GRAVITY_VORTEX_AHEAD = 3.5;
    public static final double GRAVITY_VORTEX_RADIUS = 5.0;
    public static final float GRAVITY_VORTEX_DAMAGE = 7.0F;
    public static final int GRAVITY_VORTEX_LEVITATION_TICKS = 35;

    public static final int METEOR_SCOOP_SPEED_TICKS = 60;
    public static final double METEOR_SCOOP_PULL_RADIUS = 5.0;

    public static final int STAR_HOP_MANA = 15;
    public static final int STAR_HOP_COOLDOWN = 80;
    public static final int STAR_HOP_SLOW_FALLING_TICKS = 60;

    public static final int ASTRAL_BLOOM_MANA = 20;
    public static final int ASTRAL_BLOOM_COOLDOWN = 90;
    public static final int ASTRAL_BLOOM_RADIUS = 2;

    private record PendingDropPull(ResourceKey<Level> dimension, UUID minerId, BlockPos pos, double radius) {}

    private static final List<PendingDropPull> PENDING_DROP_PULLS = new ArrayList<>();

    private StarlightAbilities() {}

    /**
     * Starlight Sword active: safe 6-block astral lunge (stopping before solid blocks),
     * dealing 7.5 damage + Levitation I (30t) to KidSafeTargets hostiles along the path and
     * granting Slow Falling (40t) to the caster.
     */
    public static boolean castStarstepDash(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, STARSTEP_DASH_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, STARSTEP_DASH_COOLDOWN);

        Vec3 start = player.position();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 safePos = start;
        Set<LivingEntity> hitTargets = new LinkedHashSet<>();

        for (double step = 0.5; step <= STARSTEP_DASH_RANGE; step += 0.5) {
            Vec3 candidate = start.add(look.scale(step));
            BlockPos footPos = BlockPos.containing(candidate.x, candidate.y, candidate.z);
            BlockPos headPos = BlockPos.containing(candidate.x, candidate.y + 1.0, candidate.z);

            if (!isPassable(level, footPos) || !isPassable(level, headPos)) {
                break;
            }
            safePos = candidate;

            AABB stepBox = new AABB(
                    candidate.x - 1.75, candidate.y - 0.5, candidate.z - 1.75,
                    candidate.x + 1.75, candidate.y + 2.0, candidate.z + 1.75);
            List<LivingEntity> candidates = level.getEntitiesOfClass(
                    LivingEntity.class,
                    stepBox,
                    entity -> KidSafeTargets.isValidOffensiveTarget(player, entity));
            hitTargets.addAll(candidates);

            level.sendParticles(ParticleTypes.END_ROD,
                    candidate.x, candidate.y + 0.9, candidate.z,
                    2, 0.15, 0.15, 0.15, 0.02);
            level.sendParticles(ParticleTypes.GLOW,
                    candidate.x, candidate.y + 0.9, candidate.z,
                    2, 0.18, 0.18, 0.18, 0.01);
        }

        for (LivingEntity target : hitTargets) {
            target.hurt(level.damageSources().playerAttack(player), STARSTEP_DASH_DAMAGE);
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, STARSTEP_DASH_LEVITATION_TICKS, 0));
        }

        player.teleportTo(safePos.x, safePos.y, safePos.z);
        player.fallDistance = 0.0F;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, STARSTEP_DASH_SLOW_FALLING_TICKS, 0));

        level.playSound(null, safePos.x, safePos.y, safePos.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.85F, 1.35F);
        level.playSound(null, safePos.x, safePos.y, safePos.z,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.3F);
        return true;
    }

    private static boolean isPassable(ServerLevel level, BlockPos pos) {
        if (!level.isInWorldBounds(pos)) {
            return false;
        }
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    /** Starlight Pickaxe passive: mining ores grants Haste I (80t) and pulls ore drops within 6 blocks. */
    public static void onStarlitMining(ServerLevel level, LivingEntity miner, BlockState state, BlockPos pos) {
        if (!state.is(Tags.Blocks.ORES)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, STARLIT_MAGNET_HASTE_TICKS, 0));
        pullNearbyDrops(level, miner, pos, STARLIT_MAGNET_PULL_RADIUS);
        synchronized (PENDING_DROP_PULLS) {
            PENDING_DROP_PULLS.add(new PendingDropPull(
                    level.dimension(), miner.getUUID(), pos.immutable(), STARLIT_MAGNET_PULL_RADIUS));
        }
        level.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                6, 0.25, 0.25, 0.25, 0.02);
        level.sendParticles(ParticleTypes.GLOW,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                4, 0.25, 0.25, 0.25, 0.01);
    }

    /**
     * Starlight Pickaxe active: grants Night Vision (160t) and performs a strictly read-only
     * 12-block ore scan marking nearby ores with cyan starlight particle beacons.
     *
     * @return number of ore blocks detected, or {@code -1} if on cooldown / insufficient Mana
     */
    public static int castConstellationPulse(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return -1;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, CONSTELLATION_PULSE_MANA)) {
            return -1;
        }
        player.getCooldowns().addCooldown(item, CONSTELLATION_PULSE_COOLDOWN);
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, CONSTELLATION_PULSE_NIGHT_VISION_TICKS, 0));

        BlockPos origin = player.blockPosition();
        int radius = CONSTELLATION_PULSE_SCAN_RADIUS;
        int radiusSqr = radius * radius;
        int oreCount = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSqr) {
                        continue;
                    }
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (!level.isInWorldBounds(pos)) {
                        continue;
                    }
                    if (level.getBlockState(pos).is(Tags.Blocks.ORES)) {
                        oreCount++;
                        level.sendParticles(ParticleTypes.END_ROD,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                2, 0.15, 0.25, 0.15, 0.01);
                        level.sendParticles(ParticleTypes.GLOW,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                2, 0.2, 0.2, 0.2, 0.01);
                    }
                }
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.45F);
        return oreCount;
    }

    /**
     * Starlight Axe active: pulls KidSafeTargets hostiles within 5 blocks inward to a point 3.5 blocks
     * ahead, dealing 7.0 damage + Levitation I (35t).
     */
    public static boolean castGravityVortex(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, GRAVITY_VORTEX_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, GRAVITY_VORTEX_COOLDOWN);

        Vec3 look = player.getLookAngle().normalize();
        Vec3 vortexCenter = player.getEyePosition().add(look.scale(GRAVITY_VORTEX_AHEAD));
        double maxDistSqr = GRAVITY_VORTEX_RADIUS * GRAVITY_VORTEX_RADIUS;
        AABB searchBox = new AABB(vortexCenter, vortexCenter).inflate(GRAVITY_VORTEX_RADIUS);

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity.position().distanceToSqr(vortexCenter) <= maxDistSqr
                        && KidSafeTargets.isValidOffensiveTarget(player, entity));

        for (LivingEntity target : targets) {
            Vec3 toCenter = vortexCenter.subtract(target.position());
            double dist = toCenter.length();
            if (dist > 0.4) {
                Vec3 dir = toCenter.scale(1.0 / dist);
                target.setDeltaMovement(dir.x * 0.65, Math.max(target.getDeltaMovement().y, 0.25), dir.z * 0.65);
                target.setPos(target.position().add(dir.scale(Math.min(1.25, dist * 0.45))));
                target.hurtMarked = true;
                target.hasImpulse = true;
            }
            target.hurt(level.damageSources().playerAttack(player), GRAVITY_VORTEX_DAMAGE);
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, GRAVITY_VORTEX_LEVITATION_TICKS, 0));
        }

        for (int i = 0; i < 12; i++) {
            double angle = (Math.PI * 2.0 * i) / 12.0;
            double px = vortexCenter.x + Math.cos(angle) * 1.8;
            double pz = vortexCenter.z + Math.sin(angle) * 1.8;
            level.sendParticles(ParticleTypes.PORTAL, px, vortexCenter.y, pz, 3, 0.15, 0.2, 0.15, 0.05);
            level.sendParticles(ParticleTypes.END_ROD, px, vortexCenter.y, pz, 1, 0.1, 0.1, 0.1, 0.01);
        }
        level.playSound(null, vortexCenter.x, vortexCenter.y, vortexCenter.z,
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 1.25F);
        return true;
    }

    /** Starlight Shovel passive: breaking shovel-mineable blocks grants Speed I (60t) and pulls drops within 5 blocks. */
    public static void onMeteorScoop(ServerLevel level, LivingEntity miner, BlockState state, BlockPos pos) {
        if (!state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, METEOR_SCOOP_SPEED_TICKS, 0));
        pullNearbyDrops(level, miner, pos, METEOR_SCOOP_PULL_RADIUS);
        synchronized (PENDING_DROP_PULLS) {
            PENDING_DROP_PULLS.add(new PendingDropPull(
                    level.dimension(), miner.getUUID(), pos.immutable(), METEOR_SCOOP_PULL_RADIUS));
        }
        level.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                5, 0.25, 0.2, 0.25, 0.02);
    }

    /** Starlight Shovel active: launches the player upward/forward in a low-gravity hop with Slow Falling (60t). */
    public static boolean castStarHop(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, STAR_HOP_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, STAR_HOP_COOLDOWN);

        Vec3 look = player.getLookAngle();
        double horizLen = Math.hypot(look.x, look.z);
        double hx = horizLen > 1.0E-4 ? look.x / horizLen : 0.0;
        double hz = horizLen > 1.0E-4 ? look.z / horizLen : 0.0;

        player.setDeltaMovement(hx * 0.65, 0.85, hz * 0.65);
        player.hurtMarked = true;
        player.hasImpulse = true;
        player.fallDistance = 0.0F;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, STAR_HOP_SLOW_FALLING_TICKS, 0));

        level.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY(0.25), player.getZ(),
                12, 0.35, 0.15, 0.35, 0.04);
        level.sendParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY(0.1), player.getZ(),
                8, 0.3, 0.1, 0.3, 0.03);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
        return true;
    }

    /** True when the block is a crop, sapling, stem, berry bush, or farmland eligible for Astral Bloom. */
    public static boolean isAstralBloomTarget(BlockState state) {
        return SunsteelAbilities.isSunburstTarget(state) || state.is(Blocks.FARMLAND);
    }

    /** Starlight Hoe active: applies a 5x5 bone-meal growth pulse and hydrates dry farmland in 5x5. */
    public static boolean castAstralBloom(ServerLevel level, Player player, Item item, BlockPos centerPos) {
        BlockState centerState = level.getBlockState(centerPos);
        if (!isAstralBloomTarget(centerState)) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, ASTRAL_BLOOM_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, ASTRAL_BLOOM_COOLDOWN);

        int farmlandY = centerState.is(Blocks.FARMLAND) ? centerPos.getY() : centerPos.getY() - 1;
        int cropY = centerState.is(Blocks.FARMLAND) ? centerPos.getY() + 1 : centerPos.getY();

        for (int dx = -ASTRAL_BLOOM_RADIUS; dx <= ASTRAL_BLOOM_RADIUS; dx++) {
            for (int dz = -ASTRAL_BLOOM_RADIUS; dz <= ASTRAL_BLOOM_RADIUS; dz++) {
                BlockPos farmPos = new BlockPos(centerPos.getX() + dx, farmlandY, centerPos.getZ() + dz);
                BlockState farmState = level.getBlockState(farmPos);
                if (farmState.is(Blocks.FARMLAND)
                        && farmState.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
                    level.setBlock(farmPos, farmState.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
                }

                BlockPos cropPos = new BlockPos(centerPos.getX() + dx, cropY, centerPos.getZ() + dz);
                BlockState cropState = level.getBlockState(cropPos);
                if (SunsteelAbilities.isSunburstTarget(cropState)
                        && cropState.getBlock() instanceof BonemealableBlock bonemealable
                        && bonemealable.isValidBonemealTarget(level, cropPos, cropState)) {
                    bonemealable.performBonemeal(level, level.random, cropPos, cropState);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                            4, 0.25, 0.2, 0.25, 0.02);
                    level.sendParticles(ParticleTypes.GLOW,
                            cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                            2, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }

        level.playSound(null, centerPos, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.15F);
        level.playSound(null, centerPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.35F);
        return true;
    }

    /** Processes queued Starlit Magnet and Meteor Scoop drop pulls for {@code level}. */
    public static void tickServerLevel(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
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
                pullNearbyDrops(level, miner, pull.pos(), pull.radius());
            }
        }
    }

    private static void pullNearbyDrops(ServerLevel level, LivingEntity miner, BlockPos pos, double radius) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(pos).inflate(radius);
        double maxDistSqr = radius * radius;
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
                drop.setDeltaMovement(dir.scale(Math.min(0.5, Math.max(0.22, dist * 0.25))));
                drop.setPos(drop.position().add(dir.scale(Math.min(0.4, dist * 0.25))));
                drop.hasImpulse = true;
            }
        }
    }
}
