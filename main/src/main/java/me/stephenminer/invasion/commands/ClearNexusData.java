package me.stephenminer.invasion.commands;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.nexus.Nexus;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearNexusData implements CommandExecutor {
    private final Invasion plugin;

    public ClearNexusData(){
        this.plugin = JavaPlugin.getPlugin(Invasion.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("invasion.commands.clear-nexus-data")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        if (!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return false;
        }
        Player player = (Player) sender;
        World world = player.getWorld();
        Chunk[] chunks = world.getLoadedChunks();
        for (Chunk chunk : chunks){
            PersistentDataContainer container = chunk.getPersistentDataContainer();
            container.remove(Nexus.POS_KEY);
        }
        player.sendMessage(ChatColor.GREEN + "Wiped nexus data in loaded chunks");
        return true;
    }
}
