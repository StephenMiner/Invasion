package me.stephenminer.v1_21_R1.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class BuilderPathfinder extends InvasionPathfinder{

    public BuilderPathfinder(Level world, int fallDist, Set<Block> blacklist){
        super(world, fallDist, blacklist);

    }

    @Override
    public Node evalPosition(BlockPos pos, BlockPos goal, Node current){
        if (!pos.equals(current.pos.above()))
            return super.evalPosition(pos, goal, current);

        Direction dir = null;
        if (current.parent == null) dir = Direction.Plane.HORIZONTAL.getRandomDirection(world.getRandom());
        double dist = -1;
        for (Direction d : Direction.Plane.HORIZONTAL){
            double tempDist = pos.relative(d).distManhattan(goal);
            if (dist == -1 || tempDist < dist){
                dir = d;
                dist = tempDist;
            }
        }
        int segmentsToBuild = 4;
        if (current.buildTargets != null && scaffoldNode(current)){
            segmentsToBuild = 2;
        }

        Node tower = current;
        //complicated looking code final boss
        for (int i = segmentsToBuild != 4 ? 0 : -1; i < segmentsToBuild; i++){
            BlockPos pillar = pos.relative(dir).relative(Direction.UP, i);
            BlockState ladderState = Blocks.LADDER.defaultBlockState();
            ladderState = ladderState.setValue(LadderBlock.FACING, dir.getOpposite());
            BlockPos ladder = pos.relative(Direction.UP, i);
            tower = buildNode(tower, ladder, goal);
            tower.buildTargets = new BlockPos[]{pillar, ladder};
            tower.buildMats = new BlockState[]{Blocks.OAK_PLANKS.defaultBlockState(), ladderState};
            tower.cost += 1;
        }
        return tower;
    }


    private boolean scaffoldNode(Node node){
        if (node.digTargets == null || node.buildMats== null) return false;
        for (int i = 0; i < node.buildMats.length; i++){
            BlockState block = node.buildMats[i];
            if (block.getBlock() == Blocks.LADDER) return true;
        }
        return false;
    }


}
