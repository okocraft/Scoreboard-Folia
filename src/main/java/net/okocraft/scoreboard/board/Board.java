package net.okocraft.scoreboard.board;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Board {

    private final String name;
    private final Line title;
    private final List<Line> lines;

    public Board(@NotNull String name, @NotNull Line title, @NotNull List<Line> lines) {
        this.name = name;
        this.title = title;
        this.lines = lines;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Line getTitle() {
        return title;
    }

    @NotNull
    public List<Line> getLines() {
        return lines;
    }
}
