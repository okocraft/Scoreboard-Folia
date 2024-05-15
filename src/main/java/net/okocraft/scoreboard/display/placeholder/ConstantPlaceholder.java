package net.okocraft.scoreboard.display.placeholder;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

record ConstantPlaceholder(@NotNull Component component) implements Placeholder {
    @Override
    public @NotNull Component apply(@NotNull Context context) {
        return this.component;
    }
}
