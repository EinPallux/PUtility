package com.pallux.putility.features.unknowncommand;

import com.pallux.putility.PUtility;
import com.pallux.putility.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.lang.reflect.Field;

public class UnknownCommandListener implements Listener {

    private final PUtility plugin;
    private CommandMap commandMap;

    public UnknownCommandListener(PUtility plugin) {
        this.plugin = plugin;
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            commandMap = (CommandMap) field.get(Bukkit.getServer());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not access CommandMap - unknown command feature may not work.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        UnknownCommandFeature feature = plugin.getUnknownCommandFeature();
        if (feature == null || !feature.isEnabled()) return;

        String message = event.getMessage();
        // Strip leading slash
        String commandLine = message.startsWith("/") ? message.substring(1) : message;
        String label = commandLine.split(" ")[0].toLowerCase();

        // Check if this command actually exists
        if (commandMap != null) {
            Command cmd = commandMap.getCommand(label);
            if (cmd != null) return; // Known command, leave it alone
        }

        // Command doesn't exist — cancel and send custom message
        event.setCancelled(true);

        FileConfiguration cfg = plugin.getConfigManager().get("unknowncommand");
        String raw = cfg.getString("messages.unknown-command",
                "&cUnknown command &7'&f/{command}&7'&c. Try &e/help &cfor a list of commands.");
        String formatted = raw.replace("{command}", label);

        Player player = event.getPlayer();
        player.sendMessage(MessageUtils.parse(formatted, player));
    }
}
