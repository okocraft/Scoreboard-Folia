package net.okocraft.scoreboard.display.line;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.scoreboard.board.Line;
import net.okocraft.scoreboard.display.placeholder.Placeholders;
import net.okocraft.scoreboard.external.PlaceholderAPIHooker;
import net.okocraft.scoreboard.util.LengthChecker;
import net.okocraft.scoreboard.util.PlatformHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LineDisplay {

    public static int globalLengthLimit = 32;

    private final Player player;
    private final Line line;
    private final int num;
    private final String name;

    private TextComponent prevLine;
    private TextComponent currentLine;

    private int currentIndex = 0;

    public LineDisplay(@NotNull Player player, @NotNull Line line, int num) {
        this.player = player;
        this.line = line;
        this.num = num;
        this.name = String.valueOf(num);

        if (line.isEmpty()) {
            this.currentLine = Component.empty();
        } else {
            this.currentLine = processLine(0);
        }
    }

    public int getLineNumber() {
        return this.num;
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

        currentLine = processLine(currentIndex);
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

    private @NotNull TextComponent processLine(int index) {
        var replaceResult = Placeholders.replace(player, line.get(index)); // replace built-in placeholders
        var processing = replaceResult.replaced();

        if (PlaceholderAPIHooker.isEnabled() && replaceResult.hasUnknownPlaceholders()) {
            processing = PlatformHelper.runOnPlayerScheduler(player, () -> PlaceholderAPIHooker.run(player, replaceResult.replaced())).join();
        }

        int lengthLimit = line.getLengthLimit();

        return LengthChecker.check(
                LegacyComponentSerializer.legacyAmpersand().deserialize(processing),
                lengthLimit < 0 ? globalLengthLimit : lengthLimit
        );
    }
}
