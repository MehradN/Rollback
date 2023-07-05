package ir.mehradn.rollback.rollback.metadata;

import com.google.gson.*;
import ir.mehradn.mehradconfig.MehradConfig;
import ir.mehradn.mehradconfig.entry.BooleanEntry;
import ir.mehradn.mehradconfig.entry.EnumEntry;
import ir.mehradn.mehradconfig.entry.NumberEntry;
import ir.mehradn.mehradconfig.entry.OptionalEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.BackupManager;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Type;

public class RollbackWorldConfig extends RollbackConfig implements RollbackMetadata {
    // BooleanEntry("backupEnabled", false).makeOptional()
    // NumberEntry("maxBackups", 1, MAX_AUTOMATED, 5).makeOptional()
    // NumberEntry("backupFrequency", 1, MAX_FREQUENCY, 1).makeOptional()
    // EnumEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE).makeOptional()
    private static final String CONFIG_NAME = "rollbackWorldOptions";
    private RollbackConfig defaultConfig = null;

    public RollbackWorldConfig() {
        super(
            CONFIG_NAME,
            RollbackWorldConfig::new,
            new BooleanEntry("backupEnabled", false).makeOptional(),
            new NumberEntry("maxBackups", 1, MAX_AUTOMATED, 5).makeOptional(),
            new NumberEntry("backupFrequency", 1, MAX_FREQUENCY, 1).makeOptional(),
            new EnumEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE).makeOptional()
        );
    }

    public RollbackWorldConfig(@NotNull RollbackConfig defaultConfig) {
        super(
            CONFIG_NAME,
            RollbackWorldConfig::new,
            new BooleanEntry("backupEnabled", false).makeOptional(defaultConfig.backupEnabled),
            new NumberEntry("maxBackups", 1, MAX_AUTOMATED, 5).makeOptional(defaultConfig.maxBackups),
            new NumberEntry("backupFrequency", 1, MAX_FREQUENCY, 1).makeOptional(defaultConfig.backupFrequency),
            new EnumEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE).makeOptional(defaultConfig.timerMode)
        );
        this.defaultConfig = defaultConfig;
    }

    public void mergeTo(RollbackConfig config) {
        ((OptionalEntry<Boolean>)this.backupEnabled).mergeTo(config.backupEnabled);
        ((OptionalEntry<Integer>)this.maxBackups).mergeTo(config.maxBackups);
        ((OptionalEntry<Integer>)this.backupFrequency).mergeTo(config.backupFrequency);
        ((OptionalEntry<TimerMode>)this.timerMode).mergeTo(config.timerMode);
    }

    @Override
    public MehradConfig createNewInstance() {
        if (this.defaultConfig == null)
            return super.createNewInstance();
        return new RollbackWorldConfig(this.defaultConfig);
    }

    @Override
    public void update(BackupManager backupManager) {
        RollbackConfig defaultConfig = backupManager.getDefaultConfig();
        ((OptionalEntry<Boolean>)this.backupEnabled).setFallbackEntry(defaultConfig.backupEnabled);
        ((OptionalEntry<Integer>)this.maxBackups).setFallbackEntry(defaultConfig.maxBackups);
        ((OptionalEntry<Integer>)this.backupFrequency).setFallbackEntry(defaultConfig.backupFrequency);
        ((OptionalEntry<TimerMode>)this.timerMode).setFallbackEntry(defaultConfig.timerMode);
        this.defaultConfig = defaultConfig;
    }

    @Override
    public void writeToBuf(FriendlyByteBuf buf, boolean integrated) {
        writeToBuf(buf);
    }

    public static class Adapter implements JsonSerializer<RollbackWorldConfig>, JsonDeserializer<RollbackWorldConfig> {
        @Override
        public JsonElement serialize(RollbackWorldConfig object, Type type, JsonSerializationContext context) {
            return object.toJson();
        }

        @Override
        public RollbackWorldConfig deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            RollbackWorldConfig config = new RollbackWorldConfig();
            config.fromJson(json.getAsJsonObject());
            return config;
        }
    }
}
