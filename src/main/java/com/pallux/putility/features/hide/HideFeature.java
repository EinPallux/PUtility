package com.pallux.putility.features.hide;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.Feature;
import com.pallux.putility.utils.MessageUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
    // Maps player UUID → original display name
    private final Map<UUID, String> originalDisplayNames = new HashMap<>();

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
        plugin.getLogger().info("Hide feature disabled.");
    }

    @Override
    protected void onReload() {
        // Nothing config-dependent to reload at runtime
    }

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

        // Store original display name
        String originalName = LegacyComponentSerializer.legacyAmpersand()
                .serialize(player.displayName());
        originalDisplayNames.put(player.getUniqueId(), originalName);

        // Scramble name
        String scrambled = scrambleName(player.getName(), cfg);
        hiddenPlayers.put(player.getUniqueId(), scrambled);

        boolean replaceDisplay = cfg.getBoolean("settings.replace-display-name", true);
        boolean replaceTab = cfg.getBoolean("settings.replace-tab-name", true);

        if (replaceDisplay) {
            player.displayName(MessageUtils.parse("&8[&7?&8] &7" + scrambled));
        }
        if (replaceTab) {
            player.playerListName(MessageUtils.parse("&8[&7?&8] &7" + scrambled));
        }

        String hideMsg = cfg.getString("messages.now-hidden", "&7Your name has been scrambled. You are now hidden.");
        player.sendMessage(MessageUtils.parse(
                plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ") + hideMsg, player));
    }

    private void restorePlayer(Player player) {
        FileConfiguration cfg = plugin.getConfigManager().get("hide");

        String originalName = originalDisplayNames.remove(player.getUniqueId());
        hiddenPlayers.remove(player.getUniqueId());

        // Restore display name
        if (originalName != null) {
            player.displayName(MessageUtils.parse(originalName));
        } else {
            player.displayName(null); // Reset to default
        }
        player.playerListName(null); // Reset tab name

        String showMsg = cfg.getString("messages.now-visible", "&7Your name has been restored. You are now visible.");
        player.sendMessage(MessageUtils.parse(
                plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ") + showMsg, player));
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!isHidden(player)) return;

        FileConfiguration cfg = plugin.getConfigManager().get("hide");
        String format = cfg.getString("messages.chat-format-hidden", "&8[&7?&8] &7{scrambled}&8: &f{message}");

        String scrambled = hiddenPlayers.get(player.getUniqueId());
        String messageText = LegacyComponentSerializer.legacyAmpersand()
                .serialize(event.message());

        String formatted = format
                .replace("{scrambled}", scrambled)
                .replace("{player}", player.getName())
                .replace("{message}", messageText);

        event.renderer((source, sourceDisplayName, message, viewer) ->
                MessageUtils.parse(formatted, player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isHidden(player)) {
            restorePlayer(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Ensure no stale data
        hiddenPlayers.remove(event.getPlayer().getUniqueId());
        originalDisplayNames.remove(event.getPlayer().getUniqueId());
    }

    public Map<UUID, String> getHiddenPlayers() { return Collections.unmodifiableMap(hiddenPlayers); }
}
