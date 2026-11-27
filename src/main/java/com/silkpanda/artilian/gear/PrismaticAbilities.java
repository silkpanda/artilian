package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.safety.KidSafeTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side Tier 3 Prismatic Brimstone ability service for all five Phase 6 Stage 1C items.
 * Every offensive skill filters targets through {@link KidSafeTargets}, Mana costs go through
 * {@link SunsteelAbilities#consumeAbilityMana}, and block-breaking skills never touch BlockEntities
 * or place terrain fire.
 */
public final class PrismaticAbilities {
    public static final int PRISM_BEAM_MANA = 25;
    public static final int PRISM_BEAM_COOLDOWN = 80;
    public static final double PRISM_BEAM_RANGE = 10.0;
    public static final float PRISM_BEAM_DAMAGE = 9.5F;

    public static final int PRISM_SMELT_HASTE_TICKS = 80;
    public static final double PRISM_PULL_RADIUS = 6.0;

    public static final int VEIN_PULSE_MANA = 20;
    public static final int VEIN_PULSE_COOLDOWN = 100;
    public static final int VEIN_PULSE_MAX_BLOCKS = 8;

    public static final int BRIMSTONE_SLAM_MANA = 25;
    public static final int BRIMSTONE_SLAM_COOLDOWN = 100;
    public static final double BRIMSTONE_SLAM_RANGE = 5.5;
    public static final float BRIMSTONE_SLAM_DAMAGE = 9.0F;
    public static final int BRIMSTONE_SLAM_WEAKNESS_TICKS = 80;

    public static final int MOLTEN_SCOOP_SPEED_TICKS = 60;
    public static final int PRISM_EXCAVATION_MANA = 20;
    public static final int PRISM_EXCAVATION_COOLDOWN = 80;

    public static final int PRISMATIC_HARVEST_MANA = 25;
    public static final int PRISMATIC_HARVEST_COOLDOWN = 100;

    private record PendingDropAction(ResourceKey<Level> dimension, UUID minerId, BlockPos pos, boolean smelt) {}

    private static final List<PendingDropAction> PENDING_DROP_ACTIONS = new ArrayList<>();

    private PrismaticAbilities() {}

    /** Prismatic Sword active: piercing 10-block rainbow-and-ember beam stopping at solid walls. */
    public static boolean castPrismBeam(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, PRISM_BEAM_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, PRISM_BEAM_COOLDOWN);

        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Set<LivingEntity> hitTargets = new LinkedHashSet<>();

        for (double d = 0.5; d <= PRISM_BEAM_RANGE; d += 0.5) {
            Vec3 point = origin.add(look.scale(d));
            BlockPos bp = BlockPos.containing(point);
            if (!level.isInWorldBounds(bp) || !level.getBlockState(bp).getCollisionShape(level, bp).isEmpty()) {
                break;
            }
            AABB stepBox = AABB.ofSize(point, 1.4, 1.4, 1.4);
            List<LivingEntity> found = level.getEntitiesOfClass(
                    LivingEntity.class,
                    stepBox,
                    entity -> KidSafeTargets.isValidOffensiveTarget(player, entity));
            hitTargets.addAll(found);

            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.08, 0.08, 0.08, 0.01);
            level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 1, 0.08, 0.08, 0.08, 0.01);
            if (((int) Math.round(d * 2.0)) % 2 == 0) {
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, point.x, point.y, point.z, 2, 0.12, 0.12, 0.12, 0.02);
            }
        }

        for (LivingEntity target : hitTargets) {
            target.hurt(level.damageSources().playerAttack(player), PRISM_BEAM_DAMAGE);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            target.igniteForSeconds(3);
        }

        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.25F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.75F, 1.2F);
        return true;
    }

    /** Returns true if {@code stack} has Silk Touch enchanted. */
    public static boolean hasSilkTouch(ServerLevel level, ItemStack stack) {
        Optional<Holder.Reference<Enchantment>> holder = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(Enchantments.SILK_TOUCH);
        return holder.map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, stack) > 0).orElse(false);
    }

    /** Returns the smelted result of {@code stack} if a smelting recipe exists, or {@code stack} unchanged. */
    public static ItemStack trySmeltItemStack(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
        if (recipe.isEmpty()) {
            return stack;
        }
        ItemStack result = recipe.get().value().getResultItem(level.registryAccess());
        if (result.isEmpty()) {
            return stack;
        }
        ItemStack smelted = result.copy();
        smelted.setCount(result.getCount() * stack.getCount());
        return smelted;
    }

    /** Prismatic Pickaxe passive: mining ores grants Haste II, pulls drops, and auto-smelts unless Silk Touch. */
    public static void onPrismSmelt(ServerLevel level, LivingEntity miner, ItemStack toolStack, BlockState state, BlockPos pos) {
        if (!state.is(Tags.Blocks.ORES)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, PRISM_SMELT_HASTE_TICKS, 1));
        boolean smelt = !hasSilkTouch(level, toolStack);
        processNearbyDrops(level, miner, pos, smelt);
        synchronized (PENDING_DROP_ACTIONS) {
            PENDING_DROP_ACTIONS.add(new PendingDropAction(level.dimension(), miner.getUUID(), pos.immutable(), smelt));
        }
        level.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.25, 0.25, 0.25, 0.02);
        level.sendParticles(ParticleTypes.FLAME,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
    }

    /** Prismatic Pickaxe active: breaks up to 8 connected matching ore blocks in 3x3x3 without touching BlockEntities. */
    public static boolean castVeinPulse(ServerLevel level, Player player, ItemStack stack, Item item, BlockPos originPos) {
        BlockState originState = level.getBlockState(originPos);
        if (!originState.is(Tags.Blocks.ORES) || level.getBlockEntity(originPos) != null) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, VEIN_PULSE_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, VEIN_PULSE_COOLDOWN);

        List<BlockPos> vein = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        BlockPos start = originPos.immutable();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty() && vein.size() < VEIN_PULSE_MAX_BLOCKS) {
            BlockPos current = queue.poll();
            BlockState currentState = level.getBlockState(current);
            if (!currentState.is(originState.getBlock()) || !currentState.is(Tags.Blocks.ORES)
                    || level.getBlockEntity(current) != null) {
                continue;
            }
            vein.add(current);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        BlockPos next = current.offset(dx, dy, dz);
                        if (Math.abs(next.getX() - start.getX()) <= 1
                                && Math.abs(next.getY() - start.getY()) <= 1
                                && Math.abs(next.getZ() - start.getZ()) <= 1
                                && visited.add(next)) {
                            queue.add(next);
                        }
                    }
                }
            }
        }

        boolean smelt = !hasSilkTouch(level, stack);
        int broken = 0;
        for (BlockPos pos : vein) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(originState.getBlock()) || !state.is(Tags.Blocks.ORES) || level.getBlockEntity(pos) != null) {
                continue;
            }
            List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, stack);
            level.removeBlock(pos, false);
            broken++;
            for (ItemStack drop : drops) {
                ItemStack out = smelt ? trySmeltItemStack(level, drop) : drop;
                spawnAndPullDrop(level, player, pos, out);
            }
            level.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.02);
            level.sendParticles(ParticleTypes.FLAME,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
        }

        if (broken > 0 && !player.isCreative()) {
            stack.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, PRISM_SMELT_HASTE_TICKS, 1));
        level.playSound(null, originPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.15F);
        level.playSound(null, originPos, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.65F, 1.3F);
        return broken > 0;
    }

    /** Prismatic Axe active: 5.5-block fiery-prism shockwave ring dealing 9.0 dmg, 3s fire, Weakness I, and knockback. */
    public static boolean castBrimstoneSlam(ServerLevel level, Player player, Item item) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, BRIMSTONE_SLAM_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, BRIMSTONE_SLAM_COOLDOWN);

        double maxDistSqr = BRIMSTONE_SLAM_RANGE * BRIMSTONE_SLAM_RANGE;
        AABB searchBox = player.getBoundingBox().inflate(BRIMSTONE_SLAM_RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity.distanceToSqr(player) <= maxDistSqr
                        && KidSafeTargets.isValidOffensiveTarget(player, entity));

        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().playerAttack(player), BRIMSTONE_SLAM_DAMAGE);
            target.igniteForSeconds(3);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, BRIMSTONE_SLAM_WEAKNESS_TICKS, 0));

            double rx = target.getX() - player.getX();
            double rz = target.getZ() - player.getZ();
            double horiz = Math.hypot(rx, rz);
            double dirX = horiz > 1.0E-4 ? rx / horiz : 1.0;
            double dirZ = horiz > 1.0E-4 ? rz / horiz : 0.0;
            target.setDeltaMovement(dirX * 0.65, Math.max(target.getDeltaMovement().y, 0.3), dirZ * 0.65);
            target.hurtMarked = true;
            target.hasImpulse = true;
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0));

        for (int i = 0; i < 16; i++) {
            double angle = (Math.PI * 2.0 * i) / 16.0;
            double px = player.getX() + Math.cos(angle) * 3.0;
            double py = player.getY(0.25);
            double pz = player.getZ() + Math.sin(angle) * 3.0;
            level.sendParticles(ParticleTypes.FLAME, px, py, pz, 2, 0.12, 0.1, 0.12, 0.03);
            level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0.1, 0.1, 0.1, 0.02);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.9F, 0.9F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 0.85F);
        return true;
    }

    /** Prismatic Shovel passive: breaking shovel-mineable blocks grants Speed II and pulls drops within 6 blocks. */
    public static void onMoltenScoop(ServerLevel level, LivingEntity miner, BlockState state, BlockPos pos) {
        if (!state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return;
        }
        miner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MOLTEN_SCOOP_SPEED_TICKS, 1));
        processNearbyDrops(level, miner, pos, false);
        synchronized (PENDING_DROP_ACTIONS) {
            PENDING_DROP_ACTIONS.add(new PendingDropAction(level.dimension(), miner.getUUID(), pos.immutable(), false));
        }
        level.sendParticles(ParticleTypes.FLAME,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.25, 0.2, 0.25, 0.02);
    }

    /** Prismatic Shovel active: clears a 3x3 face of matching MINEABLE_WITH_SHOVEL blocks safely. */
    public static boolean castPrismExcavation(
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
        if (!SunsteelAbilities.consumeAbilityMana(player, PRISM_EXCAVATION_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, PRISM_EXCAVATION_COOLDOWN);

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
                level.sendParticles(ParticleTypes.END_ROD,
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        3, 0.2, 0.2, 0.2, 0.02);
                level.sendParticles(ParticleTypes.FLAME,
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        2, 0.15, 0.15, 0.15, 0.02);
            }
        }

        if (broken > 0 && !player.isCreative()) {
            stack.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MOLTEN_SCOOP_SPEED_TICKS, 1));
        level.playSound(null, originPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.PLAYERS, 1.0F, 1.15F);
        return broken > 0;
    }

    /** True when the block is a crop, sapling, stem, berry bush, or farmland eligible for Prismatic Harvest. */
    public static boolean isPrismaticHarvestTarget(BlockState state) {
        return SunsteelAbilities.isSunburstTarget(state) || state.is(Blocks.FARMLAND);
    }

    /** Prismatic Hoe active: 7x7 bonemeal + farmland hydration and 5x5 auto-harvest + replant for mature crops. */
    public static boolean castPrismaticHarvest(
            ServerLevel level, Player player, ItemStack stack, Item item, BlockPos clickedPos) {
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!isPrismaticHarvestTarget(clickedState)) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        if (!SunsteelAbilities.consumeAbilityMana(player, PRISMATIC_HARVEST_MANA)) {
            return false;
        }
        player.getCooldowns().addCooldown(item, PRISMATIC_HARVEST_COOLDOWN);

        BlockPos baseCropPos = clickedState.is(Blocks.FARMLAND) ? clickedPos.above() : clickedPos;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
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
                if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2
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
                            4, 0.2, 0.2, 0.2, 0.02);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.02);
                } else if (SunsteelAbilities.isSunburstTarget(cropState)
                        && cropState.getBlock() instanceof BonemealableBlock bonemealable
                        && bonemealable.isValidBonemealTarget(level, cropPos, cropState)) {
                    bonemealable.performBonemeal(level, level.random, cropPos, cropState);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.02);
                    level.sendParticles(ParticleTypes.END_ROD,
                            cropPos.getX() + 0.5, cropPos.getY() + 0.4, cropPos.getZ() + 0.5,
                            2, 0.2, 0.2, 0.2, 0.02);
                }
            }
        }

        level.playSound(null, baseCropPos, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.15F);
        level.playSound(null, baseCropPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.3F);
        return true;
    }

    /** Processes queued Prism Smelt / Molten Scoop drop pulls and auto-smelting for {@code level}. */
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
        AABB box = new AABB(pos).inflate(PRISM_PULL_RADIUS);
        double maxDistSqr = PRISM_PULL_RADIUS * PRISM_PULL_RADIUS;
        List<ItemEntity> drops = level.getEntitiesOfClass(
                ItemEntity.class,
                box,
                item -> item.isAlive() && item.position().distanceToSqr(center) <= maxDistSqr);

        for (ItemEntity drop : drops) {
            if (smelt) {
                ItemStack current = drop.getItem();
                ItemStack smelted = trySmeltItemStack(level, current);
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
            drop.setDeltaMovement(dir.scale(Math.min(0.55, Math.max(0.25, dist * 0.3))));
            drop.setPos(drop.position().add(dir.scale(Math.min(0.4, dist * 0.25))));
            drop.hasImpulse = true;
        }
    }
}
