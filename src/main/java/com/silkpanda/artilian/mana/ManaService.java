package com.silkpanda.artilian.mana;

import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.safety.KidSafeTargets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;

/**
 * Server-side rules for Mana regeneration, syncing, cost enforcement, and wand spell execution.
 */
public final class ManaService {
    private static final double SPARK_RANGE = 16.0;
    private static final double SPARK_AIM_RADIUS = 1.75;
    private static final double SPARK_CHAIN_RADIUS = 4.5;
    private static final int SPARK_MAX_CHAINS = 2;

    private static final double HEART_BLOOM_RADIUS = 8.0;
    private static final double ZEPHYR_GUST_RADIUS = 4.0;
    private static final double PRISM_NOVA_RADIUS = 6.5;
    private static final double METEOR_RANGE = 20.0;
    private static final double METEOR_IMPACT_RADIUS = 4.0;
    private static final double TWIN_STAR_HARMONY_RADIUS = 8.0;

    private ManaService() {}

    /** Pushes the player's current Mana state to their client. */
    public static void sync(ServerPlayer player) {
        ManaData data = ManaData.get(player);
        data.recordSync();
        if (player.connection != null
                && player.connection.getConnection().channel() != null
                && player.connection.hasChannel(ManaSyncPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, new ManaSyncPayload(data.getMana(), data.getMaxMana()));
        }
    }

    /** Advances Mana regeneration by one tick and syncs if Mana changed. */
    public static void tickPlayer(ServerPlayer player) {
        if (!player.isAlive()) {
            return;
        }
        if (ManaData.get(player).tickServer()) {
            sync(player);
        }
    }

