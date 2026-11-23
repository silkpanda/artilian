package com.silkpanda.artilian.gametest;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.dimension.EyeRealmDimension;
import com.silkpanda.artilian.enemy.EyeRealmEnemyEntity;
import com.silkpanda.artilian.enemy.LensGuardianEntity;
import com.silkpanda.artilian.enemy.OculusSlimeEntity;
import com.silkpanda.artilian.enemy.PeeperEntity;
import com.silkpanda.artilian.pet.PetService;
import com.silkpanda.artilian.registry.ArtilianEntities;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Phase 4 Stage 5: Eye Realm structures, chest loot tables, and guardian spawns. */
@GameTestHolder(ArtilianMod.MODID)
@PrefixGameTestTemplate(false)
public class EyeRealmStructuresGameTests {
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
    public static void eyeRealmStructuresAndSetsRegisteredForEyeRealmBiomeOnly(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        Registry<StructureTemplatePool> pools = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<Structure> structures = registries.registryOrThrow(Registries.STRUCTURE);
        Registry<StructureSet> sets = registries.registryOrThrow(Registries.STRUCTURE_SET);
        Registry<Biome> biomes = registries.registryOrThrow(Registries.BIOME);

        Holder<Biome> eyeRealm = biomes.getHolderOrThrow(EyeRealmDimension.EYE_REALM_BIOME);
        List<Holder<Biome>> overworldBiomes = List.of(
                biomes.getHolderOrThrow(ArtilianBiomes.CRYSTAL_CANOPY_WOODS),
                biomes.getHolderOrThrow(ArtilianBiomes.EMBERBLOOM_MEADOWS),
                biomes.getHolderOrThrow(ArtilianBiomes.STARLIGHT_HIGHLANDS)
        );

        verifyStructureBinding(helper, pools, structures, sets,
                ArtilianStructures.OCULAR_OBSERVATORY_POOL,
                ArtilianStructures.OCULAR_OBSERVATORY,
                ArtilianStructures.OCULAR_OBSERVATORIES,
                eyeRealm,
                overworldBiomes,
                ArtilianEntities.PEEPER.get());

        verifyStructureBinding(helper, pools, structures, sets,
                ArtilianStructures.GAZING_SANCTUM_POOL,
                ArtilianStructures.GAZING_SANCTUM,
                ArtilianStructures.GAZING_SANCTUMS,
                eyeRealm,
                overworldBiomes,
                ArtilianEntities.LENS_GUARDIAN.get());

        helper.succeed();
    }

    @GameTest(template = "structure_arena")
    public static void ocularObservatoryPlacesChestLootAndGuardians(GameTestHelper helper) {
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexObs");
        AABB bounds = placeAndVerifyChest(
                helper,
                alex,
                ArtilianMod.id("ocular_observatory"),
                ArtilianStructures.OCULAR_OBSERVATORY_CHEST,
                false);

        List<PeeperEntity> peepers = helper.getLevel().getEntitiesOfClass(PeeperEntity.class, bounds);
        List<OculusSlimeEntity> slimes = helper.getLevel().getEntitiesOfClass(OculusSlimeEntity.class, bounds);
        helper.assertTrue(peepers.size() >= 2,
                "Expected at least 2 Peeper guardians in ocular_observatory, found " + peepers.size());
        helper.assertTrue(slimes.size() >= 1,
                "Expected at least 1 Oculus Slime guardian in ocular_observatory, found " + slimes.size());
        verifyGuardians(helper, alex, peepers);
        verifyGuardians(helper, alex, slimes);

        helper.succeed();
    }

    @GameTest(template = "structure_arena")
    public static void gazingSanctumPlacesChestLootAndGuardians(GameTestHelper helper) {
        ServerPlayer alex = ChesterTestSupport.playerInTestArea(helper, "AlexSanc");
        AABB bounds = placeAndVerifyChest(
                helper,
                alex,
                ArtilianMod.id("gazing_sanctum"),
                ArtilianStructures.GAZING_SANCTUM_CHEST,
                true);

        List<LensGuardianEntity> lensGuardians = helper.getLevel().getEntitiesOfClass(LensGuardianEntity.class, bounds);
        List<PeeperEntity> peepers = helper.getLevel().getEntitiesOfClass(PeeperEntity.class, bounds);
        helper.assertTrue(lensGuardians.size() >= 2,
                "Expected at least 2 Lens Guardian defenders in gazing_sanctum, found " + lensGuardians.size());
        helper.assertTrue(peepers.size() >= 1,
                "Expected at least 1 Peeper scout in gazing_sanctum, found " + peepers.size());
        verifyGuardians(helper, alex, lensGuardians);
        verifyGuardians(helper, alex, peepers);

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

    private static AABB placeAndVerifyChest(
            GameTestHelper helper,
            ServerPlayer player,
            ResourceLocation templateId,
            ResourceKey<LootTable> chestLootKey,
            boolean requireStarlightCrystal) {
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
        for (int dx = 0; dx < size.getX(); dx++) {
            for (int dy = 0; dy < size.getY(); dy++) {
                for (int dz = 0; dz < size.getZ(); dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    Block block = level.getBlockState(pos).getBlock();
                    helper.assertFalse(BANNED_TEMPLATE_BLOCKS.contains(block),
                            "Structure template " + templateId + " contains banned block " + block + " at " + pos);
                    if (level.getBlockEntity(pos) instanceof ChestBlockEntity chestEntity) {
                        chests.add(chestEntity);
                    }
                }
            }
        }
        helper.assertTrue(chests.size() == 1,
                "Expected exactly 1 reward chest inside " + templateId + ", found " + chests.size());
        ChestBlockEntity chest = chests.get(0);
        helper.assertTrue(chestLootKey.equals(chest.getLootTable()),
                "Expected chest loot table " + chestLootKey.location() + ", got " + chest.getLootTable());

        chest.unpackLootTable(player);
        boolean hasTreat = false;
        boolean hasCaptureCrystal = false;
        boolean hasStarlightCrystal = false;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.is(ArtilianItems.BIOME_TREAT.get())) {
                hasTreat = true;
            } else if (stack.is(ArtilianItems.CAPTURE_CRYSTAL.get())) {
                hasCaptureCrystal = true;
            } else if (stack.is(ArtilianItems.STARLIGHT_CRYSTAL.get())) {
                hasStarlightCrystal = true;
            }
        }
        helper.assertTrue(hasTreat && hasCaptureCrystal,
                "Expected " + chestLootKey.location() + " chest to contain Biome Treats and Capture Crystals");
        if (requireStarlightCrystal) {
            helper.assertTrue(hasStarlightCrystal,
                    "Expected " + chestLootKey.location() + " chest to contain Starlight Crystals");
        }

        return new AABB(
                origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + size.getX(), origin.getY() + size.getY(), origin.getZ() + size.getZ()
        ).inflate(2.0);
    }

    private static <T extends EyeRealmEnemyEntity> void verifyGuardians(
            GameTestHelper helper,
            ServerPlayer player,
            List<T> guardians) {
        for (T guardian : guardians) {
            helper.assertTrue(guardian.isPersistenceRequired(),
                    "Eye Realm structure guardian must be persistent so it stays at its post");
            helper.assertTrue(PetService.isValidAutonomousBattleTarget(player, guardian),
                    "Eye Realm structure guardian must be attackable by Alex's battle pets");
            guardian.discard();
        }
    }
}
