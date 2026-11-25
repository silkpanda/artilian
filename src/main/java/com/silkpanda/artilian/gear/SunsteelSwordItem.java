package com.silkpanda.artilian.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

/** Sunsteel Sword with the Solar Slash right-click active ability. */
public final class SunsteelSwordItem extends ArtilianSwordItem {
    public SunsteelSwordItem(Tier tier, Properties properties, String tooltipKey, ChatFormatting tooltipColor) {
        super(tier, properties, tooltipKey, tooltipColor);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (level instanceof ServerLevel serverLevel && SunsteelAbilities.castSolarSlash(serverLevel, player, this)) {
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}
