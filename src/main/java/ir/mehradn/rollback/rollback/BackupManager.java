package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import org.jetbrains.annotations.NotNull;

public interface BackupManager {
    int MAX_NAME_LENGTH = 32;

    @NotNull RollbackWorld getWorld();

    void loadWorld() throws BackupManagerException;

    void saveWorld() throws BackupManagerException;

    void deleteWorld() throws BackupManagerException;

    void createBackup(String name, BackupType type) throws BackupManagerException;

    void deleteBackup(int backupID, BackupType type) throws BackupManagerException;

    void convertBackup(int backupID, BackupType from, String name, BackupType to) throws BackupManagerException;

    void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException;
}
