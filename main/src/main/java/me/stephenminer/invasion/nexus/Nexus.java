package me.stephenminer.invasion.nexus;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.entity.MobType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Location;

import java.util.UUID;

public class Nexus {
    public static NamespacedKey POS_KEY = new NamespacedKey(JavaPlugin.getPlugin(Invasion.class), "nexus-loc");
    public static NamespacedKey ITEM_KEY = new NamespacedKey(JavaPlugin.getPlugin(Invasion.class),"nexus-item");
    private final Invasion plugin;
    private final Location loc;
    private final UUID uuid;
    private Catalyst catalyst;
    private int health, maxHealth;

    public Nexus(Location loc, int maxHealth){
        this(loc, maxHealth, maxHealth, UUID.randomUUID());
    }

    public Nexus(Location loc, int maxHealth, int health, UUID uuid){
        this.plugin = JavaPlugin.getPlugin(Invasion.class);
        this.loc = loc;
        this.maxHealth = maxHealth;
        this.health = health;
        this.uuid = uuid;
    }


    public void testSpawn(Location l){
        MobType.instance(MobType.IZOMBIE, l, this);
    }

    public void setCatalyst(Catalyst catalyst){
        this.catalyst = catalyst;
    }

    public Location loc(){ return loc; }
    public Catalyst catalyst(){ return catalyst; }


    public enum Catalyst{
        UNSTABLE((byte) 1),
        STABLE((byte) 2);


        public static Catalyst fromByte(byte encoding){
            switch (encoding){
                case 1:
                    return Catalyst.UNSTABLE;
                case 2:
                    return Catalyst.STABLE;
                default:
                    return null;
            }
        }

        private final byte encoding;

        private Catalyst(byte encoding){
            this.encoding = encoding;
        }


        public byte encoding(){ return encoding; }
    }
}
