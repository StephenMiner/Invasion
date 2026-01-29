package me.stephenminer.v1_21_R1.pathfinder;

import me.stephenminer.invasion.nexus.Nexus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class InvasionGoal extends Goal {
    protected final Mob mob;
    protected final int maxBuildTime = 40;
    protected BlockPos targetPos;
    protected List<Node> path;
    protected int actionCooldown = 40;
    protected int stepIndex = 0;
    protected int digIndex, buildIndex;
    protected int breakProg, buildProg = 0;
    protected int maxBreakTime = 40;
    protected boolean digging = false;
    protected boolean moveFlag = false;
    protected int recalcCooldown = 0;
    protected Vec3 prevPos = null;
    protected int stuck = 0;

    protected final InvasionPathfinder pathfinder;
    protected static final int MAX_STUCK_TIME = 40;
    protected static final double MAX_STUCK_THRESHHOLd = 0.05;

    public InvasionGoal(Mob mob){
        this.mob = mob;
        this.pathfinder = new InvasionPathfinder(mob.level(),2, Set.of());
    }

    @Override
    public void start(){
        recalcPath();
        stepIndex = 0;
        prevPos = mob.position();
        stuck = 0;
        breakProg = 0;
        buildProg = 0;
        recalcCooldown = 0;
        digIndex = 0;
        buildIndex = 0;
    }

    @Override
    public boolean canUse(){
        return targetPos != null && path != null && stepIndex < path.size();
    }

    @Override
    public void tick(){
        if (!digging && actionCooldown > 0){
            actionCooldown --;
        }
        if (digging && breakProg < maxBreakTime)
            breakProg++;
        if (!digging && buildProg < maxBuildTime)
            buildProg++;
        if (stepIndex >= path.size())
            return;
        Node current = path.get(stepIndex);
        Level level = mob.level();
        int numDig = current.digTargets != null ? current.digTargets.length : 0;
        int numBuild = current.buildTargets != null ? current.buildTargets.length : 0;
        if (digIndex >= numDig && buildIndex >= numBuild){
            moveFlag = true;
            digIndex = 0;
            buildIndex = 0;
        }

        if (!moveFlag){
            if (digIndex < numDig){
                mob.getNavigation().stop();
                digging = true;
                if (breakProg % 10 == 0)
                    mob.swing(InteractionHand.MAIN_HAND);
                if (breakProg < maxBreakTime) return;
                digging = false;
                level.destroyBlock(current.digTargets[digIndex], true, mob);
                digIndex++;
                if (digIndex < numBuild)
                    maxBreakTime = breakTicks(current.digTargets[digIndex]);
                breakProg = 0;
            }
            if (!digging && buildIndex < numBuild){
                mob.getNavigation().stop();
                if (buildProg % 10 == 0)
                    mob.swing(InteractionHand.MAIN_HAND);
                if (buildProg < maxBuildTime) return;
                level.setBlockAndUpdate(current.buildTargets[buildIndex], Blocks.OAK_PLANKS.defaultBlockState());
                buildIndex++;

                buildProg = 0;
            }
        }else{
            Vec3 nextPos = getEntityPosAtNode(mob, stepIndex);
            mob.getMoveControl().setWantedPosition(nextPos.x, nextPos.y, nextPos.z, 1.0f);
            if (mob.position().distanceToSqr(prevPos) < MAX_STUCK_THRESHHOLd){
                mob.getMoveControl().setWantedPosition(current.pos.getX() + 0.5, current.pos.getY() + 0.5, current.pos.getZ() + 0.5, 1.0f);
                stuck++;
            }else stuck = 0;
            if (mob.blockPosition().equals(current.pos)){
                stepIndex++;
                moveFlag = false;
                stuck = 0;
            }
        }
    }

    public Vec3 getEntityPosAtNode(Entity entity, int index){
        Node node = this.path.get(index);
        double x = (double) node.x + (double)((int) (entity.getBbWidth() + 1.0F)) * 0.5;
        double y = (double) node.y;
        double z = (double) node.z + (double)((int) (entity.getBbWidth() + 1.0F)) * 0.5;
        return new Vec3(x, y, z);
    }

    public void recalcPath(){
        path = pathfinder.findPath(mob.blockPosition(), targetPos);
        stepIndex = 0;
        digIndex = 0;
        buildIndex = 0;
        buildProg = 0;
        breakProg = 0;
        stuck = 0;
        digging = false;
        moveFlag = false;
    }

    private int breakTicks(BlockPos pos){
        return (int) (2 * mob.level().getBlockState(pos).destroySpeed);
    }

    public void setTargetPos(BlockPos pos){
        this.targetPos = pos;
        recalcPath();
    }
}
