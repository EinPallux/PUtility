package com.pallux.putility.economy;

import com.pallux.putility.PUtility;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHandler {

    private final PUtility plugin;
    private Economy economy;

    public EconomyHandler(PUtility plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public double getBalance(Player player) {
        if (!isAvailable()) return 0;
        return economy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        if (!isAvailable()) return false;
        return economy.has(player, amount);
    }

    /**
     * Withdraw from player. Returns true if successful.
     */
    public boolean withdraw(Player player, double amount) {
        if (!isAvailable()) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        if (!isAvailable()) return "$" + String.format("%.2f", amount);
        return economy.format(amount);
    }
}
