package com.silkpanda.artilian.mana;

import net.minecraft.ChatFormatting;

/**
 * Metadata for each starter wand or staff spell: Mana cost, cooldown, and tooltip styling.
 */
public enum WandSpell {
    SPARK(20, 10, "item.artilian.spark_wand.tooltip", ChatFormatting.AQUA),
    HEART_BLOOM(30, 20, "item.artilian.heart_bloom_staff.tooltip", ChatFormatting.GREEN),
    ZEPHYR_DASH(25, 15, "item.artilian.zephyr_dash_wand.tooltip", ChatFormatting.LIGHT_PURPLE),
    PRISM_NOVA(40, 25, "item.artilian.prism_nova_staff.tooltip", ChatFormatting.LIGHT_PURPLE),
    BRIMSTONE_METEOR(45, 30, "item.artilian.brimstone_meteor_staff.tooltip", ChatFormatting.GOLD),
    TWIN_STAR_HARMONY(50, 35, "item.artilian.twin_star_staff.tooltip", ChatFormatting.AQUA);

    private final int manaCost;
    private final int cooldownTicks;
    private final String tooltipKey;
    private final ChatFormatting tooltipColor;

    WandSpell(int manaCost, int cooldownTicks, String tooltipKey, ChatFormatting tooltipColor) {
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
        this.tooltipKey = tooltipKey;
        this.tooltipColor = tooltipColor;
    }

    public int manaCost() {
        return this.manaCost;
    }

    public int cooldownTicks() {
        return this.cooldownTicks;
    }

    public String tooltipKey() {
        return this.tooltipKey;
    }

    public ChatFormatting tooltipColor() {
        return this.tooltipColor;
    }
}
