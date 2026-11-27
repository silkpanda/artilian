package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.gear.SunsteelAbilities;
import com.silkpanda.artilian.mana.ManaData;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/** Phase 6 Stage 1A: Sunsteel weapons & tools active/passive ability GameTests. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class SunsteelAbilitiesGameTests {

    private static ServerPlayer spawnTestPlayer(GameTestHelper helper, String name, Vec3 relPos) {
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, name, relPos);
        NetworkRegistry.configureMockConnection(player.connection.getConnection());
        return player;
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void solarSlashHitsHostilesAndSparesKidSafeTargets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexSunSword", new Vec3(7.5, 2.0, 4.5));

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 7));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(6, 2, 7));

        ChesterEntity chester = new ChesterEntity(ArtilianEntities.CHESTER.get(), level);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(8.5, 2.0, 7.5));
        chester.setPos(chesterPos.x, chesterPos.y, chesterPos.z);
        chester.setOwner(player);
        level.addFreshEntity(chester);

        player.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        float zombieHpBefore = zombie.getHealth();
        float villagerHpBefore = villager.getHealth();
        float chesterHpBefore = chester.getHealth();

        boolean cast = SunsteelAbilities.castSolarSlash(level, player, ArtilianItems.SUNSTEEL_SWORD.get());
        helper.assertTrue(cast, "Solar Slash should succeed with full Mana");
        helper.assertTrue(zombie.getHealth() < zombieHpBefore, "Solar Slash should damage hostile Zombie");
        helper.assertTrue(zombie.hasEffect(MobEffects.GLOWING), "Solar Slash should apply Glowing to Zombie");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Solar Slash should grant player Speed I");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - SunsteelAbilities.SOLAR_SLASH_MANA,
                "Solar Slash should consume 15 Mana");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ArtilianItems.SUNSTEEL_SWORD.get()),
                "Solar Slash should apply sword cooldown");

        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.GLOWING),
                "Solar Slash must spare Villager");
        helper.assertTrue(chester.getHealth() == chesterHpBefore && !chester.hasEffect(MobEffects.GLOWING),
                "Solar Slash must spare Chester");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 340)
    public static void solarFlarePlacesAirOnlyLightAndExpires(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveSunPick", new Vec3(7.5, 2.0, 4.5));

        BlockPos stonePos = new BlockPos(7, 2, 7);
        BlockPos chestPos = new BlockPos(8, 2, 7);
        BlockPos airPos = new BlockPos(7, 3, 7);
        helper.setBlock(stonePos, Blocks.STONE);
        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(airPos, Blocks.AIR);

        BlockPos absStone = helper.absolutePos(stonePos);
        BlockPos absChest = helper.absolutePos(chestPos);
        BlockPos absAir = helper.absolutePos(airPos);

        helper.assertFalse(SunsteelAbilities.castSolarFlare(level, player, ArtilianItems.SUNSTEEL_PICKAXE.get(), absStone),
                "Solar Flare must refuse non-air Stone block");
        helper.assertTrue(level.getBlockState(absStone).is(Blocks.STONE),
                "Solar Flare must leave Stone intact");

        helper.assertFalse(SunsteelAbilities.castSolarFlare(level, player, ArtilianItems.SUNSTEEL_PICKAXE.get(), absChest),
                "Solar Flare must refuse Chest BlockEntity");
        helper.assertTrue(level.getBlockState(absChest).is(Blocks.CHEST),
                "Solar Flare must leave Chest intact");
        helper.assertTrue(ManaData.get(player).getMana() == ManaData.DEFAULT_MAX_MANA,
                "Refused Solar Flare must not consume Mana");

        helper.assertTrue(SunsteelAbilities.castSolarFlare(level, player, ArtilianItems.SUNSTEEL_PICKAXE.get(), absAir),
                "Solar Flare should place light in air");
        BlockState placed = level.getBlockState(absAir);
        helper.assertTrue(placed.is(Blocks.LIGHT)
                        && placed.getValue(LightBlock.LEVEL) == SunsteelAbilities.SOLAR_FLARE_LIGHT_LEVEL,
                "Solar Flare must place level-15 minecraft:light");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - SunsteelAbilities.SOLAR_FLARE_MANA,
                "Solar Flare should consume 10 Mana");

        helper.runAtTickTime(305, () -> {
            helper.assertTrue(level.getBlockState(absAir).isAir(),
                    "Solar Flare temporary light must expire back to AIR after 300 ticks");
            helper.succeed();
        });
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void sunlitMiningGrantsHasteOnOreBreak(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexSunMine", new Vec3(7.5, 2.0, 4.5));
        ItemStack pick = new ItemStack(ArtilianItems.SUNSTEEL_PICKAXE.get());

        BlockPos stonePos = helper.absolutePos(new BlockPos(7, 2, 6));
        pick.getItem().mineBlock(pick, level, Blocks.STONE.defaultBlockState(), stonePos, player);
        helper.assertFalse(player.hasEffect(MobEffects.DIG_SPEED),
                "Mining plain Stone should not grant Sunlit Mining Haste");

        BlockPos orePos = helper.absolutePos(new BlockPos(7, 2, 7));
        pick.getItem().mineBlock(pick, level, ArtilianBlocks.SUNSTEEL_ORE.get().defaultBlockState(), orePos, player);
        helper.assertTrue(player.hasEffect(MobEffects.DIG_SPEED),
                "Mining Sunsteel Ore should grant Sunlit Mining Haste I");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void sunCleaveDamagesAndSlowsHostiles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveSunAxe", new Vec3(7.5, 2.0, 4.5));

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 7));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(6, 2, 6));

        player.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        float zombieHpBefore = zombie.getHealth();
        float villagerHpBefore = villager.getHealth();

        boolean cast = SunsteelAbilities.castSunCleave(level, player, ArtilianItems.SUNSTEEL_AXE.get());
        helper.assertTrue(cast, "Sun-Cleave should succeed with full Mana");
        helper.assertTrue(zombie.getHealth() < zombieHpBefore, "Sun-Cleave should damage hostile Zombie");
        helper.assertTrue(zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "Sun-Cleave should apply Slowness I to Zombie");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - SunsteelAbilities.SUN_CLEAVE_MANA,
                "Sun-Cleave should consume 15 Mana");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ArtilianItems.SUNSTEEL_AXE.get()),
                "Sun-Cleave should apply axe cooldown");
        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "Sun-Cleave must spare Villager");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void warmScoopGrantsSpeedAndPullsDrops(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexSunShovel", new Vec3(7.5, 2.0, 4.5));
        ItemStack shovel = new ItemStack(ArtilianItems.SUNSTEEL_SHOVEL.get());

        BlockPos dirtPos = helper.absolutePos(new BlockPos(7, 2, 7));
        Vec3 dropStart = helper.absoluteVec(new Vec3(7.5, 2.2, 7.0));
        ItemEntity drop = new ItemEntity(level, dropStart.x, dropStart.y, dropStart.z, new ItemStack(Items.DIRT));
        drop.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(drop);

        double distBefore = drop.position().distanceTo(player.position());
        shovel.getItem().mineBlock(shovel, level, Blocks.DIRT.defaultBlockState(), dirtPos, player);

        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED),
                "Warm Scoop should grant Speed I on shovel-mineable block break");
        double distAfter = drop.position().distanceTo(player.position());
        helper.assertTrue(distAfter < distBefore && drop.getDeltaMovement().z < 0.0,
                "Warm Scoop should pull nearby item drops toward the player");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void sunburstGrowthBonemealsThreeByThreeCrops(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveSunHoe", new Vec3(5.5, 2.0, 5.5));

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                helper.setBlock(new BlockPos(7 + dx, 1, 7 + dz), Blocks.FARMLAND);
                helper.setBlock(new BlockPos(7 + dx, 2, 7 + dz), Blocks.WHEAT.defaultBlockState());
            }
        }

        BlockPos center = helper.absolutePos(new BlockPos(7, 2, 7));
        boolean cast = SunsteelAbilities.castSunburstGrowth(level, player, ArtilianItems.SUNSTEEL_HOE.get(), center);
        helper.assertTrue(cast, "Sunburst Growth should succeed on wheat crop");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - SunsteelAbilities.SUNBURST_GROWTH_MANA,
                "Sunburst Growth should consume 15 Mana");

        int grownCount = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockState state = helper.getBlockState(new BlockPos(7 + dx, 2, 7 + dz));
                if (state.getBlock() instanceof CropBlock && state.getValue(CropBlock.AGE) > 0) {
                    grownCount++;
                }
            }
        }
        helper.assertTrue(grownCount == 9,
                "Sunburst Growth should bonemeal all 9 crops in 3x3 area, grew " + grownCount);
        helper.succeed();
    }
}
