package net.okocraft.scoreboard.display.manager;

import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.display.board.BoardDisplay;
import net.okocraft.scoreboard.display.board.BukkitBoardDisplay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BukkitDisplayManager extends AbstractDisplayManager {

    private final ScoreboardPlugin plugin;

    public BukkitDisplayManager(@NotNull ScoreboardPlugin plugin) {
        super(plugin.getBoardManager());
        this.plugin = plugin;
    }

    @Override
    protected @NotNull BoardDisplay newDisplay(@NotNull Player player, @NotNull Board board) {
        Scoreboard scoreboard;

        if (Bukkit.isPrimaryThread()) {
            scoreboard = this.newScoreboard();
        } else {
            scoreboard = CompletableFuture.supplyAsync(
                    this::newScoreboard,
                    this.plugin.getServer().getScheduler().getMainThreadExecutor(this.plugin)
            ).join();
        }

        return new BukkitBoardDisplay(this.plugin, board, player, scoreboard);
    }

    private @NotNull Scoreboard newScoreboard() {
        return this.plugin.getServer().getScoreboardManager().getNewScoreboard();
    }
}
