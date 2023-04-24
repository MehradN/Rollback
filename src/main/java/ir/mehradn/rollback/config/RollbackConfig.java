package ir.mehradn.rollback.config;

import eu.midnightdust.lib.config.MidnightConfig;
import ir.mehradn.rollback.Rollback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class RollbackConfig extends MidnightConfig {
    @Entry(min = 1, max = 10, isSlider = true) public static int backupsPerWorld = 5;
    @Entry(min = 1, max = 15, isSlider = true) public static int backupFrequency = 1;
    @Entry public static TimerMode timerMode = TimerMode.DAYLIGHT_CYCLE;
    @Entry public static boolean replaceReCreateButton = true;
    @Entry public static boolean promptEnabled = true;

    public static void register() {
        MidnightConfig.init(Rollback.MOD_ID, RollbackConfig.class);
    }

    public static int maxBackupsPerWorld() {
        return backupsPerWorld;
    }

    public static int daysPerBackup() {
        return backupFrequency;
    }

    public static int ticksPerBackup() {
        return backupFrequency * 24000;
    }

    public enum TimerMode {
        DAYLIGHT_CYCLE,
        IN_GAME_TIME
    }
}
