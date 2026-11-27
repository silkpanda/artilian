package com.silkpanda.artilian.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

/**
 * Phase 5 Dragonscale sword forged from Dragon Jungle wild dragon scales.
 * Scorches targets with dragonfire embers and brief Weakness on hit, and casts Dragonfire Breath on Right-Click.
 */
public final class DragonscaleSwordItem extends ArtilianSwordItem {
    public DragonscaleSwordItem(Tier tier, Properties properties, String tooltipKey, ChatFormatting tooltipColor) {
        super(tier, properties, tooltipKey, tooltipColor);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (level instanceof ServerLevel serverLevel && DragonscaleAbilities.castDragonfireBreath(serverLevel, player, this)) {
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide) {
            target.igniteForSeconds(3);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        target.getX(), target.getY(0.5), target.getZ(),
                        8, 0.35, 0.35, 0.35, 0.03);
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        target.getX(), target.getY(0.5), target.getZ(),
                        6, 0.3, 0.3, 0.3, 0.03);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
