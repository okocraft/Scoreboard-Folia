package net.okocraft.scoreboard.display.board;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.util.Tick;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.display.line.LineDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BukkitBoardDisplay implements BoardDisplay {

    private static final int MAX_LINES = 16;

    private final ScoreboardPlugin plugin;
    private final Player player;
    private final Scoreboard scoreboard;
    private final Objective objective;

    private final LineDisplay title;
    private final List<LineDisplay> lines;

    private final List<ScheduledTask> updateTasks = new ArrayList<>(MAX_LINES);

    public BukkitBoardDisplay(@NotNull ScoreboardPlugin plugin, @NotNull Board board,
                              @NotNull Player player, @NotNull Scoreboard scoreboard) {
        this.plugin = plugin;
        this.player = player;
        this.scoreboard = scoreboard;

        this.title = new LineDisplay(player, board.title(), 0);

        this.objective = scoreboard.registerNewObjective("sb", Criteria.DUMMY, this.title.getCurrentLine(), RenderType.INTEGER);

        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int size = Math.min(board.lines().size(), MAX_LINES);
        var lines = new ArrayList<LineDisplay>(size);

        for (int i = 0; i < size; i++) {
            var line = new LineDisplay(player, board.lines().get(i), i);
            lines.add(line);

            var score = this.objective.getScore(line.getName());
            score.setScore(size - i);
            score.numberFormat(NumberFormat.blank());
            score.customName(line.getCurrentLine());
        }

        this.lines = Collections.unmodifiableList(lines);
    }

    @Override
    public boolean isVisible() {
        return this.player.getScoreboard().equals(this.scoreboard);
    }

    @Override
    public void showBoard() {
        this.player.setScoreboard(this.scoreboard);
        this.scheduleUpdateTasks();
    }

    @Override
    public void hideBoard() {
        this.player.setScoreboard(this.plugin.getServer().getScoreboardManager().getMainScoreboard());
        this.cancelUpdateTasks();
    }

    @Override
    public void applyTitle() {
        if (this.title.isChanged()) {
            this.objective.displayName(this.title.getCurrentLine());
        }
    }

    @Override
    public void applyLine(@NotNull LineDisplay line) {
        if (line.isChanged()) {
            this.objective.getScore(line.getName()).customName(line.getCurrentLine());
        }
    }

    @Override
    @NotNull
    public LineDisplay getTitle() {
        return this.title;
    }

    @Override
    @NotNull
    public List<LineDisplay> getLines() {
        return this.lines;
    }

    private void scheduleUpdateTasks() {
        if (this.getTitle().shouldUpdate()) {
            this.updateTasks.add(this.scheduleUpdateTask(this.getTitle(), true, this.getTitle().getInterval()));
        }

        for (LineDisplay line : this.getLines()) {
            if (line.shouldUpdate()) {
                this.updateTasks.add(this.scheduleUpdateTask(line, false, line.getInterval()));
            }
        }
    }

    private void cancelUpdateTasks() {
        this.updateTasks.forEach(ScheduledTask::cancel);
        this.updateTasks.clear();
    }

    private ScheduledTask scheduleUpdateTask(@NotNull LineDisplay display, boolean isTitleLine, long ticks) {
        long interval = Tick.of(ticks).toMillis();
        return this.player.getServer().getAsyncScheduler().runAtFixedRate(this.plugin, ignored -> this.update(display, isTitleLine), interval, interval, TimeUnit.MILLISECONDS);
    }

    private void update(@NotNull LineDisplay line, boolean isTitle) {
        line.update();
        if (isTitle) {
            this.applyTitle();
        } else {
            this.applyLine(line);
        }
    }
}
