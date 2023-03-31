package net.okocraft.scoreboard.external;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PlaceholderAPIHooker {

    private static boolean enabled;

    private PlaceholderAPIHooker() {
        throw new UnsupportedOperationException();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        PlaceholderAPIHooker.enabled = enabled;
    }

    public static boolean checkEnabled(@NotNull Server server) {
        enabled = server.getPluginManager().getPlugin("PlaceholderAPI") != null;
        return enabled;
    }

    @NotNull
    public static String run(@NotNull Player player, @NotNull String str) {
        if (enabled && !str.isEmpty()) {
            try {
                return PlaceholderAPI.setPlaceholders(player, str);
            } catch (Throwable ignored) {
                return "";
            }
        } else {
            return str;
        }
    }
}
