package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.DragonJungleDimension;
import com.silkpanda.artilian.dimension.PrimalPortalBlock;
import com.silkpanda.artilian.dimension.PrimalPortalEvents;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 5 Stage 1: Dragon Jungle dimension, Primal Portal, boss-drop Nether Portal ignition, and P5-01 arrival safety. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class DragonJunglePortalGameTests {

    @GameTest(template = "empty")
    public static void dragonJungleDimensionAndFlatPlaceholderAreRegistered(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();

        var dimTypeRegistry = registries.registryOrThrow(Registries.DIMENSION_TYPE);
        DimensionType jungleType = dimTypeRegistry.get(DragonJungleDimension.DRAGON_JUNGLE_TYPE);
        helper.assertTrue(jungleType != null, "artilian:dragon_jungle DimensionType should be registered");
        helper.assertTrue(jungleType.ambientLight() >= 0.2F,
                "Dragon Jungle ambientLight should be >= 0.2 for kid-friendly visibility");
        helper.assertTrue(jungleType.coordinateScale() == 1.0,
                "Dragon Jungle coordinateScale should be 1.0");

        var biomeRegistry = registries.registryOrThrow(Registries.BIOME);
        helper.assertTrue(biomeRegistry.containsKey(DragonJungleDimension.DRAGON_JUNGLE_BIOME),
                "artilian:dragon_jungle biome should be registered");

        helper.assertTrue(assetExists("data/artilian/dimension/dragon_jungle.json"),
                "data/artilian/dimension/dragon_jungle.json LevelStem should be generated");
        helper.assertTrue(assetExists("data/artilian/dimension_type/dragon_jungle.json"),
                "data/artilian/dimension_type/dragon_jungle.json DimensionType should be generated");
        helper.assertTrue(assetExists("assets/artilian/textures/block/primal_portal.png"),
                "primal_portal.png should exist");
        helper.assertTrue(assetExists("assets/artilian/textures/block/primal_portal.png.mcmeta"),
                "primal_portal.png.mcmeta should exist");

        ServerLevel dragonJungle = helper.getLevel().getServer().getLevel(DragonJungleDimension.DRAGON_JUNGLE_LEVEL);
        if (dragonJungle != null) {
            dragonJungle.getChunk(0, 0);
            helper.assertTrue(
                    dragonJungle.dimensionType().effectsLocation().equals(DragonJungleDimension.DRAGON_JUNGLE_EFFECTS),
                    "Live Dragon Jungle should use artilian:dragon_jungle effects");
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void throwingBossDropIntoNetherPortalTransformsItToPrimalPortalAndConsumesOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(2, 2, 4));
        buildLitNetherPortal(level, base, Direction.Axis.X);

        ItemStack stack = new ItemStack(ArtilianItems.PRISM_CORE.get(), 2);
        helper.assertTrue(stack.is(ArtilianTags.Items.BOSS_DROPS),
                "Prism Core must be in artilian:boss_drops");

        BlockPos interiorPos = base.offset(1, 1, 0);
        ItemEntity dropped = new ItemEntity(
                level,
                interiorPos.getX() + 0.5,
                interiorPos.getY() + 0.5,
                interiorPos.getZ() + 0.5,
                stack);
        level.addFreshEntity(dropped);

        boolean transformed = PrimalPortalEvents.tryTransformIfBossDropInNetherPortal(level, dropped);
        helper.assertTrue(transformed, "Throwing a boss drop into a Nether Portal must transform it");

        for (int dx = 1; dx <= 2; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                BlockState state = level.getBlockState(base.offset(dx, dy, 0));
                helper.assertTrue(state.is(ArtilianBlocks.PRIMAL_PORTAL.get()),
                        "Portal interior at (" + dx + "," + dy + ") should become PRIMAL_PORTAL");
                helper.assertTrue(state.getValue(PrimalPortalBlock.AXIS) == Direction.Axis.X,
                        "Primal Portal should preserve Nether Portal X axis");
            }
        }

        helper.assertTrue(dropped.getItem().getCount() == 1,
                "Transforming a Nether Portal should consume exactly 1 boss drop item");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nonBossDropItemInNetherPortalDoesNotTransformPortal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(2, 2, 4));
        buildLitNetherPortal(level, base, Direction.Axis.X);

        BlockPos interiorPos = base.offset(1, 1, 0);
        ItemEntity nonBoss = new ItemEntity(
                level,
                interiorPos.getX() + 0.5,
                interiorPos.getY() + 0.5,
                interiorPos.getZ() + 0.5,
                new ItemStack(Items.COBBLESTONE, 1));
        level.addFreshEntity(nonBoss);

        boolean transformed = PrimalPortalEvents.tryTransformIfBossDropInNetherPortal(level, nonBoss);
        helper.assertTrue(!transformed, "Non-boss-drop items must NOT transform a Nether Portal");
        helper.assertTrue(level.getBlockState(interiorPos).is(Blocks.NETHER_PORTAL),
                "Nether Portal should remain NETHER_PORTAL for non-boss-drop items");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void primalPortalTransitionsBidirectionallyAndCollapsesOnBreak(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(3, 2, 3));
        buildObsidianFrame(overworld, base, Direction.Axis.X);
        helper.assertTrue(PrimalPortalBlock.trySpawnPortal(overworld, base.offset(1, 1, 0)),
                "trySpawnPortal should fill valid Obsidian frame");

        BlockPos portalPos = base.offset(1, 1, 0);
        PrimalPortalBlock portal = ArtilianBlocks.PRIMAL_PORTAL.get();
        Player traveler = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(portal.getPortalTransitionTime(overworld, traveler) == 40,
                "Survival player transition time should be 40 ticks");

        ServerLevel dragonJungle = overworld.getServer().getLevel(DragonJungleDimension.DRAGON_JUNGLE_LEVEL);
        DimensionTransition toJungle = portal.getPortalDestination(overworld, traveler, portalPos);
        if (dragonJungle != null) {
            helper.assertTrue(toJungle != null, "Portal transition to Dragon Jungle should not be null");
            helper.assertTrue(toJungle.newLevel().dimension().equals(DragonJungleDimension.DRAGON_JUNGLE_LEVEL),
                    "Overworld Primal Portal should target artilian:dragon_jungle");

            BlockPos arrivalBlock = BlockPos.containing(toJungle.pos());
            helper.assertTrue(dragonJungle.getBlockState(arrivalBlock).is(ArtilianBlocks.PRIMAL_PORTAL.get()),
                    "Dragon Jungle arrival should place a return Primal Portal");

            DimensionTransition backToOverworld = portal.getPortalDestination(dragonJungle, traveler, arrivalBlock);
            helper.assertTrue(backToOverworld != null, "Return portal transition should not be null");
            helper.assertTrue(backToOverworld.newLevel().dimension().equals(Level.OVERWORLD),
                    "Dragon Jungle Primal Portal should return to Overworld");
        } else {
            BlockPos found = PrimalPortalBlock.findClosestPortal(overworld, portalPos.offset(4, 0, 4), 16);
            helper.assertTrue(found != null && overworld.getBlockState(found).is(ArtilianBlocks.PRIMAL_PORTAL.get()),
                    "findClosestPortal should locate an existing Primal Portal within search radius");
        }

        // Break top Obsidian frame block -> Primal Portal should collapse cleanly
        overworld.destroyBlock(base.offset(1, 4, 0), false);
        helper.assertTrue(overworld.getBlockState(base.offset(1, 3, 0)).isAir(),
                "Breaking a Primal Portal frame block should collapse the portal");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void p5_01_primalPortalArrivalIsAlwaysSafeAndCreatesLandingPad(GameTestHelper helper) {
        ServerLevel target = helper.getLevel().getServer().getLevel(DragonJungleDimension.DRAGON_JUNGLE_LEVEL);
        if (target == null) {
            target = helper.getLevel();
        }

        BlockPos hint = helper.absolutePos(new BlockPos(6, -100, 6));
        BlockPos arrival = PrimalPortalBlock.createReturnPortalWithPlatform(target, hint, Direction.Axis.Z);

        helper.assertTrue(arrival.getY() >= target.getMinBuildHeight() + 8,
                "P5-01: Arrival Y must never be in the void");
        helper.assertTrue(target.getBlockState(arrival).is(ArtilianBlocks.PRIMAL_PORTAL.get()),
                "P5-01: Arrival block must be PRIMAL_PORTAL");
        helper.assertTrue(target.getBlockState(arrival.above()).is(ArtilianBlocks.PRIMAL_PORTAL.get()),
                "P5-01: Arrival headspace must not suffocate the player");

        BlockPos padCenter = arrival.below(2);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockState padState = target.getBlockState(padCenter.offset(dx, 0, dz));
                helper.assertTrue(padState.is(Blocks.MOSS_BLOCK),
                        "P5-01: 5x5 Moss Block landing pad must be solid beneath arrival portal");
            }
        }

        helper.succeed();
    }

    private static void buildObsidianFrame(Level level, BlockPos bottomLeft, Direction.Axis axis) {
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                BlockPos cell = bottomLeft.relative(right, w).above(h);
                boolean isEdge = w == 0 || w == 3 || h == 0 || h == 4;
                level.setBlock(cell, isEdge ? obsidian : Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static void buildLitNetherPortal(Level level, BlockPos bottomLeft, Direction.Axis axis) {
        buildObsidianFrame(level, bottomLeft, axis);
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockState netherPortal = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        for (int w = 1; w <= 2; w++) {
            for (int h = 1; h <= 3; h++) {
                level.setBlock(bottomLeft.relative(right, w).above(h), netherPortal, 18);
            }
        }
    }

    private static boolean assetExists(String path) {
        return DragonJunglePortalGameTests.class.getClassLoader().getResource(path) != null;
    }
}
