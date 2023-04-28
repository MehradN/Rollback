package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.rollback.exception.MinecraftException;
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

    public String getLevelID() {
        return ((MinecraftServerExpanded)this.server).getLevelStorageAccess().getLevelId();
    }

    public int getDaysPlayed() {
        ServerLevel level = this.server.overworld();
        return (int)(level == null ? -1 : level.getDayTime() / 24000);
    }

    public Path getBackupDirectory() {
        return ((MinecraftServerExpanded)this.server).getLevelStorageSource().getBackupPath();
    }

    public Path getSaveDirectory() {
        return ((MinecraftServerExpanded)this.server).getLevelStorageSource().getBaseDir();
    }

    public void saveEverything() throws MinecraftException {
        if (!this.server.saveEverything(true, true, true))
            throw new MinecraftException("Failed to save everything on the server!");
    }

    public BackupInfo makeBackup() throws IOException {
        LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerExpanded)this.server).getLevelStorageAccess();
        long size = storageAccess.makeWorldBackup();
        Path path = ((LevelStorageAccessExpanded)storageAccess).getLatestBackupPath();
        return new BackupInfo(path, size);
    }

    public void deleteLevel() throws IOException {
        LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerExpanded)this.server).getLevelStorageAccess();
        storageAccess.deleteLevel();
    }
}
