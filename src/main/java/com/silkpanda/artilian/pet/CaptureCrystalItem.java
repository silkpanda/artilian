package com.silkpanda.artilian.pet;

import com.silkpanda.artilian.registry.ArtilianDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Captures wild Artilian pets and safely holds, summons, recalls, and revives them.
 */
public class CaptureCrystalItem extends Item {
    public CaptureCrystalItem(Properties properties) {
        super(properties);
    }

    /** Model predicate value: 0.0 empty, 0.25 fainted/resting, 0.5 ready, 1.0 summoned. */
    public static float getVisualState(ItemStack stack) {
        CapturedPetData data = stack.get(ArtilianDataComponents.CAPTURED_PET.get());
        if (data == null) {
            return 0.0F;
        }
        if (data.fainted()) {
            return 0.25F;
        }
        if (data.summoned()) {
            return 1.0F;
        }
        return 0.5F;
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof ArtilianPetEntity pet) {
            CapturedPetData heldData = stack.get(ArtilianDataComponents.CAPTURED_PET.get());
            if (heldData != null && pet.getPetId().filter(heldData.petId()::equals).isPresent()) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    PetService.useCaptureCrystal(serverLevel, player, hand, stack);
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            return PetService.tryCapturePet(player, pet, stack);
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            return PetService.useCaptureCrystal(serverLevel, player, hand, stack);
        }
        return stack.has(ArtilianDataComponents.CAPTURED_PET.get())
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ArtilianDataComponents.CAPTURED_PET.get());
    }

    @Override
    public Component getName(ItemStack stack) {
        CapturedPetData data = stack.get(ArtilianDataComponents.CAPTURED_PET.get());
        if (data == null) {
            return super.getName(stack);
        }
        Component petName = data.customName()
                .filter(name -> !name.isBlank())
                .<Component>map(Component::literal)
                .orElseGet(() -> BuiltInRegistries.ENTITY_TYPE.get(data.entityType()).getDescription());
        return Component.translatable("item.artilian.capture_crystal.filled", petName);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CapturedPetData data = stack.get(ArtilianDataComponents.CAPTURED_PET.get());
        if (data == null) {
            tooltip.add(Component.translatable("item.artilian.capture_crystal.tooltip")
                    .withStyle(ChatFormatting.AQUA));
            return;
        }
        tooltip.add(Component.translatable("tooltip.artilian.pet.elements",
                PetElement.formatBadge(data.primaryElement(), data.secondaryElement())));
        if (data.fainted()) {
            tooltip.add(Component.translatable("tooltip.artilian.crystal.fainted", data.ownerName())
                    .withStyle(ChatFormatting.GOLD));
        } else if (data.summoned()) {
            tooltip.add(Component.translatable("tooltip.artilian.crystal.summoned", data.ownerName())
                    .withStyle(ChatFormatting.GREEN));
        } else {
            int hp = Math.max(1, Math.round(data.health()));
            int maxHp = Math.max(1, Math.round(data.maxHealth()));
            tooltip.add(Component.translatable("tooltip.artilian.crystal.ready", hp, maxHp, data.ownerName())
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
