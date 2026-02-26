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

    // Root-level configs
    private static final String[] ROOT_CONFIGS = {
            "config", "messages"
    };

    // Feature configs — stored in /features/ subfolder
    private static final String[] FEATURE_CONFIGS = {
            "simpleshop", "premiumshards", "doublejump", "lobbyfly"
    };

    public ConfigManager(PUtility plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        for (String name : ROOT_CONFIGS) {
            loadConfig(name, false);
        }
        for (String name : FEATURE_CONFIGS) {
            loadConfig(name, true);
        }
    }

    private void loadConfig(String name, boolean inFeaturesFolder) {
        String fileName = name.equals("config") ? "config.yml" : name + ".yml";
        String resourcePath = inFeaturesFolder ? "features/" + fileName : fileName;

        File file = inFeaturesFolder
                ? new File(plugin.getDataFolder(), "features" + File.separator + fileName)
                : new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(resourcePath, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        InputStream defStream = plugin.getResource(resourcePath);
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