package ir.mehradn.rollback.rollback;

import ir.mehradn.rollback.config.ConfigEntry;
import ir.mehradn.rollback.config.RollbackConfig;
import ir.mehradn.rollback.exception.Assertion;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.network.packets.*;
import ir.mehradn.rollback.rollback.metadata.RollbackBackup;
import ir.mehradn.rollback.rollback.metadata.RollbackWorldConfig;
import ir.mehradn.rollback.util.Utils;
import ir.mehradn.rollback.util.mixin.LevelStorageAccessExpanded;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerBackupManager extends CommonBackupManager {
    private final MinecraftServer server;
    private int lastUpdateId;

    public ServerBackupManager(MinecraftServer server) {
        super();
        this.server = server;
        this.lastUpdateId = 0;
    }

    public int getLastUpdateId() {
        return this.lastUpdateId;
    }

    public boolean validateUpdateId(int lastUpdateId) {
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

    public void setIconPath(int backupId, BackupType type, Path iconPath) throws BackupManagerException {
        this.getWorld().getBackup(backupId, type).iconPath = iconPath;
        saveWorld();
        increaseUpdateId();
    }

    @Override
    public void createBackup(BackupType type, @Nullable String name) throws BackupManagerException {
        super.createBackup(type, name);
        increaseUpdateId();
        backupWarning();

        try {
            Path iconDirectory = getRollbackDirectory().resolve("icons");
            Files.createDirectories(iconDirectory);
            String fileName = FileUtil.findAvailableName(iconDirectory, getLevelID() + "_" + this.getWorld().lastID, ".png");
            Path iconPath = getRollbackDirectory().relativize(iconDirectory.resolve(fileName));
            broadcast(new TakeScreenshot(this.getWorld().lastID, type, iconPath));
        } catch (IOException ignored) { }
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
        broadcast(new BackupManagerError(translatableTitle, literalInfo));
    }

    @Override
    protected void broadcastSuccessfulBackup(BackupType type, long size) {
        String str = FileUtils.byteCountToDisplaySize(size);
        this.server.sendSystemMessage(Component.translatable("rollback.success.createBackup", type, str));
        broadcast(new SuccessfulBackup(type, size));
    }

    @Override
    protected void broadcastSuccessfulDelete(int backupID, BackupType type) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.deleteBackup", backupID, type));
        broadcast(new SuccessfulDelete(backupID, type));
    }

    @Override
    protected void broadcastSuccessfulRename(int backupID, BackupType type) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.renameBackup", backupID, type));
        broadcast(new SuccessfulRename(backupID, type));
    }

    @Override
    protected void broadcastSuccessfulConvert(int backupID, BackupType from, BackupType to) {
        this.server.sendSystemMessage(Component.translatable("rollback.success.convertBackup", backupID, from, to));
        broadcast(new SuccessfulConvert(backupID, from, to));
    }

    @Override
    protected void broadcastSuccessfulConfig(boolean defaultConfig) {
        RollbackConfig config = (defaultConfig ? getDefaultConfig() : getWorld().config);
        MutableComponent text = Component.translatable("rollback.success.updateConfig." + (defaultConfig ? "default" : "world"));
        for (ConfigEntry<?> entry : config.getEntries())
            text.append("\n" + entry.name + " = " + entry.get().toString());
        this.server.sendSystemMessage(text);
        broadcast(new SuccessfulConfig(defaultConfig));
    }

    private boolean shouldSendBackupWarning(int count) {
        if (count < 20)
            return false;
        if (count < 50)
            return (count % 10 == 0);
        if (count < 80)
            return (count % 5 == 0);
        return true;
    }

    private long totalBackupSize() {
        long sum = 0;
        for (RollbackBackup backup : getWorld().getBackups(BackupType.COMMAND).values())
            if (backup.fileSize > 0)
                sum += backup.fileSize;
        return sum;
    }

    private void backupWarning() {
        int count = getWorld().getBackups(BackupType.COMMAND).size();
        if (!shouldSendBackupWarning(count))
            return;

        long size = totalBackupSize();
        broadcast(new BackupWarning(count, size));

        MutableComponent text = Component.translatable("rollback.backupWarning.line1", count, Utils.fileSizeToString(size));
        if (count >= 90)
            text.append("\n").append(Component.translatable("rollback.backupWarning.line2", 99));
        text.append("\n").append(Component.translatable("rollback.backupWarning.line3"));
        text.withStyle(ChatFormatting.YELLOW);

        this.server.sendSystemMessage(text);
        for (ServerPlayer player : PlayerLookup.all(this.server))
            if (getDefaultConfig().hasCommandPermission(player))
                player.sendSystemMessage(text);
    }

    private void increaseUpdateId() {
        this.lastUpdateId++;
        for (ServerPlayer player : PlayerLookup.all(this.server))
            ServerPlayNetworking.send(player, new NewUpdateId());
    }

    private <T extends FabricPacket> void broadcast(T packet) {
        for (ServerPlayer player : PlayerLookup.all(this.server))
            if (getDefaultConfig().hasCommandPermission(player))
                ServerPlayNetworking.send(player, packet);
    }
}
