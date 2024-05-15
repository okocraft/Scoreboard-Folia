package net.okocraft.scoreboard.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * A class that holds subcommands.
 */
public class SubCommandHolder {

    private final List<Command> subCommands;

    /**
     * The constructor of {@link SubCommandHolder}.
     *
     * @param subCommands the set of subcommands
     */
    public SubCommandHolder(@NotNull Command... subCommands) {
        Objects.requireNonNull(subCommands);
        this.subCommands = new ArrayList<>(Arrays.asList(subCommands));
    }

    /**
     * Gets the set of subcommands.
     * <p>
     * The returned list can be changed in implementation,
     * but it should not add or remove.
     *
     * @return the set of subcommands
     */
    public @NotNull @Unmodifiable List<Command> getSubCommands() {
        return this.subCommands;
    }

    /**
     * Registers the new subcommand.
     *
     * @param subCommand the new subcommand
     */
    public void register(@NotNull Command subCommand) {
        this.subCommands.add(subCommand);
    }

    /**
     * Unregisters the subcommand.
     *
     * @param subCommand the subcommand to unregister
     */
    public void unregister(@NotNull Command subCommand) {
        this.subCommands.remove(subCommand);
    }

    /**
     * Searches for a command that matches the name or aliases.
     *
     * @param name the name or the alias of the command
     * @return the search result
     */
    public @NotNull Optional<Command> search(@NotNull String name) {
        name = Objects.requireNonNull(name).toLowerCase(Locale.ROOT);

        for (var subCommand : this.subCommands) {
            if (subCommand.getName().equals(name) || subCommand.getAliases().contains(name)) {
                return Optional.of(subCommand);
            }
        }

        return Optional.empty();
    }
}
