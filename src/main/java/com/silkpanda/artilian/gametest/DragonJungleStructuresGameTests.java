package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.DragonJungleDimension;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.worldgen.ArtilianBiomes;
import com.silkpanda.artilian.worldgen.ArtilianStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Phase 5 Stage 3: Dragon Jungle structures (Dragon Roost and Amber Temple) and chest loot tables. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class DragonJungleStructuresGameTests {
    private static final Set<Block> BANNED_TEMPLATE_BLOCKS = Set.of(
            Blocks.LAVA,
            Blocks.FIRE,
            Blocks.SOUL_FIRE,
            Blocks.TNT,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.SPAWNER,
            Blocks.STRUCTURE_BLOCK
    );

    @GameTest(template = "structure_arena")
    public static void dragonJungleStructuresAndSetsRegisteredForDragonJungleBiomesOnly(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        Registry<StructureTemplatePool> pools = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<Structure> structures = registries.registryOrThrow(Registries.STRUCTURE);
        Registry<StructureSet> sets = registries.registryOrThrow(Registries.STRUCTURE_SET);
        Registry<Biome> biomes = registries.registryOrThrow(Registries.BIOME);

        Holder<Biome> dragonJungle = biomes.getHolderOrThrow(DragonJungleDimension.DRAGON_JUNGLE_BIOME);
        Holder<Biome> amberGrove = biomes.getHolderOrThrow(DragonJungleDimension.AMBER_GROVE_BIOME);
        Holder<Biome> eyeRealm = biomes.getHolderOrThrow(EyeRealmDimension.EYE_REALM_BIOME);
        Holder<Biome> crystalWoods = biomes.getHolderOrThrow(ArtilianBiomes.CRYSTAL_CANOPY_WOODS);
        Holder<Biome> emberMeadows = biomes.getHolderOrThrow(ArtilianBiomes.EMBERBLOOM_MEADOWS);
        Holder<Biome> starlightHighlands = biomes.getHolderOrThrow(ArtilianBiomes.STARLIGHT_HIGHLANDS);

        verifyStructureBinding(helper, pools, structures, sets,
                ArtilianStructures.DRAGON_ROOST_POOL,
                ArtilianStructures.DRAGON_ROOST,
                ArtilianStructures.DRAGON_ROOSTS,
                dragonJungle,
                List.of(amberGrove, eyeRealm, crystalWoods, emberMeadows, starlightHighlands));

        verifyStructureBinding(helper, pools, structures, sets,
                ArtilianStructures.AMBER_TEMPLE_POOL,
                ArtilianStructures.AMBER_TEMPLE,
                ArtilianStructures.AMBER_TEMPLES,
                amberGrove,
                List.of(dragonJungle, eyeRealm, crystalWoods, emberMeadows, starlightHighlands));

        helper.succeed();
    }

    @GameTest(template = "structure_arena")
    public static void dragonRoostPlacesTemplateWithoutBannedBlocksAndUnpacksChestLoot(GameTestHelper helper) {
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "SteveRoost");
        placeAndVerifyChest(
                helper,
                steve,
                ArtilianMod.id("dragon_roost"),
                ArtilianStructures.DRAGON_ROOST_CHEST,
                false);
        helper.succeed();
    }

    @GameTest(template = "structure_arena")
    public static void amberTemplePlacesTemplateWithoutBannedBlocksAndUnpacksChestLoot(GameTestHelper helper) {
        ServerPlayer steve = ChesterTestSupport.playerInTestArea(helper, "SteveTemple");
        placeAndVerifyChest(
                helper,
                steve,
                ArtilianMod.id("amber_temple"),
                ArtilianStructures.AMBER_TEMPLE_CHEST,
                true);
        helper.succeed();
    }

    private static void verifyStructureBinding(
            GameTestHelper helper,
            Registry<StructureTemplatePool> pools,
            Registry<Structure> structures,
            Registry<StructureSet> sets,
            ResourceKey<StructureTemplatePool> poolKey,
            ResourceKey<Structure> structureKey,
            ResourceKey<StructureSet> setKey,
            Holder<Biome> expectedBiome,
            List<Holder<Biome>> excludedBiomes) {
        helper.assertTrue(pools.containsKey(poolKey), "Missing template pool: " + poolKey.location());
        helper.assertTrue(structures.containsKey(structureKey), "Missing structure: " + structureKey.location());
        helper.assertTrue(sets.containsKey(setKey), "Missing structure set: " + setKey.location());

        Structure structure = structures.getOrThrow(structureKey);
        helper.assertTrue(structure.biomes().contains(expectedBiome),
                structureKey.location() + " must generate in " + expectedBiome.getKey());
        for (Holder<Biome> other : excludedBiomes) {
            helper.assertFalse(structure.biomes().contains(other),
                    structureKey.location() + " must NOT generate in " + other.getKey());
        }

        StructureSet set = sets.getOrThrow(setKey);
        helper.assertTrue(set.structures().stream().anyMatch(entry -> entry.structure().is(structureKey)),
                setKey.location() + " must reference " + structureKey.location());
    }

    private static void placeAndVerifyChest(
            GameTestHelper helper,
            ServerPlayer player,
            ResourceLocation templateId,
            ResourceKey<LootTable> chestLootKey,
            boolean requireAmberMossInChest) {
        ServerLevel level = helper.getLevel();
        StructureTemplate template = level.getStructureManager().get(templateId).orElse(null);
        helper.assertTrue(template != null, "Missing structure template NBT: " + templateId);

        Vec3i size = template.getSize();
        helper.assertTrue(size.getX() <= 16 && size.getY() <= 20 && size.getZ() <= 16,
                "Structure template " + templateId + " exceeds 16x20x16 size limit: " + size);

        BlockPos origin = helper.absolutePos(new BlockPos(1, 1, 1));
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(false)
                .setFinalizeEntities(true);
        boolean placed = template.placeInWorld(level, origin, origin, settings, level.random, 2);
        helper.assertTrue(placed, "Failed to place structure template: " + templateId);

        List<ChestBlockEntity> chests = new ArrayList<>();
        boolean hasBasalt = false;
        boolean hasDragonwoodLog = false;
        boolean hasAmberMoss = false;
        for (int dx = 0; dx < size.getX(); dx++) {
            for (int dy = 0; dy < size.getY(); dy++) {
                for (int dz = 0; dz < size.getZ(); dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    Block block = level.getBlockState(pos).getBlock();
                    helper.assertFalse(BANNED_TEMPLATE_BLOCKS.contains(block),
                            "Structure template " + templateId + " contains banned block " + block + " at " + pos);
                    if (block == ArtilianBlocks.PRIMAL_BASALT.get()) {
                        hasBasalt = true;
                    } else if (block == ArtilianBlocks.DRAGONWOOD_LOG.get()) {
                        hasDragonwoodLog = true;
                    } else if (block == ArtilianBlocks.AMBER_MOSS.get()) {
                        hasAmberMoss = true;
                    }
                    if (level.getBlockEntity(pos) instanceof ChestBlockEntity chestEntity) {
                        chests.add(chestEntity);
                    }
                }
            }
        }
        helper.assertTrue(hasBasalt && hasDragonwoodLog && hasAmberMoss,
                "Expected Dragon Jungle signature blocks in " + templateId);
        helper.assertTrue(chests.size() == 1,
                "Expected exactly 1 reward chest inside " + templateId + ", found " + chests.size());
        ChestBlockEntity chest = chests.get(0);
        helper.assertTrue(chestLootKey.equals(chest.getLootTable()),
                "Expected chest loot table " + chestLootKey.location() + ", got " + chest.getLootTable());

        chest.unpackLootTable(player);
        boolean hasTreat = false;
        boolean hasCaptureCrystal = false;
        boolean hasAmberMossLoot = false;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.is(ArtilianItems.BIOME_TREAT.get())) {
                hasTreat = true;
            } else if (stack.is(ArtilianItems.CAPTURE_CRYSTAL.get())) {
                hasCaptureCrystal = true;
            } else if (stack.is(ArtilianBlocks.AMBER_MOSS.get().asItem())) {
                hasAmberMossLoot = true;
            }
        }
        helper.assertTrue(hasTreat && hasCaptureCrystal,
                "Expected " + chestLootKey.location() + " chest to contain Biome Treats and Capture Crystals");
        if (requireAmberMossInChest) {
            helper.assertTrue(hasAmberMossLoot,
                    "Expected " + chestLootKey.location() + " chest to contain Amber Moss");
        }
    }
}
