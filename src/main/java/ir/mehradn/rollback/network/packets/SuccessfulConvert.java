package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class SuccessfulConvert implements FabricPacket {
    public static final PacketType<SuccessfulConvert> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "successful_convert"),
        SuccessfulConvert::new
    );
    public final int backupId;
    public final BackupType from;
    public final BackupType to;

    public SuccessfulConvert(int backupId, BackupType from, BackupType to) {
        this.backupId = backupId;
        this.from = from;
        this.to = to;
    }

    public SuccessfulConvert(FriendlyByteBuf buf) {
        this.backupId = buf.readInt();
        this.from = buf.readEnum(BackupType.class);
        this.to = buf.readEnum(BackupType.class);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.backupId);
        buf.writeEnum(this.from);
        buf.writeEnum(this.to);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
