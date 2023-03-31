package net.okocraft.scoreboard.display.manager;

import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.display.board.BoardDisplay;
import net.okocraft.scoreboard.display.board.BukkitBoardDisplay;
import net.okocraft.scoreboard.util.ScheduledExecutorFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

public class BukkitDisplayManager extends AbstractDisplayManager {

    private final ScoreboardPlugin plugin;
    private final ScheduledExecutorService scheduler = ScheduledExecutorFactory.create(2);

    public BukkitDisplayManager(@NotNull ScoreboardPlugin plugin) {
        super(plugin.getBoardManager());
        this.plugin = plugin;
    }

    @Override
    protected @NotNull BoardDisplay newDisplay(@NotNull Player player, @NotNull Board board) {
        Scoreboard scoreboard;

        if (Bukkit.isPrimaryThread()) {
            scoreboard = newScoreboard();
        } else {
            scoreboard = CompletableFuture.supplyAsync(
                    this::newScoreboard,
                    plugin.getServer().getScheduler().getMainThreadExecutor(plugin)
            ).join();
        }

        return new BukkitBoardDisplay(scheduler, board, player, scoreboard);
    }

    private @NotNull Scoreboard newScoreboard() {
        return plugin.getServer().getScoreboardManager().getNewScoreboard();
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
    }
}
