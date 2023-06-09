package ir.mehradn.rollback.network.packets;

import ir.mehradn.rollback.Rollback;
import ir.mehradn.rollback.rollback.BackupType;
import ir.mehradn.rollback.util.Utils;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.nio.file.Path;

public final class TakeScreenshot implements FabricPacket {
    public static final PacketType<TakeScreenshot> TYPE = PacketType.create(
        new ResourceLocation(Rollback.MOD_ID, "take_screenshot"),
        TakeScreenshot::new
    );
    public final int backupId;
    public final BackupType backupType;
    public final Path iconPath;

    public TakeScreenshot(int backupId, BackupType type, Path iconPath) {
        this.backupId = backupId;
        this.backupType = type;
        this.iconPath = iconPath;
    }

    public TakeScreenshot(FriendlyByteBuf buf) {
        this.backupId = buf.readInt();
        this.backupType = buf.readEnum(BackupType.class);
        this.iconPath = Path.of(Utils.readString(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.backupId);
        buf.writeEnum(this.backupType);
        Utils.writeString(buf, this.iconPath.toString());
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
