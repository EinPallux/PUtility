package com.pallux.putility.features.doublejump;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.Feature;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DoubleJumpFeature extends Feature {

    // Players who currently have their double jump charge available
    private final Set<UUID> hasDoubleJump = new HashSet<>();
    // Players currently on cooldown: uuid → system time (ms) when cooldown expires
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public DoubleJumpFeature(PUtility plugin) {
        super(plugin);
    }

    @Override
    protected void onEnable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
        plugin.getLogger().info("DoubleJump feature enabled.");
    }

    @Override
    protected void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            removeDoubleJump(player);
        }
        hasDoubleJump.clear();
        cooldowns.clear();
        plugin.getLogger().info("DoubleJump feature disabled.");
    }

    @Override
    protected void onReload() {
        cooldowns.clear();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updatePlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        hasDoubleJump.remove(uuid);
        cooldowns.remove(uuid);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        updatePlayer(event.getPlayer());
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Only intercept if this player has a double jump charge and is NOT being
        // granted flight by LobbyFly (i.e. they are not actively flying via LobbyFly)
        if (!hasDoubleJump.contains(uuid)) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;

        // If LobbyFly has this player flying, don't intercept — let vanilla flight toggle work
        boolean lobbyFlyActive = plugin.getLobbyFlyFeature() != null
                && plugin.getLobbyFlyFeature().isEnabled()
                && plugin.getLobbyFlyFeature().isFlyingViaFeature(player);
        if (lobbyFlyActive) return;

        event.setCancelled(true);
        player.setAllowFlight(false);
        hasDoubleJump.remove(uuid);

        // Apply velocity
        double vertical   = plugin.getConfigManager().get("doublejump").getDouble("velocity.vertical", 1.0);
        double horizontal = plugin.getConfigManager().get("doublejump").getDouble("velocity.horizontal", 0.0);

        Vector vel = player.getVelocity();
        vel.setY(vertical);
        if (horizontal > 0) {
            Vector dir = player.getLocation().getDirection().normalize();
            dir.setY(0);
            if (dir.lengthSquared() > 0) {
                dir.normalize().multiply(horizontal);
                vel.setX(dir.getX());
                vel.setZ(dir.getZ());
            }
        }
        player.setVelocity(vel);

        // Particle effect
        String effectStr = plugin.getConfigManager().get("doublejump").getString("effect", "");
        if (effectStr != null && !effectStr.isBlank()) {
            try {
                Particle particle = Particle.valueOf(effectStr.toUpperCase());
                player.getWorld().spawnParticle(particle, player.getLocation(), 20, 0.3, 0.1, 0.3, 0.05);
            } catch (IllegalArgumentException ignored) {}
        }

        // Start cooldown
        long cooldownSeconds = plugin.getConfigManager().get("doublejump").getLong("cooldown-seconds", 0L);
        if (cooldownSeconds > 0) {
            cooldowns.put(uuid, System.currentTimeMillis() + (cooldownSeconds * 1000L));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!isInAllowedWorld(player)) return;
        if (!player.hasPermission("pu.doublejump")) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (!player.isOnGround()) return;
        if (hasDoubleJump.contains(uuid)) return;

        // Check cooldown
        Long expiresAt = cooldowns.get(uuid);
        if (expiresAt != null) {
            if (System.currentTimeMillis() < expiresAt) return;
            cooldowns.remove(uuid);
        }

        // Restore double jump charge — only set allowFlight if LobbyFly isn't already doing so
        hasDoubleJump.add(uuid);
        player.setAllowFlight(true);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns true if this player currently has a double jump charge. */
    public boolean hasCharge(Player player) {
        return hasDoubleJump.contains(player.getUniqueId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updatePlayer(Player player) {
        if (isInAllowedWorld(player)
                && player.hasPermission("pu.doublejump")
                && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)) {
            hasDoubleJump.add(player.getUniqueId());
            player.setAllowFlight(true);
        } else {
            removeDoubleJump(player);
        }
    }

    private void removeDoubleJump(Player player) {
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            // Only revoke allowFlight if LobbyFly isn't keeping it active
            boolean lobbyFlyActive = plugin.getLobbyFlyFeature() != null
                    && plugin.getLobbyFlyFeature().isEnabled()
                    && plugin.getLobbyFlyFeature().isFlyingViaFeature(player);
            if (!lobbyFlyActive) {
                player.setAllowFlight(false);
            }
        }
        hasDoubleJump.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());
    }

    private boolean isInAllowedWorld(Player player) {
        List<String> worlds = plugin.getConfigManager().get("doublejump").getStringList("worlds");
        return worlds.contains(player.getWorld().getName());
    }
}