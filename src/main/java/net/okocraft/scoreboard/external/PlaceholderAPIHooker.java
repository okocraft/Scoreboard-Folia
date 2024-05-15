package net.okocraft.scoreboard.external;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.display.placeholder.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class PlaceholderAPIHooker {

    private static boolean enabled;

    private PlaceholderAPIHooker() {
        throw new UnsupportedOperationException();
    }

    public static void setEnabled(boolean enabled) {
        PlaceholderAPIHooker.enabled = enabled;
    }

    public static boolean checkEnabled(@NotNull Server server) {
        var plugin = server.getPluginManager().getPlugin("PlaceholderAPI");
        enabled = plugin != null && plugin.isEnabled();
        return enabled;
    }

    public static Placeholder createPlaceholder(String placeholder) {
        var cachedPlaceholder = '%' + placeholder + '%';
        return context -> Component.text(
                enabled ?
                        renderPlaceholder(context, cachedPlaceholder) :
                        cachedPlaceholder
        );
    }

    private static String renderPlaceholder(@NotNull Placeholder.Context context, @NotNull String placeholder) {
        if (Bukkit.isOwnedByCurrentRegion(context.viewer())) {
            return renderPlaceholder0(context, placeholder);
        } else {
            var future = new CompletableFuture<String>();
            context.viewer().getScheduler().run(JavaPlugin.getPlugin(ScoreboardPlugin.class), ignored -> future.complete(renderPlaceholder0(context, placeholder)), () -> future.complete(""));
            return future.join();
        }
    }

    private static @NotNull String renderPlaceholder0(Placeholder.Context context, String placeholder) {
        return PlaceholderAPI.setPlaceholders(context.viewer().getPlayer(), placeholder);
    }
}
