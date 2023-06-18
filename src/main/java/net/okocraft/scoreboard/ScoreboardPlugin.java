package net.okocraft.scoreboard;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.key.Key;
import net.okocraft.scoreboard.command.ScoreboardCommand;
import net.okocraft.scoreboard.config.BoardManager;
import net.okocraft.scoreboard.display.manager.DisplayManager;
import net.okocraft.scoreboard.display.manager.PacketBasedDisplayManager;
import net.okocraft.scoreboard.external.PlaceholderAPIHooker;
import net.okocraft.scoreboard.listener.PlayerListener;
import net.okocraft.scoreboard.listener.PluginListener;
import net.okocraft.scoreboard.util.LengthChecker;
import net.okocraft.scoreboard.util.PlatformHelper;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

public class ScoreboardPlugin extends JavaPlugin {

    private static ScoreboardPlugin INSTANCE;

    public static @NotNull Plugin getPlugin() {
        return Objects.requireNonNull(INSTANCE);
    }

    private final TranslationDirectory translationDirectory =
            TranslationDirectory.newBuilder()
                    .setKey(Key.key("scoreboard:languages"))
                    .setDirectory(getDataFolder().toPath().resolve("languages"))
                    .setDefaultLocale(Locale.ENGLISH)
                    .onDirectoryCreated(this::saveDefaultLanguages)
                    .build();

    private BoardManager boardManager;
    private DisplayManager displayManager;
    private PlayerListener playerListener;
    private PluginListener pluginListener;

    @Override
    public void onLoad() {
        INSTANCE = this;

        try {
            saveDefaultFiles();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save default files", e);
        }

        try {
            translationDirectory.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load languages", e);
        }

        loadConfig();
        boardManager = new BoardManager(this);
        boardManager.reload();
    }

    @Override
    public void onEnable() {
        pluginListener = new PluginListener(this);
        pluginListener.register();

        displayManager = new PacketBasedDisplayManager(boardManager);

        playerListener = new PlayerListener(this);
        playerListener.register();


        if (PlaceholderAPIHooker.checkEnabled(getServer())) {
            printPlaceholderIsAvailable();
        }

        var command = getCommand("sboard");

        if (command != null) {
            var impl = new ScoreboardCommand(this);
            command.setExecutor(impl);
            command.setTabCompleter(impl);
        }

        PlatformHelper.runAsync(this::showDefaultBoardToOnlinePlayers);
    }

    @Override
    public void onDisable() {
        if (playerListener != null) {
            playerListener.unregister();
        }

        if (displayManager != null) {
            displayManager.hideAllBoards();
            displayManager.close();
        }

        if (pluginListener != null) {
            pluginListener.unregister();
        }
    }

    public void reload() {
        displayManager.hideAllBoards();

        try {
            translationDirectory.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load languages", e);
        }

        loadConfig();
        boardManager.reload();

        PlatformHelper.runAsync(this::showDefaultBoardToOnlinePlayers);
    }

    @NotNull
    public BoardManager getBoardManager() {
        if (boardManager == null) {
            throw new IllegalStateException();
        }

        return boardManager;
    }

    public DisplayManager getDisplayManager() {
        if (boardManager == null) {
            throw new IllegalStateException();
        }

        return displayManager;
    }

    public void printPlaceholderIsAvailable() {
        getLogger().info("PlaceholderAPI is available!");
    }

    private void saveDefaultFiles() throws IOException {
        ResourceUtils.copyFromJarIfNotExists(
                getFile().toPath(), "config.yml", getDataFolder().toPath().resolve("config.yml")
        );

        ResourceUtils.copyFromJarIfNotExists(
                getFile().toPath(), "default.yml", getDataFolder().toPath().resolve("default.yml")
        );
    }

    private void loadConfig() {
        try (var config = YamlConfiguration.create(getDataFolder().toPath().resolve("config.yml"))) {
            config.load();
            LengthChecker.setLimit(Math.max(config.getInteger("max-line-length", 32), 1));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml", e);
        }
    }

    private void saveDefaultLanguages(@NotNull Path directory) throws IOException {
        var english = "en.yml";
        ResourceUtils.copyFromJarIfNotExists(getFile().toPath(), english, directory.resolve(english));

        var japanese = "ja_JP.yml";
        ResourceUtils.copyFromJarIfNotExists(getFile().toPath(), japanese, directory.resolve(japanese));
    }

    private void showDefaultBoardToOnlinePlayers() {
        getServer().getOnlinePlayers()
                .stream()
                .filter(player -> player.hasPermission("scoreboard.show-on-join"))
                .forEach(displayManager::showDefaultBoard);
    }
}
