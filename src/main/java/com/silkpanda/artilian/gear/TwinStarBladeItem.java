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
 * Phase 5 Stage 9 Twin-Star Blade uniting Alex's Eye Realm and Steve's Dragon Jungle.
 * Ignites targets with dragonfire, weakens them, and outlines them in twin-star light.
 */
public final class TwinStarBladeItem extends ArtilianSwordItem {
    public TwinStarBladeItem(Tier tier, Properties properties, String tooltipKey, ChatFormatting tooltipColor) {
        super(tier, properties, tooltipKey, tooltipColor);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide) {
            target.igniteForSeconds(3);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        target.getX(), target.getY(0.5), target.getZ(),
                        8, 0.35, 0.35, 0.35, 0.04);
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        target.getX(), target.getY(0.5), target.getZ(),
                        8, 0.35, 0.35, 0.35, 0.05);
                serverLevel.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        target.getX(), target.getY(0.5), target.getZ(),
                        6, 0.3, 0.3, 0.3, 0.03);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
