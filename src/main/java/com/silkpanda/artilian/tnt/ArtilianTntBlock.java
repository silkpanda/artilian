package com.silkpanda.artilian.tnt;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Custom TNT block that always primes {@link ArtilianPrimedTnt} for every ignition path
 * (redstone, flint &amp; steel, fire charges, Shift+Right-Click, chain explosions, arrows, and dispensers).
 */
public class ArtilianTntBlock extends TntBlock {
    public static final MapCodec<ArtilianTntBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(TntVariant::values).fieldOf("variant").forGetter(ArtilianTntBlock::getVariant),
            propertiesCodec()
    ).apply(instance, ArtilianTntBlock::new));

    private final TntVariant variant;

    public ArtilianTntBlock(TntVariant variant, BlockBehaviour.Properties properties) {
        super(properties);
        this.variant = variant;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<TntBlock> codec() {
        return (MapCodec<TntBlock>) (MapCodec<?>) CODEC;
    }

    public TntVariant getVariant() {
        return this.variant;
    }

    @Override
    public void onCaughtFire(
            BlockState state, Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        prime(level, pos, igniter);
    }

    public ArtilianPrimedTnt prime(Level level, BlockPos pos, @Nullable LivingEntity igniter) {
        if (level.isClientSide) {
            return null;
        }
        ArtilianPrimedTnt primed = new ArtilianPrimedTnt(
                level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter, this.variant);
        level.addFreshEntity(primed);
        level.playSound(null, primed.getX(), primed.getY(), primed.getZ(),
                SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        return primed;
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            ArtilianPrimedTnt primed = new ArtilianPrimedTnt(
                    level,
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5,
                    explosion.getIndirectSourceEntity(),
                    this.variant);
            int fuse = primed.getFuse();
            primed.setFuse(level.random.nextInt(Math.max(1, fuse / 4)) + fuse / 8);
            level.addFreshEntity(primed);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isSecondaryUseActive()) {
            this.onCaughtFire(state, level, pos, hitResult.getDirection(), player);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable(this.variant.getTooltipKey()).withStyle(this.variant.getTooltipColor()));
    }

    /** Registers dispenser behaviors so dispensers prime {@link ArtilianPrimedTnt} instead of dropping blocks. */
    public static void registerDispenserBehaviors() {
        for (Block block : List.of(
                ArtilianBlocks.PARTY_TNT.get(),
                ArtilianBlocks.BOUNCE_TNT.get(),
                ArtilianBlocks.FROST_NOVA_TNT.get(),
                ArtilianBlocks.CLUSTER_QUARRY_TNT.get())) {
            if (block instanceof ArtilianTntBlock tntBlock) {
                DispenserBlock.registerBehavior(tntBlock, new DefaultDispenseItemBehavior() {
                    @Override
                    protected ItemStack execute(BlockSource source, ItemStack stack) {
                        Level level = source.level();
                        BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                        ArtilianPrimedTnt primed = new ArtilianPrimedTnt(
                                level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, null, tntBlock.getVariant());
                        level.addFreshEntity(primed);
                        level.playSound(null, primed.getX(), primed.getY(), primed.getZ(),
                                SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(null, GameEvent.ENTITY_PLACE, pos);
                        stack.shrink(1);
                        return stack;
                    }
                });
            }
        }
    }
}
