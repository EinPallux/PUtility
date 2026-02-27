package com.pallux.putility.features.simpleshop;

import com.pallux.putility.PUtility;
import com.pallux.putility.economy.EconomyHandler;
import com.pallux.putility.economy.PlayerPointsHandler;
import com.pallux.putility.gui.AbstractGui;
import com.pallux.putility.utils.ItemBuilder;
import com.pallux.putility.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopCategoryGui extends AbstractGui {

    private final PUtility plugin;
    private final Player player;
    private final ShopData shopData;
    private final ShopCategory category;

    // Maps inventory slot → ShopItem for instant-buy categories
    private final Map<Integer, ShopItem> slotToItemMap = new HashMap<>();

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
        slotToItemMap.clear();

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

            // Skip the back button slot if it overlaps
            while (slotIdx < ITEM_SLOTS.length && ITEM_SLOTS[slotIdx] == backSlot) {
                slotIdx++;
            }
            if (slotIdx >= ITEM_SLOTS.length) break;

            int targetSlot = ITEM_SLOTS[slotIdx];

            ItemStack display = ItemBuilder.buildFromConfig(
                    item.getMaterial().name(),
                    item.getName(),
                    item.getLore().isEmpty() ? List.of("&7Price: &a$" + item.getPrice()) : item.getLore(),
                    player,
                    Map.of()
            );
            inventory.setItem(targetSlot, display);
            slotToItemMap.put(targetSlot, item);
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

        ShopItem item = slotToItemMap.get(clickedSlot);
        if (item == null) return;

        if (category.isInstantBuy()) {
            processInstantPurchase(clicker, item);
        } else {
            new ShopBuyGui(plugin, clicker, shopData, category, item).open(clicker);
        }
    }

    private void processInstantPurchase(Player buyer, ShopItem shopItem) {
        FileConfiguration msgs = plugin.getConfigManager().get("messages");
        String prefix = msgs.getString("prefix", "&7&lUTILITY &7➠ ");
        int amount = 1;
        double totalPrice = shopItem.getPriceFor(amount);

        if (shopItem.getCurrencyType() == ShopItem.CurrencyType.PLAYER_POINTS) {
            PlayerPointsHandler pp = plugin.getPlayerPointsHandler();
            if (!pp.isAvailable()) {
                buyer.sendMessage(MessageUtils.parse(prefix + "&cPlayerPoints is not installed or enabled.", buyer));
                return;
            }
            int pointsCost = (int) Math.ceil(totalPrice);
            if (!pp.has(buyer, pointsCost)) {
                String msg = msgs.getString("purchase-failed-funds-points",
                                "&cYou don't have enough Points! You need &f{price} Points&c.")
                        .replace("{price}", String.valueOf(pointsCost));
                buyer.sendMessage(MessageUtils.parse(prefix + msg, buyer));
                return;
            }
            pp.withdraw(buyer, pointsCost);
        } else {
            EconomyHandler eco = plugin.getEconomyHandler();
            if (!eco.isAvailable()) {
                buyer.sendMessage(MessageUtils.parse(prefix + "&cEconomy is not available.", buyer));
                return;
            }
            if (!eco.has(buyer, totalPrice)) {
                String msg = msgs.getString("purchase-failed-funds",
                                "&cYou don't have enough money! You need &f{price}&c.")
                        .replace("{price}", eco.format(totalPrice));
                buyer.sendMessage(MessageUtils.parse(prefix + msg, buyer));
                return;
            }
            eco.withdraw(buyer, totalPrice);
        }

        if (shopItem.hasCommands()) {
            for (String cmd : shopItem.getCommands()) {
                String parsed = cmd
                        .replace("%player%", buyer.getName())
                        .replace("%amount%", String.valueOf(amount));
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
            }
        }

        String priceStr = shopItem.getCurrencyType() == ShopItem.CurrencyType.PLAYER_POINTS
                ? (int) Math.ceil(totalPrice) + " Points"
                : plugin.getEconomyHandler().isAvailable()
                ? plugin.getEconomyHandler().format(totalPrice)
                : String.format("$%.2f", totalPrice);

        String msg = msgs.getString("purchase-success",
                        "&aYou purchased &f{amount}x {item} &afor &f{price}&a!")
                .replace("{amount}", String.valueOf(amount))
                .replace("{item}", shopItem.getName())
                .replace("{price}", priceStr);
        buyer.sendMessage(MessageUtils.parse(prefix + msg, buyer));
        buyer.closeInventory();
    }

    private Material parseMaterial(String name) {
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.BLACK_STAINED_GLASS_PANE; }
    }
}