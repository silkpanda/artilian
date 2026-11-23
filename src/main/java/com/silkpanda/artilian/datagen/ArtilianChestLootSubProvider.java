package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.worldgen.ArtilianStructures;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

/** Reward chest loot tables for Phase 2 Stage 6 Overworld biome structures. */
public class ArtilianChestLootSubProvider implements LootTableSubProvider {
    public ArtilianChestLootSubProvider(HolderLookup.Provider registries) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(ArtilianStructures.CRYSTAL_GROVE_SHRINE_CHEST, buildShrineChest(
                Items.AMETHYST_SHARD,
                ArtilianItems.STARLIGHT_CRYSTAL.get(),
                ArtilianBlocks.CRYSTAL_BLOOM.get(),
                ArtilianItems.HEART_BLOOM_STAFF.get()));

        output.accept(ArtilianStructures.EMBERBLOOM_OUTPOST_CHEST, buildShrineChest(
                ArtilianItems.RAW_SUNSTEEL.get(),
                ArtilianItems.SUNSTEEL_INGOT.get(),
                ArtilianBlocks.EMBER_BLOOM.get(),
                ArtilianItems.PARTY_TNT_ITEM.get()));

        output.accept(ArtilianStructures.STARLIGHT_WATCHTOWER_CHEST, buildShrineChest(
                ArtilianItems.STARLIGHT_CRYSTAL.get(),
                Items.ENDER_PEARL,
                ArtilianBlocks.STARLIGHT_BLOSSOM.get(),
                ArtilianItems.SPARK_WAND.get()));

        output.accept(ArtilianStructures.CRYSTAL_COLOSSUS_ARENA_CHEST, buildCrystalColossusArenaChest());
        output.accept(ArtilianStructures.BRIMSTONE_BASTION_CHEST, buildBrimstoneBastionChest());
        output.accept(ArtilianStructures.OCULAR_OBSERVATORY_CHEST, buildOcularObservatoryChest());
        output.accept(ArtilianStructures.GAZING_SANCTUM_CHEST, buildGazingSanctumChest());
    }

    private static LootTable.Builder buildOcularObservatoryChest() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.BIOME_TREAT.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.CAPTURE_CRYSTAL.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 3))
                        .add(LootItem.lootTableItem(Items.AMETHYST_SHARD)
                                .setWeight(4)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                        .add(LootItem.lootTableItem(ArtilianItems.STARLIGHT_CRYSTAL.get())
                                .setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                        .add(LootItem.lootTableItem(Items.ENDER_PEARL)
                                .setWeight(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                        .add(LootItem.lootTableItem(ArtilianBlocks.OCULAR_SAPLING.get())
                                .setWeight(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                        .add(LootItem.lootTableItem(ArtilianItems.GAZING_EYE.get())
                                .setWeight(1)));
    }

    private static LootTable.Builder buildGazingSanctumChest() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.BIOME_TREAT.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.CAPTURE_CRYSTAL.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.STARLIGHT_CRYSTAL.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 3))
                        .add(LootItem.lootTableItem(Items.ENDER_PEARL)
                                .setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                        .add(LootItem.lootTableItem(ArtilianBlocks.BLINKING_EYE_BLOCK.get())
                                .setWeight(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                        .add(LootItem.lootTableItem(Items.GOLDEN_APPLE)
                                .setWeight(1))
                        .add(LootItem.lootTableItem(ArtilianItems.STARLIGHT_GRAVITY_TNT_ITEM.get())
                                .setWeight(2))
                        .add(LootItem.lootTableItem(ArtilianItems.GAZING_EYE.get())
                                .setWeight(1)));
    }

    private static LootTable.Builder buildBrimstoneBastionChest() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.BRIMSTONE_COMPASS.get())))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.BIOME_TREAT.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 3))
                        .add(LootItem.lootTableItem(ArtilianItems.SUNSTEEL_INGOT.get())
                                .setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                        .add(LootItem.lootTableItem(Items.BLAZE_POWDER)
                                .setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                        .add(LootItem.lootTableItem(Items.GOLDEN_APPLE)
                                .setWeight(1))
                        .add(LootItem.lootTableItem(ArtilianItems.CAPTURE_CRYSTAL.get())
                                .setWeight(2)));
    }

    private static LootTable.Builder buildCrystalColossusArenaChest() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.COLOSSUS_HEART.get())))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.BIOME_TREAT.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 3))
                        .add(LootItem.lootTableItem(ArtilianItems.STARLIGHT_CRYSTAL.get())
                                .setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                        .add(LootItem.lootTableItem(Items.AMETHYST_SHARD)
                                .setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                        .add(LootItem.lootTableItem(Items.GOLDEN_APPLE)
                                .setWeight(1))
                        .add(LootItem.lootTableItem(ArtilianItems.CAPTURE_CRYSTAL.get())
                                .setWeight(2)));
    }

    private static LootTable.Builder buildShrineChest(
            ItemLike commonGem, ItemLike rareGoodie, ItemLike biomeFlower, ItemLike bonusGear) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.BIOME_TREAT.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.CAPTURE_CRYSTAL.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 3))
                        .add(LootItem.lootTableItem(commonGem)
                                .setWeight(4)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                        .add(LootItem.lootTableItem(rareGoodie)
                                .setWeight(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                        .add(LootItem.lootTableItem(biomeFlower)
                                .setWeight(2)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                        .add(LootItem.lootTableItem(bonusGear).setWeight(1)));
    }
}
