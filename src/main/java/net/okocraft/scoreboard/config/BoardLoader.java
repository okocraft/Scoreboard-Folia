package net.okocraft.scoreboard.config;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.board.Line;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

final class BoardLoader {

    private static final String PATH_TITLE = "title";
    private static final String PATH_LINES = "lines";
    private static final String LEGACY_PATH_LINE = "line";
    private static final String PATH_LIST_SUFFIX = ".list";
    private static final String PATH_INTERVAL_SUFFIX = ".interval";
    private static final String PATH_LENGTH_LIMIT_SUFFIX = ".length-limit";

    private BoardLoader() {
        throw new UnsupportedOperationException();
    }

    static @NotNull Board loadDefaultBoard(@NotNull ScoreboardPlugin plugin) {
        var yaml = YamlConfiguration.create(plugin.getDataFolder().toPath().resolve("default.yml"));

        try {
            yaml.load();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load default.yml", e);
        }

        return createBoardFromYaml(yaml);
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
                    .map(YamlConfiguration::create)
                    .map(yaml -> {
                        try {
                            yaml.load();
                            return yaml;
                        } catch (IOException e) {
                            plugin.getLogger().log(Level.SEVERE, "Could not load " + yaml.getPath().getFileName(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .map(BoardLoader::createBoardFromYaml)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load board files", e);
            return Collections.emptyList();
        }
    }

    private static @NotNull Board createBoardFromYaml(@NotNull YamlConfiguration yaml) {
        Line title = createLine(yaml, PATH_TITLE);

        var section = yaml.getSection(LEGACY_PATH_LINE);

        if (section == null) {
            section = yaml.getSection(PATH_LINES);
        }

        List<Line> lines;

        if (section == null) {
            lines = Collections.emptyList();
        } else {
            var rootKeys = section.getKeyList();
            lines = new ArrayList<>(rootKeys.size());

            for (String root : rootKeys) {
                lines.add(createLine(section, root));
            }
        }

        var name = yaml.getPath().getFileName().toString();

        return new Board(name.substring(0, name.lastIndexOf('.')), title, lines);
    }

    private static boolean checkFilename(String filename) {
        var boardName = filename.substring(0, filename.lastIndexOf('.'));
        return isYaml(filename) && !boardName.equals("default");
    }

    private static boolean isYaml(String filename) {
        var checking = filename.toLowerCase(Locale.ENGLISH);
        return (checking.endsWith(".yml") && 4 < checking.length()) || (checking.endsWith(".yaml") && 5 < checking.length());
    }

    private static @NotNull Line createLine(@NotNull Configuration source, @NotNull String root) {
        List<String> lineList = source.getStringList(root + PATH_LIST_SUFFIX);
        if (lineList.isEmpty()) {
            return Line.EMPTY;
        } else {
            return new Line(
                    lineList,
                    source.getLong(root + PATH_INTERVAL_SUFFIX),
                    source.getInteger(root + PATH_LENGTH_LIMIT_SUFFIX, -1)
            );
        }
    }
}
