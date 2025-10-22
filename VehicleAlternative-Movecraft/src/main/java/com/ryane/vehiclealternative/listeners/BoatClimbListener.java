package com.ryane.vehiclealternative.listeners;

import com.ryane.vehiclealternative.VehicleAlternative;
import com.ryane.vehiclealternative.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class BoatClimbListener implements Listener {

    private final VehicleAlternative plugin;
    private final ConfigManager config;

    public BoatClimbListener(VehicleAlternative plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onBoatMove(VehicleMoveEvent event) {
        if (!config.isEnabled() || !config.isBoatsEnabled() || !config.isBoatClimbingEnabled()) {
            return;
        }

        if (!(event.getVehicle() instanceof Boat)) {
            return;
        }

        Boat boat = (Boat) event.getVehicle();
        
        // Check if player is driving
        if (boat.getPassengers().isEmpty()) return;
        Entity passenger = boat.getPassengers().get(0);
        if (!(passenger instanceof Player)) return;
        
        Player player = (Player) passenger;
        if (config.isBoatRequirePermission() && !player.hasPermission("vehiclealternative.use.boat")) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Check if boat is moving forward
        Vector velocity = boat.getVelocity();
        if (velocity.length() < 0.1) return; // Too slow to climb

        // Get block ahead of boat
        Block currentBlock = boat.getLocation().getBlock();
        Block blockAhead = getBlockAhead(boat, velocity);
        Block blockAbove = currentBlock.getRelative(BlockFace.UP);
        
        Material currentMaterial = currentBlock.getType();
        Material aheadMaterial = blockAhead.getType();
        
        boolean inWater = isWater(currentMaterial);
        boolean onLand = !inWater && currentBlock.getRelative(BlockFace.DOWN).getType().isSolid();
        
        // Check if we should attempt climbing
        boolean shouldClimb = false;
        
        if (inWater && config.isBoatClimbInWater()) {
            // Climbing waterfalls or water blocks
            if (isWater(aheadMaterial) && aheadMaterial != Material.AIR) {
                shouldClimb = true;
            }
        }
        
        if (onLand && config.isBoatClimbOnLand()) {
            // Climbing on land
            if (aheadMaterial.isSolid() && !aheadMaterial.equals(Material.BARRIER)) {
                shouldClimb = true;
            }
        }
        
        if (!shouldClimb) return;
        
        // Calculate climb height
        int climbHeight = calculateClimbHeight(blockAhead, config.getBoatMaxClimbHeight());
        
        if (climbHeight > 0 && climbHeight <= config.getBoatMaxClimbHeight()) {
            performClimb(boat, climbHeight);
        }
    }

    private Block getBlockAhead(Boat boat, Vector velocity) {
        Vector direction = velocity.clone().normalize();
        Location ahead = boat.getLocation().clone().add(direction.multiply(1.5));
        return ahead.getBlock();
    }

    private boolean isWater(Material material) {
        return material == Material.WATER || material == Material.BUBBLE_COLUMN;
    }

    private int calculateClimbHeight(Block baseBlock, int maxHeight) {
        int height = 0;
        Block checkBlock = baseBlock;
        
        // Count solid blocks going up
        while (height < maxHeight && checkBlock.getType().isSolid()) {
            height++;
            checkBlock = checkBlock.getRelative(BlockFace.UP);
        }
        
        return height;
    }

    private void performClimb(Boat boat, int height) {
        double climbSpeed = config.getBoatClimbSpeed();
        Vector velocity = boat.getVelocity();
        
        // Add upward velocity based on climb speed
        velocity.setY(climbSpeed * height * 0.5);
        
        // Keep forward momentum
        boat.setVelocity(velocity);
        
        if (config.isDebug()) {
            plugin.getLogger().info("Boat climbing " + height + " blocks");
        }
    }
}
