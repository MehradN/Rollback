package ir.mehradn.rollback.util.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public interface LevelStorageAccessExpanded {
    Path getLatestBackupPath();
}
