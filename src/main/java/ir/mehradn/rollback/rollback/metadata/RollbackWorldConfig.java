package ir.mehradn.rollback.rollback.metadata;

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
}
