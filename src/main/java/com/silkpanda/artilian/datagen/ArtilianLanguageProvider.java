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
        this.addItem(ArtilianItems.CHESTER_SPAWN_EGG, "Chester Spawn Egg");

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
        this.add("item.artilian.sunsteel_sword.tooltip", "Right-Click (15 Mana) — Solar Slash golden arc + Speed!");
        this.addItem(ArtilianItems.SUNSTEEL_PICKAXE, "Sunsteel Pickaxe");
        this.add("item.artilian.sunsteel_pickaxe.tooltip", "Ore Haste + Shift+Click (10 Mana) Solar Flare light!");
        this.addItem(ArtilianItems.SUNSTEEL_AXE, "Sunsteel Axe");
        this.add("item.artilian.sunsteel_axe.tooltip", "Right-Click (15 Mana) — Sun-Cleave sweep + Slow!");
        this.addItem(ArtilianItems.SUNSTEEL_SHOVEL, "Sunsteel Shovel");
        this.add("item.artilian.sunsteel_shovel.tooltip", "Warm Scoop — Speed boost + pulls nearby drops!");
        this.addItem(ArtilianItems.SUNSTEEL_HOE, "Sunsteel Hoe");
        this.add("item.artilian.sunsteel_hoe.tooltip", "Shift+Click Crop (15 Mana) — 3x3 Sunburst Growth!");

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

        this.addItem(ArtilianItems.PRISMATIC_SWORD, "Prismatic Sword");
        this.add("item.artilian.prismatic_sword.tooltip", "Right-Click (25 Mana) — Prism Beam + Glowing & Embers!");
        this.addItem(ArtilianItems.PRISMATIC_PICKAXE, "Prismatic Pickaxe");
        this.add("item.artilian.prismatic_pickaxe.tooltip", "Prism Smelt + Shift+Click Ore (20 Mana) Vein Pulse!");
        this.addItem(ArtilianItems.PRISMATIC_AXE, "Prismatic Axe");
        this.add("item.artilian.prismatic_axe.tooltip", "Right-Click (25 Mana) — Brimstone Slam shockwave!");
        this.addItem(ArtilianItems.PRISMATIC_SHOVEL, "Prismatic Shovel");
        this.add("item.artilian.prismatic_shovel.tooltip", "Molten Scoop + Shift+Click (20 Mana) 3x3 Excavation!");
        this.addItem(ArtilianItems.PRISMATIC_HOE, "Prismatic Hoe");
        this.add("item.artilian.prismatic_hoe.tooltip", "Shift+Click Crop (25 Mana) — 7x7 Growth & 5x5 Replant!");
        this.addItem(ArtilianItems.PRISMATIC_HELMET, "Prismatic Helmet");
        this.addItem(ArtilianItems.PRISMATIC_CHESTPLATE, "Prismatic Chestplate");
        this.addItem(ArtilianItems.PRISMATIC_LEGGINGS, "Prismatic Leggings");
        this.addItem(ArtilianItems.PRISMATIC_BOOTS, "Prismatic Boots");
        this.add("item.artilian.prismatic_armor.tooltip", "Full Set — Grants Fire Resistance!");

        this.addItem(ArtilianItems.DRAGONSCALE_SWORD, "Dragonscale Sword");
        this.add("item.artilian.dragonscale_sword.tooltip", "Right-Click (30 Mana) — Dragonfire Breath + Scorch & Weaken!");
        this.addItem(ArtilianItems.DRAGONSCALE_PICKAXE, "Dragonscale Pickaxe");
        this.add("item.artilian.dragonscale_pickaxe.tooltip", "Wyvern Forge + Shift+Click (25 Mana) 3x3 Dragon Bore!");
        this.addItem(ArtilianItems.DRAGONSCALE_AXE, "Dragonscale Axe");
        this.add("item.artilian.dragonscale_axe.tooltip", "Right-Click (30 Mana) Wingbeat Roar + Shift+Click Log (20 Mana) Canopy Fell!");
        this.addItem(ArtilianItems.DRAGONSCALE_SHOVEL, "Dragonscale Shovel");
        this.add("item.artilian.dragonscale_shovel.tooltip", "Sovereign Scoop + Shift+Click (25 Mana) 3x3x2 Terra Rake!");
        this.addItem(ArtilianItems.DRAGONSCALE_HOE, "Dragonscale Hoe");
        this.add("item.artilian.dragonscale_hoe.tooltip", "Shift+Click Crop (25 Mana) — 9x9 Growth & 7x7 Replant!");
        this.addItem(ArtilianItems.DRAGONSCALE_HELMET, "Dragonscale Helmet");
        this.addItem(ArtilianItems.DRAGONSCALE_CHESTPLATE, "Dragonscale Chestplate");
        this.addItem(ArtilianItems.DRAGONSCALE_LEGGINGS, "Dragonscale Leggings");
        this.addItem(ArtilianItems.DRAGONSCALE_BOOTS, "Dragonscale Boots");
        this.add("item.artilian.dragonscale_armor.tooltip", "Full Set — Grants Fire Resistance & Strength!");

        this.addEntityType(ArtilianEntities.PRIMED_TNT, "Primed Artilian TNT");
        this.addBlock(ArtilianBlocks.PARTY_TNT, "Party TNT");
        this.add("block.artilian.party_tnt.tooltip", "Safe Blast — Fireworks, speed, and heal!");
        this.addBlock(ArtilianBlocks.BOUNCE_TNT, "Bounce TNT");
        this.add("block.artilian.bounce_tnt.tooltip", "Safe Blast — Big bounce with Slow Falling!");
        this.addBlock(ArtilianBlocks.FROST_NOVA_TNT, "Frost Nova TNT");
        this.add("block.artilian.frost_nova_tnt.tooltip", "Safe Blast — Freezes foes and turns water to ice!");
        this.addBlock(ArtilianBlocks.CLUSTER_QUARRY_TNT, "Cluster Quarry TNT");
        this.add("block.artilian.cluster_quarry_tnt.tooltip", "Quarry Blast — Safe unless enabled in config!");
        this.addBlock(ArtilianBlocks.STARLIGHT_GRAVITY_TNT, "Starlight Gravity TNT");
        this.add("block.artilian.starlight_gravity_tnt.tooltip", "Safe Blast — Pulls foes inward and floats them!");
        this.addBlock(ArtilianBlocks.BRIMSTONE_METEOR_TNT, "Brimstone Meteor TNT");
        this.add("block.artilian.brimstone_meteor_tnt.tooltip", "Meteor Blast — Safe unless enabled in config!");
        this.addBlock(ArtilianBlocks.DRAGONFIRE_MEGA_TNT, "Dragonfire Mega TNT");
        this.add("block.artilian.dragonfire_mega_tnt.tooltip", "Dragonfire Blast — Safe unless enabled in config!");
        this.add("message.artilian.tnt.safe_mode", "Safe Blast Mode is ON — no blocks broken!");

        this.addItem(ArtilianItems.SPARK_WAND, "Spark Wand");
        this.add("item.artilian.spark_wand.tooltip", "20 Mana — Zaps foes with chain starlight!");
        this.addItem(ArtilianItems.HEART_BLOOM_STAFF, "Heart-Bloom Staff");
        this.add("item.artilian.heart_bloom_staff.tooltip", "30 Mana — Heals and shields friends nearby!");
        this.addItem(ArtilianItems.ZEPHYR_DASH_WAND, "Zephyr Dash Wand");
        this.add("item.artilian.zephyr_dash_wand.tooltip", "25 Mana — Sky dash with Slow Falling!");
        this.addItem(ArtilianItems.PRISM_NOVA_STAFF, "Prism Nova Staff");
        this.add("item.artilian.prism_nova_staff.tooltip", "40 Mana — Radiant starlight shockwave!");
        this.addItem(ArtilianItems.BRIMSTONE_METEOR_STAFF, "Brimstone Meteor Staff");
        this.add("item.artilian.brimstone_meteor_staff.tooltip", "45 Mana — Calls a safe fiery meteor!");
        this.add("message.artilian.mana.not_enough", "Need %s Mana (have %s)");
        this.add("hud.artilian.mana", "%s / %s");

        this.addBlock(ArtilianBlocks.CRYSTAL_TURF, "Crystal Turf");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_LOG, "Crystal Canopy Log");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_PLANKS, "Crystal Canopy Planks");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_STAIRS, "Crystal Canopy Stairs");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_SLAB, "Crystal Canopy Slab");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_FENCE, "Crystal Canopy Fence");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_FENCE_GATE, "Crystal Canopy Fence Gate");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_DOOR, "Crystal Canopy Door");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_TRAPDOOR, "Crystal Canopy Trapdoor");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_LEAVES, "Crystal Canopy Leaves");
        this.addBlock(ArtilianBlocks.CRYSTAL_CANOPY_SAPLING, "Crystal Canopy Sapling");
        this.addBlock(ArtilianBlocks.CRYSTAL_BLOOM, "Crystal Bloom");
        this.add("biome.artilian.crystal_canopy_woods", "Crystal Canopy Woods");

        this.addBlock(ArtilianBlocks.EMBER_TURF, "Ember Turf");
        this.addBlock(ArtilianBlocks.EMBERWOOD_LOG, "Emberwood Log");
        this.addBlock(ArtilianBlocks.EMBERWOOD_PLANKS, "Emberwood Planks");
        this.addBlock(ArtilianBlocks.EMBERWOOD_STAIRS, "Emberwood Stairs");
        this.addBlock(ArtilianBlocks.EMBERWOOD_SLAB, "Emberwood Slab");
        this.addBlock(ArtilianBlocks.EMBERWOOD_FENCE, "Emberwood Fence");
        this.addBlock(ArtilianBlocks.EMBERWOOD_FENCE_GATE, "Emberwood Fence Gate");
        this.addBlock(ArtilianBlocks.EMBERWOOD_DOOR, "Emberwood Door");
        this.addBlock(ArtilianBlocks.EMBERWOOD_TRAPDOOR, "Emberwood Trapdoor");
        this.addBlock(ArtilianBlocks.EMBERWOOD_LEAVES, "Emberwood Leaves");
        this.addBlock(ArtilianBlocks.EMBERWOOD_SAPLING, "Emberwood Sapling");
        this.addBlock(ArtilianBlocks.EMBER_BLOOM, "Ember Bloom");
        this.add("biome.artilian.emberbloom_meadows", "Emberbloom Meadows");

        this.addBlock(ArtilianBlocks.STARLIGHT_TURF, "Starlight Turf");
        this.addBlock(ArtilianBlocks.STARLIGHT_SLATE, "Starlight Slate");
        this.addBlock(ArtilianBlocks.STARLIGHT_SLATE_STAIRS, "Starlight Slate Stairs");
        this.addBlock(ArtilianBlocks.STARLIGHT_SLATE_SLAB, "Starlight Slate Slab");
        this.addBlock(ArtilianBlocks.STARLIGHT_SLATE_WALL, "Starlight Slate Wall");
        this.addBlock(ArtilianBlocks.STARLIGHT_BLOSSOM, "Starlight Blossom");
        this.add("biome.artilian.starlight_highlands", "Starlight Highlands");

        // Phase 2 Stage 3: Pet Capture & Expanded Element System
        this.addEntityType(ArtilianEntities.CRYSTAL_FOX, "Crystal Fox");
        // Phase 2 St: Steve's Pets (working names until Steve picks official names)
        this.addEntityType(ArtilianEntities.SHADOW_WISP, "Shadow Wisp");
        this.addEntityType(ArtilianEntities.CINDER_CRAG, "Cinder Crag");
        // Phase 2 St: Alex's Pets
        this.addEntityType(ArtilianEntities.ZAPHOX, "Zaphox");
        this.addEntityType(ArtilianEntities.GALECORN, "Galecorn");
        // Phase 2 Stage 5: Early Dimension-Theme Pets (Dragon Hatchling & Eye-Themed Pet)
        this.addEntityType(ArtilianEntities.EMBER_DRAKE, "Ember Drake");
        this.addEntityType(ArtilianEntities.PEEPKIN, "Peepkin");
        this.addEntityType(ArtilianEntities.PRISMLING, "Prismling");
        this.addEntityType(ArtilianEntities.FLURRYWING, "Flurrywing");
        this.addEntityType(ArtilianEntities.SUNBEAM, "Sunbeam");
        this.addEntityType(ArtilianEntities.FROGDUCK, "Frogduck");
        this.addEntityType(ArtilianEntities.ZAPKIN, "Zapkin");
        this.addItem(ArtilianItems.CRYSTAL_FOX_SPAWN_EGG, "Crystal Fox Spawn Egg");
        this.addItem(ArtilianItems.SHADOW_WISP_SPAWN_EGG, "Shadow Wisp Spawn Egg");
        this.addItem(ArtilianItems.CINDER_CRAG_SPAWN_EGG, "Cinder Crag Spawn Egg");
        this.addItem(ArtilianItems.ZAPHOX_SPAWN_EGG, "Zaphox Spawn Egg");
        this.addItem(ArtilianItems.GALECORN_SPAWN_EGG, "Galecorn Spawn Egg");
        this.addItem(ArtilianItems.EMBER_DRAKE_SPAWN_EGG, "Ember Drake Spawn Egg");
        this.addItem(ArtilianItems.PEEPKIN_SPAWN_EGG, "Peepkin Spawn Egg");
        this.addItem(ArtilianItems.PRISMLING_SPAWN_EGG, "Prismling Spawn Egg");
        this.addItem(ArtilianItems.FLURRYWING_SPAWN_EGG, "Flurrywing Spawn Egg");
        this.addItem(ArtilianItems.SUNBEAM_SPAWN_EGG, "Sunbeam Spawn Egg");
        this.addItem(ArtilianItems.FROGDUCK_SPAWN_EGG, "Frogduck Spawn Egg");
        this.addItem(ArtilianItems.ZAPKIN_SPAWN_EGG, "Zapkin Spawn Egg");
        // Phase 2 Stage 6: Structure Enemies
        this.addEntityType(ArtilianEntities.PRISM_SENTINEL, "Prism Sentinel");
        this.addEntityType(ArtilianEntities.CINDER_RAIDER, "Cinder Raider");
        this.addEntityType(ArtilianEntities.ASTRAL_STALKER, "Astral Stalker");
        // Phase 3 Stage 1: Boss framework test boss
        this.addEntityType(ArtilianEntities.TRAINING_GOLEM, "Training Golem");
        // Phase 3 Stage 1: Boss Altar
        this.addBlock(ArtilianBlocks.BOSS_ALTAR, "Boss Altar");
        this.addItem(ArtilianItems.GOLEM_CORE, "Golem Core");
        this.add("message.artilian.boss_altar.busy", "This altar's boss is still out there!");
        // Phase 3 Stage 2: Crystal Colossus
        this.addEntityType(ArtilianEntities.CRYSTAL_COLOSSUS, "Crystal Colossus");
        this.addItem(ArtilianItems.CRYSTAL_COLOSSUS_SPAWN_EGG, "Crystal Colossus Spawn Egg");
        this.addItem(ArtilianItems.COLOSSUS_HEART, "Colossus Heart");
        this.add("item.artilian.colossus_heart.tooltip", "Use on a Boss Altar to summon the Crystal Colossus!");
        this.addItem(ArtilianItems.PRISM_CORE, "Prism Core");
        this.add("item.artilian.prism_core.tooltip", "Boss Trophy — Forges Tier 3 Crystal gear!");
        this.add("message.artilian.crystal_colossus.overcharged", "The Crystal Colossus shines with Starlight power!");
        // Phase 3 Stage 3: Captain Brimstone
        this.addEntityType(ArtilianEntities.CAPTAIN_BRIMSTONE, "Captain Brimstone");
        this.addItem(ArtilianItems.CAPTAIN_BRIMSTONE_SPAWN_EGG, "Captain Brimstone Spawn Egg");
        this.addItem(ArtilianItems.BRIMSTONE_COMPASS, "Brimstone Compass");
        this.add("item.artilian.brimstone_compass.tooltip", "Use on a Boss Altar to summon Captain Brimstone!");
        this.addItem(ArtilianItems.BRIMSTONE_DOUBLOON, "Brimstone Doubloon");
        this.add("item.artilian.brimstone_doubloon.tooltip", "Boss Trophy — Forges Tier 3 Brimstone gear!");
        this.add("message.artilian.captain_brimstone.broadside", "Captain Brimstone bellows: All hands, fire the broadside!");
        this.addItem(ArtilianItems.PRISM_SENTINEL_SPAWN_EGG, "Prism Sentinel Spawn Egg");
        this.addItem(ArtilianItems.CINDER_RAIDER_SPAWN_EGG, "Cinder Raider Spawn Egg");
        this.addItem(ArtilianItems.ASTRAL_STALKER_SPAWN_EGG, "Astral Stalker Spawn Egg");
        // Phase 4 Stage 3: Eye Realm Enemies
        this.addEntityType(ArtilianEntities.PEEPER, "Peeper");
        this.addEntityType(ArtilianEntities.OCULUS_SLIME, "Oculus Slime");
        this.addEntityType(ArtilianEntities.LENS_GUARDIAN, "Lens Guardian");
        this.addItem(ArtilianItems.PEEPER_SPAWN_EGG, "Peeper Spawn Egg");
        this.addItem(ArtilianItems.OCULUS_SLIME_SPAWN_EGG, "Oculus Slime Spawn Egg");
        this.addItem(ArtilianItems.LENS_GUARDIAN_SPAWN_EGG, "Lens Guardian Spawn Egg");
        // Phase 4 Stage 4: Blinky Pet
        this.addEntityType(ArtilianEntities.BLINKY, "Blinky");
        this.addItem(ArtilianItems.BLINKY_SPAWN_EGG, "Blinky Spawn Egg");
        // Phase 4 Stage 6: The Great All-Seer
        this.addEntityType(ArtilianEntities.GREAT_ALL_SEER, "The Great All-Seer");
        this.addItem(ArtilianItems.GREAT_ALL_SEER_SPAWN_EGG, "Great All-Seer Spawn Egg");
        this.addItem(ArtilianItems.OCULAR_KEY, "Ocular Key");
        this.add("item.artilian.ocular_key.tooltip", "Use on a Boss Altar to summon The Great All-Seer!");
        this.addItem(ArtilianItems.ALL_SEERS_LENS, "All-Seer's Lens");
        this.add("item.artilian.all_seers_lens.tooltip", "Boss Trophy — Relic of The Great All-Seer!");
        this.add("message.artilian.great_all_seer.all_seeing", "The Great All-Seer opens its All-Seeing Gaze!");
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
        this.add("element.artilian.sunsteel", "Sunsteel");
        this.add("element.artilian.all_types", "All Types");

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

        // Phase 4 Stage 1: Eye Realm
        this.addBlock(ArtilianBlocks.GAZING_PORTAL, "Gazing Portal");
        this.addItem(ArtilianItems.GAZING_EYE, "Gazing Eye");
        this.add("tooltip.artilian.gazing_eye", "Right-click a Starlight Slate portal frame to open the Eye Realm!");
        this.add("biome.artilian.eye_realm", "Eye Realm");
        this.add("message.artilian.crystal.resting", "%s is resting (%ss left) — Hold a Biome Treat to wake early!");

        // Phase 4 Stage 2: Eye Realm Terrain & Trees
        this.addBlock(ArtilianBlocks.EYE_TURF, "Eye Turf");
        this.addBlock(ArtilianBlocks.GAZE_STONE, "Gaze Stone");
        this.addBlock(ArtilianBlocks.GAZE_STONE_STAIRS, "Gaze Stone Stairs");
        this.addBlock(ArtilianBlocks.GAZE_STONE_SLAB, "Gaze Stone Slab");
        this.addBlock(ArtilianBlocks.GAZE_STONE_WALL, "Gaze Stone Wall");
        this.addBlock(ArtilianBlocks.BLINKING_EYE_BLOCK, "Blinking Eye Block");
        this.addBlock(ArtilianBlocks.OCULAR_LOG, "Ocular Log");
        this.addBlock(ArtilianBlocks.OCULAR_PLANKS, "Ocular Planks");
        this.addBlock(ArtilianBlocks.OCULAR_STAIRS, "Ocular Stairs");
        this.addBlock(ArtilianBlocks.OCULAR_SLAB, "Ocular Slab");
        this.addBlock(ArtilianBlocks.OCULAR_LEAVES, "Ocular Leaves");
        this.addBlock(ArtilianBlocks.OCULAR_SAPLING, "Ocular Sapling");

        // Phase 5 Stage 1: Giant Dragon Jungle
        this.addBlock(ArtilianBlocks.PRIMAL_PORTAL, "Primal Portal");
        this.add("biome.artilian.dragon_jungle", "Dragon Jungle");

        // Phase 5 Stage 2: Mega-Jungle Worldgen
        this.add("biome.artilian.amber_volcanic_grove", "Amber Volcanic Grove");
        this.addBlock(ArtilianBlocks.JUNGLE_TURF, "Jungle Turf");
        this.addBlock(ArtilianBlocks.PRIMAL_BASALT, "Primal Basalt");
        this.addBlock(ArtilianBlocks.PRIMAL_BASALT_STAIRS, "Primal Basalt Stairs");
        this.addBlock(ArtilianBlocks.PRIMAL_BASALT_SLAB, "Primal Basalt Slab");
        this.addBlock(ArtilianBlocks.PRIMAL_BASALT_WALL, "Primal Basalt Wall");
        this.addBlock(ArtilianBlocks.AMBER_MOSS, "Amber Moss");
        this.addBlock(ArtilianBlocks.DRAGONWOOD_LOG, "Dragonwood Log");
        this.addBlock(ArtilianBlocks.DRAGONWOOD_PLANKS, "Dragonwood Planks");
        this.addBlock(ArtilianBlocks.DRAGONWOOD_STAIRS, "Dragonwood Stairs");
        this.addBlock(ArtilianBlocks.DRAGONWOOD_SLAB, "Dragonwood Slab");
        this.addBlock(ArtilianBlocks.DRAGONWOOD_LEAVES, "Dragonwood Leaves");
        this.addBlock(ArtilianBlocks.DRAGONWOOD_SAPLING, "Dragonwood Sapling");

        // Phase 5 Stage 4–5: Dragon Species
        this.addEntityType(ArtilianEntities.CANOPY_WYVERN, "Canopy Wyvern");
        this.addItem(ArtilianItems.CANOPY_WYVERN_SPAWN_EGG, "Canopy Wyvern Spawn Egg");
        this.addEntityType(ArtilianEntities.AMBER_WYVERN, "Amber Wyvern");
        this.addItem(ArtilianItems.AMBER_WYVERN_SPAWN_EGG, "Amber Wyvern Spawn Egg");
        this.addEntityType(ArtilianEntities.STORM_GLIDEWING, "Storm Glidewing");
        this.addItem(ArtilianItems.STORM_GLIDEWING_SPAWN_EGG, "Storm Glidewing Spawn Egg");
        this.addItem(ArtilianItems.DRAGON_SCALE, "Dragon Scale");

        // Phase 5 Stage 6: Hatchable Pet Dragons
        this.addBlock(ArtilianBlocks.CANOPY_WYVERN_EGG, "Canopy Wyvern Egg");
        this.addBlock(ArtilianBlocks.AMBER_WYVERN_EGG, "Amber Wyvern Egg");
        this.addBlock(ArtilianBlocks.STORM_GLIDEWING_EGG, "Storm Glidewing Egg");
        this.addEntityType(ArtilianEntities.CANOPY_DRAGONLING, "Canopy Dragonling");
        this.addEntityType(ArtilianEntities.AMBER_DRAGONLING, "Amber Dragonling");
        this.addEntityType(ArtilianEntities.STORM_DRAGONLING, "Storm Dragonling");
        this.addItem(ArtilianItems.CANOPY_DRAGONLING_SPAWN_EGG, "Canopy Dragonling Spawn Egg");
        this.addItem(ArtilianItems.AMBER_DRAGONLING_SPAWN_EGG, "Amber Dragonling Spawn Egg");
        this.addItem(ArtilianItems.STORM_DRAGONLING_SPAWN_EGG, "Storm Dragonling Spawn Egg");
        this.add("message.artilian.dragon_egg.hatched", "Your %s hatched! Use a Capture Crystal to bond with it!");

        // Phase 5 Stage 8: Embercrown Sovereign
        this.addEntityType(ArtilianEntities.EMBERCROWN_SOVEREIGN, "Embercrown Sovereign");
        this.addItem(ArtilianItems.EMBERCROWN_SOVEREIGN_SPAWN_EGG, "Embercrown Sovereign Spawn Egg");
        this.addItem(ArtilianItems.PRIMAL_DRAGON_CREST, "Primal Dragon Crest");
        this.add("item.artilian.primal_dragon_crest.tooltip", "Use on a Boss Altar to summon Embercrown Sovereign!");
        this.addItem(ArtilianItems.SOVEREIGNS_EMBER_HEART, "Sovereign's Ember Heart");
        this.add("item.artilian.sovereigns_ember_heart.tooltip", "Boss Trophy — Heart of the Embercrown Sovereign!");
        this.add("message.artilian.embercrown_sovereign.primal_fury", "Embercrown Sovereign unleashes Primal Fury!");

        // Phase 5 Stage 9: Twin-Star Endgame Rewards
        this.addItem(ArtilianItems.TWIN_STAR_CORE, "Twin-Star Core");
        this.add("item.artilian.twin_star_core.tooltip", "Unites the Eye Realm and Dragon Jungle in twin-star harmony!");
        this.addItem(ArtilianItems.TWIN_STAR_BLADE, "Twin-Star Blade");
        this.add("item.artilian.twin_star_blade.tooltip", "Twin-Realm Blade — Ignites, weakens, and reveals foes!");
        this.addItem(ArtilianItems.TWIN_STAR_STAFF, "Twin-Star Harmony Staff");
        this.add("item.artilian.twin_star_staff.tooltip", "50 Mana — Heals allies & pets while blasting nearby foes!");
        this.addItem(ArtilianItems.TWIN_STAR_HELMET, "Twin-Star Helmet");
        this.addItem(ArtilianItems.TWIN_STAR_CHESTPLATE, "Twin-Star Chestplate");
        this.addItem(ArtilianItems.TWIN_STAR_LEGGINGS, "Twin-Star Leggings");
        this.addItem(ArtilianItems.TWIN_STAR_BOOTS, "Twin-Star Boots");
        this.add("item.artilian.twin_star_armor.tooltip", "Full Set — Grants Fire Resistance, Strength, Night Vision & Regeneration!");
    }
}
