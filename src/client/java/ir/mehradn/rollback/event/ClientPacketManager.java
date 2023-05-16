package ir.mehradn.rollback.event;

import ir.mehradn.rollback.gui.RollbackScreen;
import ir.mehradn.rollback.network.packets.Packet;
import ir.mehradn.rollback.network.packets.Packets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class ClientPacketManager {
    public static void register() {
        registerPacket(Packets.openGui, ClientPacketManager::onOpenGui);
    }

    private static <T> void registerPacket(Packet<T> packet, OnReceive<T> onReceive) {
        ClientPlayNetworking.registerGlobalReceiver(packet.identifier, ((client, handler, buf, responseSender) -> {
            T data = packet.fromBuf(buf);
            client.execute(() -> onReceive.call(client, data));
        }));
    }

    private static void onOpenGui(Minecraft client, Void data) {
        client.setScreen(null);
        client.pauseGame(false);
        client.setScreen(new RollbackScreen(null, client.screen));
    }

    @FunctionalInterface
    private interface OnReceive <T> {
        void call(Minecraft client, T data);
    }
}
