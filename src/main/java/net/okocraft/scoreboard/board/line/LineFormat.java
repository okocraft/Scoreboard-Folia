package net.okocraft.scoreboard.board.line;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.okocraft.scoreboard.display.placeholder.Placeholder;
import net.okocraft.scoreboard.display.placeholder.PlaceholderProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LineFormat {

    public static @NotNull Compiler compiler(@NotNull PlaceholderProvider provider) {
        return new LineFormatCompiler(provider);
    }

    private final List<StyleInheritingPlaceholder> placeholders;

    LineFormat(@NotNull List<StyleInheritingPlaceholder> placeholders) {
        this.placeholders = placeholders;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public TextComponent render(@NotNull Placeholder.Context context) {
        var builder = Component.text();

        for (int i = 0, size = this.placeholders.size(); i < size; i++) {
            this.placeholders.get(i).appendRendered(builder, context);
        }

        return builder.build();
    }

    public interface Compiler {
        @NotNull LineFormat compile(@NotNull Component format);
    }

    record StyleInheritingPlaceholder(@NotNull Placeholder placeholder, @NotNull Style style) {
        private void appendRendered(@NotNull TextComponent.Builder builder, @NotNull Placeholder.Context context) {
            builder.append(this.placeholder.apply(context).applyFallbackStyle(this.style));
        }
    }
}
