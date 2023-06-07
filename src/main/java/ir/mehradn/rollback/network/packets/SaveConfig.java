package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.rollback.metadata.RollbackWorldConfig;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public final class SaveConfig extends Packet<SaveConfig.Arguments, SaveConfig.Arguments> {
    SaveConfig() {
        super("save_config");
    }

    @Override
    public FriendlyByteBuf toBuf(Arguments data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.lastChangeId);
        buf.writeBoolean(data.saveAsDefault);
        data.config.writeToBuf(buf, false);
        return buf;
    }

    @Override
    public Arguments fromBuf(FriendlyByteBuf buf) {
        int id = buf.readInt();
        boolean sad = buf.readBoolean();
        RollbackWorldConfig config = new RollbackWorldConfig();
        config.readFromBuf(buf);
        return new Arguments(id, config, sad);
    }

    public record Arguments(int lastChangeId, RollbackWorldConfig config, boolean saveAsDefault) { }
}
