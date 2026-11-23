package com.silkpanda.artilian.dragon;

import com.silkpanda.artilian.pet.ArtilianPetEntity;
import com.silkpanda.artilian.pet.PetDragonEntity;
import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Hatchable dragon egg block dropped by Phase 5 wild dragons.
 *
 * <p>Automatically incubates through {@link #HATCH} stages {@code 0 -> 1 -> 2 -> hatch} via
 * scheduled ticks (faster on warm or Dragon Jungle blocks like Amber Moss, Jungle Turf, Moss
 * Block, Hay Block, or Magma Block). Players can also speed up incubation by right-clicking with
 * a {@link ArtilianItems#BIOME_TREAT} or {@link Items#BLAZE_POWDER}.
 */
public class DragonEggBlock extends Block {
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final int WARM_HATCH_INTERVAL_TICKS = 240;
    public static final int NORMAL_HATCH_INTERVAL_TICKS = 400;

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 15.0, 14.0);

    private final Supplier<? extends EntityType<? extends PetDragonEntity>> hatchedType;
    private final ParticleOptions hatchParticle;

    public DragonEggBlock(
            Properties properties,
            Supplier<? extends EntityType<? extends PetDragonEntity>> hatchedType,
            ParticleOptions hatchParticle) {
        super(properties);
        this.hatchedType = hatchedType;
        this.hatchParticle = hatchParticle;
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(this)) {
            level.scheduleTick(pos, this, getHatchIntervalTicks(level, pos));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        advanceHatch(level, pos, state, null);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(ArtilianItems.BIOME_TREAT.get()) && !stack.is(Items.BLAZE_POWDER)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        if (level instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            serverLevel.playSound(
                    null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.25F);
            advanceHatch(serverLevel, pos, state, player);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    public int getHatchIntervalTicks(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        boolean warm = below.is(ArtilianBlocks.AMBER_MOSS.get())
                || below.is(ArtilianBlocks.JUNGLE_TURF.get())
                || below.is(Blocks.MOSS_BLOCK)
                || below.is(Blocks.HAY_BLOCK)
                || below.is(Blocks.MAGMA_BLOCK);
        return warm ? WARM_HATCH_INTERVAL_TICKS : NORMAL_HATCH_INTERVAL_TICKS;
    }

    public void advanceHatch(ServerLevel level, BlockPos pos, BlockState state, @Nullable Player booster) {
        int hatch = state.getValue(HATCH);
        if (hatch < 2) {
            level.setBlock(pos, state.setValue(HATCH, hatch + 1), Block.UPDATE_CLIENTS);
            level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.9F, 0.95F + hatch * 0.1F);
            level.sendParticles(
                    this.hatchParticle,
                    pos.getX() + 0.5,
                    pos.getY() + 0.7,
                    pos.getZ() + 0.5,
                    10,
                    0.25,
                    0.25,
                    0.25,
                    0.03);
            level.scheduleTick(pos, this, getHatchIntervalTicks(level, pos));
            return;
        }
        hatchEgg(level, pos, booster);
    }

    @Nullable
    public PetDragonEntity hatchEgg(ServerLevel level, BlockPos pos, @Nullable Player hatcher) {
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 1.0F, 1.05F);
        level.sendParticles(
                this.hatchParticle,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                20,
                0.35,
                0.35,
                0.35,
                0.05);
        level.sendParticles(
                ParticleTypes.DRAGON_BREATH,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                14,
                0.3,
                0.3,
                0.3,
                0.04);

        PetDragonEntity dragonling = this.hatchedType.get().create(level);
        if (dragonling == null) {
            return null;
        }
        dragonling.moveTo(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        dragonling.addAffinity(ArtilianPetEntity.MAX_AFFINITY);
        dragonling.setPersistenceRequired();
        level.addFreshEntity(dragonling);

        Player notifyTarget = hatcher != null
                ? hatcher
                : level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false);
        if (notifyTarget != null) {
            notifyTarget.displayClientMessage(
                    Component.translatable("message.artilian.dragon_egg.hatched", dragonling.getDisplayName())
                            .withStyle(ChatFormatting.GREEN),
                    true);
        }
        return dragonling;
    }
}
