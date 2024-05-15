package net.okocraft.scoreboard.display.manager;

import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.config.BoardManager;
import net.okocraft.scoreboard.display.board.BoardDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDisplayManager implements DisplayManager {

    private final Map<UUID, BoardDisplay> displayMap = new ConcurrentHashMap<>();
    private final BoardManager boardManager;

    public AbstractDisplayManager(@NotNull BoardManager boardManager) {
        this.boardManager = boardManager;
    }

    @Override
    public void showBoard(@NotNull Player player, @NotNull Board board) {
        this.hideBoard(player);

        var display = this.newDisplay(player, board);

        if (!display.isVisible()) {
            display.showBoard();
        }

        this.displayMap.put(player.getUniqueId(), display);
    }

    @Override
    public void showDefaultBoard(@NotNull Player player) {
        this.showBoard(player, this.boardManager.getDefaultBoard());
    }

    @Override
    public void hideBoard(@NotNull Player player) {
        var display = this.displayMap.remove(player.getUniqueId());

        if (display != null && display.isVisible()) {
            display.hideBoard();
        }
    }

    @Override
    public void hideAllBoards() {
        this.displayMap.values().stream().filter(BoardDisplay::isVisible).forEach(BoardDisplay::hideBoard);
        this.displayMap.clear();
    }

    @Override
    public boolean isDisplayed(@NotNull Player player) {
        return this.displayMap.containsKey(player.getUniqueId());
    }

    protected abstract @NotNull BoardDisplay newDisplay(@NotNull Player player, @NotNull Board board);
}
