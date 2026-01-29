package me.stephenminer.invasion.entity;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.nexus.Nexus;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface InvasionMob {
    public static NamespacedKey MOB_TYPE_KEY = new NamespacedKey(JavaPlugin.getPlugin(Invasion.class),"mob-type");
    public static NamespacedKey NEXUS_KEY = new NamespacedKey( JavaPlugin.getPlugin(Invasion.class), "nexus-mob-key");

    default void implantMobType(){
        Mob mob = bukkitMob();
        PersistentDataContainer container = mob.getPersistentDataContainer();
        Invasion plugin = JavaPlugin.getPlugin(Invasion.class);
        container.set(InvasionMob.MOB_TYPE_KEY, PersistentDataType.STRING, mobType().name());
    }

    default void implantNexusData(){
        Mob mob = bukkitMob();
        PersistentDataContainer container = mob.getPersistentDataContainer();
        UUID uuid = nexusUUID();
        if (uuid == null) return;
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        container.set(InvasionMob.NEXUS_KEY, PersistentDataType.BYTE_ARRAY, buffer.array());
    }

    public double x();
    public double y();
    public double z();
    public Location loc();
    public Mob bukkitMob();
    public MobType mobType();
    public UUID nexusUUID();

    public void setTargetPos(int x, int y, int z);
}
