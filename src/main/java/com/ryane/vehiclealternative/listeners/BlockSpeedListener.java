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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

/**
 * Applies a configurable speed boost to players walking on foot over any block
 * listed under {@code block-speed-boost-config.blocks} in config.yml.
 *
 * Vehicle block-speed is handled inside {@link VehicleSpeedListener} (boats /
 * minecarts via velocity) and the scheduled task in {@link VehicleAlternative}
 * (rideable mobs via AttributeModifier).
 */
public class BlockSpeedListener implements Listener {

    private final VehicleAlternative plugin;
    private final ConfigManager config;

    public BlockSpeedListener(VehicleAlternative plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    // -------------------------------------------------------------------------
    // Player foot movement
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!config.isEnabled()
                || !config.isBlockSpeedBoostEnabled()
                || !config.isBlockSpeedApplyToPlayers()) return;

        // Only re-evaluate when the player crosses a block boundary — avoids
        // running this logic dozens of times per tick for micro-movements.
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();

        // Players riding a vehicle are handled by the vehicle subsystem.
        if (player.isInsideVehicle()) {
            removePlayerBoost(player);
            return;
        }

        if (config.isBlockSpeedRequirePermission()
                && !player.hasPermission("vehiclealternative.blockspeed")) {
            removePlayerBoost(player);
            return;
        }

        // Check the block the player is standing on (one block below their feet).
        Block below = event.getTo().getBlock().getRelative(BlockFace.DOWN);
        if (isSpeedBlock(below.getType())) {
            applyPlayerBoost(player);
        } else {
            removePlayerBoost(player);
        }
    }

    /** Clean up when a player leaves the server. */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayerBoost(event.getPlayer());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public boolean isSpeedBlock(Material material) {
        return config.getBlockSpeedBlocks().contains(material);
    }

    /**
     * Applies the block-speed MULTIPLY_SCALAR_1 modifier to the player,
     * replacing any existing one so it never stacks on repeated calls.
     */
    private void applyPlayerBoost(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr == null) return;

        // Remove old modifier first to avoid stacking
        for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
            if (mod.getUniqueId().equals(VehicleConstants.BLOCK_SPEED_MODIFIER_UUID)) {
                attr.removeModifier(mod);
            }
        }

        double amount = config.getBlockSpeedMultiplier() - 1.0; // MULTIPLY_SCALAR_1: base + base*amount
        attr.addModifier(new AttributeModifier(
                VehicleConstants.BLOCK_SPEED_MODIFIER_UUID,
                VehicleConstants.BLOCK_SPEED_MODIFIER_NAME,
                amount,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        ));

        if (config.isDebug()) {
            plugin.getLogger().info("BlockSpeed applied to " + player.getName()
                    + " (x" + config.getBlockSpeedMultiplier() + ")");
        }
    }

    /** Removes the block-speed modifier from a player if it is present. */
    public void removePlayerBoost(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr == null) return;
        for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
            if (mod.getUniqueId().equals(VehicleConstants.BLOCK_SPEED_MODIFIER_UUID)) {
                attr.removeModifier(mod);
            }
        }
    }
}
