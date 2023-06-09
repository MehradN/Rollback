package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class BackupWarning implements FabricPacket {
    public static final PacketType<BackupWarning> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "backup_warning"),
        BackupWarning::new
    );
    public final int backupCount;
    public final long totalSize;

    public BackupWarning(int backupCount, long totalSize) {
        this.backupCount = backupCount;
        this.totalSize = totalSize;
    }

    public BackupWarning(FriendlyByteBuf buf) {
        this.backupCount = buf.readInt();
        this.totalSize = buf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.backupCount);
        buf.writeLong(this.totalSize);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
