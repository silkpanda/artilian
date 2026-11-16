package com.silkpanda.artilian.tnt;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * The four Phase 1 custom TNT variants and their DESIGN.md visual/safety properties.
 */
public enum TntVariant implements StringRepresentable {
    PARTY(
            "party_tnt",
            false,
            ChatFormatting.LIGHT_PURPLE,
            "block.artilian.party_tnt.tooltip",
            new DustParticleOptions(new Vector3f(0.851F, 0.275F, 0.937F), 1.3F),
            ArtilianBlocks.PARTY_TNT),
    BOUNCE(
            "bounce_tnt",
            false,
            ChatFormatting.GREEN,
            "block.artilian.bounce_tnt.tooltip",
            new DustParticleOptions(new Vector3f(0.298F, 0.851F, 0.392F), 1.3F),
            ArtilianBlocks.BOUNCE_TNT),
    FROST_NOVA(
            "frost_nova_tnt",
            false,
            ChatFormatting.AQUA,
            "block.artilian.frost_nova_tnt.tooltip",
            new DustParticleOptions(new Vector3f(0.220F, 0.851F, 0.753F), 1.3F),
            ArtilianBlocks.FROST_NOVA_TNT),
    CLUSTER_QUARRY(
            "cluster_quarry_tnt",
            true,
            ChatFormatting.GOLD,
            "block.artilian.cluster_quarry_tnt.tooltip",
            new DustParticleOptions(new Vector3f(1.000F, 0.624F, 0.220F), 1.4F),
            ArtilianBlocks.CLUSTER_QUARRY_TNT);

    private final String serializedName;
    private final boolean destructiveCapable;
    private final ChatFormatting tooltipColor;
    private final String tooltipKey;
    private final ParticleOptions telegraphParticle;
    private final Supplier<? extends Block> blockSupplier;

    TntVariant(
            String serializedName,
            boolean destructiveCapable,
            ChatFormatting tooltipColor,
            String tooltipKey,
            ParticleOptions telegraphParticle,
            Supplier<? extends Block> blockSupplier) {
        this.serializedName = serializedName;
        this.destructiveCapable = destructiveCapable;
        this.tooltipColor = tooltipColor;
        this.tooltipKey = tooltipKey;
        this.telegraphParticle = telegraphParticle;
        this.blockSupplier = blockSupplier;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public boolean isDestructiveCapable() {
        return this.destructiveCapable;
    }

    public ChatFormatting getTooltipColor() {
        return this.tooltipColor;
    }

    public String getTooltipKey() {
        return this.tooltipKey;
    }

    public ParticleOptions getTelegraphParticle() {
        return this.telegraphParticle;
    }

    public BlockState defaultBlockState() {
        return this.blockSupplier.get().defaultBlockState();
    }

    public static TntVariant fromName(String name) {
        for (TntVariant variant : values()) {
            if (variant.serializedName.equals(name)) {
                return variant;
            }
        }
        return PARTY;
    }
}
