package net.okocraft.scoreboard.display.manager;

import net.okocraft.scoreboard.board.Board;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface DisplayManager {

    void showBoard(@NotNull Player player, @NotNull Board board);

    void showDefaultBoard(@NotNull Player player);

    void hideBoard(@NotNull Player player);

    void hideAllBoards();

    boolean isDisplayed(@NotNull Player player);
}
