package com.silkpanda.artilian.datagen;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.worldgen.ArtilianStructures;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.storage.loot.LootTable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the three Phase 2 Stage 6 Overworld biome structure templates ({@code .nbt}) with
 * loot chests and persistent {@code artilian:structure_enemies} guardians.
 */
public class ArtilianStructureTemplateProvider implements DataProvider {
    private final PackOutput output;

    public ArtilianStructureTemplateProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return CompletableFuture.runAsync(() -> {
            writeTemplate(cachedOutput, "crystal_grove_shrine", buildCrystalGroveShrine());
            writeTemplate(cachedOutput, "emberbloom_outpost", buildEmberbloomOutpost());
            writeTemplate(cachedOutput, "starlight_watchtower", buildStarlightWatchtower());
            writeTemplate(cachedOutput, "crystal_colossus_arena", buildCrystalColossusArena());
        });
    }

    private static CompoundTag buildCrystalGroveShrine() {
        TemplateBuilder b = new TemplateBuilder(15, 13, 15);
        BlockState planks = ArtilianBlocks.CRYSTAL_CANOPY_PLANKS.get().defaultBlockState();
        BlockState log = ArtilianBlocks.CRYSTAL_CANOPY_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState turf = ArtilianBlocks.CRYSTAL_TURF.get().defaultBlockState();
        BlockState leaves = ArtilianBlocks.CRYSTAL_CANOPY_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);
        BlockState crystalBlock = ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState bloom = ArtilianBlocks.CRYSTAL_BLOOM.get().defaultBlockState();
        BlockState fence = ArtilianBlocks.CRYSTAL_CANOPY_FENCE.get().defaultBlockState();
        Block crystalStairs = ArtilianBlocks.CRYSTAL_CANOPY_STAIRS.get();
        Block crystalSlab = ArtilianBlocks.CRYSTAL_CANOPY_SLAB.get();
        Block crystalTrapdoor = ArtilianBlocks.CRYSTAL_CANOPY_TRAPDOOR.get();

        BlockState chiseledStone = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        BlockState mossyStone = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        BlockState mossyWall = Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState();
        BlockState mossBlock = Blocks.MOSS_BLOCK.defaultBlockState();
        BlockState mossCarpet = Blocks.MOSS_CARPET.defaultBlockState();
        BlockState soulLantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
        BlockState hangingLantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        BlockState amethystUp = Blocks.AMETHYST_CLUSTER.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, Direction.UP);

        // Clear upper volume
        b.fillBox(0, 1, 0, 14, 12, 14, Blocks.AIR.defaultBlockState());

        // y = 0: 15x15 ground-level stone foundation with moss & front crystal turf corners
        for (int x = 0; x <= 14; x++) {
            for (int z = 0; z <= 14; z++) {
                b.set(x, 0, z, weatheredStone(x, 0, z));
            }
        }
        b.set(0, 0, 0, mossBlock);
        b.set(14, 0, 0, mossBlock);
        b.set(0, 0, 14, turf);
        b.set(14, 0, 14, turf);
        b.set(0, 1, 14, bloom);
        b.set(14, 1, 14, bloom);

        // y = 1: First stepped plinth (13x13 at 1..13) + wide South temple staircase
        for (int x = 2; x <= 12; x++) {
            for (int z = 2; z <= 12; z++) {
                b.set(x, 1, z, weatheredStone(x, 1, z));
            }
        }
        for (int i = 3; i <= 11; i++) {
            b.set(i, 1, 1, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            b.set(1, 1, i, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
            b.set(13, 1, i, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
        }
        for (int x = 4; x <= 10; x++) {
            b.set(x, 1, 13, stair(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
        }
        // Corner plinth pedestals + front stair flower planters
        for (int cx : new int[]{1, 13}) {
            for (int cz : new int[]{1, 13}) {
                b.set(cx, 1, cz, chiseledStone);
                b.set(cx, 2, cz, mossyWall);
                b.set(cx, 3, cz, soulLantern);
            }
        }
        b.set(2, 1, 1, mossyStone);
        b.set(1, 1, 2, mossyStone);
        b.set(12, 1, 1, mossyStone);
        b.set(13, 1, 2, mossyStone);
        b.set(2, 1, 13, mossyStone);
        b.set(1, 1, 12, mossyStone);
        b.set(12, 1, 13, mossyStone);
        b.set(13, 1, 12, mossyStone);
        b.set(3, 1, 13, turf);
        b.set(11, 1, 13, turf);
        b.set(3, 2, 13, bloom);
        b.set(11, 2, 13, bloom);

        // y = 2: Second stepped plinth (11x11 at 2..12) + Sanctuary Floor
        for (int x = 3; x <= 11; x++) {
            for (int z = 3; z <= 11; z++) {
                boolean perimeter = (x == 3 || x == 11 || z == 3 || z == 11);
                b.set(x, 2, z, perimeter ? weatheredStone(x, 2, z) : planks);
            }
        }
        // Sanctuary floor stone inlay & crystal core
        b.set(7, 2, 7, crystalBlock);
        b.set(6, 2, 7, chiseledStone);
        b.set(8, 2, 7, chiseledStone);
        b.set(7, 2, 6, chiseledStone);
        b.set(7, 2, 8, chiseledStone);
        for (int i = 3; i <= 11; i++) {
            b.set(i, 2, 2, stair(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            b.set(2, 2, i, stair(Blocks.STONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
            b.set(12, 2, i, stair(Blocks.STONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
        }
        b.set(3, 2, 12, mossyStone);
        b.set(4, 2, 12, mossyStone);
        b.set(10, 2, 12, mossyStone);
        b.set(11, 2, 12, mossyStone);
        for (int x = 5; x <= 9; x++) {
            b.set(x, 2, 12, stair(crystalStairs, Direction.NORTH, Half.BOTTOM));
        }
        for (int cx : new int[]{2, 12}) {
            for (int cz : new int[]{2, 12}) {
                b.set(cx, 2, cz, mossyStone);
            }
        }

        // y = 3: Corner buttress slopes leaning into corner pillars
        b.set(2, 3, 3, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
        b.set(12, 3, 3, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
        b.set(2, 3, 11, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
        b.set(12, 3, 11, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
        b.set(3, 3, 2, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
        b.set(11, 3, 2, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
        b.set(3, 3, 12, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
        b.set(11, 3, 12, stair(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));

        // y = 3..6: Colonnade columns (corners + bay dividers)
        int[][] columns = {
                {3, 3}, {5, 3}, {9, 3}, {11, 3},
                {3, 5}, {3, 9},
                {11, 5}, {11, 9},
                {3, 11}, {5, 11}, {9, 11}, {11, 11}
        };
        for (int[] col : columns) {
            b.set(col[0], 2, col[1], chiseledStone);
            b.set(col[0], 3, col[1], chiseledStone);
            b.fillBox(col[0], 4, col[1], col[0], 6, col[1], log);
        }

        // Front facade (z = 11): open 3-wide arched doorway + crystal fence side screens
        b.set(6, 6, 11, stair(crystalStairs, Direction.WEST, Half.TOP));
        b.set(8, 6, 11, stair(crystalStairs, Direction.EAST, Half.TOP));
        b.set(7, 6, 11, slab(crystalSlab, SlabType.TOP));
        for (int bx : new int[]{4, 10}) {
            b.set(bx, 3, 11, mossyStone);
            b.set(bx, 4, 11, fence);
            b.set(bx, 5, 11, fence);
            b.set(bx, 6, 11, chiseledStone);
        }

        // West (x = 3) & East (x = 11) walls: narrow fence bays + central arched colonnade windows
        for (int wx : new int[]{3, 11}) {
            for (int bz : new int[]{4, 10}) {
                b.set(wx, 3, bz, mossyStone);
                b.set(wx, 4, bz, fence);
                b.set(wx, 5, bz, fence);
                b.set(wx, 6, bz, chiseledStone);
            }
            b.set(wx, 3, 6, weatheredStone(wx, 3, 6));
            b.set(wx, 3, 7, chiseledStone);
            b.set(wx, 3, 8, weatheredStone(wx, 3, 8));
            b.set(wx, 4, 6, fence);
            b.set(wx, 4, 7, fence);
            b.set(wx, 4, 8, fence);
            b.set(wx, 5, 6, fence);
            b.set(wx, 5, 8, fence);
            b.set(wx, 6, 6, stair(crystalStairs, Direction.NORTH, Half.TOP));
            b.set(wx, 6, 7, slab(crystalSlab, SlabType.TOP));
            b.set(wx, 6, 8, stair(crystalStairs, Direction.SOUTH, Half.TOP));
        }

        // Back wall (z = 3): glowing crystal mosaic behind altar + side fence slits
        for (int bx : new int[]{4, 10}) {
            b.set(bx, 3, 3, mossyStone);
            b.set(bx, 4, 3, fence);
            b.set(bx, 5, 3, fence);
            b.set(bx, 6, 3, chiseledStone);
        }
        b.set(6, 3, 3, weatheredStone(6, 3, 3));
        b.set(7, 3, 3, chiseledStone);
        b.set(8, 3, 3, weatheredStone(8, 3, 3));
        b.set(6, 4, 3, chiseledStone);
        b.set(7, 4, 3, crystalBlock);
        b.set(8, 4, 3, chiseledStone);
        b.set(6, 5, 3, fence);
        b.set(7, 5, 3, crystalBlock);
        b.set(8, 5, 3, fence);
        b.set(6, 6, 3, stair(crystalStairs, Direction.WEST, Half.TOP));
        b.set(7, 6, 3, slab(crystalSlab, SlabType.TOP));
        b.set(8, 6, 3, stair(crystalStairs, Direction.EAST, Half.TOP));

        // y = 7: Carved frieze band + jutting corbel teeth + trapdoor relief panels + interior lanterns
        for (int i = 3; i <= 11; i++) {
            BlockState friezeBlock = (i == 7) ? crystalBlock : (i % 2 == 1 ? chiseledStone : planks);
            b.set(i, 7, 3, friezeBlock);
            b.set(i, 7, 11, friezeBlock);
            b.set(3, 7, i, friezeBlock);
            b.set(11, 7, i, friezeBlock);
        }
        for (int i = 2; i <= 12; i += 2) {
            b.set(i, 7, 12, stair(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, Half.TOP));
            b.set(i, 7, 2, stair(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.TOP));
            if (i >= 4 && i <= 10) {
                b.set(2, 7, i, stair(Blocks.STONE_BRICK_STAIRS, Direction.EAST, Half.TOP));
                b.set(12, 7, i, stair(Blocks.STONE_BRICK_STAIRS, Direction.WEST, Half.TOP));
            }
        }
        for (int t : new int[]{5, 9}) {
            b.set(t, 7, 12, trapdoor(crystalTrapdoor, Direction.SOUTH, Half.BOTTOM, true));
            b.set(t, 7, 2, trapdoor(crystalTrapdoor, Direction.NORTH, Half.BOTTOM, true));
            b.set(2, 7, t, trapdoor(crystalTrapdoor, Direction.WEST, Half.BOTTOM, true));
            b.set(12, 7, t, trapdoor(crystalTrapdoor, Direction.EAST, Half.BOTTOM, true));
        }
        for (int lx : new int[]{5, 9}) {
            for (int lz : new int[]{5, 9}) {
                b.set(lx, 7, lz, hangingLantern);
            }
        }

        // y = 8: 11x11 overhanging cornice & ceiling deck
        for (int x = 2; x <= 12; x++) {
            for (int z = 2; z <= 12; z++) {
                boolean outer = (x == 2 || x == 12 || z == 2 || z == 12 || x == 3 || x == 11 || z == 3 || z == 11);
                b.set(x, 8, z, outer ? weatheredStone(x, 8, z) : planks);
            }
        }
        b.set(2, 8, 2, chiseledStone);
        b.set(12, 8, 2, chiseledStone);
        b.set(2, 8, 12, chiseledStone);
        b.set(12, 8, 12, chiseledStone);
        b.set(7, 8, 7, crystalBlock);

        // y = 9: Stepped Roof Tier 1 (9x9 at 3..11) using Crystal Canopy stairs & slabs
        for (int x = 4; x <= 10; x++) {
            for (int z = 4; z <= 10; z++) {
                b.set(x, 9, z, weatheredStone(x, 9, z));
            }
        }
        for (int i = 4; i <= 10; i++) {
            b.set(i, 9, 11, stair(crystalStairs, Direction.NORTH, Half.BOTTOM));
            b.set(i, 9, 3, stair(crystalStairs, Direction.SOUTH, Half.BOTTOM));
            b.set(3, 9, i, stair(crystalStairs, Direction.EAST, Half.BOTTOM));
            b.set(11, 9, i, stair(crystalStairs, Direction.WEST, Half.BOTTOM));
        }
        b.set(3, 9, 3, slab(crystalSlab, SlabType.BOTTOM));
        b.set(11, 9, 3, slab(crystalSlab, SlabType.BOTTOM));
        b.set(3, 9, 11, slab(crystalSlab, SlabType.BOTTOM));
        b.set(11, 9, 11, slab(crystalSlab, SlabType.BOTTOM));

        // y = 10..11: Stepped Roof Tier 2 (7x7 at 4..10) + Crystal Crown
        for (int x = 5; x <= 9; x++) {
            for (int z = 5; z <= 9; z++) {
                b.set(x, 10, z, slab(crystalSlab, SlabType.BOTTOM));
            }
        }
        for (int i = 5; i <= 9; i++) {
            b.set(i, 10, 10, stair(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
            b.set(i, 10, 4, stair(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            b.set(4, 10, i, stair(Blocks.STONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
            b.set(10, 10, i, stair(Blocks.STONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
        }
        b.set(4, 10, 4, slab(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        b.set(10, 10, 4, slab(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        b.set(4, 10, 10, slab(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        b.set(10, 10, 10, slab(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        b.set(7, 10, 6, planks);
        b.set(7, 10, 8, planks);
        b.set(6, 10, 7, planks);
        b.set(8, 10, 7, planks);
        b.set(7, 10, 7, chiseledStone);
        b.set(7, 11, 7, crystalBlock);
        b.set(7, 12, 7, amethystUp);

        // Overgrowth: Crystal Canopy Leaves draping corners, moss carpet & hanging vines
        int[][] leafSpots = {
                {3, 10, 3}, {4, 10, 3}, {3, 10, 4}, {2, 9, 3}, {2, 9, 4}, {3, 9, 2},
                {11, 10, 11}, {11, 10, 10}, {12, 9, 10}, {11, 9, 12},
                {11, 9, 3}, {12, 9, 4}, {10, 10, 3}
        };
        for (int[] leaf : leafSpots) {
            b.set(leaf[0], leaf[1], leaf[2], leaves);
        }
        b.set(5, 9, 12, mossCarpet);
        b.set(9, 9, 2, mossCarpet);
        b.set(2, 9, 8, mossCarpet);
        b.set(12, 9, 6, mossCarpet);

        // Vines clinging to exterior walls and columns (Direction = side where solid block sits)
        for (int y = 4; y <= 7; y++) {
            b.set(3, y, 12, vine(true, false, false, false));
            b.set(11, y, 12, vine(true, false, false, false));
        }
        for (int y = 3; y <= 6; y++) {
            b.set(5, y, 12, vine(true, false, false, false));
            b.set(5, y, 2, vine(false, false, true, false));
            b.set(2, y, 5, vine(false, true, false, false));
            b.set(12, y, 9, vine(false, false, false, true));
        }
        for (int y = 4; y <= 6; y++) {
            b.set(9, y, 2, vine(false, false, true, false));
            b.set(2, y, 9, vine(false, true, false, false));
            b.set(12, y, 5, vine(false, false, false, true));
        }

        // Interior altar, crystal blooms, reward chest & two persistent Prism Sentinels
        b.set(4, 2, 4, turf);
        b.set(10, 2, 4, turf);
        b.set(4, 3, 4, bloom);
        b.set(10, 3, 4, bloom);
        b.set(6, 3, 4, chiseledStone);
        b.set(8, 3, 4, chiseledStone);
        b.set(6, 4, 4, amethystUp);
        b.set(8, 4, 4, amethystUp);
        b.set(7, 3, 4, crystalBlock);
        b.set(7, 4, 4, amethystUp);
        b.setChest(7, 3, 5, Direction.SOUTH, ArtilianStructures.CRYSTAL_GROVE_SHRINE_CHEST);

        b.addGuardian(ArtilianEntities.PRISM_SENTINEL.get(), 5.5, 3.0, 8.5);
        b.addGuardian(ArtilianEntities.PRISM_SENTINEL.get(), 9.5, 3.0, 8.5);
        b.connectFencesAndWalls();
        return b.build();
    }

    /**
     * Timber-framed workshop on a rubble-stone base, a fenced front porch, a palisade yard with a
     * lantern-lit gate, and a stone chimney venting campfire smoke. Front (gate, porch, door) faces
     * south (+Z). 15 x 13 x 15.
     */
    private static CompoundTag buildEmberbloomOutpost() {
        TemplateBuilder b = new TemplateBuilder(15, 13, 15);
        BlockState planks = ArtilianBlocks.EMBERWOOD_PLANKS.get().defaultBlockState();
        BlockState logY = ArtilianBlocks.EMBERWOOD_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState logX = logY.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState logZ = logY.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        BlockState turf = ArtilianBlocks.EMBER_TURF.get().defaultBlockState();
        BlockState bloom = ArtilianBlocks.EMBER_BLOOM.get().defaultBlockState();
        BlockState fence = ArtilianBlocks.EMBERWOOD_FENCE.get().defaultBlockState();
        Block emberStairs = ArtilianBlocks.EMBERWOOD_STAIRS.get();
        Block emberSlab = ArtilianBlocks.EMBERWOOD_SLAB.get();
        BlockState lantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
        BlockState hangingLantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        BlockState path = Blocks.DIRT_PATH.defaultBlockState();
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();

        // Clear the volume above ground so terrain and trees never poke through the yard.
        b.fillBox(0, 1, 0, 14, 12, 14, Blocks.AIR.defaultBlockState());

        // y = 0: ember turf yard, dirt path from the gate to the porch.
        b.fillBox(0, 0, 0, 14, 0, 14, turf);
        b.fillBox(6, 0, 12, 8, 0, 13, path);
        b.set(7, 0, 14, path);

        // ---- Palisade: ragged log posts with fence spikes, lanterns on the corners ----
        for (int i = 0; i <= 14; i++) {
            palisadePost(b, i, 0, logY, fence);
            palisadePost(b, 0, i, logY, fence);
            palisadePost(b, 14, i, logY, fence);
            if (i < 6 || i > 8) {
                palisadePost(b, i, 14, logY, fence);
            }
        }
        for (int cx : new int[]{0, 14}) {
            for (int cz : new int[]{0, 14}) {
                b.fillBox(cx, 1, cz, cx, 3, cz, logY);
                b.set(cx, 4, cz, lantern);
            }
        }
        // Gatehouse: two tall posts, a lintel beam, lanterns, and the gate itself.
        for (int gx : new int[]{6, 8}) {
            b.fillBox(gx, 1, 14, gx, 4, 14, logY);
            b.set(gx, 5, 14, lantern);
        }
        b.set(7, 4, 14, logX);
        b.set(7, 3, 14, hangingLantern);
        b.set(7, 1, 14, ArtilianBlocks.EMBERWOOD_FENCE_GATE.get().defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.SOUTH)
                .setValue(FenceGateBlock.OPEN, false)
                .setValue(FenceGateBlock.IN_WALL, false));

        // ---- Workshop: footprint x 3..11, z 2..8 ----
        // Floor
        b.fillBox(4, 0, 3, 10, 0, 7, planks);
        for (int x = 3; x <= 11; x++) {
            for (int z = 2; z <= 8; z++) {
                boolean perimeter = x == 3 || x == 11 || z == 2 || z == 8;
                if (perimeter) {
                    b.set(x, 0, z, stoneBricks);
                    // y = 1..2: rubble-stone base
                    b.set(x, 1, z, rubbleStone(x, 1, z));
                    b.set(x, 2, z, rubbleStone(x, 2, z));
                }
            }
        }
        // y = 3 sill beam and y = 6 top plate: horizontal logs along each wall, vertical at corners.
        for (int y : new int[]{3, 6}) {
            for (int x = 3; x <= 11; x++) {
                b.set(x, y, 2, logX);
                b.set(x, y, 8, logX);
            }
            for (int z = 3; z <= 7; z++) {
                b.set(3, y, z, logZ);
                b.set(11, y, z, logZ);
            }
            for (int cx : new int[]{3, 11}) {
                for (int cz : new int[]{2, 8}) {
                    b.set(cx, y, cz, logY);
                }
            }
        }
        // y = 4..5: pale daub infill between vertical posts, so the dark timber frame reads.
        BlockState daub = Blocks.CALCITE.defaultBlockState();
        for (int y = 4; y <= 5; y++) {
            for (int x = 3; x <= 11; x++) {
                BlockState infill = (x == 3 || x == 7 || x == 11) ? logY : daub;
                b.set(x, y, 2, infill);
                b.set(x, y, 8, infill);
            }
            for (int z = 3; z <= 7; z++) {
                BlockState infill = (z == 5) ? logY : daub;
                b.set(3, y, z, infill);
                b.set(11, y, z, infill);
            }
        }
        // Windows: amber panes in the timber on the back and sides, low windows under the porch.
        BlockState paneAlongX = pane(false, true, false, true);
        BlockState paneAlongZ = pane(true, false, true, false);
        for (int wx : new int[]{5, 9}) {
            b.set(wx, 4, 2, paneAlongX);
            b.set(wx, 2, 8, paneAlongX);
        }
        for (int wz : new int[]{4, 6}) {
            b.set(3, 4, wz, paneAlongZ);
            b.set(11, 4, wz, paneAlongZ);
        }
        // Front door
        BlockState door = ArtilianBlocks.EMBERWOOD_DOOR.get().defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false);
        b.set(7, 1, 8, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        b.set(7, 2, 8, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        // Interior tie beams carry the hanging lanterns.
        for (int tx : new int[]{5, 9}) {
            for (int z = 3; z <= 7; z++) {
                b.set(tx, 6, z, logZ);
            }
            b.set(tx, 5, 5, hangingLantern);
        }

        // ---- Gable roof, ridge running east-west, 1-block overhang all round ----
        for (int x = 2; x <= 12; x++) {
            for (int step = 0; step <= 3; step++) {
                b.set(x, 6 + step, 1 + step, stair(emberStairs, Direction.SOUTH, Half.BOTTOM));
                b.set(x, 6 + step, 9 - step, stair(emberStairs, Direction.NORTH, Half.BOTTOM));
            }
            b.set(x, 10, 5, slab(emberSlab, SlabType.BOTTOM));
        }
        // Eave trim: upside-down stairs tucked under the overhang on the long sides.
        for (int x = 3; x <= 11; x++) {
            b.set(x, 5, 1, stair(emberStairs, Direction.SOUTH, Half.TOP));
        }
        // Gable ends
        for (int gx : new int[]{3, 11}) {
            for (int z = 3; z <= 7; z++) {
                b.set(gx, 7, z, daub);
            }
            for (int z = 4; z <= 6; z++) {
                b.set(gx, 8, z, daub);
            }
            b.set(gx, 9, 5, daub);
            b.set(gx, 8, 5, logX);
        }

        // ---- Chimney on the east gable, venting campfire smoke above the ridge ----
        for (int y = 0; y <= 11; y++) {
            b.set(12, y, 5, (y % 3 == 0) ? stoneBricks : rubbleStone(12, y, 5));
        }
        for (int cz : new int[]{4, 6}) {
            b.set(12, 1, cz, Blocks.COBBLESTONE.defaultBlockState());
            b.set(12, 2, cz, slab(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        }
        b.set(12, 12, 5, Blocks.CAMPFIRE.defaultBlockState()
                .setValue(CampfireBlock.LIT, true)
                .setValue(CampfireBlock.SIGNAL_FIRE, false)
                .setValue(CampfireBlock.FACING, Direction.SOUTH));

        // ---- Front porch: z 9..11, plank floor, fenced rail, slab roof on log posts ----
        b.fillBox(3, 0, 9, 11, 0, 11, planks);
        for (int px : new int[]{3, 11}) {
            b.fillBox(px, 1, 11, px, 3, 11, logY);
            b.set(px, 1, 9, fence);
            b.set(px, 1, 10, fence);
        }
        for (int x : new int[]{4, 5, 9, 10}) {
            b.set(x, 1, 11, fence);
        }
        // Lean-to porch roof stepping down from the wall to the posts.
        for (int x = 2; x <= 12; x++) {
            b.set(x, 5, 9, stair(emberStairs, Direction.NORTH, Half.BOTTOM));
            b.set(x, 4, 10, stair(emberStairs, Direction.NORTH, Half.BOTTOM));
            b.set(x, 4, 11, slab(emberSlab, SlabType.BOTTOM));
        }
        b.set(5, 3, 10, hangingLantern);
        b.set(9, 3, 10, hangingLantern);

        // ---- Interior: workbench corner, barrels, furnace by the chimney, red runner to the chest ----
        b.set(4, 1, 3, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(5, 1, 3, Blocks.SMITHING_TABLE.defaultBlockState());
        BlockState barrel = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP);
        b.set(10, 1, 3, barrel);
        b.set(10, 2, 3, lantern);
        b.set(10, 1, 4, barrel);
        b.set(10, 1, 6, Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.WEST));
        b.set(4, 1, 7, Blocks.BOOKSHELF.defaultBlockState());
        for (int z = 4; z <= 7; z++) {
            b.set(7, 1, z, Blocks.RED_CARPET.defaultBlockState());
        }
        b.setChest(7, 1, 3, Direction.SOUTH, ArtilianStructures.EMBERBLOOM_OUTPOST_CHEST);

        // ---- Yard dressing: woodpile, hay, ember blooms ----
        b.set(1, 1, 5, logZ);
        b.set(1, 1, 6, logZ);
        b.set(1, 1, 7, logZ);
        b.set(1, 2, 5, logZ);
        b.set(1, 2, 6, logZ);
        BlockState hay = Blocks.HAY_BLOCK.defaultBlockState();
        b.set(12, 1, 12, hay);
        b.set(13, 1, 12, hay);
        b.set(13, 2, 12, hay);
        int[][] blooms = {{2, 3}, {1, 10}, {13, 9}, {12, 2}, {2, 13}, {13, 13}, {1, 2}, {4, 13}, {10, 13}};
        for (int[] spot : blooms) {
            b.set(spot[0], 1, spot[1], bloom);
        }

        // Two persistent Cinder Raiders guard the yard in front of the porch.
        b.addGuardian(ArtilianEntities.CINDER_RAIDER.get(), 4.5, 1.0, 12.5);
        b.addGuardian(ArtilianEntities.CINDER_RAIDER.get(), 10.5, 1.0, 12.5);
        b.connectFencesAndWalls();
        return b.build();
    }

    /** Palisade cell. Every third block is a log post with a fence spike; the rest is two-high fence rail, so you can see into the yard. */
    private static void palisadePost(TemplateBuilder b, int x, int z, BlockState logY, BlockState fence) {
        int along = (x == 0 || x == 14) ? z : x;
        if (along % 3 == 0 || along == 14) {
            b.fillBox(x, 1, z, x, 2, z, logY);
            b.set(x, 3, z, fence);
        } else {
            b.set(x, 1, z, fence);
            b.set(x, 2, z, fence);
        }
    }

    private static BlockState rubbleStone(int x, int y, int z) {
        int hash = Math.floorMod(x * 73856093 ^ y * 19349663 ^ z * 83492791, 10);
        if (hash < 3) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        }
        if (hash < 7) {
            return Blocks.COBBLESTONE.defaultBlockState();
        }
        return Blocks.STONE_BRICKS.defaultBlockState();
    }

    private static BlockState pane(boolean north, boolean east, boolean south, boolean west) {
        return Blocks.ORANGE_STAINED_GLASS_PANE.defaultBlockState()
                .setValue(CrossCollisionBlock.NORTH, north)
                .setValue(CrossCollisionBlock.EAST, east)
                .setValue(CrossCollisionBlock.SOUTH, south)
                .setValue(CrossCollisionBlock.WEST, west);
    }

    private static CompoundTag buildStarlightWatchtower() {
        TemplateBuilder b = new TemplateBuilder(13, 20, 13);
        BlockState slate = ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState();
        BlockState bricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState tiles = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState polished = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState chiseled = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState turf = ArtilianBlocks.STARLIGHT_TURF.get().defaultBlockState();
        BlockState blossom = ArtilianBlocks.STARLIGHT_BLOSSOM.get().defaultBlockState();
        BlockState crystalBlock = ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState amethystBlock = Blocks.AMETHYST_BLOCK.defaultBlockState();
        BlockState soulLantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
        BlockState hangingLantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        BlockState slateWall = ArtilianBlocks.STARLIGHT_SLATE_WALL.get().defaultBlockState();
        Block slateStairs = ArtilianBlocks.STARLIGHT_SLATE_STAIRS.get();
        Block slateSlab = ArtilianBlocks.STARLIGHT_SLATE_SLAB.get();
        Block tileStairs = Blocks.DEEPSLATE_TILE_STAIRS;
        Block tileSlab = Blocks.DEEPSLATE_TILE_SLAB;

        BlockState purplePaneX = Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState()
                .setValue(CrossCollisionBlock.NORTH, false)
                .setValue(CrossCollisionBlock.EAST, true)
                .setValue(CrossCollisionBlock.SOUTH, false)
                .setValue(CrossCollisionBlock.WEST, true);
        BlockState purplePaneZ = Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState()
                .setValue(CrossCollisionBlock.NORTH, true)
                .setValue(CrossCollisionBlock.EAST, false)
                .setValue(CrossCollisionBlock.SOUTH, true)
                .setValue(CrossCollisionBlock.WEST, false);

        // Clear upper volume so Starlight Highlands hills/boulders never clip inside.
        b.fillBox(0, 1, 0, 12, 19, 12, Blocks.AIR.defaultBlockState());

        // y = 0: 13x13 ground foundation with deepslate-brick apron and starlight blossom corners.
        b.fillBox(0, 0, 0, 12, 0, 12, bricks);
        b.fillBox(1, 0, 1, 11, 0, 11, slate);
        b.fillBox(3, 0, 3, 9, 0, 9, polished);
        for (int[] spot : new int[][]{{1, 1}, {11, 1}, {1, 11}, {11, 11}, {1, 6}, {11, 6}, {6, 1}}) {
            b.set(spot[0], 0, spot[1], turf);
            b.set(spot[0], 1, spot[1], blossom);
        }

        // y = 1: Stepped plinth (9x9 at 2..10) + Floor 1 floor (top at y = 2.0) + South stairs.
        b.fillBox(2, 1, 2, 10, 1, 10, slate);
        b.fillBox(4, 1, 4, 8, 1, 8, polished);
        for (int x = 5; x <= 7; x++) {
            b.set(x, 1, 10, stair(slateStairs, Direction.NORTH, Half.BOTTOM));
            b.set(x, 1, 9, polished);
        }
        for (int i = 3; i <= 9; i++) {
            b.set(i, 1, 2, stair(slateStairs, Direction.SOUTH, Half.BOTTOM));
            b.set(2, 1, i, stair(slateStairs, Direction.EAST, Half.BOTTOM));
            b.set(10, 1, i, stair(slateStairs, Direction.WEST, Half.BOTTOM));
        }
        for (int cx : new int[]{2, 10}) {
            for (int cz : new int[]{2, 10}) {
                b.set(cx, 1, cz, chiseled);
                b.set(cx, 2, cz, slateWall);
                b.set(cx, 3, cz, soulLantern);
            }
        }

        // Corner buttresses at y = 2 leaning into the 7x7 tower corners.
        b.set(2, 2, 3, stair(slateStairs, Direction.EAST, Half.BOTTOM));
        b.set(10, 2, 3, stair(slateStairs, Direction.WEST, Half.BOTTOM));
        b.set(2, 2, 9, stair(slateStairs, Direction.EAST, Half.BOTTOM));
        b.set(10, 2, 9, stair(slateStairs, Direction.WEST, Half.BOTTOM));
        b.set(3, 2, 2, stair(slateStairs, Direction.SOUTH, Half.BOTTOM));
        b.set(9, 2, 2, stair(slateStairs, Direction.SOUTH, Half.BOTTOM));

        // Tower shaft: 7x7 body (x,z 3..9), Floor 1 (y=2..4) and Floor 2 (y=6..8).
        for (int y = 2; y <= 8; y++) {
            for (int i = 3; i <= 9; i++) {
                b.set(i, y, 3, slate);
                b.set(i, y, 9, slate);
                b.set(3, y, i, slate);
                b.set(9, y, i, slate);
            }
            for (int cx : new int[]{3, 9}) {
                for (int cz : new int[]{3, 9}) {
                    b.set(cx, y, cz, (y == 4 || y == 8) ? chiseled : bricks);
                }
            }
        }

        // Ground-level arched doorway on the south face (z = 9, flush with y = 1 floor).
        BlockState doorBase = ArtilianBlocks.CRYSTAL_CANOPY_DOOR.get().defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false);
        b.set(6, 2, 9, doorBase.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        b.set(6, 3, 9, doorBase.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        b.set(6, 4, 9, chiseled);
        b.set(5, 4, 9, stair(slateStairs, Direction.WEST, Half.TOP));
        b.set(7, 4, 9, stair(slateStairs, Direction.EAST, Half.TOP));
        b.set(4, 3, 9, purplePaneX);
        b.set(8, 3, 9, purplePaneX);

        // Floor 1 windows (y = 3) on North, East, and West sides (keeping x=3, z=6 solid behind ladder).
        for (int wx : new int[]{5, 7}) {
            b.set(wx, 3, 3, purplePaneX);
        }
        b.set(6, 3, 3, slateWall);
        for (int wz : new int[]{5, 7}) {
            b.set(9, 3, wz, purplePaneZ);
        }
        b.set(9, 3, 6, slateWall);
        b.set(3, 3, 4, purplePaneZ);
        b.set(3, 3, 8, purplePaneZ);

        // Floor 2 (y = 5..8): floor + 3-wide framed windows with slate-wall mullions.
        b.fillBox(4, 5, 4, 8, 5, 8, polished);
        for (int y : new int[]{7, 8}) {
            b.set(5, y, 9, purplePaneX);
            b.set(6, y, 9, slateWall);
            b.set(7, y, 9, purplePaneX);
            b.set(5, y, 3, purplePaneX);
            b.set(6, y, 3, slateWall);
            b.set(7, y, 3, purplePaneX);
            b.set(9, y, 5, purplePaneZ);
            b.set(9, y, 6, slateWall);
            b.set(9, y, 7, purplePaneZ);
        }
        b.set(3, 7, 4, purplePaneZ);
        b.set(3, 7, 8, purplePaneZ);
        b.set(3, 6, 6, bricks);
        b.set(3, 7, 6, bricks);
        b.set(3, 8, 6, bricks);

        // Interior lighting for Floor 1 and Floor 2.
        b.set(6, 4, 6, hangingLantern);
        b.set(6, 8, 6, hangingLantern);

        // Tier 1 Flared Pagoda Eave at y = 5..6 (11x11 flare) with hanging corner soul lanterns.
        for (int i = 3; i <= 9; i++) {
            b.set(i, 5, 2, stair(slateStairs, Direction.SOUTH, Half.TOP));
            b.set(i, 5, 10, stair(slateStairs, Direction.NORTH, Half.TOP));
            b.set(2, 5, i, stair(slateStairs, Direction.EAST, Half.TOP));
            b.set(10, 5, i, stair(slateStairs, Direction.WEST, Half.TOP));
            b.set(i, 6, 2, slab(tileSlab, SlabType.BOTTOM));
            b.set(i, 6, 10, slab(tileSlab, SlabType.BOTTOM));
            b.set(2, 6, i, slab(tileSlab, SlabType.BOTTOM));
            b.set(10, 6, i, slab(tileSlab, SlabType.BOTTOM));
        }
        for (int cx : new int[]{2, 10}) {
            for (int cz : new int[]{2, 10}) {
                b.set(cx, 5, cz, tiles);
                b.set(cx, 4, cz, hangingLantern);
            }
        }

        // Tier 2 Pagoda Eave & Open Observation Deck (y = 9..12).
        b.fillBox(3, 9, 3, 9, 9, 9, polished);
        b.fillBox(4, 9, 4, 8, 9, 8, slate);
        for (int i = 3; i <= 9; i++) {
            b.set(i, 9, 2, stair(slateStairs, Direction.SOUTH, Half.TOP));
            b.set(i, 9, 10, stair(slateStairs, Direction.NORTH, Half.TOP));
            b.set(2, 9, i, stair(slateStairs, Direction.EAST, Half.TOP));
            b.set(10, 9, i, stair(slateStairs, Direction.WEST, Half.TOP));
        }
        for (int cx : new int[]{2, 10}) {
            for (int cz : new int[]{2, 10}) {
                b.set(cx, 9, cz, tiles);
                b.set(cx, 8, cz, hangingLantern);
            }
        }
        // Starlight slate wall balustrade around the deck perimeter (x,z = 2..10, y = 10).
        for (int i = 2; i <= 10; i++) {
            b.set(i, 10, 2, slateWall);
            b.set(i, 10, 10, slateWall);
            b.set(2, 10, i, slateWall);
            b.set(10, 10, i, slateWall);
        }
        for (int cx : new int[]{2, 10}) {
            for (int cz : new int[]{2, 10}) {
                b.set(cx, 11, cz, soulLantern);
            }
        }

        // Open deck corner pillars (y = 10..12) leaving panoramic views on all four sides.
        for (int cx : new int[]{3, 9}) {
            for (int cz : new int[]{3, 9}) {
                b.set(cx, 10, cz, chiseled);
                b.set(cx, 11, cz, bricks);
                b.set(cx, 12, cz, amethystBlock);
            }
        }

        // Ladder mounted on the west wall (x=3 solid; ladder at x=4 faces EAST),
        // climbing cleanly through Floor 2 (y=5) and onto the open Observation Deck (y=9).
        for (int y = 2; y <= 9; y++) {
            b.set(4, y, 6, Blocks.LADDER.defaultBlockState()
                    .setValue(LadderBlock.FACING, Direction.EAST));
        }
        b.set(4, 10, 6, Blocks.AIR.defaultBlockState());
        b.set(4, 11, 6, Blocks.AIR.defaultBlockState());

        // Reachable reward chest sitting openly on the observation deck.
        b.setChest(7, 10, 5, Direction.SOUTH, ArtilianStructures.STARLIGHT_WATCHTOWER_CHEST);

        // Tiered Pagoda Roof Pavilion & Starlight Beacon Spire (y = 13..19).
        b.fillBox(3, 13, 3, 9, 13, 9, bricks);
        b.fillBox(5, 13, 5, 7, 13, 7, polished);
        for (int i = 3; i <= 9; i++) {
            b.set(i, 13, 2, slab(slateSlab, SlabType.BOTTOM));
            b.set(i, 13, 10, slab(slateSlab, SlabType.BOTTOM));
            b.set(2, 13, i, slab(slateSlab, SlabType.BOTTOM));
            b.set(10, 13, i, slab(slateSlab, SlabType.BOTTOM));
        }
        for (int cx : new int[]{2, 10}) {
            for (int cz : new int[]{2, 10}) {
                b.set(cx, 13, cz, tiles);
                b.set(cx, 12, cz, hangingLantern);
            }
        }
        // y = 14: 9x9 pitched Starlight Slate roof tier
        b.fillBox(4, 14, 4, 8, 14, 8, slate);
        for (int i = 4; i <= 8; i++) {
            b.set(i, 14, 9, stair(slateStairs, Direction.NORTH, Half.BOTTOM));
            b.set(i, 14, 3, stair(slateStairs, Direction.SOUTH, Half.BOTTOM));
            b.set(3, 14, i, stair(slateStairs, Direction.EAST, Half.BOTTOM));
            b.set(9, 14, i, stair(slateStairs, Direction.WEST, Half.BOTTOM));
        }
        for (int cx : new int[]{3, 9}) {
            for (int cz : new int[]{3, 9}) {
                b.set(cx, 14, cz, slab(slateSlab, SlabType.BOTTOM));
            }
        }
        // y = 15: 7x7 pitched Deepslate Tile roof tier
        b.fillBox(5, 15, 5, 7, 15, 7, bricks);
        for (int i = 5; i <= 7; i++) {
            b.set(i, 15, 8, stair(tileStairs, Direction.NORTH, Half.BOTTOM));
            b.set(i, 15, 4, stair(tileStairs, Direction.SOUTH, Half.BOTTOM));
            b.set(4, 15, i, stair(tileStairs, Direction.EAST, Half.BOTTOM));
            b.set(8, 15, i, stair(tileStairs, Direction.WEST, Half.BOTTOM));
        }
        for (int cx : new int[]{4, 8}) {
            for (int cz : new int[]{4, 8}) {
                b.set(cx, 15, cz, slab(tileSlab, SlabType.BOTTOM));
            }
        }
        // y = 16: 5x5 pitched Starlight Slate crown + chiseled pedestal
        for (int i = 5; i <= 7; i++) {
            b.set(i, 16, 7, stair(slateStairs, Direction.NORTH, Half.BOTTOM));
            b.set(i, 16, 5, stair(slateStairs, Direction.SOUTH, Half.BOTTOM));
            b.set(5, 16, i, stair(slateStairs, Direction.EAST, Half.BOTTOM));
            b.set(7, 16, i, stair(slateStairs, Direction.WEST, Half.BOTTOM));
        }
        for (int cx : new int[]{5, 7}) {
            for (int cz : new int[]{5, 7}) {
                b.set(cx, 16, cz, slab(slateSlab, SlabType.BOTTOM));
            }
        }
        b.set(6, 16, 6, chiseled);

        // y = 17..19: Glowing Starlight Beacon Spire with outward-pointing amethyst clusters.
        b.set(6, 17, 6, crystalBlock);
        b.set(5, 17, 6, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.WEST));
        b.set(7, 17, 6, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.EAST));
        b.set(6, 17, 5, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.NORTH));
        b.set(6, 17, 7, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.SOUTH));
        b.set(6, 18, 6, slateWall);
        b.set(6, 19, 6, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));

        // Two persistent Astral Stalkers guarding the front terrace.
        b.addGuardian(ArtilianEntities.ASTRAL_STALKER.get(), 4.5, 1.0, 11.5);
        b.addGuardian(ArtilianEntities.ASTRAL_STALKER.get(), 8.5, 1.0, 11.5);

        b.connectFencesAndWalls();
        return b.build();
    }

    private static CompoundTag buildCrystalColossusArena() {
        TemplateBuilder b = new TemplateBuilder(15, 12, 15);
        BlockState slate = ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState();
        Block slateStairs = ArtilianBlocks.STARLIGHT_SLATE_STAIRS.get();
        BlockState slateWall = ArtilianBlocks.STARLIGHT_SLATE_WALL.get().defaultBlockState();
        BlockState crystalBlock = ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState turf = ArtilianBlocks.CRYSTAL_TURF.get().defaultBlockState();
        BlockState bloom = ArtilianBlocks.CRYSTAL_BLOOM.get().defaultBlockState();
        BlockState chiseledStone = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        BlockState soulLantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
        BlockState amethystUp = Blocks.AMETHYST_CLUSTER.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, Direction.UP);

        // Open-air arena volume
        b.fillBox(0, 1, 0, 14, 11, 14, Blocks.AIR.defaultBlockState());

        // y = 0: 15x15 ground foundation with Crystal Turf outer corners
        for (int x = 0; x <= 14; x++) {
            for (int z = 0; z <= 14; z++) {
                boolean outerEdge = (x == 0 || x == 14 || z == 0 || z == 14);
                b.set(x, 0, z, outerEdge ? slate : weatheredStone(x, 0, z));
            }
        }
        for (int cx : new int[]{0, 14}) {
            for (int cz : new int[]{0, 14}) {
                b.set(cx, 0, cz, turf);
                b.set(cx, 1, cz, bloom);
            }
        }

        // y = 1: Raised 13x13 battle dais with wide 5-block cardinal stairs
        for (int x = 1; x <= 13; x++) {
            for (int z = 1; z <= 13; z++) {
                boolean border = (x == 1 || x == 13 || z == 1 || z == 13);
                b.set(x, 1, z, border ? slate : weatheredStone(x, 1, z));
            }
        }
        for (int i = 5; i <= 9; i++) {
            b.set(i, 1, 1, stair(slateStairs, Direction.SOUTH, Half.BOTTOM));
            b.set(i, 1, 13, stair(slateStairs, Direction.NORTH, Half.BOTTOM));
            b.set(1, 1, i, stair(slateStairs, Direction.EAST, Half.BOTTOM));
            b.set(13, 1, i, stair(slateStairs, Direction.WEST, Half.BOTTOM));
        }
        for (int cx : new int[]{1, 13}) {
            for (int cz : new int[]{1, 13}) {
                b.set(cx, 1, cz, chiseledStone);
            }
        }
        // Central summon mosaic inlay at y = 1
        for (int x = 5; x <= 9; x++) {
            for (int z = 5; z <= 9; z++) {
                if (Math.abs(x - 7) + Math.abs(z - 7) <= 3) {
                    b.set(x, 1, z, slate);
                }
            }
        }
        b.set(7, 1, 7, crystalBlock);
        b.set(7, 1, 5, crystalBlock);
        b.set(7, 1, 9, crystalBlock);
        b.set(5, 1, 7, crystalBlock);
        b.set(9, 1, 7, crystalBlock);

        // y = 2: Central Boss Altar under open sky + South prep chest
        b.set(7, 2, 7, ArtilianBlocks.BOSS_ALTAR.get().defaultBlockState());
        b.setChest(5, 2, 12, Direction.NORTH, ArtilianStructures.CRYSTAL_COLOSSUS_ARENA_CHEST);

        // Four corner crystal obelisks + low parapet arcs (wide cardinal openings stay clear)
        int[][] corners = {{2, 2, 1, 1}, {12, 2, -1, 1}, {2, 12, 1, -1}, {12, 12, -1, -1}};
        for (int[] c : corners) {
            int ox = c[0];
            int oz = c[1];
            int dx = c[2];
            int dz = c[3];
            b.set(ox, 2, oz, chiseledStone);
            b.set(ox, 3, oz, slate);
            b.set(ox, 4, oz, slate);
            b.set(ox, 5, oz, crystalBlock);
            b.set(ox, 6, oz, slateWall);
            b.set(ox, 7, oz, slateWall);
            b.set(ox, 8, oz, chiseledStone);
            b.set(ox, 9, oz, amethystUp);

            // Low parapet wall wings flanking each obelisk, capped with Soul Lanterns near stairs
            b.set(ox + dx, 2, oz, slateWall);
            b.set(ox + 2 * dx, 2, oz, slateWall);
            b.set(ox + 2 * dx, 3, oz, soulLantern);
            b.set(ox, 2, oz + dz, slateWall);
            b.set(ox, 2, oz + 2 * dz, slateWall);
            b.set(ox, 3, oz + 2 * dz, soulLantern);
        }

        b.connectFencesAndWalls();
        return b.build();
    }

    private void writeTemplate(CachedOutput cachedOutput, String name, CompoundTag tag) {
        Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(ArtilianMod.MODID)
                .resolve("structure")
                .resolve(name + ".nbt");
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, buffer);
            byte[] bytes = buffer.toByteArray();

            @SuppressWarnings("deprecation") // Guava's sha1 helper is what CachedOutput expects here.
            HashCode hash = Hashing.sha1().hashBytes(bytes);
            cachedOutput.writeIfNeeded(path, bytes, hash);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getName() {
        return "Artilian Biome Structure Templates";
    }

    private static BlockState weatheredStone(int x, int y, int z) {
        int hash = Math.floorMod(x * 73856093 ^ y * 19349663 ^ z * 83492791, 10);
        if (hash < 3) {
            return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        }
        if (hash == 3) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        }
        return Blocks.STONE_BRICKS.defaultBlockState();
    }

    private static BlockState stair(Block block, Direction facing, Half half) {
        return block.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
    }

    private static BlockState slab(Block block, SlabType type) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    private static BlockState trapdoor(Block block, Direction facing, Half half, boolean open) {
        return block.defaultBlockState()
                .setValue(TrapDoorBlock.FACING, facing)
                .setValue(TrapDoorBlock.HALF, half)
                .setValue(TrapDoorBlock.OPEN, open);
    }

    private static BlockState vine(boolean north, boolean east, boolean south, boolean west) {
        return Blocks.VINE.defaultBlockState()
                .setValue(VineBlock.NORTH, north)
                .setValue(VineBlock.EAST, east)
                .setValue(VineBlock.SOUTH, south)
                .setValue(VineBlock.WEST, west);
    }

    private static final class TemplateBuilder {
        private final int sx;
        private final int sy;
        private final int sz;
        private final Map<BlockState, Integer> paletteIndex = new LinkedHashMap<>();
        private final ListTag palette = new ListTag();
        private final Map<Long, BlockEntry> blocks = new LinkedHashMap<>();
        private final Map<Long, BlockState> rawStates = new LinkedHashMap<>();
        private final ListTag entities = new ListTag();

        private record BlockEntry(int x, int y, int z, int state, CompoundTag nbt) {}

        TemplateBuilder(int sx, int sy, int sz) {
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
        }

        void set(int x, int y, int z, BlockState state) {
            setWithNbt(x, y, z, state, null);
        }

        void setChest(int x, int y, int z, Direction facing, ResourceKey<LootTable> lootTable) {
            CompoundTag chestNbt = new CompoundTag();
            chestNbt.putString("id", "minecraft:chest");
            chestNbt.putString("LootTable", lootTable.location().toString());
            setWithNbt(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), chestNbt);
        }

        void fillBox(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    for (int z = z1; z <= z2; z++) {
                        set(x, y, z, state);
                    }
                }
            }
        }

        void addGuardian(EntityType<?> entityType, double x, double y, double z) {
            CompoundTag entityNbt = new CompoundTag();
            entityNbt.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
            entityNbt.putBoolean("PersistenceRequired", true);

            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(x));
            pos.add(DoubleTag.valueOf(y));
            pos.add(DoubleTag.valueOf(z));

            ListTag blockPos = new ListTag();
            blockPos.add(IntTag.valueOf((int) Math.floor(x)));
            blockPos.add(IntTag.valueOf((int) Math.floor(y)));
            blockPos.add(IntTag.valueOf((int) Math.floor(z)));

            CompoundTag entry = new CompoundTag();
            entry.put("pos", pos);
            entry.put("blockPos", blockPos);
            entry.put("nbt", entityNbt);
            this.entities.add(entry);
        }

        void connectFencesAndWalls() {
            for (var entry : new java.util.ArrayList<>(this.rawStates.entrySet())) {
                BlockState state = entry.getValue();
                long key = entry.getKey();
                int x = (int) ((key >> 20) & 0x3FF);
                int y = (int) ((key >> 10) & 0x3FF);
                int z = (int) (key & 0x3FF);
                if (state.getBlock() instanceof FenceBlock) {
                    BlockState updated = state
                            .setValue(FenceBlock.NORTH, connectsFence(x, y, z - 1))
                            .setValue(FenceBlock.EAST, connectsFence(x + 1, y, z))
                            .setValue(FenceBlock.SOUTH, connectsFence(x, y, z + 1))
                            .setValue(FenceBlock.WEST, connectsFence(x - 1, y, z));
                    if (!updated.equals(state)) {
                        setWithNbt(x, y, z, updated, this.blocks.get(key).nbt());
                    }
                } else if (state.getBlock() instanceof WallBlock) {
                    BlockState updated = state
                            .setValue(WallBlock.NORTH_WALL, connectsWall(x, y, z - 1) ? WallSide.LOW : WallSide.NONE)
                            .setValue(WallBlock.EAST_WALL, connectsWall(x + 1, y, z) ? WallSide.LOW : WallSide.NONE)
                            .setValue(WallBlock.SOUTH_WALL, connectsWall(x, y, z + 1) ? WallSide.LOW : WallSide.NONE)
                            .setValue(WallBlock.WEST_WALL, connectsWall(x - 1, y, z) ? WallSide.LOW : WallSide.NONE)
                            .setValue(WallBlock.UP, !isAir(x, y + 1, z)
                                    || !(connectsWall(x, y, z - 1) && connectsWall(x, y, z + 1)
                                    && !connectsWall(x - 1, y, z) && !connectsWall(x + 1, y, z)));
                    if (!updated.equals(state)) {
                        setWithNbt(x, y, z, updated, this.blocks.get(key).nbt());
                    }
                }
            }
        }

        private boolean connectsFence(int x, int y, int z) {
            BlockState neighbor = this.rawStates.get(posKey(x, y, z));
            if (neighbor == null || neighbor.isAir()) {
                return false;
            }
            return neighbor.getBlock() instanceof FenceBlock || isFullStructuralCube(neighbor);
        }

        private boolean connectsWall(int x, int y, int z) {
            BlockState neighbor = this.rawStates.get(posKey(x, y, z));
            if (neighbor == null || neighbor.isAir()) {
                return false;
            }
            return neighbor.getBlock() instanceof WallBlock || isFullStructuralCube(neighbor);
        }

        private boolean isAir(int x, int y, int z) {
            BlockState state = this.rawStates.get(posKey(x, y, z));
            return state == null || state.isAir();
        }

        private static boolean isFullStructuralCube(BlockState state) {
            Block b = state.getBlock();
            return !(b instanceof StairBlock || b instanceof SlabBlock || b instanceof TrapDoorBlock
                    || b instanceof VineBlock || b instanceof LanternBlock || b instanceof AmethystClusterBlock
                    || b instanceof LadderBlock || b instanceof LeavesBlock || b instanceof ChestBlock
                    || b instanceof DoorBlock || b instanceof FenceGateBlock || b instanceof FlowerBlock
                    || b == Blocks.MOSS_CARPET);
        }

        private static long posKey(int x, int y, int z) {
            return (((long) x & 0x3FF) << 20) | (((long) y & 0x3FF) << 10) | ((long) z & 0x3FF);
        }

        private void setWithNbt(int x, int y, int z, BlockState state, CompoundTag nbt) {
            int stateId = this.paletteIndex.computeIfAbsent(state, s -> {
                int idx = this.palette.size();
                this.palette.add(NbtUtils.writeBlockState(s));
                return idx;
            });
            long key = posKey(x, y, z);
            this.rawStates.put(key, state);
            this.blocks.put(key, new BlockEntry(x, y, z, stateId, nbt));
        }

        CompoundTag build() {
            CompoundTag root = new CompoundTag();
            root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());

            ListTag size = new ListTag();
            size.add(IntTag.valueOf(this.sx));
            size.add(IntTag.valueOf(this.sy));
            size.add(IntTag.valueOf(this.sz));
            root.put("size", size);
            root.put("palette", this.palette);

            ListTag blockList = new ListTag();
            for (BlockEntry entry : this.blocks.values()) {
                ListTag pos = new ListTag();
                pos.add(IntTag.valueOf(entry.x()));
                pos.add(IntTag.valueOf(entry.y()));
                pos.add(IntTag.valueOf(entry.z()));

                CompoundTag blockTag = new CompoundTag();
                blockTag.put("pos", pos);
                blockTag.putInt("state", entry.state());
                if (entry.nbt() != null) {
                    blockTag.put("nbt", entry.nbt());
                }
                blockList.add(blockTag);
            }
            root.put("blocks", blockList);
            root.put("entities", this.entities);
            return root;
        }
    }
}
