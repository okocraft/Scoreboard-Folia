package net.okocraft.scoreboard.config;

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
    private static final String PATH_LINE = "line";
    private static final String PATH_LIST_SUFFIX = ".list";
    private static final String PATH_INTERVAL_SUFFIX = ".interval";

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
        List<String> titleList = yaml.getStringList(PATH_TITLE + PATH_LIST_SUFFIX);
        Line title;

        if (titleList.isEmpty()) {
            title = Line.EMPTY;
        } else {
            title = new Line(titleList, yaml.getLong(PATH_TITLE + PATH_INTERVAL_SUFFIX));
        }

        var section = yaml.getSection(PATH_LINE);

        List<Line> lines;

        if (section == null) {
            lines = Collections.emptyList();
        } else {
            var rootKeys = section.getKeyList();
            lines = new ArrayList<>(rootKeys.size());

            for (String root : rootKeys) {
                List<String> lineList = section.getStringList(root + PATH_LIST_SUFFIX);

                if (lineList.isEmpty()) {
                    lines.add(Line.EMPTY);
                } else {
                    lines.add(new Line(lineList, section.getLong(root + PATH_INTERVAL_SUFFIX)));
                }
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
}
