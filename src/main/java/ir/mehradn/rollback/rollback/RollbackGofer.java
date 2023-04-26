package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.rollback.exception.MinecraftException;
import java.io.IOException;
import java.nio.file.Path;

public interface RollbackGofer {
    int getDaysPlayed();

    Path getSaveDirectory();

    void saveEverything() throws MinecraftException;

    BackupInfo makeBackup() throws IOException;

    void deleteLevel() throws IOException;
}
