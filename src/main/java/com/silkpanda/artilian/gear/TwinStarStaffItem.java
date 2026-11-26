package com.silkpanda.artilian.gear;

import com.silkpanda.artilian.mana.ArtilianWandItem;
import com.silkpanda.artilian.mana.ManaData;
import com.silkpanda.artilian.mana.WandSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Twin-Star Harmony Staff supporting Right-Click (Twin-Star Harmony) and
 * Shift + Right-Click (Twin-Star Sanctuary co-op cleanse/buff ring).
 */
public final class TwinStarStaffItem extends ArtilianWandItem {
    public TwinStarStaffItem(WandSpell spell, Properties properties) {
        super(spell, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(usedHand);
            if (level.isClientSide) {
                if (ManaData.get(player).canSpend(TwinStarAbilities.TWIN_STAR_SANCTUARY_MANA)) {
                    return InteractionResultHolder.success(stack);
                }
                return InteractionResultHolder.fail(stack);
            }
            if (level instanceof ServerLevel serverLevel
                    && TwinStarAbilities.castTwinStarSanctuary(serverLevel, player, this)) {
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.consume(stack);
            }
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.artilian.twin_star_staff.sanctuary_tooltip")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
