package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.tnt.ArtilianTntBlock;
import com.silkpanda.artilian.tnt.TntVariant;
import com.silkpanda.artilian.worldgen.ArtilianTreeGrowers;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArtilianBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ArtilianMod.MODID);

    public static final DeferredBlock<Block> SUNSTEEL_ORE = BLOCKS.register("sunsteel_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 3.0F)));

    public static final DeferredBlock<Block> DEEPSLATE_SUNSTEEL_ORE = BLOCKS.register("deepslate_sunsteel_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(SUNSTEEL_ORE.get())
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> SUNSTEEL_BLOCK = BLOCKS.register("sunsteel_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)));

    public static final DeferredBlock<Block> STARLIGHT_CRYSTAL_ORE = BLOCKS.register("starlight_crystal_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 3.0F)
                    .lightLevel(state -> 10)));

    public static final DeferredBlock<Block> DEEPSLATE_STARLIGHT_CRYSTAL_ORE = BLOCKS.register("deepslate_starlight_crystal_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5), BlockBehaviour.Properties.ofFullCopy(STARLIGHT_CRYSTAL_ORE.get())
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE)
                    .lightLevel(state -> 10)));

    public static final DeferredBlock<Block> STARLIGHT_CRYSTAL_BLOCK = BLOCKS.register("starlight_crystal_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIAMOND)
                    .instrument(NoteBlockInstrument.CHIME)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> 14)));

    // Custom TNTs (Phase 1 Stage 4)
    public static final DeferredBlock<ArtilianTntBlock> PARTY_TNT = BLOCKS.register("party_tnt",
            () -> new ArtilianTntBlock(TntVariant.PARTY,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.TNT).mapColor(MapColor.COLOR_MAGENTA)));
    public static final DeferredBlock<ArtilianTntBlock> BOUNCE_TNT = BLOCKS.register("bounce_tnt",
            () -> new ArtilianTntBlock(TntVariant.BOUNCE,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.TNT).mapColor(MapColor.COLOR_LIGHT_GREEN)));
    public static final DeferredBlock<ArtilianTntBlock> FROST_NOVA_TNT = BLOCKS.register("frost_nova_tnt",
            () -> new ArtilianTntBlock(TntVariant.FROST_NOVA,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.TNT).mapColor(MapColor.ICE)));
    public static final DeferredBlock<ArtilianTntBlock> CLUSTER_QUARRY_TNT = BLOCKS.register("cluster_quarry_tnt",
            () -> new ArtilianTntBlock(TntVariant.CLUSTER_QUARRY,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.TNT).mapColor(MapColor.COLOR_ORANGE)));

    // Crystal Canopy Woods (Phase 2 Stage 1)
    public static final DeferredBlock<Block> CRYSTAL_TURF = BLOCKS.register("crystal_turf",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    public static final DeferredBlock<RotatedPillarBlock> CRYSTAL_CANOPY_LOG = BLOCKS.register("crystal_canopy_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                    .mapColor(MapColor.COLOR_BLUE)));

    public static final DeferredBlock<Block> CRYSTAL_CANOPY_PLANKS = BLOCKS.register("crystal_canopy_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.COLOR_PURPLE)));

    // Building-block variants (Structure Rebuild Stage 1)
    public static final DeferredBlock<StairBlock> CRYSTAL_CANOPY_STAIRS = BLOCKS.register("crystal_canopy_stairs",
            () -> new StairBlock(CRYSTAL_CANOPY_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)
                            .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<SlabBlock> CRYSTAL_CANOPY_SLAB = BLOCKS.register("crystal_canopy_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)
                    .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<FenceBlock> CRYSTAL_CANOPY_FENCE = BLOCKS.register("crystal_canopy_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)
                    .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<FenceGateBlock> CRYSTAL_CANOPY_FENCE_GATE = BLOCKS.register("crystal_canopy_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)
                            .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<DoorBlock> CRYSTAL_CANOPY_DOOR = BLOCKS.register("crystal_canopy_door",
            () -> new DoorBlock(BlockSetType.OAK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)
                            .mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<TrapDoorBlock> CRYSTAL_CANOPY_TRAPDOOR = BLOCKS.register("crystal_canopy_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)
                            .mapColor(MapColor.COLOR_PURPLE)));

    public static final DeferredBlock<LeavesBlock> CRYSTAL_CANOPY_LEAVES = BLOCKS.register("crystal_canopy_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .mapColor(MapColor.COLOR_CYAN)
                    .lightLevel(state -> 6)));

    public static final DeferredBlock<SaplingBlock> CRYSTAL_CANOPY_SAPLING = BLOCKS.register("crystal_canopy_sapling",
            () -> new SaplingBlock(ArtilianTreeGrowers.CRYSTAL_CANOPY,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)
                            .mapColor(MapColor.COLOR_CYAN)
                            .lightLevel(state -> 4)));

    public static final DeferredBlock<FlowerBlock> CRYSTAL_BLOOM = BLOCKS.register("crystal_bloom",
            () -> new FlowerBlock(MobEffects.GLOWING, 5.0F,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                            .mapColor(MapColor.COLOR_MAGENTA)
                            .lightLevel(state -> 9)));

    // Emberbloom Meadows (Phase 2 Stage 2)
    public static final DeferredBlock<Block> EMBER_TURF = BLOCKS.register("ember_turf",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    public static final DeferredBlock<RotatedPillarBlock> EMBERWOOD_LOG = BLOCKS.register("emberwood_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                    .mapColor(MapColor.COLOR_ORANGE)));

    public static final DeferredBlock<Block> EMBERWOOD_PLANKS = BLOCKS.register("emberwood_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.COLOR_ORANGE)));

    // Building-block variants (Structure Rebuild Stage 1)
    public static final DeferredBlock<StairBlock> EMBERWOOD_STAIRS = BLOCKS.register("emberwood_stairs",
            () -> new StairBlock(EMBERWOOD_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)
                            .mapColor(MapColor.COLOR_ORANGE)));
    public static final DeferredBlock<SlabBlock> EMBERWOOD_SLAB = BLOCKS.register("emberwood_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)
                    .mapColor(MapColor.COLOR_ORANGE)));
    public static final DeferredBlock<FenceBlock> EMBERWOOD_FENCE = BLOCKS.register("emberwood_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)
                    .mapColor(MapColor.COLOR_ORANGE)));
    public static final DeferredBlock<FenceGateBlock> EMBERWOOD_FENCE_GATE = BLOCKS.register("emberwood_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)
                            .mapColor(MapColor.COLOR_ORANGE)));
    public static final DeferredBlock<DoorBlock> EMBERWOOD_DOOR = BLOCKS.register("emberwood_door",
            () -> new DoorBlock(BlockSetType.OAK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)
                            .mapColor(MapColor.COLOR_ORANGE)));
    public static final DeferredBlock<TrapDoorBlock> EMBERWOOD_TRAPDOOR = BLOCKS.register("emberwood_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)
                            .mapColor(MapColor.COLOR_ORANGE)));

    public static final DeferredBlock<LeavesBlock> EMBERWOOD_LEAVES = BLOCKS.register("emberwood_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .mapColor(MapColor.COLOR_YELLOW)
                    .lightLevel(state -> 4)));

    public static final DeferredBlock<SaplingBlock> EMBERWOOD_SAPLING = BLOCKS.register("emberwood_sapling",
            () -> new SaplingBlock(ArtilianTreeGrowers.EMBERWOOD,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)
                            .mapColor(MapColor.COLOR_ORANGE)
                            .lightLevel(state -> 4)));

    public static final DeferredBlock<FlowerBlock> EMBER_BLOOM = BLOCKS.register("ember_bloom",
            () -> new FlowerBlock(MobEffects.FIRE_RESISTANCE, 5.0F,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                            .mapColor(MapColor.COLOR_ORANGE)
                            .lightLevel(state -> 9)));

    // Starlight Highlands (Phase 2 Stage 2)
    public static final DeferredBlock<Block> STARLIGHT_TURF = BLOCKS.register("starlight_turf",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    public static final DeferredBlock<Block> STARLIGHT_SLATE = BLOCKS.register("starlight_slate",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F, 6.0F)));

    // Building-block variants (Structure Rebuild Stage 1)
    public static final DeferredBlock<StairBlock> STARLIGHT_SLATE_STAIRS = BLOCKS.register("starlight_slate_stairs",
            () -> new StairBlock(STARLIGHT_SLATE.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(STARLIGHT_SLATE.get())));
    public static final DeferredBlock<SlabBlock> STARLIGHT_SLATE_SLAB = BLOCKS.register("starlight_slate_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(STARLIGHT_SLATE.get())));
    public static final DeferredBlock<WallBlock> STARLIGHT_SLATE_WALL = BLOCKS.register("starlight_slate_wall",
            () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(STARLIGHT_SLATE.get())));

    public static final DeferredBlock<FlowerBlock> STARLIGHT_BLOSSOM = BLOCKS.register("starlight_blossom",
            () -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .lightLevel(state -> 10)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
