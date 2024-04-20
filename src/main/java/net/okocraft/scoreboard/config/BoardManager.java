package net.okocraft.scoreboard.config;

import com.github.siroshun09.configapi.core.node.MapNode;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class BoardManager {

    private final ScoreboardPlugin plugin;

    private Board defaultBoard = BoardLoader.createBoardFromNode("default", MapNode.empty());
    private List<Board> customBoards = Collections.emptyList();

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

    public void reload() throws IOException {
        defaultBoard = BoardLoader.loadDefaultBoard(plugin);
        customBoards = BoardLoader.loadCustomBoards(plugin);
    }
}
