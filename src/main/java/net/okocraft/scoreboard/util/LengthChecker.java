package net.okocraft.scoreboard.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public final class LengthChecker {

    public static @NotNull TextComponent check(@NotNull TextComponent component, int lengthLimit) {
        if (lengthLimit < 1) {
            return Component.empty();
        }

        if (lengthLimit < component.content().length()) { // The length of the root component has already been exceeded.
            return truncateContent(removeChildren(component), lengthLimit);
        } else if (component.children().isEmpty()) { // The component does not have the children, so return as-is.
            return component;
        }

        var builder = removeChildren(component).toBuilder(); // Remove the children from the root component.
        int totalLength = component.content().length();

        for (var element : component.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            if (element == component) { // Ignore the root component.
                continue;
            }

            if (element instanceof TextComponent textComponent) {
                var content = textComponent.content();
                int currentLength = totalLength; // Memorize totalLength before adding for calculating the number of characters remaining.
                totalLength += content.length();

                if (totalLength <= lengthLimit) { // There is still room for more characters.
                    builder.append(element);
                } else {
                    int remaining = lengthLimit - currentLength; // Calculate how many characters are remaining.

                    if (0 < remaining) { // Only the remaining number of characters is added to the result.
                        builder.append(truncateContent(removeChildren(textComponent), remaining));
                    }

                    break; // No further components will be processed because the character limit has been reached.
                }
            }
        }

        return builder.build();
    }

    private static @NotNull TextComponent removeChildren(@NotNull TextComponent component) {
        return component.children().isEmpty() ? component : component.children(Collections.emptyList());
    }

    private static @NotNull TextComponent truncateContent(@NotNull TextComponent component, int length) {
        return component.content(component.content().substring(0, length));
    }

    private LengthChecker() {
        throw new UnsupportedOperationException();
    }
}
