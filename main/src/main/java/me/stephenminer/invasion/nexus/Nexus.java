package me.stephenminer.invasion.nexus;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.entity.MobType;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Location;

public class Nexus {
    public static NamespacedKey POS_KEY = new NamespacedKey(JavaPlugin.getPlugin(Invasion.class), "nexus-loc");
    public static NamespacedKey ITEM_KEY = new NamespacedKey(JavaPlugin.getPlugin(Invasion.class),"nexus-item");
    private final Invasion plugin;
    private final Location loc;
    private Catalyst catalyst;
    private int health, maxHealth;

    public Nexus(Location loc, int maxHealth){
        this.plugin = JavaPlugin.getPlugin(Invasion.class);
        this.loc = loc;
        this.maxHealth = maxHealth;
        this.health = maxHealth;

    }


    public void testSpawn(Location l){
        MobType.instance(MobType.IZOMBIE, l, this);
    }

    public Location loc(){ return loc; }
    public Catalyst catalyst(){ return catalyst; }



    public enum Catalyst{
        UNSTABLE,
        STABLE
    }
}
