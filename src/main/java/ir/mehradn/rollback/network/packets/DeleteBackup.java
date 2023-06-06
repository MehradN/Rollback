package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public final class DeleteBackup extends Packet<DeleteBackup.Arguments, DeleteBackup.Arguments> {
    DeleteBackup() {
        super("delete_backup");
    }

    @Override
    public FriendlyByteBuf toBuf(Arguments data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.lastChangeId);
        buf.writeInt(data.backupID);
        buf.writeEnum(data.type);
        return buf;
    }

    @Override
    public Arguments fromBuf(FriendlyByteBuf buf) {
        int changeId = buf.readInt();
        int backupId = buf.readInt();
        BackupType type = buf.readEnum(BackupType.class);
        return new Arguments(changeId, backupId, type);
    }

    public record Arguments(int lastChangeId, int backupID, BackupType type) { }
}
