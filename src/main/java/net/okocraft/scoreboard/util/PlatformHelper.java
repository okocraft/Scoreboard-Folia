package net.okocraft.scoreboard.util;

import ca.spottedleaf.concurrentutil.map.SWMRLong2ObjectHashTable;
import io.papermc.paper.chunk.system.scheduling.ChunkFullTask;
import io.papermc.paper.threadedregions.ThreadedRegionizer;
import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegions;
import net.okocraft.scoreboard.ScoreboardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public final class PlatformHelper {

    private static final boolean FOLIA;
    private static final boolean ASYNC_SCHEDULER;
    private static final VarHandle FOLIA_REGIONIZER_REGIONS_BY_ID;

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

        if (FOLIA) {
            VarHandle regionsById;
            try {
                var regionsByIdField = ThreadedRegionizer.class.getDeclaredField("regionsById");
                regionsById = MethodHandles.privateLookupIn(ThreadedRegionizer.class, MethodHandles.lookup()).unreflectVarHandle(regionsByIdField);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
            FOLIA_REGIONIZER_REGIONS_BY_ID = regionsById;
        } else {
            FOLIA_REGIONIZER_REGIONS_BY_ID = null;
        }
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

    public static int getGlobalRegionCount() {
        int count = 0;

        for (var bukkitWorld : Bukkit.getWorlds()) {
            count += getWorldRegionCount(bukkitWorld);
        }

        return count;
    }

    public static int getWorldRegionCount(World bukkitWorld) {
        var world = ((CraftWorld) bukkitWorld).getHandle();
        var regionsById = (SWMRLong2ObjectHashTable<?>) FOLIA_REGIONIZER_REGIONS_BY_ID.get(world.regioniser);
        return regionsById.size();
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
