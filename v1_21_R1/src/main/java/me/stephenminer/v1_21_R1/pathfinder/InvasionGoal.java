package me.stephenminer.v1_21_R1.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

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
        this(mob, new InvasionPathfinder(mob.level(),2, Set.of()));
    }

    public InvasionGoal(Mob mob, InvasionPathfinder pathfinder){
        this.mob = mob;
        this.pathfinder = pathfinder;
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
        if (pathfinder.looker.contains(current)){
            System.out.println("==========wow========");
        }
        Level level = mob.level();
        int numDig = current.digTargets != null ? current.digTargets.length : 0;
        int numBuild = current.buildTargets != null ? current.buildTargets.length : 0;
        if (digIndex >= numDig && buildIndex >= numBuild){
            moveFlag = true;
            digIndex = 0;
            buildIndex = 0;
        }

        if (!moveFlag){
            if (mob.distanceToSqr(current.pos.getCenter()) > 12) {
                // kind of ew, but recalc path
                System.out.println("Triggered Recalc based of Distance");
                recalcPath();
                return;
            }
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
                BlockPos pos = current.buildTargets[buildIndex];
                BlockState state = current.buildMats[buildIndex];
                if (heightDif(mob.getY(), pos.getY()) > 5){
                    //mob.mo
                }
                mob.getNavigation().stop();
                if (buildProg % 10 == 0)
                    mob.swing(InteractionHand.MAIN_HAND);

                BlockState worldState = level.getBlockState(pos);
                if (pathfinder.isSolid(pos, level.getBlockState(pos)) && ((pathfinder.walkable(state) && !pathfinder.walkable(worldState)) || (!pathfinder.walkable(state) && pathfinder.walkable(worldState)))) {
                    // Someone placed a block where a walkable block was going to be placed
                    System.out.println("Path compromised! Block placed where entity wanted a block to go");
                    recalcPath();
                    return;
                }
                if (pathfinder.isSolid(pos, level.getBlockState(pos)) && !pathfinder.walkable(state) && !pathfinder.walkable(worldState)){
                    // Someone placed a solid block where a solid block was going to be placed, advance building one step
                    buildIndex++;
                    buildProg = 0;
                    return;
                }
                if (buildProg < maxBuildTime) return;
                level.setBlockAndUpdate(current.buildTargets[buildIndex], current.buildMats[buildIndex]);
                buildIndex++;

                buildProg = 0;
            }
        }else{
            Vec3 nextPos = getEntityPosAtNode(mob, stepIndex);
            if (isOnLadder()) {// && !isOnLadder(current.pos)
                /*
                if (!blockPosValid(current.pos)){
                    System.out.println("found bad position at node : " + current);
                    recalcPath();
                    return;
                }

                 */
                double dx = current.x + 0.5 - mob.getX();
                double dy = current.y + 0.5 - mob.getY();
                double dz = current.z + 0.5 - mob.getZ();
                setMobLadderMovement(level, current, nextPos);
              //  mob.setPos(current.x + 0.5, mob.getY() + dy * 0.25, current.z + 0.5); OG method
            }else mob.getMoveControl().setWantedPosition(nextPos.x, nextPos.y, nextPos.z, 1.0f);
            if (mob.position().distanceToSqr(prevPos) < MAX_STUCK_THRESHHOLd){
                mob.getMoveControl().setWantedPosition(current.pos.getX() + 0.5, current.pos.getY() + 0.5, current.pos.getZ() + 0.5, 1.0f);
                stuck++;
            }else stuck = 0;
            System.out.println(current);
            if (mob.blockPosition().equals(current.pos)){
                System.out.println(mob.blockPosition());
                stepIndex++;
                moveFlag = false;
                stuck = 0;
            }
        }
    }

    private void setMobLadderMovement(Level world, Node current, Vec3 nextPos){
        BlockPos pos = mob.blockPosition();
        Direction ladderDir = determineLadderDir(pos, world);
        if (ladderDir == null) return; // not on a ladder
        if (current.pos.getX() != pos.getX() || current.pos.getZ() != pos.getZ()) {
            double dx = nextPos.x - mob.getX();
            double dy = nextPos.y - mob.getY();
            double dz = nextPos.z - mob.getZ();
            Vec3 dir = new Vec3(dx, dy, dz).normalize();
            mob.setDeltaMovement(dir.multiply(0.5,0.5,0.5));
           // mob.getMoveControl().setWantedPosition(nextPos.x, nextPos.y, nextPos.z, 1.0f);
           // mob.getMoveControl().setWantedPosition( current.x + 0.5,  current.y + 0.5, current.z + 0.5, 1.0f);
            System.out.println("Non vert movement detectedddddd");
            return; // Need to perform non-vertical movement
        }

        double dx = ladderDir.getStepX() * 0.25;
        double dz = ladderDir.getStepZ() * 0.25;
      //  mob.setDeltaMovement(delta.x + dx, delta.y, delta.z + dz);
        Vec3 ladderMovement = new Vec3(dx, 0, dz);
        ladderMovement = applyCenteringCorrection(ladderMovement, mob.position(), nextPos);
        mob.setDeltaMovement(ladderMovement);

    }

    private Direction determineLadderDir(BlockPos mobPos, Level world){
        if (!isOnLadder()) return null;
        BlockState state = world.getBlockState(mobPos);
        // All ladders should have a facing property
        return state.getValue(LadderBlock.FACING).getOpposite();
    }

    private Vec3 applyCenteringCorrection(Vec3 current, Vec3 pos, Vec3 desired){
        double dx = desired.x - pos.x;
        double dz = desired.z - pos.z;
        Vec3 delta = new Vec3(dx, 0, dz);
        if (delta.length() > 1){
            delta = delta.normalize().multiply(0.25,0,0.25);
        }
        return current.add(delta);
    }

    private boolean blockPosValid(BlockPos pos){
        Level level = mob.level();
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        BlockState currentState = level.getBlockState(pos);
        BlockState aboveState = level.getBlockState(pos.above());
        return pathfinder.isSolid(pos, belowState) && pathfinder.walkable(currentState) && pathfinder.walkable(aboveState);
    }

    private boolean isOnLadder(BlockPos pos){
        BlockState state = this.mob.level().getBlockState(pos);
        return state.is(Blocks.LADDER);
    }

    private boolean isOnLadder(){
        BlockPos pos = this.mob.blockPosition();
        BlockState state = this.mob.level().getBlockState(pos);
        return state.is(Blocks.LADDER);
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

    private double heightDif(BlockPos target, BlockPos origin){
        return heightDif(target.getY(), origin.getY());
    }
    private double heightDif(double y1, double y2){
        return y1 - y2;
    }


    private int breakTicks(BlockPos pos){
        return (int) (2 * mob.level().getBlockState(pos).destroySpeed);
    }

    public void setTargetPos(BlockPos pos){
        this.targetPos = pos;
        recalcPath();
    }
}
