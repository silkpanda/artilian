package com.silkpanda.artilian.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

/**
 * Kid-safe configuration for Adventures in Artilian ({@code config/artilian-common.toml}).
 */
public final class ArtilianConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.BooleanValue ALLOW_DESTRUCTIVE_CUSTOM_TNT;

    @Nullable
    private static volatile Boolean testOverride;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("tnt");
        ALLOW_DESTRUCTIVE_CUSTOM_TNT = builder
                .comment("When false (the kid-safe default), destructive custom TNTs like Cluster Quarry TNT never break blocks in the world.")
                .define("allowDestructiveCustomTnt", false);
        builder.pop();
        COMMON_SPEC = builder.build();
    }

    private ArtilianConfig() {}

    /**
     * Returns whether destructive-capable custom TNTs (e.g., Cluster Quarry TNT) may break blocks.
     * Defaults to {@code false} until explicitly enabled in config.
     */
    public static boolean isDestructiveTntAllowed() {
        Boolean override = testOverride;
        if (override != null) {
            return override;
        }
        return COMMON_SPEC.isLoaded() && ALLOW_DESTRUCTIVE_CUSTOM_TNT.getAsBoolean();
    }

    /**
     * Overrides {@link #isDestructiveTntAllowed()} deterministically inside tests. Always reset to
     * {@code null} in a {@code finally} block.
     */
    public static void setTestOverride(@Nullable Boolean override) {
        testOverride = override;
    }
}
