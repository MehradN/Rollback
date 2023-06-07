package ir.mehradn.rollback.network.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class SuccessfulConfig extends Packet<Boolean, Boolean> {
    SuccessfulConfig() {
        super("successful_config");
    }

    @Override
    public FriendlyByteBuf toBuf(Boolean data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(data);
        return buf;
    }

    @Override
    public Boolean fromBuf(FriendlyByteBuf buf) {
        return buf.readBoolean();
    }
}
