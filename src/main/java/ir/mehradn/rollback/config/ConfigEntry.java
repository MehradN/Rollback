package ir.mehradn.rollback.config;

import ir.mehradn.rollback.exception.Assertion;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigEntry <T> {
    public final String name;
    public final Class<T> type;
    @Nullable private final T defaultValue;
    private final Trimmer<T> trimmer;
    @Nullable private ConfigEntry<T> fallback;
    @Nullable private T value;

    public ConfigEntry(String name, Class<T> type, @Nullable T defaultValue, @Nullable Trimmer<T> trimmer) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.trimmer = (trimmer == null ? new EmptyTrimmer<>() : trimmer);
    }

    public void setFallback(@NotNull ConfigEntry<T> fallback) {
        this.fallback = fallback;
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
        this.value = this.defaultValue;
    }

    public void copy(ConfigEntry<T> entry) {
        if (entry.value != null)
            this.value = entry.value;
    }

    public boolean needsFallback() {
        return this.value == null;
    }

    public Trimmer<T> getTrimmer() {
        return this.trimmer;
    }

    public String getAsString() {
        return this.get().toString();
    }

    public Component getAsTranslated() {
        String str = getAsString();
        Component text;
        if (this.type == Boolean.class)
            text = Component.translatable("rollback.config.bool." + this.name + "." + str);
        else if (this.type.isEnum())
            text = Component.translatable("rollback.config.enum." + this.name + "." + str);
        else
            text = Component.literal(str);
        return text;
    }

    public interface Trimmer <T> {
        @NotNull T trim(@NotNull T value);
    }

    public static class IntegerTrimmer implements Trimmer<Integer> {
        private final int min;
        private final int max;

        public IntegerTrimmer(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public @NotNull Integer trim(@NotNull Integer value) {
            return Mth.clamp(value, this.min, this.max);
        }
    }

    private static class EmptyTrimmer <T> implements Trimmer<T> {
        @Override
        public @NotNull T trim(@NotNull T value) {
            return value;
        }
    }
}
