package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterWhistleItem;
import com.silkpanda.artilian.gear.ArtilianArmorItem;
import com.silkpanda.artilian.gear.ArtilianSwordItem;
import com.silkpanda.artilian.gear.ArtilianTiers;
import com.silkpanda.artilian.mana.ArtilianWandItem;
import com.silkpanda.artilian.mana.WandSpell;
import com.silkpanda.artilian.pet.BiomeTreatItem;
import com.silkpanda.artilian.pet.CaptureCrystalItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArtilianItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArtilianMod.MODID);

    public static final DeferredItem<ChesterWhistleItem> CHESTER_WHISTLE =
            ITEMS.register("chester_whistle", () -> new ChesterWhistleItem(new Item.Properties().stacksTo(1)));

    // Pet capture & treats (Phase 2 Stage 3)
    public static final DeferredItem<CaptureCrystalItem> CAPTURE_CRYSTAL =
            ITEMS.register("capture_crystal", () -> new CaptureCrystalItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<BiomeTreatItem> BIOME_TREAT =
            ITEMS.register("biome_treat", () -> new BiomeTreatItem(new Item.Properties().stacksTo(64)));

    // Creative spawn eggs (all living entities)
    public static final DeferredItem<DeferredSpawnEggItem> CHESTER_SPAWN_EGG =
            ITEMS.register("chester_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.CHESTER, 0x9C6B37, 0xF5D04E, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CRYSTAL_FOX_SPAWN_EGG =
            ITEMS.register("crystal_fox_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.CRYSTAL_FOX, 0xD4F4F3, 0x38D9C0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SHADOW_WISP_SPAWN_EGG =
            ITEMS.register("shadow_wisp_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.SHADOW_WISP, 0x3A324C, 0xD946EF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CINDER_CRAG_SPAWN_EGG =
            ITEMS.register("cinder_crag_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.CINDER_CRAG, 0xB6203E, 0xF97316, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ZAPHOX_SPAWN_EGG =
            ITEMS.register("zaphox_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.ZAPHOX, 0xFBBF24, 0xF97316, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GALECORN_SPAWN_EGG =
            ITEMS.register("galecorn_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.GALECORN, 0xFDE068, 0x38D9C0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> EMBER_DRAKE_SPAWN_EGG =
            ITEMS.register("ember_drake_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.EMBER_DRAKE, 0x22C55E, 0xF97316, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PEEPKIN_SPAWN_EGG =
            ITEMS.register("peepkin_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.PEEPKIN, 0x563892, 0xD946EF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PRISM_SENTINEL_SPAWN_EGG =
            ITEMS.register("prism_sentinel_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.PRISM_SENTINEL, 0x3A2F4D, 0x38D9C0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CINDER_RAIDER_SPAWN_EGG =
            ITEMS.register("cinder_raider_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.CINDER_RAIDER, 0x54221C, 0xFF9F38, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ASTRAL_STALKER_SPAWN_EGG =
            ITEMS.register("astral_stalker_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.ASTRAL_STALKER, 0x2B2338, 0xD946EF, new Item.Properties()));

    // Blocks
    public static final DeferredItem<BlockItem> SUNSTEEL_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("sunsteel_ore", ArtilianBlocks.SUNSTEEL_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_SUNSTEEL_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("deepslate_sunsteel_ore", ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE);
    public static final DeferredItem<BlockItem> SUNSTEEL_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("sunsteel_block", ArtilianBlocks.SUNSTEEL_BLOCK);
    public static final DeferredItem<BlockItem> STARLIGHT_CRYSTAL_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("starlight_crystal_ore", ArtilianBlocks.STARLIGHT_CRYSTAL_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_STARLIGHT_CRYSTAL_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("deepslate_starlight_crystal_ore", ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE);
    public static final DeferredItem<BlockItem> STARLIGHT_CRYSTAL_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("starlight_crystal_block", ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK);

    // Custom TNTs
    public static final DeferredItem<BlockItem> PARTY_TNT_ITEM =
            ITEMS.registerSimpleBlockItem("party_tnt", ArtilianBlocks.PARTY_TNT);
    public static final DeferredItem<BlockItem> BOUNCE_TNT_ITEM =
            ITEMS.registerSimpleBlockItem("bounce_tnt", ArtilianBlocks.BOUNCE_TNT);
    public static final DeferredItem<BlockItem> FROST_NOVA_TNT_ITEM =
            ITEMS.registerSimpleBlockItem("frost_nova_tnt", ArtilianBlocks.FROST_NOVA_TNT);
    public static final DeferredItem<BlockItem> CLUSTER_QUARRY_TNT_ITEM =
            ITEMS.registerSimpleBlockItem("cluster_quarry_tnt", ArtilianBlocks.CLUSTER_QUARRY_TNT);

    // Crystal Canopy Woods blocks
    public static final DeferredItem<BlockItem> CRYSTAL_TURF_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_turf", ArtilianBlocks.CRYSTAL_TURF);
    public static final DeferredItem<BlockItem> CRYSTAL_CANOPY_LOG_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_canopy_log", ArtilianBlocks.CRYSTAL_CANOPY_LOG);
    public static final DeferredItem<BlockItem> CRYSTAL_CANOPY_PLANKS_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_canopy_planks", ArtilianBlocks.CRYSTAL_CANOPY_PLANKS);

    // Building-block variants (Structure Rebuild Stage 1)
    public static final DeferredItem<BlockItem> CRYSTAL_CANOPY_STAIRS_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_canopy_stairs", ArtilianBlocks.CRYSTAL_CANOPY_STAIRS);
    public static final DeferredItem<BlockItem> CRYSTAL_CANOPY_SLAB_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_canopy_slab", ArtilianBlocks.CRYSTAL_CANOPY_SLAB);
    public static final DeferredItem<BlockItem> CRYSTAL_CANOPY_FENCE_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_canopy_fence", ArtilianBlocks.CRYSTAL_CANOPY_FENCE);
    public static final DeferredItem<BlockItem> CRYSTAL_CANOPY_FENCE_GATE_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_canopy_fence_gate", ArtilianBlocks.CRYSTAL_CANOPY_FENCE_GATE);
    public static final DeferredItem<DoubleHighBlockItem> CRYSTAL_CANOPY_DOOR_ITEM =
            ITEMS.register("crystal_canopy_door", () -> new DoubleHighBlockItem(
                    ArtilianBlocks.CRYSTAL_CANOPY_DOOR.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> CRYSTAL_CANOPY_TRAPDOOR_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_canopy_trapdoor", ArtilianBlocks.CRYSTAL_CANOPY_TRAPDOOR);

    public static final DeferredItem<BlockItem> CRYSTAL_CANOPY_LEAVES_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_canopy_leaves", ArtilianBlocks.CRYSTAL_CANOPY_LEAVES);
    public static final DeferredItem<BlockItem> CRYSTAL_CANOPY_SAPLING_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_canopy_sapling", ArtilianBlocks.CRYSTAL_CANOPY_SAPLING);
    public static final DeferredItem<BlockItem> CRYSTAL_BLOOM_ITEM =
            ITEMS.registerSimpleBlockItem("crystal_bloom", ArtilianBlocks.CRYSTAL_BLOOM);

    // Emberbloom Meadows blocks (Phase 2 Stage 2)
    public static final DeferredItem<BlockItem> EMBER_TURF_ITEM =
            ITEMS.registerSimpleBlockItem("ember_turf", ArtilianBlocks.EMBER_TURF);
    public static final DeferredItem<BlockItem> EMBERWOOD_LOG_ITEM =
            ITEMS.registerSimpleBlockItem("emberwood_log", ArtilianBlocks.EMBERWOOD_LOG);
    public static final DeferredItem<BlockItem> EMBERWOOD_PLANKS_ITEM =
            ITEMS.registerSimpleBlockItem("emberwood_planks", ArtilianBlocks.EMBERWOOD_PLANKS);

    // Building-block variants (Structure Rebuild Stage 1)
    public static final DeferredItem<BlockItem> EMBERWOOD_STAIRS_ITEM =
            ITEMS.registerSimpleBlockItem("emberwood_stairs", ArtilianBlocks.EMBERWOOD_STAIRS);
    public static final DeferredItem<BlockItem> EMBERWOOD_SLAB_ITEM =
            ITEMS.registerSimpleBlockItem("emberwood_slab", ArtilianBlocks.EMBERWOOD_SLAB);
    public static final DeferredItem<BlockItem> EMBERWOOD_FENCE_ITEM =
            ITEMS.registerSimpleBlockItem("emberwood_fence", ArtilianBlocks.EMBERWOOD_FENCE);
    public static final DeferredItem<BlockItem> EMBERWOOD_FENCE_GATE_ITEM =
            ITEMS.registerSimpleBlockItem("emberwood_fence_gate", ArtilianBlocks.EMBERWOOD_FENCE_GATE);
    public static final DeferredItem<DoubleHighBlockItem> EMBERWOOD_DOOR_ITEM =
            ITEMS.register("emberwood_door", () -> new DoubleHighBlockItem(
                    ArtilianBlocks.EMBERWOOD_DOOR.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> EMBERWOOD_TRAPDOOR_ITEM =
            ITEMS.registerSimpleBlockItem("emberwood_trapdoor", ArtilianBlocks.EMBERWOOD_TRAPDOOR);

    public static final DeferredItem<BlockItem> EMBERWOOD_LEAVES_ITEM =
            ITEMS.registerSimpleBlockItem("emberwood_leaves", ArtilianBlocks.EMBERWOOD_LEAVES);
    public static final DeferredItem<BlockItem> EMBERWOOD_SAPLING_ITEM =
            ITEMS.registerSimpleBlockItem("emberwood_sapling", ArtilianBlocks.EMBERWOOD_SAPLING);
    public static final DeferredItem<BlockItem> EMBER_BLOOM_ITEM =
            ITEMS.registerSimpleBlockItem("ember_bloom", ArtilianBlocks.EMBER_BLOOM);

    // Starlight Highlands blocks (Phase 2 Stage 2)
    public static final DeferredItem<BlockItem> STARLIGHT_TURF_ITEM =
            ITEMS.registerSimpleBlockItem("starlight_turf", ArtilianBlocks.STARLIGHT_TURF);
    public static final DeferredItem<BlockItem> STARLIGHT_SLATE_ITEM =
            ITEMS.registerSimpleBlockItem("starlight_slate", ArtilianBlocks.STARLIGHT_SLATE);

    // Building-block variants (Structure Rebuild Stage 1)
    public static final DeferredItem<BlockItem> STARLIGHT_SLATE_STAIRS_ITEM =
            ITEMS.registerSimpleBlockItem("starlight_slate_stairs", ArtilianBlocks.STARLIGHT_SLATE_STAIRS);
    public static final DeferredItem<BlockItem> STARLIGHT_SLATE_SLAB_ITEM =
            ITEMS.registerSimpleBlockItem("starlight_slate_slab", ArtilianBlocks.STARLIGHT_SLATE_SLAB);
    public static final DeferredItem<BlockItem> STARLIGHT_SLATE_WALL_ITEM =
            ITEMS.registerSimpleBlockItem("starlight_slate_wall", ArtilianBlocks.STARLIGHT_SLATE_WALL);

    public static final DeferredItem<BlockItem> STARLIGHT_BLOSSOM_ITEM =
            ITEMS.registerSimpleBlockItem("starlight_blossom", ArtilianBlocks.STARLIGHT_BLOSSOM);

    // Wands & Staves
    public static final DeferredItem<ArtilianWandItem> SPARK_WAND = ITEMS.register("spark_wand",
            () -> new ArtilianWandItem(WandSpell.SPARK, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ArtilianWandItem> HEART_BLOOM_STAFF = ITEMS.register("heart_bloom_staff",
            () -> new ArtilianWandItem(WandSpell.HEART_BLOOM, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ArtilianWandItem> ZEPHYR_DASH_WAND = ITEMS.register("zephyr_dash_wand",
            () -> new ArtilianWandItem(WandSpell.ZEPHYR_DASH, new Item.Properties().stacksTo(1)));

    // Raw & refined materials
    public static final DeferredItem<Item> RAW_SUNSTEEL =
            ITEMS.registerSimpleItem("raw_sunsteel");
    public static final DeferredItem<Item> SUNSTEEL_INGOT =
            ITEMS.registerSimpleItem("sunsteel_ingot");
    public static final DeferredItem<Item> STARLIGHT_CRYSTAL =
            ITEMS.registerSimpleItem("starlight_crystal");

    // Sunsteel weapons & tools
    public static final DeferredItem<SwordItem> SUNSTEEL_SWORD = ITEMS.register("sunsteel_sword",
            () -> new ArtilianSwordItem(
                    ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(SwordItem.createAttributes(ArtilianTiers.SUNSTEEL, 3, -2.4F)),
                    "item.artilian.sunsteel_sword.tooltip",
                    ChatFormatting.GOLD));
    public static final DeferredItem<PickaxeItem> SUNSTEEL_PICKAXE = ITEMS.register("sunsteel_pickaxe",
            () -> new PickaxeItem(ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(PickaxeItem.createAttributes(ArtilianTiers.SUNSTEEL, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> SUNSTEEL_AXE = ITEMS.register("sunsteel_axe",
            () -> new AxeItem(ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(AxeItem.createAttributes(ArtilianTiers.SUNSTEEL, 5.5F, -3.0F))));
    public static final DeferredItem<ShovelItem> SUNSTEEL_SHOVEL = ITEMS.register("sunsteel_shovel",
            () -> new ShovelItem(ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(ShovelItem.createAttributes(ArtilianTiers.SUNSTEEL, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> SUNSTEEL_HOE = ITEMS.register("sunsteel_hoe",
            () -> new HoeItem(ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(HoeItem.createAttributes(ArtilianTiers.SUNSTEEL, -2.0F, -1.0F))));

    // Starlight Crystal weapons & tools
    public static final DeferredItem<SwordItem> STARLIGHT_SWORD = ITEMS.register("starlight_sword",
            () -> new ArtilianSwordItem(
                    ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(SwordItem.createAttributes(ArtilianTiers.STARLIGHT, 3, -2.2F)),
                    "item.artilian.starlight_sword.tooltip",
                    ChatFormatting.AQUA));
    public static final DeferredItem<PickaxeItem> STARLIGHT_PICKAXE = ITEMS.register("starlight_pickaxe",
            () -> new PickaxeItem(ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(PickaxeItem.createAttributes(ArtilianTiers.STARLIGHT, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> STARLIGHT_AXE = ITEMS.register("starlight_axe",
            () -> new AxeItem(ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(AxeItem.createAttributes(ArtilianTiers.STARLIGHT, 5.0F, -3.0F))));
    public static final DeferredItem<ShovelItem> STARLIGHT_SHOVEL = ITEMS.register("starlight_shovel",
            () -> new ShovelItem(ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(ShovelItem.createAttributes(ArtilianTiers.STARLIGHT, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> STARLIGHT_HOE = ITEMS.register("starlight_hoe",
            () -> new HoeItem(ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(HoeItem.createAttributes(ArtilianTiers.STARLIGHT, -3.0F, 0.0F))));

    // Sunsteel armor
    public static final DeferredItem<ArmorItem> SUNSTEEL_HELMET =
            armor("sunsteel_helmet", "sunsteel", ArtilianArmorMaterials.SUNSTEEL, ArmorItem.Type.HELMET, 20);
    public static final DeferredItem<ArmorItem> SUNSTEEL_CHESTPLATE =
            armor("sunsteel_chestplate", "sunsteel", ArtilianArmorMaterials.SUNSTEEL, ArmorItem.Type.CHESTPLATE, 20);
    public static final DeferredItem<ArmorItem> SUNSTEEL_LEGGINGS =
            armor("sunsteel_leggings", "sunsteel", ArtilianArmorMaterials.SUNSTEEL, ArmorItem.Type.LEGGINGS, 20);
    public static final DeferredItem<ArmorItem> SUNSTEEL_BOOTS =
            armor("sunsteel_boots", "sunsteel", ArtilianArmorMaterials.SUNSTEEL, ArmorItem.Type.BOOTS, 20);

    // Starlight Crystal armor
    public static final DeferredItem<ArmorItem> STARLIGHT_HELMET =
            armor("starlight_helmet", "starlight", ArtilianArmorMaterials.STARLIGHT, ArmorItem.Type.HELMET, 28);
    public static final DeferredItem<ArmorItem> STARLIGHT_CHESTPLATE =
            armor("starlight_chestplate", "starlight", ArtilianArmorMaterials.STARLIGHT, ArmorItem.Type.CHESTPLATE, 28);
    public static final DeferredItem<ArmorItem> STARLIGHT_LEGGINGS =
            armor("starlight_leggings", "starlight", ArtilianArmorMaterials.STARLIGHT, ArmorItem.Type.LEGGINGS, 28);
    public static final DeferredItem<ArmorItem> STARLIGHT_BOOTS =
            armor("starlight_boots", "starlight", ArtilianArmorMaterials.STARLIGHT, ArmorItem.Type.BOOTS, 28);

    private static DeferredItem<ArmorItem> armor(
            String name,
            String set,
            Holder<ArmorMaterial> material,
            ArmorItem.Type type,
            int durabilityMultiplier) {
        return ITEMS.register(name, () -> new ArtilianArmorItem(
                material,
                type,
                new Item.Properties().durability(type.getDurability(durabilityMultiplier)),
                set));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
