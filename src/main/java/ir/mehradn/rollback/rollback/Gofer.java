package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.exception.BackupManagerException;
import java.nio.file.Path;

public interface Gofer {
    String getLevelID();

    int getDaysPlayed();

    Path getBackupDirectory();

    Path getSaveDirectory();

    void saveEverything() throws BackupManagerException;

    BackupInfo makeBackup() throws BackupManagerException;

    void deleteLevel() throws BackupManagerException;
}
