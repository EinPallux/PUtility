package com.pallux.putility.commands;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.hide.HideFeature;
import com.pallux.putility.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HideCommand implements CommandExecutor {

    private final PUtility plugin;

    public HideCommand(PUtility plugin) {
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

        if (!player.hasPermission("pu.hide")) {
            player.sendMessage(MessageUtils.parse(
                    plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ")
                    + plugin.getConfigManager().get("messages").getString("no-permission", "&cYou do not have permission to use this command."), player));
            return true;
        }

        HideFeature feature = plugin.getHideFeature();
        if (feature == null || !feature.isEnabled()) {
            player.sendMessage(MessageUtils.parse(
                    plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ")
                    + plugin.getConfigManager().get("hide").getString("messages.hide-disabled", "&cThe hide feature is currently disabled."), player));
            return true;
        }

        feature.toggleHide(player);
        return true;
    }
}
