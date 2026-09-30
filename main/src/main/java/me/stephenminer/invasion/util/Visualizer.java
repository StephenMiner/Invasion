package me.stephenminer.invasion.util;

import me.stephenminer.invasion.Invasion;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public interface Visualizer {


    /**
     *
     * @param start
     * @param goal
     */
    public void visualizePath(Location start, Location goal);

    public void stopVisualization();

    public boolean visualizing();

    public List<int[]> movementPoints();
    public List<int[]> constructionPoints();
    public List<int[]> diggingPoints();

    default void run(Location start, Location goal){
        visualizePath(start, goal);
        World world = start.getWorld();
        new BukkitRunnable(){
            @Override
            public void run(){
                if (!visualizing()){
                    this.cancel();
                    return;
                }
                for (int[] item : movementPoints()){
                    Location loc = centerLoc(world, item);
                    world.spawnParticle(Particle.FLAME, loc, 0);
                }
                for (int[] item : diggingPoints()){
                    Location loc = centerLoc(world, item);
                    world.spawnParticle(Particle.VILLAGER_HAPPY, loc, 0);
                }
                for (int[] item : constructionPoints()){
                    Location loc = centerLoc(world, item);
                    world.spawnParticle(Particle.CRIT, loc, 0);
                }
            }
        }.runTaskTimer(JavaPlugin.getPlugin(Invasion.class),1,1);
    }

    default Location centerLoc(World world, int[] coordinates){
        return new Location(world, coordinates[0] + 0.5, coordinates[1] + 0.5, coordinates[2] + 0.5);
    }
}

