package ir.mehradn.rollback.event;

import ir.mehradn.rollback.gui.RollbackScreen;
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
        client.setScreen(new RollbackScreen(new NetworkBackupManager(client), client.screen));
    }

    private static void onSendMetadata(Minecraft client, SendMetadata.Metadata data) {
        NetworkBackupManager backupManager = getBackupManager(client);
        if (backupManager == null)
            return;
        backupManager.loadingFinished(data);
    }

    private static @Nullable NetworkBackupManager getBackupManager(Minecraft client) {
        if (!(client.screen instanceof RollbackScreen screen))
            return null;
        if (!(screen.backupManager instanceof NetworkBackupManager backupManager))
            return null;
        return backupManager;
    }
}
