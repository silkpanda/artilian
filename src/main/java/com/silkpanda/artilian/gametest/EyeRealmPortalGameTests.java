package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
import com.silkpanda.artilian.dimension.GazingPortalBlock;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 4 Stage 1: Eye Realm dimension, Gazing Portal, Gazing Eye, and P4-01 arrival safety. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class EyeRealmPortalGameTests {

    @GameTest(template = "empty")
    public static void eyeRealmDimensionAndFlatPlaceholderAreRegistered(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();

        var dimTypeRegistry = registries.registryOrThrow(Registries.DIMENSION_TYPE);
        DimensionType eyeType = dimTypeRegistry.get(EyeRealmDimension.EYE_REALM_TYPE);
        helper.assertTrue(eyeType != null, "artilian:eye_realm DimensionType should be registered");
        helper.assertTrue(eyeType.ambientLight() >= 0.2F,
                "Eye Realm ambientLight should be >= 0.2 for kid-friendly visibility");
        helper.assertTrue(eyeType.coordinateScale() == 1.0,
                "Eye Realm coordinateScale should be 1.0");

        var biomeRegistry = registries.registryOrThrow(Registries.BIOME);
        helper.assertTrue(biomeRegistry.containsKey(EyeRealmDimension.EYE_REALM_BIOME),
                "artilian:eye_realm biome should be registered");

        helper.assertTrue(assetExists("data/artilian/dimension/eye_realm.json"),
                "data/artilian/dimension/eye_realm.json LevelStem should be generated");
        helper.assertTrue(assetExists("data/artilian/dimension_type/eye_realm.json"),
                "data/artilian/dimension_type/eye_realm.json DimensionType should be generated");

        // If running on a full server (not GameTestServer's 3-dimension preset), also verify live ServerLevel
        ServerLevel eyeRealm = helper.getLevel().getServer().getLevel(EyeRealmDimension.EYE_REALM_LEVEL);
        if (eyeRealm != null) {
            eyeRealm.getChunk(0, 0);
            helper.assertTrue(eyeRealm.getBlockState(new BlockPos(0, 63, 0)).is(ArtilianBlocks.STARLIGHT_SLATE.get()),
                    "Live Eye Realm surface at Y=63 should be Starlight Slate");
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gazingEyeLightsValidFrameAndCollapsesOnBreak(GameTestHelper helper) {
        BlockPos base = helper.absolutePos(new BlockPos(2, 2, 4));
        buildSlateFrame(helper.getLevel(), base, Direction.Axis.X);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack eye = new ItemStack(ArtilianItems.GAZING_EYE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, eye);

        BlockPos frameBottom = base.offset(1, 0, 0);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(frameBottom), Direction.UP, frameBottom, false);
        InteractionResult result = eye.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.assertTrue(result.consumesAction(), "Gazing Eye should light a valid 4x5 Starlight Slate frame");
        helper.assertTrue(eye.getDamageValue() == 1, "Gazing Eye should lose 1 durability on survival activation");

        for (int dx = 1; dx <= 2; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                helper.assertTrue(
                        helper.getLevel().getBlockState(base.offset(dx, dy, 0)).is(ArtilianBlocks.GAZING_PORTAL.get()),
                        "Portal interior at (" + dx + "," + dy + ") should be GAZING_PORTAL");
            }
        }

        // Break top frame block -> portal should collapse cleanly
        helper.getLevel().destroyBlock(base.offset(1, 4, 0), false);
        helper.assertTrue(
                helper.getLevel().getBlockState(base.offset(1, 3, 0)).isAir(),
                "Breaking a portal frame block should collapse the Gazing Portal");

        // Incomplete frame should NOT light
        BlockPos badBase = helper.absolutePos(new BlockPos(8, 2, 4));
        buildSlateFrame(helper.getLevel(), badBase, Direction.Axis.X);
        helper.getLevel().setBlock(badBase.offset(2, 4, 0), Blocks.AIR.defaultBlockState(), 3);
        BlockPos badBottom = badBase.offset(1, 0, 0);
        InteractionResult badResult = eye.useOn(new UseOnContext(
                player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(badBottom), Direction.UP, badBottom, false)));
        helper.assertTrue(!badResult.consumesAction(), "Incomplete frame must not light a Gazing Portal");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gazingPortalTransitionsAndReturnsCorrectTargetKeys(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(3, 2, 3));
        buildSlateFrame(overworld, base, Direction.Axis.X);
        helper.assertTrue(GazingPortalBlock.trySpawnPortal(overworld, base.offset(1, 1, 0)),
                "trySpawnPortal should fill valid Starlight Slate frame");

        BlockPos portalPos = base.offset(1, 1, 0);
        GazingPortalBlock portal = ArtilianBlocks.GAZING_PORTAL.get();
        Player traveler = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(portal.getPortalTransitionTime(overworld, traveler) == 40,
                "Survival player transition time should be 40 ticks");

        ServerLevel eyeRealm = overworld.getServer().getLevel(EyeRealmDimension.EYE_REALM_LEVEL);
        DimensionTransition toEyeRealm = portal.getPortalDestination(overworld, traveler, portalPos);
        if (eyeRealm != null) {
            helper.assertTrue(toEyeRealm != null, "Portal transition to Eye Realm should not be null");
            helper.assertTrue(toEyeRealm.newLevel().dimension().equals(EyeRealmDimension.EYE_REALM_LEVEL),
                    "Overworld Gazing Portal should target artilian:eye_realm");

            BlockPos arrivalBlock = BlockPos.containing(toEyeRealm.pos());
            helper.assertTrue(eyeRealm.getBlockState(arrivalBlock).is(ArtilianBlocks.GAZING_PORTAL.get()),
                    "Eye Realm arrival should place a return Gazing Portal");

            DimensionTransition backToOverworld = portal.getPortalDestination(eyeRealm, traveler, arrivalBlock);
            helper.assertTrue(backToOverworld != null, "Return portal transition should not be null");
            helper.assertTrue(backToOverworld.newLevel().dimension().equals(Level.OVERWORLD),
                    "Eye Realm Gazing Portal should return to Overworld");
        } else {
            // GameTestServer only boots the 3 vanilla dimensions; verify findClosestPortal finds our Overworld portal
            BlockPos found = GazingPortalBlock.findClosestPortal(overworld, portalPos.offset(4, 0, 4), 16);
            helper.assertTrue(found != null && overworld.getBlockState(found).is(ArtilianBlocks.GAZING_PORTAL.get()),
                    "findClosestPortal should locate an existing Gazing Portal within search radius");
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void p4_01_gazingPortalArrivalIsAlwaysSafeAndCreatesLandingPad(GameTestHelper helper) {
        ServerLevel target = helper.getLevel().getServer().getLevel(EyeRealmDimension.EYE_REALM_LEVEL);
        if (target == null) {
            target = helper.getLevel();
        }

        BlockPos hint = helper.absolutePos(new BlockPos(6, -100, 6));
        BlockPos arrival = GazingPortalBlock.createReturnPortalWithPlatform(target, hint, Direction.Axis.Z);

        helper.assertTrue(arrival.getY() >= target.getMinBuildHeight() + 8,
                "P4-01: Arrival Y must never be in the void");
        helper.assertTrue(target.getBlockState(arrival).is(ArtilianBlocks.GAZING_PORTAL.get()),
                "P4-01: Arrival block must be GAZING_PORTAL");
        helper.assertTrue(target.getBlockState(arrival.above()).is(ArtilianBlocks.GAZING_PORTAL.get()),
                "P4-01: Arrival headspace must not suffocate the player");

        // Verify 5x5 Starlight Slate landing pad beneath the portal
        BlockPos padCenter = arrival.below(2);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockState padState = target.getBlockState(padCenter.offset(dx, 0, dz));
                helper.assertTrue(padState.is(ArtilianBlocks.STARLIGHT_SLATE.get()),
                        "P4-01: 5x5 Starlight Slate landing pad must be solid beneath arrival portal");
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gazingEyeRecipeAndPortalAssetsExist(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        helper.assertTrue(
                recipes.byKey(ArtilianMod.id("gazing_eye")).isPresent(),
                "artilian:gazing_eye recipe should be generated");

        helper.assertTrue(assetExists("assets/artilian/textures/block/gazing_portal.png"),
                "gazing_portal.png should exist");
        helper.assertTrue(assetExists("assets/artilian/textures/block/gazing_portal.png.mcmeta"),
                "gazing_portal.png.mcmeta should exist");
        helper.assertTrue(assetExists("assets/artilian/textures/item/gazing_eye.png"),
                "gazing_eye.png should exist");

        helper.succeed();
    }

    private static void buildSlateFrame(Level level, BlockPos bottomLeft, Direction.Axis axis) {
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockState slate = ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState();
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                BlockPos cell = bottomLeft.relative(right, w).above(h);
                boolean isEdge = w == 0 || w == 3 || h == 0 || h == 4;
                level.setBlock(cell, isEdge ? slate : Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static boolean assetExists(String path) {
        return EyeRealmPortalGameTests.class.getClassLoader().getResource(path) != null;
    }
}
