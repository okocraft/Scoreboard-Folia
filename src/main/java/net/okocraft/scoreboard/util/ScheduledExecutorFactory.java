package net.okocraft.scoreboard.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.okocraft.scoreboard.ScoreboardPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

public final class ScheduledExecutorFactory {

    public static @NotNull ScheduledExecutorService create(int corePoolSize) {
        return Executors.newScheduledThreadPool(corePoolSize, createThreadFactory());
    }

    private static @NotNull ThreadFactory createThreadFactory() {
        return new ThreadFactoryBuilder()
                .setNameFormat("Scoreboard Thread - %d")
                .setUncaughtExceptionHandler(ScheduledExecutorFactory::catchUncaughtException)
                .setDaemon(true)
                .build();
    }

    private static void catchUncaughtException(Thread thread, Throwable exception) {
        JavaPlugin.getPlugin(ScoreboardPlugin.class).getLogger().log(
                Level.SEVERE,
                "An exception occurred on " + thread.getName(),
                exception
        );
    }

    private ScheduledExecutorFactory() {
        throw new UnsupportedOperationException();
    }

}
