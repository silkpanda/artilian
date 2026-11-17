package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Structure Rebuild Stage 1: the 15 new building blocks and their wiring. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class BuildingBlockVariantsGameTests {

    private record Variant(String name, Block block, Item item, Item tool,
                           List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {}

    private static Variant wood(String name, Block block, Item item,
                                TagKey<Block> shapeBlockTag, TagKey<Item> shapeItemTag) {
        return new Variant(name, block, item, Items.IRON_AXE,
                List.of(shapeBlockTag, BlockTags.MINEABLE_WITH_AXE), List.of(shapeItemTag));
    }

    private static Variant slate(String name, Block block, Item item,
                                 TagKey<Block> shapeBlockTag, TagKey<Item> shapeItemTag) {
        return new Variant(name, block, item, Items.IRON_PICKAXE,
                List.of(shapeBlockTag, BlockTags.MINEABLE_WITH_PICKAXE), List.of(shapeItemTag));
    }

    private static List<Variant> variants() {
        return List.of(
                wood("crystal_canopy_stairs", ArtilianBlocks.CRYSTAL_CANOPY_STAIRS.get(),
                        ArtilianItems.CRYSTAL_CANOPY_STAIRS_ITEM.get(), BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS),
                wood("crystal_canopy_slab", ArtilianBlocks.CRYSTAL_CANOPY_SLAB.get(),
                        ArtilianItems.CRYSTAL_CANOPY_SLAB_ITEM.get(), BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS),
                wood("crystal_canopy_fence", ArtilianBlocks.CRYSTAL_CANOPY_FENCE.get(),
                        ArtilianItems.CRYSTAL_CANOPY_FENCE_ITEM.get(), BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES),
                wood("crystal_canopy_fence_gate", ArtilianBlocks.CRYSTAL_CANOPY_FENCE_GATE.get(),
                        ArtilianItems.CRYSTAL_CANOPY_FENCE_GATE_ITEM.get(), BlockTags.FENCE_GATES, ItemTags.FENCE_GATES),
                wood("crystal_canopy_door", ArtilianBlocks.CRYSTAL_CANOPY_DOOR.get(),
                        ArtilianItems.CRYSTAL_CANOPY_DOOR_ITEM.get(), BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS),
                wood("crystal_canopy_trapdoor", ArtilianBlocks.CRYSTAL_CANOPY_TRAPDOOR.get(),
                        ArtilianItems.CRYSTAL_CANOPY_TRAPDOOR_ITEM.get(), BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS),

                wood("emberwood_stairs", ArtilianBlocks.EMBERWOOD_STAIRS.get(),
                        ArtilianItems.EMBERWOOD_STAIRS_ITEM.get(), BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS),
                wood("emberwood_slab", ArtilianBlocks.EMBERWOOD_SLAB.get(),
                        ArtilianItems.EMBERWOOD_SLAB_ITEM.get(), BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS),
                wood("emberwood_fence", ArtilianBlocks.EMBERWOOD_FENCE.get(),
                        ArtilianItems.EMBERWOOD_FENCE_ITEM.get(), BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES),
                wood("emberwood_fence_gate", ArtilianBlocks.EMBERWOOD_FENCE_GATE.get(),
                        ArtilianItems.EMBERWOOD_FENCE_GATE_ITEM.get(), BlockTags.FENCE_GATES, ItemTags.FENCE_GATES),
                wood("emberwood_door", ArtilianBlocks.EMBERWOOD_DOOR.get(),
                        ArtilianItems.EMBERWOOD_DOOR_ITEM.get(), BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS),
                wood("emberwood_trapdoor", ArtilianBlocks.EMBERWOOD_TRAPDOOR.get(),
                        ArtilianItems.EMBERWOOD_TRAPDOOR_ITEM.get(), BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS),

                slate("starlight_slate_stairs", ArtilianBlocks.STARLIGHT_SLATE_STAIRS.get(),
                        ArtilianItems.STARLIGHT_SLATE_STAIRS_ITEM.get(), BlockTags.STAIRS, ItemTags.STAIRS),
                slate("starlight_slate_slab", ArtilianBlocks.STARLIGHT_SLATE_SLAB.get(),
                        ArtilianItems.STARLIGHT_SLATE_SLAB_ITEM.get(), BlockTags.SLABS, ItemTags.SLABS),
                slate("starlight_slate_wall", ArtilianBlocks.STARLIGHT_SLATE_WALL.get(),
                        ArtilianItems.STARLIGHT_SLATE_WALL_ITEM.get(), BlockTags.WALLS, ItemTags.WALLS)
        );
    }

    @GameTest(template = "tnt_arena")
    public static void buildingBlockVariantsRegisteredWithItemsAndTags(GameTestHelper helper) {
        for (Variant variant : variants()) {
            helper.assertTrue(variant.block().asItem() != Items.AIR,
                    variant.name() + " must have a non-air item");
            helper.assertTrue(variant.item() == variant.block().asItem(),
                    variant.name() + " must use its own block item");
            for (TagKey<Block> tag : variant.blockTags()) {
                helper.assertTrue(variant.block().defaultBlockState().is(tag),
                        variant.name() + " must be in block tag " + tag.location());
            }
            for (TagKey<Item> tag : variant.itemTags()) {
                helper.assertTrue(variant.item().builtInRegistryHolder().is(tag),
                        variant.name() + " item must be in item tag " + tag.location());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void buildingBlockVariantsDropThemselves(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = ChesterTestSupport.playerInTestArea(helper, "VariantBuilder");
        List<Variant> variants = variants();

        for (int i = 0; i < variants.size(); i++) {
            Variant variant = variants.get(i);
            BlockPos rel = new BlockPos(1 + (i % 7) * 2, 3, 1 + (i / 7) * 4);
            helper.setBlock(rel.below(), Blocks.STONE);

            BlockState placeState = variant.block().defaultBlockState();
            if (variant.block() instanceof SlabBlock) {
                placeState = placeState.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
            }
            helper.setBlock(rel, placeState);
            if (variant.block() instanceof DoorBlock) {
                helper.setBlock(rel.above(), placeState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            }

            BlockPos abs = helper.absolutePos(rel);
            BlockState state = level.getBlockState(abs);
            ItemStack tool = new ItemStack(variant.tool());
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);
            List<ItemStack> drops = Block.getDrops(state, level, abs, null, player, tool);

            int expectedCount = variant.block() instanceof SlabBlock ? 2 : 1;
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(variant.item())
                            && drops.get(0).getCount() == expectedCount,
                    variant.name() + " must drop " + expectedCount + " of itself, got " + drops);
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void buildingBlockVariantsHaveRecipes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (Variant variant : variants()) {
            boolean craftable = level.getRecipeManager().getRecipes().stream()
                    .anyMatch(holder -> holder.value().getResultItem(level.registryAccess()).is(variant.item()));
            helper.assertTrue(craftable, "Missing recipe producing " + variant.name());
        }
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void fencesAndWallsConnect(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        BlockPos westFence = new BlockPos(2, 3, 2);
        BlockPos eastFence = westFence.east();
        helper.setBlock(westFence, ArtilianBlocks.CRYSTAL_CANOPY_FENCE.get());
        helper.setBlock(eastFence, ArtilianBlocks.CRYSTAL_CANOPY_FENCE.get());
        BlockPos westFenceAbs = helper.absolutePos(westFence);
        BlockState fenceState = Block.updateFromNeighbourShapes(
                level.getBlockState(westFenceAbs), level, westFenceAbs);
        helper.assertTrue(fenceState.getValue(FenceBlock.EAST),
                "Crystal Canopy Fence must connect EAST to the next fence, got " + fenceState);

        BlockPos westWall = new BlockPos(2, 3, 6);
        BlockPos eastWall = westWall.east();
        helper.setBlock(westWall, ArtilianBlocks.STARLIGHT_SLATE_WALL.get());
        helper.setBlock(eastWall, ArtilianBlocks.STARLIGHT_SLATE_WALL.get());
        BlockPos westWallAbs = helper.absolutePos(westWall);
        BlockState wallState = Block.updateFromNeighbourShapes(
                level.getBlockState(westWallAbs), level, westWallAbs);
        helper.assertTrue(wallState.getValue(WallBlock.EAST_WALL) != WallSide.NONE,
                "Starlight Slate Wall must connect EAST to the next wall, got " + wallState);

        helper.succeed();
    }
}
