package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/** Custom Artilian item, block, and entity tags. */
public final class ArtilianTags {
    private ArtilianTags() {}

    public static final class Items {
        public static final TagKey<Item> BIOME_BLOOMS = ItemTags.create(ArtilianMod.id("biome_blooms"));

        private Items() {}
    }

    public static final class EntityTypes {
        public static final TagKey<EntityType<?>> STRUCTURE_ENEMIES =
                TagKey.create(Registries.ENTITY_TYPE, ArtilianMod.id("structure_enemies"));

        private EntityTypes() {}
    }
}
