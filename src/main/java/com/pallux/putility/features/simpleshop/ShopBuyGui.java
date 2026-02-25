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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class ShopBuyGui extends AbstractGui {

    private final PUtility plugin;
    private final Player player;
    private final ShopData shopData;
    private final ShopCategory category;
    private final ShopItem shopItem;

    private static final int[] AMOUNTS = {1, 16, 32, 64};

    public ShopBuyGui(PUtility plugin, Player player, ShopData shopData, ShopCategory category, ShopItem shopItem) {
        super(9, MessageUtils.parse(
                plugin.getConfigManager().get("simpleshop")
                        .getString("gui.buy.title", "&8Buy &7{item_name}")
                        .replace("{item_name}", shopItem.getName()),
                player));
        this.plugin = plugin;
        this.player = player;
        this.shopData = shopData;
        this.category = category;
        this.shopItem = shopItem;
    }

    @Override
    protected void build() {
        FileConfiguration cfg = plugin.getConfigManager().get("simpleshop");

        Material filler = parseMaterial(cfg.getString("gui.buy.filler.material", "BLACK_STAINED_GLASS_PANE"));
        ItemStack fillerItem = ItemBuilder.build(filler, MessageUtils.parse(" "));
        for (int i = 0; i < 9; i++) inventory.setItem(i, fillerItem);

        int backSlot = cfg.getInt("gui.buy.back-button.slot", 0);
        inventory.setItem(backSlot, ItemBuilder.buildFromConfig(
                cfg.getString("gui.buy.back-button.material", "ARROW"),
                cfg.getString("gui.buy.back-button.name", "&cBack"),
                cfg.getStringList("gui.buy.back-button.lore"),
                player, Map.of()
        ));

        String currencySymbol = getCurrencySymbol();
        String[] buyKeys = {"buy-1", "buy-16", "buy-32", "buy-64"};
        int[] defaultSlots = {3, 4, 5, 6};

        for (int i = 0; i < 4; i++) {
            int amount = AMOUNTS[i];
            double totalPrice = shopItem.getPriceFor(amount);
            String key = "gui.buy." + buyKeys[i];
            int slot = cfg.getInt(key + ".slot", defaultSlots[i]);
            String priceStr = formatPrice(totalPrice);

            String name = cfg.getString(key + ".name", "&aBuy &f" + amount + "x &7{item_name}")
                    .replace("{item_name}", shopItem.getName())
                    .replace("{price_" + amount + "}", priceStr)
                    .replace("{currency}", currencySymbol);

            List<String> processedLore = cfg.getStringList(key + ".lore").stream()
                    .map(l -> l
                            .replace("{item_name}", shopItem.getName())
                            .replace("{price_" + amount + "}", priceStr)
                            .replace("{currency}", currencySymbol))
                    .toList();

            // Build as LIME_STAINED_GLASS_PANE with the configured name & lore
            ItemStack buyItem = ItemBuilder.buildFromConfig(
                    "LIME_STAINED_GLASS_PANE", name, processedLore, player, Map.of()
            );
            inventory.setItem(slot, buyItem);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        FileConfiguration cfg = plugin.getConfigManager().get("simpleshop");

        int backSlot = cfg.getInt("gui.buy.back-button.slot", 0);
        int clickedSlot = event.getSlot();

        if (clickedSlot == backSlot) {
            new ShopCategoryGui(plugin, clicker, shopData, category).open(clicker);
            return;
        }

        String[] buyKeys = {"buy-1", "buy-16", "buy-32", "buy-64"};
        int[] defaultSlots = {3, 4, 5, 6};

        for (int i = 0; i < 4; i++) {
            int slot = cfg.getInt("gui.buy." + buyKeys[i] + ".slot", defaultSlots[i]);
            if (clickedSlot == slot) {
                processPurchase(clicker, AMOUNTS[i]);
                return;
            }
        }
    }

    private void processPurchase(Player buyer, int amount) {
        FileConfiguration cfg = plugin.getConfigManager().get("simpleshop");
        String prefix = plugin.getConfigManager().get("messages").getString("prefix", "&7&lUTILITY &7➠ ");

        double totalPrice = shopItem.getPriceFor(amount);
        String priceStr = formatPrice(totalPrice);

        if (shopItem.getCurrencyType() == ShopItem.CurrencyType.PLAYER_POINTS) {
            PlayerPointsHandler pp = plugin.getPlayerPointsHandler();
            if (!pp.isAvailable()) {
                buyer.sendMessage(MessageUtils.parse(prefix + "&cPlayerPoints is not installed or enabled.", buyer));
                return;
            }
            int pointsCost = (int) Math.ceil(totalPrice);
            if (!pp.has(buyer, pointsCost)) {
                String msg = cfg.getString("messages.purchase-failed-funds-points",
                                "&cYou don't have enough Points! You need &f{price} Points&c.")
                        .replace("{price}", String.valueOf(pointsCost));
                buyer.sendMessage(MessageUtils.parse(prefix + msg, buyer));
                return;
            }
            if (!shopItem.hasCommands() || shopItem.getMaterial() != Material.AIR) {
                ItemStack toGive = new ItemStack(shopItem.getMaterial(), amount);
                if (!hasSpace(buyer, toGive)) {
                    buyer.sendMessage(MessageUtils.parse(prefix +
                            cfg.getString("messages.purchase-failed-inventory", "&cYour inventory is full!"), buyer));
                    return;
                }
                buyer.getInventory().addItem(toGive);
            }
            pp.withdraw(buyer, pointsCost);
        } else {
            EconomyHandler eco = plugin.getEconomyHandler();
            if (!eco.isAvailable()) {
                buyer.sendMessage(MessageUtils.parse(prefix + "&cEconomy is not available.", buyer));
                return;
            }
            if (!eco.has(buyer, totalPrice)) {
                String msg = cfg.getString("messages.purchase-failed-funds",
                                "&cYou don't have enough money! You need &f{price}&c.")
                        .replace("{price}", priceStr);
                buyer.sendMessage(MessageUtils.parse(prefix + msg, buyer));
                return;
            }
            if (!shopItem.hasCommands() || shopItem.getMaterial() != Material.AIR) {
                ItemStack toGive = new ItemStack(shopItem.getMaterial(), amount);
                if (!hasSpace(buyer, toGive)) {
                    buyer.sendMessage(MessageUtils.parse(prefix +
                            cfg.getString("messages.purchase-failed-inventory", "&cYour inventory is full!"), buyer));
                    return;
                }
                buyer.getInventory().addItem(toGive);
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

        String msg = cfg.getString("messages.purchase-success",
                        "&aYou purchased &f{amount}x {item} &afor &f{price}&a!")
                .replace("{amount}", String.valueOf(amount))
                .replace("{item}", shopItem.getName())
                .replace("{price}", priceStr);
        buyer.sendMessage(MessageUtils.parse(prefix + msg, buyer));
        buyer.closeInventory();
    }

    private boolean hasSpace(Player player, ItemStack item) {
        return player.getInventory().firstEmpty() != -1
                || player.getInventory().containsAtLeast(item, 1);
    }

    private String formatPrice(double price) {
        if (shopItem.getCurrencyType() == ShopItem.CurrencyType.PLAYER_POINTS) {
            return (int) Math.ceil(price) + " Points";
        }
        EconomyHandler eco = plugin.getEconomyHandler();
        if (eco.isAvailable()) return eco.format(price);
        return String.format("$%.2f", price);
    }

    private String getCurrencySymbol() {
        return shopItem.getCurrencyType() == ShopItem.CurrencyType.PLAYER_POINTS ? "Points" : "Money";
    }

    private Material parseMaterial(String name) {
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return Material.BLACK_STAINED_GLASS_PANE; }
    }
}