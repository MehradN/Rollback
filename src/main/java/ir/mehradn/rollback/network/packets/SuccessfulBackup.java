package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.io.FileUtils;

public class SuccessfulBackup extends Packet<SuccessfulBackup.Info, SuccessfulBackup.Info> {
    SuccessfulBackup() {
        super("successful_backup");
    }

    @Override
    public FriendlyByteBuf toBuf(Info data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeEnum(data.type);
        buf.writeLong(data.size);
        return buf;
    }

    @Override public Info fromBuf(FriendlyByteBuf buf) {
        BackupType type = buf.readEnum(BackupType.class);
        long size = buf.readLong();
        return new Info(type, size);
    }

    public record Info(BackupType type, long size) {
        public String sizeAsString() {
            return FileUtils.byteCountToDisplaySize(this.size);
        }
    }
}
