package ir.mehradn.rollback.config;

import com.google.gson.*;
import ir.mehradn.rollback.rollback.BackupType;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class RollbackConfig {
    public static final int MAX_AUTOMATED = 10;
    public static final int MAX_COMMAND = 99;
    public static final int MAX_FREQUENCY = 100;
    public static RollbackConfig DEFAULT;
    public final ConfigEntry<Boolean> backupEnabled;
    public final ConfigEntry<Integer> maxBackups;
    public final ConfigEntry<Integer> backupFrequency;
    public final ConfigEntry<TimerMode> timerMode;
    protected List<ConfigEntry<?>> entries;
    private static final Gson GSON = new GsonBuilder().create();
    private boolean locked = false;

    protected RollbackConfig(ConfigEntry<Boolean> backupEnabled, ConfigEntry<Integer> maxBackups, ConfigEntry<Integer> backupFrequency,
                             ConfigEntry<TimerMode> timerMode) {
        this.backupEnabled = backupEnabled;
        this.maxBackups = maxBackups;
        this.backupFrequency = backupFrequency;
        this.timerMode = timerMode;
        this.entries = new ArrayList<>(List.of(this.backupEnabled, this.maxBackups, this.backupFrequency, this.timerMode));
    }

    public static int getMaxMaxBackups(BackupType type) {
        if (type == BackupType.AUTOMATED)
            return MAX_AUTOMATED;
        return MAX_COMMAND;
    }

    public int getMaxBackupsForType(BackupType type) {
        if (type == BackupType.AUTOMATED)
            return this.maxBackups.get();
        return getMaxMaxBackups(type);
    }

    public List<ConfigEntry<?>> getEntries() {
        if (!this.locked) {
            this.entries = List.copyOf(this.entries);
            this.locked = true;
        }
        return this.entries;
    }

    public abstract void save() throws Exception;

    protected JsonObject toJson() {
        JsonObject json = new JsonObject();
        for (ConfigEntry<?> entry : this.getEntries())
            if (!entry.needsFallback())
                json.add(entry.name, GSON.toJsonTree(entry.get(), entry.type));
        return json;
    }

    protected void fromJson(JsonObject json) {
        for (ConfigEntry<?> entry : this.getEntries())
            setEntryFromJson(entry, json.get(entry.name));
    }

    private <T> void setEntryFromJson(ConfigEntry<T> entry, JsonElement json) {
        if (json != null)
            entry.set(GSON.fromJson(json, entry.type));
        else
            entry.reset();
    }

    public enum TimerMode {
        DAYLIGHT_CYCLE("daylightCycle"),
        IN_GAME_TIME("inGameTime");
        public final String id;

        TimerMode(String id) {
            this.id = id;
        }

        public String toString() {
            return this.id;
        }
    }

    public static class Adapter <T extends RollbackConfig> implements JsonSerializer<T>, JsonDeserializer<T> {
        private final Class<T> type;

        public Adapter(Class<T> type) {
            this.type = type;
        }

        @Override @Nullable
        public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                T config = this.type.getDeclaredConstructor().newInstance();
                config.fromJson(json.getAsJsonObject());
                return config;
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }

        @Override
        public JsonElement serialize(T object, Type type, JsonSerializationContext context) {
            return object.toJson();
        }
    }
}
