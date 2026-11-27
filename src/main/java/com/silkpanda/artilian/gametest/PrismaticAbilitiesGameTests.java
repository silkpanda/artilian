package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.gear.PrismaticAbilities;
import com.silkpanda.artilian.mana.ManaData;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.List;

/** Phase 6 Stage 1C: Tier 3 Prismatic Brimstone weapons & tools active/passive ability GameTests. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class PrismaticAbilitiesGameTests {

    private static ServerPlayer spawnTestPlayer(GameTestHelper helper, String name, Vec3 relPos) {
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, name, relPos);
        NetworkRegistry.configureMockConnection(player.connection.getConnection());
        return player;
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void prismBeamDamagesHostilesSparesKidSafeAndStopsAtWalls(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexPrismSword", new Vec3(7.5, 2.0, 2.5));

        Zombie frontZombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 6));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(7, 2, 5));

        ChesterEntity chester = new ChesterEntity(ArtilianEntities.CHESTER.get(), level);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(7.5, 2.0, 4.5));
        chester.setPos(chesterPos.x, chesterPos.y, chesterPos.z);
        chester.setOwner(player);
        level.addFreshEntity(chester);

        // Solid wall at z=8, hidden zombie behind wall at z=10
        for (int dy = 0; dy <= 2; dy++) {
            helper.setBlock(new BlockPos(7, 2 + dy, 8), Blocks.STONE);
        }
        Zombie behindWallZombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 10));

        player.lookAt(EntityAnchorArgument.Anchor.EYES, frontZombie.getEyePosition());
        float frontHpBefore = frontZombie.getHealth();
        float behindHpBefore = behindWallZombie.getHealth();
        float villagerHpBefore = villager.getHealth();
        float chesterHpBefore = chester.getHealth();

        boolean cast = PrismaticAbilities.castPrismBeam(level, player, ArtilianItems.PRISMATIC_SWORD.get());
        helper.assertTrue(cast, "Prism Beam should succeed with full Mana");
        helper.assertTrue(frontZombie.getHealth() < frontHpBefore, "Prism Beam should damage front Zombie");
        helper.assertTrue(frontZombie.hasEffect(MobEffects.GLOWING), "Prism Beam should apply Glowing");
        helper.assertTrue(frontZombie.getRemainingFireTicks() > 0, "Prism Beam should ignite front Zombie");

        helper.assertTrue(behindWallZombie.getHealth() == behindHpBefore && !behindWallZombie.hasEffect(MobEffects.GLOWING),
                "Prism Beam must stop at solid walls");
        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.GLOWING),
                "Prism Beam must spare Villager");
        helper.assertTrue(chester.getHealth() == chesterHpBefore && !chester.hasEffect(MobEffects.GLOWING),
                "Prism Beam must spare Chester");

        helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE), "Prism Beam should grant Fire Resistance");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Prism Beam should grant Speed I");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - PrismaticAbilities.PRISM_BEAM_MANA,
                "Prism Beam should consume 25 Mana");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ArtilianItems.PRISMATIC_SWORD.get()),
                "Prism Beam should apply sword cooldown");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void prismSmeltAutoSmeltsOresAndGrantsHasteII(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "StevePrismPick", new Vec3(7.5, 2.0, 4.5));
        ItemStack pick = new ItemStack(ArtilianItems.PRISMATIC_PICKAXE.get());

        BlockPos orePos = helper.absolutePos(new BlockPos(7, 2, 7));
        Vec3 dropStart = helper.absoluteVec(new Vec3(7.5, 2.2, 7.0));
        ItemEntity rawDrop = new ItemEntity(level, dropStart.x, dropStart.y, dropStart.z, new ItemStack(Items.RAW_IRON));
        rawDrop.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(rawDrop);

        double distBefore = rawDrop.position().distanceTo(player.position());
        pick.getItem().mineBlock(pick, level, Blocks.IRON_ORE.defaultBlockState(), orePos, player);

        MobEffectInstance haste = player.getEffect(MobEffects.DIG_SPEED);
        helper.assertTrue(haste != null && haste.getAmplifier() >= 1,
                "Prism Smelt should grant Haste II on ore break");
        helper.assertTrue(rawDrop.getItem().is(Items.IRON_INGOT),
                "Prism Smelt should auto-smelt RAW_IRON into IRON_INGOT");
        helper.assertTrue(rawDrop.position().distanceTo(player.position()) < distBefore,
                "Prism Smelt should pull drops toward player");

        // Silk Touch must bypass auto-smelt
        ItemStack silkPick = new ItemStack(ArtilianItems.PRISMATIC_PICKAXE.get());
        Holder<Enchantment> silkTouch = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        silkPick.enchant(silkTouch, 1);

        ItemEntity silkRawDrop = new ItemEntity(level, dropStart.x, dropStart.y, dropStart.z, new ItemStack(Items.RAW_IRON));
        silkRawDrop.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(silkRawDrop);
        silkPick.getItem().mineBlock(silkPick, level, Blocks.IRON_ORE.defaultBlockState(), orePos, player);
        helper.assertTrue(silkRawDrop.getItem().is(Items.RAW_IRON),
                "Prism Smelt must NOT auto-smelt when Silk Touch is present");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void veinPulseMinesConnectedMatchingOresOnly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexVeinPulse", new Vec3(7.5, 2.0, 4.5));
        ItemStack pick = new ItemStack(ArtilianItems.PRISMATIC_PICKAXE.get());

        BlockPos p1 = new BlockPos(7, 2, 7);
        BlockPos p2 = new BlockPos(8, 2, 7);
        BlockPos p3 = new BlockPos(7, 3, 7);
        BlockPos p4 = new BlockPos(8, 3, 7);
        BlockPos stonePos = new BlockPos(6, 2, 7);
        BlockPos chestPos = new BlockPos(7, 2, 8);
        BlockPos goldPos = new BlockPos(9, 2, 7);

        helper.setBlock(p1, Blocks.IRON_ORE);
        helper.setBlock(p2, Blocks.IRON_ORE);
        helper.setBlock(p3, Blocks.IRON_ORE);
        helper.setBlock(p4, Blocks.IRON_ORE);
        helper.setBlock(stonePos, Blocks.STONE);
        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(goldPos, Blocks.GOLD_ORE);

        boolean cast = PrismaticAbilities.castVeinPulse(
                level, player, pick, ArtilianItems.PRISMATIC_PICKAXE.get(), helper.absolutePos(p1));
        helper.assertTrue(cast, "Vein Pulse should succeed on Iron Ore");

        helper.assertTrue(helper.getBlockState(p1).isAir()
                        && helper.getBlockState(p2).isAir()
                        && helper.getBlockState(p3).isAir()
                        && helper.getBlockState(p4).isAir(),
                "Vein Pulse should break all 4 connected Iron Ore blocks");
        helper.assertTrue(helper.getBlockState(stonePos).is(Blocks.STONE),
                "Vein Pulse must leave Stone intact");
        helper.assertTrue(helper.getBlockState(chestPos).is(Blocks.CHEST),
                "Vein Pulse must leave Chest BlockEntity intact");
        helper.assertTrue(helper.getBlockState(goldPos).is(Blocks.GOLD_ORE),
                "Vein Pulse must leave non-matching Gold Ore intact");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - PrismaticAbilities.VEIN_PULSE_MANA,
                "Vein Pulse should consume 20 Mana");

        List<ItemEntity> ingots = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(helper.absolutePos(p1)).inflate(6.0),
                item -> item.getItem().is(Items.IRON_INGOT));
        helper.assertTrue(!ingots.isEmpty(), "Vein Pulse should spawn smelted IRON_INGOT drops");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void brimstoneSlamDamagesWeakensHostilesAndSparesKidSafe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "StevePrismAxe", new Vec3(7.5, 2.0, 5.5));

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 8));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(5, 2, 5));

        float zombieHpBefore = zombie.getHealth();
        float villagerHpBefore = villager.getHealth();

        boolean cast = PrismaticAbilities.castBrimstoneSlam(level, player, ArtilianItems.PRISMATIC_AXE.get());
        helper.assertTrue(cast, "Brimstone Slam should succeed with full Mana");
        helper.assertTrue(zombie.getHealth() < zombieHpBefore, "Brimstone Slam should damage Zombie");
        helper.assertTrue(zombie.hasEffect(MobEffects.WEAKNESS), "Brimstone Slam should apply Weakness I");
        helper.assertTrue(zombie.getRemainingFireTicks() > 0, "Brimstone Slam should ignite Zombie");

        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.WEAKNESS),
                "Brimstone Slam must spare Villager");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_RESISTANCE),
                "Brimstone Slam should grant Resistance I to caster");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - PrismaticAbilities.BRIMSTONE_SLAM_MANA,
                "Brimstone Slam should consume 25 Mana");

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                helper.assertFalse(helper.getBlockState(new BlockPos(7 + dx, 2, 5 + dz)).is(Blocks.FIRE),
                        "Brimstone Slam must never place fire blocks on terrain");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void prismExcavationClearsThreeByThreeShovelBlocksSafely(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexPrismShovel", new Vec3(7.5, 3.0, 4.5));
        ItemStack shovel = new ItemStack(ArtilianItems.PRISMATIC_SHOVEL.get());

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                helper.setBlock(new BlockPos(7 + dx, 2, 7 + dz), Blocks.DIRT);
            }
        }
        BlockPos stonePos = new BlockPos(6, 2, 6);
        BlockPos chestPos = new BlockPos(8, 2, 7);
        helper.setBlock(stonePos, Blocks.STONE);
        helper.setBlock(chestPos, Blocks.CHEST);

        boolean cast = PrismaticAbilities.castPrismExcavation(
                level, player, shovel, ArtilianItems.PRISMATIC_SHOVEL.get(),
                helper.absolutePos(new BlockPos(7, 2, 7)), Direction.UP);
        helper.assertTrue(cast, "Prism Excavation should succeed on Dirt");

        int clearedDirt = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = new BlockPos(7 + dx, 2, 7 + dz);
                if (!pos.equals(stonePos) && !pos.equals(chestPos) && helper.getBlockState(pos).isAir()) {
                    clearedDirt++;
                }
            }
        }
        helper.assertTrue(clearedDirt == 7, "Prism Excavation should clear all 7 matching Dirt blocks, cleared " + clearedDirt);
        helper.assertTrue(helper.getBlockState(stonePos).is(Blocks.STONE),
                "Prism Excavation must leave Stone intact");
        helper.assertTrue(helper.getBlockState(chestPos).is(Blocks.CHEST),
                "Prism Excavation must leave Chest BlockEntity intact");
        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        helper.assertTrue(speed != null && speed.getAmplifier() >= 1,
                "Prism Excavation should grant Speed II");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - PrismaticAbilities.PRISM_EXCAVATION_MANA,
                "Prism Excavation should consume 20 Mana");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void prismaticHarvestGrowsHydratesAndReplantsMatureCrops(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "StevePrismHoe", new Vec3(7.5, 2.0, 2.5));
        ItemStack hoe = new ItemStack(ArtilianItems.PRISMATIC_HOE.get());

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                helper.setBlock(new BlockPos(7 + dx, 1, 7 + dz),
                        Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0));
                boolean isOuterRing = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                int age = isOuterRing ? 0 : CropBlock.MAX_AGE;
                helper.setBlock(new BlockPos(7 + dx, 2, 7 + dz),
                        Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age));
            }
        }

        BlockPos center = helper.absolutePos(new BlockPos(7, 2, 7));
        boolean cast = PrismaticAbilities.castPrismaticHarvest(
                level, player, hoe, ArtilianItems.PRISMATIC_HOE.get(), center);
        helper.assertTrue(cast, "Prismatic Harvest should succeed on wheat crop");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - PrismaticAbilities.PRISMATIC_HARVEST_MANA,
                "Prismatic Harvest should consume 25 Mana");

        int hydratedCount = 0;
        int outerGrownCount = 0;
        int innerReplantedCount = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockState farmState = helper.getBlockState(new BlockPos(7 + dx, 1, 7 + dz));
                if (farmState.is(Blocks.FARMLAND) && farmState.getValue(FarmBlock.MOISTURE) == FarmBlock.MAX_MOISTURE) {
                    hydratedCount++;
                }
                BlockState cropState = helper.getBlockState(new BlockPos(7 + dx, 2, 7 + dz));
                boolean isOuterRing = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                if (cropState.getBlock() instanceof CropBlock crop) {
                    int age = cropState.getValue(CropBlock.AGE);
                    if (isOuterRing && age > 0) {
                        outerGrownCount++;
                    } else if (!isOuterRing && age == 0) {
                        innerReplantedCount++;
                    }
                }
            }
        }
        helper.assertTrue(hydratedCount == 49,
                "Prismatic Harvest should hydrate all 49 farmland blocks in 7x7, hydrated " + hydratedCount);
        helper.assertTrue(outerGrownCount == 24,
                "Prismatic Harvest should bonemeal all 24 outer-ring crops in 7x7, grew " + outerGrownCount);
        helper.assertTrue(innerReplantedCount == 25,
                "Prismatic Harvest should auto-harvest and replant all 25 mature crops in 5x5, replanted " + innerReplantedCount);

        List<ItemEntity> wheatDrops = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(center).inflate(6.0),
                item -> item.getItem().is(Items.WHEAT));
        helper.assertTrue(!wheatDrops.isEmpty(), "Prismatic Harvest should spawn harvested WHEAT drops");
        helper.succeed();
    }
}
