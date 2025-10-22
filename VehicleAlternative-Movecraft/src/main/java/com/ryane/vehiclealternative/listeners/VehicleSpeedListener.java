package com.ryane.vehiclealternative.listeners;

import com.ryane.vehiclealternative.VehicleAlternative;
import com.ryane.vehiclealternative.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class VehicleSpeedListener implements Listener {

    private final VehicleAlternative plugin;
    private final ConfigManager config;

    public VehicleSpeedListener(VehicleAlternative plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!config.isEnabled()) return;
        
        Entity vehicle = event.getVehicle();
        Entity entered = event.getEntered();
        
        if (!(entered instanceof Player)) return;
        Player player = (Player) entered;

        // Handle horses, donkeys, mules
        if (vehicle instanceof AbstractHorse) {
            handleHorseEnter((AbstractHorse) vehicle, player);
        }
        // Handle pigs
        else if (vehicle instanceof Pig) {
            handlePigEnter((Pig) vehicle, player);
        }
        // Handle striders
        else if (vehicle instanceof Strider) {
            handleStriderEnter((Strider) vehicle, player);
        }
        // Handle camels
        else if (vehicle instanceof Camel) {
            handleCamelEnter((Camel) vehicle, player);
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!config.isEnabled()) return;
        
        Vehicle vehicle = event.getVehicle();
        
        // Handle boat speed modifications
        if (vehicle instanceof Boat) {
            handleBoatMove((Boat) vehicle, event);
        }
        // Handle minecart speed modifications
        else if (vehicle instanceof Minecart) {
            handleMinecartMove((Minecart) vehicle, event);
        }
    }

    private void handleHorseEnter(AbstractHorse horse, Player player) {
        if (!config.isHorsesEnabled()) return;
        
        // Check specific types
        if (horse instanceof Donkey && !config.isApplyToDonkeys()) return;
        if (horse instanceof Mule && !config.isApplyToMules()) return;
        
        // Check permission
        if (config.isHorseRequirePermission() && !player.hasPermission("vehiclealternative.use.horse")) {
            return;
        }

        // Modify speed attribute
        AttributeInstance speedAttr = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            double baseSpeed = speedAttr.getBaseValue();
            double multiplier = config.getHorseSpeedMultiplier();
            
            // Clamp between min and max
            multiplier = Math.max(multiplier, config.getHorseMinSpeed());
            multiplier = Math.min(multiplier, config.getHorseMaxSpeed());
            
            speedAttr.setBaseValue(baseSpeed * multiplier);
        }

        // Modify jump strength
        AttributeInstance jumpAttr = horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH);
        if (jumpAttr != null) {
            double baseJump = jumpAttr.getBaseValue();
            jumpAttr.setBaseValue(baseJump * config.getHorseJumpMultiplier());
        }

        if (config.isDebug()) {
            plugin.getLogger().info("Modified speed for horse ridden by " + player.getName());
        }
    }

    private void handlePigEnter(Pig pig, Player player) {
        if (!config.isPigsEnabled()) return;
        
        if (config.isPigRequirePermission() && !player.hasPermission("vehiclealternative.use.pig")) {
            return;
        }

        AttributeInstance speedAttr = pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            double baseSpeed = speedAttr.getBaseValue();
            speedAttr.setBaseValue(baseSpeed * config.getPigSpeedMultiplier());
        }
    }

    private void handleStriderEnter(Strider strider, Player player) {
        if (!config.isStridersEnabled()) return;
        
        if (config.isStriderRequirePermission() && !player.hasPermission("vehiclealternative.use.strider")) {
            return;
        }

        AttributeInstance speedAttr = strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            double baseSpeed = speedAttr.getBaseValue();
            // Check if in lava or on land
            if (strider.getLocation().getBlock().getType() == Material.LAVA) {
                speedAttr.setBaseValue(baseSpeed * config.getStriderLavaSpeed());
            } else {
                speedAttr.setBaseValue(baseSpeed * config.getStriderLandSpeed());
            }
        }
    }

    private void handleCamelEnter(Camel camel, Player player) {
        if (!config.isCamelsEnabled()) return;
        
        if (config.isCamelRequirePermission() && !player.hasPermission("vehiclealternative.use.camel")) {
            return;
        }

        AttributeInstance speedAttr = camel.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            double baseSpeed = speedAttr.getBaseValue();
            speedAttr.setBaseValue(baseSpeed * config.getCamelSpeedMultiplier());
        }
    }

    private void handleBoatMove(Boat boat, VehicleMoveEvent event) {
        if (!config.isBoatsEnabled()) return;
        
        // Check if player is driving
        if (boat.getPassengers().isEmpty()) return;
        Entity passenger = boat.getPassengers().get(0);
        if (!(passenger instanceof Player)) return;
        
        Player player = (Player) passenger;
        if (config.isBoatRequirePermission() && !player.hasPermission("vehiclealternative.use.boat")) {
            return;
        }

        Vector velocity = boat.getVelocity();
        Material blockType = boat.getLocation().getBlock().getType();
        
        double multiplier = 1.0;
        
        // Check block type for appropriate multiplier
        if (blockType == Material.WATER || blockType == Material.BUBBLE_COLUMN) {
            multiplier = config.getBoatWaterSpeed();
        } else if (blockType == Material.ICE || blockType == Material.PACKED_ICE || 
                   blockType == Material.BLUE_ICE || blockType == Material.FROSTED_ICE) {
            multiplier = config.getBoatIceSpeed();
        } else if (blockType.isSolid()) {
            multiplier = config.getBoatLandSpeed();
        }

        // Apply speed multiplier
        if (multiplier != 1.0) {
            velocity.multiply(multiplier);
            boat.setVelocity(velocity);
        }
    }

    private void handleMinecartMove(Minecart minecart, VehicleMoveEvent event) {
        if (!config.isMinecartsEnabled()) return;
        
        // Check if player is riding
        if (minecart.getPassengers().isEmpty()) return;
        Entity passenger = minecart.getPassengers().get(0);
        if (!(passenger instanceof Player)) return;
        
        Player player = (Player) passenger;
        if (config.isMinecartRequirePermission() && !player.hasPermission("vehiclealternative.use.minecart")) {
            return;
        }

        Vector velocity = minecart.getVelocity();
        double speed = velocity.length();
        
        // Apply speed multiplier
        double multiplier = config.getMinecartSpeedMultiplier();
        double maxSpeed = config.getMinecartMaxSpeed();
        
        if (speed > 0) {
            double newSpeed = Math.min(speed * multiplier, maxSpeed);
            velocity.normalize().multiply(newSpeed);
            minecart.setVelocity(velocity);
        }

        // Check if on powered rail for additional boost
        Material railType = minecart.getLocation().getBlock().getType();
        if (railType == Material.POWERED_RAIL) {
            velocity.multiply(config.getPoweredRailBoost());
            minecart.setVelocity(velocity);
        }
    }
}
