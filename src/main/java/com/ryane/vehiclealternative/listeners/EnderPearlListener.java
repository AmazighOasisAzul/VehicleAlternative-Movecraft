package com.ryane.vehiclealternative.listeners;

import com.ryane.vehiclealternative.VehicleAlternative;
import com.ryane.vehiclealternative.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles ender-pearl restrictions configured under {@code ender-pearls} in config.yml.
 *
 * <ul>
 *   <li>{@code disable: true} — cancels every throw (pearl is returned to inventory).</li>
 *   <li>{@code cooldown: N} — N seconds of extra cooldown on top of vanilla's built-in 1s.</li>
 * </ul>
 */
public class EnderPearlListener implements Listener {

    private final VehicleAlternative plugin;
    private final ConfigManager      config;

    /** Stores System.currentTimeMillis() of each player's last successful throw. */
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public EnderPearlListener(VehicleAlternative plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        if (!config.isEnabled() || !config.isEnderPearlsEnabled()) return;

        Player player = (Player) event.getEntity().getShooter();

        // Permission gate — if required and missing, leave vanilla behaviour untouched
        if (config.isEnderPearlRequirePermission()
                && !player.hasPermission("vehiclealternative.enderpearl")) return;

        // ── Hard disable ──────────────────────────────────────────────────────
        if (config.isEnderPearlDisable()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getPrefix() + "&cEnder pearls are disabled on this server."));
            return;
        }

        // ── Extra cooldown ────────────────────────────────────────────────────
        long cooldownMs = (long) config.getEnderPearlCooldown() * 1000L;
        if (cooldownMs <= 0) return;

        long now = System.currentTimeMillis();
        Long lastThrow = cooldowns.get(player.getUniqueId());

        if (lastThrow != null && now - lastThrow < cooldownMs) {
            event.setCancelled(true);
            long remainingSec = ((cooldownMs - (now - lastThrow)) / 1000L) + 1L;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getPrefix() + "&eEnder pearl on cooldown — &f" + remainingSec
                            + "s &eremaining."));
            return;
        }

        // Pearl is going through — record the timestamp
        cooldowns.put(player.getUniqueId(), now);
    }

    /** Remove cooldown entry when a player disconnects. */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}
