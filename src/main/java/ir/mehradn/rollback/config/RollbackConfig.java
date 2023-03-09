package ir.mehradn.rollback.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class RollbackConfig {
    public static void register() {
        MidnightConfig.init("rollback", _RollbackConfig.class);
    }

    public static int maxBackupsPerWorld() {
        return _RollbackConfig.backupsPerWorld;
    }

    public static int daysPerBackup() {
        return _RollbackConfig.backupFrequency;
    }

    public static int ticksPerBackup() {
        return _RollbackConfig.backupFrequency * 24000;
    }

    public static TimerMode timerMode() {
        switch (_RollbackConfig.timerMode) {
            case DAYLIGHT_CYCLE -> {return TimerMode.DAYLIGHT_CYCLE;}
            case IN_GAME_TIME -> {return TimerMode.IN_GAME_TIME;}
        }
        return TimerMode.IN_GAME_TIME;
    }

    public static CommandAccess commandAccess() {
        switch (_RollbackConfig.commandAccess) {
            case ON_CHEATS -> {return CommandAccess.ON_CHEATS;}
            case ALWAYS -> {return CommandAccess.ALWAYS;}
        }
        return CommandAccess.ON_CHEATS;
    }

    public static boolean replaceReCreateButton() {
        return _RollbackConfig.replaceReCreateButton;
    }

    public static boolean promptDisabled() {
        return !_RollbackConfig.promptEnabled;
    }

    public enum TimerMode {
        DAYLIGHT_CYCLE,
        IN_GAME_TIME
    }

    public enum CommandAccess {
        ON_CHEATS,
        ALWAYS
    }

    // DO NOT USE OUTSIDE OF THIS CLASS
    // I am forced to keep it public.
    public static final class _RollbackConfig extends MidnightConfig {
        public enum _TimerMode {
            DAYLIGHT_CYCLE,
            IN_GAME_TIME
        }

        public enum _CommandAccess {
            ON_CHEATS,
            ALWAYS
        }

        @Entry(min = 1, max = 10, isSlider = true)
        public static int backupsPerWorld = 5;
        @Entry(min = 1, max = 15, isSlider = true)
        public static int backupFrequency = 1;
        @Entry
        public static _TimerMode timerMode = _TimerMode.DAYLIGHT_CYCLE;
        @Entry
        public static _CommandAccess commandAccess = _CommandAccess.ON_CHEATS;
        @Entry
        public static boolean replaceReCreateButton = true;
        @Entry
        public static boolean promptEnabled = true;
    }
}
