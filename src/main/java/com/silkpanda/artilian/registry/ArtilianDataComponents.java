package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.pet.CapturedPetData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Custom NeoForge 1.21.1 DataComponents for Artilian items. */
public final class ArtilianDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ArtilianMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CapturedPetData>> CAPTURED_PET =
            DATA_COMPONENTS.registerComponentType("captured_pet", builder -> builder
                    .persistent(CapturedPetData.CODEC)
                    .networkSynchronized(CapturedPetData.STREAM_CODEC));

    private ArtilianDataComponents() {}

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
