package ir.mehradn.rollback.util.mixin;

import ir.mehradn.rollback.util.backup.BackupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.storage.LevelStorage;

@Environment(EnvType.CLIENT)
public interface MinecraftServerExpanded {
    LevelStorage.Session getSession();

    BackupManager getBackupManager();
}
