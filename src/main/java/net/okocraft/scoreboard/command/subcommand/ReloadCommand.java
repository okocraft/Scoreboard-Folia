package net.okocraft.scoreboard.command.subcommand;

import net.kyori.adventure.text.Component;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.command.AbstractCommand;
import net.okocraft.scoreboard.message.CommandMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class ReloadCommand extends AbstractCommand {

    private final ScoreboardPlugin plugin;

    public ReloadCommand(@NotNull ScoreboardPlugin plugin) {
        super("reload", "scoreboard.command.reload");
        this.plugin = plugin;
    }

    @Override
    public @NotNull Component getHelp() {
        return CommandMessage.RELOAD_HELP;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        try {
            plugin.reload();
            sender.sendMessage(CommandMessage.RELOAD_FINISH);
        } catch (Throwable t) {
            sender.sendMessage(CommandMessage.RELOAD_ERROR.apply(t));
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Could not complete reloading.",
                    t
            );
        }
    }
}
