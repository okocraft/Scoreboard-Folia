package net.okocraft.scoreboard.display.board;

import net.okocraft.scoreboard.display.line.LineDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BoardDisplay {

    @NotNull LineDisplay getTitle();

    @NotNull List<LineDisplay> getLines();

    boolean isVisible();

    void showBoard();

    void hideBoard();

    void applyTitle();

    void applyLine(@NotNull LineDisplay line);

    void scheduleUpdateTasks();

    void cancelUpdateTasks();
}
