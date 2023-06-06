package ir.mehradn.rollback.config;

import com.google.gson.*;
import ir.mehradn.rollback.network.packets.Packets;
import ir.mehradn.rollback.rollback.BackupType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class RollbackConfig {
    public final ConfigEntry<Boolean> backupEnabled;
    public final ConfigEntry<Short> maxBackups;
    public final ConfigEntry<Short> backupFrequency;
    public final ConfigEntry<TimerMode> timerMode;
    protected static final short MAX_AUTOMATED = 10;
    protected static final short MAX_COMMAND = 99;
    protected static final short MAX_FREQUENCY = 100;
    protected List<ConfigEntry<?>> entries;
    private boolean locked = false;

    protected RollbackConfig(ConfigEntry<Boolean> backupEnabled, ConfigEntry<Short> maxBackups, ConfigEntry<Short> backupFrequency,
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

    public void mergeFrom(RollbackConfig config) {
        this.backupEnabled.mergeFrom(config.backupEnabled);
        this.maxBackups.mergeFrom(config.maxBackups);
        this.backupFrequency.mergeFrom(config.backupFrequency);
        this.timerMode.mergeFrom(config.timerMode);
    }

    public List<ConfigEntry<?>> getEntries() {
        if (!this.locked) {
            this.entries = List.copyOf(this.entries);
            this.locked = true;
        }
        return this.entries;
    }

    protected void writeToBuf(FriendlyByteBuf buf) {
        int size = this.getEntries().size();
        boolean[] c = new boolean[size];

        for (int i = 0; i < size; i++)
            c[i] = this.getEntries().get(i).hasValue();
        Packets.writeBooleanArray(buf, c);

        for (int i = 0; i < size; i++)
            if (c[i])
                getEntries().get(i).writeToBuf(buf);
    }

    protected void readFromBuf(FriendlyByteBuf buf) {
        int size = this.getEntries().size();
        boolean[] c = Packets.readBooleanArray(buf, size);

        for (int i = 0; i < size; i++)
            if (c[i])
                getEntries().get(i).readFromBuf(buf);
    }

    protected JsonObject toJson() {
        JsonObject json = new JsonObject();
        for (ConfigEntry<?> entry : this.getEntries())
            if (entry.hasValue())
                json.add(entry.name, entry.toJson());
        return json;
    }

    protected void fromJson(JsonObject json) {
        for (ConfigEntry<?> entry : this.getEntries()) {
            JsonElement obj = json.get(entry.name);
            if (obj == null)
                entry.reset();
            else
                entry.fromJson(obj);
        }
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
