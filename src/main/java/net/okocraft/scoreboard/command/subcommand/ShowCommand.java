package net.okocraft.scoreboard.command.subcommand;

import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.command.AbstractCommand;
import net.okocraft.scoreboard.config.BoardManager;
import net.okocraft.scoreboard.display.manager.DisplayManager;
import net.okocraft.scoreboard.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ShowCommand extends AbstractCommand {
    private static final String SHOW_PERMISSION = "scoreboard.command.show";
    private static final String SHOW_PERMISSION_OTHER = SHOW_PERMISSION + ".other";

    private final BoardManager boardManager;
    private final DisplayManager displayManager;

    public ShowCommand(@NotNull BoardManager boardManager, @NotNull DisplayManager displayManager) {
        super("show", SHOW_PERMISSION, Set.of("s"));
        this.boardManager = boardManager;
        this.displayManager = displayManager;
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return Messages.SHOW_HELP.create(msgSrc);
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull MiniMessageSource msgSrc) {
        Board board;

        if (1 < args.length) {
            board = this.searchForBoard(args[1]);

            if (board == null) {
                Messages.BOARD_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
                return;
            }

            if (!args[0].equalsIgnoreCase("default") && !sender.hasPermission(board.permissionNode())) {
                Messages.NO_PERMISSION.apply(board.permissionNode()).source(msgSrc).send(sender);
                return;
            }
        } else {
            board = this.boardManager.getDefaultBoard();
        }

        Player target;

        if (2 < args.length) {
            if (!sender.hasPermission(SHOW_PERMISSION_OTHER)) {
                Messages.NO_PERMISSION.apply(SHOW_PERMISSION_OTHER).source(msgSrc).send(sender);
                return;
            }

            target = Bukkit.getPlayer(args[2]);

            if (target == null) {
                Messages.PLAYER_NOT_FOUND.apply(args[2]).source(msgSrc).send(sender);
                return;
            }
        } else {
            if (sender instanceof Player player) {
                target = player;
            } else {
                Messages.ONLY_PLAYER.source(msgSrc).send(sender);
                return;
            }
        }

        this.displayManager.showBoard(target, board);

        if (sender.equals(target)) {
            Messages.SHOW_SELF.apply(board).source(msgSrc).send(sender);
        } else {
            Messages.SHOW_OTHER.apply(board, target).source(msgSrc).send(sender);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission(this.getPermissionNode())) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            var result = new ArrayList<String>();
            var filter = args[1].toLowerCase(Locale.ENGLISH);

            if ("default".startsWith(filter)) {
                result.add("default");
            }

            this.boardManager.getCustomBoards().stream()
                    .filter(board -> sender.hasPermission(board.permissionNode()))
                    .map(Board::name)
                    .filter(name -> name.startsWith(filter))
                    .forEach(result::add);
            return result;
        }

        if (args.length == 3 && sender.hasPermission(SHOW_PERMISSION_OTHER)) {
            var filter = args[2].toLowerCase(Locale.ENGLISH);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ENGLISH).startsWith(filter))
                    .toList();
        }

        return Collections.emptyList();
    }

    private @Nullable Board searchForBoard(@NotNull String name) {
        if (name.equalsIgnoreCase("default")) {
            return this.boardManager.getDefaultBoard();
        } else {
            return this.boardManager.getCustomBoards().stream()
                    .filter(b -> b.name().equals(name))
                    .findFirst().orElse(null);
        }
    }
}
