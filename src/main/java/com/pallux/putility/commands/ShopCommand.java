package com.pallux.putility.commands;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.simpleshop.ShopFeature;
import com.pallux.putility.features.simpleshop.ShopMainGui;
import com.pallux.putility.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShopCommand implements CommandExecutor, TabCompleter {

    private final PUtility plugin;

    public ShopCommand(PUtility plugin) {
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

        ShopFeature feature = plugin.getShopFeature();
        if (feature == null || !feature.isEnabled()) {
            player.sendMessage(MessageUtils.parse(
                    plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ")
                            + plugin.getConfigManager().get("messages").getString("shop-disabled", "&cThe shop is currently disabled."), player));
            return true;
        }

        if (!player.hasPermission("pu.shop")) {
            player.sendMessage(MessageUtils.parse(
                    plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ")
                            + plugin.getConfigManager().get("messages").getString("no-permission", "&cYou do not have permission to use this command."), player));
            return true;
        }

        new ShopMainGui(plugin, player, feature.getShopData()).open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}