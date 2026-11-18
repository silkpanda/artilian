package com.silkpanda.artilian.boss;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/** An item that summons one specific boss when used on a Boss Altar. */
public class BossSummonItem extends Item {
    private final Supplier<? extends EntityType<? extends ArtilianBossEntity>> bossType;

    public BossSummonItem(Supplier<? extends EntityType<? extends ArtilianBossEntity>> bossType, Properties properties) {
        super(properties);
        this.bossType = bossType;
    }

    public EntityType<? extends ArtilianBossEntity> bossType() {
        return this.bossType.get();
    }
}
