package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterEntity;
import com.silkpanda.artilian.gear.StarlightAbilities;
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
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/** Phase 6 Stage 1B: Starlight weapons & tools active/passive ability GameTests. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class StarlightAbilitiesGameTests {

    private static ServerPlayer spawnTestPlayer(GameTestHelper helper, String name, Vec3 relPos) {
        ServerPlayer player = CustomTntGameTests.spawnSurvivalPlayer(helper, name, relPos);
        NetworkRegistry.configureMockConnection(player.connection.getConnection());
        return player;
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 60)
    public static void starstepDashHitsHostilesSparesKidSafeAndStopsAtWalls(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexStarSword", new Vec3(7.5, 2.0, 3.5));

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 6));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(6, 2, 6));

        ChesterEntity chester = new ChesterEntity(ArtilianEntities.CHESTER.get(), level);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(8.5, 2.0, 6.5));
        chester.setPos(chesterPos.x, chesterPos.y, chesterPos.z);
        chester.setOwner(player);
        level.addFreshEntity(chester);

        // Place a 2-high solid stone wall at Z=8 so Starstep Dash must stop before Z=8.
        helper.setBlock(new BlockPos(7, 2, 8), Blocks.STONE);
        helper.setBlock(new BlockPos(7, 3, 8), Blocks.STONE);

        player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(
                helper.absoluteVec(new Vec3(7.5, 3.6, 8.5)).x,
                helper.absoluteVec(new Vec3(7.5, 3.6, 8.5)).y,
                helper.absoluteVec(new Vec3(7.5, 3.6, 8.5)).z));
        float zombieHpBefore = zombie.getHealth();
        float villagerHpBefore = villager.getHealth();
        float chesterHpBefore = chester.getHealth();

        boolean cast = StarlightAbilities.castStarstepDash(level, player, ArtilianItems.STARLIGHT_SWORD.get());
        helper.assertTrue(cast, "Starstep Dash should succeed with full Mana");
        helper.assertTrue(zombie.getHealth() < zombieHpBefore, "Starstep Dash should damage hostile Zombie");
        helper.assertTrue(zombie.hasEffect(MobEffects.LEVITATION), "Starstep Dash should apply Levitation to Zombie");
        helper.assertTrue(player.hasEffect(MobEffects.SLOW_FALLING), "Starstep Dash should grant caster Slow Falling");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - StarlightAbilities.STARSTEP_DASH_MANA,
                "Starstep Dash should consume 20 Mana");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ArtilianItems.STARLIGHT_SWORD.get()),
                "Starstep Dash should apply sword cooldown");

        double wallZ = helper.absoluteVec(new Vec3(7.5, 2.0, 8.0)).z;
        helper.assertTrue(player.getZ() < wallZ && player.getZ() > helper.absoluteVec(new Vec3(7.5, 2.0, 4.0)).z,
                "Starstep Dash must advance toward wall and stop before solid wall at Z=8 (got Z=" + player.getZ() + ")");

        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.LEVITATION),
                "Starstep Dash must spare Villager");
        helper.assertTrue(chester.getHealth() == chesterHpBefore && !chester.hasEffect(MobEffects.LEVITATION),
                "Starstep Dash must spare Chester");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void constellationPulseScansOresAndGrantsNightVision(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveStarPick", new Vec3(7.5, 2.0, 4.5));

        BlockPos starlightOrePos = new BlockPos(7, 2, 7);
        BlockPos sunsteelOrePos = new BlockPos(9, 2, 7);
        helper.setBlock(starlightOrePos, ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get());
        helper.setBlock(sunsteelOrePos, ArtilianBlocks.SUNSTEEL_ORE.get());

        int found = StarlightAbilities.castConstellationPulse(level, player, ArtilianItems.STARLIGHT_PICKAXE.get());
        helper.assertTrue(found >= 2, "Constellation Pulse should detect at least the 2 placed ores, found " + found);
        helper.assertTrue(player.hasEffect(MobEffects.NIGHT_VISION),
                "Constellation Pulse should grant Night Vision");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - StarlightAbilities.CONSTELLATION_PULSE_MANA,
                "Constellation Pulse should consume 15 Mana");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ArtilianItems.STARLIGHT_PICKAXE.get()),
                "Constellation Pulse should apply pickaxe cooldown");
        helper.assertTrue(helper.getBlockState(starlightOrePos).is(ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get())
                        && helper.getBlockState(sunsteelOrePos).is(ArtilianBlocks.SUNSTEEL_ORE.get()),
                "Constellation Pulse must be strictly read-only and leave ores intact");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void starlitMagnetGrantsHasteAndPullsOreDrops(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexStarMine", new Vec3(7.5, 2.0, 4.5));
        ItemStack pick = new ItemStack(ArtilianItems.STARLIGHT_PICKAXE.get());

        BlockPos stonePos = helper.absolutePos(new BlockPos(7, 2, 6));
        pick.getItem().mineBlock(pick, level, Blocks.STONE.defaultBlockState(), stonePos, player);
        helper.assertFalse(player.hasEffect(MobEffects.DIG_SPEED),
                "Mining plain Stone should not grant Starlit Magnet Haste");

        BlockPos orePos = helper.absolutePos(new BlockPos(7, 2, 8));
        Vec3 dropStart = helper.absoluteVec(new Vec3(7.5, 2.2, 8.5));
        ItemEntity drop = new ItemEntity(level, dropStart.x, dropStart.y, dropStart.z,
                new ItemStack(ArtilianItems.STARLIGHT_CRYSTAL.get()));
        drop.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(drop);

        double distBefore = drop.position().distanceTo(player.position());
        pick.getItem().mineBlock(pick, level, ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get().defaultBlockState(), orePos, player);
        helper.assertTrue(player.hasEffect(MobEffects.DIG_SPEED),
                "Mining Starlight Crystal Ore should grant Starlit Magnet Haste I");
        double distAfter = drop.position().distanceTo(player.position());
        helper.assertTrue(distAfter < distBefore && drop.getDeltaMovement().z < 0.0,
                "Starlit Magnet should pull ore drops toward the player");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void gravityVortexPullsFloatsAndSparesKidSafeTargets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveStarAxe", new Vec3(7.5, 2.0, 3.5));

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 9));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(6, 2, 7));

        ChesterEntity chester = new ChesterEntity(ArtilianEntities.CHESTER.get(), level);
        Vec3 chesterPos = helper.absoluteVec(new Vec3(8.5, 2.0, 7.5));
        chester.setPos(chesterPos.x, chesterPos.y, chesterPos.z);
        chester.setOwner(player);
        level.addFreshEntity(chester);

        Vec3 lookTarget = helper.absoluteVec(new Vec3(7.5, 3.0, 7.0));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, lookTarget);

        Vec3 vortexCenter = player.getEyePosition().add(player.getLookAngle().normalize().scale(StarlightAbilities.GRAVITY_VORTEX_AHEAD));
        double zombieDistBefore = zombie.position().distanceTo(vortexCenter);
        float zombieHpBefore = zombie.getHealth();
        float villagerHpBefore = villager.getHealth();
        float chesterHpBefore = chester.getHealth();

        boolean cast = StarlightAbilities.castGravityVortex(level, player, ArtilianItems.STARLIGHT_AXE.get());
        helper.assertTrue(cast, "Gravity Vortex should succeed with full Mana");
        helper.assertTrue(zombie.getHealth() < zombieHpBefore, "Gravity Vortex should damage hostile Zombie");
        helper.assertTrue(zombie.hasEffect(MobEffects.LEVITATION), "Gravity Vortex should apply Levitation to Zombie");
        helper.assertTrue(zombie.position().distanceTo(vortexCenter) < zombieDistBefore,
                "Gravity Vortex should pull Zombie closer to vortex center");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - StarlightAbilities.GRAVITY_VORTEX_MANA,
                "Gravity Vortex should consume 20 Mana");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ArtilianItems.STARLIGHT_AXE.get()),
                "Gravity Vortex should apply axe cooldown");

        helper.assertTrue(villager.getHealth() == villagerHpBefore && !villager.hasEffect(MobEffects.LEVITATION),
                "Gravity Vortex must spare Villager");
        helper.assertTrue(chester.getHealth() == chesterHpBefore && !chester.hasEffect(MobEffects.LEVITATION),
                "Gravity Vortex must spare Chester");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void starHopLaunchesWithSlowFallingAndMeteorScoopPullsDrops(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "AlexStarShovel", new Vec3(7.5, 2.0, 4.5));
        ItemStack shovel = new ItemStack(ArtilianItems.STARLIGHT_SHOVEL.get());

        BlockPos dirtPos = helper.absolutePos(new BlockPos(7, 2, 7));
        Vec3 dropStart = helper.absoluteVec(new Vec3(7.5, 2.2, 7.5));
        ItemEntity drop = new ItemEntity(level, dropStart.x, dropStart.y, dropStart.z, new ItemStack(Items.DIRT));
        drop.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(drop);

        double distBefore = drop.position().distanceTo(player.position());
        shovel.getItem().mineBlock(shovel, level, Blocks.DIRT.defaultBlockState(), dirtPos, player);
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED),
                "Meteor Scoop should grant Speed I on shovel-mineable block break");
        double distAfter = drop.position().distanceTo(player.position());
        helper.assertTrue(distAfter < distBefore && drop.getDeltaMovement().z < 0.0,
                "Meteor Scoop should pull nearby drops toward the player");

        boolean cast = StarlightAbilities.castStarHop(level, player, ArtilianItems.STARLIGHT_SHOVEL.get());
        helper.assertTrue(cast, "Star-Hop should succeed with full Mana");
        helper.assertTrue(player.getDeltaMovement().y > 0.5, "Star-Hop should launch player upward");
        helper.assertTrue(player.hasEffect(MobEffects.SLOW_FALLING), "Star-Hop should grant Slow Falling");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - StarlightAbilities.STAR_HOP_MANA,
                "Star-Hop should consume 15 Mana");
        helper.assertTrue(player.getCooldowns().isOnCooldown(ArtilianItems.STARLIGHT_SHOVEL.get()),
                "Star-Hop should apply shovel cooldown");
        helper.succeed();
    }

    @GameTest(template = "tnt_arena", timeoutTicks = 40)
    public static void astralBloomGrowsAndHydratesFiveByFive(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = spawnTestPlayer(helper, "SteveStarHoe", new Vec3(5.5, 2.0, 5.5));

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                helper.setBlock(new BlockPos(7 + dx, 1, 7 + dz),
                        Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0));
                helper.setBlock(new BlockPos(7 + dx, 2, 7 + dz), Blocks.WHEAT.defaultBlockState());
            }
        }

        BlockPos center = helper.absolutePos(new BlockPos(7, 2, 7));
        boolean cast = StarlightAbilities.castAstralBloom(level, player, ArtilianItems.STARLIGHT_HOE.get(), center);
        helper.assertTrue(cast, "Astral Bloom should succeed on wheat crop");
        helper.assertTrue(ManaData.get(player).getMana() == 100 - StarlightAbilities.ASTRAL_BLOOM_MANA,
                "Astral Bloom should consume 20 Mana");

        int grownCount = 0;
        int hydratedCount = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockState farmState = helper.getBlockState(new BlockPos(7 + dx, 1, 7 + dz));
                if (farmState.is(Blocks.FARMLAND) && farmState.getValue(FarmBlock.MOISTURE) == FarmBlock.MAX_MOISTURE) {
                    hydratedCount++;
                }
                BlockState cropState = helper.getBlockState(new BlockPos(7 + dx, 2, 7 + dz));
                if (cropState.getBlock() instanceof CropBlock && cropState.getValue(CropBlock.AGE) > 0) {
                    grownCount++;
                }
            }
        }
        helper.assertTrue(grownCount == 25,
                "Astral Bloom should bonemeal all 25 crops in 5x5 area, grew " + grownCount);
        helper.assertTrue(hydratedCount == 25,
                "Astral Bloom should hydrate all 25 farmland blocks in 5x5 area, hydrated " + hydratedCount);
        helper.succeed();
    }
}
