package net.okocraft.scoreboard.display.placeholder;

import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegions;
import net.kyori.adventure.text.Component;
import net.okocraft.scoreboard.util.PlatformHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.kyori.adventure.text.Component.text;

public interface Placeholder {

    static @NotNull Placeholder string(@NotNull String value) {
        return new ConstantPlaceholder(Component.text(value));
    }

    static void registerDefaults(@NotNull PlaceholderProvider target) {
        target.register("server_tps", context -> text(BigDecimal.valueOf(Bukkit.getTPS()[0]).setScale(2, RoundingMode.HALF_UP).toPlainString()));
        target.register("server_online", context -> text(Bukkit.getOnlinePlayers().size()));
        target.register("server_ram_used", context -> text(toMB(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));
        target.register("server_ram_free", context -> text(toMB(Runtime.getRuntime().freeMemory())));
        target.register("server_ram_total", context -> text(toMB(Runtime.getRuntime().totalMemory())));
        target.register("server_ram_max", context -> text(toMB(Runtime.getRuntime().maxMemory())));
        target.register("player_name", context -> context.viewer().name());
        target.register("player_displayname", context -> context.viewer().displayName());
        target.register("player_world", context -> text(context.world().getName()));
        target.register("player_block_x", context -> text(context.blockX()));
        target.register("player_block_y", context -> text(context.blockY()));
        target.register("player_block_z", context -> text(context.blockZ()));
        target.register("player_ping", context -> text(context.viewer().getPing()));

        if (!PlatformHelper.isFolia()) {
            return;
        }

        target.register("server_total_regions", context -> text(formatInt(PlatformHelper.getGlobalRegionCount())));
        target.register("server_chunk_load_rate", context -> text(formatDouble(PlatformHelper.getLoadRate())));
        target.register("server_chunk_gen_rate", context -> text(formatDouble(PlatformHelper.getGenRate())));
        target.register("world_total_regions", context -> text(formatInt(PlatformHelper.getWorldRegionCount(context.world()))));
        Placeholder tpsPlaceholder = context -> text(formatDouble(PlatformHelper.getFromRegionReport(context, report -> report.tpsData().segmentAll().average())));
        target.register("server_tps", tpsPlaceholder); // overwrite to %server_tps% due to Bukkit.getTPS() always returns 20.00
        target.register("region_tps", tpsPlaceholder);
        target.register("region_util", context -> text(formatDouble(PlatformHelper.getFromRegionReport(context, TickData.TickReportData::utilisation) * 100)));
        target.register("region_mspt", context -> text(formatDouble(PlatformHelper.getFromRegionReport(context, report -> report.timePerTickData().segmentAll().average()) / 1.0E6)));
        target.register("region_mspt_colored", context -> {
            double value = PlatformHelper.getFromRegionReport(context, report -> report.timePerTickData().segmentAll().average()) / 1.0E6;
            return PlatformHelper.colorizeMSPT(text().content(formatDouble(value)), value);
        });
        target.register("region_chunks", context -> text(formatInt(PlatformHelper.getFromRegionStats(context, TickRegions.RegionStats::getChunkCount))));
        target.register("region_players", context -> text(formatInt(PlatformHelper.getFromRegionStats(context, TickRegions.RegionStats::getPlayerCount))));
        target.register("region_entities", context -> text(formatInt(PlatformHelper.getFromRegionStats(context, TickRegions.RegionStats::getEntityCount))));
    }

    private static @NotNull String toMB(long bytes) {
        return Long.toString(bytes >> 20); // bytes / 1024 / 1024 (MB)
    }

    private static @NotNull String formatDouble(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static @NotNull String formatInt(int value) {
        return Integer.toString(value);
    }

    @NotNull
    Component apply(@NotNull Context context);

    record Context(@NotNull Player viewer, int blockX, int blockY, int blockZ) {

        public Context(@NotNull Player viewer) {
            this(viewer, NumberConversions.floor(viewer.getX()), NumberConversions.floor(viewer.getY()), NumberConversions.floor(viewer.getZ()));
        }

        public @NotNull World world() {
            return this.viewer.getWorld();
        }
    }
}
