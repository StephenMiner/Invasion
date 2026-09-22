package me.stephenminer.v1_21_R1.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BuilderPathfinder extends InvasionPathfinder{


    public BuilderPathfinder(Level world, int fallDist, Set<Block> blacklist){
        super(world, fallDist, blacklist);
    }

    @Override
    public List<Node> findPath(BlockPos start, BlockPos goal){
        List<Node> path = super.findPath(start, goal);
      //  System.out.println(path);
        addPlatforms(path, goal);
        return path;
    }

    @Override
    public Node evalPosition(BlockPos pos, BlockPos goal, Node current){
        if (!pos.equals(current.pos.above())) {
            //TODO: Insert straight down check, but ensure some kind of drop. Then run drop down ladder evaluator.
            Node dropNode = evalDropLadder(pos, goal, current); // null if not straight down, or impossible to generate
            if (pos.equals(current.pos.below())) return null; // Unsupported movement
            Node regular = super.evalPosition(pos, goal, current);
            return regular == null ? evalBridgeRoute(pos, goal, current) : regular;
        }
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
            tower = evalLadderNode(pos, goal, tower, dir, i);
            if (tower == null) return null;
        }
        // tower can never equal current by the time we reach this, so no need to worry 'bout cost doubling.
        tower.cost += current.cost;
        return tower;
    }

    private Node evalLadderNode(BlockPos pos, BlockPos goal, Node current, Direction dir, int upOffset){
        // No dig extra because we are only in a pure upwards direction
        BlockPos above = pos.above();

        BlockState state = world.getBlockState(pos);
        BlockState stateAbove = world.getBlockState(above);

        BlockPos pillar = pos.relative(dir).relative(Direction.UP, upOffset);
        BlockState pillarState = world.getBlockState(pillar);

        Node node = buildNode(current, pos, goal);
        BlockState ladderState = Blocks.LADDER.defaultBlockState();
        ladderState = ladderState.setValue(LadderBlock.FACING, dir.getOpposite());
        BlockPos[] digTargets = null;
        float digCost = 0;
        if (walkable(stateAbove) && walkable(state)){
            // free space, may delete branch
        }else if (!walkable(stateAbove) && walkable(state)){
            if (!canDig(stateAbove)) return null;
            digTargets = new BlockPos[]{above};
        }else if (walkable(stateAbove) && !walkable(state)){
            if (!canDig(state)) return null;
            digTargets = new BlockPos[]{pos};
        }else if (!walkable(state) && !walkable(stateAbove)){
            if (!canDig(state) || !canDig(stateAbove)) return null;
            digTargets = new BlockPos[]{pos, above};
        }
        if (!canBridge(pillarState)) {
            node.buildTargets = new BlockPos[]{pos};
            node.buildMats = new BlockState[]{ladderState};
        }else{
            node.buildTargets = new BlockPos[]{pillar, pos};
            node.buildMats = new BlockState[]{Blocks.OAK_PLANKS.defaultBlockState(), ladderState};
        }
        digCost += determineDigCost(digTargets);
        if (digCost < 0) return null;
        node.digTargets = digTargets;
        // defer addition of final cost to avoid over-penalizing tower creation.
        node.cost += (int) digCost;
        return node;
    }


    /**
     * PRECONDITION
     * The Node generated from super.evaluatePos MUST be null
     * This function assumes that horizontal movement to the proposed position is impossible
     * This happens if the positon is unreachable due to bedrock like conditions or a empty foot level
     *
     *
     * @param pos
     * @param goal
     * @param current
     * @return
     */

    private Node evalBridgeRoute(BlockPos pos, BlockPos goal, Node current){
        int dy = pos.getY() - current.y;
        BlockPos digExtra = digExtraCeiling(dy, current.pos, pos);
        BlockPos above = pos.above();
        BlockPos below = pos.below();

        BlockState state = world.getBlockState(pos);
        BlockState stateAbove = world.getBlockState(above);
        BlockState stateBelow = world.getBlockState(below);
        Node node = null;
        double digCost = 0;
        node = buildNode(current, pos, goal);
        // This is a case that we assume to already have been covered. Not a valid move target
        if (isSolid(below, stateBelow) || !canBridge(stateBelow))
            return null;
        double buildCost = 2;
        BlockPos[] buildTargets = new BlockPos[]{below};
        BlockState[] buildStates =  new BlockState[]{Blocks.OAK_PLANKS.defaultBlockState()};

        node = buildNode(current, pos, goal);
        if (walkable(stateAbove) && walkable(state)){
            if (digExtra != null){
                node.digTargets = new BlockPos[]{digExtra};
                digCost += determineDigCost(node.digTargets);
            }
        }else if (walkable(stateAbove) && !walkable(state)){
            if (!canDig(stateAbove)) return null;
            if (digExtra != null){
                node.digTargets = new BlockPos[]{digExtra, pos};
            }else node.digTargets = new BlockPos[]{pos};
            digCost += determineDigCost(node.digTargets);
        }else if (!walkable(stateAbove) && walkable(state)){
            if (!canDig(stateAbove)) return null;
            if (digExtra != null){
                node.digTargets = new BlockPos[]{digExtra, above};
            }else node.digTargets = new BlockPos[]{above};
            digCost += determineDigCost(node.digTargets);
        }else if (!walkable(stateAbove) && !walkable(state)){
            if (!canDig(stateAbove) || !canDig(state)) return null;
            if (digExtra != null)
                node.digTargets = new BlockPos[]{digExtra, above, pos};
            else node.digTargets = new BlockPos[]{above, pos};
            digCost += determineDigCost(node.digTargets);
        }else node = null;

        if (digCost < 0) return null;
        if (node != null) {
            node.cost += (int) (current.cost + digCost + 2); // the 2 is for bridge tax
            node.buildMats = buildStates;
            node.buildTargets = buildTargets;
        }
        return node;
    }

    /**
     *   Assumed to be a down-xz-direction. Straight downs shouldn't pass the evaluator
     *   that is, where H is where we are (current) and P is the proposed location
     *   .H.
     *   ..P
     *   ...
     *   We break if the current pos is not air
      */

    private Node evalDropLadder(BlockPos pos, BlockPos goal, Node current){
        int y = pos.getY();
        int x = pos.getX();
        int z = pos.getZ();
        Node dropDown = current;
        Direction returnDir = detDropLadderDir(current.pos, pos);
        if (returnDir == null){
            System.out.println("Failed to calculate return dir:" + current.pos + "," + pos);
            return null;
        }
        for (;y > world.getMinBuildHeight(); y--){
            BlockPos ladder = new BlockPos(x, y, z);
            if (!world.getBlockState(ladder).isAir()) break;
            dropDown = buildNode(dropDown,  pos, goal);
            BlockPos pillar = ladder.relative(returnDir);
            BlockPos[] buildTargets;
            BlockState[] buildStates;
            BlockState ladderState = Blocks.LADDER.defaultBlockState();
            ladderState.setValue(LadderBlock.FACING, returnDir);
            if (world.getBlockState(pillar).isAir()){
                buildTargets = new BlockPos[]{pillar, ladder};
                buildStates = new BlockState[]{Blocks.OAK_PLANKS.defaultBlockState(), ladderState};
            }else{
                buildTargets = new BlockPos[]{ladder};
                buildStates = new BlockState[]{ladderState};
            }
            dropDown.buildTargets = buildTargets;
            dropDown.buildMats = buildStates;
            dropDown.cost += 1;// kind of irrelevant to be honest
        }

        /*
         note to self, this is like a wormhole of generating cost. We only apply the historial cost to the final node which gets added to stack.
         This also lessens the penalty for this as well.
         */
        if (dropDown == current) return null; // No legal dropdown pillar could be formed?
        dropDown.cost += current.cost + 2;
        return dropDown;
    }

    /**
     *  Calculates the dropdown ladder pillar direction relative to where the "drop" is
     * @return    The direction we need to apply to proposed ladder-pillar positions
     *          to get back to the original x,z from before we starting pillaring down.
     *          ex.
     *          If we are moving East, we hope to see West be the direction returned.
     */
    private Direction detDropLadderDir(BlockPos current, BlockPos proposed){
        int dx = current.getX() - proposed.getX();
        int dz = current.getZ() - proposed.getZ();

        return  Direction.fromDelta(dx, 0, dz);
    }

    private boolean checkParentOverlap(Node current, BlockPos toAdd){
        if (current.parent == null || current.parent.buildTargets == null) return false;
        for (int i = 0; i < current.buildTargets.length; i++){
            BlockPos pos = current.buildTargets[i];
            if (toAdd.equals(pos)) return true;
        }
        return false;
    }


    /**
     * Determines where platforms should be formed on a given path based on calculated scaffold heights
     * @param path
     * @return
     */
    private void addPlatforms(List<Node> path, BlockPos goal){
        if (path.isEmpty()) return;
        int scaffoldHeight = 0;
        int cached = 0;
        Node prev = path.get(0);
        for (int i = 1; i < path.size(); i++){
            Node node = path.get(i);
            System.out.println(nodeDyOnly(node, prev));
            if (nodeDyOnly(node, prev) && scaffoldNode(node) && scaffoldNode(prev)) {
                System.out.println("DY VAL: " + (prev.y - node.y));
                if (scaffoldHeight != 0 && scaffoldHeight % 4 == 0) {
                    System.out.println("INJECTING");
                    injectPlatforms(node, path, i, cached, goal);
                    cached = i;
                }else System.out.println("HEIGHT: " + scaffoldHeight);
                scaffoldHeight++;
            }else scaffoldHeight = 0;
            prev = node;
        }
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

    private void injectPlatforms(Node node, List<Node> path, int index, int lastIndex, BlockPos goal){
        BlockPos[] positions = platformPositions(node.pos);
        List<BlockPos> additions = new ArrayList<>();
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
            additions.add(pos);
        }
        System.out.println("Generating " + additions.size() + " Platforms");
        int additionSize = additions.size();
        int sizes = additionSize + (node.buildTargets == null ? 0 : node.buildTargets.length);
        BlockPos[] targets = new BlockPos[sizes];
        BlockState[] states = new BlockState[sizes];
        final BlockState oakState = Blocks.OAK_PLANKS.defaultBlockState();

        // add additional build targets first, we probably want the platform constructed before anything else...
        int i;
        for (i = 0; i < additions.size(); i++){
            targets[i] = additions.get(i);
            states[i] = oakState;
        }

        // Restore original build decisions to end of array
        for ( ; i < targets.length; i++){
            targets[i] = node.buildTargets[i - additionSize];
            states[i] = node.buildMats[i - additionSize];
        }

        node.buildTargets = targets;
        node.buildMats = states;

        addPlatformMoveNode(node, path , index, goal);
        compressBuilding(path, index, lastIndex);

    }

    private void compressBuilding(List<Node> path, int index, int lastIndex){
        List<BlockPos> posCopy = new ArrayList<>();
        List<BlockState> stateCopy = new ArrayList<>();
        for (int i = index; i > lastIndex; i--){
            Node node = path.get(i);
            BlockPos[] positions = node.buildTargets;
            if (positions == null) continue;
            BlockState[] states = node.buildMats;
            int len = positions.length;
            if (len == 0) continue;
            for (int j = len - 1; j >= 0; j--){
                posCopy.add(0, positions[j]);
                stateCopy.add(0, states[j]);
            }
            node.buildTargets = null;
            node.buildMats = null;
        }
        Node current = path.get(lastIndex);
        current.buildTargets = posCopy.toArray(new BlockPos[0]);
        current.buildMats = stateCopy.toArray(new BlockState[0]);
    }


    /**
     * Analyzes build targets (assume they represent a platform construction node
     * Selects a random acceptable position to stand on.
     * @param parent
     */
    /*
        ~~TODO: entity chooses the one case I do not want it to choose consistently. Breaking the pillar block
            Maybe the movement nodes aren't injected properly?~~

            Looks fine now?
     */
    private void addPlatformMoveNode(Node parent, List<Node> path, int index, BlockPos goal){
        if (parent.buildTargets == null) return;
        BlockPos[] positions = parent.buildTargets;
        BlockState[] states = parent.buildMats;
        List<BlockPos> valid = new ArrayList<>();
        BlockPos pillar = pillarPos(states, positions);
        if (pillar == null){
            System.out.println("Failed to find a pillar position");
            return;
        }
        BlockPos position = parent.pos();
        BlockPos poleFoot = position.above();
        BlockPos poleHead = position.above().above();
        BlockState poleFtState = world.getBlockState(poleFoot);
        BlockState poleHdState = world.getBlockState(poleHead);

        if (!walkable(poleFtState) || !walkable(poleHdState)) {
            System.out.println("Injecting no nodes for movement onto platforms: Obstructed pole");
            return;
        }
        Node poleNode = buildNode(parent, poleFoot, goal);
        Set<BlockPos> validTargets = validTargets(parent.pos, pillar);
        for (int i = 0; i < positions.length; i++){
            BlockPos pos = positions[i];
            if (!validTargets.contains(pos)) continue;
            if (pos.getX() == pillar.getX() && pos.getZ() == pillar.getZ()) continue;
            BlockPos moveTarget = pos.above();
            BlockState state = states[i];
            BlockState footState = world.getBlockState(moveTarget);
            BlockState headState = world.getBlockState(pos.above().above());
            if (!isSolid(pos, state) || !walkable(footState) || !walkable(headState)) continue;
            valid.add(moveTarget);
        }

        Node node;
        if (valid.isEmpty()) {
            System.out.println("Generating dig node for movement injection");
            node = generateDigNode(parent, goal, validTargets);
            if (node == null){
                System.out.println("Something went wrong here...");
                return;
            }
        }else{
            BlockPos selection = valid.get(ThreadLocalRandom.current().nextInt(valid.size()));
            node = buildNode(poleNode, selection, goal);
        }
        // If the current node is at the end of the list,
        // just inject new node at end of the path
        if (index + 1 >= path.size()) {
            path.add(poleNode);
            path.add(node);
        }else{
            // If not at the end of the list, set the current i + 1 node's parent to injected node
            // Inject node into the i + 1 position
            Node child = path.get(index + 1);
            child.parent = node; // Cost is now corrupted...
            path.add(index + 1, poleNode);
            path.add(index + 2, node);

            System.out.println("Generating MOVE NODE");
            System.out.println("===" + node);
            looker.add(node);
        }
    }


    private Set<BlockPos> validTargets(BlockPos center, BlockPos pillar){
        Set<BlockPos> targets = new HashSet<>();
        targets.add(center.north());
        targets.add(center.east());
        targets.add(center.west());
        targets.add(center.south());
        targets.remove(pillar);
        return targets;
    }

    /**
     * Assumes that there are no valid standing positions
     * @param parent
     */
    private Node generateDigNode(Node parent, BlockPos goal, Set<BlockPos> targets){
        List<Node> nodes = new ArrayList<>();
        for (BlockPos pos : targets){
            BlockState futureState = stateInBuildArr(pos, parent);
            BlockPos feetPos = pos.above();
            BlockPos headPos = pos.above().above();
            BlockState feetState = world.getBlockState(feetPos);
            BlockState headState = world.getBlockState(headPos);
            BlockPos[] digTargets = null;
            BlockState[] buildMats = null;
            BlockPos[] buildTargets = null;
            if (!walkable(feetState) && walkable(headState))
                digTargets = new BlockPos[]{feetPos};
            else if (walkable(feetState) && !walkable(headState))
                digTargets = new BlockPos[]{headPos};
            else if (!walkable(feetState) && !walkable(headState))
                digTargets = new BlockPos[]{feetPos, headPos};
            if (futureState == null || !walkable(futureState)){
                buildTargets = new BlockPos[]{pos};
                buildMats = new BlockState[]{Blocks.OAK_PLANKS.defaultBlockState()};
            }
            Node node = buildNode(parent, pos, goal);
            double digCost = digTargets == null ? 0: determineDigCost(digTargets);
            double buildCost = buildTargets == null ? 0 : 1;
            node.cost += digCost + buildCost;
            node.digTargets = digTargets;
            node.buildTargets = buildTargets;
            node.buildMats = buildMats;
            nodes.add(node);
        }
        nodes.sort(Comparator.comparingInt(Node::totalCost));
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    private BlockPos pillarPos(BlockState[] buildMats, BlockPos[] positions){
        for (int i = 0; i < buildMats.length; i++){
            BlockState state = buildMats[i];
            if (!state.is(Blocks.LADDER)) continue;
            Direction pillarDir = state.getValue(LadderBlock.FACING).getOpposite();
            return positions[i].relative(pillarDir);
        }
        return null;
    }

    private BlockState stateInBuildArr(BlockPos pos, Node current) {
        if (current.buildTargets == null) return null;
        BlockPos[] positions = current.buildTargets;
        for (int i = 0; i < positions.length; i++) {
            BlockPos p = positions[i];
            if (p.equals(pos))
                return current.buildMats[i];
        }
        return null;
    }

    private boolean posDyOnly(Node node1, Node node2){
        return node2.y - node1.y > 0 && node2.x == node1.x && node2.z == node1.z;
    }

    private boolean negDyOnly(Node node1, Node node2){
        return node2.y - node1.y < 0 && node2.x == node1.x && node2.z == node1.z;
    }
    private boolean nodeDyOnly(Node node1, Node node2){
        return node2.y != node1.y && node2.x == node1.x && node2.z == node1.z;
    }

    private boolean scaffoldNode(Node node){
        if (node.buildMats== null) return false;
        for (int i = 0; i < node.buildMats.length; i++){
            BlockState block = node.buildMats[i];
            if (block.getBlock() == Blocks.LADDER) return true;
        }
        return false;
    }


}
