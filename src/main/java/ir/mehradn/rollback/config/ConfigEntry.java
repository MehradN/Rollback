package ir.mehradn.rollback.config;

import net.minecraft.util.Mth;
import java.util.Objects;

public class ConfigEntry <T> {
    public final String name;
    public final Class<T> type;
    private final ConfigEntry<T> fallback;
    private final Trimmer<T> trimmer;
    private T value;

    public ConfigEntry(String name, Class<T> type, T value, ConfigEntry<T> fallback, Trimmer<T> trimmer) {
        assert value != null || fallback != null;
        this.name = name;
        this.type = type;
        this.value = value;
        this.fallback = fallback;
        this.trimmer = Objects.requireNonNullElse(trimmer, new EmptyTrimmer<>());
    }

    public T get() {
        if (this.value == null)
            return this.fallback.get();
        return this.value;
    }

    public void set(T value) {
        assert value != null;
        this.value = this.trimmer.trim(value);
    }

    public void reset() {
        assert this.fallback != null;
        this.value = null;
    }

    public boolean needsFallback() {
        return this.value == null;
    }

    public boolean hasFallback() {
        return this.fallback != null;
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

        public Integer trim(Integer value) {
            return Mth.clamp(value, this.min, this.max);
        }
    }

    private static class EmptyTrimmer <T> implements Trimmer<T> {
        public T trim(T value) {
            return value;
        }
    }
}
