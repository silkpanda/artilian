package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ArtilianItemTagsProvider extends ItemTagsProvider {
    public ArtilianItemTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, ArtilianMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
        copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

        tag(Tags.Items.RAW_MATERIALS).add(ArtilianItems.RAW_SUNSTEEL.get());
        tag(Tags.Items.INGOTS).add(ArtilianItems.SUNSTEEL_INGOT.get());
        tag(Tags.Items.GEMS).add(ArtilianItems.STARLIGHT_CRYSTAL.get());

        tag(ItemTags.SWORDS).add(
                ArtilianItems.SUNSTEEL_SWORD.get(), ArtilianItems.STARLIGHT_SWORD.get(),
                ArtilianItems.PRISMATIC_SWORD.get(), ArtilianItems.DRAGONSCALE_SWORD.get());
        tag(ItemTags.PICKAXES).add(
                ArtilianItems.SUNSTEEL_PICKAXE.get(), ArtilianItems.STARLIGHT_PICKAXE.get(),
                ArtilianItems.PRISMATIC_PICKAXE.get(), ArtilianItems.DRAGONSCALE_PICKAXE.get());
        tag(ItemTags.AXES).add(
                ArtilianItems.SUNSTEEL_AXE.get(), ArtilianItems.STARLIGHT_AXE.get(),
                ArtilianItems.PRISMATIC_AXE.get(), ArtilianItems.DRAGONSCALE_AXE.get());
        tag(ItemTags.SHOVELS).add(
                ArtilianItems.SUNSTEEL_SHOVEL.get(), ArtilianItems.STARLIGHT_SHOVEL.get(),
                ArtilianItems.PRISMATIC_SHOVEL.get(), ArtilianItems.DRAGONSCALE_SHOVEL.get());
        tag(ItemTags.HOES).add(
                ArtilianItems.SUNSTEEL_HOE.get(), ArtilianItems.STARLIGHT_HOE.get(),
                ArtilianItems.PRISMATIC_HOE.get(), ArtilianItems.DRAGONSCALE_HOE.get());

        tag(ItemTags.HEAD_ARMOR).add(
                ArtilianItems.SUNSTEEL_HELMET.get(), ArtilianItems.STARLIGHT_HELMET.get(),
                ArtilianItems.PRISMATIC_HELMET.get(), ArtilianItems.DRAGONSCALE_HELMET.get());
        tag(ItemTags.CHEST_ARMOR).add(
                ArtilianItems.SUNSTEEL_CHESTPLATE.get(), ArtilianItems.STARLIGHT_CHESTPLATE.get(),
                ArtilianItems.PRISMATIC_CHESTPLATE.get(), ArtilianItems.DRAGONSCALE_CHESTPLATE.get());
        tag(ItemTags.LEG_ARMOR).add(
                ArtilianItems.SUNSTEEL_LEGGINGS.get(), ArtilianItems.STARLIGHT_LEGGINGS.get(),
                ArtilianItems.PRISMATIC_LEGGINGS.get(), ArtilianItems.DRAGONSCALE_LEGGINGS.get());
        tag(ItemTags.FOOT_ARMOR).add(
                ArtilianItems.SUNSTEEL_BOOTS.get(), ArtilianItems.STARLIGHT_BOOTS.get(),
                ArtilianItems.PRISMATIC_BOOTS.get(), ArtilianItems.DRAGONSCALE_BOOTS.get());

        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.SLABS, ItemTags.SLABS);
        copy(BlockTags.WALLS, ItemTags.WALLS);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
        copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);

        tag(ArtilianTags.Items.BIOME_BLOOMS).add(
                ArtilianItems.CRYSTAL_BLOOM_ITEM.get(),
                ArtilianItems.EMBER_BLOOM_ITEM.get(),
                ArtilianItems.STARLIGHT_BLOSSOM_ITEM.get()
        );
        tag(ArtilianTags.Items.BOSS_DROPS).add(
                ArtilianItems.PRISM_CORE.get(),
                ArtilianItems.BRIMSTONE_DOUBLOON.get(),
                ArtilianItems.ALL_SEERS_LENS.get()
        );
    }
}
