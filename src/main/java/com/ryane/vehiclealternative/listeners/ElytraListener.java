package com.ryane.vehiclealternative.listeners;

import com.ryane.vehiclealternative.VehicleAlternative;
import com.ryane.vehiclealternative.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Manages elytra behaviour configured under {@code elytra} in config.yml:
 *
 * <ul>
 *   <li>Horizontal speed adjustment (soft cap, not physics override).</li>
 *   <li>Extra durability drain, or slower drain, while gliding.</li>
 *   <li>Optional firework-boost disable while gliding.</li>
 *   <li>Configurable firework-boost speed multiplier.</li>
 * </ul>
 *
 * Speed and durability ticks are driven by the scheduled task in
 * {@link VehicleAlternative}: {@code tickSpeed(Player)} every 5 ticks,
 * {@code tickDurability(Player)} every 20 ticks.
 */
public class ElytraListener implements Listener {

    /**
     * Vanilla horizontal cruise speed estimate (blocks/tick) used as the
     * reference point for the speed multiplier soft-cap. Elytra natural speed
     * varies with angle; this constant represents level flight at modest height.
     */
    private static final double VANILLA_CRUISE_SPEED = 0.8;

    private final VehicleAlternative plugin;
    private final ConfigManager      config;

    public ElytraListener(VehicleAlternative plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    // -------------------------------------------------------------------------
    // Firework — disable or multiply boost
    // -------------------------------------------------------------------------

    /**
     * Intercepts firework launches while the shooter is gliding.
     * Cancels if fireworks are disabled, or schedules a 1-tick follow-up
     * velocity scale when the firework-speed multiplier differs from 1.0.
     */
    @EventHandler(ignoreCancelled = true)
    public void onFireworkLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Firework)) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        if (!config.isEnabled() || !config.isElytraEnabled()) return;

        Player player = (Player) event.getEntity().getShooter();
        if (!player.isGliding()) return;

        if (config.isElytraRequirePermission()
                && !player.hasPermission("vehiclealternative.elytra")) return;

        // ── Hard disable ──────────────────────────────────────────────────────
        if (config.isElytraDisableFireworks()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getPrefix() + "&cFirework boosting is disabled while gliding."));
            return;
        }

        // ── Firework speed multiplier ─────────────────────────────────────────
        double mult = config.getElytraFireworkSpeedMultiplier();
        if (Math.abs(mult - 1.0) < 0.01) return;

        // The vanilla boost is applied the same tick the firework launches.
        // A 1-tick delay ensures vanilla has already written the new velocity,
        // then we scale it.
        UUID uuid = player.getUniqueId();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p == null || !p.isGliding()) return;
            Vector vel = p.getVelocity();
            if (vel.length() > 0.05) {
                p.setVelocity(vel.multiply(mult));
            }
        }, 1L);
    }

    // -------------------------------------------------------------------------
    // Periodic ticks — driven by VehicleAlternative's scheduler
    // -------------------------------------------------------------------------

    /**
     * Called every 5 ticks for each online player.
     * Adjusts horizontal speed toward the configured soft cap without affecting
     * vertical velocity (gravity / dive physics remain vanilla).
     */
    public void tickSpeed(Player player) {
        if (!config.isEnabled() || !config.isElytraEnabled()) return;
        if (!player.isGliding()) return;
        if (config.isElytraRequirePermission()
                && !player.hasPermission("vehiclealternative.elytra")) return;

        double mult = config.getElytraSpeedMultiplier();
        if (Math.abs(mult - 1.0) < 0.01) return;

        Vector vel    = player.getVelocity();
        double horizSpeed = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());
        if (horizSpeed < 0.05) return;

        // Soft target: multiples of the vanilla cruise estimate
        double targetHorizSpeed = VANILLA_CRUISE_SPEED * mult;

        if (mult > 1.0 && horizSpeed < targetHorizSpeed) {
            // Gently boost toward target — 3 % per 5-tick step (~12 steps to target from rest)
            double newSpeed = Math.min(horizSpeed * 1.03, targetHorizSpeed);
            double scale    = newSpeed / horizSpeed;
            vel.setX(vel.getX() * scale);
            vel.setZ(vel.getZ() * scale);
            player.setVelocity(vel);
        } else if (mult < 1.0 && horizSpeed > targetHorizSpeed) {
            // Gently dampen toward target — 3 % per 5-tick step
            double newSpeed = Math.max(horizSpeed * 0.97, targetHorizSpeed);
            double scale    = newSpeed / horizSpeed;
            vel.setX(vel.getX() * scale);
            vel.setZ(vel.getZ() * scale);
            player.setVelocity(vel);
        }
    }

    /**
     * Called every 20 ticks (1 second) for each gliding player.
     *
     * <p>Vanilla drains 1 durability per second automatically; this method
     * either adds extra damage ({@code multiplier > 1}) or restores a fraction
     * ({@code multiplier < 1}) to achieve the configured net drain rate:
     * {@code net = multiplier} durability per second.</p>
     */
    public void tickDurability(Player player) {
        if (!config.isEnabled() || !config.isElytraEnabled()) return;
        if (!player.isGliding()) return;
        if (config.isElytraRequirePermission()
                && !player.hasPermission("vehiclealternative.elytra")) return;

        double mult = config.getElytraDurabilityMultiplier();
        if (Math.abs(mult - 1.0) < 0.01) return;

        ItemStack elytra = player.getInventory().getChestplate();
        if (elytra == null || elytra.getType() != Material.ELYTRA) return;

        ItemMeta meta = elytra.getItemMeta();
        if (!(meta instanceof Damageable)) return;
        Damageable damageable = (Damageable) meta;

        int  currentDamage  = damageable.getDamage();
        int  maxDurability  = elytra.getType().getMaxDurability();

        if (mult > 1.0) {
            // Extra drain: add (mult − 1) damage per second on top of vanilla's 1/s
            // Use probabilistic rounding for fractional values.
            double extra        = mult - 1.0;
            int    wholeDamage  = (int) extra;
            int    toApply      = wholeDamage + (Math.random() < (extra - wholeDamage) ? 1 : 0);
            if (toApply > 0) {
                int newDamage = Math.min(currentDamage + toApply, maxDurability - 1);
                damageable.setDamage(newDamage);
                elytra.setItemMeta(meta);
            }
        } else {
            // Slower drain: restore (1 − mult) durability per second to counteract
            // vanilla's fixed 1/s drain so the net rate equals mult/s.
            double restore       = 1.0 - mult;
            int    wholeRestore  = (int) restore;
            int    toRestore     = wholeRestore + (Math.random() < (restore - wholeRestore) ? 1 : 0);
            if (toRestore > 0 && currentDamage > 0) {
                damageable.setDamage(Math.max(0, currentDamage - toRestore));
                elytra.setItemMeta(meta);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // No per-player state requires cleanup; held cooldowns are in
        // EnderPearlListener. Method kept as hook for future additions.
    }
}
