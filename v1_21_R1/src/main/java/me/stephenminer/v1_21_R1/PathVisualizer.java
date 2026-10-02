package me.stephenminer.v1_21_R1;

import me.stephenminer.invasion.util.Visualizer;
import me.stephenminer.v1_21_R1.pathfinder.BuilderPathfinder;
import me.stephenminer.v1_21_R1.pathfinder.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PathVisualizer implements Visualizer {
    private boolean visualizing;
    private Level world;
    private BlockPos start, goal;

    private List<int[]> constructionPoints, diggingPoints, movementPoints;

    public PathVisualizer(){}


    @Override
    public void visualizePath(Location start, Location goal) {
        this.start = new BlockPos(start.getBlockX(), start.getBlockY(), start.getBlockZ());
        this.goal = new BlockPos(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());
        this.world = ((CraftWorld) start.getWorld()).getHandle();
        BuilderPathfinder pathfinder = new BuilderPathfinder(world, 1, Set.of());
        List<Node> nodes = pathfinder.findPath(this.start, this.goal);

        List<int[]> movementPoints = new ArrayList<>();
        List<int[]> diggingPoints = new ArrayList<>();
        List<int[]> constructionPoints = new ArrayList<>();

        for (Node node : nodes){
            BlockPos pos = node.pos();
            movementPoints.add(new int[]{pos.getX(), pos.getY(), pos.getZ()});
            addPoints(diggingPoints, node.digTargets());
            addPoints(constructionPoints, node.buildTargets());
        }
        this.movementPoints = movementPoints;
        this.diggingPoints = diggingPoints;
        this.constructionPoints = constructionPoints;
        visualizing = true;
    }

    @Override
    public void stopVisualization() {
        visualizing = false;
    }

    private void addPoints(List<int[]> pointList, BlockPos... positions){
        if (positions == null) return;
        for (BlockPos pos : positions){
            pointList.add(new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }
    }

    @Override
    public boolean visualizing(){ return visualizing; }

    @Override
    public List<int[]> movementPoints(){ return this.movementPoints; }

    @Override
    public List<int[]> diggingPoints(){ return this.diggingPoints; }

    @Override
    public List<int[]> constructionPoints(){ return this.constructionPoints; }
}
