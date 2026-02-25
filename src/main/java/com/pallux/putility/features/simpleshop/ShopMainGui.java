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

public class ShopMainGui extends AbstractGui {

    private final PUtility plugin;
    private final Player player;
    private final ShopData shopData;

    public ShopMainGui(PUtility plugin, Player player, ShopData shopData) {
        super(27, MessageUtils.parse(plugin.getConfigManager().get("simpleshop").getString("gui.main.title", "&8Shop"), player));
        this.plugin = plugin;
        this.player = player;
        this.shopData = shopData;
    }

    @Override
    protected void build() {
        FileConfiguration cfg = plugin.getConfigManager().get("simpleshop");

        String fillerMat = cfg.getString("gui.main.filler.material", "BLACK_STAINED_GLASS_PANE");
        String fillerName = cfg.getString("gui.main.filler.name", " ");
        Material filler = parseMaterial(fillerMat);
        ItemStack fillerItem = ItemBuilder.build(filler, MessageUtils.parse(fillerName));

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, fillerItem);
        }

        List<Integer> categorySlots = cfg.getIntegerList("gui.main.category-slots");
        if (categorySlots.isEmpty()) {
            for (int i = 10; i <= 16; i++) categorySlots.add(i);
        }

        int slotIndex = 0;
        for (ShopCategory category : shopData.getCategories().values()) {
            if (slotIndex >= categorySlots.size()) break;
            int slot = categorySlots.get(slotIndex++);

            ItemStack catItem = ItemBuilder.buildFromConfig(
                    category.getIcon().name(),
                    category.getName(),
                    category.getLore(),
                    player,
                    Map.of("category_name", category.getName())
            );
            inventory.setItem(slot, catItem);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        FileConfiguration cfg = plugin.getConfigManager().get("simpleshop");
        String fillerMat = cfg.getString("gui.main.filler.material", "BLACK_STAINED_GLASS_PANE");
        if (clicked.getType() == parseMaterial(fillerMat)) return;

        int clickedSlot = event.getSlot();
        List<Integer> categorySlots = cfg.getIntegerList("gui.main.category-slots");
        if (categorySlots.isEmpty()) {
            for (int i = 10; i <= 16; i++) categorySlots.add(i);
        }

        int slotIndex = categorySlots.indexOf(clickedSlot);
        if (slotIndex < 0) return;

        ShopCategory[] cats = shopData.getCategories().values().toArray(new ShopCategory[0]);
        if (slotIndex >= cats.length) return;

        new ShopCategoryGui(plugin, clicker, shopData, cats[slotIndex]).open(clicker);
    }

    private Material parseMaterial(String name) {
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.BLACK_STAINED_GLASS_PANE; }
    }
}
