package net.okocraft.scoreboard.display.placeholder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class PlaceholderProvider {

    private final Map<String, Placeholder> registry = new ConcurrentHashMap<>();
    private final Function<String, Placeholder> fallbackPlaceholderFunction;

    public PlaceholderProvider(@NotNull Function<String, Placeholder> fallbackPlaceholderFunction) {
        this.fallbackPlaceholderFunction = fallbackPlaceholderFunction;
    }

    public void register(@NotNull String key, @NotNull Placeholder placeholder) {
        this.registry.put(key, placeholder);
    }

    public void unregister(@NotNull String key) {
        this.registry.remove(key);
    }

    public @Nullable Placeholder get(@NotNull String key) {
        var placeholder = this.registry.get(key);
        return placeholder != null ? placeholder : this.fallbackPlaceholderFunction.apply(key);
    }
}
