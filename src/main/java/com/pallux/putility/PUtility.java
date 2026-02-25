package com.pallux.putility;

import com.pallux.putility.commands.HideCommand;
import com.pallux.putility.commands.PUtilityCommand;
import com.pallux.putility.commands.ShopCommand;
import com.pallux.putility.config.ConfigManager;
import com.pallux.putility.economy.EconomyHandler;
import com.pallux.putility.economy.PlayerPointsHandler;
import com.pallux.putility.features.hide.HideFeature;
import com.pallux.putility.features.simpleshop.ShopFeature;
import com.pallux.putility.features.unknowncommand.UnknownCommandFeature;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PUtility extends JavaPlugin {

    private static PUtility instance;

    private ConfigManager configManager;
    private EconomyHandler economyHandler;
    private PlayerPointsHandler playerPointsHandler;
    private ShopFeature shopFeature;
    private HideFeature hideFeature;
    private UnknownCommandFeature unknownCommandFeature;

    @Override
    public void onEnable() {
        instance = this;

        // Load configs
        configManager = new ConfigManager(this);
        configManager.loadAll();

        // Setup economy (Vault)
        economyHandler = new EconomyHandler(this);
        if (!economyHandler.setup()) {
            getLogger().warning("Vault economy not found! Vault-priced shop items will not work.");
        }

        // Setup PlayerPoints (soft depend)
        playerPointsHandler = new PlayerPointsHandler();
        if (playerPointsHandler.setup()) {
            getLogger().info("PlayerPoints found and hooked successfully.");
        } else {
            getLogger().info("PlayerPoints not found. PlayerPoints-priced shop items will not work.");
        }

        // Register global GUI listener
        getServer().getPluginManager().registerEvents(new com.pallux.putility.gui.GuiListener(), this);

        // Init features
        initFeatures();

        // Register commands
        registerCommands();

        getLogger().info("PUtility enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (shopFeature != null) shopFeature.disable();
        if (hideFeature != null) hideFeature.disable();
        if (unknownCommandFeature != null) unknownCommandFeature.disable();
        getLogger().info("PUtility disabled.");
    }

    private void initFeatures() {
        boolean shopEnabled = getConfig().getBoolean("features.simpleshop", true);
        boolean hideEnabled = getConfig().getBoolean("features.hide", true);
        boolean unknownCmdEnabled = getConfig().getBoolean("features.unknowncommand", true);

        shopFeature = new ShopFeature(this);
        hideFeature = new HideFeature(this);
        unknownCommandFeature = new UnknownCommandFeature(this);

        if (shopEnabled) shopFeature.enable();
        if (hideEnabled) hideFeature.enable();
        if (unknownCmdEnabled) unknownCommandFeature.enable();
    }

    private void registerCommands() {
        registerCmd("shop", new ShopCommand(this));
        registerCmd("hide", new HideCommand(this));
        registerCmd("putility", new PUtilityCommand(this));
    }

    private void registerCmd(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            if (executor instanceof org.bukkit.command.TabCompleter tc) {
                cmd.setTabCompleter(tc);
            }
        } else {
            getLogger().log(Level.WARNING, "Command '" + name + "' not found in plugin.yml!");
        }
    }

    public void reload() {
        configManager.loadAll();
        if (shopFeature != null) shopFeature.reload();
        if (hideFeature != null) hideFeature.reload();
        if (unknownCommandFeature != null) unknownCommandFeature.reload();
    }

    // --- Getters ---
    public static PUtility getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public EconomyHandler getEconomyHandler() { return economyHandler; }
    public PlayerPointsHandler getPlayerPointsHandler() { return playerPointsHandler; }
    public ShopFeature getShopFeature() { return shopFeature; }
    public HideFeature getHideFeature() { return hideFeature; }
    public UnknownCommandFeature getUnknownCommandFeature() { return unknownCommandFeature; }
}
