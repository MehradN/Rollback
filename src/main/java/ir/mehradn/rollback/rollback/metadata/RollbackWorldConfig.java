package ir.mehradn.rollback.rollback.metadata;

import ir.mehradn.rollback.command.argument.BooleanArgument;
import ir.mehradn.rollback.command.argument.CommandArgument;
import ir.mehradn.rollback.command.argument.EnumArgument;
import ir.mehradn.rollback.command.argument.IntegerArgument;
import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.config.RollbackDefaultConfig;

public class RollbackWorldConfig extends RollbackConfig {
    // ConfigEntry<>("backupEnabled", Boolean.class, null, null),
    // ConfigEntry<>("maxBackups", Integer.class, null, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED)),
    // ConfigEntry<>("backupFrequency", Integer.class, null, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY)),
    // ConfigEntry<>("timerMode", TimerMode.class, null, null)

    public RollbackWorldConfig() {
        super(
            new ConfigEntry<>("backupEnabled", Boolean.class, null, null),
            new ConfigEntry<>("maxBackups", Integer.class, null, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED)),
            new ConfigEntry<>("backupFrequency", Integer.class, null, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY)),
            new ConfigEntry<>("timerMode", TimerMode.class, null, null)
        );
    }

    public void setDefaultConfig(RollbackDefaultConfig defaultConfig) {
        this.backupEnabled.setFallback(defaultConfig.backupEnabled);
        this.maxBackups.setFallback(defaultConfig.maxBackups);
        this.backupFrequency.setFallback(defaultConfig.backupFrequency);
        this.timerMode.setFallback(defaultConfig.timerMode);
    }

    public void forceCopyFrom(RollbackConfig config) {
        this.backupEnabled.forceCopy(config.backupEnabled);
        this.maxBackups.forceCopy(config.maxBackups);
        this.backupFrequency.forceCopy(config.backupFrequency);
        this.timerMode.forceCopy(config.timerMode);
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigEntry<T> getEntry(Entry entry) {
        return switch (entry) {
            case BACKUP_ENABLED -> (ConfigEntry<T>)this.backupEnabled;
            case MAX_BACKUPS -> (ConfigEntry<T>)this.maxBackups;
            case BACKUP_FREQUENCY -> (ConfigEntry<T>)this.backupFrequency;
            case TIMER_MODE -> (ConfigEntry<T>)this.timerMode;
        };
    }

    public enum Entry {
        BACKUP_ENABLED("backupEnabled", new BooleanArgument()),
        MAX_BACKUPS("maxBackups", new IntegerArgument(1, MAX_AUTOMATED)),
        BACKUP_FREQUENCY("backupFrequency", new IntegerArgument(1, MAX_FREQUENCY)),
        TIMER_MODE("timerMode", new EnumArgument<>(TimerMode.class));
        public final String name;
        public final CommandArgument<?> commandArgument;

        Entry(String name, CommandArgument<?> commandArgument) {
            this.name = name;
            this.commandArgument = commandArgument;
        }

        public String toString() {
            return this.name;
        }
    }
}
