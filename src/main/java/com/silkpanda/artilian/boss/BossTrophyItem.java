package com.silkpanda.artilian.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Radiant boss trophy dropped for forging Phase 3 Stage 4 Tier 3 equipment. */
public class BossTrophyItem extends Item {
    private final String tooltipKey;
    private final ChatFormatting tooltipColor;

    public BossTrophyItem(Properties properties, String tooltipKey, ChatFormatting tooltipColor) {
        super(properties);
        this.tooltipKey = tooltipKey;
        this.tooltipColor = tooltipColor;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.tooltipKey).withStyle(this.tooltipColor));
    }
}
