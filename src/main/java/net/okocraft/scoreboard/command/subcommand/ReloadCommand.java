package net.okocraft.scoreboard.command.subcommand;

import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.command.AbstractCommand;
import net.okocraft.scoreboard.message.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends AbstractCommand {

    private final ScoreboardPlugin plugin;

    public ReloadCommand(@NotNull ScoreboardPlugin plugin) {
        super("reload", "scoreboard.command.reload");
        this.plugin = plugin;
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return Messages.RELOAD_HELP.create(msgSrc);
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull MiniMessageSource msgSrc) {
        if (plugin.reload(ex -> Messages.RELOAD_ERROR.apply(ex).source(msgSrc).send(sender))) {
            Messages.RELOAD_FINISH.source(msgSrc).send(sender);
        }
    }
}
