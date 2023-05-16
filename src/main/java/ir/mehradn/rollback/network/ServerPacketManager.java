package ir.mehradn.rollback.network;

import ir.mehradn.rollback.network.packets.Packet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class ServerPacketManager {
    public static <T> void send(ServerPlayer player, Packet<T> packet, T data) {
        ServerPlayNetworking.send(player, packet.identifier, packet.toBuf(data));
    }
}
