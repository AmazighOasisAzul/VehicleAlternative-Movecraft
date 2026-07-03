package com.ryane.vehiclealternative.listeners;

import com.ryane.vehiclealternative.VehicleAlternative;
import com.ryane.vehiclealternative.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages elytra behaviour configured under {@code elytra} in config.yml:
 *
 * <ul>
 *   <li>Horizontal speed adjustment (soft cap, not a physics override).</li>
 *   <li>Extra / reduced durability drain while gliding — respects Unbreaking.</li>
 *   <li>Optional firework-boost disable while gliding.</li>
 *   <li>Configurable firework-boost speed multiplier applied after the firework
 *       explodes (matching the tick vanilla applies the velocity impulse).</li>
 * </ul>
 *
 * Speed and durability ticks are driven by the scheduled task in
 * {@link VehicleAlternative}: {@code tickSpeed(Player)} every 5 ticks,
 * {@code tickDurability(Player)} every 20 ticks.
 */
public class ElytraListener implements Listener {

    /**
     * Vanilla horizontal cruise speed estimate (blocks/tick) used as the
     * reference point for the speed-multiplier soft cap.  Actual elytra speed
     * varies with dive angle; this constant represents level cruise flight.
     */
    private static final double VANILLA_CRUISE_SPEED = 0.8;

    private final VehicleAlternative plugin;
    private final ConfigManager      config;

    /**
     * Tracks fireworks launched by gliding players so we can apply the
     * firework-speed multiplier when the firework explodes.
     * Key = firework entity UUID, Value = player UUID.
     */
    private final Map<UUID, UUID> fireworkToPlayer = new HashMap<>();

    public ElytraListener(VehicleAlternative plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    // -------------------------------------------------------------------------
    // Firework — disable or schedule speed-multiplier
    // -------------------------------------------------------------------------

    /**
     * Intercepts firework launches while the shooter is gliding.
     *
     * <p>If fireworks are disabled, the event is cancelled (no boost, no item
     * consumed).  Otherwise, if a non-1.0 speed multiplier is configured, the
     * firework's UUID is stored so {@link #onFireworkExplode} can apply the
     * multiplier at the correct moment.</p>
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

        // ── Track firework for speed-multiplier ───────────────────────────────
        // The vanilla elytra firework boost is applied when the firework explodes,
        // not on launch.  We record the mapping here and apply the multiplier in
        // onFireworkExplode (1 tick after explosion so vanilla runs first).
        double mult = config.getElytraFireworkSpeedMultiplier();
        if (Math.abs(mult - 1.0) >= 0.01) {
            fireworkToPlayer.put(event.getEntity().getUniqueId(), player.getUniqueId());
        }
    }

    /**
     * Applies the configured firework-speed multiplier one tick after the
     * firework explodes.  The 1-tick delay lets the vanilla velocity impulse
     * land first, then we scale the result.
     */
    @EventHandler
    public void onFireworkExplode(FireworkExplodeEvent event) {
        UUID playerUuid = fireworkToPlayer.remove(event.getEntity().getUniqueId());
        if (playerUuid == null) return;

        double mult = config.getElytraFireworkSpeedMultiplier();
        if (Math.abs(mult - 1.0) < 0.01) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player p = plugin.getServer().getPlayer(playerUuid);
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
     * vertical velocity (gravity and dive physics remain vanilla).
     */
    public void tickSpeed(Player player) {
        if (!config.isEnabled() || !config.isElytraEnabled()) return;
        if (!player.isGliding()) return;
        if (config.isElytraRequirePermission()
                && !player.hasPermission("vehiclealternative.elytra")) return;

        double mult = config.getElytraSpeedMultiplier();
        if (Math.abs(mult - 1.0) < 0.01) return;

        Vector vel       = player.getVelocity();
        double horizSpeed = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());
        if (horizSpeed < 0.05) return;

        double targetHorizSpeed = VANILLA_CRUISE_SPEED * mult;

        if (mult > 1.0 && horizSpeed < targetHorizSpeed) {
            // Gently boost toward target — 3% per 5-tick step
            double newSpeed = Math.min(horizSpeed * 1.03, targetHorizSpeed);
            double scale    = newSpeed / horizSpeed;
            vel.setX(vel.getX() * scale);
            vel.setZ(vel.getZ() * scale);
            player.setVelocity(vel);
        } else if (mult < 1.0 && horizSpeed > targetHorizSpeed) {
            // Gently dampen toward target — 3% per 5-tick step
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
     * <p>Vanilla drains 1 durability per second automatically.  This method
     * either adds extra damage ({@code multiplier > 1.0}) or restores a
     * fraction ({@code multiplier < 1.0}) to achieve the configured net rate:
     * {@code net = multiplier} durability per second.</p>
     *
     * <p>Extra damage respects the Unbreaking enchantment using the same
     * probability formula vanilla uses: each point has a
     * {@code 1/(level+1)} chance of being applied.</p>
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

        int currentDamage = damageable.getDamage();
        int maxDurability = elytra.getType().getMaxDurability();

        if (mult > 1.0) {
            // Extra drain: add (mult − 1) damage on top of vanilla's 1/s.
            // Uses probabilistic rounding for fractional values.
            double extra       = mult - 1.0;
            int    wholeDamage = (int) extra;
            int    toApply     = wholeDamage + (Math.random() < (extra - wholeDamage) ? 1 : 0);
            if (toApply <= 0) return;

            // Respect Unbreaking: vanilla uses 1/(level+1) probability per damage point.
            int    unbreaking      = elytra.getEnchantmentLevel(Enchantment.DURABILITY);
            double unbreakingProb  = (unbreaking > 0) ? (1.0 / (unbreaking + 1)) : 1.0;
            int    actualDamage    = 0;
            for (int i = 0; i < toApply; i++) {
                if (Math.random() < unbreakingProb) actualDamage++;
            }
            if (actualDamage <= 0) return;

            int newDamage = Math.min(currentDamage + actualDamage, maxDurability - 1);
            damageable.setDamage(newDamage);
            elytra.setItemMeta(meta);

        } else {
            // Slower drain: restore (1 − mult) durability per second to counteract
            // vanilla's fixed 1/s drain so the net rate equals mult/s.
            double restore      = 1.0 - mult;
            int    wholeRestore = (int) restore;
            int    toRestore    = wholeRestore + (Math.random() < (restore - wholeRestore) ? 1 : 0);
            if (toRestore <= 0 || currentDamage <= 0) return;

            damageable.setDamage(Math.max(0, currentDamage - toRestore));
            elytra.setItemMeta(meta);
        }
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    /** Remove firework-tracking entries when a player disconnects. */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        fireworkToPlayer.entrySet().removeIf(entry -> entry.getValue().equals(playerId));
    }
}
