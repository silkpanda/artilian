package com.silkpanda.artilian.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/** Dragonscale Hoe with Primal Overgrowth (Shift + Right-Click on crops/saplings/farmland). */
public final class DragonscaleHoeItem extends HoeItem {
    public DragonscaleHoeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player != null && player.isShiftKeyDown()
                && PrismaticAbilities.isPrismaticHarvestTarget(level.getBlockState(pos))) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (level instanceof ServerLevel serverLevel
                    && DragonscaleAbilities.castPrimalOvergrowth(serverLevel, player, context.getItemInHand(), this, pos)) {
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.artilian.dragonscale_hoe.tooltip").withStyle(ChatFormatting.GREEN));
    }
}
