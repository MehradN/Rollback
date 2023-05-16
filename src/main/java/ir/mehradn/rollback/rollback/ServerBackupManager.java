package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.ConfigType;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BMECause;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.util.mixin.LevelStorageAccessExpanded;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import java.io.IOException;
import java.nio.file.Path;

public final class ServerBackupManager extends CommonBackupManager {
    private final MinecraftServer server;

    public ServerBackupManager(MinecraftServer server) {
        super();
        this.server = server;
    }

    @Override
    public void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException {
        //noinspection DataFlowIssue
        Assertion.runtime(false, "Rollback is not implemented for servers");
    }

    @Override
    protected Path getBackupDirectory() {
        return ((MinecraftServerExpanded)this.server).getLevelStorageSource().getBackupPath();
    }

    @Override
    protected Path getSaveDirectory() {
        return ((MinecraftServerExpanded)this.server).getLevelStorageSource().getBaseDir();
    }

    @Override
    protected String getLevelID() {
        return ((MinecraftServerExpanded)this.server).getLevelStorageAccess().getLevelId();
    }

    @Override
    protected int getDaysPlayed() {
        ServerLevel level = this.server.overworld();
        return (int)(level.getDayTime() / 24000);
    }

    @Override
    protected void saveEverything() throws BackupManagerException {
        if (!this.server.saveEverything(true, true, true))
            throw new BackupManagerException(BMECause.MINECRAFT_FAILURE, "Failed to save everything on the server!");
    }

    @Override
    protected BackupInfo makeBackup() throws BackupManagerException {
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
    protected void broadcastError(String translatableTitle, String literalInfo) {
        this.server.sendSystemMessage(Component.translatable(translatableTitle).append("\n" + literalInfo));
    }

    @Override
    protected void broadcastSuccessfulBackup(BackupType type, long size) {
        String str = FileUtils.byteCountToDisplaySize(size);
        this.server.sendSystemMessage(Component.translatable("rollback.success.createBackup", type, str));
    }

    @Override
    protected void broadcastSuccessfulDelete(int backupID, BackupType type) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.deleteBackup", backupID, type));
    }

    @Override
    protected void broadcastSuccessfulConvert(int backupID, BackupType from, BackupType to) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.convertBackup", backupID, from, to));
    }

    @Override
    protected void broadcastSuccessfulConfig(ConfigType configType) {
        RollbackConfig config = switch (configType) {
            case WORLD -> getDefaultConfig();
            case DEFAULT -> getWorld().config;
        };

        MutableComponent component = Component.translatable("rollback.success.updateConfig." + configType);
        for (ConfigEntry<?> entry : config.getEntries())
            component.append("\n" + entry.name + " = " + entry.getAsString());
        this.server.sendSystemMessage(component);
    }
}
