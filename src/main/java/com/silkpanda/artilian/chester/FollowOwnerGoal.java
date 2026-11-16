package com.silkpanda.artilian.chester;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/** Chester trots after his owner, and hops to them if he falls too far behind. */
public class FollowOwnerGoal extends Goal {
    private static final double START_DISTANCE_SQR = 4.0 * 4.0;
    private static final double STOP_DISTANCE_SQR = 2.5 * 2.5;
    private static final double TELEPORT_DISTANCE_SQR = 12.0 * 12.0;
    private static final double SPEED = 1.3;

    private final ChesterEntity chester;
    private Player owner;
    private int recalcPathIn;

    public FollowOwnerGoal(ChesterEntity chester) {
        this.chester = chester;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player candidate = this.chester.getOwnerInLevel();
        if (candidate == null || candidate.isSpectator() || !this.chester.canFollow()) {
            return false;
        }
        if (this.chester.distanceToSqr(candidate) < START_DISTANCE_SQR) {
            return false;
        }
        this.owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null
                && this.owner.isAlive()
                && this.chester.canFollow()
                && this.chester.distanceToSqr(this.owner) > STOP_DISTANCE_SQR;
    }

    @Override
    public void start() {
        this.recalcPathIn = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.chester.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.chester.getLookControl().setLookAt(this.owner, 10.0F, this.chester.getMaxHeadXRot());
        if (--this.recalcPathIn > 0) {
            return;
        }
        this.recalcPathIn = this.adjustedTickDelay(10);
        if (this.chester.distanceToSqr(this.owner) >= TELEPORT_DISTANCE_SQR) {
            this.hopToOwner();
        } else {
            this.chester.getNavigation().moveTo(this.owner, SPEED);
        }
    }

    private void hopToOwner() {
        BlockPos ownerPos = this.owner.blockPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = this.chester.getRandom().nextIntBetweenInclusive(-3, 3);
            int dz = this.chester.getRandom().nextIntBetweenInclusive(-3, 3);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }
            BlockPos target = ownerPos.offset(dx, 0, dz);
            if (this.isSafeLanding(target)) {
                this.chester.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5,
                        this.chester.getYRot(), this.chester.getXRot());
                this.chester.getNavigation().stop();
                return;
            }
        }
    }

    private boolean isSafeLanding(BlockPos target) {
        if (!this.chester.level().getBlockState(target.below()).isSolid()) {
            return false;
        }
        BlockPos offset = target.subtract(this.chester.blockPosition());
        return this.chester.level().noCollision(this.chester, this.chester.getBoundingBox().move(offset));
    }
}
