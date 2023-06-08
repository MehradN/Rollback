package ir.mehradn.rollback.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

@Environment(EnvType.CLIENT)
public class RollbackClientConfig extends RollbackDefaultConfig {
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(RollbackClientConfig.class, new Adapter<>(RollbackClientConfig.class))
        .setPrettyPrinting().create();
    // ConfigEntry.Boolean("backupEnabled", false)
    // ConfigEntry.Short("maxBackups", (short)1, MAX_AUTOMATED, (short)5)
    // ConfigEntry.Short("backupFrequency", (short)1, MAX_FREQUENCY, (short)1)
    // ConfigEntry.Enum<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)
    public final ConfigEntry.Boolean replaceButton = new ConfigEntry.Boolean("replaceButton", true);
    public final ConfigEntry.Boolean promptEnabled = new ConfigEntry.Boolean("promptEnabled", true);
    public final ConfigEntry.Boolean commandAccess = new ConfigEntry.Boolean("commandAccess", false);

    public RollbackClientConfig() {
        super();
        this.entries.add(this.replaceButton);
        this.entries.add(this.promptEnabled);
        this.entries.add(this.commandAccess);
    }

    public static RollbackClientConfig load() {
        return load(GSON, RollbackClientConfig.class, RollbackClientConfig::new);
    }

    public void copyFrom(RollbackClientConfig config) {
        super.copyFrom(config);
        this.replaceButton.copyFrom(config.replaceButton);
        this.promptEnabled.copyFrom(config.promptEnabled);
        this.commandAccess.copyFrom(config.commandAccess);
    }

    @Override
    public boolean hasCommandPermission(ServerPlayer player) {
        if (super.hasCommandPermission(player))
            return true;

        if (!this.commandAccess.get())
            return false;
        String hostUUID = Minecraft.getInstance().getUser().getUuid();
        return player.getUUID().toString().equals(hostUUID);
    }

    @Override
    protected Gson getGson() {
        return GSON;
    }
}
