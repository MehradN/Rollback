package ir.mehradn.rollback.rollback.metadata;

import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.BackupManager;
import ir.mehradn.rollback.rollback.exception.BackupManagerException;

public class RollbackWorldConfig extends RollbackConfig {
    // backupEnabled = new ConfigEntry<>("backupEnabled", Boolean.class, null, DEFAULT.backupEnabled, null),
    // maxBackups = new ConfigEntry<>("maxBackups", Integer.class, null, DEFAULT.maxBackups, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED)),
    // backupFrequency = new ConfigEntry<>("backupFrequency", Integer.class, null, DEFAULT.backupFrequency, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY)),
    // timerMode = new ConfigEntry<>("timerMode", TimerMode.class, null, DEFAULT.timerMode, null)
    private BackupManager backupManager = null;

    public RollbackWorldConfig() {
        super(
            new ConfigEntry<>("backupEnabled", Boolean.class, null, DEFAULT.backupEnabled, null),
            new ConfigEntry<>("maxBackups", Integer.class, null, DEFAULT.maxBackups, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED)),
            new ConfigEntry<>("backupFrequency", Integer.class, null, DEFAULT.backupFrequency, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY)),
            new ConfigEntry<>("timerMode", TimerMode.class, null, DEFAULT.timerMode, null)
        );
    }

    public void setBackupManager(BackupManager backupManager) {
        this.backupManager = backupManager;
    }

    public void save() throws BackupManagerException {
        assert this.backupManager != null;
        this.backupManager.saveWorld();
    }
}
