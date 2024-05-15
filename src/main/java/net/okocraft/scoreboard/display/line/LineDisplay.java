package net.okocraft.scoreboard.display.line;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.okocraft.scoreboard.board.line.Line;
import net.okocraft.scoreboard.display.placeholder.Placeholder;
import net.okocraft.scoreboard.util.LengthChecker;
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

        if (line.lines().isEmpty()) {
            this.currentLine = Component.empty();
        } else {
            this.currentLine = this.processLine(0);
        }
    }

    public int getLineNumber() {
        return this.num;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull TextComponent getCurrentLine() {
        return this.currentLine;
    }

    public void update() {
        if (this.line.lines().size() <= ++this.currentIndex) {
            this.currentIndex = 0;
        }

        this.currentLine = this.processLine(this.currentIndex);
    }

    public boolean isChanged() {
        if (this.currentLine.equals(this.prevLine)) {
            return false;
        } else {
            this.prevLine = this.currentLine;
            return true;
        }
    }

    public boolean shouldUpdate() {
        return this.line.shouldUpdate();
    }

    public long getInterval() {
        return this.line.interval();
    }

    private @NotNull TextComponent processLine(int index) {
        return LengthChecker.check(
                this.line.lines().get(index).render(new Placeholder.Context(this.player)),
                this.line.lengthLimit(globalLengthLimit)
        );
    }
}
