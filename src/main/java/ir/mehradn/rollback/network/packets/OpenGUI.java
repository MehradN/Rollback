package ir.mehradn.rollback.network.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class OpenGUI extends Packet<Void> {
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
