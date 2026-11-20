package com.silkpanda.artilian.worldgen;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

/** Tree growers for Artilian custom biome saplings. */
public final class ArtilianTreeGrowers {
    private ArtilianTreeGrowers() {}

    public static final TreeGrower CRYSTAL_CANOPY = new TreeGrower(
            ArtilianMod.MODID + ":crystal_canopy",
            Optional.empty(),
            Optional.of(ArtilianWorldGen.CRYSTAL_CANOPY_TREE),
            Optional.empty());

    public static final TreeGrower EMBERWOOD = new TreeGrower(
            ArtilianMod.MODID + ":emberwood",
            Optional.empty(),
            Optional.of(ArtilianWorldGen.EMBERWOOD_TREE),
            Optional.empty());

    public static final TreeGrower OCULAR = new TreeGrower(
            ArtilianMod.MODID + ":ocular",
            Optional.empty(),
            Optional.of(ArtilianWorldGen.OCULAR_TREE),
            Optional.empty());
}
