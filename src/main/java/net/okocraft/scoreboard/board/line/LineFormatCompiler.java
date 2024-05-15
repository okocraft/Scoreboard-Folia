package net.okocraft.scoreboard.board.line;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.okocraft.scoreboard.display.placeholder.Placeholder;
import net.okocraft.scoreboard.display.placeholder.PlaceholderProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

final class LineFormatCompiler implements LineFormat.Compiler {

    private static final char PLACEHOLDER_BRACKET = '%';

    private final PlaceholderProvider provider;

    LineFormatCompiler(@NotNull PlaceholderProvider provider) {
        this.provider = provider;
    }

    public @NotNull LineFormat compile(@NotNull Component format) {
        var result = new ArrayList<LineFormat.StyleInheritingPlaceholder>();

        if (format instanceof TextComponent rootText) {
            var style = format.style();
            this.compile(rootText.content(), placeholder -> result.add(new LineFormat.StyleInheritingPlaceholder(placeholder, style)));
        }

        if (format.children().isEmpty()) {
            return new LineFormat(Collections.unmodifiableList(result));
        }

        for (var element : format.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            if (element == format) { // Ignore the root component.
                continue;
            }

            if (element instanceof TextComponent textComponent) {
                var style = element.style();
                this.compile(textComponent.content(), placeholder -> result.add(new LineFormat.StyleInheritingPlaceholder(placeholder, style)));
            }
        }

        return new LineFormat(Collections.unmodifiableList(result));
    }

    private void compile(@NotNull String raw, @NotNull Consumer<Placeholder> consumer) {
        boolean inPlaceholder = false;

        var textBuilder = new StringBuilder();

        for (int codePoint : raw.codePoints().toArray()) {
            if (codePoint == PLACEHOLDER_BRACKET) {
                if (inPlaceholder) {
                    var rawPlaceholder = textBuilder.toString();
                    var placeholder = this.provider.get(rawPlaceholder);

                    consumer.accept(
                            placeholder != null ?
                                    placeholder :
                                    Placeholder.string(
                                            textBuilder.insert(0, PLACEHOLDER_BRACKET)
                                                    .append(PLACEHOLDER_BRACKET).toString()
                                    )
                    );

                    textBuilder.setLength(0);
                } else {
                    var text = textBuilder.toString();

                    if (!text.isEmpty()) {
                        consumer.accept(Placeholder.string(text));
                        textBuilder.setLength(0);
                    }
                }

                inPlaceholder = !inPlaceholder;
            } else {
                textBuilder.appendCodePoint(codePoint);
            }
        }

        if (!textBuilder.isEmpty()) {
            if (inPlaceholder) {
                consumer.accept(Placeholder.string(textBuilder.insert(0, PLACEHOLDER_BRACKET).toString()));
            } else {
                consumer.accept(Placeholder.string(textBuilder.toString()));
            }
        }
    }
}
