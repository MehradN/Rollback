package ir.mehradn.rollback.rollback.metadata;

import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.BackupManager;

public class RollbackWorldConfig extends RollbackConfig implements UpdatesAfterLoading {
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

    @Override
    public void update(BackupManager backupManager) {
        RollbackConfig defaultConfig = backupManager.getDefaultConfig();
        this.backupEnabled.setFallback(defaultConfig.backupEnabled);
        this.maxBackups.setFallback(defaultConfig.maxBackups);
        this.backupFrequency.setFallback(defaultConfig.backupFrequency);
        this.timerMode.setFallback(defaultConfig.timerMode);
    }
}
