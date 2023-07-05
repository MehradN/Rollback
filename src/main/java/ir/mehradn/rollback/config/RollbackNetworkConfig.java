package ir.mehradn.rollback.config;

import ir.mehradn.mehradconfig.entry.BooleanEntry;
import ir.mehradn.mehradconfig.entry.EnumEntry;
import ir.mehradn.mehradconfig.entry.NumberEntry;
import ir.mehradn.rollback.rollback.metadata.RollbackMetadata;
import net.minecraft.network.FriendlyByteBuf;

public class RollbackNetworkConfig extends RollbackConfig implements RollbackMetadata {
    // BooleanEntry("backupEnabled", false)
    // NumberEntry("maxBackups", 1, MAX_AUTOMATED, 5)
    // NumberEntry("backupFrequency", 1, MAX_FREQUENCY, 1)
    // EnumEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)

    public RollbackNetworkConfig() {
        super(
            RollbackNetworkConfig::new,
            new BooleanEntry("backupEnabled", false),
            new NumberEntry("maxBackups", 1, MAX_AUTOMATED, 5),
            new NumberEntry("backupFrequency", 1, MAX_FREQUENCY, 1),
            new EnumEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)
        );
    }

    @Override
    public void writeToBuf(FriendlyByteBuf buf, boolean integrated) {
        super.writeToBuf(buf);
    }
}
