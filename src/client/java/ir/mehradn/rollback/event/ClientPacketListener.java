package ir.mehradn.rollback.event;

import ir.mehradn.rollback.gui.ScreenManager;
import ir.mehradn.rollback.network.ClientPacketManager;
import ir.mehradn.rollback.network.packets.*;
import ir.mehradn.rollback.rollback.NetworkBackupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientPacketListener {
    public static void register() {
        ClientPacketManager.register(Packets.backupManagerError, ClientPacketListener::onBackupManagerError);
        ClientPacketManager.register(Packets.newUpdateId, ClientPacketListener::onNewUpdateId);
        ClientPacketManager.register(Packets.openGui, ClientPacketListener::onOpenGui);
        ClientPacketManager.register(Packets.sendMetadata, ClientPacketListener::onSendMetadata);
        ClientPacketManager.register(Packets.successfulBackup, ClientPacketListener::onSuccessfulBackup);
        ClientPacketManager.register(Packets.successfulConfig, ClientPacketListener::onSuccessfulConfig);
        ClientPacketManager.register(Packets.successfulConvert, ClientPacketListener::onSuccessfulConvert);
        ClientPacketManager.register(Packets.successfulDelete, ClientPacketListener::onSuccessfulDelete);
        ClientPacketManager.register(Packets.successfulRename, ClientPacketListener::onSuccessfulRename);
    }

    private static void onBackupManagerError(Minecraft minecraft, BackupManagerError.Info info) {
        if (ScreenManager.getInstance() != null) {
            ScreenManager.getInstance().onError(info.translatableTitle(), info.literalInfo());
        } else if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(
                Component.translatable(info.translatableTitle())
                    .withStyle(ChatFormatting.BOLD)
                    .append(Component.literal(info.literalInfo()))
                    .withStyle(ChatFormatting.RED)
            );
        }
    }

    private static void onNewUpdateId(Minecraft minecraft, Void data) {
        if (ScreenManager.getInstance() != null)
            ScreenManager.getInstance().loadMetadata();
    }

    private static void onOpenGui(Minecraft minecraft, Void data) {
        minecraft.setScreen(null);
        minecraft.pauseGame(false);
        ScreenManager.activate(minecraft, new NetworkBackupManager(minecraft));
        ClientPacketManager.send(Packets.openGui, null);
    }

    private static void onSendMetadata(Minecraft minecraft, @Nullable SendMetadata.MetadataReceive data) {
        if (ScreenManager.getInstance() == null)
            return;
        if (data == null) {
            ScreenManager.getInstance().onNotMatchingVersions();
            return;
        }
        NetworkBackupManager backupManager = getBackupManager();
        if (backupManager == null)
            return;
        backupManager.loadingFinished(data);
    }

    private static void onSuccessfulBackup(Minecraft minecraft, SuccessfulBackup.Info data) {
        ScreenManager.showToast(
            minecraft,
            Component.translatable("rollback.toast.successfulBackup.title"),
            Component.translatable("rollback.toast.successfulBackup.info", data.type().toComponent(), data.sizeAsString())
        );
    }

    private static void onSuccessfulConfig(Minecraft minecraft, Boolean data) {
        ScreenManager.showToast(
            minecraft,
            Component.translatable("rollback.toast.successfulConfig." + (data ? "default" : "world")),
            Component.empty()
        );
    }

    private static void onSuccessfulConvert(Minecraft minecraft, SuccessfulConvert.Info data) {
        ScreenManager.showToast(
            minecraft,
            Component.translatable("rollback.toast.successfulConvert.title"),
            Component.translatable("rollback.toast.successfulConvert.info", data.backupId(), data.from().toComponent(), data.to().toComponent())
        );
    }

    private static void onSuccessfulDelete(Minecraft minecraft, SuccessfulDelete.Info data) {
        ScreenManager.showToast(
            minecraft,
            Component.translatable("rollback.toast.successfulDelete.title"),
            Component.translatable("rollback.toast.successfulDelete.info", data.type().toComponent(), data.backupId())
        );
    }

    private static void onSuccessfulRename(Minecraft minecraft, SuccessfulRename.Info data) {
        ScreenManager.showToast(
            minecraft,
            Component.translatable("rollback.toast.successfulRename.title"),
            Component.translatable("rollback.toast.successfulRename.info", data.type().toComponent(), data.backupId())
        );
    }

    private static @Nullable NetworkBackupManager getBackupManager() {
        ScreenManager screenManager = ScreenManager.getInstance();
        if (screenManager != null && screenManager.backupManager != null && screenManager.backupManager instanceof NetworkBackupManager backupManager)
            return backupManager;
        return null;
    }
}
