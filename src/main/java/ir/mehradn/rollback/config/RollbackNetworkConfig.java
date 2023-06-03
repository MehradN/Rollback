package ir.mehradn.rollback.config;

import ir.mehradn.rollback.rollback.metadata.RollbackMetadata;
import net.minecraft.network.FriendlyByteBuf;

public class RollbackNetworkConfig extends RollbackConfig implements RollbackMetadata {
    // ConfigEntry<>("backupEnabled", Boolean.class, false, null),
    // ConfigEntry<>("maxBackups", Short.class, (short)5, new ConfigEntry.ShortTrimmer(1, MAX_AUTOMATED)),
    // ConfigEntry<>("backupFrequency", Short.class, (short)1, new ConfigEntry.ShortTrimmer(1, MAX_FREQUENCY)),
    // ConfigEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE, null)

    public RollbackNetworkConfig() {
        super(
            new ConfigEntry<>("backupEnabled", Boolean.class, false, null),
            new ConfigEntry<>("maxBackups", Short.class, (short)5, new ConfigEntry.ShortTrimmer(1, MAX_AUTOMATED)),
            new ConfigEntry<>("backupFrequency", Short.class, (short)1, new ConfigEntry.ShortTrimmer(1, MAX_FREQUENCY)),
            new ConfigEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE, null)
        );
    }

    @Override
    public void writeToBuf(FriendlyByteBuf buf, boolean integrated) {
        super.writeToBuf(buf);
    }

    @Override
    public void readFromBuf(FriendlyByteBuf buf) {
        super.readFromBuf(buf);
    }
}
