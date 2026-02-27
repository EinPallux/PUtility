package com.pallux.putility.features.blockedcommands;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.Feature;
import com.pallux.putility.utils.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockedCommandsFeature extends Feature {

    private final Set<String> blockedCommands = new HashSet<>();

    public BlockedCommandsFeature(PUtility plugin) {
        super(plugin);
    }

    @Override
    protected void onEnable() {
        loadCommands();
        plugin.getLogger().info("BlockedCommands feature enabled.");
    }

    @Override
    protected void onDisable() {
        blockedCommands.clear();
        plugin.getLogger().info("BlockedCommands feature disabled.");
    }

    @Override
    protected void onReload() {
        loadCommands();
    }

    private void loadCommands() {
        blockedCommands.clear();
        FileConfiguration cfg = plugin.getConfigManager().get("blocked-commands");
        List<String> list = cfg.getStringList("blocked-commands");
        for (String cmd : list) {
            blockedCommands.add(cmd.toLowerCase());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().hasPermission("pu.admin")) return;

        // Extract the base command (first word, strip leading slash)
        String message = event.getMessage();
        String base = message.split(" ")[0].toLowerCase();
        if (base.startsWith("/")) base = base.substring(1);

        if (blockedCommands.contains(base)) {
            event.setCancelled(true);
            FileConfiguration cfg = plugin.getConfigManager().get("blocked-commands");
            String msg = cfg.getString("blocked-message", "&cCommand unknown or not permitted to use.");
            event.getPlayer().sendMessage(MessageUtils.parse(msg, event.getPlayer()));
        }
    }
}