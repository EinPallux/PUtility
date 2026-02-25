package com.pallux.putility.features.simpleshop;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShopCategory {

    private String id;
    private String name;
    private List<String> lore;
    private Material icon;
    private int slot;
    private Map<Integer, ShopItem> items;

    public ShopCategory(String id, String name, List<String> lore, Material icon, int slot) {
        this.id = id;
        this.name = name;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.icon = icon;
        this.slot = slot;
        this.items = new LinkedHashMap<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public Material getIcon() { return icon; }
    public void setIcon(Material icon) { this.icon = icon; }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }
    public Map<Integer, ShopItem> getItems() { return items; }

    public void addItem(ShopItem item) {
        int nextSlot = 0;
        while (items.containsKey(nextSlot)) nextSlot++;
        item.setSlot(nextSlot);
        items.put(nextSlot, item);
    }

    public void removeItem(int slot) {
        items.remove(slot);
    }
}
