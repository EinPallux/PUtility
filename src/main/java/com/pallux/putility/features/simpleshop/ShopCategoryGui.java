package com.pallux.putility.features.simpleshop;

import com.pallux.putility.PUtility;
import com.pallux.putility.gui.AbstractGui;
import com.pallux.putility.utils.ItemBuilder;
import com.pallux.putility.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class ShopCategoryGui extends AbstractGui {

    private final PUtility plugin;
    private final Player player;
    private final ShopData shopData;
    private final ShopCategory category;

    private static final int[] ITEM_SLOTS = {10, 11, 12, 13, 14, 15, 16};

    public ShopCategoryGui(PUtility plugin, Player player, ShopData shopData, ShopCategory category) {
        super(27, MessageUtils.parse(
                plugin.getConfigManager().get("simpleshop")
                        .getString("gui.category.title", "&8{category_name}")
                        .replace("{category_name}", category.getName()),
                player));
        this.plugin = plugin;
        this.player = player;
        this.shopData = shopData;
        this.category = category;
    }

    @Override
    protected void build() {
        FileConfiguration cfg = plugin.getConfigManager().get("simpleshop");

        Material filler = parseMaterial(cfg.getString("gui.category.filler.material", "BLACK_STAINED_GLASS_PANE"));
        ItemStack fillerItem = ItemBuilder.build(filler, MessageUtils.parse(cfg.getString("gui.category.filler.name", " ")));
        for (int i = 0; i < 27; i++) inventory.setItem(i, fillerItem);

        int backSlot = cfg.getInt("gui.category.back-button.slot", 22);
        inventory.setItem(backSlot, ItemBuilder.buildFromConfig(
                cfg.getString("gui.category.back-button.material", "ARROW"),
                cfg.getString("gui.category.back-button.name", "&cBack"),
                cfg.getStringList("gui.category.back-button.lore"),
                player, Map.of()
        ));

        int slotIdx = 0;
        for (ShopItem item : category.getItems().values()) {
            if (slotIdx >= ITEM_SLOTS.length) break;
            int targetSlot = ITEM_SLOTS[slotIdx];

            // Skip the back button slot if it overlaps
            while (slotIdx < ITEM_SLOTS.length && ITEM_SLOTS[slotIdx] == backSlot) {
                slotIdx++;
            }
            if (slotIdx >= ITEM_SLOTS.length) break;
            targetSlot = ITEM_SLOTS[slotIdx];

            ItemStack display = ItemBuilder.buildFromConfig(
                    item.getMaterial().name(),
                    item.getName(),
                    item.getLore().isEmpty() ? List.of("&7Price: &a$" + item.getPrice()) : item.getLore(),
                    player,
                    Map.of()
            );
            inventory.setItem(targetSlot, display);
            slotIdx++;
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        FileConfiguration cfg = plugin.getConfigManager().get("simpleshop");

        int backSlot = cfg.getInt("gui.category.back-button.slot", 22);
        int clickedSlot = event.getSlot();

        if (clickedSlot == backSlot) {
            new ShopMainGui(plugin, clicker, shopData).open(clicker);
            return;
        }

        Material filler = parseMaterial(cfg.getString("gui.category.filler.material", "BLACK_STAINED_GLASS_PANE"));
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == filler) return;

        // Find which index in ITEM_SLOTS was clicked
        int itemIndex = -1;
        int validIdx = 0;
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            if (ITEM_SLOTS[i] == backSlot) continue;
            if (ITEM_SLOTS[i] == clickedSlot) {
                itemIndex = validIdx;
                break;
            }
            validIdx++;
        }

        if (itemIndex < 0) return;

        ShopItem[] items = category.getItems().values().toArray(new ShopItem[0]);
        if (itemIndex >= items.length) return;

        new ShopBuyGui(plugin, clicker, shopData, category, items[itemIndex]).open(clicker);
    }

    private Material parseMaterial(String name) {
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.BLACK_STAINED_GLASS_PANE; }
    }
}