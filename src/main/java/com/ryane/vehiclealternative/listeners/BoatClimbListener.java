package com.ryane.vehiclealternative.listeners;

import com.ryane.vehiclealternative.VehicleAlternative;
import com.ryane.vehiclealternative.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
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
        if (!config.isEnabled() || !config.isBoatsEnabled() || !config.isBoatClimbingEnabled()) return;
        if (!(event.getVehicle() instanceof Boat)) return;

        Boat boat = (Boat) event.getVehicle();

        // Respect boats.types config
        if (!config.getAllowedBoatTypes().contains(boat.getBoatType())) return;

        if (boat.getPassengers().isEmpty()) return;
        Entity passenger = boat.getPassengers().get(0);
        if (!(passenger instanceof Player)) return;
        Player player = (Player) passenger;
        if (config.isBoatRequirePermission() && !player.hasPermission("vehiclealternative.use.boat")) return;

        Vector velocity = boat.getVelocity();
        if (velocity.length() < 0.1) return;

        Block currentBlock = boat.getLocation().getBlock();
        Block blockAhead   = getBlockAhead(boat, velocity);

        Material currentMaterial = currentBlock.getType();
        Material aheadMaterial   = blockAhead.getType();

        boolean inWater = isWaterOrWaterlogged(currentBlock);
        boolean onLand  = !inWater && currentBlock.getRelative(BlockFace.DOWN).getType().isSolid();

        boolean shouldClimb = false;

        if (inWater && config.isBoatClimbInWater()) {
            // Boat is in water and the block ahead is also water (waterfall / water column)
            if (isWaterOrWaterlogged(blockAhead)) {
                shouldClimb = true;
            }
        }

        if (onLand && config.isBoatClimbOnLand()) {
            // Solid block ahead that isn't a barrier (invisible obstacle — not logical to climb)
            if (aheadMaterial.isSolid() && aheadMaterial != Material.BARRIER) {
                shouldClimb = true;
            }
        }

        if (!shouldClimb) return;

        int climbHeight = calculateClimbHeight(blockAhead, config.getBoatMaxClimbHeight());
        if (climbHeight > 0 && climbHeight <= config.getBoatMaxClimbHeight()) {
            performClimb(boat, climbHeight);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Block getBlockAhead(Boat boat, Vector velocity) {
        Vector direction = velocity.clone().normalize();
        return boat.getLocation().clone().add(direction.multiply(1.5)).getBlock();
    }

    /**
     * Returns true if the block is WATER, BUBBLE_COLUMN, or any waterlogged block.
     * Fixes the original check that missed waterlogged slabs/stairs/etc. (1.20.1+).
     */
    private boolean isWaterOrWaterlogged(Block block) {
        Material m = block.getType();
        if (m == Material.WATER || m == Material.BUBBLE_COLUMN) return true;
        BlockData data = block.getBlockData();
        return (data instanceof Waterlogged) && ((Waterlogged) data).isWaterlogged();
    }

    /**
     * Counts consecutive solid blocks going up from {@code baseBlock}.
     * Returns 0 if the block at baseBlock is already clear.
     */
    private int calculateClimbHeight(Block baseBlock, int maxHeight) {
        int height = 0;
        Block check = baseBlock;
        while (height < maxHeight && check.getType().isSolid()) {
            height++;
            check = check.getRelative(BlockFace.UP);
        }
        return height;
    }

    private void performClimb(Boat boat, int height) {
        Vector velocity = boat.getVelocity();
        velocity.setY(config.getBoatClimbSpeed() * height * 0.5);
        boat.setVelocity(velocity);

        if (config.isDebug()) {
            plugin.getLogger().info("Boat climbing " + height + " block(s)");
        }
    }
}
