package net.okocraft.scoreboard.display.line;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Line;
import net.okocraft.scoreboard.display.placeholder.Placeholders;
import net.okocraft.scoreboard.external.PlaceholderAPIHooker;
import net.okocraft.scoreboard.util.LengthChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class LineDisplay {

    private final Player player;
    private final Line line;

    private final String name;

    private TextComponent prevLine;
    private TextComponent currentLine;

    private int currentIndex = 0;

    public LineDisplay(@NotNull Player player, @NotNull Line line, int num) {
        this.player = player;
        this.line = line;
        this.name = String.valueOf(num);

        if (line.isEmpty()) {
            this.currentLine = Component.empty();
        } else {
            this.currentLine = processLine(line.get(0));
        }
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull TextComponent getCurrentLine() {
        return currentLine;
    }

    public void update() {
        currentIndex++;

        if (line.getMaxIndex() < currentIndex) {
            currentIndex = 0;
        }

        currentLine = processLine(line.get(currentIndex));
    }

    public boolean isChanged() {
        if (currentLine.equals(prevLine)) {
            return false;
        } else {
            prevLine = currentLine;
            return true;
        }
    }

    public boolean shouldUpdate() {
        return line.shouldUpdate();
    }

    public long getInterval() {
        return line.getInterval();
    }

    private @NotNull TextComponent processLine(@NotNull String line) {
        var replaceResult = Placeholders.replace(player, line); // replace built-in placeholders
        var processing = replaceResult.replaced();

        if (PlaceholderAPIHooker.isEnabled() && replaceResult.hasUnknownPlaceholders()) {
            if (Bukkit.isPrimaryThread()) {
                processing = PlaceholderAPIHooker.run(player, replaceResult.replaced());
            } else {
                processing =
                        CompletableFuture.supplyAsync(
                                () -> PlaceholderAPIHooker.run(player, replaceResult.replaced()),
                                Bukkit.getScheduler().getMainThreadExecutor(ScoreboardPlugin.getPlugin())
                        ).join();
            }
        }

        processing = LengthChecker.check(processing);

        return LegacyComponentSerializer.legacyAmpersand().deserialize(processing);
    }
}
