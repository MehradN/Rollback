package ir.mehradn.rollback.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import ir.mehradn.rollback.exception.Assertion;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ConfigEntry <T> {
    public final String name;
    @Nullable private final T defaultValue;
    @Nullable private ConfigEntry<T> fallback;
    @Nullable private T value;

    protected ConfigEntry(String name, @Nullable T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public void setFallback(@NotNull ConfigEntry<T> fallback) {
        this.fallback = fallback;
    }

    public T get() {
        if (this.value == null) {
            Assertion.state(this.fallback != null, "You must provide a default value, or a fallback!");
            return this.fallback.get();
        }
        return this.value;
    }

    public void set(@NotNull T value) {
        this.value = trim(value);
    }

    public void reset() {
        this.value = this.defaultValue;
    }

    public void mergeFrom(ConfigEntry<T> entry) {
        if (entry.value != null)
            this.value = entry.value;
    }

    public boolean hasValue() {
        return this.value != null;
    }

    public abstract Component getTranslated();

    public abstract void writeToBuf(FriendlyByteBuf buf);

    public abstract void readFromBuf(FriendlyByteBuf buf);

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement json);

    protected abstract T trim(@NotNull T value);

    public static class Boolean extends ConfigEntry<java.lang.Boolean> {
        public Boolean(String name, @Nullable java.lang.Boolean defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public Component getTranslated() {
            return Component.translatable("rollback.config.bool." + this.name + "." + get().toString());
        }

        @Override
        public void writeToBuf(FriendlyByteBuf buf) {
            buf.writeBoolean(get());
        }

        @Override
        public void readFromBuf(FriendlyByteBuf buf) {
            set(buf.readBoolean());
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(get());
        }

        @Override
        public void fromJson(JsonElement json) {
            Assertion.argument(json instanceof JsonPrimitive primitive && primitive.isBoolean(), "Invalid config json!");
            set(json.getAsBoolean());
        }

        @Override
        protected java.lang.Boolean trim(@NotNull java.lang.Boolean value) {
            return value;
        }
    }

    public static class Short extends ConfigEntry<java.lang.Short> {
        private final short min;
        private final short max;

        public Short(String name, short min, short max, java.lang.@Nullable Short defaultValue) {
            super(name, defaultValue);
            Assertion.argument(min <= max, "Min must be less than max!");
            this.min = min;
            this.max = max;
        }

        @Override public Component getTranslated() {
            return Component.literal(get().toString());
        }

        @Override
        public void writeToBuf(FriendlyByteBuf buf) {
            buf.writeShort(get());
        }

        @Override
        public void readFromBuf(FriendlyByteBuf buf) {
            set(buf.readShort());
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(get());
        }

        @Override
        public void fromJson(JsonElement json) {
            Assertion.argument(json instanceof JsonPrimitive primitive && primitive.isNumber(), "Invalid config json!");
            set(json.getAsShort());
        }

        @Override protected java.lang.Short trim(@NotNull java.lang.Short value) {
            if (value > this.max)
                return this.max;
            if (value < this.min)
                return this.min;
            return value;
        }
    }

    public static class Enum <T extends java.lang.Enum<T>> extends ConfigEntry<T> {
        public final Class<T> enumClass;
        private static final Gson GSON = new Gson();

        public Enum(String name, Class<T> enumClass, @Nullable T defaultValue) {
            super(name, defaultValue);
            this.enumClass = enumClass;
        }

        @Override
        public Component getTranslated() {
            return Component.translatable("rollback.config.enum." + this.name + "." + get().toString());
        }

        @Override
        public void writeToBuf(FriendlyByteBuf buf) {
            buf.writeEnum(get());
        }

        @Override
        public void readFromBuf(FriendlyByteBuf buf) {
            set(buf.readEnum(this.enumClass));
        }

        @Override
        public JsonElement toJson() {
            return GSON.toJsonTree(get(), this.enumClass);
        }

        @Override
        public void fromJson(JsonElement json) {
            set(GSON.fromJson(json, this.enumClass));
        }

        @Override
        protected T trim(@NotNull T value) {
            return value;
        }
    }
}
