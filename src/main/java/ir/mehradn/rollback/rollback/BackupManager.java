package ir.mehradn.rollback.rollback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import ir.mehradn.rollback.rollback.metadata.RollbackWorldConfig;
import ir.mehradn.rollback.util.gson.LocalDateTimeAdapter;
import ir.mehradn.rollback.util.gson.PathAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;
import java.time.LocalDateTime;

public interface BackupManager {
    int MAX_NAME_LENGTH = 32;
    Gson GSON = new GsonBuilder()
        .registerTypeHierarchyAdapter(Path.class, new PathAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .registerTypeAdapter(RollbackVersion.class, new RollbackVersion.Adapter())
        .registerTypeAdapter(RollbackWorldConfig.class, new RollbackConfig.Adapter<>(RollbackWorldConfig.class))
        .create();

    @NotNull State getCurrentState();

    @NotNull RollbackWorld getWorld();

    @NotNull RollbackDefaultConfig getDefaultConfig();

    void loadWorld() throws BackupManagerException;

    void deleteWorld() throws BackupManagerException;

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
        LOADING
    }
}
