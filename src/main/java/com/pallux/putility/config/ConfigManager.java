package com.pallux.putility.config;

import com.pallux.putility.PUtility;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final PUtility plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();

    private static final String[] CONFIG_NAMES = {
            "config", "messages", "simpleshop", "hide"
    };

    public ConfigManager(PUtility plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        for (String name : CONFIG_NAMES) {
            loadConfig(name);
        }
    }

    private void loadConfig(String name) {
        String fileName = name.equals("config") ? "config.yml" : name + ".yml";
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from jar
        InputStream defStream = plugin.getResource(fileName);
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
        }

        configs.put(name, config);
        configFiles.put(name, file);
    }

    public FileConfiguration get(String name) {
        return configs.getOrDefault(name, plugin.getConfig());
    }

    public void save(String name) {
        File file = configFiles.get(name);
        FileConfiguration config = configs.get(name);
        if (file == null || config == null) return;
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + name + ".yml", e);
        }
    }
}
