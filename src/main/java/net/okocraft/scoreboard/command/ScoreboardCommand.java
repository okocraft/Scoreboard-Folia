package net.okocraft.scoreboard.command;

import com.github.siroshun09.messages.minimessage.localization.MiniMessageLocalization;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.command.subcommand.HideCommand;
import net.okocraft.scoreboard.command.subcommand.ReloadCommand;
import net.okocraft.scoreboard.command.subcommand.ShowCommand;
import net.okocraft.scoreboard.message.Messages;
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

public class ScoreboardCommand implements CommandExecutor, TabCompleter {

    private static final String COMMAND_PERMISSION = "scoreboard.command";

    private final MiniMessageLocalization localization;
    private final SubCommandHolder subCommandHolder;

    public ScoreboardCommand(@NotNull ScoreboardPlugin plugin) {
        this.localization = plugin.getLocalization();
        this.subCommandHolder = new SubCommandHolder(
                new ShowCommand(plugin.getBoardManager(), plugin.getDisplayManager()),
                new HideCommand(plugin.getDisplayManager()),
                new ReloadCommand(plugin)
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        var msgSrc = this.localization.findSource(sender);

        if (!(sender.hasPermission(COMMAND_PERMISSION))) {
            Messages.NO_PERMISSION.apply(COMMAND_PERMISSION).source(msgSrc).send(sender);
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            this.sendHelp(sender, msgSrc);
            return true;
        }

        var optionalSubCommand = this.subCommandHolder.search(args[0]);

        if (optionalSubCommand.isEmpty()) {
            this.sendHelp(sender, msgSrc);
            return true;
        }

        var subCommand = optionalSubCommand.get();

        if (sender.hasPermission(subCommand.getPermissionNode())) {
            subCommand.onCommand(sender, args, msgSrc);
        } else {
            Messages.NO_PERMISSION.apply(subCommand.getPermissionNode()).source(msgSrc).send(sender);
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
            return this.subCommandHolder.getSubCommands().stream()
                    .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                    .map(net.okocraft.scoreboard.command.Command::getName)
                    .filter(cmdName -> cmdName.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }

        return this.subCommandHolder.search(args[0])
                .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                .map(cmd -> cmd.onTabComplete(sender, args))
                .orElse(Collections.emptyList());
    }

    private void sendHelp(@NotNull CommandSender sender, @NotNull MiniMessageSource msgSrc) {
        Messages.COMMAND_HELP_HEADER.source(msgSrc).send(sender);
        sender.sendMessage(Component.join(
                JoinConfiguration.newlines(),
                ((Iterable<Component>) this.subCommandHolder.getSubCommands().stream().map(cmd -> cmd.getHelp(msgSrc))::iterator)
        ));
    }
}
