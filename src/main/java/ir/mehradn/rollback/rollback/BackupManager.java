package ir.mehradn.rollback.rollback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.mehradn.rollback.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.metadata.RollbackBackupType;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import ir.mehradn.rollback.util.gson.LocalDateTimeAdapter;
import ir.mehradn.rollback.util.gson.PathAdapter;
import ir.mehradn.rollback.util.gson.RollbackVersionAdapter;
import java.nio.file.Path;
import java.time.LocalDateTime;

public abstract class BackupManager {
    public static final int MAX_NAME_LENGTH = 32;
    protected static final Gson GSON = new GsonBuilder()
        .registerTypeHierarchyAdapter(Path.class, new PathAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .registerTypeAdapter(RollbackVersion.class, new RollbackVersionAdapter())
        .create();
    public RollbackWorld world;

    public abstract void loadData() throws BackupManagerException;

    public abstract void updateWorld() throws BackupManagerException;

    public abstract void deleteWorld() throws BackupManagerException;

    public abstract void deleteBackup(int backupID, RollbackBackupType type) throws BackupManagerException;

    public abstract void createNormalBackup() throws BackupManagerException;

    public abstract void createSpecialBackup(String name, RollbackBackupType type) throws BackupManagerException;

    public abstract void rollbackToBackup(int backupID, RollbackBackupType type) throws BackupManagerException;
}
