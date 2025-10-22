package com.ryane.vehiclealternative.commands;

import com.ryane.vehiclealternative.VehicleAlternative;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final VehicleAlternative plugin;

    public MainCommand(VehicleAlternative plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("vehiclealternative.admin")) {
                    sender.sendMessage(colorize(plugin.getConfigManager().getPrefix() + 
                        plugin.getConfigManager().getNoPermission()));
                    return true;
                }
                plugin.reload();
                sender.sendMessage(colorize(plugin.getConfigManager().getPrefix() + 
                    plugin.getConfigManager().getReloadSuccess()));
                return true;

            case "help":
                sendHelp(sender);
                return true;

            case "info":
            case "version":
                sender.sendMessage(colorize(plugin.getConfigManager().getPrefix() + 
                    plugin.getConfigManager().getPluginInfo()));
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(colorize("&8&m                                          "));
        sender.sendMessage(colorize("&b&lVehicleAlternative &7- Commands"));
        sender.sendMessage(colorize("&8&m                                          "));
        sender.sendMessage(colorize("&b/va help &8- &7Show this help menu"));
        sender.sendMessage(colorize("&b/va info &8- &7Show plugin information"));
        sender.sendMessage(colorize("&b/va reload &8- &7Reload configuration"));
        sender.sendMessage(colorize("&8&m                                          "));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "info", "version", "reload");
            String input = args[0].toLowerCase();
            for (String sub : subCommands) {
                if (sub.startsWith(input)) {
                    if (sub.equals("reload") && !sender.hasPermission("vehiclealternative.admin")) {
                        continue;
                    }
                    completions.add(sub);
                }
            }
        }

        return completions;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
