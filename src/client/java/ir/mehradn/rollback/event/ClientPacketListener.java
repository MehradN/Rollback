package ir.mehradn.rollback.event;

import ir.mehradn.rollback.gui.ScreenManager;
import ir.mehradn.rollback.network.ClientPacketManager;
import ir.mehradn.rollback.network.packets.Packets;
import ir.mehradn.rollback.network.packets.SendMetadata;
import ir.mehradn.rollback.rollback.NetworkBackupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientPacketListener {
    public static void register() {
        ClientPacketManager.register(Packets.openGui, ClientPacketListener::onOpenGui);
        ClientPacketManager.register(Packets.sendMetadata, ClientPacketListener::onSendMetadata);
    }

    private static void onOpenGui(Minecraft minecraft, Void data) {
        minecraft.setScreen(null);
        minecraft.pauseGame(false);
        ScreenManager.activate(minecraft, new NetworkBackupManager(minecraft));
        ClientPacketManager.send(Packets.openGui, null);
    }

    private static void onSendMetadata(Minecraft minecraft, @Nullable SendMetadata.MetadataReceive data) {
        if (data == null) {
            if (ScreenManager.getInstance() != null)
                ScreenManager.getInstance().onNotMatchingVersions();
            return;
        }
        NetworkBackupManager backupManager = getBackupManager();
        if (backupManager == null)
            return;
        backupManager.loadingFinished(data);
    }

    private static @Nullable NetworkBackupManager getBackupManager() {
        ScreenManager screenManager = ScreenManager.getInstance();
        if (screenManager != null && screenManager.backupManager != null && screenManager.backupManager instanceof NetworkBackupManager backupManager)
            return backupManager;
        return null;
    }
}
