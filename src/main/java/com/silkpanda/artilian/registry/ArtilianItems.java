package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.boss.BossSummonItem;
import com.silkpanda.artilian.boss.BossTrophyItem;
import com.silkpanda.artilian.dimension.GazingEyeItem;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterWhistleItem;
import com.silkpanda.artilian.gear.ArtilianArmorItem;
import com.silkpanda.artilian.gear.ArtilianSwordItem;
import com.silkpanda.artilian.gear.ArtilianTiers;
import com.silkpanda.artilian.gear.DragonscaleAxeItem;
import com.silkpanda.artilian.gear.DragonscaleHoeItem;
import com.silkpanda.artilian.gear.DragonscalePickaxeItem;
import com.silkpanda.artilian.gear.DragonscaleShovelItem;
import com.silkpanda.artilian.gear.DragonscaleSwordItem;
import com.silkpanda.artilian.gear.PrismaticAxeItem;
import com.silkpanda.artilian.gear.PrismaticHoeItem;
import com.silkpanda.artilian.gear.PrismaticPickaxeItem;
import com.silkpanda.artilian.gear.PrismaticShovelItem;
import com.silkpanda.artilian.gear.PrismaticSwordItem;
import com.silkpanda.artilian.gear.StarlightAxeItem;
import com.silkpanda.artilian.gear.StarlightHoeItem;
import com.silkpanda.artilian.gear.StarlightPickaxeItem;
import com.silkpanda.artilian.gear.StarlightShovelItem;
import com.silkpanda.artilian.gear.StarlightSwordItem;
import com.silkpanda.artilian.gear.SunsteelAxeItem;
import com.silkpanda.artilian.gear.SunsteelHoeItem;
import com.silkpanda.artilian.gear.SunsteelPickaxeItem;
import com.silkpanda.artilian.gear.SunsteelShovelItem;
import com.silkpanda.artilian.gear.SunsteelSwordItem;
import com.silkpanda.artilian.gear.TwinStarAxeItem;
import com.silkpanda.artilian.gear.TwinStarBladeItem;
import com.silkpanda.artilian.gear.TwinStarHoeItem;
import com.silkpanda.artilian.gear.TwinStarPickaxeItem;
import com.silkpanda.artilian.gear.TwinStarShovelItem;
import com.silkpanda.artilian.gear.TwinStarStaffItem;
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
import net.minecraft.world.item.Rarity;
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
    public static final DeferredItem<DeferredSpawnEggItem> PRISMLING_SPAWN_EGG =
            ITEMS.register("prismling_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.PRISMLING, 0xF472B6, 0x38BDF8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> FLURRYWING_SPAWN_EGG =
            ITEMS.register("flurrywing_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.FLURRYWING, 0xFFFFFF, 0x7DF9FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SUNBEAM_SPAWN_EGG =
            ITEMS.register("sunbeam_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.SUNBEAM, 0xFB923C, 0xFDE68A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> FROGDUCK_SPAWN_EGG =
            ITEMS.register("frogduck_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.FROGDUCK, 0x14B8A6, 0xF97316, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ZAPKIN_SPAWN_EGG =
            ITEMS.register("zapkin_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.ZAPKIN, 0xFACC15, 0xDC2626, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> FANGWING_SPAWN_EGG =
            ITEMS.register("fangwing_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.FANGWING, 0x3B1E5C, 0xD946EF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> VINER_SPAWN_EGG =
            ITEMS.register("viner_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.VINER, 0x2EA043, 0x38BDF8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PRISM_SENTINEL_SPAWN_EGG =
            ITEMS.register("prism_sentinel_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.PRISM_SENTINEL, 0x3A2F4D, 0x38D9C0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CINDER_RAIDER_SPAWN_EGG =
            ITEMS.register("cinder_raider_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.CINDER_RAIDER, 0x54221C, 0xFF9F38, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ASTRAL_STALKER_SPAWN_EGG =
            ITEMS.register("astral_stalker_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.ASTRAL_STALKER, 0x2B2338, 0xD946EF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PEEPER_SPAWN_EGG =
            ITEMS.register("peeper_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.PEEPER, 0x2B2338, 0x7DF9FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> OCULUS_SLIME_SPAWN_EGG =
            ITEMS.register("oculus_slime_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.OCULUS_SLIME, 0x9C4DCC, 0x7DF9FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LENS_GUARDIAN_SPAWN_EGG =
            ITEMS.register("lens_guardian_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.LENS_GUARDIAN, 0x3A2F4D, 0xD946EF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BLINKY_SPAWN_EGG =
            ITEMS.register("blinky_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.BLINKY, 0x3C305E, 0x7DF9FF, new Item.Properties()));

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
    public static final DeferredItem<BlockItem> STARLIGHT_GRAVITY_TNT_ITEM =
            ITEMS.registerSimpleBlockItem("starlight_gravity_tnt", ArtilianBlocks.STARLIGHT_GRAVITY_TNT);
    public static final DeferredItem<BlockItem> BRIMSTONE_METEOR_TNT_ITEM =
            ITEMS.registerSimpleBlockItem("brimstone_meteor_tnt", ArtilianBlocks.BRIMSTONE_METEOR_TNT);
    public static final DeferredItem<BlockItem> DRAGONFIRE_MEGA_TNT_ITEM =
            ITEMS.registerSimpleBlockItem("dragonfire_mega_tnt", ArtilianBlocks.DRAGONFIRE_MEGA_TNT);

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
    public static final DeferredItem<ArtilianWandItem> PRISM_NOVA_STAFF = ITEMS.register("prism_nova_staff",
            () -> new ArtilianWandItem(WandSpell.PRISM_NOVA,
                    new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<ArtilianWandItem> BRIMSTONE_METEOR_STAFF = ITEMS.register("brimstone_meteor_staff",
            () -> new ArtilianWandItem(WandSpell.BRIMSTONE_METEOR,
                    new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));

    // Raw & refined materials
    public static final DeferredItem<Item> RAW_SUNSTEEL =
            ITEMS.registerSimpleItem("raw_sunsteel");
    public static final DeferredItem<Item> SUNSTEEL_INGOT =
            ITEMS.registerSimpleItem("sunsteel_ingot");
    public static final DeferredItem<Item> STARLIGHT_CRYSTAL =
            ITEMS.registerSimpleItem("starlight_crystal");

    // Sunsteel weapons & tools
    public static final DeferredItem<SwordItem> SUNSTEEL_SWORD = ITEMS.register("sunsteel_sword",
            () -> new SunsteelSwordItem(
                    ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(SwordItem.createAttributes(ArtilianTiers.SUNSTEEL, 3, -2.4F)),
                    "item.artilian.sunsteel_sword.tooltip",
                    ChatFormatting.GOLD));
    public static final DeferredItem<PickaxeItem> SUNSTEEL_PICKAXE = ITEMS.register("sunsteel_pickaxe",
            () -> new SunsteelPickaxeItem(ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(PickaxeItem.createAttributes(ArtilianTiers.SUNSTEEL, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> SUNSTEEL_AXE = ITEMS.register("sunsteel_axe",
            () -> new SunsteelAxeItem(ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(AxeItem.createAttributes(ArtilianTiers.SUNSTEEL, 5.5F, -3.0F))));
    public static final DeferredItem<ShovelItem> SUNSTEEL_SHOVEL = ITEMS.register("sunsteel_shovel",
            () -> new SunsteelShovelItem(ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(ShovelItem.createAttributes(ArtilianTiers.SUNSTEEL, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> SUNSTEEL_HOE = ITEMS.register("sunsteel_hoe",
            () -> new SunsteelHoeItem(ArtilianTiers.SUNSTEEL,
                    new Item.Properties().attributes(HoeItem.createAttributes(ArtilianTiers.SUNSTEEL, -2.0F, -1.0F))));

    // Starlight Crystal weapons & tools
    public static final DeferredItem<SwordItem> STARLIGHT_SWORD = ITEMS.register("starlight_sword",
            () -> new StarlightSwordItem(
                    ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(SwordItem.createAttributes(ArtilianTiers.STARLIGHT, 3, -2.2F)),
                    "item.artilian.starlight_sword.tooltip",
                    ChatFormatting.AQUA));
    public static final DeferredItem<PickaxeItem> STARLIGHT_PICKAXE = ITEMS.register("starlight_pickaxe",
            () -> new StarlightPickaxeItem(ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(PickaxeItem.createAttributes(ArtilianTiers.STARLIGHT, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> STARLIGHT_AXE = ITEMS.register("starlight_axe",
            () -> new StarlightAxeItem(ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(AxeItem.createAttributes(ArtilianTiers.STARLIGHT, 5.0F, -3.0F))));
    public static final DeferredItem<ShovelItem> STARLIGHT_SHOVEL = ITEMS.register("starlight_shovel",
            () -> new StarlightShovelItem(ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(ShovelItem.createAttributes(ArtilianTiers.STARLIGHT, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> STARLIGHT_HOE = ITEMS.register("starlight_hoe",
            () -> new StarlightHoeItem(ArtilianTiers.STARLIGHT,
                    new Item.Properties().attributes(HoeItem.createAttributes(ArtilianTiers.STARLIGHT, -3.0F, 0.0F))));

    // Tier 3 Prismatic Brimstone weapons & tools (boss-forged, fire-resistant)
    public static final DeferredItem<SwordItem> PRISMATIC_SWORD = ITEMS.register("prismatic_sword",
            () -> new PrismaticSwordItem(
                    ArtilianTiers.PRISMATIC,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)
                            .attributes(SwordItem.createAttributes(ArtilianTiers.PRISMATIC, 4, -2.2F)),
                    "item.artilian.prismatic_sword.tooltip",
                    ChatFormatting.LIGHT_PURPLE));
    public static final DeferredItem<PickaxeItem> PRISMATIC_PICKAXE = ITEMS.register("prismatic_pickaxe",
            () -> new PrismaticPickaxeItem(ArtilianTiers.PRISMATIC,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE)
                            .attributes(PickaxeItem.createAttributes(ArtilianTiers.PRISMATIC, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> PRISMATIC_AXE = ITEMS.register("prismatic_axe",
            () -> new PrismaticAxeItem(ArtilianTiers.PRISMATIC,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE)
                            .attributes(AxeItem.createAttributes(ArtilianTiers.PRISMATIC, 5.0F, -3.0F))));
    public static final DeferredItem<ShovelItem> PRISMATIC_SHOVEL = ITEMS.register("prismatic_shovel",
            () -> new PrismaticShovelItem(ArtilianTiers.PRISMATIC,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE)
                            .attributes(ShovelItem.createAttributes(ArtilianTiers.PRISMATIC, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> PRISMATIC_HOE = ITEMS.register("prismatic_hoe",
            () -> new PrismaticHoeItem(ArtilianTiers.PRISMATIC,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE)
                            .attributes(HoeItem.createAttributes(ArtilianTiers.PRISMATIC, -4.0F, 0.0F))));

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

    // Tier 3 Prismatic Brimstone armor
    public static final DeferredItem<ArmorItem> PRISMATIC_HELMET =
            armor("prismatic_helmet", "prismatic", ArtilianArmorMaterials.PRISMATIC, ArmorItem.Type.HELMET, 37, true);
    public static final DeferredItem<ArmorItem> PRISMATIC_CHESTPLATE =
            armor("prismatic_chestplate", "prismatic", ArtilianArmorMaterials.PRISMATIC, ArmorItem.Type.CHESTPLATE, 37, true);
    public static final DeferredItem<ArmorItem> PRISMATIC_LEGGINGS =
            armor("prismatic_leggings", "prismatic", ArtilianArmorMaterials.PRISMATIC, ArmorItem.Type.LEGGINGS, 37, true);
    public static final DeferredItem<ArmorItem> PRISMATIC_BOOTS =
            armor("prismatic_boots", "prismatic", ArtilianArmorMaterials.PRISMATIC, ArmorItem.Type.BOOTS, 37, true);

    private static DeferredItem<ArmorItem> armor(
            String name,
            String set,
            Holder<ArmorMaterial> material,
            ArmorItem.Type type,
            int durabilityMultiplier) {
        return armor(name, set, material, type, durabilityMultiplier, false);
    }

    private static DeferredItem<ArmorItem> armor(
            String name,
            String set,
            Holder<ArmorMaterial> material,
            ArmorItem.Type type,
            int durabilityMultiplier,
            boolean fireResistant) {
        Item.Properties props = new Item.Properties().durability(type.getDurability(durabilityMultiplier));
        if (fireResistant) {
            props = props.fireResistant().rarity(Rarity.EPIC);
        }
        Item.Properties finalProps = props;
        return ITEMS.register(name, () -> new ArtilianArmorItem(material, type, finalProps, set));
    }

    // Boss Framework Stage 3
    public static final DeferredItem<BlockItem> BOSS_ALTAR_ITEM =
            ITEMS.registerSimpleBlockItem("boss_altar", ArtilianBlocks.BOSS_ALTAR);
    /** Summons the test-only Training Golem; kept out of the creative tab like the golem. */
    public static final DeferredItem<BossSummonItem> GOLEM_CORE =
            ITEMS.register("golem_core", () -> new BossSummonItem(ArtilianEntities.TRAINING_GOLEM,
                    new Item.Properties().stacksTo(16)));

    // Crystal Colossus (Phase 3 Stage 2)
    public static final DeferredItem<DeferredSpawnEggItem> CRYSTAL_COLOSSUS_SPAWN_EGG =
            ITEMS.register("crystal_colossus_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.CRYSTAL_COLOSSUS, 0x38D9C0, 0x8B7CFF, new Item.Properties()));
    public static final DeferredItem<BossSummonItem> COLOSSUS_HEART =
            ITEMS.register("colossus_heart", () -> new BossSummonItem(
                    ArtilianEntities.CRYSTAL_COLOSSUS,
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE),
                    "item.artilian.colossus_heart.tooltip",
                    ChatFormatting.AQUA));
    public static final DeferredItem<BossTrophyItem> PRISM_CORE =
            ITEMS.register("prism_core", () -> new BossTrophyItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.EPIC),
                    "item.artilian.prism_core.tooltip",
                    ChatFormatting.LIGHT_PURPLE));

    // Captain Brimstone (Phase 3 Stage 3)
    public static final DeferredItem<DeferredSpawnEggItem> CAPTAIN_BRIMSTONE_SPAWN_EGG =
            ITEMS.register("captain_brimstone_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.CAPTAIN_BRIMSTONE, 0x3A2024, 0xFF7A1F, new Item.Properties()));
    public static final DeferredItem<BossSummonItem> BRIMSTONE_COMPASS =
            ITEMS.register("brimstone_compass", () -> new BossSummonItem(
                    ArtilianEntities.CAPTAIN_BRIMSTONE,
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE),
                    "item.artilian.brimstone_compass.tooltip",
                    ChatFormatting.GOLD));
    public static final DeferredItem<BossTrophyItem> BRIMSTONE_DOUBLOON =
            ITEMS.register("brimstone_doubloon", () -> new BossTrophyItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.EPIC),
                    "item.artilian.brimstone_doubloon.tooltip",
                    ChatFormatting.RED));

    // Phase 4 Stage 1: Eye Realm
    public static final DeferredItem<GazingEyeItem> GAZING_EYE =
            ITEMS.register("gazing_eye", () -> new GazingEyeItem(
                    new Item.Properties().stacksTo(1).durability(64).rarity(Rarity.RARE)));

    // Phase 4 Stage 2: Eye Realm Terrain & Trees
    public static final DeferredItem<BlockItem> EYE_TURF_ITEM =
            ITEMS.registerSimpleBlockItem("eye_turf", ArtilianBlocks.EYE_TURF);
    public static final DeferredItem<BlockItem> GAZE_STONE_ITEM =
            ITEMS.registerSimpleBlockItem("gaze_stone", ArtilianBlocks.GAZE_STONE);
    public static final DeferredItem<BlockItem> GAZE_STONE_STAIRS_ITEM =
            ITEMS.registerSimpleBlockItem("gaze_stone_stairs", ArtilianBlocks.GAZE_STONE_STAIRS);
    public static final DeferredItem<BlockItem> GAZE_STONE_SLAB_ITEM =
            ITEMS.registerSimpleBlockItem("gaze_stone_slab", ArtilianBlocks.GAZE_STONE_SLAB);
    public static final DeferredItem<BlockItem> GAZE_STONE_WALL_ITEM =
            ITEMS.registerSimpleBlockItem("gaze_stone_wall", ArtilianBlocks.GAZE_STONE_WALL);
    public static final DeferredItem<BlockItem> BLINKING_EYE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("blinking_eye_block", ArtilianBlocks.BLINKING_EYE_BLOCK);
    public static final DeferredItem<BlockItem> OCULAR_LOG_ITEM =
            ITEMS.registerSimpleBlockItem("ocular_log", ArtilianBlocks.OCULAR_LOG);
    public static final DeferredItem<BlockItem> OCULAR_PLANKS_ITEM =
            ITEMS.registerSimpleBlockItem("ocular_planks", ArtilianBlocks.OCULAR_PLANKS);
    public static final DeferredItem<BlockItem> OCULAR_STAIRS_ITEM =
            ITEMS.registerSimpleBlockItem("ocular_stairs", ArtilianBlocks.OCULAR_STAIRS);
    public static final DeferredItem<BlockItem> OCULAR_SLAB_ITEM =
            ITEMS.registerSimpleBlockItem("ocular_slab", ArtilianBlocks.OCULAR_SLAB);
    public static final DeferredItem<BlockItem> OCULAR_LEAVES_ITEM =
            ITEMS.registerSimpleBlockItem("ocular_leaves", ArtilianBlocks.OCULAR_LEAVES);
    public static final DeferredItem<BlockItem> OCULAR_SAPLING_ITEM =
            ITEMS.registerSimpleBlockItem("ocular_sapling", ArtilianBlocks.OCULAR_SAPLING);

    // Phase 4 Stage 6: The Great All-Seer
    public static final DeferredItem<DeferredSpawnEggItem> GREAT_ALL_SEER_SPAWN_EGG =
            ITEMS.register("great_all_seer_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.GREAT_ALL_SEER, 0x2B2338, 0xD946EF, new Item.Properties()));
    public static final DeferredItem<BossSummonItem> OCULAR_KEY =
            ITEMS.register("ocular_key", () -> new BossSummonItem(
                    ArtilianEntities.GREAT_ALL_SEER,
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE),
                    "item.artilian.ocular_key.tooltip",
                    ChatFormatting.LIGHT_PURPLE));
    public static final DeferredItem<BossTrophyItem> ALL_SEERS_LENS =
            ITEMS.register("all_seers_lens", () -> new BossTrophyItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.EPIC),
                    "item.artilian.all_seers_lens.tooltip",
                    ChatFormatting.AQUA));

    // Phase 5 Stage 2: Mega-Jungle Worldgen
    public static final DeferredItem<BlockItem> JUNGLE_TURF_ITEM =
            ITEMS.registerSimpleBlockItem("jungle_turf", ArtilianBlocks.JUNGLE_TURF);
    public static final DeferredItem<BlockItem> PRIMAL_BASALT_ITEM =
            ITEMS.registerSimpleBlockItem("primal_basalt", ArtilianBlocks.PRIMAL_BASALT);
    public static final DeferredItem<BlockItem> PRIMAL_BASALT_STAIRS_ITEM =
            ITEMS.registerSimpleBlockItem("primal_basalt_stairs", ArtilianBlocks.PRIMAL_BASALT_STAIRS);
    public static final DeferredItem<BlockItem> PRIMAL_BASALT_SLAB_ITEM =
            ITEMS.registerSimpleBlockItem("primal_basalt_slab", ArtilianBlocks.PRIMAL_BASALT_SLAB);
    public static final DeferredItem<BlockItem> PRIMAL_BASALT_WALL_ITEM =
            ITEMS.registerSimpleBlockItem("primal_basalt_wall", ArtilianBlocks.PRIMAL_BASALT_WALL);
    public static final DeferredItem<BlockItem> AMBER_MOSS_ITEM =
            ITEMS.registerSimpleBlockItem("amber_moss", ArtilianBlocks.AMBER_MOSS);
    public static final DeferredItem<BlockItem> DRAGONWOOD_LOG_ITEM =
            ITEMS.registerSimpleBlockItem("dragonwood_log", ArtilianBlocks.DRAGONWOOD_LOG);
    public static final DeferredItem<BlockItem> DRAGONWOOD_PLANKS_ITEM =
            ITEMS.registerSimpleBlockItem("dragonwood_planks", ArtilianBlocks.DRAGONWOOD_PLANKS);
    public static final DeferredItem<BlockItem> DRAGONWOOD_STAIRS_ITEM =
            ITEMS.registerSimpleBlockItem("dragonwood_stairs", ArtilianBlocks.DRAGONWOOD_STAIRS);
    public static final DeferredItem<BlockItem> DRAGONWOOD_SLAB_ITEM =
            ITEMS.registerSimpleBlockItem("dragonwood_slab", ArtilianBlocks.DRAGONWOOD_SLAB);
    public static final DeferredItem<BlockItem> DRAGONWOOD_LEAVES_ITEM =
            ITEMS.registerSimpleBlockItem("dragonwood_leaves", ArtilianBlocks.DRAGONWOOD_LEAVES);
    public static final DeferredItem<BlockItem> DRAGONWOOD_SAPLING_ITEM =
            ITEMS.registerSimpleBlockItem("dragonwood_sapling", ArtilianBlocks.DRAGONWOOD_SAPLING);

    // Phase 5 Stage 4: First Dragon Species (Canopy Wyvern)
    public static final DeferredItem<Item> DRAGON_SCALE =
            ITEMS.register("dragon_scale", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DeferredSpawnEggItem> CANOPY_WYVERN_SPAWN_EGG =
            ITEMS.register("canopy_wyvern_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.CANOPY_WYVERN, 0x1E4634, 0xF97316, new Item.Properties()));

    // Phase 5 Stage 5: More Dragon Species
    public static final DeferredItem<DeferredSpawnEggItem> AMBER_WYVERN_SPAWN_EGG =
            ITEMS.register("amber_wyvern_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.AMBER_WYVERN, 0x24181C, 0xFDE047, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> STORM_GLIDEWING_SPAWN_EGG =
            ITEMS.register("storm_glidewing_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.STORM_GLIDEWING, 0x1C2644, 0x38D9C0, new Item.Properties()));

    // Phase 5 Stage 6: Hatchable Pet Dragons & Dragon Eggs
    public static final DeferredItem<BlockItem> CANOPY_WYVERN_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("canopy_wyvern_egg", ArtilianBlocks.CANOPY_WYVERN_EGG);
    public static final DeferredItem<BlockItem> AMBER_WYVERN_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("amber_wyvern_egg", ArtilianBlocks.AMBER_WYVERN_EGG);
    public static final DeferredItem<BlockItem> STORM_GLIDEWING_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("storm_glidewing_egg", ArtilianBlocks.STORM_GLIDEWING_EGG);
    public static final DeferredItem<DeferredSpawnEggItem> CANOPY_DRAGONLING_SPAWN_EGG =
            ITEMS.register("canopy_dragonling_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.CANOPY_DRAGONLING, 0x22C55E, 0xF97316, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> AMBER_DRAGONLING_SPAWN_EGG =
            ITEMS.register("amber_dragonling_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.AMBER_DRAGONLING, 0x2E1E22, 0xFB923C, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> STORM_DRAGONLING_SPAWN_EGG =
            ITEMS.register("storm_dragonling_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.STORM_DRAGONLING, 0x1E2848, 0x7DF9FF, new Item.Properties()));

    // Phase 5 Stage 7: Dragonscale Gear
    public static final DeferredItem<SwordItem> DRAGONSCALE_SWORD = ITEMS.register("dragonscale_sword",
            () -> new DragonscaleSwordItem(
                    ArtilianTiers.DRAGONSCALE,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)
                            .attributes(SwordItem.createAttributes(ArtilianTiers.DRAGONSCALE, 4, -2.1F)),
                    "item.artilian.dragonscale_sword.tooltip",
                    ChatFormatting.GREEN));
    public static final DeferredItem<PickaxeItem> DRAGONSCALE_PICKAXE = ITEMS.register("dragonscale_pickaxe",
            () -> new DragonscalePickaxeItem(ArtilianTiers.DRAGONSCALE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE)
                            .attributes(PickaxeItem.createAttributes(ArtilianTiers.DRAGONSCALE, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> DRAGONSCALE_AXE = ITEMS.register("dragonscale_axe",
            () -> new DragonscaleAxeItem(ArtilianTiers.DRAGONSCALE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE)
                            .attributes(AxeItem.createAttributes(ArtilianTiers.DRAGONSCALE, 5.0F, -3.0F))));
    public static final DeferredItem<ShovelItem> DRAGONSCALE_SHOVEL = ITEMS.register("dragonscale_shovel",
            () -> new DragonscaleShovelItem(ArtilianTiers.DRAGONSCALE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE)
                            .attributes(ShovelItem.createAttributes(ArtilianTiers.DRAGONSCALE, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> DRAGONSCALE_HOE = ITEMS.register("dragonscale_hoe",
            () -> new DragonscaleHoeItem(ArtilianTiers.DRAGONSCALE,
                    new Item.Properties().fireResistant().rarity(Rarity.RARE)
                            .attributes(HoeItem.createAttributes(ArtilianTiers.DRAGONSCALE, -4.0F, 0.0F))));
    public static final DeferredItem<ArmorItem> DRAGONSCALE_HELMET =
            armor("dragonscale_helmet", "dragonscale", ArtilianArmorMaterials.DRAGONSCALE, ArmorItem.Type.HELMET, 40, true);
    public static final DeferredItem<ArmorItem> DRAGONSCALE_CHESTPLATE =
            armor("dragonscale_chestplate", "dragonscale", ArtilianArmorMaterials.DRAGONSCALE, ArmorItem.Type.CHESTPLATE, 40, true);
    public static final DeferredItem<ArmorItem> DRAGONSCALE_LEGGINGS =
            armor("dragonscale_leggings", "dragonscale", ArtilianArmorMaterials.DRAGONSCALE, ArmorItem.Type.LEGGINGS, 40, true);
    public static final DeferredItem<ArmorItem> DRAGONSCALE_BOOTS =
            armor("dragonscale_boots", "dragonscale", ArtilianArmorMaterials.DRAGONSCALE, ArmorItem.Type.BOOTS, 40, true);

    // Phase 5 Stage 8: Apex Dragon Boss
    public static final DeferredItem<DeferredSpawnEggItem> EMBERCROWN_SOVEREIGN_SPAWN_EGG =
            ITEMS.register("embercrown_sovereign_spawn_egg", () -> new DeferredSpawnEggItem(
                    ArtilianEntities.EMBERCROWN_SOVEREIGN, 0x1E1420, 0xF97316, new Item.Properties()));
    public static final DeferredItem<BossSummonItem> PRIMAL_DRAGON_CREST =
            ITEMS.register("primal_dragon_crest", () -> new BossSummonItem(
                    ArtilianEntities.EMBERCROWN_SOVEREIGN,
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE),
                    "item.artilian.primal_dragon_crest.tooltip",
                    ChatFormatting.GOLD));
    public static final DeferredItem<BossTrophyItem> SOVEREIGNS_EMBER_HEART =
            ITEMS.register("sovereigns_ember_heart", () -> new BossTrophyItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant(),
                    "item.artilian.sovereigns_ember_heart.tooltip",
                    ChatFormatting.GOLD));

    // Phase 5 Stage 9: Twin-Star Endgame Rewards
    public static final DeferredItem<BossTrophyItem> TWIN_STAR_CORE =
            ITEMS.register("twin_star_core", () -> new BossTrophyItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant(),
                    "item.artilian.twin_star_core.tooltip",
                    ChatFormatting.AQUA));
    public static final DeferredItem<SwordItem> TWIN_STAR_BLADE = ITEMS.register("twin_star_blade",
            () -> new TwinStarBladeItem(
                    ArtilianTiers.TWIN_STAR,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)
                            .attributes(SwordItem.createAttributes(ArtilianTiers.TWIN_STAR, 4, -2.2F)),
                    "item.artilian.twin_star_blade.tooltip",
                    ChatFormatting.AQUA));
    public static final DeferredItem<ArtilianWandItem> TWIN_STAR_STAFF = ITEMS.register("twin_star_staff",
            () -> new TwinStarStaffItem(
                    WandSpell.TWIN_STAR_HARMONY,
                    new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static final DeferredItem<PickaxeItem> TWIN_STAR_PICKAXE = ITEMS.register("twin_star_pickaxe",
            () -> new TwinStarPickaxeItem(ArtilianTiers.TWIN_STAR,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)
                            .attributes(PickaxeItem.createAttributes(ArtilianTiers.TWIN_STAR, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> TWIN_STAR_AXE = ITEMS.register("twin_star_axe",
            () -> new TwinStarAxeItem(ArtilianTiers.TWIN_STAR,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)
                            .attributes(AxeItem.createAttributes(ArtilianTiers.TWIN_STAR, 5.5F, -2.8F))));
    public static final DeferredItem<ShovelItem> TWIN_STAR_SHOVEL = ITEMS.register("twin_star_shovel",
            () -> new TwinStarShovelItem(ArtilianTiers.TWIN_STAR,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)
                            .attributes(ShovelItem.createAttributes(ArtilianTiers.TWIN_STAR, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> TWIN_STAR_HOE = ITEMS.register("twin_star_hoe",
            () -> new TwinStarHoeItem(ArtilianTiers.TWIN_STAR,
                    new Item.Properties().fireResistant().rarity(Rarity.EPIC)
                            .attributes(HoeItem.createAttributes(ArtilianTiers.TWIN_STAR, -5.0F, 0.0F))));
    public static final DeferredItem<ArmorItem> TWIN_STAR_HELMET = ITEMS.register("twin_star_helmet",
            () -> new ArtilianArmorItem(ArtilianArmorMaterials.TWIN_STAR, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(46))
                            .fireResistant().rarity(Rarity.EPIC),
                    "twin_star"));
    public static final DeferredItem<ArmorItem> TWIN_STAR_CHESTPLATE = ITEMS.register("twin_star_chestplate",
            () -> new ArtilianArmorItem(ArtilianArmorMaterials.TWIN_STAR, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(46))
                            .fireResistant().rarity(Rarity.EPIC),
                    "twin_star"));
    public static final DeferredItem<ArmorItem> TWIN_STAR_LEGGINGS = ITEMS.register("twin_star_leggings",
            () -> new ArtilianArmorItem(ArtilianArmorMaterials.TWIN_STAR, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(46))
                            .fireResistant().rarity(Rarity.EPIC),
                    "twin_star"));
    public static final DeferredItem<ArmorItem> TWIN_STAR_BOOTS = ITEMS.register("twin_star_boots",
            () -> new ArtilianArmorItem(ArtilianArmorMaterials.TWIN_STAR, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(46))
                            .fireResistant().rarity(Rarity.EPIC),
                    "twin_star"));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
