package me.stephenminer.invasion;

import me.stephenminer.invasion.commands.ClearNexusData;
import me.stephenminer.invasion.commands.TestNexus;
import me.stephenminer.invasion.entity.InvasionMob;
import me.stephenminer.invasion.entity.MobType;
import me.stephenminer.invasion.listener.NexusListener;
import me.stephenminer.invasion.nexus.Nexus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Invasion extends JavaPlugin {
    public static Map<UUID, Nexus> nexusMap;


    @Override
    public void onEnable() {
        // Plugin startup logic
        Invasion.nexusMap = new HashMap<>();
        registerEvents();
        addCommands();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Mob)) continue;
                Mob mob = (Mob) entity;
                attemptConvertMob(mob);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerEvents(){
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new NexusListener(), this);
    }

    private void addCommands(){
        getCommand("testnexus").setExecutor(new TestNexus());
        getCommand("clearnexusdata").setExecutor(new ClearNexusData());
    }



    public ItemStack nexusItem(){
        ItemStack item = new ItemStack(Material.RESPAWN_ANCHOR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Nexus");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(Nexus.ITEM_KEY, PersistentDataType.STRING, "nexus");
        item.setItemMeta(meta);
        return item;
    }



    public void attemptConvertMob(Mob mob){
        PersistentDataContainer container = mob.getPersistentDataContainer();
        if (!container.has(InvasionMob.NEXUS_KEY, PersistentDataType.BYTE_ARRAY)) return;
        byte[] uuidBytes = container.get(InvasionMob.NEXUS_KEY, PersistentDataType.BYTE_ARRAY);
        ByteBuffer buff = ByteBuffer.wrap(uuidBytes);
        UUID uuid = new UUID(buff.getLong(), buff.getLong());
        this.getLogger().info("Thing happening");
        InvasionMob invasionMob = MobType.copy(mob, uuid);
    }

}
