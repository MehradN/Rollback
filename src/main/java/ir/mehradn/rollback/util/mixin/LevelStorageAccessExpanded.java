package ir.mehradn.rollback.util.mixin;

import net.minecraft.world.level.storage.LevelStorageSource;
import java.nio.file.Path;

public interface LevelStorageAccessExpanded {
    LevelStorageSource getSource();

    Path getLatestBackupPath();
}
