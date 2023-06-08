package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class SuccessfulDelete implements FabricPacket {
    public static final PacketType<SuccessfulDelete> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "successful_delete"),
        SuccessfulDelete::new
    );
    public final int backupId;
    public final BackupType type;

    public SuccessfulDelete(int backupId, BackupType type) {
        this.backupId = backupId;
        this.type = type;
    }

    public SuccessfulDelete(FriendlyByteBuf buf) {
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
