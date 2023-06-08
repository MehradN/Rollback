package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class SuccessfulBackup implements FabricPacket {
    public static final PacketType<SuccessfulBackup> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "successful_backup"),
        SuccessfulBackup::new
    );
    public final BackupType type;
    public final long fileSize;

    public SuccessfulBackup(BackupType type, long fileSize) {
        this.type = type;
        this.fileSize = fileSize;
    }

    public SuccessfulBackup(FriendlyByteBuf buf) {
        this.type = buf.readEnum(BackupType.class);
        this.fileSize = buf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.type);
        buf.writeLong(this.fileSize);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
