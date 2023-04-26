package ir.mehradn.rollback.rollback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.mehradn.rollback.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.metadata.RollbackBackupType;
import ir.mehradn.rollback.rollback.metadata.RollbackData;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import ir.mehradn.rollback.util.gson.LocalDateTimeAdapter;
import ir.mehradn.rollback.util.gson.PathAdapter;
import ir.mehradn.rollback.util.gson.RollbackVersionAdapter;
import java.nio.file.Path;
import java.time.LocalDateTime;

public abstract class BackupManager {
    protected static final Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Path.class, new PathAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .registerTypeAdapter(RollbackVersion.class, new RollbackVersionAdapter())
        .create();
    public RollbackData data;

    public abstract void loadData() throws BackupManagerException;

    public abstract void updateWorld(String levelID, RollbackWorld world) throws BackupManagerException;

    public abstract void deleteWorld(String levelID) throws BackupManagerException;

    public abstract void deleteBackup(String levelID, int backupID, RollbackBackupType type) throws BackupManagerException;

    public abstract void createNormalBackup(String name) throws BackupManagerException;

    public abstract void createSpecialBackup(String levelID, String name, RollbackBackupType type) throws BackupManagerException;

    public abstract void rollbackToBackup(String levelID, int backupID, RollbackBackupType type) throws BackupManagerException;
}
