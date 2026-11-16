package com.silkpanda.artilian.pet;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Sweet, glowing snack made from Artilian biome flowers that befriends wild pets and heals tamed ones.
 */
public class BiomeTreatItem extends Item {
    public BiomeTreatItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof ArtilianPetEntity pet) {
            return PetService.feedBiomeTreat(player, pet, stack);
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.artilian.biome_treat.tooltip")
                .withStyle(ChatFormatting.GREEN));
    }
}
