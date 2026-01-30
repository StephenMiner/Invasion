package me.stephenminer.invasion;

import me.stephenminer.invasion.commands.ClearNexusData;
import me.stephenminer.invasion.commands.TestNexus;
import me.stephenminer.invasion.listener.NexusListener;
import me.stephenminer.invasion.nexus.Nexus;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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


}
