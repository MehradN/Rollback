package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public final class RenameBackup extends Packet<RenameBackup.Arguments, RenameBackup.Arguments> {
    RenameBackup() {
        super("rename_backup");
    }

    @Override
    public FriendlyByteBuf toBuf(Arguments data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.lastChangeId);
        buf.writeInt(data.backupID);
        buf.writeEnum(data.type);
        Packets.writeString(buf, data.name);
        return buf;
    }

    @Override
    public Arguments fromBuf(FriendlyByteBuf buf) {
        int changeId = buf.readInt();
        int backupId = buf.readInt();
        BackupType type = buf.readEnum(BackupType.class);
        String name = Packets.readString(buf);
        return new Arguments(changeId, backupId, type, name);
    }

    public record Arguments(int lastChangeId, int backupID, BackupType type, String name) { }
}
