package com.pallux.putility.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public final class ItemBuilder {

    private ItemBuilder() {}

    public static ItemStack build(Material material, Component name) {
        return build(material, name, null);
    }

    public static ItemStack build(Material material, Component name, List<Component> lore) {
        return build(material, 1, name, lore);
    }

    public static ItemStack build(Material material, int amount, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.displayName(name);
        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack buildFromConfig(String materialStr, String nameStr, List<String> loreStr, Player player, Map<String, String> placeholders) {
        Material material;
        try {
            material = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.PAPER;
        }
        Component name = MessageUtils.parse(nameStr, player, placeholders);
        List<Component> lore = loreStr != null
                ? MessageUtils.parseList(loreStr, player, placeholders)
                : null;
        return build(material, name, lore);
    }
}
