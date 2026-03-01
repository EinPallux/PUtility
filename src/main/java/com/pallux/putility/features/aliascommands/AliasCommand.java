package com.pallux.putility.features.aliascommands;

import com.pallux.putility.PUtility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AliasCommand extends Command {

    private final PUtility plugin;
    private final String targetCommand;

    public AliasCommand(PUtility plugin, String alias, String targetCommand) {
        super(alias);
        this.plugin = plugin;
        this.targetCommand = targetCommand;
        // Make the command appear in tab-complete for all players
        setPermission(null);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        // Build the resolved command, appending any extra args the player provided
        String resolved = targetCommand;
        if (args.length > 0) {
            resolved = resolved + " " + String.join(" ", args);
        }

        // Replace %player% if the sender is a player
        if (sender instanceof Player player) {
            resolved = resolved.replace("%player%", player.getName());
            player.performCommand(resolved);
        } else {
            resolved = resolved.replace("%player%", sender.getName());
            plugin.getServer().dispatchCommand(sender, resolved);
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        // Delegate tab-complete to the target command if possible
        String baseCmd = targetCommand.split(" ")[0];
        Command targetCmd = plugin.getServer().getCommandMap().getCommand(baseCmd);
        if (targetCmd != null) {
            // Build the args as if the player typed the full target command
            String[] targetArgs = buildTargetArgs(args);
            try {
                return targetCmd.tabComplete(sender, baseCmd, targetArgs);
            } catch (Exception ignored) {}
        }
        return Collections.emptyList();
    }

    /**
     * Merges any fixed arguments from the target command with the extra args
     * the player has typed so far, to pass to the real command's tab completer.
     */
    private String[] buildTargetArgs(String[] extraArgs) {
        String[] parts = targetCommand.split(" ");
        if (parts.length <= 1) {
            // No fixed args — pass through directly
            return extraArgs;
        }
        // parts[0] is the base command, parts[1..] are fixed args
        String[] fixedArgs = java.util.Arrays.copyOfRange(parts, 1, parts.length);
        String[] combined = new String[fixedArgs.length + extraArgs.length];
        System.arraycopy(fixedArgs, 0, combined, 0, fixedArgs.length);
        System.arraycopy(extraArgs, 0, combined, fixedArgs.length, extraArgs.length);
        return combined;
    }
}