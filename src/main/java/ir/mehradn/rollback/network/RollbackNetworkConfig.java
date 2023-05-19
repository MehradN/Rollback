package ir.mehradn.rollback.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;

public class RollbackNetworkConfig extends RollbackConfig {
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(RollbackNetworkConfig.class, new Adapter<>(RollbackNetworkConfig.class))
        .create();
    // ConfigEntry<>("backupEnabled", Boolean.class, false, null),
    // ConfigEntry<>("maxBackups", Integer.class, 5, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED)),
    // ConfigEntry<>("backupFrequency", Integer.class, 1, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY)),
    // ConfigEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE, null)

    public RollbackNetworkConfig() {
        super(
            new ConfigEntry<>("backupEnabled", Boolean.class, false, null),
            new ConfigEntry<>("maxBackups", Integer.class, 5, new ConfigEntry.IntegerTrimmer(1, MAX_AUTOMATED)),
            new ConfigEntry<>("backupFrequency", Integer.class, 1, new ConfigEntry.IntegerTrimmer(1, MAX_FREQUENCY)),
            new ConfigEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE, null)
        );
    }

    public static RollbackNetworkConfig fromJson(JsonElement json) {
        return GSON.fromJson(json, RollbackNetworkConfig.class);
    }

    public JsonElement toJson() {
        return GSON.toJsonTree(this, RollbackNetworkConfig.class);
    }
}
