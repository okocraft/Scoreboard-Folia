package net.okocraft.scoreboard.message;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.arg.Arg2;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.base.Placeholder;
import net.kyori.adventure.text.Component;
import net.okocraft.scoreboard.board.Board;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.arg.Arg2.arg2;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.withTagResolverBase;
import static com.github.siroshun09.messages.minimessage.base.Placeholder.messageBase;

public final class Messages {

    private static final Map<String, String> DEFAULT_MESSAGES = new LinkedHashMap<>();
    private static final Placeholder<String> PERMISSION = Placeholder.component("permission", Component::text);
    private static final Placeholder<String> NAME = Placeholder.component("name", Component::text);
    private static final Placeholder<String> COMMANDLINE_PLACEHOLDER = messageBase("commandline", MiniMessageBase::messageKey);
    private static final Placeholder<String> HELP_PLACEHOLDER = messageBase("help", MiniMessageBase::messageKey);
    private static final Placeholder<Board> BOARD = Placeholder.component("board", board -> Component.text(board.name()));
    private static final Placeholder<Player> PLAYER = Placeholder.component("board", player -> Component.text().content(player.getName()).hoverEvent(player).build());
    private static final Placeholder<Throwable> ERROR = Placeholder.component("error", ex -> Component.text(ex.getMessage()));

    public static final Arg1<String> NO_PERMISSION = arg1(def("scoreboard.error.no-permission", "<red>You don't have the permission: <aqua><permission>"), PERMISSION);
    public static final Arg1<String> BOARD_NOT_FOUND = arg1(def("scoreboard.error.board-not-found", "<red>Board <aqua><name></aqua> was not found."), NAME);
    public static final Arg1<String> PLAYER_NOT_FOUND = arg1(def("scoreboard.error.player-not-found", "<red>Player <aqua><name></aqua> was not found."), NAME);
    public static final MiniMessageBase ONLY_PLAYER = messageKey(def("scoreboard.error.only-player", "<red>This command can only be executed by the player."));

    public static final MiniMessageBase COMMAND_HELP_HEADER = withTagResolverBase(def("scoreboard.help.header", "<dark_gray><st>=========================<reset><gold><b> Scoreboard <reset><dark_gray><st>========================="));
    private static final String COMMAND_HELP_LINE_KEY = def("scoreboard.help.line", "<aqua><commandline><dark_gray>: <gray><help>");

    public static final MiniMessageBase SHOW_HELP = help(def("scoreboard.show.commandline", "/sb show <board> {player}"), def("scoreboard.show.help", "Shows the board"));
    public static final Arg1<Board> SHOW_SELF = arg1(def("scoreboard.show.self", "<gray>Board <aqua><board></aqua> is now displayed."), BOARD);
    public static final Arg2<Board, Player> SHOW_OTHER = arg2(def("scoreboard.show.other", "<gray>Board <aqua><board></aqua> is now displayed for player <aqua><player><aqua>."), BOARD, PLAYER);

    public static final MiniMessageBase HIDE_HELP = help(def("scoreboard.hide.commandline", "/sb hide {player}"), def("scoreboard.hide.help", "Hides the board"));
    public static final MiniMessageBase HIDE_ALREADY = messageKey(def("scoreboard.hide.already", "<red>The board is already hidden."));
    public static final MiniMessageBase HIDE_SELF = messageKey(def("scoreboard.hide.self", "<gray>The board is now hidden."));
    public static final Arg1<Player> HIDE_OTHER = arg1(def("scoreboard.hide.other", "<gray>Player <aqua><player><aqua>'s board is now hidden."), PLAYER);

    public static final MiniMessageBase RELOAD_HELP = help(def("scoreboard.reload.commandline", "/sb reload"), def("scoreboard.reload.help", "Reloads configurations."));
    public static final Arg1<Throwable> RELOAD_ERROR = arg1(def("scoreboard.reload.error", "<red>An error occurred while reloading configurations: <white><error>"), ERROR);
    public static final MiniMessageBase RELOAD_FINISH = messageKey(def("scoreboard.reload.finish", "<gray>Configurations have been reloaded!"));

    private static @NotNull String def(@NotNull String key, @NotNull String msg) {
        DEFAULT_MESSAGES.put(key, msg);
        return key;
    }

    private static @NotNull MiniMessageBase help(String commandlineKey, String helpKey) {
        return MiniMessageBase.withTagResolverBase(COMMAND_HELP_LINE_KEY, COMMANDLINE_PLACEHOLDER.apply(commandlineKey), HELP_PLACEHOLDER.apply(helpKey));
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView Map<String, String> defaultMessages() {
        return Collections.unmodifiableMap(DEFAULT_MESSAGES);
    }

    public static @Nullable Locale getLocaleFrom(@Nullable Object obj) {
        if (obj instanceof Locale locale) {
            return locale;
        } else if (obj instanceof Player player) {
            return player.locale();
        } else {
            return Locale.getDefault();
        }
    }

    private Messages() {
        throw new UnsupportedOperationException();
    }
}
