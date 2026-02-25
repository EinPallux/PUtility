package com.pallux.putility.features;

import com.pallux.putility.PUtility;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Feature implements Listener {

    protected final PUtility plugin;
    private boolean enabled = false;

    public Feature(PUtility plugin) {
        this.plugin = plugin;
    }

    public final void enable() {
        if (enabled) return;
        enabled = true;
        onEnable();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public final void disable() {
        if (!enabled) return;
        enabled = false;
        onDisable();
        HandlerList.unregisterAll(this);
    }

    public final void reload() {
        onReload();
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected abstract void onEnable();
    protected abstract void onDisable();
    protected abstract void onReload();
}
