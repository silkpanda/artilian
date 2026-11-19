package com.silkpanda.artilian.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/** An item that summons one specific boss when used on a Boss Altar. */
public class BossSummonItem extends Item {
    private final Supplier<? extends EntityType<? extends ArtilianBossEntity>> bossType;
    @Nullable
    private final String tooltipKey;
    private final ChatFormatting tooltipColor;

    public BossSummonItem(Supplier<? extends EntityType<? extends ArtilianBossEntity>> bossType, Properties properties) {
        this(bossType, properties, null, ChatFormatting.AQUA);
    }

    public BossSummonItem(
            Supplier<? extends EntityType<? extends ArtilianBossEntity>> bossType,
            Properties properties,
            @Nullable String tooltipKey,
            ChatFormatting tooltipColor) {
        super(properties);
        this.bossType = bossType;
        this.tooltipKey = tooltipKey;
        this.tooltipColor = tooltipColor;
    }

    public EntityType<? extends ArtilianBossEntity> bossType() {
        return this.bossType.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (this.tooltipKey != null) {
            tooltip.add(Component.translatable(this.tooltipKey).withStyle(this.tooltipColor));
        }
    }
}
