package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.stream.Stream;

/** Loot drops for custom Phase 2 Stage 6 structure enemies. */
public class ArtilianEntityLootSubProvider extends EntityLootSubProvider {
    protected ArtilianEntityLootSubProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        this.add(ArtilianEntities.PRISM_SENTINEL.get(), guardianTable(Items.AMETHYST_SHARD, 1, 2));
        this.add(ArtilianEntities.CINDER_RAIDER.get(), guardianTable(ArtilianItems.RAW_SUNSTEEL.get(), 1, 2));
        this.add(ArtilianEntities.ASTRAL_STALKER.get(), guardianTable(ArtilianItems.STARLIGHT_CRYSTAL.get(), 1, 1));
        this.add(ArtilianEntities.TRAINING_GOLEM.get(), LootTable.lootTable());
        this.add(ArtilianEntities.CRYSTAL_COLOSSUS.get(), crystalColossusTable());
    }

    private static LootTable.Builder crystalColossusTable() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.PRISM_CORE.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 3)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.STARLIGHT_CRYSTAL.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 6)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.AMETHYST_SHARD)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 6)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ArtilianItems.BIOME_TREAT.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))));
    }

    private static LootTable.Builder guardianTable(ItemLike primaryDrop, int min, int max) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(primaryDrop)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.5F))
                        .add(LootItem.lootTableItem(ArtilianItems.BIOME_TREAT.get())));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(
                ArtilianEntities.PRISM_SENTINEL.get(),
                ArtilianEntities.CINDER_RAIDER.get(),
                ArtilianEntities.ASTRAL_STALKER.get(),
                ArtilianEntities.TRAINING_GOLEM.get(),
                ArtilianEntities.CRYSTAL_COLOSSUS.get()
        );
    }
}
