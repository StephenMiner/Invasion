package me.stephenminer.invasion.commands;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.entity.MobType;
import me.stephenminer.invasion.nexus.Nexus;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnMonster implements CommandExecutor, TabCompleter {
    private final Invasion plugin;

    public SpawnMonster(){
        this.plugin = JavaPlugin.getPlugin(Invasion.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("invasion.commands.spawn")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        if (!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "You need to be a player to use this command!");
            return false;
        }
        if (args.length < 1){
            sender.sendMessage(ChatColor.RED + "Please specify which mob you want to spawn in");
            return false;
        }
        String mobName = args[0];

        MobType type = genType(mobName);

        if (type == null){
            sender.sendMessage(ChatColor.RED + mobName + " is not a valid invasion monster type!");
            return false;
        }
        Player player = (Player) sender;


        for (Nexus nexus : Invasion.nexusMap.values()){
            nexus.testSpawn(player.getLocation(), type);
        }
        sender.sendMessage(ChatColor.GREEN + "Spawned in monsters at all nexuses!");
        return true;
    }

    private MobType genType(String mobName){
        try{
            return MobType.valueOf(mobName);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        if (args.length == 1)
            return mobTypes(args[0]);
        return null;
    }


    private List<String> mobTypes(String match){
        return plugin.filter(Arrays.stream(MobType.values()).map(MobType::name).collect(Collectors.toSet()), match);
    }
}
