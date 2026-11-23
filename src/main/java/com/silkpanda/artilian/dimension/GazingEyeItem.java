package com.silkpanda.artilian.dimension;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Reusable Eye Realm portal igniter: right-click a Starlight Slate frame to awaken a Gazing Portal.
 */
public class GazingEyeItem extends Item {
    public GazingEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockPos targetPos = level.getBlockState(clickedPos).canBeReplaced()
                ? clickedPos
                : clickedPos.relative(context.getClickedFace());

        if (!level.getBlockState(targetPos).isAir() && !level.getBlockState(targetPos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        if (GazingPortalBlock.trySpawnPortal(level, targetPos)) {
            level.playSound(
                    context.getPlayer(),
                    targetPos,
                    SoundEvents.ENDER_EYE_DEATH,
                    SoundSource.BLOCKS,
                    0.9F,
                    1.15F);
            level.playSound(
                    context.getPlayer(),
                    targetPos,
                    SoundEvents.PORTAL_TRIGGER,
                    SoundSource.BLOCKS,
                    0.6F,
                    1.2F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 0.5,
                        targetPos.getZ() + 0.5,
                        18,
                        0.45,
                        0.45,
                        0.45,
                        0.04);
            }
            Player player = context.getPlayer();
            if (player != null && !player.getAbilities().instabuild) {
                EquipmentSlot slot = context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND
                        : EquipmentSlot.OFFHAND;
                context.getItemInHand().hurtAndBreak(1, player, slot);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.artilian.gazing_eye").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
