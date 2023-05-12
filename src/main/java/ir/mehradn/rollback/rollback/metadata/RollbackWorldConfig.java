package ir.mehradn.rollback.rollback.metadata;

import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.BackupManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RollbackWorldConfig extends RollbackConfig {
    // ConfigEntry<>("backupEnabled", Boolean.class, DEFAULT.backupEnabled, null),
    // ConfigEntry<>("maxBackups", Integer.class, DEFAULT.maxBackups, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED)),
    // ConfigEntry<>("backupFrequency", Integer.class, DEFAULT.backupFrequency, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY)),
    // ConfigEntry<>("timerMode", TimerMode.class, DEFAULT.timerMode, null)
    @Nullable private BackupManager backupManager = null;

    public RollbackWorldConfig() {
        super(
            new ConfigEntry<>("backupEnabled", Boolean.class, DEFAULT.backupEnabled, null),
            new ConfigEntry<>("maxBackups", Integer.class, DEFAULT.maxBackups, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED)),
            new ConfigEntry<>("backupFrequency", Integer.class, DEFAULT.backupFrequency, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY)),
            new ConfigEntry<>("timerMode", TimerMode.class, DEFAULT.timerMode, null)
        );
    }

    public void setBackupManager(@NotNull BackupManager backupManager) {
        this.backupManager = backupManager;
    }

    @Override
    public void save() throws BackupManagerException {
        Assertion.state(this.backupManager != null, "Call setBackupManager before this!");
        this.backupManager.saveWorld();
    }
}
