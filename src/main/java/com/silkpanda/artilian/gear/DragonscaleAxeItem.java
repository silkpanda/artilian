package com.silkpanda.artilian.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/** Dragonscale Axe with Wingbeat Roar (Right-Click) and Canopy Fell (Shift + Right-Click on log). */
public final class DragonscaleAxeItem extends AxeItem {
    public DragonscaleAxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (level instanceof ServerLevel serverLevel && DragonscaleAbilities.castWingbeatRoar(serverLevel, player, this)) {
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player != null && player.isShiftKeyDown() && level.getBlockState(pos).is(BlockTags.LOGS)) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (level instanceof ServerLevel serverLevel
                    && DragonscaleAbilities.castCanopyFell(serverLevel, player, context.getItemInHand(), this, pos)) {
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.artilian.dragonscale_axe.tooltip").withStyle(ChatFormatting.GREEN));
    }
}
