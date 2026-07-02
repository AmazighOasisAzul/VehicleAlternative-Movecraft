package com.ryane.vehiclealternative.commands;

import com.ryane.vehiclealternative.VehicleAlternative;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final VehicleAlternative plugin;
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    public MainCommand(VehicleAlternative plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("vehiclealternative.admin")) {
                    send(sender, plugin.getConfigManager().getPrefix()
                            + plugin.getConfigManager().getNoPermission());
                    return true;
                }
                plugin.reload();
                send(sender, plugin.getConfigManager().getPrefix()
                        + plugin.getConfigManager().getReloadSuccess());
                return true;

            case "info":
            case "version":
                send(sender, plugin.getConfigManager().getPrefix()
                        + plugin.getConfigManager().getPluginInfo());
                return true;

            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        send(sender, "&8&m                                          ");
        send(sender, "&b&lVehicleAlternative &7- Commands");
        send(sender, "&8&m                                          ");
        send(sender, "&b/va help &8- &7Show this help menu");
        send(sender, "&b/va info &8- &7Show plugin information");
        send(sender, "&b/va reload &8- &7Reload configuration");
        send(sender, "&8&m                                          ");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String sub : Arrays.asList("help", "info", "version", "reload")) {
                if (sub.startsWith(input)) {
                    if (sub.equals("reload") && !sender.hasPermission("vehiclealternative.admin")) continue;
                    completions.add(sub);
                }
            }
        }
        return completions;
    }

    private void send(CommandSender sender, String message) {
        Component component = LEGACY.deserialize(message);
        sender.sendMessage(component);
    }
}
