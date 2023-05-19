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

    private static void onOpenGui(Minecraft client, Void data) {
        client.setScreen(null);
        client.pauseGame(false);
        ScreenManager.activate(client, new NetworkBackupManager(client));
        ClientPacketManager.send(Packets.openGui, null);
    }

    private static void onSendMetadata(Minecraft client, SendMetadata.MetadataReceive data) {
        NetworkBackupManager backupManager = getBackupManager();
        if (backupManager == null)
            return;
        backupManager.loadingFinished(data);
    }

    private static @Nullable NetworkBackupManager getBackupManager() {
        if (ScreenManager.getInstance() != null && ScreenManager.getInstance().backupManager instanceof NetworkBackupManager backupManager)
            return backupManager;
        return null;
    }
}
