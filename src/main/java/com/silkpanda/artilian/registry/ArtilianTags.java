package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

/** Custom Artilian item, block, entity, and biome tags. */
public final class ArtilianTags {
    private ArtilianTags() {}

    public static final class Items {
        public static final TagKey<Item> BIOME_BLOOMS = ItemTags.create(ArtilianMod.id("biome_blooms"));

        private Items() {}
    }

    public static final class EntityTypes {
        public static final TagKey<EntityType<?>> STRUCTURE_ENEMIES =
                TagKey.create(Registries.ENTITY_TYPE, ArtilianMod.id("structure_enemies"));
        public static final TagKey<EntityType<?>> BOSSES =
                TagKey.create(Registries.ENTITY_TYPE, ArtilianMod.id("bosses"));

        private EntityTypes() {}
    }

    public static final class Biomes {
        public static final TagKey<Biome> HAS_CRYSTAL_GROVE_SHRINE =
                TagKey.create(Registries.BIOME, ArtilianMod.id("has_structure/crystal_grove_shrine"));
        public static final TagKey<Biome> HAS_EMBERBLOOM_OUTPOST =
                TagKey.create(Registries.BIOME, ArtilianMod.id("has_structure/emberbloom_outpost"));
        public static final TagKey<Biome> HAS_STARLIGHT_WATCHTOWER =
                TagKey.create(Registries.BIOME, ArtilianMod.id("has_structure/starlight_watchtower"));
        public static final TagKey<Biome> HAS_CRYSTAL_COLOSSUS_ARENA =
                TagKey.create(Registries.BIOME, ArtilianMod.id("has_structure/crystal_colossus_arena"));
        public static final TagKey<Biome> HAS_BRIMSTONE_BASTION =
                TagKey.create(Registries.BIOME, ArtilianMod.id("has_structure/brimstone_bastion"));

        private Biomes() {}
    }
}
