package com.pallux.putility.features.hide;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.Feature;
import com.pallux.putility.utils.MessageUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class HideFeature extends Feature {

    // Maps player UUID → scrambled name
    private final Map<UUID, String> hiddenPlayers = new HashMap<>();
    // Maps player UUID → original display name component
    private final Map<UUID, Component> originalDisplayNames = new HashMap<>();
    // Maps player UUID → original tab name component
    private final Map<UUID, Component> originalTabNames = new HashMap<>();

    public HideFeature(PUtility plugin) {
        super(plugin);
    }

    @Override
    protected void onEnable() {
        plugin.getLogger().info("Hide feature enabled.");
    }

    @Override
    protected void onDisable() {
        // Restore all hidden players
        for (UUID uuid : new HashSet<>(hiddenPlayers.keySet())) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) restorePlayer(p);
        }
        hiddenPlayers.clear();
        originalDisplayNames.clear();
        originalTabNames.clear();
        plugin.getLogger().info("Hide feature disabled.");
    }

    @Override
    protected void onReload() {
        // Nothing config-dependent to reload at runtime
    }

    /**
     * Toggle hide state for the given player.
     * @return true if the player is now hidden, false if now visible.
     */
    public boolean toggleHide(Player player) {
        if (isHidden(player)) {
            restorePlayer(player);
            return false;
        } else {
            hidePlayer(player);
            return true;
        }
    }

    public boolean isHidden(Player player) {
        return hiddenPlayers.containsKey(player.getUniqueId());
    }

    private void hidePlayer(Player player) {
        FileConfiguration cfg = plugin.getConfigManager().get("hide");

        // Store originals before overwriting
        originalDisplayNames.put(player.getUniqueId(), player.displayName());
        originalTabNames.put(player.getUniqueId(), player.playerListName());

        // Generate scrambled name
        String scrambled = scrambleName(player.getName(), cfg);
        hiddenPlayers.put(player.getUniqueId(), scrambled);

        boolean replaceDisplay = cfg.getBoolean("settings.replace-display-name", true);
        boolean replaceTab    = cfg.getBoolean("settings.replace-tab-name", true);

        Component scrambledComponent = MessageUtils.parse("&8[&7?&8] &7" + scrambled);

        if (replaceDisplay) {
            player.displayName(scrambledComponent);
        }
        if (replaceTab) {
            player.playerListName(scrambledComponent);
        }

        String prefix = plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ");
        String hideMsg = cfg.getString("messages.now-hidden", "&7Your name has been scrambled. You are now hidden.");
        player.sendMessage(MessageUtils.parse(prefix + hideMsg, player));
    }

    private void restorePlayer(Player player) {
        FileConfiguration cfg = plugin.getConfigManager().get("hide");

        Component originalDisplay = originalDisplayNames.remove(player.getUniqueId());
        Component originalTab    = originalTabNames.remove(player.getUniqueId());
        hiddenPlayers.remove(player.getUniqueId());

        // Restore display name — fall back to username component if not stored
        player.displayName(originalDisplay != null ? originalDisplay
                : Component.text(player.getName()));
        // Restore tab name — null resets to default (username)
        player.playerListName(originalTab != null ? originalTab
                : Component.text(player.getName()));

        String prefix = plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ");
        String showMsg = cfg.getString("messages.now-visible", "&7Your name has been restored. You are now visible.");
        player.sendMessage(MessageUtils.parse(prefix + showMsg, player));
    }

    private String scrambleName(String original, FileConfiguration cfg) {
        String charset = cfg.getString("settings.scramble-charset",
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        int length = cfg.getInt("settings.scramble-length", 0);
        if (length <= 0) length = original.length();

        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(random.nextInt(charset.length())));
        }
        return sb.toString();
    }

    /**
     * Intercept chat for hidden players and replace their name with the scrambled one.
     * Uses AsyncChatEvent renderer so it works with all chat plugins.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!isHidden(player)) return;

        FileConfiguration cfg = plugin.getConfigManager().get("hide");
        String format = cfg.getString("messages.chat-format-hidden",
                "&8[&7?&8] &7{scrambled}&8: &f{message}");

        String scrambled = hiddenPlayers.get(player.getUniqueId());

        // Extract plain message text from the Component
        String messageText = PlainTextComponentSerializer.plainText().serialize(event.message());

        String formatted = format
                .replace("{scrambled}", scrambled)
                .replace("{player}", player.getName())
                .replace("{message}", messageText);

        Component formattedComponent = MessageUtils.parse(formatted, player);

        // Override the renderer so every viewer sees the scrambled format
        event.renderer((source, sourceDisplayName, message, viewer) -> formattedComponent);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isHidden(player)) {
            // Clean up data — don't bother restoring display name since they're leaving
            hiddenPlayers.remove(player.getUniqueId());
            originalDisplayNames.remove(player.getUniqueId());
            originalTabNames.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Ensure no stale data from a previous session
        UUID uuid = event.getPlayer().getUniqueId();
        hiddenPlayers.remove(uuid);
        originalDisplayNames.remove(uuid);
        originalTabNames.remove(uuid);
    }

    public Map<UUID, String> getHiddenPlayers() {
        return Collections.unmodifiableMap(hiddenPlayers);
    }
}
