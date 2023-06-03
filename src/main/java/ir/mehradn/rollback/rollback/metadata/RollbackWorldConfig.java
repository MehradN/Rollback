package ir.mehradn.rollback.rollback.metadata;

import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.rollback.BackupManager;
import net.minecraft.network.FriendlyByteBuf;

public class RollbackWorldConfig extends RollbackConfig implements RollbackMetadata {
    // ConfigEntry<>("backupEnabled", Boolean.class, null, null),
    // ConfigEntry<>("maxBackups", Short.class, null, new ConfigEntry.ShortTrimmer(1, MAX_AUTOMATED)),
    // ConfigEntry<>("backupFrequency", Short.class, null, new ConfigEntry.ShortTrimmer(1, MAX_FREQUENCY)),
    // ConfigEntry<>("timerMode", TimerMode.class, null, null)

    public RollbackWorldConfig() {
        super(
            new ConfigEntry<>("backupEnabled", Boolean.class, null, null),
            new ConfigEntry<>("maxBackups", Short.class, null, new ConfigEntry.ShortTrimmer(1, MAX_AUTOMATED)),
            new ConfigEntry<>("backupFrequency", Short.class, null, new ConfigEntry.ShortTrimmer(1, MAX_FREQUENCY)),
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

    @Override
    public void writeToBuf(FriendlyByteBuf buf, boolean integrated) {
        writeToBuf(buf);
    }

    @Override
    public void readFromBuf(FriendlyByteBuf buf) {
        super.readFromBuf(buf);
    }
}
