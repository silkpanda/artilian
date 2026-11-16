package com.silkpanda.artilian.datagen;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/** Every word the kids read in game. Keep it short: written for early readers. */
public class ArtilianLanguageProvider extends LanguageProvider {
    public ArtilianLanguageProvider(PackOutput output) {
        super(output, ArtilianMod.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("itemGroup.artilian", "Adventures in Artilian");

        this.addEntityType(ArtilianEntities.CHESTER, "Chester");
        this.add("entity.artilian.chester.owned", "%s's Chester");
        this.add("message.artilian.chester.not_yours", "This is %s's Chester");
        this.add("message.artilian.chester.no_owner", "This Chester has no owner");
        this.add("message.artilian.chester.sleeping", "Chester is sleeping. Shh!");
        this.add("message.artilian.chester.awake", "Chester is awake!");
        this.add("message.artilian.chester.lost", "Chester got lost! Blow your Chester Whistle to call him back.");

        this.addItem(ArtilianItems.CHESTER_WHISTLE, "Chester Whistle");
        this.add("item.artilian.chester_whistle.tooltip", "Calls Chester");

        this.addBlock(ArtilianBlocks.SUNSTEEL_ORE, "Sunsteel Ore");
        this.addBlock(ArtilianBlocks.DEEPSLATE_SUNSTEEL_ORE, "Deepslate Sunsteel Ore");
        this.addBlock(ArtilianBlocks.SUNSTEEL_BLOCK, "Block of Sunsteel");
        this.addBlock(ArtilianBlocks.STARLIGHT_CRYSTAL_ORE, "Starlight Crystal Ore");
        this.addBlock(ArtilianBlocks.DEEPSLATE_STARLIGHT_CRYSTAL_ORE, "Deepslate Starlight Crystal Ore");
        this.addBlock(ArtilianBlocks.STARLIGHT_CRYSTAL_BLOCK, "Block of Starlight Crystal");

        this.addItem(ArtilianItems.RAW_SUNSTEEL, "Raw Sunsteel");
        this.addItem(ArtilianItems.SUNSTEEL_INGOT, "Sunsteel Ingot");
        this.addItem(ArtilianItems.STARLIGHT_CRYSTAL, "Starlight Crystal");

        this.addItem(ArtilianItems.SUNSTEEL_SWORD, "Sunsteel Sword");
        this.add("item.artilian.sunsteel_sword.tooltip", "Sturdy Sunsteel blade");
        this.addItem(ArtilianItems.SUNSTEEL_PICKAXE, "Sunsteel Pickaxe");
        this.addItem(ArtilianItems.SUNSTEEL_AXE, "Sunsteel Axe");
        this.addItem(ArtilianItems.SUNSTEEL_SHOVEL, "Sunsteel Shovel");
        this.addItem(ArtilianItems.SUNSTEEL_HOE, "Sunsteel Hoe");

        this.addItem(ArtilianItems.STARLIGHT_SWORD, "Starlight Sword");
        this.add("item.artilian.starlight_sword.tooltip", "Swift Starlight blade");
        this.addItem(ArtilianItems.STARLIGHT_PICKAXE, "Starlight Pickaxe");
        this.addItem(ArtilianItems.STARLIGHT_AXE, "Starlight Axe");
        this.addItem(ArtilianItems.STARLIGHT_SHOVEL, "Starlight Shovel");
        this.addItem(ArtilianItems.STARLIGHT_HOE, "Starlight Hoe");

        this.addItem(ArtilianItems.SUNSTEEL_HELMET, "Sunsteel Helmet");
        this.addItem(ArtilianItems.SUNSTEEL_CHESTPLATE, "Sunsteel Chestplate");
        this.addItem(ArtilianItems.SUNSTEEL_LEGGINGS, "Sunsteel Leggings");
        this.addItem(ArtilianItems.SUNSTEEL_BOOTS, "Sunsteel Boots");

        this.addItem(ArtilianItems.STARLIGHT_HELMET, "Starlight Helmet");
        this.addItem(ArtilianItems.STARLIGHT_CHESTPLATE, "Starlight Chestplate");
        this.addItem(ArtilianItems.STARLIGHT_LEGGINGS, "Starlight Leggings");
        this.addItem(ArtilianItems.STARLIGHT_BOOTS, "Starlight Boots");

        this.addEntityType(ArtilianEntities.PRIMED_TNT, "Primed Artilian TNT");
        this.addBlock(ArtilianBlocks.PARTY_TNT, "Party TNT");
        this.add("block.artilian.party_tnt.tooltip", "Safe Blast — Fireworks, speed, and heal!");
        this.addBlock(ArtilianBlocks.BOUNCE_TNT, "Bounce TNT");
        this.add("block.artilian.bounce_tnt.tooltip", "Safe Blast — Big bounce with Slow Falling!");
        this.addBlock(ArtilianBlocks.FROST_NOVA_TNT, "Frost Nova TNT");
        this.add("block.artilian.frost_nova_tnt.tooltip", "Safe Blast — Freezes foes and turns water to ice!");
        this.addBlock(ArtilianBlocks.CLUSTER_QUARRY_TNT, "Cluster Quarry TNT");
        this.add("block.artilian.cluster_quarry_tnt.tooltip", "Quarry Blast — Safe unless enabled in config!");
        this.add("message.artilian.tnt.safe_mode", "Safe Blast Mode is ON — no blocks broken!");

        this.addItem(ArtilianItems.SPARK_WAND, "Spark Wand");
        this.add("item.artilian.spark_wand.tooltip", "20 Mana — Zaps foes with chain starlight!");
        this.addItem(ArtilianItems.HEART_BLOOM_STAFF, "Heart-Bloom Staff");
        this.add("item.artilian.heart_bloom_staff.tooltip", "30 Mana — Heals and shields friends nearby!");
        this.addItem(ArtilianItems.ZEPHYR_DASH_WAND, "Zephyr Dash Wand");
        this.add("item.artilian.zephyr_dash_wand.tooltip", "25 Mana — Sky dash with Slow Falling!");
        this.add("message.artilian.mana.not_enough", "Need %s Mana (have %s)");
        this.add("hud.artilian.mana", "%s / %s");

        this.addBlock(ArtilianBlocks.CRYSTAL_TURF, "Crystal Turf");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_LOG, "Crystal Canopy Log");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_PLANKS, "Crystal Canopy Planks");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_LEAVES, "Crystal Canopy Leaves");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_SAPLING, "Crystal Canopy Sapling");
        this.addBlock(ArtilianBlocks.CRYSTAL_BLOOM, "Crystal Bloom");
        this.add("biome.artilian.crystal_canopy_woods", "Crystal Canopy Woods");

        this.addBlock(ArtilianBlocks.EMBER_TURF, "Ember Turf");
        this.addBlock(ArtilianBlocks.EMBERWOOD_LOG, "Emberwood Log");
        this.addBlock(ArtilianBlocks.EMBERWOOD_PLANKS, "Emberwood Planks");
        this.addBlock(ArtilianBlocks.EMBERWOOD_LEAVES, "Emberwood Leaves");
        this.addBlock(ArtilianBlocks.EMBERWOOD_SAPLING, "Emberwood Sapling");
        this.addBlock(ArtilianBlocks.EMBER_BLOOM, "Ember Bloom");
        this.add("biome.artilian.emberbloom_meadows", "Emberbloom Meadows");

        this.addBlock(ArtilianBlocks.STARLIGHT_TURF, "Starlight Turf");
        this.addBlock(ArtilianBlocks.STARLIGHT_SLATE, "Starlight Slate");
        this.addBlock(ArtilianBlocks.STARLIGHT_BLOSSOM, "Starlight Blossom");
        this.add("biome.artilian.starlight_highlands", "Starlight Highlands");

        // Phase 2 Stage 3: Pet Capture & Expanded Element System
        this.addEntityType(ArtilianEntities.CRYSTAL_FOX, "Crystal Fox");
        // Phase 2 St: Bob's Pets (working names until Bob picks official names)
        this.addEntityType(ArtilianEntities.SHADOW_WISP, "Shadow Wisp");
        this.addEntityType(ArtilianEntities.CINDER_CRAG, "Cinder Crag");
        this.addItem(ArtilianItems.CAPTURE_CRYSTAL, "Capture Crystal");
        this.add("item.artilian.capture_crystal.filled", "Capture Crystal (%s)");
        this.add("item.artilian.capture_crystal.tooltip", "Captures Biome Pets — Right-click a wild pet!");
        this.addItem(ArtilianItems.BIOME_TREAT, "Biome Treat");
        this.add("item.artilian.biome_treat.tooltip", "Befriends wild pets & heals tamed pets!");

        this.add("tooltip.artilian.pet.elements", "Element: %s");
        this.add("tooltip.artilian.crystal.ready", "Ready (%s/%s HP) — Owner: %s");
        this.add("tooltip.artilian.crystal.summoned", "Summoned — Right-click to recall (%s)");
        this.add("tooltip.artilian.crystal.fainted", "Resting safely — Hold Biome Treat to wake (%s)");

        this.add("element.artilian.fire", "Fire");
        this.add("element.artilian.water", "Water");
        this.add("element.artilian.nature", "Nature");
        this.add("element.artilian.electric", "Electric");
        this.add("element.artilian.ice", "Ice");
        this.add("element.artilian.air", "Air");
        this.add("element.artilian.earth", "Earth");
        this.add("element.artilian.crystal", "Crystal");
        this.add("element.artilian.starlight", "Starlight");
        this.add("element.artilian.shadow", "Shadow");
        this.add("element.artilian.dragon", "Dragon");

        this.add("message.artilian.pet.affinity", "%s likes you! (%s%% befriended)");
        this.add("message.artilian.pet.befriended", "%s is befriended! Use a Capture Crystal to catch it!");
        this.add("message.artilian.pet.treat_healed", "%s loved the Biome Treat!");
        this.add("message.artilian.pet.sitting", "%s is sitting");
        this.add("message.artilian.pet.following", "%s is following you!");
        this.add("message.artilian.pet.not_yours", "This %s belongs to %s");
        this.add("message.artilian.pet.fainted", "Your %s fainted and returned safely to its Capture Crystal!");

        this.add("message.artilian.crystal.empty_hint", "Right-click a wild Biome Pet to capture it!");
        this.add("message.artilian.crystal.already_full", "This Capture Crystal already holds a pet!");
        this.add("message.artilian.crystal.use_bound_crystal", "Use %s's own Capture Crystal to recall it!");
        this.add("message.artilian.crystal.almost", "Almost! Feed a Biome Treat to befriend %s!");
        this.add("message.artilian.crystal.captured", "Captured %s!");
        this.add("message.artilian.crystal.not_yours", "This Capture Crystal belongs to %s");
        this.add("message.artilian.crystal.summoned", "Go, %s!");
        this.add("message.artilian.crystal.recalled", "Recalled %s!");
        this.add("message.artilian.crystal.resting", "%s is resting (%ss left) — Hold a Biome Treat to wake early!");
    }
}
