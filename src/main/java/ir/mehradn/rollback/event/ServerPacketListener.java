package ir.mehradn.rollback.event;

import ir.mehradn.rollback.config.RollbackNetworkConfig;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.network.ServerPacketManager;
import ir.mehradn.rollback.network.packets.*;
import ir.mehradn.rollback.rollback.ServerBackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerPacketListener {
    public static void register() {
        ServerPacketManager.register(Packets.convertBackup, ServerPacketListener::onConvertBackup);
        ServerPacketManager.register(Packets.createBackup, ServerPacketListener::onCreateBackup);
        ServerPacketManager.register(Packets.deleteBackup, ServerPacketListener::onDeleteBackup);
        ServerPacketManager.register(Packets.fetchMetadata, ServerPacketListener::onFetchMetadata);
        ServerPacketManager.register(Packets.openGui, ServerPacketListener::onOpenedGui);
        ServerPacketManager.register(Packets.renameBackup, ServerPacketListener::onRenameBackup);
        ServerPacketManager.register(Packets.rollbackBackup, ServerPacketListener::onRollbackBackup);
        ServerPacketManager.register(Packets.saveConfig, ServerPacketListener::onSaveConfig);
    }

    private static void onConvertBackup(MinecraftServer server, ServerPlayer player, ConvertBackup.Arguments data) {
        ServerBackupManager backupManager = getBackupManager(server);
        if (backupManager.setRequester(player, data.lastChangeId()))
            return;
        try {
            backupManager.convertBackup(data.backupID(), data.from(), data.to());
        } catch (BackupManagerException ignored) { }
    }

    private static void onCreateBackup(MinecraftServer server, ServerPlayer player, CreateBackup.Arguments data) {
        ServerBackupManager backupManager = getBackupManager(server);
        if (backupManager.setRequester(player, data.lastChangeId()))
            return;
        try {
            backupManager.createBackup(data.type(), data.name());
        } catch (BackupManagerException ignored) { }
    }

    private static void onDeleteBackup(MinecraftServer server, ServerPlayer player, DeleteBackup.Arguments data) {
        ServerBackupManager backupManager = getBackupManager(server);
        if (backupManager.setRequester(player, data.lastChangeId()))
            return;
        try {
            backupManager.deleteBackup(data.backupID(), data.type());
        } catch (BackupManagerException ignored) { }
    }

    private static void onFetchMetadata(MinecraftServer server, ServerPlayer player, Boolean data) {
        ServerBackupManager backupManager = getBackupManager(server);
        int id = backupManager.getLastUpdateId();
        RollbackVersion version = RollbackVersion.LATEST_VERSION;
        RollbackWorld world = backupManager.getWorld();
        RollbackNetworkConfig config = new RollbackNetworkConfig();
        config.mergeFrom(backupManager.getDefaultConfig());
        ServerPacketManager.send(player, Packets.sendMetadata, new SendMetadata.MetadataSend(data, id, version, world, config));
    }

    private static void onOpenedGui(MinecraftServer server, ServerPlayer player, Void data) {
        OpenGUI.awaitingPlayers.remove(player);
    }

    private static void onRenameBackup(MinecraftServer server, ServerPlayer player, RenameBackup.Arguments data) {
        ServerBackupManager backupManager = getBackupManager(server);
        if (backupManager.setRequester(player, data.lastChangeId()))
            return;
        try {
            backupManager.renameBackup(data.backupID(), data.type(), data.name());
        } catch (BackupManagerException ignored) { }
    }

    private static void onRollbackBackup(MinecraftServer server, ServerPlayer player, RollbackBackup.Arguments data) {
        ServerBackupManager backupManager = getBackupManager(server);
        if (backupManager.setRequester(player, data.lastChangeId()))
            return;
        try {
            backupManager.rollbackToBackup(data.backupID(), data.type());
        } catch (BackupManagerException ignored) { }
    }

    private static void onSaveConfig(MinecraftServer server, ServerPlayer player, SaveConfig.Arguments data) {
        ServerBackupManager backupManager = getBackupManager(server);
        if (backupManager.setRequester(player, data.lastChangeId()))
            return;
        try {
            if (data.saveAsDefault())
                backupManager.saveToDefaultConfig(data.config());
            else
                backupManager.saveToConfig(data.config());
        } catch (BackupManagerException ignored) { }
    }

    private static ServerBackupManager getBackupManager(MinecraftServer server) {
        return ((MinecraftServerExpanded)server).getBackupManager();
    }
}
