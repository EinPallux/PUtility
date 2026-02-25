package com.pallux.putility.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class AbstractGui implements InventoryHolder {

    protected final Inventory inventory;

    public AbstractGui(int size, Component title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public abstract void handleClick(InventoryClickEvent event);

    public void open(Player player) {
        build();
        player.openInventory(inventory);
    }

    protected abstract void build();

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
