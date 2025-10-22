package com.ryane.vehiclealternative.config;

import com.ryane.vehiclealternative.VehicleAlternative;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final VehicleAlternative plugin;
    
    // Global settings
    private boolean enabled;
    private boolean debug;
    
    // Horse settings
    private boolean horsesEnabled;
    private double horseSpeedMultiplier;
    private double horseJumpMultiplier;
    private double horseMinSpeed;
    private double horseMaxSpeed;
    private boolean applyToDonkeys;
    private boolean applyToMules;
    private boolean horseRequirePermission;
    
    // Boat settings
    private boolean boatsEnabled;
    private double boatWaterSpeed;
    private double boatIceSpeed;
    private double boatLandSpeed;
    private boolean boatClimbingEnabled;
    private boolean boatClimbOnLand;
    private boolean boatClimbInWater;
    private int boatMaxClimbHeight;
    private double boatClimbSpeed;
    private boolean boatRequirePermission;
    
    // Minecart settings
    private boolean minecartsEnabled;
    private double minecartSpeedMultiplier;
    private double minecartMaxSpeed;
    private double poweredRailBoost;
    private boolean minecartRequirePermission;
    
    // Pig settings
    private boolean pigsEnabled;
    private double pigSpeedMultiplier;
    private boolean pigRequirePermission;
    
    // Strider settings
    private boolean stridersEnabled;
    private double striderLavaSpeed;
    private double striderLandSpeed;
    private boolean striderRequirePermission;
    
    // Camel settings
    private boolean camelsEnabled;
    private double camelSpeedMultiplier;
    private double camelDashCooldown;
    private boolean camelRequirePermission;
    
    // Effects
    private boolean particlesEnabled;
    private String particleType;
    private int particleAmount;
    private boolean soundsEnabled;
    private boolean soundOnSpeedChange;
    private String soundType;
    private float soundVolume;
    private float soundPitch;
    
    // Performance
    private int updateInterval;
    private int maxVehiclesPerTick;
    
    // Messages
    private String prefix;
    private String reloadSuccess;
    private String noPermission;
    private String pluginInfo;

    public ConfigManager(VehicleAlternative plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Global settings
        enabled = config.getBoolean("global.enabled", true);
        debug = config.getBoolean("global.debug", false);
        
        // Horse settings
        horsesEnabled = config.getBoolean("horses.enabled", true);
        horseSpeedMultiplier = config.getDouble("horses.speed-multiplier", 1.5);
        horseJumpMultiplier = config.getDouble("horses.jump-multiplier", 1.2);
        horseMinSpeed = config.getDouble("horses.min-speed-multiplier", 1.2);
        horseMaxSpeed = config.getDouble("horses.max-speed-multiplier", 2.5);
        applyToDonkeys = config.getBoolean("horses.apply-to-donkeys", true);
        applyToMules = config.getBoolean("horses.apply-to-mules", true);
        horseRequirePermission = config.getBoolean("horses.require-permission", false);
        
        // Boat settings
        boatsEnabled = config.getBoolean("boats.enabled", true);
        boatWaterSpeed = config.getDouble("boats.speed.water-multiplier", 1.8);
        boatIceSpeed = config.getDouble("boats.speed.ice-multiplier", 2.5);
        boatLandSpeed = config.getDouble("boats.speed.land-multiplier", 0.8);
        boatClimbingEnabled = config.getBoolean("boats.climbing.enabled", true);
        boatClimbOnLand = config.getBoolean("boats.climbing.allow-on-land", true);
        boatClimbInWater = config.getBoolean("boats.climbing.allow-in-water", true);
        boatMaxClimbHeight = config.getInt("boats.climbing.max-climb-height", 2);
        boatClimbSpeed = config.getDouble("boats.climbing.climb-speed", 0.5);
        boatRequirePermission = config.getBoolean("boats.require-permission", false);
        
        // Minecart settings
        minecartsEnabled = config.getBoolean("minecarts.enabled", true);
        minecartSpeedMultiplier = config.getDouble("minecarts.speed-multiplier", 1.5);
        minecartMaxSpeed = config.getDouble("minecarts.max-speed-multiplier", 3.0);
        poweredRailBoost = config.getDouble("minecarts.powered-rail-boost", 2.0);
        minecartRequirePermission = config.getBoolean("minecarts.require-permission", false);
        
        // Pig settings
        pigsEnabled = config.getBoolean("pigs.enabled", true);
        pigSpeedMultiplier = config.getDouble("pigs.speed-multiplier", 1.3);
        pigRequirePermission = config.getBoolean("pigs.require-permission", false);
        
        // Strider settings
        stridersEnabled = config.getBoolean("striders.enabled", true);
        striderLavaSpeed = config.getDouble("striders.lava-speed-multiplier", 1.4);
        striderLandSpeed = config.getDouble("striders.land-speed-multiplier", 1.0);
        striderRequirePermission = config.getBoolean("striders.require-permission", false);
        
        // Camel settings
        camelsEnabled = config.getBoolean("camels.enabled", true);
        camelSpeedMultiplier = config.getDouble("camels.speed-multiplier", 1.3);
        camelDashCooldown = config.getDouble("camels.dash-cooldown", 4.0);
        camelRequirePermission = config.getBoolean("camels.require-permission", false);
        
        // Effects
        particlesEnabled = config.getBoolean("effects.particles.enabled", true);
        particleType = config.getString("effects.particles.type", "CLOUD");
        particleAmount = config.getInt("effects.particles.amount", 3);
        soundsEnabled = config.getBoolean("effects.sounds.enabled", true);
        soundOnSpeedChange = config.getBoolean("effects.sounds.on-speed-change", true);
        soundType = config.getString("effects.sounds.sound-type", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundVolume = (float) config.getDouble("effects.sounds.volume", 0.5);
        soundPitch = (float) config.getDouble("effects.sounds.pitch", 1.0);
        
        // Performance
        updateInterval = config.getInt("performance.update-interval", 5);
        maxVehiclesPerTick = config.getInt("performance.max-vehicles-per-tick", 50);
        
        // Messages
        prefix = config.getString("messages.prefix", "&8[&bVehicleAlternative&8]&r ");
        reloadSuccess = config.getString("messages.reload-success", "&aConfiguration reloaded successfully!");
        noPermission = config.getString("messages.no-permission", "&cYou don't have permission to use this command.");
        pluginInfo = config.getString("messages.plugin-info", "&bVehicleAlternative &7v${project.version}");
        
        if (debug) {
            plugin.getLogger().info("Configuration loaded with debug mode enabled");
        }
    }

    // Getters
    public boolean isEnabled() { return enabled; }
    public boolean isDebug() { return debug; }
    
    public boolean isHorsesEnabled() { return horsesEnabled; }
    public double getHorseSpeedMultiplier() { return horseSpeedMultiplier; }
    public double getHorseJumpMultiplier() { return horseJumpMultiplier; }
    public double getHorseMinSpeed() { return horseMinSpeed; }
    public double getHorseMaxSpeed() { return horseMaxSpeed; }
    public boolean isApplyToDonkeys() { return applyToDonkeys; }
    public boolean isApplyToMules() { return applyToMules; }
    public boolean isHorseRequirePermission() { return horseRequirePermission; }
    
    public boolean isBoatsEnabled() { return boatsEnabled; }
    public double getBoatWaterSpeed() { return boatWaterSpeed; }
    public double getBoatIceSpeed() { return boatIceSpeed; }
    public double getBoatLandSpeed() { return boatLandSpeed; }
    public boolean isBoatClimbingEnabled() { return boatClimbingEnabled; }
    public boolean isBoatClimbOnLand() { return boatClimbOnLand; }
    public boolean isBoatClimbInWater() { return boatClimbInWater; }
    public int getBoatMaxClimbHeight() { return boatMaxClimbHeight; }
    public double getBoatClimbSpeed() { return boatClimbSpeed; }
    public boolean isBoatRequirePermission() { return boatRequirePermission; }
    
    public boolean isMinecartsEnabled() { return minecartsEnabled; }
    public double getMinecartSpeedMultiplier() { return minecartSpeedMultiplier; }
    public double getMinecartMaxSpeed() { return minecartMaxSpeed; }
    public double getPoweredRailBoost() { return poweredRailBoost; }
    public boolean isMinecartRequirePermission() { return minecartRequirePermission; }
    
    public boolean isPigsEnabled() { return pigsEnabled; }
    public double getPigSpeedMultiplier() { return pigSpeedMultiplier; }
    public boolean isPigRequirePermission() { return pigRequirePermission; }
    
    public boolean isStridersEnabled() { return stridersEnabled; }
    public double getStriderLavaSpeed() { return striderLavaSpeed; }
    public double getStriderLandSpeed() { return striderLandSpeed; }
    public boolean isStriderRequirePermission() { return striderRequirePermission; }
    
    public boolean isCamelsEnabled() { return camelsEnabled; }
    public double getCamelSpeedMultiplier() { return camelSpeedMultiplier; }
    public double getCamelDashCooldown() { return camelDashCooldown; }
    public boolean isCamelRequirePermission() { return camelRequirePermission; }
    
    public boolean isParticlesEnabled() { return particlesEnabled; }
    public String getParticleType() { return particleType; }
    public int getParticleAmount() { return particleAmount; }
    public boolean isSoundsEnabled() { return soundsEnabled; }
    public boolean isSoundOnSpeedChange() { return soundOnSpeedChange; }
    public String getSoundType() { return soundType; }
    public float getSoundVolume() { return soundVolume; }
    public float getSoundPitch() { return soundPitch; }
    
    public int getUpdateInterval() { return updateInterval; }
    public int getMaxVehiclesPerTick() { return maxVehiclesPerTick; }
    
    public String getPrefix() { return prefix; }
    public String getReloadSuccess() { return reloadSuccess; }
    public String getNoPermission() { return noPermission; }
    public String getPluginInfo() { return pluginInfo; }
}
