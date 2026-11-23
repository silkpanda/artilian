package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.GazingPortalBlock;
import com.silkpanda.artilian.dimension.PrimalPortalBlock;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
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
        tntBlock(ArtilianBlocks.STARLIGHT_GRAVITY_TNT);
        tntBlock(ArtilianBlocks.BRIMSTONE_METEOR_TNT);

        turfBlock(ArtilianBlocks.CRYSTAL_TURF, "crystal_turf", mcLoc("block/dirt"));
        woodSet(
                ArtilianBlocks.CRYSTAL_CANOPY_LOG,
                ArtilianBlocks.CRYSTAL_CANOPY_PLANKS,
                ArtilianBlocks.CRYSTAL_CANOPY_LEAVES,
                ArtilianBlocks.CRYSTAL_CANOPY_SAPLING);
        woodVariants(
                ArtilianBlocks.CRYSTAL_CANOPY_PLANKS,
                ArtilianBlocks.CRYSTAL_CANOPY_STAIRS,
                ArtilianBlocks.CRYSTAL_CANOPY_SLAB,
                ArtilianBlocks.CRYSTAL_CANOPY_FENCE,
                ArtilianBlocks.CRYSTAL_CANOPY_FENCE_GATE,
                ArtilianBlocks.CRYSTAL_CANOPY_DOOR,
                ArtilianBlocks.CRYSTAL_CANOPY_TRAPDOOR);
        flowerCross(ArtilianBlocks.CRYSTAL_BLOOM);

        turfBlock(ArtilianBlocks.EMBER_TURF, "ember_turf", mcLoc("block/dirt"));
        woodSet(
                ArtilianBlocks.EMBERWOOD_LOG,
                ArtilianBlocks.EMBERWOOD_PLANKS,
                ArtilianBlocks.EMBERWOOD_LEAVES,
                ArtilianBlocks.EMBERWOOD_SAPLING);
        woodVariants(
                ArtilianBlocks.EMBERWOOD_PLANKS,
                ArtilianBlocks.EMBERWOOD_STAIRS,
                ArtilianBlocks.EMBERWOOD_SLAB,
                ArtilianBlocks.EMBERWOOD_FENCE,
                ArtilianBlocks.EMBERWOOD_FENCE_GATE,
                ArtilianBlocks.EMBERWOOD_DOOR,
                ArtilianBlocks.EMBERWOOD_TRAPDOOR);
        flowerCross(ArtilianBlocks.EMBER_BLOOM);

        turfBlock(ArtilianBlocks.STARLIGHT_TURF, "starlight_turf", modLoc("block/starlight_slate"));
        simpleCube(ArtilianBlocks.STARLIGHT_SLATE);
        simpleBlockWithItem(ArtilianBlocks.BOSS_ALTAR.get(), cubeAll(ArtilianBlocks.BOSS_ALTAR.get()));
        slateVariants(
                ArtilianBlocks.STARLIGHT_SLATE_STAIRS,
                ArtilianBlocks.STARLIGHT_SLATE_SLAB,
                ArtilianBlocks.STARLIGHT_SLATE_WALL);
        flowerCross(ArtilianBlocks.STARLIGHT_BLOSSOM);
        gazingPortalBlock();

        // Phase 4 Stage 2: Eye Realm Terrain & Trees
        turfBlock(ArtilianBlocks.EYE_TURF, "eye_turf", modLoc("block/gaze_stone"));
        simpleCube(ArtilianBlocks.GAZE_STONE);
        slateVariants(
                ArtilianBlocks.GAZE_STONE_STAIRS,
                ArtilianBlocks.GAZE_STONE_SLAB,
                ArtilianBlocks.GAZE_STONE_WALL);
        simpleCube(ArtilianBlocks.BLINKING_EYE_BLOCK);
        woodSet(
                ArtilianBlocks.OCULAR_LOG,
                ArtilianBlocks.OCULAR_PLANKS,
                ArtilianBlocks.OCULAR_LEAVES,
                ArtilianBlocks.OCULAR_SAPLING);
        stairsBlock(ArtilianBlocks.OCULAR_STAIRS.get(), blockTexture(ArtilianBlocks.OCULAR_PLANKS.get()));
        simpleBlockItem(ArtilianBlocks.OCULAR_STAIRS.get(),
                models().getExistingFile(modLoc("block/ocular_stairs")));
        slabBlock(ArtilianBlocks.OCULAR_SLAB.get(),
                blockTexture(ArtilianBlocks.OCULAR_PLANKS.get()),
                blockTexture(ArtilianBlocks.OCULAR_PLANKS.get()));
        simpleBlockItem(ArtilianBlocks.OCULAR_SLAB.get(),
                models().getExistingFile(modLoc("block/ocular_slab")));

        // Phase 5 Stage 1: Giant Dragon Jungle
        portalBlock(ArtilianBlocks.PRIMAL_PORTAL.get(), "primal_portal", PrimalPortalBlock.AXIS);
    }

    private void gazingPortalBlock() {
        portalBlock(ArtilianBlocks.GAZING_PORTAL.get(), "gazing_portal", GazingPortalBlock.AXIS);
    }

    private void portalBlock(Block block, String name, EnumProperty<Direction.Axis> axisProp) {
        var nsModel = models().withExistingParent(name + "_ns", mcLoc("block/nether_portal_ns"))
                .texture("particle", modLoc("block/" + name))
                .texture("portal", modLoc("block/" + name))
                .renderType("translucent");
        var ewModel = models().withExistingParent(name + "_ew", mcLoc("block/nether_portal_ew"))
                .texture("particle", modLoc("block/" + name))
                .texture("portal", modLoc("block/" + name))
                .renderType("translucent");
        getVariantBuilder(block)
                .partialState().with(axisProp, Direction.Axis.X)
                .modelForState().modelFile(nsModel).addModel()
                .partialState().with(axisProp, Direction.Axis.Z)
                .modelForState().modelFile(ewModel).addModel();
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

    private void woodVariants(
            DeferredBlock<Block> planks,
            DeferredBlock<StairBlock> stairs,
            DeferredBlock<SlabBlock> slab,
            DeferredBlock<FenceBlock> fence,
            DeferredBlock<FenceGateBlock> fenceGate,
            DeferredBlock<DoorBlock> door,
            DeferredBlock<TrapDoorBlock> trapdoor) {
        String planksName = BuiltInRegistries.BLOCK.getKey(planks.get()).getPath();
        String wood = planksName.replace("_planks", "");
        ResourceLocation planksTexture = modLoc("block/" + planksName);

        stairsBlock(stairs.get(), planksTexture);
        simpleBlockItem(stairs.get(), models().getExistingFile(modLoc("block/" + wood + "_stairs")));
        slabBlock(slab.get(), modLoc("block/" + planksName), planksTexture);
        simpleBlockItem(slab.get(), models().getExistingFile(modLoc("block/" + wood + "_slab")));
        fenceBlock(fence.get(), planksTexture);
        simpleBlockItem(fence.get(), models().fenceInventory(wood + "_fence_inventory", planksTexture));
        fenceGateBlock(fenceGate.get(), planksTexture);
        simpleBlockItem(fenceGate.get(), models().getExistingFile(modLoc("block/" + wood + "_fence_gate")));
        doorBlockWithRenderType(door.get(),
                modLoc("block/" + wood + "_door_bottom"),
                modLoc("block/" + wood + "_door_top"),
                "cutout");
        trapdoorBlockWithRenderType(trapdoor.get(), modLoc("block/" + wood + "_trapdoor"), true, "cutout");
        simpleBlockItem(trapdoor.get(), models().getExistingFile(modLoc("block/" + wood + "_trapdoor_bottom")));
    }

    private void slateVariants(
            DeferredBlock<StairBlock> stairs,
            DeferredBlock<SlabBlock> slab,
            DeferredBlock<WallBlock> wall) {
        ResourceLocation slateTexture = modLoc("block/starlight_slate");

        stairsBlock(stairs.get(), slateTexture);
        simpleBlockItem(stairs.get(), models().getExistingFile(modLoc("block/starlight_slate_stairs")));
        slabBlock(slab.get(), modLoc("block/starlight_slate"), slateTexture);
        simpleBlockItem(slab.get(), models().getExistingFile(modLoc("block/starlight_slate_slab")));
        wallBlock(wall.get(), slateTexture);
        simpleBlockItem(wall.get(), models().wallInventory("starlight_slate_wall_inventory", slateTexture));
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
