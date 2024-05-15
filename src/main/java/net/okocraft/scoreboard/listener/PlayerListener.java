package net.okocraft.scoreboard.listener;

import net.okocraft.scoreboard.ScoreboardPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

    private final ScoreboardPlugin plugin;

    public PlayerListener(@NotNull ScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();

        if (player.hasPermission("scoreboard.show-on-join")) {
            this.plugin.getServer().getAsyncScheduler().runNow(this.plugin, ignored -> this.plugin.getDisplayManager().showDefaultBoard(event.getPlayer()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        this.plugin.getDisplayManager().hideBoard(event.getPlayer());
    }
}
