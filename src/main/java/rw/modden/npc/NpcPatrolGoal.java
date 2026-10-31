package rw.modden.npc;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class NpcPatrolGoal extends Goal {
    private static final int CHECK_TICKS = 40, MAX_DETOURS = 3;
    private final NpcEntity npc;
    private final double speed;
    private Vec3d lastPos = Vec3d.ZERO;
    private int ticks, detours;

    public NpcPatrolGoal(NpcEntity npc, double speed) {
        this.npc = npc;
        this.speed = speed;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() { return npc.getAiMode()==NpcEntity.AiMode.PATROL && !npc.getPatrolPoints().isEmpty(); }
    @Override
    public boolean shouldContinue() { return canStart(); }
    @Override
    public boolean shouldRunEveryTick() { return true; }
    @Override
    public void start() { resetProgress(); }
    @Override
    public void stop() { npc.getNavigation().stop(); }

    @Override
    public void tick() {
        BlockPos target = npc.currentPatrolPoint();
        if (npc.getBlockPos().isWithinDistance(target, 2.0) || detours>MAX_DETOURS) {
            npc.nextPatrolPoint();
            resetProgress();
            return;
        }
        if (npc.getNavigation().isIdle() && !npc.getNavigation().startMovingTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed)) {
            detours = MAX_DETOURS + 1;
            return;
        }
        if (++ticks<CHECK_TICKS) return;
        ticks = 0;
        double minMove = npc.isTouchingWater()?0.2:1.0;
        boolean moved = npc.getPos().squaredDistanceTo(lastPos)>minMove*minMove;
        lastPos = npc.getPos();
        if (moved) return;
        detours++;
        Vec3d aside = NoPenaltyTargeting.find(npc, 3, 1);
        if (aside != null) npc.getNavigation().startMovingTo(aside.x, aside.y, aside.z, speed);
    }

    private void resetProgress() {
        lastPos = npc.getPos();
        ticks = 0;
        detours = 0;
    }
}
