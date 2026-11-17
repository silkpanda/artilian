package com.silkpanda.artilian.worldgen;

import com.mojang.datafixers.util.Pair;
import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianEntities;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.Map;

/** Phase 2 Stage 6 biome-specific Overworld structures, template pools, and chest loot keys. */
public final class ArtilianStructures {
    private ArtilianStructures() {}

    public static final ResourceKey<LootTable> CRYSTAL_GROVE_SHRINE_CHEST =
            ResourceKey.create(Registries.LOOT_TABLE, ArtilianMod.id("chests/crystal_grove_shrine"));
    public static final ResourceKey<LootTable> EMBERBLOOM_OUTPOST_CHEST =
            ResourceKey.create(Registries.LOOT_TABLE, ArtilianMod.id("chests/emberbloom_outpost"));
    public static final ResourceKey<LootTable> STARLIGHT_WATCHTOWER_CHEST =
            ResourceKey.create(Registries.LOOT_TABLE, ArtilianMod.id("chests/starlight_watchtower"));

    public static final ResourceKey<StructureTemplatePool> CRYSTAL_GROVE_SHRINE_POOL =
            ResourceKey.create(Registries.TEMPLATE_POOL, ArtilianMod.id("crystal_grove_shrine"));
    public static final ResourceKey<StructureTemplatePool> EMBERBLOOM_OUTPOST_POOL =
            ResourceKey.create(Registries.TEMPLATE_POOL, ArtilianMod.id("emberbloom_outpost"));
    public static final ResourceKey<StructureTemplatePool> STARLIGHT_WATCHTOWER_POOL =
            ResourceKey.create(Registries.TEMPLATE_POOL, ArtilianMod.id("starlight_watchtower"));

    public static final ResourceKey<Structure> CRYSTAL_GROVE_SHRINE =
            ResourceKey.create(Registries.STRUCTURE, ArtilianMod.id("crystal_grove_shrine"));
    public static final ResourceKey<Structure> EMBERBLOOM_OUTPOST =
            ResourceKey.create(Registries.STRUCTURE, ArtilianMod.id("emberbloom_outpost"));
    public static final ResourceKey<Structure> STARLIGHT_WATCHTOWER =
            ResourceKey.create(Registries.STRUCTURE, ArtilianMod.id("starlight_watchtower"));

    public static final ResourceKey<StructureSet> CRYSTAL_GROVE_SHRINES =
            ResourceKey.create(Registries.STRUCTURE_SET, ArtilianMod.id("crystal_grove_shrines"));
    public static final ResourceKey<StructureSet> EMBERBLOOM_OUTPOSTS =
            ResourceKey.create(Registries.STRUCTURE_SET, ArtilianMod.id("emberbloom_outposts"));
    public static final ResourceKey<StructureSet> STARLIGHT_WATCHTOWERS =
            ResourceKey.create(Registries.STRUCTURE_SET, ArtilianMod.id("starlight_watchtowers"));

    public static void bootstrapTemplatePools(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        var emptyFallback = pools.getOrThrow(Pools.EMPTY);

        registerSinglePool(context, CRYSTAL_GROVE_SHRINE_POOL, emptyFallback, "artilian:crystal_grove_shrine");
        registerSinglePool(context, EMBERBLOOM_OUTPOST_POOL, emptyFallback, "artilian:emberbloom_outpost");
        registerSinglePool(context, STARLIGHT_WATCHTOWER_POOL, emptyFallback, "artilian:starlight_watchtower");
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        context.register(CRYSTAL_GROVE_SHRINE, surfaceStructure(
                biomes, pools, ArtilianTags.Biomes.HAS_CRYSTAL_GROVE_SHRINE,
                CRYSTAL_GROVE_SHRINE_POOL, ArtilianEntities.PRISM_SENTINEL.get()));
        context.register(EMBERBLOOM_OUTPOST, surfaceStructure(
                biomes, pools, ArtilianTags.Biomes.HAS_EMBERBLOOM_OUTPOST,
                EMBERBLOOM_OUTPOST_POOL, ArtilianEntities.CINDER_RAIDER.get()));
        context.register(STARLIGHT_WATCHTOWER, surfaceStructure(
                biomes, pools, ArtilianTags.Biomes.HAS_STARLIGHT_WATCHTOWER,
                STARLIGHT_WATCHTOWER_POOL, ArtilianEntities.ASTRAL_STALKER.get()));
    }

    public static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);

        context.register(CRYSTAL_GROVE_SHRINES, spreadSet(structures, CRYSTAL_GROVE_SHRINE, 61482011));
        context.register(EMBERBLOOM_OUTPOSTS, spreadSet(structures, EMBERBLOOM_OUTPOST, 72593122));
        context.register(STARLIGHT_WATCHTOWERS, spreadSet(structures, STARLIGHT_WATCHTOWER, 83604233));
    }

    private static void registerSinglePool(
            BootstrapContext<StructureTemplatePool> context,
            ResourceKey<StructureTemplatePool> key,
            net.minecraft.core.Holder<StructureTemplatePool> fallback,
            String templateId) {
        context.register(key, new StructureTemplatePool(
                fallback,
                List.of(Pair.of(StructurePoolElement.single(templateId), 1)),
                StructureTemplatePool.Projection.RIGID));
    }

    private static Structure surfaceStructure(
            HolderGetter<Biome> biomes,
            HolderGetter<StructureTemplatePool> pools,
            TagKey<Biome> biomeTag,
            ResourceKey<StructureTemplatePool> startPool,
            EntityType<?> guardian) {
        Map<MobCategory, StructureSpawnOverride> overrides = Map.of(
                MobCategory.MONSTER,
                new StructureSpawnOverride(
                        StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                        WeightedRandomList.create(new MobSpawnSettings.SpawnerData(guardian, 10, 1, 2)))
        );
        Structure.StructureSettings settings = new Structure.StructureSettings.Builder(biomes.getOrThrow(biomeTag))
                .spawnOverrides(overrides)
                .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                .build();
        return new JigsawStructure(
                settings,
                pools.getOrThrow(startPool),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG);
    }

    private static StructureSet spreadSet(
            HolderGetter<Structure> structures,
            ResourceKey<Structure> structureKey,
            int salt) {
        return new StructureSet(
                structures.getOrThrow(structureKey),
                new RandomSpreadStructurePlacement(18, 8, RandomSpreadType.LINEAR, salt));
    }
}
