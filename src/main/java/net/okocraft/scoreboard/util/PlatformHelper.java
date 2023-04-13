package net.okocraft.scoreboard.util;

import io.papermc.paper.chunk.system.scheduling.ChunkFullTask;
import io.papermc.paper.threadedregions.ThreadedRegionizer;
import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegionScheduler;
import io.papermc.paper.threadedregions.TickRegions;
import net.okocraft.scoreboard.ScoreboardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
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
            return PlatformHelper.getFromRegionReport(location, report -> report.tpsData().segmentAll().average());
        } else {
            return Bukkit.getTPS()[0];
        }
    }

    public static <T> T getFromRegionReport(@NotNull Location location, @NotNull Function<TickData.TickReportData, T> function) {
        if (Bukkit.isOwnedByCurrentRegion(location)) {
            return function.apply(getRegionData().getRegionSchedulingHandle().getTickReport5s(System.nanoTime()));
        } else {
            return CompletableFuture.supplyAsync(
                    () -> function.apply(getRegionData().getRegionSchedulingHandle().getTickReport5s(System.nanoTime())),
                    createExecutorFromRegionScheduler(location)
            ).join();
        }
    }

    public static <T> T getFromRegionStats(@NotNull Location location, @NotNull Function<TickRegions.RegionStats, T> function) {
        if (Bukkit.isOwnedByCurrentRegion(location)) {
            return function.apply(getRegionData().getRegionStats());
        } else {
            return CompletableFuture.supplyAsync(
                    () -> function.apply(getRegionData().getRegionStats()),
                    createExecutorFromRegionScheduler(location)
            ).join();
        }
    }

    public static List<ThreadedRegionizer.ThreadedRegion<TickRegions.TickRegionData, TickRegions.TickRegionSectionData>> getAllRegions() {
        List<ThreadedRegionizer.ThreadedRegion<TickRegions.TickRegionData, TickRegions.TickRegionSectionData>> regions = new ArrayList<>();

        for (var bukkitWorld : Bukkit.getWorlds()) {
            var world = ((CraftWorld) bukkitWorld).getHandle();
            world.regioniser.computeForAllRegions(regions::add);
        }

        return regions;
    }

    public static double getLoadRate() {
        return ChunkFullTask.loadRate(System.nanoTime());
    }


    public static double getGenRate() {
        return ChunkFullTask.genRate(System.nanoTime());
    }

    private static @NotNull TickRegions.TickRegionData getRegionData() {
        return TickRegionScheduler.getCurrentRegion().getData();
    }

    private static @NotNull Executor createExecutorFromPlayerScheduler(@NotNull Player player) {
        return command -> player.getScheduler().run(ScoreboardPlugin.getPlugin(), $ -> command.run(), null);
    }

    private static @NotNull Executor createExecutorFromRegionScheduler(@NotNull Location location) {
        return command -> Bukkit.getRegionScheduler().run(ScoreboardPlugin.getPlugin(), location, $ -> command.run());
    }
}
