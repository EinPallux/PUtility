package com.pallux.putility.commands;

import com.pallux.putility.PUtility;
import com.pallux.putility.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PUtilityCommand implements CommandExecutor, TabCompleter {

    private final PUtility plugin;

    public PUtilityCommand(PUtility plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("pu.admin")) {
            sender.sendMessage(MessageUtils.parse(
                    plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ")
                    + plugin.getConfigManager().get("messages").getString("no-permission", "&cYou do not have permission to use this command.")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(MessageUtils.parse("&7&lUTILITY &7➠ &7PUtility v" + plugin.getDescription().getVersion()));
            sender.sendMessage(MessageUtils.parse("&7Use &e/putility reload &7to reload configuration."));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            sender.sendMessage(MessageUtils.parse(
                    plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ")
                    + plugin.getConfigManager().get("messages").getString("reload-success", "&aConfiguration reloaded successfully.")));
            return true;
        }

        sender.sendMessage(MessageUtils.parse(
                plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ")
                + "&cUnknown subcommand. Use &e/putility reload&c."));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("pu.admin")) {
            return List.of("reload");
        }
        return List.of();
    }
}
