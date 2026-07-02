package com.ryane.vehiclealternative.config;

import com.ryane.vehiclealternative.VehicleAlternative;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Boat;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private final VehicleAlternative plugin;

    // Global
    private boolean enabled;
    private boolean debug;

    // Horse
    private boolean horsesEnabled;
    private double  horseSpeedMultiplier;
    private double  horseJumpMultiplier;
    private double  horseMinSpeed;
    private double  horseMaxSpeed;
    private boolean applyToDonkeys;
    private boolean applyToMules;
    private boolean horseRequirePermission;

    // Boat
    private boolean boatsEnabled;
    private double  boatWaterSpeed;
    private double  boatIceSpeed;
    private double  boatLandSpeed;
    private boolean boatClimbingEnabled;
    private boolean boatClimbOnLand;
    private boolean boatClimbInWater;
    private int     boatMaxClimbHeight;
    private double  boatClimbSpeed;
    private boolean boatRequirePermission;
    private Set<Boat.Type> allowedBoatTypes;

    // Minecart
    private boolean minecartsEnabled;
    private double  minecartSpeedMultiplier;
    private double  minecartMaxSpeed;
    private double  poweredRailBoost;
    private boolean minecartRequirePermission;
    private boolean minecartRegular;
    private boolean minecartChest;
    private boolean minecartFurnace;
    private boolean minecartHopper;
    private boolean minecartTNT;

    // Pig
    private boolean pigsEnabled;
    private double  pigSpeedMultiplier;
    private boolean pigRequirePermission;

    // Strider
    private boolean stridersEnabled;
    private double  striderLavaSpeed;
    private double  striderLandSpeed;
    private boolean striderRequirePermission;

    // Camel
    private boolean camelsEnabled;
    private double  camelSpeedMultiplier;
    private boolean camelRequirePermission;

    // Block speed boost
    private boolean        blockSpeedBoostEnabled;
    private boolean        blockSpeedApplyToPlayers;
    private boolean        blockSpeedApplyToVehicles;
    private double         blockSpeedMultiplier;
    private boolean        blockSpeedRequirePermission;
    private Set<Material>  blockSpeedBlocks;

    // Effects
    private boolean particlesEnabled;
    private String  particleType;
    private int     particleAmount;
    private boolean soundsEnabled;
    private boolean soundOnSpeedChange;
    private String  soundType;
    private float   soundVolume;
    private float   soundPitch;

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
        FileConfiguration cfg = plugin.getConfig();

        enabled = cfg.getBoolean("global.enabled", true);
        debug   = cfg.getBoolean("global.debug",   false);

        horsesEnabled          = cfg.getBoolean("horses.enabled", true);
        horseSpeedMultiplier   = cfg.getDouble("horses.speed-multiplier", 1.5);
        horseJumpMultiplier    = cfg.getDouble("horses.jump-multiplier", 1.2);
        horseMinSpeed          = cfg.getDouble("horses.min-speed-multiplier", 1.2);
        horseMaxSpeed          = cfg.getDouble("horses.max-speed-multiplier", 2.5);
        applyToDonkeys         = cfg.getBoolean("horses.apply-to-donkeys", true);
        applyToMules           = cfg.getBoolean("horses.apply-to-mules", true);
        horseRequirePermission = cfg.getBoolean("horses.require-permission", false);

        boatsEnabled          = cfg.getBoolean("boats.enabled", true);
        boatWaterSpeed        = cfg.getDouble("boats.speed.water-multiplier", 1.8);
        boatIceSpeed          = cfg.getDouble("boats.speed.ice-multiplier", 2.5);
        boatLandSpeed         = cfg.getDouble("boats.speed.land-multiplier", 0.8);
        boatClimbingEnabled   = cfg.getBoolean("boats.climbing.enabled", true);
        boatClimbOnLand       = cfg.getBoolean("boats.climbing.allow-on-land", true);
        boatClimbInWater      = cfg.getBoolean("boats.climbing.allow-in-water", true);
        boatMaxClimbHeight    = cfg.getInt("boats.climbing.max-climb-height", 2);
        boatClimbSpeed        = cfg.getDouble("boats.climbing.climb-speed", 0.5);
        boatRequirePermission = cfg.getBoolean("boats.require-permission", false);

        allowedBoatTypes = EnumSet.noneOf(Boat.Type.class);
        for (Boat.Type type : Boat.Type.values()) {
            if (cfg.getBoolean("boats.types." + type.name().toLowerCase(), true)) {
                allowedBoatTypes.add(type);
            }
        }

        minecartsEnabled          = cfg.getBoolean("minecarts.enabled", true);
        minecartSpeedMultiplier   = cfg.getDouble("minecarts.speed-multiplier", 1.5);
        minecartMaxSpeed          = cfg.getDouble("minecarts.max-speed-multiplier", 3.0);
        poweredRailBoost          = cfg.getDouble("minecarts.powered-rail-boost", 2.0);
        minecartRequirePermission = cfg.getBoolean("minecarts.require-permission", false);
        minecartRegular  = cfg.getBoolean("minecarts.types.regular",  true);
        minecartChest    = cfg.getBoolean("minecarts.types.chest",    true);
        minecartFurnace  = cfg.getBoolean("minecarts.types.furnace",  true);
        minecartHopper   = cfg.getBoolean("minecarts.types.hopper",   true);
        minecartTNT      = cfg.getBoolean("minecarts.types.tnt",      true);

        pigsEnabled          = cfg.getBoolean("pigs.enabled", true);
        pigSpeedMultiplier   = cfg.getDouble("pigs.speed-multiplier", 1.3);
        pigRequirePermission = cfg.getBoolean("pigs.require-permission", false);

        stridersEnabled          = cfg.getBoolean("striders.enabled", true);
        striderLavaSpeed         = cfg.getDouble("striders.lava-speed-multiplier", 1.4);
        striderLandSpeed         = cfg.getDouble("striders.land-speed-multiplier", 1.0);
        striderRequirePermission = cfg.getBoolean("striders.require-permission", false);

        camelsEnabled          = cfg.getBoolean("camels.enabled", true);
        camelSpeedMultiplier   = cfg.getDouble("camels.speed-multiplier", 1.3);
        camelRequirePermission = cfg.getBoolean("camels.require-permission", false);

        // Block speed boost
        blockSpeedBoostEnabled     = cfg.getBoolean("block-speed-boost.enabled", true);
        blockSpeedApplyToPlayers   = cfg.getBoolean("block-speed-boost.apply-to-players", true);
        blockSpeedApplyToVehicles  = cfg.getBoolean("block-speed-boost.apply-to-vehicles", true);
        blockSpeedMultiplier       = cfg.getDouble("block-speed-boost.multiplier", 1.5);
        blockSpeedRequirePermission= cfg.getBoolean("block-speed-boost.require-permission", false);

        blockSpeedBlocks = EnumSet.noneOf(Material.class);
        List<String> blockNames = cfg.getStringList("block-speed-boost.blocks");
        for (String name : blockNames) {
            try {
                blockSpeedBlocks.add(Material.valueOf(name.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[BlockSpeedBoost] Unknown material: '" + name + "' — skipping.");
            }
        }

        particlesEnabled   = cfg.getBoolean("effects.particles.enabled", true);
        particleType       = cfg.getString("effects.particles.type", "CLOUD");
        particleAmount     = cfg.getInt("effects.particles.amount", 3);
        soundsEnabled      = cfg.getBoolean("effects.sounds.enabled", true);
        soundOnSpeedChange = cfg.getBoolean("effects.sounds.on-speed-change", true);
        soundType          = cfg.getString("effects.sounds.sound-type", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundVolume        = (float) cfg.getDouble("effects.sounds.volume", 0.5);
        soundPitch         = (float) cfg.getDouble("effects.sounds.pitch", 1.0);

        updateInterval     = cfg.getInt("performance.update-interval", 5);
        maxVehiclesPerTick = cfg.getInt("performance.max-vehicles-per-tick", 50);

        prefix        = cfg.getString("messages.prefix", "&8[&bVehicleAlternative&8]&r ");
        reloadSuccess = cfg.getString("messages.reload-success", "&aConfiguration reloaded successfully!");
        noPermission  = cfg.getString("messages.no-permission", "&cYou don't have permission to use this command.");
        pluginInfo    = cfg.getString("messages.plugin-info",
                "&bVehicleAlternative &7v" + plugin.getDescription().getVersion());

        if (debug) {
            plugin.getLogger().info("Config loaded — debug ON");
            plugin.getLogger().info("Block-speed blocks: " + blockSpeedBlocks);
            plugin.getLogger().info("Allowed boat types: " + allowedBoatTypes);
        }
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public boolean isEnabled()  { return enabled; }
    public boolean isDebug()    { return debug; }

    public boolean isHorsesEnabled()           { return horsesEnabled; }
    public double  getHorseSpeedMultiplier()   { return horseSpeedMultiplier; }
    public double  getHorseJumpMultiplier()    { return horseJumpMultiplier; }
    public double  getHorseMinSpeed()          { return horseMinSpeed; }
    public double  getHorseMaxSpeed()          { return horseMaxSpeed; }
    public boolean isApplyToDonkeys()          { return applyToDonkeys; }
    public boolean isApplyToMules()            { return applyToMules; }
    public boolean isHorseRequirePermission()  { return horseRequirePermission; }

    public boolean isBoatsEnabled()            { return boatsEnabled; }
    public double  getBoatWaterSpeed()         { return boatWaterSpeed; }
    public double  getBoatIceSpeed()           { return boatIceSpeed; }
    public double  getBoatLandSpeed()          { return boatLandSpeed; }
    public boolean isBoatClimbingEnabled()     { return boatClimbingEnabled; }
    public boolean isBoatClimbOnLand()         { return boatClimbOnLand; }
    public boolean isBoatClimbInWater()        { return boatClimbInWater; }
    public int     getBoatMaxClimbHeight()     { return boatMaxClimbHeight; }
    public double  getBoatClimbSpeed()         { return boatClimbSpeed; }
    public boolean isBoatRequirePermission()   { return boatRequirePermission; }
    public Set<Boat.Type> getAllowedBoatTypes() { return allowedBoatTypes; }

    public boolean isMinecartsEnabled()           { return minecartsEnabled; }
    public double  getMinecartSpeedMultiplier()   { return minecartSpeedMultiplier; }
    public double  getMinecartMaxSpeed()          { return minecartMaxSpeed; }
    public double  getPoweredRailBoost()          { return poweredRailBoost; }
    public boolean isMinecartRequirePermission()  { return minecartRequirePermission; }
    public boolean isMinecartRegular()            { return minecartRegular; }
    public boolean isMinecartChest()              { return minecartChest; }
    public boolean isMinecartFurnace()            { return minecartFurnace; }
    public boolean isMinecartHopper()             { return minecartHopper; }
    public boolean isMinecartTNT()                { return minecartTNT; }

    public boolean isPigsEnabled()            { return pigsEnabled; }
    public double  getPigSpeedMultiplier()    { return pigSpeedMultiplier; }
    public boolean isPigRequirePermission()   { return pigRequirePermission; }

    public boolean isStridersEnabled()           { return stridersEnabled; }
    public double  getStriderLavaSpeed()         { return striderLavaSpeed; }
    public double  getStriderLandSpeed()         { return striderLandSpeed; }
    public boolean isStriderRequirePermission()  { return striderRequirePermission; }

    public boolean isCamelsEnabled()           { return camelsEnabled; }
    public double  getCamelSpeedMultiplier()   { return camelSpeedMultiplier; }
    public boolean isCamelRequirePermission()  { return camelRequirePermission; }

    public boolean       isBlockSpeedBoostEnabled()      { return blockSpeedBoostEnabled; }
    public boolean       isBlockSpeedApplyToPlayers()    { return blockSpeedApplyToPlayers; }
    public boolean       isBlockSpeedApplyToVehicles()   { return blockSpeedApplyToVehicles; }
    public double        getBlockSpeedMultiplier()       { return blockSpeedMultiplier; }
    public boolean       isBlockSpeedRequirePermission() { return blockSpeedRequirePermission; }
    public Set<Material> getBlockSpeedBlocks()           { return blockSpeedBlocks; }

    public boolean isParticlesEnabled()    { return particlesEnabled; }
    public String  getParticleType()       { return particleType; }
    public int     getParticleAmount()     { return particleAmount; }
    public boolean isSoundsEnabled()       { return soundsEnabled; }
    public boolean isSoundOnSpeedChange()  { return soundOnSpeedChange; }
    public String  getSoundType()          { return soundType; }
    public float   getSoundVolume()        { return soundVolume; }
    public float   getSoundPitch()         { return soundPitch; }

    public int getUpdateInterval()      { return updateInterval; }
    public int getMaxVehiclesPerTick()  { return maxVehiclesPerTick; }

    public String getPrefix()        { return prefix; }
    public String getReloadSuccess() { return reloadSuccess; }
    public String getNoPermission()  { return noPermission; }
    public String getPluginInfo()    { return pluginInfo; }
}
