package net.okocraft.scoreboard.folia.placeholder;

import ca.spottedleaf.concurrentutil.map.SWMRLong2ObjectHashTable;
import io.papermc.paper.chunk.system.scheduling.ChunkFullTask;
import io.papermc.paper.threadedregions.ThreadedRegionizer;
import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.okocraft.scoreboard.display.placeholder.Placeholder;
import net.okocraft.scoreboard.display.placeholder.PlaceholderProvider;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import static net.kyori.adventure.text.Component.text;

public class FoliaPlaceholders {

    private static final VarHandle FOLIA_REGIONIZER_REGIONS_BY_ID;

    private static final TextColor MSPT_WARN_ORANGE_COLOR = TextColor.color(0xff4b00);
    private static final TextColor MSPT_WARN_RED_COLOR = TextColor.color(0xf6aa00);

    static {
        VarHandle regionsById;
        try {
            var regionsByIdField = ThreadedRegionizer.class.getDeclaredField("regionsById");
            regionsById = MethodHandles.privateLookupIn(ThreadedRegionizer.class, MethodHandles.lookup()).unreflectVarHandle(regionsByIdField);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
        FOLIA_REGIONIZER_REGIONS_BY_ID = regionsById;
    }

    public static void registerDefaults(@NotNull PlaceholderProvider target) {
        target.register("world_total_regions", context -> text(formatInt(getWorldRegionCount(context.world()))));
        target.register("server_total_regions", context -> text(formatInt(getGlobalRegionCount())));

        target.register("server_chunk_load_rate", context -> text(formatDouble(getLoadRate())));
        target.register("server_chunk_gen_rate", context -> text(formatDouble(getGenRate())));

        Placeholder tpsPlaceholder = context -> text(formatDouble(getFromRegionReport(context, report -> report.tpsData().segmentAll().average())));
        target.register("server_tps", tpsPlaceholder); // overwrite to %server_tps% due to Bukkit.getTPS() always returns 20.00
        target.register("region_tps", tpsPlaceholder);
        target.register("region_util", context -> text(formatDouble(getFromRegionReport(context, TickData.TickReportData::utilisation) * 100)));
        target.register("region_mspt", context -> text(formatDouble(getFromRegionReport(context, report -> report.timePerTickData().segmentAll().average()) / 1.0E6)));
        target.register("region_mspt_colored", context -> {
            double value = getFromRegionReport(context, report -> report.timePerTickData().segmentAll().average()) / 1.0E6;
            return colorizeMSPT(text().content(formatDouble(value)), value);
        });
        target.register("region_chunks", context -> text(formatInt(getFromRegionStats(context, TickRegions.RegionStats::getChunkCount))));
        target.register("region_players", context -> text(formatInt(getFromRegionStats(context, TickRegions.RegionStats::getPlayerCount))));
        target.register("region_entities", context -> text(formatInt(getFromRegionStats(context, TickRegions.RegionStats::getEntityCount))));
    }

    private static @NotNull String formatDouble(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static @NotNull String formatInt(int value) {
        return Integer.toString(value);
    }

    private static double getFromRegionReport(@NotNull Placeholder.Context context, @NotNull ToDoubleFunction<TickData.TickReportData> function) {
        var data = getTickReport(context);
        return data != null ? function.applyAsDouble(data) : 0.0;
    }

    private static int getFromRegionStats(@NotNull Placeholder.Context context, @NotNull ToIntFunction<TickRegions.RegionStats> function) {
        var stats = getRegionStats(context);
        return stats != null ? function.applyAsInt(stats) : 0;
    }

    private static int getGlobalRegionCount() {
        int count = 0;

        for (var bukkitWorld : Bukkit.getWorlds()) {
            count += getWorldRegionCount(bukkitWorld);
        }

        return count;
    }

    private static int getWorldRegionCount(World bukkitWorld) {
        var world = ((CraftWorld) bukkitWorld).getHandle();
        var regionsById = (SWMRLong2ObjectHashTable<?>) FOLIA_REGIONIZER_REGIONS_BY_ID.get(world.regioniser);
        return regionsById.size();
    }

    private static double getLoadRate() {
        return ChunkFullTask.loadRate(System.nanoTime());
    }

    private static double getGenRate() {
        return ChunkFullTask.genRate(System.nanoTime());
    }

    private static @NotNull Component colorizeMSPT(@NotNull TextComponent.Builder builder, double value) {
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
