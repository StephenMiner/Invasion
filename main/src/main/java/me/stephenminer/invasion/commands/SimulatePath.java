package me.stephenminer.invasion.commands;

import me.stephenminer.invasion.Invasion;
import me.stephenminer.invasion.nexus.Nexus;
import me.stephenminer.invasion.util.Visualizer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimulatePath implements CommandExecutor {
    private final Invasion plugin;

    private Visualizer visualizer;

    public SimulatePath(){
        this.plugin = JavaPlugin.getPlugin(Invasion.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You need to be a player to use this command!");
            return false;
        }

        if (!sender.hasPermission("invasion.commands.visualizepath")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }

        if (args.length >= 1){
            String arg = args[0];
            if (arg.equalsIgnoreCase("stop") || arg.equalsIgnoreCase("off") || arg.equalsIgnoreCase("false")){
                visualizer.stopVisualization();
                sender.sendMessage(ChatColor.GREEN + "Ended Path Visualization");
                return true;
            }
        }

        if (Invasion.nexusMap.values().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No Nexus to visualize");
            return false;
        }
        Nexus nexus = Invasion.nexusMap.values().stream().findFirst().get();
        if (visualizer != null){
            visualizer.stopVisualization();
        }
        visualizer = instance();
        Player player = (Player) sender;
        Location start = player.getLocation();
        Location goal = nexus.loc();

        visualizer.run(start, goal);
        sender.sendMessage(ChatColor.GREEN + "Showing path");
        return true;
    }


    public static Visualizer instance(){
        String packageName = "me.stephenminer";
        String ver = Bukkit.getServer().getBukkitVersion();
        ver = ver.substring(0, ver.indexOf("-"));
        Visualizer visualizer = null;
        try{
            switch (ver){
                case "1.21":
                    visualizer = (Visualizer) Class.forName(packageName + ".v1_21_R1.PathVisualizer") .getConstructor().newInstance();
                    break;
                default:

                    visualizer = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return visualizer;
    }
}
