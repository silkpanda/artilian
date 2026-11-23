package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ArtilianBlockTagsProvider extends BlockTagsProvider {
    public ArtilianBlockTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ArtilianMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ArtilianBlocks.SUNSTEEL_ORE.get(),
                ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE.get(),
                ArtilianBlocks.SUNSTEEL_BLOCK.get(),
                ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get(),
                ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE.get(),
                ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get(),
                ArtilianBlocks.STARLIGHT_SLATE.get(),
                ArtilianBlocks.STARLIGHT_SLATE_STAIRS.get(),
                ArtilianBlocks.STARLIGHT_SLATE_SLAB.get(),
                ArtilianBlocks.STARLIGHT_SLATE_WALL.get(),
                ArtilianBlocks.BOSS_ALTAR.get()
        );

        tag(BlockTags.NEEDS_STONE_TOOL).add(
                ArtilianBlocks.SUNSTEEL_ORE.get(),
                ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE.get(),
                ArtilianBlocks.SUNSTEEL_BLOCK.get()
        );

        tag(BlockTags.NEEDS_IRON_TOOL).add(
                ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get(),
                ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE.get(),
                ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get()
        );

        tag(Tags.Blocks.ORES).add(
                ArtilianBlocks.SUNSTEEL_ORE.get(),
                ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE.get(),
                ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get(),
                ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE.get()
        );

        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(
                ArtilianBlocks.SUNSTEEL_ORE.get(),
                ArtilianBlocks.STARLIGHT_CRYSTAL_ORE.get()
        );

        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(
                ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE.get(),
                ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE.get()
        );

        tag(Tags.Blocks.STORAGE_BLOCKS).add(
                ArtilianBlocks.SUNSTEEL_BLOCK.get(),
                ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK.get()
        );

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ArtilianBlocks.GAZE_STONE.get(),
                ArtilianBlocks.GAZE_STONE_STAIRS.get(),
                ArtilianBlocks.GAZE_STONE_SLAB.get(),
                ArtilianBlocks.GAZE_STONE_WALL.get(),
                ArtilianBlocks.BLINKING_EYE_BLOCK.get()
        );
        tag(BlockTags.DIRT).add(
                ArtilianBlocks.CRYSTAL_TURF.get(),
                ArtilianBlocks.EMBER_TURF.get(),
                ArtilianBlocks.STARLIGHT_TURF.get(),
                ArtilianBlocks.EYE_TURF.get()
        );
        tag(BlockTags.ANIMALS_SPAWNABLE_ON).add(
                ArtilianBlocks.CRYSTAL_TURF.get(),
                ArtilianBlocks.EMBER_TURF.get(),
                ArtilianBlocks.STARLIGHT_TURF.get(),
                ArtilianBlocks.EYE_TURF.get()
        );
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(
                ArtilianBlocks.CRYSTAL_TURF.get(),
                ArtilianBlocks.EMBER_TURF.get(),
                ArtilianBlocks.STARLIGHT_TURF.get(),
                ArtilianBlocks.EYE_TURF.get()
        );
        tag(BlockTags.MINEABLE_WITH_AXE).add(
                ArtilianBlocks.CRYSTAL_CANOPY_LOG.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_PLANKS.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_STAIRS.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_SLAB.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_FENCE.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_FENCE_GATE.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_DOOR.get(),
                ArtilianBlocks.CRYSTAL_CANOPY_TRAPDOOR.get(),
                ArtilianBlocks.EMBERWOOD_LOG.get(),
                ArtilianBlocks.EMBERWOOD_PLANKS.get(),
                ArtilianBlocks.EMBERWOOD_STAIRS.get(),
                ArtilianBlocks.EMBERWOOD_SLAB.get(),
                ArtilianBlocks.EMBERWOOD_FENCE.get(),
                ArtilianBlocks.EMBERWOOD_FENCE_GATE.get(),
                ArtilianBlocks.EMBERWOOD_DOOR.get(),
                ArtilianBlocks.EMBERWOOD_TRAPDOOR.get(),
                ArtilianBlocks.OCULAR_LOG.get(),
                ArtilianBlocks.OCULAR_PLANKS.get(),
                ArtilianBlocks.OCULAR_STAIRS.get(),
                ArtilianBlocks.OCULAR_SLAB.get()
        );
        tag(BlockTags.WOODEN_STAIRS).add(
                ArtilianBlocks.CRYSTAL_CANOPY_STAIRS.get(),
                ArtilianBlocks.EMBERWOOD_STAIRS.get(),
                ArtilianBlocks.OCULAR_STAIRS.get()
        );
        tag(BlockTags.WOODEN_SLABS).add(
                ArtilianBlocks.CRYSTAL_CANOPY_SLAB.get(),
                ArtilianBlocks.EMBERWOOD_SLAB.get(),
                ArtilianBlocks.OCULAR_SLAB.get()
        );
        tag(BlockTags.WOODEN_FENCES).add(
                ArtilianBlocks.CRYSTAL_CANOPY_FENCE.get(),
                ArtilianBlocks.EMBERWOOD_FENCE.get()
        );
        tag(BlockTags.FENCE_GATES).add(
                ArtilianBlocks.CRYSTAL_CANOPY_FENCE_GATE.get(),
                ArtilianBlocks.EMBERWOOD_FENCE_GATE.get()
        );
        tag(BlockTags.WOODEN_DOORS).add(
                ArtilianBlocks.CRYSTAL_CANOPY_DOOR.get(),
                ArtilianBlocks.EMBERWOOD_DOOR.get()
        );
        tag(BlockTags.WOODEN_TRAPDOORS).add(
                ArtilianBlocks.CRYSTAL_CANOPY_TRAPDOOR.get(),
                ArtilianBlocks.EMBERWOOD_TRAPDOOR.get()
        );
        tag(BlockTags.STAIRS).add(
                ArtilianBlocks.STARLIGHT_SLATE_STAIRS.get(),
                ArtilianBlocks.GAZE_STONE_STAIRS.get()
        );
        tag(BlockTags.SLABS).add(
                ArtilianBlocks.STARLIGHT_SLATE_SLAB.get(),
                ArtilianBlocks.GAZE_STONE_SLAB.get()
        );
        tag(BlockTags.WALLS).add(
                ArtilianBlocks.STARLIGHT_SLATE_WALL.get(),
                ArtilianBlocks.GAZE_STONE_WALL.get()
        );
        tag(BlockTags.MINEABLE_WITH_HOE).add(
                ArtilianBlocks.CRYSTAL_CANOPY_LEAVES.get(),
                ArtilianBlocks.EMBERWOOD_LEAVES.get(),
                ArtilianBlocks.OCULAR_LEAVES.get()
        );
        tag(BlockTags.LOGS_THAT_BURN).add(
                ArtilianBlocks.CRYSTAL_CANOPY_LOG.get(),
                ArtilianBlocks.EMBERWOOD_LOG.get(),
                ArtilianBlocks.OCULAR_LOG.get()
        );
        tag(BlockTags.PLANKS).add(
                ArtilianBlocks.CRYSTAL_CANOPY_PLANKS.get(),
                ArtilianBlocks.EMBERWOOD_PLANKS.get(),
                ArtilianBlocks.OCULAR_PLANKS.get()
        );
        tag(BlockTags.LEAVES).add(
                ArtilianBlocks.CRYSTAL_CANOPY_LEAVES.get(),
                ArtilianBlocks.EMBERWOOD_LEAVES.get(),
                ArtilianBlocks.OCULAR_LEAVES.get()
        );
        tag(BlockTags.SAPLINGS).add(
                ArtilianBlocks.CRYSTAL_CANOPY_SAPLING.get(),
                ArtilianBlocks.EMBERWOOD_SAPLING.get(),
                ArtilianBlocks.OCULAR_SAPLING.get()
        );
        tag(BlockTags.SMALL_FLOWERS).add(
                ArtilianBlocks.CRYSTAL_BLOOM.get(),
                ArtilianBlocks.EMBER_BLOOM.get(),
                ArtilianBlocks.STARLIGHT_BLOSSOM.get()
        );
        tag(ArtilianTags.Blocks.GAZING_PORTAL_FRAME).add(
                ArtilianBlocks.STARLIGHT_SLATE.get()
        );
        tag(ArtilianTags.Blocks.PRIMAL_PORTAL_FRAME).add(
                net.minecraft.world.level.block.Blocks.OBSIDIAN,
                net.minecraft.world.level.block.Blocks.CRYING_OBSIDIAN
        );
    }
}
