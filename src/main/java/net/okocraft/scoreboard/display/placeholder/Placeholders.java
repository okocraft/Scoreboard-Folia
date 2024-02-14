package net.okocraft.scoreboard.display.placeholder;

import io.papermc.paper.threadedregions.TickData;
import io.papermc.paper.threadedregions.TickRegions;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.scoreboard.util.PlatformHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Placeholders {

    public static @NotNull Result replace(@NotNull Player player, @NotNull String line) {
        var locationSnapshot = player.getLocation();
        var resultBuilder = new StringBuilder();

        boolean inPlaceholder = false;
        boolean hasUnknownPlaceholders = false;

        var placeholderBuilder = new StringBuilder();

        for (int codePoint : line.codePoints().toArray()) {
            if (codePoint == '%') {
                placeholderBuilder.appendCodePoint(codePoint);

                if (inPlaceholder) {
                    var placeholder = placeholderBuilder.toString();
                    var replaced = processPlaceholder(player, locationSnapshot, placeholder);

                    if (!hasUnknownPlaceholders && placeholder.equals(replaced)) {
                        hasUnknownPlaceholders = true;
                    }

                    resultBuilder.append(replaced);
                    placeholderBuilder.setLength(0);
                }

                inPlaceholder = !inPlaceholder;
            } else {
                if (inPlaceholder) {
                    placeholderBuilder.appendCodePoint(codePoint);
                } else {
                    resultBuilder.appendCodePoint(codePoint);
                }
            }
        }

        if (inPlaceholder) {
            resultBuilder.append(placeholderBuilder);
        }

        return new Result(resultBuilder.toString(), hasUnknownPlaceholders);
    }

    private static @NotNull String processPlaceholder(@NotNull Player player, @NotNull Location locationSnapshot, @NotNull String placeholder) {
        //@formatter:off
        return switch (placeholder) {
            case "%server_tps%" -> formatDouble(PlatformHelper.getRegionTPS(locationSnapshot));
            case "%server_online%" -> Integer.toString(Bukkit.getOnlinePlayers().size());
            case "%server_ram_used%" -> toMB(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            case "%server_ram_free%" -> toMB(Runtime.getRuntime().freeMemory());
            case "%server_ram_total%" -> toMB(Runtime.getRuntime().totalMemory());
            case "%server_ram_max%" -> toMB(Runtime.getRuntime().maxMemory());
            case "%player_name%" -> player.getName();
            case "%player_displayname%" -> LegacyComponentSerializer.legacyAmpersand().serialize(player.displayName());
            case "%player_world%" -> player.getWorld().getName();
            case "%player_block_x%" -> formatInt(locationSnapshot.getBlockX());
            case "%player_block_y%" -> formatInt(locationSnapshot.getBlockY());
            case "%player_block_z%" -> formatInt(locationSnapshot.getBlockZ());
            case "%player_ping%" -> formatInt(player.getPing());
            default -> foliaPlaceholders(locationSnapshot, placeholder);
        };
        //@formatter:on
    }

    private static @NotNull String foliaPlaceholders(@NotNull Location locationSnapshot, @NotNull String placeholder) {
        if (!PlatformHelper.isFolia()) {
            return placeholder;
        }

        //@formatter:off
        return switch (placeholder) {
            case "%server_total_regions%" -> formatInt(PlatformHelper.getGlobalRegionCount());
            case "%server_chunk_load_rate%" -> formatDouble(PlatformHelper.getLoadRate());
            case "%server_chunk_gen_rate%" -> formatDouble(PlatformHelper.getGenRate());
            case "%world_total_regions%" -> formatInt(PlatformHelper.getWorldRegionCount(locationSnapshot.getWorld()));
            case "%region_tps%" -> formatDouble(PlatformHelper.getFromRegionReport(locationSnapshot, report -> report.tpsData().segmentAll().average()));
            case "%region_util%" -> formatDouble(PlatformHelper.getFromRegionReport(locationSnapshot, TickData.TickReportData::utilisation) * 100);
            case "%region_mspt%" -> formatDouble(PlatformHelper.getFromRegionReport(locationSnapshot, report -> report.timePerTickData().segmentAll().average()) / 1.0E6);
            case "%region_chunks%" -> formatInt(PlatformHelper.getFromRegionStats(locationSnapshot, TickRegions.RegionStats::getChunkCount));
            case "%region_players%" -> formatInt(PlatformHelper.getFromRegionStats(locationSnapshot, TickRegions.RegionStats::getPlayerCount));
            case "%region_entities%" -> formatInt(PlatformHelper.getFromRegionStats(locationSnapshot, TickRegions.RegionStats::getEntityCount));
            default -> placeholder;
        };
        //@formatter:on
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

    private Placeholders() {
    }

    public record Result(@NotNull String replaced, boolean hasUnknownPlaceholders) {
    }
}
