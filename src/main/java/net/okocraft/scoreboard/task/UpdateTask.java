package net.okocraft.scoreboard.task;

import net.okocraft.scoreboard.display.board.BoardDisplay;
import net.okocraft.scoreboard.display.line.LineDisplay;
import org.jetbrains.annotations.NotNull;

public class UpdateTask implements Runnable {

    private final BoardDisplay board;
    private final LineDisplay line;
    private final boolean isTitleLine;

    public UpdateTask(@NotNull BoardDisplay board, @NotNull LineDisplay line, boolean isTitleLine) {
        this.board = board;
        this.line = line;
        this.isTitleLine = isTitleLine;
    }

    @Override
    public void run() {
        line.update();

        if (isTitleLine) {
            board.applyTitle();
        } else {
            board.applyLine(line);
        }
    }
}
