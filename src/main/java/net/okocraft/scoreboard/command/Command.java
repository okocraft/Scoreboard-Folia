package net.okocraft.scoreboard.command;

import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Set;

/**
 * An interface of the command.
 */
public interface Command {

    /**
     * Gets the command name.
     *
     * @return the command name
     */
    @NotNull String getName();

    /**
     * Gets the permission node.
     *
     * @return the permission node
     */
    @NotNull String getPermissionNode();

    /**
     * Gets the set of aliases.
     *
     * @return the set of aliases
     */
    @NotNull @Unmodifiable Set<String> getAliases();

    /**
     * Gets the helps.
     *
     * @return the helps
     */
    @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc);

    /**
     * Executes the command.
     * <p>
     * When this method is called, the executor has the permission
     * and the length of the argument array is greater than or equal to 1.
     *
     * @param sender the executor
     * @param args   the array of arguments
     */
    void onCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull MiniMessageSource msgSrc);

    /**
     * Gets the tab-completion.
     * <p>
     * When this method is called, the executor has the permission
     * and the length of the argument array is greater than or equal to 1.
     *
     * @param sender the executor
     * @param args   the array of arguments
     * @return the result of the tab-completion or an empty list
     */
    @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args);
}
