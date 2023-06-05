package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class ConvertBackup extends Packet<ConvertBackup.Arguments, ConvertBackup.Arguments> {
    ConvertBackup() {
        super("convert_backup");
    }

    @Override
    public FriendlyByteBuf toBuf(Arguments data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.lastChangeId);
        buf.writeInt(data.backupID);
        buf.writeEnum(data.from);
        buf.writeEnum(data.to);
        return buf;
    }

    @Override
    public Arguments fromBuf(FriendlyByteBuf buf) {
        int changeId = buf.readInt();
        int backupId = buf.readInt();
        BackupType from = buf.readEnum(BackupType.class);
        BackupType to = buf.readEnum(BackupType.class);
        return new Arguments(changeId, backupId, from, to);
    }

    public record Arguments(int lastChangeId, int backupID, BackupType from, BackupType to) { }
}
