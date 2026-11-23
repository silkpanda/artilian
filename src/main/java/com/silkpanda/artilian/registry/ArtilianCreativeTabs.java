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
                        output.accept(ArtilianItems.CHESTER_SPAWN_EGG.get());
                        output.accept(ArtilianItems.CAPTURE_CRYSTAL.get());
                        output.accept(ArtilianItems.BIOME_TREAT.get());
                        output.accept(ArtilianItems.CRYSTAL_FOX_SPAWN_EGG.get());
                        output.accept(ArtilianItems.SHADOW_WISP_SPAWN_EGG.get());
                        output.accept(ArtilianItems.CINDER_CRAG_SPAWN_EGG.get());
                        output.accept(ArtilianItems.ZAPHOX_SPAWN_EGG.get());
                        output.accept(ArtilianItems.GALECORN_SPAWN_EGG.get());
                        output.accept(ArtilianItems.EMBER_DRAKE_SPAWN_EGG.get());
                        output.accept(ArtilianItems.PEEPKIN_SPAWN_EGG.get());
                        output.accept(ArtilianItems.PRISMLING_SPAWN_EGG.get());
                        output.accept(ArtilianItems.FLURRYWING_SPAWN_EGG.get());
                        output.accept(ArtilianItems.SUNBEAM_SPAWN_EGG.get());
                        output.accept(ArtilianItems.FROGDUCK_SPAWN_EGG.get());
                        output.accept(ArtilianItems.ZAPKIN_SPAWN_EGG.get());
                        output.accept(ArtilianItems.PRISM_SENTINEL_SPAWN_EGG.get());
                        output.accept(ArtilianItems.CINDER_RAIDER_SPAWN_EGG.get());
                        output.accept(ArtilianItems.ASTRAL_STALKER_SPAWN_EGG.get());
                        output.accept(ArtilianItems.PEEPER_SPAWN_EGG.get());
                        output.accept(ArtilianItems.OCULUS_SLIME_SPAWN_EGG.get());
                        output.accept(ArtilianItems.LENS_GUARDIAN_SPAWN_EGG.get());
                        output.accept(ArtilianItems.BLINKY_SPAWN_EGG.get());
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
                        output.accept(ArtilianItems.STARLIGHT_GRAVITY_TNT_ITEM.get());
                        output.accept(ArtilianItems.BRIMSTONE_METEOR_TNT_ITEM.get());
                        output.accept(ArtilianItems.SPARK_WAND.get());
                        output.accept(ArtilianItems.HEART_BLOOM_STAFF.get());
                        output.accept(ArtilianItems.ZEPHYR_DASH_WAND.get());
                        output.accept(ArtilianItems.PRISM_NOVA_STAFF.get());
                        output.accept(ArtilianItems.BRIMSTONE_METEOR_STAFF.get());
                        output.accept(ArtilianItems.CRYSTAL_TURF_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_LOG_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_PLANKS_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_STAIRS_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_SLAB_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_FENCE_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_FENCE_GATE_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_DOOR_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_TRAPDOOR_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_LEAVES_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_CANOPY_SAPLING_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_BLOOM_ITEM.get());
                        output.accept(ArtilianItems.EMBER_TURF_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_LOG_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_PLANKS_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_STAIRS_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_SLAB_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_FENCE_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_FENCE_GATE_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_DOOR_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_TRAPDOOR_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_LEAVES_ITEM.get());
                        output.accept(ArtilianItems.EMBERWOOD_SAPLING_ITEM.get());
                        output.accept(ArtilianItems.EMBER_BLOOM_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_TURF_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_SLATE_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_SLATE_STAIRS_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_SLATE_SLAB_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_SLATE_WALL_ITEM.get());
                        output.accept(ArtilianItems.STARLIGHT_BLOSSOM_ITEM.get());
                        output.accept(ArtilianItems.BOSS_ALTAR_ITEM.get());
                        output.accept(ArtilianItems.CRYSTAL_COLOSSUS_SPAWN_EGG.get());
                        output.accept(ArtilianItems.COLOSSUS_HEART.get());
                        output.accept(ArtilianItems.PRISM_CORE.get());
                        output.accept(ArtilianItems.CAPTAIN_BRIMSTONE_SPAWN_EGG.get());
                        output.accept(ArtilianItems.BRIMSTONE_COMPASS.get());
                        output.accept(ArtilianItems.BRIMSTONE_DOUBLOON.get());
                        output.accept(ArtilianItems.PRISMATIC_SWORD.get());
                        output.accept(ArtilianItems.PRISMATIC_PICKAXE.get());
                        output.accept(ArtilianItems.PRISMATIC_AXE.get());
                        output.accept(ArtilianItems.PRISMATIC_SHOVEL.get());
                        output.accept(ArtilianItems.PRISMATIC_HOE.get());
                        output.accept(ArtilianItems.PRISMATIC_HELMET.get());
                        output.accept(ArtilianItems.PRISMATIC_CHESTPLATE.get());
                        output.accept(ArtilianItems.PRISMATIC_LEGGINGS.get());
                        output.accept(ArtilianItems.PRISMATIC_BOOTS.get());
                        output.accept(ArtilianItems.GAZING_EYE.get());
                        output.accept(ArtilianItems.EYE_TURF_ITEM.get());
                        output.accept(ArtilianItems.GAZE_STONE_ITEM.get());
                        output.accept(ArtilianItems.GAZE_STONE_STAIRS_ITEM.get());
                        output.accept(ArtilianItems.GAZE_STONE_SLAB_ITEM.get());
                        output.accept(ArtilianItems.GAZE_STONE_WALL_ITEM.get());
                        output.accept(ArtilianItems.BLINKING_EYE_BLOCK_ITEM.get());
                        output.accept(ArtilianItems.OCULAR_LOG_ITEM.get());
                        output.accept(ArtilianItems.OCULAR_PLANKS_ITEM.get());
                        output.accept(ArtilianItems.OCULAR_STAIRS_ITEM.get());
                        output.accept(ArtilianItems.OCULAR_SLAB_ITEM.get());
                        output.accept(ArtilianItems.OCULAR_LEAVES_ITEM.get());
                        output.accept(ArtilianItems.OCULAR_SAPLING_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
