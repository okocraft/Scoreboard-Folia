package net.okocraft.scoreboard.util;

import io.papermc.paper.chunk.system.scheduling.ChunkFullTask;
import io.papermc.paper.threadedregions.ThreadedRegionizer;
import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegions;
import net.okocraft.scoreboard.ScoreboardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public final class PlatformHelper {

    private static final boolean FOLIA;
    private static final boolean ASYNC_SCHEDULER;

    static {
        boolean isFolia;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }

        FOLIA = isFolia;

        boolean asyncScheduler;

        if (FOLIA) {
            asyncScheduler = true;
        } else {
            try {
                Bukkit.class.getDeclaredMethod("getAsyncScheduler");
                asyncScheduler = true;
            } catch (NoSuchMethodException e) {
                asyncScheduler = false;
            }
        }

        ASYNC_SCHEDULER = asyncScheduler;
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void runAsync(@NotNull Runnable runnable) {
        if (ASYNC_SCHEDULER) {
            Bukkit.getAsyncScheduler().runNow(ScoreboardPlugin.getPlugin(), $ -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(ScoreboardPlugin.getPlugin(), $ -> runnable.run());
        }
    }

    public static <T> @NotNull CompletableFuture<T> runOnPlayerScheduler(@NotNull Player player, @NotNull Supplier<T> supplier) {
        if (ASYNC_SCHEDULER) {
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
            return getFromRegionReport(location, report -> report.tpsData().segmentAll().average());
        } else {
            return Bukkit.getTPS()[0];
        }
    }

    public static double getFromRegionReport(@NotNull Location location, @NotNull ToDoubleFunction<TickData.TickReportData> function) {
        var data = getTickReport(location);
        return data != null ? function.applyAsDouble(data) : 0.0;
    }

    public static int getFromRegionStats(@NotNull Location location, @NotNull ToIntFunction<TickRegions.RegionStats> function) {
        var stats = getRegionStats(location);
        return stats != null ? function.applyAsInt(stats) : 0;
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

    private static @Nullable TickRegions.TickRegionData getRegionData(@NotNull Location location) {
        if (!(location.getWorld() instanceof CraftWorld world)) {
            return null;
        }

        var region = world.getHandle().regioniser.getRegionAtSynchronised(location.getBlockX() >> 4, location.getBlockZ() >> 4);

        if (region == null) {
            return null;
        }

        return region.getData();
    }

    private static @Nullable TickRegions.RegionStats getRegionStats(@NotNull Location location) {
        var data = getRegionData(location);
        return data != null ? data.getRegionStats() : null;
    }

    private static @Nullable TickData.TickReportData getTickReport(@NotNull Location location) {
        var data = getRegionData(location);
        return data != null ? data.getRegionSchedulingHandle().getTickReport5s(System.nanoTime()) : null;
    }

    private static @NotNull Executor createExecutorFromPlayerScheduler(@NotNull Player player) {
        return command -> player.getScheduler().run(ScoreboardPlugin.getPlugin(), $ -> command.run(), null);
    }
}
