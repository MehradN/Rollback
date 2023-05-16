package ir.mehradn.rollback.util.mixin;

import ir.mehradn.rollback.rollback.ServerBackupManager;
import net.minecraft.world.level.storage.LevelStorageSource;

public interface MinecraftServerExpanded {
    LevelStorageSource.LevelStorageAccess getLevelStorageAccess();

    LevelStorageSource getLevelStorageSource();

    ServerBackupManager getBackupManager();
}
