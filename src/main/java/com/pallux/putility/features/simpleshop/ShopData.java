package com.pallux.putility.features.simpleshop;

import com.pallux.putility.PUtility;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShopData {

    private final PUtility plugin;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();

    public ShopData(PUtility plugin) {
        this.plugin = plugin;
    }

    public void load() {
        categories.clear();
        FileConfiguration cfg = plugin.getConfigManager().get("simpleshop");
        ConfigurationSection catSection = cfg.getConfigurationSection("categories");
        if (catSection == null) return;

        for (String catId : catSection.getKeys(false)) {
            ConfigurationSection cat = catSection.getConfigurationSection(catId);
            if (cat == null) continue;

            String name = cat.getString("name", catId);
            List<String> lore = cat.getStringList("lore");
            Material icon = parseMaterial(cat.getString("icon", "CHEST"));
            int slot = cat.getInt("slot", 10);

            ShopCategory category = new ShopCategory(catId, name, lore, icon, slot);

            ConfigurationSection itemsSection = cat.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String itemKey : itemsSection.getKeys(false)) {
                    ConfigurationSection itemCfg = itemsSection.getConfigurationSection(itemKey);
                    if (itemCfg == null) continue;

                    Material mat = parseMaterial(itemCfg.getString("material", "PAPER"));
                    String itemName = itemCfg.getString("name", mat.name());
                    List<String> itemLore = itemCfg.getStringList("lore");
                    double price = itemCfg.getDouble("price", 10.0);
                    int itemSlot = itemCfg.getInt("slot", 0);

                    // Currency type
                    String currencyStr = itemCfg.getString("currency", "VAULT").toUpperCase();
                    ShopItem.CurrencyType currency;
                    try {
                        currency = ShopItem.CurrencyType.valueOf(currencyStr);
                    } catch (IllegalArgumentException e) {
                        currency = ShopItem.CurrencyType.VAULT;
                    }

                    // Console commands
                    List<String> commands = itemCfg.getStringList("commands");

                    ShopItem shopItem = new ShopItem(mat, itemName, itemLore, price, currency, commands, itemSlot);
                    category.getItems().put(itemSlot, shopItem);
                }
            }

            categories.put(catId, category);
        }
    }

    public Map<String, ShopCategory> getCategories() { return categories; }
    public ShopCategory getCategory(String id) { return categories.get(id); }

    public void addCategory(ShopCategory category) {
        categories.put(category.getId(), category);
    }

    public void removeCategory(String id) {
        categories.remove(id);
    }

    private Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.PAPER;
        }
    }
}
