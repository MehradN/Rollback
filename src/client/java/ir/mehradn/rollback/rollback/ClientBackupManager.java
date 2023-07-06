package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.gui.ScreenManager;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import ir.mehradn.rollback.util.Utils;
import ir.mehradn.rollback.util.mixin.LevelStorageAccessExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.ServerLevelData;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ClientBackupManager extends CommonBackupManager {
    private final Minecraft minecraft;
    private final LevelSummary levelSummary;
    private final ServerLevelData levelData;

    public ClientBackupManager(Minecraft minecraft, LevelSummary levelSummary) {
        super();
        this.minecraft = minecraft;
        this.levelSummary = levelSummary;

        try (LevelStorageSource.LevelStorageAccess storageAccess = minecraft.getLevelSource().createAccess(levelSummary.getLevelId())) {
            WorldStem worldStem = minecraft.createWorldOpenFlows().loadWorldStem(storageAccess, false);
            this.levelData = worldStem.worldData().overworldData();
            worldStem.close();
        } catch (Exception e) {
            Rollback.LOGGER.error("Failed to load the level data!", e);
            throw new RuntimeException(e);
        }
    }

    public LevelSummary getSummary() {
        return this.levelSummary;
    }

    public void deleteWorld() throws BackupManagerException {
        Assertion.state(this.data != null && this.world != null, "Call loadWorld before this!");
        String levelID = getLevelID();
        Rollback.LOGGER.info("Deleting all the backups for world \"{}\"...", levelID);

        Set<Integer> automatedIDs = this.world.automatedBackups.keySet();
        Set<Integer> commandIDs = this.world.commandBackups.keySet();
        while (!automatedIDs.isEmpty())
            deleteBackup(automatedIDs.iterator().next(), BackupType.ROLLBACK);
        while (!commandIDs.isEmpty())
            deleteBackup(commandIDs.iterator().next(), BackupType.BACKUP);

        this.data.worlds.remove(levelID);
        saveWorld();
    }

    @Override
    public void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException {
        RollbackBackup backup = getWorld().getBackup(backupID, type);
        Rollback.LOGGER.info("Rolling back to backup \"{}\"...", backup.backupPath.toString());

        Rollback.LOGGER.debug("Deleting the current save...");
        try (LevelStorageSource.LevelStorageAccess levelAccess = this.minecraft.getLevelSource().createAccess(this.levelSummary.getLevelId())) {
            levelAccess.deleteLevel();
        } catch (IOException e) {
            throw showError("rollback.error.rollbackToBackup", "Failed to delete the current save!",
                BackupManagerException.Cause.IO_EXCEPTION, e);
        }

        extractBackup(backupID, type);
    }

    @Override
    public Path getBackupDirectory() {
        return Minecraft.getInstance().getLevelSource().getBackupPath();
    }

    @Override
    public Path getSaveDirectory() {
        return Minecraft.getInstance().getLevelSource().getBaseDir();
    }

    @Override
    protected String getLevelID() {
        return this.levelSummary.getLevelId();
    }

    @Override
    protected int getDaysPlayed() {
        return (int)(this.levelData.getDayTime() / 24000);
    }

    @Override
    protected void saveEverything() { }

    @Override
    protected BackupInfo makeBackup() throws BackupManagerException {
        try (LevelStorageSource.LevelStorageAccess storageAccess = this.minecraft.getLevelSource().createAccess(this.levelSummary.getLevelId())) {
            long size = storageAccess.makeWorldBackup();
            Path path = ((LevelStorageAccessExpanded)storageAccess).getLatestBackupPath();
            return new BackupInfo(path, size);
        } catch (IOException e) {
            throw new BackupManagerException(BackupManagerException.Cause.IO_EXCEPTION, "Failed to create a backup!", e);
        }
    }

    @Override
    protected void broadcastError(String translatableTitle, String literalInfo) {
        if (ScreenManager.getInstance() != null) {
            ScreenManager.getInstance().onError(translatableTitle, literalInfo);
        } else {
            ScreenManager.showToast(
                Minecraft.getInstance(),
                Component.translatable(translatableTitle),
                Component.literal(literalInfo)
            );
        }
    }

    @Override
    protected void broadcastSuccessfulBackup(BackupType type, long size) {
        ScreenManager.showToast(
            Minecraft.getInstance(),
            Component.translatable("rollback.toast.title.successfulBackup." + type),
            Component.translatable("rollback.toast.info.successfulBackup", Utils.fileSizeToString(size))
        );
    }

    @Override
    protected void broadcastSuccessfulDelete(int backupId, BackupType type) {
        ScreenManager.showToast(
            Minecraft.getInstance(),
            Component.translatable("rollback.toast.title.successfulDelete"),
            Component.translatable("rollback.toast.info.successfulDelete", backupId)
        );
    }

    @Override
    protected void broadcastSuccessfulRename(int backupId, BackupType type) {
        ScreenManager.showToast(
            Minecraft.getInstance(),
            Component.translatable("rollback.toast.title.successfulRename"),
            Component.translatable("rollback.toast.info.successfulRename", type.toComponent(), backupId)
        );
    }

    @Override
    protected void broadcastSuccessfulConvert(int backupId, BackupType from, BackupType to) {
        ScreenManager.showToast(
            Minecraft.getInstance(),
            Component.translatable("rollback.toast.title.successfulConvert." + from),
            Component.translatable("rollback.toast.info.successfulConvert", backupId, from.toComponent(), to.toComponent())
        );
    }

    @Override
    protected void broadcastSuccessfulConfig() { }
}
