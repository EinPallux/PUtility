package com.pallux.putility.commands;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.lobbyfly.LobbyFlyFeature;
import com.pallux.putility.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FlyCommand implements CommandExecutor, TabCompleter {

    private final PUtility plugin;

    public FlyCommand(PUtility plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtils.parse(
                    plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ")
                            + plugin.getConfigManager().get("messages").getString("player-only", "&cThis command can only be used by players.")));
            return true;
        }

        String prefix = plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ");

        if (!player.hasPermission("pu.fly")) {
            player.sendMessage(MessageUtils.parse(prefix
                            + plugin.getConfigManager().get("messages").getString("no-permission", "&cYou do not have permission to use this command."),
                    player));
            return true;
        }

        LobbyFlyFeature feature = plugin.getLobbyFlyFeature();
        if (feature == null || !feature.isEnabled()) {
            player.sendMessage(MessageUtils.parse(prefix
                            + plugin.getConfigManager().get("messages").getString("feature-disabled", "&cThis feature is currently disabled."),
                    player));
            return true;
        }

        if (!feature.isInAllowedWorld(player)) {
            player.sendMessage(MessageUtils.parse(prefix
                            + plugin.getConfigManager().get("messages").getString("fly-not-in-world", "&cYou cannot use fly in this world."),
                    player));
            return true;
        }

        boolean nowFlying = feature.toggleFly(player);

        String msgKey = nowFlying ? "fly-enabled" : "fly-disabled";
        String msgDefault = nowFlying ? "&aFlight enabled." : "&cFlight disabled.";
        player.sendMessage(MessageUtils.parse(prefix
                        + plugin.getConfigManager().get("messages").getString(msgKey, msgDefault),
                player));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}