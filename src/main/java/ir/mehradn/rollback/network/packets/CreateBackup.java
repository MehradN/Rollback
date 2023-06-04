package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class CreateBackup extends Packet<CreateBackup.Properties, CreateBackup.Properties> {
    CreateBackup() {
        super("create_backup");
    }

    @Override
    public FriendlyByteBuf toBuf(Properties data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.lastChangeId);
        buf.writeEnum(data.type);
        Packets.writeString(buf, data.name);
        return buf;
    }

    @Override
    public Properties fromBuf(FriendlyByteBuf buf) {
        int id = buf.readInt();
        BackupType type = buf.readEnum(BackupType.class);
        String name = Packets.readString(buf);
        return new Properties(id, type, name);
    }

    public record Properties(int lastChangeId, BackupType type, String name) { }
}
