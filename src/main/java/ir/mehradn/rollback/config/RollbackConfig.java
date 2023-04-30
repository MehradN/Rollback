package ir.mehradn.rollback.config;

import ir.mehradn.rollback.rollback.BackupType;

// TODO
public class RollbackConfig {
    public static final int MAX_MAX_AUTOMATED_BACKUPS = 10;
    public static final int MAX_MAX_COMMAND_BACKUPS = 99;

    public static int maxBackupsPerWorld(BackupType type) {
        assert type.list;
        assert type.automatedDeletion;
        if (type == BackupType.COMMAND)
            return MAX_MAX_COMMAND_BACKUPS;
        return 5;
    }
}
