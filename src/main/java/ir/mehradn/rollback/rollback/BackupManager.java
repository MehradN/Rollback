package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;

public abstract class BackupManager {
    public static final int MAX_NAME_LENGTH = 32;
    public RollbackWorld world;

    public abstract void loadWorld() throws BackupManagerException;

    public abstract void saveWorld() throws BackupManagerException;

    public abstract void deleteWorld() throws BackupManagerException;

    public abstract void createBackup(String name, BackupType type) throws BackupManagerException;

    public abstract void deleteBackup(int backupID, BackupType type) throws BackupManagerException;

    public abstract void convertBackup(int backupID, BackupType from, String name, BackupType to) throws BackupManagerException;

    public abstract void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException;
}
