package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.enemy.AstralStalkerEntity;
import com.silkpanda.artilian.enemy.CinderRaiderEntity;
import com.silkpanda.artilian.enemy.PrismSentinelEntity;
import com.silkpanda.artilian.enemy.StructureEnemyEntity;
import com.silkpanda.artilian.pet.PetService;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianItems;
import com.silkpanda.artilian.worldgen.ArtilianBiomes;
import com.silkpanda.artilian.worldgen.ArtilianStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Phase 2 Stage 6 Stage 2: Biome-specific structures, chest loot tables, and guardian spawns. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class BiomeStructuresGameTests {

    @GameTest(template = "tnt_arena")
    public static void biomeStructuresAndSetsRegisteredForMatchingBiomes(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        Registry<StructureTemplatePool> pools = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<Structure> structures = registries.registryOrThrow(Registries.STRUCTURE);
        Registry<StructureSet> sets = registries.registryOrThrow(Registries.STRUCTURE_SET);
        Registry<Biome> biomes = registries.registryOrThrow(Registries.BIOME);

        Holder<Biome> crystalWoods = biomes.getHolderOrThrow(ArtilianBiomes.CRYSTAL_CANOPY_WOODS);
        Holder<Biome> emberMeadows = biomes.getHolderOrThrow(ArtilianBiomes.EMBERBLOOM_MEADOWS);
        Holder<Biome> starlightHighlands = biomes.getHolderOrThrow(ArtilianBiomes.STARLIGHT_HIGHLANDS);

        verifyStructureBinding(helper, pools, structures, sets,
                ArtilianStructures.CRYSTAL_GROVE_SHRINE_POOL,
                ArtilianStructures.CRYSTAL_GROVE_SHRINE,
                ArtilianStructures.CRYSTAL_GROVE_SHRINES,
                crystalWoods,
                List.of(emberMeadows, starlightHighlands),
                ArtilianEntities.PRISM_SENTINEL.get());

        verifyStructureBinding(helper, pools, structures, sets,
                ArtilianStructures.EMBERBLOOM_OUTPOST_POOL,
                ArtilianStructures.EMBERBLOOM_OUTPOST,
                ArtilianStructures.EMBERBLOOM_OUTPOSTS,
                emberMeadows,
                List.of(crystalWoods, starlightHighlands),
                ArtilianEntities.CINDER_RAIDER.get());

        verifyStructureBinding(helper, pools, structures, sets,
                ArtilianStructures.STARLIGHT_WATCHTOWER_POOL,
                ArtilianStructures.STARLIGHT_WATCHTOWER,
                ArtilianStructures.STARLIGHT_WATCHTOWERS,
                starlightHighlands,
                List.of(crystalWoods, emberMeadows),
                ArtilianEntities.ASTRAL_STALKER.get());

        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void crystalGroveShrinePlacesChestLootAndPrismSentinels(GameTestHelper helper) {
        verifyTemplatePlacement(
                helper,
                ArtilianMod.id("crystal_grove_shrine"),
                ArtilianStructures.CRYSTAL_GROVE_SHRINE_CHEST,
                ArtilianEntities.PRISM_SENTINEL.get(),
                PrismSentinelEntity.class);
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void emberbloomOutpostPlacesChestLootAndCinderRaiders(GameTestHelper helper) {
        verifyTemplatePlacement(
                helper,
                ArtilianMod.id("emberbloom_outpost"),
                ArtilianStructures.EMBERBLOOM_OUTPOST_CHEST,
                ArtilianEntities.CINDER_RAIDER.get(),
                CinderRaiderEntity.class);
        helper.succeed();
    }

    @GameTest(template = "tnt_arena")
    public static void starlightWatchtowerPlacesChestLootAndAstralStalkers(GameTestHelper helper) {
        verifyTemplatePlacement(
                helper,
                ArtilianMod.id("starlight_watchtower"),
                ArtilianStructures.STARLIGHT_WATCHTOWER_CHEST,
                ArtilianEntities.ASTRAL_STALKER.get(),
                AstralStalkerEntity.class);
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
            List<Holder<Biome>> excludedBiomes,
            EntityType<?> expectedGuardian) {
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

        var monsterOverride = structure.spawnOverrides().get(MobCategory.MONSTER);
        helper.assertTrue(monsterOverride != null
                        && monsterOverride.spawns().unwrap().stream().anyMatch(s -> s.type == expectedGuardian),
                structureKey.location() + " spawn overrides must include " + expectedGuardian);

        StructureSet set = sets.getOrThrow(setKey);
        helper.assertTrue(set.structures().stream().anyMatch(entry -> entry.structure().is(structureKey)),
                setKey.location() + " must reference " + structureKey.location());
    }

    private static <T extends StructureEnemyEntity> void verifyTemplatePlacement(
            GameTestHelper helper,
            ResourceLocation templateId,
            ResourceKey<LootTable> chestLootKey,
            EntityType<T> guardianType,
            Class<T> guardianClass) {
        ServerLevel level = helper.getLevel();
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexStruct");

        StructureTemplate template = level.getStructureManager().get(templateId).orElse(null);
        helper.assertTrue(template != null, "Missing structure template NBT: " + templateId);

        BlockPos origin = helper.absolutePos(new BlockPos(1, 1, 1));
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(false)
                .setFinalizeEntities(true);
        boolean placed = template.placeInWorld(level, origin, origin, settings, level.random, 2);
        helper.assertTrue(placed, "Failed to place structure template: " + templateId);

        BlockPos chestPos = origin.offset(4, 1, 2);
        helper.assertTrue(level.getBlockEntity(chestPos) instanceof ChestBlockEntity,
                "Expected reward chest at " + chestPos + " inside " + templateId);
        ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
        helper.assertTrue(chestLootKey.equals(chest.getLootTable()),
                "Expected chest loot table " + chestLootKey.location() + ", got " + chest.getLootTable());

        chest.unpackLootTable(alex);
        boolean hasTreat = false;
        boolean hasCrystal = false;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.is(ArtilianItems.BIOME_TREAT.get())) {
                hasTreat = true;
            } else if (stack.is(ArtilianItems.CAPTURE_CRYSTAL.get())) {
                hasCrystal = true;
            }
        }
        helper.assertTrue(hasTreat && hasCrystal,
                "Expected " + chestLootKey.location() + " chest to contain Biome Treats and Capture Crystals");

        AABB bounds = new AABB(origin).inflate(10.0);
        List<T> guardians = level.getEntitiesOfClass(guardianClass, bounds);
        helper.assertTrue(guardians.size() >= 2,
                "Expected at least 2 " + guardianType + " guardians inside " + templateId + ", found " + guardians.size());
        for (T guardian : guardians) {
            helper.assertTrue(guardian.isPersistenceRequired(),
                    "Structure guardian must be persistent so it stays at its post");
            helper.assertTrue(PetService.isValidAutonomousBattleTarget(alex, guardian),
                    "Structure guardian must be attackable by Alex's battle pets");
            guardian.discard();
        }
    }
}
