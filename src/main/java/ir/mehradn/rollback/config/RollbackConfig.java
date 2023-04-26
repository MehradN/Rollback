package ir.mehradn.rollback.config;

import ir.mehradn.rollback.rollback.metadata.RollbackBackupType;

// TODO
public class RollbackConfig {
    public static int maxBackupsPerWorld(RollbackBackupType type) {
        return 5;
    }
}
