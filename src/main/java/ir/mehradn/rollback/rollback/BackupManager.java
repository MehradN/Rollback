package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BackupManager {
    int MAX_NAME_LENGTH = 32;

    @NotNull State getCurrentState();

    @NotNull RollbackWorld getWorld();

    @NotNull RollbackConfig getDefaultConfig();

    void loadWorld() throws BackupManagerException;

    void createBackup(BackupType type, @Nullable String name) throws BackupManagerException;

    void deleteBackup(int backupID, BackupType type) throws BackupManagerException;

    void renameBackup(int backupID, BackupType type, @Nullable String name) throws BackupManagerException;

    void convertBackup(int backupID, BackupType from, BackupType to) throws BackupManagerException;

    void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException;

    void saveConfig() throws BackupManagerException;

    void saveConfigAsDefault() throws BackupManagerException;

    enum State {
        INITIAL,
        IDLE,
        LOADING,
        ACTION
    }
}
