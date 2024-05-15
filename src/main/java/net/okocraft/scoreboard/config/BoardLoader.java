package net.okocraft.scoreboard.config;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.board.line.Line;
import net.okocraft.scoreboard.board.line.LineFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

final class BoardLoader {

    private static final String PATH_TITLE = "title";
    private static final String LEGACY_PATH_LINE = "line";
    private static final String PATH_LINES = "lines";
    private static final String PATH_LIST = "list";
    private static final String PATH_INTERVAL = "interval";
    private static final String PATH_LENGTH_LIMIT = "length-limit";

    private BoardLoader() {
        throw new UnsupportedOperationException();
    }

    static @NotNull Board loadDefaultBoard(@NotNull ScoreboardPlugin plugin) throws IOException {
        var filepath = plugin.saveResource("default.yml");
        return createBoardFromNode(plugin.getLineCompiler(), getBoardName(filepath), YamlFormat.DEFAULT.load(filepath));
    }

    static @NotNull @Unmodifiable List<Board> loadCustomBoards(@NotNull ScoreboardPlugin plugin) {
        Path dirPath = plugin.getDataFolder().toPath().resolve("boards");

        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create board directory", e);
            }

            return Collections.emptyList();
        }

        try (var listStream = Files.list(dirPath)) {
            return listStream
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(p -> checkFilename(p.getFileName().toString()))
                    .map(filepath -> {
                        try {
                            return createBoardFromNode(plugin.getLineCompiler(), getBoardName(filepath), YamlFormat.DEFAULT.load(filepath));
                        } catch (IOException e) {
                            plugin.getLogger().log(Level.SEVERE, "Could not load " + filepath.getFileName(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load board files", e);
            return Collections.emptyList();
        }
    }

    private static @NotNull Board createBoardFromNode(@NotNull LineFormat.Compiler compiler, @NotNull String name, @NotNull MapNode node) {
        Line title = createLine(compiler, node.getMap(PATH_TITLE));

        List<Line> lines;

        if (node.getOrDefault(LEGACY_PATH_LINE, node.get(PATH_LINES)) instanceof MapNode linesSection) {
            lines = linesSection.value().values().stream()
                    .filter(MapNode.class::isInstance)
                    .map(MapNode.class::cast)
                    .map(mapNode -> createLine(compiler, mapNode))
                    .toList();
        } else {
            lines = Collections.emptyList();
        }

        return new Board(name, title, lines);
    }

    private static boolean checkFilename(String filename) {
        var boardName = filename.substring(0, filename.lastIndexOf('.'));
        return isYaml(filename) && !boardName.equals("default");
    }

    private static boolean isYaml(String filename) {
        var checking = filename.toLowerCase(Locale.ENGLISH);
        return (checking.endsWith(".yml") && 4 < checking.length()) || (checking.endsWith(".yaml") && 5 < checking.length());
    }

    private static String getBoardName(Path filepath) {
        var name = filepath.getFileName().toString();
        return name.substring(0, name.lastIndexOf('.'));
    }

    private static @NotNull Line createLine(@NotNull LineFormat.Compiler compiler, @NotNull MapNode source) {
        List<LineFormat> lineList = source.getList(PATH_LIST).asList(String.class).stream().map(LegacyComponentSerializer.legacyAmpersand()::deserialize).map(compiler::compile).toList();
        if (lineList.isEmpty()) {
            return Line.EMPTY;
        } else {
            return new Line(lineList, source.getLong(PATH_INTERVAL), source.getInteger(PATH_LENGTH_LIMIT, -1));
        }
    }
}
