package com.silkpanda.artilian.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Starter sword with a concise, kid-friendly color tooltip matching DESIGN.md. */
public class ArtilianSwordItem extends SwordItem {
    private final String tooltipKey;
    private final ChatFormatting tooltipColor;

    public ArtilianSwordItem(Tier tier, Properties properties, String tooltipKey, ChatFormatting tooltipColor) {
        super(tier, properties);
        this.tooltipKey = tooltipKey;
        this.tooltipColor = tooltipColor;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.tooltipKey).withStyle(this.tooltipColor));
    }
}
