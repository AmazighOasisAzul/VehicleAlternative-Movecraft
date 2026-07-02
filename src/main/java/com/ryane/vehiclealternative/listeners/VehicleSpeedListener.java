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

    // -------------------------------------------------------------------------
    // Mount events — apply ride-speed modifier
    // -------------------------------------------------------------------------

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!config.isEnabled()) return;
        if (!(event.getEntered() instanceof Player)) return;
        Player player = (Player) event.getEntered();
        Entity vehicle = event.getVehicle();

        // Camel extends AbstractHorse in 1.20.1 — check it FIRST so it gets its
        // own handler rather than falling into the generic horse handler.
        if (vehicle instanceof Camel) {
            handleCamelEnter((Camel) vehicle, player);
        } else if (vehicle instanceof AbstractHorse) {
            handleHorseEnter((AbstractHorse) vehicle, player);
        } else if (vehicle instanceof Pig) {
            handlePigEnter((Pig) vehicle, player);
        } else if (vehicle instanceof Strider) {
            handleStriderEnter((Strider) vehicle, player);
        }
    }

    // -------------------------------------------------------------------------
    // Move events — velocity-based speed for boats & minecarts
    // -------------------------------------------------------------------------

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
        if (config.isHorseRequirePermission()
                && !player.hasPermission("vehiclealternative.use.horse")) return;

        double multiplier = config.getHorseSpeedMultiplier();
        multiplier = Math.max(multiplier, config.getHorseMinSpeed());
        multiplier = Math.min(multiplier, config.getHorseMaxSpeed());

        applyModifier(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                VehicleConstants.SPEED_MODIFIER_UUID,
                VehicleConstants.SPEED_MODIFIER_NAME, multiplier);

        applyModifier(horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH),
                VehicleConstants.JUMP_MODIFIER_UUID,
                VehicleConstants.JUMP_MODIFIER_NAME, config.getHorseJumpMultiplier());

        if (config.isDebug())
            plugin.getLogger().info("Horse speed x" + multiplier + " → " + player.getName());
    }

    // -------------------------------------------------------------------------
    // Pig
    // -------------------------------------------------------------------------

    private void handlePigEnter(Pig pig, Player player) {
        if (!config.isPigsEnabled()) return;
        if (config.isPigRequirePermission()
                && !player.hasPermission("vehiclealternative.use.pig")) return;
        applyModifier(pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                VehicleConstants.SPEED_MODIFIER_UUID,
                VehicleConstants.SPEED_MODIFIER_NAME, config.getPigSpeedMultiplier());
    }

    // -------------------------------------------------------------------------
    // Strider
    // -------------------------------------------------------------------------

    private void handleStriderEnter(Strider strider, Player player) {
        if (!config.isStridersEnabled()) return;
        if (config.isStriderRequirePermission()
                && !player.hasPermission("vehiclealternative.use.strider")) return;

        // Striders float ON TOP of lava — check block below, not the block at feet
        Block below = strider.getLocation().getBlock().getRelative(BlockFace.DOWN);
        double multiplier = (below.getType() == Material.LAVA)
                ? config.getStriderLavaSpeed()
                : config.getStriderLandSpeed();

        applyModifier(strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                VehicleConstants.SPEED_MODIFIER_UUID,
                VehicleConstants.SPEED_MODIFIER_NAME, multiplier);
    }

    // -------------------------------------------------------------------------
    // Camel
    // -------------------------------------------------------------------------

    private void handleCamelEnter(Camel camel, Player player) {
        if (!config.isCamelsEnabled()) return;
        if (config.isCamelRequirePermission()
                && !player.hasPermission("vehiclealternative.use.camel")) return;
        applyModifier(camel.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                VehicleConstants.SPEED_MODIFIER_UUID,
                VehicleConstants.SPEED_MODIFIER_NAME, config.getCamelSpeedMultiplier());
    }

    // -------------------------------------------------------------------------
    // Boat — target-based velocity (no per-tick multiplicative runaway)
    // -------------------------------------------------------------------------

    private void handleBoatMove(Boat boat) {
        if (!config.isBoatsEnabled()) return;
        if (boat.getPassengers().isEmpty()) return;
        Entity passenger = boat.getPassengers().get(0);
        if (!(passenger instanceof Player)) return;
        Player player = (Player) passenger;
        if (config.isBoatRequirePermission()
                && !player.hasPermission("vehiclealternative.use.boat")) return;
        if (!config.getAllowedBoatTypes().contains(boat.getBoatType())) return;

        Vector velocity = boat.getVelocity();
        double horizSpeed = Math.sqrt(velocity.getX() * velocity.getX()
                + velocity.getZ() * velocity.getZ());
        if (horizSpeed < 0.01) return;

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

        // Block-speed-boost stacks on top of the terrain multiplier
        if (config.isBlockSpeedBoostEnabled() && config.isBlockSpeedApplyToVehicles()) {
            if (config.isBlockSpeedRequirePermission()
                    && !player.hasPermission("vehiclealternative.blockspeed")) {
                // no extra boost
            } else {
                Block below = boat.getLocation().getBlock().getRelative(BlockFace.DOWN);
                if (config.getBlockSpeedBlocks().contains(blockType)
                        || config.getBlockSpeedBlocks().contains(below.getType())) {
                    multiplier *= config.getBlockSpeedMultiplier();
                }
            }
        }

        double targetSpeed = VehicleConstants.VANILLA_BOAT_WATER_SPEED * multiplier;
        if (horizSpeed < targetSpeed) {
            double scale = targetSpeed / horizSpeed;
            velocity.setX(velocity.getX() * scale);
            velocity.setZ(velocity.getZ() * scale);
            boat.setVelocity(velocity);
        }
    }

    // -------------------------------------------------------------------------
    // Minecart — normalized velocity, max-speed cap
    // -------------------------------------------------------------------------

    private void handleMinecartMove(Minecart minecart) {
        if (!config.isMinecartsEnabled()) return;
        if (!isMinecartTypeAllowed(minecart)) return;
        if (minecart.getPassengers().isEmpty()) return;
        Entity passenger = minecart.getPassengers().get(0);
        if (!(passenger instanceof Player)) return;
        Player player = (Player) passenger;
        if (config.isMinecartRequirePermission()
                && !player.hasPermission("vehiclealternative.use.minecart")) return;

        Vector velocity = minecart.getVelocity();
        double speed = velocity.length();
        if (speed < 0.01) return;

        double maxSpeed = config.getMinecartMaxSpeed();
        Material railType = minecart.getLocation().getBlock().getType();
        double boost = (railType == Material.POWERED_RAIL) ? config.getPoweredRailBoost() : 1.0;

        // Block-speed-boost
        double blockBoost = 1.0;
        if (config.isBlockSpeedBoostEnabled() && config.isBlockSpeedApplyToVehicles()) {
            boolean hasPerm = !config.isBlockSpeedRequirePermission()
                    || player.hasPermission("vehiclealternative.blockspeed");
            if (hasPerm) {
                Block below = minecart.getLocation().getBlock().getRelative(BlockFace.DOWN);
                if (config.getBlockSpeedBlocks().contains(below.getType())) {
                    blockBoost = config.getBlockSpeedMultiplier();
                }
            }
        }

        double newSpeed = Math.min(speed * config.getMinecartSpeedMultiplier() * boost * blockBoost, maxSpeed);
        if (newSpeed > speed) {
            minecart.setVelocity(velocity.normalize().multiply(newSpeed));
        }
    }

    // -------------------------------------------------------------------------
    // Shared attribute helper
    // -------------------------------------------------------------------------

    /**
     * Applies a MULTIPLY_SCALAR_1 modifier, removing any pre-existing one with
     * the same UUID first to prevent stacking on repeated mounts.
     */
    public static void applyModifier(AttributeInstance attr, UUID uuid,
                                     String name, double multiplier) {
        if (attr == null) return;
        for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
            if (mod.getUniqueId().equals(uuid)) attr.removeModifier(mod);
        }
        // MULTIPLY_SCALAR_1: finalValue = base + base * amount  →  amount = multiplier − 1
        attr.addModifier(new AttributeModifier(uuid, name,
                multiplier - 1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
    }

    private boolean isMinecartTypeAllowed(Minecart minecart) {
        if (minecart instanceof StorageMinecart)  return config.isMinecartChest();
        if (minecart instanceof PoweredMinecart)  return config.isMinecartFurnace();
        if (minecart instanceof HopperMinecart)   return config.isMinecartHopper();
        if (minecart instanceof ExplosiveMinecart) return config.isMinecartTNT();
        return config.isMinecartRegular();
    }
}
