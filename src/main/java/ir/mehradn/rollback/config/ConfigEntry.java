package ir.mehradn.rollback.config;

import ir.mehradn.rollback.exception.Assertion;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigEntry <T> {
    public final String name;
    public final Class<T> type;
    @Nullable private final ConfigEntry<T> fallback;
    @Nullable private final T defaultValue;
    private final Trimmer<T> trimmer;
    @Nullable private T value;

    public ConfigEntry(String name, Class<T> type, @NotNull T defaultValue, @Nullable Trimmer<T> trimmer) {
        this(name, type, null, defaultValue, trimmer);
    }

    public ConfigEntry(String name, Class<T> type, @NotNull ConfigEntry<T> fallback, @Nullable Trimmer<T> trimmer) {
        this(name, type, fallback, null, trimmer);
    }

    private ConfigEntry(String name, Class<T> type, @Nullable ConfigEntry<T> fallback, @Nullable T defaultValue, @Nullable Trimmer<T> trimmer) {
        this.name = name;
        this.type = type;
        this.fallback = fallback;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.trimmer = (trimmer == null ? new EmptyTrimmer<>() : trimmer);
    }

    public T get() {
        if (this.value == null) {
            Assertion.state(this.fallback != null);
            return this.fallback.get();
        }
        return this.value;
    }

    public void set(@NotNull T value) {
        this.value = this.trimmer.trim(value);
    }

    public void reset() {
        if (this.defaultValue != null)
            this.value = this.defaultValue;
        else if (this.fallback != null)
            this.value = null;
    }

    public boolean needsFallback() {
        return this.value == null;
    }

    public Trimmer<T> getTrimmer() {
        return this.trimmer;
    }

    public interface Trimmer <T> {
        T trim(T value);
    }

    public static class IntegerTrimmer implements Trimmer<Integer> {
        private final int min;
        private final int max;

        public IntegerTrimmer(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public Integer trim(Integer value) {
            return Mth.clamp(value, this.min, this.max);
        }
    }

    private static class EmptyTrimmer <T> implements Trimmer<T> {
        @Override
        public T trim(T value) {
            return value;
        }
    }
}
