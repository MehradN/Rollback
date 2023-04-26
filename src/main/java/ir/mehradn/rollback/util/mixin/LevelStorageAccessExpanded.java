package ir.mehradn.rollback.util.mixin;

import java.nio.file.Path;

public interface LevelStorageAccessExpanded {
    Path getLatestBackupPath();
}
