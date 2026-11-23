package com.silkpanda.artilian.dimension;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Bidirectional twilight portal linking the Overworld and {@code artilian:eye_realm}.
 */
public class GazingPortalBlock extends Block implements Portal {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    private static final int PORTAL_SEARCH_RADIUS = 64;

    public GazingPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    public static boolean trySpawnPortal(LevelAccessor level, BlockPos pos) {
        Optional<GazingPortalShape> shape = GazingPortalShape.findEmptyPortalShape(level, pos, Direction.Axis.X);
        if (shape.isPresent()) {
            shape.get().createPortalBlocks();
            return true;
        }
        return false;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction facing,
            BlockState facingState,
            LevelAccessor level,
            BlockPos currentPos,
            BlockPos facingPos) {
        Direction.Axis facingAxis = facing.getAxis();
        Direction.Axis portalAxis = state.getValue(AXIS);
        boolean horizontalPerpendicular = portalAxis != facingAxis && facingAxis.isHorizontal();
        if (!horizontalPerpendicular
                && !facingState.is(this)
                && !new GazingPortalShape(level, currentPos, portalAxis).isComplete()) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        if (entity instanceof Player player) {
            return player.getAbilities().instabuild ? 1 : 40;
        }
        return 0;
    }

    @Override
    public Portal.Transition getLocalTransition() {
        return Portal.Transition.CONFUSION;
    }

    @Override
    @Nullable
    public DimensionTransition getPortalDestination(ServerLevel sourceLevel, Entity entity, BlockPos pos) {
        ResourceKey<Level> targetKey = sourceLevel.dimension() == EyeRealmDimension.EYE_REALM_LEVEL
                ? Level.OVERWORLD
                : EyeRealmDimension.EYE_REALM_LEVEL;
        ServerLevel targetLevel = sourceLevel.getServer().getLevel(targetKey);
        if (targetLevel == null) {
            return null;
        }

        Direction.Axis sourceAxis = sourceLevel.getBlockState(pos).getOptionalValue(AXIS).orElse(Direction.Axis.X);
        BlockPos existing = findClosestPortal(targetLevel, pos, PORTAL_SEARCH_RADIUS);
        BlockPos arrivalInterior = existing != null
                ? existing
                : createReturnPortalWithPlatform(targetLevel, pos, sourceAxis);

        Vec3 arrivalPos = new Vec3(
                arrivalInterior.getX() + 0.5,
                arrivalInterior.getY(),
                arrivalInterior.getZ() + 0.5);

        return new DimensionTransition(
                targetLevel,
                arrivalPos,
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot(),
                DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
    }

    @Nullable
    public static BlockPos findClosestPortal(ServerLevel level, BlockPos around, int radius) {
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 4;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                int x = around.getX() + dx;
                int z = around.getZ() + dz;
                int surfaceY = Math.clamp(level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), minY, maxY);
                int scanLow = Math.max(minY, Math.min(surfaceY - 12, around.getY() - 16));
                int scanHigh = Math.min(maxY, Math.max(surfaceY + 12, around.getY() + 16));
                for (int y = scanLow; y <= scanHigh; y++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).is(ArtilianBlocks.GAZING_PORTAL.get())) {
                        double distSq = cursor.distSqr(around);
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq;
                            best = cursor.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    /**
     * Builds a safe {@code 5x5} Starlight Slate landing pad and a {@code 4x5} Gazing Portal
     * frame at a safe open surface Y so players never arrive in void or solid rock.
     */
    public static BlockPos createReturnPortalWithPlatform(ServerLevel level, BlockPos targetHint, Direction.Axis axis) {
        int safeY = findSafeSurfaceY(level, targetHint.getX(), targetHint.getZ());
        BlockPos origin = new BlockPos(targetHint.getX(), safeY, targetHint.getZ());
        BlockState frameState = ArtilianBlocks.STARLIGHT_SLATE.get().defaultBlockState();
        BlockState portalState = ArtilianBlocks.GAZING_PORTAL.get().defaultBlockState().setValue(AXIS, axis);

        // 5x5 solid landing pad at safeY - 1
        for (int px = -2; px <= 2; px++) {
            for (int pz = -2; pz <= 2; pz++) {
                level.setBlock(origin.offset(px, -1, pz), frameState, 3);
            }
        }

        // Clear 5x4x5 air pocket above the landing pad so players never suffocate
        for (int px = -2; px <= 2; px++) {
            for (int py = 0; py <= 4; py++) {
                for (int pz = -2; pz <= 2; pz++) {
                    level.setBlock(origin.offset(px, py, pz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockPos frameStart = origin.relative(right.getOpposite(), 1);

        // Build 4-wide x 5-tall frame edges first so portal interior never collapses mid-build
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                boolean isEdge = w == 0 || w == 3 || h == 0 || h == 4;
                if (isEdge) {
                    level.setBlock(frameStart.relative(right, w).above(h), frameState, 3);
                }
            }
        }
        // Fill 2x3 interior with flag 18 (UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE)
        for (int w = 1; w <= 2; w++) {
            for (int h = 1; h <= 3; h++) {
                level.setBlock(frameStart.relative(right, w).above(h), portalState, 18);
            }
        }

        return origin.above(1);
    }

    public static int findSafeSurfaceY(ServerLevel level, int x, int z) {
        int minSafe = level.getMinBuildHeight() + 8;
        int maxSafe = level.getMaxBuildHeight() - 16;
        // Ensure chunk is loaded before sampling heightmap
        level.getChunk(x >> 4, z >> 4);
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (surface < minSafe) {
            return Math.max(minSafe, 64);
        }
        return Math.clamp(surface, minSafe, maxSafe);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.4F,
                    random.nextFloat() * 0.4F + 0.9F,
                    false);
        }
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double vx = (random.nextDouble() - 0.5) * 0.3;
            double vy = (random.nextDouble() - 0.5) * 0.3;
            double vz = (random.nextDouble() - 0.5) * 0.3;
            level.addParticle(ParticleTypes.PORTAL, x, y, z, vx, vy, vz);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> state.getValue(AXIS) == Direction.Axis.X
                    ? state.setValue(AXIS, Direction.Axis.Z)
                    : state.setValue(AXIS, Direction.Axis.X);
            default -> state;
        };
    }
}
