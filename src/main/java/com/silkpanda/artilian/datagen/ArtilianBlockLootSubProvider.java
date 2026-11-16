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

        add(ArtilianBlocks.CRYSTAL_TURF.get(),
                block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_LOG.get());
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_PLANKS.get());
        add(ArtilianBlocks.CRYSTAL_CANOPY_LEAVES.get(),
                block -> createLeavesDrops(block, ArtilianBlocks.CRYSTAL_CANOPY_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(ArtilianBlocks.CRYSTAL_CANOPY_SAPLING.get());
        dropSelf(ArtilianBlocks.CRYSTAL_BLOOM.get());

        add(ArtilianBlocks.EMBER_TURF.get(),
                block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        dropSelf(ArtilianBlocks.EMBERWOOD_LOG.get());
        dropSelf(ArtilianBlocks.EMBERWOOD_PLANKS.get());
        add(ArtilianBlocks.EMBERWOOD_LEAVES.get(),
                block -> createLeavesDrops(block, ArtilianBlocks.EMBERWOOD_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(ArtilianBlocks.EMBERWOOD_SAPLING.get());
        dropSelf(ArtilianBlocks.EMBER_BLOOM.get());

        add(ArtilianBlocks.STARLIGHT_TURF.get(),
                block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        dropSelf(ArtilianBlocks.STARLIGHT_SLATE.get());
        dropSelf(ArtilianBlocks.STARLIGHT_BLOSSOM.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ArtilianBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
