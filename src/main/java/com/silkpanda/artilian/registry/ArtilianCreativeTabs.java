package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArtilianCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArtilianMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.artilian"))
                    .icon(() -> new ItemStack(ArtilianItems.SUNSTEEL_SWORD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ArtilianItems.CHESTER_WHISTLE.get());
                        output.accept(ArtilianItems.CAPTURE_CRYSTAL.get());
                        output.accept(ArtilianItems.BIOME_TREAT.get());
                        output.accept(ArtilianItems.SUNSTEEL_ORE_ITEM.get());
                        output.accept(ArtilianItems.DEEPSLATE_SUNSTEEL_ORE_ITEM.get());
                        output.accept(ArtilianItems.SUNSTEEL_BLOCK_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_CRYSTAL_ORE_ITEM.get());
                        output.accept(ArtilianItems.DEEPSLATE_STARLIGHT_CRYSTAL_ORE_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_CRYSTAL_BLOCK_ITEM.get());
                        output.accept(ArtilianItems.RAW_SUNSTEEL.get());
                        output.accept(ArtilianItems.SUNSTEEL_INGOT.get());
                        output.accept(ArtilianItems.STARLIGHT_CRYSTAL.get());
                        output.accept(ArtilianItems.SUNSTEEL_SWORD.get());
                        output.accept(ArtilianItems.SUNSTEEL_PICKAXE.get());
                        output.accept(ArtilianItems.SUNSTEEL_AXE.get());
                        output.accept(ArtilianItems.SUNSTEEL_SHOVEL.get());
                        output.accept(ArtilianItems.SUNSTEEL_HOE.get());
                        output.accept(ArtilianItems.STARLIGHT_SWORD.get());
                        output.accept(ArtilianItems.STARLIGHT_PICKAXE.get());
                        output.accept(ArtilianItems.STARLIGHT_AXE.get());
                        output.accept(ArtilianItems.STARLIGHT_SHOVEL.get());
                        output.accept(ArtilianItems.STARLIGHT_HOE.get());
                        output.accept(ArtilianItems.SUNSTEEL_HELMET.get());
                        output.accept(ArtilianItems.SUNSTEEL_CHESTPLATE.get());
                        output.accept(ArtilianItems.SUNSTEEL_LEGGINGS.get());
                        output.accept(ArtilianItems.SUNSTEEL_BOOTS.get());
                        output.accept(ArtilianItems.STARLIGHT_HELMET.get());
                        output.accept(ArtilianItems.STARLIGHT_CHESTPLATE.get());
                        output.accept(ArtilianItems.STARLIGHT_LEGGINGS.get());
                        output.accept(ArtilianItems.STARLIGHT_BOOTS.get());
                        output.accept(ArtilianItems.PARTY_TNT_ITEM.get());
                        output.accept(ArtilianItems.BOUNCE_TNT_ITEM.get());
                        output.accept(ArtilianItems.FROST_NOVA_TNT_ITEM.get());
                        output.accept(ArtilianItems.CLUSTER_QUARRY_TNT_ITEM.get());
                        output.accept(ArtilianItems.SPARK_WAND.get());
                        output.accept(ArtilianItems.HEART_BLOOM_STAFF.get());
                        output.accept(ArtilianItems.ZEPHYR_DASH_WAND.get());
                        output.accept(ArtilianItems.CRYSTAL_TURF_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_LOG_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_PLANKS_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_LEAVES_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_SAPLING_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_BLOOM_ITEM.get());
                        output.accept(ArtilianItems.EMBER_TURF_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_LOG_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_PLANKS_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_LEAVES_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_SAPLING_ITEM.get());
                        output.accept(ArtilianItems.EMBER_BLOOM_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_TURF_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_SLATE_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_BLOSSOM_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
