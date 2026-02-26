package com.pallux.putility.features.lobbyfly;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.Feature;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LobbyFlyFeature extends Feature {

    // Players currently flying via this feature
    private final Set<UUID> flyingPlayers = new HashSet<>();

    public LobbyFlyFeature(PUtility plugin) {
        super(plugin);
    }

    @Override
    protected void onEnable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyFly(player);
        }
        plugin.getLogger().info("LobbyFly feature enabled.");
    }

    @Override
    protected void onDisable() {
        for (UUID uuid : new HashSet<>(flyingPlayers)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) revokeFly(player);
        }
        flyingPlayers.clear();
        plugin.getLogger().info("LobbyFly feature disabled.");
    }

    @Override
    protected void onReload() {
        for (UUID uuid : new HashSet<>(flyingPlayers)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) revokeFly(player);
        }
        flyingPlayers.clear();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyFly(player);
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applyFly(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        flyingPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        revokeFly(player);
        flyingPlayers.remove(player.getUniqueId());
        applyFly(player);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Toggles fly for the player. Returns true if fly is now ON, false if now OFF.
     */
    public boolean toggleFly(Player player) {
        if (flyingPlayers.contains(player.getUniqueId())) {
            revokeFly(player);
            flyingPlayers.remove(player.getUniqueId());
            return false;
        } else {
            grantFly(player);
            flyingPlayers.add(player.getUniqueId());
            return true;
        }
    }

    /**
     * Returns true if this player is currently flying via LobbyFly.
     */
    public boolean isFlyingViaFeature(Player player) {
        return flyingPlayers.contains(player.getUniqueId());
    }

    public boolean isInAllowedWorld(Player player) {
        List<String> worlds = plugin.getConfigManager().get("lobbyfly").getStringList("worlds");
        return worlds.contains(player.getWorld().getName());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void applyFly(Player player) {
        if (isInAllowedWorld(player) && player.hasPermission("pu.fly")) {
            grantFly(player);
            flyingPlayers.add(player.getUniqueId());
        }
    }

    private void grantFly(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    private void revokeFly(Player player) {
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            player.setFlying(false);
            boolean doubleJumpActive = plugin.getDoubleJumpFeature() != null
                    && plugin.getDoubleJumpFeature().isEnabled()
                    && plugin.getDoubleJumpFeature().hasCharge(player);
            if (!doubleJumpActive) {
                player.setAllowFlight(false);
            }
        }
    }
}