package ir.mehradn.rollback.event;

import ir.mehradn.rollback.config.RollbackDefaultConfig;
import ir.mehradn.rollback.config.RollbackNetworkConfig;
import ir.mehradn.rollback.exception.BackupManagerException;
import ir.mehradn.rollback.network.packets.*;
import ir.mehradn.rollback.rollback.ServerBackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class ServerPacketListener {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ConvertBackup.TYPE, ServerPacketListener::onConvertBackup);
        ServerPlayNetworking.registerGlobalReceiver(CreateBackup.TYPE, ServerPacketListener::onCreateBackup);
        ServerPlayNetworking.registerGlobalReceiver(DeleteBackup.TYPE, ServerPacketListener::onDeleteBackup);
        ServerPlayNetworking.registerGlobalReceiver(FetchMetadata.TYPE, ServerPacketListener::onFetchMetadata);
        ServerPlayNetworking.registerGlobalReceiver(OpenGUI.TYPE, ServerPacketListener::onOpenedGui);
        ServerPlayNetworking.registerGlobalReceiver(RenameBackup.TYPE, ServerPacketListener::onRenameBackup);
        ServerPlayNetworking.registerGlobalReceiver(RollbackBackup.TYPE, ServerPacketListener::onRollbackBackup);
        ServerPlayNetworking.registerGlobalReceiver(SaveConfig.TYPE, ServerPacketListener::onSaveConfig);
    }

    private static void onConvertBackup(ConvertBackup packet, ServerPlayer player, PacketSender responseSender) {
        if (checkPermission(player))
            return;
        ServerBackupManager backupManager = getBackupManager(player);
        if (backupManager.validateUpdateId(packet.lastUpdateId))
            return;
        try {
            backupManager.convertBackup(packet.backupId, packet.from, packet.to);
        } catch (BackupManagerException ignored) { }
    }

    private static void onCreateBackup(CreateBackup packet, ServerPlayer player, PacketSender responseSender) {
        if (checkPermission(player))
            return;
        ServerBackupManager backupManager = getBackupManager(player);
        if (backupManager.validateUpdateId(packet.lastUpdateId))
            return;
        try {
            backupManager.createBackup(packet.type, packet.name);
        } catch (BackupManagerException ignored) { }
    }

    private static void onDeleteBackup(DeleteBackup packet, ServerPlayer player, PacketSender responseSender) {
        if (checkPermission(player))
            return;
        ServerBackupManager backupManager = getBackupManager(player);
        if (backupManager.validateUpdateId(packet.lastUpdateId))
            return;
        try {
            backupManager.deleteBackup(packet.backupId, packet.type);
        } catch (BackupManagerException ignored) { }
    }

    private static void onFetchMetadata(FetchMetadata packet, ServerPlayer player, PacketSender responseSender) {
        if (checkPermission(player))
            return;
        ServerBackupManager backupManager = getBackupManager(player);
        int id = backupManager.getLastUpdateId();
        RollbackVersion version = RollbackVersion.LATEST_VERSION;
        RollbackWorld world = backupManager.getWorld();
        RollbackNetworkConfig config = new RollbackNetworkConfig();
        config.mergeFrom(backupManager.getDefaultConfig());
        ServerPlayNetworking.send(player, new SendMetadata(version, id, config, world).setIntegrated(packet.integrated));
    }

    private static void onOpenedGui(OpenGUI packet, ServerPlayer player, PacketSender responseSender) {
        OpenGUI.awaitingPlayers.remove(player);
    }

    private static void onRenameBackup(RenameBackup packet, ServerPlayer player, PacketSender responseSender) {
        if (checkPermission(player))
            return;
        ServerBackupManager backupManager = getBackupManager(player);
        if (backupManager.validateUpdateId(packet.lastUpdateId))
            return;
        try {
            backupManager.renameBackup(packet.backupId, packet.type, packet.name);
        } catch (BackupManagerException ignored) { }
    }

    private static void onRollbackBackup(RollbackBackup packet, ServerPlayer player, PacketSender responseSender) {
        if (checkPermission(player))
            return;
        ServerBackupManager backupManager = getBackupManager(player);
        if (backupManager.validateUpdateId(packet.lastUpdateId))
            return;
        try {
            backupManager.rollbackToBackup(packet.backupId, packet.type);
        } catch (BackupManagerException ignored) { }
    }

    private static void onSaveConfig(SaveConfig packet, ServerPlayer player, PacketSender responseSender) {
        if (checkPermission(player))
            return;
        ServerBackupManager backupManager = getBackupManager(player);
        if (backupManager.validateUpdateId(packet.lastUpdateId))
            return;
        try {
            if (packet.saveAsDefault)
                backupManager.saveToDefaultConfig(packet.worldConfig);
            else
                backupManager.saveToConfig(packet.worldConfig);
        } catch (BackupManagerException ignored) { }
    }

    private static ServerBackupManager getBackupManager(ServerPlayer player) {
        return ((MinecraftServerExpanded)player.server).getBackupManager();
    }

    private static boolean checkPermission(ServerPlayer player) {
        RollbackDefaultConfig config = getBackupManager(player).getDefaultConfig();
        return !config.hasCommandPermission(player);
    }
}
