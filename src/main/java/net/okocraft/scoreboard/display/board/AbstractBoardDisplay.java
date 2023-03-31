package net.okocraft.scoreboard.display.board;

import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.display.line.LineDisplay;
import net.okocraft.scoreboard.task.UpdateTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public abstract class AbstractBoardDisplay implements BoardDisplay {

    protected static final int MAX_LINES = 16;

    protected final ScoreboardPlugin plugin;
    private final List<ScheduledFuture<?>> updateTasks = new ArrayList<>(MAX_LINES);

    public AbstractBoardDisplay(@NotNull ScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void scheduleUpdateTasks() {
        if (getTitle().shouldUpdate()) {
            updateTasks.add(scheduleUpdateTask(getTitle(), true, getTitle().getInterval()));
        }

        for (LineDisplay line : getLines()) {
            if (line.shouldUpdate()) {
                updateTasks.add(scheduleUpdateTask(line, false, line.getInterval()));
            }
        }
    }

    @Override
    public void cancelUpdateTasks() {
        updateTasks.stream().filter(t -> !t.isCancelled()).forEach(t -> t.cancel(true));
        updateTasks.clear();
    }

    private ScheduledFuture<?> scheduleUpdateTask(@NotNull LineDisplay display, boolean isTitleLine, long interval) {
        return plugin.scheduleUpdateTask(new UpdateTask(this, display, isTitleLine), interval);
    }
}
