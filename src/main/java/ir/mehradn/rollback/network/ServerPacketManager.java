package ir.mehradn.rollback.network;

import ir.mehradn.rollback.network.packets.Packet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerPacketManager {
    public static <T> void register(Packet<T> packet, OnReceive<T> onReceive) {
        ServerPlayNetworking.registerGlobalReceiver(packet.identifier, (server, player, handler, buf, responseSender) -> {
            T data = packet.fromBuf(buf);
            server.execute(() -> onReceive.call(server, player, data));
        });
    }

    public static <T> void send(ServerPlayer player, Packet<T> packet, T data) {
        ServerPlayNetworking.send(player, packet.identifier, packet.toBuf(data));
    }

    @FunctionalInterface
    public interface OnReceive <T> {
        void call(MinecraftServer server, ServerPlayer player, T data);
    }
}
