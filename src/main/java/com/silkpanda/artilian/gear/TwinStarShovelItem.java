package com.silkpanda.artilian.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Twin-Star Shovel with Starlight Glide passive and Eclipse Excavate (Shift + Right-Click) 5x5 active. */
public final class TwinStarShovelItem extends ShovelItem {
    public TwinStarShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (level instanceof ServerLevel serverLevel) {
            TwinStarAbilities.onStarlightGlide(serverLevel, miningEntity, state, pos);
        }
        return super.mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel
                && TwinStarAbilities.castEclipseExcavate(
                        serverLevel, player, context.getItemInHand(), this,
                        context.getClickedPos(), context.getClickedFace())) {
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.artilian.twin_star_shovel.tooltip").withStyle(ChatFormatting.AQUA));
    }
}
