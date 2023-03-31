package net.okocraft.scoreboard.util;

import io.papermc.paper.threadedregions.TickRegionScheduler;
import net.okocraft.scoreboard.ScoreboardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class PlatformHelper {

    private static final boolean FOLIA;

    static {
        boolean isFolia;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }

        FOLIA = isFolia;
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void runAsync(@NotNull Runnable runnable) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runNow(ScoreboardPlugin.getPlugin(), $ -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(ScoreboardPlugin.getPlugin(), $ -> runnable.run());
        }
    }

    public static <T> @NotNull CompletableFuture<T> runOnPlayerScheduler(@NotNull Player player, @NotNull Supplier<T> supplier) {
        if (FOLIA) {
            if (Bukkit.isOwnedByCurrentRegion(player)) {
                return CompletableFuture.completedFuture(supplier.get());
            } else {
                return CompletableFuture.supplyAsync(supplier, createExecutorFromPlayerScheduler(player));
            }
        } else {
            if (Bukkit.isPrimaryThread()) {
                return CompletableFuture.completedFuture(supplier.get());
            } else {
                return CompletableFuture.supplyAsync(supplier, Bukkit.getScheduler().getMainThreadExecutor(ScoreboardPlugin.getPlugin()));
            }
        }
    }

    public static double getRegionTPS(@NotNull Location location) {
        if (FOLIA) {
            if (Bukkit.isOwnedByCurrentRegion(location)) {
                return getCurrentRegionTPS();
            } else {
                return CompletableFuture.supplyAsync(PlatformHelper::getCurrentRegionTPS, createExecutorFromRegionScheduler(location)).join();
            }
        } else {
            return Bukkit.getTPS()[0];
        }
    }

    private static double getCurrentRegionTPS() {
        return TickRegionScheduler.getCurrentRegion()
                .getData().getRegionSchedulingHandle()
                .getTickReport15s(System.nanoTime())
                .tpsData().segmentAll().average();
    }

    private static @NotNull Executor createExecutorFromPlayerScheduler(@NotNull Player player) {
        return command -> player.getScheduler().run(ScoreboardPlugin.getPlugin(), $ -> command.run(), null);
    }

    private static @NotNull Executor createExecutorFromRegionScheduler(@NotNull Location location) {
        return command -> Bukkit.getRegionScheduler().run(ScoreboardPlugin.getPlugin(), location, $ -> command.run());
    }
}
