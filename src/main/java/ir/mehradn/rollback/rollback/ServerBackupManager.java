package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.network.ServerPacketManager;
import ir.mehradn.rollback.network.packets.*;
import ir.mehradn.rollback.rollback.metadata.RollbackWorldConfig;
import ir.mehradn.rollback.util.mixin.LevelStorageAccessExpanded;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.file.Path;

public class ServerBackupManager extends CommonBackupManager {
    private final MinecraftServer server;
    private int lastUpdateId;
    private ServerPlayer requester = null;

    public ServerBackupManager(MinecraftServer server) {
        super();
        this.server = server;
        this.lastUpdateId = 0;
    }

    public int getLastUpdateId() {
        return this.lastUpdateId;
    }

    public void removeRequester() {
        this.requester = null;
    }

    public void setCommandSender(ServerPlayer player) {
        this.requester = player;
    }

    public boolean setRequester(ServerPlayer requester, int lastUpdateId) {
        this.requester = requester;
        if (lastUpdateId != this.lastUpdateId) {
            int diff = Math.abs(this.lastUpdateId - lastUpdateId);
            broadcastError("rollback.error.outOfSync", "You were out of sync by " + diff + " updates! Please try again.");
            return true;
        }
        return false;
    }

    public void saveToConfig(RollbackWorldConfig config) throws BackupManagerException {
        this.getWorld().config.copyFrom(config);
        saveConfig();
    }

    public void saveToDefaultConfig(RollbackWorldConfig config) throws BackupManagerException {
        this.getWorld().config.copyFrom(config);
        saveConfigAsDefault();
    }

    @Override
    public void createBackup(BackupType type, @Nullable String name) throws BackupManagerException {
        super.createBackup(type, name);
        increaseUpdateId();
    }

    @Override
    public void deleteBackup(int backupID, BackupType type) throws BackupManagerException {
        super.deleteBackup(backupID, type);
        increaseUpdateId();
    }

    @Override
    public void renameBackup(int backupID, BackupType type, @Nullable String name) throws BackupManagerException {
        super.renameBackup(backupID, type, name);
        increaseUpdateId();
    }

    @Override
    public void convertBackup(int backupID, BackupType from, BackupType to) throws BackupManagerException {
        super.convertBackup(backupID, from, to);
        increaseUpdateId();
    }

    @Override
    public void rollbackToBackup(int backupID, BackupType type) throws BackupManagerException {
        //noinspection DataFlowIssue
        Assertion.runtime(false, "Rollback is not implemented for servers");
        increaseUpdateId();
    }

    @Override
    public void saveConfig() throws BackupManagerException {
        super.saveConfig();
        increaseUpdateId();
    }

    @Override
    public void saveConfigAsDefault() throws BackupManagerException {
        super.saveConfigAsDefault();
        increaseUpdateId();
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
            throw new BackupManagerException(BackupManagerException.Cause.MINECRAFT_FAILURE, "Failed to save everything on the server!");
    }

    @Override
    protected BackupInfo makeBackup() throws BackupManagerException {
        LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerExpanded)this.server).getLevelStorageAccess();
        try {
            long size = storageAccess.makeWorldBackup();
            Path path = ((LevelStorageAccessExpanded)storageAccess).getLatestBackupPath();
            return new BackupInfo(path, size);
        } catch (IOException e) {
            throw new BackupManagerException(BackupManagerException.Cause.IO_EXCEPTION, "Failed to create a backup!", e);
        }
    }

    @Override
    protected void broadcastError(String translatableTitle, String literalInfo) {
        this.server.sendSystemMessage(Component.translatable(translatableTitle).append("\n" + literalInfo));
        if (this.requester != null)
            ServerPacketManager.send(this.requester, Packets.backupManagerError, new BackupManagerError.Info(translatableTitle, literalInfo));
    }

    @Override
    protected void broadcastSuccessfulBackup(BackupType type, long size) {
        String str = FileUtils.byteCountToDisplaySize(size);
        this.server.sendSystemMessage(Component.translatable("rollback.success.createBackup", type, str));
        if (this.requester != null)
            ServerPacketManager.send(this.requester, Packets.successfulBackup, new SuccessfulBackup.Info(type, size));
    }

    @Override
    protected void broadcastSuccessfulDelete(int backupID, BackupType type) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.deleteBackup", backupID, type));
        if (this.requester != null)
            ServerPacketManager.send(this.requester, Packets.successfulDelete, new SuccessfulDelete.Info(backupID, type));
    }

    @Override
    protected void broadcastSuccessfulRename(int backupID, BackupType type) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.renameBackup", backupID, type));
        if (this.requester != null)
            ServerPacketManager.send(this.requester, Packets.successfulRename, new SuccessfulRename.Info(backupID, type));
    }

    @Override
    protected void broadcastSuccessfulConvert(int backupID, BackupType from, BackupType to) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.convertBackup", backupID, from, to));
        if (this.requester != null)
            ServerPacketManager.send(this.requester, Packets.successfulConvert, new SuccessfulConvert.Info(backupID, from, to));
    }

    @Override
    protected void broadcastSuccessfulConfig(boolean defaultConfig) {
        RollbackConfig config = (defaultConfig ? getDefaultConfig() : getWorld().config);
        MutableComponent component = Component.translatable("rollback.success.updateConfig." + (defaultConfig ? "default" : "world"));
        for (ConfigEntry<?> entry : config.getEntries())
            component.append("\n" + entry.name + " = " + entry.get().toString());
        this.server.sendSystemMessage(component);
        if (this.requester != null)
            ServerPacketManager.send(this.requester, Packets.successfulConfig, defaultConfig);
    }

    private void increaseUpdateId() {
        this.lastUpdateId++;
        for (ServerPlayer player : PlayerLookup.all(this.server))
            ServerPacketManager.send(player, Packets.newUpdateId, null);
    }
}
