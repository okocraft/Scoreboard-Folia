package net.okocraft.scoreboard;

import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import com.github.siroshun09.messages.api.directory.DirectorySource;
import com.github.siroshun09.messages.api.directory.MessageProcessors;
import com.github.siroshun09.messages.api.source.StringMessageMap;
import com.github.siroshun09.messages.api.util.PropertiesFile;
import com.github.siroshun09.messages.minimessage.localization.MiniMessageLocalization;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.okocraft.scoreboard.command.ScoreboardCommand;
import net.okocraft.scoreboard.config.BoardManager;
import net.okocraft.scoreboard.display.line.LineDisplay;
import net.okocraft.scoreboard.display.manager.DisplayManager;
import net.okocraft.scoreboard.display.manager.PacketBasedDisplayManager;
import net.okocraft.scoreboard.external.PlaceholderAPIHooker;
import net.okocraft.scoreboard.listener.PlayerListener;
import net.okocraft.scoreboard.listener.PluginListener;
import net.okocraft.scoreboard.message.Messages;
import net.okocraft.scoreboard.util.PlatformHelper;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ScoreboardPlugin extends JavaPlugin {

    private static ScoreboardPlugin INSTANCE;

    public static @NotNull Plugin getPlugin() {
        return Objects.requireNonNull(INSTANCE);
    }

    private final BoardManager boardManager = new BoardManager(this);

    private boolean boardLoaded;
    private MiniMessageLocalization localization;
    private DisplayManager displayManager;
    private PlayerListener playerListener;
    private PluginListener pluginListener;

    @Override
    public void onLoad() {
        INSTANCE = this;

        this.boardLoaded = this.reloadSettings(ex -> {
        });
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

        if (this.boardLoaded) {
            PlatformHelper.runAsync(this::showDefaultBoardToOnlinePlayers);
        }
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

    public boolean reload(@NotNull Consumer<Throwable> exceptionConsumer) {
        displayManager.hideAllBoards();

        if (this.reloadSettings(exceptionConsumer)) {
            PlatformHelper.runAsync(this::showDefaultBoardToOnlinePlayers);
            return true;
        } else {
            return false;
        }
    }

    private boolean reloadSettings(@NotNull Consumer<Throwable> exceptionConsumer) {
        try {
            this.loadConfig();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml", e);
            exceptionConsumer.accept(e);
            return false;
        }

        try {
            this.loadMessages();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load languages", e);
            exceptionConsumer.accept(e);
            return false;
        }

        try {
            this.boardManager.reload();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load boards", e);
            exceptionConsumer.accept(e);
            return false;
        }

        return true;
    }

    public @NotNull MiniMessageLocalization getLocalization() {
        return this.localization;
    }

    @NotNull
    public BoardManager getBoardManager() {
        return boardManager;
    }

    public DisplayManager getDisplayManager() {
        if (displayManager == null) {
            throw new IllegalStateException();
        }

        return displayManager;
    }

    public void printPlaceholderIsAvailable() {
        getLogger().info("PlaceholderAPI is available!");
    }

    public Path saveResource(String filename) throws IOException {
        var filepath = this.getDataFolder().toPath().resolve(filename);
        if (!Files.isRegularFile(filepath)) {
            try (var input = this.getResource(filename)) {
                if (input == null) {
                    throw new IllegalStateException(filename + " was not found in the jar.");
                }
                Files.copy(input, filepath);
            }
        }
        return filepath;
    }

    private void loadConfig() throws IOException {
        var config = YamlFormat.DEFAULT.load(this.saveResource("config.yml"));
        LineDisplay.globalLengthLimit = Math.max(config.getInteger("max-line-length", 32), 1);
    }

    private void loadMessages() throws IOException {
        if (this.localization == null) { // on startup
            this.localization = new MiniMessageLocalization(MiniMessageSource.create(StringMessageMap.create(Messages.defaultMessages())), Messages::getLocaleFrom);
        } else { // on reload
            this.localization.clearSources();
        }

        DirectorySource.propertiesFiles(this.getDataFolder().toPath().resolve("languages"))
                .defaultLocale(Locale.ENGLISH, Locale.JAPANESE)
                .messageProcessor(MessageProcessors.appendMissingMessagesToPropertiesFile(this::loadDefaultMessageMap))
                .load(loaded -> this.localization.addSource(loaded.locale(), MiniMessageSource.create(loaded.messageSource())));
    }

    private @Nullable Map<String, String> loadDefaultMessageMap(@NotNull Locale locale) throws IOException {
        if (locale.equals(Locale.ENGLISH)) {
            return Messages.defaultMessages();
        } else {
            try (var input = this.getResource(locale + ".properties")) {
                return input != null ? PropertiesFile.load(input) : null;
            }
        }
    }

    private void showDefaultBoardToOnlinePlayers() {
        getServer().getOnlinePlayers()
                .stream()
                .filter(player -> player.hasPermission("scoreboard.show-on-join"))
                .forEach(displayManager::showDefaultBoard);
    }
}
