package net.okocraft.scoreboard.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractCommand implements Command {

    private final String name;
    private final String permissionNode;
    private final Set<String> aliases;

    /**
     * The constructor of {@link AbstractCommand}.
     *
     * @param name           the command name
     * @param permissionNode the permission node
     */
    public AbstractCommand(@NotNull String name, @NotNull String permissionNode) {
        this(name, permissionNode, Collections.emptySet());
    }

    /**
     * The constructor of {@link AbstractCommand}.
     *
     * @param name           the command name
     * @param permissionNode the permission node
     * @param aliases        the set of aliases
     */
    public AbstractCommand(@NotNull String name, @NotNull String permissionNode, @NotNull Set<String> aliases) {
        this.name = Objects.requireNonNull(name);
        this.permissionNode = Objects.requireNonNull(permissionNode);
        this.aliases = Objects.requireNonNull(aliases);
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull String getPermissionNode() {
        return this.permissionNode;
    }

    public @NotNull @Unmodifiable Set<String> getAliases() {
        return this.aliases;
    }

    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
