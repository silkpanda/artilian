package com.silkpanda.artilian.dimension;

import com.silkpanda.artilian.ArtilianMod;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Watches for any {@code artilian:boss_drops} item thrown into a lit Nether Portal
 * and transforms that portal into a {@link PrimalPortalBlock} leading to {@code artilian:dragon_jungle}.
 */
@EventBusSubscriber(modid = ArtilianMod.MODID)
public final class PrimalPortalEvents {
    private PrimalPortalEvents() {}

    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)
                || itemEntity.level().isClientSide
                || !itemEntity.isAlive()) {
            return;
        }
        if (!(itemEntity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        tryTransformIfBossDropInNetherPortal(serverLevel, itemEntity);
    }

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)
                || !(itemEntity.level() instanceof ServerLevel serverLevel)
                || !itemEntity.getItem().is(ArtilianTags.Items.BOSS_DROPS)) {
            return;
        }
        if (event.getDimension() == Level.NETHER || event.getDimension() == Level.OVERWORLD) {
            if (tryTransformIfBossDropInNetherPortal(serverLevel, itemEntity)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Checks whether {@code itemEntity} is a boss drop inside a Nether Portal block; if so,
     * transforms the portal and consumes 1 item from the stack.
     */
    public static boolean tryTransformIfBossDropInNetherPortal(ServerLevel level, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty() || !stack.is(ArtilianTags.Items.BOSS_DROPS)) {
            return false;
        }
        BlockPos pos = itemEntity.blockPosition();
        if (!level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
            return false;
        }
        if (PrimalPortalBlock.transformNetherPortal(level, pos) <= 0) {
            return false;
        }
        stack.shrink(1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }
        return true;
    }
}
