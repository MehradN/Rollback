package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.util.Utils;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class RenameBackup implements FabricPacket {
    public static final PacketType<RenameBackup> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "rename_backup"),
        RenameBackup::new
    );
    public final int lastUpdateId;
    public final int backupId;
    public final BackupType type;
    public final String name;

    public RenameBackup(int lastUpdateId, int backupId, BackupType type, String name) {
        this.lastUpdateId = lastUpdateId;
        this.backupId = backupId;
        this.type = type;
        this.name = name;
    }

    public RenameBackup(FriendlyByteBuf buf) {
        this.lastUpdateId = buf.readInt();
        this.backupId = buf.readInt();
        this.type = buf.readEnum(BackupType.class);
        this.name = Utils.readString(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.lastUpdateId);
        buf.writeInt(this.backupId);
        buf.writeEnum(this.type);
        Utils.writeString(buf, this.name);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
