package ir.mehradn.rollback.config;

import ir.mehradn.mehradconfig.MehradConfig;
import ir.mehradn.mehradconfig.entry.ConfigEntry;
import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class RollbackConfig extends MehradConfig {
    protected static final short MAX_AUTOMATED = 10;
    protected static final short MAX_COMMAND = 99;
    protected static final short MAX_FREQUENCY = 100;
    public final ConfigEntry<Boolean> backupEnabled;
    public final ConfigEntry<Integer> maxBackups;
    public final ConfigEntry<Integer> backupFrequency;
    public final ConfigEntry<TimerMode> timerMode;
    protected List<ConfigEntry<?>> entries;
    private final Supplier<RollbackConfig> constructor;
    private boolean locked = false;

    protected RollbackConfig(Supplier<RollbackConfig> constructor, ConfigEntry<Boolean> backupEnabled, ConfigEntry<Integer> maxBackups,
                             ConfigEntry<Integer> backupFrequency, ConfigEntry<TimerMode> timerMode) {
        super(Rollback.MOD_ID);
        this.constructor = constructor;
        this.backupEnabled = backupEnabled;
        this.maxBackups = maxBackups;
        this.backupFrequency = backupFrequency;
        this.timerMode = timerMode;
        this.entries = new ArrayList<>(List.of(this.backupEnabled, this.maxBackups, this.backupFrequency, this.timerMode));
    }

    protected RollbackConfig(String name, Supplier<RollbackConfig> constructor, ConfigEntry<Boolean> backupEnabled, ConfigEntry<Integer> maxBackups,
                             ConfigEntry<Integer> backupFrequency, ConfigEntry<TimerMode> timerMode) {
        super(Rollback.MOD_ID, name);
        this.constructor = constructor;
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

    @Override
    public List<ConfigEntry<?>> getEntries() {
        if (!this.locked) {
            this.entries = List.copyOf(this.entries);
            this.locked = true;
        }
        return this.entries;
    }

    @Override
    public MehradConfig createNewInstance() {
        return this.constructor.get();
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
}