package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ArtilianMod.MODID)
public class ArtilianDataGen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ArtilianLanguageProvider(output));
        generator.addProvider(event.includeClient(), new ArtilianBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ArtilianItemModelProvider(output, existingFileHelper));

        generator.addProvider(event.includeServer(), new GameTestStructureProvider(output));
        generator.addProvider(event.includeServer(), new ArtilianStructureTemplateProvider(output));
        ArtilianBlockTagsProvider blockTags = new ArtilianBlockTagsProvider(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(),
                new ArtilianItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(),
                new ArtilianEntityTypeTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ArtilianLootTableProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(), new ArtilianRecipeProvider(output, lookupProvider));
        ArtilianWorldGenProvider worldGen = new ArtilianWorldGenProvider(output, lookupProvider);
        generator.addProvider(event.includeServer(), worldGen);
        generator.addProvider(event.includeServer(),
                new ArtilianBiomeTagsProvider(output, worldGen.getRegistryProvider(), existingFileHelper));
    }
}
