package com.silkpanda.artilian.dimension;

import com.silkpanda.artilian.registry.ArtilianBlocks;
import com.silkpanda.artilian.registry.ArtilianTags;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

/** Validates rectangular Gazing Portal frames built from {@code artilian:gazing_portal_frame}. */
public class GazingPortalShape {
    public static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    public static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;

    private static final Predicate<BlockState> FRAME_PREDICATE =
            state -> state.is(ArtilianTags.Blocks.GAZING_PORTAL_FRAME);

    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;
    @Nullable
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    public static Optional<GazingPortalShape> findEmptyPortalShape(LevelAccessor level, BlockPos bottomLeft, Direction.Axis axis) {
        return findPortalShape(level, bottomLeft, shape -> shape.isValid() && shape.numPortalBlocks == 0, axis);
    }

    public static Optional<GazingPortalShape> findPortalShape(
            LevelAccessor level, BlockPos pos, Predicate<GazingPortalShape> filter, Direction.Axis axis) {
        Optional<GazingPortalShape> first = Optional.of(new GazingPortalShape(level, pos, axis)).filter(filter);
        if (first.isPresent()) {
            return first;
        }
        Direction.Axis otherAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return Optional.of(new GazingPortalShape(level, pos, otherAxis)).filter(filter);
    }

    public GazingPortalShape(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        this.level = level;
        this.axis = axis;
        this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = this.calculateBottomLeft(pos);
        if (this.bottomLeft == null) {
            this.bottomLeft = pos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.height = this.calculateHeight();
            }
        }
    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos pos) {
        int minY = Math.max(this.level.getMinBuildHeight(), pos.getY() - MAX_HEIGHT);
        BlockPos cursor = pos;
        while (cursor.getY() > minY && isEmpty(this.level.getBlockState(cursor.below()))) {
            cursor = cursor.below();
        }
        Direction leftDir = this.rightDir.getOpposite();
        int distToLeftEdge = this.getDistanceUntilEdgeAboveFrame(cursor, leftDir) - 1;
        return distToLeftEdge < 0 ? null : cursor.relative(leftDir, distToLeftEdge);
    }

    private int calculateWidth() {
        int dist = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
        return dist >= MIN_WIDTH && dist <= MAX_WIDTH ? dist : 0;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos pos, Direction dir) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= MAX_WIDTH; i++) {
            cursor.set(pos).move(dir, i);
            BlockState state = this.level.getBlockState(cursor);
            if (!isEmpty(state)) {
                if (FRAME_PREDICATE.test(state)) {
                    return i;
                }
                break;
            }
            BlockState below = this.level.getBlockState(cursor.move(Direction.DOWN));
            if (!FRAME_PREDICATE.test(below)) {
                break;
            }
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int h = this.getDistanceUntilTop(cursor);
        return h >= MIN_HEIGHT && h <= MAX_HEIGHT && this.hasTopFrame(cursor, h) ? h : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos cursor, int height) {
        for (int i = 0; i < this.width; i++) {
            cursor.set(this.bottomLeft).move(Direction.UP, height).move(this.rightDir, i);
            if (!FRAME_PREDICATE.test(this.level.getBlockState(cursor))) {
                return false;
            }
        }
        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos cursor) {
        for (int y = 0; y < MAX_HEIGHT; y++) {
            cursor.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir, -1);
            if (!FRAME_PREDICATE.test(this.level.getBlockState(cursor))) {
                return y;
            }
            cursor.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir, this.width);
            if (!FRAME_PREDICATE.test(this.level.getBlockState(cursor))) {
                return y;
            }
            for (int x = 0; x < this.width; x++) {
                cursor.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir, x);
                BlockState state = this.level.getBlockState(cursor);
                if (!isEmpty(state)) {
                    return y;
                }
                if (state.is(ArtilianBlocks.GAZING_PORTAL.get())) {
                    this.numPortalBlocks++;
                }
            }
        }
        return MAX_HEIGHT;
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(ArtilianBlocks.GAZING_PORTAL.get());
    }

    public boolean isValid() {
        return this.bottomLeft != null
                && this.width >= MIN_WIDTH && this.width <= MAX_WIDTH
                && this.height >= MIN_HEIGHT && this.height <= MAX_HEIGHT;
    }

    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }

    public void createPortalBlocks() {
        BlockState portalState = ArtilianBlocks.GAZING_PORTAL.get()
                .defaultBlockState()
                .setValue(GazingPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(
                this.bottomLeft,
                this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)
        ).forEach(pos -> this.level.setBlock(pos, portalState, 18));
    }

    public Direction.Axis getAxis() {
        return this.axis;
    }

    @Nullable
    public BlockPos getBottomLeft() {
        return this.bottomLeft;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public BlockUtil.FoundRectangle getRectangle() {
        return new BlockUtil.FoundRectangle(this.bottomLeft, this.width, this.height);
    }
}
