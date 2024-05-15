package net.okocraft.scoreboard.board.line;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public record Line(@NotNull List<LineFormat> lines, long interval, boolean shouldUpdate, int lengthLimit) {

    public static final Line EMPTY = new Line(Collections.emptyList(), 0, false, 0);

    public Line(@NotNull List<LineFormat> lines, long interval, int lengthLimit) {
        this(lines, interval, !lines.isEmpty() && 0 < interval, lengthLimit);
    }

    public int lengthLimit(int defaultLimit) {
        return this.lengthLimit < 0 ? defaultLimit : this.lengthLimit;
    }
}
