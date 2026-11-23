package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/** Tool and weapon tiers for Sunsteel (between Iron and Diamond) and Starlight Crystal (Diamond grade). */
public final class ArtilianTiers {
    private ArtilianTiers() {}

    public static final Tier SUNSTEEL = new SimpleTier(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            450,
            7.0F,
            2.5F,
            16,
            () -> Ingredient.of(ArtilianItems.SUNSTEEL_INGOT.get()));

    public static final Tier STARLIGHT = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1100,
            8.5F,
            3.5F,
            20,
            () -> Ingredient.of(ArtilianItems.STARLIGHT_CRYSTAL.get()));

    public static final Tier PRISMATIC = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2050,
            9.5F,
            4.5F,
            24,
            () -> Ingredient.of(ArtilianItems.PRISM_CORE.get()));

    public static final Tier DRAGONSCALE = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2350,
            10.0F,
            5.0F,
            26,
            () -> Ingredient.of(ArtilianItems.DRAGON_SCALE.get()));

    public static final Tier TWIN_STAR = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2800,
            11.0F,
            6.0F,
            30,
            () -> Ingredient.of(ArtilianItems.TWIN_STAR_CORE.get()));
}
