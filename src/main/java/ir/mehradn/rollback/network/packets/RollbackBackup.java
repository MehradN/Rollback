package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class RollbackBackup implements FabricPacket {
    public static final PacketType<RollbackBackup> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "rollback_backup"),
        RollbackBackup::new
    );
    public final int lastUpdateId;
    public final int backupId;
    public final BackupType type;

    public RollbackBackup(int lastUpdateId, int backupId, BackupType type) {
        this.lastUpdateId = lastUpdateId;
        this.backupId = backupId;
        this.type = type;
    }

    public RollbackBackup(FriendlyByteBuf buf) {
        this.lastUpdateId = buf.readInt();
        this.backupId = buf.readInt();
        this.type = buf.readEnum(BackupType.class);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.lastUpdateId);
        buf.writeInt(this.backupId);
        buf.writeEnum(this.type);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
