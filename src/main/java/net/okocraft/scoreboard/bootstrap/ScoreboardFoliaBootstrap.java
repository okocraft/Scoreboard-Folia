package net.okocraft.scoreboard.bootstrap;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.display.placeholder.Placeholder;
import net.okocraft.scoreboard.display.placeholder.PlaceholderProvider;
import net.okocraft.scoreboard.folia.display.PacketBasedBoardDisplay;
import net.okocraft.scoreboard.folia.placeholder.FoliaPlaceholders;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class ScoreboardFoliaBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext bootstrapContext) {
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        var plugin = new ScoreboardPlugin(PacketBasedBoardDisplay::new);
        this.registerPlaceholders(plugin.getPlaceholderProvider());
        return plugin;
    }

    private void registerPlaceholders(@NotNull PlaceholderProvider provider) {
        Placeholder.registerDefaults(provider);

        if (isFolia()) {
            FoliaPlaceholders.registerDefaults(provider);
        }
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
