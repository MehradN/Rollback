package ir.mehradn.rollback.config;

import ir.mehradn.mehradconfig.entry.BooleanEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

@Environment(EnvType.CLIENT)
public class RollbackClientConfig extends RollbackDefaultConfig {
    // BooleanEntry("backupEnabled", false)
    // NumberEntry("maxBackups", 1, MAX_AUTOMATED, 5)
    // NumberEntry("backupFrequency", 1, MAX_FREQUENCY, 1)
    // EnumEntry<>("timerMode", TimerMode.class, TimerMode.DAYLIGHT_CYCLE)
    public final BooleanEntry replaceButton = new BooleanEntry("replaceButton", true);
    public final BooleanEntry promptEnabled = new BooleanEntry("promptEnabled", true);
    public final BooleanEntry commandAccess = new BooleanEntry("commandAccess", false);

    public RollbackClientConfig() {
        super(RollbackClientConfig::new);
        this.entries.add(this.replaceButton);
        this.entries.add(this.promptEnabled);
        this.entries.add(this.commandAccess);
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
}
