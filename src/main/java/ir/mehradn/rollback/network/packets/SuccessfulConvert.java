package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class SuccessfulConvert extends Packet<SuccessfulConvert.Info, SuccessfulConvert.Info> {
    SuccessfulConvert() {
        super("successful_convert");
    }

    @Override
    public FriendlyByteBuf toBuf(Info data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.backupId);
        buf.writeEnum(data.from);
        buf.writeEnum(data.to);
        return buf;
    }

    @Override
    public Info fromBuf(FriendlyByteBuf buf) {
        int id = buf.readInt();
        BackupType from = buf.readEnum(BackupType.class);
        BackupType to = buf.readEnum(BackupType.class);
        return new Info(id, from, to);
    }

    public record Info(int backupId, BackupType from, BackupType to) { }
}
