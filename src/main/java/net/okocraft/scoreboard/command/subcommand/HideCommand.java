package net.okocraft.scoreboard.command.subcommand;

import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.scoreboard.command.AbstractCommand;
import net.okocraft.scoreboard.display.manager.DisplayManager;
import net.okocraft.scoreboard.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HideCommand extends AbstractCommand {

    private static final String HIDE_PERMISSION = "scoreboard.command.hide";
    private static final String HIDE_PERMISSION_OTHER = HIDE_PERMISSION + ".other";

    private final DisplayManager displayManager;

    public HideCommand(@NotNull DisplayManager displayManager) {
        super("hide", HIDE_PERMISSION, Set.of("h"));
        this.displayManager = displayManager;
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return Messages.HIDE_HELP.create(msgSrc);
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull MiniMessageSource msgSrc) {
        Player target;

        if (1 < args.length) {
            if (!sender.hasPermission(HIDE_PERMISSION_OTHER)) {
                Messages.NO_PERMISSION.apply(HIDE_PERMISSION_OTHER).source(msgSrc).send(sender);
                return;
            }

            target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                Messages.PLAYER_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
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

        if (displayManager.isDisplayed(target)) {
            displayManager.hideBoard(target);
        } else {
            Messages.HIDE_ALREADY.source(msgSrc).send(sender);
            return;
        }

        if (sender.equals(target)) {
            Messages.HIDE_SELF.source(msgSrc).send(sender);
        } else {
            Messages.HIDE_OTHER.apply(target).source(msgSrc).send(sender);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission(getPermissionNode())) {
            return Collections.emptyList();
        }

        if (args.length == 2 && sender.hasPermission(HIDE_PERMISSION_OTHER)) {
            var filter = args[1].toLowerCase(Locale.ENGLISH);
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.startsWith(filter)).toList();
        }

        return Collections.emptyList();
    }
}
