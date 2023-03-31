package net.okocraft.scoreboard.config;

import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BoardManager {

    private final ScoreboardPlugin plugin;

    private Board defaultBoard;
    private List<Board> customBoards;

    public BoardManager(@NotNull ScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public Board getDefaultBoard() {
        return defaultBoard;
    }

    @NotNull
    public List<Board> getCustomBoards() {
        return customBoards;
    }

    public void reload() {
        defaultBoard = BoardLoader.loadDefaultBoard(plugin);
        customBoards = BoardLoader.loadCustomBoards(plugin);
    }
}