    /**
     * Checks and consumes {@code cost} Mana server-side, syncing on success or showing an
     * action-bar feedback cue on failure.
     */
    public static boolean tryConsumeMana(Player player, int cost) {
        ManaData data = ManaData.get(player);
        if (data.trySpend(cost)) {
            if (player instanceof ServerPlayer serverPlayer) {
                sync(serverPlayer);
            }
            return true;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.artilian.mana.not_enough", cost, data.getMana())
                            .withStyle(ChatFormatting.AQUA),
                    true);
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.6F);
        }
        return false;
    }

    /** Validates cooldown and Mana, applies cooldown, and executes {@code spell}. */
    public static boolean castWand(ServerLevel level, Player player, ArtilianWandItem item, WandSpell spell) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!tryConsumeMana(player, spell.manaCost())) {
            return false;
        }
        player.getCooldowns().addCooldown(item, spell.cooldownTicks());
        switch (spell) {
            case SPARK -> castSparkWand(level, player);
            case HEART_BLOOM -> castHeartBloomStaff(level, player);
            case ZEPHYR_DASH -> castZephyrDashWand(level, player);
            case PRISM_NOVA -> castPrismNovaStaff(level, player);
            case BRIMSTONE_METEOR -> castBrimstoneMeteorStaff(level, player);
            case TWIN_STAR_HARMONY -> castTwinStarHarmonyStaff(level, player);
        }
        return true;
    }

    /** Kid-safe offensive target filter. The rule itself lives in {@link KidSafeTargets}. */
    public static boolean isValidOffensiveTarget(Player caster, LivingEntity entity) {
        return KidSafeTargets.isValidOffensiveTarget(caster, entity);
    }

    /** Ally filter for Heart-Bloom healing: heals Players, Chester, tamed pets, and passive animals. */
    public static boolean isValidHealingTarget(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator() || entity instanceof Enemy) {
            return false;
        }
        return entity instanceof Player
                || entity instanceof ChesterEntity
                || (entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null)
                || (entity instanceof TamableAnimal tamable && tamable.isTame())
                || (entity instanceof AbstractHorse horse && horse.isTamed())
                || entity instanceof Animal;
    }

    private static void castSparkWand(ServerLevel level, Player caster) {
        Vec3 eyePos = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        // The spark stops at the first solid block, so it cannot reach through a wall.
        Vec3 fullReach = eyePos.add(look.scale(SPARK_RANGE));
        BlockHitResult wall = level.clip(new ClipContext(
                eyePos, fullReach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 rayEnd = wall.getType() == HitResult.Type.MISS ? fullReach : wall.getLocation();
        double rayLength = eyePos.distanceTo(rayEnd);

        emitParticleLine(level, eyePos, rayEnd, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.END_ROD, 24);
        level.sendParticles(ParticleTypes.WAX_ON,
                eyePos.x + look.x * 1.2, eyePos.y + look.y * 1.2, eyePos.z + look.z * 1.2,
                8, 0.15, 0.15, 0.15, 0.04);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.35F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.9F, 1.25F);

        AABB searchBox = new AABB(eyePos, rayEnd).inflate(SPARK_AIM_RADIUS + 1.0);
        List<LivingEntity> rayHits = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> isValidOffensiveTarget(caster, entity)
                        && isNearRay(entity, eyePos, rayEnd, look, rayLength)
                        && caster.hasLineOfSight(entity))
                .stream()
                .sorted(monstersFirstThenNearest(caster))
                .toList();

        if (rayHits.isEmpty()) {
            return;
        }

        LivingEntity primary = rayHits.get(0);
        primary.hurt(level.damageSources().indirectMagic(caster, caster), 6.0F);
        primary.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
        primary.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                primary.getX(), primary.getY(0.5), primary.getZ(),
                18, 0.35, 0.45, 0.35, 0.1);

        AABB chainBox = primary.getBoundingBox().inflate(SPARK_CHAIN_RADIUS);
        double chainMaxSqr = SPARK_CHAIN_RADIUS * SPARK_CHAIN_RADIUS;
        List<LivingEntity> chains = level.getEntitiesOfClass(
                LivingEntity.class,
                chainBox,
                entity -> entity != primary
                        && isValidOffensiveTarget(caster, entity)
                        && entity.distanceToSqr(primary) <= chainMaxSqr
                        && primary.hasLineOfSight(entity))
                .stream()
                .sorted(monstersFirstThenNearest(primary))
                .limit(SPARK_MAX_CHAINS)
                .toList();

        Vec3 primaryCenter = primary.getBoundingBox().getCenter();
        for (LivingEntity chain : chains) {
            chain.hurt(level.damageSources().indirectMagic(caster, caster), 4.0F);
            chain.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            emitParticleLine(level, primaryCenter, chain.getBoundingBox().getCenter(),
                    ParticleTypes.ELECTRIC_SPARK, ParticleTypes.END_ROD, 10);
        }
    }

    /** A monster always outranks an animal, however close the animal is. Then nearest wins. */
    private static Comparator<LivingEntity> monstersFirstThenNearest(Entity from) {
        return Comparator.comparing((LivingEntity entity) -> !KidSafeTargets.isHostile(entity))
                .thenComparingDouble(from::distanceToSqr);
    }

    private static boolean isNearRay(LivingEntity target, Vec3 eyePos, Vec3 rayEnd, Vec3 look, double rayLength) {
        if (target.getBoundingBox().inflate(SPARK_AIM_RADIUS).clip(eyePos, rayEnd).isPresent()) {
            return true;
        }
        Vec3 center = target.getBoundingBox().getCenter();
        double along = Mth.clamp(center.subtract(eyePos).dot(look), 0.0, rayLength);
        Vec3 closest = eyePos.add(look.scale(along));
        return closest.distanceTo(center) <= SPARK_AIM_RADIUS;
    }

    private static void castHeartBloomStaff(ServerLevel level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.85F, 1.4F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.9F, 1.2F);

        for (int i = 0; i < 24; i++) {
            double angle = (Math.PI * 2.0 * i) / 24.0;
            double px = caster.getX() + Math.cos(angle) * HEART_BLOOM_RADIUS;
            double pz = caster.getZ() + Math.sin(angle) * HEART_BLOOM_RADIUS;
            level.sendParticles(ParticleTypes.HEART, px, caster.getY() + 0.4, pz, 1, 0.1, 0.15, 0.1, 0.02);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, caster.getY() + 0.2, pz, 2, 0.2, 0.2, 0.2, 0.05);
        }

        AABB area = caster.getBoundingBox().inflate(HEART_BLOOM_RADIUS);
        double maxDistSqr = HEART_BLOOM_RADIUS * HEART_BLOOM_RADIUS;
        List<LivingEntity> allies = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.distanceToSqr(caster) <= maxDistSqr && isValidHealingTarget(entity));

        for (LivingEntity ally : allies) {
            ally.heal(6.0F);
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0));
            ally.removeEffect(MobEffects.POISON);
            ally.removeEffect(MobEffects.WITHER);
            ally.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            ally.removeEffect(MobEffects.WEAKNESS);
            ally.clearFire();
            level.sendParticles(ParticleTypes.HEART,
                    ally.getX(), ally.getY(0.8), ally.getZ(),
                    5, 0.3, 0.3, 0.3, 0.02);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    ally.getX(), ally.getY(0.5), ally.getZ(),
                    6, 0.35, 0.35, 0.35, 0.04);
        }
    }

    private static void castZephyrDashWand(ServerLevel level, Player caster) {
        Vec3 look = caster.getLookAngle().normalize();
        double dx = look.x * 1.45;
        double dz = look.z * 1.45;
        double dy = Math.max(0.55, look.y * 0.9 + 0.35);

        caster.setDeltaMovement(dx, dy, dz);
        caster.hurtMarked = true;
        caster.hasImpulse = true;
        caster.fallDistance = 0.0F;

        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
        caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));

        level.sendParticles(ParticleTypes.CLOUD, caster.getX(), caster.getY() + 0.3, caster.getZ(), 20, 0.6, 0.3, 0.6, 0.12);
        level.sendParticles(ParticleTypes.POOF, caster.getX(), caster.getY() + 0.3, caster.getZ(), 14, 0.5, 0.2, 0.5, 0.08);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.1F, 1.15F);

        AABB gustArea = caster.getBoundingBox().inflate(ZEPHYR_GUST_RADIUS);
        double maxDistSqr = ZEPHYR_GUST_RADIUS * ZEPHYR_GUST_RADIUS;
        List<LivingEntity> foes = level.getEntitiesOfClass(
                LivingEntity.class,
                gustArea,
                entity -> entity.distanceToSqr(caster) <= maxDistSqr && isValidOffensiveTarget(caster, entity));

        for (LivingEntity foe : foes) {
            double rx = foe.getX() - caster.getX();
            double rz = foe.getZ() - caster.getZ();
            double horiz = Math.hypot(rx, rz);
            double dirX = horiz > 1.0E-4 ? rx / horiz : look.x;
            double dirZ = horiz > 1.0E-4 ? rz / horiz : look.z;
            double dirNorm = Math.hypot(dirX, dirZ);
            if (dirNorm > 1.0E-4) {
                dirX /= dirNorm;
                dirZ /= dirNorm;
            } else {
                dirX = 1.0;
                dirZ = 0.0;
            }
            foe.setDeltaMovement(dirX * 0.8, Math.max(foe.getDeltaMovement().y, 0.35), dirZ * 0.8);
            foe.hasImpulse = true;
            foe.hurtMarked = true;
        }
    }

    private static void castPrismNovaStaff(ServerLevel level, Player caster) {
        Vec3 center = caster.position();
        AABB box = caster.getBoundingBox().inflate(PRISM_NOVA_RADIUS);
        List<LivingEntity> foes = level.getEntitiesOfClass(
                LivingEntity.class, box, e -> isValidOffensiveTarget(caster, e)
                        && e.distanceToSqr(center) <= PRISM_NOVA_RADIUS * PRISM_NOVA_RADIUS);

        for (LivingEntity foe : foes) {
            foe.hurt(level.damageSources().indirectMagic(caster, caster), 10.0F);
            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            double dx = foe.getX() - caster.getX();
            double dz = foe.getZ() - caster.getZ();
            double dist = Math.hypot(dx, dz);
            if (dist > 1.0E-4) {
                foe.push((dx / dist) * 0.55, 0.25, (dz / dist) * 0.55);
                foe.hurtMarked = true;
            }
        }

        for (int i = 0; i < 24; i++) {
            double angle = (Math.PI * 2.0 * i) / 24.0;
            double px = caster.getX() + Math.cos(angle) * 2.8;
            double pz = caster.getZ() + Math.sin(angle) * 2.8;
            level.sendParticles(ParticleTypes.END_ROD, px, caster.getY() + 0.8, pz, 2, 0.15, 0.2, 0.15, 0.05);
            level.sendParticles(ParticleTypes.PORTAL, px, caster.getY() + 0.6, pz, 2, 0.2, 0.2, 0.2, 0.05);
        }
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.1F);
    }

    private static void castBrimstoneMeteorStaff(ServerLevel level, Player caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 maxEnd = eye.add(look.scale(METEOR_RANGE));
        BlockHitResult blockHit = level.clip(new ClipContext(
                eye, maxEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 blockImpact = blockHit.getType() == HitResult.Type.MISS ? maxEnd : blockHit.getLocation();

        // Snap impact to nearest offensive target along aim ray if closer than block hit
        double maxDistSqr = blockImpact.distanceToSqr(eye);
        AABB rayBox = caster.getBoundingBox().expandTowards(look.scale(METEOR_RANGE)).inflate(1.75);
        LivingEntity aimedTarget = level.getEntitiesOfClass(
                LivingEntity.class, rayBox, e -> isValidOffensiveTarget(caster, e)
                        && e.getBoundingBox().inflate(1.0).clip(eye, blockImpact).isPresent())
                .stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(eye)))
                .orElse(null);
        Vec3 impact = (aimedTarget != null && aimedTarget.distanceToSqr(eye) < maxDistSqr)
                ? aimedTarget.position()
                : blockImpact;

        AABB impactBox = AABB.ofSize(impact, METEOR_IMPACT_RADIUS * 2.0, METEOR_IMPACT_RADIUS * 2.0, METEOR_IMPACT_RADIUS * 2.0);
        List<LivingEntity> foes = level.getEntitiesOfClass(
                LivingEntity.class, impactBox, e -> isValidOffensiveTarget(caster, e)
                        && e.distanceToSqr(impact) <= METEOR_IMPACT_RADIUS * METEOR_IMPACT_RADIUS);
        for (LivingEntity foe : foes) {
            foe.hurt(level.damageSources().onFire(), 12.0F);
            foe.igniteForSeconds(3);
        }

        // Visual meteor trail from sky to impact (zero block breaking, zero fire blocks placed)
        emitParticleLine(level, impact.add(0.0, 6.0, 0.0), impact, ParticleTypes.FLAME, ParticleTypes.LAVA, 12);
        level.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y + 0.4, impact.z, 2, 0.4, 0.2, 0.4, 0.0);
        level.sendParticles(ParticleTypes.FLAME, impact.x, impact.y + 0.5, impact.z, 28, 1.2, 0.5, 1.2, 0.08);
        level.playSound(null, impact.x, impact.y, impact.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9F, 1.0F);
    }

    private static void castTwinStarHarmonyStaff(ServerLevel level, Player caster) {
        Vec3 center = caster.position();
        double maxDistSqr = TWIN_STAR_HARMONY_RADIUS * TWIN_STAR_HARMONY_RADIUS;
        AABB area = caster.getBoundingBox().inflate(TWIN_STAR_HARMONY_RADIUS);

        List<LivingEntity> allies = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.distanceToSqr(caster) <= maxDistSqr && isValidHealingTarget(entity));
        for (LivingEntity ally : allies) {
            ally.heal(4.0F);
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1));
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0));
            level.sendParticles(ParticleTypes.HEART,
                    ally.getX(), ally.getY(0.85), ally.getZ(),
                    4, 0.25, 0.25, 0.25, 0.02);
        }

        List<LivingEntity> foes = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> isValidOffensiveTarget(caster, entity) && entity.distanceToSqr(center) <= maxDistSqr);
        for (LivingEntity foe : foes) {
            foe.hurt(level.damageSources().indirectMagic(caster, caster), 10.0F);
            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            double dx = foe.getX() - caster.getX();
            double dz = foe.getZ() - caster.getZ();
            double dist = Math.hypot(dx, dz);
            if (dist > 1.0E-4) {
                foe.setDeltaMovement((dx / dist) * 0.65, Math.max(foe.getDeltaMovement().y, 0.28), (dz / dist) * 0.65);
                foe.hurtMarked = true;
                foe.hasImpulse = true;
            }
        }

        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0, center.z, 26, 2.0, 0.6, 2.0, 0.06);
        level.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 0.8, center.z, 24, 1.8, 0.5, 1.8, 0.12);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 0.8, center.z, 20, 1.6, 0.4, 1.6, 0.05);
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.1F, 1.25F);
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.15F);
    }

    private static void emitParticleLine(
            ServerLevel level,
            Vec3 from,
            Vec3 to,
            ParticleOptions primary,
            ParticleOptions accent,
            int steps) {
        Vec3 delta = to.subtract(from);
        for (int i = 1; i <= steps; i++) {
            double frac = (double) i / (double) steps;
            Vec3 point = from.add(delta.scale(frac));
            level.sendParticles(primary, point.x, point.y, point.z, 2, 0.06, 0.06, 0.06, 0.02);
            if (i % 2 == 0) {
                level.sendParticles(accent, point.x, point.y, point.z, 1, 0.04, 0.04, 0.04, 0.01);
            }
        }
    }
}
