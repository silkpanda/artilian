package com.silkpanda.artilian.mana;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Right-click castable Wand or Staff backed by {@link WandSpell} and per-player {@link ManaData}.
 */
public class ArtilianWandItem extends Item {
    private final WandSpell spell;

    public ArtilianWandItem(WandSpell spell, Properties properties) {
        super(properties);
        this.spell = spell;
    }

    public WandSpell getSpell() {
        return this.spell;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            if (ManaData.get(player).canSpend(this.spell.manaCost())) {
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (level instanceof ServerLevel serverLevel
                && ManaService.castWand(serverLevel, player, this, this.spell)) {
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.spell.tooltipKey()).withStyle(this.spell.tooltipColor()));
    }
}
