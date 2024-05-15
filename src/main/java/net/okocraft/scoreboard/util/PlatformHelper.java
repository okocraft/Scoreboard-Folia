package net.okocraft.scoreboard.util;

import ca.spottedleaf.concurrentutil.map.SWMRLong2ObjectHashTable;
import io.papermc.paper.chunk.system.scheduling.ChunkFullTask;
import io.papermc.paper.threadedregions.ThreadedRegionizer;
import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.okocraft.scoreboard.display.placeholder.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public final class PlatformHelper {

    private static final boolean FOLIA;
    private static final VarHandle FOLIA_REGIONIZER_REGIONS_BY_ID;

    private static final TextColor MSPT_WARN_ORANGE_COLOR = TextColor.color(0xff4b00);
    private static final TextColor MSPT_WARN_RED_COLOR = TextColor.color(0xf6aa00);

    static {
        boolean isFolia;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }

        FOLIA = isFolia;

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

    public static double getFromRegionReport(@NotNull Placeholder.Context context, @NotNull ToDoubleFunction<TickData.TickReportData> function) {
        var data = getTickReport(context);
        return data != null ? function.applyAsDouble(data) : 0.0;
    }

    public static int getFromRegionStats(@NotNull Placeholder.Context context, @NotNull ToIntFunction<TickRegions.RegionStats> function) {
        var stats = getRegionStats(context);
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

    public static @NotNull Component colorizeMSPT(@NotNull TextComponent.Builder builder, double value) {
        if (value < 45) {
            return builder.build();
        } else if (55 < value) {
            return builder.color(MSPT_WARN_RED_COLOR).build();
        } else {
            return builder.color(MSPT_WARN_ORANGE_COLOR).build();
        }
    }

    private static @Nullable TickRegions.TickRegionData getRegionData(@NotNull Placeholder.Context context) {
        if (!(context.world() instanceof CraftWorld world)) {
            return null;
        }

        var region = world.getHandle().regioniser.getRegionAtSynchronised(context.blockX() >> 4, context.blockZ() >> 4);
        return region != null ? region.getData() : null;
    }

    private static @Nullable TickRegions.RegionStats getRegionStats(@NotNull Placeholder.Context context) {
        var data = getRegionData(context);
        return data != null ? data.getRegionStats() : null;
    }

    private static @Nullable TickData.TickReportData getTickReport(@NotNull Placeholder.Context context) {
        var data = getRegionData(context);
        return data != null ? data.getRegionSchedulingHandle().getTickReport5s(System.nanoTime()) : null;
    }
}
