package ir.mehradn.rollback.util.mixin;

import ir.mehradn.rollback.rollback.CommonBackupManager;
import ir.mehradn.rollback.rollback.ServerGofer;
import net.minecraft.world.level.storage.LevelStorageSource;

public interface MinecraftServerExpanded {
    LevelStorageSource.LevelStorageAccess getLevelStorageAccess();

    LevelStorageSource getLevelStorageSource();

    CommonBackupManager getBackupManager();

    ServerGofer getGofer();
}
