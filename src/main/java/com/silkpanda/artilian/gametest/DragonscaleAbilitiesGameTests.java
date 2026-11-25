package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.gear.DragonscaleAbilities;
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

/** Phase 6 Stage 1D: Tier 4 Dragonscale weapons & tools active/passive ability GameTests. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class DragonscaleAbilitiesGameTests {

    private static ServerPlayer spawnTestPlayer(GameTestHelper helper, String name, Vec3 relPos) {
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, name, relPos);
        NetworkRegistry.configureMockConnection(player.connection.getConnection());
        return player;
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void dragonfireBreathHitsConeHostilesSparesKidSafeAndNoFireBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexDragonSword", new Vec3(7.5, 2.0, 2.5));

        Zombie coneZombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 6));
        Zombie behindZombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 0));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(7, 2, 5));

        ChesterEntity chester = new ChesterEntity(ArtilianEntities.CHESTER.get(), level);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(7.5, 2.0, 4.5));
        chester.setPos(chesterPos.x, chesterPos.y, chesterPos.z);
        chester.setOwner(player);
        level.addFreshEntity(chester);

        player.lookAt(EntityAnchorArgument.Anchor.EYES, coneZombie.getEyePosition());
        float coneHpBefore = coneZombie.getHealth();
        float behindHpBefore = behindZombie.getHealth();
        float villagerHpBefore = villager.getHealth();
        float chesterHpBefore = chester.getHealth();

        boolean cast = DragonscaleAbilities.castDragonfireBreath(level, player, ArtilianItems.DRAGONSCALE_SWORD.get());
        helper.assertTrue(cast, "Dragonfire Breath should succeed with full Mana");
        helper.assertTrue(coneZombie.getHealth() < coneHpBefore, "Dragonfire Breath should damage Zombie in cone");
        helper.assertTrue(coneZombie.getRemainingFireTicks() > 0, "Dragonfire Breath should ignite Zombie");
        MobEffectInstance weakness = coneZombie.getEffect(MobEffects.WEAKNESS);
        helper.assertTrue(weakness != null && weakness.getAmplifier() >= 1,
                "Dragonfire Breath should apply Weakness II");

        helper.assertTrue(behindZombie.getHealth() == behindHpBefore,
                "Dragonfire Breath should only hit targets inside the forward cone");
        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.WEAKNESS),
                "Dragonfire Breath must spare Villager");
        helper.assertTrue(chester.getHealth() == chesterHpBefore && !chester.hasEffect(MobEffects.WEAKNESS),
                "Dragonfire Breath must spare Chester");

        helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE),
                "Dragonfire Breath should grant Fire Resistance");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST),
                "Dragonfire Breath should grant Strength I");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - DragonscaleAbilities.DRAGONFIRE_BREATH_MANA,
                "Dragonfire Breath should consume 30 Mana");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ArtilianItems.DRAGONSCALE_SWORD.get()),
                "Dragonfire Breath should apply sword cooldown");

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = 0; dz <= 6; dz++) {
                helper.assertFalse(helper.getBlockState(new BlockPos(7 + dx, 2, 2 + dz)).is(Blocks.FIRE),
                        "Dragonfire Breath must never place fire blocks on terrain");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void wyvernForgeAutoSmeltsOresAndRespectsSilkTouch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveDragonPick", new Vec3(7.5, 2.0, 3.5));
        ItemStack pick = new ItemStack(ArtilianItems.DRAGONSCALE_PICKAXE.get());

        BlockPos orePos = helper.absolutePos(new BlockPos(7, 2, 9));
        Vec3 dropStart = helper.absoluteVec(new Vec3(7.5, 2.2, 9.0));
        ItemEntity rawDrop = new ItemEntity(level, dropStart.x, dropStart.y, dropStart.z, new ItemStack(Items.RAW_IRON));
        rawDrop.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(rawDrop);

        double distBefore = rawDrop.position().distanceTo(player.position());
        pick.getItem().mineBlock(pick, level, Blocks.IRON_ORE.defaultBlockState(), orePos, player);

        MobEffectInstance haste = player.getEffect(MobEffects.DIG_SPEED);
        helper.assertTrue(haste != null && haste.getAmplifier() >= 1,
                "Wyvern Forge should grant Haste II on ore break");
        helper.assertTrue(rawDrop.getItem().is(Items.IRON_INGOT),
                "Wyvern Forge should auto-smelt RAW_IRON into IRON_INGOT");
        helper.assertTrue(rawDrop.position().distanceTo(player.position()) < distBefore,
                "Wyvern Forge should pull drops within 8 blocks toward player");

        ItemStack silkPick = new ItemStack(ArtilianItems.DRAGONSCALE_PICKAXE.get());
        Holder<Enchantment> silkTouch = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        silkPick.enchant(silkTouch, 1);

        ItemEntity silkRawDrop = new ItemEntity(level, dropStart.x, dropStart.y, dropStart.z, new ItemStack(Items.RAW_IRON));
        silkRawDrop.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(silkRawDrop);
        silkPick.getItem().mineBlock(silkPick, level, Blocks.IRON_ORE.defaultBlockState(), orePos, player);
        helper.assertTrue(silkRawDrop.getItem().is(Items.RAW_IRON),
                "Wyvern Forge must NOT auto-smelt when Silk Touch is present");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void dragonBoreCarvesThreeByThreeSafelySkippingBlockEntitiesAndObsidian(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexDragonBore", new Vec3(7.5, 2.0, 4.5));
        ItemStack pick = new ItemStack(ArtilianItems.DRAGONSCALE_PICKAXE.get());

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                helper.setBlock(new BlockPos(7 + dx, 2 + dy, 7), Blocks.STONE);
            }
        }
        BlockPos orePos = new BlockPos(7, 3, 7);
        BlockPos chestPos = new BlockPos(8, 2, 7);
        BlockPos obsidianPos = new BlockPos(6, 4, 7);
        helper.setBlock(orePos, Blocks.IRON_ORE);
        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(obsidianPos, Blocks.OBSIDIAN);

        boolean cast = DragonscaleAbilities.castDragonBore(
                level, player, pick, ArtilianItems.DRAGONSCALE_PICKAXE.get(),
                helper.absolutePos(orePos), Direction.NORTH);
        helper.assertTrue(cast, "Dragon Bore should succeed on pickaxe-mineable face");

        int cleared = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                BlockPos pos = new BlockPos(7 + dx, 2 + dy, 7);
                if (!pos.equals(chestPos) && !pos.equals(obsidianPos) && helper.getBlockState(pos).isAir()) {
                    cleared++;
                }
            }
        }
        helper.assertTrue(cleared == 7, "Dragon Bore should carve 7 safe blocks (6 Stone + 1 Iron Ore), cleared " + cleared);
        helper.assertTrue(helper.getBlockState(chestPos).is(Blocks.CHEST),
                "Dragon Bore must leave Chest BlockEntity intact");
        helper.assertTrue(helper.getBlockState(obsidianPos).is(Blocks.OBSIDIAN),
                "Dragon Bore must leave Obsidian intact");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - DragonscaleAbilities.DRAGON_BORE_MANA,
                "Dragon Bore should consume 25 Mana");

        List<ItemEntity> ingots = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(helper.absolutePos(orePos)).inflate(6.0),
                item -> item.getItem().is(Items.IRON_INGOT));
        helper.assertTrue(!ingots.isEmpty(), "Dragon Bore should auto-smelt mined ore drops");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void wingbeatRoarDamagesHostilesSparesKidSafeAndBuffsAllies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveDragonAxe", new Vec3(7.5, 2.0, 5.5));

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 9));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(5, 2, 5));

        ChesterEntity chester = new ChesterEntity(ArtilianEntities.CHESTER.get(), level);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(8.5, 2.0, 5.5));
        chester.setPos(chesterPos.x, chesterPos.y, chesterPos.z);
        chester.setOwner(player);
        level.addFreshEntity(chester);

        float zombieHpBefore = zombie.getHealth();
        float villagerHpBefore = villager.getHealth();
        float chesterHpBefore = chester.getHealth();

        boolean cast = DragonscaleAbilities.castWingbeatRoar(level, player, ArtilianItems.DRAGONSCALE_AXE.get());
        helper.assertTrue(cast, "Wingbeat Roar should succeed with full Mana");
        helper.assertTrue(zombie.getHealth() < zombieHpBefore, "Wingbeat Roar should damage Zombie");
        MobEffectInstance slowness = zombie.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        helper.assertTrue(slowness != null && slowness.getAmplifier() >= 1,
                "Wingbeat Roar should apply Slowness II");

        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "Wingbeat Roar must spare Villager");
        helper.assertTrue(chester.getHealth() == chesterHpBefore && chester.hasEffect(MobEffects.DAMAGE_RESISTANCE),
                "Wingbeat Roar must spare Chester and grant Resistance I");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_RESISTANCE),
                "Wingbeat Roar should grant Resistance I to caster");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - DragonscaleAbilities.WINGBEAT_ROAR_MANA,
                "Wingbeat Roar should consume 30 Mana");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void canopyFellBreaksConnectedMatchingLogsOnly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexCanopyFell", new Vec3(7.5, 2.0, 4.5));
        ItemStack axe = new ItemStack(ArtilianItems.DRAGONSCALE_AXE.get());

        BlockPos l1 = new BlockPos(7, 2, 7);
        BlockPos l2 = new BlockPos(7, 3, 7);
        BlockPos l3 = new BlockPos(7, 4, 7);
        BlockPos l4 = new BlockPos(8, 4, 7);
        BlockPos spruceLog = new BlockPos(6, 2, 7);
        BlockPos stonePos = new BlockPos(7, 2, 8);
        BlockPos chestPos = new BlockPos(8, 2, 7);

        helper.setBlock(l1, Blocks.OAK_LOG);
        helper.setBlock(l2, Blocks.OAK_LOG);
        helper.setBlock(l3, Blocks.OAK_LOG);
        helper.setBlock(l4, Blocks.OAK_LOG);
        helper.setBlock(spruceLog, Blocks.SPRUCE_LOG);
        helper.setBlock(stonePos, Blocks.STONE);
        helper.setBlock(chestPos, Blocks.CHEST);

        boolean cast = DragonscaleAbilities.castCanopyFell(
                level, player, axe, ArtilianItems.DRAGONSCALE_AXE.get(), helper.absolutePos(l1));
        helper.assertTrue(cast, "Canopy Fell should succeed on Oak Log");

        helper.assertTrue(helper.getBlockState(l1).isAir()
                        && helper.getBlockState(l2).isAir()
                        && helper.getBlockState(l3).isAir()
                        && helper.getBlockState(l4).isAir(),
                "Canopy Fell should fell all 4 connected Oak Logs");
        helper.assertTrue(helper.getBlockState(spruceLog).is(Blocks.SPRUCE_LOG),
                "Canopy Fell must leave non-matching Spruce Log intact");
        helper.assertTrue(helper.getBlockState(stonePos).is(Blocks.STONE),
                "Canopy Fell must leave Stone intact");
        helper.assertTrue(helper.getBlockState(chestPos).is(Blocks.CHEST),
                "Canopy Fell must leave Chest BlockEntity intact");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - DragonscaleAbilities.CANOPY_FELL_MANA,
                "Canopy Fell should consume 20 Mana");

        List<ItemEntity> logDrops = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(helper.absolutePos(l1)).inflate(6.0),
                item -> item.getItem().is(Items.OAK_LOG));
        helper.assertTrue(!logDrops.isEmpty(), "Canopy Fell should spawn OAK_LOG drops");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void terraRakeClearsThreeByThreeByTwoShovelBlocksSafely(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveTerraRake", new Vec3(7.5, 4.0, 4.5));
        ItemStack shovel = new ItemStack(ArtilianItems.DRAGONSCALE_SHOVEL.get());

        for (int dy = 2; dy <= 3; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    helper.setBlock(new BlockPos(7 + dx, dy, 7 + dz), Blocks.DIRT);
                }
            }
        }
        BlockPos stonePos = new BlockPos(6, 3, 6);
        BlockPos chestPos = new BlockPos(8, 2, 7);
        helper.setBlock(stonePos, Blocks.STONE);
        helper.setBlock(chestPos, Blocks.CHEST);

        boolean cast = DragonscaleAbilities.castTerraRake(
                level, player, shovel, ArtilianItems.DRAGONSCALE_SHOVEL.get(),
                helper.absolutePos(new BlockPos(7, 3, 7)), Direction.UP);
        helper.assertTrue(cast, "Terra Rake should succeed on Dirt");

        int cleared = 0;
        for (int dy = 2; dy <= 3; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = new BlockPos(7 + dx, dy, 7 + dz);
                    if (!pos.equals(stonePos) && !pos.equals(chestPos) && helper.getBlockState(pos).isAir()) {
                        cleared++;
                    }
                }
            }
        }
        helper.assertTrue(cleared == 16, "Terra Rake should clear all 16 safe Dirt blocks in 3x3x2, cleared " + cleared);
        helper.assertTrue(helper.getBlockState(stonePos).is(Blocks.STONE),
                "Terra Rake must leave Stone intact");
        helper.assertTrue(helper.getBlockState(chestPos).is(Blocks.CHEST),
                "Terra Rake must leave Chest BlockEntity intact");
        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        helper.assertTrue(speed != null && speed.getAmplifier() >= 1,
                "Terra Rake should grant Speed II");
        helper.assertTrue(player.hasEffect(MobEffects.DIG_SPEED),
                "Terra Rake should grant Haste I");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - DragonscaleAbilities.TERRA_RAKE_MANA,
                "Terra Rake should consume 25 Mana");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void primalOvergrowthGrowsHydratesNineByNineAndReplantsSevenBySeven(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexDragonHoe", new Vec3(7.5, 2.0, 2.5));
        ItemStack hoe = new ItemStack(ArtilianItems.DRAGONSCALE_HOE.get());

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                helper.setBlock(new BlockPos(7 + dx, 1, 7 + dz),
                        Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0));
                boolean isOuterRing = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                int age = isOuterRing ? 0 : CropBlock.MAX_AGE;
                helper.setBlock(new BlockPos(7 + dx, 2, 7 + dz),
                        Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age));
            }
        }

        BlockPos center = helper.absolutePos(new BlockPos(7, 2, 7));
        boolean cast = DragonscaleAbilities.castPrimalOvergrowth(
                level, player, hoe, ArtilianItems.DRAGONSCALE_HOE.get(), center);
        helper.assertTrue(cast, "Primal Overgrowth should succeed on wheat crop");
        helper.assertTrue(player.hasEffect(MobEffects.REGENERATION),
                "Primal Overgrowth should grant Regeneration I to caster");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - DragonscaleAbilities.PRIMAL_OVERGROWTH_MANA,
                "Primal Overgrowth should consume 25 Mana");

        int hydratedCount = 0;
        int outerGrownCount = 0;
        int innerReplantedCount = 0;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockState farmState = helper.getBlockState(new BlockPos(7 + dx, 1, 7 + dz));
                if (farmState.is(Blocks.FARMLAND) && farmState.getValue(FarmBlock.MOISTURE) == FarmBlock.MAX_MOISTURE) {
                    hydratedCount++;
                }
                BlockState cropState = helper.getBlockState(new BlockPos(7 + dx, 2, 7 + dz));
                boolean isOuterRing = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                if (cropState.getBlock() instanceof CropBlock) {
                    int age = cropState.getValue(CropBlock.AGE);
                    if (isOuterRing && age > 0) {
                        outerGrownCount++;
                    } else if (!isOuterRing && age == 0) {
                        innerReplantedCount++;
                    }
                }
            }
        }
        helper.assertTrue(hydratedCount == 81,
                "Primal Overgrowth should hydrate all 81 farmland blocks in 9x9, hydrated " + hydratedCount);
        helper.assertTrue(outerGrownCount == 32,
                "Primal Overgrowth should bonemeal all 32 outer-ring crops in 9x9, grew " + outerGrownCount);
        helper.assertTrue(innerReplantedCount == 49,
                "Primal Overgrowth should auto-harvest and replant all 49 mature crops in 7x7, replanted " + innerReplantedCount);

        List<ItemEntity> wheatDrops = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(center).inflate(8.0),
                item -> item.getItem().is(Items.WHEAT));
        helper.assertTrue(!wheatDrops.isEmpty(), "Primal Overgrowth should spawn harvested WHEAT drops");
        helper.succeed();
    }
}
