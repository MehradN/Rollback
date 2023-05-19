package ir.mehradn.rollback.network.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import java.util.HashSet;

public final class OpenGUI extends Packet<Void, Void> {
    public static final HashSet<ServerPlayer> awaitingPlayers = new HashSet<>();

    OpenGUI() {
        super("open_gui");
    }

    @Override
    public FriendlyByteBuf toBuf(Void data) {
        return PacketByteBufs.empty();
    }

    @Override
    public Void fromBuf(FriendlyByteBuf buf) {
        return null;
    }
}
