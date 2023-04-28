package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.rollback.exception.MinecraftException;
import java.io.IOException;
import java.nio.file.Path;

public interface Gofer {
    String getLevelID();

    int getDaysPlayed();

    Path getBackupDirectory();

    Path getSaveDirectory();

    void saveEverything() throws MinecraftException;

    BackupInfo makeBackup() throws IOException;

    void deleteLevel() throws IOException;
}
