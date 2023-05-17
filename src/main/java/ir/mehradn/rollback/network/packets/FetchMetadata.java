package ir.mehradn.rollback.network.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public final class FetchMetadata extends Packet<Void> {
    FetchMetadata() {
        super("fetch_metadata");
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
