package ir.mehradn.rollback.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class RollbackConfig {
    public static void register() {
        MidnightConfig.init("rollback", _RollbackConfig.class);
    }

    public static int getMaxBackupsPerWorld() {
        return _RollbackConfig.backupsPerWorld;
    }

    public static BackupMode backupMode() {
        switch (_RollbackConfig.backupFrequency) {
            case ONE_PER_DAY, TWO_PER_DAY, FOUR_PER_DAY -> {return BackupMode.IN_GAME_DAY;}
            case TWENTY_MINUTES, TEN_MINUTES, FIVE_MINUTES -> {return BackupMode.REAL_TIME;}
        }
        return BackupMode.REAL_TIME;
    }

    public static int ticksPerBackup() {
        switch (_RollbackConfig.backupFrequency) {
            case ONE_PER_DAY, TWENTY_MINUTES -> {return 24000;}
            case TWO_PER_DAY, TEN_MINUTES -> {return 12000;}
            case FOUR_PER_DAY, FIVE_MINUTES -> {return 6000;}
        }
        return 24000;
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

    public static boolean replaceGameRulesButton() {
        return _RollbackConfig.replaceGameRulesButton;
    }

    public enum BackupMode {
        IN_GAME_DAY,
        REAL_TIME
    }

    public enum CommandAccess {
        ON_CHEATS,
        ALWAYS
    }

    // DO NOT USE OUTSIDE OF THIS CLASS
    // I am forced to keep it public.
    public static final class _RollbackConfig extends MidnightConfig {
        public enum _BackupFrequency {
            ONE_PER_DAY,
            TWO_PER_DAY,
            FOUR_PER_DAY,
            TWENTY_MINUTES,
            TEN_MINUTES,
            FIVE_MINUTES
        }

        public enum _CommandAccess {
            ON_CHEATS,
            ALWAYS
        }

        @Entry(min = 1, max = 10, isSlider = true)
        public static int backupsPerWorld = 5;
        @Entry
        public static _BackupFrequency backupFrequency = _BackupFrequency.ONE_PER_DAY;
        @Entry
        public static _CommandAccess commandAccess = _CommandAccess.ON_CHEATS;
        @Entry
        public static boolean replaceReCreateButton = true;
        @Entry
        public static boolean replaceGameRulesButton = true;
    }
}
