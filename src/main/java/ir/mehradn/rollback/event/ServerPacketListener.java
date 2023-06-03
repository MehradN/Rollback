package ir.mehradn.rollback.event;

import ir.mehradn.rollback.config.RollbackNetworkConfig;
import ir.mehradn.rollback.network.ServerPacketManager;
import ir.mehradn.rollback.network.packets.OpenGUI;
import ir.mehradn.rollback.network.packets.Packets;
import ir.mehradn.rollback.network.packets.SendMetadata;
import ir.mehradn.rollback.rollback.ServerBackupManager;
import ir.mehradn.rollback.rollback.metadata.RollbackVersion;
import ir.mehradn.rollback.rollback.metadata.RollbackWorld;
import ir.mehradn.rollback.util.mixin.MinecraftServerExpanded;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerPacketListener {
    public static void register() {
        ServerPacketManager.register(Packets.openGui, ServerPacketListener::onOpenedGui);
        ServerPacketManager.register(Packets.fetchMetadata, ServerPacketListener::onFetchMetadata);
    }

    private static void onOpenedGui(MinecraftServer server, ServerPlayer player, Void data) {
        OpenGUI.awaitingPlayers.remove(player);
    }

    private static void onFetchMetadata(MinecraftServer server, ServerPlayer player, Boolean data) {
        ServerBackupManager backupManager = getBackupManager(server);
        RollbackVersion version = RollbackVersion.LATEST_VERSION;
        RollbackWorld world = backupManager.getWorld();
        RollbackNetworkConfig config = new RollbackNetworkConfig();
        config.copyFrom(backupManager.getDefaultConfig());
        ServerPacketManager.send(player, Packets.sendMetadata, new SendMetadata.MetadataSend(data, version, world, config));
    }

    private static ServerBackupManager getBackupManager(MinecraftServer server) {
        return ((MinecraftServerExpanded)server).getBackupManager();
    }
}
