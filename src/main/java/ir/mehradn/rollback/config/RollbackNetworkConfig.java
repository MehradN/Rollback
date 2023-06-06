package ir.mehradn.rollback.config;

import ir.mehradn.rollback.rollback.metadata.RollbackMetadata;
import net.minecraft.network.FriendlyByteBuf;

public class RollbackNetworkConfig extends RollbackConfig implements RollbackMetadata {
    // ConfigEntry.Boolean("backupEnabled", false)
    // ConfigEntry.Short("maxBackups", (short)1, MAX_AUTOMATED, (short)5)
    // ConfigEntry.Short("backupFrequency", (short)1, MAX_FREQUENCY, (short)1)
    // ConfigEntry.Enum<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)

    public RollbackNetworkConfig() {
        super(
            new ConfigEntry.Boolean("backupEnabled", false),
            new ConfigEntry.Short("maxBackups", (short)1, MAX_AUTOMATED, (short)5),
            new ConfigEntry.Short("backupFrequency", (short)1, MAX_FREQUENCY, (short)1),
            new ConfigEntry.Enum<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)
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
