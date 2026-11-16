package com.silkpanda.artilian.worldgen;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;

/** Surface rules for Artilian's Overworld biomes. */
public final class ArtilianSurfaceRules {
    private ArtilianSurfaceRules() {}

    public static SurfaceRules.RuleSource makeRules() {
        SurfaceRules.RuleSource crystalCanopy = turfRule(
                ArtilianBiomes.CRYSTAL_CANOPY_WOODS, ArtilianBlocks.CRYSTAL_TURF.get(), Blocks.DIRT);
        SurfaceRules.RuleSource emberbloomMeadows = turfRule(
                ArtilianBiomes.EMBERBLOOM_MEADOWS, ArtilianBlocks.EMBER_TURF.get(), Blocks.DIRT);
        SurfaceRules.RuleSource starlightHighlands = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(ArtilianBiomes.STARLIGHT_HIGHLANDS),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                                SurfaceRules.steep(),
                                SurfaceRules.state(ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState())),
                        turfFloor(ArtilianBlocks.STARLIGHT_TURF.get(), ArtilianBlocks.STARLIGHT_SLATE.get())));

        return SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.sequence(crystalCanopy, emberbloomMeadows, starlightHighlands));
    }

    private static SurfaceRules.RuleSource turfRule(ResourceKey<Biome> biome, Block turfBlock, Block underFloorBlock) {
        return SurfaceRules.ifTrue(SurfaceRules.isBiome(biome), turfFloor(turfBlock, underFloorBlock));
    }

    private static SurfaceRules.RuleSource turfFloor(Block turfBlock, Block underFloorBlock) {
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(
                                SurfaceRules.waterBlockCheck(-1, 0),
                                SurfaceRules.state(turfBlock.defaultBlockState()))),
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.state(underFloorBlock.defaultBlockState())),
                SurfaceRules.ifTrue(
                        SurfaceRules.UNDER_FLOOR,
                        SurfaceRules.state(underFloorBlock.defaultBlockState()))
        );
    }
}
