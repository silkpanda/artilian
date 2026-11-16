package com.silkpanda.artilian.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ArtilianLootTableProvider extends LootTableProvider {
    public ArtilianLootTableProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ArtilianBlockLootSubProvider::new, LootContextParamSets.BLOCK)
        ), registries);
    }
}
