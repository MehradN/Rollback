package ir.mehradn.rollback.rollback.metadata;

import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.BackupManager;
import net.minecraft.network.FriendlyByteBuf;

public class RollbackWorldConfig extends RollbackConfig implements RollbackMetadata {
    // ConfigEntry.Boolean("backup_enabled", null)
    // ConfigEntry.Short("max_backups", (short)1, MAX_AUTOMATED, null)
    // ConfigEntry.Short("backup_frequency", (short)1, MAX_FREQUENCY, null)
    // ConfigEntry.Enum<>("timer_mode", TimerMode.class, null)

    public RollbackWorldConfig() {
        super(
            new ConfigEntry.Boolean("backup_enabled", null),
            new ConfigEntry.Short("max_backups", (short)1, MAX_AUTOMATED, null),
            new ConfigEntry.Short("backup_frequency", (short)1, MAX_FREQUENCY, null),
            new ConfigEntry.Enum<>("timer_mode", TimerMode.class, null)
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

    @Override
    public void writeToBuf(FriendlyByteBuf buf, boolean integrated) {
        writeToBuf(buf);
    }

    @Override
    public void readFromBuf(FriendlyByteBuf buf) {
        super.readFromBuf(buf);
    }
}
