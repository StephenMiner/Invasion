package me.stephenminer.v1_21_R1.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class InvasionPathfinder {
    private final Level world;
    private final int maxFallDist;
    private final Set<Block> blacklist;

    public InvasionPathfinder(Level world, int maxFallDist, Set<Block> blacklist){
        this.world = world;
        this.maxFallDist = maxFallDist;
        this.blacklist = blacklist;
    }

    public List<Node> findPath(BlockPos start, BlockPos goal){
        NodeHeap open = new NodeHeap(512);
        NodeMap visited = new NodeMap(2048);
        Node startNode = new Node(null, start, 0, 0);
        open.push(startNode);
        int nodes = 0;
        while (!open.empty()){
            Node current = open.pop();
            //if (current.closed) continue;
          //  current.closed = true;

            if (current.pos().equals(goal)) {
                return reconstructPath(current);
            }
            Node best = visited.get(current.pos.asLong());
            if (best != null && !current.equals(best)) continue;
            BlockPos[] neighbors = neighbors(current.pos);
            for (BlockPos pos : neighbors){
                int dy = pos.getY() - current.y;
                BlockPos aboveHead = current.pos.above();

              //  boolean digAbove = dy > 0 && !walkable(world.getBlockState(aboveHead));
              //  boolean digFront = dy < 0 && !walkable(world.getBlockState(current.pos.));
                BlockPos digExtra = digExtraCeiling(dy, current.pos, pos);
                BlockPos above = pos.above();
                BlockPos below = pos.below();
                BlockState state = world.getBlockState(pos);
                BlockState stateAbove = world.getBlockState(above);
                BlockState stateBelow = world.getBlockState(below);
                Node node = null;
                double digCost = 0;
                if (walkable(stateAbove) && walkable(state) && isSolid(below, stateBelow)){
                    node = buildNode(current, pos, goal);
                    if (digExtra != null){
                        node.digTargets = new BlockPos[]{digExtra};
                        digCost += determineDigCost(node.digTargets);
                    }
                }else if (walkable(stateAbove) && !walkable(state) && isSolid(below, stateBelow)){
                    if (!canDig(state)) continue;
                    node = buildNode(current, pos, goal);
                    if (digExtra != null)
                        node.digTargets = new BlockPos[]{digExtra, pos};
                    else
                        node.digTargets = new BlockPos[]{pos};
                    digCost += determineDigCost(node.digTargets);
                }else if (!walkable(stateAbove) && walkable(state) && isSolid(below, stateBelow)){
                    if (!canDig(stateAbove)) continue;
                    node = buildNode(current, pos, goal);
                    if (digExtra != null)
                        node.digTargets = new BlockPos[]{digExtra, above};
                    else node.digTargets = new BlockPos[]{above};
                    digCost += determineDigCost(node.digTargets);
                }else if (!walkable(stateAbove) && !walkable(state) && isSolid(below, stateBelow)){
                    if (!canDig( stateAbove) || !canDig(state)) continue;
                    node = buildNode(current, pos, goal);
                    if (digExtra != null)
                        node.digTargets = new BlockPos[]{digExtra, above, pos};
                    else node.digTargets = new BlockPos[]{above, pos};
                    digCost += determineDigCost(node.digTargets);
                }
                if (node == null) continue;
                if (digCost < 0) continue;
                node.cost += current.cost + digCost;
                long key = pos.asLong();
                Node onFile = visited.get(key);
                if (onFile == null || node.cost < onFile.cost){
                    visited.put(key, node);
                    open.push(node);
                }
            }
        }
        return null;
    }


    private Node buildNode(Node current, BlockPos pos, BlockPos goal){
        double cost = 1.0;
        double heuristic = heuristic(pos, goal);
       // Node node = discovered.get(pos.asLong());
        return new Node(current, pos, cost, heuristic);
    }

    private float determineDigCost(BlockPos... positions){
        float sum = 0;
        for (BlockPos pos : positions){
            if (world.getBlockState(pos).destroySpeed < 0) return -1;
            sum += world.getBlockState(pos).destroySpeed;
        }
        return 0.5f * sum;
    }


    /**
     * Gets the position of the block that we will need to dig out in order to reach
     * the specified future position
     * @param dy the difference in y-levels between the future position and current position
     * @param current where we currently are (as defined by our pathfinder)
     * @param future where we are trying to go next (as defined by our pathfinder)
     * @return A BlockPosition containing the position of the block we need to dig to allow us to
     *         to reach the future position. Checks cases for stairs only. Returns null if no digging needed
     */
    private BlockPos digExtraCeiling(int dy, BlockPos current, BlockPos future){
        BlockPos pos = null;
        if (dy >= 1 && !walkable(world.getBlockState(current.above().above()))){
            pos = current.above().above();
        }else if (dy <= -1 && !walkable(world.getBlockState(future.above().above()))){
            pos = future.above().above();
        }
        return pos;
    }

    private BlockPos[] neighbors(BlockPos pos){
        BlockPos[] positions = new BlockPos[12];
        positions[0] = pos.north();
        positions[1] = pos.south();
        positions[2] = pos.east();
        positions[3] = pos.west();

        positions[4] = pos.north().above();
        positions[5] = pos.east().above();
        positions[6] = pos.west().above();
        positions[7] = pos.south().above();

        positions[8] = pos.north().below();
        positions[9] = pos.east().below();
        positions[10] = pos.south().below();
        positions[11] = pos.west().below();
        return positions;
    }

    private boolean walkable(BlockState state){
        return state.isAir() || state.canBeReplaced();
    }

    private List<Node> reconstructPath(Node node){
        List<Node> path = new ArrayList<>();
        while (node != null){
            path.add(node);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private double heuristic(BlockPos pos1, BlockPos pos2){
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()) + Math.abs(pos1.getZ() - pos2.getZ());
    }

    private double heuristic(int x, int y, int z, BlockPos target){
        return Math.abs(x - target.getX()) + Math.abs(y - target.getY()) + Math.abs(z - target.getZ());
    }

    public boolean isSolid(BlockPos pos, BlockState state){
        return !state.isAir() && !state.getCollisionShape(world, pos).isEmpty();
    }

    public boolean canDig(BlockState state){
        return !blacklist.contains(state.getBlock()) && !state.isAir() && !liquid(state);
    }

    public boolean liquid(BlockState state){
        return state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }

    public boolean canBridge(BlockState state){
        return state.isAir() || state.canBeReplaced() || liquid(state);
    }
}
