package ir.mehradn.rollback.event;

import ir.mehradn.rollback.gui.ScreenManager;
import ir.mehradn.rollback.network.packets.*;
import ir.mehradn.rollback.rollback.NetworkBackupManager;
import ir.mehradn.rollback.util.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientPacketListener {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(BackupManagerError.TYPE, ClientPacketListener::onBackupManagerError);
        ClientPlayNetworking.registerGlobalReceiver(BackupWarning.TYPE, ClientPacketListener::onBackupWarning);
        ClientPlayNetworking.registerGlobalReceiver(NewUpdateId.TYPE, ClientPacketListener::onNewUpdateId);
        ClientPlayNetworking.registerGlobalReceiver(OpenGUI.TYPE, ClientPacketListener::onOpenGui);
        ClientPlayNetworking.registerGlobalReceiver(SendMetadata.TYPE, ClientPacketListener::onSendMetadata);
        ClientPlayNetworking.registerGlobalReceiver(SuccessfulBackup.TYPE, ClientPacketListener::onSuccessfulBackup);
        ClientPlayNetworking.registerGlobalReceiver(SuccessfulConfig.TYPE, ClientPacketListener::onSuccessfulConfig);
        ClientPlayNetworking.registerGlobalReceiver(SuccessfulConvert.TYPE, ClientPacketListener::onSuccessfulConvert);
        ClientPlayNetworking.registerGlobalReceiver(SuccessfulDelete.TYPE, ClientPacketListener::onSuccessfulDelete);
        ClientPlayNetworking.registerGlobalReceiver(SuccessfulRename.TYPE, ClientPacketListener::onSuccessfulRename);
    }

    private static void onBackupManagerError(BackupManagerError packet, LocalPlayer player, PacketSender responseSender) {
        if (ScreenManager.getInstance() != null) {
            ScreenManager.getInstance().onError(packet.translatableTitle, packet.literalInfo);
        } else {
            ScreenManager.showToast(
                Minecraft.getInstance(),
                Component.translatable(packet.translatableTitle),
                Component.literal(packet.literalInfo)
            );
        }
    }

    private static void onBackupWarning(BackupWarning packet, LocalPlayer player, PacketSender responseSender) {
        if (ScreenManager.getInstance() != null)
            ScreenManager.getInstance().onWarning(packet.backupCount, packet.totalSize);
    }

    private static void onNewUpdateId(NewUpdateId packet, LocalPlayer player, PacketSender responseSender) {
        if (ScreenManager.getInstance() != null)
            ScreenManager.getInstance().loadMetadata();
    }

    private static void onOpenGui(OpenGUI packet, LocalPlayer player, PacketSender responseSender) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(null);
        minecraft.pauseGame(false);
        ScreenManager.activate(minecraft, new NetworkBackupManager(minecraft));
        ClientPlayNetworking.send(new OpenGUI());
    }

    private static void onSendMetadata(SendMetadata packet, LocalPlayer player, PacketSender responseSender) {
        if (ScreenManager.getInstance() == null)
            return;
        if (packet.version.notMatch()) {
            ScreenManager.getInstance().onNotMatchingVersions();
            return;
        }
        NetworkBackupManager backupManager = getBackupManager();
        if (backupManager == null)
            return;
        backupManager.loadingFinished(packet);
    }

    private static void onSuccessfulBackup(SuccessfulBackup packet, LocalPlayer player, PacketSender responseSender) {
        ScreenManager.showToast(
            Minecraft.getInstance(),
            Component.translatable("rollback.toast.successfulBackup.title"),
            Component.translatable("rollback.toast.successfulBackup.info", packet.type.toComponent(), Utils.fileSizeToString(packet.fileSize))
        );
    }

    private static void onSuccessfulConfig(SuccessfulConfig packet, LocalPlayer player, PacketSender responseSender) {
        ScreenManager.showToast(
            Minecraft.getInstance(),
            Component.translatable("rollback.toast.successfulConfig." + (packet.defaultConfig ? "default" : "world")),
            Component.empty()
        );
    }

    private static void onSuccessfulConvert(SuccessfulConvert packet, LocalPlayer player, PacketSender responseSender) {
        ScreenManager.showToast(
            Minecraft.getInstance(),
            Component.translatable("rollback.toast.successfulConvert.title"),
            Component.translatable("rollback.toast.successfulConvert.info", packet.backupId, packet.from.toComponent(), packet.to.toComponent())
        );
    }

    private static void onSuccessfulDelete(SuccessfulDelete packet, LocalPlayer player, PacketSender responseSender) {
        ScreenManager.showToast(
            Minecraft.getInstance(),
            Component.translatable("rollback.toast.successfulDelete.title"),
            Component.translatable("rollback.toast.successfulDelete.info", packet.type.toComponent(), packet.backupId)
        );
    }

    private static void onSuccessfulRename(SuccessfulRename packet, LocalPlayer player, PacketSender responseSender) {
        ScreenManager.showToast(
            Minecraft.getInstance(),
            Component.translatable("rollback.toast.successfulRename.title"),
            Component.translatable("rollback.toast.successfulRename.info", packet.type.toComponent(), packet.backupId)
        );
    }

    private static @Nullable NetworkBackupManager getBackupManager() {
        ScreenManager screenManager = ScreenManager.getInstance();
        if (screenManager != null && screenManager.backupManager != null &&
            screenManager.backupManager instanceof NetworkBackupManager backupManager)
            return backupManager;
        return null;
    }
}
