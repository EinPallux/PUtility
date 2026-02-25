package com.pallux.putility.features.simpleshop;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.Feature;

public class ShopFeature extends Feature {

    private ShopData shopData;

    public ShopFeature(PUtility plugin) {
        super(plugin);
    }

    @Override
    protected void onEnable() {
        shopData = new ShopData(plugin);
        shopData.load();
        plugin.getLogger().info("SimpleShop feature enabled.");
    }

    @Override
    protected void onDisable() {
        plugin.getLogger().info("SimpleShop feature disabled.");
    }

    @Override
    protected void onReload() {
        if (shopData != null) shopData.load();
    }

    public ShopData getShopData() { return shopData; }
}
