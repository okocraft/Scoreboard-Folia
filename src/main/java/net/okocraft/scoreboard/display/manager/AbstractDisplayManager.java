package net.okocraft.scoreboard.display.manager;

import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.display.board.BoardDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDisplayManager implements DisplayManager {

    protected final ScoreboardPlugin plugin;
    private final Map<UUID, BoardDisplay> displayMap = new ConcurrentHashMap<>();

    public AbstractDisplayManager(@NotNull ScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void showBoard(@NotNull Player player, @NotNull Board board) {
        hideBoard(player);

        var display = newDisplay(player, board);

        if (!display.isVisible()) {
            display.showBoard();
        }

        displayMap.put(player.getUniqueId(), display);
    }

    @Override
    public void showDefaultBoard(@NotNull Player player) {
        showBoard(player, plugin.getBoardManager().getDefaultBoard());
    }

    @Override
    public void hideBoard(@NotNull Player player) {
        var display = displayMap.remove(player.getUniqueId());

        if (display != null && display.isVisible()) {
            display.hideBoard();
        }
    }

    @Override
    public void hideAllBoards() {
        displayMap.values().stream().filter(BoardDisplay::isVisible).forEach(BoardDisplay::hideBoard);
        displayMap.clear();
    }

    @Override
    public boolean isDisplayed(@NotNull Player player) {
        return displayMap.containsKey(player.getUniqueId());
    }

    protected abstract @NotNull BoardDisplay newDisplay(@NotNull Player player, @NotNull Board board);
}
