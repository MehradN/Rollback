package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class ConvertBackup implements FabricPacket {
    public static final PacketType<ConvertBackup> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "convert_backup"),
        ConvertBackup::new
    );
    public final int lastUpdateId;
    public final int backupId;
    public final BackupType from;
    public final BackupType to;

    public ConvertBackup(int lastUpdateId, int backupId, BackupType from, BackupType to) {
        this.lastUpdateId = lastUpdateId;
        this.backupId = backupId;
        this.from = from;
        this.to = to;
    }

    public ConvertBackup(FriendlyByteBuf buf) {
        this.lastUpdateId = buf.readInt();
        this.backupId = buf.readInt();
        this.from = buf.readEnum(BackupType.class);
        this.to = buf.readEnum(BackupType.class);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.lastUpdateId);
        buf.writeInt(this.backupId);
        buf.writeEnum(this.from);
        buf.writeEnum(this.to);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
