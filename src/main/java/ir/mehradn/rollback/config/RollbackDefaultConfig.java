package ir.mehradn.rollback.config;

import com.google.gson.JsonObject;
import ir.mehradn.mehradconfig.entry.BooleanEntry;
import ir.mehradn.mehradconfig.entry.EnumEntry;
import ir.mehradn.mehradconfig.entry.NumberEntry;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Supplier;

public class RollbackDefaultConfig extends RollbackConfig {
    // BooleanEntry("backupEnabled", false)
    // NumberEntry("maxBackups", 1, MAX_AUTOMATED, 5)
    // NumberEntry("backupFrequency", 1, MAX_FREQUENCY, 1)
    // EnumEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)

    public RollbackDefaultConfig() {
        this(RollbackDefaultConfig::new);
    }

    protected RollbackDefaultConfig(Supplier<RollbackConfig> constructor) {
        super(
            constructor,
            new BooleanEntry("backupEnabled", false),
            new NumberEntry("maxBackups", 1, MAX_AUTOMATED, 5),
            new NumberEntry("backupFrequency", 1, MAX_FREQUENCY, 1),
            new EnumEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)
        );
    }

    public boolean hasCommandPermission(ServerPlayer player) {
        return player.hasPermissions(4);
    }

    @Override
    public void fromJson(JsonObject json) {
        ConfigUpdater configUpdater = new ConfigUpdater(json);
        configUpdater.update();
        super.fromJson(json);
    }
}
