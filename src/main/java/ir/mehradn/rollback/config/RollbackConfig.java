package ir.mehradn.rollback.config;

import com.google.gson.*;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.network.packets.Packets;
import ir.mehradn.rollback.rollback.BackupType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class RollbackConfig {
    public static final int MAX_AUTOMATED = 10;
    public static final int MAX_COMMAND = 99;
    public static final int MAX_FREQUENCY = 100;
    public final ConfigEntry<Boolean> backupEnabled;
    public final ConfigEntry<Short> maxBackups;
    public final ConfigEntry<Short> backupFrequency;
    public final ConfigEntry<TimerMode> timerMode;
    protected List<ConfigEntry<?>> entries;
    private static final Gson GSON = new GsonBuilder().create();
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

    public void copyFrom(RollbackConfig config) {
        this.backupEnabled.copy(config.backupEnabled);
        this.maxBackups.copy(config.maxBackups);
        this.backupFrequency.copy(config.backupFrequency);
        this.timerMode.copy(config.timerMode);
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

        for (int i = 0; i < size; i++) {
            if (!c[i])
                continue;
            ConfigEntry<?> entry = this.getEntries().get(i);
            if (entry.type == Boolean.class)
                buf.writeBoolean((boolean)entry.get());
            else if (entry.type == Short.class)
                buf.writeShort((short)entry.get());
            else if (entry.type.isEnum())
                buf.writeEnum((Enum<?>)entry.get());
            else //noinspection DataFlowIssue
                Assertion.runtime(false);
        }
    }

    protected void readFromBuf(FriendlyByteBuf buf) {
        int size = this.getEntries().size();
        boolean[] c = Packets.readBooleanArray(buf, size);
        for (int i = 0; i < size; i++) {
            if (!c[i])
                continue;
            ConfigEntry<?> entry = this.getEntries().get(i);
            setEntryFromBuf(entry, buf);
        }
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        for (ConfigEntry<?> entry : this.getEntries())
            if (entry.hasValue())
                json.add(entry.name, GSON.toJsonTree(entry.get(), entry.type));
        return json;
    }

    void fromJson(JsonObject json) {
        for (ConfigEntry<?> entry : this.getEntries())
            setEntryFromJson(entry, json.get(entry.name));
    }

    private <T> void setEntryFromJson(ConfigEntry<T> entry, JsonElement json) {
        if (json != null)
            entry.set(GSON.fromJson(json, entry.type));
        else
            entry.reset();
    }

    @SuppressWarnings("unchecked")
    private <T> void setEntryFromBuf(ConfigEntry<T> entry, FriendlyByteBuf buf) {
        if (entry.type == Boolean.class)
            entry.set((T)(Object)buf.readBoolean());
        else if (entry.type == Short.class)
            entry.set((T)(Object)buf.readShort());
        else if (entry.type.isEnum())
            setEnumEntryFromBuf(entry, buf);
        else //noinspection DataFlowIssue
            Assertion.runtime(false);
    }

    @SuppressWarnings("unchecked")
    private <S, T extends Enum<T>> void setEnumEntryFromBuf(ConfigEntry<S> entry, FriendlyByteBuf buf) {
        entry.set((S)buf.readEnum((Class<T>)entry.type));
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
