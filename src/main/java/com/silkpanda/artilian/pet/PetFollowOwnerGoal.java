package com.silkpanda.artilian.pet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/** Keeps a summoned, non-sitting pet trotting cheerfully beside its owner. */
public class PetFollowOwnerGoal extends Goal {
    public static final double START_DISTANCE_SQR = 4.0 * 4.0;
    public static final double COMBAT_LEASH_DISTANCE_SQR = 14.0 * 14.0;
    private static final double STOP_DISTANCE_SQR = 2.5 * 2.5;
    private static final double TELEPORT_DISTANCE_SQR = 14.0 * 14.0;
    private static final double SPEED = 1.25;

    private final ArtilianPetEntity pet;
    private Player owner;
    private int recalcPathIn;

    public PetFollowOwnerGoal(ArtilianPetEntity pet) {
        this.pet = pet;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player candidate = this.pet.getOwnerInLevel();
        if (candidate == null || candidate.isSpectator() || !this.pet.canFollow()) {
            return false;
        }
        double threshold = this.pet.getTarget() != null ? COMBAT_LEASH_DISTANCE_SQR : START_DISTANCE_SQR;
        if (this.pet.distanceToSqr(candidate) < threshold) {
            return false;
        }
        this.owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null
                && this.owner.isAlive()
                && this.pet.canFollow()
                && (this.pet.getTarget() == null || this.pet.distanceToSqr(this.owner) >= COMBAT_LEASH_DISTANCE_SQR)
                && this.pet.distanceToSqr(this.owner) > STOP_DISTANCE_SQR;
    }

    @Override
    public void start() {
        this.recalcPathIn = 0;
        if (this.owner != null && this.pet.getTarget() != null
                && this.pet.distanceToSqr(this.owner) >= COMBAT_LEASH_DISTANCE_SQR) {
            this.pet.setTarget(null);
        }
    }

    @Override
    public void stop() {
        this.owner = null;
        this.pet.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.pet.getLookControl().setLookAt(this.owner, 10.0F, this.pet.getMaxHeadXRot());
        if (--this.recalcPathIn > 0) {
            return;
        }
        this.recalcPathIn = this.adjustedTickDelay(10);
        if (this.pet.distanceToSqr(this.owner) >= TELEPORT_DISTANCE_SQR) {
            this.hopToOwner();
        } else {
            this.pet.getNavigation().moveTo(this.owner, SPEED);
        }
    }

    private void hopToOwner() {
        BlockPos ownerPos = this.owner.blockPosition();
        int[] dyOffsets = {0, -1, 1};
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = this.pet.getRandom().nextIntBetweenInclusive(-3, 3);
            int dz = this.pet.getRandom().nextIntBetweenInclusive(-3, 3);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }
            for (int dy : dyOffsets) {
                BlockPos target = ownerPos.offset(dx, dy, dz);
                if (this.isSafeLanding(target)) {
                    this.pet.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5,
                            this.pet.getYRot(), this.pet.getXRot());
                    this.pet.getNavigation().stop();
                    return;
                }
            }
        }
    }

    private boolean isSafeLanding(BlockPos target) {
        if (!this.pet.level().getBlockState(target.below()).isSolid()) {
            return false;
        }
        BlockPos offset = target.subtract(this.pet.blockPosition());
        return this.pet.level().noCollision(this.pet, this.pet.getBoundingBox().move(offset));
    }
}
