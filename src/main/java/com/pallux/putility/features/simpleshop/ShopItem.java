package com.pallux.putility.features.simpleshop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShopItem {

    public enum CurrencyType {
        VAULT,
        PLAYER_POINTS
    }

    private Material material;
    private String name;
    private List<String> lore;
    private double price;
    private CurrencyType currencyType;
    private int slot;
    private List<String> commands;

    public ShopItem(Material material, String name, List<String> lore,
                    double price, CurrencyType currencyType, List<String> commands, int slot) {
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.price = price;
        this.currencyType = currencyType != null ? currencyType : CurrencyType.VAULT;
        this.commands = commands != null ? commands : List.of();
        this.slot = slot;
    }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public CurrencyType getCurrencyType() { return currencyType; }
    public void setCurrencyType(CurrencyType currencyType) { this.currencyType = currencyType; }
    public List<String> getCommands() { return commands; }
    public void setCommands(List<String> commands) { this.commands = commands; }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    public double getPriceFor(int amount) {
        return price * amount;
    }

    public boolean hasCommands() {
        return commands != null && !commands.isEmpty();
    }

    public ItemStack createItemStack(int amount) {
        return new ItemStack(material, amount);
    }
}
