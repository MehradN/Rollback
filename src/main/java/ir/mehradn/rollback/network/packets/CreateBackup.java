package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.util.Utils;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class CreateBackup implements FabricPacket {
    public static final PacketType<CreateBackup> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "create_backup"),
        CreateBackup::new
    );
    public final int lastUpdateId;
    public final BackupType type;
    public final String name;

    public CreateBackup(int lastUpdateId, BackupType type, String name) {
        this.lastUpdateId = lastUpdateId;
        this.type = type;
        this.name = name;
    }

    public CreateBackup(FriendlyByteBuf buf) {
        this.lastUpdateId = buf.readInt();
        this.type = buf.readEnum(BackupType.class);
        this.name = Utils.readString(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.lastUpdateId);
        buf.writeEnum(this.type);
        Utils.writeString(buf, this.name);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
