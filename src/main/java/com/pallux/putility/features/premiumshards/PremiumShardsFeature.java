package com.pallux.putility.features.premiumshards;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.Feature;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class PremiumShardsFeature extends Feature {

    private BukkitTask task;

    public PremiumShardsFeature(PUtility plugin) {
        super(plugin);
    }

    @Override
    protected void onEnable() {
        startTask();
        plugin.getLogger().info("PremiumShards feature enabled.");
    }

    @Override
    protected void onDisable() {
        stopTask();
        plugin.getLogger().info("PremiumShards feature disabled.");
    }

    @Override
    protected void onReload() {
        stopTask();
        startTask();
    }

    private void startTask() {
        FileConfiguration cfg = plugin.getConfigManager().get("premiumshards");
        long intervalTicks = cfg.getLong("interval-seconds", 60L) * 20L;

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            FileConfiguration config = plugin.getConfigManager().get("premiumshards");
            String permission = config.getString("permission", "pu.shards");
            String command = config.getString("command", "shards give %player% 1");

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(permission)) {
                    String parsed = command.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
                }
            }
        }, intervalTicks, intervalTicks);
    }

    private void stopTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }
}