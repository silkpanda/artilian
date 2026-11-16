package com.silkpanda.artilian.registry;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.chester.ChesterData;
import com.silkpanda.artilian.mana.ManaData;
import com.silkpanda.artilian.pet.PetRosterData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ArtilianAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ArtilianMod.MODID);

    /**
     * Per-player Chester storage. copyOnDeath is the kid-safe control: it is what carries the
     * items onto the respawned player. Do not remove it (see AGENTS.md).
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChesterData>> CHESTER =
            ATTACHMENT_TYPES.register("chester", () -> AttachmentType.serializable(ChesterData::new)
                    .copyOnDeath()
                    .build());

    /** Per-player auto-regenerating Mana pool, preserved across death. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ManaData>> MANA =
            ATTACHMENT_TYPES.register("mana", () -> AttachmentType.serializable(ManaData::new)
                    .copyOnDeath()
                    .build());

    /** Per-player captured pet state, preserved across death to prevent pet loss or duplication. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PetRosterData>> PETS =
            ATTACHMENT_TYPES.register("pets", () -> AttachmentType.serializable(PetRosterData::new)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
