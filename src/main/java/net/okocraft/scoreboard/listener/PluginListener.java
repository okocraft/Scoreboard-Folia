package net.okocraft.scoreboard.listener;

import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.external.PlaceholderAPIHooker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;

public class PluginListener implements Listener {

    private final ScoreboardPlugin plugin;

    public PluginListener(@NotNull ScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnable(@NotNull PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("PlaceholderAPI")) {
            PlaceholderAPIHooker.setEnabled(true);
            this.plugin.printPlaceholderIsAvailable();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisable(@NotNull PluginDisableEvent event) {
        if (event.getPlugin().getName().equals("PlaceholderAPI")) {
            PlaceholderAPIHooker.setEnabled(false);
        }
    }
}
