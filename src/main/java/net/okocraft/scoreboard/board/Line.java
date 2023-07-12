package net.okocraft.scoreboard.board;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Line {

    public static final Line EMPTY = new Line(Collections.emptyList(), 0, 0);

    private final List<String> lines;
    private final long interval;
    private final boolean shouldUpdate;
    private final int lengthLimit;

    public Line(@NotNull List<String> lines, long interval, int lengthLimit) {
        this.lines = List.copyOf(lines);
        this.shouldUpdate = !lines.isEmpty() && 0 < interval;
        this.interval = interval;
        this.lengthLimit = lengthLimit;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public int getMaxIndex() {
        return lines.size() - 1;
    }

    @NotNull
    public String get(int index) {
        return lines.get(index);
    }

    public boolean shouldUpdate() {
        return shouldUpdate;
    }

    public long getInterval() {
        return interval;
    }

    public int getLengthLimit() {
        return lengthLimit;
    }
}
