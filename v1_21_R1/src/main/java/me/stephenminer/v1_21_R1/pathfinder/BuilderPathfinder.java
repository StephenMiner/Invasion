package me.stephenminer.v1_21_R1.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        // -1 is so that the place at the entities foot position will also have a pillar + ladder if they are just starting the scaffold. Otherwise we don't do this
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


    /**
     * Determines where platforms should be formed on a given path based on calculated scaffold heights
     * @param path
     * @return
     */
    private Set<Node> determinePlatforms(List<Node> path){
        if (path.isEmpty()) return Set.of();
        Set<Node> platformNodes = new HashSet<>();
        int scaffoldHeight = 0;
        Node prev = path.get(0);
        for (int i = 1; i < path.size(); i++){
            Node node = path.get(i);
            if (nodeDyOnly(node, prev) && scaffoldNode(node) && scaffoldNode(prev)) {
                if (scaffoldHeight % 4 == 0)
                    platformNodes.add(node);
                scaffoldHeight++;
            }else scaffoldHeight = 0;
        }
        return platformNodes;
    }

    private BlockPos[] platformPositions(BlockPos pos){
        return new BlockPos[]{
                // Cardinal direction positions
            pos.north(),
                pos.east(),
                pos.south(),
                pos.west(),

                // Diagonal positions (corners)
                pos.north().east(),
                pos.south().east(),
                pos.south().west(),
                pos.north().west()
        };
    }

    private void injectPlatforms(Node node){
        BlockPos[] positions = platformPositions(node.pos);
        List<Node> additions = new ArrayList<>();
        for (BlockPos pos : positions){
            // Skip block pos if it is already in build target array
            boolean skip = false;
            if (node.buildTargets != null){
                for (BlockPos taken : node.buildTargets){
                    if (pos.equals(taken)){
                        skip = true;
                        break;
                    }
                }
            }
            if (skip) continue;
            // Skip Block pos if the pos already contains a ladder or a solid block
            BlockState state = world.getBlockState(pos);
            if (isSolid(pos, state) && !isLadder(state)) continue;
            additions.add(node);
        }
        int additionSize = additions.size();
        int sizes = additionSize + (node.buildTargets == null ? 0 : node.buildTargets.length);
        BlockPos[] targets = new BlockPos[sizes];
        BlockState[] states = new BlockState[sizes];
        final BlockState oakState = Blocks.OAK_PLANKS.defaultBlockState();

        // add additional build targets first, we probably want the platform constructed before anything else...
        int i;
        for (i = 0; i < additions.size(); i++){
            targets[i] = additions.get(i).pos;
            states[i] = oakState;
        }

        // Restore original build decisions to end of array
        for ( ; i < targets.length; i++){
            targets[i] = node.buildTargets[i - additionSize];
            states[i] = node.buildMats[i - additionSize];
        }

        node.buildTargets = targets;
        node.buildMats = states;
    }

    private boolean nodeDyOnly(Node node1, Node node2){
        return node2.y - node1.y > 0 && node2.x == node1.x && node2.z == node1.z;
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
