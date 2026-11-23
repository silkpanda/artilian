package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.worldgen.ArtilianWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.Set;

/** Phase 4 Stage 2: Eye Realm noise terrain, Eye Turf, Gaze Stone, Ocular Trees, and Blinking Skybox. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class EyeRealmTerrainGameTests {

    @GameTest(template = "empty")
    public static void eyeRealmUsesNoiseSettingsAndCustomSkyEffects(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();

        DimensionType eyeType = registries.registryOrThrow(Registries.DIMENSION_TYPE).get(EyeRealmDimension.EYE_REALM_TYPE);
        helper.assertTrue(eyeType != null, "EYE_REALM_TYPE should be registered");
        helper.assertTrue(EyeRealmDimension.EYE_REALM_EFFECTS.equals(eyeType.effectsLocation()),
                "Eye Realm DimensionType should reference custom artilian:eye_realm sky effects");

        NoiseGeneratorSettings noise = registries.registryOrThrow(Registries.NOISE_SETTINGS)
                .get(EyeRealmDimension.EYE_REALM_NOISE);
        helper.assertTrue(noise != null, "EYE_REALM_NOISE should be registered");
        helper.assertTrue(noise.defaultBlock().is(ArtilianBlocks.GAZE_STONE.get()),
                "Eye Realm noise defaultBlock should be Gaze Stone");

        helper.assertTrue(
                EyeRealmTerrainGameTests.class.getClassLoader()
                        .getResource("data/artilian/worldgen/noise_settings/eye_realm.json") != null,
                "data/artilian/worldgen/noise_settings/eye_realm.json should be generated");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eyeRealmBiomeContainsOcularTreesAndBlinkingEyes(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        Biome biome = registries.registryOrThrow(Registries.BIOME).get(EyeRealmDimension.EYE_REALM_BIOME);
        helper.assertTrue(biome != null, "EYE_REALM_BIOME should be registered");

        Set<String> placedIds = new HashSet<>();
        for (var step : biome.getGenerationSettings().features()) {
            for (Holder<PlacedFeature> holder : step) {
                holder.unwrapKey().ifPresent(key -> placedIds.add(key.location().toString()));
            }
        }

        helper.assertTrue(placedIds.contains(ArtilianWorldGen.OCULAR_TREE_PLACED.location().toString()),
                "Eye Realm biome should include artilian:ocular_tree_placed");
        helper.assertTrue(placedIds.contains(ArtilianWorldGen.BLINKING_EYE_PATCH_PLACED.location().toString()),
                "Eye Realm biome should include artilian:blinking_eye_patch_placed");

        helper.succeed();
    }

    @GameTest(template = "worldgen")
    public static void ocularSaplingGrowsOcularTreeOnEyeTurf(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos ground = helper.absolutePos(new BlockPos(3, 1, 3));
        level.setBlockAndUpdate(ground, ArtilianBlocks.EYE_TURF.get().defaultBlockState());

        BlockPos saplingPos = ground.above();
        for (BlockPos clear : BlockPos.betweenClosed(saplingPos.offset(-2, 0, -2), saplingPos.offset(2, 8, 2))) {
            level.setBlockAndUpdate(clear, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        }
        BlockState saplingState = ArtilianBlocks.OCULAR_SAPLING.get().defaultBlockState();
        level.setBlockAndUpdate(saplingPos, saplingState);

        SaplingBlock sapling = (SaplingBlock) ArtilianBlocks.OCULAR_SAPLING.get();
        for (int i = 0; i < 6 && level.getBlockState(saplingPos).is(ArtilianBlocks.OCULAR_SAPLING.get()); i++) {
            sapling.advanceTree(level, saplingPos, level.getBlockState(saplingPos), level.random);
        }

        helper.assertTrue(level.getBlockState(saplingPos).is(ArtilianBlocks.OCULAR_LOG.get()),
                "Ocular Sapling should grow an Ocular Log trunk on Eye Turf");

        boolean foundLeaves = false;
        for (BlockPos pos : BlockPos.betweenClosed(saplingPos.offset(-3, 2, -3), saplingPos.offset(3, 8, 3))) {
            if (level.getBlockState(pos).is(ArtilianBlocks.OCULAR_LEAVES.get())) {
                foundLeaves = true;
                break;
            }
        }
        helper.assertTrue(foundLeaves, "Grown Ocular Tree should have Ocular Leaves canopy");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eyeRealmBlocksHaveExpectedTagsAndLightLevels(GameTestHelper helper) {
        BlockState turf = ArtilianBlocks.EYE_TURF.get().defaultBlockState();
        BlockState gazeStone = ArtilianBlocks.GAZE_STONE.get().defaultBlockState();
        BlockState blinkingEye = ArtilianBlocks.BLINKING_EYE_BLOCK.get().defaultBlockState();
        BlockState ocularLog = ArtilianBlocks.OCULAR_LOG.get().defaultBlockState();
        BlockState ocularLeaves = ArtilianBlocks.OCULAR_LEAVES.get().defaultBlockState();

        helper.assertTrue(turf.is(BlockTags.DIRT), "Eye Turf should be in BlockTags.DIRT");
        helper.assertTrue(gazeStone.is(BlockTags.MINEABLE_WITH_PICKAXE),
                "Gaze Stone should be mineable with pickaxe");
        helper.assertTrue(blinkingEye.getLightEmission() == 7,
                "Blinking Eye Block should emit light level 7");
        helper.assertTrue(ocularLog.is(BlockTags.LOGS_THAT_BURN),
                "Ocular Log should be in BlockTags.LOGS_THAT_BURN");
        helper.assertTrue(ocularLeaves.is(BlockTags.LEAVES),
                "Ocular Leaves should be in BlockTags.LEAVES");

        helper.succeed();
    }
}
