package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.exception.BMECause;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.util.mixin.LevelStorageAccessExpanded;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import java.io.IOException;
import java.nio.file.Path;

public class ServerGofer implements Gofer {
    private final MinecraftServer server;

    public ServerGofer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public String getLevelID() {
        return ((MinecraftServerExpanded)this.server).getLevelStorageAccess().getLevelId();
    }

    @Override
    public int getDaysPlayed() {
        ServerLevel level = this.server.overworld();
        return (int)(level.getDayTime() / 24000);
    }

    @Override
    public Path getBackupDirectory() {
        return ((MinecraftServerExpanded)this.server).getLevelStorageSource().getBackupPath();
    }

    @Override
    public Path getSaveDirectory() {
        return ((MinecraftServerExpanded)this.server).getLevelStorageSource().getBaseDir();
    }

    @Override
    public void saveEverything() throws BackupManagerException {
        if (!this.server.saveEverything(true, true, true))
            throw new BackupManagerException(BMECause.MINECRAFT_FAILURE, "Failed to save everything on the server!");
    }

    @Override
    public BackupInfo makeBackup() throws BackupManagerException {
        LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerExpanded)this.server).getLevelStorageAccess();
        try {
            long size = storageAccess.makeWorldBackup();
            Path path = ((LevelStorageAccessExpanded)storageAccess).getLatestBackupPath();
            return new BackupInfo(path, size);
        } catch (IOException e) {
            throw new BackupManagerException(BMECause.IO_EXCEPTION, "Failed to create a backup!", e);
        }
    }

    @Override
    public void deleteLevel() throws BackupManagerException {
        LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerExpanded)this.server).getLevelStorageAccess();
        try {
            storageAccess.deleteLevel();
        } catch (IOException e) {
            throw new BackupManagerException(BMECause.IO_EXCEPTION, "Failed to delete the level", e);
        }
    }
}
