package ir.mehradn.rollback.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public final class RollbackConfig {
    public enum BackupMode {
        IN_GAME_DAY,
        REAL_TIME
    }

    public enum CommandAccess {
        ON_CHEATS,
        ALWAYS
    }

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

    public static boolean isAllowedWorldType(GameMode gameMode) {
        int x, y;
        switch (gameMode) {
            case SURVIVAL -> x = 1;
            case ADVENTURE -> x = 2;
            default -> x = 3;
        }
        switch (_RollbackConfig.allowedWorldTypes) {
            case NONE -> y = 0;
            case SURVIVAL -> y = 1;
            case ADVENTURE -> y = 2;
            case ALL_TYPES -> y = 3;
            default -> y = 4;
        }
        return x <= y;
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

    protected static final class _RollbackConfig extends MidnightConfig {
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

        public enum _AllowedWorldTypes {
            NONE,
            SURVIVAL,
            ADVENTURE,
            ALL_TYPES
        }

        @Entry(min = 1, max = 10, isSlider = true)
        public static int backupsPerWorld = 5;
        @Entry
        public static _BackupFrequency backupFrequency = _BackupFrequency.ONE_PER_DAY;
        @Entry
        public static _AllowedWorldTypes allowedWorldTypes = _AllowedWorldTypes.SURVIVAL;
        @Entry
        public static _CommandAccess commandAccess = _CommandAccess.ON_CHEATS;
        @Entry
        public static boolean replaceReCreateButton = true;
    }
}
