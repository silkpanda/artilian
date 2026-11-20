package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

public class ArtilianBlockLootSubProvider extends BlockLootSubProvider {
    protected ArtilianBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        add(ArtilianBlocks.SUNSTEEL_ORE.get(),
                block -> createOreDrop(block, ArtilianItems.RAW_SUNSTEEL.get()));
        add(ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE.get(),
                block -> createOreDrop(block, ArtilianItems.RAW_SUNSTEEL.get()));
        dropSelf(ArtilianBlocks.SUNSTEEL_BLOCK.get());

        add(ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get(),
                block -> createOreDrop(block, ArtilianItems.STARLIGHT_CRYSTAL.get()));
        add(ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE.get(),
                block -> createOreDrop(block, ArtilianItems.STARLIGHT_CRYSTAL.get()));
        dropSelf(ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get());

        dropSelf(ArtilianBlocks.PARTY_TNT.get());
        dropSelf(ArtilianBlocks.BOUNCE_TNT.get());
        dropSelf(ArtilianBlocks.FROST_NOVA_TNT.get());
        dropSelf(ArtilianBlocks.CLUSTER_QUARRY_TNT.get());
        dropSelf(ArtilianBlocks.STARLIGHT_GRAVITY_TNT.get());
        dropSelf(ArtilianBlocks.BRIMSTONE_METEOR_TNT.get());

        add(ArtilianBlocks.CRYSTAL_TURF.get(),
                block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_LOG.get());
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_PLANKS.get());
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_STAIRS.get());
        add(ArtilianBlocks.CRYSTAL_CANOPY_SLAB.get(), block -> createSlabItemTable(block));
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_FENCE.get());
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_FENCE_GATE.get());
        add(ArtilianBlocks.CRYSTAL_CANOPY_DOOR.get(), block -> createDoorTable(block));
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_TRAPDOOR.get());
        add(ArtilianBlocks.CRYSTAL_CANOPY_LEAVES.get(),
                block -> createLeavesDrops(block, ArtilianBlocks.CRYSTAL_CANOPY_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_SAPLING.get());
        dropSelf(ArtilianBlocks.CRYSTAL_BLOOM.get());

        add(ArtilianBlocks.EMBER_TURF.get(),
                block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        dropSelf(ArtilianBlocks.EMBERWOOD_LOG.get());
        dropSelf(ArtilianBlocks.EMBERWOOD_PLANKS.get());
        dropSelf(ArtilianBlocks.EMBERWOOD_STAIRS.get());
        add(ArtilianBlocks.EMBERWOOD_SLAB.get(), block -> createSlabItemTable(block));
        dropSelf(ArtilianBlocks.EMBERWOOD_FENCE.get());
        dropSelf(ArtilianBlocks.EMBERWOOD_FENCE_GATE.get());
        add(ArtilianBlocks.EMBERWOOD_DOOR.get(), block -> createDoorTable(block));
        dropSelf(ArtilianBlocks.EMBERWOOD_TRAPDOOR.get());
        add(ArtilianBlocks.EMBERWOOD_LEAVES.get(),
                block -> createLeavesDrops(block, ArtilianBlocks.EMBERWOOD_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(ArtilianBlocks.EMBERWOOD_SAPLING.get());
        dropSelf(ArtilianBlocks.EMBER_BLOOM.get());

        add(ArtilianBlocks.STARLIGHT_TURF.get(),
                block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        dropSelf(ArtilianBlocks.STARLIGHT_SLATE.get());
        dropSelf(ArtilianBlocks.BOSS_ALTAR.get());
        dropSelf(ArtilianBlocks.STARLIGHT_SLATE_STAIRS.get());
        add(ArtilianBlocks.STARLIGHT_SLATE_SLAB.get(), block -> createSlabItemTable(block));
        dropSelf(ArtilianBlocks.STARLIGHT_SLATE_WALL.get());
        dropSelf(ArtilianBlocks.STARLIGHT_BLOSSOM.get());

        // Phase 4 Stage 2: Eye Realm Terrain & Trees
        add(ArtilianBlocks.EYE_TURF.get(),
                block -> createSingleItemTableWithSilkTouch(block, ArtilianBlocks.GAZE_STONE.get()));
        dropSelf(ArtilianBlocks.GAZE_STONE.get());
        dropSelf(ArtilianBlocks.GAZE_STONE_STAIRS.get());
        add(ArtilianBlocks.GAZE_STONE_SLAB.get(), block -> createSlabItemTable(block));
        dropSelf(ArtilianBlocks.GAZE_STONE_WALL.get());
        dropSelf(ArtilianBlocks.BLINKING_EYE_BLOCK.get());
        dropSelf(ArtilianBlocks.OCULAR_LOG.get());
        dropSelf(ArtilianBlocks.OCULAR_PLANKS.get());
        dropSelf(ArtilianBlocks.OCULAR_STAIRS.get());
        add(ArtilianBlocks.OCULAR_SLAB.get(), block -> createSlabItemTable(block));
        add(ArtilianBlocks.OCULAR_LEAVES.get(),
                block -> createLeavesDrops(block, ArtilianBlocks.OCULAR_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(ArtilianBlocks.OCULAR_SAPLING.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ArtilianBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
