package me.stephenminer.invasion.commands;

import me.stephenminer.invasion.Invasion;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TestNexus implements CommandExecutor {
    private final Invasion plugin;
    public TestNexus(){
        this.plugin = JavaPlugin.getPlugin(Invasion.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("invasion.commands.test")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        if (!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "You need to be a player to use this command!");
            return false;
        }
        Player player = (Player) sender;
        player.getInventory().addItem(plugin.nexusItem());
        player.sendMessage(ChatColor.GREEN + "You were given a Nexus item");
        return true;
    }
}
