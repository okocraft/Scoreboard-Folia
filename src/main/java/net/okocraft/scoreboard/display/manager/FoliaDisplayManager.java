package net.okocraft.scoreboard.display.manager;

import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.config.BoardManager;
import net.okocraft.scoreboard.display.board.BoardDisplay;
import net.okocraft.scoreboard.display.board.FoliaBoardDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FoliaDisplayManager extends AbstractDisplayManager {

    public FoliaDisplayManager(@NotNull BoardManager boardManager) {
        super(boardManager);
    }

    @Override
    protected @NotNull BoardDisplay newDisplay(@NotNull Player player, @NotNull Board board) {
        return new FoliaBoardDisplay(player, board);
    }
}
