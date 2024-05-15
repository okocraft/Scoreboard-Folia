package net.okocraft.scoreboard.board;

import net.okocraft.scoreboard.board.line.Line;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Board(String name, Line title, List<Line> lines) {
    public @NotNull String permissionNode() {
        return "scoreboard.board." + this.name;
    }
}
