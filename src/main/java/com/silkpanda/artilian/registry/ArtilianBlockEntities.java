package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.boss.BossAltarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArtilianBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ArtilianMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BossAltarBlockEntity>> BOSS_ALTAR =
            BLOCK_ENTITY_TYPES.register("boss_altar", () -> BlockEntityType.Builder
                    .of(BossAltarBlockEntity::new, ArtilianBlocks.BOSS_ALTAR.get())
                    .build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
