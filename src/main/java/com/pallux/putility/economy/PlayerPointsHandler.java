package com.pallux.putility.economy;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlayerPointsHandler {

    private PlayerPointsAPI api;

    public boolean setup() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (plugin instanceof PlayerPoints pp) {
            api = pp.getAPI();
            return true;
        }
        return false;
    }

    public boolean isAvailable() {
        return api != null;
    }

    public int getBalance(Player player) {
        if (!isAvailable()) return 0;
        return api.look(player.getUniqueId());
    }

    public boolean has(Player player, int amount) {
        if (!isAvailable()) return false;
        return api.look(player.getUniqueId()) >= amount;
    }

    public boolean withdraw(Player player, int amount) {
        if (!isAvailable()) return false;
        return api.take(player.getUniqueId(), amount);
    }

    public String format(int amount) {
        return amount + " Points";
    }
}
