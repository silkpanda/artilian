package com.silkpanda.artilian.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

/**
 * Tier 3 boss-forged sword combining Crystal Colossus and Captain Brimstone power.
 * Lights targets up with Glowing and brief Brimstone embers on hit.
 */
public final class PrismaticSwordItem extends ArtilianSwordItem {
    public PrismaticSwordItem(Tier tier, Properties properties, String tooltipKey, ChatFormatting tooltipColor) {
        super(tier, properties, tooltipKey, tooltipColor);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            target.igniteForSeconds(2);
            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        target.getX(), target.getY(0.5), target.getZ(),
                        8, 0.35, 0.35, 0.35, 0.04);
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        target.getX(), target.getY(0.5), target.getZ(),
                        6, 0.3, 0.3, 0.3, 0.03);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
