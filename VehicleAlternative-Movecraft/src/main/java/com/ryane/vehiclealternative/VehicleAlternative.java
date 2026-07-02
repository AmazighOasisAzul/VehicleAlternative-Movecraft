package com.ryane.vehiclealternative;

import com.ryane.vehiclealternative.commands.MainCommand;
import com.ryane.vehiclealternative.config.ConfigManager;
import com.ryane.vehiclealternative.listeners.BoatClimbListener;
import com.ryane.vehiclealternative.listeners.VehicleSpeedListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Strider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class VehicleAlternative extends JavaPlugin {

    private static VehicleAlternative instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        getServer().getPluginManager().registerEvents(new VehicleSpeedListener(this), this);
        getServer().getPluginManager().registerEvents(new BoatClimbListener(this), this);

        PluginCommand cmd = getCommand("vehiclealternative");
        if (cmd != null) {
            MainCommand mainCommand = new MainCommand(this);
            cmd.setExecutor(mainCommand);
            cmd.setTabCompleter(mainCommand);
        } else {
            getLogger().severe("Command 'vehiclealternative' not found — check plugin.yml!");
        }

        getLogger().info("VehicleAlternative has been enabled!");
        getLogger().info("Vehicle speed modifications and boat climbing mechanics are now active.");
    }

    @Override
    public void onDisable() {
        removeAllModifiers();
        getLogger().info("VehicleAlternative has been disabled!");
    }

    /**
     * Strips all attribute modifiers added by this plugin from every loaded entity.
     * Called on disable so attribute values don't persist after unload.
     */
    private void removeAllModifiers() {
        for (World world : Bukkit.getWorlds()) {
            for (AbstractHorse horse : world.getEntitiesByClass(AbstractHorse.class)) {
                stripModifier(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED));
                stripModifier(horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH));
            }
            for (Pig pig : world.getEntitiesByClass(Pig.class)) {
                stripModifier(pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED));
            }
            for (Strider strider : world.getEntitiesByClass(Strider.class)) {
                stripModifier(strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED));
            }
            for (Camel camel : world.getEntitiesByClass(Camel.class)) {
                stripModifier(camel.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED));
            }
        }
    }

    private void stripModifier(AttributeInstance attr) {
        if (attr == null) return;
        for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
            if (mod.getUniqueId().equals(VehicleConstants.SPEED_MODIFIER_UUID)
                    || mod.getUniqueId().equals(VehicleConstants.JUMP_MODIFIER_UUID)) {
                attr.removeModifier(mod);
            }
        }
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
