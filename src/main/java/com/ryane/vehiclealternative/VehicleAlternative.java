package com.ryane.vehiclealternative;

import com.ryane.vehiclealternative.commands.MainCommand;
import com.ryane.vehiclealternative.config.ConfigManager;
import com.ryane.vehiclealternative.listeners.BoatClimbListener;
import com.ryane.vehiclealternative.listeners.VehicleSpeedListener;
import org.bukkit.plugin.java.JavaPlugin;

public class VehicleAlternative extends JavaPlugin {

    private static VehicleAlternative instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configuration
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new VehicleSpeedListener(this), this);
        getServer().getPluginManager().registerEvents(new BoatClimbListener(this), this);
        
        // Register commands
        getCommand("vehiclealternative").setExecutor(new MainCommand(this));
        
        getLogger().info("VehicleAlternative has been enabled!");
        getLogger().info("Vehicle speed modifications and boat climbing mechanics are now active.");
    }

    @Override
    public void onDisable() {
        getLogger().info("VehicleAlternative has been disabled!");
    }

    public static VehicleAlternative getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reload() {
        reloadConfig();
        configManager.loadConfig();
        getLogger().info("Configuration reloaded!");
    }
}
