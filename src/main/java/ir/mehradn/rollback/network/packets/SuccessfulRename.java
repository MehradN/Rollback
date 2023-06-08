package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class SuccessfulRename implements FabricPacket {
    public static final PacketType<SuccessfulRename> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "successful_rename"),
        SuccessfulRename::new
    );
    public final int backupId;
    public final BackupType type;

    public SuccessfulRename(int backupId, BackupType type) {
        this.backupId = backupId;
        this.type = type;
    }

    public SuccessfulRename(FriendlyByteBuf buf) {
        this.backupId = buf.readInt();
        this.type = buf.readEnum(BackupType.class);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.backupId);
        buf.writeEnum(this.type);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
