package com.pallux.putility;

import com.pallux.putility.commands.PUtilityCommand;
import com.pallux.putility.commands.ShopCommand;
import com.pallux.putility.config.ConfigManager;
import com.pallux.putility.economy.EconomyHandler;
import com.pallux.putility.economy.PlayerPointsHandler;
import com.pallux.putility.features.premiumshards.PremiumShardsFeature;
import com.pallux.putility.features.simpleshop.ShopFeature;
import com.pallux.putility.gui.GuiListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PUtility extends JavaPlugin {

    private static PUtility instance;

    private ConfigManager configManager;
    private EconomyHandler economyHandler;
    private PlayerPointsHandler playerPointsHandler;
    private ShopFeature shopFeature;
    private PremiumShardsFeature premiumShardsFeature;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.loadAll();

        economyHandler = new EconomyHandler(this);
        if (!economyHandler.setup()) {
            getLogger().warning("Vault economy not found! Vault-priced shop items will not work.");
        }

        playerPointsHandler = new PlayerPointsHandler();
        if (playerPointsHandler.setup()) {
            getLogger().info("PlayerPoints found and hooked successfully.");
        } else {
            getLogger().info("PlayerPoints not found. PlayerPoints-priced shop items will not work.");
        }

        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        initFeatures();
        registerCommands();

        getLogger().info("PUtility enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (shopFeature != null) shopFeature.disable();
        if (premiumShardsFeature != null) premiumShardsFeature.disable();
        getLogger().info("PUtility disabled.");
    }

    private void initFeatures() {
        shopFeature = new ShopFeature(this);
        if (getConfig().getBoolean("features.simpleshop", true)) {
            shopFeature.enable();
        }

        premiumShardsFeature = new PremiumShardsFeature(this);
        if (getConfig().getBoolean("features.premiumshards", true)) {
            premiumShardsFeature.enable();
        }
    }

    private void registerCommands() {
        registerCmd("shop", new ShopCommand(this));
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
        if (premiumShardsFeature != null) premiumShardsFeature.reload();
    }

    public static PUtility getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public EconomyHandler getEconomyHandler() { return economyHandler; }
    public PlayerPointsHandler getPlayerPointsHandler() { return playerPointsHandler; }
    public ShopFeature getShopFeature() { return shopFeature; }
    public PremiumShardsFeature getPremiumShardsFeature() { return premiumShardsFeature; }
}