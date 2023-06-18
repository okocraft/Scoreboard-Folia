package net.okocraft.scoreboard.display.board;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.scoreboard.display.line.LineDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface BoardDisplay {

    @Unmodifiable List<String> ENTRY_NAMES = createEntryNameList();

    private static List<String> createEntryNameList() {
        return NamedTextColor.NAMES.values().stream().map(BoardDisplay::createEntryNameFromColor).toList();
    }

    private static String createEntryNameFromColor(@NotNull NamedTextColor color) {
        return LegacyComponentSerializer.legacySection().serialize(Component.text("a", color)).substring(0, 2);
    }

    @NotNull LineDisplay getTitle();

    @NotNull List<LineDisplay> getLines();

    boolean isVisible();

    void showBoard();

    void hideBoard();

    void applyTitle();

    void applyLine(@NotNull LineDisplay line);
}
