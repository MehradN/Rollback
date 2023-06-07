package ir.mehradn.rollback.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RollbackClientConfig extends RollbackDefaultConfig {
    // ConfigEntry.Boolean("backupEnabled", false)
    // ConfigEntry.Short("maxBackups", (short)1, MAX_AUTOMATED, (short)5)
    // ConfigEntry.Short("backupFrequency", (short)1, MAX_FREQUENCY, (short)1)
    // ConfigEntry.Enum<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)
    public final ConfigEntry.Boolean replaceButton = new ConfigEntry.Boolean("replaceButton", true);
    public final ConfigEntry.Boolean promptEnabled = new ConfigEntry.Boolean("promptEnabled", true);
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(RollbackClientConfig.class, new Adapter<>(RollbackClientConfig.class))
        .setPrettyPrinting().create();

    public RollbackClientConfig() {
        super();
        this.entries.add(this.replaceButton);
        this.entries.add(this.promptEnabled);
    }

    public static RollbackClientConfig load() {
        return load(GSON, RollbackClientConfig.class, RollbackClientConfig::new);
    }

    public void copyFrom(RollbackClientConfig config) {
        super.copyFrom(config);
        this.replaceButton.copyFrom(config.replaceButton);
        this.promptEnabled.copyFrom(config.promptEnabled);
    }

    @Override
    protected Gson getGson() {
        return GSON;
    }
}
