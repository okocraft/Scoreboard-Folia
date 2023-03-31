package net.okocraft.scoreboard.message;

import com.github.siroshun09.translationloader.argument.DoubleArgument;
import com.github.siroshun09.translationloader.argument.SingleArgument;
import net.kyori.adventure.text.Component;
import net.okocraft.scoreboard.board.Board;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class CommandMessage {

    public static final SingleArgument<String> NO_PERMISSION =
            permission ->
                    translatable()
                            .key("scoreboard.error.no-permission")
                            .args(text(permission, AQUA))
                            .color(RED)
                            .build();

    public static final SingleArgument<String> BOARD_NOT_FOUND =
            name -> translatable("scoreboard.error.board-not-found", RED).args(text(name, AQUA));

    public static final SingleArgument<String> PLAYER_NOT_FOUND =
            name -> translatable("scoreboard.error.player-not-found", RED).args(text(name, AQUA));

    public static final Component ONLY_PLAYER =
            translatable("scoreboard.error.only-player", RED);

    public static final Component SHOW_HELP = createCommandHelp("/sb show <board> {player}", "scoreboard.show.help");

    public static final SingleArgument<Board> SHOW_BOARD_SELF =
            board ->
                    translatable("scoreboard.show.self", GRAY)
                            .args(text(board.getName(), AQUA));

    public static final DoubleArgument<Board, Player> SHOW_BOARD_OTHER =
            (board, target) ->
                    translatable("scoreboard.show.other", GRAY)
                            .args(
                                    text(board.getName(), AQUA),
                                    text(target.getName(), AQUA)
                            );

    public static final Component HIDE_HELP = createCommandHelp("/sb hide {player}", "scoreboard.hide.help");

    public static final Component HIDE_ALREADY = translatable("scoreboard.hide.already", RED);

    public static final Component HIDE_SELF = translatable("scoreboard.hide.self", GRAY);

    public static final SingleArgument<Player> HIDE_OTHER =
            target ->
                    translatable("scoreboard.hide.other", GRAY)
                            .args(
                                    text(target.getName(), AQUA)
                            );

    public static final Component RELOAD_HELP = createCommandHelp("/sb reload", "scoreboard.reload.help");

    public static final SingleArgument<Throwable> RELOAD_ERROR =
            throwable ->
                    translatable("scoreboard.reload.error", RED)
                            .args(text(throwable.getMessage(), WHITE));

    public static final Component RELOAD_FINISH = translatable("scoreboard.reload.finish", GRAY);

    private static @NotNull Component createCommandHelp(@NotNull String commandLine, @NotNull String helpKey) {
        return text()
                .append(text(commandLine, AQUA))
                .append(text(" - ", DARK_GRAY))
                .append(translatable(helpKey, GRAY))
                .build();
    }

    private CommandMessage() {
        throw new UnsupportedOperationException();
    }
}
