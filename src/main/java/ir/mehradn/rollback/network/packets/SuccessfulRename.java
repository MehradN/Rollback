package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class SuccessfulRename extends Packet<SuccessfulRename.Info, SuccessfulRename.Info> {
    SuccessfulRename() {
        super("successful_rename");
    }

    @Override
    public FriendlyByteBuf toBuf(Info data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.backupId);
        buf.writeEnum(data.type);
        return buf;
    }

    @Override
    public Info fromBuf(FriendlyByteBuf buf) {
        int id = buf.readInt();
        BackupType type = buf.readEnum(BackupType.class);
        return new Info(id, type);
    }

    public record Info(int backupId, BackupType type) { }
}
