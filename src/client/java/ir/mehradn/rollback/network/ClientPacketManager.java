package ir.mehradn.rollback.network;

import ir.mehradn.rollback.network.packets.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class ClientPacketManager {
    public static <T> void register(Packet<T> packet, OnReceive<T> onReceive) {
        ClientPlayNetworking.registerGlobalReceiver(packet.identifier, ((client, handler, buf, responseSender) -> {
            T data = packet.fromBuf(buf);
            client.execute(() -> onReceive.call(client, data));
        }));
    }

    public static <T> void send(Packet<T> packet, T data) {
        ClientPlayNetworking.send(packet.identifier, packet.toBuf(data));
    }

    @FunctionalInterface
    public interface OnReceive <T> {
        void call(Minecraft client, T data);
    }
}
