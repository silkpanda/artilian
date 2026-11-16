package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ArtilianBlockStateProvider extends BlockStateProvider {
    public ArtilianBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ArtilianMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleCube(ArtilianBlocks.SUNSTEEL_ORE);
        simpleCube(ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE);
        simpleCube(ArtilianBlocks.SUNSTEEL_BLOCK);
        simpleCube(ArtilianBlocks.STARLIGHT_CRYSTAL_ORE);
        simpleCube(ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE);
        simpleCube(ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK);

        tntBlock(ArtilianBlocks.PARTY_TNT);
        tntBlock(ArtilianBlocks.BOUNCE_TNT);
        tntBlock(ArtilianBlocks.FROST_NOVA_TNT);
        tntBlock(ArtilianBlocks.CLUSTER_QUARRY_TNT);

        turfBlock(ArtilianBlocks.CRYSTAL_TURF, "crystal_turf", mcLoc("block/dirt"));
        woodSet(
                ArtilianBlocks.CRYSTAL_CANOPY_LOG,
                ArtilianBlocks.CRYSTAL_CANOPY_PLANKS,
                ArtilianBlocks.CRYSTAL_CANOPY_LEAVES,
                ArtilianBlocks.CRYSTAL_CANOPY_SAPLING);
        flowerCross(ArtilianBlocks.CRYSTAL_BLOOM);

        turfBlock(ArtilianBlocks.EMBER_TURF, "ember_turf", mcLoc("block/dirt"));
        woodSet(
                ArtilianBlocks.EMBERWOOD_LOG,
                ArtilianBlocks.EMBERWOOD_PLANKS,
                ArtilianBlocks.EMBERWOOD_LEAVES,
                ArtilianBlocks.EMBERWOOD_SAPLING);
        flowerCross(ArtilianBlocks.EMBER_BLOOM);

        turfBlock(ArtilianBlocks.STARLIGHT_TURF, "starlight_turf", modLoc("block/starlight_slate"));
        simpleCube(ArtilianBlocks.STARLIGHT_SLATE);
        flowerCross(ArtilianBlocks.STARLIGHT_BLOSSOM);
    }

    private void turfBlock(DeferredBlock<Block> block, String name, ResourceLocation bottomTexture) {
        simpleBlockWithItem(block.get(), models().cubeBottomTop(
                name,
                modLoc("block/" + name + "_side"),
                bottomTexture,
                modLoc("block/" + name + "_top")));
    }

    private void woodSet(
            DeferredBlock<RotatedPillarBlock> log,
            DeferredBlock<Block> planks,
            DeferredBlock<LeavesBlock> leaves,
            DeferredBlock<SaplingBlock> sapling) {
        String logName = BuiltInRegistries.BLOCK.getKey(log.get()).getPath();
        logBlock(log.get());
        simpleBlockItem(log.get(), models().getExistingFile(modLoc("block/" + logName)));
        simpleCube(planks);
        String leavesName = BuiltInRegistries.BLOCK.getKey(leaves.get()).getPath();
        simpleBlockWithItem(leaves.get(),
                models().singleTexture(leavesName, mcLoc("block/leaves"), "all", modLoc("block/" + leavesName))
                        .renderType("cutout_mipped"));
        flowerCross(sapling);
    }

    private void flowerCross(DeferredBlock<? extends Block> block) {
        String name = BuiltInRegistries.BLOCK.getKey(block.get()).getPath();
        simpleBlock(block.get(),
                models().cross(name, modLoc("block/" + name)).renderType("cutout"));
    }

    private void simpleCube(DeferredBlock<Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }

    private void tntBlock(DeferredBlock<? extends Block> block) {
        String name = BuiltInRegistries.BLOCK.getKey(block.get()).getPath();
        simpleBlockWithItem(block.get(), models().cubeBottomTop(
                name,
                modLoc("block/" + name + "_side"),
                modLoc("block/" + name + "_bottom"),
                modLoc("block/" + name + "_top")));
    }
}
