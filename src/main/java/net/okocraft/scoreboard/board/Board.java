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

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Line getTitle() {
        return title;
    }

    public @NotNull List<Line> getLines() {
        return lines;
    }

    public @NotNull String getPermissionNode() {
        return "scoreboard.board." + name;
    }
}
