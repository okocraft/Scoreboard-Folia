package net.okocraft.scoreboard.command;

import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.command.subcommand.HideCommand;
import net.okocraft.scoreboard.command.subcommand.ReloadCommand;
import net.okocraft.scoreboard.command.subcommand.ShowCommand;
import net.okocraft.scoreboard.message.CommandMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class ScoreboardCommand implements CommandExecutor, TabCompleter {

    private static final String COMMAND_PERMISSION = "scoreboard.command";

    private final SubCommandHolder subCommandHolder;

    public ScoreboardCommand(@NotNull ScoreboardPlugin plugin) {
        subCommandHolder = new SubCommandHolder(
                new ShowCommand(plugin.getBoardManager(), plugin.getDisplayManager()),
                new HideCommand(plugin.getDisplayManager()),
                new ReloadCommand(plugin)
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender.hasPermission(COMMAND_PERMISSION))) {
            sender.sendMessage(CommandMessage.NO_PERMISSION.apply(COMMAND_PERMISSION));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        var optionalSubCommand = subCommandHolder.search(args[0]);

        if (optionalSubCommand.isEmpty()) {
            sendHelp(sender);
            return true;
        }

        var subCommand = optionalSubCommand.get();

        if (sender.hasPermission(subCommand.getPermissionNode())) {
            subCommand.onCommand(sender, args);
        } else {
            sender.sendMessage(CommandMessage.NO_PERMISSION.apply(subCommand.getPermissionNode()));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command c, @NotNull String s, @NotNull String[] args) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(args);

        if (args.length == 0 || !sender.hasPermission(COMMAND_PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return subCommandHolder.getSubCommands().stream()
                    .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                    .map(net.okocraft.scoreboard.command.Command::getName)
                    .filter(cmdName -> cmdName.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }

        return subCommandHolder.search(args[0])
                .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                .map(cmd -> cmd.onTabComplete(sender, args))
                .orElse(Collections.emptyList());
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(
                text("============================== ", DARK_GRAY)
                        .append(text("Scoreboard Help", GOLD))
                        .append(text(" ============================== ", DARK_GRAY))
        );

        subCommandHolder.getSubCommands()
                .stream()
                .map(net.okocraft.scoreboard.command.Command::getHelp)
                .forEach(sender::sendMessage);
    }
}
