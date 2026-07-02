package com.ryane.vehiclealternative.listeners;

import com.ryane.vehiclealternative.VehicleAlternative;
import com.ryane.vehiclealternative.VehicleConstants;
import com.ryane.vehiclealternative.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

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

        if (vehicle instanceof AbstractHorse) {
            handleHorseEnter((AbstractHorse) vehicle, player);
        } else if (vehicle instanceof Pig) {
            handlePigEnter((Pig) vehicle, player);
        } else if (vehicle instanceof Strider) {
            handleStriderEnter((Strider) vehicle, player);
        } else if (vehicle instanceof Camel) {
            handleCamelEnter((Camel) vehicle, player);
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!config.isEnabled()) return;

        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof Boat) {
            handleBoatMove((Boat) vehicle);
        } else if (vehicle instanceof Minecart) {
            handleMinecartMove((Minecart) vehicle);
        }
    }

    // -------------------------------------------------------------------------
    // Horse / donkey / mule
    // -------------------------------------------------------------------------

    private void handleHorseEnter(AbstractHorse horse, Player player) {
        if (!config.isHorsesEnabled()) return;
        if (horse instanceof Donkey && !config.isApplyToDonkeys()) return;
        if (horse instanceof Mule   && !config.isApplyToMules())   return;
        if (config.isHorseRequirePermission() && !player.hasPermission("vehiclealternative.use.horse")) return;

        double multiplier = config.getHorseSpeedMultiplier();
        multiplier = Math.max(multiplier, config.getHorseMinSpeed());
        multiplier = Math.min(multiplier, config.getHorseMaxSpeed());

        applyModifier(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                VehicleConstants.SPEED_MODIFIER_UUID,
                VehicleConstants.SPEED_MODIFIER_NAME,
                multiplier);

        applyModifier(horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH),
                VehicleConstants.JUMP_MODIFIER_UUID,
                VehicleConstants.JUMP_MODIFIER_NAME,
                config.getHorseJumpMultiplier());

        if (config.isDebug()) {
            plugin.getLogger().info("Applied speed modifier (" + multiplier + "x) to horse ridden by " + player.getName());
        }
    }

    // -------------------------------------------------------------------------
    // Pig
    // -------------------------------------------------------------------------

    private void handlePigEnter(Pig pig, Player player) {
        if (!config.isPigsEnabled()) return;
        if (config.isPigRequirePermission() && !player.hasPermission("vehiclealternative.use.pig")) return;

        applyModifier(pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                VehicleConstants.SPEED_MODIFIER_UUID,
                VehicleConstants.SPEED_MODIFIER_NAME,
                config.getPigSpeedMultiplier());
    }

    // -------------------------------------------------------------------------
    // Strider
    // -------------------------------------------------------------------------

    private void handleStriderEnter(Strider strider, Player player) {
        if (!config.isStridersEnabled()) return;
        if (config.isStriderRequirePermission() && !player.hasPermission("vehiclealternative.use.strider")) return;

        // Striders ride ON TOP of lava — check block below, not block at feet
        Block below = strider.getLocation().getBlock().getRelative(BlockFace.DOWN);
        double multiplier = (below.getType() == Material.LAVA)
                ? config.getStriderLavaSpeed()
                : config.getStriderLandSpeed();

        applyModifier(strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                VehicleConstants.SPEED_MODIFIER_UUID,
                VehicleConstants.SPEED_MODIFIER_NAME,
                multiplier);
    }

    // -------------------------------------------------------------------------
    // Camel
    // -------------------------------------------------------------------------

    private void handleCamelEnter(Camel camel, Player player) {
        if (!config.isCamelsEnabled()) return;
        if (config.isCamelRequirePermission() && !player.hasPermission("vehiclealternative.use.camel")) return;

        applyModifier(camel.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                VehicleConstants.SPEED_MODIFIER_UUID,
                VehicleConstants.SPEED_MODIFIER_NAME,
                config.getCamelSpeedMultiplier());
    }

    // -------------------------------------------------------------------------
    // Boat — velocity capped, not multiplied every tick
    // -------------------------------------------------------------------------

    private void handleBoatMove(Boat boat) {
        if (!config.isBoatsEnabled()) return;
        if (boat.getPassengers().isEmpty()) return;
        Entity passenger = boat.getPassengers().get(0);
        if (!(passenger instanceof Player)) return;
        Player player = (Player) passenger;
        if (config.isBoatRequirePermission() && !player.hasPermission("vehiclealternative.use.boat")) return;

        // Respect boats.types config
        if (!config.getAllowedBoatTypes().contains(boat.getBoatType())) return;

        Vector velocity = boat.getVelocity();
        double horizSpeed = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
        if (horizSpeed < 0.01) return; // Stationary — nothing to boost

        Material blockType = boat.getLocation().getBlock().getType();
        double multiplier;

        if (blockType == Material.WATER || blockType == Material.BUBBLE_COLUMN) {
            multiplier = config.getBoatWaterSpeed();
        } else if (blockType == Material.ICE || blockType == Material.PACKED_ICE
                || blockType == Material.BLUE_ICE || blockType == Material.FROSTED_ICE) {
            multiplier = config.getBoatIceSpeed();
        } else if (blockType.isSolid()) {
            multiplier = config.getBoatLandSpeed();
        } else {
            return;
        }

        // Target-based approach: only scale up when below the desired cap.
        // This avoids exponential runaway from repeated multiplication every tick.
        double targetSpeed = VehicleConstants.VANILLA_BOAT_WATER_SPEED * multiplier;
        if (horizSpeed < targetSpeed) {
            double scale = targetSpeed / horizSpeed;
            velocity.setX(velocity.getX() * scale);
            velocity.setZ(velocity.getZ() * scale);
            boat.setVelocity(velocity);
        }
    }

    // -------------------------------------------------------------------------
    // Minecart
    // -------------------------------------------------------------------------

    private void handleMinecartMove(Minecart minecart) {
        if (!config.isMinecartsEnabled()) return;

        // Respect minecarts.types config
        if (!isMinecartTypeAllowed(minecart)) return;

        if (minecart.getPassengers().isEmpty()) return;
        Entity passenger = minecart.getPassengers().get(0);
        if (!(passenger instanceof Player)) return;
        Player player = (Player) passenger;
        if (config.isMinecartRequirePermission() && !player.hasPermission("vehiclealternative.use.minecart")) return;

        Vector velocity = minecart.getVelocity();
        double speed = velocity.length();
        if (speed < 0.01) return; // normalize() on a zero vector yields NaN

        double maxSpeed = config.getMinecartMaxSpeed();

        // Apply powered-rail boost inside the cap calculation to avoid bypassing maxSpeed
        Material railType = minecart.getLocation().getBlock().getType();
        double boost = (railType == Material.POWERED_RAIL) ? config.getPoweredRailBoost() : 1.0;

        double newSpeed = Math.min(speed * config.getMinecartSpeedMultiplier() * boost, maxSpeed);
        if (newSpeed > speed) {
            minecart.setVelocity(velocity.normalize().multiply(newSpeed));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Applies a MULTIPLY_SCALAR_1 attribute modifier, replacing any existing
     * modifier from this plugin to prevent speed stacking on repeated mounts.
     */
    private void applyModifier(AttributeInstance attr, UUID uuid, String name, double multiplier) {
        if (attr == null) return;

        // Remove any existing modifier added by this plugin
        for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
            if (mod.getUniqueId().equals(uuid)) {
                attr.removeModifier(mod);
            }
        }

        // MULTIPLY_SCALAR_1: finalValue = base + base * amount
        // So amount = multiplier - 1 gives the desired total multiplier
        attr.addModifier(new AttributeModifier(
                uuid, name,
                multiplier - 1.0,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        ));
    }

    private boolean isMinecartTypeAllowed(Minecart minecart) {
        if (minecart instanceof StorageMinecart) return config.isMinecartChest();
        if (minecart instanceof PoweredMinecart)  return config.isMinecartFurnace();
        if (minecart instanceof HopperMinecart)   return config.isMinecartHopper();
        if (minecart instanceof ExplosiveMinecart) return config.isMinecartTNT();
        return config.isMinecartRegular();
    }
}
