package me.stephenminer.v1_21_R1.pathfinder;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public class Node {
    protected Node parent;
    protected int x, y, z;
    protected BlockPos pos;
    protected double cost, heuristic;
    protected boolean closed;
    protected BlockPos[] digTargets, buildTargets;

    public Node (Node parent, BlockPos pos, double cost, double heuristic){
        this.parent = parent;
        this.pos = pos;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.cost = cost;
        this.heuristic = heuristic;
        this.closed = false;
    }

    public double totalCost(){ return cost + heuristic; }

    public double distSqr(int x, int y, int z){
        return Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2) + Math.pow(this.z - z, 2);
    }

    public int x(){ return x; }
    public int y(){ return y; }
    public int z(){ return z; }
    public double cost(){ return cost; }
    public double heuristic(){ return heuristic; }
    public boolean closed(){ return closed; }
    public BlockPos pos(){ return pos; }

    @Override
    public boolean equals(Object other){
        if (other instanceof Node){
            Node node = (Node) other;
            return this.x == node.x && this.y == node.y && this.z == node.z && this.cost == node.cost && this.heuristic == node.heuristic;
        }
        return false;
    }
}
