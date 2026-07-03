package com.ryane.vehiclealternative;

import com.ryane.vehiclealternative.commands.MainCommand;
import com.ryane.vehiclealternative.config.ConfigManager;
import com.ryane.vehiclealternative.listeners.BlockSpeedListener;
import com.ryane.vehiclealternative.listeners.BoatClimbListener;
import com.ryane.vehiclealternative.listeners.ElytraListener;
import com.ryane.vehiclealternative.listeners.EnderPearlListener;
import com.ryane.vehiclealternative.listeners.VehicleSpeedListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class VehicleAlternative extends JavaPlugin {

    private static VehicleAlternative instance;
    private ConfigManager      configManager;
    private BlockSpeedListener blockSpeedListener;
    private BukkitTask         blockSpeedTask;
    private ElytraListener     elytraListener;
    private BukkitTask         elytraTask;
    private int                elytraTick = 0;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Listeners
        VehicleSpeedListener vehicleSpeedListener = new VehicleSpeedListener(this);
        blockSpeedListener = new BlockSpeedListener(this);
        elytraListener     = new ElytraListener(this);
        getServer().getPluginManager().registerEvents(vehicleSpeedListener, this);
        getServer().getPluginManager().registerEvents(new BoatClimbListener(this), this);
        getServer().getPluginManager().registerEvents(blockSpeedListener, this);
        getServer().getPluginManager().registerEvents(new EnderPearlListener(this), this);
        getServer().getPluginManager().registerEvents(elytraListener, this);

        // Scheduled tasks
        startBlockSpeedTask();
        startElytraTask();

        // Commands
        PluginCommand cmd = getCommand("vehiclealternative");
        if (cmd != null) {
            MainCommand mainCommand = new MainCommand(this);
            cmd.setExecutor(mainCommand);
            cmd.setTabCompleter(mainCommand);
        } else {
            getLogger().severe("Command 'vehiclealternative' not found — check plugin.yml!");
        }

        getLogger().info("VehicleAlternative v" + getDescription().getVersion() + " enabled.");
        getLogger().info("Block-speed-boost tracks "
                + configManager.getBlockSpeedBlocks().size() + " block type(s).");
    }

    @Override
    public void onDisable() {
        if (blockSpeedTask != null) blockSpeedTask.cancel();
        if (elytraTask     != null) elytraTask.cancel();
        removeAllModifiers();
        if (blockSpeedListener != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                blockSpeedListener.removePlayerBoost(p);
            }
        }
        getLogger().info("VehicleAlternative disabled.");
    }

    // -------------------------------------------------------------------------
    // Block-speed scheduled task for rideable mobs
    // -------------------------------------------------------------------------

    private void startBlockSpeedTask() {
        if (blockSpeedTask != null) blockSpeedTask.cancel();
        long interval = Math.max(1L, configManager.getUpdateInterval());
        blockSpeedTask = Bukkit.getScheduler().runTaskTimer(this, this::tickRideableMobBlockSpeed, 0L, interval);
    }

    // -------------------------------------------------------------------------
    // Elytra scheduled task (speed + durability)
    // -------------------------------------------------------------------------

    private void startElytraTask() {
        if (elytraTask != null) elytraTask.cancel();
        elytraTick = 0;
        // Runs every 5 ticks; durability subtick fires every 4th call (= 20 ticks / 1 s).
        elytraTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (elytraListener == null) return;
            elytraTick++;
            boolean durabilityTick = (elytraTick % 4 == 0);
            for (Player p : Bukkit.getOnlinePlayers()) {
                elytraListener.tickSpeed(p);
                if (durabilityTick) elytraListener.tickDurability(p);
            }
        }, 0L, 5L);
    }

    /**
     * Called every {@code update-interval} ticks. Checks each ridden mob's
     * current surface block and applies or removes the block-speed modifier.
     */
    private void tickRideableMobBlockSpeed() {
        if (!configManager.isEnabled()
                || !configManager.isBlockSpeedBoostEnabled()
                || !configManager.isBlockSpeedApplyToVehicles()) return;

        for (World world : Bukkit.getWorlds()) {
            // AbstractHorse already includes Camel (Camel extends AbstractHorse in 1.20.1),
            // Donkey, Mule, Horse, SkeletonHorse, ZombieHorse — one call covers all.
            tickMobClass(world.getEntitiesByClass(AbstractHorse.class), Attribute.GENERIC_MOVEMENT_SPEED);
            tickMobClass(world.getEntitiesByClass(Pig.class),           Attribute.GENERIC_MOVEMENT_SPEED);
            tickMobClass(world.getEntitiesByClass(Strider.class),       Attribute.GENERIC_MOVEMENT_SPEED);
        }
    }

    private <T extends LivingEntity> void tickMobClass(
            java.util.Collection<T> entities, Attribute attr) {
        for (T entity : entities) {
            if (entity.getPassengers().isEmpty()
                    || !(entity.getPassengers().get(0) instanceof Player)) {
                // No player rider — remove the block-speed modifier if present
                stripModifier(entity.getAttribute(attr), VehicleConstants.BLOCK_SPEED_MODIFIER_UUID);
                continue;
            }
            Player rider = (Player) entity.getPassengers().get(0);

            if (configManager.isBlockSpeedRequirePermission()
                    && !rider.hasPermission("vehiclealternative.blockspeed")) {
                stripModifier(entity.getAttribute(attr), VehicleConstants.BLOCK_SPEED_MODIFIER_UUID);
                continue;
            }

            // Check block below entity (ground surface)
            Block below = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (configManager.getBlockSpeedBlocks().contains(below.getType())) {
                VehicleSpeedListener.applyModifier(
                        entity.getAttribute(attr),
                        VehicleConstants.BLOCK_SPEED_MODIFIER_UUID,
                        VehicleConstants.BLOCK_SPEED_MODIFIER_NAME,
                        configManager.getBlockSpeedMultiplier());
            } else {
                stripModifier(entity.getAttribute(attr), VehicleConstants.BLOCK_SPEED_MODIFIER_UUID);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Modifier cleanup
    // -------------------------------------------------------------------------

    /** Strips all VehicleAlternative modifiers from every loaded entity on disable. */
    private void removeAllModifiers() {
        for (World world : Bukkit.getWorlds()) {
            // AbstractHorse covers Horse, Donkey, Mule, Camel, SkeletonHorse, ZombieHorse
            // in 1.20.1 — no separate Camel loop needed.
            for (AbstractHorse horse : world.getEntitiesByClass(AbstractHorse.class)) {
                stripModifier(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), VehicleConstants.SPEED_MODIFIER_UUID);
                stripModifier(horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH),    VehicleConstants.JUMP_MODIFIER_UUID);
                stripModifier(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), VehicleConstants.BLOCK_SPEED_MODIFIER_UUID);
            }
            for (Pig pig : world.getEntitiesByClass(Pig.class)) {
                stripModifier(pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), VehicleConstants.SPEED_MODIFIER_UUID);
                stripModifier(pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), VehicleConstants.BLOCK_SPEED_MODIFIER_UUID);
            }
            for (Strider strider : world.getEntitiesByClass(Strider.class)) {
                stripModifier(strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), VehicleConstants.SPEED_MODIFIER_UUID);
                stripModifier(strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), VehicleConstants.BLOCK_SPEED_MODIFIER_UUID);
            }
        }
    }

    private void stripModifier(AttributeInstance attr, java.util.UUID uuid) {
        if (attr == null) return;
        for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
            if (mod.getUniqueId().equals(uuid)) attr.removeModifier(mod);
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public static VehicleAlternative getInstance() { return instance; }
    public ConfigManager getConfigManager()        { return configManager; }

    public void reload() {
        reloadConfig();
        configManager.loadConfig();
        if (blockSpeedTask != null) blockSpeedTask.cancel();
        startBlockSpeedTask();
        startElytraTask();
        getLogger().info("Configuration reloaded. Block-speed tracks "
                + configManager.getBlockSpeedBlocks().size() + " block type(s).");
    }
}
