package com.pallux.putility.features.aliascommands;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.Feature;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class AliasCommandsFeature extends Feature {

    // alias (lowercase) → target command string
    private final Map<String, String> aliases = new HashMap<>();

    public AliasCommandsFeature(PUtility plugin) {
        super(plugin);
    }

    @Override
    protected void onEnable() {
        loadAliases();
        registerAll();
        plugin.getLogger().info("AliasCommands feature enabled with " + aliases.size() + " alias(es).");
    }

    @Override
    protected void onDisable() {
        aliases.clear();
        plugin.getLogger().info("AliasCommands feature disabled.");
    }

    @Override
    protected void onReload() {
        aliases.clear();
        loadAliases();
        registerAll();
        plugin.getLogger().info("AliasCommands reloaded with " + aliases.size() + " alias(es).");
    }

    private void loadAliases() {
        FileConfiguration cfg = plugin.getConfigManager().get("aliascommands");
        ConfigurationSection section = cfg.getConfigurationSection("aliases");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            String target = section.getString(key);
            if (target != null && !target.isBlank()) {
                aliases.put(key.toLowerCase(), target);
            }
        }
    }

    private void registerAll() {
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            AliasCommand cmd = new AliasCommand(plugin, entry.getKey(), entry.getValue());
            plugin.getServer().getCommandMap().register(plugin.getName().toLowerCase(), cmd);
        }
    }

    public Map<String, String> getAliases() {
        return aliases;
    }
}