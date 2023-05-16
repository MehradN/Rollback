package ir.mehradn.rollback.rollback.metadata;

import ir.mehradn.rollback.rollback.BackupManager;

public interface UpdatesAfterLoading {
    void update(BackupManager backupManager);
}
