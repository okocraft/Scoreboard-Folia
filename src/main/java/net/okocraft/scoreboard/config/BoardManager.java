package net.okocraft.scoreboard.config;

import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.board.line.Line;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class BoardManager {

    private final ScoreboardPlugin plugin;

    private Board defaultBoard = new Board("default", Line.EMPTY, Collections.emptyList());
    private List<Board> customBoards = Collections.emptyList();

    public BoardManager(@NotNull ScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public Board getDefaultBoard() {
        return this.defaultBoard;
    }

    @NotNull
    public List<Board> getCustomBoards() {
        return this.customBoards;
    }

    public void reload() throws IOException {
        this.defaultBoard = BoardLoader.loadDefaultBoard(this.plugin);
        this.customBoards = BoardLoader.loadCustomBoards(this.plugin);
    }
}
