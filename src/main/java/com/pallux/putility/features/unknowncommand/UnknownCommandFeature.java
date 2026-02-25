package com.pallux.putility.features.unknowncommand;

import com.pallux.putility.PUtility;
import com.pallux.putility.features.Feature;
import org.bukkit.event.HandlerList;

public class UnknownCommandFeature extends Feature {

    private UnknownCommandListener listener;

    public UnknownCommandFeature(PUtility plugin) {
        super(plugin);
    }

    @Override
    protected void onEnable() {
        listener = new UnknownCommandListener(plugin);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        plugin.getLogger().info("UnknownCommand feature enabled.");
    }

    @Override
    protected void onDisable() {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
        plugin.getLogger().info("UnknownCommand feature disabled.");
    }

    @Override
    protected void onReload() {}
}
