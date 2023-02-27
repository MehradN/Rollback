package ir.mehradn.rollback.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.world.GameMode;

public class RollbackConfig extends MidnightConfig {
    public enum BackupFrequency {
        ONE_PER_DAY,
        TWO_PER_DAY,
        FOUR_PER_DAY,
        TWENTY_MINUTES,
        TEN_MINUTES,
        FIVE_MINUTES
    }

    public enum BackupMode {
        IN_GAME_DAY,
        REAL_TIME
    }

    public enum CommandAccess {
        ON_CHEATS,
        ALWAYS
    }

    public enum AllowedWorldTypes {
        NONE,
        SURVIVAL,
        ADVENTURE,
        ALL_TYPES
    }

    @Entry(min = 1, max = 10, isSlider = true)
    public static int backupsPerWorld = 5;
    @Entry
    public static BackupFrequency backupFrequency = BackupFrequency.ONE_PER_DAY;
    @Entry
    public static AllowedWorldTypes allowedWorldTypes = AllowedWorldTypes.SURVIVAL;
    @Entry
    public static CommandAccess commandAccess = CommandAccess.ON_CHEATS;
    @Entry
    public static boolean replaceReCreateButton = true;

    public static int getMaxBackups() {
        return backupsPerWorld;
    }

    public static BackupMode getBackupMode() {
        return (backupFrequency.ordinal() < 3 ? BackupMode.IN_GAME_DAY : BackupMode.REAL_TIME);
    }

    public static int getBackupTicks() {
        int t = backupFrequency.ordinal() % 3;
        if (t == 0)
            return 24000;
        if (t == 1)
            return 12000;
        return 6000;
    }

    public static boolean isAllowedWorldType(GameMode gameMode) {
        AllowedWorldTypes worldType;
        if (gameMode == GameMode.SURVIVAL)
            worldType = AllowedWorldTypes.SURVIVAL;
        else if (gameMode == GameMode.ADVENTURE)
            worldType = AllowedWorldTypes.ADVENTURE;
        else
            worldType = AllowedWorldTypes.ALL_TYPES;

        return (worldType.ordinal() <= allowedWorldTypes.ordinal());
    }

    public static CommandAccess getCommandAccess() {
        return commandAccess;
    }

    public static boolean getReplaceReCreateButton() {
        return replaceReCreateButton;
    }
}
