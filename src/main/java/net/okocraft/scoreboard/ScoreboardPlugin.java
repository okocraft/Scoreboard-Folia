package net.okocraft.scoreboard;

import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import com.github.siroshun09.messages.api.directory.DirectorySource;
import com.github.siroshun09.messages.api.directory.MessageProcessors;
import com.github.siroshun09.messages.api.source.StringMessageMap;
import com.github.siroshun09.messages.api.util.PropertiesFile;
import com.github.siroshun09.messages.minimessage.localization.MiniMessageLocalization;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.okocraft.scoreboard.board.line.LineFormat;
import net.okocraft.scoreboard.command.ScoreboardCommand;
import net.okocraft.scoreboard.config.BoardManager;
import net.okocraft.scoreboard.display.line.LineDisplay;
import net.okocraft.scoreboard.display.manager.DisplayManager;
import net.okocraft.scoreboard.display.manager.PacketBasedDisplayManager;
import net.okocraft.scoreboard.display.placeholder.Placeholder;
import net.okocraft.scoreboard.display.placeholder.PlaceholderProvider;
import net.okocraft.scoreboard.external.PlaceholderAPIHooker;
import net.okocraft.scoreboard.listener.PlayerListener;
import net.okocraft.scoreboard.listener.PluginListener;
import net.okocraft.scoreboard.message.Messages;
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

    private final PlaceholderProvider placeholderProvider = new PlaceholderProvider(PlaceholderAPIHooker::createPlaceholder);
    private final LineFormat.Compiler lineCompiler = LineFormat.compiler(this.placeholderProvider);
    private final BoardManager boardManager = new BoardManager(this);

    private MiniMessageLocalization localization;
    private DisplayManager displayManager;
    private PlayerListener playerListener;
    private PluginListener pluginListener;

    @Override
    public void onLoad() {
        INSTANCE = this;

        Placeholder.registerDefaults(this.placeholderProvider);

        this.reloadSettings(ex -> {});
    }

    @Override
    public void onEnable() {
        this.pluginListener = new PluginListener(this);
        this.pluginListener.register();

        this.displayManager = new PacketBasedDisplayManager(this.boardManager);

        this.playerListener = new PlayerListener(this);
        this.playerListener.register();


        if (PlaceholderAPIHooker.checkEnabled(this.getServer())) {
            this.printPlaceholderIsAvailable();
        }

        var command = this.getCommand("sboard");

        if (command != null) {
            var impl = new ScoreboardCommand(this);
            command.setExecutor(impl);
            command.setTabCompleter(impl);
        }
    }

    @Override
    public void onDisable() {
        if (this.playerListener != null) {
            this.playerListener.unregister();
        }

        if (this.displayManager != null) {
            this.displayManager.hideAllBoards();
            this.displayManager.close();
        }

        if (this.pluginListener != null) {
            this.pluginListener.unregister();
        }

        this.getServer().getAsyncScheduler().cancelTasks(this);
    }

    public boolean reloadSettings(@NotNull Consumer<Throwable> exceptionConsumer) {
        try {
            this.loadConfig();
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not load config.yml", e);
            exceptionConsumer.accept(e);
            return false;
        }

        try {
            this.loadMessages();
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not load languages", e);
            exceptionConsumer.accept(e);
            return false;
        }

        try {
            this.boardManager.reload();
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not load boards", e);
            exceptionConsumer.accept(e);
            return false;
        }

        return true;
    }

    public @NotNull PlaceholderProvider getPlaceholderProvider() {
        return this.placeholderProvider;
    }

    public @NotNull LineFormat.Compiler getLineCompiler() {
        return this.lineCompiler;
    }

    public @NotNull MiniMessageLocalization getLocalization() {
        return this.localization;
    }

    @NotNull
    public BoardManager getBoardManager() {
        return this.boardManager;
    }

    public DisplayManager getDisplayManager() {
        if (this.displayManager == null) {
            throw new IllegalStateException();
        }

        return this.displayManager;
    }

    public void printPlaceholderIsAvailable() {
        this.getLogger().info("PlaceholderAPI is available!");
    }

    public Path saveResource(String filename) throws IOException {
        var filepath = this.getDataFolder().toPath().resolve(filename);
        if (!Files.isRegularFile(filepath)) {
            Files.createDirectories(this.getDataFolder().toPath());
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
}
