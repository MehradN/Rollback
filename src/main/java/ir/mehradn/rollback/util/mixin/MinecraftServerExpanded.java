package ir.mehradn.rollback.util.mixin;

import ir.mehradn.rollback.rollback.CommonBackupManager;
import net.minecraft.world.level.storage.LevelStorageSource;

public interface MinecraftServerExpanded {
    LevelStorageSource.LevelStorageAccess getLevelStorageAccess();

    LevelStorageSource getLevelStorageSource();

    CommonBackupManager getBackupManager();
}
